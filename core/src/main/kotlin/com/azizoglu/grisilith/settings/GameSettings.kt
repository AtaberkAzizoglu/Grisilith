package com.azizoglu.grisilith.settings

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences

/*
 * Oyunun kullanıcı ayarlarını saklar.
 *
 * Değerler LibGDX Preferences sistemi sayesinde
 * oyun kapatılıp yeniden açıldığında korunur.
 */
object GameSettings {
    enum class Targeting(val label: String) {
        CLOSEST("Closest"), MOUSE("Closest to mouse icon"), BOSS("Boss prioritized")
    }
    enum class AbilityCasting(val label: String) {
        PREPARE("Prepare Aim"), INSTANT("Instant Casting"), AUTO("Auto Casting")
    }
    enum class NormalRange(val label: String) {
        OFF("Off"), ALL("All weapons"), MAIN("Main weapon")
    }
    enum class AbilityRange(val label: String) {
        OFF("Off"), PREPARING("While preparing"), ALL("Always: all weapons"), MAIN("Always: main weapon")
    }
    var targeting = Targeting.CLOSEST
        private set
    var abilityCasting = AbilityCasting.PREPARE
        private set
    val prepareAbilities: Boolean get() = abilityCasting == AbilityCasting.PREPARE
    var normalRange = NormalRange.OFF
        private set
    var abilityRange = AbilityRange.OFF
        private set

    data class Snapshot(
        val masterVolume: Float,
        val musicVolume: Float,
        val soundEffectsVolume: Float,
        val fullscreen: Boolean,
        val vSync: Boolean,
        val screenShake: Boolean,
        val damageNumbers: Boolean,
        val showGameSpeedButton: Boolean,
        val showAbilityShortcuts: Boolean,
        val showAbilityCooldowns: Boolean,
        val gameSpeed: Float,
        val bossPriorityAbilities: Boolean = false,
        val targeting: Targeting = Targeting.CLOSEST,
        val abilityCasting: AbilityCasting = AbilityCasting.PREPARE,
        val normalRange: NormalRange = NormalRange.OFF,
        val abilityRange: AbilityRange = AbilityRange.OFF,
        val showScore: Boolean = true,
        val directionalStereo: Boolean = true
    )

    private const val PREFERENCES_NAME =
        "grisilith_settings"
    private const val KEY_SHOW_SCORE = "show_score"

    private const val KEY_MASTER_VOLUME =
        "master_volume"

    private const val KEY_MUSIC_VOLUME =
        "music_volume"

    private const val KEY_SOUND_EFFECTS_VOLUME =
        "sound_effects_volume"

    private const val KEY_FULLSCREEN =
        "fullscreen"

    private const val KEY_VSYNC =
        "vsync"

    private const val KEY_SCREEN_SHAKE =
        "screen_shake"

    private const val KEY_DAMAGE_NUMBERS =
        "damage_numbers"

    private const val KEY_SHOW_GAME_SPEED_BUTTON =
        "show_game_speed_button"

    private const val KEY_SHOW_ABILITY_SHORTCUTS =
        "show_ability_shortcuts"

    private const val KEY_SHOW_ABILITY_COOLDOWNS =
        "show_ability_cooldowns"

    private const val DEFAULT_MASTER_VOLUME =
        1f

    private const val DEFAULT_MUSIC_VOLUME =
        0.8f

    private const val DEFAULT_SOUND_EFFECTS_VOLUME =
        0.8f

    /*
     * Beta/PC build varsayılanı:
     * Oyun ilk kez açıldığında fullscreen başlar.
     *
     * Kullanıcı Settings'ten WINDOWED seçerse seçim Preferences'a kaydedilir
     * ve sonraki açılışlarda kullanıcının tercihi korunur.
     */
    private const val DEFAULT_FULLSCREEN =
        true

    private const val DEFAULT_VSYNC =
        true

    private const val DEFAULT_SCREEN_SHAKE =
        true

    private const val DEFAULT_DAMAGE_NUMBERS =
        true

    private const val DEFAULT_SHOW_GAME_SPEED_BUTTON =
        true

    private const val DEFAULT_SHOW_ABILITY_SHORTCUTS =
        true

    private const val DEFAULT_SHOW_ABILITY_COOLDOWNS =
        true

    private const val VOLUME_STEP =
        0.1f

    private const val KEY_GAME_SPEED =
        "game_speed"

    private const val DEFAULT_GAME_SPEED =
        1f

    private val preferences:
        Preferences
        get() =
            Gdx.app.getPreferences(
                PREFERENCES_NAME
            )

