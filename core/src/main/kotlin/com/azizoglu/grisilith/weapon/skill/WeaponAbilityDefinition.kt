package com.azizoglu.grisilith.weapon.skill

data class WeaponAbilityDefinition(
    val id: String,
    val displayName: String,
    val activationType: WeaponAbilityActivationType,
    val cooldownSeconds: Float = 0f,
    val durationSeconds: Float = 0f
) {
    init {
        require(id.isNotBlank())
        require(displayName.isNotBlank())
        require(cooldownSeconds >= 0f)
        require(durationSeconds >= 0f)

        if (
            activationType ==
            WeaponAbilityActivationType.PASSIVE
        ) {
            require(cooldownSeconds == 0f)
            require(durationSeconds == 0f)
        }
    }
}
