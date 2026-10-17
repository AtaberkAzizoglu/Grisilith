package com.azizoglu.grisilith.summon

import com.azizoglu.grisilith.weapon.summon.IvySummonChoice

/** Read-only descriptions of summon rules; does not change combat balance. */
object SummonPresentation {
    private const val SCALING = "Their starting strength follows the current stage. Shepherd increases damage, health, attack speed, movement speed and defenses; summon-related item bonuses also apply."

    fun choice(choice: IvySummonChoice): String = when (choice) {
        IvySummonChoice.SUMMON_ANIMALS -> "Summon sturdy allied beasts to fight at your side. Their melee attacks are modest, but their high health and physical protection help them hold the front line.\n\nLevel 3: 2 beasts. Level 5: 3 beasts with stronger Shepherd scaling.\nDuration: 15 seconds. Cooldown: 30 seconds after the last beast in the wave disappears.\n\n$SCALING"
        IvySummonChoice.SUMMON_DEMONS -> "Summon allied demons that bombard enemies with powerful ranged Fire attacks. They trade durability for damage, so keep them out of trouble.\n\nLevel 3: 1 demon. Level 5: 2 demons with stronger Shepherd scaling.\nDuration: 20 seconds. Cooldown: 20 seconds after the last demon in the wave disappears.\n\n$SCALING"
        IvySummonChoice.CALL_THE_KNIGHT -> "Call one neutral Oathbreaker into battle. Neutral does not mean friendly: it can attack enemies, you and other summons. Its powerful melee attacks make this a dangerous bargain.\n\nDuration: 30 seconds. Cooldown: 30 seconds after the Oathbreaker disappears. Level 5 improves its Shepherd scaling.\n\n$SCALING"
    }

    fun archive(type: SummonType): String = when (type) {
        SummonType.COW -> "Source: Ivy Staff — Summon Animals.\n\nA durable allied front-liner with modest melee damage, high health and strong physical defenses. Summons 2 cows at Level 3 and 3 at Level 5. Each lives for 15 seconds; the 30-second cooldown begins when the last cow disappears.\n\n$SCALING Level 5 improves the staff's Shepherd scaling."
        SummonType.IMP -> "Source: Ivy Staff — Summon Demons.\n\nA fragile allied demon that attacks from range with Fire projectiles. Summons 1 Imp at Level 3 and 2 at Level 5. Each lives for 20 seconds; the 20-second cooldown begins when the last Imp disappears.\n\n$SCALING Level 5 improves the staff's Shepherd scaling."
        SummonType.KNIGHT_SPIRIT -> "Source: Ivy Staff — Call the Oathbreaker.\n\nA neutral, heavily armed melee fighter hostile to enemies, the player and other summons alike. Only one is called at a time. It lives for 30 seconds, followed by a 30-second cooldown.\n\n$SCALING Level 5 improves the staff's Shepherd scaling."
        SummonType.TOAD -> "Sources: Linlos Dustman's The Toad Cycle trait and Bone Totem: Toads.\n\nA small allied ranged attacker that spits Poison projectiles and applies Poisoned. It is fragile, but several toads can support the battle from a safer distance. Trait toads have no timed expiry (up to 4 at once); Bone Totem toads live for 40 seconds.\n\n$SCALING"
        SummonType.GUARD -> "Source: village defense reinforcements in Vogtsweier Siege.\n\nAn allied lance fighter that defends its assigned area rather than following you everywhere. Guards have no timed expiry, but can be killed.\n\nHealth and movement come from the source enemy. Damage and lance level increase with the stage; lance level rises every 10 stages, up to Level 5. Unlike Ivy Staff summons, guards do not scale with Shepherd."
    }
}
