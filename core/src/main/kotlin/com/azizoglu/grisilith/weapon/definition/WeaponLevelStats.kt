package com.azizoglu.grisilith.weapon.definition

import com.azizoglu.grisilith.content.StatModifier

data class WeaponLevelStats(
    val level: Int,

    /*
     * Level 1 taban değerdir.
     * Sonraki değerler kümülatif çarpanlardır.
     */
    val damageMultiplier: Float = 1f,
    val attackSpeedMultiplier: Float = 1f,
    val rangeMultiplier: Float = 1f,
    val knockbackMultiplier: Float = 1f,

    /*
     * Projectile veya piercing saldırılarının
     * vurabileceği ek hedef sayısı.
     */
    val additionalTargets: Int = 0,

    /*
     * Level yükselmesinin verdiği garantili
     * character stat bonusları.
     */
    val grantedStatModifiers:
    List<StatModifier> =
        emptyList(),

    /*
     * Level 3'te normal saldırı geliştirmesi açılır.
     */
    val basicAttackUpgraded: Boolean = false,

    /*
     * Level 5'te silaha özgü yetenek açılır.
     */
    val weaponAbilityUnlocked: Boolean = false
) {
    init {
        require(level in 1..5) {
            "Weapon level must be between 1 and 5."
        }

        require(damageMultiplier > 0f)
        require(attackSpeedMultiplier > 0f)
        require(rangeMultiplier > 0f)
        require(knockbackMultiplier >= 0f)
        require(additionalTargets >= 0)

        if (basicAttackUpgraded) {
            require(level >= 3) {
                "Basic attack upgrade cannot unlock before level 3."
            }
        }

        if (weaponAbilityUnlocked) {
            require(level >= 5) {
                "Weapon ability cannot unlock before level 5."
            }
        }
    }
}
