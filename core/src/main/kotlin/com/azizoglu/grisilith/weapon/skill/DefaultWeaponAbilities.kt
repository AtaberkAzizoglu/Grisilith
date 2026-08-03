package com.azizoglu.grisilith.weapon.skill

object DefaultWeaponAbilities {

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
