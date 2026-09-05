package com.azizoglu.grisilith.screen.controller

import com.azizoglu.grisilith.enemy.EnemySpawner
import com.azizoglu.grisilith.player.Player
import com.azizoglu.grisilith.screen.render.ChestCharacterStatRow
import com.azizoglu.grisilith.screen.render.StageStatRewardRenderer
import com.azizoglu.grisilith.stage.StageController
import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.item.runtime.ActiveItemEffects
import com.azizoglu.grisilith.statistics.StatCard
import com.azizoglu.grisilith.statistics.StatCardGenerator
import com.azizoglu.grisilith.statistics.StatUpgradeType
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.Viewport

class StatCardController(
    private val player: Player,
    private val playerStats: PlayerStats,
    private val stageController: StageController,
    private val enemySpawner: EnemySpawner,

    private val viewport: Viewport,

    camera: OrthographicCamera,
    cameraWidth: Float,
    cameraHeight: Float,
    shapeRenderer: ShapeRenderer,
    batch: SpriteBatch,
    font: BitmapFont,

    characterNameProvider: () -> String,

    characterStatsProvider:
        () -> List<ChestCharacterStatRow>,

    private val onOpenMarket:
        () -> Unit
) {

    private var pendingCards =
        emptyList<StatCard>()

    private var openMarketAfterSelection =
        false

    private val pointerPosition =
        Vector2()

    private var hoveredCardIndex:
        Int? =
        null

    private val renderer =
        StageStatRewardRenderer(
            camera =
                camera,

            cameraWidth =
                cameraWidth,

            cameraHeight =
                cameraHeight,

            shapeRenderer =
                shapeRenderer,

            batch =
                batch,

            font =
                font,

            characterNameProvider =
                characterNameProvider,

            characterStatsProvider =
                characterStatsProvider
        )

    var isOpen:
        Boolean =
        false
        private set

    fun beginSelection(
        shouldOpenMarketAfterSelection: Boolean
    ) {

        val availableUpgradeTypes =
            StatUpgradeType.entries
                .filter {
                        upgradeType ->

                    playerStats
                        .totalScore(
                            upgradeType
                                .statType
                        ) <
                        playerStats.maximumScore(
                            upgradeType.statType
                        )
                }

        pendingCards =
            StatCardGenerator
                .generateCards(
                    currentStage =
                        stageController
                            .currentStage,

                    luckScore =
                        playerStats
                            .luckScore(),

                    availableUpgradeTypes =
                        availableUpgradeTypes
                )

        openMarketAfterSelection =
            shouldOpenMarketAfterSelection

        hoveredCardIndex =
            null

        /*
         * Normal koşulda havuz boş kalmaz.
         * Tüm desteklenen Statlar maksimumdaysa stage'i
         * kilitlememek için seçim ekranını atlarız.
         */
        if (
            pendingCards.isEmpty()
        ) {
            finishWithoutSelection()
            return
        }

        isOpen =
            true
    }

    fun update(
        delta: Float
    ) {

        if (!isOpen) {
            return
        }

        renderer.update(
            delta
        )

        updatePointerPosition()

        hoveredCardIndex =
            renderer
                .currentLayout()
                .cardPolygons
                .take(
                    pendingCards.size
                )
                .indexOfFirst {
                        polygon ->

                    polygon.contains(
                        pointerPosition.x,
                        pointerPosition.y
                    )
                }
                .takeIf {
                    it >= 0
                }
    }

    fun handleInput() {

        if (!isOpen) {
            return
        }

        when {

            Gdx.input.isKeyJustPressed(
                Input.Keys.NUM_1
            ) -> {
                selectCard(
                    0
                )
                return
            }

            Gdx.input.isKeyJustPressed(
                Input.Keys.NUM_2
            ) -> {
                selectCard(
                    1
                )
                return
            }

            Gdx.input.isKeyJustPressed(
                Input.Keys.NUM_3
            ) -> {
                selectCard(
                    2
                )
                return
            }

            Gdx.input.isKeyJustPressed(
                Input.Keys.NUM_4
            ) -> {
                selectCard(
                    3
                )
                return
            }
        }

        if (
            !Gdx.input.isButtonJustPressed(
                Input.Buttons.LEFT
            )
        ) {
            return
        }

        updatePointerPosition()

        val selectedIndex =
            renderer
                .currentLayout()
                .cardPolygons
                .take(
                    pendingCards.size
                )
                .indexOfFirst {
                        polygon ->

                    polygon.contains(
                        pointerPosition.x,
                        pointerPosition.y
                    )
                }

        if (
            selectedIndex >= 0
        ) {
            selectCard(
                selectedIndex
            )
        }
    }

    private fun selectCard(
        cardIndex: Int
    ) {

        val selectedCard =
            pendingCards
                .getOrNull(
                    cardIndex
                )
                ?: return

        player.addRunStatBonus(
            statType =
                selectedCard
                    .type
                    .statType,

            amount =
                selectedCard
                    .amount
        )

        pendingCards =
            emptyList()

        hoveredCardIndex =
            null

        isOpen =
            false

        stageController
            .completeCurrentStage()

        if (
            openMarketAfterSelection
        ) {
            openMarketAfterSelection =
                false

            onOpenMarket()
        } else {
            enemySpawner
                .resetTimer()
        }
    }

    private fun finishWithoutSelection() {

        pendingCards =
            emptyList()

        hoveredCardIndex =
            null

        isOpen =
            false

        stageController
            .completeCurrentStage()

        if (
            openMarketAfterSelection
        ) {
            openMarketAfterSelection =
                false

            onOpenMarket()
        } else {
            enemySpawner
                .resetTimer()
        }
    }

    fun renderShapes() {

        if (!isOpen) {
            return
        }

        renderer.renderShapes(
            cards =
                pendingCards,

            hoveredCardIndex =
                hoveredCardIndex
        )
    }

    fun renderTexts() {

        if (!isOpen) {
            return
        }

        renderer.renderTexts(
            cards =
                pendingCards
        )
    }

    /*
     * Eski FirstScreen çağrısı kalırsa derleme bozulmasın.
     * Doğru görünüm için renderShapes() ve renderTexts()
     * ayrı ayrı kullanılmalıdır.
     */
    fun render() {
        renderTexts()
    }

    private fun updatePointerPosition() {

        pointerPosition.set(
            Gdx.input.x.toFloat(),
            Gdx.input.y.toFloat()
        )

        viewport.unproject(
            pointerPosition
        )
    }
}
