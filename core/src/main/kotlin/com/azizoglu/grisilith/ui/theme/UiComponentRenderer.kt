package com.azizoglu.grisilith.ui.theme

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Rectangle

/**
 * Shared renderer for the dark-fantasy UI component pack.
 *
 * Text and interaction bounds deliberately stay outside this class: screens keep
 * their existing layout and hitboxes while only their presentation is replaced.
 */
object UiComponentRenderer {

    private var renderOpacity = 1f

    fun <T> withOpacity(opacity: Float, block: () -> T): T {
        val previous = renderOpacity
        renderOpacity = opacity.coerceIn(0f, 1f)
        return try {
            block()
        } finally {
            renderOpacity = previous
        }
    }

    private fun faded(color: Color): Color = Color(color).also { it.a *= renderOpacity }

    private const val ROOT = "ui/components/"
    private val stepLeft by lazy { texture("controls/scrollbar/scrollbar_arrow_left.png") }
    private val stepRight by lazy { texture("controls/scrollbar/scrollbar_arrow_right.png") }

    fun drawStepperArrow(batch: SpriteBatch, bounds: Rectangle, right: Boolean, state: ButtonVisualState) {
        val old = Color(batch.color)
        batch.color = faded(when(state) {
            ButtonVisualState.HOVERED, ButtonVisualState.SELECTED -> Color(1f,.88f,.58f,1f)
            ButtonVisualState.PRESSED -> Color(.8f,.7f,.5f,1f)
            ButtonVisualState.DISABLED -> Color.DARK_GRAY
            else -> Color.WHITE
        })
        batch.draw(if(right)stepRight else stepLeft, bounds.x,bounds.y,bounds.width,bounds.height)
        batch.color = old
    }

    private fun texture(path: String): Texture =
        Texture(Gdx.files.internal(ROOT + path)).apply {
            setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        }

    private fun patch(path: String, horizontalBorder: Int, verticalBorder: Int): NinePatch =
        NinePatch(
            texture(path),
            horizontalBorder,
            horizontalBorder,
            verticalBorder,
            verticalBorder
        )

    private val buttonNormalTexture by lazy { texture("filters/filter_normal.png") }
    private val buttonSelectedTexture by lazy { texture("filters/filter_selected.png") }
    private val buttonDisabledTexture by lazy { texture("filters/filter_disabled.png") }

    private val buttonNormal by lazy {
        NinePatch(buttonNormalTexture, 24, 24, 18, 18)
    }

    private val buttonSelected by lazy {
        NinePatch(buttonSelectedTexture, 24, 24, 18, 18)
    }

    private val buttonDisabled by lazy {
        NinePatch(buttonDisabledTexture, 24, 24, 18, 18)
    }

    /*
     * All scalable panels intentionally share one clean source texture.  The
     * previous pack baked large corner ornaments into every bitmap; NinePatch
     * then magnified those ornaments on large screens.  This source keeps all
     * detail inside a narrow, uniform edge band, while role-specific tinting
     * below preserves the visual hierarchy.
     */
    private val cleanPanelTexture by lazy {
        texture("panels/clean_panel.png")
    }
    private val stonePanelTexture by lazy {
        Texture(Gdx.files.internal("ui/backgrounds/chronicle_panel_hd.png")).apply {
            setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        }
    }

    /** Full-resolution stone surface; do not magnify a tiny tiled center. */
    fun drawStonePanel(batch: SpriteBatch, bounds: Rectangle) {
        val old = Color(batch.color)
        batch.color = Color.WHITE
        batch.draw(stonePanelTexture, bounds.x, bounds.y, bounds.width, bounds.height)
        batch.color = old
    }

    private fun cleanPanelPatch(): NinePatch =
        NinePatch(cleanPanelTexture, 22, 22, 22, 22)

