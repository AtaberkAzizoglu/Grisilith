package qa

import com.azizoglu.grisilith.GrisilithGame
import com.azizoglu.grisilith.character.trait.*
import com.azizoglu.grisilith.combat.DamageDefense
import com.azizoglu.grisilith.content.DamageType
import com.azizoglu.grisilith.enemy.*
import com.azizoglu.grisilith.player.Player
import com.azizoglu.grisilith.statistics.*
import com.azizoglu.grisilith.tamga.runtime.TamgaRuntimeManager
import com.azizoglu.grisilith.ui.ScreenTitleArt
import com.azizoglu.grisilith.weapon.definition.DefaultWeapons
import com.azizoglu.grisilith.weapon.runtime.*
import com.azizoglu.grisilith.weapon.glyph.*
import com.azizoglu.grisilith.weapon.skill.*
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g2d.*
import com.badlogic.gdx.math.*

object UrgentHotfixChecks {
    private fun invoke(o:Any,name:String,vararg args:Any):Any? = o.javaClass.declaredMethods.first {
        it.name==name && it.parameterCount==args.size
    }.apply { isAccessible=true }.invoke(o,*args)

    private fun shot(name:String) {
        val pix=Pixmap.createFromFrameBuffer(0,0,1280,720)
        PixmapIO.writePNG(Gdx.files.local("../tools/combat-targeting/hotfix-$name.png"),pix,-1,true);pix.dispose()
    }

