package com.azizoglu.grisilith.weapon.runtime

import com.azizoglu.grisilith.content.DamageType
import com.azizoglu.grisilith.enemy.*
import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.weapon.Projectile
import com.azizoglu.grisilith.weapon.definition.catalog.NewPhysicalWeapons
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import kotlin.math.*

/** New physical gear. Flight, delayed strikes and guidance belong to a WeaponInstance, never an ID. */
class ExtendedPhysicalWeaponRuntime(private val stats: PlayerStats) : WeaponAttackExecutor {
    private val calculator = WeaponDamageCalculator(stats)
    private data class Event(var delay: Float, val ctx: WeaponAttackContext, val action: () -> Unit)
    private data class Flash(val start: Vector2, val end: Vector2, val radius: Float,
                             val cone: Boolean, var remaining: Float = .24f,
                             val sheet: String = "earthbreaker", val angle: Float = 120f,
                             val duration: Float = remaining)
    private data class FragmentRequest(val ctx: WeaponAttackContext, val enemy: Enemy, val center: Vector2)
    private data class Boulder(val ctx: WeaponAttackContext, var start: Vector2, var end: Vector2,
                              val direction: Vector2, val upgraded: Boolean, var hop: Int = 0,
                              var remaining: Float = .6f, var duration: Float = .6f,
                              var position: Vector2 = Vector2(start),
                              var velocity: Vector2 = Vector2(end).sub(start).scl(1f/duration))
    private data class Boomerang(val ctx: WeaponAttackContext, val normal: Boolean, val direction: Vector2,
                                val range: Float, val legDuration: Float, val position: Vector2,
                                var phase: Int = 0, var remaining: Float = legDuration,
                                var rearOrigin: Vector2 = Vector2(), var rotation: Float = 0f,
                                val sizeScale: Float = 1f,
                                val hitEnemies: MutableSet<Enemy> = mutableSetOf())
    private val events = mutableListOf<Event>()
    private val flashes = mutableListOf<Flash>()
    private val fragments = mutableListOf<FragmentRequest>()
    private val stones = mutableListOf<Projectile>()
    private val boulders = mutableListOf<Boulder>()
    private val boomerangs = mutableListOf<Boomerang>()
    private val guidance = mutableMapOf<String, Float>()
    private val spriteBatch = lazy { SpriteBatch() }
    private val textures = mutableMapOf<String, Texture>()
    private val aim = Vector2(1f, 0f)
    var projectileVelocityBender: ((Float,Float,Vector2,Float) -> Unit)? = null
    fun updateAim(x: Float, y: Float) { aim.set(x,y) }
    fun canHandle(weapon: WeaponInstance) = weapon.definition.id.value in
        setOf("WPN_WARHAMMER", "WPN_WHIP", "WPN_SLING", "WPN_BOOMERANG")