    private val mainPanel by lazy { cleanPanelPatch() }
    private val innerPanel by lazy { cleanPanelPatch() }
    private val modalPanel by lazy { cleanPanelPatch() }
    private val horizontalPanel by lazy { cleanPanelPatch() }
    private val listRowPanel by lazy { cleanPanelPatch() }
    private val tooltipPanel by lazy { cleanPanelPatch() }
    private val titleRibbon by lazy { cleanPanelPatch() }

    private val slotNormal by lazy { texture("slots/slot_normal.png") }
    private val slotSelected by lazy { texture("slots/slot_selected.png") }
    private val slotLocked by lazy { texture("slots/slot_locked.png") }

    private val dropdownClosed by lazy {
        patch("inputs/dropdown_closed.png", 32, 24)
    }

    private val dropdownOpen by lazy {
        patch("inputs/dropdown_open.png", 32, 28)
    }

    private val dropdownRowNormal by lazy {
        patch("inputs/dropdown_row_normal.png", 28, 22)
    }

    private val dropdownRowSelected by lazy {
        patch("inputs/dropdown_row_selected.png", 28, 22)
    }

    private val checkboxUnchecked by lazy {
        texture("controls/checkbox/clean_checkbox_unchecked.png")
    }
    private val checkboxChecked by lazy {
        texture("controls/checkbox/clean_checkbox_checked.png")
    }

