package qa

import com.azizoglu.grisilith.audio.*
import com.azizoglu.grisilith.item.definition.*
import com.azizoglu.grisilith.item.runtime.*
import com.azizoglu.grisilith.item.world.*
import com.azizoglu.grisilith.player.Player
import com.azizoglu.grisilith.statistics.*
import com.azizoglu.grisilith.weapon.definition.DefaultWeapons
import com.azizoglu.grisilith.weapon.runtime.*
import com.azizoglu.grisilith.enemy.*
import com.azizoglu.grisilith.enemy.definition.EnemyKindIds
import com.badlogic.gdx.*
import java.lang.reflect.Proxy

object ItemAudioSmoke {
 private fun method(value:Any,name:String)=value.javaClass.declaredMethods.single{it.name==name}.apply{isAccessible=true}
 private fun field(value:Any,name:String)=value.javaClass.getDeclaredField(name).apply{isAccessible=true}
 @JvmStatic fun main(args:Array<String>) {
    Gdx.input=Proxy.newProxyInstance(Input::class.java.classLoader,arrayOf(Input::class.java)){_,m,_->
        when(m.returnType){java.lang.Boolean.TYPE->false;java.lang.Integer.TYPE->0;java.lang.Float.TYPE->0f;else->null}
    } as Input
    var now=1000L
    val sounds=UiSound.entries.associateWith{AudioSmoke.FakeSound()}
    val audio=AudioController({AudioSmoke.FakeMusic().api},{sounds.getValue(it).api},{AudioLevels(1f,1f,1f)},{now})
    GameAudio::class.java.getDeclaredField("controller").apply{isAccessible=true}.set(null,audio)
    GameAudio.worldView(0f,0f,2000f,2000f,500f,500f)
    fun advance(ms:Long){now+=ms;GameAudio.update();GameAudio.update()}
    fun count(cue:UiSound)=sounds.getValue(cue).played.size
    val channels=field(audio,"channels").get(audio) as Map<*,*>
    val inventory=ItemInventory();val stats=PlayerStats();val player=Player(476f,476f,4000f,4000f,stats)
    val world=WorldItemRuntime(inventory,4000f,4000f)
    fun add(item:ItemDefinition){check(inventory.addItem(ItemInstance(item))==ItemInventoryResult.ADDED)}
    ActiveItemEffects.bind(inventory,WeaponInventory(),stats,player,{emptyList()},{emptyList()},{1},
        {emptyList()},{0},{StatType.STRENGTH},{0})
    // Exact outcome-to-sound mapping; HA5 is an outcome cue, not an aura tick loop.
    val cues=listOf(UiSound.ITEM_HEALING_AURA,UiSound.ITEM_PROTECTERS,UiSound.ITEM_TREASURE,
        UiSound.ITEM_HOURGLASS,UiSound.ITEM_TOADS,UiSound.ITEM_LOSE_GOLD,UiSound.ITEM_HOSTILE_BOSS,
        UiSound.ITEM_SHRINK,UiSound.ITEM_CURSE)
    TotemResult.entries.zip(cues).forEach{(result,cue)->
        ItemAudio.totemResult(result,500f,500f);check(count(cue)==1);advance(10000)
    }
    check(channels.isEmpty()) // Ephemeral owners are reclaimed, unlike explicitly owned effect loops.
    ItemAudio.at(UiSound.ITEM_SHRINK,500f,500f)
    val shrink=sounds.getValue(UiSound.ITEM_SHRINK)
    GameAudio.combatPlayback(2f,false);advance(500)
    check(shrink.pitches.last()==2f)
    GameAudio.combatPlayback(2f,true);val stops=shrink.stops;advance(5000)
    check(shrink.pauses>0 && shrink.stops==stops && channels.isNotEmpty())
    GameAudio.combatPlayback(2f,false);advance(1300);check(channels.isEmpty())
    GameAudio.combatPlayback(1f,false)
    ItemAudio.at(UiSound.ITEM_CURSE,500f,500f);GameAudio.stopStageEffects();check(channels.isEmpty())
    advance(5000);check(channels.isEmpty())
    // A single aggregate Broom cadence, not one sound per copy on every update.
    add(DefaultItems.animatedBroom)
    method(world,"synchronizeBrooms").invoke(world,player)
    val broom=method(world,"updateBrooms")
    val collect:(Float,Float,Float)->Boolean={_,_,_->false}
    val nearest:(Float,Float)->Pair<Float,Float>?={_,_->null}
    broom.invoke(world,2.9f,collect,nearest);check(count(UiSound.ITEM_BROOM)==0)
    advance(3000);broom.invoke(world,.11f,collect,nearest);check(count(UiSound.ITEM_BROOM)==1)
    add(DefaultItems.animatedBroom);method(world,"synchronizeBrooms").invoke(world,player)
    advance(2700);broom.invoke(world,2.7f,collect,nearest);check(count(UiSound.ITEM_BROOM)==2)
    check(kotlin.math.abs(ItemAudio.broomInterval(2)-2.7f)<.001f && ItemAudio.broomInterval(100)==.3f)
    // Close Call only on entering the buff, not every near-miss refresh.
    advance(5000);add(DefaultItems.closeCall)
    ActiveItemEffects.onCloseCall();check(count(UiSound.ITEM_CLOSE_CALL)==1)
    repeat(20){ActiveItemEffects.onCloseCall()};check(count(UiSound.ITEM_CLOSE_CALL)==1)
    ActiveItemEffects.update(3.1f);advance(3100);ActiveItemEffects.onCloseCall();check(count(UiSound.ITEM_CLOSE_CALL)==2)
    // Second Heart is consumed once and prevents the lethal HP event.
    advance(5000);add(DefaultItems.secondHeart)
    val damage=ActiveItemEffects.resolveDamageBeforeHealth(player.health.currentHealth+100)
    check(damage==player.health.currentHealth-1 && count(UiSound.ITEM_SECOND_HEART)==1)
    ActiveItemEffects.resolveDamageBeforeHealth(10000);check(count(UiSound.ITEM_SECOND_HEART)==1)
    // Blood Vial plays only on a successful proc, not on each ability attempt.
    advance(5000);add(DefaultItems.bloodSacrifice)
    val weapon=WeaponInstance(DefaultWeapons.longsword,3)
    var proc=false
    repeat(200){if(!proc)proc=ActiveItemEffects.onAbilityActivated(weapon)}
    check(proc && count(UiSound.ITEM_BLOOD_VIAL)==1)
    // Pit Trap: build, each actual victim death, then one completion cue.
    advance(5000);add(DefaultItems.pitTrap)
    val pit=method(world,"updatePitTrap")
    val killed:(Enemy)->Int={1};var releases=0
    val release:(Float,Float,Int)->Unit={_,_,_->releases++}
    pit.invoke(world,.01f,player,emptyList<Enemy>(),killed,release)
    pit.invoke(world,2.01f,player,emptyList<Enemy>(),killed,release)
    check(count(UiSound.ITEM_TRAP_BUILD)==1)
    pit.invoke(world,3.01f,player,emptyList<Enemy>(),killed,release)
    val trap=(field(world,"traps").get(world) as List<*>).single()!!
    val x=field(trap,"x").getFloat(trap);val y=field(trap,"y").getFloat(trap)
    val enemies=List(4){Enemy(x,y,EnemyStats(EnemyType.NORMAL,100,10,0f,20f,0f,kindId=EnemyKindIds.GRAY_WOLF))}
    repeat(4){advance(1000);pit.invoke(world,.1f,player,enemies,killed,release)}
    check(enemies.all{it.isDead} && count(UiSound.ITEM_TRAP_KILL)==4 && count(UiSound.ITEM_TRAP_END)==1 && releases==1)
    repeat(5){pit.invoke(world,1f,player,emptyList<Enemy>(),killed,release)}
    check(count(UiSound.ITEM_TRAP_END)==1)
    // Merely acquiring Cash Out does not play the market-only cue.
    ActiveItemEffects.applyAcquisition(DefaultItems.cashOut);GameAudio.update();check(count(UiSound.ITEM_CASH_OUT)==0)
    ActiveItemEffects.unbind(stats);GameAudio.dispose()
    println("ITEM_AUDIO_PASS: all nine Totem outcomes; owner cleanup/speed/pause/stage discard; aggregate Broom cadence; Close Call refresh guard; Second Heart; Blood Vial proc; all three Pit Trap events; Cash Out acquisition silence")
 }
}
