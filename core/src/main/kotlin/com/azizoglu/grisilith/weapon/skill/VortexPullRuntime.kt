package com.azizoglu.grisilith.weapon.skill

import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.enemy.EnemyType
import com.azizoglu.grisilith.content.DamageType
import com.azizoglu.grisilith.weapon.skill.area.AreaSkillField
import com.azizoglu.grisilith.weapon.skill.area.AreaSkillRuntime
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer

class VortexPullRuntime {

    private val reactiveTarget =
        ReactiveSpellFieldTarget(DamageType.NECROTIC)

    private val areaRuntime =
        AreaSkillRuntime()

    private val selectedCenter =
        Vector2()

    private var visualRotationDegrees =
        0f

    private var visualTimeSeconds =
        0f

    private var plagueStormActive =
        false

    private var plagueActivationVisualRemainingSeconds =
        0f

    private var plaguePulseSeconds =
        0f

    private var damageTickSeconds =
        DefaultWeaponAbilities.VORTEX_PULL_DAMAGE_TICK_SECONDS

    val isPlagueStormActive: Boolean
        get() =
            plagueStormActive

    val isActive:
        Boolean
        get() =
            areaRuntime.isActive

    val field:
        AreaSkillField?
        get() =
            areaRuntime.field

    val remainingDurationSeconds:
        Float
        get() =
            areaRuntime
                .remainingDurationSeconds

    val visualAnimationSeconds: Float
        get() =
            visualTimeSeconds

    val durationRatio:
        Float
        get() {
            val activeField =
                areaRuntime.field
                    ?: return 0f

            return (
                remainingDurationSeconds /
                    activeField.durationSeconds
                )
                .coerceIn(
                    0f,
                    1f
                )
        }

    fun contains(
        worldX: Float,
        worldY: Float
    ): Boolean {

        return areaRuntime
            .field
            ?.contains(
                worldX,
                worldY
            )
            ?: false
    }

    fun activatePlagueStorm():
        Boolean {

        if (
            !isActive ||
            plagueStormActive
        ) {
            return false
        }

        val currentField =
            areaRuntime.field
                ?: return false

        val centerX =
            currentField.center.x

        val centerY =
            currentField.center.y

        val radius =
            currentField.radius

        /*
         * Plague Storm başladığında mevcut Vortex süresi bırakılır
         * ve merkez/radius korunarak 12 saniyelik yeni süre başlatılır.
         */
        areaRuntime.clear()

        areaRuntime.begin(
            centerX =
                centerX,

            centerY =
                centerY,

            radius =
                radius,

            durationSeconds =
                PLAGUE_STORM_DURATION_SECONDS
        )

        plagueStormActive =
            true

        plagueActivationVisualRemainingSeconds =
            PLAGUE_ACTIVATION_VISUAL_SECONDS

        return true
    }

    fun tryDamageReactiveTarget(
        damageType: DamageType?,
        worldX: Float,
        worldY: Float
    ): Boolean {
        if (!isActive || plagueStormActive || !contains(worldX, worldY)) return false
        if (!reactiveTarget.tryDamage(damageType)) return false
        return activatePlagueStorm()
    }

    /*
     * Uygun düşman yoksa false döner.
     * Böylece manager ability state'ini ACTIVE yapmadan bekleyebilir
     * ve cooldown başlamaz.
     */
    fun tryBegin(
        playerCenterX: Float,
        playerCenterY: Float,
        enemies: List<Enemy>,
        worldWidth: Float,
        worldHeight: Float,
        upgraded: Boolean
    ): Boolean {
        if (isActive) {
            return false
        }

        val livingCandidates =
            enemies.filter {
                    enemy ->

                !enemy.isDead &&
                    Vector2.dst2(
                        playerCenterX,
                        playerCenterY,
                        enemy.centerX,
                        enemy.centerY
                    ) <=
                    DefaultWeaponAbilities
                        .VORTEX_PULL_PLACEMENT_RANGE
                        .let {
                            it *
                                it
                        }
            }

        if (
            livingCandidates.isEmpty()
        ) {
            return false
        }

        /*
         * Vortex Pull artık menzil içindeki EN YAKIN yaşayan Enemy'yi
         * hedefler ve alan merkezini doğrudan o Enemy'nin merkezine koyar.
         * Rastgele anchor / random offset yoktur.
         */
        val bossCandidates =
            livingCandidates.filter { enemy ->
                enemy.type == EnemyType.MINI_BOSS ||
                    enemy.type == EnemyType.FINAL_BOSS
            }

        val anchor =
            (if (bossCandidates.isNotEmpty()) bossCandidates else livingCandidates)
                .minByOrNull { enemy ->
                    Vector2.dst2(
                        playerCenterX,
                        playerCenterY,
                        enemy.centerX,
                        enemy.centerY
                    )
                }
                ?: return false

        selectedCenter.set(
            anchor.centerX,
            anchor.centerY
        )

        val radius =
            DefaultWeaponAbilities
                .VORTEX_PULL_RADIUS

        selectedCenter.x =
            selectedCenter.x
                .coerceIn(
                    radius,
                    (
                        worldWidth -
                            radius
                        )
                        .coerceAtLeast(
                            radius
                        )
                )

        selectedCenter.y =
            selectedCenter.y
                .coerceIn(
                    radius,
                    (
                        worldHeight -
                            radius
                        )
                        .coerceAtLeast(
                            radius
                        )
                )

        /*
         * Clamp sonrasında alanın hâlâ en az bir hedef içerdiğini
         * doğrularız. İçermiyorsa anchor merkezine geri döneriz.
         */
        val containsLivingEnemy =
            livingCandidates.any {
                    enemy ->

                Vector2.dst2(
                    selectedCenter.x,
                    selectedCenter.y,
                    enemy.centerX,
                    enemy.centerY
                ) <=
                    radius *
                    radius
            }

        if (!containsLivingEnemy) {
            selectedCenter.set(
                anchor.centerX
                    .coerceIn(
                        radius,
                        (
                            worldWidth -
                                radius
                            )
                            .coerceAtLeast(
                                radius
                            )
                    ),

                anchor.centerY
                    .coerceIn(
                        radius,
                        (
                            worldHeight -
                                radius
                            )
                            .coerceAtLeast(
                                radius
                            )
                    )
            )
        }

        damageTickSeconds =
            if (upgraded) {
                DefaultWeaponAbilities.VORTEX_PULL_UPGRADED_DAMAGE_TICK_SECONDS
            } else {
                DefaultWeaponAbilities.VORTEX_PULL_DAMAGE_TICK_SECONDS
            }

        val began = areaRuntime.begin(
            centerX =
                selectedCenter.x,

            centerY =
                selectedCenter.y,

            radius =
                radius,

            durationSeconds =
                DefaultWeaponAbilities
                    .VORTEX_PULL_DURATION_SECONDS
        )
        if (began) reactiveTarget.reset()
        return began
    }

