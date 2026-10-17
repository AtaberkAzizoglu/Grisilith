package com.azizoglu.grisilith.screen.render

import com.azizoglu.grisilith.chest.ChestOpeningState
import com.azizoglu.grisilith.chest.ChestRewardRoll
import com.azizoglu.grisilith.chest.ChestRewardType
import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.statistics.StatDetailPresentation
import com.azizoglu.grisilith.ui.icon.ContentIconRenderer
import com.azizoglu.grisilith.ui.stat.StatSymbolRenderer
import com.azizoglu.grisilith.ui.theme.ButtonVisualState
import com.azizoglu.grisilith.ui.theme.PanelVisualStyle
import com.azizoglu.grisilith.ui.theme.UiComponentRenderer
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Align
import kotlin.math.ceil

class ChestRewardOverlayRenderer(
    private val camera: OrthographicCamera,
    private val cameraWidth: Float,
    private val cameraHeight: Float,
    private val shapeRenderer: ShapeRenderer,
    private val batch: SpriteBatch,
    private val font: BitmapFont,
    playerStatsProvider: () -> PlayerStats,
    private val pointerPositionProvider: () -> Vector2?
) {
    data class Layout(
        val panelBounds: Rectangle,
        val chestBounds: Rectangle,
        val rewardVisualBounds: Rectangle,
        val characterPortraitBounds: Rectangle,
        val characterStatsBounds: Rectangle,
        val takeButtonBounds: Rectangle,
        val discardButtonBounds: Rectangle,
        val itemDetailBounds: Rectangle,
        val openFiveButtonBounds: Rectangle,
        val openTenButtonBounds: Rectangle,
        val openAllButtonBounds: Rectangle,
        val rewardBounds: List<Rectangle>,
        val rewardDiscardButtonBounds: List<Rectangle>
    )

    private val panel = Rectangle()
    private val chest = Rectangle()
    private val rewardVisual = Rectangle()
    private val characterPanel = Rectangle()
    private val take = Rectangle()
    private val discard = Rectangle()
    private val openFive = Rectangle()
    private val openTen = Rectangle()
    private val openAll = Rectangle()
    private val screen = Rectangle()
    private val rewardCells = mutableListOf<Rectangle>()
    private val rewardDiscardButtons = mutableListOf<Rectangle>()
    private val sealedChest by lazy { Texture(Gdx.files.internal("ui/chests/chest_sealed.png")) }
    private val openedChest by lazy { Texture(Gdx.files.internal("ui/chests/chest_open.png")) }
    private val legendaryGlow by lazy { Texture(Gdx.files.internal("ui/chests/effects/legendary_reward_glow.png")) }
    private val artifactGlow by lazy { Texture(Gdx.files.internal("ui/chests/effects/artifact_reward_glow.png")) }
    private val mainPanelTexture by lazy {
        Texture(Gdx.files.internal("ui/rewards/stage_reward_panel_v2.png")).also {
            it.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        }
    }
    private val statsRenderer = CharacterStatsPanelRenderer(batch, font, playerStatsProvider)
    private val playerStats = playerStatsProvider

    fun update(delta: Float) = Unit

    fun currentLayout(rewardCount: Int = 1): Layout {
        updateLayout(rewardCount)
        return layout()
    }

    private fun layout() = Layout(panel, chest, rewardVisual, characterPanel, characterPanel, take, discard, Rectangle(), openFive, openTen, openAll, rewardCells, rewardDiscardButtons)

    fun renderShapes(
        state: ChestOpeningState,
        reward: ChestRewardRoll?,
        rewards: List<ChestRewardRoll>,
        revealProgress: Float,
        discardEnabled: Boolean,
        discardedRewardIndices: Set<Int>,
        remainingDiscardCount: Int,
        remainingChestCount: Int
    ) {
        val shown = if (rewards.isNotEmpty()) rewards else if (state == ChestOpeningState.REWARD_SHOWN) listOfNotNull(reward) else emptyList()
        updateLayout(shown.size.coerceAtLeast(1))
        val pointer = pointerPositionProvider()
        fun buttonState(bounds: Rectangle): ButtonVisualState =
            if (pointer?.let(bounds::contains) == true) ButtonVisualState.HOVERED else ButtonVisualState.NORMAL
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color(0.08f, 0.025f, 0.12f, 0.80f)
        shapeRenderer.rect(screen.x, screen.y, screen.width, screen.height)
        shapeRenderer.end()

        batch.projectionMatrix = camera.combined
        batch.begin()
        batch.draw(mainPanelTexture, panel.x, panel.y, panel.width, panel.height)
        UiComponentRenderer.drawPanel(batch, openedCounterBounds(), PanelVisualStyle.TITLE)
        if (shown.isNotEmpty()) {
            UiComponentRenderer.drawSimpleVerticalSeparator(
                batch,
                Rectangle(characterPanel.x - 18f, panel.y + 35f, 5f, panel.height - 70f)
            )
            shown.forEachIndexed { index, current ->
                drawRarityGlow(current, rewardCells[index])
                val disabled = index in discardedRewardIndices || remainingDiscardCount <= 0
                UiComponentRenderer.drawDangerButton(
                    batch,
                    rewardDiscardButtons[index],
                    if (disabled) ButtonVisualState.DISABLED else buttonState(rewardDiscardButtons[index])
                )
            }
            UiComponentRenderer.drawButton(batch, take, buttonState(take))
        } else {
            val alpha = if (state == ChestOpeningState.REVEALING) (1f - revealProgress).coerceIn(0f, 1f) else 1f
            batch.color = Color(1f, 1f, 1f, alpha)
            batch.draw(if (state == ChestOpeningState.UNLOCKED || state == ChestOpeningState.REVEALING) openedChest else sealedChest, chest.x, chest.y, chest.width, chest.height)
            batch.color = Color.WHITE
            if (state == ChestOpeningState.LOCKED) {
                if (remainingChestCount >= 5) UiComponentRenderer.drawButton(batch, openFive, buttonState(openFive))
                if (remainingChestCount >= 10) UiComponentRenderer.drawButton(batch, openTen, buttonState(openTen))
                UiComponentRenderer.drawButton(batch, openAll, buttonState(openAll))
            }
        }
        batch.end()
    }

    fun renderTexts(
        state: ChestOpeningState,
        reward: ChestRewardRoll?,
        rewards: List<ChestRewardRoll>,
        openedChestCount: Int,
        totalChestCount: Int,
        remainingDiscardCount: Int,
        discardedRewardIndices: Set<Int>,
        remainingChestCount: Int
    ) {
        val shown = if (rewards.isNotEmpty()) rewards else if (state == ChestOpeningState.REWARD_SHOWN) listOfNotNull(reward) else emptyList()
        updateLayout(shown.size.coerceAtLeast(1))
        val oldX = font.data.scaleX
        val oldY = font.data.scaleY
        val oldColor = Color(font.color)

        val counter = openedCounterBounds()
        font.data.setScale(0.62f)
        font.color = Color(1f, 0.78f, 0.20f, 1f)
        val counterBaseline = counter.y + (counter.height + font.capHeight) / 2f
        font.draw(batch, "CHESTS OPENED  $openedChestCount / $totalChestCount", counter.x, counterBaseline, counter.width, Align.center, false)

        if (shown.isEmpty()) {
            font.data.setScale(0.70f)
            font.color = Color.WHITE
            val instruction = when (state) {
                ChestOpeningState.LOCKED -> "Click the chest to open one reward"
                ChestOpeningState.UNLOCKED -> "Click again to reveal the reward"
                ChestOpeningState.REVEALING -> "OPENING..."
                else -> ""
            }
            font.draw(batch, instruction, chest.x - 80f, chest.y - 16f, chest.width + 160f, Align.center, false)
            if (state == ChestOpeningState.LOCKED) {
                font.data.setScale(0.56f)
                if (remainingChestCount >= 5) font.draw(batch, "OPEN 5", openFive.x, openFive.y + 38f, openFive.width, Align.center, false)
                if (remainingChestCount >= 10) font.draw(batch, "OPEN 10", openTen.x, openTen.y + 38f, openTen.width, Align.center, false)
                font.draw(batch, "OPEN ALL CHESTS", openAll.x, openAll.y + 38f, openAll.width, Align.center, false)
            }
        } else {
            shown.forEachIndexed { index, current -> renderReward(current, rewardCells[index]) }
            rewardDiscardButtons.forEachIndexed { index, button ->
                font.data.setScale(0.47f)
                font.color = when {
                    index in discardedRewardIndices -> Color(0.84f, 0.74f, 0.74f, 1f)
                    remainingDiscardCount <= 0 -> Color(0.72f, 0.68f, 0.68f, 1f)
                    else -> Color.WHITE
                }
                val discardBaseline = button.y + (button.height + font.capHeight) / 2f - 0.5f
                font.draw(batch, if (index in discardedRewardIndices) "DISCARDED" else "DISCARD", button.x, discardBaseline, button.width, Align.center, false)
            }
            font.data.setScale(0.64f)
            font.color = Color.WHITE
            font.draw(batch, if (shown.size > 1) "TAKE ALL" else "TAKE", take.x, take.y + 42f, take.width, Align.center, false)
            font.data.setScale(0.50f)
            font.color = Color.LIGHT_GRAY
            font.draw(batch, "DISCARDS LEFT  $remainingDiscardCount", take.x + take.width + 20f, take.y + 39f, 210f, Align.left, false)
            statsRenderer.render(characterPanel, pointerPositionProvider(), screen, drawBackground = false)
            renderHoveredRewardTooltip(shown)
        }

        font.data.setScale(oldX, oldY)
        font.color = oldColor
    }

    private fun renderReward(reward: ChestRewardRoll, bounds: Rectangle) {
        val iconBounds = rewardIconBounds(bounds, reward)
        val iconSize = iconBounds.width
        if (reward.rewardType == ChestRewardType.ITEM) {
            reward.itemDefinition?.let {
                ContentIconRenderer.draw(batch, it, iconBounds)
                renderItemTitle(reward.title, iconBounds)
            }
        } else {
            reward.statType?.let { StatSymbolRenderer.drawIcon(batch, it, iconBounds.x, iconBounds.y, iconSize) }
        }
    }

    private fun renderItemTitle(title: String, iconBounds: Rectangle) {
        val oldScaleX = font.data.scaleX
        val oldScaleY = font.data.scaleY
        val oldColor = Color(font.color)
        font.data.setScale(0.56f)
        font.color = Color(1f, 0.78f, 0.10f, 1f)
        font.draw(
            batch,
            title,
            iconBounds.x - 24f,
            iconBounds.y + iconBounds.height + 20f,
            iconBounds.width + 48f,
            Align.center,
            false
        )
        font.data.setScale(oldScaleX, oldScaleY)
        font.color = oldColor
    }

    private fun drawRarityGlow(reward: ChestRewardRoll, bounds: Rectangle) {
        val rarity = reward.itemDefinition?.rarity?.name ?: return
        val texture = when (rarity) {
            "LEGENDARY" -> legendaryGlow
            "ARTIFACT" -> artifactGlow
            else -> return
        }
        val multiplier = if (rarity == "ARTIFACT") 1.34f else 1.08f
        val size = minOf(bounds.width, bounds.height) * multiplier
        val centerX = bounds.x + bounds.width / 2f
        val centerY = bounds.y + 44f + (bounds.height - 44f) / 2f
        val oldColor = Color(batch.color)
        batch.color = Color(1f, 1f, 1f, if (rarity == "ARTIFACT") 0.92f else 0.72f)
        batch.draw(texture, centerX - size / 2f, centerY - size / 2f, size, size)
        batch.color = oldColor
    }

    private fun renderHoveredRewardTooltip(rewards: List<ChestRewardRoll>) {
        val pointer = pointerPositionProvider() ?: return
        val index = rewardCells.indices.firstOrNull { index ->
            val reward = rewards.getOrNull(index) ?: return@firstOrNull false
            rewardIconBounds(rewardCells[index], reward).contains(pointer)
        } ?: -1
        val reward = rewards.getOrNull(index) ?: return
        val text = if (reward.rewardType == ChestRewardType.STAT && reward.statType != null) {
            StatDetailPresentation.tooltipText(playerStats(), reward.statType)
        } else reward.description
        AdaptiveTooltipRenderer.draw(
            batch, font, camera, cameraWidth, cameraHeight, pointer,
            reward.title, text, maxWidth = 450f
        )
    }

    private fun openedCounterBounds() = Rectangle(
        rewardVisual.x + (rewardVisual.width - 370f) / 2f,
        panel.y + panel.height - 78f,
        370f,
        50f
    )

    private fun rewardIconBounds(bounds: Rectangle, reward: ChestRewardRoll): Rectangle {
        val titleSpace = if (reward.rewardType == ChestRewardType.ITEM) 28f else 0f
        val availableHeight = (bounds.height - 44f - titleSpace).coerceAtLeast(48f)
        val iconSize = minOf(
            (minOf(bounds.width, bounds.height) * 0.72f).coerceIn(48f, 250f),
            availableHeight
        )
        return Rectangle(
            bounds.x + (bounds.width - iconSize) / 2f,
            bounds.y + 44f + (availableHeight - iconSize) / 2f,
            iconSize,
            iconSize
        )
    }

    private fun updateLayout(rewardCount: Int) {
        val cx = camera.position.x
        val cy = camera.position.y
        screen.set(cx - cameraWidth / 2f, cy - cameraHeight / 2f, cameraWidth, cameraHeight)
        panel.set(cx - 650f, cy - 330f, 1300f, 660f)
        chest.set(cx - 260f, cy - 170f, 520f, 390f)
        characterPanel.set(panel.x + panel.width - 500f, panel.y + 24f, 475f, panel.height - 48f)
        take.set(panel.x + 190f, panel.y + 28f, 230f, 68f)
        discard.set(panel.x + 480f, panel.y + 28f, 230f, 68f)
        openFive.set(cx - 430f, panel.y + 35f, 230f, 68f)
        openTen.set(cx - 115f, panel.y + 35f, 230f, 68f)
        openAll.set(cx + 200f, panel.y + 35f, 230f, 68f)
        rewardVisual.set(panel.x + 28f, panel.y + 120f, panel.width - characterPanel.width - 75f, panel.height - 165f)
        rewardCells.clear()
        rewardDiscardButtons.clear()
        val columns = rewardCount.coerceAtMost(5)
        val rows = ceil(rewardCount / columns.toFloat()).toInt().coerceAtLeast(1)
        val gap = 10f
        val maximumCellWidth = when (rewardCount) {
            1 -> 390f
            in 2..3 -> 285f
            else -> 250f
        }
        val maximumCellHeight = when (rewardCount) {
            1 -> 390f
            in 2..5 -> 300f
            else -> 220f
        }
        val cellWidth = ((rewardVisual.width - gap * (columns - 1)) / columns).coerceAtMost(maximumCellWidth)
        val cellHeight = ((rewardVisual.height - gap * (rows - 1)) / rows).coerceAtMost(maximumCellHeight)
        val gridWidth = columns * cellWidth + (columns - 1) * gap
        val gridHeight = rows * cellHeight + (rows - 1) * gap
        val startX = rewardVisual.x + (rewardVisual.width - gridWidth) / 2f
        val startY = rewardVisual.y + (rewardVisual.height - gridHeight) / 2f
        repeat(rewardCount) { index ->
            val col = index % columns
            val row = index / columns
            val cell = Rectangle(startX + col * (cellWidth + gap), startY + gridHeight - (row + 1) * cellHeight - row * gap, cellWidth, cellHeight)
            rewardCells += cell
            val discardWidth = minOf(cell.width + 4f, 224f)
            rewardDiscardButtons += Rectangle(cell.x + (cell.width - discardWidth) / 2f, cell.y + 1f, discardWidth, 46f)
        }
    }
}
