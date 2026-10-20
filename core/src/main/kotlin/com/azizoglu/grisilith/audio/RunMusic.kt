package com.azizoglu.grisilith.audio

import com.azizoglu.grisilith.config.GameMap
import com.azizoglu.grisilith.stage.StageType

/** Streamed music, never a Sound effect: independent of combat speed and SFX volume. */
enum class MusicTrack(val path:String,val title:String,val selection:String,val looping:Boolean=true) {
    MENU("gregorian_chant.mp3","Gregorian Chant","MENU"),
    FOREST_NORMAL("forest_normal.mp3","Ascending the Vale","CF2"),
    FOREST_MINI("forest_mini.mp3","Lord of the Land","CF5"),
    FOREST_FINAL("forest_final.mp3","Five Armies","VS9"),
    SIEGE_NORMAL("siege_normal.mp3","Crusade","VS1"),
    SIEGE_MINI("siege_mini.mp3","The Descent","VS2"),
    SIEGE_FINAL("siege_final.mp3","Hero Down","VS6"),
    DEPTHS_NORMAL("depths_normal.mp3","Ossuary 1 - A Beginning","HD1"),
    DEPTHS_MINI("depths_mini.mp3","Darkest Child","HD8"),
    DEPTHS_FINAL("depths_final.mp3","Decay","HD10"),
    MARKET("market.mp3","Teller of the Tales","MK2"),
    SUMMARY_DEFEAT("summary_defeat.mp3","Dark Times","DF3",false),
    SUMMARY_VICTORY("summary_victory.mp3","Take a Chance","VC17",false);

    val baseGain:Float get()=when(this) {
        MENU,MARKET,SUMMARY_DEFEAT,SUMMARY_VICTORY -> .45f
        else -> .27f
    }
}

object RunMusic {
    fun select(map:GameMap,stage:StageType,blocked:Boolean=false,market:Boolean=false):MusicTrack? {
        if(blocked)return null
        if(market)return MusicTrack.MARKET
        return when(map) {
            GameMap.ROAD_TO_HOME -> when(stage) {
                StageType.NORMAL -> MusicTrack.FOREST_NORMAL
                StageType.MINI_BOSS -> MusicTrack.FOREST_MINI
                StageType.FINAL_BOSS -> MusicTrack.FOREST_FINAL
            }
            GameMap.TROUBLE_AT_HOME -> when(stage) {
                StageType.NORMAL -> MusicTrack.SIEGE_NORMAL
                StageType.MINI_BOSS -> MusicTrack.SIEGE_MINI
                StageType.FINAL_BOSS -> MusicTrack.SIEGE_FINAL
            }
            GameMap.FOLLOWING_THE_DUNGEON_DELVER -> when(stage) {
                StageType.NORMAL -> MusicTrack.DEPTHS_NORMAL
                StageType.MINI_BOSS -> MusicTrack.DEPTHS_MINI
                StageType.FINAL_BOSS -> MusicTrack.DEPTHS_FINAL
            }
            GameMap.TEST_MAP -> null // No map soundtrack was selected for the sandbox.
        }
    }
}
