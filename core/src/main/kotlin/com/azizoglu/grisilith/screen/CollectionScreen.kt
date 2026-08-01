package com.azizoglu.grisilith.screen

import com.azizoglu.grisilith.GrisilithGame
import com.azizoglu.grisilith.content.ContentCategory
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
    private val game:
    GrisilithGame
) : ScreenAdapter() {

    companion object {

        private const val VIEWPORT_WIDTH =
            1280f

        private const val VIEWPORT_HEIGHT =
            720f

        private const val BUTTON_WIDTH =
            320f

        private const val BUTTON_HEIGHT =
            65f

        private const val LEFT_COLUMN_X =
            285f

        private const val RIGHT_COLUMN_X =
            675f

        private const val FIRST_ROW_Y =
            470f

        private const val ROW_SPACING =
            90f
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

    private val renderer =
        CollectionRenderer(
            grisilithCoinRenderer =
                GrisilithCoinRenderer(),

            purchasedStatPointRenderer =
                PurchasedStatPointRenderer()
        )

    private val categoryButtons =
        listOf(
            createCategoryButton(
                text =
                    "CHARACTERS",

                category =
                    ContentCategory.CHARACTER,

                x =
                    LEFT_COLUMN_X,

                y =
                    FIRST_ROW_Y
            ),

            createCategoryButton(
                text =
                    "WEAPONS",

                category =
                    ContentCategory.WEAPON,

                x =
                    RIGHT_COLUMN_X,

                y =
                    FIRST_ROW_Y
            ),

            createCategoryButton(
                text =
                    "ARMORS",

                category =
                    ContentCategory.ARMOR,

                x =
                    LEFT_COLUMN_X,

                y =
                    FIRST_ROW_Y -
                        ROW_SPACING
            ),

            createCategoryButton(
                text =
                    "ITEMS",

                category =
                    ContentCategory.ITEM,

                x =
                    RIGHT_COLUMN_X,

                y =
                    FIRST_ROW_Y -
                        ROW_SPACING
            ),

            createCategoryButton(
                text =
                    "TAMGAS",

                category =
                    ContentCategory.TAMGA,

                x =
                    LEFT_COLUMN_X,

                y =
                    FIRST_ROW_Y -
                        ROW_SPACING *
                        2f
            ),

            createCategoryButton(
                text =
                    "ENEMIES",

                category =
                    ContentCategory.ENEMY,

                x =
                    RIGHT_COLUMN_X,

                y =
                    FIRST_ROW_Y -
                        ROW_SPACING *
                        2f
            ),

            createCategoryButton(
                text =
                    "MAPS",

                category =
                    ContentCategory.MAP,

                x =
                    480f,

                y =
                    FIRST_ROW_Y -
                        ROW_SPACING *
                        3f
            )
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
            game.showMainMenu()
        }

    private val buttons =
        categoryButtons +
            backButton

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

    override fun render(
        delta:
        Float
    ) {

        pointer.update()

        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.ESCAPE
            )
        ) {
            game.showMainMenu()
            return
        }

        buttons.forEach {
                button ->

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

        renderer.render(
            batch =
                batch,

            font =
                font,

            profile =
                PlayerProfileRepository
                    .profile
        )

        buttons.forEach {
                button ->

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

        batch.end()
    }

    private fun createCategoryButton(
        text:
        String,

        category:
        ContentCategory,

        x:
        Float,

        y:
        Float
    ): MenuButton {

        return MenuButton(
            text =
                text,

            x =
                x,

            y =
                y,

            width =
                BUTTON_WIDTH,

            height =
                BUTTON_HEIGHT
        ) {
            game.showCollectionCategory(
                category
            )
        }
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

    override fun dispose() {

        batch.dispose()
        font.dispose()
    }
}
