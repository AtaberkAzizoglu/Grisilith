package com.azizoglu.grisilith.screen

import com.azizoglu.grisilith.GrisilithGame
import com.azizoglu.grisilith.screen.components.MenuButton
import com.azizoglu.grisilith.screen.components.MenuPointer
import com.azizoglu.grisilith.screen.render.HighScoreRenderer
import com.azizoglu.grisilith.statistics.career.HighScoreRepository
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.viewport.FitViewport

class HighScoreScreen(
    private val game: GrisilithGame
) : ScreenAdapter() {

    companion object {

        private const val VIEWPORT_WIDTH =
            1280f

        private const val VIEWPORT_HEIGHT =
            720f

        /*
         * Sayfadaki bütün bölümlerin yaklaşık toplam yüksekliği.
         */
        private const val CONTENT_HEIGHT =
            1510f

        private const val MINIMUM_SCROLL =
            0f

        private const val MAXIMUM_SCROLL =
            CONTENT_HEIGHT -
                VIEWPORT_HEIGHT +
                120f

        private const val WHEEL_SCROLL_AMOUNT =
            75f

        private const val KEYBOARD_SCROLL_SPEED =
            520f
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
        HighScoreRenderer()

    private var scrollOffset =
        0f

    private var previousInputProcessor:
        InputProcessor? =
        null

    /*
     * Basılı tutup sürükleyerek scroll yapmak için
     * önceki ekran Y pozisyonu.
     */
    private var previousDragScreenY:
        Int? =
        null

    private val inputProcessor =
        object : InputAdapter() {

            override fun scrolled(
                amountX: Float,
                amountY: Float
            ): Boolean {

                scrollBy(
                    amountY *
                        WHEEL_SCROLL_AMOUNT
                )

                return true
            }
        }

    private val backButton =
        MenuButton(
            text =
                "BACK",

            x =
                40f,

            y =
                30f,

            width =
                180f,

            height =
                60f
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

        scrollOffset =
            0f

        previousDragScreenY =
            null

        previousInputProcessor =
            Gdx.input.inputProcessor

        Gdx.input.inputProcessor =
            inputProcessor
    }

    override fun render(
        delta: Float
    ) {

        pointer.update()

        if (
            handleKeyboardInput(
                delta
            )
        ) {
            return
        }

        handleDragScrolling()

        if (
            backButton.update(
                pointerPosition =
                    pointer.position,

                pointerJustPressed =
                    pointer.justPressed,

                pointerJustReleased =
                    pointer.justReleased
            )
        ) {
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

        shapeRenderer.projectionMatrix =
            camera.combined

        batch.projectionMatrix =
            camera.combined

        renderer.renderShapes(
            shapeRenderer =
                shapeRenderer,

            statistics =
                HighScoreRepository
                    .statistics,

            scrollOffset =
                scrollOffset
        )

        batch.begin()

        renderer.renderTexts(
            batch =
                batch,

            font =
                font,

            statistics =
                HighScoreRepository
                    .statistics,

            scrollOffset =
                scrollOffset
        )

        /*
         * BACK butonu scroll içeriğinden bağımsız,
         * sabit olarak ekranda kalır.
         */
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

        batch.end()
    }

    /*
     * true dönerse ekran değişmiştir ve render()
     * devam etmemelidir.
     */
    private fun handleKeyboardInput(
        delta: Float
    ): Boolean {

        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.ESCAPE
            )
        ) {
            game.showMainMenu()
            return true
        }

        var scrollDirection =
            0f

        if (
            Gdx.input.isKeyPressed(
                Input.Keys.DOWN
            ) ||
            Gdx.input.isKeyPressed(
                Input.Keys.S
            )
        ) {
            scrollDirection +=
                1f
        }

        if (
            Gdx.input.isKeyPressed(
                Input.Keys.UP
            ) ||
            Gdx.input.isKeyPressed(
                Input.Keys.W
            )
        ) {
            scrollDirection -=
                1f
        }

        if (
            scrollDirection !=
            0f
        ) {
            scrollBy(
                scrollDirection *
                    KEYBOARD_SCROLL_SPEED *
                    delta
            )
        }

        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.HOME
            )
        ) {
            scrollOffset =
                MINIMUM_SCROLL
        }

        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.END
            )
        ) {
            scrollOffset =
                MAXIMUM_SCROLL
        }

        return false
    }

    private fun handleDragScrolling() {

        if (
            !Gdx.input.isTouched
        ) {
            previousDragScreenY =
                null

            return
        }

        /*
         * BACK butonunun üzerinde başlayan hareket
         * içerik scroll'u olarak değerlendirilmez.
         */
        if (
            backButton.bounds.contains(
                pointer.position
            )
        ) {
            previousDragScreenY =
                null

            return
        }

        val currentScreenY =
            Gdx.input.y

        val previousScreenY =
            previousDragScreenY

        if (
            previousScreenY != null
        ) {
            val dragDifference =
                previousScreenY -
                    currentScreenY

            scrollBy(
                dragDifference.toFloat()
            )
        }

        previousDragScreenY =
            currentScreenY
    }

    private fun scrollBy(
        amount: Float
    ) {

        scrollOffset =
            MathUtils.clamp(
                scrollOffset +
                    amount,

                MINIMUM_SCROLL,
                MAXIMUM_SCROLL
            )
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

    override fun hide() {

        if (
            Gdx.input.inputProcessor ===
            inputProcessor
        ) {
            Gdx.input.inputProcessor =
                previousInputProcessor
        }

        previousInputProcessor =
            null
    }

    override fun dispose() {

        hide()

        batch.dispose()
        shapeRenderer.dispose()
        font.dispose()
    }
}
