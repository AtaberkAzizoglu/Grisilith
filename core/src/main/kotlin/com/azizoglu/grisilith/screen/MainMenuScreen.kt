package com.azizoglu.grisilith.screen

import com.azizoglu.grisilith.GrisilithGame
import com.azizoglu.grisilith.screen.components.MenuButton
import com.azizoglu.grisilith.screen.components.MenuPointer
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.azizoglu.grisilith.ui.typography.UiFontFactory
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.math.Rectangle
import com.azizoglu.grisilith.ui.theme.PanelVisualStyle
import com.azizoglu.grisilith.ui.theme.UiComponentRenderer
import com.azizoglu.grisilith.statistics.career.HighScoreRepository
import com.azizoglu.grisilith.online.OnlineBackend
import com.azizoglu.grisilith.online.OnlineUi
import com.azizoglu.grisilith.online.OnlineNameDialog
import com.badlogic.gdx.utils.JsonValue

class MainMenuScreen(
    private val game:
    GrisilithGame
) : ScreenAdapter() {

    companion object {
        private const val VIEWPORT_WIDTH =
            1280f

        private const val VIEWPORT_HEIGHT =
            720f

        private const val BACKGROUND_CHANGE_SECONDS =
            600f
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
        UiFontFactory.createDisplay()

    private val titleTexture =
        Texture(Gdx.files.internal("ui/branding/grisilith_title.png")).also {
            it.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        }

    private val chronicleRewardBadge =
        Texture(
            Gdx.files.internal(
                "ui/components/overlays/chronicle_reward_badge.png"
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

    private val backgroundPaths =
        listOf(
            "ui/backgrounds/main_menu/segliano.png",
            "ui/backgrounds/main_menu/town_undead_battle.png",
            "ui/backgrounds/main_menu/flokwarham.png",
            "ui/backgrounds/main_menu/antia.png",
            "ui/backgrounds/main_menu/jyn_ark.png",
            "ui/backgrounds/main_menu/xzorsh.png",
            "ui/backgrounds/main_menu/requiem.png",
            "ui/backgrounds/main_menu/dunsheaden.png",
            "ui/backgrounds/main_menu/grand_battle.png",
            "ui/backgrounds/main_menu/halatha_magic_school.png"
        )

    private val remainingBackgrounds =
        mutableListOf<Int>()

    private var currentBackgroundIndex = -1
    private var currentBackground: Texture? = null
    private var backgroundElapsed = 0f

    private val nextBackgroundBounds =
        Rectangle(24f, 24f, 54f, 54f)

    private var nextBackgroundPressed = false

    private var quitConfirmationOpen = false
    private val onlineUi = OnlineUi()
    private var nameDialog: OnlineNameDialog? = null
    private var nameAttemptedThisVisit = false
    private var board: JsonValue? = null
    private var boardLoading = false
    private var boardFailed = false
    private var nextBoardAt = 0L
    private var visible = false
    private var visit = 0

    private val menuButtons =
        listOf(
            MenuButton(
                text =
                    "PLAY",

                x =
                    950f,

                y =
                    480f,

                width =
                    270f,

                height =
                    62f
            ) {
                game.showPlayMenu()
            },

            MenuButton(
                text =
                    "COLLECTION",

                x =
                    950f,

                y =
                    408f,

                width =
                    270f,

                height =
                    62f
            ) {
                game.showCollection()
            },

            MenuButton(
                text =
                    "MERCHANT",

                x =
                    950f,

                y =
                    336f,

                width =
                    270f,

                height =
                    62f
            ) {
                game.showMerchant()
            },

            MenuButton(
                text =
                    "CHRONICLE",

                x =
                    950f,

                y =
                    264f,

                width =
                    270f,

                height =
                    62f
            ) {
                game.showHighScores()
            },

            MenuButton(
                text =
                    "SETTINGS",

                x =
                    950f,

                y =
                    192f,

                width =
                    270f,

                height =
                    62f
            ) {
                game.showSettings()
            },

            MenuButton("CREDITS",950f,120f,270f,62f) { game.showAudioCredits() },
            MenuButton(
                text = "QUIT GAME",
                x = 950f,
                y = 48f,
                width = 270f,
                height = 62f
            ) {
                quitConfirmationOpen = true
            }
        )

    private val quitButtons =
        listOf(
            MenuButton(
                text = "YES, QUIT",
                x = 455f,
                y = 254f,
                width = 175f,
                height = 64f
            ) {
                Gdx.app.exit()
            },
            MenuButton(
                text = "CANCEL",
                x = 650f,
                y = 254f,
                width = 175f,
                height = 64f
            ) {
                quitConfirmationOpen = false
            }
        )

    override fun show() {
        visible = true
        visit++
        nameAttemptedThisVisit = false
        nextBoardAt = 0L
        if (nameDialog == null) game.online?.let { nameDialog = OnlineNameDialog(it, onlineUi) }
        viewport.apply()

        camera.position.set(
            VIEWPORT_WIDTH / 2f,
            VIEWPORT_HEIGHT / 2f,
            0f
        )

        camera.update()

        pointer.reset()

        if (currentBackground == null) {
            showNextBackground()
        }
    }

    override fun render(
        delta:
        Float
    ) {
        pointer.update()
        val online = game.online
        if (online?.status == OnlineBackend.Status.READY) {
            if (!online.nameConfirmed && !nameAttemptedThisVisit && !quitConfirmationOpen) {
                nameAttemptedThisVisit = true
                nameDialog?.show(true) { nextBoardAt = 0L }
            }
            val now = System.nanoTime()/1_000_000L
            if (!boardLoading && now >= nextBoardAt) {
                boardLoading = true
                nextBoardAt = now + 30_000L
                val requestVisit = visit
                online.leaderboardAll { result ->
                    boardLoading = false
                    if (visible && requestVisit == visit) {
                        boardFailed = result.isFailure
                        board = result.getOrNull()
                    }
                }
            }
        } else {
            board = null
        }
        nameDialog?.update(pointer)
        val naming = nameDialog?.open == true

        if (!quitConfirmationOpen && !naming) {
            backgroundElapsed += delta.coerceAtMost(1f)
            if (backgroundElapsed >= BACKGROUND_CHANGE_SECONDS) {
                showNextBackground()
            }
            updateNextBackgroundButton()
        } else {
            nextBackgroundPressed = false
        }

        val activeButtons = if (naming) emptyList() else if (quitConfirmationOpen) quitButtons else menuButtons
        activeButtons.forEach { button ->
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

        batch.color = Color.WHITE
        currentBackground?.let { background ->
            batch.draw(background, 0f, 0f, VIEWPORT_WIDTH, VIEWPORT_HEIGHT)
        }

        batch.color = Color.WHITE
        batch.draw(
            titleTexture,
            895f,
            565f,
            0f,
            0f,
            360f,
            120f,
            1f,
            1f,
            0f,
            0,
            0,
            titleTexture.width,
            titleTexture.height,
            false,
            false
        )
        onlineUi.connection(batch, game.online, 38f, 676f)
        onlineUi.leaderboard(batch, board, game.online, boardLoading, boardFailed)

        val oldScaleX =
            font.data.scaleX

        val oldScaleY =
            font.data.scaleY

        font.data.setScale(0.66f)
        font.color = Color(0.72f, 0.73f, 0.75f, 1f)
        font.draw(batch, "Version 0.3.5", 440f, 48f, 400f, Align.center, false)
        font.draw(batch, "Made by o1cylon", 440f, 26f, 400f, Align.center, false)
        font.data.setScale(oldScaleX, oldScaleY)

        if (!quitConfirmationOpen) {
            UiComponentRenderer.drawGameSpeedButton(
                batch = batch,
                bounds = nextBackgroundBounds,
                speed = 1f
            )
            font.data.setScale(1.02f)
            font.color = Color(0.94f, 0.88f, 0.72f, 1f)
            font.draw(
                batch,
                ">",
                nextBackgroundBounds.x,
                nextBackgroundBounds.y + 37f,
                nextBackgroundBounds.width,
                Align.center,
                false
            )
            font.data.setScale(oldScaleX, oldScaleY)
        }

        if (quitConfirmationOpen) {
            UiComponentRenderer.drawPanel(
                batch = batch,
                bounds = Rectangle(370f, 210f, 540f, 230f),
                style = PanelVisualStyle.MODAL
            )
            font.data.setScale(1.15f)
            font.color = Color(0.95f, 0.91f, 0.84f, 1f)
            font.draw(batch, "ARE YOU SURE?", 370f, 382f, 540f, Align.center, false)
            font.data.setScale(0.78f)
            font.color = Color(0.75f, 0.76f, 0.78f, 1f)
            font.draw(batch, "Do you really want to quit the game?", 400f, 342f, 480f, Align.center, false)
            font.data.setScale(oldScaleX, oldScaleY)
        }

        activeButtons.forEach { button ->
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

        if (
            !quitConfirmationOpen &&
            HighScoreRepository
                .hasClaimableAchievementRewards()
        ) {
            batch.color = Color.WHITE
            batch.draw(
                chronicleRewardBadge,
                1186f,
                297f,
                24f,
                24f
            )
        }

        nameDialog?.render(batch,pointer)
        batch.end()
    }

    override fun hide() {
        visible = false
        visit++
        nameDialog?.close()
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

    private fun updateNextBackgroundButton() {
        val centerX = nextBackgroundBounds.x + nextBackgroundBounds.width / 2f
        val centerY = nextBackgroundBounds.y + nextBackgroundBounds.height / 2f
        val dx = pointer.position.x - centerX
        val dy = pointer.position.y - centerY
        val inside = dx * dx + dy * dy <= nextBackgroundBounds.width * nextBackgroundBounds.width / 4f

        if (pointer.justPressed) {
            nextBackgroundPressed = inside
        }
        if (pointer.justReleased) {
            if (nextBackgroundPressed && inside) {
                showNextBackground()
            }
            nextBackgroundPressed = false
        }
    }

    private fun showNextBackground() {
        if (remainingBackgrounds.isEmpty()) {
            val shuffled = backgroundPaths.indices.shuffled().toMutableList()
            if (shuffled.size > 1 && shuffled.last() == currentBackgroundIndex) {
                val swapIndex = shuffled.indexOfFirst { it != currentBackgroundIndex }
                val replacement = shuffled[swapIndex]
                shuffled[swapIndex] = shuffled.last()
                shuffled[shuffled.lastIndex] = replacement
            }
            remainingBackgrounds += shuffled
        }

        currentBackgroundIndex = remainingBackgrounds.removeAt(remainingBackgrounds.lastIndex)
        currentBackground?.dispose()
        currentBackground = Texture(Gdx.files.internal(backgroundPaths[currentBackgroundIndex])).also {
            it.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        }
        backgroundElapsed = 0f
    }

    override fun dispose() {
        hide()
        nameDialog?.dispose()
        onlineUi.dispose()
        batch.dispose()
        font.dispose()
        titleTexture.dispose()
        chronicleRewardBadge.dispose()
        currentBackground?.dispose()
    }
}
