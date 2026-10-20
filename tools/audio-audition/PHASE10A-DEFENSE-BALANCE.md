# Phase 10a — percentage defense tuning

Damage taken = incoming damage × 100 / (100 + defense).

- Physical defense = (Base Armor + Armor Class) × (1 − incoming piercing ratio).
- Magical defense = Base Spell Protection + Magic Resistance; piercing has no effect.
- Existing piercing conversion and 75% cap are retained.
- Affinities resolve before defense. Immunity stays 0. Positive damage rounds to the nearest integer, with a minimum of 1.
- Continuous damage uses the same percentage per tick, independent of the tick interval.
- Outgoing damage, attack speeds, ability coefficients and adaptive-growth coefficients are unchanged.

## Base definitions

These are definition values, before stage, mini/final-boss and family multipliers. The internal persisted field `baseMagicResistance` remains named that way for compatibility; its display name is Base Spell Protection.

| Boss | Base HP | Base Armor | Armor Class | Base Spell Protection | Magic Resistance |
| --- | ---: | ---: | ---: | ---: | ---: |
| Crocodile | 8,000 | 40 | 62 | 10 | 15 |
| Bear | 9,800 | 40 | 62 | 30 | 46 |
| Swarm of Bees | 6,300 | 20 | 31 | 20 | 31 |
| Spearman | 5,500 | 40 | 62 | 20 | 31 |
| Horseman | 11,700 | 20 | 31 | 10 | 15 |
| Witch | 5,400 | 20 | 27 | 40 | 53 |
| Slime | 11,600 | 20 | 31 | 20 | 31 |
| Ghost | 5,400 | 10 | 15 | 40 | 62 |
| Vampire Lord | 9,000 | 40 | 53 | 40 | 53 |

HP targets at stage 25: average ≈28,000; above average ≈36,000; high ≈44,000; very high ≈60,000. Definition values compensate for the existing family and boss-type multipliers, so raw HP alone does not represent the final tier. In particular, Witch's base HP was reduced from 24,000 to 5,400 and Vampire Lord's from 22,000 to 9,000; compare resolved values below rather than treating these as unscaled runtime HP.

## Stage-25 reference (no adaptive player bonus)

Defense includes the existing stage/family/type scaling. Damage columns apply defense only to a 1,000-damage neutral hit: no piercing, affinity, special trait, second form or other item/reaction modifier.

| Boss | HP | Physical defense | Magical defense | Physical taken | Magical taken |
| --- | ---: | ---: | ---: | ---: | ---: |
| Crocodile | 35,840 | 183 | 45 | 353 | 690 |
| Bear | 43,904 | 183 | 136 | 353 | 424 |
| Swarm of Bees | 28,224 | 92 | 92 | 521 | 521 |
| Spearman | 28,188 | 189 | 94 | 346 | 515 |
| Horseman | 59,964 | 94 | 46 | 515 | 685 |
| Witch | 35,683 | 95 | 188 | 513 | 347 |
| Slime | 60,075 | 94 | 94 | 515 | 515 |
| Ghost | 27,966 | 46 | 188 | 685 | 347 |
| Vampire Lord | 60,228 | 187 | 187 | 348 | 348 |

Existing affinities are preserved: e.g. Crocodile's Fire resistance and Slime's Acid immunity/Water resistance further affect actual hits. Vampire Lord's second form still increases Armor Class by 50%. Gorilla is not part of the supplied table and its base values are unchanged. Non-boss and summon base values are unchanged; their damage taken now uses the shared percentage formula.

## Verification

`Phase10dDefenseChecks` exercises real player, enemy and summon damage entry points; physical/magical classification; piercing and its cap; immunity; minimum damage; integer overflow boundaries; interval-independent damage reduction; stage 1/25/50 boss values; and stage-25 HP tiers.

Additional checks: `phase10aSmoke`, `phase10bSmoke`, `phase10cSmoke`, `verifyMagicResistance`, `verifyDotBalance`. Visual snapshots cover Easy, Normal and Hard selections, plus armor hover versus Collection description.

This is a coherent initial balance profile, not a substitute for playtesting. Adaptive scaling, affinity and boss traits can make actual encounter durability differ from the reference.
