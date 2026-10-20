package com.azizoglu.grisilith.screen.render

import com.azizoglu.grisilith.content.ContentDefinition
import com.azizoglu.grisilith.item.definition.ItemDefinition
import com.azizoglu.grisilith.ui.icon.ContentIconRenderer
import com.azizoglu.grisilith.ui.theme.UiComponentRenderer
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.Align

/** Identical gear ghost in Pause and Market. Item copies have level 1, not stack-count level. */
object GearDragPreview {
    fun isMergeCandidate(source: ContentDefinition, level: Int, target: com.azizoglu.grisilith.weapon.runtime.WeaponInstance): Boolean =
        level in 1 until com.azizoglu.grisilith.weapon.runtime.WeaponInstance.MAXIMUM_LEVEL &&
            source.id==target.definition.id && target.level<=level
    fun draw(batch:SpriteBatch,font:BitmapFont,definition:ContentDefinition,level:Int,x:Float,y:Float) {
        // Coordinates are in the caller's camera space (Market may follow the world camera).
        val cx=x;val cy=y
        ContentIconRenderer.draw(batch,definition,Rectangle(cx-36f,cy-36f,72f,72f))
        if (definition is ItemDefinition) return
        val panel=Rectangle(cx-54f,cy-73f,108f,34f)
        UiComponentRenderer.drawSummarySectionPanel(batch,panel)
        val sx=font.data.scaleX;val sy=font.data.scaleY;val color=Color(font.color)
        font.data.setScale(.9f);font.color=Color.WHITE
        val text="Lv. ${romanLevel(level)}"
        val height=GlyphLayout(font,text).height
        font.draw(batch,text,panel.x,panel.y+(panel.height+height)/2f,panel.width,Align.center,false)
        font.data.setScale(sx,sy);font.color=color
    }

    internal fun romanLevel(level: Int): String {
        var remaining = level.coerceIn(1, 3999)
        return buildString {
            for ((value, symbol) in listOf(1000 to "M",900 to "CM",500 to "D",400 to "CD",100 to "C",90 to "XC",50 to "L",40 to "XL",10 to "X",9 to "IX",5 to "V",4 to "IV",1 to "I")) {
                while (remaining >= value) { append(symbol); remaining -= value }
            }
        }
    }
}
