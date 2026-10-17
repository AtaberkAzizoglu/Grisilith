package qa

import com.azizoglu.grisilith.GrisilithGame
import com.azizoglu.grisilith.character.DefaultCharacters
import com.azizoglu.grisilith.config.*
import com.azizoglu.grisilith.enemy.EnemyType
import com.azizoglu.grisilith.screen.*
import com.azizoglu.grisilith.screen.render.RunSummaryRenderer
import com.azizoglu.grisilith.stage.StageController
import com.azizoglu.grisilith.statistics.RunStatistics
import com.azizoglu.grisilith.statistics.career.*
import com.azizoglu.grisilith.summary.*
import com.azizoglu.grisilith.ui.typography.UiFontFactory
import com.badlogic.gdx.*
import com.badlogic.gdx.backends.lwjgl3.*
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g2d.*
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.viewport.FitViewport
import java.math.BigDecimal
import java.math.RoundingMode

object ScoreSmoke {
    @JvmStatic fun main(args:Array<String>) {
        Lwjgl3Application(object:ApplicationAdapter() {
            override fun create() { try { checks();Gdx.app.exit() } catch(t:Throwable) {t.printStackTrace();kotlin.system.exitProcess(1)} }
        },Lwjgl3ApplicationConfiguration().apply {
            setWindowedMode(1280,720);setInitialVisible(false);disableAudio(true)
            setPreferencesConfig("../tools/combat-targeting/score-prefs",Files.FileType.Local)
        })
    }
    private fun field(o:Any,name:String)=o.javaClass.getDeclaredField(name).apply{isAccessible=true}
    private fun invoke(o:Any,name:String)=o.javaClass.getDeclaredMethod(name).apply{isAccessible=true}.invoke(o)
    private fun score(n:Int=0,u:Int=0,m:Int=0,f:Int=0,s:Int=0,d:GameDifficulty=GameDifficulty.HARD,map:GameMap=GameMap.ROAD_TO_HOME)=
        RunScoreCalculator.calculate(map,d,n,u,m,f,s)
    private fun shot(name:String) {
        val pix=Pixmap.createFromFrameBuffer(0,0,1280,720)
        PixmapIO.writePNG(Gdx.files.local("../tools/combat-targeting/score-v1-$name.png"),pix,-1,true);pix.dispose()
    }
    private fun completed(value:Long)=CompletedRunStatistics(false,90,4,1,1,10000L,200L,50f,10f,0,value)
    private fun checks() {
        check(score(n=1)==1L && score(u=1)==3L && score(m=1)==250L && score(f=1)==5000L)
        check(score(n=1,u=1,m=1,f=1)==5254L)
        check(score(n=10000,s=10)==20000L)
        check(score(n=10000,s=25)==35000L)
        check(score(n=10000,s=50)==60000L)
        check(score(n=10000,s=25,d=GameDifficulty.EASY)==11550L)
        check(score(n=10000,s=35,d=GameDifficulty.NORMAL)==22500L)
        check(score(n=1,d=GameDifficulty.NORMAL)==1L)
        check(score(n=1,d=GameDifficulty.EASY)==0L)
        check(score(s=50)==0L && score(n=-1,u=-1,m=-1,f=-1,s=-1)==0L)
        for(difficulty in GameDifficulty.entries)for(stage in listOf(-10,0,10,25,35,50,Int.MAX_VALUE)) {
            check(score(Int.MAX_VALUE,Int.MAX_VALUE,Int.MAX_VALUE,Int.MAX_VALUE,stage,difficulty,GameMap.TEST_MAP)==0L)
            val base=BigDecimal(Int.MAX_VALUE).multiply(BigDecimal(5254))
            val multiplier=when(difficulty){GameDifficulty.EASY->"0.33";GameDifficulty.NORMAL->"0.50";GameDifficulty.HARD->"1.00"}
            val completed=stage.coerceIn(0,difficulty.maximumStage)
            val expected=base.multiply(BigDecimal(multiplier)).multiply(BigDecimal.ONE.add(BigDecimal(completed).movePointLeft(1)))
                .setScale(0,RoundingMode.HALF_UP).longValueExact()
            check(score(Int.MAX_VALUE,Int.MAX_VALUE,Int.MAX_VALUE,Int.MAX_VALUE,stage,difficulty)==expected)
        }
        val stats=RunStatistics()
        EnemyType.entries.forEach(stats::recordEnemyKilled)
        check(stats.totalEnemiesKilled==4 && stats.totalNormalEnemiesKilled==1 && stats.totalUniqueEnemiesKilled==1)
        check(stats.totalBossesDefeated==1 && stats.totalFinalBossesDefeated==1)
        val saved=stats.exportSaveState()
        val restored=RunStatistics().apply{restoreSaveState(saved)}
        check(restored.totalUniqueEnemiesKilled==1 && restored.totalNormalEnemiesKilled==1)
        val legacy=com.azizoglu.grisilith.save.RunSaveStatistics(totalEnemiesKilled=12,totalMiniBossesDefeated=2,totalFinalBossesDefeated=1)
        restored.restoreSaveState(legacy)
        check(restored.totalUniqueEnemiesKilled==0 && restored.totalNormalEnemiesKilled==9)

        val prefs=Gdx.app.getPreferences("grisilith_high_scores")
        // Only touches this test's isolated score-prefs directory.
        prefs.clear()
        prefs.putLong("enemies_killed",123L).putLong("highest_damage",999L).flush()
        HighScoreRepository.load()
        check(HighScoreRepository.statistics.totalScore==0L && HighScoreRepository.statistics.highestScore==0L)
        check(HighScoreRepository.statistics.enemiesKilled==123L && HighScoreRepository.statistics.highestDamage==999L)
        HighScoreRepository.commitCompletedRun(completed(100))
        HighScoreRepository.commitCompletedRun(completed(50))
        HighScoreRepository.commitCompletedRun(completed(0))
        HighScoreRepository.load()
        check(HighScoreRepository.statistics.totalScore==150L && HighScoreRepository.statistics.highestScore==100L)
        HighScoreRepository.commitCompletedRun(completed(-100))
        check(HighScoreRepository.statistics.totalScore==150L)
        println("SCORE_PURE_PASS: exact kill weights, no double counting, difficulty factors, completed-stage bonuses, final rounding, zero Test Map, overflow boundaries, unique-count save compatibility and career migration/persistence")

        val game=GrisilithGame()
        val screen=FirstScreen(game,GameDifficulty.HARD,GameMap.ROAD_TO_HOME,DefaultCharacters.all.first())
        val runtimeStats=field(screen,"runStatistics").get(screen) as RunStatistics
        val stage=field(screen,"stageController").get(screen) as StageController
        EnemyType.entries.forEach(runtimeStats::recordEnemyKilled)
        repeat(10){stage.completeCurrentStage()}
        val expected=score(1,1,1,1,11)
        check(invoke(screen,"currentEnemyScore")==5254L)
        check(invoke(screen,"currentRunScore")==expected)
        runtimeStats.recordDamageDealt(1000000,"Ignored score source")
        runtimeStats.recordGoldCollected(50000f)
        check(invoke(screen,"currentRunScore")==expected)
        val before=HighScoreRepository.statistics.totalScore
        invoke(screen,"commitCareerStatistics");invoke(screen,"commitCareerStatistics")
        check(HighScoreRepository.statistics.totalScore==before+expected)
        val snapshot=invoke(screen,"currentRunSummarySnapshot") as RunSummarySnapshot
        check(snapshot.score==expected)
        check(snapshot.enemyScore==5254L && snapshot.scoreBeforeStageBonus==5254L)
        stage.restoreCheckpointStage(17)
        val stage17=invoke(screen,"currentRunSummarySnapshot") as RunSummarySnapshot
        check(kotlin.math.abs(stage17.scoreStageMultiplier-2.7f)<.0001f)
        check(stage17.score==score(1,1,1,1,17))
        repeat(40){stage.completeCurrentStage()}
        check(stage.isRunComplete && invoke(screen,"currentRunScore")==score(1,1,1,1,50))

        val camera=OrthographicCamera();val viewport=FitViewport(1280f,720f,camera);viewport.update(1280,720,true)
        val batch=SpriteBatch();val shapes=ShapeRenderer();val font=UiFontFactory.createBody();val heading=UiFontFactory.createDisplay(28)
        val renderer=RunSummaryRenderer(camera,1280f,720f,viewport,shapes,batch,font,heading)
        fun draw(snapshot:RunSummarySnapshot,name:String,time:Float=Float.POSITIVE_INFINITY) {
            Gdx.gl.glClearColor(.04f,.04f,.04f,1f);Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
            batch.projectionMatrix=camera.combined
            batch.begin();renderer.renderTexts(snapshot,scoreElapsedSeconds=time);batch.end();shot(name)
        }
        revealChecks()
        draw(stage17,"reveal-start",0f)
        draw(stage17,"reveal-delay",1.9f)
        draw(stage17,"reveal-counting",2.6f)
        draw(stage17,"reveal-finished",4f)
        draw(snapshot,"summary")
        draw(snapshot.copy(score=Long.MAX_VALUE),"summary-large")
        val test=FirstScreen(game,GameDifficulty.HARD,GameMap.TEST_MAP,DefaultCharacters.all.first())
        val testStats=field(test,"runStatistics").get(test) as RunStatistics
        repeat(100){EnemyType.entries.forEach(testStats::recordEnemyKilled)}
        check(invoke(test,"currentRunScore")==0L)
        val testBefore=HighScoreRepository.statistics.totalScore
        invoke(test,"commitCareerStatistics")
        check(HighScoreRepository.statistics.totalScore==testBefore)
        val testSnapshot=invoke(test,"currentRunSummarySnapshot") as RunSummarySnapshot
        check(testSnapshot.score==0L);draw(testSnapshot,"test-map-summary")
        val chronicle=HighScoreScreen(game);chronicle.show();chronicle.resize(1280,720)
        field(chronicle,"scrollOffset").setFloat(chronicle,300f)
        chronicle.render(.016f);shot("chronicle")
        hudChecks(game)
        chronicle.dispose();test.dispose();screen.dispose();batch.dispose();shapes.dispose();font.dispose();heading.dispose()
        println("SCORE_INTEGRATION_PASS: actual FirstScreen Score/summary/commit, damage and gold independence, completed stage vs reached stage, victory cap, one-time career commit, Test Map zero and no career contribution; summary and Chronicle renders")
    }

