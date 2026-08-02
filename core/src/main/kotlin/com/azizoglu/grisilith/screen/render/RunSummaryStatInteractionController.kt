package com.azizoglu.grisilith.screen.controller

import com.azizoglu.grisilith.screen.render.RunSummaryStatRenderer
import com.azizoglu.grisilith.summary.RunSummarySnapshot
import com.azizoglu.grisilith.summary.RunSummaryStatEntry
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
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

        if (isDetailOpen) {
            hoveredStatIndex =
                null

            return
        }

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
        Boolean {

        if (
            selectedStat !=
            null
        ) {
            return handleDetailInput()
        }

        if (
            !Gdx.input.isButtonJustPressed(
                Input.Buttons.LEFT
            )
        ) {
            return false
        }

        updatePointerPosition()

        val snapshot =
            snapshotProvider()

        val selectedIndex =
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

        if (
            selectedIndex <
            0
        ) {
            return false
        }

        selectedStat =
            snapshot.stats
                .getOrNull(
                    selectedIndex
                )

        return selectedStat !=
            null
    }

    private fun handleDetailInput():
        Boolean {

        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.ESCAPE
            )
        ) {
            closeDetail()

            return true
        }

        if (
            !Gdx.input.isButtonJustPressed(
                Input.Buttons.LEFT
            )
        ) {
            /*
             * Detay açıkken klavye Restart/Main Menu de
             * çalışmasın. Modal input tamamen tüketir.
             */
            return true
        }

        updatePointerPosition()

        val layout =
            renderer.currentLayout(
                snapshotProvider()
                    .stats
                    .size
            )

        if (
            !layout.detailBounds
                .contains(
                    pointerPosition
                )
        ) {
            closeDetail()
        }

        return true
    }

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
