package com.azizoglu.grisilith.weapon.skill

object DefaultWeaponAbilities {

    const val RAPID_STROKES_ID =
        "WPN_ABILITY_RAPID_STROKES"
    const val RAPID_STROKES_COOLDOWN_SECONDS = 30f
    const val RAPID_STROKES_ATTACKS = 5
    const val RAPID_STROKES_UPGRADED_ATTACKS = 7
    const val RAPID_STROKES_UPGRADED_SIZE_MULTIPLIER = 2.5f

    const val AIR_SLASH_ID =
        "WPN_ABILITY_AIR_SLASH"
    const val AIR_SLASH_COOLDOWN_SECONDS = 35f
    const val AIR_SLASH_RANGE = 350f
    const val AIR_SLASH_UPGRADED_RANGE = 450f
    const val AIR_SLASH_ANGLE_DEGREES = 40f
    const val AIR_SLASH_MISSING_HEALTH_DAMAGE_PER_PERCENT = 0.003f
    const val AIR_SLASH_MAXIMUM_MISSING_HEALTH_PERCENT = 70f

    const val SPIN_ATTACK_ID =
        "WPN_ABILITY_SPIN_ATTACK"
    const val SPIN_ATTACK_COOLDOWN_SECONDS = 40f
    const val SPIN_ATTACK_BASE_RADIUS_MULTIPLIER = 0.75f
    const val SPIN_ATTACK_UPGRADED_RADIUS_MULTIPLIER = 1f
    const val SPIN_ATTACK_UPGRADED_DAMAGE_MULTIPLIER = 1.5f

    const val DEFENSIVE_LINE_BURST_ID =
        "WPN_ABILITY_DEFENSIVE_LINE_BURST"
    const val DEFENSIVE_LINE_BURST_COOLDOWN_SECONDS = 35f
    const val DEFENSIVE_LINE_BURST_RADIUS = 190f
    const val DEFENSIVE_LINE_BURST_AC_DAMAGE_MULTIPLIER = 0.75f
    const val DEFENSIVE_LINE_BURST_KNOCKBACK = 95f

    const val KNIFE_SWARM_ID =
        "WPN_ABILITY_KNIFE_SWARM"
    const val KNIFE_SWARM_COOLDOWN_SECONDS = 20f
    const val KNIFE_SWARM_PROJECTILES = 6
    const val KNIFE_SWARM_UPGRADED_REPEAT_DELAY_SECONDS = 0.25f
    const val KNIFE_SWARM_UPGRADED_ANGLE_OFFSET_DEGREES = 30f
    const val KNIFE_SWARM_RANGE = 600f

    const val DAGGER_BASIC_CRITICAL_DAMAGE_MULTIPLIER = 1.5f

    const val GLYPH_CASTING_ID =
        "WPN_ABILITY_GLYPH_CASTING"

    /*
     * Glyph Motion en fazla 10 saniye boyunca çizim toplar.
     *
     * Başarı ve başarısızlık cooldown'ları klasik
     * WeaponAbilityRuntime cooldown'undan ayrı yönetilecektir;
     * çünkü sonuç çizim değerlendirildikten sonra belli olur.
     */
    const val GLYPH_CASTING_DRAW_DURATION_SECONDS =
        10f

    const val GLYPH_CASTING_SUCCESS_COOLDOWN_SECONDS =
        30f

    const val GLYPH_CASTING_FAILURE_COOLDOWN_SECONDS =
        5f

    const val GLYPH_TRAIL_DAMAGE_TICK_SECONDS =
        1f

    const val GLYPH_TRAIL_WISDOM_DAMAGE_MULTIPLIER =
        0.35f

    const val GLYPH_MINIMUM_DRAW_DURATION_SECONDS =
        0.75f

    const val GLYPH_MINIMUM_PATH_LENGTH =
        120f

    const val GLYPH_MINIMUM_POINT_COUNT =
        12

    const val ERASING_SOULS_ID =
        "WPN_ABILITY_ERASING_SOULS"

    const val ERASING_SOULS_TRIGGER_RADIUS = 450f
    const val ERASING_SOULS_UPGRADED_TRIGGER_RADIUS = 600f

    const val ERASING_SOULS_EXPLOSION_RADIUS =
        100f

    const val ERASING_SOULS_DAMAGE_MULTIPLIER = 1.25f
    const val ERASING_SOULS_UPGRADED_DAMAGE_MULTIPLIER = 2f

    const val ERASING_SOULS_NECROSIS_DURATION_SECONDS =
        2f

    const val IMPALE_AND_DRAG_ID =
        "WPN_ABILITY_IMPALE_AND_DRAG"

    /*
     * Legacy constant name is preserved so older save/build code and the
     * obsolete ImpaleAndDragRuntime class continue to compile.
     * Gameplay now uses the reworked "Impale" mechanic.
     */
    const val IMPALE_AND_DRAG_DURATION_SECONDS =
        7f

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

