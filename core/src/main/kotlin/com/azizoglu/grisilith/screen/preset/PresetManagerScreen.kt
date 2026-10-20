package com.azizoglu.grisilith.screen.preset

import com.azizoglu.grisilith.GrisilithGame
import com.azizoglu.grisilith.content.DefaultContentRegistry
import com.azizoglu.grisilith.config.GameMap
import com.azizoglu.grisilith.tamga.definition.TamgaActivationType
import com.azizoglu.grisilith.tamga.definition.TamgaDefinition
import com.azizoglu.grisilith.tamga.definition.TamgaSlotType
import com.azizoglu.grisilith.preset.PresetConstants
import com.azizoglu.grisilith.preset.PresetContentResolver
import com.azizoglu.grisilith.preset.PresetRepository
import com.azizoglu.grisilith.preset.PresetRepositoryResult
import com.azizoglu.grisilith.preset.ResolvedPresetContent
import com.azizoglu.grisilith.preset.RunPreset
import com.azizoglu.grisilith.screen.components.MenuButton
import com.azizoglu.grisilith.screen.components.MenuPointer
import com.azizoglu.grisilith.statistics.StatType
import com.azizoglu.grisilith.statistics.StatPresentationGroup
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.FitViewport
import com.azizoglu.grisilith.ui.icon.ContentIconRenderer
import com.azizoglu.grisilith.ui.stat.StatSymbolRenderer
import com.azizoglu.grisilith.ui.theme.UiComponentRenderer
import com.azizoglu.grisilith.ui.input.VerticalScrollbarDragController
import com.azizoglu.grisilith.content.ContentTag
import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.weapon.definition.WeaponAttackPattern
import com.azizoglu.grisilith.weapon.definition.WeaponDefinition
import com.azizoglu.grisilith.weapon.runtime.WeaponDamageCalculator
import com.azizoglu.grisilith.weapon.runtime.WeaponInstance
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import java.util.Locale
import kotlin.math.roundToInt
import com.azizoglu.grisilith.ui.typography.UiFontFactory

