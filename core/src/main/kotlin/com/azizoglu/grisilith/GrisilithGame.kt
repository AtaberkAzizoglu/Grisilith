package com.azizoglu.grisilith

import com.azizoglu.grisilith.character.CharacterDefinition
import com.azizoglu.grisilith.config.GameDifficulty
import com.azizoglu.grisilith.config.GameMap
import com.azizoglu.grisilith.content.DefaultContentRegistry
import com.azizoglu.grisilith.screen.CharacterSelectionScreen
import com.azizoglu.grisilith.screen.DifficultySelectionScreen
import com.azizoglu.grisilith.screen.FirstScreen
import com.azizoglu.grisilith.screen.MainMenuScreen
import com.azizoglu.grisilith.screen.MapSelectionScreen
import com.azizoglu.grisilith.screen.PlayMenuScreen
import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.azizoglu.grisilith.settings.GameSettings
import com.azizoglu.grisilith.input.RunInputBindings
import com.azizoglu.grisilith.screen.SettingsScreen
import com.azizoglu.grisilith.screen.SplashLoadingScreen
import com.azizoglu.grisilith.preset.PresetRepository
import com.azizoglu.grisilith.preset.PresetContentResolver
import com.azizoglu.grisilith.screen.preset.PresetManagerScreen
import com.azizoglu.grisilith.screen.preset.CreatePresetScreen
import com.azizoglu.grisilith.profile.PlayerProfileRepository
import com.azizoglu.grisilith.screen.CollectionScreen
import com.azizoglu.grisilith.screen.MerchantScreen
import com.azizoglu.grisilith.content.ContentCategory
import com.azizoglu.grisilith.screen.CollectionCategoryScreen
import com.azizoglu.grisilith.statistics.career.HighScoreRepository
import com.azizoglu.grisilith.screen.HighScoreScreen
import com.azizoglu.grisilith.save.RunSaveRepository
import com.badlogic.gdx.Preferences
import com.azizoglu.grisilith.audio.GameAudio

class GrisilithGame : Game() {

    var online: com.azizoglu.grisilith.online.OnlineBackend? = null
        private set

    companion object {

        private const val BETA_RESET_PREFERENCES_NAME =
            "grisilith_beta_migrations"

        private const val BETA_RESET_KEY =
            "beta_clean_start_v1"
    }

    private val betaResetPreferences:
        Preferences
        get() =
            Gdx.app.getPreferences(
                BETA_RESET_PREFERENCES_NAME
            )

    private fun performOneTimeBetaResetIfNeeded() {

        if (
            betaResetPreferences
                .getBoolean(
                    BETA_RESET_KEY,
                    false
                )
        ) {
            return
        }

        HighScoreRepository
            .resetForBeta()

        PlayerProfileRepository
            .resetForBeta()

        RunSaveRepository
            .delete()

        betaResetPreferences
            .putBoolean(
                BETA_RESET_KEY,
                true
            )
            .flush()
    }

    override fun create() {
        setScreen(
            SplashLoadingScreen(
                game =
                    this,

                initializationSteps =
                    startupInitializationSteps(),

                onFinished = {
                    showMainMenu()
                }
            )
        )
    }

    private fun startupInitializationSteps():
        List<() -> Unit> {

        return listOf(
            {
                /*
                 * Registry ilk kez burada oluşturulur.
                 */
                DefaultContentRegistry.instance
            },

            {
                GameSettings.load()
                RunInputBindings.load()
                GameAudio.initialize()
            },

            {
                PresetRepository.load()
            },

            {
                performOneTimeBetaResetIfNeeded()
                /* Run continuation was removed; discard any checkpoint left by older builds. */
                RunSaveRepository.delete()
            },

            {
                PlayerProfileRepository.load()
            },

            {
                HighScoreRepository.load()
            },

            {
                online = com.azizoglu.grisilith.online.OnlineBackend.create().also { it.bootstrap() }
            },

            )
    }

    fun showMainMenu() {
        changeScreen(
            MainMenuScreen(
                this
            )
        )
    }

    fun showAudioCredits() {
        changeScreen(com.azizoglu.grisilith.screen.AudioCreditsScreen(this))
    }

    override fun setScreen(screen: Screen?) {
        // Settings opened from a paused run keeps that run's current soundtrack.
        val menu = when(screen) {
            is FirstScreen, is SplashLoadingScreen, null -> false
            is SettingsScreen -> GameAudio.inMenus()
            else -> true
        }
        GameAudio.menuContext(menu)
        GameAudio.combatPlayback(1f,true)
        super.setScreen(screen)
    }

    fun showPlayMenu(
        selectedPresetId: String? = null,
        selectedMap: GameMap? = null
    ) {
        changeScreen(
            PlayMenuScreen(
                game = this,
                initialPresetId = selectedPresetId,
                initialMap = selectedMap
            )
        )
    }

    fun showDifficultySelection(
        gameMap:
        GameMap,

        character:
        CharacterDefinition
    ) {
        changeScreen(
            DifficultySelectionScreen(
                game =
                    this,

                gameMap =
                    gameMap,

                selectedCharacter =
                    character
            )
        )
    }

    fun showMapSelection() {
        changeScreen(
            MapSelectionScreen(
                game =
                    this,

                presetId =
                    null
            )
        )
    }

