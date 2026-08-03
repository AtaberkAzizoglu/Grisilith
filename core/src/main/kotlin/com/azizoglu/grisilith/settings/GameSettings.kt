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

    private const val PREFERENCES_NAME =
        "grisilith_settings"

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

    private const val DEFAULT_MASTER_VOLUME =
        1f

    private const val DEFAULT_MUSIC_VOLUME =
        0.8f

    private const val DEFAULT_SOUND_EFFECTS_VOLUME =
        0.8f

    private const val DEFAULT_FULLSCREEN =
        false

    private const val DEFAULT_VSYNC =
        true

    private const val DEFAULT_SCREEN_SHAKE =
        true

    private const val DEFAULT_DAMAGE_NUMBERS =
        true

    private const val DEFAULT_SHOW_GAME_SPEED_BUTTON =
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

    var gameSpeed:
        Float =
        DEFAULT_GAME_SPEED
        private set

    /*
     * GrisilithGame.create() içinde bir kez
     * çağrılmalıdır.
     */
    fun load() {
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

    fun resetToDefaults() {
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

        gameSpeed =
            DEFAULT_GAME_SPEED

        applyDisplaySettings()
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
            .putFloat(
                KEY_GAME_SPEED,
                gameSpeed
            )
            .flush()
    }
}
