package com.azizoglu.grisilith.ui.icon

import com.azizoglu.grisilith.content.ContentCategory
import com.azizoglu.grisilith.content.ContentDefinition
import com.azizoglu.grisilith.item.definition.ItemDefinition
import com.azizoglu.grisilith.item.definition.ItemRarity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle

/**
 * Shared, data-driven renderer for collection and gear icons.
 *
 * Frames and foreground artwork remain separate at runtime. This keeps every
 * frame visually identical and lets the lock symbol be applied without
 * creating a second copy of every icon.
 */
object ContentIconRenderer {
    enum class SummonFrame { PURPLE, RED, GREEN, BLUE }
    private val summonCrops = mutableMapOf<String, IntArray>()

    private fun summonCrop(path: String): IntArray = summonCrops.getOrPut(path) {
        val pixels = com.badlogic.gdx.graphics.Pixmap(Gdx.files.internal(path))
        try {
            var left=pixels.width;var top=pixels.height;var right=-1;var bottom=-1
            for(y in 0 until pixels.height) for(x in 0 until pixels.width) {
                if((pixels.getPixel(x,y) and 255)>32) {
                    left=minOf(left,x);top=minOf(top,y);right=maxOf(right,x);bottom=maxOf(bottom,y)
                }
            }
            if(right<left) intArrayOf(0,0,pixels.width,pixels.height)
            else intArrayOf(left,top,right-left+1,bottom-top+1)
        } finally { pixels.dispose() }
    }
    private const val ROOT = "ui/content_icons/"
    private const val FRAME_SHEET = ROOT + "frames/gear_base_symbols.png"
    private const val LOCK_OVERLAY = ROOT + "frames/locked_overlay.png"
    private const val ICON_INSET_RATIO = 0.115f

    private data class FrameRegion(
        val x: Int,
        val y: Int,
        val width: Int,
        val height: Int
    )

    private enum class FrameKind(val region: FrameRegion) {
        GEAR(FrameRegion(37, 201, 303, 303)),
        UNCOMMON(FrameRegion(1107, 201, 306, 303)),
        RARE(FrameRegion(203, 567, 309, 302)),
        LEGENDARY(FrameRegion(571, 568, 304, 300)),
        ARTIFACT(FrameRegion(933, 567, 308, 301))
    }

