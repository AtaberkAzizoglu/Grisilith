package com.azizoglu.grisilith.screen.render

import com.azizoglu.grisilith.profile.PlayerProfile
import com.azizoglu.grisilith.screen.MerchantColumn
import com.azizoglu.grisilith.unlock.UnlockDefinition
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.Align

class MerchantRenderer(
    private val grisilithCoinRenderer: GrisilithCoinRenderer,
    private val purchasedStatPointRenderer: PurchasedStatPointRenderer
) {

    companion object {
        private const val VIEWPORT_WIDTH = 1280f
        private const val TITLE_Y = 665f
        private const val SUBTITLE_Y = 620f
        private const val CURRENCY_Y = 680f
        private const val COIN_X = 1030f
        private const val STAT_POINT_X = 1160f
        private const val MESSAGE_Y = 115f
    }

    private val whitePixel: Texture = createWhitePixel()

    fun render(
        batch: SpriteBatch,
        font: BitmapFont,
        profile: PlayerProfile,
        columns: List<MerchantColumn>,
        columnRectangles: List<Rectangle>,
        cardRectanglesById: Map<String, Rectangle>,
        selectedUnlockId: String?,
        hoveredUnlockId: String?,
        pressedUnlockId: String?,
        tooltipProduct: UnlockDefinition?,
        message: String?,
        viewportRectangle: Rectangle,
        horizontalScrollOffset: Float,
        maximumHorizontalScrollOffset: Float
    ) {
        val oldScaleX = font.data.scaleX
        val oldScaleY = font.data.scaleY
        val oldColor = Color(font.color)

        renderTitle(batch, font)
        renderSubtitle(batch, font)
        renderCurrencies(batch, font, profile)

        drawRectangle(
            batch,
            viewportRectangle,
            Color(0.055f, 0.045f, 0.075f, 1f)
        )

        renderColumns(
            batch = batch,
            font = font,
            columns = columns,
            columnRectangles = columnRectangles,
            cardRectanglesById = cardRectanglesById,
            selectedUnlockId = selectedUnlockId,
            hoveredUnlockId = hoveredUnlockId,
            pressedUnlockId = pressedUnlockId,
            viewportRectangle = viewportRectangle
        )

        /*
        renderScrollHint(
            batch,
            font,
            viewportRectangle,
            horizontalScrollOffset,
            maximumHorizontalScrollOffset
        )
        */

        renderTooltip(
            batch,
            font,
            tooltipProduct,
            hoveredUnlockId,
            pressedUnlockId
        )

        renderMessage(batch, font, message)

        font.data.setScale(oldScaleX, oldScaleY)
        font.color = oldColor
    }

    private fun renderTitle(
        batch: SpriteBatch,
        font: BitmapFont
    ) {
        font.data.setScale(2f)
        font.color = Color.WHITE

        font.draw(
            batch,
            "MERCHANT FROM HALATHA",
            0f,
            TITLE_Y,
            VIEWPORT_WIDTH,
            Align.center,
            false
        )
    }

    private fun renderSubtitle(
        batch: SpriteBatch,
        font: BitmapFont
    ) {
        font.data.setScale(0.88f)
        font.color = Color.GRAY

        font.draw(
            batch,
            "Drag horizontally. Hover or hold a product for details.",
            0f,
            SUBTITLE_Y,
            VIEWPORT_WIDTH,
            Align.center,
            false
        )
    }

    private fun renderCurrencies(
        batch: SpriteBatch,
        font: BitmapFont,
        profile: PlayerProfile
    ) {
        grisilithCoinRenderer.render(
            batch = batch,
            font = font,
            amount = profile.grisilithCoins,
            x = COIN_X,
            y = CURRENCY_Y
        )

        purchasedStatPointRenderer.render(
            batch = batch,
            font = font,
            amount = profile.purchasedStatPoints,
            x = STAT_POINT_X,
            y = CURRENCY_Y
        )
    }

    private fun renderColumns(
        batch: SpriteBatch,
        font: BitmapFont,
        columns: List<MerchantColumn>,
        columnRectangles: List<Rectangle>,
        cardRectanglesById: Map<String, Rectangle>,
        selectedUnlockId: String?,
        hoveredUnlockId: String?,
        pressedUnlockId: String?,
        viewportRectangle: Rectangle
    ) {
        columns.forEachIndexed { index, column ->
            val columnRectangle =
                columnRectangles.getOrNull(index)
                    ?: return@forEachIndexed

            if (!intersects(columnRectangle, viewportRectangle)) {
                return@forEachIndexed
            }

            drawRectangle(
                batch,
                columnRectangle,
                Color(0.085f, 0.070f, 0.115f, 1f)
            )

            font.data.setScale(1.05f)
            font.color = Color.GOLD

            font.draw(
                batch,
                column.title,
                columnRectangle.x,
                columnRectangle.y + columnRectangle.height - 18f,
                columnRectangle.width,
                Align.center,
                false
            )

            if (column.products.isEmpty()) {
                font.data.setScale(0.72f)
                font.color = Color.GRAY

                font.draw(
                    batch,
                    "NO PRODUCTS",
                    columnRectangle.x,
                    columnRectangle.y + columnRectangle.height - 90f,
                    columnRectangle.width,
                    Align.center,
                    false
                )
            }

            column.products.forEach { product ->
                val card =
                    cardRectanglesById[product.id]
                        ?: return@forEach

                val cardIsInsideViewport =
                    card.x + card.width > viewportRectangle.x &&
                        card.x < viewportRectangle.x + viewportRectangle.width &&
                        card.y >= viewportRectangle.y &&
                        card.y + card.height <=
                        viewportRectangle.y + viewportRectangle.height

                if (!cardIsInsideViewport) {
                    return@forEach
                }

                val isSelected = product.id == selectedUnlockId
                val isHovered = product.id == hoveredUnlockId
                val isPressed = product.id == pressedUnlockId

                val fillColor =
                    when {
                        isPressed ->
                            Color(0.16f, 0.20f, 0.29f, 1f)

                        isSelected ->
                            Color(0.13f, 0.24f, 0.34f, 1f)

                        isHovered ->
                            Color(0.12f, 0.16f, 0.24f, 1f)

                        else ->
                            Color(0.095f, 0.105f, 0.145f, 1f)
                    }

                drawRectangle(
                    batch,
                    card,
                    fillColor
                )

                val imageRectangle =
                    Rectangle(
                        card.x + 8f,
                        card.y + 4f,
                        64f,
                        64f
                    )

                drawRectangle(
                    batch,
                    imageRectangle,
                    Color(
                        0.18f,
                        0.18f,
                        0.22f,
                        1f
                    )
                )

                val imageInnerRectangle =
                    Rectangle(
                        imageRectangle.x + 3f,
                        imageRectangle.y + 3f,
                        imageRectangle.width - 6f,
                        imageRectangle.height - 6f
                    )

                drawRectangle(
                    batch,
                    imageInnerRectangle,
                    Color(
                        0.10f,
                        0.10f,
                        0.14f,
                        1f
                    )
                )

                font.data.setScale(0.52f)
                font.color = Color.GRAY

                font.draw(
                    batch,
                    "IMAGE",
                    imageRectangle.x,
                    imageRectangle.y + 36f,
                    imageRectangle.width,
                    Align.center,
                    false
                )

                font.data.setScale(0.80f)
                font.color = Color.WHITE

                font.draw(
                    batch,
                    product.displayName,
                    card.x + 82f,
                    card.y + card.height - 14f,
                    card.width - 92f,
                    Align.left,
                    true
                )

                font.data.setScale(0.62f)
                font.color = Color.LIGHT_GRAY

                font.draw(
                    batch,
                    product.type.name.replace("_", " "),
                    card.x + 82f,
                    card.y + 31f,
                    card.width - 92f,
                    Align.left,
                    false
                )

                font.data.setScale(0.70f)
                font.color = Color.GOLD

                font.draw(
                    batch,
                    "${product.cost} G",
                    card.x + 82f,
                    card.y + 14f,
                    card.width - 92f,
                    Align.left,
                    false
                )
            }
        }
    }

    private fun renderTooltip(
        batch: SpriteBatch,
        font: BitmapFont,
        product: UnlockDefinition?,
        hoveredUnlockId: String?,
        pressedUnlockId: String?
    ) {
        if (product == null) {
            return
        }

        val panel = Rectangle(310f, 105f, 760f, 125f)

        drawRectangle(
            batch,
            panel,
            Color(0.025f, 0.025f, 0.045f, 0.97f)
        )

        font.data.setScale(0.95f)
        font.color = Color.WHITE

        font.draw(
            batch,
            product.displayName,
            panel.x + 18f,
            panel.y + panel.height - 18f
        )

        font.data.setScale(0.76f)
        font.color = Color.LIGHT_GRAY

        font.draw(
            batch,
            product.description,
            panel.x + 18f,
            panel.y + panel.height - 48f,
            panel.width - 36f,
            Align.left,
            true
        )

        font.data.setScale(0.70f)
        font.color = Color.GOLD

        val interactionText =
            if (pressedUnlockId == product.id) {
                "Release without dragging to select"
            } else if (hoveredUnlockId == product.id) {
                "Click to select"
            } else {
                ""
            }

        font.draw(
            batch,
            "$interactionText    Cost: ${product.cost} G",
            panel.x + 18f,
            panel.y + 18f,
            panel.width - 36f,
            Align.right,
            false
        )
    }

    private fun renderMessage(
        batch: SpriteBatch,
        font: BitmapFont,
        message: String?
    ) {
        if (message.isNullOrBlank()) {
            return
        }

        font.data.setScale(0.88f)
        font.color = Color.WHITE

        font.draw(
            batch,
            message,
            0f,
            MESSAGE_Y,
            VIEWPORT_WIDTH,
            Align.center,
            false
        )
    }

    private fun drawRectangle(
        batch: SpriteBatch,
        rectangle: Rectangle,
        color: Color
    ) {
        batch.color = color

        batch.draw(
            whitePixel,
            rectangle.x,
            rectangle.y,
            rectangle.width,
            rectangle.height
        )

        batch.color = Color.WHITE
    }

    private fun intersects(
        first: Rectangle,
        second: Rectangle
    ): Boolean {
        return first.x < second.x + second.width &&
            first.x + first.width > second.x &&
            first.y < second.y + second.height &&
            first.y + first.height > second.y
    }

    private fun createWhitePixel(): Texture {
        val pixmap =
            Pixmap(
                1,
                1,
                Pixmap.Format.RGBA8888
            )

        pixmap.setColor(Color.WHITE)
        pixmap.fill()

        val texture = Texture(pixmap)
        pixmap.dispose()

        return texture
    }

    fun dispose() {
        whitePixel.dispose()
    }
}