    fun showPresetMapSelection(
        presetId:
        String
    ) {
        showPlayMenu(
            selectedPresetId = presetId
        )
    }

    fun showCharacterSelection(
        gameMap:
        GameMap
    ) {
        changeScreen(
            CharacterSelectionScreen(
                game =
                    this,

                gameMap =
                    gameMap
            )
        )
    }

    fun startRun(
        difficulty:
        GameDifficulty,

        gameMap:
        GameMap,

        character:
        CharacterDefinition
    ) {
        RunSaveRepository.delete()
        changeScreen(
            FirstScreen(
                game =
                    this,

                difficulty =
                    difficulty,

                gameMap =
                    gameMap,

                selectedCharacter =
                    character
            )
        )
    }

    fun showPresetMenu(
        selectedMap:
        GameMap? = null
    ) {
        changeScreen(
            com.azizoglu.grisilith.screen.preset.PresetJournalScreen(
                game =
                    this,

                selectedMap =
                    selectedMap
            )
        )
    }

    fun showCreatePreset() {
        changeScreen(
            com.azizoglu.grisilith.screen.preset.PresetJournalScreen(
                game = this,
                createNew = true
            )
        )
    }

    fun showEditPreset(
        presetId: String
    ) {
        changeScreen(
            com.azizoglu.grisilith.screen.preset.PresetJournalScreen(
                game = this,
                editingPresetId = presetId
            )
        )
    }

    fun showPresetDifficultySelection(
        presetId:
        String,

        gameMap:
        GameMap
    ) {
        showPlayMenu(
            selectedPresetId = presetId,
            selectedMap = gameMap
        )
    }

    fun startPresetRun(
        difficulty:
        GameDifficulty,

        gameMap:
        GameMap,

        presetId:
        String
    ) {
        val preset =
            PresetRepository.findById(
                presetId
            )
                ?: run {
                    showPresetMenu(
                        selectedMap =
                            gameMap
                    )

                    return
                }

        val resolvedPreset =
            PresetContentResolver(
                DefaultContentRegistry.instance
            ).resolve(
                preset
            )
                ?: run {
                    showPresetMenu(
                        selectedMap =
                            gameMap
                    )

                    return
                }

        com.azizoglu.grisilith.preset.PresetUsageRepository.markUsed(presetId)
        RunSaveRepository.delete()

        changeScreen(
            FirstScreen(
                game =
                    this,

                difficulty =
                    difficulty,

                gameMap =
                    gameMap,

                selectedCharacter =
                    resolvedPreset.character,

                selectedPreset =
                    resolvedPreset
            )
        )
    }

    fun showCollection() {

        changeScreen(
            CollectionScreen(
                game =
                    this
            )
        )
    }

    fun showCollectionCategory(
        category:
        ContentCategory
    ) {

        changeScreen(
            CollectionCategoryScreen(
                game =
                    this,

                category =
                    category
            )
        )
    }

    fun showMerchant() {
        changeScreen(
            MerchantScreen(
                game =
                    this
            )
        )
    }

    fun showHighScores() {

        changeScreen(
            HighScoreScreen(
                game =
                    this
            )
        )
    }

    fun showSettings() {
        changeScreen(
            SettingsScreen(
                game =
                    this,

                onBack = {
                    showMainMenu()
                }
            )
        )
    }

    /*
 * Pause ekranından Settings'e geçerken aktif
 * run dispose edilmez.
 */
    fun showSettingsFromPausedRun(
        pausedRun:
        FirstScreen
    ) {
        val settingsScreen =
            SettingsScreen(
                game =
                    this,

                onBack = {
                    returnToPausedRun(
                        pausedRun
                    )
                }
            )

        /*
         * changeScreen kullanılmıyor; çünkü
         * pausedRun korunmalı.
         */
        setScreen(
            settingsScreen
        )
    }

    private fun returnToPausedRun(
        pausedRun:
        FirstScreen
    ) {
        val settingsScreen =
            screen

        setScreen(
            pausedRun
        )

        if (
            settingsScreen != null &&
            settingsScreen !==
            pausedRun
        ) {
            Gdx.app.postRunnable {
                settingsScreen.dispose()
            }
        }
    }

    private fun changeScreen(
        newScreen:
        Screen
    ) {
        val oldScreen =
            screen

        setScreen(
            newScreen
        )

        if (
            oldScreen != null &&
            oldScreen !== newScreen
        ) {
            Gdx.app.postRunnable {
                oldScreen.dispose()
            }
        }
    }

    override fun render() {
        // Keep connection checks running in menus/settings too, independent of game speed.
        online?.update()
        super.render()
        // Flush one highest-priority UI event after the screen reports its actual outcome.
        GameAudio.update()
    }

    override fun pause() {GameAudio.pause();super.pause()}
    override fun resume() {super.resume();GameAudio.resume()}

    override fun dispose() {
        GameAudio.dispose()
        online?.close()
        screen?.dispose()
        com.azizoglu.grisilith.ui.icon.ContentIconRenderer.dispose()
        com.azizoglu.grisilith.ui.stat.SemanticSymbolRenderer.dispose()
        com.azizoglu.grisilith.ui.ScreenTitleArt.dispose()
        super.dispose()
    }
}
