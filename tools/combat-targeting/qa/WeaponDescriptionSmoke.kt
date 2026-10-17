package qa

import com.azizoglu.grisilith.ui.stat.*
import com.azizoglu.grisilith.weapon.definition.*
import com.azizoglu.grisilith.weapon.runtime.WeaponInstance
import com.azizoglu.grisilith.weapon.summon.IvySummonChoice
import com.azizoglu.grisilith.content.*
import com.azizoglu.grisilith.combat.condition.visual.ConditionVisualRenderer
import com.azizoglu.grisilith.enemy.*
import com.azizoglu.grisilith.player.Player
import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.summon.*
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.g2d.*
import com.badlogic.gdx.math.Rectangle

object WeaponDescriptionSmoke {
    fun checks(batch:SpriteBatch,font:BitmapFont,camera:OrthographicCamera,screenshot:(String)->Unit) {
        val symbols=SemanticSymbolRenderer
        check(symbols.decoration("Piercing",null)==null) // Falls through to the red STAT namespace.
        check(symbols.decoration("Piercing","Damage Type")!!.kind==SemanticSymbolRenderer.Kind.DAMAGE)
        check(symbols.decoration("Slashing","Attack Type")!!.kind==SemanticSymbolRenderer.Kind.PATTERN)
        check(symbols.decoration("Base Attack Speed",null)==null)
        check(symbols.decoration("Base Attack Speed:","Base Attack Speed")!!.kind==SemanticSymbolRenderer.Kind.HEADING)
        check(symbols.decoration("Armor Break",null)!!.icon==symbols.ARMOR_BREAK)
        val p=WeaponPresentation
        check(p.scalings(DefaultWeapons.orbOfSky,1)=="Wisdom > Might of Nature > Elemental Damage")
        check(p.scalings(DefaultWeapons.ivyStaff,1)=="Wisdom > Shepherd > Might of Nature")
        check("Ability:" !in p.scalings(DefaultWeapons.dagger))
        check(p.attackType(DefaultWeapons.doubleSwords).startsWith("Slashing"))
        check("Cannot activate" !in p.ability(DefaultWeapons.doubleSwords))
        check("hostile" !in p.ability(DefaultWeapons.ivyStaff))
        check("teleports" in p.ability(DefaultWeapons.steamWand))
        check("100 px" in p.ability(DefaultWeapons.tomeOfEryndor))
        check("ALL boomerangs" in p.ability(DefaultWeapons.boomerang))
        for(w in DefaultWeapons.all)for(level in 1..5) {
            val instance=WeaponInstance(w,level)
            check("Level $level" in p.hover(instance))
            check("Base Attack Speed:" in p.hover(instance))
        }
        for(choice in IvySummonChoice.entries) {
            val ivy=WeaponInstance(DefaultWeapons.ivyStaff,5)
            ivy.chooseIvySummon(choice)
            val cd=SummonSpawnScheduler(ivy.runtimeId,choice).cooldownSeconds.toInt()
            check("cooldown ${cd}s" in p.hover(ivy))
        }
        val marker=ConditionType.ARMOR_BREAK
        val enemy=Enemy(100f,100f,EnemyStats(EnemyType.NORMAL,100,0,0f,20f,0f,armorClass=10))
        enemy.applyTemporaryArmorClassReduction(1,3f)
        check(enemy.effectiveArmorClass==9 && enemy.conditions.contains(marker))
        fun tick(delta:Float)=enemy.update(delta,1500f,1500f,Rectangle(1500f,1500f,20f,20f),
            worldWidth=2000f,worldHeight=2000f,mapObstacles=emptyList(),nearbyEnemies=emptyList(),onContactDamage={_,_->})
        tick(2f);check(enemy.conditions.contains(marker))
        enemy.applyTemporaryArmorClassReduction(1,3f)
        tick(2.9f);check(enemy.effectiveArmorClass==9 && enemy.conditions.contains(marker))
        tick(.11f);check(enemy.effectiveArmorClass==10 && !enemy.conditions.contains(marker))
        val player=Player(500f,300f,2000f,2000f,PlayerStats())
        val summon=SummonedEntity("qa","WPN_IVY_STAFF","Ivy Staff",SummonType.COW,
            SummonStats(20,2,0f,30f,1f),700f,300f,15f)
        val renderer=ConditionVisualRenderer()
        for(container in listOf(player.conditions,enemy.conditions,summon.conditions)) {
            container.apply(marker,3f)
            check(marker in renderer.symbolTypes(container))
        }
        Gdx.gl.glClearColor(.06f,.06f,.07f,1f);Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        batch.begin()
        for(i in 0..29) {
            val x=30f+(i%6)*200f;val y=590f-(i/6)*115f
            symbols.drawIcon(batch,i,x,y,64f)
            font.data.setScale(.7f);font.draw(batch,"Symbol $i",x,y-5f)
        }
        batch.end();screenshot("weapon-semantic-symbols")
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        renderer.renderSymbols(player.conditions,500f,350f,camera)
        renderer.renderSymbols(enemy.conditions,650f,350f,camera)
        renderer.renderSymbols(summon.conditions,800f,350f,camera)
        screenshot("armor-break-all-entities")
        renderer.dispose()
        val atlas=Pixmap(Gdx.files.internal(SemanticSymbolRenderer.ATLAS_PATH))
        check((atlas.getPixel(0,0) and 255)==0);atlas.dispose()
        println("WEAPON_SEMANTICS_PASS: 30 glyphs; piercing stat/damage and base/stat speed namespaces; spell scaling order; 90 level-aware hovers; chosen summon cooldowns; armor-break refresh/expiry and player/enemy/summon symbols")
    }
}