    fun update(
        delta: Float,
        enemies: List<Enemy>,
        currentStage: Int,
        plagueMoveSpeedPixelsPerSecond: Float,
        onPullEnemy: (
            enemy: Enemy,
            centerX: Float,
            centerY: Float,
            pullMultiplier: Float,
            frameDelta: Float
        ) -> Unit,
        onDamageTick: (
            enemy: Enemy,
            tickIntervalSeconds: Float
        ) -> Unit,
        onCompleted: () -> Unit
    ) {
        visualRotationDegrees =
            (
                visualRotationDegrees +
                    delta.coerceAtLeast(
                        0f
                    ) *
                    VISUAL_ROTATION_SPEED_DEGREES
                ) %
                360f

        visualTimeSeconds +=
            delta.coerceAtLeast(
                0f
            )

        plaguePulseSeconds +=
            delta.coerceAtLeast(
                0f
            )

        plagueActivationVisualRemainingSeconds =
            (
                plagueActivationVisualRemainingSeconds -
                    delta.coerceAtLeast(
                        0f
                    )
                )
                .coerceAtLeast(
                    0f
                )

        areaRuntime.update(
            delta =
                delta,

            tickIntervalSeconds =
                damageTickSeconds,

            onFrame = {
                    area,
                    frameDelta ->

                if (
                    plagueStormActive
                ) {
                    val hasEnemyInside =
                        enemies.any {
                            !it.isDead &&
                                area.contains(
                                    it.centerX,
                                    it.centerY
                                )
                        }

                    if (
                        !hasEnemyInside
                    ) {
                        val nearestEnemy =
                            enemies
                                .asSequence()
                                .filter {
                                    !it.isDead
                                }
                                .minByOrNull {
                                    Vector2.dst2(
                                        area.center.x,
                                        area.center.y,
                                        it.centerX,
                                        it.centerY
                                    )
                                }

                        nearestEnemy
                            ?.let {
                                    target ->

                                val movement =
                                    Vector2(
                                        target.centerX -
                                            area.center.x,

                                        target.centerY -
                                            area.center.y
                                    )

                                if (
                                    !movement.isZero(
                                        0.001f
                                    )
                                ) {
                                    movement
                                        .nor()
                                        .scl(
                                            plagueMoveSpeedPixelsPerSecond *
                                                frameDelta
                                        )

                                    area.center.add(
                                        movement
                                    )
                                }
                            }
                    }
                }

                enemies
                    .asSequence()
                    .filter {
                            enemy ->
                        !enemy.isDead
                    }
                    .filter {
                            enemy ->
                        area.contains(
                            enemy.centerX,
                            enemy.centerY
                        )
                    }
                    .forEach {
                            enemy ->

                        onPullEnemy(
                            enemy,
                            area.center.x,
                            area.center.y,
                            VortexPullTargetRules
                                .pullMultiplier(
                                    enemy =
                                        enemy,

                                    currentStage =
                                        currentStage
                                ),
                            frameDelta
                        )
                    }
            },

            onTick = {
                    area ->

                enemies
                    .asSequence()
                    .filter {
                            enemy ->
                        !enemy.isDead
                    }
                    .filter {
                            enemy ->
                        area.contains(
                            enemy.centerX,
                            enemy.centerY
                        )
                    }
                    .forEach {
                            enemy ->

                        onDamageTick(
                            enemy,
                            damageTickSeconds
                        )
                    }
            },

            onCompleted =
                onCompleted
        )
    }

    @Suppress("UNUSED_PARAMETER")
    fun render(
        shapeRenderer: ShapeRenderer
    ) {
        // Sprite visual is rendered by SpellFocusEffectSpriteRenderer.
    }

    fun clear() {
        areaRuntime.clear()
        reactiveTarget.clear()

        visualRotationDegrees =
            0f

        visualTimeSeconds =
            0f

        plagueStormActive =
            false

        plagueActivationVisualRemainingSeconds =
            0f

        plaguePulseSeconds =
            0f
    }

    companion object {
        private const val VISUAL_ROTATION_SPEED_DEGREES =
            92f

        private const val VISUAL_ARM_COUNT =
            6

        private const val VISUAL_ARM_CURVE_DEGREES =
            42f

        private const val CENTER_MARKER_RADIUS =
            9f

        private const val PLAGUE_STORM_DURATION_SECONDS =
            13f

        private const val PLAGUE_ACTIVATION_VISUAL_SECONDS =
            0.60f

        private const val PLAGUE_PULSE_SPEED =
            4.4f

        private const val PLAGUE_ORBITAL_COUNT =
            7
    }

}
