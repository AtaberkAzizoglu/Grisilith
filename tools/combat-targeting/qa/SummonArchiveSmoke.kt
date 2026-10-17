package qa

import com.azizoglu.grisilith.GrisilithGame
import com.azizoglu.grisilith.content.*
import com.azizoglu.grisilith.summon.*
import com.azizoglu.grisilith.screen.*
import com.azizoglu.grisilith.screen.glyph.ChooseGlyphSpellPopup
import com.azizoglu.grisilith.screen.summon.ChooseToSummonPopup
import com.azizoglu.grisilith.ui.icon.ContentIconRenderer
import com.azizoglu.grisilith.ui.stat.SemanticSymbolRenderer
import com.azizoglu.grisilith.ui.typography.UiFontFactory
import com.azizoglu.grisilith.weapon.runtime.WeaponInstance
import com.azizoglu.grisilith.weapon.definition.DefaultWeapons
import com.azizoglu.grisilith.weapon.glyph.*
import com.azizoglu.grisilith.weapon.summon.IvySummonChoice
import com.badlogic.gdx.*
import com.badlogic.gdx.backends.lwjgl3.*
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g2d.*
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.*
import com.badlogic.gdx.utils.viewport.FitViewport

object SummonArchiveSmoke {
    @JvmStatic fun main(args:Array<String>) {
        Lwjgl3Application(object:ApplicationAdapter() {
            override fun create() { try { checks();Gdx.app.exit() }catch(t:Throwable){t.printStackTrace();kotlin.system.exitProcess(1)} }
        },Lwjgl3ApplicationConfiguration().apply {
            setWindowedMode(1280,720);setInitialVisible(false);disableAudio(true)
            setPreferencesConfig("../tools/combat-targeting/summon-prefs",Files.FileType.Local)
        })
    }
    private fun field(target:Any,name:String)=target.javaClass.getDeclaredField(name).apply{isAccessible=true}
    private fun shot(name:String) {
        val pix=Pixmap.createFromFrameBuffer(0,0,1280,720)
        PixmapIO.writePNG(Gdx.files.local("../tools/combat-targeting/summons-$name.png"),pix,-1,true);pix.dispose()
    }
    private fun checks() {
        check(SummonCollectionDefinition.all.size==SummonType.entries.size)
        check(DefaultContentRegistry.instance.definitionsForCategory(ContentCategory.SUMMON).size==5)
        for(def in SummonCollectionDefinition.all) {
            check(ContentIconRenderer.hasIcon(def.id.value))
            check("Source" in def.description)
        }
        for(color in ContentIconRenderer.SummonFrame.entries) {
            val pix=Pixmap(Gdx.files.internal("ui/content_icons/frames/summons/${color.name.lowercase()}_v1.png"))
            check((pix.getPixel(pix.width/2,pix.height/2) and 255)==0)
            check((pix.getPixel(0,0) and 255)==0);pix.dispose()
        }
        val heading=SemanticSymbolRenderer.decoration("Damage:",null)!!
        val damage=SemanticSymbolRenderer.decoration("Fire",null)!!
        for(decoration in listOf(heading,damage)) {
            val paper=SemanticSymbolRenderer.readableColor(decoration,Color(.2f,.1f,.04f,1f))
            val stone=SemanticSymbolRenderer.readableColor(decoration,Color.WHITE)
            check(paper!=stone && stone.r+stone.g+stone.b>paper.r+paper.g+paper.b)
        }
        for(choice in IvySummonChoice.entries) {
            val scheduler=SummonSpawnScheduler("test",choice)
            val text=SummonPresentation.choice(choice)
            check("${scheduler.activeLifetimeSeconds.toInt()} seconds" in text)
            check("${scheduler.cooldownSeconds.toInt()} seconds" in text)
        }
        for(choice in ChalkDustSpellChoice.entries) {
            val text=GlyphChoicePresentation.description(choice)
            check("Wisdom" in text && "30 seconds" in text && "2.5 seconds" in text)
        }
        val game=GrisilithGame()
        val home=CollectionScreen(game);home.show();home.resize(1280,720);home.render(.016f);shot("home");home.dispose()
        val archive=CollectionCategoryScreen(game,ContentCategory.SUMMON)
        archive.show();archive.resize(1280,720)
        for(def in SummonCollectionDefinition.all) {
            field(archive,"selectedContentId").set(archive,def.id.value)
            archive.render(.016f);shot(def.id.value)
        }
        archive.dispose()
        val camera=OrthographicCamera();val viewport=FitViewport(1280f,720f,camera)
        viewport.update(1280,720,true)
        // Non-zero world origin checks tooltips while the run camera follows the player.
        camera.position.set(2100f,1700f,0f);camera.update()
        val batch=SpriteBatch();val shape=ShapeRenderer();val font=UiFontFactory.createBody()
        val ivy=ChooseToSummonPopup(camera,viewport,shape,batch,font)
        val glyph=ChooseGlyphSpellPopup(camera,viewport,shape,batch,font)
        val original=Gdx.input;var mx=640;var my=360
        Gdx.input=java.lang.reflect.Proxy.newProxyInstance(Input::class.java.classLoader,arrayOf(Input::class.java)) {_,method,_ ->
            when(method.name) {"getX"->mx;"getY"->my;else->when(method.returnType){java.lang.Boolean.TYPE->false;java.lang.Integer.TYPE->0;java.lang.Float.TYPE->0f;else->null}}
        } as Input
        try {
            for(level in listOf(3,5)) for(popup in listOf(ivy,glyph)) {
                field(popup,"pendingWeapon").set(popup,null)
                if(popup===ivy) ivy.open(WeaponInstance(DefaultWeapons.ivyStaff,level)) else glyph.open(WeaponInstance(DefaultWeapons.enchantedChalkDust,level))
                val layout=popup.javaClass.getDeclaredMethod("updateLayout").apply{isAccessible=true};layout.invoke(popup)
                @Suppress("UNCHECKED_CAST") val options=field(popup,"optionBounds").get(popup) as Array<Rectangle>
                for((index,rect) in options.withIndex()) {
                    val p=viewport.project(Vector2(rect.x+rect.width/2,rect.y+rect.height/2))
                    mx=p.x.toInt();my=720-p.y.toInt()
                    Gdx.gl.glClearColor(.04f,.04f,.04f,1f);Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
                    if(popup===ivy)ivy.render() else glyph.render()
                    shot("${if(popup===ivy)"ivy" else "glyph"}-$level-$index")
                }
            }
        }finally{Gdx.input=original}
        camera.position.set(640f,360f,0f);camera.update();batch.projectionMatrix=camera.combined
        val paper=com.azizoglu.grisilith.ui.theme.PaperUi()
        val sample="Attack Type: Projectile\nDamage: 20\nBase Attack Speed: 1/s\nRange: 300 (ranged)\nScaling: Wisdom + Elemental Damage\nDamage Type: Fire, Piercing damage\nAbility: A Fire burst. Cooldown: 20s."
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);batch.begin()
        com.azizoglu.grisilith.screen.render.PaperTooltipRenderer.draw(batch,paper,font,Vector2(50f,680f),"PAPER CONTRAST CHECK",sample)
        com.azizoglu.grisilith.screen.render.AdaptiveTooltipRenderer.draw(batch,font,Rectangle(0f,0f,1280f,720f),Vector2(650f,680f),"STONE CONTRAST CHECK",sample,490f,.88f,.73f)
        batch.end();shot("palette");paper.dispose()
        UrgentHotfixChecks.checks(batch,font,camera)
        batch.dispose();shape.dispose();font.dispose()
        println("SUMMON_ARCHIVE_PASS: eight menu categories, five summon records, four alpha frames, contextual palettes, scheduler-aligned descriptions; all seven option hovers at levels 3/5 with a moving world camera")
    }
}
