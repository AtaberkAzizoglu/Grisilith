package com.azizoglu.grisilith.screen

import com.azizoglu.grisilith.save.RunSaveSnapshot
import com.azizoglu.grisilith.screen.components.MenuButton
import com.azizoglu.grisilith.screen.components.MenuPointer
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.FitViewport

class LoadGameScreen(
    private val snapshot:
    RunSaveSnapshot,

    private val onContinue:
        () -> Unit,

    private val onBack:
        () -> Unit
) : ScreenAdapter() {

    companion object {

        private const val VIEWPORT_WIDTH =
            1280f

        private const val VIEWPORT_HEIGHT =
            720f
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

    private val buttons =
        listOf(
            MenuButton(
                text =
                    "CONTINUE",

                x =
                    440f,

                y =
                    145f,

                width =
                    400f,

                height =
                    70f
            ) {
                onContinue()
            },

            MenuButton(
                text =
                    "BACK",

                x =
                    490f,

                y =
                    55f,

                width =
                    300f,

                height =
                    60f
            ) {
                onBack()
            }
        )

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

        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.ESCAPE
            )
        ) {
            onBack()
            return
        }

        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.ENTER
            )
        ) {
            onContinue()
            return
        }

        pointer.update()

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

            if (
                screenChanged
            ) {
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

        renderTitle()
        renderInformation()

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
            "CONTINUE RUN",
            0f,
            625f,
            VIEWPORT_WIDTH,
            Align.center,
            false
        )

        font.data.setScale(
            oldScaleX,
            oldScaleY
        )
    }

    private fun renderInformation() {

        val preview =
            snapshot.preview

        val panelX =
            340f

        val labelX =
            panelX

        val valueX =
            650f

        var currentY =
            500f

        val lineSpacing =
            62f

        font.data.setScale(
            1.15f
        )

        drawInformationRow(
            label =
                "CHARACTER",

            value =
                preview.characterDisplayName,

            labelX =
                labelX,

            valueX =
                valueX,

            y =
                currentY
        )

        currentY -=
            lineSpacing

        drawInformationRow(
            label =
                "MAP",

            value =
                preview.mapDisplayName,

            labelX =
                labelX,

            valueX =
                valueX,

            y =
                currentY
        )

        currentY -=
            lineSpacing

        drawInformationRow(
            label =
                "DIFFICULTY",

            value =
                preview.difficultyDisplayName,

            labelX =
                labelX,

            valueX =
                valueX,

            y =
                currentY
        )

        currentY -=
            lineSpacing

        drawInformationRow(
            label =
                "STAGE",

            value =
                "${snapshot.checkpointStage} / " +
                    "${snapshot.maximumStage}",

            labelX =
                labelX,

            valueX =
                valueX,

            y =
                currentY
        )

        font.data.setScale(
            0.8f
        )

        font.color =
            Color.GRAY

        font.draw(
            batch,
            "This checkpoint can only be used once.",
            0f,
            250f,
            VIEWPORT_WIDTH,
            Align.center,
            false
        )
    }

    private fun drawInformationRow(
        label:
        String,

        value:
        String,

        labelX:
        Float,

        valueX:
        Float,

        y:
        Float
    ) {

        font.color =
            Color.GOLD

        font.draw(
            batch,
            label,
            labelX,
            y
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            value,
            valueX,
            y
        )
    }

    override fun resize(
        width:
        Int,

        height:
        Int
    ) {

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
