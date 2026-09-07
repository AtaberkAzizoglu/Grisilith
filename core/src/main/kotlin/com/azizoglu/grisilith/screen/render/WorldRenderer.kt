package com.azizoglu.grisilith.screen.render

import com.azizoglu.grisilith.chest.ChestWorldVisualManager
import com.azizoglu.grisilith.combat.condition.visual.ConditionVisualRenderer
import com.azizoglu.grisilith.economy.CurrencyPickupManager
import com.azizoglu.grisilith.character.visual.CharacterSpriteRenderer
import com.azizoglu.grisilith.character.visual.MountedCharacterSpriteRenderer
import com.azizoglu.grisilith.content.ContentId
import com.azizoglu.grisilith.enemy.EnemyManager
import com.azizoglu.grisilith.enemy.visual.EnemySpriteRenderer
import com.azizoglu.grisilith.map.MapObstacle
import com.azizoglu.grisilith.map.TroubleAtHomeDefenseRuntime
import com.azizoglu.grisilith.map.visual.MapObstacleSpriteRenderer
import com.azizoglu.grisilith.map.tile.MapGroundTileRenderer
import com.azizoglu.grisilith.config.GameMap
import com.azizoglu.grisilith.player.Player
import com.azizoglu.grisilith.summon.SummonedEntity
import com.azizoglu.grisilith.summon.visual.SummonSpriteRenderer
import com.azizoglu.grisilith.weapon.Projectile
import com.azizoglu.grisilith.weapon.visual.ProjectileSpriteRenderer
import com.azizoglu.grisilith.weapon.visual.carrier.WeaponCarrierRenderer
import com.azizoglu.grisilith.weapon.visual.attack.MeleeAttackVisualRenderer
import com.azizoglu.grisilith.weapon.visual.carrier.WeaponCarrierVisualSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.glutils.ShapeRenderer

