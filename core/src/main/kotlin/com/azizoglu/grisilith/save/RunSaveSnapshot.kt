package com.azizoglu.grisilith.save

data class RunSaveSnapshot(
    val version: Int = CURRENT_VERSION,
    val savedAtEpochMillis: Long = System.currentTimeMillis(),
    val difficultyName: String = "",
    val mapName: String = "",
    val characterDefinitionId: String = "",
    val presetId: String? = null,
    val checkpointStage: Int = 1,
    val maximumStage: Int = 1,
    val elapsedRunTimeSeconds: Float = 0f,
    val currentHealth: Int = 1,
    val sessionGold: Int = 0,
    val remainingChestDiscards: Int = 0,
    val statLayers: RunSaveStatLayers = RunSaveStatLayers(),
    val weaponSlots: List<RunSaveWeaponSlot> = emptyList(),
    val armorDefinitionId: String? = null,
    val weaponTamgaDefinitionId: String? = null,
    val armorTamgaDefinitionId: String? = null,
    val weaponTamgaRuntimeState: RunSaveActiveTamgaState? = null,
    val armorTamgaRuntimeState: RunSaveActiveTamgaState? = null,
    val itemStacks: List<RunSaveItemStack> = emptyList(),
    val statistics: RunSaveStatistics = RunSaveStatistics(),
    val rewardState: RunSaveRewardState = RunSaveRewardState(),
    val preview: RunSavePreview = RunSavePreview()
) {
    companion object {
        const val CURRENT_VERSION: Int = 2
    }

    fun validationErrors(): List<String> {
        val errors = mutableListOf<String>()

        if (version <= 0) errors.add("Invalid save version.")
        if (difficultyName.isBlank()) errors.add("Difficulty is missing.")
        if (mapName.isBlank()) errors.add("Map is missing.")
        if (characterDefinitionId.isBlank()) errors.add("Character is missing.")
        if (checkpointStage <= 0) errors.add("Checkpoint stage must be positive.")
        if (maximumStage <= 0) errors.add("Maximum stage must be positive.")
        if (checkpointStage > maximumStage) errors.add("Checkpoint stage exceeds maximum stage.")
        if (elapsedRunTimeSeconds < 0f) errors.add("Elapsed run time cannot be negative.")
        if (currentHealth <= 0) errors.add("Checkpoint health must be positive.")
        if (sessionGold < 0) errors.add("Session Gold cannot be negative.")
        if (remainingChestDiscards < 0) errors.add("Remaining chest discards cannot be negative.")

        weaponSlots.forEach { slot ->
            if (slot.slotIndex < 0) errors.add("Weapon slot index cannot be negative.")
            if (slot.weaponDefinitionId.isBlank()) errors.add("Weapon definition ID is missing.")
            if (slot.level <= 0) errors.add("Weapon level must be positive.")
        }

        itemStacks.forEach { stack ->
            if (stack.itemDefinitionId.isBlank()) errors.add("Item definition ID is missing.")
            if (stack.count <= 0) errors.add("Item stack count must be positive.")
        }

        listOfNotNull(
            weaponTamgaRuntimeState,
            armorTamgaRuntimeState
        ).forEach { runtimeState ->
            if (runtimeState.activeElapsedSeconds < 0f) {
                errors.add("Active Tamga elapsed time cannot be negative.")
            }
            if (runtimeState.cooldownRemainingSeconds < 0f) {
                errors.add("Active Tamga cooldown cannot be negative.")
            }
        }

        return errors
    }

    val isValid: Boolean
        get() = validationErrors().isEmpty()
}
