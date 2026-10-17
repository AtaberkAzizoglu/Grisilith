"""Split generated transparent UI pack sheets into ready-to-use PNG assets."""

from __future__ import annotations

from collections import deque
from pathlib import Path

import numpy as np
from PIL import Image


ROOT = Path(r"C:\Android Projects\Grisilith\assets\ui\components")


PACKS = [
    (Path(r"C:\Users\aaata\.codex\generated_images\01a06715-881d-79a1-a298-234d89d6359b\exec-ceb5ea71-4e90-4685-929b-e235c7341503.png"), 4, 3, [
        "panels/main_panel", "panels/inner_panel", "panels/modal_panel", "panels/tooltip_panel",
        "panels/horizontal_panel", "panels/small_panel", "popups/confirm_popup", "popups/information_popup",
        "popups/warning_popup", "popups/error_popup", "panels/title_ribbon", "panels/list_row_panel",
    ]),
    (Path(r"C:\Users\aaata\.codex\generated_images\01a06715-881d-79a1-a298-234d89d6359b\exec-53140536-436a-4f96-91e8-5f07aa4afe3a.png"), 4, 4, [
        "slots/slot_normal", "slots/slot_hover", "slots/slot_selected", "slots/slot_locked",
        "slots/slot_empty", "slots/slot_dragging", "slots/slot_invalid_drop", "slots/slot_merge_target",
        "overlays/hover_glow", "overlays/selection_glow", "overlays/cooldown_overlay", "overlays/disabled_overlay",
        "overlays/locked_overlay", "overlays/new_badge", "overlays/warning_badge", "overlays/active_ability_frame",
    ]),
    (Path(r"C:\Users\aaata\.codex\generated_images\01a06715-881d-79a1-a298-234d89d6359b\exec-b853558f-48d0-4a09-afc7-44a27dacb656.png"), 4, 4, [
        "controls/checkbox/checkbox_unchecked", "controls/checkbox/checkbox_checked", "controls/checkbox/checkbox_hover", "controls/checkbox/checkbox_disabled",
        "controls/radio/radio_unchecked", "controls/radio/radio_selected", "controls/radio/radio_hover", "controls/radio/radio_disabled",
        "controls/toggle/toggle_off", "controls/toggle/toggle_on", "controls/toggle/toggle_hover", "controls/toggle/toggle_disabled",
        "controls/slider/slider_track", "controls/slider/slider_fill", "controls/slider/slider_thumb", "controls/scrollbar/scrollbar_track_vertical",
    ]),
    (Path(r"C:\Users\aaata\.codex\generated_images\01a06715-881d-79a1-a298-234d89d6359b\exec-7d73dec8-4563-4d4f-a63d-a4bc7fb49857.png"), 5, 4, [
        "tabs/tab_normal", "tabs/tab_hover", "tabs/tab_selected", "tabs/tab_disabled", "tabs/tab_notification",
        "filters/filter_normal", "filters/filter_selected", "filters/filter_disabled", "inputs/text_input_normal", "inputs/text_input_focused",
        "inputs/text_input_disabled", "inputs/dropdown_closed", "inputs/dropdown_open", "inputs/dropdown_row_normal", "inputs/dropdown_row_hover",
        "inputs/dropdown_row_selected", "inputs/search_input", "inputs/stepper", "icons/dropdown_arrow", "icons/clear",
    ]),
    (Path(r"C:\Users\aaata\.codex\generated_images\01a06715-881d-79a1-a298-234d89d6359b\exec-ad7bf313-3a89-4953-8b10-6766080eb461.png"), 5, 4, [
        "separators/separator_horizontal_short", "separators/separator_horizontal_long", "separators/separator_vertical", "separators/heading_ornament", "separators/separator_plain",
        "progress/progress_frame_small", "progress/progress_frame_boss", "progress/fill_hp", "progress/fill_boss_hp", "progress/fill_xp",
        "progress/fill_cast", "progress/fill_cooldown", "progress/fill_gate_hp", "progress/fill_magic", "progress/fill_neutral",
        "controls/scrollbar/scrollbar_thumb_vertical", "controls/scrollbar/scrollbar_arrow_up", "controls/scrollbar/scrollbar_arrow_down", "controls/scrollbar/scrollbar_thumb_horizontal", "overlays/drag_shadow",
    ]),
]


