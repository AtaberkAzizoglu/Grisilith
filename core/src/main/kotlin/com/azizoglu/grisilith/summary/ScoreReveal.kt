package com.azizoglu.grisilith.summary

/** Pure presentation timeline. Never writes score or career statistics. */
object ScoreReveal {
    const val DELAY_SECONDS=2f
    const val COUNT_SECONDS=2f
    const val TOTAL_SECONDS=DELAY_SECONDS+COUNT_SECONDS
    data class Frame(val value: Long, val progress: Float, val multiplierAlpha: Float, val pulse: Float)

    fun sample(start: Long, target: Long, elapsed: Float): Frame {
        val from=start.coerceAtLeast(0L);val to=target.coerceAtLeast(from)
        val time=if(elapsed.isNaN())0f else elapsed.coerceAtLeast(0f)
        val progress=((time-DELAY_SECONDS)/COUNT_SECONDS).coerceIn(0f,1f)
        val eased=1.0-(1.0-progress)*(1.0-progress)*(1.0-progress)
        val value=when {
            progress<=0f -> from
            progress>=1f -> to // Exact end value even for Long.MAX_VALUE.
            else -> (from.toDouble()+(to-from).toDouble()*eased).toLong().coerceIn(from,to)
        }
        val fade=((time-DELAY_SECONDS)/.3f).coerceIn(0f,1f)
        val pulse=if(time>=DELAY_SECONDS && time<DELAY_SECONDS+.5f)
            (1f-(time-DELAY_SECONDS)/.5f) else 0f
        return Frame(value,progress,fade,pulse)
    }
}
