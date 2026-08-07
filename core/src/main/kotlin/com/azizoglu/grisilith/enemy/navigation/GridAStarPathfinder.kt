package com.azizoglu.grisilith.enemy.navigation

import com.azizoglu.grisilith.map.MapObstacle
import com.badlogic.gdx.math.Vector2
import java.util.PriorityQueue
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.sqrt

/*
 * Statik obstacle listeleri için hafif grid tabanlı A*.
 *
 * Grid, her Enemy'nin gerçek boyutuna göre obstacle'ları genişletilmiş
 * kabul eder; bu sayede yalnızca merkezi değil bütün collision alanı
 * geçerli bir rota takip eder.
 */
object GridAStarPathfinder {

    private const val CELL_SIZE =
        48f

    private const val OBSTACLE_CLEARANCE =
        5f

    private const val DIRECT_PATH_SAMPLE_STEP =
        24f

    private val neighbourColumns =
        intArrayOf(
            -1, 0, 1,
            -1,    1,
            -1, 0, 1
        )

    private val neighbourRows =
        intArrayOf(
            1, 1, 1,
            0,    0,
            -1, -1, -1
        )

    fun hasDirectPath(
        startX: Float,
        startY: Float,
        targetX: Float,
        targetY: Float,
        agentSize: Float,
        worldWidth: Float,
        worldHeight: Float,
        obstacles: List<MapObstacle>
    ): Boolean {

        val differenceX =
            targetX -
                startX

        val differenceY =
            targetY -
                startY

        val distance =
            sqrt(
                differenceX *
                    differenceX +
                    differenceY *
                    differenceY
            )

        if (
            distance <=
            0.001f
        ) {
            return true
        }

        val steps =
            ceil(
                distance /
                    DIRECT_PATH_SAMPLE_STEP
            )
                .toInt()
                .coerceAtLeast(
                    1
                )

        for (
        step in 1..steps
        ) {
            val ratio =
                step.toFloat() /
                    steps.toFloat()

            val sampleX =
                startX +
                    differenceX *
                    ratio

            val sampleY =
                startY +
                    differenceY *
                    ratio

            if (
                isPositionBlocked(
                    centerX =
                        sampleX,

                    centerY =
                        sampleY,

                    agentSize =
                        agentSize,

                    worldWidth =
                        worldWidth,

                    worldHeight =
                        worldHeight,

                    obstacles =
                        obstacles
                )
            ) {
                return false
            }
        }

        return true
    }

