package com.azizoglu.grisilith.statistics

import com.azizoglu.grisilith.save.RunSaveStatLayers
import kotlin.math.roundToInt

class PlayerStats(
    initialBaseScores: Map<StatType, Int> =
        defaultBaseScores()
) {
    companion object {
        const val MINIMUM_SCORE = 0
        const val PRE_RUN_MAXIMUM_SCORE = 25
        const val RUN_MAXIMUM_SCORE = 50

        private fun defaultBaseScores(): Map<StatType, Int> {
            return StatType.entries.associateWith {
                10
            }
        }
    }

    private val baseScores =
        mutableMapOf<StatType, Int>()

    private val gearBonuses =
        mutableMapOf<StatType, Int>()

    private val runBonuses =
        mutableMapOf<StatType, Int>()

    private val weaponBonuses =
        mutableMapOf<StatType, Int>()

    private val itemBonuses =
        mutableMapOf<StatType, Int>()

    private val tamgaBonuses =
        mutableMapOf<StatType, Int>()

    /*
     * Süreli Active Tamga bonusları, Holy Lights gibi
     * pasif Tamga bonuslarından ayrı tutulur.
     */
    private val activeTamgaBonuses =
        mutableMapOf<StatType, Int>()

    init {
        StatType.entries.forEach { statType ->
            val initialValue =
                initialBaseScores[statType] ?: 10

            baseScores[statType] =
                initialValue.coerceIn(
                    MINIMUM_SCORE,
                    PRE_RUN_MAXIMUM_SCORE
                )

            gearBonuses[statType] = 0
            weaponBonuses[statType] = 0
            itemBonuses[statType] = 0
            runBonuses[statType] = 0
            tamgaBonuses[statType] = 0
            activeTamgaBonuses[statType] = 0
        }
    }

    fun baseScore(
        statType: StatType
    ): Int {
        return baseScores[statType] ?: MINIMUM_SCORE
    }

    fun gearBonus(
        statType: StatType
    ): Int {
        return gearBonuses[statType] ?: 0
    }

    fun weaponBonus(
        statType: StatType
    ): Int {
        return weaponBonuses[statType]
            ?: 0
    }

    fun itemBonus(
        statType: StatType
    ): Int {
        return itemBonuses[statType]
            ?: 0
    }

    fun runBonus(
        statType: StatType
    ): Int {
        return runBonuses[statType] ?: 0
    }

    fun tamgaBonus(
        statType: StatType
    ): Int {

        return tamgaBonuses[statType]
            ?: 0
    }

    fun activeTamgaBonus(
        statType: StatType
    ): Int {
        return activeTamgaBonuses[statType] ?: 0
    }

    fun totalScore(
        statType: StatType
    ): Int {
        return (
            totalScoreWithoutTamga(
                statType
            ) +
                tamgaBonus(
                    statType
                ) +
                activeTamgaBonus(
                    statType
                )
            )
            .coerceIn(
                MINIMUM_SCORE,
                RUN_MAXIMUM_SCORE
            )
    }

    /*
 * Aura'nın hangi Stat'ı seçeceğini hesaplarken
 * Tamga'nın kendi verdiği bonus dahil edilmez.
 */
    fun totalScoreWithoutTamga(
        statType: StatType
    ): Int {

        return (
            baseScore(statType) +
                gearBonus(statType) +
                weaponBonus(statType) +
                itemBonus(statType) +
                runBonus(statType)
            )
            .coerceIn(
                MINIMUM_SCORE,
                RUN_MAXIMUM_SCORE
            )
    }

    fun setTamgaBonuses(
        bonuses: Map<StatType, Int>
    ) {
        StatType.entries.forEach { statType ->
            tamgaBonuses[statType] =
                bonuses[statType]
                    ?: 0
        }
    }

    fun clearTamgaBonuses() {
        StatType.entries.forEach { statType ->
            tamgaBonuses[statType] = 0
        }
    }


    fun setActiveTamgaBonuses(
        bonuses: Map<StatType, Int>
    ) {
        StatType.entries.forEach { statType ->
            activeTamgaBonuses[statType] =
                bonuses[statType] ?: 0
        }
    }

    fun clearActiveTamgaBonuses() {
        StatType.entries.forEach { statType ->
            activeTamgaBonuses[statType] = 0
        }
    }

    fun addRunBonus(
        statType: StatType,
        amount: Int
    ): Int {
        if (amount <= 0) {
            return 0
        }

        val previousTotal =
            totalScore(statType)

        val currentRunBonus =
            runBonus(statType)

        runBonuses[statType] =
            currentRunBonus + amount

        val newTotal =
            totalScore(statType)

        return newTotal - previousTotal
    }

    fun addGearBonus(
        statType: StatType,
        amount: Int
    ): Int {
        if (amount == 0) {
            return 0
        }

        val previousTotal =
            totalScore(statType)

        val currentGearBonus =
            gearBonus(statType)

        gearBonuses[statType] =
            currentGearBonus + amount

        val newTotal =
            totalScore(statType)

        return newTotal - previousTotal
    }

    /*
 * Preset Gear'larından gelen bütün bonusları
 * tek seferde yeniler.
 *
 * Şimdilik yalnızca Armor kullanır.
 * Daha sonra Armor Tamga da bu hesaplamaya
 * dahil edilecektir.
 */
    fun setGearBonuses(
        bonuses:
        Map<StatType, Int>
    ) {
        StatType.entries
            .forEach { statType ->

                gearBonuses[
                    statType
                ] =
                    bonuses[
                        statType
                    ] ?: 0
            }
    }

    fun setWeaponBonuses(
        bonuses: Map<StatType, Int>
    ) {
        StatType.entries.forEach { statType ->
            weaponBonuses[statType] =
                bonuses[statType]
                    ?: 0
        }
    }

    fun setItemBonuses(
        bonuses: Map<StatType, Int>
    ) {
        StatType.entries.forEach { statType ->
            itemBonuses[statType] =
                bonuses[statType]
                    ?: 0
        }
    }

    fun clearGearBonuses() {
        StatType.entries
            .forEach { statType ->
                gearBonuses[
                    statType
                ] =
                    0
            }
    }

    fun clearWeaponBonuses() {
        StatType.entries.forEach { statType ->
            weaponBonuses[statType] = 0
        }
    }

    fun clearItemBonuses() {
        StatType.entries.forEach { statType ->
            itemBonuses[statType] = 0
        }
    }

    fun setBaseScore(
        statType: StatType,
        score: Int
    ) {
        baseScores[statType] =
            score.coerceIn(
                MINIMUM_SCORE,
                PRE_RUN_MAXIMUM_SCORE
            )
    }

    fun clearRunBonuses() {
        StatType.entries.forEach { statType ->
            runBonuses[statType] = 0
        }
    }


    fun calculatedMaximumHealth(): Int {
        val score =
            totalScore(StatType.MAX_HP)

        return (
            100 +
                (score - 10) * 10
            ).coerceAtLeast(10)
    }

    fun calculatedMovementSpeed(): Float {
        val score =
            totalScore(StatType.MOVEMENT_SPEED)

        val multiplier =
            1f + (score - 10) * 0.04f

        return (
            260f * multiplier
            ).coerceAtLeast(80f)
    }

    fun attackSpeedMultiplier(): Float {
        val score =
            totalScore(StatType.ATTACK_SPEED)

        return (
            1f + (score - 10) * 0.05f
            ).coerceAtLeast(0.20f)
    }

    fun hasteAttackSpeedMultiplier(): Float {
        val score =
            totalScore(
                StatType.HASTE
            )

        /*
         * Yalnızca Spell Focus normal saldırıları:
         * Staff, Wand ve The Book.
         *
         * Haste 10 = x1.00
         * Her puan ±%5 saldırı hızı.
         *
         * Haste 0  = x0.50
         * Haste 10 = x1.00
         * Haste 20 = x1.50
         * Haste 30 = x2.00
         */
        return (
            1f +
                (score - 10) *
                0.05f
            )
            .coerceAtLeast(0.20f)
    }

    fun armorDamageMultiplier(): Float {
        val armorScore =
            totalScore(
                StatType.ARMOR_CLASS
            )

        val damageReduction =
            armorScore.toFloat() /
                (armorScore + 50f)

        return (
            1f - damageReduction
            ).coerceIn(
                0.25f,
                1f
            )
    }

    fun hpRegenerationPerSecond(): Float {
        val score =
            totalScore(
                StatType.HP_REGENERATION
            )

        return (
            score - 10
            )
            .coerceAtLeast(0) *
            0.20f
    }

    fun lifeStealChance(): Float {
        val score =
            totalScore(
                StatType.LIFE_STEAL
            )

        return (
            score - 10
            )
            .coerceAtLeast(0) *
            0.015f
    }

    fun lightWeaponPowerScore(): Int {
        val strength =
            totalScore(
                StatType.STRENGTH
            )

        val dexterity =
            totalScore(
                StatType.DEXTERITY
            )

        return maxOf(
            strength,
            dexterity
        )
    }

    fun starterWeaponDamage(): Int {
        val powerScore =
            lightWeaponPowerScore()

        val multiplier =
            1f +
                (powerScore - 10) *
                0.08f

        return (
            20f * multiplier
            )
            .roundToInt()
            .coerceAtLeast(1)
    }

    fun criticalHitChance(): Float {
        val score =
            totalScore(
                StatType.CRIT_RATE
            )

        return (
            0.05f +
                (score - 10) *
                0.02f
            )
            .coerceIn(
                0f,
                0.95f
            )
    }

    fun criticalDamageMultiplier(): Float {
        val score =
            totalScore(
                StatType.CRIT_DAMAGE
            )

        return (
            1.50f +
                (score - 10) *
                0.05f
            )
            .coerceAtLeast(1f)
    }

    fun enemyCountMultiplier(): Float {
        val score =
            totalScore(
                StatType.ENEMY_COUNT
            )

        return (
            1f +
                (score - 10) *
                0.05f
            )
            .coerceAtLeast(0.25f)
    }

    fun magicPowerMultiplier(): Float {
        val score =
            totalScore(
                StatType.WISDOM
            )

        return (
            1f +
                (score - 10) *
                0.08f
            )
            .coerceAtLeast(0.20f)
    }

    fun luckScore(): Int {
        return totalScore(
            StatType.LUCK
        )
    }

    fun harvestGoldMultiplier(): Float {
        val score =
            totalScore(
                StatType.HARVEST
            )

        return (
            1f +
                (score - 10) *
                0.04f
            )
            .coerceAtLeast(0.20f)
    }

    fun projectileTargetCount(): Int {
        val score =
            totalScore(
                StatType.PIERCING
            )

        val additionalTargets =
            (score - 10)
                .coerceAtLeast(0) /
                5

        return 1 + additionalTargets
    }

    /*
     * Save sistemi için bütün kalıcı Stat katmanlarını dışarı aktarır.
     *
     * Anahtar olarak StatType.name kullanılır. displayName
     * kullanılmaz; böylece görsel metin değişiklikleri save
     * dosyasını bozmaz.
     */
    fun exportSaveLayers():
        RunSaveStatLayers {

        return RunSaveStatLayers(
            baseScores =
                encodeLayer(
                    baseScores
                ),

            gearBonuses =
                encodeLayer(
                    gearBonuses
                ),

            weaponBonuses =
                encodeLayer(
                    weaponBonuses
                ),

            itemBonuses =
                encodeLayer(
                    itemBonuses
                ),

            runBonuses =
                encodeLayer(
                    runBonuses
                ),

            tamgaBonuses =
                encodeLayer(
                    tamgaBonuses
                ),

            activeTamgaBonuses =
                encodeLayer(
                    activeTamgaBonuses
                )
        )
    }

    /*
     * Save dosyasındaki Stat katmanlarını mevcut PlayerStats
     * nesnesine uygular.
     *
     * Bilinmeyen Stat anahtarları yok sayılır. Böylece ileride
     * kaldırılmış veya yeniden adlandırılmış bir Stat, bütün save
     * yüklemesini çökertmez.
     *
     * Base değer eksikse constructor tarafından kurulmuş mevcut
     * Character / Preset değeri korunur. Bonus katmanında eksik
     * değer varsa güvenli biçimde 0 kabul edilir.
     */
    fun restoreSaveLayers(
        layers:
        RunSaveStatLayers
    ) {

        val restoredBaseScores =
            decodeLayer(
                layers.baseScores
            )

        val restoredGearBonuses =
            decodeLayer(
                layers.gearBonuses
            )

        val restoredWeaponBonuses =
            decodeLayer(
                layers.weaponBonuses
            )

        val restoredItemBonuses =
            decodeLayer(
                layers.itemBonuses
            )

        val restoredRunBonuses =
            decodeLayer(
                layers.runBonuses
            )

        val restoredTamgaBonuses =
            decodeLayer(
                layers.tamgaBonuses
            )

        val restoredActiveTamgaBonuses =
            decodeLayer(
                layers.activeTamgaBonuses
            )

        StatType.entries
            .forEach { statType ->

                restoredBaseScores[
                    statType
                ]
                    ?.let {
                            restoredValue ->

                        baseScores[
                            statType
                        ] =
                            restoredValue
                                .coerceIn(
                                    MINIMUM_SCORE,
                                    PRE_RUN_MAXIMUM_SCORE
                                )
                    }

                gearBonuses[
                    statType
                ] =
                    restoredGearBonuses[
                        statType
                    ] ?: 0

                weaponBonuses[
                    statType
                ] =
                    restoredWeaponBonuses[
                        statType
                    ] ?: 0

                itemBonuses[
                    statType
                ] =
                    restoredItemBonuses[
                        statType
                    ] ?: 0

                runBonuses[
                    statType
                ] =
                    restoredRunBonuses[
                        statType
                    ] ?: 0

                tamgaBonuses[
                    statType
                ] =
                    restoredTamgaBonuses[
                        statType
                    ] ?: 0

                activeTamgaBonuses[
                    statType
                ] =
                    restoredActiveTamgaBonuses[
                        statType
                    ] ?: 0
            }
    }

    private fun encodeLayer(
        layer:
        Map<StatType, Int>
    ): Map<String, Int> {

        return StatType.entries
            .associate {
                    statType ->

                statType.name to
                    (
                        layer[
                            statType
                        ] ?: 0
                        )
            }
    }

    private fun decodeLayer(
        encodedLayer:
        Map<String, Int>
    ): Map<StatType, Int> {

        return encodedLayer
            .mapNotNull {
                    entry ->

                val statType =
                    StatType.entries
                        .firstOrNull {
                                candidate ->

                            candidate.name ==
                                entry.key
                        }
                        ?: return@mapNotNull null

                statType to
                    entry.value
            }
            .toMap()
    }

}
