package com.azizoglu.grisilith.screen.render

import com.azizoglu.grisilith.chest.ChestPickupManager
import com.azizoglu.grisilith.character.visual.CharacterSpriteRenderer
import com.azizoglu.grisilith.content.ContentId
import com.azizoglu.grisilith.economy.GoldCoinPickup
import com.azizoglu.grisilith.enemy.EnemyManager
import com.azizoglu.grisilith.enemy.visual.EnemySpriteRenderer
import com.azizoglu.grisilith.map.MapObstacle
import com.azizoglu.grisilith.map.visual.MapObstacleSpriteRenderer
import com.azizoglu.grisilith.map.tile.MapGroundTileRenderer
import com.azizoglu.grisilith.player.Player
import com.azizoglu.grisilith.weapon.Projectile
import com.azizoglu.grisilith.weapon.visual.ProjectileSpriteRenderer
import com.azizoglu.grisilith.weapon.visual.carrier.WeaponCarrierRenderer
import com.azizoglu.grisilith.weapon.visual.carrier.WeaponCarrierVisualSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.glutils.ShapeRenderer

class WorldRenderer(
    private val shapeRenderer: ShapeRenderer,
    private val camera: OrthographicCamera,
    private val worldWidth: Float,
    private val worldHeight: Float,
    private val selectedCharacterId: ContentId
) {

    companion object {
        private const val GRID_SIZE =
            100f

        /*
         * Grass tile aktifken prototip grid'ini gizliyoruz.
         * Gerektiğinde debug için true yapılabilir.
         */
        private const val SHOW_DEBUG_GRID =
            false
    }

    private val mapGroundTileRenderer =
        MapGroundTileRenderer(
            worldWidth =
                worldWidth,

            worldHeight =
                worldHeight
        )

    private val mapObstacleSpriteRenderer =
        MapObstacleSpriteRenderer()

    private val characterSpriteRenderer =
        CharacterSpriteRenderer()

    private val enemySpriteRenderer =
        EnemySpriteRenderer()

    private val projectileSpriteRenderer =
        ProjectileSpriteRenderer()

    private val weaponCarrierRenderer =
        WeaponCarrierRenderer()

    /*
     * LibGDX world Y ekseni yukarı doğru büyür.
     *
     * Top-down depth için kuzeyde/yukarıda kalan obje önce,
     * güneyde/aşağıda kalan obje sonra çizilmelidir.
     * Böylece aşağıdaki obje öndeymiş gibi görünür.
     */
    private data class DepthRenderEntry(
        val depthY: Float,
        val render: () -> Unit
    )

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
        List<Projectile>,

        weaponCarrierVisualSystem:
        WeaponCarrierVisualSystem,

        renderBehindEntities:
        ((ShapeRenderer) -> Unit)? = null
    ) {
        clearScreen()

        /*
         * PASS 0:
         * Road to Home ground layer.
         * Şimdilik yalnızca Grass Tile kullanılıyor.
         */
        mapGroundTileRenderer.render(
            camera =
                camera
        )

        shapeRenderer.projectionMatrix =
            camera.combined

        if (
            SHOW_DEBUG_GRID
        ) {
            renderGrid()
        }

        renderWorldBorder()

        /*
         * PASS 1:
         * Dünya tabanı, pickup'lar ve sprite tanımı bulunmayan
         * placeholder gövdeler.
         */
        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Filled
        )

        renderBehindEntities
            ?.invoke(
                shapeRenderer
            )

        mapObstacles.forEach {
                obstacle ->

            obstacle.render(
                shapeRenderer =
                    shapeRenderer,

                renderBody =
                    !mapObstacleSpriteRenderer
                        .hasVisualDefinition(
                            obstacle
                        )
            )
        }

        player.render(
            shapeRenderer =
                shapeRenderer,

            renderBody =
                !characterSpriteRenderer
                    .hasVisualDefinition(
                        selectedCharacterId
                    )
        )

        goldCoinPickups.forEach {
                pickup ->

            pickup.render(
                shapeRenderer
            )
        }

        chestPickupManager.render(
            shapeRenderer
        )

        shapeRenderer.end()

        /*
         * PASS 2:
         * MAP OBSTACLE + CHARACTER + ENEMY ortak Y-sort.
         *
         * depthY olarak entity/obstacle'ın zemine temas eden alt
         * kenarını kullanıyoruz. Büyük Y önce çizilir; küçük Y sonra
         * çizildiği için ekranda daha güneyde olan obje öne gelir.
         */
        val depthEntries =
            mutableListOf<DepthRenderEntry>()

        mapObstacles.forEach {
                obstacle ->

            if (
                mapObstacleSpriteRenderer
                    .hasVisualDefinition(
                        obstacle
                    )
            ) {
                depthEntries.add(
                    DepthRenderEntry(
                        depthY =
                            obstacle.bounds.y,

                        render = {
                            mapObstacleSpriteRenderer.render(
                                obstacles =
                                    listOf(
                                        obstacle
                                    ),

                                camera =
                                    camera
                            )
                        }
                    )
                )
            }
        }

        val carrierSnapshots =
            weaponCarrierVisualSystem
                .snapshots(
                    player
                )

        val playerCarrier =
            carrierSnapshots
                .firstOrNull {
                    it.slotIndex == 0
                }

        if (
            characterSpriteRenderer
                .hasVisualDefinition(
                    selectedCharacterId
                )
        ) {
            depthEntries.add(
                DepthRenderEntry(
                    depthY =
                        player.bounds.y,

                    render = {
                        if (
                            playerCarrier !=
                            null
                        ) {
                            weaponCarrierRenderer
                                .renderPlayerCarrier(
                                    snapshot =
                                        playerCarrier,

                                    camera =
                                        camera,

                                    renderCharacter = {
                                        characterSpriteRenderer
                                            .render(
                                                player =
                                                    player,

                                                characterId =
                                                    selectedCharacterId,

                                                camera =
                                                    camera
                                            )
                                    }
                                )
                        } else {
                            characterSpriteRenderer.render(
                                player =
                                    player,

                                characterId =
                                    selectedCharacterId,

                                camera =
                                    camera
                            )
                        }
                    }
                )
            )
        }

        carrierSnapshots
            .filter {
                it.slotIndex > 0
            }
            .forEach {
                    snapshot ->

                depthEntries.add(
                    DepthRenderEntry(
                        depthY =
                            snapshot.depthY,

                        render = {
                            weaponCarrierRenderer
                                .renderMinionCarrier(
                                    snapshot =
                                        snapshot,

                                    camera =
                                        camera
                                )
                        }
                    )
                )
            }

        enemyManager.allEnemies
            .forEach {
                    enemy ->

                if (
                    enemySpriteRenderer
                        .hasVisualDefinition(
                            enemy
                        )
                ) {
                    depthEntries.add(
                        DepthRenderEntry(
                            depthY =
                                enemy.bounds.y,

                            render = {
                                enemySpriteRenderer.render(
                                    enemies =
                                        listOf(
                                            enemy
                                        ),

                                    camera =
                                        camera
                                )
                            }
                        )
                    )
                }
            }

        depthEntries
            .sortedByDescending {
                    entry ->
                entry.depthY
            }
            .forEach {
                    entry ->
                entry.render()
            }

        /*
         * Minions only carry the visuals.
         * Every weapon attack still originates from player-center gameplay
         * executors, so their attack FX is also emitted at player center.
         */
        carrierSnapshots
            .filter {
                it.slotIndex > 0 &&
                    it.attackActive
            }
            .forEach {
                    snapshot ->

                weaponCarrierRenderer
                    .renderAttackEffectAtPlayer(
                        snapshot =
                            snapshot,

                        playerCenterX =
                            player.centerX,

                        playerCenterY =
                            player.centerY,

                        camera =
                            camera
                    )
            }

        /*
         * PASS 3:
         * Projectile sprite'ları world depth'in üzerinde kalır.
         */
        projectileSpriteRenderer.render(
            projectiles =
                projectiles,

            camera =
                camera
        )

        /*
         * PASS 4:
         * Enemy health bar / condition / trait overlay'leri,
         * görseli olmayan enemy fallback'leri ve projectile
         * fallback'leri en üstte kalır.
         */
        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Filled
        )

        enemyManager.allEnemies
            .forEach {
                    enemy ->

                enemy.render(
                    shapeRenderer =
                        shapeRenderer,

                    renderBody =
                        !enemySpriteRenderer
                            .hasVisualDefinition(
                                enemy
                            )
                )
            }

        projectiles.forEach {
                projectile ->

            projectile.render(
                shapeRenderer =
                    shapeRenderer,

                renderBody =
                    !projectileSpriteRenderer
                        .hasVisualDefinition(
                            projectile
                        )
            )
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

        var x =
            0f

        while (
            x <=
            worldWidth
        ) {
            shapeRenderer.line(
                x,
                0f,
                x,
                worldHeight
            )

            x +=
                GRID_SIZE
        }

        var y =
            0f

        while (
            y <=
            worldHeight
        ) {
            shapeRenderer.line(
                0f,
                y,
                worldWidth,
                y
            )

            y +=
                GRID_SIZE
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

    fun dispose() {
        mapGroundTileRenderer
            .dispose()

        mapObstacleSpriteRenderer
            .dispose()

        characterSpriteRenderer
            .dispose()

        enemySpriteRenderer
            .dispose()

        projectileSpriteRenderer
            .dispose()

        weaponCarrierRenderer
            .dispose()
    }
}
