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
import com.azizoglu.grisilith.screen.PlaceholderMenuScreen
import com.azizoglu.grisilith.screen.PlayMenuScreen
import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.azizoglu.grisilith.settings.GameSettings
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
import com.azizoglu.grisilith.character.DefaultCharacters
import com.azizoglu.grisilith.save.RunSaveLoadResult
import com.azizoglu.grisilith.save.RunSaveSnapshot
import com.azizoglu.grisilith.screen.LoadGameScreen

class GrisilithGame : Game() {

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
            },

            {
                PresetRepository.load()
            },

            {
                PlayerProfileRepository.load()
            },

            {
                HighScoreRepository.load()
            },

            {
                if (
                    PlayerProfileRepository
                        .profile
                        .grisilithCoins < 1000
                ) {
                    PlayerProfileRepository
                        .addGrisilithCoins(
                            1000
                        )
                }
            }
        )
    }

    fun showMainMenu() {
        changeScreen(
            MainMenuScreen(
                this
            )
        )
    }

    fun showPlayMenu() {
        changeScreen(
            PlayMenuScreen(
                this
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
        changeScreen(
            MapSelectionScreen(
                game =
                    this,

                presetId =
                    presetId
            )
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

    fun showLoadGame() {

        when (
            val loadResult =
                RunSaveRepository.load()
        ) {

            is RunSaveLoadResult.Success -> {

                val snapshot =
                    loadResult.snapshot

                changeScreen(
                    LoadGameScreen(
                        snapshot =
                            snapshot,

                        onContinue = {
                            consumeSavedRun(
                                snapshot
                            )
                        },

                        onBack = {
                            showPlayMenu()
                        }
                    )
                )
            }

            RunSaveLoadResult.NoSave -> {

                showLoadFailure(
                    "No saved run was found."
                )
            }

            is RunSaveLoadResult.UnsupportedVersion -> {

                showLoadFailure(
                    "This save belongs to a newer game version."
                )
            }

            is RunSaveLoadResult.InvalidSave -> {

                showLoadFailure(
                    "The saved run is incomplete or invalid."
                )
            }

            is RunSaveLoadResult.CorruptedSave -> {

                showLoadFailure(
                    "The saved run could not be read."
                )
            }
        }
    }

    private fun consumeSavedRun(
        snapshot:
        RunSaveSnapshot
    ) {

        val difficulty =
            GameDifficulty.entries
                .firstOrNull {
                    it.name ==
                        snapshot.difficultyName
                }

        val gameMap =
            GameMap.entries
                .firstOrNull {
                    it.name ==
                        snapshot.mapName
                }

        val character =
            DefaultCharacters.all
                .firstOrNull {
                    it.id.toString() ==
                        snapshot.characterDefinitionId ||
                        it.id.value ==
                        snapshot.characterDefinitionId
                }

        if (
            difficulty == null ||
            gameMap == null ||
            character == null
        ) {
            showLoadFailure(
                "Saved character, map or difficulty is no longer available."
            )

            return
        }

        val resolvedPreset =
            snapshot.presetId
                ?.let { presetId ->

                    val preset =
                        PresetRepository
                            .findById(
                                presetId
                            )
                            ?: return@let null

                    PresetContentResolver(
                        DefaultContentRegistry.instance
                    ).resolve(
                        preset
                    )
                }

        if (
            snapshot.presetId != null &&
            resolvedPreset == null
        ) {
            showLoadFailure(
                "The preset used by this run no longer exists."
            )

            return
        }

        val saveDeleted =
            RunSaveRepository.delete()

        if (
            !saveDeleted &&
            RunSaveRepository.hasSave()
        ) {

            showLoadFailure(
                "The checkpoint could not be consumed."
            )

            return
        }

        changeScreen(
            FirstScreen(
                game =
                    this,

                difficulty =
                    difficulty,

                gameMap =
                    gameMap,

                selectedCharacter =
                    character,

                selectedPreset =
                    resolvedPreset,

                restoredRun =
                    snapshot
            )
        )
    }

    private fun showLoadFailure(
        message: String
    ) {
        changeScreen(
            PlaceholderMenuScreen(
                title =
                    "LOAD GAME",

                description =
                    message,

                onBack = {
                    showPlayMenu()
                }
            )
        )
    }

    fun showPresetMenu(
        selectedMap:
        GameMap? = null
    ) {
        changeScreen(
            PresetManagerScreen(
                game =
                    this,

                selectedMap =
                    selectedMap
            )
        )
    }

    fun showCreatePreset() {
        changeScreen(
            CreatePresetScreen(
                game = this,
                editingPresetId = null
            )
        )
    }

    fun showEditPreset(
        presetId: String
    ) {
        changeScreen(
            CreatePresetScreen(
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
        changeScreen(
            DifficultySelectionScreen(
                game =
                    this,

                gameMap =
                    gameMap,

                selectedCharacter =
                    null,

                presetId =
                    presetId
            )
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

    override fun dispose() {
        screen?.dispose()
        super.dispose()
    }
}
