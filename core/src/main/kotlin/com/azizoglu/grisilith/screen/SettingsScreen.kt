package com.azizoglu.grisilith.screen

import com.azizoglu.grisilith.GrisilithGame
import com.azizoglu.grisilith.screen.components.MenuButton
import com.azizoglu.grisilith.screen.components.MenuPointer
import com.azizoglu.grisilith.settings.GameSettings
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.FitViewport
import kotlin.math.roundToInt

class SettingsScreen(
    private val game:
    GrisilithGame,

    private val onBack:
        () -> Unit
) : ScreenAdapter() {

    companion object {
        private const val VIEWPORT_WIDTH =
            1280f

        private const val VIEWPORT_HEIGHT =
            720f

        private const val LABEL_X =
            250f

        private const val VALUE_X =
            690f

        private const val ROW_START_Y =
            500f

        private const val ROW_SPACING =
            62f
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

    private var buttons:
        List<MenuButton> =
        emptyList()

    override fun show() {
        viewport.apply()

        camera.position.set(
            VIEWPORT_WIDTH / 2f,
            VIEWPORT_HEIGHT / 2f,
            0f
        )

        camera.update()

        pointer.reset()

        buttons =
            buildButtons()
    }

    private fun refreshButtons() {
        buttons =
            buildButtons()
    }

    override fun render(
        delta:
        Float
    ) {
        pointer.update()

        /*
         * Ayar değiştikçe buton yazıları da değiştiği
         * için liste her frame güncel değerlerle kurulur.
         */
        val currentButtons =
            buttons

        currentButtons.forEach { button ->
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

        renderBackground()

        batch.projectionMatrix =
            camera.combined

        batch.begin()

        renderTitle()
        renderLabels()

        currentButtons.forEach { button ->
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

    private fun buildButtons():
        List<MenuButton> {

        val volumeButtonWidth =
            65f

        val valueButtonWidth =
            170f

        val buttonHeight =
            42f

        fun rowY(
            rowIndex:
            Int
        ): Float {
            return ROW_START_Y -
                rowIndex *
                ROW_SPACING -
                31f
        }

        return listOf(
            /*
             * Master Volume
             */
            MenuButton(
                text =
                    "-",

                x =
                    VALUE_X,

                y =
                    rowY(0),

                width =
                    volumeButtonWidth,

                height =
                    buttonHeight
            ) {
                GameSettings
                    .decreaseMasterVolume()

                refreshButtons()
            },

            MenuButton(
                text =
                    volumeText(
                        GameSettings.masterVolume
                    ),

                x =
                    VALUE_X +
                        75f,

                y =
                    rowY(0),

                width =
                    valueButtonWidth,

                height =
                    buttonHeight
            ) {
                /*
                 * Değer butonu bilgi amaçlı.
                 */
            },

            MenuButton(
                text =
                    "+",

                x =
                    VALUE_X +
                        255f,

                y =
                    rowY(0),

                width =
                    volumeButtonWidth,

                height =
                    buttonHeight
            ) {
                GameSettings
                    .increaseMasterVolume()
                refreshButtons()
            },

            /*
             * Music Volume
             */
            MenuButton(
                text =
                    "-",

                x =
                    VALUE_X,

                y =
                    rowY(1),

                width =
                    volumeButtonWidth,

                height =
                    buttonHeight
            ) {
                GameSettings
                    .decreaseMusicVolume()
                refreshButtons()
            },

            MenuButton(
                text =
                    volumeText(
                        GameSettings.musicVolume
                    ),

                x =
                    VALUE_X +
                        75f,

                y =
                    rowY(1),

                width =
                    valueButtonWidth,

                height =
                    buttonHeight
            ) {
            },

            MenuButton(
                text =
                    "+",

                x =
                    VALUE_X +
                        255f,

                y =
                    rowY(1),

                width =
                    volumeButtonWidth,

                height =
                    buttonHeight
            ) {
                GameSettings
                    .increaseMusicVolume()
                refreshButtons()
            },

            /*
             * Sound Effects Volume
             */
            MenuButton(
                text =
                    "-",

                x =
                    VALUE_X,

                y =
                    rowY(2),

                width =
                    volumeButtonWidth,

                height =
                    buttonHeight
            ) {
                GameSettings
                    .decreaseSoundEffectsVolume()
                refreshButtons()
            },

            MenuButton(
                text =
                    volumeText(
                        GameSettings
                            .soundEffectsVolume
                    ),

                x =
                    VALUE_X +
                        75f,

                y =
                    rowY(2),

                width =
                    valueButtonWidth,

                height =
                    buttonHeight
            ) {
            },

            MenuButton(
                text =
                    "+",

                x =
                    VALUE_X +
                        255f,

                y =
                    rowY(2),

                width =
                    volumeButtonWidth,

                height =
                    buttonHeight
            ) {
                GameSettings
                    .increaseSoundEffectsVolume()
                refreshButtons()
            },

            /*
             * Fullscreen
             */
            MenuButton(
                text =
                    onOffText(
                        GameSettings.fullscreen,
                        enabledText =
                            "FULLSCREEN",

                        disabledText =
                            "WINDOWED"
                    ),

                x =
                    VALUE_X,

                y =
                    rowY(3),

                width =
                    320f,

                height =
                    buttonHeight
            ) {
                GameSettings
                    .toggleFullscreen()
                refreshButtons()
            },

            /*
             * VSync
             */
            MenuButton(
                text =
                    onOffText(
                        GameSettings.vSync
                    ),

                x =
                    VALUE_X,

                y =
                    rowY(4),

                width =
                    320f,

                height =
                    buttonHeight
            ) {
                GameSettings
                    .toggleVSync()
                refreshButtons()
            },

            /*
             * Screen Shake
             */
            MenuButton(
                text =
                    onOffText(
                        GameSettings.screenShake
                    ),

                x =
                    VALUE_X,

                y =
                    rowY(5),

                width =
                    320f,

                height =
                    buttonHeight
            ) {
                GameSettings
                    .toggleScreenShake()
                refreshButtons()
            },

            /*
             * Damage Numbers
             */
            MenuButton(
                text =
                    onOffText(
                        GameSettings.damageNumbers
                    ),

                x =
                    VALUE_X,

                y =
                    rowY(6),

                width =
                    320f,

                height =
                    buttonHeight
            ) {
                GameSettings
                    .toggleDamageNumbers()
                refreshButtons()
            },

            MenuButton(
                text =
                    "RESET TO DEFAULTS",

                x =
                    280f,

                y =
                    35f,

                width =
                    320f,

                height =
                    58f
            ) {
                GameSettings
                    .resetToDefaults()
                refreshButtons()
            },

            MenuButton(
                text =
                    "BACK",

                x =
                    680f,

                y =
                    35f,

                width =
                    320f,

                height =
                    58f,

                onClick =
                    onBack
            )
        )
    }

    private fun renderBackground() {
        Gdx.gl.glClearColor(
            0.035f,
            0.025f,
            0.055f,
            1f
        )

        Gdx.gl.glClear(
            GL20.GL_COLOR_BUFFER_BIT
        )
    }

    private fun renderTitle() {
        val oldScaleX =
            font.data.scaleX

        val oldScaleY =
            font.data.scaleY

        font.data.setScale(
            2f
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            "SETTINGS",
            0f,
            650f,
            VIEWPORT_WIDTH,
            Align.center,
            false
        )

        font.data.setScale(
            0.85f
        )

        font.color =
            Color.GRAY

        font.draw(
            batch,
            "Display settings are applied immediately.",
            0f,
            603f,
            VIEWPORT_WIDTH,
            Align.center,
            false
        )

        font.data.setScale(
            oldScaleX,
            oldScaleY
        )
    }

    private fun renderLabels() {
        val labels =
            listOf(
                "Master Volume",
                "Music Volume",
                "Sound Effects Volume",
                "Display Mode",
                "VSync",
                "Screen Shake",
                "Damage Numbers"
            )

        val oldScaleX =
            font.data.scaleX

        val oldScaleY =
            font.data.scaleY

        font.data.setScale(
            1.1f
        )

        labels.forEachIndexed {
                index,
                label ->

            font.color =
                Color.LIGHT_GRAY

            font.draw(
                batch,
                label,
                LABEL_X,
                ROW_START_Y -
                    index *
                    ROW_SPACING
            )
        }

        font.data.setScale(
            oldScaleX,
            oldScaleY
        )
    }

    private fun volumeText(
        value:
        Float
    ): String {
        val percent =
            (
                value *
                    100f
                )
                .roundToInt()

        return "$percent%"
    }

    private fun onOffText(
        enabled:
        Boolean,

        enabledText:
        String =
            "ON",

        disabledText:
        String =
            "OFF"
    ): String {
        return if (enabled) {
            enabledText
        } else {
            disabledText
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
