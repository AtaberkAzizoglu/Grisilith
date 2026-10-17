package com.azizoglu.grisilith.screen.controller

import com.azizoglu.grisilith.screen.render.RunSummaryStatRenderer
import com.azizoglu.grisilith.summary.RunSummarySnapshot
import com.azizoglu.grisilith.summary.RunSummaryStatEntry
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.Viewport

/*
 * Summary Stat satırlarının hover/tıklama davranışını yönetir.
 *
 * Restart/Main Menu input'u mevcut FirstScreen akışında kalır.
 */
class RunSummaryStatInteractionController(
    private val viewport: Viewport,
    private val renderer:
    RunSummaryStatRenderer,
    private val snapshotProvider:
        () -> RunSummarySnapshot
) {

    private val pointerPosition =
        Vector2()

    var hoveredStatIndex:
        Int? =
        null
        private set

    var selectedStat:
        RunSummaryStatEntry? =
        null
        private set

    val isDetailOpen:
        Boolean
        get() =
            selectedStat !=
                null

    fun update() {

        updatePointerPosition()

        val snapshot =
            snapshotProvider()

        hoveredStatIndex =
            renderer
                .currentLayout(
                    snapshot.stats.size
                )
                .statRowBounds
                .indexOfFirst {
                        bounds ->

                    bounds.contains(
                        pointerPosition
                    )
                }
                .takeIf {
                    it >=
                        0
                }
    }

    /*
     * true dönerse input tüketilmiştir ve FirstScreen
     * aynı frame'de Restart/Main Menu işlemi yapmamalıdır.
     */
    fun handleInput():
        Boolean = false

    fun closeDetail() {

        selectedStat =
            null

        hoveredStatIndex =
            null
    }

    private fun updatePointerPosition() {

        pointerPosition.set(
            Gdx.input.x.toFloat(),
            Gdx.input.y.toFloat()
        )

        viewport.unproject(
            pointerPosition
        )
    }
}
