package com.azizoglu.grisilith.weapon.skill

object DefaultWeaponAbilities {

    const val ERASING_SOULS_ID =
        "WPN_ABILITY_ERASING_SOULS"

    const val ERASING_SOULS_TRIGGER_RADIUS =
        640f

    const val ERASING_SOULS_EXPLOSION_RADIUS =
        92f

    const val ERASING_SOULS_DAMAGE_MULTIPLIER =
        1f

    const val ERASING_SOULS_NECROSIS_DURATION_SECONDS =
        2f

    const val IMPALE_AND_DRAG_ID =
        "WPN_ABILITY_IMPALE_AND_DRAG"

    const val IMPALE_AND_DRAG_DURATION_SECONDS =
        5f

    const val IMPALE_AND_DRAG_COOLDOWN_SECONDS =
        55f

    const val IMPALE_AND_DRAG_MINIMUM_TRAVEL_DISTANCE =
        115f

    const val IMPALE_AND_DRAG_MOVEMENT_THRESHOLD =
        12f

    const val IMPALE_AND_DRAG_MAX_IMPALES_PER_SECOND =
        2

    const val IMPALE_AND_DRAG_MAXIMUM_CORPSES =
        10

    const val IMPALE_AND_DRAG_GOLD_PER_CORPSE =
        3

    const val IMPALE_AND_DRAG_BASE_CHEST_CHANCE_PER_CORPSE =
        0.035f

    const val IMPALE_AND_DRAG_MAXIMUM_CHEST_CHANCE =
        0.45f

    const val STATIC_CLOUDS_ID =
        "WPN_ABILITY_STATIC_CLOUDS"

    const val STATIC_CLOUDS_DURATION_SECONDS =
        5f

    /*
     * Kaynak tasarım 30-45 saniye aralığı veriyor.
     * İlk sabit denge değeri olarak orta nokta kullanılır.
     */
    const val STATIC_CLOUDS_COOLDOWN_SECONDS =
        38f

    const val STATIC_CLOUDS_RADIUS =
        145f

    const val STATIC_CLOUDS_TICK_SECONDS =
        0.50f

    const val STATIC_CLOUDS_LIGHTNING_DPS_MULTIPLIER =
        0.55f

    const val STATIC_CLOUDS_STUN_CHANCE =
        0.05f

    const val STATIC_CLOUDS_STUN_DURATION_SECONDS =
        1f

    const val ACIDIC_POOL_ID =
        "WPN_ABILITY_ACIDIC_POOL"

    const val ACIDIC_POOL_DURATION_SECONDS =
        6f

    const val ACIDIC_POOL_COOLDOWN_SECONDS =
        45f

    const val ACIDIC_POOL_PLACEMENT_RANGE =
        360f

    const val ACIDIC_POOL_RADIUS =
        155f

    const val ACIDIC_POOL_TICK_SECONDS =
        0.50f

    /*
     * Değer DPS çarpanıdır.
     *
     * Tick başına:
     * Active Skill Damage × 0.90 × 0.50
     */
    const val ACIDIC_POOL_DAMAGE_PER_SECOND_MULTIPLIER =
        0.90f

    const val ACIDIC_POOL_CORRODING_DURATION_SECONDS =
        3f

    const val VORTEX_PULL_ID =
        "WPN_ABILITY_VORTEX_PULL"

    /*
     * İlk denge değerleri. Vortex Pull tanımında kesin süre ve
     * cooldown verilmediği için test edilebilir orta değerler seçildi.
     */
    const val VORTEX_PULL_DURATION_SECONDS =
        5f

    const val VORTEX_PULL_COOLDOWN_SECONDS =
        45f

    const val VORTEX_PULL_PLACEMENT_RANGE =
        340f

    const val VORTEX_PULL_RADIUS =
        150f

    const val VORTEX_PULL_DAMAGE_TICK_SECONDS =
        0.25f

    const val VORTEX_PULL_DAMAGE_PER_SECOND_MULTIPLIER =
        0.85f

    const val VORTEX_PULL_NORMAL_PULL_MULTIPLIER =
        1f

    const val VORTEX_PULL_UNIQUE_PULL_MULTIPLIER =
        0.72f

    const val VORTEX_PULL_MINI_BOSS_PULL_MULTIPLIER =
        0.22f

    const val VORTEX_PULL_FINAL_BOSS_PULL_MULTIPLIER =
        0.10f

    const val IGNITE_DASH_ID =
        "WPN_ABILITY_IGNITE_DASH"

    const val IGNITE_DASH_AIM_DURATION_SECONDS =
        3f

    /*
     * Dokümanda cooldown 3-4 stage arası / 50-100 saniye
     * olarak tarif edildiği için ilk denge değeri 75 saniyedir.
     */
    const val IGNITE_DASH_COOLDOWN_SECONDS =
        75f

    const val IGNITE_DASH_MAXIMUM_RANGE =
        430f

    const val IGNITE_DASH_EXPLOSION_RADIUS =
        120f

    const val IGNITE_DASH_DAMAGE_MULTIPLIER =
        3f

    const val IGNITE_DASH_BURN_DURATION_SECONDS =
        3f

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

    val impaleAndDrag =
        WeaponAbilityDefinition(
            id =
                IMPALE_AND_DRAG_ID,

            displayName =
                "Impale and Drag",

            activationType =
                WeaponAbilityActivationType
                    .ACTIVE,

            cooldownSeconds =
                IMPALE_AND_DRAG_COOLDOWN_SECONDS,

            durationSeconds =
                IMPALE_AND_DRAG_DURATION_SECONDS
        )

    val staticClouds =
        WeaponAbilityDefinition(
            id =
                STATIC_CLOUDS_ID,

            displayName =
                "Static Clouds",

            activationType =
                WeaponAbilityActivationType
                    .ACTIVE,

            cooldownSeconds =
                STATIC_CLOUDS_COOLDOWN_SECONDS,

            durationSeconds =
                STATIC_CLOUDS_DURATION_SECONDS
        )

    val acidicPool =
        WeaponAbilityDefinition(
            id =
                ACIDIC_POOL_ID,

            displayName =
                "Acidic Pool",

            activationType =
                WeaponAbilityActivationType
                    .ACTIVE,

            cooldownSeconds =
                ACIDIC_POOL_COOLDOWN_SECONDS,

            durationSeconds =
                ACIDIC_POOL_DURATION_SECONDS
        )

    val vortexPull =
        WeaponAbilityDefinition(
            id =
                VORTEX_PULL_ID,

            displayName =
                "Vortex Pull",

            activationType =
                WeaponAbilityActivationType
                    .ACTIVE,

            cooldownSeconds =
                VORTEX_PULL_COOLDOWN_SECONDS,

            durationSeconds =
                VORTEX_PULL_DURATION_SECONDS
        )

    val igniteDash =
        WeaponAbilityDefinition(
            id =
                IGNITE_DASH_ID,

            displayName =
                "Ignite Dash",

            activationType =
                WeaponAbilityActivationType
                    .ACTIVE,

            cooldownSeconds =
                IGNITE_DASH_COOLDOWN_SECONDS,

            durationSeconds =
                IGNITE_DASH_AIM_DURATION_SECONDS
        )

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

    val erasingSouls =
        WeaponAbilityDefinition(
            id = ERASING_SOULS_ID,
            displayName = "Erasing the Souls",
            activationType =
                WeaponAbilityActivationType.PASSIVE
        )

}
