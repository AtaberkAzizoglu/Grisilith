package qa

import com.azizoglu.grisilith.combat.DamageDefense
import com.azizoglu.grisilith.content.DamageType
import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.enemy.EnemyStats
import com.azizoglu.grisilith.enemy.EnemyDamageContext
import com.azizoglu.grisilith.enemy.EnemyType
import com.azizoglu.grisilith.enemy.definition.DefaultEnemyKinds
import com.azizoglu.grisilith.enemy.runtime.EnemyKindStatScaler
import com.azizoglu.grisilith.player.Player
import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.statistics.StatType
import com.azizoglu.grisilith.summon.*
import kotlin.math.roundToInt

object Phase10dDefenseChecks {
    fun run() {
        fun hit(type:DamageType, piercing:Float=0f, interval:Float=1f, amount:Int=200) =
            DamageDefense.resolve(amount,type,60,40,20,30,piercing,interval)
        DamageType.entries.filter {it.isPhysical}.forEach {
            check(hit(it)==100)
            check(hit(it,.5f)==133)
            check(hit(it,1f)==160) // Still capped at 75%.
            check(hit(it,-1f)==100)
        }
        DamageType.entries.filter {it.isMagical}.forEach {
            check(hit(it)==133 && hit(it,.75f)==133)
            listOf(0f,.05f,.25f,.5f,1f).forEach {interval->check(hit(it,interval=interval)==133)}
        }
        check(hit(DamageType.NONE)==200)
        check(hit(DamageType.FIRE,amount=0)==0)
        check(hit(DamageType.FIRE,amount=-100)==0)
        check(hit(DamageType.FIRE,amount=1)==1)
        check(DamageDefense.resolve(200,DamageType.SLASHING,-50,-50,0,0)==200)
        check(DamageDefense.resolve(1,DamageType.FIRE,0,0,Int.MAX_VALUE,Int.MAX_VALUE)==1)
        check(DamageDefense.resolve(Int.MAX_VALUE,DamageType.FIRE,0,0,0,0)==Int.MAX_VALUE)
        for (defense in listOf(0,25,50,100,200,400,1000)) {
            check(DamageDefense.resolve(1000,DamageType.SLASHING,defense,0,0,0)==
                (1000.0*100/(100+defense)).roundToInt())
        }

        // Exercise all three actual actor entry points, not only the shared helper.
        val ps=PlayerStats(mapOf(StatType.MAX_HP to 100,StatType.ARMOR_CLASS to 40,StatType.MAGIC_RESISTANCE to 30))
        ps.adjustRunBonus(StatType.ARMOR_CLASS,15)
        ps.adjustRunBonus(StatType.MAGIC_RESISTANCE,5)
        ps.setArmorBaseValues(100,60,20)
        val player=Player(100f,100f,2000f,2000f,ps)
        check(player.takeDamage(200,DamageType.SLASHING)==100)
        check(player.takeDamage(200,DamageType.FIRE)==133)
        check(player.takeDamage(200,DamageType.PIERCING,.5f)==133)
        check(kotlin.math.abs(ps.rawArmorDamageReduction()-50f)<.001f)
        check(kotlin.math.abs(ps.effectiveArmorDamageReduction(.5f)-100f/3f)<.001f)
        val es=EnemyStats(EnemyType.NORMAL,10000,1,0f,40f,0f,40,60,
            baseMagicResistance=20,magicResistance=30)
        val enemy=Enemy(100f,100f,es)
        check(enemy.takeDamage(200,EnemyDamageContext(damageType=DamageType.SLASHING))==100)
        check(enemy.takeDamage(200,EnemyDamageContext(damageType=DamageType.FIRE))==133)
        check(enemy.takeDamage(200,EnemyDamageContext(damageType=DamageType.PIERCING,attackerPiercingIgnoreRatio=.5f))==133)
        val immune=Enemy(100f,100f,es.copy(damageAffinities=
            com.azizoglu.grisilith.combat.affinity.DamageAffinityProfile.of(
                DamageType.ACID to com.azizoglu.grisilith.combat.affinity.DamageAffinity.IMMUNE)))
        check(immune.takeDamage(200,EnemyDamageContext(damageType=DamageType.ACID))==0)
        val summon=SummonedEntity("test","test","test",SummonType.COW,
            SummonStats(10000,1,0f,1f,1f,40,30,60,20),100f,100f,30f)
        check(summon.takeDamage(200,DamageType.SLASHING)==100)
        check(summon.takeDamage(200,DamageType.FIRE,defenseTimeScale=.05f)==133)
        check(summon.takeDamage(200,DamageType.PIERCING,.5f)==133)

        val bosses=listOf(DefaultEnemyKinds.crocodile,DefaultEnemyKinds.grizzlyBear,
            DefaultEnemyKinds.beeSwarm,DefaultEnemyKinds.spearman,DefaultEnemyKinds.horseman,
            DefaultEnemyKinds.witch,DefaultEnemyKinds.slime,DefaultEnemyKinds.ghost,DefaultEnemyKinds.vampireLord)
        com.azizoglu.grisilith.enemy.runtime.EnemyAdaptiveScalingRuntime.clear()
        for (stage in listOf(1,25,50)) {
            bosses.forEach {kind->
                val s=EnemyKindStatScaler.resolve(kind,kind.allowedTypes.single(),stage)
                val physical=s.baseArmor+s.armorClass
                val magical=s.baseMagicResistance+s.magicResistance
                println("BOSS_BALANCE stage=$stage ${kind.displayName}: HP=${s.maximumHealth}, physical=$physical, magical=$magical, taken1000=${DamageDefense.resolve(1000,DamageType.SLASHING,s.baseArmor,s.armorClass,s.baseMagicResistance,s.magicResistance)}/${DamageDefense.resolve(1000,DamageType.FIRE,s.baseArmor,s.armorClass,s.baseMagicResistance,s.magicResistance)}")
            }
        }
        // HP tiers are compared at the same stage, including family and mini/final modifiers.
        bosses.forEachIndexed {index,kind->
            val hp=EnemyKindStatScaler.resolve(kind,kind.allowedTypes.single(),25).maximumHealth
            val target=listOf(36000,44000,28000,28000,60000,36000,60000,28000,60000)[index]
            check(kotlin.math.abs(hp-target)<target*.02f) { "HP tier mismatch: ${kind.displayName}" }
        }
        println("PHASE10D_DEFENSE_PASS: curve, rounding, piercing, magical types, interval independence, overflow, and real player/enemy/summon entry points")
    }
}
