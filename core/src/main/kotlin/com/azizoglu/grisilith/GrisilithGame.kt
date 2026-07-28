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
import com.azizoglu.grisilith.screen.preset.PresetManagerScreen

class GrisilithGame : Game() {

    override fun create() {
        /*
         * Registry ilk kez burada oluşturulur.
         */
        DefaultContentRegistry.instance

        GameSettings.load()

        PresetRepository.load()

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
                game = this
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

    fun showPresetMenu() {
        changeScreen(
            PresetManagerScreen(
                game =
                    this
            )
        )
    }

    fun showCreatePreset() {
        changeScreen(
            PlaceholderMenuScreen(
                title =
                    "CREATE PRESET",

                description =
                    "The Create Preset editor will be implemented in the next step.",

                onBack = {
                    showPresetMenu()
                }
            )
        )
    }

    fun showEditPreset(
        presetId:
        String
    ) {
        changeScreen(
            PlaceholderMenuScreen(
                title =
                    "EDIT PRESET",

                description =
                    "Preset ID:\n$presetId\n\n" +
                        "The editor will open this Preset in the next step.",

                onBack = {
                    showPresetMenu()
                }
            )
        )
    }

    fun showPresetDifficultySelection(
        presetId:
        String
    ) {
        changeScreen(
            PlaceholderMenuScreen(
                title =
                    "PRESET READY",

                description =
                    "Selected Preset:\n$presetId\n\n" +
                        "Difficulty selection will be connected " +
                        "after the Create Preset editor and " +
                        "Preset run loader are completed.",

                onBack = {
                    showPresetMenu()
                }
            )
        )
    }

    fun showCollection() {
        changeScreen(
            PlaceholderMenuScreen(
                title =
                    "COLLECTION",

                description =
                    "Characters, Weapons, Armors, Items and Tamgas will be displayed here.",

                onBack = {
                    showMainMenu()
                }
            )
        )
    }

    fun showMerchant() {
        changeScreen(
            PlaceholderMenuScreen(
                title =
                    "MERCHANT FROM HALATHA",

                description =
                    "Characters, Gears, Tamgas and Cosmetics will be unlocked here with Grisilith Coin.",

                onBack = {
                    showMainMenu()
                }
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
