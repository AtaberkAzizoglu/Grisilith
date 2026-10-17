package com.azizoglu.grisilith.screen.render

import com.azizoglu.grisilith.statistics.StatPresentationGroup
import com.azizoglu.grisilith.statistics.StatDetailPresentation
import com.azizoglu.grisilith.summary.RunSummaryStatEntry
import com.azizoglu.grisilith.ui.stat.StatSymbolRenderer
import com.azizoglu.grisilith.ui.theme.UiComponentRenderer
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.Viewport

class RunSummaryStatRenderer(
    private val camera: OrthographicCamera,
    private val viewport: Viewport,
    private val shapeRenderer: ShapeRenderer,
    private val batch: SpriteBatch,
    private val font: BitmapFont
) {
    data class Layout(
        val firstPanelBounds: Rectangle,
        val secondPanelBounds: Rectangle,
        val statRowBounds: List<Rectangle>,
        val detailBounds: Rectangle
    )

    private val statsBounds = Rectangle()
    private val firstPanelBounds = Rectangle()
    private val secondPanelBounds = Rectangle()
    private val detailBounds = Rectangle()
    private val rowBounds = mutableListOf<Rectangle>()
    private val groupBounds = List(4) { Rectangle() }
    private val pointer = Vector2()
    private var renderAlpha = 1f

    fun currentLayout(statCount: Int): Layout {
        updateLayout(statCount)
        return Layout(
            Rectangle(firstPanelBounds),
            Rectangle(secondPanelBounds),
            rowBounds.map(::Rectangle),
            Rectangle(detailBounds)
        )
    }

    fun renderShapes(stats: List<RunSummaryStatEntry>, hoveredIndex: Int?, selectedEntry: RunSummaryStatEntry?, alpha: Float = 1f) {
        renderAlpha = alpha.coerceIn(0f, 1f)
        updateLayout(stats.size)
        hoveredIndex?.let { index ->
            rowBounds.getOrNull(index)?.let { bounds ->
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
                shapeRenderer.color = Color(0.32f, 0.22f, 0.12f, 0.28f * renderAlpha)
                shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height)
                shapeRenderer.end()
            }
        }
    }

    fun renderTexts(
        stats: List<RunSummaryStatEntry>,
        hoveredIndex: Int?,
        selectedEntry: RunSummaryStatEntry?,
        alpha: Float = 1f
    ) {
        renderAlpha = alpha.coerceIn(0f, 1f)
        updateLayout(stats.size)
        val oldScaleX = font.data.scaleX
        val oldScaleY = font.data.scaleY
        val oldColor = Color(font.color)

        UiComponentRenderer.withOpacity(renderAlpha) {
            renderStandardStats(stats)
        }
        if (renderAlpha >= 0.999f) {
            hoveredIndex?.let { index ->
                stats.getOrNull(index)?.let(::renderTooltip)
            }
        }

        font.data.setScale(oldScaleX, oldScaleY)
        font.color = oldColor
    }

    private fun renderStandardStats(stats: List<RunSummaryStatEntry>) {
        font.data.setScale(1.10f)
        font.color = faded(Color.WHITE)
        font.draw(batch, "STATS", statsBounds.x, statsBounds.y + statsBounds.height, statsBounds.width, Align.center, false)

        StatPresentationGroup.ordered.forEachIndexed { groupIndex, group ->
            val block = groupBounds[groupIndex]
            font.data.setScale(0.76f)
            font.color = faded(Color(0.72f, 0.78f, 0.90f, 1f))
            font.draw(batch, group.title, block.x, block.y + block.height - 3f, block.width, Align.center, false)
            UiComponentRenderer.drawHorizontalSeparator(
                batch,
                Rectangle(block.x + 3f, block.y + block.height - 25f, block.width - 6f, 6f)
            )
        }

        stats.forEachIndexed { index, entry ->
            val bounds = rowBounds.getOrNull(index) ?: return@forEachIndexed
            font.data.setScale(0.76f)
            StatSymbolRenderer.drawLabel(
                batch, font, entry.statType,
                bounds.x + 4f, bounds.y + bounds.height - 3f,
                20f, bounds.width - 41f
            )
            font.data.setScale(0.76f)
            font.color = faded(Color.WHITE)
            font.draw(
                batch, entry.totalValue.toString(),
                bounds.x + bounds.width - 30f, bounds.y + bounds.height - 3f,
                27f, Align.right, false
            )
        }
    }

    private fun updateLayout(statCount: Int) {
        val centerX = camera.position.x
        val centerY = camera.position.y
        statsBounds.set(centerX - 250f, centerY - 276f, 500f, 374f)
        val gap = 9f
        val blockWidth = (statsBounds.width - gap) / 2f
        val blockHeight = (statsBounds.height - 34f - gap) / 2f
        groupBounds[0].set(statsBounds.x, statsBounds.y + blockHeight + gap, blockWidth, blockHeight)
        groupBounds[1].set(statsBounds.x + blockWidth + gap, statsBounds.y + blockHeight + gap, blockWidth, blockHeight)
        groupBounds[2].set(statsBounds.x, statsBounds.y, blockWidth, blockHeight)
        groupBounds[3].set(statsBounds.x + blockWidth + gap, statsBounds.y, blockWidth, blockHeight)
        firstPanelBounds.set(groupBounds[0])
        secondPanelBounds.set(groupBounds[1])
        detailBounds.set(centerX - 235f, centerY - 180f, 470f, 360f)

        rowBounds.clear()
        var globalIndex = 0
        StatPresentationGroup.ordered.forEachIndexed { groupIndex, group ->
            val block = groupBounds[groupIndex]
            val rowHeight = ((block.height - 31f) / group.stats.size.coerceAtLeast(1)).coerceAtLeast(19f)
            var top = block.y + block.height - 29f
            group.stats.forEach {
                if (globalIndex < statCount) {
                    rowBounds += Rectangle(block.x + 3f, top - rowHeight, block.width - 6f, rowHeight)
                    globalIndex++
                }
                top -= rowHeight
            }
        }
    }

    private fun renderTooltip(entry: RunSummaryStatEntry) {
        pointer.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat())
        viewport.unproject(pointer)
        val sources = entry.nonZeroContributions.joinToString("\n") { contribution ->
            val amount = if (contribution.amount > 0) "+${contribution.amount}" else contribution.amount.toString()
            "${contribution.source.displayName}: $amount"
        }.ifBlank { "No active sources." }
        val body = buildString {
            appendLine(StatDetailPresentation.description(entry.statType))
            appendLine()
            appendLine("Total: ${entry.totalValue}")
            appendLine("Sources:")
            append(sources)
        }
        AdaptiveTooltipRenderer.draw(
            batch, font, camera, 1280f, 720f, pointer,
            entry.label, body, maxWidth = 450f
        )
    }

    private fun faded(color: Color) = Color(color).also { it.a *= renderAlpha }
}
