package com.azizoglu.grisilith.screen.render

import com.azizoglu.grisilith.chest.ChestOpeningState
import com.azizoglu.grisilith.chest.ChestRewardRoll
import com.azizoglu.grisilith.chest.ChestRewardType
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.Align
import kotlin.math.sin

/*
 * Chest ekranının yalnızca görsel katmanı.
 *
 * Gerçek chest, item ve character görselleri geldiğinde
 * yalnızca placeholder çizim fonksiyonları değiştirilecek.
 */
class ChestRewardOverlayRenderer(
    private val camera: OrthographicCamera,
    private val cameraWidth: Float,
    private val cameraHeight: Float,
    private val shapeRenderer: ShapeRenderer,
    private val batch: SpriteBatch,
    private val font: BitmapFont,
    private val characterNameProvider: () -> String,
    private val characterStatsProvider:
        () -> List<ChestCharacterStatRow>
) {

    data class Layout(
        val panelBounds: Rectangle,
        val chestBounds: Rectangle,
        val rewardVisualBounds: Rectangle,
        val characterPortraitBounds: Rectangle,
        val characterStatsBounds: Rectangle,
        val takeButtonBounds: Rectangle,
        val discardButtonBounds: Rectangle,
        val itemDetailBounds: Rectangle
    )

    private val layout =
        Layout(
            panelBounds = Rectangle(),
            chestBounds = Rectangle(),
            rewardVisualBounds = Rectangle(),
            characterPortraitBounds = Rectangle(),
            characterStatsBounds = Rectangle(),
            takeButtonBounds = Rectangle(),
            discardButtonBounds = Rectangle(),
            itemDetailBounds = Rectangle()
        )

    private var animationTimeSeconds =
        0f

    fun update(
        delta: Float
    ) {
        animationTimeSeconds +=
            delta.coerceAtLeast(
                0f
            )
    }

    fun currentLayout():
        Layout {

        updateLayout()
        return layout
    }

    fun renderShapes(
        state: ChestOpeningState,
        reward: ChestRewardRoll?,
        revealProgress: Float,
        discardEnabled: Boolean,
        itemDetailOpen: Boolean
    ) {

        updateLayout()

        renderFog()
        renderPanel(
            accentColor =
                accentColor(
                    reward
                )
        )

        when (state) {
            ChestOpeningState.LOCKED,
            ChestOpeningState.UNLOCKED,
            ChestOpeningState.REVEALING -> {
                renderChestPlaceholder(
                    state =
                        state,

                    accentColor =
                        accentColor(
                            reward
                        ),

                    revealProgress =
                        revealProgress
                )
            }

            ChestOpeningState.REWARD_SHOWN -> {
                renderRewardPlaceholder(
                    reward =
                        reward,

                    accentColor =
                        accentColor(
                            reward
                        )
                )

                renderCharacterPanel()

                renderButton(
                    bounds =
                        layout.takeButtonBounds,

                    enabled =
                        true,

                    emphasized =
                        true
                )

                renderButton(
                    bounds =
                        layout.discardButtonBounds,

                    enabled =
                        discardEnabled,

                    emphasized =
                        false
                )

                if (
                    itemDetailOpen &&
                    reward?.rewardType ==
                    ChestRewardType.ITEM
                ) {
                    renderItemDetailBackground(
                        accentColor(
                            reward
                        )
                    )
                }
            }

            ChestOpeningState.COMPLETED -> {
            }
        }
    }

    fun renderTexts(
        state: ChestOpeningState,
        reward: ChestRewardRoll?,
        currentChestNumber: Int,
        totalChestCount: Int,
        remainingDiscardCount: Int,
        itemDetailOpen: Boolean
    ) {

        updateLayout()

        val oldScaleX =
            font.data.scaleX

        val oldScaleY =
            font.data.scaleY

        val oldColor =
            Color(
                font.color
            )

        renderHeader(
            currentChestNumber =
                currentChestNumber,

            totalChestCount =
                totalChestCount
        )

        when (state) {
            ChestOpeningState.LOCKED -> {
                renderCenteredInstruction(
                    title =
                        "SEALED CHEST",

                    instruction =
                        "Click the chest to unlock it"
                )
            }

            ChestOpeningState.UNLOCKED -> {
                renderCenteredInstruction(
                    title =
                        rewardTypeLabel(
                            reward
                        ),

                    instruction =
                        "Click the chest again to open it",

                    color =
                        accentColor(
                            reward
                        )
                )
            }

            ChestOpeningState.REVEALING -> {
                renderCenteredInstruction(
                    title =
                        "OPENING...",

                    instruction =
                        ""
                )
            }

            ChestOpeningState.REWARD_SHOWN -> {

                if (
                    itemDetailOpen &&
                    reward?.rewardType ==
                    ChestRewardType.ITEM
                ) {
                    /*
                     * Detay kartı açıkken alttaki ödül adı,
                     * açıklama, TAKE / DISCARD ve karakter
                     * paneli yazıları tekrar çizilmez.
                     */
                    renderItemDetailTexts(
                        reward
                    )
                } else {
                    renderRewardTexts(
                        reward =
                            reward,

                        remainingDiscardCount =
                            remainingDiscardCount
                    )
                }
            }

            ChestOpeningState.COMPLETED -> {
            }
        }

        font.data.setScale(
            oldScaleX,
            oldScaleY
        )

        font.color =
            oldColor
    }

    private fun renderFog() {

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Filled
        )

        /*
         * Dünya görüntüsünü koruyan hafif mor sis.
         */
        shapeRenderer.color =
            Color(
                0.17f,
                0.06f,
                0.24f,
                0.72f
            )

        shapeRenderer.rect(
            camera.position.x -
                cameraWidth /
                2f,
            camera.position.y -
                cameraHeight /
                2f,
            cameraWidth,
            cameraHeight
        )

        shapeRenderer.end()
    }

    private fun renderPanel(
        accentColor: Color
    ) {

        val panel =
            layout.panelBounds

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Filled
        )

        /*
         * "Yaklaşık %20 transparan" hissi:
         * arka plan hâlâ görünür, panel daha koyu bir merkez oluşturur.
         */
        shapeRenderer.color =
            Color(
                0.035f,
                0.025f,
                0.055f,
                0.82f
            )

        shapeRenderer.rect(
            panel.x,
            panel.y,
            panel.width,
            panel.height
        )

        /*
         * Panelin iç kenarında yumuşak accent katmanı.
         */
        shapeRenderer.color =
            Color(
                accentColor.r,
                accentColor.g,
                accentColor.b,
                0.10f
            )

        shapeRenderer.rect(
            panel.x +
                10f,
            panel.y +
                10f,
            panel.width -
                20f,
            panel.height -
                20f
        )

        shapeRenderer.end()

        val pulseAlpha =
            0.72f +
                0.22f *
                (
                    sin(
                        animationTimeSeconds *
                            3.5f
                    ) *
                        0.5f +
                        0.5f
                    )

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Line
        )

        repeat(
            4
        ) { index ->


        val inset =
                index *
                    5f

            shapeRenderer.color =
                Color(
                    accentColor.r,
                    accentColor.g,
                    accentColor.b,
                    (
                        pulseAlpha -
                            index *
                            0.13f
                        )
                        .coerceIn(
                            0.15f,
                            1f
                        )
                )
        }

        shapeRenderer.end()
    }

    private fun renderChestPlaceholder(
        state: ChestOpeningState,
        accentColor: Color,
        revealProgress: Float
    ) {

        val chest =
            layout.chestBounds

        val safeRevealProgress =
            revealProgress.coerceIn(
                0f,
                1f
            )

        val verticalOffset =
            if (
                state ==
                ChestOpeningState.REVEALING
            ) {
                -220f *
                    safeRevealProgress
            } else {
                0f
            }

        val alpha =
            if (
                state ==
                ChestOpeningState.REVEALING
            ) {
                1f -
                    safeRevealProgress
            } else {
                1f
            }

        val x =
            chest.x

        val y =
            chest.y +
                verticalOffset

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Filled
        )

        /*
         * Chest body.
         */
        shapeRenderer.color =
            Color(
                0.34f,
                0.16f,
                0.055f,
                alpha
            )

        shapeRenderer.rect(
            x,
            y,
            chest.width,
            chest.height *
                0.58f
        )

        /*
         * Chest lid.
         */
        shapeRenderer.color =
            Color(
                0.46f,
                0.23f,
                0.07f,
                alpha
            )

        shapeRenderer.rect(
            x,
            y +
                chest.height *
                0.58f,
            chest.width,
            chest.height *
                0.42f
        )

        /*
         * Metal strips.
         */
        shapeRenderer.color =
            Color(
                accentColor.r,
                accentColor.g,
                accentColor.b,
                alpha
            )

        shapeRenderer.rect(
            x +
                chest.width *
                0.12f,
            y,
            18f,
            chest.height
        )

        shapeRenderer.rect(
            x +
                chest.width *
                0.80f,
            y,
            18f,
            chest.height
        )

        shapeRenderer.rect(
            x,
            y +
                chest.height *
                0.52f,
            chest.width,
            14f
        )

        /*
         * Lock.
         */
        shapeRenderer.color =
            if (
                state ==
                ChestOpeningState.LOCKED
            ) {
                Color(
                    0.95f,
                    0.78f,
                    0.18f,
                    alpha
                )
            } else {
                Color(
                    accentColor.r,
                    accentColor.g,
                    accentColor.b,
                    alpha
                )
            }

        shapeRenderer.rect(
            x +
                chest.width /
                2f -
                22f,
            y +
                chest.height *
                0.40f,
            44f,
            48f
        )

        shapeRenderer.end()
    }

    private fun renderRewardPlaceholder(
        reward: ChestRewardRoll?,
        accentColor: Color
    ) {

        val visual =
            layout.rewardVisualBounds

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Filled
        )

        shapeRenderer.color =
            Color(
                0.055f,
                0.045f,
                0.075f,
                1f
            )

        shapeRenderer.rect(
            visual.x,
            visual.y,
            visual.width,
            visual.height
        )

        shapeRenderer.color =
            Color(
                accentColor.r,
                accentColor.g,
                accentColor.b,
                0.28f
            )

        shapeRenderer.rect(
            visual.x +
                10f,
            visual.y +
                10f,
            visual.width -
                20f,
            visual.height -
                20f
        )

        /*
         * Geçici görsel:
         * Item için kare icon, Stat için elmas benzeri blok.
         */
        shapeRenderer.color =
            accentColor

        if (
            reward?.rewardType ==
            ChestRewardType.STAT
        ) {
            val centerX =
                visual.x +
                    visual.width /
                    2f

            val centerY =
                visual.y +
                    visual.height /
                    2f

            shapeRenderer.triangle(
                centerX,
                centerY +
                    62f,
                centerX +
                    62f,
                centerY,
                centerX,
                centerY -
                    62f
            )

            shapeRenderer.triangle(
                centerX,
                centerY +
                    62f,
                centerX,
                centerY -
                    62f,
                centerX -
                    62f,
                centerY
            )
        } else {
            shapeRenderer.rect(
                visual.x +
                    visual.width /
                    2f -
                    62f,
                visual.y +
                    visual.height /
                    2f -
                    62f,
                124f,
                124f
            )
        }

        shapeRenderer.end()

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Line
        )

        shapeRenderer.color =
            accentColor

        shapeRenderer.rect(
            visual.x,
            visual.y,
            visual.width,
            visual.height
        )

        shapeRenderer.end()
    }

    private fun renderCharacterPanel() {

        val portrait =
            layout.characterPortraitBounds

        val stats =
            layout.characterStatsBounds

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Filled
        )

        shapeRenderer.color =
            Color(
                0.065f,
                0.055f,
                0.085f,
                1f
            )

        shapeRenderer.rect(
            portrait.x,
            portrait.y,
            portrait.width,
            portrait.height
        )

        shapeRenderer.rect(
            stats.x,
            stats.y,
            stats.width,
            stats.height
        )

        /*
         * Portrait placeholder.
         */
        shapeRenderer.color =
            Color(
                0.24f,
                0.20f,
                0.30f,
                1f
            )

        shapeRenderer.rect(
            portrait.x +
                15f,
            portrait.y +
                15f,
            portrait.width -
                30f,
            portrait.height -
                30f
        )

        shapeRenderer.end()

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Line
        )

        shapeRenderer.color =
            Color(
                0.45f,
                0.38f,
                0.56f,
                1f
            )

        shapeRenderer.rect(
            portrait.x,
            portrait.y,
            portrait.width,
            portrait.height
        )

        shapeRenderer.rect(
            stats.x,
            stats.y,
            stats.width,
            stats.height
        )

        shapeRenderer.end()
    }

    private fun renderButton(
        bounds: Rectangle,
        enabled: Boolean,
        emphasized: Boolean
    ) {

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Filled
        )

        shapeRenderer.color =
            when {
                !enabled -> {
                    Color(
                        0.08f,
                        0.075f,
                        0.09f,
                        1f
                    )
                }

                emphasized -> {
                    Color(
                        0.15f,
                        0.29f,
                        0.20f,
                        1f
                    )
                }

                else -> {
                    Color(
                        0.25f,
                        0.12f,
                        0.16f,
                        1f
                    )
                }
            }

        shapeRenderer.rect(
            bounds.x,
            bounds.y,
            bounds.width,
            bounds.height
        )

        shapeRenderer.end()

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Line
        )

        shapeRenderer.color =
            if (enabled) {
                Color(
                    0.65f,
                    0.58f,
                    0.75f,
                    1f
                )
            } else {
                Color.DARK_GRAY
            }

        shapeRenderer.rect(
            bounds.x,
            bounds.y,
            bounds.width,
            bounds.height
        )

        shapeRenderer.end()
    }

    private fun renderItemDetailBackground(
        accentColor: Color
    ) {

        val bounds =
            layout.itemDetailBounds

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Filled
        )

        shapeRenderer.color =
            Color(
                0.025f,
                0.018f,
                0.038f,
                0.98f
            )

        shapeRenderer.rect(
            bounds.x,
            bounds.y,
            bounds.width,
            bounds.height
        )

        shapeRenderer.end()

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Line
        )

        shapeRenderer.color =
            accentColor

        shapeRenderer.rect(
            bounds.x,
            bounds.y,
            bounds.width,
            bounds.height
        )

        shapeRenderer.end()
    }

    private fun renderHeader(
        currentChestNumber: Int,
        totalChestCount: Int
    ) {

        font.data.setScale(
            1.18f
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            "CHEST OPENING",
            0f,
            layout.panelBounds.y +
                layout.panelBounds.height -
                35f,
            cameraWidth,
            Align.center,
            false
        )

        font.data.setScale(
            0.92f
        )

        font.color =
            Color.GOLD

        font.draw(
            batch,
            "$currentChestNumber / $totalChestCount",
            0f,
            layout.panelBounds.y +
                layout.panelBounds.height -
                70f,
            cameraWidth,
            Align.center,
            false
        )
    }

    private fun renderCenteredInstruction(
        title: String,
        instruction: String,
        color: Color = Color.WHITE
    ) {

        font.data.setScale(
            1.05f
        )

        font.color =
            color

        font.draw(
            batch,
            title,
            layout.chestBounds.x -
                80f,
            layout.chestBounds.y +
                layout.chestBounds.height +
                55f,
            layout.chestBounds.width +
                160f,
            Align.center,
            false
        )

        if (
            instruction.isNotBlank()
        ) {
            font.data.setScale(
                0.78f
            )

            font.color =
                Color.LIGHT_GRAY

            font.draw(
                batch,
                instruction,
                layout.panelBounds.x +
                    60f,
                layout.panelBounds.y +
                    58f,
                layout.panelBounds.width -
                    120f,
                Align.center,
                false
            )
        }
    }

    private fun renderRewardTexts(
        reward: ChestRewardRoll?,
        remainingDiscardCount: Int
    ) {

        if (
            reward == null
        ) {
            return
        }

        val accent =
            accentColor(
                reward
            )

        font.data.setScale(
            1.05f
        )

        font.color =
            accent

        font.draw(
            batch,
            reward.title,
            layout.rewardVisualBounds.x -
                20f,
            layout.rewardVisualBounds.y +
                layout.rewardVisualBounds.height +
                42f,
            layout.rewardVisualBounds.width +
                40f,
            Align.center,
            true
        )

        font.data.setScale(
            0.76f
        )

        font.color =
            Color.LIGHT_GRAY

        font.draw(
            batch,
            reward.description,
            layout.rewardVisualBounds.x -
                10f,
            layout.rewardVisualBounds.y -
                18f,
            layout.rewardVisualBounds.width +
                20f,
            Align.center,
            true
        )

        renderCharacterTexts()

        font.data.setScale(
            0.86f
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            "TAKE",
            layout.takeButtonBounds.x,
            layout.takeButtonBounds.y +
                40f,
            layout.takeButtonBounds.width,
            Align.center,
            false
        )

        font.color =
            if (
                remainingDiscardCount >
                0
            ) {
                Color.WHITE
            } else {
                Color.DARK_GRAY
            }

        font.draw(
            batch,
            "DISCARD",
            layout.discardButtonBounds.x,
            layout.discardButtonBounds.y +
                40f,
            layout.discardButtonBounds.width,
            Align.center,
            false
        )

        font.data.setScale(
            0.70f
        )

        font.color =
            Color.LIGHT_GRAY

        font.draw(
            batch,
            "Discards left: $remainingDiscardCount",
            layout.discardButtonBounds.x,
            layout.discardButtonBounds.y -
                16f,
            layout.discardButtonBounds.width,
            Align.center,
            false
        )

        if (
            reward.rewardType ==
            ChestRewardType.ITEM
        ) {
            font.color =
                accent

            font.draw(
                batch,
                "Click the item to inspect it",
                layout.rewardVisualBounds.x -
                    10f,
                layout.rewardVisualBounds.y +
                    18f,
                layout.rewardVisualBounds.width +
                    20f,
                Align.center,
                false
            )
        }
    }

    private fun renderCharacterTexts() {

        val portrait =
            layout.characterPortraitBounds

        val stats =
            layout.characterStatsBounds

        font.data.setScale(
            0.82f
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            characterNameProvider(),
            portrait.x,
            portrait.y +
                portrait.height +
                25f,
            portrait.width,
            Align.center,
            true
        )

        font.data.setScale(
            0.70f
        )

        font.color =
            Color.LIGHT_GRAY

        font.draw(
            batch,
            "PORTRAIT",
            portrait.x,
            portrait.y +
                portrait.height /
                2f +
                6f,
            portrait.width,
            Align.center,
            false
        )

        font.data.setScale(
            0.78f
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            "CURRENT STATS",
            stats.x,
            stats.y +
                stats.height -
                22f,
            stats.width,
            Align.center,
            false
        )

        var rowY =
            stats.y +
                stats.height -
                45f

        characterStatsProvider()
            .forEach { row ->

                font.data.setScale(
                    0.48f
                )

                font.color =
                    Color.LIGHT_GRAY

                font.draw(
                    batch,
                    row.label,
                    stats.x +
                        10f,
                    rowY
                )

                font.color =
                    Color.WHITE

                font.draw(
                    batch,
                    row.value,
                    stats.x +
                        78f,
                    rowY,
                    stats.width -
                        88f,
                    Align.right,
                    false
                )

                rowY -=
                    18f
            }
    }

    private fun renderItemDetailTexts(

        reward: ChestRewardRoll
    ) {

        val item =
            reward.itemDefinition
                ?: return

        val bounds =
            layout.itemDetailBounds

        font.data.setScale(
            1f
        )

        font.color =
            accentColor(
                reward
            )

        font.draw(
            batch,
            item.displayName,
            bounds.x +
                28f,
            bounds.y +
                bounds.height -
                38f,
            bounds.width -
                56f,
            Align.center,
            true
        )

        font.data.setScale(
            0.76f
        )

        font.color =
            Color.LIGHT_GRAY

        font.draw(
            batch,
            item.description,
            bounds.x +
                35f,
            bounds.y +
                bounds.height -
                92f,
            bounds.width -
                70f,
            Align.left,
            true
        )

        font.color =
            Color.GRAY

        font.draw(
            batch,
            "Click outside the detail card to close",
            bounds.x +
                35f,
            bounds.y +
                35f,
            bounds.width -
                70f,
            Align.center,
            false
        )
    }

    private fun rewardTypeLabel(
        reward: ChestRewardRoll?
    ): String {

        if (
            reward == null
        ) {
            return "UNLOCKED"
        }

        return when (
            reward.rewardType
        ) {
            ChestRewardType.STAT -> {
                "STAT REWARD"
            }

            ChestRewardType.ITEM -> {
                val rarity =
                    reward.itemDefinition
                        ?.rarity
                        ?.displayName
                        ?: "Item"

                "$rarity ITEM"
            }
        }
    }

    fun accentColor(
        reward: ChestRewardRoll?
    ): Color {

        /*
         * Reward henüz belirlenmediyse nötr gri.
         * Stat reward da nötr gri kalır.
         */
        if (
            reward == null ||
            reward.rewardType ==
            ChestRewardType.STAT
        ) {
            return Color(
                0.72f,
                0.72f,
                0.76f,
                1f
            )
        }

        return when (
            reward.itemDefinition
                ?.rarity
                ?.name
                ?.uppercase()
        ) {
            "COMMON",
            "UNCOMMON" -> {
                Color(
                    0.25f,
                    0.90f,
                    0.35f,
                    1f
                )
            }

            "RARE" -> {
                Color(
                    0.20f,
                    0.55f,
                    1f,
                    1f
                )
            }

            "LEGENDARY" -> {
                Color(
                    1f,
                    0.82f,
                    0.15f,
                    1f
                )
            }

            "ARTIFACT" -> {
                Color(
                    0.95f,
                    0.18f,
                    0.18f,
                    1f
                )
            }

            else -> {
                Color(
                    0.72f,
                    0.72f,
                    0.76f,
                    1f
                )
            }
        }
    }

    private fun updateLayout() {

        val centerX =
            camera.position.x

        val centerY =
            camera.position.y

        layout.panelBounds.set(
            centerX -
                510f,
            centerY -
                300f,
            1020f,
            600f
        )

        layout.chestBounds.set(
            centerX -
                145f,
            centerY -
                105f,
            290f,
            210f
        )

        layout.rewardVisualBounds.set(
            centerX -
                385f,
            centerY -
                95f,
            300f,
            300f
        )

        layout.characterPortraitBounds.set(
            centerX +
                35f,
            centerY +
                35f,
            170f,
            170f
        )

        layout.characterStatsBounds.set(
            centerX +
                230f,
            centerY -
                125f,
            245f,
            400f
        )

        layout.takeButtonBounds.set(
            centerX -
                250f,
            centerY -
                235f,
            220f,
            66f
        )

        layout.discardButtonBounds.set(
            centerX +
                30f,
            centerY -
                235f,
            220f,
            66f
        )

        layout.itemDetailBounds.set(
            centerX -
                340f,
            centerY -
                225f,
            680f,
            450f
        )
    }
}
