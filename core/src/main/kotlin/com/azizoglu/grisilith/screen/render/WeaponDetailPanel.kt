package com.azizoglu.grisilith.screen.render

import com.azizoglu.grisilith.weapon.definition.*
import com.azizoglu.grisilith.ui.icon.ContentIconRenderer
import com.azizoglu.grisilith.ui.stat.StatSymbolRenderer
import com.azizoglu.grisilith.ui.theme.*
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.*
import com.badlogic.gdx.math.*
import com.badlogic.gdx.utils.Align

/** Shared archive/merchant reference: readable core behavior, deeper data on demand. */
object WeaponDetailPanel {
    fun labels(weapon:WeaponDefinition)=listOf("BASE STATS","SCALING","LEVEL UPGRADES")+
        if(WeaponPresentation.hasChoices(weapon))listOf("SPELLS / SUMMONS") else emptyList()
    fun control(bounds:Rectangle,index:Int,count:Int=4):Rectangle {
        val width=(bounds.width-48f)/2f
        val x=if(count==3 && index==2)bounds.x+(bounds.width-width)/2f else bounds.x+18f+(index%2)*(bounds.width-28f)/2f
        return Rectangle(x,bounds.y+14f+(1-index/2)*42f,width,34f)
    }

    fun drawCopy(batch:SpriteBatch,font:BitmapFont,copy:String,bounds:Rectangle) {
        val sx=font.data.scaleX;val sy=font.data.scaleY;val color=Color(font.color)
        var scale=.82f
        do {
            font.data.setScale(scale)
            val measured=StatSymbolRenderer.drawRichText(batch,font,copy,0f,0f,bounds.width,measureOnly=true)
            if(measured<=bounds.height)break
            scale-=.02f
        }while(scale>.40f)
        font.color=Color.LIGHT_GRAY
        StatSymbolRenderer.drawRichText(batch,font,copy,bounds.x,bounds.y+bounds.height,bounds.width)
        font.data.setScale(sx,sy);font.color=color
    }

    fun render(batch:SpriteBatch,font:BitmapFont,weapon:WeaponDefinition,bounds:Rectangle) {
        val sx=font.data.scaleX;val sy=font.data.scaleY;val old=Color(font.color)
        val top=bounds.y+bounds.height-18f
        val icon=Rectangle(bounds.x+18f,top-58f,58f,58f)
        ContentIconRenderer.draw(batch,weapon,icon)
        font.data.setScale(.94f);font.color=Color.GOLD
        font.draw(batch,weapon.displayName,bounds.x+88f,top-6f,bounds.width-108f,Align.left,true)
        font.data.setScale(.65f);font.color=Color.LIGHT_GRAY
        font.draw(batch,"Base values • Level 1",bounds.x+88f,top-43f)
        val footer=bounds.y+108f
        val usable=(top-80f-footer).coerceAtLeast(100f)
        drawCopy(batch,font,WeaponPresentation.overview(weapon),Rectangle(bounds.x+18f,footer,bounds.width-36f,usable))
        val labels=labels(weapon)
        labels.forEachIndexed { i,label ->
            val r=control(bounds,i,labels.size)
            UiComponentRenderer.drawButton(batch,r,ButtonVisualState.NORMAL)
            font.data.setScale(.65f);font.color=Color.WHITE
            val l=GlyphLayout(font,label)
            if(l.width>r.width-26f)font.data.setScale(.65f*(r.width-26f)/l.width)
            font.draw(batch,label,r.x+10f,r.y+(r.height+GlyphLayout(font,label).height)/2f,r.width-20f,Align.center,false)
        }
        font.data.setScale(sx,sy);font.color=old
    }
    fun hover(batch:SpriteBatch,font:BitmapFont,weapon:WeaponDefinition,bounds:Rectangle,pointer:Vector2):Boolean {
        val labels=labels(weapon)
        val index=labels.indices.firstOrNull{control(bounds,it,labels.size).contains(pointer)} ?: return false
        val title=listOf("Base Stats","Damage Scaling","Level Upgrades","Spells / Summons")[index]
        val copy=when(index) {
            0 -> WeaponPresentation.baseStats(weapon)
            1 -> WeaponPresentation.scaling(weapon)
            2 -> WeaponPresentation.upgrades(weapon)
            else -> WeaponPresentation.choices(weapon)
        }
        AdaptiveTooltipRenderer.draw(batch,font,Rectangle(0f,0f,1280f,720f),pointer,"${weapon.displayName} — $title",copy,650f,.92f,.75f)
        return true
    }
}
