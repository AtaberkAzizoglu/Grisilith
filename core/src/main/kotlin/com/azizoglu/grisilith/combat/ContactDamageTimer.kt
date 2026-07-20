package com.azizoglu.grisilith.combat

class ContactDamageTimer(
    hitsPerSecond: Float
) {
    private val hitInterval: Float

    private var elapsedTime = 0f
    private var wasTouching = false

    init {
        require(hitsPerSecond > 0f) {
            "Hits per second must be greater than zero."
        }

        hitInterval = 1f / hitsPerSecond
    }

    fun update(
        delta: Float,
        isTouching: Boolean,
        onHit: () -> Unit
    ) {
        if (!isTouching) {
            elapsedTime = 0f
            wasTouching = false
            return
        }

        /*
         * Temasın başladığı ilk anda hasar uygular.
         */
        if (!wasTouching) {
            wasTouching = true
            elapsedTime = 0f
            onHit()
            return
        }

        elapsedTime += delta

        /*
         * FPS düşse bile kaçırılan hasar vuruşlarını telafi eder.
         */
        while (elapsedTime >= hitInterval) {
            elapsedTime -= hitInterval
            onHit()
        }
    }
}
