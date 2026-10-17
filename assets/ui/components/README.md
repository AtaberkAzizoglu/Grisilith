# Grisilith UI Component Asset Pack

This folder contains production-ready PNG assets matching the dark-fantasy visual language of `UI components V1.png`. Shared screen components are rendered through `UiComponentRenderer`.

## Folder structure

- `panels/`: main, inner, modal, tooltip, horizontal, small, title-ribbon and list-row panels. `clean_panel.png` is the corner-ornament-free scalable source currently used by the game.
- `popups/`: confirmation, information, warning and error popup frames.
- `slots/`: normal, hover, selected, locked, empty, dragging, invalid-drop and merge-target inventory slots.
- `controls/checkbox/`: unchecked, checked, hover and disabled states. The `clean_*` pair is the active high-resolution set.
- `controls/radio/`: unchecked, selected, hover and disabled states.
- `controls/toggle/`: off, on, hover and disabled states. The `clean_*` pair is the active high-resolution set; disabled state is produced with a neutral runtime tint.
- `controls/slider/`: track, fill and thumb.
- `controls/scrollbar/`: vertical/horizontal tracks and thumbs plus four directional arrows.
- `tabs/`: normal, hover, selected, disabled and notification states.
- `filters/`: normal, selected and disabled filter chips.
- `inputs/`: text fields, dropdown states/rows, search field and stepper.
- `progress/`: small/boss frames and HP, boss HP, XP, cast, cooldown, gate HP, magic and neutral fills.
- `separators/`: short, long, vertical, plain and heading ornament separators.
- `overlays/`: hover, selection, cooldown, disabled, locked, new, warning, active-ability and drag overlays.
- `icons/`: navigation, editing, status, search/filter, playback, sound and menu control icons.
- `nine_patch/`: LibGDX-ready `.9.png` versions of scalable panels, popups, tabs, filters, inputs and progress frames.
- `masters/`: the six original generated component sheets retained as high-resolution sources.

## Usage notes

- Prefer files in `nine_patch/` when a panel must be resized to several aspect ratios. The one-pixel guide border is LibGDX nine-patch metadata and is not rendered in game.
- Use the ordinary PNG versions when the component is displayed at a fixed aspect ratio.
- Progress fills are independent from their frames so their width can represent a runtime value.
- Overlay assets should be drawn above the underlying slot or button.
- All assets have transparent backgrounds and contain no UI text.

## Source workflow

The project-local `tools/slice_ui_components.py` script documents the source-sheet mapping and can regenerate the separated files without changing gameplay or UI code.
