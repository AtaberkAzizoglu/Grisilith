package com.azizoglu.grisilith.screen

import com.azizoglu.grisilith.GrisilithGame
import com.azizoglu.grisilith.collection.CollectionEntry
import com.azizoglu.grisilith.collection.CollectionProvider
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


    private var entries:
        List<CollectionEntry> =
        emptyList()

    private var selectedIndex:
        Int? =
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

        refreshEntries()
    }

    override fun render(
        delta: Float
    ) {

        pointer.update()

        handleKeyboardInput()

        if (
            updateBackButton()
        ) {
            return
        }

        updatePageButtons()
        updateCards()

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

        selectedEntry()
            ?.let { selectedEntry ->

                renderer.renderDetailBackground(
                    shapeRenderer =
                        shapeRenderer,

                    panelBounds =
                        detailPanelBounds,

                    entry =
                        selectedEntry
                )
            }

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
                selectedEntry(),

            detailPanelBounds =
                detailPanelBounds
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
            pageCount() > 1
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
    }

    private fun handleKeyboardInput() {

        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.ESCAPE
            )
        ) {

            if (
                selectedIndex != null
            ) {
                selectedIndex =
                    null
            } else {
                game.showCollection()
            }

            return
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

        /*
         * Detay paneli açıksa tıklamaları modal gibi ele al.
         *
         * Panelin içine basılırsa hiçbir şey olmaz.
         * Panel dışına basılırsa panel kapanır.
         * Alttaki kartlara tıklama geçmez.
         */
        if (
            selectedIndex != null
        ) {

            if (
                !detailPanelBounds.contains(
                    pointer.position
                )
            ) {
                selectedIndex =
                    null
            }

            return
        }

        val localIndex =
            cardBounds.indexOfFirst { bounds ->
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

        if (
            globalIndex !in
            entries.indices
        ) {
            return
        }

        val selectedEntry =
            entries[globalIndex]

        if (
            selectedEntry.isUnknown
        ) {
            return
        }

        selectedIndex =
            globalIndex}

    private fun selectedEntry():
        CollectionEntry? {

        val index =
            selectedIndex
                ?: return null

        return entries.getOrNull(
            index
        )
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

        selectedIndex =
            selectedIndex
                ?.takeIf { index ->
                    index in entries.indices
                }

        rebuildCardBounds()
    }

    private fun visibleEntries():
        List<CollectionEntry> {

        val startIndex =
            currentPage *
                ENTRIES_PER_PAGE

        if (
            startIndex >=
            entries.size
        ) {
            return emptyList()
        }

        val endIndex =
            minOf(
                startIndex +
                    ENTRIES_PER_PAGE,

                entries.size
            )

        return entries.subList(
            startIndex,
            endIndex
        )
    }

    private fun rebuildCardBounds() {

        cardBounds =
            visibleEntries()
                .indices
                .map { localIndex ->

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

    private fun previousPage() {

        if (
            currentPage <= 0
        ) {
            return
        }

        currentPage -=
            1

        selectedIndex =
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

        selectedIndex =
            null

        rebuildCardBounds()
    }

    private fun pageCount():
        Int {

        if (
            entries.isEmpty()
        ) {
            return 1
        }

        return ceil(
            entries.size.toFloat() /
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

        val selected =
            selectedIndex
                ?: return null

        val pageStart =
            currentPage *
                ENTRIES_PER_PAGE

        val pageEnd =
            pageStart +
                ENTRIES_PER_PAGE

        if (
            selected !in
            pageStart until pageEnd
        ) {
            return null
        }

        return selected -
            pageStart
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