class WorldRenderer(
    private val shapeRenderer: ShapeRenderer,
    private val camera: OrthographicCamera,
    private val gameMap: GameMap,
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

        /*
         * Normal character sheets use the 48 world-unit character standard.
         * Mounted character is taller/wider, so give its condition VFX the
         * mounted visual scale instead.
         */
        private const val PLAYER_CONDITION_VISUAL_WIDTH =
            48f

        private const val MOUNTED_CONDITION_VISUAL_WIDTH =
            86f

        private const val MOUNTED_HEALTH_BAR_WIDTH =
            72f

        private const val MOUNTED_HEALTH_BAR_HEIGHT =
            7f

        /*
         * Mounted sprite top is roughly 80-82 world units above Player
         * bounds.y. Put the bar safely above the rider's head.
         */
        private const val MOUNTED_VISUAL_TOP_OFFSET =
            94f
    }

    private val mapGroundTileRenderer =
        MapGroundTileRenderer(
            gameMap =
                gameMap,

            worldWidth =
                worldWidth,

            worldHeight =
                worldHeight
        )

    private val mapObstacleSpriteRenderer =
        MapObstacleSpriteRenderer(
            gameMap
        )

    private val characterSpriteRenderer =
        CharacterSpriteRenderer()

    private val mountedCharacterSpriteRenderer =
        MountedCharacterSpriteRenderer()

    private val enemySpriteRenderer =
        EnemySpriteRenderer()

    private val summonSpriteRenderer =
        SummonSpriteRenderer()

    private val projectileSpriteRenderer =
        ProjectileSpriteRenderer()

    private val weaponCarrierRenderer =
        WeaponCarrierRenderer()

    private val meleeAttackVisualRenderer =
        MeleeAttackVisualRenderer()

    private val conditionVisualRenderer =
        ConditionVisualRenderer()

    private val worldPickupSpriteRenderer =
        WorldPickupSpriteRenderer()

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

        phantomSteedActive:
        Boolean,

        enemyManager:
        EnemyManager,

        summons:
        List<SummonedEntity>,

        currencyPickupManager:
        CurrencyPickupManager,

        chestWorldVisualManager:
        ChestWorldVisualManager,

        projectiles:
        List<Projectile>,

        weaponCarrierVisualSystem:
        WeaponCarrierVisualSystem,

        troubleAtHomeDefenseRuntime:
        TroubleAtHomeDefenseRuntime? = null,

        showEnemyHealthBars: Boolean = true,

        renderGroundLayer:
        (() -> Unit)? = null,

        renderBehindEntities:
        ((ShapeRenderer) -> Unit)? = null
    ) {
        clearScreen()

        /*
         * PASS 0:
         * Selected Map ground layer.
         */
        mapGroundTileRenderer.render(
            camera =
                camera
        )

        mapObstacleSpriteRenderer.render(
            obstacles =
                mapObstacles.filter(MapObstacle::isGroundLayer),
            camera = camera
        )

        renderGroundLayer?.invoke()

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

            if (obstacle.isGroundLayer) {
                return@forEach
            }

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

        val shouldRenderMountedCharacter =
            phantomSteedActive &&
                mountedCharacterSpriteRenderer
                    .hasVisualDefinition(
                        selectedCharacterId
                    )

        /*
         * Mounted character uses a much taller visual than the normal
         * Player bounds. Player.render() also draws the normal health bar
         * directly above the normal bounds, which would cut through the
         * rider/horse sprite.
         *
         * Therefore, while mounted, skip Player.render() completely here.
         * A dedicated mounted health bar is rendered in PASS 4.
         */
        if (!shouldRenderMountedCharacter) {
            player.render(
                shapeRenderer =
                    shapeRenderer,

                renderBody =
                    !characterSpriteRenderer
                        .hasVisualDefinition(
                            selectedCharacterId
                        )
            )
        }

        shapeRenderer.end()

        /*
         * Coins and chests now use their actual pixel-art assets.
         * They are ground-layer visuals, intentionally rendered before
         * character/enemy Y-sorted entities.
         */
        worldPickupSpriteRenderer
            .renderCurrencies(
                pickups =
                    currencyPickupManager
                        .allPickups,

                camera =
                    camera
            )

        worldPickupSpriteRenderer
            .renderChests(
                chests =
                    chestWorldVisualManager
                        .allVisuals,

                camera =
                    camera
            )

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

            if (obstacle.isGroundLayer) {
                return@forEach
            }

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

        /*
         * Phantom Steed mounted sheets already contain the complete rider
         * silhouette. Drawing carried weapon sprites on top looks detached
         * and visually conflicts with the mounted art.
         *
         * This disables ONLY carrier visuals while mounted:
         * - slot 1 weapon sprite
         * - visual minions / their carried weapon sprites
         *
         * Weapon gameplay, attacks, projectiles and attack FX keep running.
         */
        val carrierSnapshots =
            if (
                shouldRenderMountedCharacter
            ) {
                emptyList()
            } else {
                weaponCarrierVisualSystem
                    .snapshots(
                        player
                    )
            }

        val playerCarrier =
            carrierSnapshots
                .firstOrNull {
                    it.slotIndex == 0
                }

        val hasNormalCharacterVisual =
            characterSpriteRenderer
                .hasVisualDefinition(
                    selectedCharacterId
                )

        val hasPlayerVisual =
            shouldRenderMountedCharacter ||
                hasNormalCharacterVisual

        if (
            hasPlayerVisual
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
                                        if (
                                            shouldRenderMountedCharacter
                                        ) {
                                            mountedCharacterSpriteRenderer
                                                .render(
                                                    player =
                                                        player,

                                                    characterId =
                                                        selectedCharacterId,

                                                    camera =
                                                        camera
                                                )
                                        } else {
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
                                    }
                                )
                        } else {
                            if (
                                shouldRenderMountedCharacter
                            ) {
                                mountedCharacterSpriteRenderer
                                    .render(
                                        player =
                                            player,

                                        characterId =
                                            selectedCharacterId,

                                        camera =
                                            camera
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

                        conditionVisualRenderer.renderGroundVisuals(
                            conditions = player.conditions,
                            centerX = player.centerX,
                            feetY = player.bounds.y,
                            targetVisualWidth =
                                if (shouldRenderMountedCharacter) {
                                    MOUNTED_CONDITION_VISUAL_WIDTH
                                } else {
                                    PLAYER_CONDITION_VISUAL_WIDTH
                                },
                            camera = camera
                        )
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

                                conditionVisualRenderer.renderGroundVisuals(
                                    conditions = enemy.conditions,
                                    centerX = enemy.centerX,
                                    feetY = enemy.bounds.y,
                                    targetVisualWidth = enemy.visualSize,
                                    camera = camera
                                )
                            }
                        )
                    )
                }
            }

        summons
            .filter {
                !it.isExpired
            }
            .forEach {
                    summon ->

                depthEntries.add(
                    DepthRenderEntry(
                        depthY =
                            summon.centerY -
                                summon.combatRadius,

                        render = {
                            summonSpriteRenderer
                                .render(
                                    summon =
                                        summon,

                                    camera =
                                        camera
                                )

                            conditionVisualRenderer.renderGroundVisuals(
                                conditions = summon.conditions,
                                centerX = summon.centerX,
                                feetY = summon.centerY - summon.combatRadius,
                                targetVisualWidth = summon.combatRadius * 2f,
                                camera = camera
                            )
                        }
                    )
                )
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
         * Completely separate melee FX layer.
         * Executor-generated events are centered on the actual target.
         */
        meleeAttackVisualRenderer
            .render(
                camera
            )

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
                            ),

                    renderHealthBar =
                        showEnemyHealthBars
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

        summons
            .filter { !it.isExpired }
            .forEach { it.renderAttackVisual(shapeRenderer) }

        if (
            shouldRenderMountedCharacter
        ) {
            renderMountedPlayerHealthBar(
                player =
                    player
            )
        }

        troubleAtHomeDefenseRuntime
            ?.renderHealthBar(
                shapeRenderer
            )

        shapeRenderer.end()

        // Condition symbols are a sprite pass above health bars.
        val playerSymbolY =
            if (shouldRenderMountedCharacter) {
                player.bounds.y + MOUNTED_VISUAL_TOP_OFFSET + MOUNTED_HEALTH_BAR_HEIGHT + 5f
            } else {
                player.bounds.y + 48f + 8f + 7f + 5f
            }

        conditionVisualRenderer.renderSymbols(
            conditions = player.conditions,
            centerX = player.centerX,
            symbolY = playerSymbolY,
            camera = camera
        )

        enemyManager.allEnemies
            .filter { !it.isDead }
            .forEach { enemy ->
                val enemyBarHeight = if (enemy.type == com.azizoglu.grisilith.enemy.EnemyType.FINAL_BOSS) 11f else 6f
                conditionVisualRenderer.renderSymbols(
                    conditions = enemy.conditions,
                    centerX = enemy.centerX,
                    symbolY = enemy.centerY + enemy.visualSize / 2f + 7f + enemyBarHeight + 5f,
                    camera = camera
                )
            }

        summons
            .filter { !it.isExpired }
            .forEach { summon ->
                conditionVisualRenderer.renderSymbols(
                    conditions = summon.conditions,
                    centerX = summon.centerX,
                    symbolY = summon.centerY + summon.combatRadius + 5f + 3f + 4f,
                    camera = camera
                )
            }
    }

    private fun renderMountedPlayerHealthBar(
        player: Player
    ) {
        /*
         * Keep bar width consistent with the normal player gameplay size,
         * but place it above the complete horse+rider sprite instead of
         * above the small Player hitbox.
         */
        val barWidth =
            MOUNTED_HEALTH_BAR_WIDTH

        val barHeight =
            MOUNTED_HEALTH_BAR_HEIGHT

        val barX =
            player.centerX -
                barWidth /
                2f

        val barY =
            player.bounds.y +
                MOUNTED_VISUAL_TOP_OFFSET

        shapeRenderer.color =
            Color.DARK_GRAY

        shapeRenderer.rect(
            barX,
            barY,
            barWidth,
            barHeight
        )

        shapeRenderer.color =
            Color.GREEN

        shapeRenderer.rect(
            barX,
            barY,
            barWidth *
                player.health.healthRatio,
            barHeight
        )
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

        mountedCharacterSpriteRenderer
            .dispose()

        enemySpriteRenderer
            .dispose()

        summonSpriteRenderer
            .dispose()

        projectileSpriteRenderer
            .dispose()

        weaponCarrierRenderer
            .dispose()

        meleeAttackVisualRenderer
            .dispose()

        conditionVisualRenderer
            .dispose()

        worldPickupSpriteRenderer
            .dispose()
    }
}
