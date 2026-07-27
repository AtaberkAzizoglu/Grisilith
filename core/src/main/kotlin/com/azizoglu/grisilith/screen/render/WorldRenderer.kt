package com.azizoglu.grisilith.screen.render

import com.azizoglu.grisilith.economy.GoldCoinPickup
import com.azizoglu.grisilith.chest.ChestPickupManager
import com.azizoglu.grisilith.enemy.EnemyManager
import com.azizoglu.grisilith.map.MapObstacle
import com.azizoglu.grisilith.player.Player
import com.azizoglu.grisilith.weapon.Projectile
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.glutils.ShapeRenderer

class WorldRenderer(
    private val shapeRenderer: ShapeRenderer,
    private val camera: OrthographicCamera,
    private val worldWidth: Float,
    private val worldHeight: Float
) {

    companion object {
        private const val GRID_SIZE = 100f
    }

    fun render(
        mapObstacles:
        List<MapObstacle>,

        player:
        Player,

        enemyManager:
        EnemyManager,

        goldCoinPickups:
        List<GoldCoinPickup>,

        chestPickupManager:
        ChestPickupManager,

        projectiles:
        List<Projectile>
    ) {
        clearScreen()

        shapeRenderer.projectionMatrix =
            camera.combined

        renderGrid()
        renderWorldBorder()

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Filled
        )

        mapObstacles.forEach { obstacle ->
            obstacle.render(shapeRenderer)
        }

        player.render(shapeRenderer)

        enemyManager.render(
            shapeRenderer
        )

        goldCoinPickups.forEach { pickup ->
            pickup.render(shapeRenderer)
        }

        chestPickupManager.render(
            shapeRenderer
        )

        projectiles.forEach { projectile ->
            projectile.render(shapeRenderer)
        }

        shapeRenderer.end()
    }

    private fun clearScreen() {
        Gdx.gl.glClearColor(
            0.05f,
            0.05f,
            0.08f,
            1f
        )

        Gdx.gl.glClear(
            GL20.GL_COLOR_BUFFER_BIT
        )
    }

    private fun renderGrid() {
        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Line
        )

        var x = 0f

        while (x <= worldWidth) {
            shapeRenderer.line(
                x,
                0f,
                x,
                worldHeight
            )

            x += GRID_SIZE
        }

        var y = 0f

        while (y <= worldHeight) {
            shapeRenderer.line(
                0f,
                y,
                worldWidth,
                y
            )

            y += GRID_SIZE
        }

        shapeRenderer.end()
    }

    private fun renderWorldBorder() {
        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Line
        )

        shapeRenderer.rect(
            0f,
            0f,
            worldWidth,
            worldHeight
        )

        shapeRenderer.end()
    }
}
