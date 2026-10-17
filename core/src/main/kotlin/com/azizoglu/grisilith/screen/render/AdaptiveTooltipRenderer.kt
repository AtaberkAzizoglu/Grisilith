package com.azizoglu.grisilith.screen.render

import com.azizoglu.grisilith.ui.theme.PanelVisualStyle
import com.azizoglu.grisilith.ui.theme.UiComponentRenderer
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Align
import kotlin.math.max

/** Draws a tooltip whose panel follows the measured, wrapped text instead of a fixed rectangle. */
object AdaptiveTooltipRenderer {
    fun draw(
        batch: SpriteBatch,
        font: BitmapFont,
        camera: OrthographicCamera,
        cameraWidth: Float,
        cameraHeight: Float,
        pointer: Vector2,
        title: String,
        body: String,
        maxWidth: Float = 360f
    ) {
        draw(
            batch = batch,
            font = font,
            screenBounds = Rectangle(
                camera.position.x - cameraWidth / 2f,
                camera.position.y - cameraHeight / 2f,
                cameraWidth,
                cameraHeight
            ),
            pointer = pointer,
            title = title,
            body = body,
            maxWidth = maxWidth
        )
    }

    fun draw(
        batch: SpriteBatch,
        font: BitmapFont,
        screenBounds: Rectangle,
        pointer: Vector2,
        title: String,
        body: String,
        maxWidth: Float = 360f,
        titleScale: Float = .59f,
        bodyScale: Float = .48f
    ) {
        val oldScaleX = font.data.scaleX
        val oldScaleY = font.data.scaleY
        val oldColor = Color(font.color)
        val horizontalPadding = 18f
        val verticalPadding = 20f
        val gap = 12f

        font.data.setScale(titleScale)
        val titleNatural = GlyphLayout(font, title)
        font.data.setScale(bodyScale)
        val bodyNatural = GlyphLayout(font, body)
        val contentWidth = max(titleNatural.width, bodyNatural.width)
            .coerceIn(190f, maxWidth - horizontalPadding * 2f)

        font.data.setScale(titleScale)
        val titleLayout = GlyphLayout(font, title, Color.GOLD, contentWidth, Align.left, true)
        font.data.setScale(bodyScale)
        var actualBodyScale=bodyScale
        var bodyHeight=com.azizoglu.grisilith.ui.stat.StatSymbolRenderer.drawRichText(batch,font,body,0f,0f,contentWidth,measureOnly=true)
        while(bodyHeight+titleLayout.height+72f>screenBounds.height-16f && actualBodyScale>.46f) {
            actualBodyScale-=.02f;font.data.setScale(actualBodyScale)
            bodyHeight=com.azizoglu.grisilith.ui.stat.StatSymbolRenderer.drawRichText(batch,font,body,0f,0f,contentWidth,measureOnly=true)
        }
        val width = contentWidth + horizontalPadding * 2f
        val height = verticalPadding * 2f + titleLayout.height + gap + bodyHeight + 12f

        val screenLeft = screenBounds.x
        val screenBottom = screenBounds.y
        val screenRight = screenBounds.x + screenBounds.width
        val screenTop = screenBounds.y + screenBounds.height
        val x = (pointer.x + 18f).coerceIn(screenLeft + 8f, screenRight - width - 8f)
        val preferredY = pointer.y - height - 14f
        val y = if (preferredY >= screenBottom + 8f) preferredY else pointer.y + 18f
        val bounds = Rectangle(x, y.coerceIn(screenBottom + 8f, screenTop - height - 8f), width, height)

        UiComponentRenderer.drawPanel(batch, bounds, PanelVisualStyle.TOOLTIP)
        font.data.setScale(titleScale)
        font.color = Color.GOLD
        val titleStat=com.azizoglu.grisilith.statistics.StatType.entries.firstOrNull{it.displayName==title}
        if(titleStat!=null) com.azizoglu.grisilith.ui.stat.StatSymbolRenderer.drawLabel(batch,font,titleStat,bounds.x+horizontalPadding,bounds.y+bounds.height-verticalPadding,18f)
        else font.draw(batch, title, bounds.x + horizontalPadding, bounds.y + bounds.height - verticalPadding, contentWidth, Align.left, true)
        font.data.setScale(actualBodyScale)
        font.color = Color.WHITE
        com.azizoglu.grisilith.ui.stat.StatSymbolRenderer.drawRichText(batch,font,body,bounds.x+horizontalPadding,
            bounds.y+bounds.height-verticalPadding-titleLayout.height-gap-4f,contentWidth)

        font.data.setScale(oldScaleX, oldScaleY)
        font.color = oldColor
    }
}
