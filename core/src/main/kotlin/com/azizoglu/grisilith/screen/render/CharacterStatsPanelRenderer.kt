package com.azizoglu.grisilith.screen.render

import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.statistics.StatDetailPresentation
import com.azizoglu.grisilith.statistics.StatPresentationGroup
import com.azizoglu.grisilith.statistics.StatType
import com.azizoglu.grisilith.ui.stat.StatSymbolRenderer
import com.azizoglu.grisilith.ui.theme.PanelVisualStyle
import com.azizoglu.grisilith.ui.theme.UiComponentRenderer
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Align

/** Shared portrait + grouped-stat presentation used by pause and reward screens. */
class CharacterStatsPanelRenderer(
    private val batch: SpriteBatch,
    private val font: BitmapFont,
    private val playerStatsProvider: () -> PlayerStats
) {
    private val rowBounds = linkedMapOf<StatType, Rectangle>()

    fun render(
        bounds: Rectangle,
        pointer: Vector2?,
        screenBounds: Rectangle,
        drawBackground: Boolean = true,
        contentScale: Float = 1f
    ) {
        val oldScaleX = font.data.scaleX
        val oldScaleY = font.data.scaleY
        val oldColor = Color(font.color)

        if (drawBackground) {
            UiComponentRenderer.drawPanel(batch, bounds, PanelVisualStyle.INNER)
        }

        val statsTop = bounds.y + bounds.height - 24f
        font.data.setScale(0.88f * contentScale)
        font.color = Color.WHITE
        font.draw(batch, "STATS", bounds.x, statsTop, bounds.width, Align.center, false)

        val gap = 10f
        val sidePadding = 18f
        val blockWidth = (bounds.width - sidePadding * 2f - gap) / 2f
        val blockHeight = ((statsTop - 38f) - (bounds.y + 18f) - gap) / 2f
        val blocks = listOf(
            Rectangle(bounds.x + sidePadding, bounds.y + 18f + blockHeight + gap, blockWidth, blockHeight),
            Rectangle(bounds.x + sidePadding + blockWidth + gap, bounds.y + 18f + blockHeight + gap, blockWidth, blockHeight),
            Rectangle(bounds.x + sidePadding, bounds.y + 18f, blockWidth, blockHeight),
            Rectangle(bounds.x + sidePadding + blockWidth + gap, bounds.y + 18f, blockWidth, blockHeight)
        )

        rowBounds.clear()
        StatPresentationGroup.ordered.take(4).forEachIndexed { index, group ->
            val block = blocks[index]
            UiComponentRenderer.drawHorizontalSeparator(batch, Rectangle(block.x + 4f, block.y + block.height - 26f, block.width - 8f, 7f))
            font.data.setScale(0.62f * contentScale)
            font.color = Color(0.72f, 0.78f, 0.90f, 1f)
            font.draw(batch, group.title, block.x + 4f, block.y + block.height - 6f, block.width - 8f, Align.center, false)

            val rowHeight = ((block.height - 34f) / group.stats.size.coerceAtLeast(1)).coerceIn(20f, 34f)
            var baseline = block.y + block.height - 38f
            group.stats.forEach { statType ->
                val row = Rectangle(block.x + 7f, baseline - rowHeight + 5f, block.width - 14f, rowHeight)
                rowBounds[statType] = row
                font.data.setScale((if (block.width >= 210f) 0.67f else 0.55f) * contentScale)
                StatSymbolRenderer.drawLabel(
                    batch,
                    font,
                    statType,
                    row.x,
                    baseline,
                    ((rowHeight - 3f).coerceAtMost(22f) * contentScale).coerceAtMost(rowHeight - 1f),
                    row.width - 38f
                )
                font.color = Color.WHITE
                font.draw(batch, playerStatsProvider().totalScore(statType).toString(), row.x + row.width - 30f, baseline, 28f, Align.right, false)
                baseline -= rowHeight
            }
        }

        val hovered = pointer?.let { point -> rowBounds.entries.firstOrNull { it.value.contains(point) }?.key }
        if (hovered != null && pointer != null) {
            renderTooltip(hovered, pointer, screenBounds)
        }

        font.data.setScale(oldScaleX, oldScaleY)
        font.color = oldColor
    }

    private fun renderTooltip(statType: StatType, pointer: Vector2, screen: Rectangle) {
        AdaptiveTooltipRenderer.draw(
            batch = batch,
            font = font,
            screenBounds = screen,
            pointer = pointer,
            title = statType.displayName,
            body = StatDetailPresentation.tooltipText(playerStatsProvider(), statType),
            maxWidth = 450f
        )
    }
}