    var masterVolume:
        Float = DEFAULT_MASTER_VOLUME
        private set

    var musicVolume:
        Float = DEFAULT_MUSIC_VOLUME
        private set

    var soundEffectsVolume:
        Float = DEFAULT_SOUND_EFFECTS_VOLUME
        private set

    var directionalStereo: Boolean = true
        private set

    var fullscreen:
        Boolean = DEFAULT_FULLSCREEN
        private set

    var vSync:
        Boolean = DEFAULT_VSYNC
        private set

    var screenShake:
        Boolean = DEFAULT_SCREEN_SHAKE
        private set

    var damageNumbers:
        Boolean = DEFAULT_DAMAGE_NUMBERS
        private set

    var showGameSpeedButton:
        Boolean =
        DEFAULT_SHOW_GAME_SPEED_BUTTON
        private set

    var showAbilityShortcuts: Boolean =
        DEFAULT_SHOW_ABILITY_SHORTCUTS
        private set

    var showAbilityCooldowns: Boolean =
        DEFAULT_SHOW_ABILITY_COOLDOWNS
        private set

    var showScore: Boolean = true
        private set

    var bossPriorityAbilities: Boolean = false
        private set

    var gameSpeed:
        Float =
        DEFAULT_GAME_SPEED
        private set

    /*
     * GrisilithGame.create() içinde bir kez
     * çağrılmalıdır.
     */
    fun load() {
        directionalStereo = preferences.getBoolean("directional_stereo",true)
        showScore = preferences.getBoolean(KEY_SHOW_SCORE,true)
        targeting = Targeting.entries.firstOrNull { it.name == preferences.getString("targeting", "CLOSEST") } ?: Targeting.CLOSEST
        abilityCasting = AbilityCasting.entries.firstOrNull { it.name == preferences.getString("ability_casting", "") }
            ?: if (preferences.getBoolean("prepare_abilities", true)) AbilityCasting.PREPARE else AbilityCasting.INSTANT
        normalRange = NormalRange.entries.firstOrNull { it.name == preferences.getString("normal_range", "OFF") } ?: NormalRange.OFF
        abilityRange = AbilityRange.entries.firstOrNull { it.name == preferences.getString("ability_range", "OFF") } ?: AbilityRange.OFF
        bossPriorityAbilities = preferences.getBoolean("boss_priority_abilities", false)
        masterVolume =
            preferences.getFloat(
                KEY_MASTER_VOLUME,
                DEFAULT_MASTER_VOLUME
            )
                .coerceIn(
                    0f,
                    1f
                )

        musicVolume =
            preferences.getFloat(
                KEY_MUSIC_VOLUME,
                DEFAULT_MUSIC_VOLUME
            )
                .coerceIn(
                    0f,
                    1f
                )

        soundEffectsVolume =
            preferences.getFloat(
                KEY_SOUND_EFFECTS_VOLUME,
                DEFAULT_SOUND_EFFECTS_VOLUME
            )
                .coerceIn(
                    0f,
                    1f
                )

        fullscreen =
            preferences.getBoolean(
                KEY_FULLSCREEN,
                DEFAULT_FULLSCREEN
            )

        vSync =
            preferences.getBoolean(
                KEY_VSYNC,
                DEFAULT_VSYNC
            )

        screenShake =
            preferences.getBoolean(
                KEY_SCREEN_SHAKE,
                DEFAULT_SCREEN_SHAKE
            )

        damageNumbers =
            preferences.getBoolean(
                KEY_DAMAGE_NUMBERS,
                DEFAULT_DAMAGE_NUMBERS
            )

        showGameSpeedButton =
            preferences.getBoolean(
                KEY_SHOW_GAME_SPEED_BUTTON,
                DEFAULT_SHOW_GAME_SPEED_BUTTON
            )

        showAbilityShortcuts =
            preferences.getBoolean(
                KEY_SHOW_ABILITY_SHORTCUTS,
                DEFAULT_SHOW_ABILITY_SHORTCUTS
            )

        showAbilityCooldowns =
            preferences.getBoolean(
                KEY_SHOW_ABILITY_COOLDOWNS,
                DEFAULT_SHOW_ABILITY_COOLDOWNS
            )

        gameSpeed =
            preferences
                .getFloat(
                    KEY_GAME_SPEED,
                    DEFAULT_GAME_SPEED
                )
                .takeIf {
                    it in
                        setOf(
                            0.5f,
                            1f,
                            2f
                        )
                }
                ?: DEFAULT_GAME_SPEED

        applyDisplaySettings()
    }

