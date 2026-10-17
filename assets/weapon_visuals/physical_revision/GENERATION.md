# v0.3 physical weapon revision

Generated with the built-in imagegen tool, transparent PNG assets.
Four 4-by-4, 16-frame row-major sheets are consumed directly by ExtendedPhysicalWeaponRuntime.
No placeholder geometry is drawn for these attacks.

- hammer_strike.png: narrow shaft, square steel head, golden impact and stone dust.
- earthbreaker.png: expanding circular earth shockwave.
- whip_strike.png: fine lash and bright tip snap.
- whip_sweep.png: isolated lash fan (a first version containing a character was rejected).

Selected upright Warhammer also replaces its UI icon; selected relaxed Sling replaces only its carrier.
Sir Imbert portrait was edited preserving his identity and frame, with a charcoal background.
Source paths and original prompts: tools/gear-v04/revision-assets.json.
The final whip_sweep was regenerated with an explicit NO CHARACTER constraint and a fixed center-right fan.

## Runtime rules

Earthbreaker Lv3: 3 circles, radius 70; Lv5: 4 circles, radius 84.
Whip normal: 30-degree cone, lengths 81.25 / 100; shaft width 4.
Whip Sweep: 80 / 120 degrees at Lv3 / Lv5.
Shatterstone: radius 75 / 93.75, one impact / two impacts; upgraded bounce advances 160.
Guided Hunt extra projectile scale: 1.25 / 1.4. Rear flight travels half normal range.

Merchant permanent unlocks are preview-only via UnlockRegistry.PREVIEW_UNLOCKS_ENABLED.
Disable this flag for v1.0; saved purchases and currency remain intact.
Repeatable Stat Points are deliberately excluded.
