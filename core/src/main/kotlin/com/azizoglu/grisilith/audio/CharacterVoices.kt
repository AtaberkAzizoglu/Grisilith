package com.azizoglu.grisilith.audio

import com.azizoglu.grisilith.content.ContentId
import com.badlogic.gdx.math.MathUtils

data class CharacterVoiceSet(val selection: UiSound, val hurt: List<UiSound>, val death: UiSound)

/** Only the user's explicitly approved pack27 cuts; no synthesized fallback. */
object CharacterVoices {
    val characters: Map<String, CharacterVoiceSet> = mapOf(
        "CHR_SIR_IMBERT" to CharacterVoiceSet(UiSound.IMBERT_SELECT, listOf(UiSound.IMBERT_HURT_1, UiSound.IMBERT_HURT_2, UiSound.IMBERT_HURT_3, UiSound.IMBERT_HURT_4, UiSound.IMBERT_HURT_5), UiSound.IMBERT_DEATH),
        "CHR_YWALETH_YOSTER" to CharacterVoiceSet(UiSound.YWALETH_SELECT, listOf(UiSound.YWALETH_HURT_1, UiSound.YWALETH_HURT_2, UiSound.YWALETH_HURT_3), UiSound.YWALETH_DEATH),
        "CHR_LINLOS_DUSTMAN" to CharacterVoiceSet(UiSound.LINLOS_SELECT, listOf(UiSound.LINLOS_HURT_1, UiSound.LINLOS_HURT_2, UiSound.LINLOS_HURT_3, UiSound.LINLOS_HURT_4, UiSound.LINLOS_HURT_5), UiSound.LINLOS_DEATH),
        "CHR_KOBA" to CharacterVoiceSet(UiSound.KOBA_SELECT, listOf(UiSound.KOBA_HURT_1, UiSound.KOBA_HURT_2, UiSound.KOBA_HURT_3, UiSound.KOBA_HURT_4, UiSound.KOBA_HURT_5), UiSound.KOBA_DEATH),
        "CHR_LAVENA_VOL" to CharacterVoiceSet(UiSound.LAVENA_SELECT, listOf(UiSound.LAVENA_HURT_1, UiSound.LAVENA_HURT_2, UiSound.LAVENA_HURT_3), UiSound.LAVENA_DEATH)
    )
    val witchHurt = listOf(UiSound.WITCH_HURT_1, UiSound.WITCH_HURT_2, UiSound.WITCH_HURT_3, UiSound.WITCH_HURT_4, UiSound.WITCH_HURT_5)
    val witchQuotes = listOf(UiSound.WITCH_QUOTE_1, UiSound.WITCH_QUOTE_2, UiSound.WITCH_QUOTE_3, UiSound.WITCH_QUOTE_4, UiSound.WITCH_QUOTE_5, UiSound.WITCH_QUOTE_6)
    val vampireHurt = listOf(UiSound.VAMPIRE_HURT, UiSound.VAMPIRE_HURT_2, UiSound.VAMPIRE_HURT_3, UiSound.VAMPIRE_HURT_4)
    val vampireQuotes = listOf(UiSound.VAMPIRE_QUOTE_1, UiSound.VAMPIRE_QUOTE_2, UiSound.VAMPIRE_QUOTE_3, UiSound.VAMPIRE_QUOTE_4, UiSound.VAMPIRE_QUOTE_5)
    val selectionCues = characters.values.map { it.selection }.toSet()
    fun pick(pool: List<UiSound>): UiSound = pool[MathUtils.random(pool.lastIndex)]
    fun select(id: ContentId) {
        val voice = characters[id.value] ?: return
        stopSelection()
        GameAudio.play(voice.selection)
    }
    fun stopSelection() = GameAudio.stopVoices(selectionCues)
}

/** Per-player cooldown; immunity, healing and lethal hits never produce hurt grunts. */
class PlayerVoiceRuntime(
    characterId: ContentId?,
    private val choose: (Int) -> Int = { MathUtils.random(it - 1) }
) {
    private val voice = characterId?.value?.let(CharacterVoices.characters::get)
    private val hurtOwner = Any()
    private var hurtCue: UiSound? = null
    private var hurtRemaining = 0f
    private var hurtCooldown = 0f
    private var deathPlayed = false

    fun damaged(applied: Int, dead: Boolean, x: Float, y: Float) {
        val set = voice ?: return
        if (applied <= 0 || deathPlayed) return
        if (dead) {
            deathPlayed = true
            clear()
            GameAudio.playAt(set.death, x, y)
        } else if (hurtCooldown <= 0f) {
            val cue = set.hurt[choose(set.hurt.size).coerceIn(set.hurt.indices)]
            hurtCue = cue
            hurtRemaining = cue.durationMs / 1000f
            hurtCooldown = maxOf(.8f, hurtRemaining + .1f)
            GameAudio.stopSustain(hurtOwner)
            GameAudio.sustain(hurtOwner, cue, false, AudioSource(x, y))
        }
    }
    fun update(delta: Float, x: Float, y: Float) {
        val dt = delta.takeIf { it.isFinite() }?.coerceAtLeast(0f) ?: 0f
        hurtCooldown = (hurtCooldown - dt).coerceAtLeast(0f)
        hurtRemaining -= dt
        if (hurtRemaining > 0f) hurtCue?.let {
            GameAudio.sustain(hurtOwner, it, false, AudioSource(x, y))
        } else {
            GameAudio.stopSustain(hurtOwner)
            hurtCue = null
        }
    }
    fun clear() {
        GameAudio.stopSustain(hurtOwner)
        hurtCue = null
        hurtRemaining = 0f
    }
}
