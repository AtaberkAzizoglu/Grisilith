package com.azizoglu.grisilith.screen.render

import com.azizoglu.grisilith.summary.RunSummarySnapshot
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.Align
import java.util.Locale

class RunSummaryRenderer(
    private val camera: OrthographicCamera,
    private val cameraWidth: Float,
    private val cameraHeight: Float,
    private val shapeRenderer: ShapeRenderer,
    private val batch: SpriteBatch,
    private val font: BitmapFont
) {

    data class Layout(
        val outerPanel: Rectangle,
        val leftColumn: Rectangle,
        val centerColumn: Rectangle,
        val rightColumn: Rectangle,
        val resultArtwork: Rectangle,
        val portrait: Rectangle,
        val gradeCard: Rectangle,
        val restartButton: Rectangle,
        val mainMenuButton: Rectangle
    )

    private val layout =
        Layout(
            Rectangle(),
            Rectangle(),
            Rectangle(),
            Rectangle(),
            Rectangle(),
            Rectangle(),
            Rectangle(),
            Rectangle(),
            Rectangle()
        )

    fun currentLayout(): Layout {
        updateLayout()
        return layout
    }

    fun renderShapes(
        snapshot: RunSummarySnapshot
    ) {
        updateLayout()

        renderFog()
        renderOuterPanel(
            snapshot.wasSuccessful
        )
        renderColumns()
        renderResultArtwork(
            snapshot.wasSuccessful
        )
        renderPortraitPlaceholder()
        renderGradeCard(
            snapshot.wasSuccessful
        )
        renderBuildCards(
            snapshot
        )
        renderButtons()
    }

    fun renderTexts(
        snapshot: RunSummarySnapshot
    ) {
        updateLayout()

        val oldScaleX =
            font.data.scaleX
        val oldScaleY =
            font.data.scaleY
        val oldColor =
            Color(font.color)

        renderResultHeader(
            snapshot
        )
        renderLeftColumn(
            snapshot
        )
        renderCenterColumn(
            snapshot
        )
        renderRightColumn(
            snapshot
        )
        renderButtonTexts()

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

        shapeRenderer.color =
            Color(
                0.08f,
                0.025f,
                0.12f,
                0.80f
            )

        shapeRenderer.rect(
            camera.position.x -
                cameraWidth / 2f,
            camera.position.y -
                cameraHeight / 2f,
            cameraWidth,
            cameraHeight
        )

        shapeRenderer.end()
    }

    private fun renderOuterPanel(
        successful: Boolean
    ) {
        val accent =
            resultAccent(
                successful
            )

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Filled
        )

        shapeRenderer.color =
            Color(
                0.025f,
                0.020f,
                0.040f,
                0.94f
            )

        shapeRenderer.rect(
            layout.outerPanel.x,
            layout.outerPanel.y,
            layout.outerPanel.width,
            layout.outerPanel.height
        )

        shapeRenderer.color =
            Color(
                accent.r,
                accent.g,
                accent.b,
                0.07f
            )

        shapeRenderer.rect(
            layout.outerPanel.x + 8f,
            layout.outerPanel.y + 8f,
            layout.outerPanel.width - 16f,
            layout.outerPanel.height - 16f
        )

        shapeRenderer.end()

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Line
        )

        repeat(3) { index ->
            val inset =
                index * 4f

            shapeRenderer.color =
                Color(
                    accent.r,
                    accent.g,
                    accent.b,
                    0.90f -
                        index * 0.22f
                )

            shapeRenderer.rect(
                layout.outerPanel.x + inset,
                layout.outerPanel.y + inset,
                layout.outerPanel.width - inset * 2f,
                layout.outerPanel.height - inset * 2f
            )
        }

        shapeRenderer.end()
    }

    private fun renderColumns() {
        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Filled
        )

        listOf(
            layout.leftColumn,
            layout.centerColumn,
            layout.rightColumn
        ).forEach { bounds ->
            shapeRenderer.color =
                Color(
                    0.045f,
                    0.038f,
                    0.060f,
                    0.94f
                )

            shapeRenderer.rect(
                bounds.x,
                bounds.y,
                bounds.width,
                bounds.height
            )
        }

        shapeRenderer.end()

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Line
        )

        shapeRenderer.color =
            Color(
                0.38f,
                0.30f,
                0.48f,
                1f
            )

        listOf(
            layout.leftColumn,
            layout.centerColumn,
            layout.rightColumn
        ).forEach { bounds ->
            shapeRenderer.rect(
                bounds.x,
                bounds.y,
                bounds.width,
                bounds.height
            )
        }

        shapeRenderer.end()
    }

    private fun renderResultArtwork(
        successful: Boolean
    ) {
        val accent =
            resultAccent(
                successful
            )

        val bounds =
            layout.resultArtwork

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Filled
        )

        shapeRenderer.color =
            Color(
                accent.r,
                accent.g,
                accent.b,
                0.14f
            )

        shapeRenderer.rect(
            bounds.x,
            bounds.y,
            bounds.width,
            bounds.height
        )

        shapeRenderer.color =
            accent

        if (successful) {
            /*
             * Geçici çapraz kılıç placeholder'ı.
             */
            shapeRenderer.rectLine(
                bounds.x + 40f,
                bounds.y + 32f,
                bounds.x + bounds.width - 40f,
                bounds.y + bounds.height - 32f,
                10f
            )

            shapeRenderer.rectLine(
                bounds.x + bounds.width - 40f,
                bounds.y + 32f,
                bounds.x + 40f,
                bounds.y + bounds.height - 32f,
                10f
            )
        } else {
            /*
             * Geçici hasarlı miğfer placeholder'ı.
             */
            shapeRenderer.rect(
                bounds.x + 58f,
                bounds.y + 42f,
                bounds.width - 116f,
                bounds.height - 76f
            )

            shapeRenderer.color =
                Color(
                    0.10f,
                    0.06f,
                    0.08f,
                    1f
                )

            shapeRenderer.rectLine(
                bounds.x + bounds.width / 2f,
                bounds.y + 30f,
                bounds.x + bounds.width / 2f - 28f,
                bounds.y + bounds.height - 22f,
                8f
            )
        }

        shapeRenderer.end()

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Line
        )

        shapeRenderer.color =
            accent

        shapeRenderer.rect(
            bounds.x,
            bounds.y,
            bounds.width,
            bounds.height
        )

        shapeRenderer.end()
    }

    private fun renderPortraitPlaceholder() {
        val bounds =
            layout.portrait

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Filled
        )

        shapeRenderer.color =
            Color(
                0.09f,
                0.075f,
                0.12f,
                1f
            )

        shapeRenderer.rect(
            bounds.x,
            bounds.y,
            bounds.width,
            bounds.height
        )

        shapeRenderer.color =
            Color(
                0.25f,
                0.21f,
                0.31f,
                1f
            )

        shapeRenderer.rect(
            bounds.x + 14f,
            bounds.y + 14f,
            bounds.width - 28f,
            bounds.height - 28f
        )

        shapeRenderer.end()

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Line
        )

        shapeRenderer.color =
            Color(
                0.55f,
                0.43f,
                0.68f,
                1f
            )

        shapeRenderer.rect(
            bounds.x,
            bounds.y,
            bounds.width,
            bounds.height
        )

        shapeRenderer.end()
    }

    private fun renderGradeCard(
        successful: Boolean
    ) {
        val bounds =
            layout.gradeCard

        val accent =
            resultAccent(
                successful
            )

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Filled
        )

        shapeRenderer.color =
            Color(
                accent.r,
                accent.g,
                accent.b,
                0.15f
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
            accent

        shapeRenderer.rect(
            bounds.x,
            bounds.y,
            bounds.width,
            bounds.height
        )

        shapeRenderer.end()
    }

    private fun renderBuildCards(
        snapshot: RunSummarySnapshot
    ) {
        /*
         * Sağ kolon kart arka planları.
         * Metinler ayrı SpriteBatch geçişinde çizilir.
         */
        var y =
            layout.rightColumn.y +
                layout.rightColumn.height -
                76f

        snapshot.weapons
            .take(3)
            .forEach {
                renderSmallCard(
                    x =
                        layout.rightColumn.x + 14f,
                    y =
                        y - 56f,
                    width =
                        layout.rightColumn.width - 28f,
                    height =
                        52f
                )

                y -= 60f
            }

        y -= 12f

        renderSmallCard(
            x =
                layout.rightColumn.x + 14f,
            y =
                y - 50f,
            width =
                layout.rightColumn.width - 28f,
            height =
                46f
        )

        y -= 72f

        snapshot.tamgas
            .take(2)
            .forEach {
                renderSmallCard(
                    x =
                        layout.rightColumn.x + 14f,
                    y =
                        y - 44f,
                    width =
                        layout.rightColumn.width - 28f,
                    height =
                        40f
                )

                y -= 46f
            }

        y -= 10f

        snapshot.items
            .take(4)
            .forEach {
                renderSmallCard(
                    x =
                        layout.rightColumn.x + 14f,
                    y =
                        y - 38f,
                    width =
                        layout.rightColumn.width - 28f,
                    height =
                        34f
                )

                y -= 40f
            }
    }

    private fun renderSmallCard(
        x: Float,
        y: Float,
        width: Float,
        height: Float
    ) {
        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Filled
        )

        shapeRenderer.color =
            Color(
                0.075f,
                0.062f,
                0.095f,
                1f
            )

        shapeRenderer.rect(
            x,
            y,
            width,
            height
        )

        shapeRenderer.end()

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Line
        )

        shapeRenderer.color =
            Color(
                0.34f,
                0.28f,
                0.43f,
                1f
            )

        shapeRenderer.rect(
            x,
            y,
            width,
            height
        )

        shapeRenderer.end()
    }

    private fun renderButtons() {
        renderButton(
            layout.restartButton,
            emphasized =
                true
        )

        renderButton(
            layout.mainMenuButton,
            emphasized =
                false
        )
    }

    private fun renderButton(
        bounds: Rectangle,
        emphasized: Boolean
    ) {
        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Filled
        )

        shapeRenderer.color =
            if (emphasized) {
                Color(
                    0.18f,
                    0.34f,
                    0.24f,
                    1f
                )
            } else {
                Color(
                    0.16f,
                    0.12f,
                    0.22f,
                    1f
                )
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
            Color(
                0.68f,
                0.58f,
                0.78f,
                1f
            )

        shapeRenderer.rect(
            bounds.x,
            bounds.y,
            bounds.width,
            bounds.height
        )

        shapeRenderer.end()
    }

    private fun renderResultHeader(
        snapshot: RunSummarySnapshot
    ) {
        val accent =
            resultAccent(
                snapshot.wasSuccessful
            )

        font.data.setScale(
            1.52f
        )

        font.color =
            accent

        font.draw(
            batch,
            if (snapshot.wasSuccessful) {
                "VICTORY"
            } else {
                "DEFEAT"
            },
            layout.centerColumn.x,
            layout.outerPanel.y +
                layout.outerPanel.height -
                22f,
            layout.centerColumn.width,
            Align.center,
            false
        )
    }

    private fun renderLeftColumn(
        snapshot: RunSummarySnapshot
    ) {
        font.data.setScale(
            0.86f
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            "RUN STATISTICS",
            layout.leftColumn.x,
            layout.leftColumn.y +
                layout.leftColumn.height -
                26f,
            layout.leftColumn.width,
            Align.center,
            false
        )

        val rows =
            listOf(
                "Difficulty" to
                    snapshot.difficultyLabel,

                "Map" to
                    snapshot.mapName,

                "Character" to
                    snapshot.characterName,

                "Stage Reached" to
                    snapshot.stageReached.toString(),

                "Run Time" to
                    snapshot.formattedRunTime,

                "Enemies Killed" to
                    formatInt(
                        snapshot.enemiesKilled
                    ),

                "Damage Dealt" to
                    formatNumber(
                        snapshot.totalDamageDealt
                    ),

                "Damage Taken" to
                    formatNumber(
                        snapshot.totalDamageTaken
                    ),

                "Gold Collected" to
                    formatNumber(
                        snapshot.goldCollected
                    ),

                "Grisilith Coins" to
                    snapshot.grisilithCoinsEarned
                        .toString(),

                "Chests Opened" to
                    snapshot.chestsOpened
                        .toString()
            )

        var y =
            layout.leftColumn.y +
                layout.leftColumn.height -
                62f

        rows.forEach { row ->
            font.data.setScale(
                0.60f
            )

            font.color =
                Color.LIGHT_GRAY

            font.draw(
                batch,
                row.first,
                layout.leftColumn.x + 14f,
                y
            )

            font.color =
                Color.WHITE

            font.draw(
                batch,
                row.second,
                layout.leftColumn.x + 112f,
                y,
                layout.leftColumn.width - 126f,
                Align.right,
                false
            )

            y -= 30f
        }
    }

    private fun renderCenterColumn(
        snapshot: RunSummarySnapshot
    ) {
        font.data.setScale(
            0.68f
        )

        font.color =
            Color.LIGHT_GRAY

        font.draw(
            batch,
            "RESULT ARTWORK",
            layout.resultArtwork.x,
            layout.resultArtwork.y +
                layout.resultArtwork.height / 2f +
                4f,
            layout.resultArtwork.width,
            Align.center,
            false
        )

        font.data.setScale(
            0.76f
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            snapshot.characterName,
            layout.portrait.x - 20f,
            layout.portrait.y +
                layout.portrait.height +
                24f,
            layout.portrait.width + 40f,
            Align.center,
            true
        )

        font.data.setScale(
            0.64f
        )

        font.color =
            Color.LIGHT_GRAY

        font.draw(
            batch,
            "PORTRAIT",
            layout.portrait.x,
            layout.portrait.y +
                layout.portrait.height / 2f +
                4f,
            layout.portrait.width,
            Align.center,
            false
        )

        font.data.setScale(
            0.72f
        )

        font.color =
            Color.LIGHT_GRAY

        font.draw(
            batch,
            "RUN GRADE",
            layout.gradeCard.x,
            layout.gradeCard.y +
                layout.gradeCard.height -
                20f,
            layout.gradeCard.width,
            Align.center,
            false
        )

        font.data.setScale(
            2.10f
        )

        font.color =
            Color.GOLD

        font.draw(
            batch,
            snapshot.gradeResult
                .grade
                .displayName,
            layout.gradeCard.x,
            layout.gradeCard.y +
                74f,
            layout.gradeCard.width,
            Align.center,
            false
        )

        font.data.setScale(
            0.65f
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            "Score: " +
                formatInt(
                    snapshot.gradeResult
                        .totalScore
                ),
            layout.gradeCard.x,
            layout.gradeCard.y +
                24f,
            layout.gradeCard.width,
            Align.center,
            false
        )
    }

    private fun renderRightColumn(
        snapshot: RunSummarySnapshot
    ) {
        font.data.setScale(
            0.86f
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            "BUILD",
            layout.rightColumn.x,
            layout.rightColumn.y +
                layout.rightColumn.height -
                26f,
            layout.rightColumn.width,
            Align.center,
            false
        )

        var y =
            layout.rightColumn.y +
                layout.rightColumn.height -
                62f

        font.data.setScale(
            0.62f
        )

        font.color =
            Color.GOLD

        font.draw(
            batch,
            "WEAPONS",
            layout.rightColumn.x + 14f,
            y
        )

        y -= 22f

        if (snapshot.weapons.isEmpty()) {
            drawBuildEmpty(
                "No weapons",
                y
            )

            y -= 42f
        } else {
            snapshot.weapons
                .take(3)
                .forEach { weapon ->
                    font.data.setScale(
                        0.58f
                    )

                    font.color =
                        Color.WHITE

                    font.draw(
                        batch,
                        "${weapon.displayName}  Lv.${weapon.level}",
                        layout.rightColumn.x + 24f,
                        y
                    )

                    font.data.setScale(
                        0.50f
                    )

                    font.color =
                        Color.LIGHT_GRAY

                    font.draw(
                        batch,
                        "Damage: " +
                            formatNumber(
                                weapon.damageDealt
                            ),
                        layout.rightColumn.x + 24f,
                        y - 19f
                    )

                    y -= 60f
                }
        }

        font.data.setScale(
            0.62f
        )

        font.color =
            Color.GOLD

        font.draw(
            batch,
            "ARMOR",
            layout.rightColumn.x + 14f,
            y
        )

        y -= 22f

        font.data.setScale(
            0.57f
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            snapshot.armor.displayName,
            layout.rightColumn.x + 24f,
            y
        )

        y -= 48f

        font.data.setScale(
            0.62f
        )

        font.color =
            Color.GOLD

        font.draw(
            batch,
            "TAMGAS",
            layout.rightColumn.x + 14f,
            y
        )

        y -= 20f

        snapshot.tamgas
            .take(2)
            .forEach { tamga ->
                font.data.setScale(
                    0.54f
                )

                font.color =
                    Color.WHITE

                font.draw(
                    batch,
                    "${tamga.slotLabel}: ${tamga.displayName}",
                    layout.rightColumn.x + 24f,
                    y
                )

                y -= 46f
            }

        font.data.setScale(
            0.62f
        )

        font.color =
            Color.GOLD

        font.draw(
            batch,
            "ITEMS",
            layout.rightColumn.x + 14f,
            y
        )

        y -= 19f

        if (snapshot.items.isEmpty()) {
            drawBuildEmpty(
                "No items",
                y
            )
        } else {
            snapshot.items
                .take(4)
                .forEach { item ->
                    font.data.setScale(
                        0.52f
                    )

                    font.color =
                        Color.WHITE

                    font.draw(
                        batch,
                        "${item.displayName}  x${item.count}",
                        layout.rightColumn.x + 24f,
                        y
                    )

                    y -= 40f
                }
        }

        val hiddenItemCount =
            (
                snapshot.items.size -
                    4
                )
                .coerceAtLeast(0)

        if (hiddenItemCount > 0) {
            font.data.setScale(
                0.48f
            )

            font.color =
                Color.GRAY

            font.draw(
                batch,
                "+$hiddenItemCount more item groups",
                layout.rightColumn.x + 24f,
                y
            )
        }
    }

    private fun drawBuildEmpty(
        text: String,
        y: Float
    ) {
        font.data.setScale(
            0.50f
        )

        font.color =
            Color.GRAY

        font.draw(
            batch,
            text,
            layout.rightColumn.x + 24f,
            y
        )
    }

    private fun renderButtonTexts() {
        font.data.setScale(
            0.78f
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            "RESTART  [R]",
            layout.restartButton.x,
            layout.restartButton.y + 35f,
            layout.restartButton.width,
            Align.center,
            false
        )

        font.draw(
            batch,
            "MAIN MENU  [ENTER]",
            layout.mainMenuButton.x,
            layout.mainMenuButton.y + 35f,
            layout.mainMenuButton.width,
            Align.center,
            false
        )
    }

    private fun resultAccent(
        successful: Boolean
    ): Color {
        return if (successful) {
            Color(
                0.96f,
                0.78f,
                0.20f,
                1f
            )
        } else {
            Color(
                0.78f,
                0.18f,
                0.22f,
                1f
            )
        }
    }

    private fun formatInt(
        value: Int
    ): String {
        return String.format(
            Locale.US,
            "%,d",
            value
        )
    }

    private fun formatNumber(
        value: Number
    ): String {
        return String.format(
            Locale.US,
            "%,.0f",
            value.toDouble()
        )
    }

    private fun updateLayout() {
        val centerX =
            camera.position.x

        val centerY =
            camera.position.y

        layout.outerPanel.set(
            centerX - 610f,
            centerY - 335f,
            1220f,
            670f
        )

        layout.leftColumn.set(
            centerX - 590f,
            centerY - 245f,
            300f,
            510f
        )

        layout.centerColumn.set(
            centerX - 270f,
            centerY - 245f,
            540f,
            510f
        )

        layout.rightColumn.set(
            centerX + 290f,
            centerY - 245f,
            300f,
            510f
        )

        layout.resultArtwork.set(
            centerX - 210f,
            centerY + 80f,
            420f,
            145f
        )

        layout.portrait.set(
            centerX - 165f,
            centerY - 105f,
            170f,
            170f
        )

        layout.gradeCard.set(
            centerX + 30f,
            centerY - 105f,
            205f,
            170f
        )

        layout.restartButton.set(
            centerX - 230f,
            centerY - 315f,
            210f,
            54f
        )

        layout.mainMenuButton.set(
            centerX + 20f,
            centerY - 315f,
            210f,
            54f
        )
    }
}