class PresetManagerScreen(
    private val game:
    GrisilithGame,

    private val selectedMap:
    GameMap? = null
) : ScreenAdapter() {

    private val scrollbarDrag = VerticalScrollbarDragController()

    companion object {
        private const val VIEWPORT_WIDTH =
            1280f

        private const val VIEWPORT_HEIGHT =
            720f

        private const val VISIBLE_PRESET_COUNT =
            4

        private const val CARD_HEIGHT =
            118f

        private const val CARD_GAP =
            15f

        private const val LIST_TOP =
            535f

        private const val DRAG_SCROLL_STEP =
            55f

        private const val DRAG_START_THRESHOLD =
            8f
    }

    private val camera =
        OrthographicCamera()

    private val viewport =
        FitViewport(
            VIEWPORT_WIDTH,
            VIEWPORT_HEIGHT,
            camera
        )

    private val batch =
        SpriteBatch()

    private val font =
        UiFontFactory.createBody()

    private val glyphLayout =
        GlyphLayout()

    private val pointer =
        MenuPointer(
            viewport
        )

    private val whitePixel =
        createWhitePixel()

    private val interactionState =
        PresetManagerInteractionState()

    private val contentResolver =
        PresetContentResolver(
            DefaultContentRegistry.instance
        )

    private var previousInputProcessor:
        InputProcessor? =
        null

    private var resolvedPresets:
        List<ResolvedPresetContent> =
        emptyList()

    private var statusMessage:
        String? =
        null

    private var statusMessageRemainingSeconds =
        0f

    private var isDraggingPresetList =
        false

    private var presetListDragStarted =
        false

    private var dragStartY =
        0f

    private var previousDragY =
        0f

    private var accumulatedDragY =
        0f

    private val backButton =
        MenuButton(
            text =
                "BACK",

            x =
                35f,

            y =
                630f,

            width =
                170f,

            height =
                55f
        ) {
            if (
                selectedMap != null
            ) {
                game.showMapSelection()
            } else {
                game.showPlayMenu()
            }
        }

    private val createPresetButton =
        MenuButton(
            text =
                "CREATE PRESET",

            x =
                975f,

            y =
                630f,

            width =
                270f,

            height =
                55f
        ) {
            game.showCreatePreset()
        }

    private val navigationButtons =
        listOf(
            backButton,
            createPresetButton
        )

    private val cardRectangles:
        List<Rectangle>
        get() =
            List(
                VISIBLE_PRESET_COUNT
            ) { visibleIndex ->
                Rectangle(
                    55f,
                    LIST_TOP -
                        CARD_HEIGHT -
                        visibleIndex *
                        (
                            CARD_HEIGHT +
                                CARD_GAP
                            ),
                    1100f,
                    CARD_HEIGHT
                )
            }

    private fun presetListRectangle():
        Rectangle {

        val rectangles =
            cardRectangles

        val bottom =
            rectangles.last().y

        return Rectangle(
            rectangles.first().x,
            bottom,
            rectangles.first().width,
            LIST_TOP -
                bottom
        )
    }

    override fun show() {
        viewport.apply()

        camera.position.set(
            VIEWPORT_WIDTH / 2f,
            VIEWPORT_HEIGHT / 2f,
            0f
        )

        camera.update()

        pointer.reset()
        interactionState.reset()
        scrollbarDrag.reset()

        isDraggingPresetList =
            false

        presetListDragStarted =
            false

        accumulatedDragY =
            0f

        refreshPresets()

        previousInputProcessor =
            Gdx.input.inputProcessor

        Gdx.input.inputProcessor =
            object : InputAdapter() {

                override fun scrolled(
                    amountX:
                    Float,

                    amountY:
                    Float
                ): Boolean {

                    if (
                        interactionState
                            .isModalOpen
                    ) {
                        return true
                    }

                    scrollBy(
                        if (amountY > 0f) {
                            1
                        } else {
                            -1
                        }
                    )

                    return true
                }
            }
    }

    override fun render(
        delta:
        Float
    ) {
        pointer.update()

        updateStatusMessage(
            delta
        )

        if (
            interactionState
                .isModalOpen
        ) {
            handleModalInput()
        } else {
            handleNormalInput()
        }

        renderScreen()
    }

    private fun handleNormalInput() {
        /*
         * Preset listesi sürükleniyorsa diğer
         * butonlar ve kart tıklamaları çalışmaz.
         */
        val dragConsumedInput =
            handlePresetScrollbarDrag() || handlePresetListDrag()

        if (dragConsumedInput) {
            return
        }

        navigationButtons
            .forEach { button ->
                val screenChanged =
                    button.update(
                        pointerPosition =
                            pointer.position,

                        pointerJustPressed =
                            pointer.justPressed,

                        pointerJustReleased =
                            pointer.justReleased
                    )

                if (screenChanged) {
                    return
                }
            }

        if (!pointer.justReleased) {
            return
        }

        val visiblePresets =
            visiblePresets()

        cardRectangles
            .forEachIndexed {
                    visibleIndex,
                    cardRectangle ->

                val preset =
                    visiblePresets
                        .getOrNull(
                            visibleIndex
                        )
                        ?: return@forEachIndexed

                handleCardClick(
                    preset =
                        preset,

                    card =
                        cardRectangle
                )
            }
    }

    private fun handlePresetScrollbarDrag(): Boolean {
        val maximum = maximumOffset()
        if (maximum <= 0) {
            scrollbarDrag.reset()
            return false
        }
        val listBounds = presetListRectangle()
        val track =
            Rectangle(
                listBounds.x + listBounds.width + 10f,
                listBounds.y,
                16f,
                listBounds.height
            )
        val visibleRatio = VISIBLE_PRESET_COUNT.toFloat() / resolvedPresets.size.toFloat()
        val thumb =
            UiComponentRenderer.verticalScrollbarThumbBounds(
                track,
                visibleRatio,
                interactionState.scrollOffset.toFloat() / maximum.toFloat()
            )
        val interactionTrack = Rectangle(track.x - 5f, track.y, track.width + 10f, track.height)
        return scrollbarDrag.update(
            pointer.position,
            pointer.justPressed,
            pointer.isHeld,
            pointer.justReleased,
            interactionTrack,
            thumb
        ) { ratio ->
            interactionState.setScrollOffset((ratio * maximum).roundToInt(), maximum)
        }
    }

    private fun handlePresetListDrag():
        Boolean {

        val pointerInsideList =
            presetListRectangle()
                .contains(
                    pointer.position
                )

        /*
         * Preset listesinin içinde basıldı.
         */
        if (
            pointer.justPressed &&
            pointerInsideList
        ) {
            isDraggingPresetList =
                true

            presetListDragStarted =
                false

            dragStartY =
                pointer.position.y

            previousDragY =
                pointer.position.y

            accumulatedDragY =
                0f

            /*
             * İlk basma frame'inde arkadaki kart
             * tıklanmasın.
             */
            return true
        }

        if (!isDraggingPresetList) {
            return false
        }

        /*
         * Fare basılı tutulurken hareket miktarını
         * topluyoruz.
         */
        if (pointer.isHeld) {
            val currentY =
                pointer.position.y

            val totalDistance =
                kotlin.math.abs(
                    currentY -
                        dragStartY
                )

            if (
                totalDistance >=
                DRAG_START_THRESHOLD
            ) {
                presetListDragStarted =
                    true
            }

            if (presetListDragStarted) {
                val frameMovement =
                    currentY -
                        previousDragY

                accumulatedDragY +=
                    frameMovement

                /*
                 * Liste yukarı sürüklenirse daha
                 * aşağıdaki Preset'ler gösterilir.
                 */
                while (
                    accumulatedDragY <=
                    -DRAG_SCROLL_STEP
                ) {
                    scrollBy(
                        1
                    )

                    accumulatedDragY +=
                        DRAG_SCROLL_STEP
                }

                /*
                 * Liste aşağı sürüklenirse daha
                 * yukarıdaki Preset'lere dönülür.
                 */
                while (
                    accumulatedDragY >=
                    DRAG_SCROLL_STEP
                ) {
                    scrollBy(
                        -1
                    )

                    accumulatedDragY -=
                        DRAG_SCROLL_STEP
                }
            }

            previousDragY =
                currentY

            return true
        }

        /*
         * Fare bırakıldı.
         *
         * Gerçek bir sürükleme yapıldıysa bırakma
         * işlemi kart tıklaması olarak kullanılmaz.
         * Hareket edilmediyse normal kart tıklamasına
         * izin verilir.
         */
        if (pointer.justReleased) {
            val wasActualDrag =
                presetListDragStarted

            isDraggingPresetList =
                false

            presetListDragStarted =
                false

            accumulatedDragY =
                0f

            return wasActualDrag
        }

        return true
    }

    private fun handleCardClick(
        preset:
        ResolvedPresetContent,

        card:
        Rectangle
    ) {
        val portrait =
            portraitRectangle(
                card
            )

        if (
            portrait.contains(
                pointer.position
            )
        ) {
            interactionState
                .openCharacter(
                    preset.preset.presetId
                )

            return
        }

        val contentRectangles =
            contentRectangles(
                card
            )

        for (
        (
            contentType,
            rectangle
        ) in contentRectangles
        ) {
            if (
                rectangle.contains(
                    pointer.position
                )
            ) {
                interactionState
                    .openContent(
                        presetId =
                            preset.preset.presetId,

                        contentType =
                            contentType
                    )

                return
            }
        }

        if (
            statsButtonRectangle(
                card
            ).contains(
                pointer.position
            )
        ) {
            interactionState
                .openStats(
                    preset.preset.presetId
                )

            return
        }

        if (
            editButtonRectangle(
                card
            ).contains(
                pointer.position
            )
        ) {
            game.showEditPreset(
                preset.preset.presetId
            )

            return
        }

        if (
            deleteButtonRectangle(
                card
            ).contains(
                pointer.position
            )
        ) {
            interactionState
                .openDeleteConfirmation(
                    preset.preset.presetId
                )

            return
        }

        if (
            selectButtonRectangle(
                card
            ).contains(
                pointer.position
            )
        ) {
            if (
                selectedMap != null
            ) {
                game.showPresetDifficultySelection(
                    presetId =
                        preset.preset.presetId,

                    gameMap =
                        selectedMap
                )
            } else {
                game.showPresetMapSelection(
                    preset.preset.presetId
                )
            }
        }
    }

    private fun handleModalInput() {
        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.ESCAPE
            )
        ) {
            interactionState.closeModal()
            return
        }

        if (!pointer.justReleased) {
            return
        }

        val modal =
            interactionState.modal

        if (
            modal is
                PresetManagerModal
                .DeleteConfirmation
        ) {
            when {
                modalConfirmRectangle()
                    .contains(
                        pointer.position
                    ) -> {
                    deletePreset(
                        modal.presetId
                    )
                }

                modalCancelRectangle()
                    .contains(
                        pointer.position
                    ) -> {
                    interactionState
                        .closeModal()
                }
            }

            return
        }

        /*
         * Detay kartının içine basmak bir şey yapmaz.
         * Dışına basmak yalnızca kartı kapatır.
         */
        if (
            !modalRectangle()
                .contains(
                    pointer.position
                )
        ) {
            interactionState.closeModal()
        }
    }

    private fun deletePreset(
        presetId:
        String
    ) {
        when (
            val result =
                PresetRepository.delete(
                    presetId
                )
        ) {
            is PresetRepositoryResult.Success -> {
                interactionState.closeModal()
                refreshPresets()

                showStatus(
                    "PRESET DELETED"
                )
            }

            is PresetRepositoryResult.Failure -> {
                interactionState.closeModal()

                showStatus(
                    result.message
                )
            }
        }
    }

    private fun refreshPresets() {
        resolvedPresets =
            PresetRepository
                .allPresets()
                .mapNotNull { preset ->
                    contentResolver.resolve(
                        preset
                    )
                }

        interactionState.clampScroll(
            maximumOffset()
        )
    }

    private fun visiblePresets():
        List<ResolvedPresetContent> {

        return resolvedPresets
            .drop(
                interactionState
                    .scrollOffset
            )
            .take(
                VISIBLE_PRESET_COUNT
            )
    }

    private fun maximumOffset():
        Int {

        return (
            resolvedPresets.size -
                VISIBLE_PRESET_COUNT
            )
            .coerceAtLeast(
                0
            )
    }

    private fun scrollBy(
        amount:
        Int
    ) {
        interactionState.scroll(
            amount =
                amount,

            maximumOffset =
                maximumOffset()
        )
    }

    private fun renderScreen() {
        Gdx.gl.glClearColor(
            0.035f,
            0.025f,
            0.055f,
            1f
        )

        Gdx.gl.glClear(
            GL20.GL_COLOR_BUFFER_BIT
        )

        batch.projectionMatrix =
            camera.combined

        batch.begin()

        renderHeader()
        renderPresetList()

        navigationButtons
            .forEach { button ->
                button.render(
                    batch =
                        batch,

                    font =
                        font,

                    pointerPosition =
                        pointer.position,

                    pointerHeld =
                        pointer.isHeld
                )
            }

        renderStatusMessage()

        if (
            interactionState
                .isModalOpen
        ) {
            renderModal()
        }

        batch.end()
    }

    private fun renderHeader() {
        font.color =
            Color.WHITE

        font.data.setScale(
            1.8f
        )

        font.draw(
            batch,
            "PRESET MANAGER",
            0f,
            670f,
            VIEWPORT_WIDTH,
            Align.center,
            false
        )

        font.data.setScale(
            1f
        )

        font.color =
            Color.LIGHT_GRAY

        font.draw(
            batch,
            "Choose Preset  " +
                "(${resolvedPresets.size}/" +
                "${PresetConstants.MAXIMUM_PRESET_COUNT})",
            0f,
            590f,
            VIEWPORT_WIDTH,
            Align.center,
            false
        )
    }

    private fun renderPresetList() {
        if (resolvedPresets.isEmpty()) {
            font.color =
                Color.GRAY

            font.draw(
                batch,
                "NO SAVED PRESETS\n\n" +
                    "Use CREATE PRESET to prepare one.",
                0f,
                390f,
                VIEWPORT_WIDTH,
                Align.center,
                true
            )

            return
        }

        val visiblePresets =
            visiblePresets()

        cardRectangles
            .forEachIndexed {
                    visibleIndex,
                    rectangle ->

                val preset =
                    visiblePresets
                        .getOrNull(
                            visibleIndex
                        )
                        ?: return@forEachIndexed

                renderPresetCard(
                    preset =
                        preset,

                    card =
                        rectangle
                )
            }

        if (resolvedPresets.size > VISIBLE_PRESET_COUNT) {
            val listBounds = presetListRectangle()
            val maximum = maximumOffset().coerceAtLeast(1)
            UiComponentRenderer.drawVerticalScrollbar(
                batch = batch,
                bounds = Rectangle(listBounds.x + listBounds.width + 10f, listBounds.y, 16f, listBounds.height),
                visibleRatio = VISIBLE_PRESET_COUNT.toFloat() / resolvedPresets.size.toFloat(),
                scrollRatio = interactionState.scrollOffset.toFloat() / maximum.toFloat()
            )
        }

        font.color =
            Color.GRAY

        font.data.setScale(
            0.75f
        )

        font.draw(
            batch,
            "Mouse Wheel or Drag the List",
            900f,
            38f
        )

        font.data.setScale(
            1f
        )
    }

    private fun renderPresetCard(
        preset:
        ResolvedPresetContent,

        card:
        Rectangle
    ) {
        drawPanel(
            rectangle =
                card,

            fillColor =
                Color(
                    0.06f,
                    0.30f,
                    0.34f,
                    1f
                )
        )

        val portrait =
            portraitRectangle(
                card
            )

        drawPanel(
            rectangle =
                portrait,

            fillColor =
                Color(
                    0.35f,
                    0.10f,
                    0.45f,
                    1f
                )
        )

        if (
            !ContentIconRenderer.draw(
                batch = batch,
                definition = preset.character,
                bounds = portrait
            )
        ) {
            drawCenteredText(
                text =
                    "CHARACTER\nPORTRAIT",

                rectangle =
                    portrait,

                color =
                    Color.WHITE,

                scale =
                    0.7f
            )
        }

        font.color =
            Color.WHITE

        font.data.setScale(
            0.88f
        )

        font.draw(
            batch,
            preset.preset.presetName,
            card.x +
                125f,
            card.y +
                card.height -
                19f
        )

        font.color =
            Color.CYAN

        font.draw(
            batch,
            preset.character.displayName,
            card.x +
                125f,
            card.y +
                card.height -
                48f,
            235f,
            Align.left,
            true
        )

        contentRectangles(
            card
        ).forEach {
                contentType,
                rectangle ->

            val text =
                when (contentType) {
                    PresetCardContentType.WEAPON -> {
                        preset.weapon.displayName
                    }

                    PresetCardContentType.ARMOR -> {
                        preset.armor
                            ?.displayName
                            ?: "No Armor"
                    }

                    PresetCardContentType.ITEM -> {
                        preset.item
                            ?.displayName
                            ?: "No Item"
                    }

                    PresetCardContentType.WEAPON_TAMGA -> {
                        preset.weaponTamga
                            ?.displayName
                            ?: "No Weapon Tamga"
                    }

                    PresetCardContentType.ARMOR_TAMGA -> {
                        preset.armorTamga
                            ?.displayName
                            ?: "No Armor Tamga"
                    }
                }

            val definition =
                when (contentType) {
                    PresetCardContentType.WEAPON -> preset.weapon
                    PresetCardContentType.ARMOR -> preset.armor
                    PresetCardContentType.ITEM -> preset.item
                    PresetCardContentType.WEAPON_TAMGA -> preset.weaponTamga
                    PresetCardContentType.ARMOR_TAMGA -> preset.armorTamga
                }

            UiComponentRenderer.drawSlot(
                batch = batch,
                bounds = rectangle,
                locked = definition == null
            )

            val renderedIcon =
                definition != null &&
                    ContentIconRenderer.draw(
                        batch = batch,
                        definition = definition,
                        bounds = rectangle
                    )

            if (!renderedIcon) {
                drawCenteredText(
                    text =
                        text,

                    rectangle =
                        rectangle,

                    color =
                        if (
                            contentType ==
                            PresetCardContentType
                                .WEAPON_TAMGA ||
                            contentType ==
                            PresetCardContentType
                                .ARMOR_TAMGA
                        ) {
                            val hasTamga =
                                when (contentType) {
                                    PresetCardContentType
                                        .WEAPON_TAMGA ->
                                        preset.weaponTamga != null

                                    PresetCardContentType
                                        .ARMOR_TAMGA ->
                                        preset.armorTamga != null

                                    else ->
                                        true
                                }

                            if (hasTamga) {
                                Color.WHITE
                            } else {
                                Color.GRAY
                            }
                        } else {
                            Color.WHITE
                        },

                    scale =
                        0.64f
                )
            }
        }

        renderSmallButton(
            text =
                "STATS",

            rectangle =
                statsButtonRectangle(
                    card
                )
        )

        renderSmallButton(
            text =
                "EDIT",

            rectangle =
                editButtonRectangle(
                    card
                )
        )

        renderSmallButton(
            text =
                "DELETE",

            rectangle =
                deleteButtonRectangle(
                    card
                )
        )

        renderSmallButton(
            text =
                "SELECT PRESET",

            rectangle =
                selectButtonRectangle(
                    card
                )
        )

        font.data.setScale(
            1f
        )
    }

    private fun renderModal() {
        drawFilledRectangle(
            rectangle =
                Rectangle(
                    0f,
                    0f,
                    VIEWPORT_WIDTH,
                    VIEWPORT_HEIGHT
                ),

            color =
                Color(
                    0f,
                    0f,
                    0f,
                    0.72f
                )
        )

        val modal =
            interactionState.modal

        when (modal) {
            is PresetManagerModal.Stats -> {
                renderStatsModal(
                    modal.presetId
                )
            }

            is PresetManagerModal.Character -> {
                renderCharacterModal(
                    modal.presetId
                )
            }

            is PresetManagerModal.Content -> {
                renderContentModal(
                    presetId =
                        modal.presetId,

                    contentType =
                        modal.contentType
                )
            }

            is PresetManagerModal
            .DeleteConfirmation -> {
                renderDeleteConfirmation(
                    modal.presetId
                )
            }

            PresetManagerModal.None -> {
            }
        }
    }

    private fun renderStatsModal(
        presetId:
        String
    ) {
        val preset =
            presetById(
                presetId
            ) ?: return

        drawPanel(
            modalRectangle()
        )

        font.color =
            Color.WHITE

        font.data.setScale(
            1.3f
        )

        font.draw(
            batch,
            "${preset.preset.presetName} — STATS",
            300f,
            560f
        )

        font.data.setScale(
            0.9f
        )

        var hoveredStat:
            StatType? =
            null

        StatPresentationGroup.ordered.forEachIndexed { index, group ->
            val column = index % 2
            val gridRow = index / 2
            val bounds = Rectangle(
                300f + column * 350f,
                145f + (1 - gridRow) * 190f,
                330f,
                178f
            )
            drawPanel(bounds)
            font.data.setScale(0.68f)
            font.color = Color(0.72f, 0.78f, 0.90f, 1f)
            font.draw(batch, group.title, bounds.x, bounds.y + bounds.height - 10f, bounds.width, Align.center, false)
            group.stats.forEachIndexed { rowIndex, statType ->
                val x = bounds.x + 12f
                val y = bounds.y + bounds.height - 42f - rowIndex * 24f
                val valueRectangle = Rectangle(bounds.x + bounds.width - 66f, y - 19f, 54f, 23f)
                val isHovered = valueRectangle.contains(pointer.position)
                font.data.setScale(0.72f)
                StatSymbolRenderer.drawLabel(batch, font, statType, x, y, 16f, bounds.width - 82f)
                font.color = if (isHovered) Color.GOLD else Color.WHITE
                font.draw(
                    batch,
                    (preset.totalStats[statType] ?: 0).toString(),
                    bounds.x + bounds.width - 62f,
                    y,
                    48f,
                    Align.right,
                    false
                )
                if (isHovered) hoveredStat = statType
            }
        }

        renderModalCloseHint()

        if (hoveredStat != null) {
            renderStatBreakdownTooltip(
                preset =
                    preset,

                statType =
                    hoveredStat
            )
        }
    }

    private fun renderStatBreakdownTooltip(
        preset:
        ResolvedPresetContent,

        statType:
        StatType
    ) {
        val total =
            preset.totalStats[
                statType
            ] ?: 0

        val base =
            preset.baseStats[
                statType
            ] ?: 0

        val gear =
            preset.gearBonuses[
                statType
            ] ?: 0

        val presetPoints =
            preset.presetStatBonuses[
                statType
            ] ?: 0

        val tooltipWidth =
            335f

        val tooltipHeight =
            92f

        val desiredX =
            pointer.position.x +
                18f

        val desiredY =
            pointer.position.y -
                tooltipHeight -
                12f

        val x =
            desiredX.coerceIn(
                modalRectangle().x +
                    10f,

                modalRectangle().x +
                    modalRectangle().width -
                    tooltipWidth -
                    10f
            )

        val y =
            desiredY.coerceIn(
                modalRectangle().y +
                    10f,

                modalRectangle().y +
                    modalRectangle().height -
                    tooltipHeight -
                    10f
            )

        val tooltip =
            Rectangle(
                x,
                y,
                tooltipWidth,
                tooltipHeight
            )

        drawFilledRectangle(
            Rectangle(
                tooltip.x +
                    4f,
                tooltip.y -
                    4f,
                tooltip.width,
                tooltip.height
            ),
            Color(
                0f,
                0f,
                0f,
                0.45f
            )
        )

        drawPanel(
            rectangle =
                tooltip,

            fillColor =
                Color(
                    0.055f,
                    0.075f,
                    0.12f,
                    1f
                )
        )

        font.data.setScale(
            0.83f
        )

        StatSymbolRenderer.drawLabel(
            batch = batch,
            font = font,
            statType = statType,
            x = tooltip.x + 13f,
            baselineY = tooltip.y + tooltip.height - 17f,
            iconSize = 18f,
            width = tooltip.width - 26f
        )

        font.data.setScale(
            0.72f
        )

        font.color =
            Color.LIGHT_GRAY

        font.draw(
            batch,
            "$total = $base Base " +
                signedPart(
                    gear
                ) +
                " Gear " +
                signedPart(
                    presetPoints
                ) +
                " Stat Points",
            tooltip.x +
                13f,
            tooltip.y +
                31f,
            tooltip.width -
                26f,
            Align.left,
            true
        )

        font.data.setScale(
            1f
        )
    }

    private fun signedPart(
        amount:
        Int
    ): String {

        return if (amount >= 0) {
            "+ $amount"
        } else {
            "- ${kotlin.math.abs(amount)}"
        }
    }

    private fun renderCharacterModal(
        presetId:
        String
    ) {
        val preset =
            presetById(
                presetId
            ) ?: return

        drawPanel(
            modalRectangle()
        )

        font.color =
            Color.CYAN

        font.data.setScale(
            1.25f
        )

        font.draw(
            batch,
            preset.character.displayName,
            300f,
            555f
        )

        font.data.setScale(
            0.92f
        )

        font.color =
            Color.LIGHT_GRAY

        font.draw(
            batch,
            preset.character.trait.displayName +
                ": " +
                preset.character.trait.description,
            300f,
            500f,
            680f,
            Align.left,
            true
        )

        font.data.setScale(
            0.78f
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            "Base Stats",
            300f,
            350f
        )

        val baseStatPreview =
            StatType.grouped
                .joinToString(
                    separator =
                        "   |   "
                ) { statType ->
                    "${statType.displayName}: " +
                        (
                            preset.baseStats[
                                statType
                            ] ?: 0
                            )
                }

        font.color =
            Color.LIGHT_GRAY

        StatSymbolRenderer.drawRichText(
            batch, font, baseStatPreview,
            300f, 315f, 680f,
            lineHeight = 18f,
            iconSize = 15f
        )

        renderModalCloseHint()
    }

    private fun renderContentModal(
        presetId:
        String,

        contentType:
        PresetCardContentType
    ) {
        val preset =
            presetById(
                presetId
            ) ?: return

        if (
            contentType ==
            PresetCardContentType.WEAPON
        ) {
            renderWeaponContentModal(
                preset
            )

            return
        }

        drawPanel(
            modalRectangle()
        )

        val title: String
        val description: String
        val details: String

        when (contentType) {
            PresetCardContentType.WEAPON -> {
                title =
                    preset.weapon.displayName

                description =
                    preset.weapon.description

                details =
                    preset.weapon
                        .passiveStatModifiers
                        .joinToString(
                            separator = "\n"
                        ) { modifier ->
                            "${modifier.statType.displayName}: " +
                                signed(
                                    modifier.amount
                                )
                        }
                        .ifBlank {
                            "No passive Stat bonuses."
                        }
            }

            PresetCardContentType.ARMOR -> {

                val armor =
                    preset.armor

                if (
                    armor == null
                ) {
                    title =
                        "NO ARMOR"

                    description =
                        "This Preset starts without Armor."

                    details =
                        if (!preset.character.canEquipArmor) {
                            "Cannot equip Armor.\nBase Health: ${preset.character.unarmoredBaseHealth}\nBase Armor: ${preset.character.unarmoredBaseArmor}\nBase Spell Protection: ${preset.character.unarmoredBaseMagicResistance}"
                        } else "This character starts with no Armor bonuses."
                } else {
                    title =
                        armor.displayName

                    description =
                        armor.description

                    details = com.azizoglu.grisilith.armor.definition.ArmorPresentation.stats(armor)
                }
            }

            PresetCardContentType.ITEM -> {

                val item =
                    preset.item

                if (
                    item == null
                ) {
                    title =
                        "NO ITEM"

                    description =
                        "This Preset starts without an Item."

                    details =
                        "Items can be obtained during the run."
                } else {
                    title =
                        item.displayName

                    description =
                        item.description

                    details =
                        item
                            .statModifiers
                            .joinToString(
                                separator =
                                    "\n"
                            ) { modifier ->

                                "${modifier.statType.displayName}: " +
                                    signed(
                                        modifier.amount
                                    )
                            }
                            .ifBlank {
                                "No Stat bonuses."
                            }
                }
            }

            PresetCardContentType.WEAPON_TAMGA -> {
                val tamga =
                    preset.weaponTamga

                title =
                    tamga
                        ?.displayName
                        ?: "WEAPON TAMGA"

                description =
                    tamga
                        ?.description
                        ?: "No Weapon Tamga selected."

                details =
                    tamga
                        ?.let {
                            tamgaDetailsText(
                                it
                            )
                        }
                        ?: "Empty Tamga slot."
            }

            PresetCardContentType.ARMOR_TAMGA -> {
                val tamga =
                    preset.armorTamga

                title =
                    tamga
                        ?.displayName
                        ?: "ARMOR TAMGA"

                description =
                    tamga
                        ?.description
                        ?: "No Armor Tamga selected."

                details =
                    tamga
                        ?.let {
                            tamgaDetailsText(
                                it
                            )
                        }
                        ?: "Empty Tamga slot."
            }
        }

        font.color =
            Color.WHITE

        font.data.setScale(
            1.3f
        )

        font.draw(
            batch,
            title,
            300f,
            555f
        )

        font.data.setScale(
            0.9f
        )

        font.color =
            Color.LIGHT_GRAY

        StatSymbolRenderer.drawRichText(
            batch, font, description,
            300f, 505f, 680f,
            lineHeight = 21f,
            iconSize = 18f
        )

        font.color =
            Color.WHITE

        StatSymbolRenderer.drawRichText(
            batch, font, details,
            300f, 330f, 680f,
            lineHeight = 20f,
            iconSize = 17f
        )

        renderModalCloseHint()
    }


    private fun tamgaDetailsText(
        tamga:
        TamgaDefinition
    ): String {

        val slotText =
            when (tamga.slotType) {
                TamgaSlotType.WEAPON ->
                    "Weapon Tamga"

                TamgaSlotType.ARMOR ->
                    "Armor Tamga"

                TamgaSlotType.GENERAL ->
                    "General Tamga"
            }

        val activationText =
            when (tamga.activationType) {
                TamgaActivationType.ACTIVE ->
                    "Active"

                TamgaActivationType.PASSIVE ->
                    "Passive"
            }

        return buildString {
            appendLine(
                "Type: $slotText"
            )

            appendLine(
                "Activation: $activationText"
            )

            if (
                tamga.requiredWeaponTags
                    .isNotEmpty()
            ) {
                appendLine(
                    "Required Weapon: " +
                        tamga.requiredWeaponTags
                            .joinToString {
                                tagDisplayName(
                                    it
                                )
                            }
                )
            }

            if (
                tamga.requiredArmorTags
                    .isNotEmpty()
            ) {
                appendLine(
                    "Required Armor: " +
                        tamga.requiredArmorTags
                            .joinToString {
                                tagDisplayName(
                                    it
                                )
                            }
                )
            }

            if (
                tamga.interactableStats
                    .isNotEmpty()
            ) {
                appendLine(
                    "Interactable Stats: " +
                        tamga.interactableStats
                            .joinToString {
                                it.displayName
                            }
                )
            }

            if (
                tamga.statModifiers
                    .isNotEmpty()
            ) {
                appendLine()
                appendLine(
                    "Stat Modifiers:"
                )

                tamga.statModifiers
                    .forEach { modifier ->
                        appendLine(
                            "${modifier.statType.displayName}: " +
                                signed(
                                    modifier.amount
                                )
                        )
                    }
            }
        }
            .trim()
            .ifBlank {
                "No direct Stat modifiers."
            }
    }

    private fun tagDisplayName(
        tag:
        ContentTag
    ): String {

        return tag.name
            .lowercase(
                Locale.US
            )
            .split(
                "_"
            )
            .joinToString(
                separator =
                    " "
            ) { word ->
                word.replaceFirstChar {
                    it.uppercase(
                        Locale.US
                    )
                }
            }
    }

    private fun renderWeaponContentModal(
        preset:
        ResolvedPresetContent
    ) {
        val panel =
            Rectangle(
                250f,
                175f,
                780f,
                370f
            )

        drawPanel(
            rectangle =
                panel,

            fillColor =
                Color(
                    0.07f,
                    0.10f,
                    0.16f,
                    1f
                )
        )

        val weapon =
            WeaponInstance(
                definition =
                    preset.weapon,

                level =
                    1
            )

        val playerStats =
            createPresetPreviewPlayerStats(
                preset
            )

        val definition =
            weapon.definition

        val levelStats =
            weapon.currentLevelStats

        val damageCalculator =
            WeaponDamageCalculator(
                playerStats =
                    playerStats
            )

        val damage =
            damageCalculator
                .calculateNonCriticalDamage(
                    weapon
                )

        val attackSpeedStatMultiplier =
            when (
                definition.normalAttackSpeedStat
            ) {
                StatType.HASTE -> {
                    playerStats
                        .hasteAttackSpeedMultiplier()
                }

                else -> {
                    playerStats
                        .attackSpeedMultiplier()
                }
            }

        val attacksPerSecond =
            definition.baseAttacksPerSecond *
                levelStats.attackSpeedMultiplier *
                attackSpeedStatMultiplier

        val range =
            definition.baseRange *
                levelStats.rangeMultiplier

        val knockback =
            definition.baseKnockback *
                levelStats.knockbackMultiplier

        val leftX =
            panel.x +
                24f

        val rightX =
            panel.x +
                440f

        var lineY =
            panel.y +
                panel.height -
                35f

        font.data.setScale(
            1.25f
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            definition.displayName,
            leftX,
            lineY
        )

        lineY -=
            38f

        font.data.setScale(
            0.92f
        )

        font.color =
            Color.GOLD

        font.draw(
            batch,
            "Level I",
            leftX,
            lineY
        )

        lineY -=
            31f

        font.color =
            Color.LIGHT_GRAY

        font.draw(
            batch,
            "Weapon Type: " +
                weaponTypeText(
                    definition
                ),
            leftX,
            lineY
        )

        lineY -=
            27f

        renderMainScalingLine(
            definition =
                definition,

            x =
                leftX,

            y =
                lineY
        )

        lineY -=
            27f

        font.color =
            Color.LIGHT_GRAY

        font.draw(
            batch,
            secondaryScalingText(
                definition
            ),
            leftX,
            lineY
        )

        lineY -=
            27f

        font.draw(
            batch,
            "Attack Model: " +
                attackModelText(
                    weapon
                ),
            leftX,
            lineY
        )

        lineY -=
            27f

        font.color =
            Color.WHITE

        font.draw(
            batch,
            "Damage: $damage",
            leftX,
            lineY
        )

        lineY -=
            25f

        font.draw(
            batch,
            "Attacks per Second: " +
                formatDecimal(
                    attacksPerSecond
                ),
            leftX,
            lineY
        )

        lineY -=
            25f

        font.draw(
            batch,
            "Range: " +
                range.roundToInt(),
            leftX,
            lineY
        )

        lineY -=
            25f

        if (knockback > 0f) {
            font.draw(
                batch,
                "Knockback: " +
                    knockback.roundToInt(),
                leftX,
                lineY
            )

            lineY -=
                25f
        }

        font.draw(
            batch,
            "Piercing: " +
                playerStats.totalScore(
                    StatType.PIERCING
                ),
            leftX,
            lineY
        )

        lineY -=
            25f

        font.draw(
            batch,
            "Critical Rate: " +
                formatDecimal(
                    playerStats
                        .criticalHitChance() *
                        100f
                ) +
                "%",
            leftX,
            lineY
        )

        lineY -=
            25f

        font.draw(
            batch,
            "Critical Damage: " +
                formatDecimal(
                    playerStats
                        .criticalDamageMultiplier() *
                        100f
                ) +
                "%",
            leftX,
            lineY
        )

        font.color =
            Color.LIGHT_GRAY

        font.data.setScale(
            0.94f
        )

        StatSymbolRenderer.drawRichText(
            batch, font, definition.description,
            rightX,
            panel.y + panel.height - 75f,
            panel.width - 465f
        )

        font.color =
            Color.GRAY

        font.data.setScale(
            0.70f
        )

        font.draw(
            batch,
            "Click outside to close.",
            panel.x +
                20f,
            panel.y +
                18f
        )

        font.data.setScale(
            1f
        )
    }

    private fun createPresetPreviewPlayerStats(
        preset:
        ResolvedPresetContent
    ): PlayerStats {

        /*
         * Character base + Preset Stat Point değerleri
         * PlayerStats'in base katmanına verilir.
         *
         * Armor, Item ve pasif Weapon bonusları
         * Gear katmanına verilir.
         */
        val preRunBaseStats =
            StatType.alphabetical
                .associateWith { statType ->
                    val base =
                        preset.baseStats[
                            statType
                        ] ?: 0

                    val presetBonus =
                        preset.presetStatBonuses[
                            statType
                        ] ?: 0

                    base +
                        presetBonus
                }

        return PlayerStats(
            initialBaseScores =
                preRunBaseStats,

            characterTraitId =
                preset.character.trait.id,
            canEquipArmor = preset.character.canEquipArmor,
            unarmoredBaseHealth = preset.character.unarmoredBaseHealth,
            unarmoredBaseArmor = preset.character.unarmoredBaseArmor,
            unarmoredBaseMagicResistance = preset.character.unarmoredBaseMagicResistance
        ).apply {
            setArmorBaseValues(
                preset.armor?.baseHealth ?: preset.character.unarmoredBaseHealth,
                preset.armor?.baseArmor ?: preset.character.unarmoredBaseArmor,
                preset.armor?.baseMagicResistance ?: preset.character.unarmoredBaseMagicResistance
            )
            setGearBonuses(
                preset.gearBonuses
            )
        }
    }

    private fun renderMainScalingLine(
        definition:
        WeaponDefinition,

        x:
        Float,

        y:
        Float
    ) {
        val mainScalingStat =
            definition
                .orderedDamageScaling()
                .firstOrNull()
                ?.first

        val label =
            "Main Damage Scaling: "

        font.color =
            Color.GOLD

        font.draw(
            batch,
            label,
            x,
            y
        )

        glyphLayout.setText(
            font,
            label
        )

        if (mainScalingStat != null) {
            font.color =
                scalingStatColor(
                    mainScalingStat
                )

            font.draw(
                batch,
                mainScalingStat.displayName,
                x +
                    glyphLayout.width,
                y
            )
        } else {
            font.color =
                Color.GRAY

            font.draw(
                batch,
                "None",
                x +
                    glyphLayout.width,
                y
            )
        }
    }

    private fun scalingStatColor(
        statType:
        StatType
    ): Color {

        return when (statType) {
            StatType.STRENGTH -> {
                Color(
                    0.95f,
                    0.20f,
                    0.17f,
                    1f
                )
            }

            StatType.DEXTERITY -> {
                Color(
                    1f,
                    0.82f,
                    0.12f,
                    1f
                )
            }

            StatType.WISDOM -> {
                Color(
                    0.55f,
                    0.18f,
                    0.72f,
                    1f
                )
            }

            else -> {
                Color.CYAN
            }
        }
    }

    private fun secondaryScalingText(
        definition:
        WeaponDefinition
    ): String {

        val secondaryScaling =
            definition
                .orderedDamageScaling()
                .drop(1)

        return if (
            secondaryScaling.isEmpty()
        ) {
            "Secondary Scaling: None"
        } else {
            "Secondary Scaling: " +
                secondaryScaling.joinToString(
                    separator =
                        ", "
                ) { scaling ->
                    scaling.first.displayName
                }
        }
    }

    private fun weaponTypeText(
        definition:
        WeaponDefinition
    ): String {

        return when {
            ContentTag.SPELL_FOCUS in
                definition.tags -> {
                "Spell Focus"
            }

            ContentTag.MELEE_WEAPON in
                definition.tags -> {
                "Melee Weapon"
            }

            ContentTag.RANGED_WEAPON in
                definition.tags -> {
                "Ranged Weapon"
            }

            else -> {
                "Weapon"
            }
        }
    }

    private fun attackModelText(
        weapon:
        WeaponInstance
    ): String {

        if (
            weapon.displayName
                .lowercase(
                    Locale.US
                ) ==
            "double swords"
        ) {
            return "X"
        }

        return when (
            weapon.definition
                .attackPattern
        ) {
            WeaponAttackPattern.SHORT_SLASH_LINES ->
                "Slash"

            WeaponAttackPattern.FORWARD_CONE ->
                "Cone"

            WeaponAttackPattern.TARGET_AREA_CIRCLE ->
                "Circle"

            WeaponAttackPattern.MELEE_OR_THROW ->
                "Stab / Throw"

            WeaponAttackPattern.STRAIGHT_PROJECTILE ->
                "Projectile"

            WeaponAttackPattern.PIERCING_PROJECTILE ->
                "Piercing Projectile"

            WeaponAttackPattern.LONG_PIERCING_LINE ->
                "Piercing Line"

            WeaponAttackPattern.SHIELD_CONE ->
                "Shield Cone"

            WeaponAttackPattern.TOME_PROJECTILE_OR_BEAM ->
                "Projectile / Beam"

            WeaponAttackPattern.TOTEM_PROJECTILE_OR_DRAIN ->
                "Projectile / Drain"

            WeaponAttackPattern.EXTENDED_PHYSICAL ->
                com.azizoglu.grisilith.weapon.definition.catalog.NewPhysicalWeapons.patternLabel(weapon.definition.id.value)
            WeaponAttackPattern.NONE ->
                "Passive"
        }
    }

    private fun formatDecimal(
        value:
        Float
    ): String {

        return String.format(
            Locale.US,
            "%.2f",
            value
        )
    }

    private fun renderDeleteConfirmation(
        presetId:
        String
    ) {
        val preset =
            presetById(
                presetId
            ) ?: return

        drawPanel(
            modalRectangle(),

            fillColor =
                Color(
                    0.17f,
                    0.07f,
                    0.08f,
                    1f
                )
        )

        font.color =
            Color.WHITE

        font.data.setScale(
            1.3f
        )

        font.draw(
            batch,
            "DELETE PRESET?",
            0f,
            540f,
            VIEWPORT_WIDTH,
            Align.center,
            false
        )

        font.data.setScale(
            0.95f
        )

        font.color =
            Color.LIGHT_GRAY

        font.draw(
            batch,
            "\"${preset.preset.presetName}\" will be permanently deleted.",
            0f,
            455f,
            VIEWPORT_WIDTH,
            Align.center,
            false
        )

        renderSmallButton(
            text =
                "CONFIRM",

            rectangle =
                modalConfirmRectangle(),

            fillColor =
                Color(
                    0.48f,
                    0.13f,
                    0.13f,
                    1f
                )
        )

        renderSmallButton(
            text =
                "CANCEL",

            rectangle =
                modalCancelRectangle()
        )
    }

    private fun presetById(
        presetId:
        String
    ): ResolvedPresetContent? {

        return resolvedPresets
            .firstOrNull { preset ->
                preset.preset.presetId ==
                    presetId
            }
    }

    private fun portraitRectangle(
        card:
        Rectangle
    ): Rectangle {

        return Rectangle(
            card.x +
                12f,

            card.y +
                14f,

            95f,

            card.height -
                28f
        )
    }

    private fun contentRectangles(
        card:
        Rectangle
    ): Map<PresetCardContentType, Rectangle> {

        val width =
            82f

        val height =
            82f

        val startX =
            card.x +
                390f

        val gap =
            12f

        return linkedMapOf(
            PresetCardContentType.WEAPON to
                Rectangle(
                    startX,
                    card.y +
                        18f,
                    width,
                    height
                ),

            PresetCardContentType.ARMOR to
                Rectangle(
                    startX +
                        (
                            width +
                                gap
                            ),
                    card.y +
                        18f,
                    width,
                    height
                ),

            PresetCardContentType.ITEM to
                Rectangle(
                    startX +
                        (
                            width +
                                gap
                            ) *
                        2f,
                    card.y +
                        18f,
                    width,
                    height
                ),

            PresetCardContentType.WEAPON_TAMGA to
                Rectangle(
                    startX +
                        (
                            width +
                                gap
                            ) *
                        3f,
                    card.y +
                        18f,
                    width,
                    height
                ),

            PresetCardContentType.ARMOR_TAMGA to
                Rectangle(
                    startX +
                        (
                            width +
                                gap
                            ) *
                        4f,
                    card.y +
                        18f,
                    width,
                    height
                )
        )
    }

    private fun statsButtonRectangle(
        card:
        Rectangle
    ) =
        Rectangle(
            card.x +
                930f,
            card.y +
                68f,
            155f,
            34f
        )

    private fun editButtonRectangle(
        card:
        Rectangle
    ) =
        Rectangle(
            card.x +
                930f,
            card.y +
                20f,
            74f,
            34f
        )

    private fun deleteButtonRectangle(
        card:
        Rectangle
    ) =
        Rectangle(
            card.x +
                1011f,
            card.y +
                20f,
            74f,
            34f
        )

    private fun selectButtonRectangle(
        card:
        Rectangle
    ) =
        Rectangle(
            card.x +
                125f,
            card.y +
                15f,
            225f,
            32f
        )

    private fun modalRectangle() =
        Rectangle(
            270f,
            145f,
            740f,
            430f
        )

    private fun modalConfirmRectangle() =
        Rectangle(
            390f,
            205f,
            220f,
            58f
        )

    private fun modalCancelRectangle() =
        Rectangle(
            670f,
            205f,
            220f,
            58f
        )

    private fun renderModalCloseHint() {
        font.color =
            Color.GRAY

        font.data.setScale(
            0.75f
        )

        font.draw(
            batch,
            "Click outside to close.",
            295f,
            172f
        )

        font.data.setScale(
            1f
        )
    }

    private fun renderSmallButton(
        text:
        String,

        rectangle:
        Rectangle,

        fillColor:
        Color =
            Color(
                0.10f,
                0.18f,
                0.29f,
                1f
            )
    ) {
        drawPanel(
            rectangle =
                rectangle,

            fillColor =
                fillColor
        )

        drawCenteredText(
            text =
                text,

            rectangle =
                rectangle,

            color =
                Color.WHITE,

            scale =
                0.7f
        )
    }

    private fun drawPanel(
        rectangle:
        Rectangle,

        fillColor:
        Color =
            Color(
                0.07f,
                0.10f,
                0.15f,
                1f
            )
    ) {
        UiComponentRenderer.drawPanel(
            batch = batch,
            bounds = rectangle
        )
    }

    private fun drawCenteredText(
        text:
        String,

        rectangle:
        Rectangle,

        color:
        Color,

        scale:
        Float
    ) {
        val oldScaleX =
            font.data.scaleX

        val oldScaleY =
            font.data.scaleY

        font.data.setScale(
            scale
        )

        font.color =
            color

        font.draw(
            batch,
            text,
            rectangle.x +
                4f,
            rectangle.y +
                rectangle.height / 2f +
                6f,
            rectangle.width -
                8f,
            Align.center,
            true
        )

        font.data.setScale(
            oldScaleX,
            oldScaleY
        )
    }

    private fun drawFilledRectangle(
        rectangle:
        Rectangle,

        color:
        Color
    ) {
        batch.color =
            color

        batch.draw(
            whitePixel,
            rectangle.x,
            rectangle.y,
            rectangle.width,
            rectangle.height
        )

        batch.color =
            Color.WHITE
    }

    private fun createWhitePixel():
        Texture {

        val pixmap =
            Pixmap(
                1,
                1,
                Pixmap.Format.RGBA8888
            )

        pixmap.setColor(
            Color.WHITE
        )

        pixmap.fill()

        val texture =
            Texture(
                pixmap
            )

        pixmap.dispose()

        return texture
    }

    private fun signed(
        amount:
        Int
    ): String {

        return if (amount >= 0) {
            "+$amount"
        } else {
            amount.toString()
        }
    }

    private fun showStatus(
        message:
        String
    ) {
        statusMessage =
            message

        statusMessageRemainingSeconds =
            2.5f
    }

    private fun updateStatusMessage(
        delta:
        Float
    ) {
        if (
            statusMessageRemainingSeconds <=
            0f
        ) {
            return
        }

        statusMessageRemainingSeconds -=
            delta

        if (
            statusMessageRemainingSeconds <=
            0f
        ) {
            statusMessage =
                null
        }
    }

    private fun renderStatusMessage() {
        val message =
            statusMessage
                ?: return

        font.color =
            Color.GOLD

        font.data.setScale(
            0.85f
        )

        font.draw(
            batch,
            message,
            0f,
            118f,
            VIEWPORT_WIDTH,
            Align.center,
            false
        )

        font.data.setScale(
            1f
        )
    }

    override fun resize(
        width:
        Int,

        height:
        Int
    ) {
        if (
            width <= 0 ||
            height <= 0
        ) {
            return
        }

        viewport.update(
            width,
            height,
            true
        )
    }

    override fun hide() {
        if (
            Gdx.input.inputProcessor !=
            previousInputProcessor
        ) {
            Gdx.input.inputProcessor =
                previousInputProcessor
        }

        previousInputProcessor =
            null
    }

    override fun dispose() {
        hide()

        whitePixel.dispose()
        batch.dispose()
        font.dispose()
    }
}
