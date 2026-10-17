package com.azizoglu.grisilith.screen.render

import com.azizoglu.grisilith.ui.theme.PaperUi
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Align

/** Identical paper, type sizes and wrapping in both preparation journals. */
object PaperTooltipRenderer {
    fun draw(batch: SpriteBatch, paper: PaperUi, font: BitmapFont, pointer: Vector2, title: String, body: String) {
        val oldX = font.data.scaleX; val oldY = font.data.scaleY; val oldColor = Color(font.color)
        val ink = Color(0.20f, 0.11f, 0.055f, 1f)
        val width = 440f
        font.data.setScale(0.80f)
        val heading = GlyphLayout(font, title, ink, width - 36f, Align.left, true)
        font.data.setScale(0.67f)
        val maximumBodyHeight = 470f - heading.height - 54f
        var copy = body
        fun measure(value:String)=com.azizoglu.grisilith.ui.stat.StatSymbolRenderer.drawRichText(batch,font,value,0f,0f,width-36f,measureOnly=true)
        var contentHeight = measure(copy)
        if (contentHeight > maximumBodyHeight) {
            var low = 0; var high = body.length
            while (low < high) {
                val mid = (low + high + 1) / 2
                if (measure(body.take(mid) + "…") <= maximumBodyHeight) low = mid else high = mid - 1
            }
            copy = body.take(low).trimEnd() + "…"
            contentHeight = measure(copy)
        }
        val height = heading.height + contentHeight + 54f
        val x = (pointer.x + 16f).coerceIn(8f, 1280f - width - 8f)
        val y = (pointer.y - height - 14f).coerceIn(8f, 720f - height - 8f)
        paper.popup(batch, Rectangle(x, y, width, height))
        font.color = ink; font.data.setScale(0.80f)
        val titleStat=com.azizoglu.grisilith.statistics.StatType.entries.firstOrNull{it.displayName==title}
        if(titleStat!=null) com.azizoglu.grisilith.ui.stat.StatSymbolRenderer.drawLabel(batch,font,titleStat,x+18f,y+height-18f,16f)
        else font.draw(batch, title, x + 18f, y + height - 18f, width - 36f, Align.left, true)
        font.data.setScale(0.67f)
        com.azizoglu.grisilith.ui.stat.StatSymbolRenderer.drawRichText(batch,font,copy,x+18f,y+height-32f-heading.height,width-36f)
        font.data.setScale(oldX, oldY); font.color = oldColor
    }
}
