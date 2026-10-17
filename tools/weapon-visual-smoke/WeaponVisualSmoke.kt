package qa

import com.azizoglu.grisilith.GrisilithGame
import com.azizoglu.grisilith.character.DefaultCharacters
import com.azizoglu.grisilith.config.*
import com.azizoglu.grisilith.content.DamageType
import com.azizoglu.grisilith.player.Player
import com.azizoglu.grisilith.screen.FirstScreen
import com.azizoglu.grisilith.weapon.Projectile
import com.azizoglu.grisilith.weapon.definition.DefaultWeapons
import com.azizoglu.grisilith.weapon.runtime.*
import com.azizoglu.grisilith.weapon.skill.*
import com.azizoglu.grisilith.weapon.visual.attack.*
import com.badlogic.gdx.*
import com.badlogic.gdx.backends.lwjgl3.*
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2

object WeaponVisualSmoke {
    fun field(obj: Any, name: String): Any = obj.javaClass.getDeclaredField(name).apply {
        isAccessible = true
    }.get(obj)
    fun call(obj: Any, name: String) = obj.javaClass.getDeclaredMethod(name).apply {
        isAccessible = true
    }.invoke(obj)

    @JvmStatic fun main(args: Array<String>) {
        val config = Lwjgl3ApplicationConfiguration().apply {
            setWindowedMode(1280, 1280); setInitialVisible(false); disableAudio(true)
            setPreferencesConfig("../tools/weapon-visual-smoke/prefs", Files.FileType.Local)
        }
        Lwjgl3Application(object : ApplicationAdapter() {
            override fun create() {
                try {
                    val screen = FirstScreen(GrisilithGame(), GameDifficulty.NORMAL,
                        GameMap.TEST_MAP, DefaultCharacters.SirImbert)
                    val inventory = field(screen, "weaponInventory") as WeaponInventory
                    inventory.clearAllSlots()
                    val weapons = listOf(
                        WeaponInstance(DefaultWeapons.shield, 3),
                        WeaponInstance(DefaultWeapons.findById("WPN_TOME_OF_ERYNDOR")!!, 5),
                        WeaponInstance(DefaultWeapons.shield, 5),
                        WeaponInstance(DefaultWeapons.longsword, 3)
                    )
                    weapons.forEach { check(inventory.addWeapon(it)) }
                    val manager = field(screen, "weaponRuntimeManager") as WeaponRuntimeManager
                    call(manager, "synchronizeWithInventory")
                    check(manager.abilityStateInSlot(0) == WeaponAbilityState.ACTIVE)
                    check(manager.abilityStateInSlot(1) == WeaponAbilityState.ACTIVE)
                    check(weapons[0].unlockedAbility?.activationType == WeaponAbilityActivationType.PASSIVE)
                    check(weapons[1].unlockedAbility?.activationType == WeaponAbilityActivationType.PASSIVE)
                    check(weapons[2].unlockedAbility?.activationType == WeaponAbilityActivationType.ACTIVE)
                    val camera = OrthographicCamera().apply { setToOrtho(false, 1280f, 1280f); update() }
                    val hud = field(screen, "hudRenderer")
                    val bounds = field(hud, "weaponSlotBounds") as List<Rectangle>
                    bounds.forEachIndexed { i, b -> b.set(120f + i * 180f, 300f, 110f, 110f) }
                    val batch = field(screen, "hudBatch") as SpriteBatch
                    clear()
                    batch.projectionMatrix = camera.combined
                    batch.begin()
                    call(hud, "renderThemedWeaponSlots")
                    call(hud, "renderWeaponSlotTexts")
                    batch.end()
                    capture("hud-passive-and-active")
                    val hudPixels = Pixmap.createFromFrameBuffer(0, 0, 1280, 1280)
                    fun cyanBorderPixels(b: Rectangle): Int {
                        var count = 0
                        for (x in b.x.toInt() until (b.x + b.width).toInt()) {
                            for (y in b.y.toInt() until (b.y + b.height).toInt()) {
                                if (x > b.x + 8 && x < b.x + b.width - 8 &&
                                    y > b.y + 8 && y < b.y + b.height - 8) continue
                                val color = hudPixels.getPixel(x, y)
                                val red = color ushr 24 and 255
                                val green = color ushr 16 and 255
                                val blue = color ushr 8 and 255
                                if (red < 140 && green > 130 && blue > 180) count++
                            }
                        }
                        return count
                    }
                    check(cyanBorderPixels(bounds[0]) < 5 && cyanBorderPixels(bounds[1]) < 5)
                    check(cyanBorderPixels(bounds[2]) > 20 && cyanBorderPixels(bounds[3]) > 20)
                    hudPixels.dispose()

                    val physical = field(manager, "physicalWeaponAbilityRuntime") as PhysicalWeaponAbilityRuntime
                    val projectiles = field(physical, "airSlashProjectiles") as MutableList<Projectile>
                    repeat(16) { index ->
                        val x = 160f + (index % 4) * 320f
                        val y = 1120f - (index / 4) * 320f
                        val projectile = Projectile(x - 14f, y - 14f, x + 100f, y - 14f,
                            sourceWeaponName = "Air Slash QA", damage = 1, isCriticalHit = false,
                            maximumTargets = 1, projectileSpeed = 1f, maximumTravelDistance = 100f,
                            damageType = DamageType.AIR)
                        projectile.javaClass.getDeclaredField("travelledDistance").apply {
                            isAccessible = true; setFloat(projectile, index / 16f * 100f)
                        }
                        projectiles += projectile
                    }
                    clear()
                    val shapes = ShapeRenderer()
                    shapes.projectionMatrix = camera.combined
                    shapes.begin(ShapeRenderer.ShapeType.Filled)
                    physical.render(shapes, field(screen, "player") as Player)
                    shapes.end()
                    capture("air-slash-playback")
                    val frames = physical.javaClass.getDeclaredMethod("getAirSlashFrames").apply {
                        isAccessible = true
                    }.invoke(physical) as Array<*>
                    check(frames.size == 16)
                    val pixmap = Pixmap(Gdx.files.internal(
                        "weapons_animations/weapon_skills/air_slash/air_slash_sheet_v3.png"))
                    check((pixmap.getPixel(0, 0) and 255) == 0) { "Sheet must be transparent" }
                    pixmap.dispose()

                    clear()
                    projectiles.clear()
                    val player = field(screen, "player") as Player
                    val position = field(player, "position") as Vector2
                    val halfPlayerSize = player.centerX - position.x
                    val defensive = DefensiveLineVisualRenderer()
                    position.set(300f - halfPlayerSize, 900f - halfPlayerSize)
                    shapes.begin(ShapeRenderer.ShapeType.Filled)
                    defensive.render(shapes, player, 5)
                    position.set(900f - halfPlayerSize, 900f - halfPlayerSize)
                    defensive.render(shapes, player, 10)
                    shapes.end()
                    MeleeAttackVisualManager.clear()
                    MeleeAttackVisualManager.spawn(MeleeAttackVisualKind.SHIELD_CONE,
                        300f, 350f, 0f, 80f * 1.72f, 80f * 1.12f, 0.27f)
                    MeleeAttackVisualManager.spawn(MeleeAttackVisualKind.SHIELD_CONE,
                        900f, 350f, 0f, 96f * 1.72f, 96f * 1.32f, 0.27f)
                    MeleeAttackVisualManager.update(0.12f)
                    val melee = MeleeAttackVisualRenderer()
                    melee.render(camera)
                    capture("shield-effects")
                    melee.dispose()
                    MeleeAttackVisualManager.clear()
                    shapes.dispose()
                    screen.dispose()
                    println("WEAPON_VISUAL_SMOKE_PASS: passive/active states, HUD rendering, transparent 16-frame Air Slash playback")
                } catch (t: Throwable) {
                    t.printStackTrace(); kotlin.system.exitProcess(1)
                }
                Gdx.app.exit()
            }
            fun clear() {
                Gdx.gl.glClearColor(0.10f, 0.10f, 0.12f, 1f)
                Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
            }
            fun capture(name: String) {
                val pixels = Pixmap.createFromFrameBuffer(0, 0, 1280, 1280)
                PixmapIO.writePNG(FileHandle("../tools/weapon-visual-smoke/$name.png"), pixels, -1, true)
                pixels.dispose()
            }
        }, config)
    }
}
