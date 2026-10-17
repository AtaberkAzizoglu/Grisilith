package qa

import com.azizoglu.grisilith.GrisilithGame
import com.azizoglu.grisilith.character.DefaultCharacters
import com.azizoglu.grisilith.config.*
import com.azizoglu.grisilith.enemy.EnemyType
import com.azizoglu.grisilith.online.*
import com.azizoglu.grisilith.screen.FirstScreen
import com.azizoglu.grisilith.stage.StageController
import com.azizoglu.grisilith.statistics.RunStatistics
import com.badlogic.gdx.*
import com.badlogic.gdx.backends.lwjgl3.*
import com.badlogic.gdx.utils.JsonReader
import com.badlogic.gdx.utils.JsonValue

/** Hidden-window integration test; fake transport, isolated local preferences, no hosted writes. */
object OnlineScreenSmoke {
    private fun field(target: Any, name: String) = target.javaClass.getDeclaredField(name).apply { isAccessible = true }
    private class Transport : OnlineTransport {
        val requests = mutableListOf<Triple<String, JsonValue, (Result<JsonValue>) -> Unit>>()
        override fun request(path: String, body: JsonValue, accessToken: String?, callback: (Result<JsonValue>) -> Unit) {
            requests += Triple(path, body, callback)
        }
        fun reply(suffix: String, response: String): JsonValue {
            val request = requests.removeAt(requests.indexOfFirst { it.first.endsWith(suffix) }.also { check(it >= 0) })
            request.third(Result.success(JsonReader().parse(response)))
            return request.second
        }
    }

    @JvmStatic fun main(args: Array<String>) {
        Lwjgl3Application(object : ApplicationAdapter() {
            override fun create() {
                try { checks(); Gdx.app.exit() }
                catch (t: Throwable) { t.printStackTrace(); kotlin.system.exitProcess(1) }
            }
        }, Lwjgl3ApplicationConfiguration().apply {
            setWindowedMode(1280,720); setInitialVisible(false); disableAudio(true)
            setPreferencesConfig("../tools/online-backend/test-prefs", Files.FileType.Local)
        })
    }

    private fun checks() {
        var now = 0L
        val transport = Transport()
        val backend = OnlineBackend(OnlineConfig("https://test.supabase.co","sb_publishable_test"), transport,
            object : OnlineSessionStore {
                override fun load() = OnlineSession("access","refresh",9000,"player-a")
                override fun save(session: OnlineSession) = Unit
            }, { now }, { 1000 })
        backend.bootstrap()
        transport.reply("grisilith_api_status", """{"api_version":1,"score_version":1,"game_version":"0.3"}""")
        transport.reply("grisilith_profile", """{"player_id":"player-a","display_name":null}""")
        val game = GrisilithGame()
        field(game,"online").set(game,backend)
        val screen = FirstScreen(game,GameDifficulty.HARD,GameMap.ROAD_TO_HOME,DefaultCharacters.koba)
        screen.resize(1280,720)
        screen.show()
        val onlineRun = field(screen,"onlineRun").get(screen) as OnlineRun
        check(onlineRun.isPreparing)
        val clock = field(screen,"gameTimeController").get(screen) as com.azizoglu.grisilith.util.GameTimeController
        screen.render(.1f)
        check(clock.elapsedGameTimeSeconds == 0f) // No combat before server begins the session.
        val start = transport.reply("grisilith_begin_run", """{"run_id":"${onlineRun.id}","eligible":true}""")
        check(start.get("p_starting_build").get("weapons").get(0).getString("id") == "WPN_LONGBOW")
        check(!start.toString().contains("description"))
        screen.render(.1f)
        check(clock.elapsedGameTimeSeconds > 0f)
        val hudPreview=com.badlogic.gdx.graphics.Pixmap.createFromFrameBuffer(0,0,1280,720)
        com.badlogic.gdx.graphics.PixmapIO.writePNG(Gdx.files.local("../tools/online-backend/phase8c-hud.png"),hudPreview,-1,true)
        hudPreview.dispose()
        val stats = field(screen,"runStatistics").get(screen) as RunStatistics
        stats.recordEnemyKilled(EnemyType.NORMAL)
        stats.recordEnemyKilled(EnemyType.FINAL_BOSS)
        val stages = field(screen,"stageController").get(screen) as StageController
        field(stages,"currentStage").setInt(stages,stages.maximumStage + 1)
        screen.render(.1f)
        screen.render(.1f)
        check(transport.requests.count { it.first.endsWith("finish_run") } == 1)
        val result = transport.reply("grisilith_finish_run", """{"accepted":true,"score":30006}""").get("p_result")
        check(result.getInt("normal_kills") == 1 && result.getInt("final_boss_kills") == 1)
        check(result.getInt("stage_reached") == 50 && result.getBoolean("was_successful"))
        check(result.get("final_build").get("weapons").size == 1)
        check(result.get("summary").get("damage_dealt_by_type").isObject)
        check(!result.has("score")) // Server, not client, determines the ranking score.
        check(onlineRun.serverScore == 30006L)
        screen.dispose()
        check(transport.requests.isEmpty())

        // A startup timeout resumes local play without retrying or uploading that run.
        val fallback = FirstScreen(game,GameDifficulty.HARD,GameMap.ROAD_TO_HOME,DefaultCharacters.koba)
        fallback.show()
        val fallbackRun = field(fallback,"onlineRun").get(fallback) as OnlineRun
        check(fallbackRun.isPreparing)
        now = 8001; backend.update()
        fallback.render(.1f)
        val fallbackClock = field(fallback,"gameTimeController").get(fallback) as com.azizoglu.grisilith.util.GameTimeController
        check(fallbackRun.state == OnlineRun.State.OFFLINE && fallbackClock.elapsedGameTimeSeconds > 0f)
        fallback.dispose(); backend.close()
        println("ONLINE_FIRST_SCREEN_SMOKE_PASS")
    }
}
