package com.azizoglu.grisilith.weapon.glyph

import com.badlogic.gdx.math.Vector2
import kotlin.math.acos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

object GlyphGeometry {

    fun pathLength(
        points: List<Vector2>
    ): Float {

        if (
            points.size <
            2
        ) {
            return 0f
        }

        var total =
            0f

        for (
        index in
        1 until points.size
        ) {
            total +=
                points[index - 1]
                    .dst(
                        points[index]
                    )
        }

        return total
    }

    fun resample(
        source: List<Vector2>,
        requestedPointCount: Int
    ): List<Vector2> {

        if (
            source.isEmpty() ||
            requestedPointCount <=
            0
        ) {
            return emptyList()
        }

        if (
            source.size ==
            1
        ) {
            return List(
                requestedPointCount
            ) {
                Vector2(
                    source.first()
                )
            }
        }

        val totalLength =
            pathLength(
                source
            )

        if (
            totalLength <=
            0.0001f
        ) {
            return List(
                requestedPointCount
            ) {
                Vector2(
                    source.first()
                )
            }
        }

        val interval =
            totalLength /
                (
                    requestedPointCount -
                        1
                    )

        val working =
            source
                .map {
                    Vector2(
                        it
                    )
                }
                .toMutableList()

        val result =
            mutableListOf(
                Vector2(
                    working.first()
                )
            )

        var accumulated =
            0f

        var index =
            1

        while (
            index <
            working.size &&
            result.size <
            requestedPointCount
        ) {
            val previous =
                working[index - 1]

            val current =
                working[index]

            val segmentLength =
                previous.dst(
                    current
                )

            if (
                segmentLength <=
                0.0001f
            ) {
                index++
                continue
            }

            if (
                accumulated +
                segmentLength >=
                interval
            ) {
                val ratio =
                    (
                        interval -
                            accumulated
                        ) /
                        segmentLength

                val inserted =
                    Vector2(
                        previous.x +
                            (
                                current.x -
                                    previous.x
                                ) *
                            ratio,

                        previous.y +
                            (
                                current.y -
                                    previous.y
                                ) *
                            ratio
                    )

                result.add(
                    inserted
                )

                working.add(
                    index,
                    inserted
                )

                accumulated =
                    0f
            } else {
                accumulated +=
                    segmentLength

                index++
            }
        }

        while (
            result.size <
            requestedPointCount
        ) {
            result.add(
                Vector2(
                    working.last()
                )
            )
        }

        return result
    }

    fun normalize(
        source: List<Vector2>,
        pointCount: Int =
            64
    ): List<Vector2> {

        val sampled =
            resample(
                source =
                    source,

                requestedPointCount =
                    pointCount
            )

        if (
            sampled.isEmpty()
        ) {
            return emptyList()
        }

        var minimumX =
            Float.POSITIVE_INFINITY

        var minimumY =
            Float.POSITIVE_INFINITY

        var maximumX =
            Float.NEGATIVE_INFINITY

        var maximumY =
            Float.NEGATIVE_INFINITY

        sampled.forEach {
                point ->

            minimumX =
                min(
                    minimumX,
                    point.x
                )

            minimumY =
                min(
                    minimumY,
                    point.y
                )

            maximumX =
                max(
                    maximumX,
                    point.x
                )

            maximumY =
                max(
                    maximumY,
                    point.y
                )
        }

        val width =
            (
                maximumX -
                    minimumX
                )
                .coerceAtLeast(
                    1f
                )

        val height =
            (
                maximumY -
                    minimumY
                )
                .coerceAtLeast(
                    1f
                )

        val scale =
            1f /
                max(
                    width,
                    height
                )

        val centerX =
            (
                minimumX +
                    maximumX
                ) /
                2f

        val centerY =
            (
                minimumY +
                    maximumY
                ) /
                2f

        return sampled.map {
                point ->

            Vector2(
                (
                    point.x -
                        centerX
                    ) *
                    scale,

                (
                    point.y -
                        centerY
                    ) *
                    scale
            )
        }
    }

    fun rotate(
        source: List<Vector2>,
        degrees: Float
    ): List<Vector2> {

        return source.map {
                point ->

            Vector2(
                point
            )
                .rotateDeg(
                    degrees
                )
        }
    }

    fun averageDistance(
        first: List<Vector2>,
        second: List<Vector2>
    ): Float {

        val count =
            min(
                first.size,
                second.size
            )

        if (
            count <=
            0
        ) {
            return Float.MAX_VALUE
        }

        var total =
            0f

        repeat(
            count
        ) {
                index ->

            total +=
                first[index]
                    .dst(
                        second[index]
                    )
        }

        return total /
            count
    }

