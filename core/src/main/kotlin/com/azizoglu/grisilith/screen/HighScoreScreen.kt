package com.azizoglu.grisilith.screen

import com.azizoglu.grisilith.GrisilithGame
import com.azizoglu.grisilith.audio.GameAudio
import com.azizoglu.grisilith.audio.UiSound
import com.azizoglu.grisilith.screen.components.MenuButton
import com.azizoglu.grisilith.screen.components.MenuPointer
import com.azizoglu.grisilith.screen.render.HighScoreRenderer
import com.azizoglu.grisilith.profile.PlayerProfileRepository
import com.azizoglu.grisilith.statistics.career.CareerAchievement
import com.azizoglu.grisilith.statistics.career.HighScoreRepository
import com.azizoglu.grisilith.ui.theme.UiComponentRenderer
import com.azizoglu.grisilith.ui.input.VerticalScrollbarDragController
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.azizoglu.grisilith.ui.typography.UiFontFactory
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack
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
            1040f

        private const val CONTENT_VIEWPORT_HEIGHT =
            500f

        private const val MINIMUM_SCROLL =
            0f

        private const val MAXIMUM_SCROLL =
            CONTENT_HEIGHT -
                CONTENT_VIEWPORT_HEIGHT

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
        UiFontFactory.createBody()

    private val titleFont =
        UiFontFactory.createDisplay(28)

    private val archiveBackground =
        Texture(
            Gdx.files.internal(
                "ui/backgrounds/chronicle_archive.png"
            )
        ).also {
            it.setFilter(
                Texture.TextureFilter.Linear,
                Texture.TextureFilter.Linear
            )
        }

    private val stonePanel =
        Texture(
            Gdx.files.internal(
                "ui/backgrounds/chronicle_panel_hd.png"
            )
        ).also {
            it.setFilter(
                Texture.TextureFilter.Linear,
                Texture.TextureFilter.Linear
            )
        }

    private val grisilithCoin =
        Texture(
            Gdx.files.internal(
                "currency/grisilith_coin.png"
            )
        ).also {
            it.setFilter(
                Texture.TextureFilter.Linear,
                Texture.TextureFilter.Linear
            )
        }

    private val pointer =
        MenuPointer(
            viewport
        )

    private val renderer =
        HighScoreRenderer()

    private val achievementTypes =
        CareerAchievement.entries

    private val achievementButtons =
        achievementTypes.map { achievement ->
            MenuButton(
                text = "",
                x = 0f,
                y = 0f,
                width = 50f,
                height = 50f
            ) {
                if (
                    HighScoreRepository
                        .claimAchievementReward(
                            achievement
                        )
                ) {
                    PlayerProfileRepository
                        .addGrisilithCoins(1)
                    GameAudio.play(UiSound.CONFIRM)
                }
            }
        }

    private var scrollOffset =
        0f

    private val scrollbarBounds =
        Rectangle(1082f, 104f, 22f, 500f)

    private val scrollbarDrag =
        VerticalScrollbarDragController()

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
                190f,

            y =
                38f,

            width =
                180f,

            height =
                60f
        ) {
            game.showMainMenu()
        }

    private val claimAllButton = MenuButton("CLAIM ALL", 858f, 30f, 240f, 60f) {
        val rewards = HighScoreRepository.claimAllAchievementRewards()
        PlayerProfileRepository.addGrisilithCoins(rewards)
        if (rewards > 0) GameAudio.play(UiSound.CONFIRM)
        updateAchievementButtons()
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

        scrollbarDrag.reset()

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

        if (!handleScrollbarDrag()) {
            handleDragScrolling()
        }

        updateAchievementButtons()

        claimAllButton.isEnabled = HighScoreRepository.hasClaimableAchievementRewards()
        claimAllButton.update(pointer.position, pointer.justPressed, pointer.justReleased)

        achievementButtons.forEach { button ->
            if (
                button.isEnabled &&
                button.update(
                    pointerPosition = pointer.position,
                    pointerJustPressed = pointer.justPressed,
                    pointerJustReleased = pointer.justReleased
                )
            ) {
                updateAchievementButtons()
            }
        }

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

        batch.begin()

        batch.color = Color.WHITE
        batch.draw(
            archiveBackground,
            0f,
            0f,
            VIEWPORT_WIDTH,
            VIEWPORT_HEIGHT
        )

        batch.color = Color.WHITE
        batch.draw(
            stonePanel,
            155f,
            24f,
            970f,
            672f
        )

        renderer.renderTitle(
            batch = batch,
            titleFont = titleFont
        )

        claimAllButton.render(batch, font, pointer.position, pointer.isHeld)
        batch.end()

        val contentClip =
            Rectangle(190f, 104f, 850f, CONTENT_VIEWPORT_HEIGHT)

        val scissors =
            Rectangle()

        ScissorStack.calculateScissors(
            camera,
            batch.transformMatrix,
            contentClip,
            scissors
        )

        val contentVisible =
            ScissorStack.pushScissors(scissors)

        if (contentVisible) {
            renderer.renderShapes(
                shapeRenderer = shapeRenderer,
                scrollOffset = scrollOffset
            )

            batch.begin()

            renderer.renderTexts(
                batch = batch,
                font = font,
                statistics = HighScoreRepository.statistics,
                scrollOffset = scrollOffset
            )

            achievementButtons.forEach { button ->
                if (isAchievementButtonVisible(button.bounds)) {
                    button.render(
                        batch = batch,
                        font = font,
                        pointerPosition = pointer.position,
                        pointerHeld = pointer.isHeld
                    )

                    val iconSize = 28f
                    batch.color =
                        if (button.isEnabled) {
                            Color.WHITE
                        } else {
                            Color(0.38f, 0.38f, 0.38f, 0.72f)
                        }
                    batch.draw(
                        grisilithCoin,
                        button.bounds.x +
                            (button.bounds.width - iconSize) / 2f,
                        button.bounds.y +
                            (button.bounds.height - iconSize) / 2f,
                        iconSize,
                        iconSize
                    )
                    batch.color = Color.WHITE
                }
            }

            batch.end()
            ScissorStack.popScissors()
        }

        batch.begin()

        UiComponentRenderer.drawChronicleScrollbar(
            batch = batch,
            bounds = scrollbarBounds,
            visibleRatio = CONTENT_VIEWPORT_HEIGHT / CONTENT_HEIGHT,
            scrollRatio =
                if (MAXIMUM_SCROLL <= 0f) {
                    0f
                } else {
                    scrollOffset / MAXIMUM_SCROLL
                }
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
            GameAudio.play(UiSound.CLICK)
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
            || claimAllButton.bounds.contains(pointer.position) ||
            achievementButtons.any {
                isAchievementButtonVisible(it.bounds) &&
                    it.bounds.contains(pointer.position)
            }
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

    private fun handleScrollbarDrag(): Boolean {
        val visibleRatio = CONTENT_VIEWPORT_HEIGHT / CONTENT_HEIGHT
        val thumb =
            UiComponentRenderer.bronzeScrollbarThumbBounds(
                scrollbarBounds,
                visibleRatio,
                scrollOffset / MAXIMUM_SCROLL
            )
        val interactionBounds =
            Rectangle(
                scrollbarBounds.x - 5f,
                scrollbarBounds.y,
                scrollbarBounds.width + 10f,
                scrollbarBounds.height
            )
        return scrollbarDrag.update(
            pointer.position,
            pointer.justPressed,
            pointer.isHeld,
            pointer.justReleased,
            interactionBounds,
            thumb
        ) { ratio ->
            scrollOffset = ratio * MAXIMUM_SCROLL
        }
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

    private fun updateAchievementButtons() {

        achievementButtons.forEachIndexed {
                index,
                button ->

            button.bounds.set(
                renderer.achievementButtonBounds(
                    index = index,
                    scrollOffset = scrollOffset
                )
            )

            button.isEnabled =
                isAchievementButtonVisible(
                    button.bounds
                ) &&
                HighScoreRepository
                    .canClaimAchievementReward(
                        achievementTypes[index]
                    )
        }
    }

    private fun isAchievementButtonVisible(
        bounds: Rectangle
    ): Boolean =
        bounds.y >= 104f &&
            bounds.y + bounds.height <= 604f

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
        titleFont.dispose()
        archiveBackground.dispose()
        stonePanel.dispose()
        grisilithCoin.dispose()
        renderer.dispose()
    }
}
