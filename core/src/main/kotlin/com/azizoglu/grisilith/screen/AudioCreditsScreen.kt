package com.azizoglu.grisilith.screen

import com.azizoglu.grisilith.GrisilithGame
import com.azizoglu.grisilith.screen.components.MenuButton
import com.azizoglu.grisilith.screen.components.MenuPointer
import com.azizoglu.grisilith.ui.theme.UiComponentRenderer
import com.azizoglu.grisilith.ui.typography.UiFontFactory
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.FitViewport

/** Required music attribution is readable in the shipped game, not just the source tree. */
class AudioCreditsScreen(private val game:GrisilithGame):ScreenAdapter() {
    private val camera=OrthographicCamera()
    private val viewport=FitViewport(1280f,720f,camera)
    private val pointer=MenuPointer(viewport)
    private val batch=SpriteBatch()
    private val font=UiFontFactory.createBody(24)
    private val back=MenuButton("BACK",90f,60f,220f,58f){game.showMainMenu()}
    private var page=0
    private val next=MenuButton("NEXT PAGE",920f,60f,260f,58f){page=(page+1)%2}
    override fun show(){viewport.apply();camera.position.set(640f,360f,0f);camera.update();pointer.reset()}
    override fun render(delta:Float) {
        pointer.update()
        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            com.azizoglu.grisilith.audio.GameAudio.play(com.azizoglu.grisilith.audio.UiSound.CLICK)
            game.showMainMenu();return
        }
        if(back.update(pointer.position,pointer.justPressed,pointer.justReleased))return
        next.update(pointer.position,pointer.justPressed,pointer.justReleased)
        Gdx.gl.glClearColor(.02f,.02f,.025f,1f);Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        batch.projectionMatrix=camera.combined;batch.begin()
        UiComponentRenderer.drawStonePanel(batch,Rectangle(55f,31f,1170f,658f))
        fun line(text:String,y:Float,scale:Float=1f,color:Color=Color(.94f,.88f,.72f,1f)) {
            font.data.setScale(scale);font.color=color;font.draw(batch,text,110f,y,1060f,Align.left,false)
        }
        line("AUDIO CREDITS",628f,1.35f)
        if(page==0) {
            line("MUSIC — Kevin MacLeod (incompetech.com)",574f,1.05f)
            com.azizoglu.grisilith.audio.MusicTrack.entries.forEachIndexed { index, track ->
                val use=when(track.selection) {
                    "MENU" -> "Menu"
                    "MK2" -> "Market"
                    else -> track.name.lowercase().replace('_',' ')
                }
                line("\"${track.title}\" — $use",530f-index*27f,.82f)
            }
            line("Licensed under Creative Commons: By Attribution 4.0 License",208f,.85f)
            line("https://creativecommons.org/licenses/by/4.0/",177f,.8f)
            line("Recordings unchanged. Source URLs: audio/RUN-MUSIC-LICENSES.txt / CREDITS.txt",146f,.67f)
        } else {
        line("SOUND EFFECTS",410f,1.05f)
        line("Kenney · artisticdude · Vehicle / Jan Schupke · Jordan Irwin — CC0",370f,.8f)
        line("Ben Jaszczak & Brian Nelson · spurioustransients · HaelDB · AntumDeluge — CC0",347f,.68f)
        line("Almitory · remaxim · SketchMan3 · Thimras · rubberduck · qubodup · Augmentality — CC0",324f,.67f)
        line("\"Whipping sound by Tuomo Untinen\" (Reemax) — CC BY 4.0",301f,.8f)
        line("opengameart.org/content/whip-sound · creativecommons.org/licenses/by/4.0/",278f,.67f)
        line("\"Fire & Evil Spell\" — artisticdude — CC BY 3.0",255f,.8f)
        line("opengameart.org/content/fire-evil-spell · creativecommons.org/licenses/by/3.0/",232f,.67f)
        line("spookymodem · Jerimee · Michel Baradari — CC BY 3.0; Secretlondon · pal.zoltan.illes — CC BY-SA 3.0",209f,.59f)
        line("pla1554alaska / qubodup · congusbongus · Vinrax · IMadeIt — CC BY 3.0",186f,.65f)
        line("Mimic lick: Manuel Graf / Freesound 176798 — CC BY 4.0; actor voices: user-provided recordings",163f,.59f)
        line("Sources & edits: audio/CREDITS.txt / CHARACTER-VOICE-SOURCES.txt / UNDEAD-VAMPIRE-LICENSES.txt",140f,.55f)
        }
        back.render(batch,font,pointer.position,pointer.isHeld)
        next.render(batch,font,pointer.position,pointer.isHeld)
        batch.end()
    }
    override fun resize(width:Int,height:Int){if(width>0&&height>0)viewport.update(width,height,true)}
    override fun dispose(){batch.dispose();font.dispose()}
}
