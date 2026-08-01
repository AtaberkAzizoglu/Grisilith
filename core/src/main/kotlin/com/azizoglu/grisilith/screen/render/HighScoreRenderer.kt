package com.azizoglu.grisilith.screen.render

import com.azizoglu.grisilith.statistics.career.CareerStatistics
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.Align
import kotlin.math.abs
import kotlin.math.roundToInt

class HighScoreRenderer {

    companion object {

        private const val VIEWPORT_WIDTH =
            1280f

        private const val TITLE_Y =
            665f

        private const val CONTENT_START_Y =
            575f

        private const val CONTENT_LEFT_X =
            250f

        private const val CONTENT_RIGHT_X =
            1030f

        private const val LABEL_X =
            300f

        private const val VALUE_X =
            760f

        private const val VALUE_WIDTH =
            220f

        private const val SECTION_LINE_HEIGHT =
            6f

        private const val SECTION_TITLE_OFFSET =
            35f

        private const val FIRST_ROW_OFFSET =
            72f

        private const val ROW_SPACING =
            39f

        private const val SECTION_BOTTOM_SPACING =
            52f

        private const val ACHIEVEMENT_BAR_X =
            300f

        private const val ACHIEVEMENT_BAR_WIDTH =
            680f

        private const val ACHIEVEMENT_BAR_HEIGHT =
            21f

        private const val ACHIEVEMENT_TITLE_TO_BAR =
            35f

        private const val ACHIEVEMENT_VALUE_TO_BAR =
            29f

        private const val ACHIEVEMENT_SPACING =
            112f
    }

    fun renderShapes(
        shapeRenderer: ShapeRenderer,
        statistics: CareerStatistics,
        scrollOffset: Float
    ) {

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Filled
        )

        var sectionY =
            CONTENT_START_Y +
                scrollOffset

        /*
         * STATISTICS
         */
        drawSectionLine(
            shapeRenderer =
                shapeRenderer,

            y =
                sectionY
        )

        sectionY =
            nextSectionY(
                currentSectionY =
                    sectionY,

                rowCount =
                    5
            )

        /*
         * ECONOMY
         */
        drawSectionLine(
            shapeRenderer =
                shapeRenderer,

            y =
                sectionY
        )

        sectionY =
            nextSectionY(
                currentSectionY =
                    sectionY,

                rowCount =
                    3
            )

        /*
         * GAMEPLAY
         */
        drawSectionLine(
            shapeRenderer =
                shapeRenderer,

            y =
                sectionY
        )

        sectionY =
            nextSectionY(
                currentSectionY =
                    sectionY,

                rowCount =
                    5
            )

        /*
         * RECORDS
         */
        drawSectionLine(
            shapeRenderer =
                shapeRenderer,

            y =
                sectionY
        )

        sectionY =
            nextSectionY(
                currentSectionY =
                    sectionY,

                rowCount =
                    5
            )

        /*
         * ACHIEVEMENTS
         */
        drawSectionLine(
            shapeRenderer =
                shapeRenderer,

            y =
                sectionY
        )

        var achievementY =
            sectionY -
                FIRST_ROW_OFFSET

        drawAchievementBar(
            shapeRenderer =
                shapeRenderer,

            y =
                achievementY,

            progress =
                statistics.achievementProgress(
                    statistics.enemiesKilled
                )
        )

        achievementY -=
            ACHIEVEMENT_SPACING

        drawAchievementBar(
            shapeRenderer =
                shapeRenderer,

            y =
                achievementY,

            progress =
                statistics.achievementProgress(
                    statistics.bossesDefeated
                )
        )

        achievementY -=
            ACHIEVEMENT_SPACING

        drawAchievementBar(
            shapeRenderer =
                shapeRenderer,

            y =
                achievementY,

            progress =
                statistics.achievementProgress(
                    statistics.finalBossesDefeated
                )
        )

