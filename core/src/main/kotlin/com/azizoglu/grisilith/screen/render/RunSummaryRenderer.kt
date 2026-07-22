package com.azizoglu.grisilith.screen.render

import com.azizoglu.grisilith.character.CharacterDefinition
import com.azizoglu.grisilith.config.GameDifficulty
import com.azizoglu.grisilith.config.GameMap
import com.azizoglu.grisilith.stage.StageController
import com.azizoglu.grisilith.statistics.RunStatistics
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import java.util.Locale

class RunSummaryRenderer(
    private val camera: OrthographicCamera,
    private val cameraWidth: Float,
    private val cameraHeight: Float,
    private val batch: SpriteBatch,
    private val font: BitmapFont,
    private val difficulty: GameDifficulty,
    private val gameMap: GameMap,
    private val selectedCharacter:
    CharacterDefinition,
    private val stageController: StageController,
    private val runStatistics: RunStatistics
) {

    fun render(
        displayedStage: Int,
        formattedGameTime: String
    ) {
        val startX =
            camera.position.x -
                cameraWidth / 2f +
                270f

        var currentY =
            camera.position.y +
                cameraHeight / 2f -
                90f

        val resultText =
            if (stageController.isRunComplete) {
                "RUN SUCCESSFUL"
            } else {
                "RUN FAILED"
            }

        font.draw(
            batch,
            resultText,
            startX,
            currentY
        )

        currentY -= 45f

        font.draw(
            batch,
            "Difficulty: ${difficulty.displayName}",
            startX,
            currentY
        )

        currentY -= 32f

        font.draw(
            batch,
            "Map: ${gameMap.displayName}",
            startX,
            currentY
        )

        currentY -= 32f

        font.draw(
            batch,
            "Character: " +
                selectedCharacter.displayName,
            startX,
            currentY
        )

        currentY -= 32f

        font.draw(
            batch,
            "Stage Reached: $displayedStage",
            startX,
            currentY
        )

        currentY -= 32f

        font.draw(
            batch,
            "Run Time: $formattedGameTime",
            startX,
            currentY
        )

        currentY -= 32f

        font.draw(
            batch,
            "Enemies Killed: " +
                "${runStatistics.totalEnemiesKilled}",
            startX,
            currentY
        )

        currentY -= 32f

        font.draw(
            batch,
            "Total Damage Dealt: " +
                "${runStatistics.totalDamageDealt}",
            startX,
            currentY
        )

        currentY -= 32f

        font.draw(
            batch,
            "Damage Taken: " +
                "${runStatistics.totalDamageTaken}",
            startX,
            currentY
        )

        currentY -= 32f

        font.draw(
            batch,
            "Gold Earned: " +
                String.format(
                    Locale.US,
                    "%.1f",
                    runStatistics.totalGoldCollected
                ),
            startX,
            currentY
        )

        currentY -= 50f

        font.draw(
            batch,
            "WEAPON DAMAGE",
            startX,
            currentY
        )

        currentY -= 35f

        val weaponEntries =
            runStatistics.weaponDamageStatistics
                .entries
                .sortedByDescending { entry ->
                    entry.value
                }

        if (weaponEntries.isEmpty()) {
            font.draw(
                batch,
                "No weapon damage recorded.",
                startX + 20f,
                currentY
            )

            currentY -= 30f
        } else {
            weaponEntries.forEach { entry ->
                font.draw(
                    batch,
                    "${entry.key}: ${entry.value}",
                    startX + 20f,
                    currentY
                )

                currentY -= 30f
            }
        }

        currentY -= 25f

        font.draw(
            batch,
            "R - Restart Run",
            startX,
            currentY
        )

        currentY -= 32f

        font.draw(
            batch,
            "ENTER - Return to Main Menu",
            startX,
            currentY
        )
    }
}
