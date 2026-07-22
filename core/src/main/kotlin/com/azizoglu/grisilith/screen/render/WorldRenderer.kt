package com.azizoglu.grisilith.screen.render

import com.badlogic.gdx.graphics.glutils.ShapeRenderer

class WorldRenderer(

    private val shapeRenderer: ShapeRenderer

) {

    fun begin() {
        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Filled
        )
    }

    fun end() {
        shapeRenderer.end()
    }
}