    private fun origin(ctx: WeaponAttackContext) = Vector2(ctx.attackOriginX, ctx.attackOriginY)
    private fun nearest(ctx: WeaponAttackContext): Enemy? {
        val range = ctx.effectiveWeaponRange(ctx.weapon.definition.baseRange * ctx.weapon.currentLevelStats.rangeMultiplier)
        return WeaponTargetSelector().selectTarget(ctx.weapon.definition.targetingType,
            ctx.player, ctx.enemyManager, range, ctx.attackOriginX, ctx.attackOriginY)
    }
    private fun direction(from: Vector2, to: Vector2): Vector2 = Vector2(to).sub(from).let {
        if (it.isZero(.001f)) it.set(1f,0f); it.nor()
    }
    override fun execute(context: WeaponAttackContext) {
        val target = nearest(context) ?: return
        val ctx = context
        if (ctx.weapon.definition.id.value == "WPN_BOOMERANG" && boomerangs.any {
                it.ctx.weapon === ctx.weapon && it.normal && it.phase <= 1 }) return
        ctx.weapon.registerNormalAttack()
        val point = Vector2(target.centerX,target.centerY)
        when (ctx.weapon.definition.id.value) {
            "WPN_WARHAMMER" -> hammer(ctx,point)
            "WPN_WHIP" -> {
                val upgraded = ctx.weapon.hasBasicAttackUpgrade
                lash(ctx,point,if(upgraded).75f else 1f)
                if(upgraded) events += Event(.15f,ctx) { lash(ctx,point,.75f) }
            }
            "WPN_SLING" -> stone(ctx,point)
            "WPN_BOOMERANG" -> launchBoomerang(ctx,point,true)
        }
    }
    private fun hit(ctx: WeaponAttackContext, enemy: Enemy, multiplier: Float, projectile: Boolean = false) {
        if(enemy.isDead) return
        val type = ctx.weapon.definition.damageTypes.first()
        val roll = calculator.calculateForHit(ctx.weapon,type,true)
        val raw = (roll.damage*multiplier).roundToInt().coerceAtLeast(1)
        val wasAlive = !enemy.isDead
        val applied = enemy.takeDamage(ctx.modifiedDamageAgainst(enemy,raw),EnemyDamageContext(
            damageType=type,projectileAttack=projectile,sourceWeaponRuntimeId=ctx.weapon.runtimeId,
            sourceWeaponDefinitionId=ctx.weapon.definition.id.value,sourceName=ctx.weapon.displayName,
            sourceWeaponTags=ctx.weapon.definition.tags,attackerPiercingIgnoreRatio=stats.piercingIgnoreRatio()))
        if(applied>0) {
            ctx.onDamageDealt(applied,ctx.weapon.displayName)
            enemy.applyKnockback(enemy.centerX-ctx.attackOriginX,enemy.centerY-ctx.attackOriginY,
                ctx.weapon.definition.baseKnockback*ctx.weapon.currentLevelStats.knockbackMultiplier)
            ctx.reportWeaponHit(enemy,applied,type,projectile)
        }
        if(wasAlive && enemy.isDead) ctx.onEnemyKilled(enemy)
    }
    private fun lineDistance(x: Float,y: Float,a: Vector2,b: Vector2): Float {
        val dx=b.x-a.x; val dy=b.y-a.y
        val t=if(dx*dx+dy*dy<.001f)0f else ((x-a.x)*dx+(y-a.y)*dy)/(dx*dx+dy*dy)
        return Vector2.dst(x,y,a.x+dx*t.coerceIn(0f,1f),a.y+dy*t.coerceIn(0f,1f))
    }
    private fun hammer(ctx: WeaponAttackContext, point: Vector2) {
        val a=origin(ctx); val radius=45f*if(ctx.weapon.hasBasicAttackUpgrade)1.5f else 1f
        flashes += Flash(a,point,radius,false,sheet="hammer_strike")
        ctx.enemyManager.weaponTargets.toList().forEach { e ->
            val body=e.bounds.width/2f
            val ratio=when {
                Vector2.dst(e.centerX,e.centerY,point.x,point.y)<=radius+body -> 1f
                lineDistance(e.centerX,e.centerY,a,point)<=7f+body -> .33f
                else -> 0f
            }
            if(ratio>0f)hit(ctx,e,ratio)
        }
    }
    private fun lash(ctx: WeaponAttackContext, center: Vector2, damage: Float) {
        val a=origin(ctx); val dir=direction(a,center)
        val coneLength=(if(ctx.weapon.hasBasicAttackUpgrade)80f else 65f)*1.25f
        val tip=Vector2(center).mulAdd(dir,coneLength*.5f)
        val apex=Vector2(center).mulAdd(dir,-coneLength*.5f)
        flashes += Flash(a,tip,coneLength,true,sheet="whip_strike",angle=30f)
        ctx.enemyManager.weaponTargets.toList().forEach { e ->
            val rel=Vector2(e.centerX-apex.x,e.centerY-apex.y)
            val forward=rel.dot(dir); val lateral=abs(rel.x*dir.y-rel.y*dir.x)
            val body=e.bounds.width*.5f
            val inCone=forward>=-body && forward<=coneLength+body &&
                lateral<=max(0f,forward)*tan(15f*MathUtils.degreesToRadians)+body
            val ratio=when { inCone -> damage; lineDistance(e.centerX,e.centerY,a,apex)<=2f+body -> damage*.33f; else -> 0f }
            if(ratio>0f)hit(ctx,e,ratio)
        }
    }
    private fun stone(ctx: WeaponAttackContext, target: Vector2, fragment: Boolean=false,
                      ignored: Set<Enemy> = emptySet()) {
        val type=DamageType.BLUDGEONING
        val base=calculator.calculateNonCriticalDamage(ctx.weapon,type)
        val range=if(fragment)80f else ctx.effectiveWeaponRange(ctx.weapon.definition.baseRange)
        val start=origin(ctx)
        val shot=Projectile(start.x,start.y,target.x,target.y,sourceWeaponName=ctx.weapon.displayName,
            sourceWeapon=ctx.weapon,sourceWeaponSlotIndex=ctx.weaponSlotIndex,
            damage=(base*if(fragment).5f else 1f).roundToInt().coerceAtLeast(1),isCriticalHit=false,
            maximumTargets=1,projectileSpeed=ctx.weapon.definition.baseProjectileSpeed,
            maximumTravelDistance=range,damageType=type,knockback=if(fragment)2f else 5f,
            sizeMultiplier=if(fragment).30f else .50f,canCriticallyHit=true,
            criticalHitChance=calculator.criticalHitChance(ctx.weapon),
            criticalDamageMultiplier=calculator.criticalDamageMultiplier(),
            attackerPiercingIgnoreRatio=stats.piercingIgnoreRatio(),customVisualId="SLING_STONE",
            ignoredEnemies=ignored,onImpact=if(!fragment&&ctx.weapon.hasBasicAttackUpgrade) { _,enemy ->
                fragments += FragmentRequest(ctx,enemy,Vector2(enemy.centerX,enemy.centerY))
            } else null)
        ctx.projectiles += shot; stones += shot
    }
    private fun launchBoomerang(ctx: WeaponAttackContext,target: Vector2,normal: Boolean) {
        val start=origin(ctx)
        boomerangs += Boomerang(ctx,normal,direction(start,target),ctx.effectiveWeaponRange(ctx.weapon.definition.baseRange),
            ctx.normalAttackIntervalSeconds.coerceAtLeast(.1f)*.5f,Vector2(start),
            sizeScale=if(normal)1f else if(ctx.weapon.hasWeaponAbilityUpgrade)1.4f else 1.25f)
    }
    fun activate(ctx: WeaponAttackContext) {
        val a=origin(ctx)
        val autoTarget = if (com.azizoglu.grisilith.settings.GameSettings.abilityCasting == com.azizoglu.grisilith.settings.GameSettings.AbilityCasting.AUTO)
            AbilityTargeting.select(ctx.enemyManager.weaponTargets,a.x,a.y,
                when(ctx.weapon.definition.id.value) { "WPN_WARHAMMER" -> 444f; "WPN_SLING" -> ctx.effectiveWeaponRange(650f)
                    else -> ctx.effectiveWeaponRange(ctx.weapon.definition.baseRange) }) else null
        val aimPoint = autoTarget?.let { Vector2(it.centerX,it.centerY) } ?: aim
        val dir=direction(a,aimPoint)
        when(ctx.weapon.unlockedAbility?.id) {
            NewPhysicalWeapons.EARTHBREAKER -> repeat(if(ctx.weapon.hasWeaponAbilityUpgrade)4 else 3) { index ->
                val center=Vector2(a).mulAdd(dir,90f+index*90f)
                events += Event(index*.18f,ctx) {
                    val radius=70f*if(ctx.weapon.hasWeaponAbilityUpgrade)1.2f else 1f
                    flashes += Flash(center,center,radius,false,.35f)
                    ctx.enemyManager.weaponTargets.toList().filter { !it.isDead && Vector2.dst(center.x,center.y,it.centerX,it.centerY)<=radius+it.bounds.width/2f }
                        .forEach { enemy ->
                            hit(ctx,enemy,if(ctx.weapon.hasWeaponAbilityUpgrade)2f else 1.5f)
                            if(enemy.type in setOf(EnemyType.MINI_BOSS,EnemyType.FINAL_BOSS)) enemy.applyMovementSlow(.90f,.4f)
                            else enemy.applyCondition(com.azizoglu.grisilith.combat.condition.ConditionDamageCalculator.createApplication(
                                com.azizoglu.grisilith.content.ConditionType.STUNNED,stats,.3f,
                                sourceWeaponRuntimeId=ctx.weapon.runtimeId,sourceName="Earthbreaker"))
                        }
                }
            }
            NewPhysicalWeapons.WHIP_SWEEP -> {
                val range=ctx.effectiveWeaponRange(ctx.weapon.definition.baseRange)
                val target=if (com.azizoglu.grisilith.settings.GameSettings.abilityCasting != com.azizoglu.grisilith.settings.GameSettings.AbilityCasting.AUTO) aim else
                    AbilityTargeting.select(ctx.enemyManager.weaponTargets,a.x,a.y,range)?.let { Vector2(it.centerX,it.centerY) } ?: aim
                val heading=direction(a,target)
                val angle=if(ctx.weapon.hasWeaponAbilityUpgrade)120f else 80f
                flashes += Flash(a,Vector2(a).mulAdd(heading,range),range,true,.35f,"whip_sweep_hd",angle)
                ctx.enemyManager.weaponTargets.toList().filter { !it.isDead }.forEach { e ->
                    val rel=Vector2(e.centerX-a.x,e.centerY-a.y)
                    if(rel.len()<=range+e.bounds.width/2f && (rel.isZero(.001f) || abs(atan2(heading.crs(rel),heading.dot(rel))*MathUtils.radiansToDegrees)<=angle*.5f + MathUtils.radiansToDegrees*asin((e.bounds.width*.5f/rel.len().coerceAtLeast(.001f)).coerceIn(0f,1f))))
                        hit(ctx,e,if(ctx.weapon.hasWeaponAbilityUpgrade)2.5f else 2f)
                }
            }
            NewPhysicalWeapons.SHATTERSTONE -> {
                val end=Vector2(a).mulAdd(dir,min(a.dst(aimPoint),ctx.effectiveWeaponRange(650f)))
                boulders += Boulder(ctx,a,end,dir,ctx.weapon.hasWeaponAbilityUpgrade)
            }
            NewPhysicalWeapons.GUIDED_HUNT -> {
                guidance[ctx.weapon.runtimeId]=if(ctx.weapon.hasWeaponAbilityUpgrade)6f else 5f
                launchBoomerang(ctx,aimPoint,false)
            }
        }
    }
    fun update(delta: Float, ownedIds: Set<String>) {
        val dt=delta.coerceAtLeast(0f)
        events.removeAll { it.ctx.weapon.runtimeId !in ownedIds }
        events.toList().forEach { it.delay-=dt; if(it.delay<=0f){ events.remove(it); it.action() } }
        flashes.forEach { it.remaining-=dt }; flashes.removeAll { it.remaining<=0f }
        fragments.toList().forEach { request ->
            if(request.ctx.weapon.runtimeId in ownedIds) {
                val count=MathUtils.random(3,8); val initial=MathUtils.random(360f)
                repeat(count) { index ->
                val dir=Vector2(1f,0f).rotateDeg(initial+index*360f/count)
                stone(request.ctx.copy(attackOriginX=request.center.x,attackOriginY=request.center.y),
                    Vector2(request.center).mulAdd(dir,80f),true,setOf(request.enemy))
                }
            }
        }; fragments.clear(); stones.removeAll { !it.isActive }
        boulders.removeAll { it.ctx.weapon.runtimeId !in ownedIds }
        boulders.toList().forEach { rock ->
            val step=min(dt,rock.remaining.coerceAtLeast(0f))
            projectileVelocityBender?.invoke(rock.position.x,rock.position.y,rock.velocity,step)
            rock.position.mulAdd(rock.velocity,step)
            rock.remaining-=dt
            if(rock.remaining<=0f) {
                val multipliers=if(rock.upgraded)floatArrayOf(3.5f,2.5f) else floatArrayOf(3f)
                val radius=75f*if(rock.upgraded)1.25f else 1f
                val impact=Vector2(rock.position)
                rock.ctx.enemyManager.weaponTargets.toList().filter { !it.isDead &&
                    Vector2.dst(impact.x,impact.y,it.centerX,it.centerY)<=radius+it.bounds.width/2f }
                    .forEach { hit(rock.ctx,it,multipliers[rock.hop],true) }
                if(!rock.upgraded || rock.hop==1)boulders.remove(rock) else {
                    rock.hop++; rock.start=Vector2(impact); rock.end=Vector2(impact).mulAdd(rock.direction,160f)
                    rock.duration=.35f; rock.remaining=.35f
                    rock.velocity=Vector2(rock.end).sub(rock.start).scl(1f/rock.duration)
                }
            }
        }
        boomerangs.removeAll { it.ctx.weapon.runtimeId !in ownedIds }
        boomerangs.toList().forEach { flight -> advanceBoomerang(flight,dt) }
        guidance.keys.toList().forEach { id ->
            val left=(guidance[id]?:0f)-dt
            if(left<=0f || id !in ownedIds) guidance.remove(id) else guidance[id]=left
        }
    }
    private fun advanceBoomerang(f: Boomerang, delta: Float) {
        var time=delta
        while(time>.000001f && f in boomerangs) {
            val step=min(time,f.remaining.coerceAtLeast(.000001f)); val before=Vector2(f.position)
            val player=Vector2(f.ctx.player.centerX,f.ctx.player.centerY)
            if(f.phase==0 || f.phase==2) {
                val guided=(guidance[f.ctx.weapon.runtimeId]?:0f)>0f
                if(guided) {
                    val guidedTarget = if (com.azizoglu.grisilith.settings.GameSettings.abilityCasting == com.azizoglu.grisilith.settings.GameSettings.AbilityCasting.AUTO)
                        AbilityTargeting.select(f.ctx.enemyManager.weaponTargets,player.x,player.y,f.range)?.let { Vector2(it.centerX,it.centerY) } else aim
                    val desired=guidedTarget?.let { direction(f.position,it) } ?: f.direction.cpy()
                    val difference=(desired.angleDeg()-f.direction.angleDeg()+540f)%360f-180f
                    f.direction.rotateDeg(difference.coerceIn(-180f*step,180f*step))
                }
                val travelRange=f.range*if(f.phase==2).5f else 1f
                f.position.mulAdd(f.direction,travelRange/f.legDuration*step)
            } else f.position.lerp(player,(step/f.remaining.coerceAtLeast(step)).coerceIn(0f,1f))
            val planned=Vector2(f.position)
            val velocity=Vector2(planned).sub(before).scl(1f/step)
            projectileVelocityBender?.invoke(before.x,before.y,velocity,step)
            f.position.set(before).mulAdd(velocity,step)
            if(f.phase==0 || f.phase==2) {
                if(!velocity.isZero(.001f)) f.direction.set(velocity).nor()
            }
            f.rotation+=720f*step
            f.ctx.enemyManager.weaponTargets.toList().filter { !it.isDead && it !in f.hitEnemies }.forEach { e ->
                if(lineDistance(e.centerX,e.centerY,before,f.position)<=18f*f.sizeScale+e.bounds.width/2f) {
                    f.hitEnemies+=e;hit(f.ctx,e,if(f.phase>=2).5f else 1f,true)
                }
            }
            f.remaining-=step;time-=step
            if(f.remaining<=.00001f) {
                if(f.phase==1 || f.phase==3) f.position.set(player)
                if(f.phase==1 && (!f.normal || !f.ctx.weapon.hasBasicAttackUpgrade) || f.phase==3) {
                    boomerangs.remove(f)
                } else {
                    f.phase++;f.remaining=f.legDuration;f.hitEnemies.clear()
                    if(f.phase==2){
                        // Continue through the catch in the direction opposite to the side of arrival.
                        if(!velocity.isZero(.001f)) f.direction.set(velocity).nor()
                        f.position.set(player);f.rearOrigin.set(player)
                    }
                }
            }
        }
    }
    fun render(renderer: ShapeRenderer) {
        if(flashes.isEmpty() && boomerangs.isEmpty() && boulders.isEmpty() && stones.isEmpty())return
        val projection=renderer.projectionMatrix.cpy(); renderer.end()
        val batch=spriteBatch.value;batch.projectionMatrix=projection;batch.begin()
        fun draw(path: String,x: Float,y: Float,size: Float,rotation: Float=0f) {
            val texture=textures.getOrPut(path){ Texture(Gdx.files.internal(path)).also { it.setFilter(Texture.TextureFilter.Linear,Texture.TextureFilter.Linear) } }
            batch.draw(texture,x-size/2,y-size/2,size/2,size/2,size,size,1f,1f,rotation,0,0,texture.width,texture.height,false,false)
        }
        flashes.forEach { effect ->
            val frame=((1f-effect.remaining/effect.duration)*16f).toInt().coerceIn(0,15)
            val highResolutionSweep = effect.sheet == "whip_sweep_hd"
            val path="weapon_visuals/physical_revision/"+effect.sheet+
                (if(highResolutionSweep) "_"+(frame/4) else "")+".png"
            val texture=textures.getOrPut(path){
                Texture(Gdx.files.internal(path)).also {
                    if(highResolutionSweep) it.setFilter(Texture.TextureFilter.Linear,Texture.TextureFilter.Linear)
                }
            }
            val columns=if(highResolutionSweep)2 else 4
            val localFrame=if(highResolutionSweep)frame%4 else frame
            val cellW=texture.width/columns; val cellH=texture.height/columns
            val region=com.badlogic.gdx.graphics.g2d.TextureRegion(texture,(localFrame%columns)*cellW,
                (localFrame/columns)*cellH,cellW,cellH)
            val heading=direction(effect.start,effect.end)
            when(effect.sheet) {
                "earthbreaker" -> batch.draw(region,effect.end.x-effect.radius,effect.end.y-effect.radius,effect.radius*2,effect.radius*2)
                "whip_sweep_hd" -> {
                    // The visible grip sits near the lower-left, not at the cell center.
                    // Normalize tip reach instead of stretching a small atlas across 2x range.
                    val size=effect.radius/.75f
                    batch.draw(region,effect.start.x-size*.18f,effect.start.y-size*.34f,size*.18f,size*.34f,size,size,1f,
                        tan(effect.angle*.5f*MathUtils.degreesToRadians)/tan(60f*MathUtils.degreesToRadians),heading.angleDeg())
                }
                else -> {
                    val length=effect.start.dst(effect.end)
                    val height=if(effect.sheet=="hammer_strike")effect.radius*2 else effect.radius*.6f
                    val anchor=if(effect.sheet=="hammer_strike").65f else .88f
                    batch.draw(region,effect.start.x,effect.start.y-height/2,0f,height/2,length/anchor,height,1f,1f,heading.angleDeg())
                }
            }
        }
        boomerangs.forEach { draw("weapon_visuals/carriers_v4/boomerang.png",it.position.x,it.position.y,42f*it.sizeScale,it.rotation) }
        stones.filter { it.isActive }.forEach { draw("weapon_visuals/carriers_v4/sling_stone.png",it.visualCenterX,it.visualCenterY,it.visualCollisionSize*1.5f) }
        boulders.forEach { b ->
            val p=(1f-b.remaining/b.duration).coerceIn(0f,1f)
            val point=Vector2(b.position)
            draw("weapon_visuals/carriers_v4/sling_stone.png",point.x,point.y+sin(p*MathUtils.PI)*150f,
                90f*(if(b.upgraded)1.25f else 1f)*(1f-b.hop*.1f),p*60f)
        }
        batch.end();renderer.begin(ShapeRenderer.ShapeType.Filled)
    }
    fun clear(){events.clear();flashes.clear();fragments.clear();stones.clear();boulders.clear();boomerangs.clear();guidance.clear()}
    fun dispose(){clear();textures.values.forEach(Texture::dispose);textures.clear();if(spriteBatch.isInitialized())spriteBatch.value.dispose()}
}
