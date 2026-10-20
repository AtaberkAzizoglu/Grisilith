package qa

import com.azizoglu.grisilith.GrisilithGame
import com.azizoglu.grisilith.audio.*
import com.azizoglu.grisilith.online.*
import com.azizoglu.grisilith.screen.*
import com.azizoglu.grisilith.screen.components.MenuPointer
import com.azizoglu.grisilith.settings.GameSettings
import com.badlogic.gdx.*
import com.badlogic.gdx.backends.lwjgl3.*
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.JsonReader
import com.badlogic.gdx.utils.JsonValue
import com.badlogic.gdx.utils.viewport.FitViewport
import java.lang.reflect.Proxy

/** Native rendering plus isolated display and online interaction tests. No real settings/network writes. */
object Phase10bSmoke {
    private fun field(o:Any,n:String)=o.javaClass.getDeclaredField(n).apply{isAccessible=true}
    private fun shot(name:String)=Pixmap.createFromFrameBuffer(0,0,1280,720).let {
        PixmapIO.writePNG(Gdx.files.local("../tools/audio-audition/$name.png"),it,-1,true);it.dispose()
    }
    private class Mode(w:Int,h:Int):Graphics.DisplayMode(w,h,60,32)
    @JvmStatic fun main(args:Array<String>) {
        Lwjgl3Application(object:ApplicationAdapter(){override fun create(){try{checks();Gdx.app.exit()}
            catch(t:Throwable){t.printStackTrace();kotlin.system.exitProcess(1)}}},Lwjgl3ApplicationConfiguration().apply{
            setWindowedMode(1280,720);setInitialVisible(false);disableAudio(true)
            setPreferencesConfig("../tools/audio-audition/test-prefs-phase10b",Files.FileType.Local)
        })
    }
    private fun checks() {
        val audio=AudioController({AudioSmoke.FakeMusic().api},{AudioSmoke.FakeSound().api})
        GameAudio::class.java.getDeclaredField("controller").apply{isAccessible=true}.set(null,audio)
        val graphics=Gdx.graphics
        var currentMode:Graphics.DisplayMode=Mode(1920,1080)
        var windowSize=1280 to 720
        Gdx.graphics=Proxy.newProxyInstance(Graphics::class.java.classLoader,arrayOf(Graphics::class.java)){_,m,a->when(m.name){
            "getDisplayMode"->currentMode
            "getDisplayModes"->arrayOf(Mode(1280,720),Mode(1600,900),Mode(1920,1080))
            "setFullscreenMode"->{currentMode=a!![0] as Graphics.DisplayMode;true}
            "setWindowedMode"->{windowSize=(a!![0] as Int) to (a[1] as Int);true}
            "setVSync"->null
            else->m.invoke(graphics,*(a?:emptyArray()))
        }} as Graphics
        val original=GameSettings.snapshot()
        check(original.fullscreen) {"First launch must default to fullscreen"}
        GameSettings.restore(original,persist=false)
        val game=GrisilithGame()
        val settings=SettingsScreen(game){};settings.resize(1280,720);settings.show()
        settings.render(.016f);shot("phase10b-settings-display")
        val preview=settings.javaClass.getDeclaredMethod("previewDisplay",Boolean::class.javaPrimitiveType,Int::class.javaPrimitiveType,Int::class.javaPrimitiveType).apply{isAccessible=true}
        val finish=settings.javaClass.getDeclaredMethod("finishDisplayPreview",Boolean::class.javaPrimitiveType).apply{isAccessible=true}
        preview.invoke(settings,false,1600,900)
        check(windowSize==1600 to 900)
        check(GameSettings.resolutionWidth==1600 && !GameSettings.fullscreen)
        settings.render(.016f);shot("phase10b-display-confirm")
        finish.invoke(settings,false)
        check(GameSettings.snapshot()==original)
        preview.invoke(settings,false,1600,900);settings.render(16f)
        check(GameSettings.snapshot()==original && field(settings,"displayBefore").get(settings)==null)
        preview.invoke(settings,false,1600,900);finish.invoke(settings,true)
        GameSettings.load()
        check(!GameSettings.fullscreen && GameSettings.resolutionWidth==1600 && windowSize==1600 to 900)
        settings.hide();settings.dispose()
        GameSettings.restore(original)
        Gdx.graphics=graphics

        val play=PlayMenuScreen(game);play.resize(1280,720);play.show();play.render(.016f)
        shot("phase10b-difficulty");play.hide();play.dispose()

        val pending=mutableListOf<Pair<String,(Result<JsonValue>)->Unit>>()
        val transport=object:OnlineTransport {
            override fun request(path:String,body:JsonValue,accessToken:String?,callback:(Result<JsonValue>)->Unit) {pending.add(path to callback)}
        }
        val store=object:OnlineSessionStore {
            override fun load()=OnlineSession("a","r",Long.MAX_VALUE,"f10a0000-0000-4000-8000-000000000002")
            override fun save(session:OnlineSession){}
        }
        val online=OnlineBackend(OnlineConfig("https://test.supabase.co","sb_publishable_test",true),transport,store)
        fun reply(suffix:String,json:String) {
            val at=pending.indexOfFirst{it.first.endsWith(suffix)};check(at>=0)
            pending.removeAt(at).second(Result.success(JsonReader().parse(json)))
        }
        online.bootstrap();reply("grisilith_api_status","""{"api_version":1,"score_version":1,"game_version":"0.3"}""")
        reply("grisilith_profile","""{"player_id":"f10a0000-0000-4000-8000-000000000002","name_confirmed":true}""")
        check(online.status==OnlineBackend.Status.READY)
        val rows=(1..10).joinToString(",") {i->"""{"run_id":"f10a0000-1111-4000-8000-${i.toString().padStart(12,'0')}","rank":$i,"display_name":"F10A0000-0000-4000-8000-000000000001","score":${1000000-i},"character_id":"CHR_KOBA","map_id":"ROAD_TO_HOME","praise_count":12,"can_praise":${i!=2},"is_self":${i==2}}"""}
        val data=JsonReader().parse("""{"top":[$rows],"footer_kind":"next"}""")
        val ui=OnlineUi();val batch=SpriteBatch();val camera=OrthographicCamera()
        val viewport=FitViewport(1280f,720f,camera);viewport.update(1280,720,true)
        val pointer=MenuPointer(viewport)
        val input=Gdx.input;var held=false;var pressed=false;var mx=0;var my=0
        Gdx.input=Proxy.newProxyInstance(Input::class.java.classLoader,arrayOf(Input::class.java)){_,m,a->when(m.name){
            "getX"->mx;"getY"->my;"isButtonPressed"->held;"isButtonJustPressed"->pressed
            else->m.invoke(input,*(a?:emptyArray()))
        }} as Input
        var refreshed=0
        fun click(x:Int,y:Int) {
            mx=x;my=720-y;held=true;pressed=true;pointer.update();ui.updateLeaderboard(pointer,data,online){refreshed++}
            held=false;pressed=false;pointer.update();ui.updateLeaderboard(pointer,data,online){refreshed++}
        }
        fun render(name:String) {
            Gdx.gl.glClearColor(.07f,.065f,.06f,1f);Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
            batch.projectionMatrix=camera.combined;batch.begin();ui.leaderboard(batch,data,online,false,false);batch.end();shot(name)
        }
        render("phase10b-leaderboard")
        click(426,528);check(pending.size==1)
        click(426,528);check(pending.size==1) // In-flight clicks are blocked.
        reply("grisilith_praise","""{"accepted":true,"praise_count":13}""")
        check(refreshed==1 && !data.get("top").get(0).getBoolean("can_praise"))
        click(426,528);check(pending.isEmpty())
        click(426,492);check(pending.isEmpty()) // Own run is disabled.
        render("phase10b-praised")
        click(54,627);check(field(ui,"collapsed").getBoolean(ui));render("phase10b-board-collapsed")
        click(54,627);check(!field(ui,"collapsed").getBoolean(ui))
        Gdx.input=input;ui.dispose();online.close()
        val numbers=com.azizoglu.grisilith.combat.damage.DamageNumberManager()
        val collect=numbers.javaClass.getDeclaredMethod("collectEvents",List::class.java,Boolean::class.javaPrimitiveType).apply{isAccessible=true}
        collect.invoke(numbers,listOf(com.azizoglu.grisilith.combat.damage.DamageNumberEvent(12,400f,400f,true)),true)
        collect.invoke(numbers,listOf(com.azizoglu.grisilith.combat.damage.DamageNumberEvent(12,500f,400f,true)),false)
        val colors=mutableListOf<Color>()
        val damageFont=object:com.badlogic.gdx.graphics.g2d.BitmapFont(){
            override fun draw(b:com.badlogic.gdx.graphics.g2d.Batch,s:CharSequence,x:Float,y:Float,w:Float,align:Int,wrap:Boolean):com.badlogic.gdx.graphics.g2d.GlyphLayout {
                colors.add(Color(color));return super.draw(b,s,x,y,w,align,wrap)
            }
        }
        numbers.render(batch,damageFont,camera.combined,true)
        check(colors.size==2 && colors[0].r==1f && colors[0].g<.2f && colors[1].g>.8f)
        damageFont.dispose();batch.dispose();GameAudio.dispose()
        println("PHASE10B_PASS: fullscreen default, resolution keep/revert/timeout/persistence, readable difficulty, top10 praise, pending/own/repeat locks, collapse/expand, player red damage/enemy critical unchanged")
    }
}
