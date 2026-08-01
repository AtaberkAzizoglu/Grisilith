package com.azizoglu.grisilith.preset

import com.azizoglu.grisilith.content.ContentId
import com.azizoglu.grisilith.content.ContentRegistry
import com.azizoglu.grisilith.statistics.StatType
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.utils.Json

object PresetRepository {

    private const val PREFERENCES_NAME =
        "grisilith_presets"

    private const val PRESETS_KEY =
        "saved_presets"

    private val json =
        Json()

    private val presets =
        mutableListOf<RunPreset>()

    private var hasLoaded =
        false

    private val preferences:
        Preferences
        get() =
            Gdx.app.getPreferences(
                PREFERENCES_NAME
            )

    fun load() {
        if (hasLoaded) {
            return
        }

        hasLoaded =
            true

        presets.clear()

        val storedJson =
            preferences.getString(
                PRESETS_KEY,
                ""
            )

        if (storedJson.isBlank()) {
            return
        }

        try {
            val saveData =
                json.fromJson(
                    PresetSaveData::class.java,
                    storedJson
                )
                    ?: return

            saveData.presets
                .mapNotNull {
                        savedPreset ->

                    savedPreset
                        .toRunPresetOrNull()
                }
                .take(
                    PresetConstants
                        .MAXIMUM_PRESET_COUNT
                )
                .forEach { preset ->
                    presets +=
                        preset
                }
        } catch (
            exception:
            Exception
        ) {
            /*
             * Hatalı veya eski kayıt oyunun
             * açılmasını engellemesin.
             */
            Gdx.app.error(
                "PresetRepository",
                "Could not load Presets.",
                exception
            )

            presets.clear()
        }
    }

    fun allPresets():
        List<RunPreset> {

        ensureLoaded()

        return presets
            .sortedByDescending { preset ->
                preset.updatedAtEpochMillis
            }
            .toList()
    }

    fun presetCount():
        Int {

        ensureLoaded()

        return presets.size
    }

    fun findById(
        presetId:
        String
    ): RunPreset? {

        ensureLoaded()

        return presets
            .firstOrNull { preset ->
                preset.presetId ==
                    presetId
            }
    }

    fun create(
        preset:
        RunPreset,

        contentRegistry:
        ContentRegistry
    ): PresetRepositoryResult {

        ensureLoaded()

        if (
            presets.size >=
            PresetConstants
                .MAXIMUM_PRESET_COUNT
        ) {
            return failure(
                "You can save at most " +
                    "${PresetConstants.MAXIMUM_PRESET_COUNT} Presets."
            )
        }

        if (
            presets.any { existing ->
                existing.presetId ==
                    preset.presetId
            }
        ) {
            return failure(
                "A Preset with the same ID already exists."
            )
        }

        val validation =
            PresetValidator.validate(
                preset =
                    preset,

                contentRegistry =
                    contentRegistry
            )

        if (
            validation is
                PresetValidationResult.Invalid
        ) {
            return failure(
                validation.message
            )
        }

        presets +=
            preset

        save()

        return PresetRepositoryResult
            .Success(
                preset =
                    preset
            )
    }

    fun update(
        preset:
        RunPreset,

        contentRegistry:
        ContentRegistry
    ): PresetRepositoryResult {

        ensureLoaded()

        val index =
            presets.indexOfFirst { existing ->
                existing.presetId ==
                    preset.presetId
            }

        if (index < 0) {
            return failure(
                "The Preset could not be found."
            )
        }

        val validation =
            PresetValidator.validate(
                preset =
                    preset,

                contentRegistry =
                    contentRegistry
            )

        if (
            validation is
                PresetValidationResult.Invalid
        ) {
            return failure(
                validation.message
            )
        }

        val updatedPreset =
            preset.copy(
                createdAtEpochMillis =
                    presets[
                        index
                    ].createdAtEpochMillis,

                updatedAtEpochMillis =
                    System.currentTimeMillis()
            )

        presets[
            index
        ] =
            updatedPreset

        save()

        return PresetRepositoryResult
            .Success(
                preset =
                    updatedPreset
            )
    }

    fun delete(
        presetId:
        String
    ): PresetRepositoryResult {

        ensureLoaded()

        val removed =
            presets.removeAll { preset ->
                preset.presetId ==
                    presetId
            }

        if (!removed) {
            return failure(
                "The Preset could not be found."
            )
        }

        save()

        return PresetRepositoryResult
            .Success()
    }

    /*
     * Geliştirme sırasında kayıtları temizlemek
     * gerekirse kullanılabilir.
     */
    fun clearAll() {
        ensureLoaded()

        presets.clear()
        save()
    }

