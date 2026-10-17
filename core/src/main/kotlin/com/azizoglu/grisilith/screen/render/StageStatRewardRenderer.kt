package com.azizoglu.grisilith.screen.render

import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.statistics.StatCard
import com.azizoglu.grisilith.settings.GameSettings
import com.azizoglu.grisilith.ui.icon.ContentIconRenderer
import com.azizoglu.grisilith.ui.stat.StatSymbolRenderer
import com.azizoglu.grisilith.ui.theme.PanelVisualStyle
import com.azizoglu.grisilith.ui.theme.UiComponentRenderer
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Polygon
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Align

class StageStatRewardRenderer(
    private val camera: OrthographicCamera,
    private val cameraWidth: Float,
    private val cameraHeight: Float,
    private val shapeRenderer: ShapeRenderer,
    private val batch: SpriteBatch,
    private val font: BitmapFont,
    characterNameProvider: () -> String,
    playerStatsProvider: () -> PlayerStats,
    private val pointerPositionProvider: () -> Vector2?
) {
    data class Layout(val panelBounds: Rectangle, val portraitBounds: Rectangle, val statsBounds: Rectangle, val cardPolygons: List<Polygon>)

    companion object {
        private const val PANEL_WIDTH = 1240f
        private const val PANEL_HEIGHT = 650f
        private const val CARD_SIZE = 132f
    }

    private val panelBounds = Rectangle()
    private val portraitBounds = Rectangle()
    private val statsBounds = Rectangle()
    private val screenBounds = Rectangle()
    private val cardPolygons = List(4) { Polygon() }
    private val statsRenderer = CharacterStatsPanelRenderer(batch, font, playerStatsProvider)
    private val characterName = characterNameProvider
    private val stagePanelTexture by lazy {
        Texture(Gdx.files.internal("ui/rewards/stage_reward_panel_v2.png")).also {
            it.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        }
    }

    fun update(delta: Float) = Unit

    fun currentLayout(): Layout {
        updateLayout()
        return Layout(panelBounds, portraitBounds, statsBounds, cardPolygons)
    }

    fun renderShapes(cards: List<StatCard>, hoveredCardIndex: Int?) {
        updateLayout()
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color(0.08f, 0.025f, 0.12f, 0.78f)
        shapeRenderer.rect(screenBounds.x, screenBounds.y, screenBounds.width, screenBounds.height)
        shapeRenderer.end()
        batch.projectionMatrix = camera.combined
        batch.begin()
        batch.draw(stagePanelTexture, panelBounds.x, panelBounds.y, panelBounds.width, panelBounds.height)
        UiComponentRenderer.drawSimpleVerticalSeparator(
            batch,
            Rectangle(statsBounds.x - 15f, panelBounds.y + 35f, 5f, panelBounds.height - 70f)
        )
        UiComponentRenderer.drawPanel(batch, portraitBounds, PanelVisualStyle.INNER)
        cards.take(4).forEachIndexed { index, card ->
            val p = cardPolygons[index]
            ContentIconRenderer.drawStatRewardFrame(batch, Rectangle(p.x - CARD_SIZE / 2f, p.y - CARD_SIZE / 2f, CARD_SIZE, CARD_SIZE), card.amount, 45f, hoveredCardIndex == index)
        }
        batch.end()
    }

    fun renderTexts(cards: List<StatCard>, hoveredCardIndex: Int?) {
        updateLayout()
        val oldScaleX = font.data.scaleX
        val oldScaleY = font.data.scaleY
        val oldColor = Color(font.color)
        statsRenderer.render(statsBounds, pointerPositionProvider(), screenBounds, drawBackground = false)
        ContentIconRenderer.drawCharacterPortrait(batch, characterName(), portraitBounds)
        font.data.setScale(0.72f)
        font.color = Color(0.20f, 0.90f, 0.92f, 1f)
        font.draw(batch, characterName(), portraitBounds.x - 12f, portraitBounds.y - 12f, portraitBounds.width + 24f, Align.center, false)
        cards.take(4).forEachIndexed { index, card ->
            renderCardText(cardPolygons[index], card, index + 1, hoveredCardIndex == index)
        }
        font.data.setScale(oldScaleX, oldScaleY)
        font.color = oldColor
    }

    private fun renderCardText(polygon: Polygon, card: StatCard, shortcut: Int, hovered: Boolean) {
        val x = polygon.x
        val y = polygon.y
        val hoverScale = if (hovered) 1.06f else 1f
        font.data.setScale(0.92f * hoverScale)
        font.color = Color.GOLD
        font.draw(batch, "+${card.amount}", x - 48f, y + 39f * hoverScale, 96f, Align.center, false)

        font.data.setScale(0.54f * hoverScale)
        val label = card.type.statType.displayName
        val textWidth = GlyphLayout(font, label).width
        val iconSize = 18f * hoverScale
        val totalWidth = iconSize + 3f + textWidth
        val startX = x - totalWidth / 2f
        StatSymbolRenderer.drawIcon(batch, card.type.statType, startX, y - 7f * hoverScale, iconSize)
        font.color = Color(0.92f, 0.18f, 0.16f, 1f)
        font.draw(batch, label, startX + iconSize + 3.7f, y + 9f * hoverScale)
        font.draw(batch, label, startX + iconSize + 3f, y + 9f * hoverScale)

        if (GameSettings.showAbilityShortcuts) {
            font.data.setScale(0.48f * hoverScale)
            font.color = Color.LIGHT_GRAY
            font.draw(batch, "[$shortcut]", x - 35f, y - 51f * hoverScale, 70f, Align.center, false)
        }
    }

    private fun updateLayout() {
        val centerX = camera.position.x
        val centerY = camera.position.y
        screenBounds.set(centerX - cameraWidth / 2f, centerY - cameraHeight / 2f, cameraWidth, cameraHeight)
        panelBounds.set(centerX - PANEL_WIDTH / 2f, centerY - PANEL_HEIGHT / 2f, PANEL_WIDTH, PANEL_HEIGHT)
        statsBounds.set(panelBounds.x + panelBounds.width - 500f, panelBounds.y + 22f, 478f, panelBounds.height - 44f)
        val clusterX = panelBounds.x + 365f
        val clusterY = centerY + 5f
        portraitBounds.set(clusterX - 92f, clusterY - 92f, 184f, 184f)
        val centers = arrayOf(clusterX to (clusterY + 205f), (clusterX + 205f) to clusterY, clusterX to (clusterY - 205f), (clusterX - 205f) to clusterY)
        val half = CARD_SIZE / 2f
        cardPolygons.forEachIndexed { index, polygon ->
            polygon.vertices = floatArrayOf(0f, half, half, 0f, 0f, -half, -half, 0f)
            polygon.setPosition(centers[index].first, centers[index].second)
        }
    }
}
