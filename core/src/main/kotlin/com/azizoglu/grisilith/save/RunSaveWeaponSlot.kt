package com.azizoglu.grisilith.save

data class RunSaveWeaponSlot(
    val slotIndex: Int = 0,
    val weaponDefinitionId: String = "",
    val level: Int = 1,
    val ivySummonChoiceName: String? = null,
    val chalkDustSpellChoiceName: String? = null
)
