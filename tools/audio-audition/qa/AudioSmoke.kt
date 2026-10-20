package qa

import com.azizoglu.grisilith.audio.*
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import java.lang.reflect.Proxy
import java.io.File
import java.security.MessageDigest
import javax.sound.sampled.AudioSystem
import javazoom.jl.decoder.Bitstream
import javazoom.jl.decoder.MP3Decoder
import javazoom.jl.decoder.OutputBuffer
import javazoom.jl.decoder.Header

/** No speakers, network, preferences or game saves are touched. */
object AudioSmoke {
    class FakeMusic {
        var playing=false;var volume=0f;var plays=0;var stops=0;var disposed=0;var looping=false
        val api=Proxy.newProxyInstance(Music::class.java.classLoader,arrayOf(Music::class.java)) { _, method, args ->
            when(method.name) {
                "play" -> {playing=true;plays++;null}
                "pause" -> {playing=false;null}
                "stop" -> {playing=false;stops++;null}
                "isPlaying" -> playing
                "setVolume" -> {volume=args[0] as Float;null}
                "getVolume" -> volume
                "setLooping" -> {looping=args[0] as Boolean;null}
                "isLooping" -> looping
                "dispose" -> {disposed++;null}
                else -> null
            }
        } as Music
    }
    class FakeSound {
        val played=mutableListOf<Float>();val volumes=mutableListOf<Float>();val pitches=mutableListOf<Float>()
        val pans=mutableListOf<Float>()
        var stops=0;var disposed=0;var pauses=0;var resumes=0
        val api=Proxy.newProxyInstance(Sound::class.java.classLoader,arrayOf(Sound::class.java)) { _, method, args ->
            when(method.name) {
                "play", "loop" -> {played+=args[0] as Float;pitches+=if(args.size>1)args[1] as Float else 1f;pans+=if(args.size>2)args[2] as Float else 0f;played.size.toLong()}
                "setPan" -> {pans+=args[1] as Float;volumes+=args[2] as Float;null}
                "setPitch" -> {pitches+=args[1] as Float;null}
                "pause" -> {pauses++;null}
                "resume" -> {resumes++;null}
                "setVolume" -> {volumes+=args[1] as Float;null}
                "stop" -> {stops++;null}
                "dispose" -> {disposed++;null}
                else -> null
            }
        } as Sound
    }
    private fun near(actual:Float,expected:Float) {check(kotlin.math.abs(actual-expected)<.0001f){"$actual != $expected"}}
    @JvmStatic fun main(args:Array<String>) {
        listOf("BACK","CANCEL","RETURN TO GAME","UNDO CHANGES","KEEP EDITING").forEach {
            check(GameAudio.button(it)==UiSound.CLICK)
        }
        var now=1000L;var levels=AudioLevels(.5f,.8f,.6f)
        val music=FakeMusic();val sounds=UiSound.entries.associateWith{FakeSound()}
        var loads=0
        val audio=AudioController({loads++;music.api},{sounds.getValue(it).api},{levels},{now})
        audio.setMenuContext(true);check(music.plays==1 && music.looping);near(music.volume,.18f)
        repeat(10){audio.setMenuContext(true);audio.update()};check(loads==1 && music.plays==1)
        audio.request(UiSound.CLICK);audio.request(UiSound.CONFIRM);audio.request(UiSound.ERROR);audio.update()
        check(sounds.getValue(UiSound.CLICK).played.isEmpty());near(sounds.getValue(UiSound.ERROR).played.single(),.3f)
        now+=10;audio.request(UiSound.CLICK);audio.update();check(sounds.getValue(UiSound.CLICK).played.isEmpty())
        val owner=Any();audio.preview(owner,AudioLevels(.25f,.4f,.2f));near(music.volume,.045f)
        near(sounds.getValue(UiSound.ERROR).volumes.last(),.05f)
        audio.clearPreview(Any());near(music.volume,.045f)
        audio.clearPreview(owner);near(music.volume,.18f)
        audio.preview(owner,AudioLevels(0f,1f,1f));check(!music.playing && sounds.getValue(UiSound.ERROR).stops==1)
        now+=100;audio.request(UiSound.CHEST);audio.update();check(sounds.getValue(UiSound.CHEST).played.isEmpty())
        audio.clearPreview(owner);check(music.playing)
        audio.setMenuContext(false);check(!music.playing && music.stops==1)
        audio.update();check(!music.playing)
        audio.setMenuContext(true);check(music.playing)
        audio.pause();check(!music.playing);audio.resume();check(music.playing)
        // Limit overlapping voices to three, even with rapid but permitted input.
        repeat(4){now+=81;audio.request(UiSound.PAGE);audio.update()}
        check(sounds.getValue(UiSound.PAGE).played.size==4 && sounds.getValue(UiSound.PAGE).stops==1)
        levels=AudioLevels(1f,0f,0f);audio.update();check(!music.playing)
        val before=sounds.values.sumOf{it.played.size}
        audio.request(UiSound.CLICK);audio.update();levels=AudioLevels(1f,1f,1f);audio.update()
        check(before==sounds.values.sumOf{it.played.size})
        audio.dispose();audio.dispose();audio.pause();audio.resume();audio.setMenuContext(true);audio.request(UiSound.CLICK);audio.update()
        check(music.disposed==1 && sounds.values.all{it.disposed<=1})
        // Distinct weapons may attack in one frame: rotate audible choices, never starve a later slot.
        val combatSounds=UiSound.entries.associateWith{FakeSound()}
        val combatAudio=AudioController({FakeMusic().api},{combatSounds.getValue(it).api},{AudioLevels(1f,1f,1f)},{now})
        val kinds=listOf(UiSound.LONGSWORD_SWING,UiSound.DOUBLE_SWORDS_SWING,UiSound.LANCE_THRUST,UiSound.SHIELD_STRIKE)
        repeat(2) {now+=1000;kinds.forEach(combatAudio::request);combatAudio.update()}
        check(kinds.all{combatSounds.getValue(it).played.size==1})
        combatAudio.request(UiSound.WHIP_LASH);combatAudio.pause();combatAudio.resume();combatAudio.update()
        check(combatSounds.getValue(UiSound.WHIP_LASH).played.isEmpty())
        combatAudio.setMenuContext(true);combatAudio.request(UiSound.WHIP_LASH);combatAudio.update()
        check(combatSounds.getValue(UiSound.WHIP_LASH).played.isEmpty())
        combatAudio.dispose()
        for(kind in UiSound.entries) {
            val file=File("audio/sfx/${kind.path}");check(file.isFile)
            // New licensed records keep their original OGG/MP3 bytes. Their full decode/hash
            // validation is in the Python installer/QA; Java Sound only decodes our PCM WAVs.
            if(file.extension!="wav") {check(file.length()>100);continue}
            AudioSystem.getAudioInputStream(file).use {stream ->
                check(stream.format.sampleRate==44100f && stream.frameLength>0)
                check(stream.readAllBytes().any{it.toInt()!=0})
            }
        }
        val previews=mapOf("ui_click.wav" to "A1", "ui_confirm.wav" to "B5", "ui_back.wav" to "C2", "ui_error.wav" to "D2", "page_turn.wav" to "E1", "chest_open.wav" to "F1")
        for((name,id) in previews) {
            val original=File("../tools/audio-audition/listening-pack-01/$id.wav")
            val digest=MessageDigest.getInstance("SHA-256")
            check(digest.digest(original.readBytes()).contentEquals(digest.digest(File("audio/sfx/$name").readBytes())))
        }
        check(File("audio/music/gregorian_chant.mp3").length()>1_000_000)
        val fitted=listOf(Triple("longsword_swing","02/G1",250),Triple("double_swords_swing","03/DS1",240),
            Triple("double_swords_upgraded","03/DS1-upgraded",240),Triple("lance_thrust","03/LN1",240),
            Triple("shield_strike","03/SH1",270),Triple("whip_lash","03/WP5",240),
            Triple("warhammer_impact","05/TH3",240),Triple("air_slash","07/AW5",673),Triple("air_slash_upgraded","07/AW5",865))
        fitted.forEach{(name,id,ms)->
            val expected=InstallMagicNormalAudio.fit(InstallMagicNormalAudio.read(File("../tools/audio-audition/listening-pack-$id.wav")),ms)
            val actual=InstallMagicNormalAudio.read(File("audio/sfx/$name.wav"))
            check(expected.size==actual.size && expected.indices.all{kotlin.math.abs(expected[it]-actual[it])<.00004f})
        }
        mapOf("bow_shot.wav" to "04/BW3", "crossbow_shot.wav" to "04/CB4", "dagger_throw.wav" to "04/DG4",
            "sling_throw.wav" to "04/SL1", "boomerang_throw.wav" to "08/GH3",
            "bow_volley.wav" to "08/BV2", "sling_impact.wav" to "08/ST1").forEach{(asset,id)->
            check(File("audio/sfx/$asset").readBytes().contentEquals(File("../tools/audio-audition/listening-pack-${id}.wav").readBytes()))
        }
        check(File("audio/sfx").listFiles()!!.none{it.name.startsWith("rapid_strokes_")})
        // Decode the entire stream with the desktop backend's MP3 decoder, without playback.
        check(File("audio/sfx/defensive_burst.wav").readBytes().contentEquals(File("../tools/audio-audition/listening-pack-06/SB5.wav").readBytes()))
        val sweepHalf=InstallMagicNormalAudio.fit(InstallMagicNormalAudio.read(File("../tools/audio-audition/listening-pack-06/WS2.wav")),175)
        val sweep=InstallMagicNormalAudio.read(File("audio/sfx/whip_sweep.wav"))
        check(sweep.size==sweepHalf.size*2 && sweep.indices.all{kotlin.math.abs(sweep[it]-sweepHalf[it%sweepHalf.size])<.00004f})
        mapOf("air" to "09/AI3","water" to "10/WV4","fire" to "10/FL5","acid" to "09/AC2",
            "lightning" to "09/LI3","necrotic" to "10/CU4","poison" to "09/PO3").forEach{(name,id)->
            check(File("audio/sfx/magic_$name.wav").readBytes().contentEquals(File("../tools/audio-audition/listening-pack-$id.wav").readBytes()))
        }
        // Game speed changes existing and new combat voices; pause freezes remaining simulation time.
        val timed=FakeSound();var time=1000L
        val timedAudio=AudioController({FakeMusic().api},{timed.api},{AudioLevels(1f,1f,1f)},{time})
        timedAudio.combatPlayback(.5f,false);timedAudio.request(UiSound.LONGSWORD_SWING);timedAudio.update()
        check(timed.pitches.last()==.5f)
        time+=400;timedAudio.update();check(timed.stops==0)
        timedAudio.combatPlayback(.5f,true);time+=2000;timedAudio.update();check(timed.stops==0&&timed.pauses==1)
        timedAudio.combatPlayback(2f,false);check(timed.resumes==1&&timed.pitches.last()==2f)
        time+=24;timedAudio.update();check(timed.stops==0)
        time+=2;timedAudio.update();check(timed.stops==1)
        time+=100;timedAudio.request(UiSound.CLICK);timedAudio.update();check(timed.pitches.last()==1f)
        timedAudio.dispose()
        for(kind in UiSound.entries.filter{it.priority==6}) {
            AudioSystem.getAudioInputStream(File("audio/sfx/${kind.path}")).use {
                val length=it.frameLength*1000L/it.format.sampleRate.toLong()
                check(kotlin.math.abs(kind.durationMs-length)<=120){"Incorrect audio lifetime: $kind $length"}
            }
        }
        // Decode the entire stream with the desktop backend's MP3 decoder, without playback.
        val bits=Bitstream(File("audio/music/gregorian_chant.mp3").inputStream())
        val decoder=MP3Decoder();var seconds=0.0;var nonSilent=false
        var output:OutputBuffer?=null
        try {
            while(true) {
                val header=bits.readFrame()?:break
                val channels=if(header.mode()==Header.SINGLE_CHANNEL)1 else 2
                if(output==null) {output=OutputBuffer(channels,false);decoder.setOutputBuffer(output)}
                val pcm=decoder.decodeFrame(header,bits)
                val size=pcm.reset()
                seconds+=size.toDouble()/2/channels/header.frequency()
                if(!nonSilent) nonSilent=pcm.buffer.take(size).any{it.toInt()!=0}
                bits.closeFrame()
            }
        } finally {bits.close()}
        check(seconds in 190.0..200.0 && nonSilent){"Invalid music stream: $seconds seconds"}
        check(File("audio/CREDITS.txt").readText().contains("creativecommons.org/licenses/by/4.0/"))
        println("AUDIO_SMOKE_PASS: mixer, mute, preview/revert, menu continuity, lifecycle, priorities, voice limit, exact selected assets; music decoded ${seconds.toInt()} seconds")
    }
}
