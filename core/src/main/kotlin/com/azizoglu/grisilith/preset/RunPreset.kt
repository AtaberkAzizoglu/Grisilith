package com.azizoglu.grisilith.preset

import com.azizoglu.grisilith.content.ContentId
import java.util.UUID

/*
 * Kalıcı olarak kaydedilen bir başlangıç Preset'i.
 *
 * Tamga sistemi henüz hazır olmadığı için Tamga
 * ID'leri nullable tutulur. Tamga eklendiğinde
 * veri modelini değiştirmemiz gerekmeyecek.
 */
data class RunPreset(
    val presetId:
    String =
        UUID.randomUUID()
            .toString(),

    val presetName:
    String,

    val characterId:
    ContentId,

    val weaponId:
    ContentId,

    val armorId:
    ContentId,

    val itemId:
    ContentId,

    val weaponTamgaId:
    ContentId? =
        null,

    val armorTamgaId:
    ContentId? =
        null,

    /*
     * Başlangıçta 10.
     *
     * İleride Merchant from Halatha üzerinden
     * daha fazla Stat Point satın alındığında
     * ilgili Preset'in bütçesi artırılabilir.
     */
    val statPointBudget:
    Int =
        PresetConstants
            .DEFAULT_STAT_POINT_BUDGET,

    val statAllocation:
    PresetStatAllocation =
        PresetStatAllocation(),

    val createdAtEpochMillis:
    Long =
        System.currentTimeMillis(),

    val updatedAtEpochMillis:
    Long =
        System.currentTimeMillis()
) {

    init {
        require(
            presetId.isNotBlank()
        ) {
            "Preset ID cannot be blank."
        }

        require(
            presetName.isNotBlank()
        ) {
            "Preset name cannot be blank."
        }

        require(
            presetName.length <=
                PresetConstants
                    .MAXIMUM_PRESET_NAME_LENGTH
        ) {
            "Preset name is too long."
        }

        require(
            statPointBudget >=
                PresetConstants
                    .DEFAULT_STAT_POINT_BUDGET
        ) {
            "Preset Stat Point budget cannot be lower than the default."
        }

        require(
            statAllocation.spentPointCount <=
                statPointBudget
        ) {
            "Preset spends more Stat Points than its budget."
        }
    }

    val remainingStatPoints: Int
        get() =
            (
                statPointBudget -
                    statAllocation
                        .spentPointCount
                )
                .coerceAtLeast(
                    0
                )

    fun renamed(
        newName:
        String
    ): RunPreset {
        return copy(
            presetName =
                newName.trim(),

            updatedAtEpochMillis =
                System.currentTimeMillis()
        )
    }

    fun withStatAllocation(
        newAllocation:
        PresetStatAllocation
    ): RunPreset {
        return copy(
            statAllocation =
                newAllocation,

            updatedAtEpochMillis =
                System.currentTimeMillis()
        )
    }

    fun withEquipment(
        weaponId:
        ContentId =
            this.weaponId,

        armorId:
        ContentId =
            this.armorId,

        itemId:
        ContentId =
            this.itemId,

        weaponTamgaId:
        ContentId? =
            this.weaponTamgaId,

        armorTamgaId:
        ContentId? =
            this.armorTamgaId
    ): RunPreset {
        return copy(
            weaponId =
                weaponId,

            armorId =
                armorId,

            itemId =
                itemId,

            weaponTamgaId =
                weaponTamgaId,

            armorTamgaId =
                armorTamgaId,

            updatedAtEpochMillis =
                System.currentTimeMillis()
        )
    }
}
