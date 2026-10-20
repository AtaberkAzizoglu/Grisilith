package com.azizoglu.grisilith.map

import com.azizoglu.grisilith.combat.HealthComponent
import com.azizoglu.grisilith.combat.damage.DamageNumberEvent
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
import com.badlogic.gdx.math.Vector2
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt

class TroubleAtHomeDefenseRuntime(
    gameMap: GameMap,
    obstacles: List<MapObstacle>
) {
    private var introArt: com.badlogic.gdx.graphics.Texture? = null
    fun dispose() { introArt?.dispose(); introArt=null }

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

    private var animationPhaseSeconds =
        0f

    private var openSpawnCooldownSeconds =
        0f

    private var gateAnimationState =
        GateAnimationState.CLOSED

    private val pendingDamageNumberEvents =
        mutableListOf<DamageNumberEvent>()

    private var introMessageRemainingSeconds =
        if (isEnabled) INTRO_MESSAGE_DURATION_SECONDS else 0f

    val targetBounds: Rectangle?
        get() =
            if (isEnabled && !gateHealth.isDead) {
                gateObstacle?.bounds?.let { physicalBounds ->
                    /*
                     * The physical gate footprint lives inside the northern
                     * wall. Enemies cannot legally enter it, so using that
                     * same rectangle as a combat target made melee contact
                     * depend on a one-pixel overlap. This attack strip sits
                     * immediately in front of the gate while preserving the
                     * real wall collision and the requested visual hitbox.
                     */
                    Rectangle(
                        physicalBounds.x,
                        physicalBounds.y - GATE_ATTACK_STRIP_DEPTH,
                        physicalBounds.width,
                        GATE_ATTACK_STRIP_DEPTH
                    )
                }
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

        when (gateAnimationState) {
            GateAnimationState.CLOSED -> {
                gateObstacle?.visualVariant = 0

                if (pendingGuardSpawns > 0) {
                    gateAnimationState = GateAnimationState.OPENING
                    animationPhaseSeconds = 0f
                }
            }

            GateAnimationState.OPENING -> {
                animationPhaseSeconds += delta.coerceAtLeast(0f)
                gateObstacle?.visualVariant =
                    if (animationPhaseSeconds < 0.38f) 1 else 2

                if (animationPhaseSeconds >= GATE_OPENING_SECONDS) {
                    gateAnimationState = GateAnimationState.OPEN
                    animationPhaseSeconds = 0f
                    openSpawnCooldownSeconds = 0f
                    gateObstacle?.visualVariant = 3
                }
            }

            GateAnimationState.OPEN -> {
                gateObstacle?.visualVariant = 3
                openSpawnCooldownSeconds =
                    (openSpawnCooldownSeconds - delta.coerceAtLeast(0f))
                        .coerceAtLeast(0f)

                if (
                    pendingGuardSpawns > 0 &&
                    openSpawnCooldownSeconds <= 0f
                ) {
                    if (
                        spawnGuard(
                            summonManager = summonManager,
                            stage = currentStage,
                            guardProfileProvider = guardProfileProvider
                        )
                    ) {
                        pendingGuardSpawns--
                        openSpawnCooldownSeconds =
                            GUARD_SPAWN_INTERVAL_SECONDS
                    } else {
                        pendingGuardSpawns = 0
                    }
                }

                if (pendingGuardSpawns <= 0) {
                    gateAnimationState = GateAnimationState.CLOSING
                    animationPhaseSeconds = 0f
                }
            }

            GateAnimationState.CLOSING -> {
                animationPhaseSeconds += delta.coerceAtLeast(0f)
                gateObstacle?.visualVariant =
                    if (animationPhaseSeconds < 0.38f) 2 else 1

                if (animationPhaseSeconds >= GATE_CLOSING_SECONDS) {
                    gateAnimationState = GateAnimationState.CLOSED
                    animationPhaseSeconds = 0f
                    gateObstacle?.visualVariant = 0
                }
            }
        }
    }

    fun takeDamage(amount: Int): Int {
        if (!isEnabled) {
            return 0
        }

        val appliedDamage = gateHealth.takeDamage(amount)

        if (appliedDamage > 0) {
            val gate = gateObstacle
            pendingDamageNumberEvents +=
                DamageNumberEvent(
                    amount = appliedDamage,
                    worldX = gate?.visualBounds?.let { it.x + it.width / 2f } ?: 0f,
                    worldY = gate?.visualBounds?.y ?: 0f
                )
        }

        return appliedDamage
    }

    fun consumeDamageNumberEvents(): List<DamageNumberEvent> {
        val result = pendingDamageNumberEvents.toList()
        pendingDamageNumberEvents.clear()
        return result
    }

    fun onStageEnded() {
        if (!isEnabled || gateHealth.isDead) {
            return
        }

        gateHealth.heal(
            (GATE_MAXIMUM_HEALTH * STAGE_HEAL_RATIO)
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
        gateAnimationState = GateAnimationState.CLOSED
        animationPhaseSeconds = 0f
        openSpawnCooldownSeconds = 0f
        gateObstacle?.visualVariant = 0
        introMessageRemainingSeconds = 0f

        repeat(guardCount.coerceIn(0, MAXIMUM_GUARDS)) {
            spawnGuard(
                summonManager = summonManager,
                stage = currentStage,
                guardProfileProvider = guardProfileProvider
            )
        }
    }

    fun renderHealthBar(
        shapeRenderer: ShapeRenderer,
        camera: OrthographicCamera
    ) {
        val gate = gateObstacle ?: return

        if (!isEnabled) {
            return
        }

        val worldBarVisible = isWorldHealthBarVisible(gate, camera)
        val width =
            if (worldBarVisible) GATE_HEALTH_BAR_WIDTH else EDGE_HEALTH_BAR_WIDTH
        val height =
            if (worldBarVisible) GATE_HEALTH_BAR_HEIGHT else EDGE_HEALTH_BAR_HEIGHT
        val edgePosition =
            if (worldBarVisible) null else edgeHealthBarPosition(gate, camera)
        val x = edgePosition?.x ?: healthBarX(gate)
        val y = edgePosition?.y ?: healthBarY(gate)

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
        camera: OrthographicCamera,
        playerCenterX: Float = camera.position.x,
        playerCenterY: Float = camera.position.y
    ) {
        if (!isEnabled) {
            return
        }

        batch.projectionMatrix = camera.combined
        batch.begin()

        gateObstacle?.let { gate ->
            val healthText =
                "${gateHealth.currentHealth}/${gateHealth.maximumHealth}"
            font.color = Color.WHITE

            if (isWorldHealthBarVisible(gate, camera)) {
                val layout = GlyphLayout(font, healthText)
                font.draw(
                    batch,
                    healthText,
                    healthBarX(gate) + GATE_HEALTH_BAR_WIDTH / 2f - layout.width / 2f,
                    healthBarY(gate) + GATE_HEALTH_BAR_HEIGHT - 4f
                )
            } else {
                val edgePosition = edgeHealthBarPosition(gate, camera)
                val previousScaleX = font.data.scaleX
                val previousScaleY = font.data.scaleY
                font.data.setScale(0.72f)
                val layout = GlyphLayout(font, healthText)
                font.draw(
                    batch,
                    healthText,
                    edgePosition.x + EDGE_HEALTH_BAR_WIDTH / 2f - layout.width / 2f,
                    edgePosition.y + EDGE_HEALTH_BAR_HEIGHT - 1f
                )
                font.data.setScale(previousScaleX, previousScaleY)
            }
        }

        if (introMessageRemainingSeconds > 0f) {
            val alpha =
                (introMessageRemainingSeconds / INTRO_MESSAGE_FADE_SECONDS)
                    .coerceIn(0f, 1f)

            val art=introArt ?: com.badlogic.gdx.graphics.Texture(com.badlogic.gdx.Gdx.files.internal("ui/text/siege_intro_v1.png")).also {
                it.setFilter(com.badlogic.gdx.graphics.Texture.TextureFilter.Linear,com.badlogic.gdx.graphics.Texture.TextureFilter.Linear)
                introArt=it
            }
            val previous=Color(batch.color)
            batch.color=Color(1f,1f,1f,alpha)
            batch.draw(art,playerCenterX-art.width/8f,playerCenterY+80f,art.width/4f,art.height/4f)
            batch.color=previous
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
            GATE_HEALTH_BAR_OFFSET_Y +
            gate.visualBounds.height

    private fun edgeHealthBarPosition(
        gate: MapObstacle,
        camera: OrthographicCamera
    ): Vector2 {
        val halfWidth = camera.viewportWidth * camera.zoom / 2f
        val halfHeight = camera.viewportHeight * camera.zoom / 2f
        val targetX = gate.visualBounds.x + gate.visualBounds.width / 2f
        val targetY = gate.visualBounds.y + gate.visualBounds.height / 2f
        val dx = targetX - camera.position.x
        val dy = targetY - camera.position.y

        if (abs(dx) < 0.001f && abs(dy) < 0.001f) {
            return Vector2(
                camera.position.x - EDGE_HEALTH_BAR_WIDTH / 2f,
                camera.position.y + halfHeight -
                    EDGE_HEALTH_BAR_HEIGHT - EDGE_INDICATOR_PADDING
            )
        }

        val horizontalLimit =
            halfWidth - EDGE_HEALTH_BAR_WIDTH / 2f - EDGE_INDICATOR_PADDING
        val verticalLimit =
            halfHeight - EDGE_HEALTH_BAR_HEIGHT / 2f - EDGE_INDICATOR_PADDING
        val xScale =
            if (abs(dx) < 0.001f) {
                Float.POSITIVE_INFINITY
            } else {
                horizontalLimit / abs(dx)
            }
        val yScale =
            if (abs(dy) < 0.001f) {
                Float.POSITIVE_INFINITY
            } else {
                verticalLimit / abs(dy)
            }
        val scale = min(xScale, yScale)
        val centerX = camera.position.x + dx * scale
        val centerY = camera.position.y + dy * scale

        return Vector2(
            centerX - EDGE_HEALTH_BAR_WIDTH / 2f,
            centerY - EDGE_HEALTH_BAR_HEIGHT / 2f
        )
    }

    private fun isWorldHealthBarVisible(
        gate: MapObstacle,
        camera: OrthographicCamera
    ): Boolean {
        val visibleWidth = camera.viewportWidth * camera.zoom
        val visibleHeight = camera.viewportHeight * camera.zoom
        val cameraBounds =
            Rectangle(
                camera.position.x - visibleWidth / 2f,
                camera.position.y - visibleHeight / 2f,
                visibleWidth,
                visibleHeight
            )
        val healthBarBounds =
            Rectangle(
                healthBarX(gate) - 3f,
                healthBarY(gate) - 3f,
                GATE_HEALTH_BAR_WIDTH + 6f,
                GATE_HEALTH_BAR_HEIGHT + 6f
            )

        return cameraBounds.overlaps(healthBarBounds)
    }

    private enum class GateAnimationState {
        CLOSED,
        OPENING,
        OPEN,
        CLOSING
    }

    companion object {
        private const val GATE_ATTACK_STRIP_DEPTH = 42f
        const val GATE_MAXIMUM_HEALTH = 1000

        private const val MAXIMUM_GUARDS = 3
        private const val REGULAR_GUARD_STAGE_INTERVAL = 2
        private const val REPLACEMENT_STAGE_DELAY = 2
        private const val GUARD_MAXIMUM_GATE_DISTANCE = 750f
        private const val GUARD_SPAWN_INTERVAL_SECONDS = 0.32f
        private const val GATE_OPENING_SECONDS = 0.76f
        private const val GATE_CLOSING_SECONDS = 0.76f
        private const val GATE_HEALTH_BAR_WIDTH = 260f
        private const val GATE_HEALTH_BAR_HEIGHT = 22f
        private const val GATE_HEALTH_BAR_OFFSET_Y = 36f
        private const val EDGE_HEALTH_BAR_WIDTH = 145f
        private const val EDGE_HEALTH_BAR_HEIGHT = 12f
        private const val EDGE_INDICATOR_PADDING = 20f
        private const val STAGE_HEAL_RATIO = 0.05f
        private const val INTRO_MESSAGE_DURATION_SECONDS = 8f
        private const val INTRO_MESSAGE_FADE_SECONDS = 1.5f
    }
}
