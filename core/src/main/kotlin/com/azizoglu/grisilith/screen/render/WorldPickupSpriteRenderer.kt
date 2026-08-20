package com.azizoglu.grisilith.screen.render

import com.azizoglu.grisilith.chest.ChestWorldVisual
import com.azizoglu.grisilith.economy.CurrencyPickup
import com.azizoglu.grisilith.economy.CurrencyPickupType
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch

class WorldPickupSpriteRenderer {

    private val batch =
        SpriteBatch()

    private val goldTexture by lazy {
        loadTexture(
            path =
                "currency/gold_coin.png",
            useSmoothScaling =
                true
        )
    }

    private val grisilithTexture by lazy {
        loadTexture(
            path =
                "currency/grisilith_coin.png",
            useSmoothScaling =
                true
        )
    }

    private val chestTexture by lazy {
        loadTexture(
            "pickup/chest.png"
        )
    }

    fun renderCurrencies(
        pickups: List<CurrencyPickup>,
        camera: OrthographicCamera
    ) {
        if (
            pickups.isEmpty()
        ) {
            return
        }

        batch.projectionMatrix =
            camera.combined

        batch.begin()

        pickups.forEach {
                pickup ->

            val baseSize =
                when (
                    pickup.type
                ) {
                    CurrencyPickupType.GOLD ->
                        18f

                    CurrencyPickupType.GRISILITH ->
                        22f
                }

            val size =
                baseSize *
                    pickup.pulseScale

            val texture =
                when (
                    pickup.type
                ) {
                    CurrencyPickupType.GOLD ->
                        goldTexture

                    CurrencyPickupType.GRISILITH ->
                        grisilithTexture
                }

            batch.draw(
                texture,
                pickup.centerX -
                    size / 2f,
                pickup.centerY -
                    size / 2f,
                size,
                size
            )
        }

        batch.end()
    }

    fun renderChests(
        chests: List<ChestWorldVisual>,
        camera: OrthographicCamera
    ) {
        if (
            chests.isEmpty()
        ) {
            return
        }

        batch.projectionMatrix =
            camera.combined

        batch.begin()

        chests.forEach {
                chest ->

            batch.draw(
                chestTexture,
                chest.worldX -
                    CHEST_WIDTH / 2f,
                chest.visualY -
                    CHEST_HEIGHT / 2f,
                CHEST_WIDTH,
                CHEST_HEIGHT
            )
        }

        batch.end()
    }

    private fun loadTexture(
        path: String,
        useSmoothScaling: Boolean =
            false
    ): Texture {
        return Texture(
            Gdx.files.internal(
                path
            ),
            useSmoothScaling
        ).also {
            if (
                useSmoothScaling
            ) {
                it.setFilter(
                    Texture.TextureFilter.MipMapLinearLinear,
                    Texture.TextureFilter.Linear
                )
            } else {
                it.setFilter(
                    Texture.TextureFilter.Nearest,
                    Texture.TextureFilter.Nearest
                )
            }
        }
    }

    fun dispose() {
        goldTexture.dispose()
        grisilithTexture.dispose()
        chestTexture.dispose()
        batch.dispose()
    }

    companion object {
        private const val CHEST_WIDTH =
            46f

        private const val CHEST_HEIGHT =
            38f
    }
}
