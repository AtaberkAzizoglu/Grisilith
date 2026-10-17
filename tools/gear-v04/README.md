# Four-weapon gear addition

## Implemented content

- Warhammer, Whip, Sling and Boomerang are registered in the shared weapon/content catalogs, permanent Merchant, Collection, preset selectors, run-market pool and Test Map catalog. Permanent weapon unlocks use the existing 5 Grisilith Coin weapon price; the four new weapons start locked until bought.
- Lv2/Lv4 bonuses match the approved Lance / Double Swords / Dagger / Crossbow references. Lv5 Earthbreaker has 2x impact damage and 10% larger radii; Whip Sweep has 2.5x damage; Shatterstone has 3.5x/2.5x/2x damage and 20% larger boulders; Guided Hunt lasts 6 seconds with a 25-second cooldown.
- Discard Voucher is Rare, follows the Rare 5-copy cap, and removes exactly one copy only when a discard beyond the normal run allowance is used. Merely opening/closing a chest does not consume it.
- Non-Chalk active Spell Focus abilities use separate channels per WeaponInstance/runtimeId, including owned reaction targets, cooldowns, field rendering and removal/stage cleanup. Ivy summon waves were already per-copy. Tome remains passive and non-chaining. Chalk retains its shared preparation/cooldown restriction.
- Damage-defense, immunity, adaptive scaling and stage/difficulty values from previous approved work are preserved.

## Generated assets

All 20 assets were produced with the built-in imagegen tool and copied into this repository. No API key or CLI fallback was used. Original generation paths and project destinations are in `generated-assets.json`; the prompt set is in `prompts.md`.

- `assets/weapon_visuals/carriers_v4/`: all 18 floating/held weapon sprites, plus Sling stone/boulder art.
- `assets/ui/content_icons/weapons/wpn_warhammer.png`, `wpn_whip.png`, `wpn_sling.png`, `wpn_boomerang.png`: new weapon icon art.
- `assets/ui/content_icons/items/item_discard_voucher.png`: voucher icon art.
- Existing shared `ContentIconRenderer` applies the gear frame to weapons and the blue Rare frame to the voucher. Generated artwork intentionally has no baked-in frame.
- Legacy held-weapon atlas mappings and disabled carrier attack-art paths were removed from `WeaponCarrierRenderer`. Unrelated existing assets, minion sprites and old weapon ability animations were not deleted.
- `assets-preview.png`: generated art inspection sheet; `qa-ui-preview.png`: actual UI frame and carrier renderer framebuffer capture, not a full gameplay screenshot.

New weapon icon style references: Warhammer uses Lance; Whip uses Longsword; Sling uses Dagger; Boomerang uses Crossbow. Existing carrier sprites reference their corresponding current icons. The same new-weapon art is used in UI and the run to keep silhouettes consistent.

## Timing / ownership rules

Boomerang snapshots the effective attack period per launch. Its front outbound/return legs complete within that period; Attack Speed changes are applied to the next throw rather than stretching an existing flight. Rear legs deal 50% damage and do not block the next front throw. Guided Hunt creates one extra non-rear-loop projectile and affects only this copy's outbound legs; returns are never mouse-guided and phase deadlines are never reset. All moving new projectiles participate in active Vortex trajectory bending.

Sling fragments are queued outside projectile collision iteration, use range 80 and half base damage, skip the first victim's collision body, and never create further fragments. Whip overlap uses the stronger region once per swing; upgraded swings are separate 75% hits with separate visual flashes. Warhammer head/shaft overlap similarly never adds both damages.

## Verification

Run from the repository root:

`gradlew.bat -I tools/gear-v04/smoke.gradle -I tools/physical-weapon-smoke.gradle -I tools/weapon-visual-smoke.gradle lwjgl3:gearAdditionSmoke lwjgl3:physicalWeaponSmoke lwjgl3:weaponVisualSmoke --console=plain`

`PreviewAssets.ps1` generates the art sheet and checks transparent corners (tolerance 2/255 for generated edge alpha). Smoke tests use hidden isolated native windows and separate QA preferences; they do not purchase unlocks or modify user profiles. They check catalogs/icons/frames, voucher limits and consumption, geometry and upgraded damage, fragment behavior, boomerang ownership/timing, independent duplicate spell channels and overlapping reactions, cleanup, shared Chalk behavior and previous physical/defense/animation regressions. Combat balance / real-player TTK has not been playtested.