    fun findPath(
        startX: Float,
        startY: Float,
        targetX: Float,
        targetY: Float,
        agentSize: Float,
        worldWidth: Float,
        worldHeight: Float,
        obstacles: List<MapObstacle>
    ): List<Vector2> {

        val columns =
            ceil(
                worldWidth /
                    CELL_SIZE
            )
                .toInt()
                .coerceAtLeast(
                    1
                )

        val rows =
            ceil(
                worldHeight /
                    CELL_SIZE
            )
                .toInt()
                .coerceAtLeast(
                    1
                )

        val startCell =
            nearestOpenCell(
                requestedColumn =
                    worldToColumn(
                        startX,
                        columns
                    ),

                requestedRow =
                    worldToRow(
                        startY,
                        rows
                    ),

                columns =
                    columns,

                rows =
                    rows,

                agentSize =
                    agentSize,

                worldWidth =
                    worldWidth,

                worldHeight =
                    worldHeight,

                obstacles =
                    obstacles
            )
                ?: return emptyList()

        val goalCell =
            nearestOpenCell(
                requestedColumn =
                    worldToColumn(
                        targetX,
                        columns
                    ),

                requestedRow =
                    worldToRow(
                        targetY,
                        rows
                    ),

                columns =
                    columns,

                rows =
                    rows,

                agentSize =
                    agentSize,

                worldWidth =
                    worldWidth,

                worldHeight =
                    worldHeight,

                obstacles =
                    obstacles
            )
                ?: return emptyList()

        val startIndex =
            cellIndex(
                startCell.first,
                startCell.second,
                columns
            )

        val goalIndex =
            cellIndex(
                goalCell.first,
                goalCell.second,
                columns
            )

        if (
            startIndex ==
            goalIndex
        ) {
            return listOf(
                Vector2(
                    targetX,
                    targetY
                )
            )
        }

        val cellCount =
            columns *
                rows

        val gScore =
            FloatArray(
                cellCount
            ) {
                Float.POSITIVE_INFINITY
            }

        val cameFrom =
            IntArray(
                cellCount
            ) {
                -1
            }

        val closed =
            BooleanArray(
                cellCount
            )

        val open =
            PriorityQueue<Node>(
                compareBy<Node> {
                    it.fScore
                }
            )

        gScore[
            startIndex
        ] =
            0f

        open.add(
            Node(
                index =
                    startIndex,

                fScore =
                    heuristic(
                        startCell.first,
                        startCell.second,
                        goalCell.first,
                        goalCell.second
                    )
            )
        )

        while (
            open.isNotEmpty()
        ) {
            val current =
                open.poll()

            if (
                closed[
                    current.index
                ]
            ) {
                continue
            }

            if (
                current.index ==
                goalIndex
            ) {
                return reconstructPath(
                    cameFrom =
                        cameFrom,

                    startIndex =
                        startIndex,

                    goalIndex =
                        goalIndex,

                    columns =
                        columns,

                    exactTargetX =
                        targetX,

                    exactTargetY =
                        targetY
                )
            }

            closed[
                current.index
            ] =
                true

            val currentColumn =
                current.index %
                    columns

            val currentRow =
                current.index /
                    columns

            for (
            neighbourIndex in
            neighbourColumns.indices
            ) {
                val columnOffset =
                    neighbourColumns[
                        neighbourIndex
                    ]

                val rowOffset =
                    neighbourRows[
                        neighbourIndex
                    ]

                val nextColumn =
                    currentColumn +
                        columnOffset

                val nextRow =
                    currentRow +
                        rowOffset

                if (
                    nextColumn !in
                    0 until columns ||
                    nextRow !in
                    0 until rows
                ) {
                    continue
                }

                if (
                    isCellBlocked(
                        column =
                            nextColumn,

                        row =
                            nextRow,

                        agentSize =
                            agentSize,

                        worldWidth =
                            worldWidth,

                        worldHeight =
                            worldHeight,

                        obstacles =
                            obstacles
                    )
                ) {
                    continue
                }

                val isDiagonal =
                    columnOffset !=
                        0 &&
                        rowOffset !=
                        0

                if (
                    isDiagonal &&
                    (
                        isCellBlocked(
                            column =
                                currentColumn +
                                    columnOffset,

                            row =
                                currentRow,

                            agentSize =
                                agentSize,

                            worldWidth =
                                worldWidth,

                            worldHeight =
                                worldHeight,

                            obstacles =
                                obstacles
                        ) ||
                            isCellBlocked(
                                column =
                                    currentColumn,

                                row =
                                    currentRow +
                                        rowOffset,

                                agentSize =
                                    agentSize,

                                worldWidth =
                                    worldWidth,

                                worldHeight =
                                    worldHeight,

                                obstacles =
                                    obstacles
                            )
                        )
                ) {
                    continue
                }

                val nextIndex =
                    cellIndex(
                        nextColumn,
                        nextRow,
                        columns
                    )

                if (
                    closed[
                        nextIndex
                    ]
                ) {
                    continue
                }

                val movementCost =
                    if (
                        isDiagonal
                    ) {
                        1.4142135f
                    } else {
                        1f
                    }

                val tentativeG =
                    gScore[
                        current.index
                    ] +
                        movementCost

                if (
                    tentativeG >=
                    gScore[
                        nextIndex
                    ]
                ) {
                    continue
                }

                cameFrom[
                    nextIndex
                ] =
                    current.index

                gScore[
                    nextIndex
                ] =
                    tentativeG

                open.add(
                    Node(
                        index =
                            nextIndex,

                        fScore =
                            tentativeG +
                                heuristic(
                                    nextColumn,
                                    nextRow,
                                    goalCell.first,
                                    goalCell.second
                                )
                    )
                )
            }
        }

        return emptyList()
    }

    private fun reconstructPath(
        cameFrom: IntArray,
        startIndex: Int,
        goalIndex: Int,
        columns: Int,
        exactTargetX: Float,
        exactTargetY: Float
    ): List<Vector2> {

        val reversed =
            mutableListOf<Int>()

        var current =
            goalIndex

        while (
            current !=
            -1 &&
            current !=
            startIndex
        ) {
            reversed.add(
                current
            )

            current =
                cameFrom[
                    current
                ]
        }

        if (
            current !=
            startIndex
        ) {
            return emptyList()
        }

        reversed.reverse()

        val result =
            ArrayList<Vector2>(
                reversed.size +
                    1
            )

        reversed.forEach { index ->
            val column =
                index %
                    columns

            val row =
                index /
                    columns

            result.add(
                Vector2(
                    cellCenterX(
                        column
                    ),

                    cellCenterY(
                        row
                    )
                )
            )
        }

        result.add(
            Vector2(
                exactTargetX,
                exactTargetY
            )
        )

        return result
    }

