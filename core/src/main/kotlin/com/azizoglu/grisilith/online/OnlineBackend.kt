package com.azizoglu.grisilith.online

import com.azizoglu.grisilith.config.GameDifficulty
import com.azizoglu.grisilith.config.GameMap
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.JsonValue
import java.util.UUID

/** Main-thread coordinator. Networking is asynchronous and never affects local progression. */
class OnlineBackend(
    private val config: OnlineConfig,
    private val transport: OnlineTransport,
    private val store: OnlineSessionStore,
    private val monotonicMillis: () -> Long = { System.nanoTime() / 1_000_000L },
    private val epochSeconds: () -> Long = { System.currentTimeMillis() / 1000L }
) {
    enum class Status { DISABLED, CONNECTING, READY, OFFLINE, BACKEND_NOT_READY, IDENTITY_EXPIRED }
    var status = if (config.enabled) Status.OFFLINE else Status.DISABLED
        private set
    var playerId: String? = null
        private set
    var displayName: String? = null
        private set
    var nameConfirmed = false
        private set
    var currentRun: OnlineRun? = null
        private set
    private var session: OnlineSession? = null
    private var retryAt = 0L
    private var closed = false
    private var refreshing = false
    private var nextProbeAt = 0L
    private var probeInFlight = false
    private val refreshWaiters = mutableListOf<(Boolean) -> Unit>()

    fun bootstrap() {
        if (closed || !config.enabled || status in listOf(Status.CONNECTING, Status.READY, Status.IDENTITY_EXPIRED)) return
        status = Status.CONNECTING
        retryAt = monotonicMillis() + 30_000L
        transport.request("/rest/v1/rpc/grisilith_api_status", jsonObject(), null) { result ->
            if (closed) return@request
            val compatible = result.mapCatching { info ->
                info.getInt("api_version", -1) == OnlineProtocol.API_VERSION &&
                    info.getInt("score_version", -1) == OnlineProtocol.SCORE_VERSION &&
                    info.getString("game_version", "") == OnlineProtocol.GAME_VERSION
            }.getOrDefault(false)
            if (!compatible) {
                status = Status.BACKEND_NOT_READY
                return@request
            }
            try { session = store.load() } catch (_: Exception) { status = Status.OFFLINE; return@request }
            if (session == null) {
                authenticate("/auth/v1/signup", jsonObject("data" to emptyMap<String, String>())) { ok -> if (ok) loadProfile() }
            } else if (session!!.expiresAtSeconds <= epochSeconds() + 120) {
                refresh { ok -> if (ok) loadProfile() }
            } else {
                playerId = session!!.playerId
                loadProfile()
            }
        }
    }

    private fun authenticate(path: String, body: JsonValue, callback: (Boolean) -> Unit) {
        transport.request(path, body, null) { result ->
            if (closed) return@request
            val response = result.getOrNull()
            if (response == null) {
                val failure = result.exceptionOrNull() as? OnlineRequestFailure
                status = if (session != null && failure?.status in listOf(400, 401, 403))
                    Status.IDENTITY_EXPIRED else Status.OFFLINE
                callback(false)
                return@request
            }
            val updated = runCatching {
                OnlineSession(response.getString("access_token"), response.getString("refresh_token"),
                    response.getLong("expires_at", epochSeconds() + response.getLong("expires_in", 3600L)),
                    response.get("user").getString("id"))
            }.getOrNull()
            if (updated == null || updated.accessToken.isBlank() || updated.refreshToken.isBlank() ||
                (session != null && session!!.playerId != updated.playerId)) {
                status = Status.IDENTITY_EXPIRED
                callback(false)
                return@request
            }
            // Persist rotated refresh token before exposing the new session to other requests.
            try { store.save(updated) } catch (_: Exception) { status = Status.OFFLINE; callback(false); return@request }
            session = updated
            playerId = updated.playerId
            callback(true)
        }
    }

    private fun refresh(callback: (Boolean) -> Unit) {
        refreshWaiters += callback
        if (refreshing) return
        val old = session
        if (old == null) { refreshWaiters.toList().also { refreshWaiters.clear() }.forEach { it(false) }; return }
        refreshing = true
        authenticate("/auth/v1/token?grant_type=refresh_token", jsonObject("refresh_token" to old.refreshToken)) { ok ->
            refreshing = false
            val callbacks = refreshWaiters.toList()
            refreshWaiters.clear()
            callbacks.forEach { it(ok) }
        }
    }

    private fun rpc(name: String, body: JsonValue, callback: (Result<JsonValue>) -> Unit) {
        if (closed || session == null) { callback(Result.failure(OnlineRequestFailure(0))); return }
        fun send(refreshed: Boolean = false) {
            transport.request("/rest/v1/rpc/grisilith_$name", body, session!!.accessToken) { result ->
                if (closed) return@request
                val failure = result.exceptionOrNull() as? OnlineRequestFailure
                if (failure?.status == 401 && !refreshed) {
                    refresh { ok ->
                        if (ok) send(true) else callback(Result.failure(OnlineRequestFailure(401)))
                    }
                } else {
                    if (failure?.status == 0) {
                        status = Status.OFFLINE
                        retryAt = monotonicMillis() + 30_000L
                    } else if (failure?.status == 401) status = Status.IDENTITY_EXPIRED
                    callback(result)
                }
            }
        }
        if (session!!.expiresAtSeconds <= epochSeconds() + 120) refresh { ok ->
            if (ok) send() else callback(Result.failure(OnlineRequestFailure(401)))
        } else send()
    }

    private fun loadProfile() {
        rpc("profile", jsonObject()) { result ->
            val valid = result.mapCatching { profile ->
                check(profile.getString("player_id", "") == session?.playerId)
                nameConfirmed = profile.getBoolean("name_confirmed", false)
                profile.getString("display_name", null)
            }
            if (valid.isFailure) {
                if (status != Status.IDENTITY_EXPIRED) status = Status.OFFLINE
            } else {
                displayName = valid.getOrNull()
                status = Status.READY
                nextProbeAt = monotonicMillis() + 15_000L
            }
        }
    }

    fun setDisplayName(name: String, callback: (Result<String>) -> Unit) {
        if ((!OnlineProtocol.isValidDisplayName(name) && name != playerId && name != playerId?.let(PlayerNamePolicy::uppercase)) || status != Status.READY) {
            callback(Result.failure(IllegalArgumentException("Invalid name or offline"))); return
        }
        rpc("profile", jsonObject("p_display_name" to PlayerNamePolicy.uppercase(name).trim(' '))) { result ->
            callback(result.mapCatching { value -> value.getString("display_name").also {
                displayName = it
                nameConfirmed = value.getBoolean("name_confirmed", true)
            } })
        }
    }

    fun leaderboardAll(callback: (Result<JsonValue>) -> Unit) {
        if (status != Status.READY) { callback(Result.failure(OnlineRequestFailure(0))); return }
        rpc("leaderboard_all", jsonObject(), callback)
    }

    fun praise(runId: String, callback: (Result<JsonValue>) -> Unit) {
        if(status!=Status.READY || !runId.matches(Regex("[0-9a-fA-F-]{36}"))) {
            callback(Result.failure(IllegalStateException("Not connected or invalid run"))); return
        }
        rpc("praise",jsonObject("p_run_id" to runId),callback)
    }

    fun nameAvailable(name: String, callback: (Result<Boolean>) -> Unit) {
        if(status != Status.READY) {callback(Result.failure(OnlineRequestFailure(0)));return}
        rpc("name_available",jsonObject("p_name" to PlayerNamePolicy.uppercase(name).trim(' '))) { result ->
            callback(result.mapCatching { it.getBoolean("available") })
        }
    }

    fun leaderboard(map: GameMap, callback: (Result<JsonValue>) -> Unit) {
        if (status != Status.READY || map == GameMap.TEST_MAP) {
            callback(Result.failure(IllegalStateException("Leaderboard unavailable"))); return
        }
        rpc("leaderboard", jsonObject("p_map_id" to map.name), callback)
    }

    fun begin(map: GameMap, difficulty: GameDifficulty, characterId: String, startingBuild: JsonValue): OnlineRun {
        currentRun?.let(::abandon)
        val run = OnlineRun(UUID.randomUUID().toString(), monotonicMillis(),
            !closed && status == Status.READY && map != GameMap.TEST_MAP)
        currentRun = run
        if (!run.isPreparing) return run // Never retry/join halfway through an offline run.
        rpc("begin_run", jsonObject("p_run_id" to run.id, "p_map_id" to map.name,
            "p_difficulty" to difficulty.name, "p_character_id" to characterId,
            "p_game_version" to OnlineProtocol.GAME_VERSION, "p_starting_build" to startingBuild)) { result ->
            val eligible = result.mapCatching { receipt ->
                receipt.getBoolean("eligible", false) && receipt.getString("run_id", "") == run.id
            }.getOrDefault(false)
            if (eligible &&
                monotonicMillis() - run.startedAtMillis < OnlineRun.START_DEADLINE_MS && run.activate(monotonicMillis())) {
                return@rpc
            }
            run.invalidate("online_start_failed")
            invalidateServerRun(run)
        }
        return run
    }

    fun update() {
        if (closed || !config.enabled) return
        val now = monotonicMillis()
        val run = currentRun
        if (status == Status.READY && run?.state != OnlineRun.State.ACTIVE && now >= nextProbeAt && !probeInFlight) {
            probeInFlight = true
            nextProbeAt = now + 15_000L
            rpc("profile", jsonObject()) { response ->
                probeInFlight = false
                if (response.isSuccess) {
                    val profile = response.getOrThrow()
                    displayName = profile.getString("display_name", displayName)
                    nameConfirmed = profile.getBoolean("name_confirmed", nameConfirmed)
                } else if (status != Status.IDENTITY_EXPIRED) {
                    status = Status.OFFLINE
                    retryAt = monotonicMillis() + 30_000L
                }
            }
        }
        if (run?.isPreparing == true && now - run.startedAtMillis >= OnlineRun.START_DEADLINE_MS) {
            run.invalidate("online_start_timeout")
        }
        if (run?.state == OnlineRun.State.ACTIVE) {
            if (now - run.lastContactMillis >= OnlineRun.CONTACT_DEADLINE_MS) {
                run.invalidate("connection_lost")
                invalidateServerRun(run)
            } else if (now >= run.nextHeartbeatMillis && !run.heartbeatInFlight) {
                run.heartbeatInFlight = true
                rpc("heartbeat", jsonObject("p_run_id" to run.id)) { response ->
                    run.heartbeatInFlight = false
                    if (run.state in listOf(OnlineRun.State.ACTIVE, OnlineRun.State.SUBMITTING)) {
                        if (response.mapCatching { it.getBoolean("eligible", false) }.getOrDefault(false) &&
                            monotonicMillis() - run.lastContactMillis < OnlineRun.CONTACT_DEADLINE_MS) run.contact(monotonicMillis())
                        else {
                            run.invalidate("connection_lost")
                            invalidateServerRun(run)
                        }
                    }
                }
            }
        }
        if (status in listOf(Status.OFFLINE, Status.BACKEND_NOT_READY) && now >= retryAt) bootstrap()
    }

    fun finish(run: OnlineRun, result: JsonValue) {
        if (monotonicMillis() - run.lastContactMillis >= OnlineRun.CONTACT_DEADLINE_MS) run.invalidate("connection_lost")
        if (!run.startSubmission()) return
        rpc("finish_run", jsonObject("p_run_id" to run.id, "p_result" to result)) { response ->
            val score = response.mapCatching { receipt ->
                check(receipt.getBoolean("accepted", false))
                receipt.getLong("score").also { check(it >= 0L) }
            }.getOrNull()
            if (score != null) run.accept(score) else run.invalidate("submission_failed")
        }
    }

    fun abandon(run: OnlineRun) {
        val wasLive = run.state in listOf(OnlineRun.State.STARTING, OnlineRun.State.ACTIVE)
        run.abandon()
        if (wasLive) invalidateServerRun(run)
    }

    private fun invalidateServerRun(run: OnlineRun) {
        if (!closed && session != null) rpc("heartbeat", jsonObject("p_run_id" to run.id, "p_abandon" to true)) { }
    }

    fun close() {
        currentRun?.let(::abandon)
        closed = true
        refreshWaiters.clear()
    }

    companion object {
        fun create(): OnlineBackend {
            val config = OnlineConfig.grisilith()
            return OnlineBackend(config, SupabaseTransport(config),
                PreferencesOnlineSessionStore(Gdx.app.getPreferences("grisilith_online_session")))
        }
    }
}
