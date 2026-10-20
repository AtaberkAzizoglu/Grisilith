package qa

import com.azizoglu.grisilith.audio.*
import com.azizoglu.grisilith.config.GameMap
import com.azizoglu.grisilith.stage.StageType
import com.badlogic.gdx.utils.JsonReader
import java.io.File
import java.security.MessageDigest
import kotlin.math.abs

object RunMusicSmoke {
    private fun near(a:Float,b:Float){check(abs(a-b)<.0001f){"$a != $b"}}
    @JvmStatic fun main(args:Array<String>) {
        val rows=JsonReader().parse(File("audio/RUN-MUSIC-SELECTIONS.json").readText())
        check(rows.size==10)
        val selected=MusicTrack.entries.filter{it!=MusicTrack.MENU && it.looping}
        selected.forEach { track ->
            val row=rows.single{it.getString("code")==track.selection}
            check(row.getString("installed")==track.path && row.getString("title")==track.title)
            val bytes=File("audio/music/${track.path}").readBytes()
            val original=File("../tools/audio-audition/listening-pack-28/${track.selection}.mp3").readBytes()
            check(bytes.contentEquals(original))
            check(MessageDigest.getInstance("SHA-256").digest(bytes).joinToString(""){"%02x".format(it)}==row.getString("sha256"))
            check(File("audio/RUN-MUSIC-LICENSES.txt").readText().contains(track.title))
        }
        val maps=listOf(GameMap.ROAD_TO_HOME,GameMap.TROUBLE_AT_HOME,GameMap.FOLLOWING_THE_DUNGEON_DELVER)
        val expected=listOf(listOf("CF2","CF5","VS9"),listOf("VS1","VS2","VS6"),listOf("HD1","HD8","HD10"))
        maps.forEachIndexed { i,map->StageType.entries.forEachIndexed { j,stage->
            check(RunMusic.select(map,stage)?.selection==expected[i][j])
            check(RunMusic.select(map,stage,blocked=true)==null)
            check(RunMusic.select(map,stage,market=true)==MusicTrack.MARKET)
            check(RunMusic.select(map,stage,blocked=true,market=true)==null)
        }}
        check(RunMusic.select(GameMap.TEST_MAP,StageType.NORMAL)==null)
        var levels=AudioLevels(.5f,.8f,.2f)
        val all=mutableListOf<Pair<MusicTrack,AudioSmoke.FakeMusic>>()
        fun load(track:MusicTrack)=AudioSmoke.FakeMusic().also{all+=track to it}.api
        val audio=AudioController({load(MusicTrack.MENU)},{AudioSmoke.FakeSound().api},{levels},
            loadRunMusic={load(it)})
        audio.setMenuContext(true);check(all.size==1)
        audio.runMusic(MusicTrack.FOREST_NORMAL);check(all.size==1) // Menu has priority.
        audio.setMenuContext(false)
        val forest=all.last().second
        check(all.last().first==MusicTrack.FOREST_NORMAL && forest.looping && forest.playing)
        near(forest.volume,.108f)
        repeat(5){audio.runMusic(MusicTrack.FOREST_NORMAL);audio.update()}
        check(forest.plays==1 && all.size==2)
        audio.runMusic(MusicTrack.FOREST_NORMAL,.65f);near(forest.volume,.108f*.65f)
        repeat(3){audio.update()};check(forest.plays==1 && all.size==2)
        audio.runMusic(MusicTrack.FOREST_NORMAL);near(forest.volume,.108f)
        audio.combatPlayback(2f,true);audio.update();check(forest.playing && forest.plays==1)
        audio.combatPlayback(.5f,false);audio.update();check(forest.playing && forest.plays==1)
        levels=levels.copy(effects=0f);audio.update();check(forest.playing);near(forest.volume,.108f)
        val preview=Any();audio.preview(preview,AudioLevels(.2f,.3f,0f));near(forest.volume,.0162f)
        audio.clearPreview(preview);near(forest.volume,.108f)
        audio.runMusic(null);check(!forest.playing && forest.stops==0)
        audio.runMusic(MusicTrack.FOREST_NORMAL);check(forest.plays==2 && all.size==2)
        audio.pause();check(!forest.playing);audio.resume();check(forest.playing && all.size==2)
        audio.runMusic(MusicTrack.FOREST_MINI);check(forest.stops==1 && forest.disposed==1)
        selected.filter{it!=MusicTrack.FOREST_MINI}.forEach { track->
            audio.runMusic(track);check(all.last().first==track)
            check(all.count{it.second.playing}==1)
        }
        val beforeMute=all.last().second
        levels=levels.copy(music=0f);audio.update();check(!beforeMute.playing)
        levels=levels.copy(music=.8f);audio.update();check(beforeMute.playing)
        audio.runMusic(null);check(all.none{it.second.playing})
        listOf(MusicTrack.SUMMARY_DEFEAT,MusicTrack.SUMMARY_VICTORY).forEach { track ->
            audio.runMusic(track);val once=all.last().second
            check(once.playing && !once.looping)
            audio.pause();audio.resume();check(once.playing)
            val starts=once.plays;once.playing=false;repeat(4){audio.update()}
            check(once.plays==starts) // A completed short fanfare must not restart every frame.
        }
        audio.setMenuContext(true);check(all.last().first==MusicTrack.MENU && all.count{it.second.playing}==1)
        audio.dispose();check(all.all{it.second.disposed==1})
        println("RUN_MUSIC_SMOKE_PASS: ten exact run assets, 9 routes, lowered map mix, market, exclusions, single-stream disposal, Pause/speed, Settings, both Summary tracks play once without frame restarts")
    }
}