        shapeRenderer.end()
    }

    fun renderTexts(
        batch: SpriteBatch,
        font: BitmapFont,
        statistics: CareerStatistics,
        scrollOffset: Float
    ) {

        val oldScaleX =
            font.data.scaleX

        val oldScaleY =
            font.data.scaleY

        val oldColor =
            Color(
                font.color
            )

        renderTitle(
            batch =
                batch,

            font =
                font
        )

        var sectionY =
            CONTENT_START_Y +
                scrollOffset

        /*
         * STATISTICS
         */
        renderSectionTitle(
            batch =
                batch,

            font =
                font,

            title =
                "STATISTICS",

            sectionY =
                sectionY
        )

        var rowY =
            sectionY -
                FIRST_ROW_OFFSET

        rowY =
            renderRow(
                batch =
                    batch,

                font =
                    font,

                label =
                    "Enemies Killed",

                value =
                    formatLong(
                        statistics.enemiesKilled
                    ),

                y =
                    rowY
            )

        rowY =
            renderRow(
                batch =
                    batch,

                font =
                    font,

                label =
                    "Bosses Defeated",

                value =
                    formatLong(
                        statistics.bossesDefeated
                    ),

                y =
                    rowY
            )

        rowY =
            renderRow(
                batch =
                    batch,

                font =
                    font,

                label =
                    "Final Bosses Defeated",

                value =
                    formatLong(
                        statistics.finalBossesDefeated
                    ),

                y =
                    rowY
            )

        rowY =
            renderRow(
                batch =
                    batch,

                font =
                    font,

                label =
                    "Damage Dealt",

                value =
                    formatLong(
                        statistics.damageDealt
                    ),

                y =
                    rowY
            )

        renderRow(
            batch =
                batch,

            font =
                font,

            label =
                "Damage Taken",

            value =
                formatLong(
                    statistics.damageTaken
                ),

            y =
                rowY
        )

        sectionY =
            nextSectionY(
                currentSectionY =
                    sectionY,

                rowCount =
                    5
            )

        /*
         * ECONOMY
         */
        renderSectionTitle(
            batch =
                batch,

            font =
                font,

            title =
                "ECONOMY",

            sectionY =
                sectionY
        )

        rowY =
            sectionY -
                FIRST_ROW_OFFSET

        rowY =
            renderRow(
                batch =
                    batch,

                font =
                    font,

                label =
                    "Gold Collected",

                value =
                    formatGold(
                        statistics.goldCollected
                    ),

                y =
                    rowY
            )

        rowY =
            renderRow(
                batch =
                    batch,

                font =
                    font,

                label =
                    "Gold Spent",

                value =
                    formatGold(
                        statistics.goldSpent
                    ),

                y =
                    rowY
            )

        renderRow(
            batch =
                batch,

            font =
                font,

            label =
                "Grisilith Coins Earned",

            value =
                formatLong(
                    statistics.grisilithCoinsEarned
                ),

            y =
                rowY
        )

        sectionY =
            nextSectionY(
                currentSectionY =
                    sectionY,

                rowCount =
                    3
            )

        /*
         * GAMEPLAY
         */
        renderSectionTitle(
            batch =
                batch,

            font =
                font,

            title =
                "GAMEPLAY",

            sectionY =
                sectionY
        )

        rowY =
            sectionY -
                FIRST_ROW_OFFSET

        rowY =
            renderRow(
                batch =
                    batch,

                font =
                    font,

                label =
                    "Total Play Time",

                value =
                    formatDuration(
                        statistics.totalPlayTimeSeconds
                    ),

                y =
                    rowY
            )

        rowY =
            renderRow(
                batch =
                    batch,

                font =
                    font,

                label =
                    "Runs Started",

                value =
                    formatLong(
                        statistics.runsStarted
                    ),

                y =
                    rowY
            )

        rowY =
            renderRow(
                batch =
                    batch,

                font =
                    font,

                label =
                    "Runs Finished",

                value =
                    formatLong(
                        statistics.runsFinished
                    ),

                y =
                    rowY
            )

        rowY =
            renderRow(
                batch =
                    batch,

                font =
                    font,

                label =
                    "Runs Won",

                value =
                    formatLong(
                        statistics.runsWon
                    ),

                y =
                    rowY
            )

        renderRow(
            batch =
                batch,

            font =
                font,

            label =
                "Runs Lost",

            value =
                formatLong(
                    statistics.runsLost
                ),

            y =
                rowY
        )

        sectionY =
            nextSectionY(
                currentSectionY =
                    sectionY,

                rowCount =
                    5
            )

        /*
         * RECORDS
         */
        renderSectionTitle(
            batch =
                batch,

            font =
                font,

            title =
                "RECORDS",

            sectionY =
                sectionY
        )

        rowY =
            sectionY -
                FIRST_ROW_OFFSET

        rowY =
            renderRecordRow(
                batch =
                    batch,

                font =
                    font,

                label =
                    "Longest Run",

                value =
                    formatDuration(
                        statistics.longestRunSeconds
                    ),

                y =
                    rowY
            )

        rowY =
            renderRecordRow(
                batch =
                    batch,

                font =
                    font,

                label =
                    "Highest Damage",

                value =
                    formatLong(
                        statistics.highestDamage
                    ),

                y =
                    rowY
            )

        rowY =
            renderRecordRow(
                batch =
                    batch,

                font =
                    font,

                label =
                    "Most Gold",

                value =
                    formatGold(
                        statistics.mostGold
                    ),

                y =
                    rowY
            )

        rowY =
            renderRecordRow(
                batch =
                    batch,

                font =
                    font,

                label =
                    "Most Grisilith Coins",

                value =
                    statistics
                        .mostGrisilithCoins
                        .coerceAtLeast(
                            0
                        )
                        .toString(),

                y =
                    rowY
            )

        renderRecordRow(
            batch =
                batch,

            font =
                font,

            label =
                "Most Damage Taken",

            value =
                formatLong(
                    statistics.mostDamageTaken
                ),

            y =
                rowY
        )

        sectionY =
            nextSectionY(
                currentSectionY =
                    sectionY,

                rowCount =
                    5
            )

        /*
         * ACHIEVEMENTS
         */
        renderSectionTitle(
            batch =
                batch,

            font =
                font,

            title =
                "ACHIEVEMENTS",

            sectionY =
                sectionY
        )

        var achievementY =
            sectionY -
                FIRST_ROW_OFFSET

        renderAchievement(
            batch =
                batch,

            font =
                font,

            title =
                "Slained Total Enemy",

            currentValue =
                statistics.enemiesKilled,

            y =
                achievementY
        )

        achievementY -=
            ACHIEVEMENT_SPACING

        renderAchievement(
            batch =
                batch,

            font =
                font,

            title =
                "Slained Bosses",

            currentValue =
                statistics.bossesDefeated,

            y =
                achievementY
        )

        achievementY -=
            ACHIEVEMENT_SPACING

        renderAchievement(
            batch =
                batch,

            font =
                font,

            title =
                "Slained Final Bosses",

            currentValue =
                statistics.finalBossesDefeated,

            y =
                achievementY
        )

        font.data.setScale(
            oldScaleX,
            oldScaleY
        )

        font.color =
            oldColor
    }

    private fun renderTitle(
        batch: SpriteBatch,
        font: BitmapFont
    ) {

        font.data.setScale(
            2.25f
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            "HIGH SCORES",
            0f,
            TITLE_Y,
            VIEWPORT_WIDTH,
            Align.center,
            false
        )
    }

    private fun renderSectionTitle(
        batch: SpriteBatch,
        font: BitmapFont,
        title: String,
        sectionY: Float
    ) {

        font.data.setScale(
            1.2f
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            title,
            LABEL_X,
            sectionY -
                SECTION_TITLE_OFFSET
        )
    }

    private fun renderRow(
        batch: SpriteBatch,
        font: BitmapFont,
        label: String,
        value: String,
        y: Float
    ): Float {

        font.data.setScale(
            0.88f
        )

        font.color =
            Color.LIGHT_GRAY

        font.draw(
            batch,
            label,
            LABEL_X,
            y
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            value,
            VALUE_X,
            y,
            VALUE_WIDTH,
            Align.right,
            false
        )

        return y -
            ROW_SPACING
    }

    private fun renderRecordRow(
        batch: SpriteBatch,
        font: BitmapFont,
        label: String,
        value: String,
        y: Float
    ): Float {

        font.data.setScale(
            0.88f
        )

        font.color =
            Color.LIGHT_GRAY

        font.draw(
            batch,
            label,
            LABEL_X,
            y
        )

        font.color =
            Color.GOLD

        font.draw(
            batch,
            value,
            VALUE_X,
            y,
            VALUE_WIDTH,
            Align.right,
            false
        )

        return y -
            ROW_SPACING
    }

    private fun renderAchievement(
        batch: SpriteBatch,
        font: BitmapFont,
        title: String,
        currentValue: Long,
        y: Float
    ) {

        font.data.setScale(
            0.88f
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            title,
            ACHIEVEMENT_BAR_X,
            y
        )

        font.data.setScale(
            0.74f
        )

        font.color =
            Color.LIGHT_GRAY

        font.draw(
            batch,
            "${formatLong(currentValue)} / " +
                formatLong(
                    CareerStatistics
                        .ACHIEVEMENT_TARGET
                ),
            ACHIEVEMENT_BAR_X,
            y -
                ACHIEVEMENT_VALUE_TO_BAR -
                ACHIEVEMENT_BAR_HEIGHT,
            ACHIEVEMENT_BAR_WIDTH,
            Align.right,
            false
        )

        if (
            currentValue >=
            CareerStatistics.ACHIEVEMENT_TARGET
        ) {
            font.color =
                Color.GOLD

            font.draw(
                batch,
                "COMPLETED",
                ACHIEVEMENT_BAR_X,
                y -
                    ACHIEVEMENT_VALUE_TO_BAR -
                    ACHIEVEMENT_BAR_HEIGHT
            )
        }
    }

    private fun drawSectionLine(
        shapeRenderer: ShapeRenderer,
        y: Float
    ) {

        shapeRenderer.color =
            Color(
                0.48f,
                0.42f,
                0.60f,
                1f
            )

        shapeRenderer.rect(
            CONTENT_LEFT_X,
            y,
            CONTENT_RIGHT_X -
                CONTENT_LEFT_X,
            SECTION_LINE_HEIGHT
        )
    }

    private fun drawAchievementBar(
        shapeRenderer: ShapeRenderer,
        y: Float,
        progress: Float
    ) {

        val barY =
            y -
                ACHIEVEMENT_TITLE_TO_BAR

        shapeRenderer.color =
            Color(
                0.12f,
                0.10f,
                0.16f,
                1f
            )

        shapeRenderer.rect(
            ACHIEVEMENT_BAR_X,
            barY,
            ACHIEVEMENT_BAR_WIDTH,
            ACHIEVEMENT_BAR_HEIGHT
        )

        shapeRenderer.color =
            Color(
                0.40f,
                0.26f,
                0.62f,
                1f
            )

        shapeRenderer.rect(
            ACHIEVEMENT_BAR_X,
            barY,
            ACHIEVEMENT_BAR_WIDTH *
                progress.coerceIn(
                    0f,
                    1f
                ),
            ACHIEVEMENT_BAR_HEIGHT
        )
    }

    private fun nextSectionY(
        currentSectionY: Float,
        rowCount: Int
    ): Float {

        return currentSectionY -
            FIRST_ROW_OFFSET -
            ROW_SPACING *
            rowCount -
            SECTION_BOTTOM_SPACING
    }

    private fun formatLong(
        value: Long
    ): String {

        return String.format(
            "%,d",
            value.coerceAtLeast(
                0L
            )
        )
    }

    private fun formatGold(
        value: Float
    ): String {

        val safeValue =
            value.coerceAtLeast(
                0f
            )

        val roundedValue =
            safeValue.roundToInt()

        return if (
            abs(
                safeValue -
                    roundedValue.toFloat()
            ) <
            0.001f
        ) {
            String.format(
                "%,d",
                roundedValue
            )
        } else {
            String.format(
                "%,.1f",
                safeValue
            )
        }
    }

    private fun formatDuration(
        totalSeconds: Long
    ): String {

        val safeSeconds =
            totalSeconds.coerceAtLeast(
                0L
            )

        val hours =
            safeSeconds /
                3600L

        val minutes =
            (
                safeSeconds %
                    3600L
                ) /
                60L

        val seconds =
            safeSeconds %
                60L

        return String.format(
            "%02d:%02d:%02d",
            hours,
            minutes,
            seconds
        )
    }
}
