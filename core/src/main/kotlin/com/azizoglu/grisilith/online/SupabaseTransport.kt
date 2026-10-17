package com.azizoglu.grisilith.online

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Net
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.utils.JsonReader
import com.badlogic.gdx.utils.JsonValue

data class OnlineConfig(val url: String, val publishableKey: String, val enabled: Boolean = true) {
    init {
        require(url.matches(Regex("https://[a-z0-9-]+\\.supabase\\.co")))
        require(publishableKey.startsWith("sb_publishable_"))
    }

    companion object {
        /** These are public application identifiers, NEVER an admin/service-role key. */
        fun grisilith() = OnlineConfig(
            "https://wadrsdretczaoarjduko.supabase.co",
            "sb_publishable_9KkX_OJA88ez_UdJ1WGoqg_9i6CjbkF",
            System.getProperty("grisilith.online.disabled") != "true"
        )
    }
}

/** toString deliberately omits tokens; session files must never be included in diagnostics. */
class OnlineSession(val accessToken: String, val refreshToken: String, val expiresAtSeconds: Long, val playerId: String) {
    override fun toString() = "OnlineSession(redacted)"
}

interface OnlineSessionStore {
    fun load(): OnlineSession?
    fun save(session: OnlineSession)
}

class PreferencesOnlineSessionStore(private val preferences: Preferences) : OnlineSessionStore {
    override fun load(): OnlineSession? {
        val access = preferences.getString("access_token", "")
        val refresh = preferences.getString("refresh_token", "")
        val id = preferences.getString("player_id", "")
        check(listOf(access, refresh, id).all { it.isBlank() } || listOf(access, refresh, id).all { it.isNotBlank() }) {
            "Incomplete online session; refusing to replace player identity"
        }
        return if (access.isBlank() || refresh.isBlank() || id.isBlank()) null else
            OnlineSession(access, refresh, preferences.getLong("expires_at", 0L), id)
    }

    override fun save(session: OnlineSession) {
        preferences.putString("access_token", session.accessToken)
            .putString("refresh_token", session.refreshToken)
            .putString("player_id", session.playerId)
            .putLong("expires_at", session.expiresAtSeconds).flush()
    }
}

class OnlineRequestFailure(val status: Int, val reason: String? = null) : RuntimeException("Online request failed ($status)")

interface OnlineTransport {
    /** Callback is delivered on the game thread. No retries and no offline submission queue. */
    fun request(path: String, body: JsonValue, accessToken: String?, callback: (Result<JsonValue>) -> Unit)
}

class SupabaseTransport(private val config: OnlineConfig) : OnlineTransport {
    override fun request(path: String, body: JsonValue, accessToken: String?, callback: (Result<JsonValue>) -> Unit) {
        require(path.startsWith("/auth/v1/") || path.startsWith("/rest/v1/rpc/grisilith_"))
        val request = Net.HttpRequest(Net.HttpMethods.POST).apply {
            url = config.url + path
            timeOut = 6000
            setHeader("apikey", config.publishableKey)
            setHeader("Content-Type", "application/json")
            accessToken?.let { setHeader("Authorization", "Bearer $it") }
            content = body.wire()
            followRedirects = false
        }
        fun deliver(result: Result<JsonValue>) { Gdx.app.postRunnable { callback(result) } }
        try {
            Gdx.net.sendHttpRequest(request, object : Net.HttpResponseListener {
                override fun handleHttpResponse(response: Net.HttpResponse) {
                    val code = response.status.statusCode
                    if (code !in 200..299) {
                        val reason = runCatching {
                            val text = response.resultAsString
                            if (text.length > 8192) null else JsonReader().parse(text).getString("message", null)
                        }.getOrNull()?.takeIf { it in setOf("display_name_taken", "invalid_display_name", "blocked_display_name") }
                        deliver(Result.failure(OnlineRequestFailure(code, reason))); return
                    }
                    deliver(runCatching {
                        val text = response.resultAsString
                        require(text.length <= 262144) { "Online response too large" }
                        JsonReader().parse(text)
                    })
                }
                override fun failed(t: Throwable) = deliver(Result.failure(OnlineRequestFailure(0)))
                override fun cancelled() = deliver(Result.failure(OnlineRequestFailure(0)))
            })
        } catch (_: Exception) { deliver(Result.failure(OnlineRequestFailure(0))) }
    }
}
