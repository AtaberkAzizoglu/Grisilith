package com.azizoglu.grisilith.online

/** Monotonic eligibility: a failed/offline/abandoned run can never become eligible again. */
class OnlineRun(val id: String, val startedAtMillis: Long, initiallyOnline: Boolean) {
    enum class State { STARTING, ACTIVE, OFFLINE, SUBMITTING, FINISHED, ABANDONED }
    var state = if (initiallyOnline) State.STARTING else State.OFFLINE
        private set
    var reason: String? = if (initiallyOnline) null else "not_connected_at_start"
        private set
    var lastContactMillis: Long = startedAtMillis
        private set
    var serverScore: Long? = null
        private set
    internal var heartbeatInFlight = false
    internal var nextHeartbeatMillis = startedAtMillis
    val isPreparing get() = state == State.STARTING

    internal fun activate(now: Long): Boolean {
        if (state != State.STARTING) return false
        state = State.ACTIVE
        contact(now)
        return true
    }

    internal fun contact(now: Long) {
        lastContactMillis = now
        nextHeartbeatMillis = now + HEARTBEAT_INTERVAL_MS
    }

    internal fun startSubmission(): Boolean {
        if (state != State.ACTIVE) return false
        state = State.SUBMITTING
        return true
    }

    internal fun accept(score: Long) {
        if (state != State.SUBMITTING) return
        serverScore = score
        state = State.FINISHED
    }

    internal fun invalidate(cause: String) {
        if (state in listOf(State.FINISHED, State.ABANDONED, State.OFFLINE)) return
        reason = cause
        state = State.OFFLINE
    }

    internal fun abandon() {
        if (state in listOf(State.FINISHED, State.OFFLINE, State.ABANDONED, State.SUBMITTING)) return
        state = State.ABANDONED
        reason = "abandoned"
    }

    companion object {
        const val HEARTBEAT_INTERVAL_MS = 10_000L
        const val CONTACT_DEADLINE_MS = 25_000L
        const val START_DEADLINE_MS = 8_000L
    }
}
