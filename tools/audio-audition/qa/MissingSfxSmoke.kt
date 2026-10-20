package qa

import com.azizoglu.grisilith.audio.*
import com.azizoglu.grisilith.combat.*
import com.azizoglu.grisilith.combat.healing.*
import com.azizoglu.grisilith.player.Player
import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.tamga.runtime.active.*
import com.azizoglu.grisilith.weapon.definition.DefaultWeapons
import com.azizoglu.grisilith.weapon.runtime.*
import com.azizoglu.grisilith.screen.components.MenuButton
import com.badlogic.gdx.math.Vector2
import kotlin.math.abs

object MissingSfxSmoke {
 @JvmStatic fun main(args:Array<String>) {
    com.badlogic.gdx.Gdx.input = java.lang.reflect.Proxy.newProxyInstance(
        com.badlogic.gdx.Input::class.java.classLoader,arrayOf(com.badlogic.gdx.Input::class.java)) { _,method,_ ->
        when(method.returnType) {
            java.lang.Boolean.TYPE -> false
            java.lang.Integer.TYPE -> 0
            java.lang.Float.TYPE -> 0f
            else -> null
        }
    } as com.badlogic.gdx.Input
    var now=1000L;var levels=AudioLevels(1f,1f,1f)
    val sounds=UiSound.entries.associateWith{AudioSmoke.FakeSound()}
    val audio=AudioController({AudioSmoke.FakeMusic().api},{sounds.getValue(it).api},{levels},{now})
    GameAudio::class.java.getDeclaredField("controller").apply{isAccessible=true}.set(null,audio)
    GameAudio.worldView(0f,0f,1000f,1000f,500f,500f)
    fun advance(ms:Long){now+=ms;GameAudio.update()}
    // Both visual types share the same real-time gate; every HP increment still applies.
    HealingVisualManager.clear()
    val hp=HealthComponent(1000);hp.takeDamage(500)
    hp.onHealingApplied={_,type->HealingVisualManager.spawn(type,500f,500f,48f)}
    repeat(20) { i -> hp.heal(1,if(i%2==0)HealingVisualType.HEAL else HealingVisualType.LIFE_STEAL);GameAudio.update();advance(50) }
    check(hp.currentHealth==520)
    check(HealingVisualManager.activeEvents.size==4)
    check(sounds.getValue(UiSound.HEAL).played.size==4)
    check(sounds.getValue(UiSound.HEAL).played.all{abs(it-.12f)<.0001f})
    levels=AudioLevels(.5f,1f,.4f);advance(2000)
    hp.heal(1);GameAudio.update();check(abs(sounds.getValue(UiSound.HEAL).played.last()-.024f)<.0001f)
    hp.heal(9999);advance(250);val before=HealingVisualManager.activeEvents.size
    check(hp.heal(1)==0 && HealingVisualManager.activeEvents.size==before)
    // Hover triggers on entry, not every frame, and cannot swallow an immediate click.
    levels=AudioLevels(1f,1f,1f);advance(3000)
    var clicks=0
    val button=MenuButton("QA",0f,0f,100f,100f){clicks++}
    val pointer=Vector2(10f,10f)
    repeat(20){button.update(pointer,false,false);GameAudio.update();advance(10)}
    check(sounds.getValue(UiSound.HOVER).played.size==1)
    button.update(pointer,true,false);advance(1);button.update(pointer,false,true);GameAudio.update()
    check(clicks==1 && sounds.getValue(UiSound.CLICK).played.size==1)
    button.update(Vector2(200f,200f),false,false);advance(100)
    button.update(pointer,false,false);GameAudio.update();check(sounds.getValue(UiSound.HOVER).played.size==2)
    // All merge entry paths emit WM1 only on a successful merge.
    advance(3000)
    val inventory=WeaponInventory()
    repeat(6){inventory.addWeapon(WeaponInstance(DefaultWeapons.longsword,1))}
    check(inventory.addPurchasedWeapon(WeaponInstance(DefaultWeapons.longsword,1))==WeaponInventoryResult.MERGED)
    GameAudio.update();check(sounds.getValue(UiSound.WEAPON_MERGE).played.size==1)
    // Phantom Steed: immediate voice, next at 11 active seconds, none after it ends.
    advance(3000)
    val stats=PlayerStats();val player=Player(476f,476f,4000f,4000f,stats)
    val steed=PhantomSteedRuntime(stats){1};steed.attachPlayer(player)
    check(steed.tryActivate());GameAudio.update()
    val horse=sounds.getValue(UiSound.PHANTOM_STEED)
    check(horse.played.size==1)
    steed.update(10f);advance(10000);check(horse.played.size==1)
    steed.update(1f);GameAudio.update();check(horse.played.size==2)
    steed.update(5f);advance(5000);steed.update(11f);GameAudio.update();check(horse.played.size==2)
    // Charger loop ends on reset and a later activation gets a fresh stage-safe owner.
    advance(3000)
    val charger=ChargerRuntime();charger.attachPlayer(player)
    check(charger.tryActivate());GameAudio.update()
    val charge=sounds.getValue(UiSound.CHARGER_CHARGE);check(charge.played.size==1)
    check(abs(charge.played.last()-.2f)<.0001f)
    GameAudio.combatPlayback(2f,false);check(charge.pitches.last()==2f)
    GameAudio.combatPlayback(2f,true);check(charge.pauses>0)
    GameAudio.combatPlayback(2f,false);check(charge.resumes>0)
    GameAudio.stopStageEffects();val old=charge.played.size
    charger.reset();GameAudio.combatPlayback(1f,false)
    check(charger.tryActivate());GameAudio.update();check(charge.played.size==old+1)
    charger.reset();check(charge.stops>0)
    GameAudio.dispose();HealingVisualManager.clear()
    println("MISSING_SFX_PASS: shared 4/sec heal/life-steal presentation cap without HP loss; low heal gain + Settings; hover entry/click; merge; steed 11s; quiet Charger loop/speed/pause/stage restart")
 }
}
