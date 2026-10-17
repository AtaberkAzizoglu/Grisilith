package qa

import com.azizoglu.grisilith.GrisilithGame
import com.azizoglu.grisilith.online.*
import com.azizoglu.grisilith.screen.MainMenuScreen
import com.azizoglu.grisilith.screen.SettingsScreen
import com.badlogic.gdx.*
import com.badlogic.gdx.backends.lwjgl3.*
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.PixmapIO
import com.badlogic.gdx.utils.*

/** Fake network only. Tests first-entry gating and renders the actual screens. */
object OnlineIdentityUiSmoke {
    private fun field(target:Any,name:String)=target.javaClass.getDeclaredField(name).apply {isAccessible=true}
    private class Transport:OnlineTransport {
        val requests=mutableListOf<Pair<String,(Result<JsonValue>)->Unit>>()
        val lastBodies=mutableMapOf<String,JsonValue>()
        override fun request(path:String,body:JsonValue,accessToken:String?,callback:(Result<JsonValue>)->Unit) {
            requests+=path to callback; lastBodies[path]=body
        }
        fun reply(suffix:String,json:String) {val i=requests.indexOfFirst {it.first.endsWith(suffix)};check(i>=0){suffix};requests.removeAt(i).second(Result.success(JsonReader().parse(json)))}
        fun fail(suffix:String,code:Int=0,reason:String?=null) {val i=requests.indexOfFirst{it.first.endsWith(suffix)};check(i>=0);requests.removeAt(i).second(Result.failure(OnlineRequestFailure(code,reason)))}
    }
    private const val ID="00000000-0000-4000-8000-000000000001"
    @JvmStatic fun main(args:Array<String>) {
        Lwjgl3Application(object:ApplicationAdapter(){
            override fun create(){try{checks();Gdx.app.exit()}catch(t:Throwable){t.printStackTrace();kotlin.system.exitProcess(1)}}
        },Lwjgl3ApplicationConfiguration().apply {
            setWindowedMode(1280,720);setInitialVisible(false);disableAudio(true)
            setPreferencesConfig("../tools/online-backend/test-prefs",Files.FileType.Local)
        })
    }
    private fun screenshot(name:String) {
        val bytes=ScreenUtils.getFrameBufferPixels(0,0,1280,720,true)
        val p=Pixmap(1280,720,Pixmap.Format.RGBA8888);p.pixels.put(bytes);p.pixels.flip()
        PixmapIO.writePNG(Gdx.files.local("../tools/online-backend/phase8c-$name.png"),p);p.dispose()
    }
    private fun checks() {
        val policy=PlayerNamePolicy.packaged
        for((name,blocked) in listOf("F U C K" to true,"FU CK" to true,"F.U.C.K" to true,"FUC K" to false,
            "KAS SESİ" to false,"PEACOCK" to false,"PEACOCK FUCK" to true,"PEACOCKFUCK" to true,
            "OCEAN" to false,"DOCUMENT" to false,"STAMINA" to false,"NAZİK" to false,"SİK" to true,
            "SIK" to true,"SIQ" to true,"S1K" to true,"5H1T" to false,"ŞHIT" to false,"FUCK FUCK" to true,
            "THE HO" to true,"HORSE" to false,"HO" to true,"K A S SESİ" to false,"HELLO CINDER" to false,
            "DARKPHOENIX" to false,"SHADOWHUNTER" to false,"COCKTAILCOCK" to true)) check(policy.isBlocked(name)==blocked){name}
        check(PlayerNamePolicy.uppercase("ice İCE ıçğöşü")=="ICE İCE IÇĞÖŞÜ")
        check(policy.error("şövalye 12")==null && policy.error("---")!=null && policy.error("A😀B")!=null)
        val game=GrisilithGame();val t=Transport();var now=0L
        val backend=OnlineBackend(OnlineConfig("https://test.supabase.co","sb_publishable_test"),t,
            object:OnlineSessionStore{override fun load()=OnlineSession("a","r",9000,ID);override fun save(session:OnlineSession)=Unit}, {now},{1000L})
        field(game,"online").set(game,backend)
        val main=MainMenuScreen(game);main.resize(1280,720);main.show();main.render(.016f)
        val dialog=field(main,"nameDialog").get(main) as OnlineNameDialog
        check(!dialog.open);screenshot("offline")
        fun ready(confirmed:Boolean=false) {
            backend.bootstrap();t.reply("api_status","""{"api_version":1,"score_version":1,"game_version":"0.3"}""")
            t.reply("profile","""{"player_id":"$ID","display_name":"$ID","name_confirmed":$confirmed}""")
        }
        ready();main.render(.016f);check(dialog.open);screenshot("first-name")
        val confirm=field(dialog,"save").get(dialog) as com.azizoglu.grisilith.screen.components.MenuButton
        check(confirm.text=="CONFIRM")
        check(dialog.javaClass.declaredFields.none {it.name=="reset"})
        "şövalye".forEach {Gdx.input.inputProcessor.keyTyped(it)}
        check(field(dialog,"input").get(dialog)=="ŞÖVALYE")
        Gdx.input.inputProcessor.keyDown(Input.Keys.ENTER)
        t.fail("profile",400,"display_name_taken");main.render(.016f);check(dialog.open);screenshot("name-taken")
        check(field(dialog,"error").get(dialog)=="This name is already taken.")
        Gdx.input.inputProcessor.keyDown(Input.Keys.ENTER)
        t.reply("profile","""{"display_name":"ŞÖVALYE","name_confirmed":true}""")
        check(!dialog.open && backend.nameConfirmed)
        val top=(0..9).joinToString(",") { i ->
            """{"rank":${if(i==2)2 else i+1},"display_name":"${if(i%2==0) ID else "ŞÖVALYE İCE"}","score":${12345-i},"duration_seconds":${100+i},"is_self":${i==1},"character_id":"CHR_KOBA","map_id":"ROAD_TO_HOME"}"""
        }
        t.reply("leaderboard_all","""{"top":[$top],"footer_kind":"next","footer":{"rank":11,"display_name":"Lİ NLOS","score":40,"character_id":"CHR_LINLOS_DUSTMAN","map_id":"FOLLOWING_THE_DUNGEON_DELVER"}}""")
        main.render(.016f);screenshot("leaderboard")
        main.hide();main.show();main.render(.016f);check(!dialog.open)
        // A failed onboarding request must retry on next main-menu visit, not each frame.
        field(backend,"nameConfirmed").setBoolean(backend,false)
        main.hide();main.show();main.render(.016f);check(dialog.open)
        Gdx.input.inputProcessor.keyDown(Input.Keys.ENTER);t.fail("profile");main.render(.016f);check(!dialog.open)
        main.render(.016f);check(!dialog.open)
        now=31_000;ready();main.render(.016f);check(!dialog.open)
        main.hide();main.show();main.render(.016f);check(dialog.open)
        // Direct, empty CONFIRM uses the Unique ID without an extra reset button.
        val center=com.badlogic.gdx.math.Vector2(confirm.bounds.x+confirm.bounds.width/2f,confirm.bounds.y+confirm.bounds.height/2f)
        confirm.update(center,true,false);confirm.update(center,false,true)
        check(t.lastBodies.entries.last{it.key.endsWith("profile")}.value.getString("p_display_name")=="")
        t.reply("profile","""{"display_name":"$ID","name_confirmed":true}""")
        check(!dialog.open && backend.displayName==ID)
        main.hide()
        val settings=SettingsScreen(game){};settings.resize(1280,720);settings.show()
        val page=field(settings,"page");page.set(settings,page.type.enumConstants.first {it.toString()=="ACCOUNT"})
        settings.javaClass.getDeclaredMethod("rebuildButtons").apply{isAccessible=true}.invoke(settings)
        settings.render(.016f);screenshot("account")
        val rename=field(settings,"nameDialog").get(settings) as OnlineNameDialog
        rename.show(false)
        "NAME".forEach {Gdx.input.inputProcessor.keyTyped(it)}
        repeat(4){Gdx.input.inputProcessor.keyDown(Input.Keys.BACKSPACE)}
        Gdx.input.inputProcessor.keyDown(Input.Keys.ENTER)
        check(t.lastBodies.entries.last{it.key.endsWith("profile")}.value.getString("p_display_name")=="")
        t.reply("profile","""{"display_name":"$ID","name_confirmed":true}""")
        check(!rename.open && backend.displayName==ID)
        settings.dispose();main.dispose();backend.close()
        println("ONLINE_IDENTITY_UI_SMOKE_PASS")
    }
}
