package qa

import com.azizoglu.grisilith.online.*
import com.azizoglu.grisilith.config.*
import com.badlogic.gdx.utils.JsonReader
import com.badlogic.gdx.utils.JsonValue

object OnlineBackendSmoke {
    private fun json(text: String) = JsonReader().parse(text)
    private class Store : OnlineSessionStore {
        var value: OnlineSession? = null
        var writes = 0
        override fun load() = value
        override fun save(session: OnlineSession) { value = session; writes++ }
    }
    private class Fake : OnlineTransport {
        data class Request(val path: String, val body: JsonValue, val token: String?, val callback: (Result<JsonValue>) -> Unit)
        val pending = mutableListOf<Request>()
        override fun request(path: String, body: JsonValue, accessToken: String?, callback: (Result<JsonValue>) -> Unit) {
            pending += Request(path, body, accessToken, callback)
        }
        fun take(suffix: String): Request {
            val index = pending.indexOfFirst { it.path.endsWith(suffix) }
            check(index >= 0) { "Missing $suffix; queued ${pending.map { it.path }}" }
            return pending.removeAt(index)
        }
        fun reply(suffix: String, text: String) = take(suffix).callback(Result.success(json(text)))
        fun fail(suffix: String, status: Int = 0) = take(suffix).callback(Result.failure(OnlineRequestFailure(status)))
    }
    private class Rig(val enabled: Boolean = true) {
        var now = 0L
        var epoch = 1000L
        val store = Store()
        val transport = Fake()
        val backend = OnlineBackend(OnlineConfig("https://test.supabase.co", "sb_publishable_test", enabled),
            transport, store, { now }, { epoch })
        fun ready(existing: Boolean = false) {
            if (existing) store.value = OnlineSession("access", "refresh", epoch + 3600, "player-a")
            backend.bootstrap()
            transport.reply("grisilith_api_status", """{"api_version":1,"score_version":1,"game_version":"0.3"}""")
            if (!existing) transport.reply("signup", """{"access_token":"access","refresh_token":"refresh","expires_at":4600,"user":{"id":"player-a"}}""")
            transport.reply("grisilith_profile", """{"player_id":"player-a","display_name":null}""")
            check(backend.status == OnlineBackend.Status.READY)
        }
        fun begin(map: GameMap = GameMap.ROAD_TO_HOME) = backend.begin(map, GameDifficulty.HARD, "CHR_KOBA", json("{}"))
        fun activate(run: OnlineRun) {
            transport.reply("grisilith_begin_run", """{"run_id":"${run.id}","eligible":true}""")
            check(run.state == OnlineRun.State.ACTIVE)
        }
    }

