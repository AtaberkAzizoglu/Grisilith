# Weapon description symbols — v1

Generated using the built-in image tool and the imagegen skill. The generated RGBA alpha is preserved. No existing stat or condition art was replaced.

Asset: `assets/ui/weapon_symbols/semantic_atlas_v1.png`

One 6-column, 5-row atlas. Runtime uses texture regions; no rendered background or tile backplates.

## Row-major mapping

- Row 1: Slashing damage, Piercing damage, Bludgeoning damage, Air, Water, Fire.
- Row 2: Earth, Poison, Acid, Necrotic, Lightning, Cold.
- Row 3: Attack Type, Damage, Base Attack Speed, Range, Scaling, Damage Type headings.
- Row 4: Ability heading, Slashing pattern, Cone, Line, Circle, Projectile.
- Row 5: Returning projectile, Beam, Drain, melee, ranged, Armor Break.

## Semantics

- Player stats retain the existing red stat glyphs.
- Damage types use distinct glyphs and dark-grey names.
- Attack patterns and melee/ranged use teal names and distinct glyphs.
- The seven description headings use white names with a larger type size.
- `Base Attack Speed` outside a heading is plain prose and never resolves to the Attack Speed stat.
- `Piercing` in a Damage Type row resolves to piercing damage, never to the Piercing stat. Stat bonus rows retain the original red stat glyph.
- Armor Break is a timed status marker tied to the existing enemy Armor Class reduction. The shared condition renderer can display it on players, enemies and summons without adding a ground effect.

## Generation prompt

Use case: stylized-concept. Asset type: one production-ready RPG UI symbol sprite atlas. A single transparent PNG with EXACTLY 6 columns and 5 rows of equally sized cells, 30 isolated icons total, centered on the exact regular grid. Request 1536x1280 canvas (256px square cells). Every icon fits within the central 60% of its cell, transparent 20% padding on every side. No text, letters, numbers, labels, frames, tiles, discs, backplates or grid lines. Entire background genuinely transparent. Style: clear simple medieval etched metal pictograms, strong crisp silhouettes, restrained shading, readable at 16px. Silver/ivory neutral edges with subtly colored damage symbols; not ornate or glowing. Consistent icon size.
Strict row-major icon order, left to right:
Row 1: slashing damage (three blade cuts); piercing damage (needle piercing a small ring); bludgeoning damage (impacting hammer head); air damage (wind spiral); water damage (water droplet); fire damage (flame).
Row 2: earth damage (three rocks); poison damage (venom droplet with tiny snake curve); acid damage (drop dissolving a surface); necrotic damage (skull with decay); lightning damage (zigzag bolt); cold damage (snowflake).
Row 3: Attack Type heading (two intersecting curved attack paths); Damage heading (impact star); Base Attack Speed heading (hourglass with motion marks); Range heading (double-ended ruler arrow); Scaling heading (three rising bars and arrow); Damage Type heading (three tiny diamonds).
Row 4: Ability heading (rune burst); Slashing attack pattern (one curved sweeping blade trail); Cone attack pattern (simple fan sector); Line attack pattern (one straight directional stroke); Circle attack pattern (plain hollow ring); Projectile attack pattern (arrowhead with two motion trails).
Row 5: Returning projectile pattern (boomerang with curved return arrow); Beam attack pattern (two parallel light rays); Drain attack pattern (three drops flowing toward center); Melee range (short sword); Ranged range (bow and arrow); Armor Break debuff (cracked shield split by a jagged fracture, amber/red accent).
Keep every symbol separated; no connected elements across cells; exactly 30 icons on the exact 6x5 grid.

