package com.azizoglu.grisilith

import com.azizoglu.grisilith.config.GameDifficulty
import com.azizoglu.grisilith.screen.DifficultySelectionScreen
import com.azizoglu.grisilith.screen.FirstScreen
import com.azizoglu.grisilith.screen.MainMenuScreen
import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.azizoglu.grisilith.config.GameMap
import com.azizoglu.grisilith.screen.MapSelectionScreen

class GrisilithGame : Game() {

    override fun create() {
        showMainMenu()
    }

    fun showMainMenu() {
        changeScreen(
            MainMenuScreen(this)
        )
    }

    fun showDifficultySelection() {
        changeScreen(
            DifficultySelectionScreen(this)
        )
    }

    fun startRun(
        difficulty: GameDifficulty,
        gameMap: GameMap
    ) {
        changeScreen(
            FirstScreen(
                game = this,
                difficulty = difficulty,
                gameMap = gameMap
            )
        )
    }

    fun showMapSelection(
        difficulty: GameDifficulty
    ) {
        changeScreen(
            MapSelectionScreen(
                game = this,
                difficulty = difficulty
            )
        )
    }

    private fun changeScreen(
        newScreen: Screen
    ) {
        val oldScreen = screen

        setScreen(newScreen)

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