    const val STATIC_CLOUDS_DURATION_SECONDS = 10f
    const val STATIC_CLOUDS_UPGRADED_DURATION_SECONDS = 12f

    /*
     * Kaynak tasarım 30-45 saniye aralığı veriyor.
     * İlk sabit denge değeri olarak orta nokta kullanılır.
     */
    const val STATIC_CLOUDS_COOLDOWN_SECONDS = 45f
    const val STATIC_CLOUDS_UPGRADED_COOLDOWN_SECONDS = 38f

    const val STATIC_CLOUDS_RADIUS =
        145f

    const val STATIC_CLOUDS_TICK_SECONDS =
        0.33333334f

    const val STATIC_CLOUDS_LIGHTNING_DPS_MULTIPLIER =
        0.000165f

    const val STATIC_CLOUDS_STUN_CHANCE = 0.05f
    const val STATIC_CLOUDS_UPGRADED_STUN_CHANCE = 0.075f

    const val STATIC_CLOUDS_STUN_DURATION_SECONDS =
        1f

    const val CHOOSE_TO_SUMMON_ID =
        "WPN_ABILITY_CHOOSE_TO_SUMMON"

    const val IVY_STAFF_PROJECTILE_SIZE_MULTIPLIER =
        1.18f

    const val IVY_STAFF_ARMOR_CLASS_REDUCTION =
        1

    const val IVY_STAFF_ARMOR_CLASS_REDUCTION_SECONDS =
        3f

    const val ACIDIC_POOL_ID =
        "WPN_ABILITY_ACIDIC_POOL"

    const val ACIDIC_POOL_DURATION_SECONDS = 10f
    const val ACIDIC_POOL_UPGRADED_DURATION_SECONDS = 15f

    const val ACIDIC_POOL_COOLDOWN_SECONDS = 50f
    const val ACIDIC_POOL_UPGRADED_COOLDOWN_SECONDS = 44f

    const val ACIDIC_POOL_PLACEMENT_RANGE = 550f
    const val ACIDIC_POOL_UPGRADED_PLACEMENT_RANGE = 660f

    const val ACIDIC_POOL_RADIUS =
        165f

    const val ACIDIC_POOL_TICK_SECONDS =
        0.25f

    /* Target Max HP ratio per (Wisdom + related magical stat), per tick. */
    const val ACIDIC_POOL_DAMAGE_PER_SECOND_MULTIPLIER =
        0.00015f

    const val ACIDIC_POOL_CORRODING_DURATION_SECONDS =
        3f

    const val VORTEX_PULL_ID =
        "WPN_ABILITY_VORTEX_PULL"

    /*
     * İlk denge değerleri. Vortex Pull tanımında kesin süre ve
     * cooldown verilmediği için test edilebilir orta değerler seçildi.
     */
    const val VORTEX_PULL_DURATION_SECONDS =
        13f

    const val VORTEX_PULL_COOLDOWN_SECONDS = 50f
    const val VORTEX_PULL_UPGRADED_COOLDOWN_SECONDS = 42f

    const val VORTEX_PULL_PLACEMENT_RANGE =
        650f

    const val VORTEX_PULL_RADIUS =
        195f

    const val VORTEX_PULL_DAMAGE_TICK_SECONDS = 0.25f
    const val VORTEX_PULL_UPGRADED_DAMAGE_TICK_SECONDS = 0.16666667f

    const val VORTEX_PULL_DAMAGE_PER_SECOND_MULTIPLIER =
        0.00010f

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
    const val IGNITE_DASH_COOLDOWN_SECONDS = 50f
    const val IGNITE_DASH_UPGRADED_COOLDOWN_SECONDS = 45f

    const val IGNITE_DASH_MAXIMUM_RANGE = 315f
    const val IGNITE_DASH_UPGRADED_MAXIMUM_RANGE = 450f

    const val IGNITE_DASH_EXPLOSION_RADIUS =
        120f

    const val IGNITE_DASH_OUTER_EXPLOSION_RADIUS =
        250f

    const val IGNITE_DASH_OUTER_DAMAGE_RATIO =
        0.40f

    const val IGNITE_DASH_DAMAGE_MULTIPLIER = 7.9125f
    const val IGNITE_DASH_UPGRADED_DAMAGE_MULTIPLIER = 10.55f

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
        3f

    const val DEFENSIVE_LINE_BASE_LIMIT =
        15

    const val EXECUTIONER_ID =
        "WPN_ABILITY_EXECUTIONER"

    const val RELENTLESS_ID =
        "WPN_ABILITY_RELENTLESS"

    const val RELENTLESS_DAMAGE_PER_CONSECUTIVE_HIT =
        0.035f

    const val RELENTLESS_MAXIMUM_DAMAGE_BONUS =
        0.50f

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
        3f