    fun increaseMasterVolume() {
        masterVolume =
            increaseVolume(
                masterVolume
            )

        save()
    }

    fun setGameSpeed(
        speed: Float
    ) {
        require(
            speed in
                setOf(
                    0.5f,
                    1f,
                    2f
                )
        ) {
            "Unsupported saved game speed: $speed"
        }

        gameSpeed =
            speed

        save()
    }

    fun decreaseMasterVolume() {
        masterVolume =
            decreaseVolume(
                masterVolume
            )

        save()
    }

    fun increaseMusicVolume() {
        musicVolume =
            increaseVolume(
                musicVolume
            )

        save()
    }

    fun decreaseMusicVolume() {
        musicVolume =
            decreaseVolume(
                musicVolume
            )

        save()
    }

    fun increaseSoundEffectsVolume() {
        soundEffectsVolume =
            increaseVolume(
                soundEffectsVolume
            )

        save()
    }

    fun decreaseSoundEffectsVolume() {
        soundEffectsVolume =
            decreaseVolume(
                soundEffectsVolume
            )

        save()
    }

    fun toggleFullscreen() {
        fullscreen =
            !fullscreen

        applyFullscreen()
        save()
    }

    fun toggleVSync() {
        vSync =
            !vSync

        Gdx.graphics.setVSync(
            vSync
        )

        save()
    }

    fun toggleScreenShake() {
        screenShake =
            !screenShake

        save()
    }

    fun toggleDamageNumbers() {
        damageNumbers =
            !damageNumbers

        save()
    }

    fun toggleGameSpeedButton() {
        showGameSpeedButton =
            !showGameSpeedButton

        save()
    }

    fun toggleAbilityShortcuts() {
        showAbilityShortcuts = !showAbilityShortcuts
        save()
    }

    fun toggleAbilityCooldowns() {
        showAbilityCooldowns = !showAbilityCooldowns
        save()
    }

    fun resetToDefaults() {
        directionalStereo = true
        targeting = Targeting.CLOSEST
        abilityCasting = AbilityCasting.PREPARE
        normalRange = NormalRange.OFF
        abilityRange = AbilityRange.OFF
        masterVolume =
            DEFAULT_MASTER_VOLUME

        musicVolume =
            DEFAULT_MUSIC_VOLUME

        soundEffectsVolume =
            DEFAULT_SOUND_EFFECTS_VOLUME

        fullscreen =
            DEFAULT_FULLSCREEN

        vSync =
            DEFAULT_VSYNC

        screenShake =
            DEFAULT_SCREEN_SHAKE

        damageNumbers =
            DEFAULT_DAMAGE_NUMBERS

        showGameSpeedButton =
            DEFAULT_SHOW_GAME_SPEED_BUTTON

        showAbilityShortcuts =
            DEFAULT_SHOW_ABILITY_SHORTCUTS

        showAbilityCooldowns =
            DEFAULT_SHOW_ABILITY_COOLDOWNS

        showScore = true

        bossPriorityAbilities = false
        gameSpeed =
            DEFAULT_GAME_SPEED

        applyDisplaySettings()
        save()
    }

    fun snapshot(): Snapshot = Snapshot(
        masterVolume = masterVolume,
        musicVolume = musicVolume,
        soundEffectsVolume = soundEffectsVolume,
        fullscreen = fullscreen,
        vSync = vSync,
        screenShake = screenShake,
        damageNumbers = damageNumbers,
        showGameSpeedButton = showGameSpeedButton,
        showAbilityShortcuts = showAbilityShortcuts,
        showAbilityCooldowns = showAbilityCooldowns,
        gameSpeed = gameSpeed,
        bossPriorityAbilities = bossPriorityAbilities,
        targeting = targeting, abilityCasting = abilityCasting,
        normalRange = normalRange, abilityRange = abilityRange,
        showScore = showScore,
        directionalStereo = directionalStereo
    )

    fun defaults(): Snapshot = Snapshot(
        masterVolume = DEFAULT_MASTER_VOLUME,
        musicVolume = DEFAULT_MUSIC_VOLUME,
        soundEffectsVolume = DEFAULT_SOUND_EFFECTS_VOLUME,
        fullscreen = fullscreen,
        vSync = DEFAULT_VSYNC,
        screenShake = DEFAULT_SCREEN_SHAKE,
        damageNumbers = DEFAULT_DAMAGE_NUMBERS,
        showGameSpeedButton = DEFAULT_SHOW_GAME_SPEED_BUTTON,
        showAbilityShortcuts = DEFAULT_SHOW_ABILITY_SHORTCUTS,
        showAbilityCooldowns = DEFAULT_SHOW_ABILITY_COOLDOWNS,
        gameSpeed = DEFAULT_GAME_SPEED,
        bossPriorityAbilities = false,
        showScore = true
    )