    private val progressFrameSmall by lazy {
        patch("progress/progress_frame_small.png", 28, 22)
    }
    private val healthBarFrame by lazy { texture("progress/health_bar_simple.png") }
    private val healthBarBackground by lazy { texture("progress/health_bar_background.png") }
    private val healthFill by lazy { texture("progress/fill_hp_inner.png") }
    private val cooldownOverlay by lazy { texture("overlays/cooldown_shade.png") }
    private val activeAbilityOverlay by lazy { texture("overlays/ability_state_frame_clean.png") }
    private val gameSpeedButton by lazy {
        texture("controls/game_speed/game_speed_button.png")
    }
    private val toggleOff by lazy { texture("controls/toggle/clean_toggle_off.png") }
    private val toggleOn by lazy { texture("controls/toggle/clean_toggle_on.png") }
    private val sliderTrack by lazy { texture("controls/slider/slider_track.png") }
    private val sliderThumb by lazy { texture("controls/slider/slider_thumb.png") }
    private val verticalScrollbarTrack by lazy { texture("controls/scrollbar/scrollbar_track_vertical.png") }
    private val verticalScrollbarThumb by lazy { texture("controls/scrollbar/scrollbar_thumb_vertical.png") }
    private val chronicleScrollbarTrackTexture by lazy {
        texture("controls/scrollbar/chronicle_scrollbar_track.png")
    }
    private val chronicleScrollbarThumbTexture by lazy {
        texture("controls/scrollbar/chronicle_scrollbar_thumb.png")
    }
    private val chronicleScrollbarTrack by lazy {
        TextureRegion(chronicleScrollbarTrackTexture, 476, 40, 73, 1457)
    }
    private val chronicleScrollbarThumb by lazy {
        TextureRegion(chronicleScrollbarThumbTexture, 514, 322, 229, 601)
    }
    private val horizontalSeparator by lazy { texture("separators/separator_plain.png") }
    private val verticalSeparator by lazy { texture("separators/separator_vertical.png") }
    private val solidPixel by lazy {
        val pixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888)
        pixmap.setColor(Color.WHITE)
        pixmap.fill()
        Texture(pixmap).also {
            it.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
            pixmap.dispose()
        }
    }

    fun drawButton(
        batch: SpriteBatch,
        bounds: Rectangle,
        state: ButtonVisualState
    ) {
        val patch = when (state) {
            ButtonVisualState.NORMAL -> buttonNormal
            ButtonVisualState.HOVERED,
            ButtonVisualState.PRESSED,
            ButtonVisualState.SELECTED -> buttonSelected
            ButtonVisualState.DISABLED -> buttonDisabled
        }

        val tint = when (state) {
            ButtonVisualState.PRESSED -> Color(0.78f, 0.92f, 1f, 1f)
            else -> Color.WHITE
        }

        if (bounds.width < 145f || bounds.height < 38f) {
            val texture = when (state) {
                ButtonVisualState.DISABLED -> buttonDisabledTexture
                ButtonVisualState.HOVERED,
                ButtonVisualState.PRESSED,
                ButtonVisualState.SELECTED -> buttonSelectedTexture
                ButtonVisualState.NORMAL -> buttonNormalTexture
            }
            val previous = Color(batch.color)
            batch.color = faded(tint)
            batch.draw(texture, bounds.x, bounds.y, bounds.width, bounds.height)
            batch.color = previous
        } else {
            drawPatch(batch, patch, bounds, tint)
        }
    }

    /** Draws the same compact beveled plate used by the Test Map pause button. */
    fun drawCompactButton(
        batch: SpriteBatch,
        bounds: Rectangle,
        selected: Boolean = false
    ) {
        val texture = if (selected) buttonSelectedTexture else buttonNormalTexture
        val previous = Color(batch.color)
        batch.color = Color.WHITE
        batch.draw(texture, bounds.x, bounds.y, bounds.width, bounds.height)
        batch.color = previous
    }

    /** Circular in-run pause control sharing the game-speed button's frame. */
    fun drawPauseButton(
        batch: SpriteBatch,
        bounds: Rectangle
    ) {
        val previous = Color(batch.color)
        batch.color = Color.WHITE
        batch.draw(gameSpeedButton, bounds.x, bounds.y, bounds.width, bounds.height)

        batch.color = Color(0.92f, 0.86f, 0.72f, 1f)
        val barWidth = bounds.width * 0.12f
        val barHeight = bounds.height * 0.42f
        val gap = bounds.width * 0.10f
        val centerX = bounds.x + bounds.width / 2f
        val barY = bounds.y + (bounds.height - barHeight) / 2f
        batch.draw(solidPixel, centerX - gap / 2f - barWidth, barY, barWidth, barHeight)
        batch.draw(solidPixel, centerX + gap / 2f, barY, barWidth, barHeight)
        batch.color = previous
    }

    fun drawDangerButton(
        batch: SpriteBatch,
        bounds: Rectangle,
        state: ButtonVisualState
    ) {
        val patch = when (state) {
            ButtonVisualState.HOVERED,
            ButtonVisualState.PRESSED,
            ButtonVisualState.SELECTED -> buttonSelected
            ButtonVisualState.DISABLED -> buttonDisabled
            ButtonVisualState.NORMAL -> buttonNormal
        }
        val tint = when (state) {
            ButtonVisualState.HOVERED,
            ButtonVisualState.PRESSED,
            ButtonVisualState.SELECTED -> Color(1f, 0.34f, 0.28f, 1f)
            ButtonVisualState.DISABLED -> Color(0.64f, 0.48f, 0.48f, 1f)
            ButtonVisualState.NORMAL -> Color(0.86f, 0.18f, 0.16f, 1f)
        }
        drawPatch(batch, patch, bounds, tint)
    }

    fun drawPanel(
        batch: SpriteBatch,
        bounds: Rectangle,
        style: PanelVisualStyle = PanelVisualStyle.ADAPTIVE
    ) {
        val resolvedStyle = if (style == PanelVisualStyle.ADAPTIVE) {
            when {
                bounds.height <= 72f -> PanelVisualStyle.LIST_ROW
                bounds.height <= 155f -> PanelVisualStyle.HORIZONTAL
                else -> PanelVisualStyle.INNER
            }
        } else {
            style
        }

        val (patch, tint) = when (resolvedStyle) {
            PanelVisualStyle.MAIN -> mainPanel to Color.WHITE
            PanelVisualStyle.INNER -> innerPanel to Color(0.82f, 0.86f, 0.92f, 1f)
            PanelVisualStyle.MODAL -> modalPanel to Color(1f, 0.92f, 0.78f, 1f)
            PanelVisualStyle.HORIZONTAL -> horizontalPanel to Color(0.90f, 0.93f, 0.98f, 1f)
            PanelVisualStyle.LIST_ROW -> listRowPanel to Color(0.74f, 0.79f, 0.87f, 1f)
            PanelVisualStyle.TOOLTIP -> tooltipPanel to Color(0.88f, 0.82f, 0.72f, 1f)
            PanelVisualStyle.TITLE -> titleRibbon to Color(1f, 0.88f, 0.62f, 1f)
            PanelVisualStyle.ADAPTIVE -> innerPanel to Color.WHITE
        }

        drawPatch(batch, patch, bounds, tint)
    }

    /** A restrained translucent panel used by the three run-summary columns. */
    fun drawSummarySectionPanel(batch: SpriteBatch, bounds: Rectangle) {
        val previous = Color(batch.color)
        batch.color = faded(Color(0.24f, 0.25f, 0.27f, 0.70f))
        batch.draw(solidPixel, bounds.x, bounds.y, bounds.width, bounds.height)

        val bronze = Color(0.55f, 0.38f, 0.19f, 0.92f)
        val innerBronze = Color(0.34f, 0.25f, 0.16f, 0.82f)
        batch.color = faded(bronze)
        batch.draw(solidPixel, bounds.x, bounds.y, bounds.width, 3f)
        batch.draw(solidPixel, bounds.x, bounds.y + bounds.height - 3f, bounds.width, 3f)
        batch.draw(solidPixel, bounds.x, bounds.y, 3f, bounds.height)
        batch.draw(solidPixel, bounds.x + bounds.width - 3f, bounds.y, 3f, bounds.height)
        batch.color = faded(innerBronze)
        batch.draw(solidPixel, bounds.x + 6f, bounds.y + 6f, bounds.width - 12f, 1f)
        batch.draw(solidPixel, bounds.x + 6f, bounds.y + bounds.height - 7f, bounds.width - 12f, 1f)
        batch.color = previous
    }

    /** Compact bronze progress rail used by Collection category cards. */
    fun drawCollectionProgressBar(
        batch: SpriteBatch,
        bounds: Rectangle,
        progress: Float
    ) {
        val previous = Color(batch.color)
        val value = progress.coerceIn(0f, 1f)

        batch.color = faded(Color(0.055f, 0.05f, 0.045f, 0.96f))
        batch.draw(solidPixel, bounds.x, bounds.y, bounds.width, bounds.height)
        batch.color = faded(Color(0.54f, 0.36f, 0.17f, 0.96f))
        batch.draw(solidPixel, bounds.x, bounds.y, bounds.width, 2f)
        batch.draw(solidPixel, bounds.x, bounds.y + bounds.height - 2f, bounds.width, 2f)

        if (value > 0f) {
            batch.color = faded(Color(0.72f, 0.48f, 0.20f, 1f))
            batch.draw(
                solidPixel,
                bounds.x + 3f,
                bounds.y + 3f,
                (bounds.width - 6f) * value,
                (bounds.height - 6f).coerceAtLeast(1f)
            )
        }
        batch.color = previous
    }

    fun drawSlot(
        batch: SpriteBatch,
        bounds: Rectangle,
        selected: Boolean = false,
        locked: Boolean = false
    ) {
        val texture = when {
            locked -> slotLocked
            selected -> slotSelected
            else -> slotNormal
        }
        val previous = Color(batch.color)
        batch.color = faded(Color.WHITE)
        batch.draw(texture, bounds.x, bounds.y, bounds.width, bounds.height)
        batch.color = previous
    }

    /** Reward-card frame. The square texture is rotated into a diamond in-place. */
    fun drawRotatedRewardSlot(
        batch: SpriteBatch,
        bounds: Rectangle,
        amount: Int,
        selected: Boolean = false
    ) {
        val texture = if (selected) slotSelected else slotNormal
        val previous = Color(batch.color)
        batch.color = when (amount) {
            5 -> Color(0.96f, 0.22f, 0.16f, 1f)
            3 -> Color(1f, 0.78f, 0.12f, 1f)
            2 -> Color(0.20f, 0.58f, 1f, 1f)
            else -> Color(0.28f, 0.90f, 0.36f, 1f)
        }
        batch.draw(
            texture,
            bounds.x,
            bounds.y,
            bounds.width / 2f,
            bounds.height / 2f,
            bounds.width,
            bounds.height,
            1f,
            1f,
            45f,
            0,
            0,
            texture.width,
            texture.height,
            false,
            false
        )
        batch.color = previous
    }

    fun drawHorizontalSeparator(batch: SpriteBatch, bounds: Rectangle) {
        val previous = Color(batch.color)
        batch.color = faded(Color.WHITE)
        batch.draw(horizontalSeparator, bounds.x, bounds.y, bounds.width, bounds.height)
        batch.color = previous
    }

    fun drawVerticalSeparator(batch: SpriteBatch, bounds: Rectangle) {
        val previous = Color(batch.color)
        batch.color = faded(Color.WHITE)
        batch.draw(verticalSeparator, bounds.x, bounds.y, bounds.width, bounds.height)
        batch.color = previous
    }

    /** A restrained bronze rule for layouts where the ornamental divider is too dominant. */
    fun drawSimpleVerticalSeparator(batch: SpriteBatch, bounds: Rectangle) {
        val previous = Color(batch.color)
        val centerX = bounds.x + bounds.width / 2f
        batch.color = Color(0.05f, 0.04f, 0.035f, 0.82f)
        batch.draw(solidPixel, centerX - 2.5f, bounds.y, 5f, bounds.height)
        batch.color = Color(0.64f, 0.47f, 0.27f, 0.72f)
        batch.draw(solidPixel, centerX - 0.75f, bounds.y + 5f, 1.5f, bounds.height - 10f)
        batch.color = Color(0.92f, 0.72f, 0.40f, 0.28f)
        batch.draw(solidPixel, centerX + 0.75f, bounds.y + 10f, 0.75f, bounds.height - 20f)
        batch.color = previous
    }

    fun drawDropdown(
        batch: SpriteBatch,
        bounds: Rectangle,
        open: Boolean
    ) {
        drawPatch(
            batch,
            if (open) dropdownOpen else dropdownClosed,
            bounds,
            Color.WHITE
        )
    }

    fun drawDropdownRow(
        batch: SpriteBatch,
        bounds: Rectangle,
        selected: Boolean
    ) {
        drawPatch(
            batch,
            if (selected) dropdownRowSelected else dropdownRowNormal,
            bounds,
            Color.WHITE
        )
    }

    fun drawCheckbox(
        batch: SpriteBatch,
        bounds: Rectangle,
        checked: Boolean
    ) {
        val texture = if (checked) checkboxChecked else checkboxUnchecked
        val previous = Color(batch.color)
        batch.color = Color.WHITE
        batch.draw(texture, bounds.x, bounds.y, bounds.width, bounds.height)
        batch.color = previous
    }

    fun drawHealthBar(
        batch: SpriteBatch,
        bounds: Rectangle,
        ratio: Float
    ) {
        val clamped = ratio.coerceIn(0f, 1f)
        val horizontalInset = 8f
        val verticalInset = 7f
        val previous = Color(batch.color)
        batch.color = Color.WHITE
        batch.draw(healthBarBackground, bounds.x, bounds.y, bounds.width, bounds.height)
        if (clamped > 0f) {
            val sourceWidth =
                (healthFill.width * clamped).toInt().coerceAtLeast(1)
            batch.draw(
                healthFill,
                bounds.x + horizontalInset,
                bounds.y + verticalInset,
                (bounds.width - horizontalInset * 2f) * clamped,
                (bounds.height - verticalInset * 2f).coerceAtLeast(1f),
                0,
                0,
                sourceWidth,
                healthFill.height,
                false,
                false
            )
        }
        batch.draw(healthBarFrame, bounds.x, bounds.y, bounds.width, bounds.height)
        batch.color = previous
    }

    fun drawCooldownOverlay(
        batch: SpriteBatch,
        bounds: Rectangle,
        ratio: Float
    ) {
        val clamped = ratio.coerceIn(0f, 1f)
        if (clamped <= 0f) return
        val previous = Color(batch.color)
        batch.color = Color.WHITE
        batch.draw(
            cooldownOverlay,
            bounds.x,
            bounds.y,
            bounds.width,
            bounds.height * clamped
        )
        batch.color = previous
    }

    fun drawActiveAbilityFrame(
        batch: SpriteBatch,
        bounds: Rectangle,
        tint: Color = Color.WHITE
    ) {
        val previous = Color(batch.color)
        batch.color = faded(tint)
        batch.draw(
            activeAbilityOverlay,
            bounds.x,
            bounds.y,
            bounds.width,
            bounds.height
        )
        batch.color = previous
    }

    fun drawGameSpeedButton(
        batch: SpriteBatch,
        bounds: Rectangle,
        speed: Float
    ) {
        val previous = Color(batch.color)
        batch.color = when (speed) {
            0.5f -> Color(0.72f, 0.86f, 1f, 1f)
            2f -> Color(1f, 0.64f, 0.32f, 1f)
            else -> Color.WHITE
        }
        batch.draw(gameSpeedButton, bounds.x, bounds.y, bounds.width, bounds.height)
        batch.color = previous
    }

    fun drawToggle(
        batch: SpriteBatch,
        bounds: Rectangle,
        enabled: Boolean,
        selected: Boolean
    ) {
        val texture = when {
            !enabled -> toggleOff
            selected -> toggleOn
            else -> toggleOff
        }
        val previous = Color(batch.color)
        batch.color =
            if (enabled) Color.WHITE
            else Color(0.48f, 0.48f, 0.50f, 0.78f)
        batch.draw(texture, bounds.x, bounds.y, bounds.width, bounds.height)
        batch.color = previous
    }

    fun drawSlider(batch: SpriteBatch, bounds: Rectangle, value: Float, hovered: Boolean = false) {
        val clamped = value.coerceIn(0f, 1f)
        val previous = Color(batch.color)
        batch.color = if (hovered) Color(1.12f, 1.12f, 1.12f, 1f) else Color.WHITE
        batch.draw(sliderTrack, bounds.x, bounds.y, bounds.width, bounds.height)
        val railInset = bounds.width * 0.14f
        val railX = bounds.x + railInset
        val railWidth = bounds.width - railInset * 2f
        val railY = bounds.y + bounds.height * 0.39f
        val railHeight = bounds.height * 0.24f
        if (clamped > 0f) {
            batch.color = if (hovered) Color(0.16f, 0.66f, 1f, 1f) else Color(0.06f, 0.47f, 0.95f, 1f)
            val fillInset = 2f
            batch.draw(
                solidPixel,
                railX + fillInset,
                railY,
                (railWidth - fillInset * 2f) * clamped,
                railHeight
            )
        }
        batch.color = if (hovered) Color(1.12f, 1.12f, 1.12f, 1f) else Color.WHITE
        val thumbHeight = bounds.height * 1.55f
        val thumbWidth = thumbHeight * (sliderThumb.width.toFloat() / sliderThumb.height.toFloat())
        val thumbX = railX + 2f + (railWidth - 4f) * clamped - thumbWidth / 2f
        batch.draw(sliderThumb, thumbX, bounds.y - (thumbHeight - bounds.height) / 2f, thumbWidth, thumbHeight)
        batch.color = previous
    }

    fun drawVerticalScrollbar(
        batch: SpriteBatch,
        bounds: Rectangle,
        visibleRatio: Float,
        scrollRatio: Float
    ) {
        val thumbBounds = verticalScrollbarThumbBounds(bounds, visibleRatio, scrollRatio)
        val previous = Color(batch.color)
        batch.color = Color.WHITE
        batch.draw(verticalScrollbarTrack, bounds.x, bounds.y, bounds.width, bounds.height)
        batch.draw(
            verticalScrollbarThumb,
            thumbBounds.x,
            thumbBounds.y,
            thumbBounds.width,
            thumbBounds.height
        )
        batch.color = previous
    }

    fun verticalScrollbarThumbBounds(
        bounds: Rectangle,
        visibleRatio: Float,
        scrollRatio: Float
    ): Rectangle {
        val clampedVisible = visibleRatio.coerceIn(0.08f, 1f)
        val clampedScroll = scrollRatio.coerceIn(0f, 1f)
        val thumbHeight =
            (bounds.height * clampedVisible)
                .coerceAtLeast(34f)
                .coerceAtMost(bounds.height)
        val travel = (bounds.height - thumbHeight).coerceAtLeast(0f)
        val thumbY = bounds.y + travel * (1f - clampedScroll)
        return Rectangle(bounds.x, thumbY, bounds.width, thumbHeight)
    }

    fun drawChronicleScrollbar(
        batch: SpriteBatch,
        bounds: Rectangle,
        visibleRatio: Float,
        scrollRatio: Float
    ) {
        val thumbBounds = bronzeScrollbarThumbBounds(bounds, visibleRatio, scrollRatio)
        val trackWidth = (bounds.width * 0.48f).coerceAtLeast(8f)
        val trackX = bounds.x + (bounds.width - trackWidth) / 2f
        val previous = Color(batch.color)

        batch.color = faded(Color.WHITE)
        batch.draw(
            chronicleScrollbarTrack,
            trackX,
            bounds.y,
            trackWidth,
            bounds.height
        )
        batch.draw(
            chronicleScrollbarThumb,
            thumbBounds.x,
            thumbBounds.y,
            thumbBounds.width,
            thumbBounds.height
        )
        batch.color = previous
    }

    fun bronzeScrollbarThumbBounds(
        bounds: Rectangle,
        visibleRatio: Float,
        scrollRatio: Float
    ): Rectangle {
        val clampedVisible = visibleRatio.coerceIn(0.08f, 1f)
        val clampedScroll = scrollRatio.coerceIn(0f, 1f)
        val thumbWidth = bounds.width + 8f
        val sourceAspect =
            chronicleScrollbarThumb.regionHeight.toFloat() /
                chronicleScrollbarThumb.regionWidth.toFloat()
        val thumbHeight =
            (thumbWidth * sourceAspect * (0.90f + clampedVisible * 0.20f))
                .coerceIn(56f, bounds.height * 0.24f)
        val travel = (bounds.height - thumbHeight).coerceAtLeast(0f)
        val thumbY = bounds.y + travel * (1f - clampedScroll)
        val thumbX = bounds.x + (bounds.width - thumbWidth) / 2f
        return Rectangle(thumbX, thumbY, thumbWidth, thumbHeight)
    }

    private fun drawPatch(
        batch: SpriteBatch,
        patch: NinePatch,
        bounds: Rectangle,
        tint: Color
    ) {
        val previous = Color(batch.color)
        batch.color = tint
        patch.draw(batch, bounds.x, bounds.y, bounds.width, bounds.height)
        batch.color = previous
    }
}

enum class ButtonVisualState {
    NORMAL,
    HOVERED,
    PRESSED,
    SELECTED,
    DISABLED
}

enum class PanelVisualStyle {
    ADAPTIVE,
    MAIN,
    INNER,
    MODAL,
    HORIZONTAL,
    LIST_ROW,
    TOOLTIP,
    TITLE
}
