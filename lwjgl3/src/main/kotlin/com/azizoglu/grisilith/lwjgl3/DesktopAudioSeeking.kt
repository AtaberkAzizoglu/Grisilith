package com.azizoglu.grisilith.lwjgl3

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.backends.lwjgl3.audio.OpenALLwjgl3Audio
import com.badlogic.gdx.backends.lwjgl3.audio.OpenALSound
import org.lwjgl.openal.AL10
import org.lwjgl.openal.AL11

/** Restore a virtual sound's timeline only. This does not enable stereo/directional audio. */
object DesktopAudioSeeking {
    fun seek(sound:Sound,id:Long,seconds:Float):Boolean {
        if(!seconds.isFinite() || seconds<0f)return false
        val audio=Gdx.audio as? OpenALLwjgl3Audio?:return false
        val source=audio.getSourceId(id)
        if(sound !is OpenALSound || source<0)return false
        AL10.alSourcef(source,AL11.AL_SEC_OFFSET,seconds)
        return AL10.alGetError()==AL10.AL_NO_ERROR
    }
}
