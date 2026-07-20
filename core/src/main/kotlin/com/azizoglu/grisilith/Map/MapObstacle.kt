package com.azizoglu.grisilith.map

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle

class MapObstacle(
    val type: MapObstacleType,
    x: Float,
    y: Float,
    width: Float,
    height: Float
) {
    val bounds =
        Rectangle(
            x,
            y,
            width,
            height
        )

    fun render(
        shapeRenderer: ShapeRenderer
    ) {
        shapeRenderer.color =
            when (type) {
                MapObstacleType.ROCK ->
                    Color.GRAY

                MapObstacleType.TREE ->
                    Color.FOREST

                MapObstacleType.BROKEN_WAGON ->
                    Color.BROWN

                MapObstacleType.HOUSE ->
                    Color.SLATE

                MapObstacleType.WELL ->
                    Color.DARK_GRAY

                MapObstacleType.HAY_BALE ->
                    Color.GOLDENROD

                MapObstacleType.CAVE_ROCK ->
                    Color.DARK_GRAY
            }

        shapeRenderer.rect(
            bounds.x,
            bounds.y,
            bounds.width,
            bounds.height
        )
    }
}
