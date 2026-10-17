# Character and carrier atlas repair (v0.3)

Inspected all five original characters and all five visual companion types before replacement.
The first three characters retain their original drawings, now fully extracted from larger source cells.
Examples: Ywaleth's first body extends above the old tight selection; Lavena's crown also extends
above the old selection. Each frame is centered with a common feet anchor, with no per-frame scale pumping.
Original source PNGs and portraits remain untouched.

Koba and Linlos use newly generated 20-frame atlases (idle plus four walk phases in four directions).
Their grey-green/red tribal barbarian and green/brown leaf-cloak halfling identities are preserved.
All companion cuts are repacked with transparent padding. The original three companion source rows
touch one another, so clean isolated variants preserve their costumes and direction layout.
Barbarian and Druid retain their four-frame style from an alpha-extraction variant.

Production paths:
- assets/characters/aligned/: sir_imbert, ywaleth_yoster, lavena_vol, koba, linlos_dustman.
- assets/weapon_visuals/minions_aligned/: soldier, scout, mage_apprentice, barbarian, druid.
- assets/weapon_visuals/carriers_v4/lance.png: upright narrow-tip replacement.

Generated using built-in imagegen; prompts and source paths are recorded in revision-prompts.json.
ImportAlignedAtlas.ps1 performs mechanical extraction/packing only. Connected-sprite extraction
keeps entire bodies even when the generator does not align the grid perfectly.
The old hand-authored minion rectangles and character frame selections are no longer used.

Carried weapons are always screen-right: player +34 units; companions +26; y +2.
Facing and attack recoil no longer change their offset. Boomerang has a fixed -45 degree orientation
to stand vertically. Lance is upright (zero rotation). Dagger renders at 80% of its previous size.
Attack targeting, collision footprints, damage, cooldowns, summons and mounted character art are unchanged.
