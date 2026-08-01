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
import com.azizoglu.grisilith.preset.PresetRepository
import com.azizoglu.grisilith.preset.PresetContentResolver
import com.azizoglu.grisilith.screen.preset.PresetManagerScreen
import com.azizoglu.grisilith.screen.preset.CreatePresetScreen
import com.azizoglu.grisilith.profile.PlayerProfileRepository
import com.azizoglu.grisilith.screen.CollectionScreen
import com.azizoglu.grisilith.screen.MerchantScreen
import com.azizoglu.grisilith.content.ContentCategory
import com.azizoglu.grisilith.screen.CollectionCategoryScreen

class GrisilithGame : Game() {

    override fun create() {
        /*
         * Registry ilk kez burada oluşturulur.
         */
        DefaultContentRegistry.instance

        GameSettings.load()

        PresetRepository.load()

        PlayerProfileRepository.load()

        if (
            PlayerProfileRepository
                .profile
                .grisilithCoins < 1000
        ) {
            PlayerProfileRepository
                .addGrisilithCoins(1000)
        }

        showMainMenu()
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
        changeScreen(
            PlaceholderMenuScreen(
                title =
                    "LOAD GAME",

                description =
                    "Save and Load systems will be added later.",

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
            PlaceholderMenuScreen(
                title =
                    "HIGH SCORES",

                description =
                    "Run results and score overviews will be displayed here.",

                onBack = {
                    showMainMenu()
                }
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
