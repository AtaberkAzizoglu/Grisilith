package com.azizoglu.grisilith.screen.render

import com.azizoglu.grisilith.collection.CollectionEntry
import com.azizoglu.grisilith.content.ContentCategory
import com.azizoglu.grisilith.ui.icon.ContentIconRenderer
import com.azizoglu.grisilith.ui.theme.ButtonVisualState
import com.azizoglu.grisilith.ui.theme.PanelVisualStyle
import com.azizoglu.grisilith.ui.theme.UiComponentRenderer
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Align

class CollectionRenderer {
    private val categoryPanel = Texture(Gdx.files.internal("ui/market/merchant_product_card.png")).apply {
        setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
    }

    private val archiveBackground = Texture(
        Gdx.files.internal("ui/backgrounds/chronicle_archive.png")
    ).apply {
        setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
    }

    fun renderBackground(batch: SpriteBatch) {
        val old = Color(batch.color)
        batch.color = Color(0.58f, 0.58f, 0.58f, 1f)
        batch.draw(archiveBackground, 0f, 0f, 1280f, 720f)
        batch.color = old
        UiComponentRenderer.drawStonePanel(
            batch,
            Rectangle(92f, 52f, 1096f, 616f)
        )
    }

    fun renderTitle(batch: SpriteBatch, font: BitmapFont) {
        val oldScaleX = font.data.scaleX
        val oldScaleY = font.data.scaleY
        val oldColor = Color(font.color)
        font.data.setScale(2.0f)
        font.color = Color(0.94f, 0.91f, 0.84f, 1f)
        com.azizoglu.grisilith.ui.ScreenTitleArt.draw(batch,"COLLECTION",Rectangle(360f,614f,560f,54f))
        font.data.setScale(oldScaleX, oldScaleY)
        font.color = oldColor
    }

    fun renderCategoryCard(
        batch: SpriteBatch,
        font: BitmapFont,
        category: ContentCategory,
        entries: List<CollectionEntry>,
        bounds: Rectangle,
        pointer: Vector2,
        pointerHeld: Boolean
    ) {
        val hovered = bounds.contains(pointer)
        val previousTint = Color(batch.color)
        batch.color = if (hovered) Color(1f, 0.93f, 0.78f, 1f) else Color.WHITE
        batch.draw(categoryPanel, bounds.x, bounds.y, bounds.width, bounds.height)
        batch.color = previousTint

        val iconBounds = Rectangle(bounds.x + 28f, bounds.y + 21f, 68f, 68f)
        if(category!=ContentCategory.SUMMON) UiComponentRenderer.drawSlot(batch, iconBounds, selected = hovered)
        val representative = entries.firstOrNull { !it.isUnknown }
        val renderedIcon = representative?.let {
            ContentIconRenderer.draw(
                batch,
                it.definition,
                Rectangle(iconBounds.x + 5f, iconBounds.y + 5f, 58f, 58f),
                locked = !it.unlocked
            )
        } ?: false

        val oldScaleX = font.data.scaleX
        val oldScaleY = font.data.scaleY
        val oldColor = Color(font.color)
        if (!renderedIcon) {
            font.data.setScale(1.12f)
            font.color = Color(0.82f, 0.66f, 0.35f, 1f)
            font.draw(batch, categoryGlyph(category), iconBounds.x, iconBounds.y + 40f, iconBounds.width, Align.center, false)
        }

        val known = entries.count { !it.isUnknown }
        val unlocked = entries.count { it.unlocked && !it.isUnknown }
        val total = entries.size.coerceAtLeast(1)
        val progressCount = if (category == ContentCategory.ENEMY || category == ContentCategory.MAP) known else unlocked
        val progressLabel = if (category == ContentCategory.ENEMY || category == ContentCategory.MAP) "DISCOVERED" else "UNLOCKED"

        font.data.setScale(1.12f)
        font.color = Color(0.94f, 0.91f, 0.84f, 1f)
        font.draw(batch, categoryTitle(category), bounds.x + 114f, bounds.y + 78f, bounds.width - 147f, Align.left, false)
        font.data.setScale(0.66f)
        font.color = Color(0.69f, 0.67f, 0.63f, 1f)
        font.draw(batch, "$progressCount / ${entries.size}  $progressLabel", bounds.x + 114f, bounds.y + 50f)
        UiComponentRenderer.drawCollectionProgressBar(
            batch,
            Rectangle(bounds.x + 120f, bounds.y + 24f, bounds.width - 172f, 9f),
            progressCount.toFloat() / total.toFloat()
        )

        font.data.setScale(oldScaleX, oldScaleY)
        font.color = oldColor
    }

    private fun categoryTitle(category: ContentCategory): String = when (category) {
        ContentCategory.CHARACTER -> "CHARACTERS"
        ContentCategory.WEAPON -> "WEAPONS"
        ContentCategory.ARMOR -> "ARMORS"
        ContentCategory.ITEM -> "ITEMS"
        ContentCategory.TAMGA -> "TAMGAS"
        ContentCategory.SUMMON -> "SUMMONS"
        ContentCategory.ENEMY -> "BESTIARY"
        ContentCategory.MAP -> "MAP ARCHIVE"
    }

    private fun categoryGlyph(category: ContentCategory): String = when (category) {
        ContentCategory.CHARACTER -> "C"
        ContentCategory.WEAPON -> "W"
        ContentCategory.ARMOR -> "A"
        ContentCategory.ITEM -> "I"
        ContentCategory.TAMGA -> "T"
        ContentCategory.SUMMON -> "S"
        ContentCategory.ENEMY -> "E"
        ContentCategory.MAP -> "M"
    }

    fun dispose() {
        archiveBackground.dispose()
        categoryPanel.dispose()
    }
}
