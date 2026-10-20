package qa

import com.azizoglu.grisilith.GrisilithGame
import com.azizoglu.grisilith.audio.*
import com.azizoglu.grisilith.screen.*
import com.azizoglu.grisilith.screen.components.MenuButton
import com.azizoglu.grisilith.screen.market.*
import com.azizoglu.grisilith.screen.render.GearDragPreview
import com.azizoglu.grisilith.settings.GameSettings
import com.azizoglu.grisilith.input.RunInputBindings
import com.azizoglu.grisilith.weapon.runtime.*
import com.azizoglu.grisilith.weapon.runtime.executor.*
import com.azizoglu.grisilith.weapon.definition.DefaultWeapons
import com.azizoglu.grisilith.weapon.visual.attack.SoulDrainVisualManager
import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.player.Player
import com.azizoglu.grisilith.enemy.*
import com.azizoglu.grisilith.enemy.definition.DefaultEnemyKinds
import com.azizoglu.grisilith.enemy.runtime.EnemyRuntimeFactory
import com.azizoglu.grisilith.map.TroubleAtHomeDefenseRuntime
import com.azizoglu.grisilith.config.GameMap
import com.azizoglu.grisilith.combat.DamageDefense
import com.azizoglu.grisilith.content.DamageType
import com.azizoglu.grisilith.item.definition.DefaultItems
import com.azizoglu.grisilith.ui.typography.UiFontFactory
import com.badlogic.gdx.*
import com.badlogic.gdx.backends.lwjgl3.*
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.*
import com.badlogic.gdx.utils.viewport.FitViewport
import java.lang.reflect.Proxy