    fun checks(batch:SpriteBatch,font:BitmapFont,camera:OrthographicCamera) {
        val stats=PlayerStats(StatType.entries.associateWith { 25 }).apply {
            StatType.entries.forEach { addRunBonus(it,25) }
        }
        check(stats.totalScore(StatType.WISDOM)==50)
        val calc=WeaponDamageCalculator(stats)
        val player=Player(1300f,1300f,3000f,3000f,stats)
        fun enemy(type:EnemyType=EnemyType.NORMAL,hp:Int=1000)=Enemy(480f,490f,
            EnemyStats(type,hp,0,120f,20f,0f,baseMagicResistance=4,magicResistance=8))
        fun context(type:DamageType,seconds:Float=1f)=EnemyDamageContext(damageType=type,defenseTimeScale=seconds,appliesAutomaticCondition=false)
        for((type,ratio,interval) in listOf(Triple(DamageType.AIR,.00010f,.25f),
            Triple(DamageType.ACID,.00015f,.25f),Triple(DamageType.NECROTIC,.00020f,.333f))) {
            val raw=calc.calculateMaximumHealthSpellTickDamage(1000,type,ratio)
            val old=enemy().takeDamage(raw,context(type))
            val fixed=enemy().takeDamage(raw,context(type,interval))
            check(old==1 && fixed>1) { "$type raw=$raw old=$old new=$fixed" }
            check(enemy().takeDamage(100,context(type))==68) { "Normal hit mitigation changed" }
            println("PERCENT_TICK_FIX: $type, HP=1000, Wisdom+related=100, MR=4x8: raw=$raw old=$old fixed=$fixed")
        }
        check(calc.calculateMaximumHealthSpellTickDamage(100000000,DamageType.AIR,.0001f)==1000000)
        val inv=WeaponInventory()
        val runtime=WeaponRuntimeManager(inv,stats,TamgaRuntimeManager(inv,stats,null,null,emptySet()){50},
            CharacterTraitDefinition(CharacterTraitId.NECROMANCY,"Test","Test"),player){50}
        val callback:(Int,String)->Unit={_,_->};val killed:(Enemy)->Unit={}
        invoke(runtime,"setVortexWeapon",WeaponInstance(DefaultWeapons.orbOfSky,5))
        val vortexEnemy=enemy();invoke(runtime,"applyVortexDamageTick",vortexEnemy,.25f,callback,killed)
        check(1000-vortexEnemy.health.currentHealth==2)
        invoke(runtime,"setAcidicPoolWeapon",WeaponInstance(DefaultWeapons.amuletOfTrollKing,5))
        val acidEnemy=enemy();invoke(runtime,"applyAcidicPoolDamageTick",acidEnemy,.25f,500f,500f,callback,killed)
        check(1000-acidEnemy.health.currentHealth==7)
        invoke(runtime,"setStaticCloudsWeapon",WeaponInstance(DefaultWeapons.totemOfDarkClouds,5))
        val cloudEnemy=enemy();invoke(runtime,"applyStaticCloudsDamageTick",cloudEnemy,1f/3f,callback,killed)
        check(1000-cloudEnemy.health.currentHealth>1)

        val singularity=SingularityRuntime(calc)
        val singularityEnemy=enemy();val enemies=EnemyManager();enemies.spawn(singularityEnemy)
        val chalk=WeaponInstance(DefaultWeapons.enchantedChalkDust,3)
        check(singularity.begin(GlyphCastRequest(GlyphType.SINGULARITY,500f,500f,chalk)))
        repeat(4){singularity.update(.1f,player,enemies,emptyList(),3000f,3000f,callback,killed)}
        check(1000-singularityEnemy.health.currentHealth>1)
        singularity.clear()

        for(type in listOf(EnemyType.MINI_BOSS,EnemyType.FINAL_BOSS)) {
            val boss=enemy(type,100000);val before=Vector2(boss.centerX,boss.centerY)
            val vortex=VortexPullRuntime()
            check(vortex.tryBegin(500f,500f,listOf(boss),3000f,3000f,false,Vector2(600f,500f)))
            var forcedPull=false
            vortex.update(.016f,listOf(boss),50,0f,{_,_,_,_,_->forcedPull=true},{_,_->},{})
            check(!forcedPull && before.epsilonEquals(boss.centerX,boss.centerY,.001f))
            check(boss.bossGravityMovementMultiplier(1f,0f)>1f)
            check(boss.bossGravityMovementMultiplier(-1f,0f)<1f)
            check(vortex.movementMultiplier(boss)==1f)
            val manager=EnemyManager();manager.spawn(boss)
            check(singularity.begin(GlyphCastRequest(GlyphType.SINGULARITY,600f,500f,chalk)))
            singularity.update(.016f,player,manager,emptyList(),3000f,3000f,callback,killed)
            check(before.epsilonEquals(boss.centerX,boss.centerY,.001f))
            check(boss.bossGravityMovementMultiplier(1f,0f) in 1f..1.10001f)
            singularity.clear()
            boss.update(.21f,1500f,1500f,Rectangle(1500f,1500f,20f,20f),worldWidth=3000f,worldHeight=3000f,
                mapObstacles=emptyList(),nearbyEnemies=emptyList(),onContactDamage={_,_->})
            check(boss.bossGravityMovementMultiplier(1f,0f)==1f)
        }
        runtime.dispose()
        check(!com.azizoglu.grisilith.summon.SummonPresentation.choice(com.azizoglu.grisilith.weapon.summon.IvySummonChoice.SUMMON_ANIMALS).contains("cow",true))
        check(!com.azizoglu.grisilith.summon.SummonPresentation.choice(com.azizoglu.grisilith.weapon.summon.IvySummonChoice.SUMMON_DEMONS).contains("imp",true))
        for(choice in ChalkDustSpellChoice.entries) check(!GlyphChoicePresentation.description(choice).startsWith("Draw "))

        Gdx.gl.glClearColor(.04f,.04f,.045f,1f);Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        camera.position.set(640f,360f,0f);camera.update();batch.projectionMatrix=camera.combined
        batch.begin()
        ScreenTitleArt.labels.forEachIndexed { index,label ->
            val column=index%3;val row=index/3
            ScreenTitleArt.draw(batch,label,Rectangle(column*426f+16f,640f-row*170f,394f,54f))
            font.data.setScale(.6f);font.color=Color.LIGHT_GRAY
            font.draw(batch,label,column*426f+16f,615f-row*170f,394f,com.badlogic.gdx.utils.Align.center,false)
        }
        batch.end();shot("title-family")
        val game=GrisilithGame()
        val settings=com.azizoglu.grisilith.screen.SettingsScreen(game){}
        settings.show();settings.resize(1280,720);settings.render(.016f);shot("settings");settings.dispose()
        val chronicle=com.azizoglu.grisilith.screen.HighScoreScreen(game)
        chronicle.show();chronicle.resize(1280,720);chronicle.render(.016f);shot("chronicle");chronicle.dispose()
        println("URGENT_HOTFIX_PASS: percentage ticks through actual Vortex/Acid/Singularity paths; unchanged direct-hit defenses; overflow safety; both boss tiers unpulled, directional speed and expiry; 12 heading images; spoilers/drawing copy")
    }
}