    private fun save() {
        val saveData =
            PresetSaveData().apply {
                presets =
                    this@PresetRepository
                        .presets
                        .map { preset ->
                            PresetRecord
                                .from(
                                    preset
                                )
                        }
                        .toMutableList()
            }

        preferences
            .putString(
                PRESETS_KEY,
                json.toJson(
                    saveData
                )
            )
            .flush()
    }

    private fun ensureLoaded() {
        if (!hasLoaded) {
            load()
        }
    }

    private fun failure(
        message:
        String
    ): PresetRepositoryResult.Failure {
        return PresetRepositoryResult
            .Failure(
                message =
                    message
            )
    }
}

/*
 * LibGDX Json için parametresiz ve mutable
 * save sınıfları kullanılır.
 */
private class PresetSaveData {

    var presets:
        MutableList<PresetRecord> =
        mutableListOf()
}

private class PresetRecord {

    var presetId:
        String =
        ""

    var presetName:
        String =
        ""

    var characterId:
        String =
        ""

    var weaponId:
        String =
        ""

    var armorId:
        String =
        ""

    var itemId:
        String =
        ""

    var weaponTamgaId:
        String? =
        null

    var armorTamgaId:
        String? =
        null

    var statPointBudget:
        Int =
        PresetConstants
            .DEFAULT_STAT_POINT_BUDGET

    var statBonuses:
        MutableMap<String, Int> =
        mutableMapOf()

    var createdAtEpochMillis:
        Long =
        0L

    var updatedAtEpochMillis:
        Long =
        0L

    fun toRunPresetOrNull():
        RunPreset? {

        if (
            presetId.isBlank() ||
            presetName.isBlank() ||
            characterId.isBlank() ||
            weaponId.isBlank()
        ) {
            return null
        }

        val parsedStatBonuses =
            statBonuses
                .mapNotNull {
                        entry ->

                    val statType =
                        runCatching {
                            StatType.valueOf(
                                entry.key
                            )
                        }
                            .getOrNull()
                            ?: return@mapNotNull null

                    statType to
                        entry.value
                            .coerceAtLeast(
                                0
                            )
                }
                .toMap()

        return runCatching {
            RunPreset(
                presetId =
                    presetId,

                presetName =
                    presetName,

                characterId =
                    ContentId(
                        characterId
                    ),

                weaponId =
                    ContentId(
                        weaponId
                    ),

                armorId =
                    armorId
                        .takeIf {
                            it.isNotBlank()
                        }
                        ?.let {
                            ContentId(
                                it
                            )
                        },

                itemId =
                    itemId
                        .takeIf {
                            it.isNotBlank()
                        }
                        ?.let {
                            ContentId(
                                it
                            )
                        },

                weaponTamgaId =
                    weaponTamgaId
                        ?.takeIf {
                            it.isNotBlank()
                        }
                        ?.let {
                            ContentId(
                                it
                            )
                        },

                armorTamgaId =
                    armorTamgaId
                        ?.takeIf {
                            it.isNotBlank()
                        }
                        ?.let {
                            ContentId(
                                it
                            )
                        },

                statPointBudget =
                    statPointBudget
                        .coerceAtLeast(
                            PresetConstants
                                .DEFAULT_STAT_POINT_BUDGET
                        ),

                statAllocation =
                    PresetStatAllocation(
                        bonusPoints =
                            parsedStatBonuses
                    ),

                createdAtEpochMillis =
                    createdAtEpochMillis,

                updatedAtEpochMillis =
                    updatedAtEpochMillis
            )
        }
            .getOrNull()
    }

    companion object {

        fun from(
            preset:
            RunPreset
        ): PresetRecord {

            return PresetRecord().apply {
                presetId =
                    preset.presetId

                presetName =
                    preset.presetName

                characterId =
                    preset.characterId.value

                weaponId =
                    preset.weaponId.value

                armorId =
                    preset.armorId
                        ?.value
                        .orEmpty()

                itemId =
                    preset.itemId
                        ?.value
                        .orEmpty()

                weaponTamgaId =
                    preset.weaponTamgaId
                        ?.value

                armorTamgaId =
                    preset.armorTamgaId
                        ?.value

                statPointBudget =
                    preset.statPointBudget

                statBonuses =
                    preset
                        .statAllocation
                        .bonusPoints
                        .mapKeys {
                                entry ->

                            entry.key.name
                        }
                        .toMutableMap()

                createdAtEpochMillis =
                    preset.createdAtEpochMillis

                updatedAtEpochMillis =
                    preset.updatedAtEpochMillis
            }
        }
    }
}