    private val iconPaths = mapOf(
        "MAP_ROAD_TO_HOME" to ROOT + "maps/crolymnos_forest_v1.png",
        "MAP_TROUBLE_AT_HOME" to ROOT + "maps/vogtsweier_v1.png",
        "MAP_FOLLOWING_THE_DUNGEON_DELVER" to ROOT + "maps/hollowgrave_depths_v1.png",
        // Weapons
        "WPN_WARHAMMER" to ROOT + "weapons/wpn_warhammer.png",
        "WPN_WHIP" to ROOT + "weapons/wpn_whip.png",
        "WPN_SLING" to ROOT + "weapons/wpn_sling.png",
        "WPN_BOOMERANG" to ROOT + "weapons/wpn_boomerang.png",
        "WPN_DOUBLE_SWORDS" to ROOT + "weapons/wpn_double_swords.png",
        "WPN_LONGSWORD" to ROOT + "weapons/wpn_longsword.png",
        "WPN_LANCE" to ROOT + "weapons/wpn_lance.png",
        "WPN_SHIELD" to ROOT + "weapons/wpn_shield.png",
        "WPN_DAGGER" to ROOT + "weapons/wpn_dagger.png",
        "WPN_LONGBOW" to ROOT + "weapons/wpn_longbow.png",
        "WPN_CROSSBOW" to ROOT + "weapons/wpn_crossbow.png",
        "WPN_IVY_STAFF" to ROOT + "weapons/wpn_ivy_staff.png",
        "WPN_STEAM_WAND" to ROOT + "weapons/wpn_steam_wand.png",
        "WPN_TOME_OF_ERYNDOR" to ROOT + "weapons/wpn_tome_of_eryndor.png",
        "WPN_ORB_OF_SKY" to ROOT + "weapons/wpn_orb_of_sky.png",
        "WPN_TOTEM_OF_DARK_CLOUDS" to ROOT + "weapons/wpn_totem_of_dark_clouds.png",
        "WPN_AMULET_OF_TROLL_KING" to ROOT + "weapons/wpn_amulet_of_troll_king.png",
        "WPN_ENCHANTED_CHALK_DUST" to ROOT + "weapons/wpn_enchanted_chalk_dust.png",

        // Armor
        "ARMOR_LEATHER" to ROOT + "armor/armor_leather.png",
        "ARMOR_SCALE_MAIL" to ROOT + "armor/armor_scale_mail.png",
        "ARMOR_PLATE" to ROOT + "armor/armor_plate.png",
        "ARMOR_ROBE_OF_MAGE" to ROOT + "armor/armor_robe_of_mage.png",
        "ARMOR_PADDED_GAMBESON" to ROOT + "armor/armor_padded_gambeson.png",
        "ARMOR_BARNACLE" to ROOT + "armor/armor_barnacle.png",
        "ARMOR_FORTRESS" to ROOT + "armor/armor_fortress.png",
        "ARMOR_ARCANE_SILK_ROBE" to ROOT + "armor/armor_arcane_silk_robe.png",

        // Tamgas
        "TMG_CHARGER" to ROOT + "tamgas/tmg_charger.png",
        "TMG_HEIGHT_INSECURITY" to ROOT + "tamgas/tmg_height_insecurity.png",
        "TMG_MOUNTED_COMBATANT" to ROOT + "tamgas/tmg_mounted_combatant.png",
        "TMG_HOLY_LIGHTS" to ROOT + "tamgas/tmg_holy_lights.png",
        "TMG_ARCANE_AURA" to ROOT + "tamgas/tmg_arcane_aura.png",
        "TMG_FLAME_BLADES" to ROOT + "tamgas/tmg_flame_blades.png",
        "TMG_QUICK_SHOT" to ROOT + "tamgas/tmg_quick_shot.png",
        "TMG_PANIC" to ROOT + "tamgas/tmg_panic.png",

        // Artifact items currently supplied by the user
        "ITEM_DISCARD_VOUCHER" to ROOT + "items/item_discard_voucher.png",
        "ITEM_BOOK_OF_EXPERTIZE" to ROOT + "items/item_book_of_expertize.png",
        "ITEM_HUNTERS_REWARD" to ROOT + "items/item_hunters_reward.png",
        "ITEM_JACK_OF_ALL_TRADES" to ROOT + "items/item_jack_of_all_trades.png",
        "ITEM_LONG_SHOT" to ROOT + "items/item_long_shot.png",
        "ITEM_ONE_WEAPON_ARMY" to ROOT + "items/item_one_weapon_army.png",
        "ITEM_OVERFLOWING_POWER" to ROOT + "items/item_overflowing_power.png",
        "ITEM_SPECIALIST" to ROOT + "items/item_specialist.png",
        "ITEM_SUMMONED_LEGACY" to ROOT + "items/item_summoned_legacy.png",

        // Legendary items
        "ITEM_EXECUTIONERS_MARK" to ROOT + "items/item_executioners_mark.png",
        "ITEM_GLASS_HEART" to ROOT + "items/item_glass_heart.png",
        "ITEM_RIGHT_INVESTMENT" to ROOT + "items/item_right_investment.png",
        "ITEM_COMPRESSED_SPACE" to ROOT + "items/item_compressed_space.png",
        "ITEM_ARSENAL" to ROOT + "items/item_arsenal.png",
        "ITEM_JEALOUS_WEAPON" to ROOT + "items/item_jealous_weapon.png",
        "ITEM_WEAKEST_LINK" to ROOT + "items/item_weakest_link.png",
        "ITEM_CONVERSION_HEALTH" to ROOT + "items/item_conversion_health.png",
        "ITEM_CONVERSION_ARMOR_CLASS" to ROOT + "items/item_conversion_armor_class.png",
        "ITEM_CONVERSION_SHEPHERD" to ROOT + "items/item_conversion_shepherd.png",
        "ITEM_CONVERSION_ATTACK_SPEED" to ROOT + "items/item_conversion_attack_speed.png",
        "ITEM_CONVERSION_MOVEMENT_SPEED" to ROOT + "items/item_conversion_movement_speed.png",
        "ITEM_CONVERSION_ENEMY_COUNT" to ROOT + "items/item_conversion_enemy_count.png",
        "ITEM_BLOOD_SACRIFICE" to ROOT + "items/item_blood_sacrifice.png",
        "ITEM_SECOND_HEART" to ROOT + "items/item_second_heart.png",
        "ITEM_BROKEN_HEART" to ROOT + "items/item_broken_heart.png",
        "ITEM_CROWD_HUNGER" to ROOT + "items/item_crowd_hunger.png",
        "ITEM_SHARED_BLOOD" to ROOT + "items/item_shared_blood.png",
        "ITEM_FEAR_OF_DEATH" to ROOT + "items/item_fear_of_death.png",
        "ITEM_CASH_OUT" to ROOT + "items/item_cash_out.png",
        "ITEM_BOUNTY" to ROOT + "items/item_bounty.png",
        "ITEM_BOSS_TROPHY" to ROOT + "items/item_boss_trophy.png",
        "ITEM_CALL_THE_PORTAL" to ROOT + "items/item_call_the_portal.png",
        "ITEM_IS_IT_MERCHANT" to ROOT + "items/item_is_it_merchant.png",
        "ITEM_PIT_TRAP" to ROOT + "items/item_pit_trap.png",

        // Rare items currently supplied by the user
        "ITEM_ANIMATED_BROOM" to ROOT + "items/item_animated_broom.png",
        "ITEM_BESTIARY_HUNTER" to ROOT + "items/item_bestiary_hunter.png",
        "ITEM_BLIND_FAITH" to ROOT + "items/item_blind_faith.png",
        "ITEM_BLOOD_MONEY" to ROOT + "items/item_blood_money.png",
        "ITEM_BLOOD_SCENT" to ROOT + "items/item_blood_scent.png",
        "ITEM_BONE_TOTEMS" to ROOT + "items/item_bone_totems.png",
        "ITEM_BONE_TOTEM_HEALING_AURA" to ROOT + "items/item_bone_totem_healing_aura.png",
        "ITEM_BONE_TOTEM_PROTECTERS_AREA" to ROOT + "items/item_bone_totem_protecters_area.png",
        "ITEM_BONE_TOTEM_BURRIED_TREASURE" to ROOT + "items/item_bone_totem_burried_treasure.png",
        "ITEM_BONE_TOTEM_TOADS" to ROOT + "items/item_bone_totem_toads.png",
        "ITEM_CLOSE_CALL" to ROOT + "items/item_close_call.png",
        "ITEM_COLLECTOR" to ROOT + "items/item_collector.png",
        "ITEM_COLLOSUS_HUNTER" to ROOT + "items/item_collosus_hunter.png",
        "ITEM_CRUEL_PRECISION" to ROOT + "items/item_cruel_precision.png",
        "ITEM_CHAOS_THEORY" to ROOT + "items/item_chaos_theory.png",
        "ITEM_CRITICAL_MOMENTUM" to ROOT + "items/item_critical_momentum.png",
        "ITEM_BONE_TOTEM_HOURGLASS" to ROOT + "items/item_bone_totem_hourglass.png",
        "ITEM_CURSED_MARK" to ROOT + "items/item_cursed_mark.png",
        "ITEM_DEEP_WOUNDS" to ROOT + "items/item_deep_wounds.png",
        "ITEM_EXTRA_REWARD" to ROOT + "items/item_extra_reward.png",
        "ITEM_SHARED_FURY" to ROOT + "items/item_shared_fury.png",
        "ITEM_GAMBLER" to ROOT + "items/item_gambler.png",
        "ITEM_GHOST_PROJECTILE" to ROOT + "items/item_ghost_projectile.png",
        "ITEM_GREEDY_SOUL" to ROOT + "items/item_greedy_soul.png",
        "ITEM_HEAVY_SOUL" to ROOT + "items/item_heavy_soul.png",
        "ITEM_HIT_ME_HARDER" to ROOT + "items/item_hit_me_harder.png",
        "ITEM_LOOT_THE_ENEMIES" to ROOT + "items/item_loot_the_enemies.png",
        "ITEM_MAIN_SLOT" to ROOT + "items/item_main_slot.png",
        "ITEM_MASS_MURDERER" to ROOT + "items/item_mass_murderer.png",
        "ITEM_NO_MERCY" to ROOT + "items/item_no_mercy.png",
        "ITEM_MUTUAL_DESTRUCTION" to ROOT + "items/item_mutual_destruction.png",
        "ITEM_ODDITY" to ROOT + "items/item_oddity.png",
        "ITEM_PREDESTINATION" to ROOT + "items/item_predestination.png",
        "ITEM_REPULSION" to ROOT + "items/item_repulsion.png",
        "ITEM_LITTLE_RISK" to ROOT + "items/item_little_risk.png",
        "ITEM_SUMMONERS_PACT" to ROOT + "items/item_summoners_pact.png",
        "ITEM_THORNS" to ROOT + "items/item_thorns.png",

        // Uncommon items
        "ITEM_AMULET_OF_AZUTH" to ROOT + "items/item_amulet_of_azuth.png",
        "ITEM_CHAIN_MAIL" to ROOT + "items/item_chain_mail.png",
        "ITEM_COPPER_ROD" to ROOT + "items/item_copper_rod.png",
        "ITEM_WARMASTERS_ORDER" to ROOT + "items/item_warmasters_order.png",
        "ITEM_GLADIATORS_LOCKET" to ROOT + "items/item_gladiators_locket.png",
        "ITEM_GODDESS_SELINUS_AMULET" to ROOT + "items/item_goddess_selinus_amulet.png",
        "ITEM_GRIFFIN_FEATHER" to ROOT + "items/item_griffin_feather.png",
        "ITEM_HEAVY_ARROW" to ROOT + "items/item_heavy_arrow.png",
        "ITEM_HILL_GIANTS_BELT" to ROOT + "items/item_hill_giants_belt.png",
        "ITEM_HOURGLASS" to ROOT + "items/item_hourglass.png",
        "ITEM_KEPENEK" to ROOT + "items/item_kepenek.png",
        "ITEM_SANDALS" to ROOT + "items/item_sandals.png",
        "ITEM_LORD_BLEDAS_BANNER" to ROOT + "items/item_lord_bledas_banner.png",
        "ITEM_LUCKY_DICE" to ROOT + "items/item_lucky_dice.png",
        "ITEM_OBSIDIAN_WARD_PENDANT" to ROOT + "items/item_obsidian_ward_pendant.png",
        "ITEM_SPELL_SCROLL" to ROOT + "items/item_spell_scroll.png",
        "ITEM_VAMPIRE_FANGS" to ROOT + "items/item_vampire_fangs.png",
        "ITEM_WAR_HORN" to ROOT + "items/item_war_horn.png",
        "ITEM_WHETSTONE" to ROOT + "items/item_whetstone.png",
        "ITEM_WIND_BOOTS" to ROOT + "items/item_wind_boots.png",

        // Character portraits
        "CHR_SIR_IMBERT" to ROOT + "characters/chr_sir_imbert.png",
        "CHR_YWALETH_YOSTER" to ROOT + "characters/chr_ywaleth_yoster.png",
        "CHR_LAVENA_VOL" to ROOT + "characters/chr_lavena_vol.png",
        "CHR_KOBA" to ROOT + "characters/chr_koba.png",
        "CHR_LINLOS_DUSTMAN" to ROOT + "characters/chr_linlos_dustman.png",

        // Beast portraits currently supplied by the user
        "ENEMY_KIND_GRAY_WOLF" to ROOT + "enemies/enemy_kind_gray_wolf.png",
        "ENEMY_KIND_BOAR" to ROOT + "enemies/enemy_kind_boar.png",
        "ENEMY_KIND_BALD_EAGLE" to ROOT + "enemies/enemy_kind_bald_eagle.png",
        "ENEMY_KIND_GRIZZLY_BEAR" to ROOT + "enemies/enemy_kind_grizzly_bear.png",
        "ENEMY_KIND_BEE_SWARM" to ROOT + "enemies/enemy_kind_bee_swarm.png",
        "ENEMY_KIND_CROCODILE" to ROOT + "enemies/enemy_kind_crocodile.png",
        "ENEMY_KIND_SILVERBACK_GORILLA" to ROOT + "enemies/enemy_kind_silverback_gorilla.png",
        "ENEMY_KIND_BANDIT" to ROOT + "enemies/enemy_kind_bandit.png",
        "ENEMY_KIND_ARCHER" to ROOT + "enemies/enemy_kind_archer.png",
        "ENEMY_KIND_SPEARMAN" to ROOT + "enemies/enemy_kind_spearman.png",
        "ENEMY_KIND_HORSEMAN" to ROOT + "enemies/enemy_kind_horseman.png",
        "ENEMY_KIND_WITCH" to ROOT + "enemies/enemy_kind_witch.png",
        "ENEMY_KIND_SKELETON_WARRIOR" to ROOT + "enemies/enemy_kind_skeleton_warrior.png",
        "ENEMY_KIND_SKELETON_ARCHER" to ROOT + "enemies/enemy_kind_skeleton_archer.png",
        "ENEMY_KIND_ZOMBIE" to ROOT + "enemies/enemy_kind_zombie.png",
        "ENEMY_KIND_GHOST" to ROOT + "enemies/enemy_kind_ghost.png",
        "ENEMY_KIND_SLIME" to ROOT + "enemies/enemy_kind_slime.png",
        "ENEMY_KIND_VAMPIRE_LORD" to ROOT + "enemies/enemy_kind_vampire_lord.png",
        "ENEMY_KIND_MIMIC" to ROOT + "enemies/enemy_kind_mimic.png",
        "SUMMON_COW" to "summon_visuals/cow/cow_south_0.png",
        "SUMMON_IMP" to "summon_visuals/imp/imp_south_0.png",
        "SUMMON_KNIGHT_SPIRIT" to "summon_visuals/knight/knight_south_0.png",
        "SUMMON_TOAD" to "summon_visuals/toad/toad_south_0.png",
        "SUMMON_GUARD" to "summon_visuals/guard/guard_south_0.png"
    )