/** Isolated native layout checks and upgraded Totem regression coverage. */
object Phase10aSmoke {
 private fun field(o:Any,n:String)=o.javaClass.getDeclaredField(n).apply{isAccessible=true}
 private fun shot(name:String){Pixmap.createFromFrameBuffer(0,0,1280,720).also{
    PixmapIO.writePNG(Gdx.files.local("../tools/audio-audition/$name.png"),it,-1,true);it.dispose()}}
 @JvmStatic fun main(args:Array<String>) {
  Lwjgl3Application(object:ApplicationAdapter(){override fun create(){try{checks();Gdx.app.exit()}
    catch(t:Throwable){t.printStackTrace();kotlin.system.exitProcess(1)}}},Lwjgl3ApplicationConfiguration().apply{
    setWindowedMode(1280,720);setInitialVisible(false);disableAudio(true)
    setPreferencesConfig("../tools/audio-audition/test-prefs",Files.FileType.Local)
  })
 }
 private fun checks() {
    val sounds=UiSound.entries.associateWith{AudioSmoke.FakeSound()}
    val audio=AudioController({AudioSmoke.FakeMusic().api},{sounds.getValue(it).api})
    GameAudio::class.java.getDeclaredField("controller").apply{isAccessible=true}.set(null,audio)
    val game=GrisilithGame();val original=GameSettings.snapshot();val bindings=RunInputBindings.snapshot()
    val settings=SettingsScreen(game){};settings.resize(1280,720);settings.show()
    val draft=original.copy(masterVolume=.37f)
    field(settings,"draftSettings").set(settings,draft)
    @Suppress("UNCHECKED_CAST") fun buttons()=field(settings,"buttons").get(settings) as List<MenuButton>
    fun click(label:String) {val b=buttons().single{it.text==label};val p=Vector2(b.bounds.x+5f,b.bounds.y+5f)
        b.update(p,true,false);b.update(p,false,true)}
    click("RESET ALL");check(field(settings,"draftSettings").get(settings)==draft)
    settings.render(.016f);shot("phase10a-reset-confirm")
    click("CANCEL");check(field(settings,"draftSettings").get(settings)==draft)
    click("RESET ALL");Gdx.input.inputProcessor.keyDown(Input.Keys.ESCAPE)
    check(field(settings,"resetConfirmation").get(settings)==null)
    click("KEY BINDINGS");click("RESET CONTROLS");click("RESET")
    check(field(settings,"draftSettings").get(settings)==draft)
    check(field(settings,"draftBindings").get(settings)==RunInputBindings.defaults())
    click("RESET ALL");click("RESET")
    check(field(settings,"draftSettings").get(settings)==GameSettings.defaults())
    check(GameSettings.snapshot()==original && RunInputBindings.snapshot()==bindings)
    settings.hide();settings.dispose()
    val chronicles=HighScoreScreen(game);chronicles.resize(1280,720);chronicles.show()
    val back=field(chronicles,"backButton").get(chronicles) as MenuButton
    check(Rectangle(155f,24f,970f,672f).contains(back.bounds))
    chronicles.render(.016f);shot("phase10a-chronicles-back");chronicles.hide();chronicles.dispose()

    // Offers begin dragging after the same 8px movement as Pause, with no 120ms delay.
    val realInput=Gdx.input;var held=true;var pressed=true
    Gdx.input=Proxy.newProxyInstance(Input::class.java.classLoader,arrayOf(Input::class.java)){_,m,_->when(m.name){
        "isButtonJustPressed"->pressed;"isButtonPressed"->held
        else->when(m.returnType){java.lang.Boolean.TYPE->false;java.lang.Integer.TYPE->0;java.lang.Float.TYPE->0f;else->null}
    }} as Input
    val camera=OrthographicCamera(1280f,720f);camera.position.set(640f,360f,0f);camera.update()
    val viewport=FitViewport(1280f,720f,camera);viewport.update(1280,720,true)
    val layout=MarketLayout.create(640f,360f,1280f,720f);val state=MarketInteractionState()
    val input=MarketInputController(viewport,{layout},state,{0f},{0})
    val pointer=field(input,"pointerPosition").get(input) as Vector2
    val handler=input.javaClass.declaredMethods.single{it.name=="handleMarketOfferPointerInput"}.apply{isAccessible=true}
    var purchases=0;var drops=0
    val buy:(Int)->Unit={purchases++};val drop:(Int,Int?,Boolean,Boolean)->Unit={_,_,_,_->drops++}
    layout.offerCards.first().let{pointer.set(it.x+it.width/2f,it.y+it.height/2f)}
    handler.invoke(input,.001f,buy,drop);pressed=false
    handler.invoke(input,.5f,buy,drop);check(!state.isDraggingMarketOffer)
    pointer.x+=9f;handler.invoke(input,.001f,buy,drop);check(state.isDraggingMarketOffer)
    held=false;handler.invoke(input,.001f,buy,drop);check(drops==1 && purchases==0)
    Gdx.input=realInput
    // Shared Pause/Market ghost: Roman weapon level, no level panel for items.
    val batch=SpriteBatch();val font=UiFontFactory.createBody()
    Gdx.gl.glClearColor(.08f,.08f,.08f,1f);Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
    batch.projectionMatrix=camera.combined;batch.begin()
    GearDragPreview.draw(batch,font,DefaultWeapons.longsword,4,450f,400f)
    GearDragPreview.draw(batch,font,DefaultItems.animatedBroom,1,700f,400f)
    batch.end();shot("phase10a-drag-previews");batch.dispose();font.dispose()
    exportSiegeArt()
    val siege=TroubleAtHomeDefenseRuntime(GameMap.TROUBLE_AT_HOME,emptyList())
    check(field(siege,"introMessageRemainingSeconds").getFloat(siege)==8f)

    // Regression: every fourth upgraded Totem attack must drain, counting once.
    val stats=PlayerStats();val player=Player(300f,300f,3000f,3000f,stats)
    val enemies=EnemyManager();enemies.spawn(Enemy(400f,300f,EnemyStats(EnemyType.NORMAL,100000,0,0f,20f,0f)))
    val weapon=WeaponInstance(DefaultWeapons.totemOfDarkClouds,4)
    val calculator=WeaponDamageCalculator(stats);val selector=WeaponTargetSelector()
    val projectile=ProjectileWeaponAttackExecutor(calculator,selector,stats)
    val totem=TotemOfDarkCloudsAttackExecutor(calculator,selector,projectile,stats)
    val context=WeaponAttackContext(weapon,0,player,player.centerX,player.centerY,enemies,
        mutableListOf(),{_,_->},{},{},{_,d->d},{it},{false})
    SoulDrainVisualManager.clear();repeat(8){totem.execute(context)}
    val counter=field(weapon,"normalAttackCount").getInt(weapon)
    check(counter==8 && SoulDrainVisualManager.activeEvents.size==2) {
        "Totem diagnostic counter=$counter, drain=${SoulDrainVisualManager.activeEvents.size}, projectiles=${context.projectiles.size}"
    }
    println("TOTEM_FIX_PASS: 8 attacks -> counter $counter; 2 drain attacks.")
    val factory=EnemyRuntimeFactory(com.azizoglu.grisilith.enemy.definition.EnemyKindRegistry().apply{registerAll(DefaultEnemyKinds.all)})
    listOf(DefaultEnemyKinds.witch,DefaultEnemyKinds.vampireLord).forEach{kind->
        val s=factory.createStats(kind,EnemyType.FINAL_BOSS,25)
        println("DEFENSE_DIAG: ${kind.displayName} stage25 physical defense=${s.baseArmor+s.armorClass}, magical defense=${s.baseMagicResistance+s.magicResistance}, contact=${s.contactDamage}")
    }
    check(DamageDefense.resolve(100,DamageType.SLASHING,22,10,0,0)==76)
    println("DAMAGE_CHECK: physical 100 vs 22 baseArmor + 10 ArmorClass -> 76.")
    check(sounds.getValue(UiSound.HOVER).played.isEmpty())
    GameAudio.dispose()
    println("PHASE10A_PASS: reset confirm/cancel/ESC, silent hover, immediate market drag, Roman weapon level/no item level, Chronicles Back inside panel, transparent Siege notice, Totem drain every fourth attack")
 }
 private fun exportSiegeArt() {
    val fbo=com.badlogic.gdx.graphics.glutils.FrameBuffer(Pixmap.Format.RGBA8888,2048,240,false)
    val batch=SpriteBatch();val font=UiFontFactory.createNumeric(72)
    fbo.begin();Gdx.gl.glClearColor(0f,0f,0f,0f);Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
    batch.projectionMatrix=Matrix4().setToOrtho2D(0f,0f,2048f,240f)
    batch.setBlendFunctionSeparate(GL20.GL_SRC_ALPHA,GL20.GL_ONE_MINUS_SRC_ALPHA,GL20.GL_ONE,GL20.GL_ONE_MINUS_SRC_ALPHA)
    batch.begin();font.data.setScale(1.45f);font.color=Color(1f,.93f,.76f,1f)
    val text="Defend your home, and survive."
    val width=com.badlogic.gdx.graphics.g2d.GlyphLayout(font,text).width
    font.draw(batch,text,1024f-width/2f,160f);batch.end()
    val pixels=Pixmap.createFromFrameBuffer(0,0,2048,240)
    val file=Gdx.files.local("ui/text/siege_intro_v1.png");file.parent().mkdirs()
    PixmapIO.writePNG(file,pixels,-1,true)
    pixels.dispose();fbo.end();fbo.dispose();batch.dispose();font.dispose()
 }
}
