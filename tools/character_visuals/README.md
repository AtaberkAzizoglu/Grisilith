# Koba and Linlos visual refresh

Generated with the built-in image generation tool on 2026-09-15.
Style references: `assets/characters/sir_imbert.png` and
`assets/characters/ywaleth_yoster.png`. Identity references were the original
Koba and Linlos sheets. Portraits and Phantom Steed sheets retain their
existing artwork; this refresh replaces the on-foot idle/walk sheets only.

## Production prompt set

Shared brief: production 2D RPG sprite sheet in the reference characters'
muted, richly shaded, crisp pixel-art fantasy style with dark outlines and
slightly top-down full-body perspective. Canvas 1536x1024, exactly 20 sprites
in five columns and four rows, no portrait. Rows down, left, right, up; column
one idle, then four distinct walking poses (left foot forward, passing, right
foot forward, opposite passing). Consistent identity, costume, head/body size,
colors and scale; clean cutouts, full bodies and padding. Transparent alpha,
no checkerboard, shadows, glow, borders, grid, text, effects or detached particles.

Koba: muscular gray-green half-orc barbarian, long black hair, small tusks,
bare chest, red tribal markings, brown leather bracers, fur-trimmed brown boots,
ragged brown/red leather waistcloth, no held weapon. Preserve his identity
while matching Sir Imbert and Ywaleth's proportion language rather than the
original oversized, soft illustration.

Linlos Dustman: adult male halfling druid, short stocky build, shaggy brown
hair, slight beard, green leaf cloak, earth-brown clothes, bare hairy feet,
small mushroom/frog backpack and crooked wooden walking staff. Preserve his
identity while matching the same reference style; no new weapon or flying insects.

## Game import

The generator baked a neutral checkerboard into RGB instead of real alpha.
`PrepareCharacterSheet.ps1` mechanically extracts that matte with connected
regions, preserves small enclosed pale highlights, removes tiny detached
specks, and packs the unaltered drawings into 640x512 RGBA atlases. A single
uniform scale per character preserves proportions across directions. Every
128x128 cell uses a shared feet pivot at y=125 and at least 3px padding.
Validation checks 20 distinct frames, nonempty content, padding and feet pivots.

Registered game assets:
- `assets/characters/koba_v2.png`
- `assets/characters/linlos_dustman_v2.png`

The original sheets are retained as non-destructive backups. Character stats,
collision, attack logic and animation timing are unchanged. Idle is separate
from the four walking frames.
