package com.azizoglu.grisilith.screen

import com.azizoglu.grisilith.GrisilithGame
import com.azizoglu.grisilith.config.GameDifficulty
import com.azizoglu.grisilith.config.GameMap
import com.azizoglu.grisilith.content.ContentDefinition
import com.azizoglu.grisilith.content.ContentUnlockResolver
import com.azizoglu.grisilith.content.DefaultContentRegistry
import com.azizoglu.grisilith.preset.PresetContentResolver
import com.azizoglu.grisilith.preset.PresetRepository
import com.azizoglu.grisilith.preset.ResolvedPresetContent
import com.azizoglu.grisilith.preset.RunPreset
import com.azizoglu.grisilith.screen.components.MenuButton
import com.azizoglu.grisilith.screen.components.MenuPointer
import com.azizoglu.grisilith.screen.render.PaperTooltipRenderer
import com.azizoglu.grisilith.ui.icon.ContentIconRenderer
import com.azizoglu.grisilith.ui.theme.UiComponentRenderer
import com.azizoglu.grisilith.ui.theme.PaperUi
import com.azizoglu.grisilith.ui.typography.UiFontFactory
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.FitViewport

class PlayMenuScreen(
    private val game: GrisilithGame,
    private val initialPresetId: String? = null,
    private val initialMap: GameMap? = null
) : ScreenAdapter() {

    companion object {
        private const val VIEWPORT_WIDTH = 1280f
        private const val VIEWPORT_HEIGHT = 720f
        private const val BACKGROUND_PATH = "ui/backgrounds/run_preparation_table.png"
        private const val PAGE_TURN_SHEET_PATH = "ui/animations/page_corner_turn_sheet.png"
        private const val PAGE_TURN_COLUMNS = 4
        private const val PAGE_TURN_ROWS = 2
        private const val RUN_SETUP_PREFERENCES = "grisilith-run-setup"
        private const val LAST_MAP_KEY = "last-played-map"
        private const val LAST_DIFFICULTY_KEY = "last-played-difficulty"
        private val INK = Color(0.18f, 0.105f, 0.055f, 1f)
        private val INK_RED = Color(0.55f, 0.12f, 0.08f, 1f)
        private val INK_GOLD = Color(0.42f, 0.27f, 0.09f, 1f)
        private val MAP_INK = Color(0.12f, 0.075f, 0.045f, 1f)
        private val PAPER_TEXT = Color(0.94f, 0.91f, 0.84f, 1f)
    }

    private val camera = OrthographicCamera()
    private val viewport = FitViewport(VIEWPORT_WIDTH, VIEWPORT_HEIGHT, camera)
    private val batch = SpriteBatch()
    private val font: BitmapFont = UiFontFactory.createBody()
    private val notebookFont: BitmapFont = UiFontFactory.createDecorative(22)
    private val pointer = MenuPointer(viewport)
    private val paper = PaperUi()
    private val presetResolver = PresetContentResolver(DefaultContentRegistry.instance)
    private val background = Texture(Gdx.files.internal(BACKGROUND_PATH)).apply {
        setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
    }
    private val pageTurnSheet = Texture(Gdx.files.internal(PAGE_TURN_SHEET_PATH)).apply {
        setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
    }
    private val previousArrowTexture = Texture(Gdx.files.internal("ui/animations/page_arrow_previous_v2.png")).apply {
        setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
    }
    private val nextArrowTexture = Texture(Gdx.files.internal("ui/animations/page_arrow_next_v2.png")).apply {
        setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
    }
    private val rightPageTurnFrames = TextureRegion.split(
        pageTurnSheet,
        pageTurnSheet.width / PAGE_TURN_COLUMNS,
        pageTurnSheet.height / PAGE_TURN_ROWS
    ).flatMap { row -> row.asList() }
    private val leftPageTurnFrames = rightPageTurnFrames.map { frame ->
        TextureRegion(frame).apply { flip(true, false) }
    }
    private val leftPageArrow = inkRegion(previousArrowTexture)
    private val rightPageArrow = inkRegion(nextArrowTexture)

    /** Ignore the generated transparent canvas when positioning the visible ink. */
    private fun inkRegion(texture: Texture): TextureRegion {
        val data = texture.textureData
        if (!data.isPrepared) data.prepare()
        val pixels = data.consumePixmap()
        var left = pixels.width; var top = pixels.height; var right = 0; var bottom = 0
        for (y in 0 until pixels.height) for (x in 0 until pixels.width) {
            if ((pixels.getPixel(x, y) and 255) > 100) {
                left = minOf(left, x); top = minOf(top, y)
                right = maxOf(right, x); bottom = maxOf(bottom, y)
            }
        }
        val region = if (left <= right) TextureRegion(texture, left, top, right - left + 1, bottom - top + 1) else TextureRegion(texture)
        if (data.disposePixmap()) pixels.dispose()
        return region
    }

    private var presets: List<RunPreset> = emptyList()
    private var selectedPresetIndex = 0
    private var selectedMap = GameMap.entries.first { it.isInitiallyUnlocked }
    private var selectedDifficulty = GameDifficulty.NORMAL
    private var pressedMap: GameMap? = null
    private var pressedDifficulty: GameDifficulty? = null
    private var pressedPageDirection = 0
    private var leftPageLift = 0f
    private var rightPageLift = 0f
    private var hoveredBuildDefinition: ContentDefinition? = null

    private val previousPageCorner = Rectangle(35f, 545f, 33f, 29f)
    private val nextPageCorner = Rectangle(371f, 545f, 33f, 29f)
    private val managePresetsButton = MenuButton("MANAGE PRESETS", 48f, 247f, 152f, 36f) {
        game.showPresetMenu()
    }
    private val startButton = MenuButton(
        "START RUN", 535f, 14f, 220f, 60f, isEnabled = false
    ) {
        startSelectedRun()
    }
    private val backButton = MenuButton("BACK", 24f, 14f, 170f, 54f) {
        game.showMainMenu()
    }

    private val mapBounds: Map<GameMap, Rectangle> = linkedMapOf(
        GameMap.ROAD_TO_HOME to Rectangle(464f, 464f, 164f, 40f),
        GameMap.TROUBLE_AT_HOME to Rectangle(758f, 392f, 164f, 40f),
        GameMap.FOLLOWING_THE_DUNGEON_DELVER to Rectangle(776f, 581f, 164f, 40f),
        GameMap.TEST_MAP to Rectangle(446f, 222f, 164f, 40f)
    )

    private val difficultyBounds: Map<GameDifficulty, Rectangle> =
        GameDifficulty.entries.mapIndexed { index, difficulty ->
            difficulty to Rectangle(1010f, 430f - index * 95f, 210f, 64f)
        }.toMap(LinkedHashMap())

    override fun show() {
        viewport.apply()
        camera.position.set(VIEWPORT_WIDTH / 2f, VIEWPORT_HEIGHT / 2f, 0f)
        camera.update()
        pointer.reset()
        presets = PresetRepository.allPresets().filter(::isActivePreset)
        selectedPresetIndex = initialPresetId
            ?.let { id -> presets.indexOfFirst { it.presetId == id }.takeIf { it >= 0 } }
            ?: selectedPresetIndex.coerceIn(0, (presets.size - 1).coerceAtLeast(0))
        selectedMap = initialMap?.takeIf { it.isInitiallyUnlocked } ?: loadLastPlayedMap()
        selectedDifficulty = loadLastPlayedDifficulty()
        updateStartButton()
    }

    override fun render(delta: Float) {
        pointer.update()
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.showMainMenu()
            return
        }

        updateStartButton()
        updatePageCornerAnimation(delta)

        val navigationButtons = listOf(managePresetsButton, startButton, backButton)
        navigationButtons.forEach { button ->
            if (button.update(pointer.position, pointer.justPressed, pointer.justReleased)) return
        }
        updateSelectionCards()

        Gdx.gl.glClearColor(0.025f, 0.02f, 0.018f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        batch.projectionMatrix = camera.combined
        batch.begin()
        batch.color = Color.WHITE
        batch.draw(background, 0f, 0f, VIEWPORT_WIDTH, VIEWPORT_HEIGHT)
        hoveredBuildDefinition = null
        renderPresetPage()
        renderMapArea()
        renderDifficultyParchment()
        renderPageCornerArrows()
        renderPageCornerFeedback()
        renderManagePresetsButton()
        startButton.render(batch, font, pointer.position, pointer.isHeld)
        backButton.render(batch, font, pointer.position, pointer.isHeld)
        hoveredBuildDefinition?.let { definition ->
            PaperTooltipRenderer.draw(
                batch = batch,
                paper = paper,
                font = font,
                pointer = pointer.position,
                title = definition.displayName,
                body = com.azizoglu.grisilith.content.ContentPresentation.hover(definition)
            )
        }
        batch.end()
    }

    private fun renderPresetPage() {
        val resolved = selectedResolvedPreset()
        val oldScaleX = notebookFont.data.scaleX
        val oldScaleY = notebookFont.data.scaleY
        val oldColor = Color(notebookFont.color)

        if (resolved == null) {
            notebookFont.data.setScale(0.70f)
            notebookFont.color = INK_RED
            notebookFont.draw(batch, "NO ACTIVE PRESET", 44f, 438f, 350f, Align.center, false)
            notebookFont.data.setScale(0.48f)
            notebookFont.color = INK
            notebookFont.draw(batch, "Create or repair a preset before starting a run.", 58f, 390f, 322f, Align.center, true)
            restoreFont(notebookFont, oldScaleX, oldScaleY, oldColor)
            return
        }

        notebookFont.data.setScale(0.69f)
        notebookFont.color = INK
        notebookFont.draw(batch, resolved.preset.presetName, 44f, 548f, 160f, Align.center, true)

        val portrait = Rectangle(51f, 362f, 148f, 148f)
        UiComponentRenderer.drawSlot(batch, portrait, selected = true)
        ContentIconRenderer.draw(batch, resolved.character, portrait)

        notebookFont.data.setScale(0.59f)
        notebookFont.color = INK
        notebookFont.draw(batch, resolved.character.displayName, 40f, 346f, 170f, Align.center, false)

        notebookFont.data.setScale(0.47f)
        notebookFont.color = INK_GOLD
        notebookFont.draw(batch, "STARTING BUILD", 214f, 538f, 194f, Align.center, false)

        buildSlots(resolved).forEach { (label, bounds, definition) ->
            notebookFont.data.setScale(if (label.length > 7) 0.25f else 0.31f)
            notebookFont.color = INK
            notebookFont.draw(batch, label, bounds.x - 10f, bounds.y + bounds.height + 15f, bounds.width + 20f, Align.center, true)
            if (!label.endsWith("TAMGA")) UiComponentRenderer.drawSlot(batch, bounds)
            definition?.let {
                ContentIconRenderer.draw(batch, it, bounds, framed = !label.endsWith("TAMGA"))
                if (bounds.contains(pointer.position)) hoveredBuildDefinition = it
            }
        }

        notebookFont.data.setScale(0.38f)
        notebookFont.color = INK
        notebookFont.draw(
            batch,
            "ACTIVE PRESETS  ${selectedPresetIndex + 1} / ${presets.size}",
            42f,
            316f,
            168f,
            Align.center,
            false
        )
        restoreFont(notebookFont, oldScaleX, oldScaleY, oldColor)
    }

    private fun buildSlots(resolved: ResolvedPresetContent): List<Triple<String, Rectangle, ContentDefinition?>> {
        val definitions = listOf(
            "WEAPON" to resolved.weapon,
            "ARMOR" to resolved.armor,
            "ITEM" to resolved.item,
            "WEAPON TAMGA" to resolved.weaponTamga,
            "ARMOR TAMGA" to resolved.armorTamga
        )
        val positions = listOf(
            Rectangle(226f, 433f, 72f, 72f),
            Rectangle(327f, 433f, 72f, 72f),
            Rectangle(276f, 334f, 72f, 72f),
            Rectangle(226f, 234f, 72f, 72f),
            Rectangle(327f, 234f, 72f, 72f)
        )
        return definitions.mapIndexed { index, (label, definition) ->
            Triple(label, positions[index], definition)
        }
    }

    private fun renderMapArea() {
        val oldScaleX = font.data.scaleX
        val oldScaleY = font.data.scaleY
        val oldColor = Color(font.color)

        mapBounds.forEach { (map, bounds) ->
            val selected = map == selectedMap
            val hovered = bounds.contains(pointer.position)
            renderCompactButton(
                bounds = bounds,
                text = map.displayName.uppercase(),
                selected = selected || hovered || (map == pressedMap && pointer.isHeld),
                enabled = map.isInitiallyUnlocked,
                textScale = if (map.displayName.length > 18) 0.60f else 0.67f,
                textBaselineOffset = 27f
            )
        }

        font.data.setScale(0.58f)
        font.color = MAP_INK
        font.draw(batch, selectedMap.displayName, 510f, 172f, 326f, Align.center, false)
        font.data.setScale(0.47f)
        font.color = MAP_INK
        font.draw(
            batch,
            selectedMap.description,
            520f,
            146f,
            306f,
            Align.center,
            true
        )
        restoreFont(oldScaleX, oldScaleY, oldColor)
    }

    private fun renderDifficultyParchment() {
        val oldScaleX = font.data.scaleX
        val oldScaleY = font.data.scaleY
        val oldColor = Color(font.color)
        font.data.setScale(0.78f)
        font.color = INK
        font.draw(batch, "DIFFICULTY", 992f, 548f, 248f, Align.center, false)

        difficultyBounds.forEach { (difficulty, bounds) ->
            val selected = difficulty == selectedDifficulty
            val hovered = bounds.contains(pointer.position)
            font.data.setScale(0.61f)
            font.color = when {
                difficulty == pressedDifficulty && pointer.isHeld -> Color(0.42f, 0.16f, 0.08f, 1f)
                selected -> Color(0.22f, 0.35f, 0.55f, 1f)
                hovered -> Color(0.56f, 0.31f, 0.08f, 1f)
                else -> INK
            }
            font.draw(
                batch,
                "${difficulty.displayName.uppercase()}  (${difficulty.maximumStage} STAGES)",
                bounds.x + 8f,
                bounds.y + 41f,
                bounds.width - 16f,
                Align.center,
                false
            )
        }
        restoreFont(oldScaleX, oldScaleY, oldColor)
    }

    private fun updateSelectionCards() {
        if (pointer.justPressed) {
            pressedPageDirection = when {
                presets.size <= 1 -> 0
                previousPageCorner.contains(pointer.position) -> -1
                nextPageCorner.contains(pointer.position) -> 1
                else -> 0
            }
            pressedMap = mapBounds.entries.firstOrNull { it.value.contains(pointer.position) && it.key.isInitiallyUnlocked }?.key
            pressedDifficulty = difficultyBounds.entries.firstOrNull { it.value.contains(pointer.position) }?.key
        }
        if (pointer.justReleased) {
            when (pressedPageDirection) {
                -1 -> if (previousPageCorner.contains(pointer.position)) cyclePreset(-1)
                1 -> if (nextPageCorner.contains(pointer.position)) cyclePreset(1)
            }
            pressedPageDirection = 0
            mapBounds[pressedMap]?.takeIf { it.contains(pointer.position) }?.let { selectedMap = pressedMap!! }
            difficultyBounds[pressedDifficulty]?.takeIf { it.contains(pointer.position) }?.let { selectedDifficulty = pressedDifficulty!! }
            pressedMap = null
            pressedDifficulty = null
            updateStartButton()
        }
    }

    private fun updatePageCornerAnimation(delta: Float) {
        val speed = MathUtils.clamp(delta * 11f, 0f, 1f)
        val leftTarget = if (presets.size > 1 && previousPageCorner.contains(pointer.position)) 1f else 0f
        val rightTarget = if (presets.size > 1 && nextPageCorner.contains(pointer.position)) 1f else 0f
        leftPageLift = MathUtils.lerp(leftPageLift, leftTarget, speed)
        rightPageLift = MathUtils.lerp(rightPageLift, rightTarget, speed)
    }

    private fun renderPageCornerFeedback() {
        if (presets.size <= 1) return
        renderPageTurnFrame(leftPageTurnFrames, leftPageLift, isLeftCorner = true)
        renderPageTurnFrame(rightPageTurnFrames, rightPageLift, isLeftCorner = false)
    }

    private fun renderPageCornerArrows() {
        if (presets.size <= 1) return
        val previousColor = Color(batch.color)
        batch.color = Color(1f, 1f, 1f, (1f - leftPageLift).coerceIn(0f, 1f))
        batch.draw(leftPageArrow, 42f, 550f, 25f, 8f)
        batch.color = Color(1f, 1f, 1f, (1f - rightPageLift).coerceIn(0f, 1f))
        batch.draw(rightPageArrow, 373f, 550f, 25f, 8f)
        batch.color = previousColor
    }

    private fun renderPageTurnFrame(
        frames: List<TextureRegion>,
        progress: Float,
        isLeftCorner: Boolean
    ) {
        if (progress <= 0.02f || frames.isEmpty()) return
        val frameIndex = (progress * (frames.lastIndex + 0.99f)).toInt().coerceIn(0, frames.lastIndex)
        val sourceRightEdges = intArrayOf(433, 416, 401, 384, 435, 418, 402, 384)
        val sourceTopEdges = intArrayOf(81, 81, 80, 81, 54, 54, 53, 53)
        val cellWidth = (pageTurnSheet.width / PAGE_TURN_COLUMNS).toFloat()
        val cellHeight = (pageTurnSheet.height / PAGE_TURN_ROWS).toFloat()
        val frameWidth = 72f
        val frameHeight = 64f
        val paperCornerX = if (isLeftCorner) 34f else 404f
        val paperCornerY = 573f
        val sourceRight = sourceRightEdges[frameIndex].toFloat()
        val visibleTipX = if (isLeftCorner) {
            (cellWidth - sourceRight) / cellWidth * frameWidth
        } else {
            sourceRight / cellWidth * frameWidth
        }
        val visibleTipY = (cellHeight - sourceTopEdges[frameIndex]) / cellHeight * frameHeight
        val x = paperCornerX - visibleTipX
        val y = paperCornerY - visibleTipY
        batch.color = Color(1f, 1f, 1f, 0.22f + progress * 0.78f)
        batch.draw(frames[frameIndex], x, y, frameWidth, frameHeight)
        batch.color = Color.WHITE
    }

    private fun renderCompactButton(
        bounds: Rectangle,
        text: String,
        selected: Boolean,
        enabled: Boolean = true,
        textScale: Float,
        textBaselineOffset: Float,
        textFont: BitmapFont = font
    ) {
        val oldScaleX = textFont.data.scaleX
        val oldScaleY = textFont.data.scaleY
        val oldColor = Color(textFont.color)
        batch.color = if (enabled) Color.WHITE else Color(0.48f, 0.48f, 0.48f, 0.78f)
        UiComponentRenderer.drawCompactButton(batch, bounds, selected && enabled)
        batch.color = Color.WHITE
        textFont.data.setScale(textScale)
        textFont.color = if (enabled) PAPER_TEXT else Color.GRAY
        textFont.draw(
            batch,
            text,
            bounds.x + 8f,
            bounds.y + textBaselineOffset,
            bounds.width - 16f,
            Align.center,
            false
        )
        restoreFont(textFont, oldScaleX, oldScaleY, oldColor)
    }

    private fun renderManagePresetsButton() {
        val bounds = managePresetsButton.bounds
        paper.button(batch, bounds, bounds.contains(pointer.position))
        val oldScaleX = notebookFont.data.scaleX
        val oldScaleY = notebookFont.data.scaleY
        val oldColor = Color(notebookFont.color)
        notebookFont.data.setScale(0.43f)
        notebookFont.color = INK
        val layout = com.badlogic.gdx.graphics.g2d.GlyphLayout(notebookFont, managePresetsButton.text)
        notebookFont.draw(batch, managePresetsButton.text, bounds.x, bounds.y + (bounds.height + layout.height) / 2f,
            bounds.width, Align.center, false)
        restoreFont(notebookFont, oldScaleX, oldScaleY, oldColor)
    }

    private fun cyclePreset(direction: Int) {
        if (presets.isEmpty()) return
        selectedPresetIndex = (selectedPresetIndex + direction + presets.size) % presets.size
        updateStartButton()
    }

    private fun isActivePreset(preset: RunPreset): Boolean {
        val resolved = presetResolver.resolve(preset) ?: return false
        return listOfNotNull(
            resolved.character,
            resolved.weapon,
            resolved.armor,
            resolved.item,
            resolved.weaponTamga,
            resolved.armorTamga
        ).all(ContentUnlockResolver::isUnlocked)
    }

    private fun selectedResolvedPreset(): ResolvedPresetContent? =
        presets.getOrNull(selectedPresetIndex)?.let(presetResolver::resolve)

    private fun updateStartButton() {
        startButton.isEnabled = selectedResolvedPreset() != null && selectedMap.isInitiallyUnlocked
    }

    private fun startSelectedRun() {
        val preset = presets.getOrNull(selectedPresetIndex) ?: return
        if (!isActivePreset(preset)) return
        Gdx.app.getPreferences(RUN_SETUP_PREFERENCES)
            .putString(LAST_MAP_KEY, selectedMap.name)
            .putString(LAST_DIFFICULTY_KEY, selectedDifficulty.name)
            .flush()
        game.startPresetRun(selectedDifficulty, selectedMap, preset.presetId)
    }

    private fun loadLastPlayedMap(): GameMap {
        val saved = Gdx.app.getPreferences(RUN_SETUP_PREFERENCES)
            .getString(LAST_MAP_KEY, GameMap.ROAD_TO_HOME.name)
        return runCatching { GameMap.valueOf(saved) }
            .getOrDefault(GameMap.ROAD_TO_HOME)
            .takeIf { it.isInitiallyUnlocked }
            ?: GameMap.ROAD_TO_HOME
    }

    private fun loadLastPlayedDifficulty(): GameDifficulty {
        val saved = Gdx.app.getPreferences(RUN_SETUP_PREFERENCES)
            .getString(LAST_DIFFICULTY_KEY, GameDifficulty.NORMAL.name)
        return runCatching { GameDifficulty.valueOf(saved) }.getOrDefault(GameDifficulty.NORMAL)
    }

    private fun restoreFont(scaleX: Float, scaleY: Float, color: Color) {
        restoreFont(font, scaleX, scaleY, color)
    }

    private fun restoreFont(targetFont: BitmapFont, scaleX: Float, scaleY: Float, color: Color) {
        targetFont.data.setScale(scaleX, scaleY)
        targetFont.color = color
    }

    override fun resize(width: Int, height: Int) {
        if (width > 0 && height > 0) viewport.update(width, height, true)
    }

    override fun dispose() {
        background.dispose()
        pageTurnSheet.dispose()
        previousArrowTexture.dispose()
        nextArrowTexture.dispose()
        paper.dispose()
        batch.dispose()
        font.dispose()
        notebookFont.dispose()
    }
}
