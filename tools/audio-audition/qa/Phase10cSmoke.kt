package qa

import com.azizoglu.grisilith.GrisilithGame
import com.azizoglu.grisilith.audio.*
import com.azizoglu.grisilith.armor.definition.*
import com.azizoglu.grisilith.character.DefaultCharacters
import com.azizoglu.grisilith.collection.CollectionEntry
import com.azizoglu.grisilith.collection.detail.DefaultCollectionDetails
import com.azizoglu.grisilith.combat.affinity.DamageAffinity
import com.azizoglu.grisilith.config.*
import com.azizoglu.grisilith.content.*
import com.azizoglu.grisilith.enemy.definition.*
import com.azizoglu.grisilith.enemy.runtime.EnemyKindStatScaler
import com.azizoglu.grisilith.online.OnlineUi
import com.azizoglu.grisilith.screen.*
import com.azizoglu.grisilith.screen.components.MenuButton
import com.azizoglu.grisilith.screen.render.*
import com.azizoglu.grisilith.ui.theme.*
import com.azizoglu.grisilith.ui.icon.ContentIconRenderer
import com.azizoglu.grisilith.ui.stat.SemanticSymbolRenderer
import com.azizoglu.grisilith.ui.typography.UiFontFactory
import com.azizoglu.grisilith.weapon.definition.DefaultWeapons
import com.azizoglu.grisilith.weapon.runtime.WeaponInstance
import com.badlogic.gdx.*
import com.badlogic.gdx.backends.lwjgl3.*
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.*
import kotlin.math.abs

