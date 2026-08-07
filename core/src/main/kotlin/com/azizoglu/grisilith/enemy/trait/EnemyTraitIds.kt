package com.azizoglu.grisilith.enemy.trait

import com.azizoglu.grisilith.enemy.definition.EnemyAbilityIds

/*
 * Enemy Ability adıyla oluşturulmuş eski ContentId değerlerini koruyup
 * yeni terminolojiyi Enemy Trait olarak sunar.
 *
 * Böylece save/content referansları kırılmaz.
 */
object EnemyTraitIds {

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

    val VAMPIRE_MISTY_TRANSPORTATION =
        EnemyAbilityIds.VAMPIRE_MISTY_TRANSPORTATION

    val VAMPIRE_HUNGER =
        EnemyAbilityIds.VAMPIRE_HUNGER
}
