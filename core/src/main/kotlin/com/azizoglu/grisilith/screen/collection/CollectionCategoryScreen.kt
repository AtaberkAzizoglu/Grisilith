package com.azizoglu.grisilith.screen

import com.azizoglu.grisilith.GrisilithGame
import com.azizoglu.grisilith.audio.GameAudio
import com.azizoglu.grisilith.audio.UiSound
import com.azizoglu.grisilith.collection.CollectionEntry
import com.azizoglu.grisilith.collection.CollectionProvider
import com.azizoglu.grisilith.collection.filter.CollectionFilterEngine
import com.azizoglu.grisilith.collection.filter.CollectionFilterOption
import com.azizoglu.grisilith.collection.filter.CollectionFilterRegistry
import com.azizoglu.grisilith.collection.filter.CollectionFilterState
import com.azizoglu.grisilith.content.ContentCategory
import com.azizoglu.grisilith.screen.components.MenuButton
import com.azizoglu.grisilith.screen.components.MenuPointer
import com.azizoglu.grisilith.screen.render.AdaptiveTooltipRenderer
import com.azizoglu.grisilith.screen.render.CollectionCategoryRenderer
import com.azizoglu.grisilith.ui.input.VerticalScrollbarDragController
import com.azizoglu.grisilith.ui.theme.UiComponentRenderer
import com.azizoglu.grisilith.ui.typography.UiFontFactory
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack
import com.badlogic.gdx.utils.viewport.FitViewport
import kotlin.math.ceil

