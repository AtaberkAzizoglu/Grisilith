package com.azizoglu.grisilith.weapon.skill

object DefaultWeaponAbilities {

    const val HEAVY_BOLTS_ID =
        "WPN_ABILITY_HEAVY_BOLTS"

    const val HEAVY_BOLT_ATTACK_INTERVAL =
        10

    const val HEAVY_BOLT_CAST_SECONDS =
        0.5f

    const val HEAVY_BOLT_DAMAGE_MULTIPLIER =
        2f

    const val HEAVY_BOLT_FIRST_BOSS_MULTIPLIER =
        1.5f

    const val HEAVY_BOLT_SIZE_MULTIPLIER =
        2.5f

    const val DEFENSIVE_LINE_ID =
        "WPN_ABILITY_DEFENSIVE_LINE"

    const val LEISURELY_SHOT_ID =
        "WPN_ABILITY_LEISURELY_SHOT"

    const val DEFENSIVE_LINE_SECONDS_PER_POINT =
        1f

    const val DEFENSIVE_LINE_START_DELAY_SECONDS =
        2f

    const val DEFENSIVE_LINE_BASE_LIMIT =
        15

    const val EXECUTIONER_ID =
        "WPN_ABILITY_EXECUTIONER"

    const val ASSASSINATE_ID =
        "WPN_ABILITY_ASSASSINATE"

    /*
     * Initial balancing values.
     *
     * Centralized here so later balance passes do not require
     * editing executors or runtime classes.
     */
    const val EXECUTIONER_HEALTH_THRESHOLD =
        0.15f

    const val EXECUTIONER_COOLDOWN_SECONDS =
        5f

    const val ASSASSINATE_DAMAGE_MULTIPLIER =
        2f

    const val ASSASSINATE_COOLDOWN_SECONDS =
        5f

    const val ASSASSINATE_PROJECTILE_SIZE_MULTIPLIER =
        1.8f

    val heavyBolts =
        WeaponAbilityDefinition(
            id =
                HEAVY_BOLTS_ID,

            displayName =
                "Heavy Bolts",

            activationType =
                WeaponAbilityActivationType
                    .PASSIVE
        )

    val defensiveLine =
        WeaponAbilityDefinition(
            id = DEFENSIVE_LINE_ID,
            displayName = "Defensive Line",
            activationType =
                WeaponAbilityActivationType.PASSIVE
        )

    val leisurelyShot =
        WeaponAbilityDefinition(
            id = LEISURELY_SHOT_ID,
            displayName = "Leisurely Shot",
            activationType =
                WeaponAbilityActivationType.PASSIVE
        )

    val executioner =
        WeaponAbilityDefinition(
            id =
                EXECUTIONER_ID,

            displayName =
                "Executioner",

            activationType =
                WeaponAbilityActivationType
                    .AUTOMATIC,

            cooldownSeconds =
                EXECUTIONER_COOLDOWN_SECONDS
        )

    val assassinate =
        WeaponAbilityDefinition(
            id =
                ASSASSINATE_ID,

            displayName =
                "Assassinate",

            activationType =
                WeaponAbilityActivationType
                    .AUTOMATIC,

            cooldownSeconds =
                ASSASSINATE_COOLDOWN_SECONDS
        )
}
