package qa

import com.azizoglu.grisilith.character.DefaultCharacters
import com.azizoglu.grisilith.character.visual.CharacterSpriteRenderer
import com.azizoglu.grisilith.character.visual.CharacterVisualDirection
import com.azizoglu.grisilith.player.Player
import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.weapon.definition.DefaultWeapons
import com.azizoglu.grisilith.weapon.runtime.WeaponInstance
import com.azizoglu.grisilith.weapon.visual.carrier.WeaponCarrierRenderer
import com.azizoglu.grisilith.weapon.visual.carrier.WeaponCarrierVisualSystem.CarrierSnapshot
import com.badlogic.gdx.*
import com.badlogic.gdx.backends.lwjgl3.*
import com.badlogic.gdx.graphics.*

/** Detect shared GL-state regressions without changing production art. */
object CharacterRenderingSmoke {
    @JvmStatic fun main(args: Array<String>) {
        val config=Lwjgl3ApplicationConfiguration().apply {
            setWindowedMode(1920,1080);setInitialVisible(false);disableAudio(true)
            setPreferencesConfig("../tools/gear-v04/qa-prefs",Files.FileType.Local)
        }
        Lwjgl3Application(object: ApplicationAdapter() {
            override fun create() {
                try {
                    val camera=OrthographicCamera(1280f,720f).apply {position.set(640f,360f,0f);update()}
                    val player=Player(300f,300f,3000f,3000f,PlayerStats())
                    val characters=CharacterSpriteRenderer()
                    val weapons=WeaponCarrierRenderer()
                    fun clear() {
                        Gdx.gl.glViewport(0,0,1920,1080)
                        Gdx.gl.glClearColor(.2f,.35f,.1f,1f)
                        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
                    }
                    fun pixels(): IntArray {
                        val p=Pixmap.createFromFrameBuffer(0,0,1920,1080)
                        val result=IntArray(1920*1080) {p.getPixel(it%1920,it/1920)}
                        p.dispose();return result
                    }
                    var comparisons=0
                    for(character in DefaultCharacters.all) for(direction in CharacterVisualDirection.entries) {
                        val movement=when(direction) {
                            CharacterVisualDirection.UP -> 0f to 1f
                            CharacterVisualDirection.DOWN -> 0f to -1f
                            CharacterVisualDirection.LEFT -> -1f to 0f
                            CharacterVisualDirection.RIGHT -> 1f to 0f
                        }
                        player.currentVisualState.update(.12f,movement.first,movement.second)
                        clear();characters.render(player,character.id,camera)
                        val before=pixels()
                        // Exercise every new carrier texture and rotation before drawing the character.
                        for(weapon in DefaultWeapons.all) weapons.renderPlayerCarrier(CarrierSnapshot(
                            0,WeaponInstance(weapon,5),-500f,-500f,direction,null,null,false,0f,0f,false,0f,0f),
                            camera,{})
                        clear();characters.render(player,character.id,camera)
                        check(before.contentEquals(pixels())) {"Carrier changed character pixels: "+character.displayName}
                        comparisons++
                    }
                    clear()
                    DefaultCharacters.all.forEachIndexed { index, character ->
                        val carried=listOf(DefaultWeapons.doubleSwords,DefaultWeapons.warhammer,DefaultWeapons.steamWand,
                            DefaultWeapons.lance,DefaultWeapons.boomerang)[index]
                        for((row,direction) in CharacterVisualDirection.entries.withIndex()) {
                            val person=Player(100f+index*220f,540f-row*125f,3000f,3000f,PlayerStats())
                            val move=when(direction) {
                                CharacterVisualDirection.UP -> 0f to 1f
                                CharacterVisualDirection.DOWN -> 0f to -1f
                                CharacterVisualDirection.LEFT -> -1f to 0f
                                CharacterVisualDirection.RIGHT -> 1f to 0f
                            }
                            person.currentVisualState.update(.12f,move.first,move.second)
                            weapons.renderPlayerCarrier(CarrierSnapshot(0,WeaponInstance(carried,5),
                                person.centerX,person.centerY,direction,null,null,false,0f,0f,false,0f,0f),camera,
                                {characters.render(person,character.id,camera)})
                            val type=com.azizoglu.grisilith.weapon.visual.carrier.WeaponCarrierVisualSystem.MinionType.entries[index]
                            val smallDirection=com.azizoglu.grisilith.weapon.visual.carrier.WeaponCarrierVisualSystem.MinionVisualDirection.SOUTH
                            weapons.renderMinionCarrier(CarrierSnapshot(1,WeaponInstance(carried,5),
                                person.centerX+80f,person.centerY,direction,type,smallDirection,true,.13f,0f,false,0f,0f),camera)
                        }
                    }
                    val preview=Pixmap.createFromFrameBuffer(0,0,1920,1080)
                    com.badlogic.gdx.graphics.PixmapIO.writePNG(Gdx.files.local("../tools/character_visuals/aligned-preview.png"),preview,-1,true)
                    preview.dispose()
                    characters.dispose();weapons.dispose()
                    println("CHARACTER_RENDERING_PASS: "+comparisons+" exact pixel comparisons; every carrier, all five characters, four directions; no shared-state degradation.")
                } catch(t: Throwable) {t.printStackTrace();kotlin.system.exitProcess(1)}
                Gdx.app.exit()
            }
        },config)
    }
}
