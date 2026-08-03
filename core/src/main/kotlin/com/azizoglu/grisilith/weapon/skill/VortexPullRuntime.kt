package com.azizoglu.grisilith.weapon.skill

import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.weapon.skill.area.AreaSkillField
import com.azizoglu.grisilith.weapon.skill.area.AreaSkillRuntime
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2

class VortexPullRuntime {

    private val areaRuntime =
        AreaSkillRuntime()

    private val selectedCenter =
        Vector2()

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
        worldHeight: Float
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
         * Rastgele bir uygun düşman seçilir; alanın merkezi düşmanın
         * tam üzerinde değil, yakınındaki rastgele bir noktadadır.
         * Böylece skill her kullanımda aynı görünmez fakat boş alana
         * düşmesi de engellenir.
         */
        val anchor =
            livingCandidates.random()

        val randomAngle =
            MathUtils.random(
                0f,
                360f
            )

        val randomDistance =
            MathUtils.random(
                0f,
                DefaultWeaponAbilities
                    .VORTEX_PULL_RADIUS *
                    0.42f
            )

        selectedCenter.set(
            anchor.centerX +
                MathUtils.cosDeg(
                    randomAngle
                ) *
                randomDistance,

            anchor.centerY +
                MathUtils.sinDeg(
                    randomAngle
                ) *
                randomDistance
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

        return areaRuntime.begin(
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
    }

    fun update(
        delta: Float,
        enemies: List<Enemy>,
        currentStage: Int,
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
        areaRuntime.update(
            delta =
                delta,

            tickIntervalSeconds =
                DefaultWeaponAbilities
                    .VORTEX_PULL_DAMAGE_TICK_SECONDS,

            onFrame = {
                    area,
                    frameDelta ->

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
                            DefaultWeaponAbilities
                                .VORTEX_PULL_DAMAGE_TICK_SECONDS
                        )
                    }
            },

            onCompleted =
                onCompleted
        )
    }

    fun clear() {
        areaRuntime.clear()
    }
}
