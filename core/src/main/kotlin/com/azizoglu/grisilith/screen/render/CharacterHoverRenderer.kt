package com.azizoglu.grisilith.screen.render

import com.azizoglu.grisilith.character.CharacterDefinition
import com.azizoglu.grisilith.character.CharacterPresentation
import com.azizoglu.grisilith.statistics.StatPresentationGroup
import com.azizoglu.grisilith.ui.theme.*
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.*
import com.badlogic.gdx.math.*
import com.badlogic.gdx.utils.Align

/** Shared readable character copy; preset selection also includes a complete stat grid. */
object CharacterHoverRenderer {
    fun draw(batch: SpriteBatch, font: BitmapFont, pointer: Vector2, character: CharacterDefinition,
             paper: PaperUi? = null, statsOnly: Boolean = false, includeStats: Boolean = false) {
        val sx=font.data.scaleX;val sy=font.data.scaleY;val old=Color(font.color)
        val ink=if(paper==null) Color(.93f,.91f,.85f,1f) else Color(.20f,.11f,.055f,1f)
        val width=560f;val contentWidth=width-40f
        val heading=if(statsOnly) "${character.displayName} — Base Stats" else character.displayName
        fun measure(text:String, scale:Float):Float {
            font.data.setScale(scale)
            return com.azizoglu.grisilith.ui.stat.StatSymbolRenderer.drawRichText(batch,font,text,0f,0f,contentWidth,measureOnly=true)
        }
        val summary=CharacterPresentation.summary(character)
        val trait="${character.trait.displayName}: ${character.trait.description}"
        val headHeight=measure(heading,.94f)
        val summaryHeight=if(statsOnly)0f else measure(summary,.76f)
        val traitHeight=if(statsOnly)0f else measure(trait,.74f)
        val statHeight=if(statsOnly || includeStats) 294f else 0f
        val height=headHeight+summaryHeight+traitHeight+statHeight+if(statsOnly)52f else if(includeStats)132f else 100f
        val x=(pointer.x+18f).coerceIn(8f,1280f-width-8f)
        val y=(pointer.y-height-14f).coerceIn(8f,720f-height-8f)
        val panel=Rectangle(x,y,width,height)
        if(paper==null) UiComponentRenderer.drawPanel(batch,panel,PanelVisualStyle.TOOLTIP) else paper.popup(batch,panel)
        var top=y+height-20f
        fun line(text:String,scale:Float) {
            font.data.setScale(scale);font.color=ink
            val height=com.azizoglu.grisilith.ui.stat.StatSymbolRenderer.drawRichText(batch,font,text,x+20f,top,contentWidth)
            top-=height+14f
        }
        line(heading,.94f)
        if(!statsOnly) { line(summary,.76f);line("Trait",.88f);line(trait,.74f) }
        if(statsOnly || includeStats) {
            if(!statsOnly) line("Base Stats",.84f)
            val start=top
            StatPresentationGroup.ordered.forEachIndexed { index,group ->
                val gx=x+20f+(index%2)*260f
                val gy=start-(index/2)*140f
                font.data.setScale(.72f);font.color=ink
                font.draw(batch,group.title,gx,gy)
                group.stats.forEachIndexed { row,stat ->
                    font.data.setScale(.68f)
                    com.azizoglu.grisilith.ui.stat.StatSymbolRenderer.drawLabel(batch,font,stat,gx,gy-25f-row*18f,14f)
                    font.draw(batch,character.baseStats.getValue(stat).toString(),gx+218f,gy-25f-row*18f,26f,Align.right,false)
                }
            }
        }
        font.data.setScale(sx,sy);font.color=old
    }
}
