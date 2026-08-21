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

    /*
     * Double Swords previously had no concrete Level 5 ability definition.
     * Keep the catalog definition intact and attach Relentless here so all
     * systems consuming DefaultWeapons receive the completed definition.
     */
    val doubleSwords =
        DoubleSwordsDefinition
            .definition
            .copy(
                weaponAbility =
                    DefaultWeaponAbilities
                        .relentless
            )

    /*
     * Geçici uyumluluk alias'ı.
     * Projedeki shortsword kullanımları tamamen
     * değiştirildiğinde kaldırılabilir.
     */
    val shortsword =
        doubleSwords

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
            enchantedChalkDust
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