    private fun nearestOpenCell(
        requestedColumn: Int,
        requestedRow: Int,
        columns: Int,
        rows: Int,
        agentSize: Float,
        worldWidth: Float,
        worldHeight: Float,
        obstacles: List<MapObstacle>
    ): Pair<Int, Int>? {

        if (
            !isCellBlocked(
                column =
                    requestedColumn,

                row =
                    requestedRow,

                agentSize =
                    agentSize,

                worldWidth =
                    worldWidth,

                worldHeight =
                    worldHeight,

                obstacles =
                    obstacles
            )
        ) {
            return Pair(
                requestedColumn,
                requestedRow
            )
        }

        for (
        radius in
        1..MAXIMUM_OPEN_CELL_SEARCH_RADIUS
        ) {
            for (
            rowOffset in
            -radius..radius
            ) {
                for (
                columnOffset in
                -radius..radius
                ) {
                    if (
                        max(
                            abs(
                                columnOffset
                            ),
                            abs(
                                rowOffset
                            )
                        ) !=
                        radius
                    ) {
                        continue
                    }

                    val column =
                        requestedColumn +
                            columnOffset

                    val row =
                        requestedRow +
                            rowOffset

                    if (
                        column !in
                        0 until columns ||
                        row !in
                        0 until rows
                    ) {
                        continue
                    }

                    if (
                        !isCellBlocked(
                            column =
                                column,

                            row =
                                row,

                            agentSize =
                                agentSize,

                            worldWidth =
                                worldWidth,

                            worldHeight =
                                worldHeight,

                            obstacles =
                                obstacles
                        )
                    ) {
                        return Pair(
                            column,
                            row
                        )
                    }
                }
            }
        }

        return null
    }

    private fun isCellBlocked(
        column: Int,
        row: Int,
        agentSize: Float,
        worldWidth: Float,
        worldHeight: Float,
        obstacles: List<MapObstacle>
    ): Boolean {
        return isPositionBlocked(
            centerX =
                cellCenterX(
                    column
                ),

            centerY =
                cellCenterY(
                    row
                ),

            agentSize =
                agentSize,

            worldWidth =
                worldWidth,

            worldHeight =
                worldHeight,

            obstacles =
                obstacles
        )
    }

    private fun isPositionBlocked(
        centerX: Float,
        centerY: Float,
        agentSize: Float,
        worldWidth: Float,
        worldHeight: Float,
        obstacles: List<MapObstacle>
    ): Boolean {

        val halfSize =
            agentSize /
                2f +
                OBSTACLE_CLEARANCE

        val left =
            centerX -
                halfSize

        val right =
            centerX +
                halfSize

        val bottom =
            centerY -
                halfSize

        val top =
            centerY +
                halfSize

        if (
            left <
            0f ||
            bottom <
            0f ||
            right >
            worldWidth ||
            top >
            worldHeight
        ) {
            return true
        }

        return obstacles.any { obstacle ->
            val bounds =
                obstacle.bounds

            right >
                bounds.x &&
                left <
                bounds.x +
                bounds.width &&
                top >
                bounds.y &&
                bottom <
                bounds.y +
                bounds.height
        }
    }

    private fun worldToColumn(
        x: Float,
        columns: Int
    ): Int {
        return floor(
            x /
                CELL_SIZE
        )
            .toInt()
            .coerceIn(
                0,
                columns -
                    1
            )
    }

    private fun worldToRow(
        y: Float,
        rows: Int
    ): Int {
        return floor(
            y /
                CELL_SIZE
        )
            .toInt()
            .coerceIn(
                0,
                rows -
                    1
            )
    }

    private fun cellCenterX(
        column: Int
    ): Float {
        return column *
            CELL_SIZE +
            CELL_SIZE /
            2f
    }

    private fun cellCenterY(
        row: Int
    ): Float {
        return row *
            CELL_SIZE +
            CELL_SIZE /
            2f
    }

    private fun cellIndex(
        column: Int,
        row: Int,
        columns: Int
    ): Int {
        return row *
            columns +
            column
    }

    private fun heuristic(
        column: Int,
        row: Int,
        goalColumn: Int,
        goalRow: Int
    ): Float {

        val differenceX =
            abs(
                goalColumn -
                    column
            )

        val differenceY =
            abs(
                goalRow -
                    row
            )

        val minimum =
            minOf(
                differenceX,
                differenceY
            )

        val maximum =
            maxOf(
                differenceX,
                differenceY
            )

        return minimum *
            1.4142135f +
            (
                maximum -
                    minimum
                )
    }

    private data class Node(
        val index: Int,
        val fScore: Float
    )

    private const val MAXIMUM_OPEN_CELL_SEARCH_RADIUS =
        4
}
