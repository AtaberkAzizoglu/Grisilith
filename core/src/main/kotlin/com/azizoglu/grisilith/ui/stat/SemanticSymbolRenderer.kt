package com.azizoglu.grisilith.ui.stat

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion

/** Separate namespaces: a damage type is never the similarly named player stat. */
object SemanticSymbolRenderer {
    /** Body ink identifies the surface: dark ink on paper, light ink on stone. */
    fun readableColor(decoration: Decoration, bodyInk: Color): Color {
        val paper = bodyInk.r + bodyInk.g + bodyInk.b < 1.5f
        val color = when (decoration.kind) {
            Kind.HEADING -> if (paper) Color(.35f,.18f,.065f,1f) else Color(.94f,.76f,.44f,1f)
            Kind.DAMAGE -> if (paper) Color(.13f,.27f,.36f,1f) else Color(.66f,.84f,.91f,1f)
            else -> if (paper && decoration.kind == Kind.PATTERN) Color(.10f,.34f,.31f,1f) else Color(decoration.color)
        }
        return color.also { it.a = bodyInk.a }
    }
    const val ATLAS_PATH="ui/weapon_symbols/semantic_atlas_v1.png"
    const val ARMOR_BREAK=29
    enum class Kind { HEADING, DAMAGE, PATTERN, EFFECT }
    data class Decoration(val icon:Int,val kind:Kind,val color:Color,val boost:Float)
    private var atlas:Texture?=null
    private var regions:Array<TextureRegion>?=null
    private val headers=linkedMapOf("Attack Type" to 12,"Damage" to 13,"Base Attack Speed" to 14,
        "Range" to 15,"Scaling" to 16,"Damage Type" to 17,"Ability" to 18,
        "Base Health" to -1,"Base Armor" to -1,"Base Spell Protection" to -1)
    private val damages=linkedMapOf("Slashing" to 0,"Piercing" to 1,"Bludgeoning" to 2,"Air" to 3,
        "Water" to 4,"Fire" to 5,"Earth" to 6,"Poison" to 7,"Acid" to 8,"Necrotic" to 9,"Lightning" to 10,"Cold" to 11)
    private val patterns=linkedMapOf("Returning projectile" to 24,"Slashing" to 19,"Cone" to 20,
        "Line" to 21,"Circle" to 22,"Projectile" to 23,"Beam" to 25,"Drain" to 26,"melee" to 27,"ranged" to 28)
    private val headingRegex=headers.keys.sortedByDescending{it.length}.joinToString("|"){Regex.escape(it)}
    private val keywordRegex=(patterns.keys+damages.keys+"Armor Break").sortedByDescending{it.length}.joinToString("|"){Regex.escape(it)}
    val lexicalFragment="(?m:^(?:$headingRegex):)|Base Attack Speed|Piercing damage|\\b(?:$keywordRegex)\\b"

    fun sectionFor(token:String):String?=token.removeSuffix(":").takeIf{token.endsWith(":") && it in headers}

    fun decoration(token:String,section:String?):Decoration? {
        val heading=sectionFor(token)
        if(heading!=null)return Decoration(headers.getValue(heading),Kind.HEADING,Color(.94f,.76f,.44f,1f),.18f)
        if(token=="Armor Break")return Decoration(ARMOR_BREAK,Kind.EFFECT,Color(.91f,.48f,.18f,1f),.10f)
        if((section=="Attack Type" || section=="Range") && token in patterns)
            return Decoration(patterns.getValue(token),Kind.PATTERN,Color(.25f,.65f,.62f,1f),.10f)
        if(token=="Piercing damage")return Decoration(1,Kind.DAMAGE,Color(.66f,.84f,.91f,1f),.10f)
        if(token in damages && (token!="Piercing" || section=="Damage Type"))
            return Decoration(damages.getValue(token),Kind.DAMAGE,Color(.66f,.84f,.91f,1f),.10f)
        return null
    }

    fun drawIcon(batch:SpriteBatch,index:Int,x:Float,y:Float,size:Float) {
        val region=(regions ?: loadRegions().also{regions=it})[index]
        val scale=size*.94f/maxOf(region.regionWidth,region.regionHeight)
        val width=region.regionWidth*scale;val height=region.regionHeight*scale
        val old=Color(batch.color);batch.color=Color.WHITE
        batch.draw(region,x+(size-width)/2f,y+(size-height)/2f,width,height);batch.color=old
    }

    private fun loadRegions():Array<TextureRegion> {
        val pixels=Pixmap(Gdx.files.internal(ATLAS_PATH))
        val image=Texture(pixels).also{it.setFilter(Texture.TextureFilter.Linear,Texture.TextureFilter.Linear)}
        atlas=image
        // Generated artwork can sit a few pixels across nominal row boundaries.
        // Locate the transparent gutters, then fit each glyph without changing its aspect ratio.
        val cuts=IntArray(6){it*pixels.height/5}
        for(i in 1..4) {
            val expected=i*pixels.height/5;val radius=pixels.height/25
            var run=-1;var bestStart=expected;var bestLength=0
            for(y in expected-radius..expected+radius) {
                val empty=(0 until pixels.width).none{(pixels.getPixel(it,y) and 255)>64}
                if(empty && run<0)run=y
                if((!empty || y==expected+radius) && run>=0) {
                    val end=if(empty)y+1 else y
                    if(end-run>bestLength){bestLength=end-run;bestStart=run}
                    run=-1
                }
            }
            if(bestLength>0)cuts[i]=bestStart+bestLength/2
        }
        val result=Array(30){i ->
            val left=(i%6)*pixels.width/6;val right=(i%6+1)*pixels.width/6
            val searchTop=(cuts[i/6]-12).coerceAtLeast(0)
            val searchBottom=(cuts[i/6+1]+12).coerceAtMost(pixels.height)
            // Ignore tiny fragments of a neighboring glyph at a gutter. Preserve
            // disconnected parts (three cuts, diamonds, droplets) in the main band.
            var runStart=-1;var lastInk=-1;var ink=0
            var bestTop=searchTop;var bestBottom=searchBottom;var bestInk=-1
            fun finishBand() {
                if(runStart>=0 && ink>bestInk){bestInk=ink;bestTop=runStart;bestBottom=lastInk+1}
                runStart=-1;lastInk=-1;ink=0
            }
            for(y in searchTop until searchBottom) {
                var count=0
                for(x in left until right)if((pixels.getPixel(x,y) and 255)>64)count++
                if(count>0) {
                    if(runStart<0)runStart=y
                    lastInk=y;ink+=count
                } else if(runStart>=0 && y-lastInk>4)finishBand()
            }
            finishBand()
            val top=bestTop;val bottom=bestBottom
            var minX=right;var maxX=left;var minY=bottom;var maxY=top
            for(y in top until bottom)for(x in left until right)if((pixels.getPixel(x,y) and 255)>64) {
                minX=minOf(minX,x);maxX=maxOf(maxX,x);minY=minOf(minY,y);maxY=maxOf(maxY,y)
            }
            require(minX<=maxX && minY<=maxY){"Missing semantic glyph $i"}
            minX=(minX-2).coerceAtLeast(left);minY=(minY-2).coerceAtLeast(top)
            maxX=(maxX+2).coerceAtMost(right-1);maxY=(maxY+2).coerceAtMost(bottom-1)
            TextureRegion(image,minX,minY,maxX-minX+1,maxY-minY+1)
        }
        pixels.dispose()
        return result
    }

    fun dispose(){atlas?.dispose();atlas=null;regions=null}
}
