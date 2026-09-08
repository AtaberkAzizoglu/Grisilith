package com.azizoglu.grisilith.map

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle

class MapObstacle(
    val type: MapObstacleType,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    visualVariant: Int = 0,
    val rendersVisual: Boolean = true
) {
    init {
        require(
            visualVariant in
                0..3
        )
    }

    var visualVariant: Int =
        visualVariant
        set(value) {
            require(value in 0..3)
            field = value
        }

    val blocksMovement: Boolean
        get() =
            type != MapObstacleType.LAKE &&
                type != MapObstacleType.PATHWAY

    val isGroundLayer: Boolean
        get() =
            type == MapObstacleType.LAKE ||
                type == MapObstacleType.PATHWAY

    /*
     * visualBounds:
     * Generator'dan gelen eski obstacle rectangle'ı.
     * Sprite yerleşimi ve dünya içindeki genel kapladığı alan için korunur.
     *
     * bounds:
     * Gerçek gameplay collision footprint'i.
     * Görselin tamamına değil, obstacle'ın zemine temas eden fiziksel kısmına
     * karşılık gelir.
     */
    val visualBounds =
        Rectangle(
            x,
            y,
            width,
            height
        )

    private val collisionBounds =
        Rectangle()

    val bounds: Rectangle
        get() {
            val footprint =
                collisionFootprint()

            val collisionWidth =
                visualBounds.width *
                    footprint.widthRatio

            val collisionHeight =
                visualBounds.height *
                    footprint.heightRatio

            val collisionX =
                visualBounds.x +
                    visualBounds.width *
                    footprint.offsetXRatio

            val collisionY =
                visualBounds.y +
                    visualBounds.height *
                    footprint.offsetYRatio

            collisionBounds.set(
                collisionX,
                collisionY,
                collisionWidth,
                collisionHeight
            )

            return collisionBounds
        }

    private fun collisionFootprint():
        CollisionFootprint {

        return when (type) {
            MapObstacleType.ROCK ->
                CollisionFootprint(
                    widthRatio = 0.72f,
                    heightRatio = 0.38f,
                    offsetXRatio = 0.14f,
                    offsetYRatio = 0.04f
                )

            MapObstacleType.TREE ->
                CollisionFootprint(
                    widthRatio = 0.30f,
                    heightRatio = 0.22f,
                    offsetXRatio = 0.35f,
                    offsetYRatio = 0.02f
                )

            MapObstacleType.WELL ->
                CollisionFootprint(
                    widthRatio = 0.76f,
                    heightRatio = 0.45f,
                    offsetXRatio = 0.12f,
                    offsetYRatio = 0.04f
                )

            MapObstacleType.HAY_BALE ->
                CollisionFootprint(
                    widthRatio = 0.82f,
                    heightRatio = 0.55f,
                    offsetXRatio = 0.09f,
                    offsetYRatio = 0.04f
                )

            MapObstacleType.BROKEN_WAGON ->
                CollisionFootprint(
                    widthRatio = 0.78f,
                    heightRatio = 0.48f,
                    offsetXRatio = 0.11f,
                    offsetYRatio = 0.04f
                )

            MapObstacleType.CAVE_ROCK ->
                CollisionFootprint(
                    widthRatio = 0.72f,
                    heightRatio = 0.38f,
                    offsetXRatio = 0.14f,
                    offsetYRatio = 0.04f
                )

            MapObstacleType.CRATE ->
                CollisionFootprint(
                    widthRatio = 0.76f,
                    heightRatio = 0.68f,
                    offsetXRatio = 0.12f,
                    offsetYRatio = 0.03f
                )

            MapObstacleType.BARREL ->
                CollisionFootprint(
                    widthRatio = 0.72f,
                    heightRatio = 0.66f,
                    offsetXRatio = 0.14f,
                    offsetYRatio = 0.03f
                )

            MapObstacleType.LOG_PILE ->
                CollisionFootprint(
                    widthRatio = 0.78f,
                    heightRatio = 0.42f,
                    offsetXRatio = 0.11f,
                    offsetYRatio = 0.04f
                )

            MapObstacleType.BUSH ->
                CollisionFootprint(
                    widthRatio = 0.55f,
                    heightRatio = 0.22f,
                    offsetXRatio = 0.225f,
                    offsetYRatio = 0.03f
                )

            MapObstacleType.LAKE ->
                CollisionFootprint(
                    widthRatio = 0.76f,
                    heightRatio = 0.64f,
                    offsetXRatio = 0.12f,
                    offsetYRatio = 0.17f
                )

            MapObstacleType.PATHWAY ->
                CollisionFootprint(
                    widthRatio = 1f,
                    heightRatio = 1f,
                    offsetXRatio = 0f,
                    offsetYRatio = 0f
                )

            MapObstacleType.CITY_WALL ->
                CollisionFootprint(
                    widthRatio = 1f,
                    heightRatio = 0.24f,
                    offsetXRatio = 0f,
                    offsetYRatio = 0f
                )

            MapObstacleType.CITY_GATE ->
                CollisionFootprint(
                    widthRatio = TroubleAtHomeLayout.GATE_COLLISION_WIDTH_RATIO,
                    heightRatio = TroubleAtHomeLayout.GATE_COLLISION_HEIGHT_RATIO,
                    offsetXRatio = TroubleAtHomeLayout.GATE_COLLISION_OFFSET_X_RATIO,
                    offsetYRatio = 0f
                )
        }
    }

    fun containsEffectPoint(
        worldX: Float,
        worldY: Float
    ): Boolean {
        if (type != MapObstacleType.LAKE) {
            return false
        }

        val area = bounds
        val radiusX = area.width / 2f
        val radiusY = area.height / 2f

        if (radiusX <= 0f || radiusY <= 0f) {
            return false
        }

        val normalizedX =
            (worldX - (area.x + radiusX)) /
                radiusX

        val normalizedY =
            (worldY - (area.y + radiusY)) /
                radiusY

        return normalizedX * normalizedX +
            normalizedY * normalizedY <= 1f
    }

    private data class CollisionFootprint(
        val widthRatio: Float,
        val heightRatio: Float,
        val offsetXRatio: Float,
        val offsetYRatio: Float
    )

    fun render(
        shapeRenderer: ShapeRenderer,
        renderBody: Boolean = true
    ) {
        if (!renderBody || !rendersVisual) {
            return
        }

        shapeRenderer.color =
            when (type) {
                MapObstacleType.ROCK ->
                    Color.GRAY

                MapObstacleType.TREE ->
                    Color.FOREST

                MapObstacleType.BROKEN_WAGON ->
                    Color.BROWN

                MapObstacleType.WELL ->
                    Color.DARK_GRAY

                MapObstacleType.HAY_BALE ->
                    Color.GOLDENROD

                MapObstacleType.CRATE,
                MapObstacleType.BARREL,
                MapObstacleType.LOG_PILE ->
                    Color.BROWN

                MapObstacleType.BUSH ->
                    Color.FOREST

                MapObstacleType.CAVE_ROCK ->
                    Color.DARK_GRAY

                MapObstacleType.LAKE ->
                    Color.CYAN

                MapObstacleType.PATHWAY ->
                    Color.TAN

                MapObstacleType.CITY_WALL,
                MapObstacleType.CITY_GATE ->
                    Color.BROWN
            }

        shapeRenderer.rect(
            bounds.x,
            bounds.y,
            bounds.width,
            bounds.height
        )
    }
}