    private val textures = mutableMapOf<String, Texture>()

    private val artifactItemIds = setOf(
        "ITEM_BOOK_OF_EXPERTIZE",
        "ITEM_HUNTERS_REWARD",
        "ITEM_JACK_OF_ALL_TRADES",
        "ITEM_LONG_SHOT",
        "ITEM_ONE_WEAPON_ARMY",
        "ITEM_OVERFLOWING_POWER",
        "ITEM_SPECIALIST",
        "ITEM_SUMMONED_LEGACY"
    )

    private val characterIdsByDisplayName = mapOf(
        "sir imbert" to "CHR_SIR_IMBERT",
        "ywaleth yoster" to "CHR_YWALETH_YOSTER",
        "lavena vol" to "CHR_LAVENA_VOL",
        "koba" to "CHR_KOBA",
        "linlos dustman" to "CHR_LINLOS_DUSTMAN",
        "linlos" to "CHR_LINLOS_DUSTMAN"
    )

    fun hasIcon(contentId: String): Boolean = iconPaths.containsKey(contentId)

    fun drawSummonFrame(batch: SpriteBatch, bounds: Rectangle, frame: SummonFrame) {
        val image = texture(ROOT + "frames/summons/${frame.name.lowercase()}_v1.png")
        batch.draw(image,bounds.x,bounds.y,bounds.width,bounds.height)
    }

