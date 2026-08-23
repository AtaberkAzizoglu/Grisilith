package com.azizoglu.grisilith.enemy.combat

/*
 * Enemy-owned Weapon davranışları için merkezi tuning değerleri.
 *
 * Mevcut Enemy weapon runtime henüz gerçek player-weapon executor'ına
 * bağlı olmadığı için bu değerler Package 3'te attack executor'a
 * bağlanacak. Şimdiden Enemy stat/profile altyapısının parçasıdır.
 */
object EnemyCombatTuning {
    const val DEFAULT_MELEE_WEAPON_RANGE_MULTIPLIER =
        0.70f

    const val DEFAULT_RANGED_WEAPON_RANGE_MULTIPLIER =
        1f

    const val DEFAULT_PROJECTILE_SPEED_MULTIPLIER =
        0.80f
}