def components(alpha: np.ndarray) -> list[tuple[int, int, int, int, int]]:
    mask = alpha > 8
    height, width = mask.shape
    seen = np.zeros(mask.shape, dtype=np.bool_)
    found: list[tuple[int, int, int, int, int]] = []
    for y in range(height):
        for x in range(width):
            if not mask[y, x] or seen[y, x]:
                continue
            queue = deque([(x, y)])
            seen[y, x] = True
            min_x = max_x = x
            min_y = max_y = y
            count = 0
            while queue:
                cx, cy = queue.popleft()
                count += 1
                min_x = min(min_x, cx)
                max_x = max(max_x, cx)
                min_y = min(min_y, cy)
                max_y = max(max_y, cy)
                for nx, ny in ((cx - 1, cy), (cx + 1, cy), (cx, cy - 1), (cx, cy + 1)):
                    if 0 <= nx < width and 0 <= ny < height and mask[ny, nx] and not seen[ny, nx]:
                        seen[ny, nx] = True
                        queue.append((nx, ny))
            if count >= 80:
                found.append((min_x, min_y, max_x + 1, max_y + 1, count))
    return found


def slice_pack(source: Path, columns: int, rows: int, names: list[str]) -> None:
    image = Image.open(source).convert("RGBA")
    alpha = np.asarray(image.getchannel("A"))
    boxes = components(alpha)
    if len(boxes) < len(names):
        print(
            f"Skipping connected-component pass for {source.name}: "
            f"found {len(boxes)} of {len(names)} components; keeping grid-sliced outputs."
        )
        return
    expected_centers = [
        ((column + 0.5) * image.width / columns, (row + 0.5) * image.height / rows)
        for row in range(rows)
        for column in range(columns)
    ]
    selected: list[tuple[int, int, int, int, int]] = []
    available = boxes.copy()
    for expected_x, expected_y in expected_centers:
        best = min(
            available,
            key=lambda box: ((box[0] + box[2]) / 2 - expected_x) ** 2 + ((box[1] + box[3]) / 2 - expected_y) ** 2,
        )
        selected.append(best)
        available.remove(best)

    for name, (left, top, right, bottom, _) in zip(names, selected):
        padding = 8
        box = (
            max(0, left - padding),
            max(0, top - padding),
            min(image.width, right + padding),
            min(image.height, bottom + padding),
        )
        destination = ROOT / f"{name}.png"
        destination.parent.mkdir(parents=True, exist_ok=True)
        image.crop(box).save(destination)


def create_scrollbar_derivatives() -> None:
    controls = ROOT / "controls" / "scrollbar"
    vertical_track = Image.open(controls / "scrollbar_track_vertical.png").convert("RGBA")
    vertical_track.rotate(90, expand=True).save(controls / "scrollbar_track_horizontal.png")
    up = Image.open(controls / "scrollbar_arrow_up.png").convert("RGBA")
    up.rotate(90, expand=True).save(controls / "scrollbar_arrow_left.png")
    up.rotate(-90, expand=True).save(controls / "scrollbar_arrow_right.png")


def create_nine_patch(source: Path, destination: Path) -> None:
    image = Image.open(source).convert("RGBA")
    output = Image.new("RGBA", (image.width + 2, image.height + 2), (0, 0, 0, 0))
    output.paste(image, (1, 1))
    black = (0, 0, 0, 255)
    stretch_x0 = max(1, round(image.width * 0.38))
    stretch_x1 = min(image.width, round(image.width * 0.62))
    stretch_y0 = max(1, round(image.height * 0.38))
    stretch_y1 = min(image.height, round(image.height * 0.62))
    for x in range(stretch_x0, stretch_x1 + 1):
        output.putpixel((x, 0), black)
    for y in range(stretch_y0, stretch_y1 + 1):
        output.putpixel((0, y), black)
    for x in range(max(1, round(image.width * 0.12)), min(image.width, round(image.width * 0.88)) + 1):
        output.putpixel((x, image.height + 1), black)
    for y in range(max(1, round(image.height * 0.12)), min(image.height, round(image.height * 0.88)) + 1):
        output.putpixel((image.width + 1, y), black)
    destination.parent.mkdir(parents=True, exist_ok=True)
    output.save(destination)


def create_nine_patch_set() -> None:
    scalable_folders = ("panels", "popups", "tabs", "filters", "inputs")
    for folder in scalable_folders:
        for source in (ROOT / folder).glob("*.png"):
            relative = source.relative_to(ROOT)
            destination = ROOT / "nine_patch" / relative.parent / f"{source.stem}.9.png"
            create_nine_patch(source, destination)
    for name in ("progress_frame_small", "progress_frame_boss"):
        source = ROOT / "progress" / f"{name}.png"
        create_nine_patch(source, ROOT / "nine_patch" / "progress" / f"{name}.9.png")


if __name__ == "__main__":
    for pack in PACKS:
        slice_pack(*pack)
    create_scrollbar_derivatives()
    create_nine_patch_set()
    print(f"Re-sliced {sum(len(pack[3]) for pack in PACKS)} assets using connected alpha components.")