    /** One intentional archive frame, never stacked with the source portrait border. */
    fun drawEnemyArchiveFrame(batch:SpriteBatch,enemy:com.azizoglu.grisilith.enemy.definition.EnemyKindDefinition,bounds:Rectangle) {
        val kind=when {
            enemy.id.value=="ENEMY_KIND_VAMPIRE_LORD" -> "vampire"
            com.azizoglu.grisilith.enemy.EnemyType.FINAL_BOSS in enemy.allowedTypes -> "arc"
            com.azizoglu.grisilith.enemy.EnemyType.MINI_BOSS in enemy.allowedTypes -> "mini"
            else -> "normal"
        }
        val image=texture(ROOT+"frames/bestiary/${kind}_v1.png")
        // Keep the full generated alpha; normalize the empty margin in UVs only.
        val pad=if(kind=="vampire") .02f else .045f
        val x=(image.width*pad).toInt();val y=(image.height*pad).toInt()
        batch.draw(image,bounds.x,bounds.y,bounds.width,bounds.height,x,y,image.width-2*x,image.height-2*y,false,false)
    }

    fun drawCharacterPortrait(
        batch: SpriteBatch,
        displayName: String,
        bounds: Rectangle,
        alpha: Float = 1f
    ): Boolean {
        val contentId =
            characterIdsByDisplayName[displayName.trim().lowercase()]
                ?: return false

        return draw(
            batch = batch,
            contentId = contentId,
            category = ContentCategory.CHARACTER,
            bounds = bounds,
            alpha = alpha
        )
    }

