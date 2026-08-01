package com.azizoglu.grisilith.screen.render

import com.azizoglu.grisilith.statistics.StatCard
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Polygon
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.Align
import kotlin.math.sin

class StageStatRewardRenderer(
    private val camera: OrthographicCamera,
    private val cameraWidth: Float,
    private val cameraHeight: Float,
    private val shapeRenderer: ShapeRenderer,
    private val batch: SpriteBatch,
    private val font: BitmapFont,
    private val characterNameProvider: () -> String,
    private val characterStatsProvider:
        () -> List<ChestCharacterStatRow>
) {

    data class Layout(
        val panelBounds: Rectangle,
        val portraitBounds: Rectangle,
        val statsBounds: Rectangle,
        val cardPolygons: List<Polygon>
    )

    companion object {

        private const val PANEL_WIDTH =
            1060f

        private const val PANEL_HEIGHT =
            620f

        private const val CARD_SIZE =
            148f
    }

    private val panelBounds =
        Rectangle()

    private val portraitBounds =
        Rectangle()

    private val statsBounds =
        Rectangle()

    private val cardPolygons =
        List(
            4
        ) {
            Polygon()
        }

    private var animationTimeSeconds =
        0f

    fun update(
        delta: Float
    ) {

        animationTimeSeconds +=
            delta.coerceAtLeast(
                0f
            )
    }

    fun currentLayout():
        Layout {

        updateLayout()

        return Layout(
            panelBounds =
                panelBounds,

            portraitBounds =
                portraitBounds,

            statsBounds =
                statsBounds,

            cardPolygons =
                cardPolygons
        )
    }

    fun renderShapes(
        cards: List<StatCard>,
        hoveredCardIndex: Int?
    ) {

        updateLayout()

        renderFog()
        renderPanel()
        renderPortrait()
        renderStatsPanel()

        cards
            .take(
                4
            )
            .forEachIndexed {
                    index,
                    card ->

                renderDiamondCard(
                    polygon =
                        cardPolygons[index],

                    hovered =
                        hoveredCardIndex ==
                            index,

                    amount =
                        card.amount
                )
            }
    }

    fun renderTexts(
        cards: List<StatCard>
    ) {

        updateLayout()

        val oldScaleX =
            font.data.scaleX

        val oldScaleY =
            font.data.scaleY

        val oldColor =
            Color(
                font.color
            )

        renderHeader()
        renderPortraitText()
        renderStatRows()

        cards
            .take(
                4
            )
            .forEachIndexed {
                    index,
                    card ->

                renderCardText(
                    polygon =
                        cardPolygons[index],

                    card =
                        card,

                    shortcutNumber =
                        index +
                            1
                )
            }

        font.data.setScale(
            oldScaleX,
            oldScaleY
        )

        font.color =
            oldColor
    }

    private fun renderFog() {

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Filled
        )

        shapeRenderer.color =
            Color(
                0.17f,
                0.06f,
                0.24f,
                0.72f
            )

        shapeRenderer.rect(
            camera.position.x -
                cameraWidth /
                2f,

            camera.position.y -
                cameraHeight /
                2f,

            cameraWidth,
            cameraHeight
        )

        shapeRenderer.end()
    }

    private fun renderPanel() {

        val pulse =
            0.68f +
                (
                    sin(
                        animationTimeSeconds *
                            2.8f
                    ) *
                        0.5f +
                        0.5f
                    ) *
                0.20f

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Filled
        )

        shapeRenderer.color =
            Color(
                0.035f,
                0.025f,
                0.055f,
                0.84f
            )

        shapeRenderer.rect(
            panelBounds.x,
            panelBounds.y,
            panelBounds.width,
            panelBounds.height
        )

        shapeRenderer.color =
            Color(
                0.46f,
                0.30f,
                0.62f,
                0.10f
            )

        shapeRenderer.rect(
            panelBounds.x +
                10f,

            panelBounds.y +
                10f,

            panelBounds.width -
                20f,

            panelBounds.height -
                20f
        )

        shapeRenderer.end()

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Line
        )

        repeat(
            4
        ) { index ->

            val inset =
                index *
                    5f

            shapeRenderer.color =
                Color(
                    0.60f,
                    0.40f,
                    0.80f,
                    (
                        pulse -
                            index *
                            0.12f
                        )
                        .coerceIn(
                            0.16f,
                            1f
                        )
                )

            shapeRenderer.rect(
                panelBounds.x +
                    inset,

                panelBounds.y +
                    inset,

                panelBounds.width -
                    inset *
                    2f,

                panelBounds.height -
                    inset *
                    2f
            )
        }

        shapeRenderer.end()
    }

    private fun renderPortrait() {

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Filled
        )

        shapeRenderer.color =
            Color(
                0.075f,
                0.060f,
                0.095f,
                1f
            )

        shapeRenderer.rect(
            portraitBounds.x,
            portraitBounds.y,
            portraitBounds.width,
            portraitBounds.height
        )

        shapeRenderer.color =
            Color(
                0.25f,
                0.20f,
                0.31f,
                1f
            )

        shapeRenderer.rect(
            portraitBounds.x +
                15f,

            portraitBounds.y +
                15f,

            portraitBounds.width -
                30f,

            portraitBounds.height -
                30f
        )

        shapeRenderer.end()

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Line
        )

        shapeRenderer.color =
            Color(
                0.62f,
                0.42f,
                0.80f,
                1f
            )

        shapeRenderer.rect(
            portraitBounds.x,
            portraitBounds.y,
            portraitBounds.width,
            portraitBounds.height
        )

        shapeRenderer.end()
    }

    private fun renderStatsPanel() {

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
            statsBounds.x,
            statsBounds.y,
            statsBounds.width,
            statsBounds.height
        )

        shapeRenderer.end()

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Line
        )

        shapeRenderer.color =
            Color(
                0.50f,
                0.40f,
                0.62f,
                1f
            )

        shapeRenderer.rect(
            statsBounds.x,
            statsBounds.y,
            statsBounds.width,
            statsBounds.height
        )

        shapeRenderer.end()
    }

    private fun renderDiamondCard(
        polygon: Polygon,
        hovered: Boolean,
        amount: Int
    ) {

        val vertices =
            polygon.transformedVertices

        val intensity =
            when (amount) {

                5 ->
                    Color(
                        1f,
                        0.74f,
                        0.18f,
                        1f
                    )

                3 ->
                    Color(
                        0.44f,
                        0.70f,
                        1f,
                        1f
                    )

                2 ->
                    Color(
                        0.42f,
                        0.88f,
                        0.50f,
                        1f
                    )

                else ->
                    Color(
                        0.72f,
                        0.72f,
                        0.76f,
                        1f
                    )
            }

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Filled
        )

        shapeRenderer.color =
            Color(
                intensity.r,
                intensity.g,
                intensity.b,
                if (hovered) {
                    0.52f
                } else {
                    0.28f
                }
            )

        shapeRenderer.triangle(
            vertices[0],
            vertices[1],
            vertices[2],
            vertices[3],
            vertices[4],
            vertices[5]
        )

        shapeRenderer.triangle(
            vertices[0],
            vertices[1],
            vertices[4],
            vertices[5],
            vertices[6],
            vertices[7]
        )

        shapeRenderer.end()

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Line
        )

        shapeRenderer.color =
            if (hovered) {
                Color.WHITE
            } else {
                intensity
            }

        shapeRenderer.polygon(
            vertices
        )

        if (hovered) {
            val centerX =
                polygon.x

            val centerY =
                polygon.y

            val extra =
                CARD_SIZE /
                    2f +
                    7f

            shapeRenderer.polygon(
                floatArrayOf(
                    centerX,
                    centerY +
                        extra,

                    centerX +
                        extra,
                    centerY,

                    centerX,
                    centerY -
                        extra,

                    centerX -
                        extra,
                    centerY
                )
            )
        }

        shapeRenderer.end()
    }

    private fun renderHeader() {

        font.data.setScale(
            1.18f
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            "CHOOSE A STAT REWARD",
            panelBounds.x - 350f,
            panelBounds.y +
                panelBounds.height -
                38f,
            panelBounds.width,
            Align.center,
            false
        )

        font.data.setScale(
            0.72f
        )

        font.color =
            Color.LIGHT_GRAY

        font.draw(
            batch,
            "Select one card. The bonus remains for the current run.",
            panelBounds.x - 350f,
            panelBounds.y +
                panelBounds.height -
                72f,
            panelBounds.width,
            Align.center,
            false
        )
    }

    private fun renderPortraitText() {

        font.data.setScale(
            0.82f
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            characterNameProvider(),
            portraitBounds.x -
                15f,
            portraitBounds.y +
                portraitBounds.height +
                27f,
            portraitBounds.width +
                30f,
            Align.center,
            true
        )

        font.data.setScale(
            0.68f
        )

        font.color =
            Color.LIGHT_GRAY

        font.draw(
            batch,
            "PORTRAIT",
            portraitBounds.x,
            portraitBounds.y +
                portraitBounds.height /
                2f +
                5f,
            portraitBounds.width,
            Align.center,
            false
        )
    }

    private fun renderStatRows() {

        font.data.setScale(
            0.74f
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            "CURRENT STATS",
            statsBounds.x,
            statsBounds.y +
                statsBounds.height -
                22f,
            statsBounds.width,
            Align.center,
            false
        )

        var y =
            statsBounds.y +
                statsBounds.height -
                49f

        characterStatsProvider()
            .forEach {
                    row ->

                font.data.setScale(
                    0.49f
                )

                font.color =
                    Color.LIGHT_GRAY

                font.draw(
                    batch,
                    row.label,
                    statsBounds.x +
                        10f,
                    y
                )

                font.color =
                    Color.WHITE

                font.draw(
                    batch,
                    row.value,
                    statsBounds.x +
                        110f,
                    y,
                    statsBounds.width -
                        120f,
                    Align.right,
                    false
                )

                y -=
                    18f
            }
    }

    private fun renderCardText(
        polygon: Polygon,
        card: StatCard,
        shortcutNumber: Int
    ) {

        val centerX =
            polygon.x

        val centerY =
            polygon.y

        font.data.setScale(
            0.72f
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            card.title,
            centerX -
                85f,
            centerY +
                31f,
            170f,
            Align.center,
            true
        )

        font.data.setScale(
            1.25f
        )

        font.color =
            Color.GOLD

        font.draw(
            batch,
            "+${card.amount}",
            centerX -
                70f,
            centerY +
                1f,
            140f,
            Align.center,
            false
        )

        font.data.setScale(
            0.62f
        )

        font.color =
            Color.LIGHT_GRAY

        font.draw(
            batch,
            "[$shortcutNumber]",
            centerX -
                50f,
            centerY -
                38f,
            100f,
            Align.center,
            false
        )
    }

    private fun updateLayout() {

        val centerX =
            camera.position.x

        val centerY =
            camera.position.y

        panelBounds.set(
            centerX -
                PANEL_WIDTH /
                2f,

            centerY -
                PANEL_HEIGHT /
                2f,

            PANEL_WIDTH,
            PANEL_HEIGHT
        )

        portraitBounds.set(
            centerX -
                95f,

            centerY -
                95f,

            190f,
            190f
        )

        statsBounds.set(
            centerX +
                300f,

            centerY -
                230f,

            220f,
            455f
        )

        val cardCenters =
            arrayOf(
                centerX to
                    (
                        centerY +
                            205f
                        ),

                (
                    centerX +
                        205f
                    ) to
                    centerY,

                centerX to
                    (
                        centerY -
                            205f
                        ),

                (
                    centerX -
                        205f
                    ) to
                    centerY
            )

        val halfSize =
            CARD_SIZE /
                2f

        cardPolygons
            .forEachIndexed {
                    index,
                    polygon ->

                val cardCenterX =
                    cardCenters[index]
                        .first

                val cardCenterY =
                    cardCenters[index]
                        .second

                polygon.vertices =
                    floatArrayOf(
                        0f,
                        halfSize,

                        halfSize,
                        0f,

                        0f,
                        -halfSize,

                        -halfSize,
                        0f
                    )

                polygon.setPosition(
                    cardCenterX,
                    cardCenterY
                )
            }
    }
}