class CollectionCategoryScreen(
    private val game: GrisilithGame,
    private val category: ContentCategory
) : ScreenAdapter() {

    companion object {
        private const val VIEWPORT_WIDTH = 1280f
        private const val VIEWPORT_HEIGHT = 720f
        private const val CARD_HEIGHT = 180f
        private const val CARD_GAP_X = 14f
        private const val CARD_GAP_Y = 16f
        private const val WHEEL_SCROLL = 88f
        private val GRID_VIEWPORT = Rectangle(48f, 105f, 654f, 414f)
        private val DETAIL_BOUNDS = Rectangle(748f, 45f, 476f, 498f)
        private val SCROLLBAR_BOUNDS = Rectangle(712f, 105f, 22f, 414f)
        private val SEARCH_BOUNDS = Rectangle(48f, 559f, 300f, 64f)
        private val STATE_BOUNDS = Rectangle(360f, 559f, 190f, 64f)
        private val SORT_BOUNDS = Rectangle(562f, 559f, 176f, 64f)
        private val FILTER_BOUNDS = Rectangle(750f, 559f, 474f, 64f)
        private const val FILTER_OPTION_HEIGHT = 54f
    }

    private enum class VisibilityFilter(val label: String) {
        ALL("ALL"), UNLOCKED("UNLOCKED"), LOCKED("LOCKED"), UNKNOWN("UNKNOWN")
    }

    private val camera = OrthographicCamera()
    private val viewport = FitViewport(VIEWPORT_WIDTH, VIEWPORT_HEIGHT, camera)
    private val batch = SpriteBatch()
    private val font = UiFontFactory.createBody()
    private val pointer = MenuPointer(viewport)
    private val renderer = CollectionCategoryRenderer()
    private val scrollbarDrag = VerticalScrollbarDragController()
    private val filterOptions: List<CollectionFilterOption> = CollectionFilterRegistry.optionsFor(category)
    private val filterState = CollectionFilterState(filterOptions)
    private var entries: List<CollectionEntry> = emptyList()
    private var selectedContentId: String? = null
    private var hoveredContentId: String? = null
    private var pressedContentId: String? = null
    private var scrollOffset = 0f
    private var searchText = ""
    private var searchFocused = false
    private var visibilityFilter = VisibilityFilter.ALL
    private var ascending = true
    private var previousInputProcessor: InputProcessor? = null

    private val backButton = MenuButton("BACK", 48f, 30f, 180f, 58f) {
        game.showCollection()
    }

    private val inputProcessor: InputProcessor = object : InputAdapter() {
        override fun scrolled(amountX: Float, amountY: Float): Boolean {
            if (!GRID_VIEWPORT.contains(pointer.position)) return false
            scrollBy(amountY * WHEEL_SCROLL)
            return true
        }

        override fun keyTyped(character: Char): Boolean {
            if (!searchFocused) return false
            when {
                character == '\b' -> searchText = searchText.dropLast(1)
                character == '\r' || character == '\n' -> searchFocused = false
                !character.isISOControl() && searchText.length < 32 -> searchText += character
            }
            onViewChanged()
            return true
        }
    }

    override fun show() {
        viewport.apply()
        camera.position.set(VIEWPORT_WIDTH / 2f, VIEWPORT_HEIGHT / 2f, 0f)
        camera.update()
        pointer.reset()
        scrollbarDrag.reset()
        filterState.reset()
        entries = CollectionProvider.entries(category)
        selectedContentId = filteredEntries().firstOrNull()?.definition?.id?.value
        hoveredContentId = null
        scrollOffset = 0f
        searchText = ""
        searchFocused = false
        visibilityFilter = VisibilityFilter.ALL
        ascending = true
        previousInputProcessor = Gdx.input.inputProcessor
        Gdx.input.inputProcessor = inputProcessor
    }

    override fun render(delta: Float) {
        pointer.update()
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (filterState.isDropdownOpen) filterState.closeDropdown() else game.showCollection()
            return
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) scrollBy(320f * delta)
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) scrollBy(-320f * delta)

        if (backButton.update(pointer.position, pointer.justPressed, pointer.justReleased)) return
        val filterConsumed = updateToolbar()
        val scrollbarConsumed = updateScrollbar()
        if (!filterConsumed && !scrollbarConsumed) updateCards()
        updateHover()

        Gdx.gl.glClearColor(0.025f, 0.02f, 0.018f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        batch.projectionMatrix = camera.combined
        batch.begin()
        renderer.renderShell(batch, font, category, GRID_VIEWPORT, DETAIL_BOUNDS)
        renderer.renderToolbar(
            batch, font, searchText, searchFocused, visibilityFilter.label,
            if (ascending) "A - Z" else "Z - A", filterLabel(),
            SEARCH_BOUNDS, STATE_BOUNDS, SORT_BOUNDS, FILTER_BOUNDS,
            filterOptions.isNotEmpty(), filterState.isDropdownOpen
        )
        batch.end()

        val scissors = Rectangle()
        ScissorStack.calculateScissors(camera, batch.transformMatrix, GRID_VIEWPORT, scissors)
        if (ScissorStack.pushScissors(scissors)) {
            batch.begin()
            val cards = cardRectangles()
            renderer.renderCards(
                batch, font, category, filteredEntries(), cards,
                selectedContentId, hoveredContentId, pressedContentId
            )
            batch.end()
            ScissorStack.popScissors()
        }

        batch.begin()
        val visibleRatio = (GRID_VIEWPORT.height / contentHeight()).coerceIn(0f, 1f)
        val scrollRatio = if (maximumScroll() <= 0f) 0f else scrollOffset / maximumScroll()
        UiComponentRenderer.drawChronicleScrollbar(batch, SCROLLBAR_BOUNDS, visibleRatio, scrollRatio)
        renderer.renderDetails(batch, font, selectedEntry(), DETAIL_BOUNDS)
        backButton.render(batch, font, pointer.position, pointer.isHeld)
        if (filterState.isDropdownOpen && filterOptions.isNotEmpty()) {
            renderer.renderFilterDropdown(
                batch, font, filterOptions, filterOptionBounds(), filterState.selectedOptionIds
            )
        }
        if (!filterState.isDropdownOpen) {
            val entry=hoveredEntry()?.takeIf { !it.isUnknown }
            val character=entry?.definition as? com.azizoglu.grisilith.character.CharacterDefinition
            if(character!=null) com.azizoglu.grisilith.screen.render.CharacterHoverRenderer.draw(batch,font,pointer.position,character)
            else if(entry!=null) AdaptiveTooltipRenderer.draw(batch,font,Rectangle(0f,0f,VIEWPORT_WIDTH,VIEWPORT_HEIGHT),pointer.position,
                entry.definition.displayName,com.azizoglu.grisilith.content.ContentPresentation.hover(entry.definition),560f,.90f,.76f)
            else renderer.renderDetailHover(batch,font,selectedEntry(),DETAIL_BOUNDS,pointer.position)
        }
        batch.end()
    }

    private fun updateToolbar(): Boolean {
        if (!pointer.justReleased) return filterState.isDropdownOpen
        if (SEARCH_BOUNDS.contains(pointer.position)) {
            searchFocused = true
            filterState.closeDropdown()
            return true
        }
        searchFocused = false
        if (STATE_BOUNDS.contains(pointer.position)) {
            GameAudio.play(UiSound.CLICK)
            val values = if (category == ContentCategory.ENEMY || category == ContentCategory.MAP) {
                VisibilityFilter.entries
            } else {
                VisibilityFilter.entries.filter { it != VisibilityFilter.UNKNOWN }
            }
            visibilityFilter = values[(values.indexOf(visibilityFilter) + 1) % values.size]
            onViewChanged()
            return true
        }
        if (SORT_BOUNDS.contains(pointer.position)) {
            GameAudio.play(UiSound.CLICK)
            ascending = !ascending
            onViewChanged()
            return true
        }
        if (filterOptions.isNotEmpty() && FILTER_BOUNDS.contains(pointer.position)) {
            GameAudio.play(UiSound.CLICK)
            filterState.toggleDropdown()
            return true
        }
        if (filterState.isDropdownOpen) {
            val index = filterOptionBounds().indexOfFirst { it.contains(pointer.position) }
            if (index >= 0) {
                GameAudio.play(UiSound.CLICK)
                filterState.toggle(filterOptions[index].id)
                onViewChanged()
            } else {
                filterState.closeDropdown()
            }
            return true
        }
        return false
    }

    private fun updateScrollbar(): Boolean {
        val visibleRatio = (GRID_VIEWPORT.height / contentHeight()).coerceIn(0f, 1f)
        val scrollRatio = if (maximumScroll() <= 0f) 0f else scrollOffset / maximumScroll()
        return scrollbarDrag.update(
            pointer.position, pointer.justPressed, pointer.isHeld, pointer.justReleased,
            SCROLLBAR_BOUNDS,
            UiComponentRenderer.bronzeScrollbarThumbBounds(SCROLLBAR_BOUNDS, visibleRatio, scrollRatio)
        ) { ratio -> scrollOffset = maximumScroll() * ratio }
    }

    private fun updateCards() {
        if (pointer.justPressed && GRID_VIEWPORT.contains(pointer.position)) {
            pressedContentId = entryAtPointer()?.definition?.id?.value
        }
        if (pointer.justReleased) {
            val entry = entryAtPointer()
            if (entry != null && entry.definition.id.value == pressedContentId) {
                selectedContentId = entry.definition.id.value
                GameAudio.play(UiSound.CLICK)
            }
            pressedContentId = null
        }
    }

    private fun updateHover() {
        hoveredContentId = if (!filterState.isDropdownOpen && GRID_VIEWPORT.contains(pointer.position)) {
            entryAtPointer()?.definition?.id?.value
        } else null
    }

    private fun filteredEntries(): List<CollectionEntry> {
        var result = CollectionFilterEngine.apply(entries, filterOptions, filterState.selectedOptionIds)
        if (searchText.isNotBlank()) {
            result = result.filter {
                !it.isUnknown && it.definition.displayName.contains(searchText.trim(), ignoreCase = true)
            }
        }
        result = when (visibilityFilter) {
            VisibilityFilter.ALL -> result
            VisibilityFilter.UNLOCKED -> result.filter { it.unlocked && !it.isUnknown }
            VisibilityFilter.LOCKED -> result.filter { !it.unlocked && !it.isUnknown }
            VisibilityFilter.UNKNOWN -> result.filter { it.isUnknown }
        }
        return if (ascending) result.sortedBy { it.definition.displayName.lowercase() }
        else result.sortedByDescending { it.definition.displayName.lowercase() }
    }

    private fun columns(): Int = 3

    private fun cardWidth(): Float =
        (GRID_VIEWPORT.width - 12f - CARD_GAP_X * (columns() - 1)) / columns()

    private fun cardRectangles(): List<Rectangle> {
        val width = cardWidth()
        val top = GRID_VIEWPORT.y + GRID_VIEWPORT.height - CARD_HEIGHT - 18f
        return filteredEntries().indices.map { index ->
            val column = index % columns()
            val row = index / columns()
            Rectangle(
                GRID_VIEWPORT.x + 6f + column * (width + CARD_GAP_X),
                top - row * (CARD_HEIGHT + CARD_GAP_Y) + scrollOffset,
                width,
                CARD_HEIGHT
            )
        }
    }

    private fun contentHeight(): Float {
        val rows = ceil(filteredEntries().size / columns().toFloat()).toInt().coerceAtLeast(1)
        return rows * CARD_HEIGHT + (rows - 1) * CARD_GAP_Y + 36f
    }

    private fun maximumScroll(): Float = (contentHeight() - GRID_VIEWPORT.height).coerceAtLeast(0f)

    private fun scrollBy(amount: Float) {
        scrollOffset = (scrollOffset + amount).coerceIn(0f, maximumScroll())
    }

    private fun entryAtPointer(): CollectionEntry? {
        val index = cardRectangles().indexOfFirst { it.contains(pointer.position) }
        return filteredEntries().getOrNull(index)
    }

    private fun selectedEntry(): CollectionEntry? =
        filteredEntries().firstOrNull { it.definition.id.value == selectedContentId }

    private fun hoveredEntry(): CollectionEntry? =
        filteredEntries().firstOrNull { it.definition.id.value == hoveredContentId }

    private fun onViewChanged() {
        scrollOffset = 0f
        val visible = filteredEntries()
        if (visible.none { it.definition.id.value == selectedContentId }) {
            selectedContentId = visible.firstOrNull()?.definition?.id?.value
        }
    }

    private fun filterLabel(): String = when (filterState.selectedOptionIds.size) {
        0 -> "FILTER"
        1 -> filterOptions.firstOrNull { it.id in filterState.selectedOptionIds }?.displayName ?: "FILTER"
        else -> "FILTER (${filterState.selectedOptionIds.size})"
    }

    private fun filterOptionBounds(): List<Rectangle> = filterOptions.mapIndexed { index, _ ->
        Rectangle(
            FILTER_BOUNDS.x,
            FILTER_BOUNDS.y - (index + 1) * FILTER_OPTION_HEIGHT,
            FILTER_BOUNDS.width,
            FILTER_OPTION_HEIGHT
        )
    }

    override fun resize(width: Int, height: Int) {
        if (width > 0 && height > 0) viewport.update(width, height, true)
    }

    override fun hide() {
        if (Gdx.input.inputProcessor === inputProcessor) Gdx.input.inputProcessor = previousInputProcessor
        previousInputProcessor = null
    }

    override fun dispose() {
        hide()
        renderer.dispose()
        batch.dispose()
        font.dispose()
    }
}
