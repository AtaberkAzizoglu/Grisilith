# Whip sweep HD replacement

Built-in imagegen generated four transparent 2x2 sprite sheets, 16 frames total.
Production files: assets/weapon_visuals/physical_revision/whip_sweep_hd_0.png through whip_sweep_hd_3.png.
Prompts: whip-hd-prompts.json.
Each sheet is 1254x1254, each frame 627x627 (old sheet: 1448x1086, approx. 362x271 per frame).
The new frame has around four times the pixel area, with linear sampling instead of nearest magnification.
The visible grip is used as the rotation anchor and tip reach is normalized to ability range.
The 80/120-degree attack geometry and damage have not changed.
Old production sheet moved to tools/weapon-visual-smoke/rejected/whip_sweep_lowres.png for recovery.

Double swords are mechanically split without resampling or repainting into:
assets/weapon_visuals/carriers_v4/double_sword_left.png and double_sword_right.png.
Each blade is displayed at height 38 (previous combined image height 42) on opposite sides.
Warhammer (Maul): height 60. Lance: height 104. Character visual height remains 48.
Tall weapons are lifted so their shaft bottom remains near the character's feet and tips rise above the head.

Characters were re-imported from their original sources in three deterministic passes.
Source cuts, transparent padding and frame hashes were checked each time.
Koba and Linlos raw generation sources are preserved in tools/character_visuals/sources_v3/.
