package com.azizoglu.grisilith.input

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import java.util.EnumMap

enum class RunInputAction(
    val displayName: String,
    val groupName: String,
    val defaultKey: Int
) {
    MOVE_UP("Move Up", "MOVEMENT", Input.Keys.W),
    MOVE_DOWN("Move Down", "MOVEMENT", Input.Keys.S),
    MOVE_LEFT("Move Left", "MOVEMENT", Input.Keys.A),
    MOVE_RIGHT("Move Right", "MOVEMENT", Input.Keys.D),
    WEAPON_1("Weapon Slot 1", "WEAPON SLOTS", Input.Keys.NUM_1),
    WEAPON_2("Weapon Slot 2", "WEAPON SLOTS", Input.Keys.NUM_2),
    WEAPON_3("Weapon Slot 3", "WEAPON SLOTS", Input.Keys.NUM_3),
    WEAPON_4("Weapon Slot 4", "WEAPON SLOTS", Input.Keys.NUM_4),
    WEAPON_5("Weapon Slot 5", "WEAPON SLOTS", Input.Keys.NUM_5),
    WEAPON_6("Weapon Slot 6", "WEAPON SLOTS", Input.Keys.NUM_6),
    WEAPON_TAMGA("Weapon Tamga", "COMBAT", Input.Keys.Q),
    ARMOR_TAMGA("Armor Tamga", "COMBAT", Input.Keys.E),
    GAME_SPEED("Cycle Game Speed", "COMBAT", Input.Keys.ALT_LEFT),
    PAUSE("Pause Game", "COMBAT", Input.Keys.ESCAPE)
}

/** Persistent source for gameplay bindings used by both input and HUD. */
object RunInputBindings {
    private const val PREFERENCES_NAME = "grisilith_key_bindings"
    private val keys = EnumMap<RunInputAction, Int>(RunInputAction::class.java)

    init {
        resetInMemory()
    }

    fun load() {
        val preferences = Gdx.app.getPreferences(PREFERENCES_NAME)
        RunInputAction.entries.forEach { action ->
            keys[action] = preferences.getInteger(action.name, action.defaultKey)
        }
        repairDuplicates()
    }

    fun key(action: RunInputAction): Int = keys[action] ?: action.defaultKey

    /** Assigns a key and swaps it with an existing conflicting action. */
    fun assign(action: RunInputAction, keyCode: Int) {
        if (keyCode == Input.Keys.UNKNOWN) return
        val previousKey = key(action)
        val conflict = RunInputAction.entries.firstOrNull {
            it != action && key(it) == keyCode
        }
        keys[action] = keyCode
        if (conflict != null) keys[conflict] = previousKey
        save()
    }

    fun snapshot(): Map<RunInputAction, Int> = EnumMap(keys)

    fun defaults(): Map<RunInputAction, Int> =
        RunInputAction.entries.associateWith { it.defaultKey }

    fun restore(snapshot: Map<RunInputAction, Int>) {
        RunInputAction.entries.forEach { action ->
            keys[action] = snapshot[action] ?: action.defaultKey
        }
        repairDuplicates()
        save()
    }

    fun resetToDefaults() {
        resetInMemory()
        save()
    }

    fun label(action: RunInputAction): String = keyLabel(key(action))

    fun keyLabel(keyCode: Int): String = Input.Keys.toString(keyCode)
        .replace("L-Alt", "Left Alt")
        .replace("R-Alt", "Right Alt")

    val moveUpKey get() = key(RunInputAction.MOVE_UP)
    val moveDownKey get() = key(RunInputAction.MOVE_DOWN)
    val moveLeftKey get() = key(RunInputAction.MOVE_LEFT)
    val moveRightKey get() = key(RunInputAction.MOVE_RIGHT)
    val weaponTamgaKey get() = key(RunInputAction.WEAPON_TAMGA)
    val armorTamgaKey get() = key(RunInputAction.ARMOR_TAMGA)
    val gameSpeedKey get() = key(RunInputAction.GAME_SPEED)
    val alternateGameSpeedKey: Int = Input.Keys.ALT_RIGHT
    val pauseKey get() = key(RunInputAction.PAUSE)
    const val resetCooldownsKey: Int = Input.Keys.R
    const val testPauseKey: Int = Input.Keys.P

    fun weaponAbilityKey(slotIndex: Int): Int = when (slotIndex) {
        0 -> key(RunInputAction.WEAPON_1)
        1 -> key(RunInputAction.WEAPON_2)
        2 -> key(RunInputAction.WEAPON_3)
        3 -> key(RunInputAction.WEAPON_4)
        4 -> key(RunInputAction.WEAPON_5)
        5 -> key(RunInputAction.WEAPON_6)
        else -> Input.Keys.UNKNOWN
    }

    fun weaponAbilityLabel(slotIndex: Int): String = keyLabel(weaponAbilityKey(slotIndex))
    fun weaponTamgaLabel(): String = label(RunInputAction.WEAPON_TAMGA)
    fun armorTamgaLabel(): String = label(RunInputAction.ARMOR_TAMGA)

    private fun resetInMemory() {
        RunInputAction.entries.forEach { keys[it] = it.defaultKey }
    }

    private fun repairDuplicates() {
        val used = mutableSetOf<Int>()
        RunInputAction.entries.forEach { action ->
            val current = key(action)
            if (!used.add(current)) {
                keys[action] = action.defaultKey
                used += action.defaultKey
            }
        }
    }

    private fun save() {
        val preferences = Gdx.app.getPreferences(PREFERENCES_NAME)
        keys.forEach { (action, keyCode) -> preferences.putInteger(action.name, keyCode) }
        preferences.flush()
    }
}
