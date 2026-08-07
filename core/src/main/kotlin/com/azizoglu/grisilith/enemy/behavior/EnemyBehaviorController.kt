package com.azizoglu.grisilith.enemy.behavior

import com.azizoglu.grisilith.content.ContentId
import com.azizoglu.grisilith.enemy.definition.EnemyBehaviorIds
import com.badlogic.gdx.math.Vector2

/*
 * Enemy'nin hedefe göre yalnızca hareket kararını çözer.
 *
 * Hasar, projectile ve Weapon runtime bağlantıları bu sınıfa
 * gömülmez; sonraki combat katmanı aynı behavior profile'ını
 * kullanacaktır.
 */
class EnemyBehaviorController(
    behaviorId: ContentId?,
    preferredAttackRange: Float
) {

    private val preferredAttackRange =
        preferredAttackRange
            .coerceAtLeast(
                0f
            )

    private val profile =
        resolveProfile(
            behaviorId
        )

    fun resolveMovementDirection(
        enemyCenterX: Float,
        enemyCenterY: Float,
        targetCenterX: Float,
        targetCenterY: Float,
        output: Vector2
    ): Vector2 {

        val towardTargetX =
            targetCenterX -
                enemyCenterX

        val towardTargetY =
            targetCenterY -
                enemyCenterY

        return when (
            profile.movementMode
        ) {
            EnemyMovementMode.CHASE -> {
                output.set(
                    towardTargetX,
                    towardTargetY
                )
            }

            EnemyMovementMode.HOLD_POSITION -> {
                output.setZero()
            }

            EnemyMovementMode.KEEP_DISTANCE -> {
                resolveKeepDistanceDirection(
                    towardTargetX =
                        towardTargetX,

                    towardTargetY =
                        towardTargetY,

                    output =
                        output
                )
            }
        }
    }

    private fun resolveKeepDistanceDirection(
        towardTargetX: Float,
        towardTargetY: Float,
        output: Vector2
    ): Vector2 {

        val distanceSquared =
            towardTargetX *
                towardTargetX +
                towardTargetY *
                towardTargetY

        val preferredDistance =
            preferredAttackRange *
                profile
                    .preferredRangeMultiplier

        val retreatDistance =
            preferredAttackRange *
                profile
                    .retreatRangeMultiplier

        return when {
            distanceSquared >
                preferredDistance *
                preferredDistance -> {

                output.set(
                    towardTargetX,
                    towardTargetY
                )
            }

            distanceSquared <
                retreatDistance *
                retreatDistance -> {

                output.set(
                    -towardTargetX,
                    -towardTargetY
                )
            }

            else -> {
                output.setZero()
            }
        }
    }

    private fun resolveProfile(
        behaviorId: ContentId?
    ): EnemyBehaviorProfile {

        return when (
            behaviorId
        ) {
            EnemyBehaviorIds.KEEP_DISTANCE_AND_SHOOT,
            EnemyBehaviorIds.MULTI_WEAPON_CASTER -> {
                EnemyBehaviorProfile(
                    movementMode =
                        EnemyMovementMode.KEEP_DISTANCE,

                    preferredRangeMultiplier =
                        0.82f,

                    retreatRangeMultiplier =
                        0.48f
                )
            }

            else -> {
                EnemyBehaviorProfile(
                    movementMode =
                        EnemyMovementMode.CHASE
                )
            }
        }
    }
}
