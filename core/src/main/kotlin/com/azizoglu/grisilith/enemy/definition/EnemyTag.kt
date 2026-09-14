package com.azizoglu.grisilith.enemy.definition

/*
 * Map'lerin kullanacağı ana Enemy grupları.
 *
 * Map tanımları şimdilik tek bir Tag kullanacak olsa da
 * EnemyKindDefinition birden fazla Tag taşıyabilir.
 */
enum class EnemyTag {
    BEAST,
    HUMANOID,
    UNDEAD,
    MONSTER
}
