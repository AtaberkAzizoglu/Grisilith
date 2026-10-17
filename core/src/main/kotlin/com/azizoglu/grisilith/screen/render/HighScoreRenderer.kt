package com.azizoglu.grisilith.screen.render

import com.azizoglu.grisilith.statistics.career.CareerAchievement
import com.azizoglu.grisilith.statistics.career.CareerStatistics
import com.azizoglu.grisilith.statistics.career.HighScoreRepository
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.Align
import kotlin.math.abs
import kotlin.math.roundToInt

class HighScoreRenderer {

    companion object {
        private const val VIEWPORT_WIDTH = 1280f
        private const val TITLE_Y = 665f
        private const val LEFT_X = 230f
        private const val LEFT_VALUE_X = 490f
        private const val RIGHT_X = 650f
        private const val RIGHT_VALUE_X = 910f
        private const val COLUMN_WIDTH = 380f
        private const val VALUE_WIDTH = 105f
        private const val TOP_ROW_Y = 575f
        private const val SECOND_ROW_Y = 280f
        private const val ACHIEVEMENT_SECTION_Y = -15f
        private const val SECTION_TITLE_OFFSET = 35f
        private const val FIRST_ROW_OFFSET = 72f
        private const val ROW_SPACING = 39f
        private const val ACHIEVEMENT_X = 230f
        private const val ACHIEVEMENT_BAR_WIDTH = 680f
        private const val ACHIEVEMENT_BAR_HEIGHT = 34f
        private const val ACHIEVEMENT_SPACING = 112f
        private const val ACHIEVEMENT_BUTTON_X = 930f
        private const val ACHIEVEMENT_BUTTON_SIZE = 50f
    }

    private val progressTrack =
        Texture(Gdx.files.internal("ui/components/progress/chronicle_progress_track.png")).also {
            it.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        }

    private val progressFill =
        Texture(Gdx.files.internal("ui/components/progress/chronicle_progress_fill.png")).also {
            it.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        }

    private val progressTrackRegion =
        croppedRegion(progressTrack, 0.027f, 0.40f, 0.973f, 0.60f)

    private val progressFillRegion =
        croppedRegion(progressFill, 0.04f, 0.41f, 0.96f, 0.59f)

    fun renderShapes(
        shapeRenderer: ShapeRenderer,
        scrollOffset: Float
    ) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        val topY = TOP_ROW_Y + scrollOffset
        val secondY = SECOND_ROW_Y + scrollOffset
        val achievementY = ACHIEVEMENT_SECTION_Y + scrollOffset

