package com.azizoglu.grisilith.tamga.definition

/** UI-only scaling copy, checked against the corresponding runtime calculations. */
object TamgaPresentation {
    fun scaling(effect: TamgaEffectId): String = when(effect) {
        TamgaEffectId.CHARGER -> "Damage = round(70 + 2.1 × Elemental Damage + 8 × charged seconds + 5 × floor((Stage - 1) / 5)). A full charge adds 12 damage. Charge time is capped at 3.5s. Radius = 240 + 18 × charged seconds; completing the charge adds 25 px. Cooldown: 120s."
        TamgaEffectId.HEIGHT_INSECURITY -> "Against Unique enemies, Mini Bosses and Final Bosses, damage = round(original hit × (1.05 + 0.05 × floor(Stage / 10))). The bonus is 5% at Stages 1–9, 10% at 10–19, then continues rising by 5% every 10 stages."
        TamgaEffectId.PHANTOM_STEED -> "No direct damage. Let n = floor((Stage - 1) / 10). While mounted: Weapon Range +(50 + 10n) px, Max HP and Movement Speed +(2 + n), Harvest -(5 + n). Lasts 15s; cooldown 120s."
        TamgaEffectId.HOLY_LIGHTS -> "Uses the highest Armor Class, Wisdom or Life Steal, excluding this Tamga's own bonus when choosing. Damage per 0.5s tick: round(7 + 0.75 × Wisdom), or round(4 + 0.35 × the selected other stat). The selected stat gains 1 + floor(Stage / 5); the Life Steal mode's hits can trigger Life Steal. Radius: 155 px."
        TamgaEffectId.ARCANE_AURA -> "Uses the highest Shepherd, Elemental Damage or Might of Nature. Damage per 0.55s tick = round(5 + 0.62 × selected stat + floor(Stage / 5)). Shepherd selects the Nature aura, Elemental Damage Water, and Might of Nature Necrotic. Radius: 170 px."
        TamgaEffectId.FLAME_BLADES -> "Extra damage per hit on a Burned target = floor(5 + 1.1 × Elemental Damage), minimum 1. The first slot's melee hits apply Burned; hits from any equipped weapon can then trigger the extra damage."
        TamgaEffectId.QUICK_SHOT -> "No direct damage. Each first-slot ranged attack adds 1 Attack Speed, up to 3 stacks at Stages 1–10, 4 at 11–20 and 5 from Stage 21. All stacks expire after 1s without an attack from that slot."
        TamgaEffectId.PANIC -> "Damage = 0. At least 8 living enemies within 160 px trigger a knockback pulse. Cooldown: 22s. No damage-stat scaling."
    }
    fun details(tamga: TamgaDefinition) = "${tamga.description}\n\nScaling\n${scaling(tamga.effectId)}\n\nround: nearest whole number. floor: round down. Damage is shown before target defenses."
}
