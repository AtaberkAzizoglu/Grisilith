package com.azizoglu.grisilith.enemy.trait

import com.azizoglu.grisilith.enemy.definition.EnemyAbilityIds

/*
 * Enemy Ability adıyla oluşturulmuş eski ContentId değerlerini koruyup
 * yeni terminolojiyi Enemy Trait olarak sunar.
 *
 * Böylece save/content referansları kırılmaz.
 */
object EnemyTraitIds {

    val AIMED_ARROW =
        EnemyAbilityIds.AIMED_ARROW

    val WITCH_ELEMENTAL_PROJECTILE =
        EnemyAbilityIds.WITCH_ELEMENTAL_PROJECTILE

    val WOLF_PACK =
        EnemyAbilityIds.WOLF_PACK

    val BOAR_CHARGE =
        EnemyAbilityIds.BOAR_CHARGE

    val GRIZZLY_ANGRY =
        EnemyAbilityIds.GRIZZLY_ANGRY

    val CROCODILE_JUMP =
        EnemyAbilityIds.CROCODILE_JUMP

    val GORILLA_THROW =
        EnemyAbilityIds.GORILLA_THROW

    val GHOST_INCORPOREAL_EVASION =
        EnemyAbilityIds.GHOST_INCORPOREAL_EVASION

    val SLIME_ACIDIC_PATH =
        EnemyAbilityIds.SLIME_ACIDIC_PATH

    val SWARM_BEING =
        EnemyAbilityIds.SWARM_BEING

    val CROCODILE_BURROW =
        EnemyAbilityIds.CROCODILE_BURROW

    val GORILLA_STOMP_THE_EARTH =
        EnemyAbilityIds.GORILLA_STOMP_THE_EARTH

    val SPEARMAN_THROW_THE_LANCE =
        EnemyAbilityIds.SPEARMAN_THROW_THE_LANCE

    val HORSEMAN_BATTLE_CRY =
        EnemyAbilityIds.HORSEMAN_BATTLE_CRY

    val WITCH_SUMMON_FAMILIARS =
        EnemyAbilityIds.WITCH_SUMMON_FAMILIARS

    val GHOST_HAUNTING_PRESENCE =
        EnemyAbilityIds.GHOST_HAUNTING_PRESENCE

    val SLIME_SPLIT_ON_DEATH =
        EnemyAbilityIds.SLIME_SPLIT_ON_DEATH

    /* Legacy content ID; runtime behavior is now Misty Escape. */
    val MISTY_ESCAPE =
        EnemyAbilityIds.VAMPIRE_MISTY_TRANSPORTATION

    val VAMPIRE_MISTY_TRANSPORTATION =
        EnemyAbilityIds.VAMPIRE_MISTY_TRANSPORTATION

    val VAMPIRE_HUNGER =
        EnemyAbilityIds.VAMPIRE_HUNGER
}