    fun draw(
        batch: SpriteBatch,
        definition: ContentDefinition,
        bounds: Rectangle,
        locked: Boolean = false,
        alpha: Float = 1f,
        framed: Boolean = true
    ): Boolean {
        return draw(
            batch = batch,
            contentId = definition.id.value,
            category = definition.category,
            bounds = bounds,
            locked = locked,
            itemRarity = (definition as? ItemDefinition)?.rarity,
            alpha = alpha,
            framed = framed
        )
    }

    fun draw(
        batch: SpriteBatch,
        contentId: String,
        category: ContentCategory,
        bounds: Rectangle,
        locked: Boolean = false,
        itemRarity: ItemRarity? = null,
        alpha: Float = 1f,
        framed: Boolean = true
    ): Boolean {
        val iconPath = iconPaths[contentId] ?: return false
        val safeAlpha = alpha.coerceIn(0f, 1f)
        val previousColor = com.badlogic.gdx.graphics.Color(batch.color)

        batch.setColor(
            previousColor.r,
            previousColor.g,
            previousColor.b,
            previousColor.a * safeAlpha
        )

        val usesFrame = framed && (category == ContentCategory.WEAPON ||
            category == ContentCategory.ARMOR ||
            category == ContentCategory.TAMGA ||
            category == ContentCategory.ITEM)

        if (usesFrame) {
            drawFrame(
                batch = batch,
                frameKind = frameKind(contentId, category, itemRarity),
                bounds = bounds
            )

            val inset = minOf(bounds.width, bounds.height) * ICON_INSET_RATIO
            batch.draw(
                texture(iconPath),
                bounds.x + inset,
                bounds.y + inset,
                bounds.width - inset * 2f,
                bounds.height - inset * 2f
            )
        } else if(category == ContentCategory.SUMMON) {
            val image = texture(iconPath)
            val crop = summonCrop(iconPath)
            val scale = minOf(bounds.width*.80f/crop[2],bounds.height*.80f/crop[3])
            val width=crop[2]*scale; val height=crop[3]*scale
            batch.draw(image,bounds.x+(bounds.width-width)/2f,bounds.y+(bounds.height-height)/2f,width,height,
                crop[0],crop[1],crop[2],crop[3],false,false)
            drawSummonFrame(batch,bounds,if(contentId=="SUMMON_KNIGHT_SPIRIT") SummonFrame.PURPLE else SummonFrame.GREEN)
        } else if(category == ContentCategory.MAP) {
            val image=texture(iconPath)
            val aspect=bounds.width/bounds.height
            val sourceWidth=minOf(image.width,(image.height*aspect).toInt())
            val sourceHeight=minOf(image.height,(sourceWidth/aspect).toInt())
            batch.draw(image,bounds.x,bounds.y,bounds.width,bounds.height,
                (image.width-sourceWidth)/2,(image.height-sourceHeight)/2,sourceWidth,sourceHeight,false,false)
        } else if(category == ContentCategory.ENEMY && !framed) {
            // Source portraits already include ornamental borders. UV cropping keeps
            // those out of the archive without resampling or modifying the originals.
            val image=texture(iconPath)
            val inset=when(contentId) {
                "ENEMY_KIND_GRIZZLY_BEAR", "ENEMY_KIND_SILVERBACK_GORILLA" -> .16f
                "ENEMY_KIND_GRAY_WOLF", "ENEMY_KIND_BOAR", "ENEMY_KIND_BALD_EAGLE",
                "ENEMY_KIND_BEE_SWARM", "ENEMY_KIND_CROCODILE" -> .085f
                else -> .235f
            }
            val x=(image.width*inset).toInt()
            val y=(image.height*if(contentId=="ENEMY_KIND_GHOST") .16f else inset).toInt()
            val sourceWidth=image.width-2*x
            val sourceHeight=if(contentId=="ENEMY_KIND_GHOST") image.height-y-(image.height*.22f).toInt() else image.height-2*y
            val scale=minOf(bounds.width/sourceWidth,bounds.height/sourceHeight)
            val width=sourceWidth*scale;val height=sourceHeight*scale
            batch.draw(image,bounds.x+(bounds.width-width)/2f,bounds.y+(bounds.height-height)/2f,width,height,
                x,y,sourceWidth,sourceHeight,false,false)
        } else {
            batch.draw(
                texture(iconPath),
                bounds.x,
                bounds.y,
                bounds.width,
                bounds.height
            )
        }

        if (locked) {
            batch.draw(
                texture(LOCK_OVERLAY),
                bounds.x,
                bounds.y,
                bounds.width,
                bounds.height
            )
        }

        batch.color = previousColor
        return true
    }

