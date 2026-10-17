package com.azizoglu.grisilith.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Rectangle
import java.util.Locale

/** Shared image headings: no enlarged bitmap-font fallback or distorted wordmarks. */
object ScreenTitleArt {
    val labels = listOf("COLLECTION","MERCHANT","CHRONICLE","SETTINGS","CHARACTER ARCHIVE",
        "WEAPON ARCHIVE","ARMOR ARCHIVE","ITEM ARCHIVE","TAMGA ARCHIVE","SUMMON ARCHIVE","BESTIARY","MAP ARCHIVE")
    private val images = mutableMapOf<String,TextureRegion>()

    fun path(label: String) = "ui/branding/headings/${label.lowercase(Locale.ROOT).replace(' ','_')}_v1.png"

    private fun image(label: String): TextureRegion = images.getOrPut(label) {
        require(label in labels) { "Unregistered heading: $label" }
        val file=Gdx.files.internal(path(label))
        val pixels=Pixmap(file)
        var left=pixels.width;var top=pixels.height;var right=-1;var bottom=-1
        for(y in 0 until pixels.height) for(x in 0 until pixels.width) {
            if((pixels.getPixel(x,y) and 255)>48) {
                left=minOf(left,x);top=minOf(top,y);right=maxOf(right,x);bottom=maxOf(bottom,y)
            }
        }
        pixels.dispose()
        require(right>=left && bottom>=top)
        val texture=Texture(file,true).apply {
            setFilter(Texture.TextureFilter.MipMapLinearLinear,Texture.TextureFilter.Linear)
        }
        TextureRegion(texture,left,top,right-left+1,bottom-top+1)
    }

    fun draw(batch: SpriteBatch, label: String, bounds: Rectangle) {
        val art=image(label)
        val scale=minOf(bounds.width/art.regionWidth,bounds.height/art.regionHeight)
        val width=art.regionWidth*scale;val height=art.regionHeight*scale
        val previous=Color(batch.color);batch.color=Color.WHITE
        batch.draw(art,bounds.x+(bounds.width-width)/2f,bounds.y+(bounds.height-height)/2f,width,height)
        batch.color=previous
    }

    fun dispose() { images.values.forEach { it.texture.dispose() };images.clear() }
}
