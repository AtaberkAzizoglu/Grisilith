package com.azizoglu.grisilith.weapon.definition

import com.azizoglu.grisilith.weapon.skill.DefaultWeaponAbilities

import com.azizoglu.grisilith.weapon.definition.catalog.CrossbowDefinition
import com.azizoglu.grisilith.weapon.definition.catalog.DaggerDefinition
import com.azizoglu.grisilith.weapon.definition.catalog.DoubleSwordsDefinition
import com.azizoglu.grisilith.weapon.definition.catalog.IvyStaffDefinition
import com.azizoglu.grisilith.weapon.definition.catalog.LanceDefinition
import com.azizoglu.grisilith.weapon.definition.catalog.LongbowDefinition
import com.azizoglu.grisilith.weapon.definition.catalog.LongswordDefinition
import com.azizoglu.grisilith.weapon.definition.catalog.ShieldDefinition
import com.azizoglu.grisilith.weapon.definition.catalog.SteamWandDefinition
import com.azizoglu.grisilith.weapon.definition.catalog.TomeOfEryndorDefinition
import com.azizoglu.grisilith.weapon.definition.catalog.AmuletOfTrollKingDefinition
import com.azizoglu.grisilith.weapon.definition.catalog.EnchantedChalkDustDefinition
import com.azizoglu.grisilith.weapon.definition.catalog.OrbOfSkyDefinition
import com.azizoglu.grisilith.weapon.definition.catalog.TotemOfDarkCloudsDefinition


object DefaultWeapons {
    val warhammer = com.azizoglu.grisilith.weapon.definition.catalog.NewPhysicalWeapons.warhammer
    val whip = com.azizoglu.grisilith.weapon.definition.catalog.NewPhysicalWeapons.whip
    val sling = com.azizoglu.grisilith.weapon.definition.catalog.NewPhysicalWeapons.sling
    val boomerang = com.azizoglu.grisilith.weapon.definition.catalog.NewPhysicalWeapons.boomerang

    /* Double Swords ability is attached here for catalog compatibility. */
    val doubleSwords =
        DoubleSwordsDefinition
            .definition
            .copy(
                weaponAbility =
                    DefaultWeaponAbilities
                        .rapidStrokes
            )

    val longsword =
        LongswordDefinition.definition

    val lance =
        LanceDefinition.definition

    val shield =
        ShieldDefinition.definition

    val dagger =
        DaggerDefinition.definition

    val longbow =
        LongbowDefinition.definition

    val crossbow =
        CrossbowDefinition.definition

    val ivyStaff =
        IvyStaffDefinition.definition

    /*
     * Geçici uyumluluk alias'ı.
     */
    val staff =
        ivyStaff

    val steamWand =
        SteamWandDefinition.definition

    /*
     * Geçici uyumluluk alias'ı.
     */
    val wand =
        steamWand

    val tomeOfEryndor =
        TomeOfEryndorDefinition.definition

    /*
     * Geçici uyumluluk alias'ı.
     */
    val theBook =
        tomeOfEryndor

    val orbOfSky =
        OrbOfSkyDefinition.definition

    val totemOfDarkClouds =
        TotemOfDarkCloudsDefinition.definition

    val amuletOfTrollKing =
        AmuletOfTrollKingDefinition.definition

    val enchantedChalkDust =
        EnchantedChalkDustDefinition.definition

    val all:
        List<WeaponDefinition> =
        listOf(
            doubleSwords,
            longsword,
            lance,
            shield,
            dagger,
            longbow,
            crossbow,
            ivyStaff,
            steamWand,
            tomeOfEryndor,
            orbOfSky,
            totemOfDarkClouds,
            amuletOfTrollKing,
            enchantedChalkDust,
            warhammer, whip, sling, boomerang
        )

    fun findById(
        contentId: String
    ): WeaponDefinition? {
        return all.firstOrNull { weapon ->
            weapon.id.value ==
                contentId
        }
    }
}