    fun turningAngles(
        points: List<Vector2>
    ): List<Float> {

        if (
            points.size <
            3
        ) {
            return emptyList()
        }

        val result =
            mutableListOf<Float>()

        for (
        index in
        1 until points.lastIndex
        ) {
            val incoming =
                Vector2(
                    points[index - 1]
                )
                    .sub(
                        points[index]
                    )

            val outgoing =
                Vector2(
                    points[index + 1]
                )
                    .sub(
                        points[index]
                    )

            if (
                incoming.isZero ||
                outgoing.isZero
            ) {
                continue
            }

            incoming.nor()
            outgoing.nor()

            val dot =
                incoming.dot(
                    outgoing
                )
                    .coerceIn(
                        -1f,
                        1f
                    )

            result.add(
                Math.toDegrees(
                    acos(
                        dot.toDouble()
                    )
                )
                    .toFloat()
            )
        }

        return result
    }

    fun segmentIntersectionCount(
        points: List<Vector2>
    ): Int {

        if (
            points.size <
            4
        ) {
            return 0
        }

        var intersections =
            0

        for (
        firstIndex in
        0 until points.size - 1
        ) {
            for (
            secondIndex in
            firstIndex + 2 until points.size - 1
            ) {
                if (
                    firstIndex ==
                    0 &&
                    secondIndex ==
                    points.size -
                    2
                ) {
                    continue
                }

                if (
                    segmentsIntersect(
                        points[firstIndex],
                        points[firstIndex + 1],
                        points[secondIndex],
                        points[secondIndex + 1]
                    )
                ) {
                    intersections++
                }
            }
        }

        return intersections
    }

    fun simplify(
        source: List<Vector2>,
        epsilon: Float
    ): List<Vector2> {

        if (source.size < 3 || epsilon <= 0f) {
            return source.map { Vector2(it) }
        }

        var maximumDistance = 0f
        var splitIndex = 0
        val start = source.first()
        val end = source.last()

        for (index in 1 until source.lastIndex) {
            val distance = distanceToSegment(
                pointX = source[index].x,
                pointY = source[index].y,
                start = start,
                end = end
            )
            if (distance > maximumDistance) {
                maximumDistance = distance
                splitIndex = index
            }
        }

        if (maximumDistance <= epsilon) {
            return listOf(Vector2(start), Vector2(end))
        }

        val firstPart = simplify(source.subList(0, splitIndex + 1), epsilon)
        val secondPart = simplify(source.subList(splitIndex, source.size), epsilon)

        return buildList {
            addAll(firstPart.dropLast(1))
            addAll(secondPart)
        }
    }

    fun distanceToSegment(
        pointX: Float,
        pointY: Float,
        start: Vector2,
        end: Vector2
    ): Float {

        val segmentX =
            end.x -
                start.x

        val segmentY =
            end.y -
                start.y

        val lengthSquared =
            segmentX *
                segmentX +
                segmentY *
                segmentY

        if (
            lengthSquared <=
            0.0001f
        ) {
            return Vector2.dst(
                pointX,
                pointY,
                start.x,
                start.y
            )
        }

        val projection =
            (
                (
                    pointX -
                        start.x
                    ) *
                    segmentX +
                    (
                        pointY -
                            start.y
                        ) *
                    segmentY
                ) /
                lengthSquared
        .coerceIn(
            0f,
            1f
        )

        val closestX =
            start.x +
                segmentX *
                projection

        val closestY =
            start.y +
                segmentY *
                projection

        return Vector2.dst(
            pointX,
            pointY,
            closestX,
            closestY
        )
    }

    private fun segmentsIntersect(
        firstStart: Vector2,
        firstEnd: Vector2,
        secondStart: Vector2,
        secondEnd: Vector2
    ): Boolean {

        val denominator =
            (
                firstEnd.x -
                    firstStart.x
                ) *
                (
                    secondEnd.y -
                        secondStart.y
                    ) -
                (
                    firstEnd.y -
                        firstStart.y
                    ) *
                (
                    secondEnd.x -
                        secondStart.x
                    )

        if (
            kotlin.math.abs(
                denominator
            ) <=
            0.0001f
        ) {
            return false
        }

        val firstParameter =
            (
                (
                    secondStart.x -
                        firstStart.x
                    ) *
                    (
                        secondEnd.y -
                            secondStart.y
                        ) -
                    (
                        secondStart.y -
                            firstStart.y
                        ) *
                    (
                        secondEnd.x -
                            secondStart.x
                        )
                ) /
                denominator

        val secondParameter =
            (
                (
                    secondStart.x -
                        firstStart.x
                    ) *
                    (
                        firstEnd.y -
                            firstStart.y
                        ) -
                    (
                        secondStart.y -
                            firstStart.y
                        ) *
                    (
                        firstEnd.x -
                            firstStart.x
                        )
                ) /
                denominator

        return firstParameter in
            0f..1f &&
            secondParameter in
            0f..1f
    }
}