    @JvmStatic fun main(args: Array<String>) {
        check(OnlineProtocol.isValidDisplayName("Player_123"))
        check(!OnlineProtocol.isValidDisplayName("ab") && !OnlineProtocol.isValidDisplayName("<script>"))
        check(!OnlineProtocol.isValidDisplayName("a".repeat(25)))
        check(OnlineSession("secret-access", "secret-refresh", 1, "a").toString() == "OnlineSession(redacted)")

        // No schema => no auth side effect, no client-side identity churn.
        Rig().apply {
            backend.bootstrap(); transport.fail("grisilith_api_status", 404)
            check(backend.status == OnlineBackend.Status.BACKEND_NOT_READY && transport.pending.isEmpty())
            val offline = begin()
            check(offline.state == OnlineRun.State.OFFLINE)
            now = 31_000; ready()
            backend.finish(offline, json("{}"))
            check(transport.pending.isEmpty() && offline.state == OnlineRun.State.OFFLINE)
        }
        Rig(false).apply { backend.bootstrap(); backend.update(); begin(); check(transport.pending.isEmpty()) }
        Rig().apply { ready(); check(store.writes == 1 && backend.playerId == "player-a") }
        Rig().apply { ready(existing = true); check(store.writes == 0) }
        Rig().apply {
            ready(existing = true)
            backend.leaderboard(GameMap.ROAD_TO_HOME) { check(it.isSuccess) }
            transport.fail("grisilith_leaderboard",401)
            transport.reply("token?grant_type=refresh_token", """{"access_token":"renewed","refresh_token":"rotated","expires_at":8100,"user":{"id":"player-a"}}""")
            check(transport.take("grisilith_leaderboard").also { check(it.token == "renewed") }.let {
                it.callback(Result.success(json("[]"))); true
            })
            check(store.value!!.playerId == "player-a")
        }
        Rig().apply {
            backend.bootstrap(); transport.reply("grisilith_api_status", """{"api_version":"not-a-number"}""")
            check(backend.status == OnlineBackend.Status.BACKEND_NOT_READY)
        }

        // Test Map never starts/submits an online run.
        Rig().apply {
            ready(); val run = begin(GameMap.TEST_MAP); backend.finish(run, json("{}"))
            check(run.state == OnlineRun.State.OFFLINE && transport.pending.isEmpty())
        }
        Rig().apply {
            ready(); val run = begin(); check(run.isPreparing); activate(run)
            now = 10_000; backend.update()
            check(transport.take("grisilith_heartbeat").also { check(it.token == "access") }.let {
                it.callback(Result.success(json("""{"eligible":true}"""))); true
            })
            backend.finish(run, json("{}")); backend.finish(run, json("{}"))
            check(transport.pending.count { it.path.endsWith("finish_run") } == 1)
            transport.reply("grisilith_finish_run", """{"accepted":true,"score":14186}""")
            check(run.state == OnlineRun.State.FINISHED && run.serverScore == 14186L)
            backend.abandon(run); check(transport.pending.isEmpty())
        }
        // A failed heartbeat is terminal, even after reconnect.
        Rig().apply {
            ready(); val run = begin(); activate(run)
            now = 10_000; backend.update(); transport.fail("grisilith_heartbeat")
            check(run.state == OnlineRun.State.OFFLINE)
            transport.reply("grisilith_heartbeat", """{"eligible":false}""")
            now = 100_000; backend.update(); backend.finish(run, json("{}"))
            check(transport.pending.none { it.path.endsWith("finish_run") })
        }
        // No heartbeat can revive a timed-out session; stale begin replies cannot activate it.
        Rig().apply {
            ready(); val run = begin(); now = 8001; backend.update()
            check(run.state == OnlineRun.State.OFFLINE)
            transport.reply("grisilith_begin_run", """{"run_id":"${run.id}","eligible":true}""")
            check(run.state == OnlineRun.State.OFFLINE)
        }
        Rig().apply {
            ready(); val run = begin(); activate(run)
            now = 10_000; backend.update()
            val delayed = transport.take("grisilith_heartbeat")
            now = 26_000; backend.update()
            delayed.callback(Result.success(json("""{"eligible":true}""")))
            check(run.state == OnlineRun.State.OFFLINE)
        }
        Rig().apply {
            ready(); val run = begin(); backend.abandon(run)
            transport.reply("grisilith_begin_run", """{"run_id":"${run.id}","eligible":true}""")
            check(run.state == OnlineRun.State.ABANDONED)
        }
        Rig().apply {
            ready(); val old = begin(); val replacement = begin()
            check(old.state == OnlineRun.State.ABANDONED && replacement.isPreparing)
        }
        // Expired credentials refresh, never create a replacement identity.
        Rig().apply {
            store.value = OnlineSession("old", "refresh", 900, "player-a")
            backend.bootstrap()
            transport.reply("grisilith_api_status", """{"api_version":1,"score_version":1,"game_version":"0.3"}""")
            transport.fail("token?grant_type=refresh_token", 400)
            check(backend.status == OnlineBackend.Status.IDENTITY_EXPIRED && store.value!!.playerId == "player-a")
            now = 100_000; backend.update(); check(transport.pending.isEmpty())
        }
        // Concurrent requests share a single refresh; refresh token is rotated persistently.
        Rig().apply {
            ready(existing = true); epoch = 4500
            backend.leaderboard(GameMap.ROAD_TO_HOME) { check(it.isSuccess) }
            backend.setDisplayName("New_Name") { check(it.getOrNull() == "New_Name") }
            check(transport.pending.count { it.path.contains("grant_type") } == 1)
            transport.reply("token?grant_type=refresh_token", """{"access_token":"new","refresh_token":"rotated","expires_at":8100,"user":{"id":"player-a"}}""")
            check(store.value!!.refreshToken == "rotated")
            transport.reply("grisilith_leaderboard", "[]")
            transport.reply("grisilith_profile", """{"display_name":"New_Name"}""")
        }
        Rig().apply {
            backend.bootstrap(); backend.close()
            transport.reply("grisilith_api_status", """{"api_version":1,"score_version":1,"game_version":"0.3"}""")
            check(store.writes == 0 && transport.pending.isEmpty())
        }
        println("ONLINE_BACKEND_STATE_SMOKE_PASS")
    }
}