object Phase10cSmoke {
    private fun field(o:Any,n:String)=o.javaClass.getDeclaredField(n).apply{isAccessible=true}
    private fun shot(name:String)=Pixmap.createFromFrameBuffer(0,0,1280,720).let {
        PixmapIO.writePNG(Gdx.files.local("../tools/audio-audition/$name.png"),it,-1,true);it.dispose()
    }
    @JvmStatic fun main(args:Array<String>) {
        Lwjgl3Application(object:ApplicationAdapter(){override fun create(){try{checks();Gdx.app.exit()}
            catch(t:Throwable){t.printStackTrace();kotlin.system.exitProcess(1)}}},Lwjgl3ApplicationConfiguration().apply{
            setWindowedMode(1280,720);setInitialVisible(false);disableAudio(true)
            setPreferencesConfig("../tools/audio-audition/test-prefs-phase10c",Files.FileType.Local)
        })
    }
    private fun checks() {
        val loaded=mutableListOf<Pair<MusicTrack,AudioSmoke.FakeMusic>>()
        val audio=AudioController({AudioSmoke.FakeMusic().api},{AudioSmoke.FakeSound().api},
            loadRunMusic={track->AudioSmoke.FakeMusic().also {loaded+=track to it}.api})
        GameAudio::class.java.getDeclaredField("controller").apply{isAccessible=true}.set(null,audio)
        Phase10dDefenseChecks.run()
        fun affinity(kind:EnemyKindDefinition,type:DamageType,expected:DamageAffinity) {
            check(EnemyKindStatScaler.resolve(kind,kind.allowedTypes.first(),1).damageAffinities.affinityFor(type)==expected)
        }
        affinity(DefaultEnemyKinds.grayWolf,DamageType.NECROTIC,DamageAffinity.VULNERABLE)
        affinity(DefaultEnemyKinds.baldEagle,DamageType.NECROTIC,DamageAffinity.VULNERABLE)
        affinity(DefaultEnemyKinds.skeletonWarrior,DamageType.BLUDGEONING,DamageAffinity.VULNERABLE)
        affinity(DefaultEnemyKinds.skeletonArcher,DamageType.BLUDGEONING,DamageAffinity.VULNERABLE)
        affinity(DefaultEnemyKinds.boar,DamageType.FIRE,DamageAffinity.RESISTANT)
        affinity(DefaultEnemyKinds.crocodile,DamageType.FIRE,DamageAffinity.RESISTANT)
        affinity(DefaultEnemyKinds.slime,DamageType.ACID,DamageAffinity.IMMUNE)
        affinity(DefaultEnemyKinds.slime,DamageType.WATER,DamageAffinity.RESISTANT)
        affinity(DefaultEnemyKinds.slime,DamageType.NECROTIC,DamageAffinity.IMMUNE)
        DefaultArmors.all.forEach {armor->
            val copy=ContentPresentation.hover(armor)
            check(copy.startsWith("Base Health: ${armor.baseHealth}\nBase Armor: ${armor.baseArmor}\nBase Spell Protection: ${armor.baseMagicResistance}"))
            check(!copy.contains("Base Magic Resistance"))
            check(!copy.contains(armor.description))
            check(DefaultCollectionDetails.all.single{it.contentId==armor.id}.rows.take(3).map {it.label}==listOf("Base Health","Base Armor","Base Spell Protection"))
        }
        val decoration=SemanticSymbolRenderer.decoration("Base Spell Protection:",null)!!
        check(decoration.kind==SemanticSymbolRenderer.Kind.HEADING && decoration.icon<0)
        val src=DefaultWeapons.longsword
        check(GearDragPreview.isMergeCandidate(src,3,WeaponInstance(src,3)))
        check(GearDragPreview.isMergeCandidate(src,3,WeaponInstance(src,2)))
        check(!GearDragPreview.isMergeCandidate(src,3,WeaponInstance(src,4)))
        check(!GearDragPreview.isMergeCandidate(src,5,WeaponInstance(src,4)))
        check(!GearDragPreview.isMergeCandidate(src,3,WeaponInstance(DefaultWeapons.lance,1)))

        val game=GrisilithGame();val settings=SettingsScreen(game){}
        settings.resize(1280,720);settings.show()
        @Suppress("UNCHECKED_CAST") fun buttons()=field(settings,"buttons").get(settings) as List<MenuButton>
        fun click(b:MenuButton) {val p=Vector2(b.bounds.x+10f,b.bounds.y+10f);b.update(p,true,false);b.update(p,false,true)}
        val control=buttons().single{it.text.endsWith("  v")}
        check(control.bounds.height==56f);click(control)
        check(field(settings,"resolutionOpen").getBoolean(settings))
        check(buttons().size>=2)
        settings.render(.016f);shot("phase10c-resolution-dropdown")
        Gdx.input.inputProcessor.keyDown(Input.Keys.ESCAPE)
        check(!field(settings,"resolutionOpen").getBoolean(settings))
        settings.hide();settings.dispose()
        val play=PlayMenuScreen(game);play.resize(1280,720);play.show();play.render(.016f)
        GameDifficulty.entries.forEach {difficulty->
            field(play,"selectedDifficulty").set(play,difficulty)
            @Suppress("UNCHECKED_CAST")
            val bounds=field(play,"difficultyBounds").get(play) as Map<GameDifficulty,Rectangle>
            val original=bounds.getValue(difficulty)
            val visualMethod=play.javaClass.getDeclaredMethod("difficultyVisualBounds",GameDifficulty::class.java,Rectangle::class.java).apply{isAccessible=true}
            val visual=visualMethod.invoke(play,difficulty,original) as Rectangle
            check(abs(visual.width/original.width-1.15f)<.001f)
            check(abs(visual.x+visual.width/2-original.x-original.width/2)<.001f)
            check(abs(visual.y+visual.height/2-original.y-original.height/2)<.001f)
            check(visual.x>=992f && visual.x+visual.width<=1267f)
            val selectedArt=Pixmap(Gdx.files.internal("ui/buttons/difficulty_selected_${difficulty.name.lowercase()}_v1.png"))
            check((selectedArt.getPixel(0,0) and 255)==0)
            for(x in 3..7) for(y in 3..6) {
                // Generated exports retain a few alpha levels of antialiasing even in solid areas.
                check((selectedArt.getPixel(selectedArt.width*x/10,selectedArt.height*y/10) and 255)>=250)
            }
            selectedArt.dispose()
            bounds.filterKeys{it!=difficulty}.forEach {(other,rect)->
                check((visualMethod.invoke(play,other,rect) as Rectangle)==rect)
            }
            play.render(.016f);shot("phase10d-difficulty-${difficulty.name.lowercase()}")
        }
        play.hide();play.dispose()

        val prefs=Gdx.app.getPreferences("grisilith_settings")
        prefs.remove("leaderboard_collapsed");prefs.flush()
        var board=OnlineUi();check(!field(board,"collapsed").getBoolean(board));board.dispose()
        prefs.putBoolean("leaderboard_collapsed",true).flush()
        board=OnlineUi();check(field(board,"collapsed").getBoolean(board));board.dispose()
        prefs.putBoolean("leaderboard_collapsed",false).flush()

        val batch=SpriteBatch();val font=UiFontFactory.createBody()
        val camera=OrthographicCamera(1280f,720f);camera.position.set(640f,360f,0f);camera.update()
        batch.projectionMatrix=camera.combined
        Gdx.gl.glClearColor(.05f,.05f,.05f,1f);Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        batch.begin()
        val archive=CollectionCategoryRenderer()
        val armor=DefaultArmors.arcaneSilkRobe
        archive.renderDetails(batch,font,CollectionEntry(armor,true,true),Rectangle(30f,35f,380f,640f))
        val paper=PaperUi()
        PaperTooltipRenderer.draw(batch,paper,font,Vector2(415f,660f),armor.displayName,ArmorPresentation.hover(armor))
        val slot=Rectangle(1050f,340f,80f,80f)
        ContentIconRenderer.draw(batch,src,slot);UiComponentRenderer.drawMergeHint(batch,slot)
        batch.end();shot("phase10c-armor-and-merge")
        paper.dispose();archive.dispose();font.dispose();batch.dispose()

        // Exercise the real FirstScreen music decision, before any summary reveal.
        val run=FirstScreen(game,GameDifficulty.HARD,GameMap.ROAD_TO_HOME,DefaultCharacters.koba)
        run.resize(1280,720);run.show()
        val sync=run.javaClass.getDeclaredMethod("synchronizeCombatAudio").apply{isAccessible=true}
        sync.invoke(run);check(loaded.last().first==MusicTrack.FOREST_NORMAL)
        val forest=loaded.last().second;val normalGain=forest.volume
        val chest=field(run,"chestRewardController").get(run)
        field(chest,"isOpen").setBoolean(chest,true);sync.invoke(run)
        check(loaded.last().second===forest && forest.playing && abs(forest.volume-normalGain*.65f)<.0001f)
        field(chest,"isOpen").setBoolean(chest,false)
        val stat=field(run,"statCardController").get(run)
        field(stat,"isOpen").setBoolean(stat,true);sync.invoke(run)
        check(loaded.last().second===forest && abs(forest.volume-normalGain*.65f)<.0001f)
        field(stat,"isOpen").setBoolean(stat,false);sync.invoke(run)
        check(abs(forest.volume-normalGain)<.0001f)
        val player=field(run,"player").get(run) as com.azizoglu.grisilith.player.Player
        player.health.takeDamage(Int.MAX_VALUE);sync.invoke(run)
        check(!field(run,"isRunSummaryOpen").getBoolean(run) && loaded.last().first==MusicTrack.SUMMARY_DEFEAT)
        val defeat=loaded.last().second;val starts=defeat.plays
        sync.invoke(run);check(defeat.plays==starts)
        val stage=field(run,"stageController").get(run)
        val maximum=stage.javaClass.getMethod("getMaximumStage").invoke(stage) as Int
        field(stage,"currentStage").setInt(stage,maximum+1);sync.invoke(run)
        check(loaded.last().first==MusicTrack.SUMMARY_VICTORY)
        run.hide();run.dispose();GameAudio.dispose()
        check(com.azizoglu.grisilith.combat.DamageDefense.resolve(100,DamageType.SLASHING,20,10,0,0)==77)
        println("PHASE10C_PASS: affinities, merge eligibility, armor stats-only hover, all difficulty selections, dropdown, remembered leaderboard, immediate summary music and uninterrupted reward music at 65%; percentage defense validated")
    }
}
