package com.azizoglu.grisilith.map

import com.azizoglu.grisilith.combat.HealthComponent
import com.azizoglu.grisilith.config.GameMap
import com.azizoglu.grisilith.enemy.EnemyStats
import com.azizoglu.grisilith.summon.SummonManager
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import kotlin.math.roundToInt

class TroubleAtHomeDefenseRuntime(
    gameMap: GameMap,
    obstacles: List<MapObstacle>
) {

    val isEnabled =
        gameMap == GameMap.TROUBLE_AT_HOME

    private val gateObstacle =
        obstacles.firstOrNull {
            it.type == MapObstacleType.CITY_GATE
        }

    val gateHealth =
        HealthComponent(GATE_MAXIMUM_HEALTH)

    private val replacementDueStages =
        mutableListOf<Int>()

    private var observedStage =
        -1

    private var pendingGuardSpawns =
        0

    private var animationSeconds =
        0f

    private var animationActive =
        false

    private var guardSpawnedDuringAnimation =
        false

    private var introMessageRemainingSeconds =
        if (isEnabled) INTRO_MESSAGE_DURATION_SECONDS else 0f

    val targetBounds: Rectangle?
        get() =
            if (isEnabled && !gateHealth.isDead) {
                gateObstacle?.bounds
            } else {
                null
            }

    val targetCenterX: Float
        get() =
            targetBounds?.let { it.x + it.width / 2f }
                ?: 0f

    val targetCenterY: Float
        get() =
            targetBounds?.let { it.y + it.height / 2f }
                ?: 0f

    fun update(
        delta: Float,
        currentStage: Int,
        summonManager: SummonManager,
        guardProfileProvider: (Int) -> Pair<String, EnemyStats>?
    ) {
        introMessageRemainingSeconds =
            (introMessageRemainingSeconds - delta.coerceAtLeast(0f))
                .coerceAtLeast(0f)

        if (!isEnabled || gateHealth.isDead) {
            gateObstacle?.visualVariant = 0
            return
        }

        repeat(summonManager.consumeVillageGuardDeaths()) {
            replacementDueStages +=
                currentStage + REPLACEMENT_STAGE_DELAY
        }

        if (observedStage != currentStage) {
            observedStage = currentStage

            val availableSlots =
                (MAXIMUM_GUARDS -
                    summonManager.livingVillageGuardCount -
                    pendingGuardSpawns)
                    .coerceAtLeast(0)

            val dueReplacements =
                replacementDueStages.count {
                    it <= currentStage
                }

            val acceptedReplacements =
                dueReplacements.coerceAtMost(availableSlots)

            repeat(acceptedReplacements) {
                val dueIndex =
                    replacementDueStages.indexOfFirst {
                        it <= currentStage
                    }

                if (dueIndex >= 0) {
                    replacementDueStages.removeAt(dueIndex)
                }
            }

            var requestedSpawns = acceptedReplacements
            val slotsAfterReplacements =
                availableSlots - acceptedReplacements

            if (
                currentStage > 1 &&
                currentStage % REGULAR_GUARD_STAGE_INTERVAL == 0 &&
                slotsAfterReplacements > 0
            ) {
                requestedSpawns += 1
            }

            pendingGuardSpawns +=
                requestedSpawns
        }

        if (!animationActive && pendingGuardSpawns > 0) {
            animationActive = true
            animationSeconds = 0f
            guardSpawnedDuringAnimation = false
        }

        if (!animationActive) {
            gateObstacle?.visualVariant = 0
            return
        }

        animationSeconds += delta.coerceAtLeast(0f)
        gateObstacle?.visualVariant =
            animationFrame(animationSeconds)

        if (
            !guardSpawnedDuringAnimation &&
            animationSeconds >= GUARD_SPAWN_TIME_SECONDS
        ) {
            guardSpawnedDuringAnimation = true

            if (
                spawnGuard(
                    summonManager = summonManager,
                    stage = currentStage,
                    guardProfileProvider = guardProfileProvider
                )
            ) {
                pendingGuardSpawns =
                    (pendingGuardSpawns - 1)
                        .coerceAtLeast(0)
            } else {
                pendingGuardSpawns = 0
            }
        }

        if (animationSeconds >= GATE_ANIMATION_SECONDS) {
            animationActive = false
            animationSeconds = 0f
            gateObstacle?.visualVariant = 0
        }
    }

    fun takeDamage(amount: Int): Int {
        if (!isEnabled) {
            return 0
        }

        return gateHealth.takeDamage(amount)
    }

    fun onStageEnded(completedStage: Int) {
        if (!isEnabled || gateHealth.isDead) {
            return
        }

        val progress =
            ((completedStage.coerceIn(1, 40) - 1) / 39f)

        val healRatio =
            MINIMUM_STAGE_HEAL_RATIO +
                (MAXIMUM_STAGE_HEAL_RATIO - MINIMUM_STAGE_HEAL_RATIO) *
                progress

        gateHealth.heal(
            (GATE_MAXIMUM_HEALTH * healRatio)
                .roundToInt()
        )
    }

    fun restoreCheckpoint(
        gateCurrentHealth: Int,
        guardCount: Int,
        currentStage: Int,
        summonManager: SummonManager,
        guardProfileProvider: (Int) -> Pair<String, EnemyStats>?
    ) {
        if (!isEnabled) {
            return
        }

        gateHealth.restoreCurrentHealth(
            gateCurrentHealth.coerceIn(1, GATE_MAXIMUM_HEALTH)
        )

        observedStage = currentStage
        pendingGuardSpawns = 0
        replacementDueStages.clear()
        introMessageRemainingSeconds = 0f

        repeat(guardCount.coerceIn(0, MAXIMUM_GUARDS)) {
            spawnGuard(
                summonManager = summonManager,
                stage = currentStage,
                guardProfileProvider = guardProfileProvider
            )
        }
    }

    fun renderHealthBar(shapeRenderer: ShapeRenderer) {
        val gate = gateObstacle ?: return

        if (!isEnabled) {
            return
        }

        val width = GATE_HEALTH_BAR_WIDTH
        val height = GATE_HEALTH_BAR_HEIGHT
        val x = healthBarX(gate)
        val y = healthBarY(gate)

        shapeRenderer.color = Color(0.08f, 0.05f, 0.04f, 0.95f)
        shapeRenderer.rect(x - 3f, y - 3f, width + 6f, height + 6f)
        shapeRenderer.color = Color(0.25f, 0.04f, 0.03f, 0.95f)
        shapeRenderer.rect(x, y, width, height)
        shapeRenderer.color = Color(0.78f, 0.16f, 0.10f, 1f)
        shapeRenderer.rect(
            x,
            y,
            width * gateHealth.healthRatio,
            height
        )
    }

    fun renderTexts(
        batch: SpriteBatch,
        font: BitmapFont,
        camera: OrthographicCamera
    ) {
        if (!isEnabled) {
            return
        }

        batch.projectionMatrix = camera.combined
        batch.begin()

        gateObstacle?.let { gate ->
            val healthText =
                "${gateHealth.currentHealth}/${gateHealth.maximumHealth}"

            val layout = GlyphLayout(font, healthText)
            font.color = Color.WHITE
            font.draw(
                batch,
                healthText,
                healthBarX(gate) + GATE_HEALTH_BAR_WIDTH / 2f - layout.width / 2f,
                healthBarY(gate) + GATE_HEALTH_BAR_HEIGHT - 4f
            )
        }

        if (introMessageRemainingSeconds > 0f) {
            val alpha =
                (introMessageRemainingSeconds / INTRO_MESSAGE_FADE_SECONDS)
                    .coerceIn(0f, 1f)

            val previousScaleX = font.data.scaleX
            val previousScaleY = font.data.scaleY
            font.data.setScale(1.45f)
            font.color = Color(1f, 0.93f, 0.76f, alpha)

            val layout = GlyphLayout(font, INTRO_MESSAGE)
            font.draw(
                batch,
                INTRO_MESSAGE,
                camera.position.x - layout.width / 2f,
                camera.position.y + camera.viewportHeight / 2f - INTRO_MESSAGE_TOP_PADDING
            )

            font.data.setScale(previousScaleX, previousScaleY)
        }

        font.color = Color.WHITE
        batch.end()
    }

    private fun spawnGuard(
        summonManager: SummonManager,
        stage: Int,
        guardProfileProvider: (Int) -> Pair<String, EnemyStats>?
    ): Boolean {
        val gate = gateObstacle ?: return false
        val (sourceEnemyName, sourceEnemyStats) =
            guardProfileProvider(stage)
                ?: return false

        val homeX =
            gate.visualBounds.x +
                gate.visualBounds.width / 2f

        val homeY =
            gate.visualBounds.y -
                TroubleAtHomeLayout.GUARD_SPAWN_OFFSET_Y

        return summonManager.spawnVillageGuard(
            stage = stage,
            sourceEnemyName = sourceEnemyName,
            sourceEnemyStats = sourceEnemyStats,
            centerX = homeX,
            centerY = homeY,
            leashCenterX = homeX,
            leashCenterY = homeY,
            maximumLeashDistance = GUARD_MAXIMUM_GATE_DISTANCE
        )
    }

    private fun healthBarX(gate: MapObstacle): Float =
        gate.visualBounds.x +
            gate.visualBounds.width / 2f -
            GATE_HEALTH_BAR_WIDTH / 2f

    private fun healthBarY(gate: MapObstacle): Float =
        gate.visualBounds.y -
            GATE_HEALTH_BAR_OFFSET_Y

    private fun animationFrame(seconds: Float): Int {
        return when {
            seconds < 0.35f -> 1
            seconds < 0.75f -> 2
            seconds < 1.55f -> 3
            seconds < 1.95f -> 2
            seconds < GATE_ANIMATION_SECONDS -> 1
            else -> 0
        }
    }

    companion object {
        const val GATE_MAXIMUM_HEALTH = 1000

        private const val MAXIMUM_GUARDS = 5
        private const val REGULAR_GUARD_STAGE_INTERVAL = 2
        private const val REPLACEMENT_STAGE_DELAY = 2
        private const val GUARD_MAXIMUM_GATE_DISTANCE = 1000f
        private const val GUARD_SPAWN_TIME_SECONDS = 0.90f
        private const val GATE_ANIMATION_SECONDS = 2.30f
        private const val GATE_HEALTH_BAR_WIDTH = 260f
        private const val GATE_HEALTH_BAR_HEIGHT = 22f
        private const val GATE_HEALTH_BAR_OFFSET_Y = 42f
        private const val MINIMUM_STAGE_HEAL_RATIO = 0.05f
        private const val MAXIMUM_STAGE_HEAL_RATIO = 0.15f
        private const val INTRO_MESSAGE = "Defend your home, and survive."
        private const val INTRO_MESSAGE_DURATION_SECONDS = 4.5f
        private const val INTRO_MESSAGE_FADE_SECONDS = 1.5f
        private const val INTRO_MESSAGE_TOP_PADDING = 58f
    }
}
