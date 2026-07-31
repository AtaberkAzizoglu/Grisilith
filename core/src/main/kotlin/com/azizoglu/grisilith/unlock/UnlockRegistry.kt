package com.azizoglu.grisilith.unlock

import com.azizoglu.grisilith.armor.definition.ArmorDefinition
import com.azizoglu.grisilith.armor.definition.DefaultArmors
import com.azizoglu.grisilith.item.definition.DefaultItems
import com.azizoglu.grisilith.tamga.definition.DefaultTamgas
import com.azizoglu.grisilith.tamga.definition.TamgaActivationType
import com.azizoglu.grisilith.weapon.definition.DefaultWeapons
import com.azizoglu.grisilith.weapon.definition.WeaponDefinition
import java.util.Locale

object UnlockRegistry {

    const val STAT_POINT_ID =
        "STAT_POINT"

    private const val WEAPON_COST =
        5

    private const val ARMOR_COST =
        5

    private const val ITEM_COST =
        10

    private const val PASSIVE_TAMGA_COST =
        15

    private const val ACTIVE_TAMGA_COST =
        20

    private const val STAT_POINT_COST =
        5

    /*
     * Merchant'ta satılacak mevcut Weapon'lar.
     *
     * ID'leri tahmin etmek yerine displayName kullanıyoruz.
     * İçerik ID'leri satın alma kaydına yine definition üzerinden yazılır.
     */
    private val merchantWeapons:
        List<WeaponDefinition> =
        listOf(
            requireWeapon(
                "Lance"
            ),
            requireWeapon(
                "Crossbow"
            ),
            requireWeapon(
                "Enchanted Chalk Dust"
            )
        )

    /*
     * Şimdilik yalnızca Heavy Plate Armor satılıyor.
     *
     * Gerçek displayName "Plate Armor" veya
     * "Heavy Plate Armor" ise ikisini de destekler.
     */
    private val merchantArmors:
        List<ArmorDefinition> =
        listOf(
            requirePlateArmor()
        )

    private val definitions:
        List<UnlockDefinition> =
        buildList {

            add(
                UnlockDefinition(
                    id =
                        STAT_POINT_ID,

                    displayName =
                        "+1 STAT POINT",

                    description =
                        "Adds one permanent point to the purchased stat pool.",

                    type =
                        UnlockType.STAT_POINT,

                    cost =
                        STAT_POINT_COST
                )
            )

            merchantWeapons.forEach {
                    weapon ->

                add(
                    UnlockDefinition(
                        id =
                            weapon.id.value,

                        displayName =
                            weapon.displayName,

                        description =
                            "Permanently unlocks ${weapon.displayName}.",

                        type =
                            UnlockType.WEAPON,

                        cost =
                            WEAPON_COST
                    )
                )
            }

            merchantArmors.forEach {
                    armor ->

                add(
                    UnlockDefinition(
                        id =
                            armor.id.value,

                        displayName =
                            armor.displayName,

                        description =
                            "Permanently unlocks ${armor.displayName}.",

                        type =
                            UnlockType.ARMOR,

                        cost =
                            ARMOR_COST
                    )
                )
            }

            /*
             * Tüm mevcut Item'ler Merchant ürünüdür.
             */
            DefaultItems.all.forEach {
                    item ->

                add(
                    UnlockDefinition(
                        id =
                            item.id.value,

                        displayName =
                            item.displayName,

                        description =
                            "Permanently unlocks ${item.displayName}.",

                        type =
                            UnlockType.ITEM,

                        cost =
                            ITEM_COST
                    )
                )
            }

            /*
             * Tüm mevcut Tamga'lar Merchant ürünüdür.
             */
            DefaultTamgas.all.forEach {
                    tamga ->

                val tamgaCost =
                    when (
                        tamga.activationType
                    ) {
                        TamgaActivationType.ACTIVE ->
                            ACTIVE_TAMGA_COST

                        TamgaActivationType.PASSIVE ->
                            PASSIVE_TAMGA_COST
                    }

                add(
                    UnlockDefinition(
                        id =
                            tamga.id.value,

                        displayName =
                            tamga.displayName,

                        description =
                            "Permanently unlocks ${tamga.displayName}.",

                        type =
                            UnlockType.TAMGA,

                        cost =
                            tamgaCost
                    )
                )
            }
        }

