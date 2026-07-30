package com.azizoglu.grisilith.screen

import com.azizoglu.grisilith.GrisilithGame
import com.azizoglu.grisilith.profile.PlayerProfileRepository
import com.azizoglu.grisilith.screen.components.MenuButton
import com.azizoglu.grisilith.screen.components.MenuPointer
import com.azizoglu.grisilith.screen.render.GrisilithCoinRenderer
import com.azizoglu.grisilith.screen.render.MerchantRenderer
import com.azizoglu.grisilith.screen.render.PurchasedStatPointRenderer
import com.azizoglu.grisilith.unlock.UnlockManager
import com.azizoglu.grisilith.unlock.UnlockPurchaseResult
import com.azizoglu.grisilith.unlock.UnlockRegistry
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.viewport.FitViewport

class MerchantScreen(
    private val game:
    GrisilithGame
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

    private val renderer =
        MerchantRenderer(
            grisilithCoinRenderer =
                GrisilithCoinRenderer(),

            purchasedStatPointRenderer =
                PurchasedStatPointRenderer()
        )

    private var message:
        String? =
        null

    private val backButton =
        MenuButton(
            text =
                "BACK",

            x =
                40f,

            y =
                40f,

            width =
                180f,

            height =
                60f
        ) {
            game.showMainMenu()
        }

    private val buyButton =
        MenuButton(
            text =
                "BUY",

            x =
                490f,

            y =
                250f,

            width =
                300f,

            height =
                70f
        ) {
            purchaseFirstAvailableUnlock()
            false
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

        message =
            null
    }

    override fun render(
        delta: Float
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

        val backScreenChanged =
            backButton.update(
                pointerPosition =
                    pointer.position,

                pointerJustPressed =
                    pointer.justPressed,

                pointerJustReleased =
                    pointer.justReleased
            )

        if (backScreenChanged) {
            return
        }

        val availableProduct =
            firstAvailableProduct()

        if (availableProduct != null) {
            buyButton.update(
                pointerPosition =
                    pointer.position,

                pointerJustPressed =
                    pointer.justPressed,

                pointerJustReleased =
                    pointer.justReleased
            )
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
                    .profile,

            product =
                availableProduct,

            message =
                message
        )

        if (availableProduct != null) {
            buyButton.render(
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

    private fun firstAvailableProduct() =
        UnlockRegistry
            .availableForMerchant(
                PlayerProfileRepository
                    .profile
                    .unlockedIds
            )
            .firstOrNull()

    private fun purchaseFirstAvailableUnlock():
        Boolean {

        val product =
            firstAvailableProduct()
                ?: return false

        message =
            when (
                UnlockManager.purchase(
                    product.id
                )
            ) {
                UnlockPurchaseResult.SUCCESS ->
                    "PURCHASE COMPLETE"

                UnlockPurchaseResult
                    .NOT_ENOUGH_COINS ->
                    "NOT ENOUGH GRISILITH COIN"

                UnlockPurchaseResult
                    .ALREADY_UNLOCKED ->
                    "ALREADY PURCHASED"

                UnlockPurchaseResult.LOCKED ->
                    "THIS UNLOCK IS LOCKED"

                UnlockPurchaseResult
                    .UNKNOWN_UNLOCK ->
                    "UNKNOWN UNLOCK"
            }

        return false
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
