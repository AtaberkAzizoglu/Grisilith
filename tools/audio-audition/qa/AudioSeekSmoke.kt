package qa

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Files
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.badlogic.gdx.backends.lwjgl3.audio.OpenALLwjgl3Audio
import com.azizoglu.grisilith.lwjgl3.DesktopAudioSeeking
import org.lwjgl.openal.AL10
import org.lwjgl.openal.AL11
import java.io.File

/** Hidden native test, zero volume throughout: verify the shipping OpenAL seek adapter. */
object AudioSeekSmoke {
    @JvmStatic fun main(args:Array<String>) {
        Lwjgl3Application(object:ApplicationAdapter(){override fun create(){
            try {
                val sound=Gdx.audio.newSound(Gdx.files.absolute(File("assets/audio/sfx/focus_vortex.wav").absolutePath))
                try {
                    val id=sound.loop(0f,1f,0f)
                    val source=(Gdx.audio as OpenALLwjgl3Audio).getSourceId(id)
                    check(source>=0){"No native audio source/device available"}
                    check(DesktopAudioSeeking.seek(sound,id,.7f))
                    val position=AL10.alGetSourcef(source,AL11.AL_SEC_OFFSET)
                    check(kotlin.math.abs(position-.7f)<.1f){"Wrong native seek position: $position"}
                    check(AL10.alGetSourcef(source,AL10.AL_GAIN)==0f){"Test must remain silent"}
                    println("AUDIO_SEEK_SMOKE_PASS: shipping OpenAL adapter resumed at ${position}s, zero volume.")
                }finally{sound.dispose()}
                Gdx.app.exit()
            }catch(t:Throwable){t.printStackTrace();kotlin.system.exitProcess(1)}
        }},Lwjgl3ApplicationConfiguration().apply{
            setWindowedMode(320,240);setInitialVisible(false)
            setPreferencesConfig("tools/audio-audition/test-prefs",Files.FileType.Local)
        })
    }
}
