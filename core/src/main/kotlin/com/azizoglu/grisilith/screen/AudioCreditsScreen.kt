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
    override fun show(){viewport.apply();camera.position.set(640f,360f,0f);camera.update();pointer.reset()}
    override fun render(delta:Float) {
        pointer.update()
        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            com.azizoglu.grisilith.audio.GameAudio.play(com.azizoglu.grisilith.audio.UiSound.CLICK)
            game.showMainMenu();return
        }
        if(back.update(pointer.position,pointer.justPressed,pointer.justReleased))return
        Gdx.gl.glClearColor(.02f,.02f,.025f,1f);Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        batch.projectionMatrix=camera.combined;batch.begin()
        UiComponentRenderer.drawStonePanel(batch,Rectangle(55f,31f,1170f,658f))
        fun line(text:String,y:Float,scale:Float=1f,color:Color=Color(.94f,.88f,.72f,1f)) {
            font.data.setScale(scale);font.color=color;font.draw(batch,text,110f,y,1060f,Align.left,false)
        }
        line("AUDIO CREDITS",628f,1.35f)
        line("MENU MUSIC",564f,1.05f)
        line("\"Gregorian Chant\" — Kevin MacLeod (incompetech.com)",522f)
        line("Licensed under Creative Commons: By Attribution 4.0 License",484f,.9f)
        line("https://creativecommons.org/licenses/by/4.0/",450f,.85f)
        line("SOUND EFFECTS",391f,1.05f)
        line("Kenney · artisticdude · Vehicle / Jan Schupke · Jordan Irwin — CC0",355f,.8f)
        line("Ben Jaszczak & Brian Nelson — CC0",330f,.8f)
        line("Almitory · remaxim · SketchMan3 · Thimras · rubberduck · qubodup · Augmentality — CC0",305f,.67f)
        line("\"Whipping sound by Tuomo Untinen\" (Reemax) — CC BY 4.0",280f,.8f)
        line("opengameart.org/content/whip-sound · creativecommons.org/licenses/by/4.0/",255f,.67f)
        line("\"Fire & Evil Spell\" — artisticdude — CC BY 3.0",230f,.8f)
        line("opengameart.org/content/fire-evil-spell · creativecommons.org/licenses/by/3.0/",205f,.67f)
        line("spookymodem · Jerimee · Michel Baradari — CC BY 3.0; Secretlondon · pal.zoltan.illes — CC BY-SA 3.0",180f,.59f)
        line("Sources, licenses & edits: audio/CREDITS.txt and audio/FOCUS-ABILITY-LICENSES.txt",155f,.65f)
        back.render(batch,font,pointer.position,pointer.isHeld)
        batch.end()
    }
    override fun resize(width:Int,height:Int){if(width>0&&height>0)viewport.update(width,height,true)}
    override fun dispose(){batch.dispose();font.dispose()}
}
