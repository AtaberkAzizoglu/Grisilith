package com.azizoglu.grisilith.screen

import com.azizoglu.grisilith.GrisilithGame
import com.azizoglu.grisilith.collection.CollectionEntry
import com.azizoglu.grisilith.collection.CollectionProvider
import com.azizoglu.grisilith.content.ContentCategory
import com.azizoglu.grisilith.screen.components.MenuButton
import com.azizoglu.grisilith.screen.components.MenuPointer
import com.azizoglu.grisilith.screen.render.CollectionRenderer
import com.azizoglu.grisilith.ui.typography.UiFontFactory
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.viewport.FitViewport

class CollectionScreen(
    private val game: GrisilithGame
) : ScreenAdapter() {

    companion object {
        private const val VIEWPORT_WIDTH = 1280f
        private const val VIEWPORT_HEIGHT = 720f
        private const val CARD_WIDTH = 450f
        private const val CARD_HEIGHT = 102f
    }

    private data class CategoryCard(
        val category: ContentCategory,
        val button: MenuButton
    )

    private val camera = OrthographicCamera()
    private val viewport = FitViewport(VIEWPORT_WIDTH, VIEWPORT_HEIGHT, camera)
    private val batch = SpriteBatch()
    private val font = UiFontFactory.createBody()
    private val pointer = MenuPointer(viewport)
    private val renderer = CollectionRenderer()
    private var entriesByCategory: Map<ContentCategory, List<CollectionEntry>> = emptyMap()

    private val categoryCards = listOf(
        card(ContentCategory.CHARACTER, 175f, 470f),
        card(ContentCategory.WEAPON, 655f, 470f),
        card(ContentCategory.ARMOR, 175f, 356f),
        card(ContentCategory.ITEM, 655f, 356f),
        card(ContentCategory.TAMGA, 175f, 242f),
        card(ContentCategory.SUMMON, 655f, 242f),
        card(ContentCategory.ENEMY, 175f, 128f),
        card(ContentCategory.MAP, 655f, 128f)
    )

    private val backButton = MenuButton(
        text = "BACK",
        x = 110f,
        y = 72f,
        width = 180f,
        height = 58f
    ) {
        game.showMainMenu()
    }

    override fun show() {
        viewport.apply()
        camera.position.set(VIEWPORT_WIDTH / 2f, VIEWPORT_HEIGHT / 2f, 0f)
        camera.update()
        pointer.reset()
        entriesByCategory = ContentCategory.entries.associateWith(CollectionProvider::entries)
    }

    override fun render(delta: Float) {
        pointer.update()
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            com.azizoglu.grisilith.audio.GameAudio.play(com.azizoglu.grisilith.audio.UiSound.CLICK)
            game.showMainMenu()
            return
        }

        categoryCards.forEach { card ->
            if (card.button.update(pointer.position, pointer.justPressed, pointer.justReleased)) return
        }
        if (backButton.update(pointer.position, pointer.justPressed, pointer.justReleased)) return

        Gdx.gl.glClearColor(0.025f, 0.02f, 0.018f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        batch.projectionMatrix = camera.combined
        batch.begin()
        renderer.renderBackground(batch)
        renderer.renderTitle(batch, font)
        categoryCards.forEach { card ->
            renderer.renderCategoryCard(
                batch = batch,
                font = font,
                category = card.category,
                entries = entriesByCategory[card.category].orEmpty(),
                bounds = card.button.bounds,
                pointer = pointer.position,
                pointerHeld = pointer.isHeld
            )
        }
        backButton.render(batch, font, pointer.position, pointer.isHeld)
        batch.end()
    }

    private fun card(category: ContentCategory, x: Float, y: Float): CategoryCard =
        CategoryCard(
            category,
            MenuButton("", x, y, CARD_WIDTH, CARD_HEIGHT) {
                game.showCollectionCategory(category)
            }
        )

    override fun resize(width: Int, height: Int) {
        if (width > 0 && height > 0) viewport.update(width, height, true)
    }

    override fun dispose() {
        renderer.dispose()
        batch.dispose()
        font.dispose()
    }
}