    private fun frameKind(
        contentId: String,
        category: ContentCategory,
        itemRarity: ItemRarity?
    ): FrameKind {
        if (category != ContentCategory.ITEM) {
            return FrameKind.GEAR
        }

        return when (itemRarity) {
            ItemRarity.UNCOMMON -> FrameKind.UNCOMMON
            ItemRarity.RARE -> FrameKind.RARE
            ItemRarity.LEGENDARY -> FrameKind.LEGENDARY
            ItemRarity.ARTIFACT -> FrameKind.ARTIFACT
            null -> if (contentId in artifactItemIds) {
                FrameKind.ARTIFACT
            } else {
                FrameKind.UNCOMMON
            }
        }
    }

    private fun drawFrame(
        batch: SpriteBatch,
        frameKind: FrameKind,
        bounds: Rectangle
    ) {
        val region = frameKind.region
        batch.draw(
            texture(FRAME_SHEET),
            bounds.x,
            bounds.y,
            bounds.width,
            bounds.height,
            region.x,
            region.y,
            region.width,
            region.height,
            false,
            false
        )
    }

    fun drawStatRewardFrame(
        batch: SpriteBatch,
        bounds: Rectangle,
        amount: Int,
        rotationDegrees: Float = 45f,
        selected: Boolean = false
    ) {
        val frameKind = when (amount) {
            5 -> FrameKind.ARTIFACT
            3 -> FrameKind.LEGENDARY
            2 -> FrameKind.RARE
            else -> FrameKind.UNCOMMON
        }
        val region = frameKind.region
        val previous = com.badlogic.gdx.graphics.Color(batch.color)
        batch.color = if (selected) com.badlogic.gdx.graphics.Color(1.18f, 1.18f, 1.18f, 1f) else com.badlogic.gdx.graphics.Color.WHITE
        batch.draw(
            texture(FRAME_SHEET),
            bounds.x,
            bounds.y,
            bounds.width / 2f,
            bounds.height / 2f,
            bounds.width,
            bounds.height,
            if (selected) 1.06f else 1f,
            if (selected) 1.06f else 1f,
            rotationDegrees,
            region.x,
            region.y,
            region.width,
            region.height,
            false,
            false
        )
        batch.color = previous
    }

    private fun texture(path: String): Texture {
        return textures.getOrPut(path) {
            Texture(Gdx.files.internal(path)).also { loaded ->
                loaded.setFilter(
                    Texture.TextureFilter.Linear,
                    Texture.TextureFilter.Linear
                )
            }
        }
    }

    fun dispose() {
        textures.values.forEach(Texture::dispose)
        textures.clear()
        summonCrops.clear()
    }
}
