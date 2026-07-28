package com.azizoglu.grisilith.screen.preset

import com.azizoglu.grisilith.GrisilithGame
import com.azizoglu.grisilith.content.DefaultContentRegistry
import com.azizoglu.grisilith.preset.PresetConstants
import com.azizoglu.grisilith.preset.PresetContentResolver
import com.azizoglu.grisilith.preset.PresetRepository
import com.azizoglu.grisilith.preset.PresetRepositoryResult
import com.azizoglu.grisilith.preset.ResolvedPresetContent
import com.azizoglu.grisilith.preset.RunPreset
import com.azizoglu.grisilith.screen.components.MenuButton
import com.azizoglu.grisilith.screen.components.MenuPointer
import com.azizoglu.grisilith.statistics.StatType
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

class PresetManagerScreen(
    private val game:
    GrisilithGame
) : ScreenAdapter() {

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
        BitmapFont()

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
            game.showPlayMenu()
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
            handlePresetListDrag()

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
            game.showPresetDifficultySelection(
                preset.preset.presetId
            )
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
                        preset.armor.displayName
                    }

                    PresetCardContentType.ITEM -> {
                        preset.item.displayName
                    }

                    PresetCardContentType.WEAPON_TAMGA -> {
                        "No Weapon Tamga"
                    }

                    PresetCardContentType.ARMOR_TAMGA -> {
                        "No Armor Tamga"
                    }
                }

            drawPanel(
                rectangle =
                    rectangle,

                fillColor =
                    when (contentType) {
                        PresetCardContentType.WEAPON -> {
                            Color(
                                0.08f,
                                0.34f,
                                0.12f,
                                1f
                            )
                        }

                        PresetCardContentType.ARMOR -> {
                            Color(
                                0.28f,
                                0.28f,
                                0.31f,
                                1f
                            )
                        }

                        PresetCardContentType.ITEM -> {
                            Color(
                                0.25f,
                                0.23f,
                                0.10f,
                                1f
                            )
                        }

                        else -> {
                            Color(
                                0.25f,
                                0.13f,
                                0.40f,
                                1f
                            )
                        }
                    }
            )

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
                        Color.GRAY
                    } else {
                        Color.WHITE
                    },

                scale =
                    0.64f
            )
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

        val stats =
            StatType.entries

        val rowsPerColumn =
            (
                stats.size +
                    1
                ) /
                2

        stats.forEachIndexed {
                index,
                statType ->

            val column =
                index /
                    rowsPerColumn

            val row =
                index %
                    rowsPerColumn

            val x =
                315f +
                    column *
                    340f

            val y =
                510f -
                    row *
                    32f

            font.color =
                Color.LIGHT_GRAY

            font.draw(
                batch,
                statType.displayName,
                x,
                y
            )

            font.color =
                Color.WHITE

            font.draw(
                batch,
                (
                    preset.totalStats[
                        statType
                    ] ?: 0
                    )
                    .toString(),
                x +
                    245f,
                y,
                50f,
                Align.right,
                false
            )
        }

        renderModalCloseHint()
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
            0.9f
        )

        font.color =
            Color.LIGHT_GRAY

        font.draw(
            batch,
            preset.character.description,
            300f,
            500f,
            680f,
            Align.left,
            true
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            "Minions: " +
                preset.character
                    .minionDescription,
            300f,
            355f,
            680f,
            Align.left,
            true
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
                title =
                    preset.armor.displayName

                description =
                    preset.armor.description

                details =
                    preset.armor
                        .allStatModifiers()
                        .joinToString(
                            separator = "\n"
                        ) { modifier ->
                            "${modifier.statType.displayName}: " +
                                signed(
                                    modifier.amount
                                )
                        }
            }

            PresetCardContentType.ITEM -> {
                title =
                    preset.item.displayName

                description =
                    preset.item.description

                details =
                    preset.item
                        .statModifiers
                        .joinToString(
                            separator = "\n"
                        ) { modifier ->
                            "${modifier.statType.displayName}: " +
                                signed(
                                    modifier.amount
                                )
                        }
            }

            PresetCardContentType.WEAPON_TAMGA -> {
                title =
                    "WEAPON TAMGA"

                description =
                    "No Weapon Tamga selected."

                details =
                    "Tamga system has not been implemented yet."
            }

            PresetCardContentType.ARMOR_TAMGA -> {
                title =
                    "ARMOR TAMGA"

                description =
                    "No Armor Tamga selected."

                details =
                    "Tamga system has not been implemented yet."
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

        font.draw(
            batch,
            description,
            300f,
            505f,
            680f,
            Align.left,
            true
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            details,
            300f,
            330f,
            680f,
            Align.left,
            true
        )

        renderModalCloseHint()
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
            105f

        val height =
            67f

        val startX =
            card.x +
                370f

        val gap =
            12f

        return linkedMapOf(
            PresetCardContentType.WEAPON to
                Rectangle(
                    startX,
                    card.y +
                        37f,
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
                        37f,
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
                        37f,
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
                        37f,
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
                        37f,
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
                965f,
            card.y +
                68f,
            120f,
            36f
        )

    private fun editButtonRectangle(
        card:
        Rectangle
    ) =
        Rectangle(
            card.x +
                965f,
            card.y +
                20f,
            55f,
            35f
        )

    private fun deleteButtonRectangle(
        card:
        Rectangle
    ) =
        Rectangle(
            card.x +
                1030f,
            card.y +
                20f,
            55f,
            35f
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
        drawFilledRectangle(
            rectangle,
            Color(
                0.34f,
                0.52f,
                0.73f,
                1f
            )
        )

        drawFilledRectangle(
            Rectangle(
                rectangle.x +
                    2f,
                rectangle.y +
                    2f,
                rectangle.width -
                    4f,
                rectangle.height -
                    4f
            ),
            fillColor
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
