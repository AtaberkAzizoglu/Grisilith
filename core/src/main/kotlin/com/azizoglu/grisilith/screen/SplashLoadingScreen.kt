package com.azizoglu.grisilith.screen

import com.azizoglu.grisilith.GrisilithGame
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Matrix4
import kotlin.math.max

class SplashLoadingScreen(
    private val game:
    GrisilithGame,

    private val initializationSteps:
    List<() -> Unit>,

    private val onFinished:
        () -> Unit
) : ScreenAdapter() {

    private val batch =
        SpriteBatch()

    private val shapeRenderer =
        ShapeRenderer()

    private val font =
        BitmapFont()

    private val glyphLayout =
        GlyphLayout()

    private val projection =
        Matrix4()

    private val splashTexture =
        Texture(
            Gdx.files.internal(
                SPLASH_TEXTURE_PATH
            )
        ).apply {
            setFilter(
                Texture.TextureFilter.Linear,
                Texture.TextureFilter.Linear
            )
        }

    private var elapsedSeconds =
        0f

    private var completedSteps =
        0

    private var startupFinished =
        false

    override fun show() {
        /*
         * Ekran açılır açılmaz progress 0'dan başlar.
         * Ağır başlangıç işlerini render döngüsüne yayarak splash'ın
         * gerçekten ekranda çizilebilmesini sağlıyoruz.
         */
        elapsedSeconds =
            0f

        completedSteps =
            0

        startupFinished =
            false
    }

    override fun render(
        delta: Float
    ) {
        val safeDelta =
            delta.coerceAtLeast(
                0f
            )

        elapsedSeconds +=
            safeDelta

        Gdx.gl.glClearColor(
            0f,
            0f,
            0f,
            1f
        )

        Gdx.gl.glClear(
            GL20.GL_COLOR_BUFFER_BIT
        )

        renderSplash()
        renderLoadingUi()

        performNextInitializationStep()

        val allStepsFinished =
            completedSteps >=
                initializationSteps.size

        if (
            !startupFinished &&
            allStepsFinished &&
            elapsedSeconds >=
            MINIMUM_SPLASH_SECONDS
        ) {
            startupFinished =
                true

            /*
             * Screen değiştirme işini mevcut Game akışına bırakıyoruz.
             * Eski splash screen, GrisilithGame.changeScreen() tarafından
             * güvenli şekilde dispose edilecek.
             */
            onFinished()
        }
    }

    private fun performNextInitializationStep() {
        if (
            completedSteps >=
            initializationSteps.size
        ) {
            return
        }

        initializationSteps[
            completedSteps
        ].invoke()

        completedSteps +=
            1
    }

    private fun renderSplash() {
        val screenWidth =
            Gdx.graphics.width
                .coerceAtLeast(
                    1
                )
                .toFloat()

        val screenHeight =
            Gdx.graphics.height
                .coerceAtLeast(
                    1
                )
                .toFloat()

        projection.setToOrtho2D(
            0f,
            0f,
            screenWidth,
            screenHeight
        )

        batch.projectionMatrix =
            projection

        val textureWidth =
            splashTexture.width
                .toFloat()

        val textureHeight =
            splashTexture.height
                .toFloat()

        /*
         * COVER davranışı:
         * farklı aspect ratio'larda siyah şerit bırakmadan tüm ekranı
         * doldurur. Fazlalık sadece kenarlardan kırpılır.
         */
        val scale =
            max(
                screenWidth /
                    textureWidth,

                screenHeight /
                    textureHeight
            )

        val drawWidth =
            textureWidth *
                scale

        val drawHeight =
            textureHeight *
                scale

        val drawX =
            (
                screenWidth -
                    drawWidth
                ) /
                2f

        val drawY =
            (
                screenHeight -
                    drawHeight
                ) /
                2f

        batch.begin()

        batch.color =
            Color.WHITE

        batch.draw(
            splashTexture,
            drawX,
            drawY,
            drawWidth,
            drawHeight
        )

        batch.end()
    }

    private fun renderLoadingUi() {
        val screenWidth =
            Gdx.graphics.width
                .coerceAtLeast(
                    1
                )
                .toFloat()

        val screenHeight =
            Gdx.graphics.height
                .coerceAtLeast(
                    1
                )
                .toFloat()

        projection.setToOrtho2D(
            0f,
            0f,
            screenWidth,
            screenHeight
        )

        val barWidth =
            screenWidth *
                0.42f

        val barHeight =
            (
                screenHeight *
                    0.012f
                )
                .coerceIn(
                    8f,
                    16f
                )

        val barX =
            (
                screenWidth -
                    barWidth
                ) /
                2f

        val barY =
            screenHeight *
                0.055f

        val progress =
            currentProgress()

        shapeRenderer.projectionMatrix =
            projection

        Gdx.gl.glEnable(
            GL20.GL_BLEND
        )

        Gdx.gl.glBlendFunc(
            GL20.GL_SRC_ALPHA,
            GL20.GL_ONE_MINUS_SRC_ALPHA
        )

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Filled
        )

        shapeRenderer.color =
            Color(
                0f,
                0f,
                0f,
                0.68f
            )

        shapeRenderer.rect(
            barX - 4f,
            barY - 4f,
            barWidth + 8f,
            barHeight + 8f
        )

        shapeRenderer.color =
            Color(
                0.10f,
                0.08f,
                0.13f,
                0.94f
            )

        shapeRenderer.rect(
            barX,
            barY,
            barWidth,
            barHeight
        )

        shapeRenderer.color =
            Color(
                0.86f,
                0.65f,
                0.23f,
                1f
            )

        shapeRenderer.rect(
            barX,
            barY,
            barWidth *
                progress,
            barHeight
        )

        shapeRenderer.end()

        renderCredit(
            screenWidth =
                screenWidth,

            screenHeight =
                screenHeight,

            barY =
                barY
        )

        Gdx.gl.glDisable(
            GL20.GL_BLEND
        )
    }

    private fun renderCredit(
        screenWidth: Float,
        screenHeight: Float,
        barY: Float
    ) {
        projection.setToOrtho2D(
            0f,
            0f,
            screenWidth,
            screenHeight
        )

        batch.projectionMatrix =
            projection

        val targetFontSize =
            (
                screenHeight *
                    0.014f
                )
                .coerceIn(
                    11f,
                    17f
                )

        /*
         * LibGDX'in varsayılan BitmapFont'u ayrı bir bold font asset'i
         * gerektirmesin diye ufak çoklu çizim ile bold hissi veriyoruz.
         */
        font.data.setScale(
            targetFontSize /
                font.capHeight
                    .coerceAtLeast(
                        1f
                    )
        )

        val text =
            CREDIT_TEXT

        glyphLayout.setText(
            font,
            text
        )

        val textX =
            (
                screenWidth *
                    0.018f
                )
                .coerceAtLeast(
                    12f
                )

        val textY =
            (
                screenHeight *
                    0.028f
                )
                .coerceAtLeast(
                    glyphLayout.height +
                        8f
                )

        batch.begin()

        font.color =
            Color(
                0f,
                0f,
                0f,
                0.85f
            )

        font.draw(
            batch,
            text,
            textX + 1.5f,
            textY - 1.5f
        )

        font.color =
            Color(
                0.94f,
                0.90f,
                0.82f,
                1f
            )

        font.draw(
            batch,
            text,
            textX,
            textY
        )

        font.draw(
            batch,
            text,
            textX + 0.8f,
            textY
        )

        batch.end()
    }

    private fun currentProgress():
        Float {

        if (
            initializationSteps.isEmpty()
        ) {
            return 1f
        }

        return (
            completedSteps
                .toFloat() /
                initializationSteps
                    .size
                    .toFloat()
            )
            .coerceIn(
                0f,
                1f
            )
    }

    override fun resize(
        width: Int,
        height: Int
    ) {
        /*
         * Projection her render'da güncellendiği için burada ek iş yok.
         */
    }

    override fun dispose() {
        splashTexture.dispose()
        font.dispose()
        shapeRenderer.dispose()
        batch.dispose()
    }

    companion object {

        private const val SPLASH_TEXTURE_PATH =
            "ui/splash/Splash.png"

        private const val CREDIT_TEXT =
            "Made by o1cylon"

        private const val MINIMUM_SPLASH_SECONDS =
            2.0f
    }
}