    private val definitionsById:
        Map<String, UnlockDefinition> =
        definitions.associateBy {
            it.id
        }

    init {
        require(
            definitionsById.size ==
                definitions.size
        ) {
            "UnlockRegistry contains duplicate ids."
        }

        validateDependencies()

        /*
         * Geçici debug çıktısı.
         * Sistem doğrulandıktan sonra kaldırılabilir.
         */

        /*
        println(
            "UNLOCK REGISTRY PRODUCTS:"
        )

        definitions.forEach {
                definition ->

            println(
                "- ${definition.type}: " +
                    "${definition.displayName} " +
                    "(${definition.cost} G)"
            )
        }*/
    }

    fun all():
        List<UnlockDefinition> {

        return definitions
    }

    fun visible():
        List<UnlockDefinition> {

        return definitions.filterNot {
            it.hidden
        }
    }

    fun availableForMerchant(
        unlockedIds: Set<String>
    ): List<UnlockDefinition> {

        return visible()
            .filter {
                    definition ->

                /*
                 * Stat Point tekrar satın alınabilir.
                 *
                 * Diğer ürünler satın alındıktan sonra
                 * Merchant listesinden çıkar.
                 */
                definition.type ==
                    UnlockType.STAT_POINT ||
                    definition.id !in
                    unlockedIds
            }
            .filter {
                    definition ->

                dependenciesAreMet(
                    definition =
                        definition,

                    unlockedIds =
                        unlockedIds
                )
            }
    }

    fun find(
        id: String
    ): UnlockDefinition? {

        return definitionsById[
            id
        ]
    }

    fun require(
        id: String
    ): UnlockDefinition {

        return checkNotNull(
            find(
                id
            )
        ) {
            "Unknown unlock id: $id"
        }
    }

    fun byType(
        type: UnlockType,
        includeHidden: Boolean = false
    ): List<UnlockDefinition> {

        return definitions.filter {
                definition ->

            definition.type ==
                type &&
                (
                    includeHidden ||
                        !definition.hidden
                    )
        }
    }

    fun dependenciesAreMet(
        definition: UnlockDefinition,
        unlockedIds: Set<String>
    ): Boolean {

        return unlockedIds.containsAll(
            definition.requiredUnlockIds
        )
    }

    private fun validateDependencies() {

        definitions.forEach {
                definition ->

            definition
                .requiredUnlockIds
                .forEach {
                        requiredId ->

                    require(
                        definitionsById
                            .containsKey(
                                requiredId
                            )
                    ) {
                        "Unlock ${definition.id} requires unknown unlock $requiredId"
                    }
                }
        }
    }

    private fun requireWeapon(
        displayName: String
    ): WeaponDefinition {

        return checkNotNull(
            DefaultWeapons.all
                .firstOrNull {
                        weapon ->

                    weapon.displayName
                        .equals(
                            displayName,
                            ignoreCase =
                                true
                        )
                }
        ) {
            "Merchant Weapon could not be found: $displayName"
        }
    }

    private fun requirePlateArmor():
        ArmorDefinition {

        return checkNotNull(
            DefaultArmors.all
                .firstOrNull {
                        armor ->

                    val normalizedName =
                        armor.displayName
                            .trim()
                            .lowercase(
                                Locale.US
                            )

                    normalizedName ==
                        "plate armor" ||
                        normalizedName ==
                        "heavy plate armor" ||
                        (
                            "plate" in
                                normalizedName &&
                                "heavy" in
                                normalizedName
                            )
                }
        ) {
            "Merchant Plate Armor could not be found. " +
                "Available Armors: " +
                DefaultArmors.all
                    .joinToString {
                        it.displayName
                    }
        }
    }
}