    fun restore(snapshot: Snapshot) {
        directionalStereo = snapshot.directionalStereo
        targeting = snapshot.targeting
        abilityCasting = snapshot.abilityCasting
        normalRange = snapshot.normalRange
        abilityRange = snapshot.abilityRange
        val fullscreenChanged = fullscreen != snapshot.fullscreen
        val vSyncChanged = vSync != snapshot.vSync
        masterVolume = snapshot.masterVolume
        musicVolume = snapshot.musicVolume
        soundEffectsVolume = snapshot.soundEffectsVolume
        fullscreen = snapshot.fullscreen
        vSync = snapshot.vSync
        screenShake = snapshot.screenShake
        damageNumbers = snapshot.damageNumbers
        showGameSpeedButton = snapshot.showGameSpeedButton
        showAbilityShortcuts = snapshot.showAbilityShortcuts
        showAbilityCooldowns = snapshot.showAbilityCooldowns
        showScore = snapshot.showScore
        bossPriorityAbilities = snapshot.bossPriorityAbilities
        gameSpeed = snapshot.gameSpeed
        if (vSyncChanged) Gdx.graphics.setVSync(vSync)
        if (fullscreenChanged) applyFullscreen()
        save()
    }

    fun effectiveMusicVolume():
        Float {

        return (
            masterVolume *
                musicVolume
            )
            .coerceIn(
                0f,
                1f
            )
    }

    fun effectiveSoundEffectsVolume():
        Float {

        return (
            masterVolume *
                soundEffectsVolume
            )
            .coerceIn(
                0f,
                1f
            )
    }

    private fun increaseVolume(
        currentValue:
        Float
    ): Float {
        return (
            currentValue +
                VOLUME_STEP
            )
            .coerceAtMost(
                1f
            )
            .roundVolume()
    }

    private fun decreaseVolume(
        currentValue:
        Float
    ): Float {
        return (
            currentValue -
                VOLUME_STEP
            )
            .coerceAtLeast(
                0f
            )
            .roundVolume()
    }

    private fun Float.roundVolume():
        Float {

        return (
            this *
                10f
            )
            .toInt() /
            10f
    }

    private fun applyDisplaySettings() {
        Gdx.graphics.setVSync(
            vSync
        )

        applyFullscreen()
    }

    private fun applyFullscreen() {
        if (fullscreen) {
            val displayMode =
                Gdx.graphics.displayMode

            Gdx.graphics.setFullscreenMode(
                displayMode
            )
        } else {
            /*
             * Şimdilik varsayılan pencere boyutu.
             * İleride Resolution ayarı geldiğinde
             * buradaki değerler kullanıcı ayarından
             * okunacak.
             */
            Gdx.graphics.setWindowedMode(
                1280,
                720
            )
        }
    }

    private fun save() {
        preferences
            .putBoolean("directional_stereo",directionalStereo)
            .putString("targeting", targeting.name)
            .putString("ability_casting", abilityCasting.name)
            .putString("normal_range", normalRange.name)
            .putString("ability_range", abilityRange.name)
            .putBoolean("boss_priority_abilities", bossPriorityAbilities)
            .putFloat(
                KEY_MASTER_VOLUME,
                masterVolume
            )
            .putFloat(
                KEY_MUSIC_VOLUME,
                musicVolume
            )
            .putFloat(
                KEY_SOUND_EFFECTS_VOLUME,
                soundEffectsVolume
            )
            .putBoolean(
                KEY_FULLSCREEN,
                fullscreen
            )
            .putBoolean(
                KEY_VSYNC,
                vSync
            )
            .putBoolean(
                KEY_SCREEN_SHAKE,
                screenShake
            )
            .putBoolean(
                KEY_DAMAGE_NUMBERS,
                damageNumbers
            )
            .putBoolean(
                KEY_SHOW_GAME_SPEED_BUTTON,
                showGameSpeedButton
            )
            .putBoolean(
                KEY_SHOW_ABILITY_SHORTCUTS,
                showAbilityShortcuts
            )
            .putBoolean(
                KEY_SHOW_ABILITY_COOLDOWNS,
                showAbilityCooldowns
            )
            .putBoolean(KEY_SHOW_SCORE,showScore)
            .putFloat(
                KEY_GAME_SPEED,
                gameSpeed
            )
            .flush()
    }
}
