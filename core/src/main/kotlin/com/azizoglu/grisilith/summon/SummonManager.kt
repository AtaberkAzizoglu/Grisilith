package com.azizoglu.grisilith.summon

import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.weapon.runtime.WeaponInventory
import com.azizoglu.grisilith.weapon.runtime.WeaponInstance
import com.azizoglu.grisilith.weapon.summon.IvySummonChoice
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils

class SummonManager(
    private val weaponInventory: WeaponInventory,
    private val playerStats: PlayerStats
) {

    private val entities =
        mutableListOf<SummonedEntity>()

    private val schedulersByWeaponRuntimeId =
        linkedMapOf<String, SummonSpawnScheduler>()

    val activeSummons:
        List<SummonedEntity>
        get() =
            entities.toList()

    fun update(
        delta: Float,
        playerCenterX: Float,
        playerCenterY: Float,
        worldWidth: Float,
        worldHeight: Float
    ) {

        if (
            delta <= 0f
        ) {
            return
        }

        synchronizeSchedulers()

        entities
            .forEach {
                    entity ->

                entity.update(
                    delta =
                        delta,

                    playerCenterX =
                        playerCenterX,

                    playerCenterY =
                        playerCenterY,

                    worldWidth =
                        worldWidth,

                    worldHeight =
                        worldHeight
                )
            }

        val endedWaveWeaponIds =
            entities
                .asSequence()
                .filter {
                    it.isExpired
                }
                .map {
                    it.sourceWeaponRuntimeId
                }
                .toSet()

        entities.removeAll {
            it.isExpired
        }

        endedWaveWeaponIds
            .forEach {
                    weaponRuntimeId ->

                val stillHasLivingEntity =
                    entities.any {
                        it.sourceWeaponRuntimeId ==
                            weaponRuntimeId
                    }

                if (
                    !stillHasLivingEntity
                ) {
                    schedulersByWeaponRuntimeId[
                        weaponRuntimeId
                    ]
                        ?.markWaveEnded()
                }
            }

        schedulersByWeaponRuntimeId
            .values
            .forEach {
                    scheduler ->

                scheduler.updateDelay(
                    delta
                )

                if (
                    scheduler.isReadyToSpawn
                ) {
                    spawnWave(
                        scheduler =
                            scheduler,

                        playerCenterX =
                            playerCenterX,

                        playerCenterY =
                            playerCenterY
                    )
                }
            }
    }

    fun render(
        shapeRenderer: ShapeRenderer
    ) {

        entities.forEach {
            it.render(
                shapeRenderer
            )
        }
    }

    fun clear() {

        entities.clear()
        schedulersByWeaponRuntimeId.clear()
    }

    private fun synchronizeSchedulers() {

        val eligibleWeapons =
            weaponInventory
                .allWeapons()
                .filter {
                        weapon ->

                    weapon.definition.id.value ==
                        IVY_STAFF_ID &&
                        weapon.hasWeaponAbility &&
                        weapon.ivySummonChoice !=
                        null
                }

        val eligibleRuntimeIds =
            eligibleWeapons
                .map {
                    it.runtimeId
                }
                .toSet()

        val removedRuntimeIds =
            schedulersByWeaponRuntimeId
                .keys
                .filter {
                    it !in
                        eligibleRuntimeIds
                }

        removedRuntimeIds
            .forEach {
                    runtimeId ->

                schedulersByWeaponRuntimeId.remove(
                    runtimeId
                )

                entities.removeAll {
                    it.sourceWeaponRuntimeId ==
                        runtimeId
                }
            }

        eligibleWeapons
            .forEach {
                    weapon ->

                val choice =
                    weapon.ivySummonChoice
                        ?: return@forEach

                val existing =
                    schedulersByWeaponRuntimeId[
                        weapon.runtimeId
                    ]

                if (
                    existing == null ||
                    existing.choice !=
                    choice
                ) {
                    schedulersByWeaponRuntimeId[
                        weapon.runtimeId
                    ] =
                        SummonSpawnScheduler(
                            sourceWeaponRuntimeId =
                                weapon.runtimeId,

                            choice =
                                choice
                        )
                }
            }
    }

    private fun spawnWave(
        scheduler: SummonSpawnScheduler,
        playerCenterX: Float,
        playerCenterY: Float
    ) {

        val type =
            summonTypeFor(
                scheduler.choice
            )

        val stats =
            SummonStats.create(
                type =
                    type,

                playerStats =
                    playerStats
            )

        repeat(
            type.defaultCount
        ) {
                index ->

            val angle =
                (
                    MathUtils.PI2 /
                        type.defaultCount
                    ) *
                    index +
                    MathUtils.random(
                        -0.28f,
                        0.28f
                    )

            val spawnDistance =
                62f +
                    index *
                    16f

            entities.add(
                SummonedEntity(
                    sourceWeaponRuntimeId =
                        scheduler
                            .sourceWeaponRuntimeId,

                    type =
                        type,

                    stats =
                        stats,

                    startCenterX =
                        playerCenterX +
                            MathUtils.cos(
                                angle
                            ) *
                            spawnDistance,

                    startCenterY =
                        playerCenterY +
                            MathUtils.sin(
                                angle
                            ) *
                            spawnDistance,

                    lifetimeSeconds =
                        SummonSpawnScheduler
                            .SUMMON_LIFETIME_SECONDS
                )
            )
        }

        scheduler.markWaveSpawned()
    }

    private fun summonTypeFor(
        choice: IvySummonChoice
    ): SummonType {

        return when (choice) {

            IvySummonChoice.SUMMON_ANIMALS ->
                SummonType.COW

            IvySummonChoice.SUMMON_DEMONS ->
                SummonType.IMP

            IvySummonChoice.CALL_THE_KNIGHT ->
                SummonType.KNIGHT_SPIRIT
        }
    }

    companion object {

        private const val IVY_STAFF_ID =
            "WPN_IVY_STAFF"
    }
}