        drawSectionLine(shapeRenderer, LEFT_X, topY, COLUMN_WIDTH)
        drawSectionLine(shapeRenderer, RIGHT_X, topY, COLUMN_WIDTH)
        drawSectionLine(shapeRenderer, LEFT_X, secondY, COLUMN_WIDTH)
        drawSectionLine(shapeRenderer, RIGHT_X, secondY, COLUMN_WIDTH)
        drawSectionLine(shapeRenderer, ACHIEVEMENT_X, achievementY, COLUMN_WIDTH * 2f + 40f)
        shapeRenderer.end()
    }

    fun renderTitle(
        batch: SpriteBatch,
        titleFont: BitmapFont
    ) {
        val oldScaleX = titleFont.data.scaleX
        val oldScaleY = titleFont.data.scaleY
        val oldColor = Color(titleFont.color)
        titleFont.data.setScale(1.32f)
        titleFont.color = Color(0.94f, 0.90f, 0.82f, 1f)
        com.azizoglu.grisilith.ui.ScreenTitleArt.draw(batch,"CHRONICLE",Rectangle(360f,626f,560f,52f))
        titleFont.data.setScale(oldScaleX, oldScaleY)
        titleFont.color = oldColor
    }

    fun renderTexts(
        batch: SpriteBatch,
        font: BitmapFont,
        statistics: CareerStatistics,
        scrollOffset: Float
    ) {
        val oldScaleX = font.data.scaleX
        val oldScaleY = font.data.scaleY
        val oldColor = Color(font.color)
        val topY = TOP_ROW_Y + scrollOffset
        val secondY = SECOND_ROW_Y + scrollOffset
        val achievementSectionY = ACHIEVEMENT_SECTION_Y + scrollOffset

        renderSectionTitle(batch, font, "STATISTICS", LEFT_X, topY)
        renderRows(
            batch,
            font,
            LEFT_X,
            LEFT_VALUE_X,
            topY,
            listOf(
                "Enemies Killed" to formatLong(statistics.enemiesKilled),
                "Bosses Defeated" to formatLong(statistics.bossesDefeated),
                "Final Bosses Defeated" to formatLong(statistics.finalBossesDefeated),
                "Damage Dealt" to formatLong(statistics.damageDealt),
                "Damage Taken" to formatLong(statistics.damageTaken)
            )
        )

        renderSectionTitle(batch, font, "ECONOMY", RIGHT_X, topY)
        renderRows(
            batch,
            font,
            RIGHT_X,
            RIGHT_VALUE_X,
            topY,
            listOf(
                "Gold Collected" to formatGold(statistics.goldCollected),
                "Gold Spent" to formatGold(statistics.goldSpent),
                "Grisilith Coins Earned" to formatLong(statistics.grisilithCoinsEarned)
            )
        )

        renderSectionTitle(batch, font, "GAMEPLAY", LEFT_X, secondY)
        renderRows(
            batch,
            font,
            LEFT_X,
            LEFT_VALUE_X,
            secondY,
            listOf(
                "Total Play Time" to formatDuration(statistics.totalPlayTimeSeconds),
                "Runs Started" to formatLong(statistics.runsStarted),
                "Runs Finished" to formatCountWithPercentage(statistics.runsFinished, statistics.runsStarted),
                "Runs Won" to formatCountWithPercentage(statistics.runsWon, statistics.runsFinished),
                "Runs Lost" to formatCountWithPercentage(statistics.runsLost, statistics.runsFinished),
                "Total Score" to formatLong(statistics.totalScore)
            )
        )

        renderSectionTitle(batch, font, "RECORDS", RIGHT_X, secondY)
        renderRows(
            batch,
            font,
            RIGHT_X,
            RIGHT_VALUE_X,
            secondY,
            listOf(
                "Longest Run" to formatDuration(statistics.longestRunSeconds),
                "Highest Damage Dealt in One Run" to formatLong(statistics.highestDamage),
                "Most Gold" to formatGold(statistics.mostGold),
                "Most Grisilith Coins" to statistics.mostGrisilithCoins.coerceAtLeast(0).toString(),
                "Most Damage Taken" to formatLong(statistics.mostDamageTaken),
                "Highest Score" to formatLong(statistics.highestScore)
            ),
            record = true
        )

        renderSectionTitle(batch, font, "ACHIEVEMENTS", ACHIEVEMENT_X, achievementSectionY)
        renderAchievement(batch, font, "Slained Total Enemy", CareerAchievement.ENEMIES, 0, scrollOffset)
        renderAchievement(batch, font, "Slained Bosses", CareerAchievement.BOSSES, 1, scrollOffset)
        renderAchievement(batch, font, "Slained Final Bosses", CareerAchievement.FINAL_BOSSES, 2, scrollOffset)

        font.data.setScale(oldScaleX, oldScaleY)
        font.color = oldColor
    }

    fun achievementButtonBounds(index: Int, scrollOffset: Float): Rectangle {
        val rowY = achievementRowY(index, scrollOffset)
        return Rectangle(
            ACHIEVEMENT_BUTTON_X,
            rowY - 58f,
            ACHIEVEMENT_BUTTON_SIZE,
            ACHIEVEMENT_BUTTON_SIZE
        )
    }

    fun dispose() {
        progressTrack.dispose()
        progressFill.dispose()
    }

    private fun renderSectionTitle(
        batch: SpriteBatch,
        font: BitmapFont,
        title: String,
        x: Float,
        sectionY: Float
    ) {
        font.data.setScale(1.12f)
        font.color = Color(0.94f, 0.90f, 0.82f, 1f)
        font.draw(batch, title, x, sectionY - SECTION_TITLE_OFFSET)
    }

    private fun renderRows(
        batch: SpriteBatch,
        font: BitmapFont,
        labelX: Float,
        valueX: Float,
        sectionY: Float,
        rows: List<Pair<String, String>>,
        record: Boolean = false
    ) {
        var y = sectionY - FIRST_ROW_OFFSET
        rows.forEach { (label, value) ->
            font.data.setScale(0.84f)
            font.color = Color.LIGHT_GRAY
            font.draw(batch, label, labelX, y)
            font.color = if (record) Color.GOLD else Color.WHITE
            font.draw(batch, value, valueX, y, VALUE_WIDTH, Align.right, false)
            y -= ROW_SPACING
        }
    }

    private fun renderAchievement(
        batch: SpriteBatch,
        font: BitmapFont,
        title: String,
        achievement: CareerAchievement,
        index: Int,
        scrollOffset: Float
    ) {
        val y = achievementRowY(index, scrollOffset)
        val barY = y - 49f
        val progress = HighScoreRepository.achievementProgress(achievement)
        val currentValue = HighScoreRepository.achievementValue(achievement)
        val targetValue = HighScoreRepository.achievementTarget(achievement)

        font.data.setScale(0.88f)
        font.color = Color.WHITE
        font.draw(batch, title, ACHIEVEMENT_X, y)

        batch.color = Color.WHITE
        batch.draw(progressTrackRegion, ACHIEVEMENT_X, barY, ACHIEVEMENT_BAR_WIDTH, ACHIEVEMENT_BAR_HEIGHT)
        if (progress > 0f) {
            batch.draw(
                progressFillRegion,
                ACHIEVEMENT_X + 17f,
                barY + 12f,
                (ACHIEVEMENT_BAR_WIDTH - 34f) * progress,
                ACHIEVEMENT_BAR_HEIGHT - 24f
            )
        }
        batch.color = Color.WHITE

        font.data.setScale(0.74f)
        font.color = Color.LIGHT_GRAY
        font.draw(
            batch,
            "${formatLong(currentValue)} / ${formatLong(targetValue)}",
            ACHIEVEMENT_X,
            barY - 8f,
            ACHIEVEMENT_BAR_WIDTH,
            Align.right,
            false
        )

        if (currentValue >= targetValue) {
            font.color = Color.GOLD
            font.draw(batch, "COMPLETED", ACHIEVEMENT_X, barY - 8f)
        }
    }

    private fun achievementRowY(index: Int, scrollOffset: Float): Float =
        ACHIEVEMENT_SECTION_Y + scrollOffset - FIRST_ROW_OFFSET - ACHIEVEMENT_SPACING * index

    private fun drawSectionLine(
        shapeRenderer: ShapeRenderer,
        x: Float,
        y: Float,
        width: Float
    ) {
        shapeRenderer.color = Color(0.62f, 0.43f, 0.23f, 0.88f)
        shapeRenderer.rect(x, y, width, 3f)
    }

    private fun croppedRegion(
        texture: Texture,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float
    ): TextureRegion {
        val x = (texture.width * left).roundToInt()
        val y = (texture.height * top).roundToInt()
        val width = (texture.width * (right - left)).roundToInt().coerceAtLeast(1)
        val height = (texture.height * (bottom - top)).roundToInt().coerceAtLeast(1)
        return TextureRegion(texture, x, y, width, height)
    }

    private fun formatLong(value: Long): String =
        String.format("%,d", value.coerceAtLeast(0L))

    private fun formatGold(value: Float): String {
        val safeValue = value.coerceAtLeast(0f)
        val roundedValue = safeValue.roundToInt()
        return if (abs(safeValue - roundedValue.toFloat()) < 0.001f) {
            String.format("%,d", roundedValue)
        } else {
            String.format("%,.1f", safeValue)
        }
    }

    private fun formatCountWithPercentage(value: Long, total: Long): String {
        val safeValue = value.coerceAtLeast(0L)
        val safeTotal = total.coerceAtLeast(0L)
        val percentage = if (safeTotal <= 0L) 0 else
            (safeValue.toDouble() / safeTotal.toDouble() * 100.0).roundToInt()
        return "${formatLong(safeValue)} ($percentage%)"
    }

    private fun formatDuration(totalSeconds: Long): String {
        val safeSeconds = totalSeconds.coerceAtLeast(0L)
        val hours = safeSeconds / 3600L
        val minutes = (safeSeconds % 3600L) / 60L
        val seconds = safeSeconds % 60L
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}
