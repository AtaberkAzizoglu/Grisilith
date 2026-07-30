package com.azizoglu.grisilith.screen

import com.azizoglu.grisilith.GrisilithGame
import com.azizoglu.grisilith.profile.PlayerProfileRepository
import com.azizoglu.grisilith.screen.components.MenuButton
import com.azizoglu.grisilith.screen.components.MenuPointer
import com.azizoglu.grisilith.screen.render.CollectionRenderer
import com.azizoglu.grisilith.screen.render.GrisilithCoinRenderer
import com.azizoglu.grisilith.screen.render.PurchasedStatPointRenderer
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.viewport.FitViewport

class CollectionScreen(
    private val game: GrisilithGame
) : ScreenAdapter() {

    companion object {
        private const val VIEWPORT_WIDTH = 1280f
        private const val VIEWPORT_HEIGHT = 720f
    }

    private val camera = OrthographicCamera()

    private val viewport =
        FitViewport(
            VIEWPORT_WIDTH,
            VIEWPORT_HEIGHT,
            camera
        )

    private val batch = SpriteBatch()
    private val font = BitmapFont()
    private val pointer = MenuPointer(viewport)

    private val renderer =
        CollectionRenderer(
            grisilithCoinRenderer = GrisilithCoinRenderer(),
            purchasedStatPointRenderer = PurchasedStatPointRenderer()
        )

    private val backButton =
        MenuButton(
            text = "BACK",
            x = 40f,
            y = 40f,
            width = 180f,
            height = 60f
        ) {
            game.showMainMenu()
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
    }

    override fun render(delta: Float) {
        pointer.update()

        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.ESCAPE
            )
        ) {
            game.showMainMenu()
            return
        }

        val screenChanged =
            backButton.update(
                pointerPosition = pointer.position,
                pointerJustPressed = pointer.justPressed,
                pointerJustReleased = pointer.justReleased
            )

        if (screenChanged) {
            return
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

        batch.projectionMatrix = camera.combined
        batch.begin()

        renderer.render(
            batch = batch,
            font = font,
            profile = PlayerProfileRepository.profile
        )

        backButton.render(
            batch = batch,
            font = font,
            pointerPosition = pointer.position,
            pointerHeld = pointer.isHeld
        )

        batch.end()
    }

    override fun resize(
        width: Int,
        height: Int
    ) {
        if (width <= 0 || height <= 0) {
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
        font.dispose()
    }
}
