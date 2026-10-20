package com.azizoglu.grisilith.screen

import com.azizoglu.grisilith.GrisilithGame
import com.azizoglu.grisilith.audio.*
import com.azizoglu.grisilith.screen.components.MenuButton
import com.azizoglu.grisilith.screen.components.MenuPointer
import com.azizoglu.grisilith.ui.input.VerticalScrollbarDragController
import com.azizoglu.grisilith.ui.theme.UiComponentRenderer
import com.azizoglu.grisilith.ui.typography.UiFontFactory
import com.badlogic.gdx.*
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g2d.*
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack
import com.badlogic.gdx.utils.*
import com.badlogic.gdx.utils.viewport.FitViewport

/** Complete shipped attribution register; contributor names intentionally await the user. */
class AudioCreditsScreen(private val game:GrisilithGame):ScreenAdapter() {
    private val camera=OrthographicCamera()
    private val viewport=FitViewport(1280f,720f,camera)
    private val pointer=MenuPointer(viewport)
    private val batch=SpriteBatch()
    private val font=UiFontFactory.createBody(24)
    private val back=MenuButton("BACK",90f,60f,220f,58f){game.showMainMenu()}
    private val list=Rectangle(100f,145f,1060f,430f)
    private val scrollbar=Rectangle(1178f,145f,22f,430f)
    private val drag=VerticalScrollbarDragController()
    private var scrollOffset=0f
    private var totalHeight=0f
    private val maximumScroll get()=(totalHeight-list.height).coerceAtLeast(0f)
    private data class Row(val layout:GlyphLayout,val top:Float,val scale:Float)
    private val rows=mutableListOf<Row>()
    private var previousInput:InputProcessor?=null
    private val input=object:InputAdapter(){override fun scrolled(amountX:Float,amountY:Float):Boolean {
        pointer.updatePosition()
        if(!list.contains(pointer.position) && !scrollbar.contains(pointer.position))return false
        scrollBy(amountY*85f);return true
    }}
    private fun scrollBy(amount:Float){scrollOffset=(scrollOffset+amount).coerceIn(0f,maximumScroll)}
    private fun rebuild() {
        rows.clear();totalHeight=0f
        fun line(text:String,heading:Boolean=false) {
            font.data.setScale(if(heading)1.12f else .82f)
            font.color=if(heading)Color(.91f,.73f,.43f,1f) else Color(.94f,.90f,.81f,1f)
            val layout=GlyphLayout(font,text,font.color,list.width-15f,Align.left,true)
            rows+=Row(layout,totalHeight,font.data.scaleX)
            totalHeight+=layout.height+if(heading)26f else 14f
        }
        line("CONTRIBUTORS",true);totalHeight+=48f
        line("MUSIC",true)
        val sources=mutableMapOf<String,JsonValue>()
        listOf("RUN-MUSIC-SELECTIONS.json","SUMMARY-MUSIC-SELECTIONS.json").forEach { path->
            Gdx.files.internal("audio/$path").takeIf{it.exists()}?.let { file->
                JsonReader().parse(file).forEach{sources[it.getString("installed")]=it}
            }
        }
        MusicTrack.entries.forEach { track->
            line("${track.title} — ${track.name.lowercase().replace('_',' ')}",true)
            line("Kevin MacLeod (incompetech.com)")
            line("Creative Commons Attribution 4.0 — https://creativecommons.org/licenses/by/4.0/")
            line("Source: "+if(track==MusicTrack.MENU)"https://incompetech.com/music/royalty-free/index.html?isrc=USUAN1600052"
                else checkNotNull(sources[track.path]){"Missing music attribution: ${track.path}"}.getString("source"))
            line("Recording unchanged. "+if(track.looping)"Playback repeats the complete track." else "Plays once on the Summary screen.")
            totalHeight+=15f
        }
        line("SOUND EFFECTS / LICENSE AND SOURCE REGISTER",true)
        listOf("CREDITS.txt","CHARACTER-VOICE-SOURCES.txt","UNDEAD-VAMPIRE-LICENSES.txt","MISSING-SFX-LICENSES.txt","ITEM-SFX-LICENSES.txt").forEach { path->
            val file=Gdx.files.internal("audio/$path")
            if(file.exists()) {
                line(path,true)
                file.readString("UTF-8").lineSequence().forEach { text->if(text.isBlank())totalHeight+=12f else line(text) }
            }
        }
    }
    override fun show(){
        viewport.apply();camera.position.set(640f,360f,0f);camera.update();pointer.reset();drag.reset()
        scrollOffset=0f;rebuild();previousInput=Gdx.input.inputProcessor;Gdx.input.inputProcessor=input
    }
    override fun render(delta:Float) {
        pointer.update()
        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            GameAudio.play(UiSound.CLICK);game.showMainMenu();return
        }
        if(back.update(pointer.position,pointer.justPressed,pointer.justReleased))return
        if(Gdx.input.isKeyPressed(Input.Keys.DOWN))scrollBy(420f*delta)
        if(Gdx.input.isKeyPressed(Input.Keys.UP))scrollBy(-420f*delta)
        if(Gdx.input.isKeyJustPressed(Input.Keys.HOME))scrollOffset=0f
        if(Gdx.input.isKeyJustPressed(Input.Keys.END))scrollOffset=maximumScroll
        val visibleRatio=(list.height/totalHeight).coerceIn(0f,1f)
        val ratio=if(maximumScroll>0f)scrollOffset/maximumScroll else 0f
        drag.update(pointer.position,pointer.justPressed,pointer.isHeld,pointer.justReleased,scrollbar,
            UiComponentRenderer.bronzeScrollbarThumbBounds(scrollbar,visibleRatio,ratio)) { scrollOffset=maximumScroll*it }
        Gdx.gl.glClearColor(.02f,.02f,.025f,1f);Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        batch.projectionMatrix=camera.combined;batch.begin()
        UiComponentRenderer.drawStonePanel(batch,Rectangle(55f,31f,1170f,658f))
        font.data.setScale(1.35f);font.color=Color(.94f,.88f,.72f,1f);font.draw(batch,"CREDITS",110f,628f)
        font.data.setScale(.65f);font.draw(batch,"Scroll to view music, sources and licenses",110f,600f)
        batch.end()
        val scissor=Rectangle();ScissorStack.calculateScissors(camera,batch.transformMatrix,list,scissor)
        if(ScissorStack.pushScissors(scissor)) {
            batch.begin()
            rows.forEach { row->
                val y=list.y+list.height-row.top+scrollOffset
                if(y>=list.y && y-row.layout.height<=list.y+list.height) {
                    font.data.setScale(row.scale);font.draw(batch,row.layout,list.x,y)
                }
            }
            batch.end();ScissorStack.popScissors()
        }
        batch.begin();UiComponentRenderer.drawChronicleScrollbar(batch,scrollbar,visibleRatio,ratio)
        back.render(batch,font,pointer.position,pointer.isHeld);batch.end()
    }
    override fun resize(width:Int,height:Int){if(width>0&&height>0)viewport.update(width,height,true)}
    override fun hide(){if(Gdx.input.inputProcessor===input)Gdx.input.inputProcessor=previousInput}
    override fun dispose(){hide();batch.dispose();font.dispose()}
}