    private fun hudChecks(game:GrisilithGame) {
        val settings=com.azizoglu.grisilith.settings.GameSettings
        val prefs=Gdx.app.getPreferences("grisilith_settings")
        prefs.remove("show_score");prefs.putBoolean("fullscreen",false).flush()
        settings.load()
        check(settings.showScore && settings.snapshot().showScore && settings.defaults().showScore)
        val menu=SettingsScreen(game){}
        menu.show();menu.resize(1280,720);menu.render(.016f);shot("settings-score-on")
        @Suppress("UNCHECKED_CAST")
        val buttons=field(menu,"buttons").get(menu) as List<com.azizoglu.grisilith.screen.components.MenuButton>
        val toggle=buttons.single { it.bounds.x==650f && it.bounds.y==186f }
        @Suppress("UNCHECKED_CAST")
        val click=field(toggle,"onClick").get(toggle) as ()->Unit
        click()
        check(!(field(menu,"draftSettings").get(menu) as com.azizoglu.grisilith.settings.GameSettings.Snapshot).showScore)
        invoke(menu,"applyChanges")
        check(!settings.showScore && !prefs.getBoolean("show_score",true))
        settings.load();check(!settings.showScore)
        menu.render(.016f);shot("settings-score-off");menu.dispose()

        val live=FirstScreen(game,GameDifficulty.HARD,GameMap.ROAD_TO_HOME,DefaultCharacters.all.first())
        live.show();live.resize(1280,720)
        val stats=field(live,"runStatistics").get(live) as RunStatistics
        val stage=field(live,"stageController").get(live) as StageController
        repeat(1000){stats.recordEnemyKilled(EnemyType.NORMAL)}
        repeat(10){stage.completeCurrentStage()}
        check(invoke(live,"currentRunScore")==2100L)
        check(invoke(live,"currentEnemyScore")==1000L)
        settings.restore(settings.snapshot().copy(showScore=true))
        invoke(live,"renderGame");shot("hud-on")
        val on=Pixmap.createFromFrameBuffer(490,592,300,35)
        settings.restore(settings.snapshot().copy(showScore=false))
        invoke(live,"renderGame");shot("hud-off")
        val off=Pixmap.createFromFrameBuffer(490,592,300,35)
        var different=0
        for(y in 0 until on.height)for(x in 0 until on.width)if(on.getPixel(x,y)!=off.getPixel(x,y))different++
        check(different>20){"Score toggle did not change the HUD score region: $different pixels"}
        on.dispose();off.dispose()
        // Hiding the HUD must never alter or freeze actual scoring.
        stats.recordEnemyKilled(EnemyType.FINAL_BOSS)
        check(invoke(live,"currentRunScore")==12600L)
        check(invoke(live,"currentEnemyScore")==6000L)
        settings.restore(settings.defaults());check(settings.showScore)
        live.dispose()
        println("SCORE_HUD_PASS: +0.1x/stage, live kill updates, default-on migration, actual General toggle, save/load/defaults and HUD pixel difference; hiding does not disable scoring")
    }

    private fun revealChecks() {
        val start=10000L;val end=27000L
        check(ScoreReveal.sample(start,end,0f).value==start)
        check(ScoreReveal.sample(start,end,1.99f).multiplierAlpha==0f)
        val middle=ScoreReveal.sample(start,end,2.5f)
        check(middle.value in start+1 until end && middle.multiplierAlpha==1f)
        check(ScoreReveal.sample(start,end,4f).value==end)
        check(ScoreReveal.sample(start,end,Float.POSITIVE_INFINITY).value==end)
        check(ScoreReveal.sample(start,end,Float.NaN).value==start)
        for((from,to) in listOf(start to end,0L to 0L,0L to Long.MAX_VALUE,Long.MAX_VALUE-100 to Long.MAX_VALUE)) {
            var previous=from
            for(frame in 0..400) {
                val value=ScoreReveal.sample(from,to,frame/100f).value
                check(value>=previous && value in from..to)
                previous=value
            }
            check(previous==to)
        }
        println("SCORE_REVEAL_PASS: delayed multiplier, monotonic eased counter, exact endpoint, huge/zero values and no frame/snapshot-dependent state")
    }
}
