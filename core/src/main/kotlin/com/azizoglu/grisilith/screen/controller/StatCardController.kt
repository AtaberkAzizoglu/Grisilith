package com.azizoglu.grisilith.screen.controller

import com.azizoglu.grisilith.enemy.EnemySpawner
import com.azizoglu.grisilith.player.Player
import com.azizoglu.grisilith.stage.StageController
import com.azizoglu.grisilith.statistics.StatCard
import com.azizoglu.grisilith.statistics.StatCardGenerator
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch

class StatCardController(
    private val player: Player,
    private val stageController: StageController,
    private val enemySpawner: EnemySpawner,
    private val camera: OrthographicCamera,
    private val cameraWidth: Float,
    private val batch: SpriteBatch,
    private val font: BitmapFont,
    private val onOpenMarket: () -> Unit
) {

    private var pendingCards =
        emptyList<StatCard>()

    private var openMarketAfterSelection =
        false

    var isOpen: Boolean = false
        private set

    fun beginSelection(
        shouldOpenMarketAfterSelection: Boolean
    ) {
        pendingCards =
            StatCardGenerator.generateCards()

        openMarketAfterSelection =
            shouldOpenMarketAfterSelection

        isOpen = true
    }

    fun handleInput() {
        if (!isOpen) {
            return
        }

        when {
            Gdx.input.isKeyJustPressed(
                Input.Keys.NUM_1
            ) -> {
                selectCard(0)
            }

            Gdx.input.isKeyJustPressed(
                Input.Keys.NUM_2
            ) -> {
                selectCard(1)
            }

            Gdx.input.isKeyJustPressed(
                Input.Keys.NUM_3
            ) -> {
                selectCard(2)
            }
        }
    }

    private fun selectCard(
        cardIndex: Int
    ) {
        val selectedCard =
            pendingCards.getOrNull(cardIndex)
                ?: return

        applyCard(selectedCard)

        pendingCards =
            emptyList()

        isOpen = false

        stageController.completeCurrentStage()

        if (openMarketAfterSelection) {
            openMarketAfterSelection = false
            onOpenMarket()
        } else {
            enemySpawner.resetTimer()
        }
    }

    private fun applyCard(
        card: StatCard
    ) {
        player.addRunStatBonus(
            statType = card.type.statType,
            amount = 1
        )
    }

    fun render() {
        if (!isOpen) {
            return
        }

        val startX =
            camera.position.x -
                cameraWidth / 2f +
                220f

        val titleY =
            camera.position.y +
                180f

        font.draw(
            batch,
            "CHOOSE A STAT CARD",
            startX,
            titleY
        )

        pendingCards.forEachIndexed {
                index,
                card ->

            val cardY =
                titleY -
                    70f -
                    index * 100f

            font.draw(
                batch,
                "${index + 1} - ${card.title}",
                startX,
                cardY
            )

            font.draw(
                batch,
                card.description,
                startX + 25f,
                cardY - 30f
            )
        }
    }
}