    const val ASSASSINATE_DAMAGE_MULTIPLIER =
        2f

    const val ASSASSINATE_COOLDOWN_SECONDS =
        5f

    const val ASSASSINATE_PROJECTILE_SIZE_MULTIPLIER =
        1.8f

    val glyphCasting =
        WeaponAbilityDefinition(
            id =
                GLYPH_CASTING_ID,

            displayName =
                "Glyph Casting",

            activationType =
                WeaponAbilityActivationType
                    .ACTIVE,

            /*
             * Başarı/başarısızlık sonucuna göre 30/5 saniyelik
             * cooldown Glyph Motion runtime tarafından uygulanır.
             * Genel ability runtime burada ek cooldown başlatmaz.
             */
            cooldownSeconds =
                0f,

            durationSeconds =
                GLYPH_CASTING_DRAW_DURATION_SECONDS
        )

    val impaleAndDrag =
        WeaponAbilityDefinition(
            id =
                IMPALE_AND_DRAG_ID,

            displayName =
                "Impale",

            activationType =
                WeaponAbilityActivationType
                    .ACTIVE,

            cooldownSeconds =
                IMPALE_AND_DRAG_COOLDOWN_SECONDS,

            durationSeconds =
                IMPALE_AND_DRAG_DURATION_SECONDS
        )

    /*
     * Clean public name for the reworked ability.
     * impaleAndDrag remains as a compatibility alias used by existing
     * catalog definitions.
     */
    val impale =
        impaleAndDrag

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

    val staticCloudsUpgraded =
        staticClouds.copy(
            cooldownSeconds = STATIC_CLOUDS_UPGRADED_COOLDOWN_SECONDS,
            durationSeconds = STATIC_CLOUDS_UPGRADED_DURATION_SECONDS
        )

    val chooseToSummon =
        WeaponAbilityDefinition(
            id =
                CHOOSE_TO_SUMMON_ID,

            displayName =
                "Choose to Summon",

            activationType =
                WeaponAbilityActivationType
                    .PASSIVE
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

    val acidicPoolUpgraded =
        acidicPool.copy(
            cooldownSeconds = ACIDIC_POOL_UPGRADED_COOLDOWN_SECONDS,
            durationSeconds = ACIDIC_POOL_UPGRADED_DURATION_SECONDS
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

    val vortexPullUpgraded =
        vortexPull.copy(
            cooldownSeconds = VORTEX_PULL_UPGRADED_COOLDOWN_SECONDS
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

    val igniteDashUpgraded =
        igniteDash.copy(
            cooldownSeconds = IGNITE_DASH_UPGRADED_COOLDOWN_SECONDS
        )

    val heavyBolts =
        WeaponAbilityDefinition(
            id =
                HEAVY_BOLTS_ID,

            displayName =
                "Heavy Bolts",

            activationType =
                WeaponAbilityActivationType
                    .ACTIVE,

            cooldownSeconds =
                30f,

            durationSeconds =
                2f
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
                WeaponAbilityActivationType.ACTIVE,
            cooldownSeconds = 25f,
            durationSeconds = 1f
        )

    val rapidStrokes =
        WeaponAbilityDefinition(
            id = RAPID_STROKES_ID,
            displayName = "Rapid Strokes",
            activationType = WeaponAbilityActivationType.ACTIVE,
            cooldownSeconds = RAPID_STROKES_COOLDOWN_SECONDS
        )

    val airSlash =
        WeaponAbilityDefinition(
            id = AIR_SLASH_ID,
            displayName = "Air Slash",
            activationType = WeaponAbilityActivationType.ACTIVE,
            cooldownSeconds = AIR_SLASH_COOLDOWN_SECONDS
        )

    val spinAttack =
        WeaponAbilityDefinition(
            id = SPIN_ATTACK_ID,
            displayName = "Spin Attack",
            activationType = WeaponAbilityActivationType.ACTIVE,
            cooldownSeconds = SPIN_ATTACK_COOLDOWN_SECONDS
        )

    val defensiveLineBurst =
        WeaponAbilityDefinition(
            id = DEFENSIVE_LINE_BURST_ID,
            displayName = "Defensive Line Burst",
            activationType = WeaponAbilityActivationType.ACTIVE,
            cooldownSeconds = DEFENSIVE_LINE_BURST_COOLDOWN_SECONDS
        )

    val knifeSwarm =
        WeaponAbilityDefinition(
            id = KNIFE_SWARM_ID,
            displayName = "Knife Swarm",
            activationType = WeaponAbilityActivationType.ACTIVE,
            cooldownSeconds = KNIFE_SWARM_COOLDOWN_SECONDS
        )

    val relentless =
        WeaponAbilityDefinition(
            id =
                RELENTLESS_ID,

            displayName =
                "Relentless",

            activationType =
                WeaponAbilityActivationType
                    .PASSIVE
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
