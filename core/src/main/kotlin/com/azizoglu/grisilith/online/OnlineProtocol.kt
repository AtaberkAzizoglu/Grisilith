package com.azizoglu.grisilith.online

import com.azizoglu.grisilith.statistics.RunStatistics
import com.azizoglu.grisilith.summary.RunSummarySnapshot
import com.badlogic.gdx.utils.JsonValue
import com.badlogic.gdx.utils.JsonWriter

/** Versioned wire format; never serialize local save files, descriptions or credentials. */
object OnlineProtocol {
    const val API_VERSION = 1
    const val SCORE_VERSION = 1
    const val GAME_VERSION = "0.3"

    fun build(snapshot: RunSummarySnapshot): JsonValue = jsonObject(
        "weapons" to snapshot.weapons.map { mapOf("id" to it.definitionId, "level" to it.level) },
        "armor" to snapshot.armor.definitionId,
        "tamgas" to snapshot.tamgas.mapNotNull { it.definitionId },
        "items" to snapshot.items.map { mapOf("id" to it.definitionId, "count" to it.count) },
        "stats" to snapshot.stats.associate { it.statType.name to it.totalValue }
    )

    fun result(snapshot: RunSummarySnapshot, statistics: RunStatistics, durationSeconds: Int): JsonValue = jsonObject(
        "normal_kills" to statistics.totalNormalEnemiesKilled,
        "unique_kills" to statistics.totalUniqueEnemiesKilled,
        "mini_boss_kills" to statistics.totalBossesDefeated,
        "final_boss_kills" to statistics.totalFinalBossesDefeated,
        "stage_reached" to snapshot.stageReached,
        "duration_seconds" to durationSeconds.coerceAtLeast(0),
        "was_successful" to snapshot.wasSuccessful,
        "final_build" to build(snapshot),
        "summary" to mapOf(
            "damage_dealt" to statistics.totalDamageDealt,
            "damage_taken" to statistics.totalDamageTaken,
            "damage_reduced" to statistics.totalDamageReduced,
            "damage_dealt_by_type" to statistics.damageDealtTypeStatistics.mapKeys { it.key.name },
            "damage_taken_by_type" to statistics.damageTakenTypeStatistics.mapKeys { it.key.name },
            "damage_reduced_by_type" to statistics.damageReducedTypeStatistics.mapKeys { it.key.name },
            "gold_collected" to statistics.totalGoldCollected,
            "gold_spent" to statistics.totalGoldSpent,
            "chests_opened" to statistics.totalChestsOpened,
            "grisilith_coins_earned" to snapshot.grisilithCoinsEarned,
            "weapon_damage" to snapshot.weapons.map { mapOf("id" to it.definitionId, "damage" to it.damageDealt) },
            "other_damage_sources" to snapshot.otherDamageSources
        )
    )

    fun isValidDisplayName(name: String): Boolean {
        val value = PlayerNamePolicy.uppercase(name).trim(' ')
        return value.isEmpty() || (value.length in 3..24 &&
            value.all { it in 'A'..'Z' || it in '0'..'9' || it in "ÇĞİÖŞÜ" || it in PlayerNamePolicy.SYMBOLS } &&
            value.any { it in 'A'..'Z' || it in '0'..'9' || it in "ÇĞİÖŞÜ" })
    }
}

internal fun jsonObject(vararg fields: Pair<String, Any?>): JsonValue = jsonNode(linkedMapOf(*fields))
internal fun JsonValue.wire(): String = toJson(JsonWriter.OutputType.json)

private fun jsonNode(value: Any?): JsonValue = when (value) {
    null -> JsonValue(JsonValue.ValueType.nullValue)
    is JsonValue -> value
    is String -> JsonValue(value)
    is Boolean -> JsonValue(value)
    is Float -> JsonValue(value.takeIf { it.isFinite() }?.toDouble() ?: 0.0)
    is Double -> JsonValue(value.takeIf { it.isFinite() } ?: 0.0)
    is Number -> JsonValue(value.toLong())
    is Map<*, *> -> JsonValue(JsonValue.ValueType.`object`).apply {
        value.forEach { (key, entry) -> addChild(key.toString(), jsonNode(entry)) }
    }
    is Iterable<*> -> JsonValue(JsonValue.ValueType.array).apply { value.forEach { addChild(jsonNode(it)) } }
    else -> error("Unsupported online payload type")
}
