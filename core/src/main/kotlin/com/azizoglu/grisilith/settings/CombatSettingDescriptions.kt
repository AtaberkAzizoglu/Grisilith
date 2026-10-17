package com.azizoglu.grisilith.settings

/** Active-option help shared by the Combat screen and the copy-editing export. */
object CombatSettingDescriptions {
    fun targeting(value: GameSettings.Targeting): String = when(value) {
        GameSettings.Targeting.CLOSEST -> "Normal attacks select the closest target within the weapon's range."
        GameSettings.Targeting.MOUSE -> "Normal attacks select the in-range target closest to the mouse cursor."
        GameSettings.Targeting.BOSS -> "Normal attacks prioritize Final Bosses, then Mini Bosses and hostile Oathbreakers, then Unique enemies, then Normal enemies. Distance breaks ties."
    }
    fun casting(value: GameSettings.AbilityCasting): String = when(value) {
        GameSettings.AbilityCasting.PREPARE -> "Shows the ability's area before casting. Press the weapon slot again or click to cast early. Dagger and Boomerang cast immediately; Tsunami has no preparation marker."
        GameSettings.AbilityCasting.INSTANT -> "Casts the ability immediately toward the current mouse position. Chalk spells still finish their drawing before casting."
        GameSettings.AbilityCasting.AUTO -> "Automatically aims at an in-range target: Final Boss, then Mini Boss or hostile Oathbreaker, then Unique, then Normal. Distance breaks ties."
    }
    fun normalRange(value: GameSettings.NormalRange): String = when(value) {
        GameSettings.NormalRange.OFF -> "Normal attack range indicators are hidden."
        GameSettings.NormalRange.ALL -> "Shows a thin white range circle for every equipped weapon."
        GameSettings.NormalRange.MAIN -> "Shows a thin white range circle only for the main weapon."
    }
    fun abilityRange(value: GameSettings.AbilityRange): String = when(value) {
        GameSettings.AbilityRange.OFF -> "Ability range indicators are hidden."
        GameSettings.AbilityRange.PREPARING -> "Shows a red range circle only while an ability is being prepared. Tsunami has no preparation marker."
        GameSettings.AbilityRange.ALL -> "Always shows red ability-range circles for all equipped weapons with active abilities. Tsunami has no range marker."
        GameSettings.AbilityRange.MAIN -> "Always shows a red ability-range circle for the main weapon's active ability. Tsunami has no range marker."
    }
}
