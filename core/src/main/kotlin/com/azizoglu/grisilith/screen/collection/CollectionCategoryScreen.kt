package com.azizoglu.grisilith.screen

import com.azizoglu.grisilith.GrisilithGame
import com.azizoglu.grisilith.collection.CollectionEntry
import com.azizoglu.grisilith.collection.CollectionProvider
import com.azizoglu.grisilith.collection.filter.CollectionFilterEngine
import com.azizoglu.grisilith.collection.filter.CollectionFilterOption
import com.azizoglu.grisilith.collection.filter.CollectionFilterRegistry
import com.azizoglu.grisilith.collection.filter.CollectionFilterState
import com.azizoglu.grisilith.content.ContentCategory
import com.azizoglu.grisilith.screen.components.MenuButton
import com.azizoglu.grisilith.screen.components.MenuPointer
import com.azizoglu.grisilith.screen.render.CollectionCategoryRenderer
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.viewport.FitViewport
import kotlin.math.ceil

class CollectionCategoryScreen(
    private val game: GrisilithGame,
    private val category: ContentCategory
) : ScreenAdapter() {

    companion object {

        private const val VIEWPORT_WIDTH =
            1280f

        private const val VIEWPORT_HEIGHT =
            720f

        private const val COLUMN_COUNT =
            5

        private const val ROW_COUNT =
            3

        private const val ENTRIES_PER_PAGE =
            COLUMN_COUNT * ROW_COUNT

        private const val CARD_WIDTH =
            160f

        private const val CARD_HEIGHT =
            150f

        private const val CARD_HORIZONTAL_SPACING =
            24f

        private const val CARD_VERTICAL_SPACING =
            20f

        private const val GRID_START_X =
            192f

        /*
         * İlk satırdaki kartların alt Y koordinatı.
         */
        private const val GRID_START_Y =
            430f

        private const val DETAIL_PANEL_X =
            180f

        private const val DETAIL_PANEL_Y =
            115f

        private const val DETAIL_PANEL_WIDTH =
            920f

        private const val DETAIL_PANEL_HEIGHT =
            455f

        private const val FILTER_HEADER_X =
            955f

        private const val FILTER_HEADER_Y =
            585f

        private const val FILTER_HEADER_WIDTH =
            245f

        private const val FILTER_HEADER_HEIGHT =
            44f

        private const val FILTER_OPTION_HEIGHT =
            38f
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

    private val shapeRenderer =
        ShapeRenderer()

    private val font =
        BitmapFont()

    private val pointer =
        MenuPointer(
            viewport
        )

    private val renderer =
        CollectionCategoryRenderer()

    private val filterOptions:
        List<CollectionFilterOption> =
        CollectionFilterRegistry.optionsFor(
            category
        )

    private val filterState =
        CollectionFilterState(
            filterOptions
        )

    private val filterHeaderBounds =
        Rectangle(
            FILTER_HEADER_X,
            FILTER_HEADER_Y,
            FILTER_HEADER_WIDTH,
            FILTER_HEADER_HEIGHT
        )

    /*
     * Registry'den gelen, filtre uygulanmamış ana liste.
     */
    private var entries:
        List<CollectionEntry> =
        emptyList()

    /*
     * Filtre sonrası indeks tutmak yerine Content ID tutulur.
     * Böylece filtre değişince yanlış kartın seçilmesi önlenir.
     */
    private var selectedContentId:
        String? =
        null

    private var currentPage =
        0

    private var cardBounds:
        List<Rectangle> =
        emptyList()

    private val detailPanelBounds =
        Rectangle(
            DETAIL_PANEL_X,
            DETAIL_PANEL_Y,
            DETAIL_PANEL_WIDTH,
            DETAIL_PANEL_HEIGHT
        )

    private val backButton =
        MenuButton(
            text =
                "BACK",

            x =
                40f,

            y =
                35f,

            width =
                180f,

            height =
                60f
        ) {
            game.showCollection()
        }

    private val previousPageButton =
        MenuButton(
            text =
                "<",

            x =
                470f,

            y =
                35f,

            width =
                90f,

            height =
                60f
        ) {
            previousPage()
        }

    private val nextPageButton =
        MenuButton(
            text =
                ">",

            x =
                720f,

            y =
                35f,

            width =
                90f,

            height =
                60f
        ) {
            nextPage()
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

        /*
         * Kategori ekranı her açıldığında filtreler sıfırlanır.
         */
        filterState.reset()

        selectedContentId =
            null

        currentPage =
            0

        refreshEntries()
    }

    override fun render(
        delta: Float
    ) {

        pointer.update()

        if (
            handleKeyboardInput()
        ) {
            return
        }

        /*
         * Input önceliği:
         *
         * 1. Detay paneli
         * 2. Filtre
         * 3. Geri ve sayfa butonları
         * 4. Collection kartları
         */
        val detailConsumedInput =
            updateDetailModalInput()

        val filterConsumedInput =
            if (
                detailConsumedInput
            ) {
                false
            } else {
                updateFilterInput()
            }

        if (
            !detailConsumedInput &&
            !filterConsumedInput
        ) {

            if (
                updateBackButton()
            ) {
                return
            }

            updatePageButtons()
            updateCards()
        }

        Gdx.gl.glClearColor(
            0.035f,
            0.025f,
            0.055f,
            1f
        )

        Gdx.gl.glClear(
            GL20.GL_COLOR_BUFFER_BIT
        )

        shapeRenderer.projectionMatrix =
            camera.combined

        batch.projectionMatrix =
            camera.combined

        val visibleEntries =
            visibleEntries()

        val selectedEntry =
            selectedEntry()

        /*
         * ==================================================
         * PASS 1 — ANA ARKA PLANLAR VE ÇERÇEVELER
         * ==================================================
         *
         * Kart kareleri ve detay paneli burada çizilir.
         */
        renderer.renderCardBackgrounds(
            shapeRenderer =
                shapeRenderer,

            entries =
                visibleEntries,

            cardBounds =
                cardBounds,

            selectedLocalIndex =
                selectedLocalIndex()
        )

        if (
            selectedEntry != null
        ) {
            renderer.renderDetailBackground(
                shapeRenderer =
                    shapeRenderer,

                panelBounds =
                    detailPanelBounds,

                entry =
                    selectedEntry
            )
        }

        /*
         * ==================================================
         * PASS 2 — ANA METİNLER VE BUTONLAR
         * ==================================================
         */
        batch.begin()

        renderer.renderTexts(
            batch =
                batch,

            font =
                font,

            category =
                category,

            entries =
                visibleEntries,

            cardBounds =
                cardBounds,

            currentPage =
                currentPage,

            pageCount =
                pageCount(),

            selectedEntry =
                selectedEntry,

            detailPanelBounds =
                detailPanelBounds,

            filterOptions =
                filterOptions,

            filterHeaderBounds =
                filterHeaderBounds,

            filterOptionBounds =
                filterOptionBounds(),

            filterDropdownOpen =
                filterState.isDropdownOpen,

            selectedFilterIds =
                filterState.selectedOptionIds
        )

        backButton.render(
            batch =
                batch,

            font =
                font,

            pointerPosition =
                pointer.position,

            pointerHeld =
                pointer.isHeld
        )

        if (
            pageCount() > 1 &&
            selectedEntry == null
        ) {
            previousPageButton.render(
                batch =
                    batch,

                font =
                    font,

                pointerPosition =
                    pointer.position,

                pointerHeld =
                    pointer.isHeld
            )

            nextPageButton.render(
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

        batch.end()

        /*
         * ==================================================
         * PASS 3 — FİLTRE OVERLAY
         * ==================================================
         *
         * Filtre en son çizildiği için kartların,
         * kart yazılarının ve butonların üzerinde kalır.
         *
         * Detay paneli açıkken filtre çizilmez.
         */
        if (
            filterOptions.isNotEmpty() &&
            selectedEntry == null
        ) {

            renderer.renderFilterBackgrounds(
                shapeRenderer =
                    shapeRenderer,

                headerBounds =
                    filterHeaderBounds,

                optionBounds =
                    filterOptionBounds(),

                dropdownOpen =
                    filterState.isDropdownOpen,

                selectedOptionIds =
                    filterState.selectedOptionIds,

                options =
                    filterOptions
            )

            batch.begin()

            renderer.renderFilterTexts(
                batch =
                    batch,

                font =
                    font,

                options =
                    filterOptions,

                headerBounds =
                    filterHeaderBounds,

                optionBounds =
                    filterOptionBounds(),

                dropdownOpen =
                    filterState.isDropdownOpen,

                selectedOptionIds =
                    filterState.selectedOptionIds
            )

            batch.end()
        }
    }

    /*
     * true dönerse ekran değişmiştir ve render() bitmelidir.
     */
    private fun handleKeyboardInput():
        Boolean {

        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.ESCAPE
            )
        ) {

            when {

                selectedContentId != null -> {
                    selectedContentId =
                        null
                }

                filterState.isDropdownOpen -> {
                    filterState.closeDropdown()
                }

                else -> {
                    game.showCollection()
                    return true
                }
            }

            return false
        }

        /*
         * Modal veya dropdown açıkken yön tuşları
         * alttaki sayfalara etki etmez.
         */
        if (
            selectedContentId != null ||
            filterState.isDropdownOpen
        ) {
            return false
        }

        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.LEFT
            )
        ) {
            previousPage()
        }

        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.RIGHT
            )
        ) {
            nextPage()
        }

        return false
    }

    /*
     * Detay paneli modal gibi davranır:
     *
     * - Panel içine tıklama hiçbir şey yapmaz.
     * - Panel dışına tıklama paneli kapatır.
     * - Aynı tıklama alttaki UI'a aktarılmaz.
     */
    private fun updateDetailModalInput():
        Boolean {

        if (
            selectedContentId == null ||
            !pointer.justReleased
        ) {
            return false
        }

        if (
            !detailPanelBounds.contains(
                pointer.position
            )
        ) {
            selectedContentId =
                null
        }

        return true
    }

    private fun updateFilterInput():
        Boolean {

        if (
            filterOptions.isEmpty() ||
            selectedEntry() != null ||
            !pointer.justReleased
        ) {
            return false
        }

        /*
         * Başlığa basınca dropdown açılır/kapanır.
         */
        if (
            filterHeaderBounds.contains(
                pointer.position
            )
        ) {
            filterState.toggleDropdown()
            return true
        }

        if (
            !filterState.isDropdownOpen
        ) {
            return false
        }

        val optionIndex =
            filterOptionBounds()
                .indexOfFirst {
                        bounds ->

                    bounds.contains(
                        pointer.position
                    )
                }

        if (
            optionIndex >= 0
        ) {
            val changed =
                filterState.toggle(
                    filterOptions[
                        optionIndex
                    ].id
                )

            if (changed) {
                onFiltersChanged()
            }

            /*
             * Seçim sonrası dropdown bilerek açık kalır.
             */
            return true
        }

        /*
         * Dropdown dışına basılırsa kapanır.
         * Bu tıklama alttaki karta veya butona geçmez.
         */
        filterState.closeDropdown()

        return true
    }

    private fun updateBackButton():
        Boolean {

        return backButton.update(
            pointerPosition =
                pointer.position,

            pointerJustPressed =
                pointer.justPressed,

            pointerJustReleased =
                pointer.justReleased
        )
    }

    private fun updatePageButtons() {

        if (
            pageCount() <= 1
        ) {
            return
        }

        previousPageButton.update(
            pointerPosition =
                pointer.position,

            pointerJustPressed =
                pointer.justPressed,

            pointerJustReleased =
                pointer.justReleased
        )

        nextPageButton.update(
            pointerPosition =
                pointer.position,

            pointerJustPressed =
                pointer.justPressed,

            pointerJustReleased =
                pointer.justReleased
        )
    }

    private fun updateCards() {

        if (
            !pointer.justReleased
        ) {
            return
        }

        val localIndex =
            cardBounds.indexOfFirst {
                    bounds ->

                bounds.contains(
                    pointer.position
                )
            }

        if (
            localIndex == -1
        ) {
            return
        }

        val globalIndex =
            currentPage *
                ENTRIES_PER_PAGE +
                localIndex

        val filteredEntries =
            filteredEntries()

        if (
            globalIndex !in
            filteredEntries.indices
        ) {
            return
        }

        val selectedEntry =
            filteredEntries[
                globalIndex
            ]

        /*
         * Hiç karşılaşılmamış Map veya Enemy
         * detay panelini açamaz.
         */
        if (
            selectedEntry.isUnknown
        ) {
            return
        }

        selectedContentId =
            selectedEntry
                .definition
                .id
                .value
    }

    private fun selectedEntry():
        CollectionEntry? {

        val selectedId =
            selectedContentId
                ?: return null

        return filteredEntries()
            .firstOrNull {
                    entry ->

                entry.definition.id.value ==
                    selectedId
            }
    }

    private fun refreshEntries() {

        entries =
            CollectionProvider.entries(
                category
            )

        currentPage =
            currentPage.coerceIn(
                0,
                lastPageIndex()
            )

        val selectedId =
            selectedContentId

        if (
            selectedId != null &&
            filteredEntries().none {
                    entry ->

                entry.definition.id.value ==
                    selectedId
            }
        ) {
            selectedContentId =
                null
        }

        rebuildCardBounds()
    }

    private fun filteredEntries():
        List<CollectionEntry> {

        return CollectionFilterEngine.apply(
            entries =
                entries,

            options =
                filterOptions,

            selectedOptionIds =
                filterState.selectedOptionIds
        )
    }

    private fun visibleEntries():
        List<CollectionEntry> {

        val filteredEntries =
            filteredEntries()

        val startIndex =
            currentPage *
                ENTRIES_PER_PAGE

        if (
            startIndex >=
            filteredEntries.size
        ) {
            return emptyList()
        }

        val endIndex =
            minOf(
                startIndex +
                    ENTRIES_PER_PAGE,

                filteredEntries.size
            )

        return filteredEntries.subList(
            startIndex,
            endIndex
        )
    }

    private fun rebuildCardBounds() {

        cardBounds =
            visibleEntries()
                .indices
                .map {
                        localIndex ->

                    val column =
                        localIndex %
                            COLUMN_COUNT

                    val row =
                        localIndex /
                            COLUMN_COUNT

                    val x =
                        GRID_START_X +
                            column *
                            (
                                CARD_WIDTH +
                                    CARD_HORIZONTAL_SPACING
                                )

                    val y =
                        GRID_START_Y -
                            row *
                            (
                                CARD_HEIGHT +
                                    CARD_VERTICAL_SPACING
                                )

                    Rectangle(
                        x,
                        y,
                        CARD_WIDTH,
                        CARD_HEIGHT
                    )
                }
    }

    private fun onFiltersChanged() {

        currentPage =
            0

        selectedContentId =
            null

        rebuildCardBounds()
    }

    private fun filterOptionBounds():
        List<Rectangle> {

        if (
            !filterState.isDropdownOpen
        ) {
            return emptyList()
        }

        return filterOptions.mapIndexed {
                index,
                _ ->

            Rectangle(
                FILTER_HEADER_X,
                FILTER_HEADER_Y -
                    FILTER_OPTION_HEIGHT *
                    (
                        index +
                            1
                        ),
                FILTER_HEADER_WIDTH,
                FILTER_OPTION_HEIGHT
            )
        }
    }

    private fun previousPage() {

        if (
            currentPage <= 0
        ) {
            return
        }

        currentPage -=
            1

        selectedContentId =
            null

        rebuildCardBounds()
    }

    private fun nextPage() {

        if (
            currentPage >=
            lastPageIndex()
        ) {
            return
        }

        currentPage +=
            1

        selectedContentId =
            null

        rebuildCardBounds()
    }

    private fun pageCount():
        Int {

        val filteredSize =
            filteredEntries().size

        if (
            filteredSize == 0
        ) {
            return 1
        }

        return ceil(
            filteredSize.toFloat() /
                ENTRIES_PER_PAGE.toFloat()
        ).toInt()
    }

    private fun lastPageIndex():
        Int {

        return (
            pageCount() -
                1
            ).coerceAtLeast(
                0
            )
    }

    private fun selectedLocalIndex():
        Int? {

        val selectedId =
            selectedContentId
                ?: return null

        return visibleEntries()
            .indexOfFirst {
                    entry ->

                entry.definition.id.value ==
                    selectedId
            }
            .takeIf {
                it >= 0
            }
    }

    override fun resize(
        width: Int,
        height: Int
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

    override fun dispose() {

        batch.dispose()
        shapeRenderer.dispose()
        font.dispose()
    }
}
