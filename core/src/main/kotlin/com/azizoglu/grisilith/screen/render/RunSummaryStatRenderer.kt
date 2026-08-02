package com.azizoglu.grisilith.screen.render

import com.azizoglu.grisilith.summary.RunSummaryStatEntry
import com.azizoglu.grisilith.summary.StatSourceContribution
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.Align

/*
 * Summary ekranındaki iki Stat panelini ve seçili Statın
 * kaynak detay penceresini çizer.
 *
 * RunSummaryRenderer'dan ayrı tutulmuştur. Böylece mevcut
 * Summary yerleşimini yeniden yazmadan eklenebilir.
 */
class RunSummaryStatRenderer(
    private val camera: OrthographicCamera,
    private val shapeRenderer: ShapeRenderer,
    private val batch: SpriteBatch,
    private val font: BitmapFont
) {

    companion object {

        private const val PANEL_WIDTH =
            255f

        private const val PANEL_HEIGHT =
            300f

        private const val PANEL_GAP =
            18f

        private const val ROW_HEIGHT =
            28f

        private const val ROW_TOP_PADDING =
            42f
    }

    data class Layout(
        val firstPanelBounds: Rectangle,
        val secondPanelBounds: Rectangle,
        val statRowBounds: List<Rectangle>,
        val detailBounds: Rectangle
    )

    private val firstPanelBounds =
        Rectangle()

    private val secondPanelBounds =
        Rectangle()

    private val detailBounds =
        Rectangle()

    private val rowBounds =
        mutableListOf<Rectangle>()

    fun currentLayout(
        statCount: Int
    ): Layout {

        updateLayout(
            statCount
        )

        return Layout(
            firstPanelBounds =
                firstPanelBounds,

            secondPanelBounds =
                secondPanelBounds,

            statRowBounds =
                rowBounds.map {
                    Rectangle(it)
                },

            detailBounds =
                detailBounds
        )
    }

    fun renderShapes(
        stats: List<RunSummaryStatEntry>,
        hoveredIndex: Int?,
        selectedEntry: RunSummaryStatEntry?
    ) {

        updateLayout(
            stats.size
        )

        rowBounds.forEachIndexed {
                index,
                bounds ->

            val isHovered =
                hoveredIndex ==
                    index

            shapeRenderer.begin(
                ShapeRenderer.ShapeType.Filled
            )

            shapeRenderer.color =
                if (isHovered) {
                    Color(
                        0.28f,
                        0.20f,
                        0.36f,
                        0.92f
                    )
                } else {
                    Color(
                        0.075f,
                        0.062f,
                        0.095f,
                        0.82f
                    )
                }

            shapeRenderer.rect(
                bounds.x,
                bounds.y,
                bounds.width,
                bounds.height
            )

            shapeRenderer.end()

            shapeRenderer.begin(
                ShapeRenderer.ShapeType.Line
            )

            shapeRenderer.color =
                if (isHovered) {
                    Color(
                        0.76f,
                        0.62f,
                        0.92f,
                        1f
                    )
                } else {
                    Color(
                        0.28f,
                        0.22f,
                        0.35f,
                        0.90f
                    )
                }

            shapeRenderer.rect(
                bounds.x,
                bounds.y,
                bounds.width,
                bounds.height
            )

            shapeRenderer.end()
        }

        if (
            selectedEntry !=
            null
        ) {
            renderDetailShape()
        }
    }

    fun renderTexts(
        stats: List<RunSummaryStatEntry>,
        selectedEntry: RunSummaryStatEntry?
    ) {

        updateLayout(
            stats.size
        )

        val previousScaleX =
            font.data.scaleX

        val previousScaleY =
            font.data.scaleY

        val previousColor =
            Color(
                font.color
            )

        if (
            selectedEntry ==
            null
        ) {

            stats.forEachIndexed {
                    index,
                    entry ->

                val bounds =
                    rowBounds
                        .getOrNull(
                            index
                        )
                        ?: return@forEachIndexed

                renderStatRow(
                    bounds =
                        bounds,

                    entry =
                        entry
                )
            }

        } else {

            renderDetailTexts(
                selectedEntry
            )
        }

        font.data.setScale(
            previousScaleX,
            previousScaleY
        )

        font.color =
            previousColor
    }

    private fun renderPanelShape(
        bounds: Rectangle
    ) {

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Filled
        )

        shapeRenderer.color =
            Color(
                0.045f,
                0.035f,
                0.060f,
                0.96f
            )

        shapeRenderer.rect(
            bounds.x,
            bounds.y,
            bounds.width,
            bounds.height
        )

        shapeRenderer.end()

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Line
        )

        shapeRenderer.color =
            Color(
                0.46f,
                0.35f,
                0.58f,
                1f
            )

        shapeRenderer.rect(
            bounds.x,
            bounds.y,
            bounds.width,
            bounds.height
        )

        shapeRenderer.end()
    }

    private fun renderPanelTitle(
        bounds: Rectangle,
        title: String
    ) {

        font.data.setScale(
            0.67f
        )

        font.color =
            Color.GOLD

        font.draw(
            batch,
            title,
            bounds.x,
            bounds.y +
                bounds.height -
                17f,
            bounds.width,
            Align.center,
            false
        )
    }

    private fun renderStatRow(
        bounds: Rectangle,
        entry: RunSummaryStatEntry
    ) {

        font.data.setScale(
            0.47f
        )

        font.color =
            Color.LIGHT_GRAY

        font.draw(
            batch,
            entry.label,
            bounds.x +
                8f,
            bounds.y +
                19f
        )

        font.data.setScale(
            0.56f
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            entry.totalValue
                .toString(),
            bounds.x +
                bounds.width -
                48f,
            bounds.y +
                20f,
            38f,
            Align.right,
            false
        )

        /*
         * Satırın altında kısa denklem:
         *
         * 37 = 25 + 7 + 5
         *
         * Sıfır katkılar gösterilmez.
         */
        val equation =
            buildEquation(
                entry
            )

        font.data.setScale(
            0.36f
        )

        font.color =
            Color.GRAY

        font.draw(
            batch,
            equation,
            bounds.x +
                8f,
            bounds.y +
                7f,
            bounds.width -
                16f,
            Align.left,
            false
        )
    }

    private fun buildEquation(
        entry: RunSummaryStatEntry
    ): String {

        val values =
            entry
                .nonZeroContributions
                .map {
                    it.amount
                }

        return if (
            values.isEmpty()
        ) {
            entry.totalValue
                .toString()
        } else {
            entry.totalValue
                .toString() +
                " = " +
                values.joinToString(
                    separator =
                        " + "
                )
        }
    }

    private fun renderDetailShape() {

        /*
         * Detail açıkken Summary ekranının üstüne hafif
         * ikinci bir sis ekler. Arkadaki satırlara tıklama
         * Controller tarafından engellenir.
         */
        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Filled
        )

        shapeRenderer.color =
            Color(
                0.06f,
                0.015f,
                0.09f,
                0.68f
            )

        shapeRenderer.rect(
            camera.position.x -
                640f,
            camera.position.y -
                360f,
            1280f,
            720f
        )

        shapeRenderer.color =
            Color(
                0.028f,
                0.020f,
                0.042f,
                0.99f
            )

        shapeRenderer.rect(
            detailBounds.x,
            detailBounds.y,
            detailBounds.width,
            detailBounds.height
        )

        shapeRenderer.end()

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Line
        )

        repeat(
            3
        ) { index ->

            val inset =
                index *
                    4f

            shapeRenderer.color =
                Color(
                    0.68f,
                    0.48f,
                    0.86f,
                    1f -
                        index *
                        0.24f
                )

            shapeRenderer.rect(
                detailBounds.x +
                    inset,
                detailBounds.y +
                    inset,
                detailBounds.width -
                    inset *
                    2f,
                detailBounds.height -
                    inset *
                    2f
            )
        }

        shapeRenderer.end()
    }

    private fun renderDetailTexts(
        entry: RunSummaryStatEntry
    ) {

        font.data.setScale(
            1.00f
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            entry.label.uppercase(),
            detailBounds.x +
                24f,
            detailBounds.y +
                detailBounds.height -
                34f,
            detailBounds.width -
                48f,
            Align.center,
            false
        )

        font.data.setScale(
            1.70f
        )

        font.color =
            Color.GOLD

        font.draw(
            batch,
            entry.totalValue
                .toString(),
            detailBounds.x,
            detailBounds.y +
                detailBounds.height -
                88f,
            detailBounds.width,
            Align.center,
            false
        )

        font.data.setScale(
            0.58f
        )

        font.color =
            Color.LIGHT_GRAY

        font.draw(
            batch,
            "SOURCE BREAKDOWN",
            detailBounds.x +
                30f,
            detailBounds.y +
                detailBounds.height -
                126f
        )

        var y =
            detailBounds.y +
                detailBounds.height -
                156f

        entry
            .nonZeroContributions
            .forEach {
                    contribution ->

                renderContributionRow(
                    contribution =
                        contribution,

                    y =
                        y
                )

                y -=
                    31f
            }

        font.data.setScale(
            0.45f
        )

        font.color =
            Color.GRAY

        font.draw(
            batch,
            "ESC or click outside to close",
            detailBounds.x +
                24f,
            detailBounds.y +
                22f,
            detailBounds.width -
                48f,
            Align.center,
            false
        )
    }

    private fun renderContributionRow(
        contribution: StatSourceContribution,
        y: Float
    ) {

        font.data.setScale(
            0.56f
        )

        font.color =
            Color.LIGHT_GRAY

        font.draw(
            batch,
            contribution.source
                .displayName,
            detailBounds.x +
                34f,
            y
        )

        font.color =
            Color.WHITE

        val amountText =
            if (
                contribution.amount >=
                0
            ) {
                "+" +
                    contribution.amount
            } else {
                contribution.amount
                    .toString()
            }

        font.draw(
            batch,
            amountText,
            detailBounds.x +
                detailBounds.width -
                104f,
            y,
            70f,
            Align.right,
            false
        )
    }

    private fun updateLayout(
        statCount: Int
    ) {

        val centerX =
            camera.position.x

        val centerY =
            camera.position.y

        val totalWidth =
            PANEL_WIDTH *
                2f +
                PANEL_GAP

        val startX =
            centerX -
                totalWidth /
                2f

        /*
         * Artwork'in altında, Summary butonlarının üstünde.
         * Hotfix 2.2'de kısaltılan center kolonuna sığar.
         */
        val panelY =
            centerY -
                190f

        firstPanelBounds.set(
            startX,
            panelY,
            PANEL_WIDTH,
            PANEL_HEIGHT
        )

        secondPanelBounds.set(
            startX +
                PANEL_WIDTH +
                PANEL_GAP,
            panelY,
            PANEL_WIDTH,
            PANEL_HEIGHT
        )

        detailBounds.set(
            centerX -
                235f,
            centerY -
                180f,
            470f,
            360f
        )

        rowBounds.clear()

        val firstColumnCount =
            (
                statCount +
                    1
                ) /
                2

        repeat(
            statCount
        ) { index ->

            val inFirstPanel =
                index <
                    firstColumnCount

            val localIndex =
                if (inFirstPanel) {
                    index
                } else {
                    index -
                        firstColumnCount
                }

            val parent =
                if (inFirstPanel) {
                    firstPanelBounds
                } else {
                    secondPanelBounds
                }

            rowBounds.add(
                Rectangle(
                    parent.x +
                        8f,
                    parent.y +
                        parent.height -
                        ROW_TOP_PADDING -
                        (
                            localIndex +
                                1
                            ) *
                        ROW_HEIGHT,
                    parent.width -
                        16f,
                    ROW_HEIGHT -
                        3f
                )
            )
        }
    }
}
