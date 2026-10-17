package com.azizoglu.grisilith.screen

import kotlin.math.roundToInt

import com.azizoglu.grisilith.combat.damage.DamageNumberManager
import com.azizoglu.grisilith.combat.healing.HealingVisualManager
import com.azizoglu.grisilith.content.ConditionType
import com.azizoglu.grisilith.content.DamageType
import com.azizoglu.grisilith.content.ContentTag
import com.azizoglu.grisilith.combat.condition.ConditionApplication
import com.azizoglu.grisilith.combat.condition.ConditionSource
import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.enemy.EnemyManager
import com.azizoglu.grisilith.enemy.runtime.EnemyAdaptiveScalingRuntime
import com.azizoglu.grisilith.player.Player
import com.azizoglu.grisilith.weapon.Projectile
import com.azizoglu.grisilith.weapon.glyph.GlyphFeedbackRenderer
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.viewport.FitViewport
import com.azizoglu.grisilith.enemy.EnemySpawner
import com.azizoglu.grisilith.statistics.RunStatistics
import com.azizoglu.grisilith.statistics.RunDamageTypeTelemetry
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.azizoglu.grisilith.util.GameTimeController
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.azizoglu.grisilith.stage.StageController
import com.azizoglu.grisilith.enemy.EnemyType
import com.azizoglu.grisilith.stage.StageType
import com.azizoglu.grisilith.economy.CurrencyPickupManager
import com.azizoglu.grisilith.economy.GoldEconomyCalculator
import com.azizoglu.grisilith.economy.PickupRangeController
import com.azizoglu.grisilith.economy.SessionWallet
import com.azizoglu.grisilith.economy.MarketOffer
import com.azizoglu.grisilith.economy.MarketOfferGenerator
import com.azizoglu.grisilith.economy.MarketProductType
import com.azizoglu.grisilith.economy.MarketRerollPriceCalculator
import com.azizoglu.grisilith.economy.MarketSlot
import com.azizoglu.grisilith.GrisilithGame
import com.azizoglu.grisilith.config.GameDifficulty
import com.azizoglu.grisilith.config.GameMap
import com.azizoglu.grisilith.map.MapObstacle
import com.azizoglu.grisilith.map.MapObstacleGenerator
import com.azizoglu.grisilith.map.MapEnvironmentEffects
import com.azizoglu.grisilith.map.TroubleAtHomeDefenseRuntime
import com.azizoglu.grisilith.map.enemy.MapEnemyRuntimeContext
import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.ui.stat.StatSymbolRenderer
import com.azizoglu.grisilith.character.CharacterDefinition
import com.azizoglu.grisilith.weapon.runtime.WeaponInstance
import com.azizoglu.grisilith.weapon.runtime.WeaponInventory
import com.azizoglu.grisilith.screen.render.HudRenderer
import com.azizoglu.grisilith.screen.render.MarketRenderer
import com.azizoglu.grisilith.screen.render.RunSummaryRenderer
import com.azizoglu.grisilith.summary.RunScoreCalculator
import com.azizoglu.grisilith.summary.RunSummaryArmorEntry
import com.azizoglu.grisilith.summary.RunSummaryItemEntry
import com.azizoglu.grisilith.summary.RunSummarySnapshot
import com.azizoglu.grisilith.summary.RunSummaryTamgaEntry
import com.azizoglu.grisilith.summary.RunSummaryWeaponEntry
import com.azizoglu.grisilith.screen.controller.StatCardController
import com.azizoglu.grisilith.screen.render.WorldRenderer
import com.azizoglu.grisilith.weapon.runtime.WeaponRuntimeManager
import com.azizoglu.grisilith.weapon.runtime.WeaponInventoryResult
import com.azizoglu.grisilith.item.runtime.ItemInventory
import com.azizoglu.grisilith.item.runtime.ItemStatBonusSynchronizer
import com.azizoglu.grisilith.item.definition.DefaultItems
import com.azizoglu.grisilith.item.definition.ItemPoolEligibility
import com.azizoglu.grisilith.item.runtime.ItemInstance
import com.azizoglu.grisilith.item.runtime.ItemInventoryResult
import com.azizoglu.grisilith.item.runtime.ActiveItemEffects
import com.azizoglu.grisilith.weapon.definition.DefaultWeapons
import com.azizoglu.grisilith.screen.market.MarketInputController
import com.azizoglu.grisilith.screen.market.MarketInteractionState
import com.azizoglu.grisilith.economy.WeaponSalePriceCalculator
import com.azizoglu.grisilith.weapon.runtime.WeaponStatBonusSynchronizer
import com.badlogic.gdx.InputProcessor
import com.azizoglu.grisilith.chest.ChestDropCalculator
import com.azizoglu.grisilith.chest.ChestDropSegmentTracker
import com.azizoglu.grisilith.chest.ChestDropSource
import com.azizoglu.grisilith.chest.ChestPickupManager
import com.azizoglu.grisilith.chest.ChestWorldVisualManager
import com.azizoglu.grisilith.chest.ChestRewardQueue
import com.azizoglu.grisilith.chest.ChestRewardRoll
import com.azizoglu.grisilith.chest.ChestRewardRoller
import com.azizoglu.grisilith.chest.ChestRewardType
import com.azizoglu.grisilith.screen.controller.ChestRewardController
import com.azizoglu.grisilith.statistics.StatType
import com.azizoglu.grisilith.statistics.StatPresentationGroup
import com.azizoglu.grisilith.screen.pause.PauseAction
import com.azizoglu.grisilith.screen.pause.PauseInputController
import com.azizoglu.grisilith.screen.pause.PauseInteractionState
import com.azizoglu.grisilith.screen.render.PauseRenderer
import com.azizoglu.grisilith.preset.ResolvedPresetContent
import com.azizoglu.grisilith.armor.runtime.EquippedArmor
import com.azizoglu.grisilith.armor.runtime.ArmorStatBonusSynchronizer
import com.azizoglu.grisilith.tamga.runtime.TamgaRuntimeManager
import com.azizoglu.grisilith.tamga.runtime.active.ActiveTamgaEndReason
import com.azizoglu.grisilith.tamga.render.ActiveTamgaHudRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.MathUtils
import com.azizoglu.grisilith.ui.icon.ContentIconRenderer
import com.azizoglu.grisilith.ui.theme.ButtonVisualState
import com.azizoglu.grisilith.ui.theme.PanelVisualStyle
import com.azizoglu.grisilith.ui.theme.UiComponentRenderer
import com.azizoglu.grisilith.ui.typography.UiFontFactory
import com.azizoglu.grisilith.input.RunInputBindings
import com.azizoglu.grisilith.enemy.definition.EnemyTag
import com.azizoglu.grisilith.reward.RunRewardTracker
import com.azizoglu.grisilith.content.CollectionEncounterIds
import com.azizoglu.grisilith.profile.PlayerProfileRepository
import com.azizoglu.grisilith.statistics.career.CompletedRunStatistics
import com.azizoglu.grisilith.statistics.career.HighScoreRepository
import com.azizoglu.grisilith.screen.render.RunSummaryStatRenderer
import com.azizoglu.grisilith.screen.controller.RunSummaryStatInteractionController
import com.azizoglu.grisilith.summary.RunSummaryStatEntry
import com.azizoglu.grisilith.summary.StatContributionSource
import com.azizoglu.grisilith.summary.StatSourceContribution
import com.azizoglu.grisilith.armor.definition.DefaultArmors
import com.azizoglu.grisilith.tamga.definition.DefaultTamgas
import com.azizoglu.grisilith.settings.GameSettings
import com.azizoglu.grisilith.screen.summon.ChooseToSummonPopup
import com.azizoglu.grisilith.screen.glyph.ChooseGlyphSpellPopup
import com.azizoglu.grisilith.weapon.glyph.ChalkDustSpellChoice
import com.azizoglu.grisilith.weapon.summon.IvySummonChoice
import com.azizoglu.grisilith.summon.SummonManager
import com.azizoglu.grisilith.weapon.visual.carrier.WeaponCarrierVisualSystem
import com.azizoglu.grisilith.item.world.WorldItemRuntime
import com.azizoglu.grisilith.item.world.WorldItemRenderer
import com.azizoglu.grisilith.item.definition.ItemEffectId

class FirstScreen(
    private val game: GrisilithGame,
    private val difficulty: GameDifficulty,
    private val gameMap: GameMap,
    private val selectedCharacter:
    CharacterDefinition,

    private val selectedPreset:
    ResolvedPresetContent? = null
) : ScreenAdapter() {

    companion object {
        private const val CAMERA_WIDTH = 1280f
        private const val CAMERA_HEIGHT = 720f
        private const val DAMAGE_FEEDBACK_DURATION = 0.46f
    }

    private val worldWidth =
        gameMap.worldWidth

    private val worldHeight =
        gameMap.worldHeight

    private val shapeRenderer =
        ShapeRenderer()

    private val camera =
        OrthographicCamera()

    private var damageFeedbackRemaining = 0f
    private var damageFeedbackStrength = 0f

    private val worldRenderer =
        WorldRenderer(
            shapeRenderer = shapeRenderer,
            camera = camera,
            gameMap = gameMap,
            worldWidth = worldWidth,
            worldHeight = worldHeight,
            selectedCharacterId =
                selectedCharacter.id
        )

    private val viewport =
        FitViewport(
            CAMERA_WIDTH,
            CAMERA_HEIGHT,
            camera
        )

    private val playerStats =
        PlayerStats(
            initialBaseScores =
                selectedPreset
                    ?.preRunBaseStats
                    ?: selectedCharacter.baseStats,

            baseCritRatePercent =
                selectedCharacter
                    .baseCritRatePercent,

            baseCritDamagePercent =
                selectedCharacter
                    .baseCritDamagePercent,

            characterTraitId =
                selectedCharacter.trait.id,
            canEquipArmor = selectedCharacter.canEquipArmor,
            unarmoredBaseHealth = selectedCharacter.unarmoredBaseHealth,
            unarmoredBaseArmor = selectedCharacter.unarmoredBaseArmor,
            unarmoredBaseMagicResistance = selectedCharacter.unarmoredBaseMagicResistance
        ).apply {
            selectedPreset
                ?.let { preset ->
                    setGearBonuses(
                        preset.armorAndTamgaBonuses
                    )
                }
        }

    private val weaponInventory =
        WeaponInventory()

    /*
     * Enemy adaptive scaling başlangıç baseline'ını burada yakalar.
     * Character'ın başlangıç dağılımı ekstra difficulty üretmez;
     * run sırasında kazanılan güç enemy scaling'e kontrollü biçimde yansır.
     */
    init {
        EnemyAdaptiveScalingRuntime
            .bind(
                playerStats =
                    playerStats,

                weaponInventory =
                    weaponInventory
            )
    }

    private val weaponStatBonusSynchronizer =
        WeaponStatBonusSynchronizer(
            inventory =
                weaponInventory,

            playerStats =
                playerStats
        )

    private val player = Player(
        startX = worldWidth / 2f,
        startY = worldHeight / 2f,
        worldWidth = worldWidth,
        worldHeight = worldHeight,
        stats = playerStats
    )

    private val equippedArmor =
        EquippedArmor(selectedCharacter.canEquipArmor).apply {
            selectedPreset
                ?.armor?.takeIf { selectedCharacter.canEquipArmor }
                ?.let { armor ->
                    equip(
                        armor
                    )
                }
        }

    private val armorStatBonusSynchronizer =
        ArmorStatBonusSynchronizer(
            equippedArmor =
                equippedArmor,

            playerStats =
                playerStats
        )

    private val itemInventory =
        ItemInventory()

    private val worldItemRuntime =
        WorldItemRuntime(
            inventory = itemInventory,
            worldWidth = worldWidth,
            worldHeight = worldHeight
        )

    private val worldItemRenderer =
        WorldItemRenderer()

    private var portalStageCompletionRequested = false

    private val noLootTotemMiniBosses =
        mutableSetOf<Enemy>()

    private val itemStatSynchronizer =
        ItemStatBonusSynchronizer(
            inventory =
                itemInventory,

            playerStats =
                playerStats
        )

    private val stageController =
        StageController(
            /*
             * StageController süreleri merkezi Stage tablosundan çözer.
             * Bu değerler yalnızca eski constructor uyumluluğu içindir.
             */
            normalStageDurationSeconds =
                30f,

            miniBossStageDurationSeconds =
                90f,

            finalBossStageDurationSeconds =
                60f,

            maximumStage =
                if (gameMap == GameMap.TEST_MAP) 50 else difficulty.maximumStage
        )


    private val tamgaRuntimeManager =
        TamgaRuntimeManager(
            weaponInventory =
                weaponInventory,

            playerStats =
                playerStats,

            weaponTamga =
                selectedPreset
                    ?.weaponTamga,

            armorTamga =
                selectedPreset
                    ?.armorTamga,

            characterPreferredStats =
                selectedPreset
                    ?.character
                    ?.preferredMarketStats
                    ?: emptySet(),

            currentStageProvider = {
                stageController.currentStage
            }
        )

    init {
        tamgaRuntimeManager
            .attachPlayer(
                player
            )
    }

    private val weaponRuntimeManager =
        WeaponRuntimeManager(
            inventory =
                weaponInventory,

            playerStats =
                playerStats,

            tamgaRuntimeManager =
                tamgaRuntimeManager,

            characterTrait =
                selectedCharacter.trait,

            traitPlayer =
                player,

            currentStageProvider = {
                stageController.currentStage
            }
        )

    private val weaponCarrierVisualSystem =
        WeaponCarrierVisualSystem(
            inventory = weaponInventory,
            characterId = selectedCharacter.id.value
        )

    private var mapObstacles: List<MapObstacle> =
        MapObstacleGenerator.generate(
            gameMap = gameMap,
            playerStartX = player.centerX,
            playerStartY = player.centerY
        )

    private val blockingMapObstacles: List<MapObstacle>
        get() = mapObstacles.filter(MapObstacle::blocksMovement)

    private val projectiles = mutableListOf<Projectile>()

    private val summonManager =
        SummonManager(
            weaponInventory =
                weaponInventory,

            playerStats =
                playerStats,

            currentStageProvider = {
                stageController.currentStage
            },

            mapObstacles =
                blockingMapObstacles
        )

    private var troubleAtHomeDefenseRuntime =
        TroubleAtHomeDefenseRuntime(
            gameMap = gameMap,
            obstacles = mapObstacles
        )

    private val runStatistics = RunStatistics()

    private val gameTimeController =
        GameTimeController().apply {
            setGameSpeed(
                GameSettings.gameSpeed
            )
        }

    private val hudBatch = SpriteBatch()
    private val hudFont = UiFontFactory.createNumeric()
    private val uiBodyFont = UiFontFactory.createBody()
    private val onlineUi = com.azizoglu.grisilith.online.OnlineUi()
    private val summaryTitleFont = UiFontFactory.createDecorative()
    private val damageNumberManager =
        DamageNumberManager()


    private val enemyManager =
        EnemyManager()

    private val mapEnemyRuntimeContext =
        MapEnemyRuntimeContext(
            gameMap =
                gameMap
        )

    private val enemySpawner =
        EnemySpawner(
            worldWidth =
                worldWidth,

            worldHeight =
                worldHeight,

            mapEnemyContext =
                mapEnemyRuntimeContext
        )

    private var isRunSummaryOpen = false

    private var runEndTransitionElapsed = -1f
    private var summaryScoreElapsed = 0f

    private var summaryPreviousInputProcessor: InputProcessor? = null

    private val isRunEndTransitionActive: Boolean
        get() = runEndTransitionElapsed >= 0f &&
            runEndTransitionElapsed < RunSummaryRenderer.TRANSITION_DURATION

    private var isMarketOpen = false

    private var isPauseMenuOpen =
        false

    private enum class TestDeathMode { END_RUN, RESTART_STAGE, IMMORTAL }

    private var isTestSettingsOpen = false
    private var isTestRunFrozen = false
    private var isTestEnemySpawningEnabled = false
    private var testEnemyTag = EnemyTag.BEAST
    private var testDummySelected = false
    private var testDeathMode = TestDeathMode.END_RUN
    private var testObstaclesEnabled = false
    private var testItemIndex = 0
    private var testWeaponIndex = 0
    private var testDummyEnemy: Enemy? = null
    private var isTestResetConfirmationOpen = false
    private var isTestStatEditorOpen = false
    private var testCatalogKind: String? = null
    private var testCatalogOffset = 0
    private var isTestStageListOpen = false
    private var testStageListOffset = 0
    private var draggedTestWeaponSlot: Int? = null
    private var draggedTestItemRuntimeId: String? = null
    private var testPointerWasDown = false
    private var isTestInfiniteStage = false
    private var isTestOwnedItemsOpen = false
    private var testItemRarityFilter: com.azizoglu.grisilith.item.definition.ItemRarity? = null
    private enum class TestWeaponFilter { ALL, MELEE, RANGED, SPELL_FOCUS }
    private var testWeaponFilter = TestWeaponFilter.ALL
    private var testCatalogScroll = 0f
    private var testPressedAtMillis = 0L
    private var testCatalogDragLastY: Float? = null
    private val testMapScrollProcessor = object : InputAdapter() {
        override fun scrolled(amountX: Float, amountY: Float): Boolean {
            if (gameMap != GameMap.TEST_MAP || !isTestSettingsOpen || (testCatalogKind == null && !isTestOwnedItemsOpen)) return false
            val entryCount = if (isTestOwnedItemsOpen) itemInventory.allItems().size else filteredTestCatalogEntries().size
            val rowHeight = if (isTestOwnedItemsOpen) 34f else 36f
            val maximum = ((entryCount - 10).coerceAtLeast(0) * rowHeight)
            testCatalogScroll = (testCatalogScroll + amountY * rowHeight * 2f).coerceIn(0f, maximum)
            return true
        }
    }

    private var hasInitializedRun =
        false

    private var marketSlots =
        emptyList<MarketSlot>()

    private var lockedSlotsForNextMarket:
        List<MarketSlot?> =
        List(5) {
            null
        }

    private var marketRerollsUsed = 0

    private var itemScrollOffset = 0f

    private var previousInputProcessor:
        InputProcessor? = null

    private var hasCommittedCareerStatistics =
        false

    private var onlineRun: com.azizoglu.grisilith.online.OnlineRun? = null

    private var remainingRunChestDiscards =
        2

    private val sessionWallet =
        SessionWallet()

    private val runRewardTracker =
        RunRewardTracker(
            difficulty =
                difficulty
        )

    private val finalBossRewardId =
        "map:${gameMap.name.lowercase()}"

    private val currencyPickupManager =
        CurrencyPickupManager()

    /*
     * Shared by coins AND chests. Future items can modify this controller
     * without changing either pickup implementation.
     */
    private val pickupRangeController =
        PickupRangeController()

    init {
        RunDamageTypeTelemetry.bind(runStatistics)

        ActiveItemEffects.bind(
            inventory = itemInventory,
            weaponInventory = weaponInventory,
            playerStats = playerStats,
            player = player,
            enemyProvider = { enemyManager.allEnemies },
            summonProvider = { summonManager.activeSummons },
            currentStageProvider = { stageController.currentStage },
            bountyCandidateProvider = {
                val eligibleTypes =
                    when (stageController.currentStageType) {
                        StageType.NORMAL ->
                            listOf(EnemyType.NORMAL)

                        StageType.MINI_BOSS ->
                            listOf(
                                EnemyType.NORMAL,
                                EnemyType.MINI_BOSS
                            )

                        StageType.FINAL_BOSS ->
                            listOf(
                                EnemyType.NORMAL,
                                EnemyType.FINAL_BOSS
                            )
                    }

                eligibleTypes.flatMap { enemyType ->
                    mapEnemyRuntimeContext.registry
                        .eligibleKinds(
                            enemyTags =
                                mapEnemyRuntimeContext
                                    .profile
                                    .enemyTags,
                            enemyType = enemyType
                        )
                        .map { definition ->
                            ActiveItemEffects.BountyCandidate(
                                kindId = definition.id,
                                displayName = definition.displayName,
                                enemyType = enemyType
                            )
                        }
                }
            },
            goldProvider = { sessionWallet.goldCoins },
            mainStatProvider = {
                listOf(
                    StatType.STRENGTH,
                    StatType.DEXTERITY,
                    StatType.WISDOM
                ).firstOrNull {
                    it in selectedCharacter.preferredMarketStats
                } ?: StatType.STRENGTH
            },
            tamgaCountProvider = {
                listOfNotNull(
                    selectedPreset?.weaponTamga,
                    selectedPreset?.armorTamga
                ).size
            }
        )
    }

    private val chestPickupManager =
        ChestPickupManager()

    private val chestWorldVisualManager =
        ChestWorldVisualManager()

    private var currentStageChestsCollected =
        0

    private var lastCurrencyTransferStage =
        stageController.currentStage


    private val chestRewardQueue =
        ChestRewardQueue()

    private val chestDropSegmentTracker =
        ChestDropSegmentTracker()


    private val hudRenderer =
        HudRenderer(
            camera = camera,
            cameraWidth = CAMERA_WIDTH,
            cameraHeight = CAMERA_HEIGHT,
            shapeRenderer = shapeRenderer,
            batch = hudBatch,
            font = hudFont,
            player = player,
            stageController = stageController,
            sessionWallet = sessionWallet,
            runRewardTracker =
                runRewardTracker,
            currencyPickupManager =
                currencyPickupManager,
            currentStageChestCollectedProvider = {
                currentStageChestsCollected
            },
            activeWorldChestCountProvider = {
                chestWorldVisualManager
                    .allVisuals
                    .size
            },
            gameTimeController =
                gameTimeController,
            weaponInventory =
                weaponInventory,
            weaponRuntimeManager =
                weaponRuntimeManager,
            summonManager =
                summonManager
        )

    private val glyphFeedbackRenderer =
        GlyphFeedbackRenderer(
            camera =
                camera,

            shapeRenderer =
                shapeRenderer,

            batch =
                hudBatch,

            font =
                hudFont
        )

    private val activeTamgaHudRenderer =
        ActiveTamgaHudRenderer(
            camera = camera,
            cameraWidth = CAMERA_WIDTH,
            cameraHeight = CAMERA_HEIGHT,
            shapeRenderer = shapeRenderer,
            batch = hudBatch,
            font = hudFont
        )

    private val runSummaryRenderer =
        RunSummaryRenderer(
            camera =
                camera,

            cameraWidth =
                CAMERA_WIDTH,

            cameraHeight =
                CAMERA_HEIGHT,

            viewport =
                viewport,

            shapeRenderer =
                shapeRenderer,

            batch =
                hudBatch,

            font =
                uiBodyFont,

            titleFont =
                summaryTitleFont
        )

    private val runSummaryInputProcessor = object : InputAdapter() {
        private var dragLastY: Float? = null

        override fun scrolled(amountX: Float, amountY: Float): Boolean {
            if (!isRunSummaryOpen) return false
            val pointer = Vector2(Gdx.input.x.toFloat(), Gdx.input.y.toFloat())
            viewport.unproject(pointer)
            if (!runSummaryRenderer.itemViewport().contains(pointer)) return false
            runSummaryRenderer.scrollItems(amountY * 42f, currentRunSummarySnapshot().items.size)
            return true
        }

        override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
            if (!isRunSummaryOpen || button != Input.Buttons.LEFT) return false
            val world = Vector2(screenX.toFloat(), screenY.toFloat())
            viewport.unproject(world)
            if (!runSummaryRenderer.itemViewport().contains(world)) return false
            dragLastY = world.y
            return true
        }

        override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
            val previousY = dragLastY ?: return false
            val world = Vector2(screenX.toFloat(), screenY.toFloat())
            viewport.unproject(world)
            runSummaryRenderer.scrollItems(world.y - previousY, currentRunSummarySnapshot().items.size)
            dragLastY = world.y
            return true
        }

        override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
            val wasDragging = dragLastY != null
            dragLastY = null
            return wasDragging
        }
    }

    private val runSummaryStatRenderer =
        RunSummaryStatRenderer(
            camera =
                camera,

            viewport =
                viewport,

            shapeRenderer =
                shapeRenderer,

            batch =
                hudBatch,

            font =
                hudFont
        )

    private val runSummaryStatInteractionController =
        RunSummaryStatInteractionController(
            viewport =
                viewport,

            renderer =
                runSummaryStatRenderer,

            snapshotProvider = {
                currentRunSummarySnapshot()
            }
        )

    private val marketInteractionState =
        MarketInteractionState()

    private val marketRenderer =
        MarketRenderer(
            camera =
                camera,

            cameraWidth =
                CAMERA_WIDTH,

            cameraHeight =
                CAMERA_HEIGHT,

            batch =
                hudBatch,

            font =
                uiBodyFont,

            sessionWallet =
                sessionWallet,

            stageController =
                stageController,

            selectedCharacter =
                selectedCharacter,

            playerStats =
                playerStats,

            weaponInventory =
                weaponInventory,

            itemInventory =
                itemInventory,

            runGrisilithCoinsProvider = {
                runRewardTracker.grisilithCoinsEarned
            }
        )

    private val marketInputController =
        MarketInputController(
            viewport =
                viewport,

            layoutProvider = {
                marketRenderer
                    .currentLayout()
            },

            interactionState =
                marketInteractionState,

            itemScrollPixelsProvider = {
                itemScrollOffset
            },

            groupedItemCountProvider = {
                itemInventory.allItems().distinctBy { it.definition.id }.size
            }
        )

    private val pauseInteractionState =
        PauseInteractionState()

    private val pauseRenderer =
        PauseRenderer(
            camera =
                camera,

            cameraWidth =
                CAMERA_WIDTH,

            cameraHeight =
                CAMERA_HEIGHT,

            batch =
                hudBatch,

            font =
                uiBodyFont,

            selectedCharacter =
                selectedCharacter,

            playerStats =
                playerStats,

            weaponInventory =
                weaponInventory,

            itemInventory =
                itemInventory,

            equippedArmor =
                equippedArmor,

            weaponTamga =
                selectedPreset?.weaponTamga,

            armorTamga =
                selectedPreset?.armorTamga
        )

    private val pauseWeaponInventoryController = com.azizoglu.grisilith.screen.pause.PauseWeaponInventoryController(
        weaponInventory, pauseInteractionState, ::synchronizeWeaponInventoryEffects
    )

    private val pauseInputController =
        PauseInputController(
            viewport =
                viewport,

            layoutProvider = {
                pauseRenderer
                    .currentLayout()
            },

            interactionState =
                pauseInteractionState,

            weaponExistsAt = {
                    slotIndex ->

                weaponInventory
                    .weaponAt(
                        slotIndex
                    ) !=
                    null
            },

            groupedItemCountProvider = {
                pauseRenderer
                    .groupedItemCount()
            },
            onWeaponDrop = pauseWeaponInventoryController::drop,
            onConfirmWeaponMerge = pauseWeaponInventoryController::confirm
        )

    private var pausePreviousInputProcessor: InputProcessor? = null

    private val statCardController =
        StatCardController(
            player =
                player,

            playerStats =
                playerStats,

            stageController =
                stageController,

            enemySpawner =
                enemySpawner,

            viewport =
                viewport,

            camera =
                camera,

            cameraWidth =
                CAMERA_WIDTH,

            cameraHeight =
                CAMERA_HEIGHT,

            shapeRenderer =
                shapeRenderer,

            batch =
                hudBatch,

            font =
                hudFont,

            characterNameProvider = {
                selectedCharacter
                    .displayName
            },

            onOpenMarket = {
                openMarket()
            }
        )

    private fun currentRunSummaryStats():
        List<RunSummaryStatEntry> {

        return StatType.grouped
            .map { statType ->

                /*
                 * PlayerStats totalScore değerini RUN_MAXIMUM_SCORE
                 * sınırında clamp eder.
                 *
                 * Kaynakların ham toplamı 50'yi aşarsa detail toplamı
                 * görünen total ile eşleşmelidir. Bu nedenle son katkı
                 * effective total üzerinden düzeltilir.
                 */
                val total =
                    playerStats
                        .totalScore(
                            statType
                        )

                val base =
                    playerStats
                        .baseScore(
                            statType
                        )

                val gear =
                    playerStats
                        .gearBonus(
                            statType
                        )

                val weapon =
                    playerStats
                        .weaponBonus(
                            statType
                        )

                val item =
                    playerStats
                        .itemBonus(
                            statType
                        )

                val run =
                    playerStats
                        .runBonus(
                            statType
                        )

                val characterTrait =
                    playerStats
                        .characterTraitBonus(
                            statType
                        )

                val rawContributions =
                    listOf(
                        StatContributionSource
                            .PRESET_OR_BASE to
                            base,

                        StatContributionSource
                            .STAGE_REWARDS to
                            run,

                        StatContributionSource
                            .ITEMS to
                            item,

                        StatContributionSource
                            .WEAPONS to
                            weapon,

                        StatContributionSource
                            .CHARACTER_TRAIT to
                            characterTrait,

                        StatContributionSource
                            .ARMOR_AND_TAMGAS to
                            gear
                    )

                /*
                 * Genellikle rawTotal == total olur.
                 * Stat 50 cap'ine ulaştığında fazla değerleri
                 * OTHER altında negatif correction olarak göstermek
                 * yerine katkıları sırayla görünen total içinde clamp
                 * ediyoruz.
                 */
                var remaining =
                    total

                val effectiveContributions =
                    rawContributions
                        .map {
                                sourceAmount ->

                            val source =
                                sourceAmount.first

                            val requestedAmount =
                                sourceAmount.second
                                    .coerceAtLeast(
                                        0
                                    )

                            val effectiveAmount =
                                requestedAmount
                                    .coerceAtMost(
                                        remaining
                                    )

                            remaining -=
                                effectiveAmount

                            StatSourceContribution(
                                source =
                                    source,

                                amount =
                                    effectiveAmount
                            )
                        }
                        .toMutableList()

                if (
                    remaining >
                    0
                ) {
                    effectiveContributions.add(
                        StatSourceContribution(
                            source =
                                StatContributionSource
                                    .OTHER,

                            amount =
                                remaining
                        )
                    )
                }

                RunSummaryStatEntry(
                    statType =
                        statType,

                    label =
                        statType
                            .displayName,

                    totalValue =
                        total,

                    contributions =
                        effectiveContributions
                )
            }
    }

    private fun currentRunSummarySnapshot():
        RunSummarySnapshot {

        val weaponEntries =
            weaponInventory
                .allWeapons()
                .map { weapon ->

                    RunSummaryWeaponEntry(
                        definitionId =
                            weapon.definition.id
                                .toString(),

                        displayName =
                            weapon.displayName,

                        description =
                            com.azizoglu.grisilith.weapon.definition.WeaponPresentation.hover(weapon),

                        level =
                            weapon.level,

                        damageDealt =
                            (
                                runStatistics
                                    .weaponDamageStatistics[
                                    weapon.displayName
                                ]
                                    ?: 0L
                                )
                                .toFloat()
                    )
                }

        val equippedWeaponNames =
            weaponEntries
                .map {
                    it.displayName
                }
                .toSet()

        val otherDamageSources =
            runStatistics
                .weaponDamageStatistics
                .filterKeys { sourceName ->

                    sourceName !in
                        equippedWeaponNames
                }
                .mapValues {
                    it.value.toFloat()
                }

        val armorDefinition =
            equippedArmor.definition

        val armorEntry =
            RunSummaryArmorEntry(
                definitionId =
                    armorDefinition
                        ?.id
                        ?.toString(),

                displayName =
                    armorDefinition
                        ?.displayName
                        ?: "No Armor",

                description =
                    armorDefinition
                        ?.description
                        ?: "No armor is equipped.",

                /*
                 * ArmorDefinition içinde tür etiketi için public
                 * alan bulunmadığından bu aşamada güvenli genel etiket.
                 */
                armorTypeLabel =
                    if (
                        armorDefinition !=
                        null
                    ) {
                        "Equipped Armor"
                    } else {
                        "Empty Slot"
                    }
            )

        val tamgaEntries =
            listOf(
                RunSummaryTamgaEntry(
                    slotLabel =
                        "Weapon",

                    definitionId =
                        selectedPreset
                            ?.weaponTamga
                            ?.id
                            ?.toString(),

                    displayName =
                        selectedPreset
                            ?.weaponTamga
                            ?.displayName
                            ?: "No Weapon Tamga",

                    description =
                        selectedPreset
                            ?.weaponTamga
                            ?.description
                            ?: "No weapon Tamga is equipped."
                ),

                RunSummaryTamgaEntry(
                    slotLabel =
                        "Armor",

                    definitionId =
                        selectedPreset
                            ?.armorTamga
                            ?.id
                            ?.toString(),

                    displayName =
                        selectedPreset
                            ?.armorTamga
                            ?.displayName
                            ?: "No Armor Tamga",

                    description =
                        selectedPreset
                            ?.armorTamga
                            ?.description
                            ?: "No armor Tamga is equipped."
                )
            )

        val itemEntries =
            itemInventory
                .allItems()
                .groupBy {
                    it.definition.id
                }
                .values
                .mapNotNull { groupedItems ->

                    val firstItem =
                        groupedItems
                            .firstOrNull()
                            ?: return@mapNotNull null

                    RunSummaryItemEntry(
                        definitionId =
                            firstItem.definition.id
                                .toString(),

                        displayName =
                            firstItem.definition
                                .displayName,

                        description =
                            firstItem.definition
                                .description,

                        count =
                            groupedItems.size
                    )
                }
                .sortedBy {
                    it.displayName
                }

        val score = currentRunScore()

        return RunSummarySnapshot(
            wasSuccessful =
                stageController
                    .isRunComplete,

            difficultyLabel =
                difficulty
                    .displayName,

            mapName =
                gameMap
                    .displayName,

            characterName =
                selectedCharacter
                    .displayName,

            stageReached =
                displayedStage(),

            formattedRunTime =
                formatGameTime(),

            enemiesKilled =
                runStatistics
                    .totalEnemiesKilled,

            totalDamageDealt =
                runStatistics
                    .totalDamageDealt
                    .toFloat(),

            totalDamageTaken =
                runStatistics
                    .totalDamageTaken
                    .toFloat(),

            totalDamageReduced =
                runStatistics
                    .totalDamageReduced
                    .toFloat(),

            damageDealtByType =
                runStatistics.damageDealtTypeStatistics.mapValues { it.value.toFloat() },

            damageTakenByType =
                runStatistics.damageTakenTypeStatistics.mapValues { it.value.toFloat() },

            damageReducedByType =
                runStatistics.damageReducedTypeStatistics.mapValues { it.value.toFloat() },

            goldCollected =
                runStatistics
                    .totalGoldCollected,

            grisilithCoinsEarned =
                runRewardTracker
                    .grisilithCoinsEarned,

            chestsOpened =
                runStatistics
                    .totalChestsOpened,

            score = score,
            enemyScore = currentEnemyScore(),
            scoreBeforeStageBonus = currentRunScoreForStage(0),
            scoreStageMultiplier = if(gameMap==GameMap.TEST_MAP)1f else RunScoreCalculator.stageMultiplier(difficulty,displayedStage()),
            scoreDifficultyMultiplier = if(gameMap==GameMap.TEST_MAP)1f else RunScoreCalculator.difficultyMultiplier(difficulty),

            stats =
                currentRunSummaryStats(),

            weapons =
                weaponEntries,

            armor =
                armorEntry,

            tamgas =
                tamgaEntries,

            items =
                itemEntries,

            otherDamageSources =
                otherDamageSources
        )
    }



    private val chestRewardController =
        ChestRewardController(
            rewardQueue =
                chestRewardQueue,

            currentStageProvider = {
                stageController
                    .currentStage
            },

            luckScoreProvider = {
                playerStats
                    .luckScore()
            },

            preferredStatTypesProvider = {
                selectedCharacter
                    .preferredMarketStats
            },

            availableStatTypesProvider = {
                StatType.alphabetical
                    .filter {
                            statType ->

                        playerStats
                            .totalScore(
                                statType
                            ) <
                            playerStats.maximumScore(
                                statType
                            )
                    }
            },

            availableItemDefinitionsProvider = {
                val ownedItemIds =
                    ownedItemDefinitionIds()

                DefaultItems.all
                    .filter { it.effectId != ItemEffectId.BROKEN_HEART }
                    .filter {
                            item ->

                        ItemPoolEligibility.isEligible(
                            item = item,
                            ownedItemIds = ownedItemIds
                        )
                    }
                    .filter(itemInventory::canAddDefinition)
            },

            camera =
                camera,

            cameraWidth =
                CAMERA_WIDTH,

            cameraHeight =
                CAMERA_HEIGHT,

            viewport =
                viewport,

            shapeRenderer =
                shapeRenderer,

            batch =
                hudBatch,

            font =
                hudFont,

            playerStats =
                playerStats,

            onChestCompleted = {
                runStatistics
                    .recordChestOpened()
            },

            remainingRunDiscardCountProvider = {
                remainingRunChestDiscards + itemInventory.countByDefinitionId(DefaultItems.discardVoucher.id)
            },

            onRunDiscardUsed = {
                if (remainingRunChestDiscards > 0) remainingRunChestDiscards--
                else itemInventory.consumeOneDefinition(DefaultItems.discardVoucher.id)
            },

            onApplyReward = {
                    reward ->

                applyChestReward(
                    reward
                )
            },

            onAllRewardsCollected = {
                    shouldOpenMarketAfterStatSelection ->

                statCardController
                    .beginSelection(
                        shouldOpenMarketAfterSelection =
                            shouldOpenMarketAfterStatSelection
                    )
            }
        )


    private val chooseGlyphSpellPopup =
        ChooseGlyphSpellPopup(
            camera = camera,
            viewport = viewport,
            shapeRenderer = shapeRenderer,
            batch = hudBatch,
            font = uiBodyFont
        )

    private val chooseToSummonPopup =
        ChooseToSummonPopup(
            camera =
                camera,

            viewport =
                viewport,

            shapeRenderer =
                shapeRenderer,

            batch =
                hudBatch,

            font =
                uiBodyFont
        )

    override fun show() {
        viewport.apply()

        if (isPauseMenuOpen) {
            pausePreviousInputProcessor = Gdx.input.inputProcessor
            Gdx.input.inputProcessor = pauseInputController
        } else if (gameMap == GameMap.TEST_MAP && Gdx.input.inputProcessor == null) {
            Gdx.input.inputProcessor = testMapScrollProcessor
        }

        camera.position.set(
            player.centerX,
            player.centerY,
            0f
        )

        if (!hasInitializedRun) {

            hasInitializedRun =
                true

            HighScoreRepository
                .recordRunStarted()

            PlayerProfileRepository
                .markMapEncountered(
                    CollectionEncounterIds.map(
                        gameMap
                    )
                )

            initializeStartingLoadout()

            if (gameMap != GameMap.TEST_MAP) {
                onlineRun = game.online?.begin(
                    gameMap, difficulty, selectedCharacter.id.value,
                    com.azizoglu.grisilith.online.OnlineProtocol.build(currentRunSummarySnapshot())
                )
            }

            recordCurrentEnemyEncounters()
        }

        camera.update()
    }



    private fun initializeStartingLoadout() {
        val preset =
            selectedPreset

        if (
            preset == null
        ) {
            /*
             * Legacy Character Selection akışı geçici
             * olarak korunuyor.
             */
            weaponInventory.addWeapon(
                WeaponInstance(
                    definition =
                        DefaultWeapons.longbow,

                    level =
                        2
                )
            )

            armorStatBonusSynchronizer
                .invalidate()

            armorStatBonusSynchronizer
                .synchronize()

            synchronizeWeaponInventoryEffects()

            player
                .synchronizeMaximumHealthWithStats(
                    healAddedHealth =
                        true
                )

            return
        }

        weaponInventory.addWeapon(
            WeaponInstance(
                definition =
                    preset.weapon,

                level =
                    WeaponInstance.MINIMUM_LEVEL
            )
        )

        preset.item
            ?.let { item ->
                val result = itemInventory.addItem(
                    ItemInstance(
                        definition =
                            item
                    )
                )

                if (result == ItemInventoryResult.ADDED) {
                    ActiveItemEffects.applyAcquisition(item)
                }
            }

        armorStatBonusSynchronizer
            .invalidate()

        armorStatBonusSynchronizer
            .synchronize()

        weaponStatBonusSynchronizer
            .invalidate()

        weaponStatBonusSynchronizer
            .synchronize()

        itemStatSynchronizer
            .synchronize()

        pickupRangeController.setMultiplier(
            ActiveItemEffects.pickupRangeMultiplier()
        )

        player
            .synchronizeMaximumHealthWithStats(
                healAddedHealth =
                    true
            )

        tamgaRuntimeManager.reset()

        weaponRuntimeManager
            .resetAllTimers()
    }

    override fun render(
        delta: Float
    ) {
        val healthBeforeFrame = player.health.currentHealth
        damageFeedbackRemaining =
            (damageFeedbackRemaining - delta.coerceAtLeast(0f)).coerceAtLeast(0f)

        if (onlineRun?.isPreparing != true) {
            handleGameControls(delta)
        }

        if (gameMap == GameMap.TEST_MAP) {
            handleTestMapDeathPolicy()
            synchronizeTestDummyVisibility()
            testDummyEnemy?.takeIf { !it.isDead }?.health?.let { health ->
                health.restoreCurrentHealth(health.maximumHealth)
            }
        }

        if (isRunSummaryOpen) {
            runSummaryStatInteractionController
                .update()
        }

        chestRewardController
            .update(
                delta
            )

        statCardController
            .update(
                delta
            )

        val runHasEnded = player.health.isDead || stageController.isRunComplete

        if (runHasEnded && runEndTransitionElapsed < 0f && !isRunSummaryOpen) {

            runRewardTracker.commitToProfile()
            commitCareerStatistics()
            onlineRun?.let { run ->
                game.online?.finish(run, com.azizoglu.grisilith.online.OnlineProtocol.result(
                    currentRunSummarySnapshot(), runStatistics,
                    gameTimeController.elapsedGameTimeSeconds.toInt()
                ))
            }
            runEndTransitionElapsed = 0f
        }

        if (isRunEndTransitionActive) {
            runEndTransitionElapsed = (runEndTransitionElapsed + delta.coerceAtLeast(0f))
                .coerceAtMost(RunSummaryRenderer.TRANSITION_DURATION)
            if (runEndTransitionElapsed >= RunSummaryRenderer.SUMMARY_REVEAL_START && !isRunSummaryOpen) {
                isRunSummaryOpen = true
                summaryPreviousInputProcessor = Gdx.input.inputProcessor
                Gdx.input.inputProcessor = runSummaryInputProcessor
            }
        }

        openPendingChalkDustSpellChoiceIfReady()
        if(isRunSummaryOpen && runEndTransitionElapsed>=RunSummaryRenderer.TRANSITION_DURATION) {
            summaryScoreElapsed=(summaryScoreElapsed+(delta.takeIf { it.isFinite() }?.coerceAtLeast(0f) ?: 0f))
                .coerceAtMost(com.azizoglu.grisilith.summary.ScoreReveal.TOTAL_SECONDS)
        }
        openPendingIvySummonChoiceIfReady()

        val gameplayStopped =
            onlineRun?.isPreparing == true ||
                runHasEnded ||
                chestRewardController.isOpen ||
                statCardController.isOpen ||
                chooseToSummonPopup.isOpen ||
                chooseGlyphSpellPopup.isOpen ||
                isMarketOpen ||
                isPauseMenuOpen ||
                isTestSettingsOpen ||
                isTestRunFrozen ||
                isRunSummaryOpen

        val gameDelta =
            if (gameplayStopped) {
                0f
            } else {
                gameTimeController
                    .calculateGameDelta(delta)
            }

        if (
            !gameTimeController.isPaused &&
            !gameplayStopped
        ) {
            updateGame(gameDelta)

            /*
             * The simulation delta is deliberately capped to prevent a
             * single slow frame from teleporting entities. Ability cooldowns
             * are clocks, however: with a crowded screen they must still
             * advance in real time instead of appearing frozen.
             */
            val uncappedCooldownDelta =
                delta.takeIf { it.isFinite() }
                    ?.coerceIn(0f, 0.5f)
                    ?.times(gameTimeController.gameSpeed)
                    ?: 0f
            weaponRuntimeManager.reduceAllAbilityCooldowns(
                (uncappedCooldownDelta - gameDelta).coerceAtLeast(0f)
            )
        }

        val damageTakenThisFrame = healthBeforeFrame - player.health.currentHealth
        if (damageTakenThisFrame > 0) {
            triggerDamageFeedback(damageTakenThisFrame)
            updateCamera()
        }

        renderGame()
    }

    private fun currentEnemyScore(): Long = RunScoreCalculator.enemyScore(gameMap,
        runStatistics.totalNormalEnemiesKilled,runStatistics.totalUniqueEnemiesKilled,
        runStatistics.totalBossesDefeated,runStatistics.totalFinalBossesDefeated)

    private fun currentRunScore(): Long = currentRunScoreForStage(displayedStage())

    private fun currentRunScoreForStage(stage: Int): Long = RunScoreCalculator.calculate(
        map = gameMap,
        difficulty = difficulty,
        normalEnemiesKilled = runStatistics.totalNormalEnemiesKilled,
        uniqueEnemiesKilled = runStatistics.totalUniqueEnemiesKilled,
        miniBossesDefeated = runStatistics.totalBossesDefeated,
        finalBossesDefeated = runStatistics.totalFinalBossesDefeated,
        stageReached = stage
    )

    private fun commitCareerStatistics() {

        if (
            hasCommittedCareerStatistics
        ) {
            return
        }

        hasCommittedCareerStatistics =
            true

        HighScoreRepository
            .commitCompletedRun(
                CompletedRunStatistics(
                    wasSuccessful =
                        stageController
                            .isRunComplete,

                    durationSeconds =
                        gameTimeController
                            .elapsedGameTimeSeconds
                            .toLong()
                            .coerceAtLeast(
                                0L
                            ),

                    enemiesKilled =
                        runStatistics
                            .totalEnemiesKilled,

                    bossesDefeated =
                        runStatistics
                            .totalBossesDefeated,

                    finalBossesDefeated =
                        runStatistics
                            .totalFinalBossesDefeated,

                    damageDealt =
                        runStatistics
                            .totalDamageDealt,

                    damageTaken =
                        runStatistics
                            .totalDamageTaken,

                    goldCollected =
                        runStatistics
                            .totalGoldCollected,

                    goldSpent =
                        runStatistics
                            .totalGoldSpent,

                    grisilithCoinsEarned =
                        runRewardTracker
                            .grisilithCoinsEarned,
                    score = currentRunScore()
                )
            )
    }

    private fun openPendingChalkDustSpellChoiceIfReady() {
        if (
            chooseGlyphSpellPopup.isOpen ||
            chooseToSummonPopup.isOpen ||
            chooseGlyphSpellPopup.isOpen ||
            player.health.isDead ||
            stageController.isRunComplete ||
            chestRewardController.isOpen ||
            statCardController.isOpen ||
            isMarketOpen ||
            isPauseMenuOpen ||
            isRunSummaryOpen
        ) return

        val pendingWeapon = weaponInventory.allWeapons().firstOrNull { weapon ->
            weapon.definition.id.value == "WPN_ENCHANTED_CHALK_DUST" &&
                weapon.hasWeaponAbility &&
                weapon.unlockedAbility?.id ==
                com.azizoglu.grisilith.weapon.skill.DefaultWeaponAbilities.GLYPH_CASTING_ID &&
                weapon.chalkDustSpellChoice == null
        } ?: return

        chooseGlyphSpellPopup.open(pendingWeapon)
    }

    private fun openPendingIvySummonChoiceIfReady() {

        if (
            chooseToSummonPopup.isOpen ||
            player.health.isDead ||
            stageController.isRunComplete ||
            chestRewardController.isOpen ||
            statCardController.isOpen ||
            isMarketOpen ||
            isPauseMenuOpen ||
            isRunSummaryOpen
        ) {
            return
        }

        val pendingWeapon =
            weaponInventory
                .allWeapons()
                .firstOrNull {
                        weapon ->

                    weapon.definition.id.value ==
                        "WPN_IVY_STAFF" &&
                        weapon.hasWeaponAbility &&
                        weapon.unlockedAbility
                            ?.id ==
                        com.azizoglu.grisilith.weapon.skill
                            .DefaultWeaponAbilities
                            .CHOOSE_TO_SUMMON_ID &&
                        weapon.ivySummonChoice ==
                        null
                }
                ?: return

        chooseToSummonPopup.open(
            pendingWeapon
        )
    }

    private fun handleGameControls(
        delta: Float
    ) {
        if (isRunEndTransitionActive) return

        if (gameMap == GameMap.TEST_MAP && canShowTestMapControls() && handleTestMapControls()) return
        if (!canShowTestMapControls()) testPointerWasDown = false

        if (chooseGlyphSpellPopup.isOpen) {
            chooseGlyphSpellPopup.handleInput()
            return
        }

        if (
            chooseToSummonPopup.isOpen
        ) {
            chooseToSummonPopup
                .handleInput()

            return
        }

        if (isRunSummaryOpen) {
            handleRunSummaryControls()
            return
        }

        if (isPauseMenuOpen) {
            handlePauseControls()
            return
        }

        if (isMarketOpen) {
            handleMarketControls(
                delta
            )

            return
        }

        if (
            chestRewardController.isOpen
        ) {
            chestRewardController
                .handleInput()

            return
        }

        if (statCardController.isOpen) {
            statCardController.handleInput()
            return
        }

        if (
            Gdx.input.isKeyJustPressed(RunInputBindings.pauseKey)
        ) {
            openPauseMenu()
            return
        }

        if (handlePauseButtonInput()) {
            return
        }

        if (Gdx.input.isKeyJustPressed(RunInputBindings.gameSpeedKey)) {
            cycleGameSpeed()
        }

        if (gameMap == GameMap.TEST_MAP &&
            Gdx.input.isKeyJustPressed(RunInputBindings.resetCooldownsKey)
        ) {
            resetAllRunCooldowns()
        }

        if (
            Gdx.input.isKeyJustPressed(
                RunInputBindings.weaponTamgaKey
            )
        ) {
            tamgaRuntimeManager
                .tryActivateWeaponTamga()
        }

        if (
            Gdx.input.isKeyJustPressed(
                RunInputBindings.armorTamgaKey
            )
        ) {
            tamgaRuntimeManager
                .tryActivateArmorTamga()
        }

        updateActiveWeaponSkillAim()
        handleActiveTamgaButtonInput()
        handleGameSpeedButtonInput()
        handleWeaponAbilityInput()

    }

    private fun handlePauseButtonInput(): Boolean {
        if (!Gdx.input.justTouched()) return false

        val pointerPosition = Vector2(
            Gdx.input.x.toFloat(),
            Gdx.input.y.toFloat()
        )
        viewport.unproject(pointerPosition)
        if (!hudRenderer.isPauseButtonAt(pointerPosition.x, pointerPosition.y)) return false

        openPauseMenu()
        return true
    }

    private fun canShowTestMapControls(): Boolean =
        gameMap == GameMap.TEST_MAP &&
            !player.health.isDead &&
            !stageController.isRunComplete &&
            !isRunSummaryOpen &&
            !isMarketOpen &&
            !isPauseMenuOpen &&
            !chestRewardController.isOpen &&
            !statCardController.isOpen &&
            !chooseToSummonPopup.isOpen &&
            !chooseGlyphSpellPopup.isOpen

    private fun handleTestMapControls(): Boolean {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) && isTestSettingsOpen) {
            when {
                testCatalogKind != null -> testCatalogKind = null
                isTestOwnedItemsOpen -> isTestOwnedItemsOpen = false
                else -> isTestSettingsOpen = false
            }
            return true
        }
        if (!isTestSettingsOpen &&
            Gdx.input.isKeyJustPressed(RunInputBindings.testPauseKey)
        ) {
            isTestRunFrozen = !isTestRunFrozen
            return true
        }
        val pointerDown = Gdx.input.isButtonPressed(Input.Buttons.LEFT)
        val justPressed = Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)
        val justReleased = !pointerDown && testPointerWasDown
        testPointerWasDown = pointerDown
        val p = Vector2(Gdx.input.x.toFloat(), Gdx.input.y.toFloat())
        viewport.unproject(p)
        val left = camera.position.x - CAMERA_WIDTH / 2f
        val bottom = camera.position.y - CAMERA_HEIGHT / 2f
        val right = left + CAMERA_WIDTH
        fun hit(x: Float, y: Float, w: Float, h: Float) = Rectangle(x, y, w, h).contains(p)

        if (justPressed) testPressedAtMillis = com.badlogic.gdx.utils.TimeUtils.millis()
        val isShortRelease = justReleased &&
            com.badlogic.gdx.utils.TimeUtils.timeSinceMillis(testPressedAtMillis) < 1_000L

        if (justReleased && isTestSettingsOpen) {
            var completedDrag = false
            draggedTestWeaponSlot?.let { source ->
                completedDrag = true
                val target = (0 until WeaponInventory.WEAPON_SLOT_COUNT).firstOrNull { i ->
                    val column = i % 3
                    val row = i / 3
                    hit(right - 315f + column * 96f, bottom + 358f - row * 104f, 82f, 82f)
                }
                when {
                    target == null && p.x < right - 330f -> weaponInventory.removeWeaponFromSlot(source)
                    target != null && target != source -> {
                        val merged = weaponInventory.mergeSlots(source, target)
                        if (merged != WeaponInventoryResult.MERGED) weaponInventory.swapSlots(source, target)
                    }
                }
                weaponStatBonusSynchronizer.synchronize()
            }
            draggedTestItemRuntimeId?.let { runtimeId ->
                completedDrag = true
                if (p.x < right - 330f) {
                    itemInventory.removeItem(runtimeId)
                    itemStatSynchronizer.synchronize()
                }
            }
            draggedTestWeaponSlot = null
            draggedTestItemRuntimeId = null
            if (completedDrag) return true
        }

        if (!isTestSettingsOpen && justPressed && hit(right - 350f, bottom + 18f, 205f, 44f)) {
            isTestSettingsOpen = !isTestSettingsOpen
            return true
        }
        if (!isTestSettingsOpen && justPressed && hit(right - 135f, bottom + 18f, 115f, 44f)) {
            isTestRunFrozen = !isTestRunFrozen
            return true
        }
        if (!isTestSettingsOpen && justPressed && hit(right - 350f, bottom + 70f, 330f, 44f)) {
            resetAllRunCooldowns()
            return true
        }
        if (!isTestSettingsOpen) return false

        if (isTestOwnedItemsOpen) {
            if (isShortRelease && hit(left + 775f, bottom + 570f, 80f, 32f)) {
                isTestOwnedItemsOpen = false
                return true
            }
            if (isShortRelease && hit(left + 325f, bottom + 145f, 520f, 390f)) {
                val row = ((bottom + 535f - p.y) / 34f).toInt()
                val index = (testCatalogScroll / 34f).toInt() + row
                val item = itemInventory.allItems().getOrNull(index)
                if (item != null && hit(left + 755f, bottom + 501f - row * 34f, 80f, 28f)) {
                    itemInventory.removeItem(item.runtimeId)
                    itemStatSynchronizer.synchronize()
                }
            }
            return true
        }

        if (testCatalogKind != null) {
            val entries = filteredTestCatalogEntries()
            val listBounds = Rectangle(left + 275f, bottom + 135f, 620f, 405f)
            if (pointerDown && listBounds.contains(p)) {
                val previousY = testCatalogDragLastY
                if (previousY != null) {
                    testCatalogScroll = (testCatalogScroll + previousY - p.y)
                        .coerceIn(0f, ((entries.size - 10).coerceAtLeast(0) * 36f))
                }
                testCatalogDragLastY = p.y
            } else if (!pointerDown) {
                testCatalogDragLastY = null
            }

            if (isShortRelease && hit(left + 840f, bottom + 570f, 55f, 32f)) {
                testCatalogKind = null
                return true
            }
            if (isShortRelease && testCatalogKind == "ITEM") {
                val rarities = listOf<com.azizoglu.grisilith.item.definition.ItemRarity?>(null) +
                    com.azizoglu.grisilith.item.definition.ItemRarity.entries
                rarities.forEachIndexed { i, rarity ->
                    if (hit(left + 280f + i * 95f, bottom + 570f, 88f, 30f)) {
                        testItemRarityFilter = rarity; testCatalogScroll = 0f; return true
                    }
                }
            }
            if (isShortRelease && testCatalogKind == "WEAPON") {
                TestWeaponFilter.entries.forEachIndexed { i, filter ->
                    if (hit(left + 280f + i * 145f, bottom + 570f, 138f, 30f)) {
                        testWeaponFilter = filter; testCatalogScroll = 0f; return true
                    }
                }
            }
            if (isShortRelease && listBounds.contains(p)) {
                val row = ((bottom + 535f - p.y) / 36f).toInt()
                val index = (testCatalogScroll / 36f).toInt() + row
                entries.getOrNull(index)?.let { entry ->
                    if (entry is com.azizoglu.grisilith.item.definition.ItemDefinition) {
                        itemInventory.addItem(ItemInstance(entry)); itemStatSynchronizer.synchronize()
                    } else if (entry is com.azizoglu.grisilith.weapon.definition.WeaponDefinition) {
                        val level = when {
                            hit(left + 835f, bottom + 501f - row * 36f, 50f, 28f) -> 5
                            hit(left + 780f, bottom + 501f - row * 36f, 50f, 28f) -> 3
                            else -> 1
                        }
                        addTestWeapon(entry, level)
                    }
                }
                return true
            }
            return true
        }

        if (justPressed && !isTestStatEditorOpen && !isTestResetConfirmationOpen) {
            (0 until WeaponInventory.WEAPON_SLOT_COUNT).firstOrNull { i ->
                val column = i % 3
                val row = i / 3
                weaponInventory.weaponAt(i) != null &&
                    hit(right - 315f + column * 96f, bottom + 358f - row * 104f, 82f, 82f)
            }?.let { draggedTestWeaponSlot = it; return true }
        }
        if (isTestStatEditorOpen) {
            if (isShortRelease) {
                testStatRows(left, bottom).forEach { (stat, rowBounds) ->
                    listOf(-5, -1, 1, 5).forEachIndexed { index, amount ->
                        val button = testStatAdjustmentBounds(rowBounds, index)
                        if (hit(button.x, button.y, button.width, button.height)) {
                            playerStats.adjustRunBonus(stat, amount)
                        }
                    }
                }
                if (hit(right - 230f, bottom + 85f, 150f, 38f)) isTestStatEditorOpen = false
            }
            player.synchronizeMaximumHealthWithStats(healAddedHealth = true)
            return true
        }
        if (isTestResetConfirmationOpen) {
            if (isShortRelease && hit(left + 300f, bottom + 300f, 120f, 44f)) restartRun()
            if (isShortRelease && hit(left + 440f, bottom + 300f, 120f, 44f)) isTestResetConfirmationOpen = false
            return true
        }
        if (isShortRelease && hit(right - 190f, bottom + 650f, 160f, 42f)) {
            isTestSettingsOpen = false
            return true
        }
        if (isShortRelease && hit(right - 213f, bottom + 482f, 173f, 34f)) {
            isTestStatEditorOpen = true
            return true
        }
        if (isShortRelease && hit(left + 55f, bottom + 595f, 120f, 28f)) {
            isTestEnemySpawningEnabled = !isTestEnemySpawningEnabled
            return true
        }
        listOf(EnemyTag.BEAST, EnemyTag.HUMANOID, EnemyTag.UNDEAD).forEachIndexed { i, tag ->
            if (isShortRelease && hit(left + 220f + i * 145f, bottom + 595f, 135f, 28f)) {
                testEnemyTag = tag
                testDummySelected = false
                enemyManager.clearAll()
                testDummyEnemy = null
                mapEnemyRuntimeContext.selectEnemyTag(tag)
                enemySpawner.resetTimer()
                return true
            }
        }
        if (isShortRelease && hit(left + 655f, bottom + 595f, 145f, 28f)) {
            testDummySelected = true
            isTestEnemySpawningEnabled = false
            enemyManager.clearAll()
            testDummyEnemy = mapEnemyRuntimeContext.runtimeFactory.createTestDummy(
                stageController.currentStage,
                worldWidth / 2f,
                worldHeight / 2f
            ).also(enemyManager::spawn)
            return true
        }
        if (isShortRelease && (hit(left + 55f, bottom + 505f, 105f, 28f) || hit(left + 170f, bottom + 505f, 105f, 28f))) {
            testObstaclesEnabled = hit(left + 55f, bottom + 505f, 105f, 28f)
            mapObstacles = if (testObstaclesEnabled) MapObstacleGenerator.generate(GameMap.ROAD_TO_HOME, player.centerX, player.centerY) else emptyList()
            summonManager.replaceMapObstacles(blockingMapObstacles)
            return true
        }
        TestDeathMode.entries.forEachIndexed { i, mode ->
            if (isShortRelease && hit(left + 430f + i * 145f, bottom + 505f, 135f, 28f)) {
                testDeathMode = mode
                return true
            }
        }
        if (isShortRelease) repeat(50) { index ->
            val stage = index + 1; val col = index % 10; val row = index / 10
            if (hit(left + 55f + col * 78f, bottom + 440f - row * 38f, 70f, 30f)) { selectTestStage(stage); return true }
        }
        if (isShortRelease && hit(left + 690f, bottom + 215f, 190f, 40f)) { isTestInfiniteStage = !isTestInfiniteStage; return true }
        if (isShortRelease && hit(left + 55f, bottom + 210f, 170f, 40f)) { sessionWallet.addGold(10_000f); return true }
        if (isShortRelease && hit(left + 245f, bottom + 210f, 190f, 40f)) { testCatalogKind = "ITEM"; testCatalogScroll = 0f; return true }
        if (isShortRelease && hit(left + 455f, bottom + 210f, 190f, 40f)) { testCatalogKind = "WEAPON"; testCatalogScroll = 0f; return true }
        if (isShortRelease && hit(right - 315f, bottom + 135f, 274f, 62f)) {
            isTestOwnedItemsOpen = true
            testCatalogScroll = 0f
            return true
        }
        if (isShortRelease && hit(left + 55f, bottom + 65f, 180f, 48f)) { isTestResetConfirmationOpen = true; return true }
        if (isShortRelease && hit(left + 255f, bottom + 65f, 180f, 48f)) { enemyManager.clearAll(); testDummyEnemy = null; return true }
        return true
    }

    private fun selectTestStage(stage: Int) {
        stageController.restoreCheckpointStage(stage.coerceIn(1, 50))
        enemyManager.clearAll()
        testDummyEnemy = null
        projectiles.clear()
        enemySpawner.resetTimer()
        portalStageCompletionRequested = false
    }

    private fun testStatGroupBounds(
        left: Float,
        bottom: Float
    ): Map<StatPresentationGroup, Rectangle> =
        StatPresentationGroup.ordered.mapIndexed { index, group ->
            val column = index % 2
            val row = index / 2
            group to Rectangle(
                left + 135f + column * 505f,
                bottom + if (row == 0) 350f else 90f,
                490f,
                245f
            )
        }.toMap(LinkedHashMap())

    private fun testStatRows(left: Float, bottom: Float): Map<StatType, Rectangle> {
        val rows = linkedMapOf<StatType, Rectangle>()
        testStatGroupBounds(left, bottom).forEach { (group, bounds) ->
            group.stats.forEachIndexed { index, stat ->
                rows[stat] = Rectangle(
                    bounds.x + 10f,
                    bounds.y + bounds.height - 65f - index * 30f,
                    bounds.width - 20f,
                    27f
                )
            }
        }
        return rows
    }

    private fun testStatAdjustmentBounds(row: Rectangle, index: Int) =
        Rectangle(row.x + row.width - 148f + index * 37f, row.y, 33f, row.height)

    private fun filteredTestCatalogEntries(): List<Any> =
        when (testCatalogKind) {
            "ITEM" ->
                DefaultItems.all
                    .asSequence()
                    .filter { definition ->
                        testItemRarityFilter == null || definition.rarity == testItemRarityFilter
                    }
                    .sortedWith(compareBy({ it.rarity.ordinal }, { it.displayName }))
                    .toList()

            "WEAPON" ->
                DefaultWeapons.all
                    .asSequence()
                    .filter { definition ->
                        when (testWeaponFilter) {
                            TestWeaponFilter.ALL -> true
                            TestWeaponFilter.MELEE -> ContentTag.MELEE_WEAPON in definition.tags
                            TestWeaponFilter.RANGED -> ContentTag.RANGED_WEAPON in definition.tags
                            TestWeaponFilter.SPELL_FOCUS -> ContentTag.SPELL_FOCUS in definition.tags
                        }
                    }
                    .sortedWith(
                        compareBy<com.azizoglu.grisilith.weapon.definition.WeaponDefinition>(
                            { definition ->
                                when {
                                    ContentTag.MELEE_WEAPON in definition.tags -> 0
                                    ContentTag.RANGED_WEAPON in definition.tags -> 1
                                    else -> 2
                                }
                            },
                            { it.displayName }
                        )
                    )
                    .toList()

            else -> emptyList()
        }

    private fun addTestWeapon(
        definition: com.azizoglu.grisilith.weapon.definition.WeaponDefinition,
        level: Int
    ) {
        val result = weaponInventory.addPurchasedWeapon(WeaponInstance(definition, level))
        if (result == WeaponInventoryResult.MERGED) {
            weaponInventory.weaponSlots
                .firstOrNull { it.weapon?.definition?.id == definition.id }
                ?.weapon
                ?.let { weapon ->
                    if (weapon.level < level) weapon.setLevel(level)
                }
        }
        weaponStatBonusSynchronizer.synchronize()
    }

    private fun restartTestStage() {
        val stage = stageController.currentStage
        enemyManager.clearAll()
        testDummyEnemy = null
        projectiles.clear()
        summonManager.clear()
        stageController.restoreCheckpointStage(stage)
        player.health.restoreCurrentHealth(player.health.maximumHealth)
        troubleAtHomeDefenseRuntime.gateHealth.restoreCurrentHealth(troubleAtHomeDefenseRuntime.gateHealth.maximumHealth)
        enemySpawner.resetTimer()
        portalStageCompletionRequested = false
    }

    private fun handleTestMapDeathPolicy() {
        val playerDead = player.health.isDead
        val gateDead = troubleAtHomeDefenseRuntime.isEnabled && troubleAtHomeDefenseRuntime.gateHealth.isDead
        if (!playerDead && !gateDead) return
        when (testDeathMode) {
            TestDeathMode.END_RUN -> Unit
            TestDeathMode.RESTART_STAGE -> restartTestStage()
            TestDeathMode.IMMORTAL -> {
                if (playerDead) player.health.restoreCurrentHealth(1)
                if (gateDead) troubleAtHomeDefenseRuntime.gateHealth.restoreCurrentHealth(1)
            }
        }
    }

    private fun synchronizeTestDummyVisibility() {
        val shouldExist = testDummySelected && canShowTestMapControls() && !isTestSettingsOpen
        val current = testDummyEnemy
        if (!shouldExist) {
            if (current != null) enemyManager.remove(current)
            testDummyEnemy = null
            return
        }
        if (current == null || current !in enemyManager.allEnemies || current.isDead) {
            testDummyEnemy = mapEnemyRuntimeContext.runtimeFactory.createTestDummy(
                stageController.currentStage,
                worldWidth / 2f,
                worldHeight / 2f
            ).also(enemyManager::spawn)
        }
    }

    private fun handleGameSpeedButtonInput() {
        if (
            !GameSettings
                .showGameSpeedButton ||
            !Gdx.input.justTouched()
        ) {
            return
        }

        val pointerPosition =
            Vector2(
                Gdx.input.x
                    .toFloat(),
                Gdx.input.y
                    .toFloat()
            )

        viewport.unproject(
            pointerPosition
        )

        if (
            !hudRenderer
                .isGameSpeedButtonAt(
                    worldX =
                        pointerPosition.x,

                    worldY =
                        pointerPosition.y
                )
        ) {
            return
        }

        cycleGameSpeed()
    }

    private fun cycleGameSpeed() {
        val nextSpeed = when (gameTimeController.gameSpeed) {
            0.5f -> 1f
            1f -> 2f
            else -> 0.5f
        }
        gameTimeController.setGameSpeed(nextSpeed)
        GameSettings.setGameSpeed(nextSpeed)
    }

    private fun resetAllRunCooldowns() {
        weaponRuntimeManager.reduceAllAbilityCooldowns(Float.MAX_VALUE)
        tamgaRuntimeManager.resetAllActiveCooldowns()
        ActiveItemEffects.resetAllCooldowns()
    }

    private fun updateActiveWeaponSkillAim() {
        val pointerPosition =
            Vector2(
                Gdx.input.x
                    .toFloat(),
                Gdx.input.y
                    .toFloat()
            )

        viewport.unproject(
            pointerPosition
        )

        weaponRuntimeManager
            .updateActiveSkillAim(
                worldX =
                    pointerPosition.x,

                worldY =
                    pointerPosition.y
            )
    }

    private fun handleWeaponAbilityInput() {
        (0 until WeaponInventory.WEAPON_SLOT_COUNT)
            .forEach { slotIndex ->

                val keyCode = RunInputBindings.weaponAbilityKey(slotIndex)

                if (
                    Gdx.input
                        .isKeyJustPressed(
                            keyCode
                        )
                ) {
                    val summonActivated =
                        summonManager
                            .tryActivateIvyStaffInSlot(
                                slotIndex =
                                    slotIndex,

                                playerCenterX =
                                    player.centerX,

                                playerCenterY =
                                    player.centerY,

                                worldWidth =
                                    worldWidth,

                                worldHeight =
                                    worldHeight
                            )

                    if (!summonActivated) {
                        weaponRuntimeManager
                            .tryActivateAbilityInSlot(
                                slotIndex =
                                    slotIndex,

                                player =
                                    player,

                                enemyManager =
                                    enemyManager,

                                worldWidth =
                                    worldWidth,

                                worldHeight =
                                    worldHeight
                            )
                    }
                }
            }

        if (
            !Gdx.input.justTouched()
        ) {
            return
        }

        val pointerPosition =
            Vector2(
                Gdx.input.x
                    .toFloat(),
                Gdx.input.y
                    .toFloat()
            )

        viewport.unproject(
            pointerPosition
        )

        if (
            hudRenderer
                .isGameSpeedButtonAt(
                    worldX =
                        pointerPosition.x,

                    worldY =
                        pointerPosition.y
                )
        ) {
            return
        }

        val clickedSlot =
            hudRenderer.weaponSlotAt(
                worldX =
                    pointerPosition.x,

                worldY =
                    pointerPosition.y
            )

        if (clickedSlot == null) {
            weaponRuntimeManager
                .confirmPreparedAbilityAtMouse(player, enemyManager, worldWidth, worldHeight)

            return
        }

        weaponRuntimeManager
            .tryActivateAbilityInSlot(
                slotIndex =
                    clickedSlot,

                player =
                    player,

                enemyManager =
                    enemyManager,

                worldWidth =
                    worldWidth,

                worldHeight =
                    worldHeight
            )
    }
    private fun handleActiveTamgaButtonInput() {
        if (
            !Gdx.input.justTouched()
        ) {
            return
        }

        val pointerPosition =
            Vector2(
                Gdx.input.x.toFloat(),
                Gdx.input.y.toFloat()
            )

        viewport.unproject(
            pointerPosition
        )

        when {
            tamgaRuntimeManager
                .hasActiveWeaponTamga &&
                activeTamgaHudRenderer
                    .isWeaponButtonHit(
                        pointerPosition.x,
                        pointerPosition.y
                    ) -> {

                tamgaRuntimeManager
                    .tryActivateWeaponTamga()
            }

            tamgaRuntimeManager
                .hasActiveArmorTamga &&
                activeTamgaHudRenderer
                    .isArmorButtonHit(
                        pointerPosition.x,
                        pointerPosition.y
                    ) -> {

                tamgaRuntimeManager
                    .tryActivateArmorTamga()
            }
        }
    }

    private fun openPauseMenu() {
        isPauseMenuOpen =
            true

        pauseInputController.reset()

        pausePreviousInputProcessor = Gdx.input.inputProcessor
        Gdx.input.inputProcessor = pauseInputController
    }

    private fun closePauseMenu() {
        isPauseMenuOpen =
            false

        pauseInputController.reset()

        if (Gdx.input.inputProcessor === pauseInputController) {
            Gdx.input.inputProcessor = pausePreviousInputProcessor
        }
        pausePreviousInputProcessor = null
    }

    private fun handlePauseControls() {

        if (
            Gdx.input.isKeyJustPressed(
                RunInputBindings.pauseKey
            )
        ) {
            if (
                pauseInteractionState
                    .isAnyModalOpen
            ) {
                pauseInteractionState
                    .closeAllModals()
            } else {
                closePauseMenu()
            }

            return
        }

        val pauseAction =
            pauseInputController.update()

        when (pauseAction) {
            PauseAction.RETURN_TO_GAME -> {
                closePauseMenu()
            }

            PauseAction.RESTART -> {

                restartRun()
            }

            PauseAction.OPEN_SETTINGS -> {
                if (Gdx.input.inputProcessor === pauseInputController) {
                    Gdx.input.inputProcessor = pausePreviousInputProcessor
                }
                pausePreviousInputProcessor = null
                game.showSettingsFromPausedRun(
                    pausedRun =
                        this
                )
            }

            PauseAction.MAIN_MENU -> {
                game.showMainMenu()
            }

            PauseAction.QUIT_GAME -> {
                Gdx.app.exit()
            }

            null -> {
            }
        }
    }
    private fun restartRun() {

        if (Gdx.input.inputProcessor === runSummaryInputProcessor) {
            Gdx.input.inputProcessor = summaryPreviousInputProcessor
        }
        summaryPreviousInputProcessor = null

        val preset =
            selectedPreset

        if (
            preset != null
        ) {
            game.startPresetRun(
                difficulty =
                    difficulty,

                gameMap =
                    gameMap,

                presetId =
                    preset.preset.presetId
            )

            return
        }

        game.startRun(
            difficulty =
                difficulty,

            gameMap =
                gameMap,

            character =
                selectedCharacter
        )
    }

    private fun handleRunSummaryControls() {

        if (isRunEndTransitionActive) return

        /*
         * Stat detay kartının input'u her şeyden önce işlenir.
         *
         * Detail açıksa:
         * - ESC kartı kapatır.
         * - Kart dışına tıklamak kartı kapatır.
         * - Kart içine tıklamak arkadaki inputlara geçmez.
         * - R ve ENTER çalışmaz.
         */
        val summaryStatInputConsumed =
            runSummaryStatInteractionController
                .handleInput()

        if (
            summaryStatInputConsumed
        ) {
            return
        }

        /*
         * Klavye kısayolları mevcut davranışı korur.
         */
        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.R
            )
        ) {
            restartRun()
            return
        }

        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.ENTER
            )
        ) {
            game.showMainMenu()
            return
        }

        /*
         * Summary ekranındaki iki görsel buton daha önce yalnızca
         * çiziliyordu; mouse input'u hiçbir yerde kontrol edilmiyordu.
         *
         * Ekran koordinatını viewport üzerinden world/UI koordinatına
         * çevirip renderer'ın kullandığı gerçek button Rectangle'larıyla
         * test ediyoruz. Böylece çizim ile tıklama alanı birebir aynı kalır.
         */
        if (
            !Gdx.input.isButtonJustPressed(
                Input.Buttons.LEFT
            )
        ) {
            return
        }

        val pointerPosition =
            Vector2(
                Gdx.input.x.toFloat(),
                Gdx.input.y.toFloat()
            )

        viewport.unproject(
            pointerPosition
        )

        val summaryLayout =
            runSummaryRenderer
                .currentLayout()

        when {
            summaryLayout.restartButton
                .contains(
                    pointerPosition
                ) -> {

                restartRun()
            }

            summaryLayout.mainMenuButton
                .contains(
                    pointerPosition
                ) -> {

                game.showMainMenu()
            }
        }
    }

    private fun runSummaryRevealAlpha(): Float {
        if (!isRunSummaryOpen || runEndTransitionElapsed < 0f) return 1f
        return ((runEndTransitionElapsed - RunSummaryRenderer.SUMMARY_REVEAL_START) /
            RunSummaryRenderer.SUMMARY_FADE_DURATION).coerceIn(0f, 1f)
    }

    private fun handleChargerEndEvent() {
        val event =
            tamgaRuntimeManager
                .consumeChargerEndEvent()
                ?: return

        when (event.reason) {

            ActiveTamgaEndReason.COMPLETED,
            ActiveTamgaEndReason.INTERRUPTED -> {

                tamgaRuntimeManager
                    .triggerChargerExplosion(
                        centerX =
                            event.centerX,

                        centerY =
                            event.centerY,

                        chargedSeconds =
                            event.chargedSeconds,

                        completedSuccessfully =
                            event.wasCompletedSuccessfully,

                        enemies =
                            enemyManager
                                .allEnemies,

                        onDamageDealt = {
                                appliedDamage,
                                sourceName ->

                            runStatistics
                                .recordDamageDealt(
                                    amount =
                                        appliedDamage,

                                    weaponName =
                                        sourceName
                                )
                        },

                        onEnemyKilled = {
                                killedEnemy ->

                            handleEnemyKilled(
                                killedEnemy
                            )
                        }
                    )
            }

            ActiveTamgaEndReason.RESET -> {
                /*
                 * Run resetinde patlama oluşturulmaz.
                 */
            }
        }
    }

    private fun bankUncollectedCurrencyForNextStage() {
        val loss =
            currencyPickupManager
                .deferUncollectedForNextStage()

        if (
            loss.grisilithLost >
            0
        ) {
            runRewardTracker
                .discardGrisilithCoins(
                    loss.grisilithLost
                )
        }
    }

    private fun applyPreBossStageGoldGrowth() {
        val currentStage =
            stageController
                .currentStage

        if (
            !GoldEconomyCalculator
                .isBossStage(
                    currentStage
                )
        ) {
            return
        }

        val bonus =
            GoldEconomyCalculator
                .preBossStageGrowthBonus(
                    currentGold =
                        sessionWallet
                            .goldCoins,

                    harvestScore =
                        playerStats
                            .totalScore(
                                StatType.HARVEST
                            ),

                    defeatedMiniBossCount =
                        runStatistics
                            .totalBossesDefeated
                )

        if (
            bonus <=
            0
        ) {
            return
        }

        sessionWallet.addGold(
            bonus.toFloat()
        )

        runStatistics
            .recordGoldCollected(
                bonus.toFloat()
            )

        /*
         * Reuse the existing (+N) HUD feedback so the player can see exactly
         * how much money was added when the boss stage begins.
         */
        currencyPickupManager
            .recordStageStartGoldBonus(
                bonus
            )
    }

    private fun updateGame(
        delta: Float
    ) {
        com.azizoglu.grisilith.weapon.visual.attack
            .MeleeAttackVisualManager
            .update(
                delta
            )

        com.azizoglu.grisilith.weapon.visual.attack
            .SoulDrainVisualManager
            .update(
                delta
            )

        if (
            stageController.currentStage !=
            lastCurrencyTransferStage
        ) {
            currentStageChestsCollected =
                0

            currencyPickupManager
                .transferDeferredAtStageStart(
                    sessionWallet =
                        sessionWallet,

                    runStatistics =
                        runStatistics
                )

            applyPreBossStageGoldGrowth()

            lastCurrencyTransferStage =
                stageController.currentStage
        }

        damageNumberManager.update(
            delta =
                delta
        )
        HealingVisualManager.update(delta)

        if (player.health.isDead) {
            return
        }

        val stageChanged =
            updateStage(
                delta
            )

        if (
            stageChanged ||
            stageController.isRunComplete
        ) {
            return
        }

        MapEnvironmentEffects.apply(
            obstacles = mapObstacles,
            player = player,
            playerIsAirborne =
                tamgaRuntimeManager.isPhantomSteedVisualActive,
            enemies = enemyManager.allEnemies,
            summons = summonManager.activeSummons
        )

        player.update(
            delta
        )

        blockingMapObstacles.forEach { obstacle ->
            player.resolveObstacleCollision(
                obstacle.bounds
            )
        }

        enemyManager.resolvePlayerCollisions {
                enemyBounds ->

            player.resolveCollision(
                enemyBounds
            )
        }

        summonManager.resolvePlayerCollisions {
                summonBounds ->

            player.resolveCollision(
                summonBounds
            )
        }

        /*
         * Player.update() sonrasında obstacle ve Enemy
         * collision çözümü konumu değiştirebilir.
         * Charger gerçek hareket hızını bu son konuma
         * göre değerlendirmelidir.
         */
        player.finalizeMovementAfterCollisions(
            delta =
                delta
        )

        ActiveItemEffects.update(delta)

        worldItemRuntime.update(
            delta = delta,
            stage = stageController.currentStage,
            stageType = stageController.currentStageType,
            elapsedStageSeconds = stageController.elapsedStageTime,
            stageDurationSeconds = stageController.currentStageDuration,
            player = player,
            enemies = enemyManager.allEnemies,
            summons = summonManager.activeSummons,
            mapObstacles = mapObstacles,
            pickupRadius = pickupRangeController.effectiveRadius,
            collectCurrency = { x, y, radius ->
                currencyPickupManager.collectNearestTo(
                    worldX = x,
                    worldY = y,
                    radius = radius,
                    sessionWallet = sessionWallet,
                    runStatistics = runStatistics
                )
            },
            nearestCurrency = currencyPickupManager::nearestPickupPosition,
            onPortalEntered = {
                portalStageCompletionRequested = true
            },
            onPitEnemyKilled = { enemy ->
                val gold = goldRewardFor(enemy)
                recordEnemyDeath(enemy, suppressGold = true)
                gold
            },
            onTrapGoldReleased = { x, y, gold ->
                currencyPickupManager.spawnGoldReward(x, y, gold.toFloat())
            },
            onHalfGold = {
                sessionWallet.spendGold(sessionWallet.goldCoins / 2)
            },
            onSpawnRiskMiniBoss = {
                enemySpawner.spawnBonusMiniBoss(
                    enemyManager = enemyManager,
                    currentStage = stageController.currentStage,
                    playerCenterX = player.centerX,
                    playerCenterY = player.centerY,
                    mapObstacles = blockingMapObstacles
                )?.let(noLootTotemMiniBosses::add)
            },
            onHourglass = {
                val reduction = runStatistics.totalBossesDefeated * 5f
                weaponRuntimeManager.reduceAllAbilityCooldowns(reduction)
                tamgaRuntimeManager.reduceAllActiveCooldowns(reduction)
                summonManager.reduceAllCooldowns(reduction)
            },
            onTreasure = { x, y ->
                val lower = runStatistics.totalBossesDefeated.coerceAtLeast(0)
                val upper = stageController.currentStage.coerceAtLeast(lower)
                val count = if (upper == lower) lower else kotlin.random.Random.nextInt(lower, upper + 1)
                val grisilith = gameMap != GameMap.TEST_MAP && count > 0 && kotlin.random.Random.nextFloat() < 0.01f &&
                    runRewardTracker.awardBonusGrisilithCoin() > 0
                currencyPickupManager.spawnGoldReward(x, y, (count - if (grisilith) 1 else 0).toFloat())
                if (grisilith) currencyPickupManager.spawnGrisilithCoins(x, y, 1)
            },
            onToads = { count, x, y ->
                summonManager.spawnBoneTotemToads(count, x, y, worldWidth, worldHeight)
            },
            hasLivingToads = summonManager::hasLivingBoneTotemToads,
            onAuraEnemyKilled = ::handleEnemyKilled
        )

        if (portalStageCompletionRequested) {
            portalStageCompletionRequested = false
            completeCurrentStage()
            return
        }

        weaponRuntimeManager.updateCharacterTrait(
            delta = delta,
            trySpawnToad = {
                summonManager.trySpawnToadCycleToad(
                    centerX = player.centerX,
                    centerY = player.centerY,
                    worldWidth = worldWidth,
                    worldHeight = worldHeight
                )
            }
        )

        summonManager.update(
            delta =
                delta,

            playerCenterX =
                player.centerX,

            playerCenterY =
                player.centerY,

            worldWidth =
                worldWidth,

            worldHeight =
                worldHeight,

            enemies =
                enemyManager
                    .allEnemies,

            projectiles =
                projectiles,

            onDamageDealt = {
                    appliedDamage,
                    sourceName ->

                runStatistics
                    .recordDamageDealt(
                        amount =
                            appliedDamage,

                        weaponName =
                            sourceName
                    )
            },

            onEnemyKilled = {
                    killedEnemy ->

                handleEnemyKilled(
                    killedEnemy
                )
            },

            onPlayerDamage = {
                    requestedDamage,
                    _ ->

                val appliedDamage =
                    player.takeDamage(
                        requestedDamage
                    )

                runStatistics
                    .recordDamageTaken(
                        amount = appliedDamage,
                        requestedAmount = requestedDamage,
                        damageType = com.azizoglu.grisilith.content.DamageType.NONE
                    )
            }
        )

        blockingMapObstacles.forEach { obstacle ->
            summonManager.activeSummons.forEach { summon ->
                summon.resolveObstacleCollision(
                    obstacle.bounds
                )
            }
        }

        summonManager.resolveBodyCollisions(
            playerBounds = player.bounds
        )

        troubleAtHomeDefenseRuntime.update(
            delta = delta,
            currentStage = stageController.currentStage,
            summonManager = summonManager,
            guardProfileProvider = ::randomVillageGuardProfile
        )

        worldItemRuntime.totemCollisionBounds()?.let { obstacle ->
            summonManager.activeSummons.forEach {
                it.resolveObstacleCollision(obstacle)
            }
        }

        tamgaRuntimeManager
            .updateChargerAfterMovement(
                delta =
                    delta
            )

        handleChargerEndEvent()

        enemyManager.allEnemies.forEach { enemy ->
            enemy.vortexMovementSpeedMultiplier = weaponRuntimeManager.vortexMovementMultiplier(enemy)
            enemy.projectileVelocityBender = weaponRuntimeManager::bendVortexProjectileVelocity
        }
        enemyManager.update(
            delta =
                delta,

            targetX =
                player.centerX,

            targetY =
                player.centerY,

            targetBounds =
                player.bounds,

            playerMaximumHealth =
                player.health.maximumHealth,

            summons =
                summonManager
                    .activeSummons,

            gateCenterX =
                troubleAtHomeDefenseRuntime
                    .targetBounds
                    ?.let {
                        troubleAtHomeDefenseRuntime.targetCenterX
                    },

            gateCenterY =
                troubleAtHomeDefenseRuntime
                    .targetBounds
                    ?.let {
                        troubleAtHomeDefenseRuntime.targetCenterY
                    },

            gateBounds =
                troubleAtHomeDefenseRuntime.targetBounds,

            worldWidth =
                worldWidth,

            worldHeight =
                worldHeight,

            mapObstacles =
                blockingMapObstacles,

            onPlayerDamaged = {
                    sourceEnemy,
                    damage,
                    damageType ->

                val appliedDamage =
                    player.takeDamage(
                        damage,
                        damageType,
                        sourceEnemy.piercingIgnoreRatio
                    )

                if (appliedDamage > 0) {
                    com.azizoglu.grisilith.combat.condition.DamageConditionRules
                        .conditionFor(damageType)
                        ?.let { condition ->
                            player.applyCondition(
                                com.azizoglu.grisilith.combat.condition.ConditionDamageCalculator.createApplication(
                                    condition,
                                    sourceEnemy.conditionDamageStatScore,
                                    sourceName = sourceEnemy.kindId?.value ?: "Enemy"
                                )
                            )
                        }
                    if (sourceEnemy.kindId == com.azizoglu.grisilith.enemy.definition.EnemyKindIds.GHOST) {
                        player.applyCondition(
                            com.azizoglu.grisilith.combat.condition.ConditionDamageCalculator.createApplication(
                                ConditionType.NECROSIS,
                                sourceEnemy.conditionDamageStatScore,
                                3f,
                                "Ghost Necrosis"
                            )
                        )
                    }
                    if (ActiveItemEffects.reflectContactDamage(sourceEnemy)) {
                        handleEnemyKilled(sourceEnemy)
                    }
                }

                runStatistics.recordDamageTaken(
                    amount = appliedDamage,
                    requestedAmount = damage,
                    damageType = damageType
                )
            },

            onSummonDamaged = {
                    summon,
                    damage,
                    damageType,
                    sourceEnemy ->

                val appliedDamage = summon.takeDamage(
                    damage,
                    damageType,
                    sourceEnemy.piercingIgnoreRatio
                )
                if (appliedDamage > 0) {
                    com.azizoglu.grisilith.combat.condition.DamageConditionRules
                        .conditionFor(damageType)
                        ?.let { condition ->
                            summon.applyCondition(
                                com.azizoglu.grisilith.combat.condition.ConditionDamageCalculator.createApplication(
                                    condition,
                                    sourceEnemy.conditionDamageStatScore,
                                    sourceName = sourceEnemy.kindId?.value ?: "Enemy"
                                )
                            )
                        }
                }
            },

            onPlayerCondition = { condition, duration, conditionStatScore ->
                if (condition == ConditionType.ACIDIC_PATH_DAMAGE) {
                    player.applyCondition(
                        com.azizoglu.grisilith.combat.condition.ConditionDamageCalculator.createApplication(
                            ConditionType.CORRODING,
                            conditionStatScore,
                            3f,
                            "Acidic Path"
                        )
                    )
                }
                player.applyCondition(
                    com.azizoglu.grisilith.combat.condition.ConditionDamageCalculator.createApplication(
                        type = condition,
                        sourceStatScore = conditionStatScore,
                        durationSeconds = duration ?: 3f,
                        sourceName = condition.displayName
                    )
                )
            },

            onSummonCondition = { summon, condition, duration, conditionStatScore ->
                if (condition == ConditionType.ACIDIC_PATH_DAMAGE) {
                    summon.applyCondition(
                        com.azizoglu.grisilith.combat.condition.ConditionDamageCalculator.createApplication(
                            ConditionType.CORRODING,
                            conditionStatScore,
                            3f,
                            "Acidic Path"
                        )
                    )
                }
                summon.applyCondition(
                    com.azizoglu.grisilith.combat.condition.ConditionDamageCalculator.createApplication(
                        type = condition,
                        sourceStatScore = conditionStatScore,
                        durationSeconds = duration ?: 3f,
                        sourceName = condition.displayName
                    )
                )
            },

            onGateDamaged = {
                    sourceEnemy,
                    damage ->

                val resolvedGateDamage =
                    kotlin.math.round(
                        damage * sourceEnemy.gateDamageMultiplier
                    )
                        .toInt()
                        .coerceAtLeast(1)

                troubleAtHomeDefenseRuntime
                    .takeDamage(resolvedGateDamage)

                if (
                    troubleAtHomeDefenseRuntime
                        .gateHealth
                        .isDead
                ) {
                    player.health.takeDamage(
                        player.health.currentHealth
                    )
                }
            }
        )

        enemyManager.spawnPendingSlimeFragments(
            runtimeFactory = mapEnemyRuntimeContext.runtimeFactory,
            worldWidth = worldWidth,
            worldHeight = worldHeight
        )

        enemyManager.spawnPendingSpecialEnemies(
            runtimeFactory = mapEnemyRuntimeContext.runtimeFactory,
            worldWidth = worldWidth,
            worldHeight = worldHeight,
            obstacles = blockingMapObstacles
        )

        worldItemRuntime.resolveEnemyExclusions(
            enemyManager.allEnemies
        )


        consumeEnemyConditionDamageEvents()

        /*
         * Quick Shot timer ve diğer genel
         * Tamga runtime güncellemeleri.
         */
        tamgaRuntimeManager.update(
            delta = delta,
            player = player,
            enemies = enemyManager.allEnemies
        )

        /*
         * Holy Lights yalnızca takılıysa
         * manager içinde çalışır.
         */
        tamgaRuntimeManager
            .updateHolyLights(
                delta = delta,

                player =
                    player,

                enemies =
                    enemyManager.allEnemies,

                onDamageDealt = {
                        appliedDamage,
                        sourceName ->

                    runStatistics
                        .recordDamageDealt(
                            amount =
                                appliedDamage,

                            weaponName =
                                sourceName
                        )
                },

                onEnemyKilled = {
                        killedEnemy ->

                    handleEnemyKilled(
                        killedEnemy
                    )
                },

                onLifeStealDamage = {
                        appliedDamage ->
                }
            )

        weaponCarrierVisualSystem.update(
            delta = delta,
            player = player,
            runtimeManager = weaponRuntimeManager
        )

        if (!player.health.isDead) {
            enemyManager.synchronizeWeaponSummons(summonManager.activeSummons)
            weaponRuntimeManager.update(
                delta = delta,

                player =
                    player,

                enemyManager =
                    enemyManager,

                projectiles =
                    projectiles,                        mapObstacles =
                    blockingMapObstacles,

                worldWidth =
                    worldWidth,

                worldHeight =
                    worldHeight,

                attackOriginForSlot = { slotIndex ->
                    weaponCarrierVisualSystem.attackOriginForSlot(slotIndex, player)
                },

                onDamageDealt = {
                        appliedDamage,
                        weaponName ->

                    runStatistics
                        .recordDamageDealt(
                            amount =
                                appliedDamage,

                            weaponName =
                                weaponName
                        )
                },

                onEnemyKilled = {
                        killedEnemy ->

                    handleEnemyKilled(
                        killedEnemy
                    )
                },

                onNormalAttackPerformed = {
                        weapon,
                        attacksPerSecond,
                        targetCenterX,
                        targetCenterY ->

                    weaponCarrierVisualSystem
                        .notifyNormalAttack(
                            weapon =
                                weapon,

                            attacksPerSecond =
                                attacksPerSecond,

                            targetCenterX =
                                targetCenterX,

                            targetCenterY =
                                targetCenterY
                        )
                }
            )
        }

        consumeImpaleAndDragReward()

        projectiles.forEach { projectile ->
            projectile.update(
                delta = delta,
                worldWidth = worldWidth,
                worldHeight = worldHeight
            )

            projectile.checkObstacleCollision(
                obstacleBounds =
                    blockingMapObstacles
                        .map {
                            it.bounds
                        } +
                        listOfNotNull(
                            worldItemRuntime.totemCollisionBounds()
                        )
            )

            projectile.checkCollision(
                enemies =
                    enemyManager.allEnemies,

                onDamageDealt = {
                        appliedDamage ->

                    runStatistics
                        .recordDamageDealt(
                            amount =
                                appliedDamage,

                            weaponName =
                                projectile
                                    .sourceWeaponName
                        )
                },

                onEnemyKilled = {
                        killedEnemy ->

                    handleEnemyKilled(
                        killedEnemy
                    )
                },

                onWeaponHit = {
                        hitEvent ->

                    if (
                        hitEvent.isCriticalHit
                    ) {
                        ActiveItemEffects.onCriticalHit()

                        hitEvent.enemy
                            .markLatestDamageNumberCritical(
                                hitEvent.appliedDamage
                            )
                    }

                    player.tryLifeSteal(
                        isEligibleHit =
                            hitEvent.isLifeStealEligible
                    )

                    weaponRuntimeManager
                        .applyCharacterTraitOnWeaponHit(
                            event = hitEvent,
                            onDamageDealt = {
                                    bonusDamage,
                                    sourceName ->

                                runStatistics
                                    .recordDamageDealt(
                                        amount = bonusDamage,
                                        weaponName = sourceName
                                    )
                            },
                            onEnemyKilled = {
                                    killedEnemy ->

                                handleEnemyKilled(
                                    killedEnemy
                                )
                            }
                        )

                    tamgaRuntimeManager
                        .onWeaponHit(
                            event =
                                hitEvent,

                            onDamageDealt = {
                                    bonusDamage,
                                    weaponName ->

                                runStatistics
                                    .recordDamageDealt(
                                        amount =
                                            bonusDamage,

                                        weaponName =
                                            weaponName
                                    )

                            },

                            onEnemyKilled = {
                                    killedEnemy ->

                                handleEnemyKilled(
                                    killedEnemy
                                )
                            }
                        )
                },

                modifyWeaponDamage = {
                        enemy,
                        baseDamage ->

                    weaponRuntimeManager
                        .modifyWeaponDamage(
                            enemy = enemy,
                            baseDamage = baseDamage
                        )
                }
            )

            projectile.checkPlayerProjectileAgainstRogueSummons(
                summons = summonManager.activeSummons,
                onDamageDealt = { appliedDamage, sourceName ->
                    runStatistics.recordDamageDealt(appliedDamage, sourceName)
                }
            )

            /* Only summon-owned projectiles react below. */
            projectile.checkSummonCollision(
                summons =
                    summonManager
                        .activeSummons,

                onDamageDealt = {
                        appliedDamage,
                        sourceName ->

                    runStatistics
                        .recordDamageDealt(
                            amount =
                                appliedDamage,

                            weaponName =
                                sourceName
                        )
                }
            )
        }

        projectiles.removeAll { projectile ->
            !projectile.isActive
        }

        currencyPickupManager.update(
            delta =
                delta,

            playerX =
                player.centerX,

            playerY =
                player.centerY,

            pickupRadius =
                pickupRangeController
                    .effectiveRadius,

            sessionWallet =
                sessionWallet,

            runStatistics =
                runStatistics
        )

        chestWorldVisualManager
            .update(
                delta
            )

        /*
         * Chest uses exactly the same effective pickup radius as currencies.
         * We expand a temporary player rectangle rather than changing the
         * existing ChestPickupManager API.
         */
        val chestPickupRadius =
            pickupRangeController
                .effectiveRadius

        val chestPickupBounds =
            com.badlogic.gdx.math.Rectangle(
                player.centerX -
                    chestPickupRadius,

                player.centerY -
                    chestPickupRadius,

                chestPickupRadius *
                    2f,

                chestPickupRadius *
                    2f
            )

        chestPickupManager.update(
            playerBounds =
                chestPickupBounds
        ) {
            chestWorldVisualManager
                .removeNearestTo(
                    worldX =
                        player.centerX,

                    worldY =
                        player.centerY
                )

            currentStageChestsCollected +=
                1

            ActiveItemEffects.healFromChest()

            chestRewardQueue
                .addChest()
        }


        enemyManager.removeDeadEnemies()

        if (completeBossStageIfDefeated()) {
            return
        }

        updateCamera()

        if (gameMap != GameMap.TEST_MAP || isTestEnemySpawningEnabled) enemySpawner.update(
            delta =
                delta,

            enemyManager =
                enemyManager,

            currentStage =
                stageController
                    .currentStage,

            maximumStage =
                stageController
                    .maximumStage,

            stageType =
                stageController
                    .currentStageType,

            stageDurationSeconds =
                stageController
                    .currentStageDuration,

            enemyCountMultiplier =
                playerStats
                    .enemyCountMultiplier(),

            playerCenterX =
                player.centerX,

            playerCenterY =
                player.centerY,

            mapObstacles =
                mapObstacles
        )

        recordCurrentEnemyEncounters()
    }

    private fun consumeEnemyConditionDamageEvents() {

        enemyManager
            .allEnemies
            .toList()
            .forEach { enemy ->

                enemy.consumeConditionDamageEvents()
                    .forEach { event ->

                        runStatistics.recordDamageDealt(
                            amount =
                                event.appliedDamage,

                            weaponName =
                                event.damageContext.sourceName
                                    ?: event.conditionType.displayName
                        )


                        if (
                            event.killedEnemy
                        ) {
                            handleEnemyKilled(
                                event.enemy
                            )
                        }
                    }
            }
    }

    private fun consumeImpaleAndDragReward() {
        val reward =
            weaponRuntimeManager
                .consumeImpaleAndDragReward()
                ?: return

        if (
            reward.extraGold >
            0
        ) {
            sessionWallet.addGold(
                reward.extraGold.toFloat()
            )

            runStatistics.recordGoldCollected(
                reward.extraGold.toFloat()
            )
        }

        repeat(
            reward.chestRewardCount
        ) {
            chestRewardQueue
                .addChest()
        }
    }

    private fun handleEnemyKilled(
        killedEnemy: Enemy
    ) {
        if(killedEnemy.weaponTargetSummon != null) return
        val explosionDeaths =
            weaponRuntimeManager.resolveErasingSoulsExplosion(
                initialDeadEnemy = killedEnemy,
                player = player,
                enemyManager = enemyManager,
                onDamageDealt = { damage, sourceName ->
                    runStatistics.recordDamageDealt(
                        amount = damage,
                        weaponName = sourceName
                    )
                }
            )

        recordEnemyDeath(killedEnemy)
        // Register deaths only: secondary victims must never trigger another explosion.
        explosionDeaths.forEach(::recordEnemyDeath)
    }

    private fun recordEnemyDeath(
        killedEnemy: Enemy,
        suppressGold: Boolean = false
    ) {
        killedEnemy.registerBabyCrocodileDeath()

        weaponRuntimeManager
            .notifyCharacterTraitEnemyKilled(
                killedEnemy
            )

        ActiveItemEffects.onEnemyKilled(killedEnemy)

        weaponRuntimeManager.onEnemyKilledForActiveSkills(
            killedEnemy
        )

        val isNoLootTotemMiniBoss =
            noLootTotemMiniBosses.remove(killedEnemy)

        val suppressAllLoot = !killedEnemy.dropsLoot

        val isLargeSlime =
            killedEnemy.kindId == com.azizoglu.grisilith.enemy.definition.EnemyKindIds.SLIME &&
                !killedEnemy.isSlimeFragment

        val grantsSwarmFamilyReward =
            killedEnemy.claimSwarmFamilyRewardIfLast(
                enemyManager.allEnemies
            )

        val slimeLootMultiplier =
            if (isLargeSlime) 0f else if (killedEnemy.isSlimeFragment) 0.25f else 1f

        val shouldOpenPortal = when {
            isLargeSlime -> false
            killedEnemy.kindId == com.azizoglu.grisilith.enemy.definition.EnemyKindIds.BEE_SWARM ->
                grantsSwarmFamilyReward && killedEnemy.type == EnemyType.MINI_BOSS
            killedEnemy.isSlimeFragment ->
                enemyManager.allEnemies.none { other ->
                    other !== killedEnemy &&
                        other.kindId == killedEnemy.kindId &&
                        other.isSlimeFragment &&
                        !other.isDead
                }
            else -> killedEnemy.type == EnemyType.MINI_BOSS
        }

        if (!isNoLootTotemMiniBoss && !suppressAllLoot) {
            worldItemRuntime.onEnemyKilled(
                enemy = killedEnemy,
                lootMultiplier = slimeLootMultiplier,
                shouldOpenPortal = shouldOpenPortal
            )
        }

        val awardedGrisilithCoins =
            if (isNoLootTotemMiniBoss || isLargeSlime || suppressAllLoot || !grantsSwarmFamilyReward) {
                0
            } else if (gameMap == GameMap.TEST_MAP) 0 else runRewardTracker.onEnemyKilled(
                enemyType =
                    killedEnemy.type,

                finalBossId =
                    if (
                        killedEnemy.type ==
                        EnemyType.FINAL_BOSS
                    ) {
                        finalBossRewardId
                    } else {
                        null
                    },
                rewardChanceMultiplier =
                    if (killedEnemy.isSlimeFragment) 0.25f else 1f
            )

        if (
            awardedGrisilithCoins >
            0
        ) {
            currencyPickupManager
                .spawnGrisilithCoins(
                    worldX =
                        killedEnemy.centerX,

                    worldY =
                        killedEnemy.centerY,

                    count =
                        awardedGrisilithCoins
                )
        }

        runStatistics.recordEnemyKilled(
            killedEnemy.type
        )

        val goldDrop =
            if (suppressGold || isNoLootTotemMiniBoss || isLargeSlime || suppressAllLoot || !grantsSwarmFamilyReward) 0
            else goldRewardFor(killedEnemy)

        if (
            goldDrop >
            0
        ) {
            currencyPickupManager
                .spawnGoldReward(
                    worldX =
                        killedEnemy.centerX,

                    worldY =
                        killedEnemy.centerY,

                    amount =
                        goldDrop.toFloat()
                )
        }

        if (!isNoLootTotemMiniBoss && !isLargeSlime && !suppressAllLoot &&
            (!killedEnemy.isSlimeFragment || kotlin.random.Random.nextFloat() < 0.25f)
        ) {
            tryDropChestFromEnemy(killedEnemy)
        }
    }

    private fun goldRewardFor(enemy: Enemy): Int =
        (
            ActiveItemEffects.modifyGoldReward(
                GoldEconomyCalculator.enemyDrop(
                enemyType = enemy.type,
                harvestScore = playerStats.totalScore(StatType.HARVEST)
                ),
                enemyType = enemy.type,
                enemyKindId = enemy.kindId
            ) * if (enemy.isSlimeFragment) 0.25f else 1f
        ).roundToInt().coerceAtLeast(if (enemy.isSlimeFragment) 1 else 0)

    private fun applyChestReward(
        reward:
        ChestRewardRoll
    ) {
        when (reward.rewardType) {
            ChestRewardType.STAT -> {
                val statType =
                    reward.statType
                        ?: return

                val appliedAmount =
                    player.addRunStatBonus(
                        statType =
                            statType,

                        amount =
                            reward.statAmount
                    )

                if (
                    appliedAmount > 0 &&
                    statType ==
                    StatType.MAX_HP
                ) {
                    player
                        .synchronizeMaximumHealthWithStats(
                            healAddedHealth =
                                true
                        )
                }
            }

            ChestRewardType.ITEM -> {
                val itemDefinition =
                    reward.itemDefinition
                        ?: return

                val result =
                    itemInventory.addItem(
                        ItemInstance(
                            definition =
                                itemDefinition
                        )
                    )

                if (
                    result !=
                    ItemInventoryResult.ADDED
                ) {
                    return
                }

                itemStatSynchronizer
                    .synchronize()

                if (ActiveItemEffects.applyAcquisition(itemDefinition)) {
                    itemStatSynchronizer.synchronize()
                }

                pickupRangeController.setMultiplier(
                    ActiveItemEffects.pickupRangeMultiplier()
                )

                player
                    .synchronizeMaximumHealthWithStats(
                        healAddedHealth =
                            true
                    )
            }
        }
    }

    private fun tryDropChestFromEnemy(
        killedEnemy: Enemy
    ) {
        val dropSource =
            chestDropSourceFor(
                killedEnemy.type
            )

        val segmentMultiplier =
            chestDropSegmentTracker
                .currentDropChanceMultiplier()

        val shouldDropChest =
            ChestDropCalculator
                .shouldDropChest(
                    source =
                        dropSource,

                    luckScore =
                        playerStats.luckScore(),

                    segmentChanceMultiplier =
                        segmentMultiplier
                )

        if (!shouldDropChest) {
            return
        }

        chestPickupManager.spawn(
            worldX =
                killedEnemy.centerX,

            worldY =
                killedEnemy.centerY
        )

        chestWorldVisualManager.spawn(
            worldX =
                killedEnemy.centerX,

            worldY =
                killedEnemy.centerY
        )

        chestDropSegmentTracker
            .recordDroppedChest()
    }

    private fun chestDropSourceFor(
        enemyType: EnemyType
    ): ChestDropSource {
        return when (enemyType) {
            EnemyType.NORMAL -> {
                ChestDropSource
                    .NORMAL_ENEMY
            }

            EnemyType.UNIQUE -> {
                ChestDropSource
                    .UNIQUE_ENEMY
            }

            EnemyType.MINI_BOSS -> {
                ChestDropSource
                    .MINI_BOSS
            }

            EnemyType.FINAL_BOSS -> {
                ChestDropSource
                    .FINAL_BOSS
            }
        }
    }

    private fun updateStage(
        delta: Float
    ): Boolean {
        if (gameMap == GameMap.TEST_MAP && isTestInfiniteStage) return false
        val normalStageTimeCompleted =
            stageController.update(delta)

        if (!normalStageTimeCompleted) {
            return false
        }

        completeCurrentStage()
        return true
    }

    private fun completeCurrentStage() {
        val completedStage =
            stageController.currentStage

        val completedStageType =
            stageController.currentStageType

        val merchantShouldOpenMarket =
            worldItemRuntime.merchantGuaranteesMarket

        worldItemRuntime.onStageEnded()

        troubleAtHomeDefenseRuntime.onStageEnded()

        summonManager.healVillageGuards(
            0.03f
        )

        bankUncollectedCurrencyForNextStage()

        enemyManager.clearAll()
        damageNumberManager.clear()
        projectiles.clear()
        synchronizeWeaponInventoryEffects()

        weaponRuntimeManager
            .prepareForStageTransition()

        consumeImpaleAndDragReward()

        /*
         * Chests persist for the entire RUN.
         * Stage transitions do not remove or auto-open them.
         */

        if (
            completedStageType ==
            StageType.MINI_BOSS
        ) {
            chestDropSegmentTracker
                .resetForNewBossInterval()
        }

        chestRewardController.begin(
            shouldOpenMarketAfterStatSelection =
                completedStageType ==
                    StageType.MINI_BOSS ||
                    merchantShouldOpenMarket
        )
    }

    private fun renderGame() {
        Gdx.gl.glEnable(
            GL20.GL_BLEND
        )

        Gdx.gl.glBlendFunc(
            GL20.GL_SRC_ALPHA,
            GL20.GL_ONE_MINUS_SRC_ALPHA
        )

        worldRenderer.render(
            mapObstacles =
                mapObstacles,

            player =
                player,

            phantomSteedActive =
                tamgaRuntimeManager
                    .isPhantomSteedVisualActive,

            enemyManager =
                enemyManager,

            summons =
                summonManager
                    .activeSummons,

            currencyPickupManager =
                currencyPickupManager,

            chestWorldVisualManager =
                chestWorldVisualManager,

            projectiles =
                projectiles,

            weaponCarrierVisualSystem =
                weaponCarrierVisualSystem,

            troubleAtHomeDefenseRuntime =
                troubleAtHomeDefenseRuntime,

            showEnemyHealthBars =
                !worldItemRuntime.hidesEnemyHealthBars,
            playerTamgaIcons = tamgaRuntimeManager.activeStatusIconPaths(),

            renderGroundLayer = {
                worldItemRenderer.renderGround(
                    snapshot = worldItemRuntime.visualSnapshot(),
                    camera = camera
                )

                tamgaRuntimeManager.renderWorldVisuals(
                    camera = camera,
                    player = player
                )
            },

            renderBehindEntities = {
                    activeShapeRenderer ->

                ActiveItemEffects.renderLegacyZones(
                    activeShapeRenderer
                )

                weaponRuntimeManager
                    .renderActiveSkillEffects(
                        shapeRenderer =
                            activeShapeRenderer,

                        player =
                            player
                    )

                summonManager.render(
                    shapeRenderer =
                        activeShapeRenderer
                )

                enemySpawner
                    .renderWarnings(
                        shapeRenderer =
                            activeShapeRenderer
                    )
            }
        )

        worldItemRenderer.render(
            snapshot = worldItemRuntime.visualSnapshot(),
            camera = camera,
            ghostDarknessActive = enemyManager.hasLivingGhost
        )

        worldItemRenderer.renderEdgeIndicators(
            snapshot = worldItemRuntime.visualSnapshot(),
            enemies = enemyManager.allEnemies,
            camera = camera
        )

        troubleAtHomeDefenseRuntime.renderTexts(
            batch = hudBatch,
            font = hudFont,
            camera = camera
        )

        Gdx.gl.glDisable(
            GL20.GL_BLEND
        )

        if (
            !isRunSummaryOpen &&
            !isMarketOpen &&
            !chestRewardController.isOpen &&
            !statCardController.isOpen &&
            !chooseToSummonPopup.isOpen &&
            !isPauseMenuOpen
        ) {
            activeTamgaHudRenderer.render(
                showWeaponButton =
                    tamgaRuntimeManager
                        .hasActiveWeaponTamga,

                weaponContentId = selectedPreset?.weaponTamga?.id?.value,

                weaponState =
                    tamgaRuntimeManager
                        .activeWeaponTamgaState,

                weaponRemainingActiveSeconds =
                    tamgaRuntimeManager
                        .activeWeaponTamgaRemainingActiveSeconds,

                weaponRemainingCooldownSeconds =
                    tamgaRuntimeManager
                        .activeWeaponTamgaRemainingCooldownSeconds,

                showArmorButton =
                    tamgaRuntimeManager
                        .hasActiveArmorTamga,

                armorContentId = selectedPreset?.armorTamga?.id?.value,

                armorState =
                    tamgaRuntimeManager
                        .activeArmorTamgaState,

                armorRemainingActiveSeconds =
                    tamgaRuntimeManager
                        .activeArmorTamgaRemainingActiveSeconds,

                armorRemainingCooldownSeconds =
                    tamgaRuntimeManager
                        .activeArmorTamgaRemainingCooldownSeconds
            )
        }

        Gdx.gl.glEnable(
            GL20.GL_BLEND
        )

        Gdx.gl.glBlendFunc(
            GL20.GL_SRC_ALPHA,
            GL20.GL_ONE_MINUS_SRC_ALPHA
        )

        chestRewardController
            .renderShapes()

        statCardController
            .renderShapes()

        if (isRunSummaryOpen) {
            runSummaryRenderer
                .renderShapes(
                    currentRunSummarySnapshot()
                )
        }

        if (isRunSummaryOpen) {

            val summarySnapshot =
                currentRunSummarySnapshot()

            runSummaryStatRenderer
                .renderShapes(
                    stats =
                        summarySnapshot.stats,

                    hoveredIndex =
                        runSummaryStatInteractionController
                            .hoveredStatIndex,

                    selectedEntry =
                        runSummaryStatInteractionController
                            .selectedStat,

                    alpha = runSummaryRevealAlpha()
                )
        }

        Gdx.gl.glDisable(
            GL20.GL_BLEND
        )

        damageNumberManager.collectFrom(
            enemyManager.allEnemies
        )

        damageNumberManager.collectFromSummons(
            summonManager
        )

        damageNumberManager.collectFromGate(
            troubleAtHomeDefenseRuntime
        )
        damageNumberManager.collectFromPlayer(player)

        damageNumberManager.render(
            batch =
                hudBatch,
            font =
                hudFont,
            projectionMatrix =
                camera.combined,
            enabled =
                GameSettings.damageNumbers
        )

        val showGameplayHud =
            !isRunSummaryOpen &&
                !isMarketOpen &&
                !chestRewardController.isOpen &&
                !statCardController.isOpen &&
                !isPauseMenuOpen

        hudRenderer.renderShapes(
            showGameplayHud =
                showGameplayHud
        )

        hudRenderer.render(
            currentScore = currentEnemyScore(),
            displayedStage =
                displayedStage(),

            remainingStageSeconds =
                remainingStageSeconds(),

            formattedGameTime =
                formatGameTime(),

            showGameplayHud =
                showGameplayHud,

            isRunSummaryOpen =
                isRunSummaryOpen,

            isMarketOpen =
                isMarketOpen,

            isShowingChestReward =
                chestRewardController.isOpen,

            isChoosingStatCard =
                statCardController.isOpen,

            renderRunSummary = {

                val summarySnapshot =
                    currentRunSummarySnapshot()

                val selectedSummaryStat =
                    runSummaryStatInteractionController
                        .selectedStat

                if (
                    selectedSummaryStat ==
                    null
                ) {
                    runSummaryRenderer
                        .renderTexts(
                            summarySnapshot,
                            runSummaryRevealAlpha(),
                            summaryScoreElapsed
                        )
                }

                runSummaryStatRenderer
                    .renderTexts(
                        stats =
                            summarySnapshot.stats,

                        hoveredIndex =
                            runSummaryStatInteractionController
                                .hoveredStatIndex,

                        selectedEntry =
                            selectedSummaryStat,

                        alpha = runSummaryRevealAlpha()
                    )

                if (selectedSummaryStat == null) {
                    runSummaryRenderer.renderHoveredTooltip()
                }
            },

            renderMarket = {
                marketRenderer.render(
                    marketSlots =
                        marketSlots,

                    marketRerollsUsed =
                        marketRerollsUsed,

                    itemScrollOffset =
                        itemScrollOffset,

                    interactionState =
                        marketInteractionState
                )
            },

            renderChestReward = {
                chestRewardController
                    .renderTexts()
            },

            renderStatCardSelection = {
                statCardController
                    .renderTexts()
            }
        )

        if (
            showGameplayHud
        ) {
            glyphFeedbackRenderer
                .render(
                    weaponRuntimeManager
                        .glyphFeedback()
                )
        }

        if (showGameplayHud) {
            renderDamageFeedback()
            hudBatch.projectionMatrix = camera.combined
            hudBatch.begin()
            val warning = if (game.online?.status == com.azizoglu.grisilith.online.OnlineBackend.Status.READY) {
                when {
                    gameMap == GameMap.TEST_MAP -> "Not ranked: Test Map"
                    onlineRun?.reason == "not_connected_at_start" -> "Not ranked: started offline"
                    onlineRun?.reason == "connection_lost" -> "Not ranked: connection interrupted"
                    onlineRun?.state == com.azizoglu.grisilith.online.OnlineRun.State.OFFLINE -> "Not ranked: online verification failed"
                    else -> null
                }
            } else null
            onlineUi.connection(hudBatch,game.online,camera.position.x-CAMERA_WIDTH/2f+22f,
                camera.position.y+CAMERA_HEIGHT/2f-23f,warning)
            hudBatch.end()
        }

        if (isPauseMenuOpen) {
            hudBatch.projectionMatrix =
                camera.combined

            hudBatch.begin()

            pauseRenderer.render(
                interactionState =
                    pauseInteractionState
            )

            hudBatch.end()
        }
        chooseToSummonPopup.render()
        chooseGlyphSpellPopup.render()

        if (isRunEndTransitionActive) {
            runSummaryRenderer.renderTransitionOverlay(
                currentRunSummarySnapshot(),
                runEndTransitionElapsed
            )
        }

        if (canShowTestMapControls()) renderTestMapControls()
    }

    private fun renderTestMapControls() {
        val renderLegacyTestControlShapes = false
        val left = camera.position.x - CAMERA_WIDTH / 2f
        val bottom = camera.position.y - CAMERA_HEIGHT / 2f
        val right = left + CAMERA_WIDTH
        val hasTestSettingsModal =
            testCatalogKind != null || isTestOwnedItemsOpen || isTestStatEditorOpen || isTestResetConfirmationOpen
        val pointer = Vector2(Gdx.input.x.toFloat(), Gdx.input.y.toFloat()).also(viewport::unproject)
        fun contains(x: Float, y: Float, width: Float, height: Float) =
            Rectangle(x, y, width, height).contains(pointer)
        var tooltip: String? = null
        var tooltipX = pointer.x + 16f
        var tooltipY = pointer.y + 12f

        if (isTestSettingsOpen && isTestOwnedItemsOpen) {
            val row = ((bottom + 535f - pointer.y) / 34f).toInt()
            if (contains(left + 325f, bottom + 145f, 520f, 390f) && row in 0..11) {
                val index = (testCatalogScroll / 34f).toInt() + row
                tooltip = itemInventory.allItems().getOrNull(index)?.definition?.description
            }
        } else if (isTestSettingsOpen && testCatalogKind != null) {
            val entries = filteredTestCatalogEntries()
            val row = ((bottom + 535f - pointer.y) / 36f).toInt()
            if (contains(left + 275f, bottom + 135f, 620f, 405f) && row in 0..11) {
                val index = (testCatalogScroll / 36f).toInt() + row
                tooltip = when (val entry = entries.getOrNull(index)) {
                    is com.azizoglu.grisilith.item.definition.ItemDefinition -> entry.description
                    is com.azizoglu.grisilith.weapon.definition.WeaponDefinition -> com.azizoglu.grisilith.weapon.definition.WeaponPresentation.hover(entry)
                    else -> null
                }
            }
        } else if (isTestSettingsOpen) {
            (0 until WeaponInventory.WEAPON_SLOT_COUNT).firstOrNull { i ->
                val column = i % 3
                val row = i / 3
                contains(right - 315f + column * 96f, bottom + 358f - row * 104f, 82f, 82f)
            }?.let { slot -> tooltip = weaponInventory.weaponAt(slot)?.let { com.azizoglu.grisilith.weapon.definition.WeaponPresentation.hover(it) } }
            if (contains(right - 315f, bottom + 82f, 82f, 45f)) tooltip = selectedPreset?.weaponTamga?.description
            if (contains(right - 219f, bottom + 82f, 82f, 45f)) tooltip = selectedPreset?.armorTamga?.description
            if (contains(right - 123f, bottom + 82f, 82f, 45f)) tooltip = equippedArmor.definition?.description
        }

        shapeRenderer.projectionMatrix = camera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        if (!isTestSettingsOpen && renderLegacyTestControlShapes) {
            shapeRenderer.color = Color(0.10f, 0.12f, 0.20f, 0.94f)
            shapeRenderer.rect(right - 300f, bottom + 18f, 150f, 42f)
            shapeRenderer.color = if (isTestRunFrozen) Color(0.75f, 0.18f, 0.18f, 0.95f) else Color(0.18f, 0.48f, 0.30f, 0.95f)
            shapeRenderer.rect(right - 140f, bottom + 18f, 120f, 42f)
            shapeRenderer.color = Color(0.24f, 0.20f, 0.45f, 0.96f)
            shapeRenderer.rect(right - 300f, bottom + 66f, 280f, 38f)
        }
        if (isTestSettingsOpen && renderLegacyTestControlShapes) {
            shapeRenderer.color = Color(0.055f, 0.06f, 0.10f, 0.97f)
            shapeRenderer.rect(left + 20f, bottom + 20f, CAMERA_WIDTH - 40f, CAMERA_HEIGHT - 40f)
            fun panel(x: Float, y: Float, width: Float, height: Float, color: Color = Color(0.13f, 0.15f, 0.24f, 1f)) {
                shapeRenderer.color = color
                shapeRenderer.rect(x, y, width, height)
            }
            panel(left + 40f, bottom + 565f, 850f, 70f)
            panel(left + 40f, bottom + 490f, 330f, 62f)
            panel(left + 385f, bottom + 490f, 505f, 62f)
            panel(left + 40f, bottom + 270f, 850f, 208f)
            panel(left + 40f, bottom + 195f, 850f, 65f)
            panel(right - 330f, bottom + 75f, 290f, 555f, Color(0.18f, 0.20f, 0.36f, 1f))
            panel(right - 315f, bottom + 482f, 92f, 100f, Color(0.03f, 0.035f, 0.055f, 1f))
            panel(right - 213f, bottom + 482f, 173f, 34f, Color(0.24f, 0.20f, 0.30f, 1f))
            repeat(6) { i ->
                val column = i % 3
                val row = i / 3
                panel(right - 315f + column * 96f, bottom + 358f - row * 104f, 82f, 82f, Color(0.10f, 0.42f, 0.52f, 1f))
            }
            panel(right - 315f, bottom + 135f, 274f, 62f, Color(0.52f, 0.55f, 0.74f, 1f))
            repeat(3) { i -> panel(right - 315f + i * 96f, bottom + 82f, 82f, 45f, Color(0.12f, 0.12f, 0.48f, 1f)) }

            fun check(x: Float, y: Float, selected: Boolean) {
                shapeRenderer.color = Color(0.025f, 0.03f, 0.05f, 1f)
                shapeRenderer.rect(x, y, 18f, 18f)
                if (selected) {
                    shapeRenderer.color = Color(0.20f, 0.85f, 0.72f, 1f)
                    shapeRenderer.rect(x + 4f, y + 4f, 10f, 10f)
                }
            }
            check(left + 55f, bottom + 600f, isTestEnemySpawningEnabled)
            check(left + 220f, bottom + 600f, !testDummySelected && testEnemyTag == EnemyTag.BEAST)
            check(left + 365f, bottom + 600f, !testDummySelected && testEnemyTag == EnemyTag.HUMANOID)
            check(left + 510f, bottom + 600f, !testDummySelected && testEnemyTag == EnemyTag.UNDEAD)
            check(left + 655f, bottom + 600f, testDummySelected)
            check(left + 55f, bottom + 510f, testObstaclesEnabled)
            check(left + 170f, bottom + 510f, !testObstaclesEnabled)
            TestDeathMode.entries.forEachIndexed { i, mode -> check(left + 430f + i * 145f, bottom + 510f, testDeathMode == mode) }
            check(left + 690f, bottom + 221f, isTestInfiniteStage)

            repeat(50) { index ->
                val stage = index + 1
                val column = index % 10
                val row = index / 10
                panel(
                    left + 55f + column * 78f,
                    bottom + 440f - row * 38f,
                    70f,
                    30f,
                    if (stageController.currentStage == stage) Color(0.20f, 0.62f, 0.48f, 1f) else Color(0.22f, 0.25f, 0.38f, 1f)
                )
            }
            panel(left + 55f, bottom + 210f, 170f, 40f, Color(0.38f, 0.34f, 0.08f, 1f))
            panel(left + 245f, bottom + 210f, 190f, 40f, Color(0.18f, 0.34f, 0.48f, 1f))
            panel(left + 455f, bottom + 210f, 190f, 40f, Color(0.18f, 0.34f, 0.48f, 1f))
            panel(left + 55f, bottom + 65f, 180f, 48f, Color(0.70f, 0.08f, 0.08f, 1f))
            panel(left + 255f, bottom + 65f, 180f, 48f, Color(0.56f, 0.18f, 0.10f, 1f))

            if (hasTestSettingsModal) {
                val modalWidth = if (testCatalogKind != null) 905f else CAMERA_WIDTH - 40f
                panel(left + 20f, bottom + 20f, modalWidth, CAMERA_HEIGHT - 40f, Color(0.035f, 0.04f, 0.07f, 1f))
            }

            if (testCatalogKind != null) {
                panel(left + 255f, bottom + 95f, 660f, 525f, Color(0.035f, 0.04f, 0.075f, 0.995f))
                panel(left + 275f, bottom + 135f, 620f, 405f, Color(0.09f, 0.10f, 0.16f, 1f))
                if (testCatalogKind == "WEAPON") {
                    val start = (testCatalogScroll / 36f).toInt()
                    filteredTestCatalogEntries().drop(start).take(11).forEachIndexed { row, _ ->
                        panel(left + 725f, bottom + 501f - row * 36f, 50f, 28f, Color(0.16f, 0.32f, 0.45f, 1f))
                        panel(left + 780f, bottom + 501f - row * 36f, 50f, 28f, Color(0.22f, 0.38f, 0.25f, 1f))
                        panel(left + 835f, bottom + 501f - row * 36f, 50f, 28f, Color(0.42f, 0.30f, 0.12f, 1f))
                    }
                }
            }
            if (isTestOwnedItemsOpen) {
                panel(left + 305f, bottom + 105f, 560f, 515f, Color(0.035f, 0.04f, 0.075f, 0.995f))
                panel(left + 325f, bottom + 145f, 520f, 390f, Color(0.09f, 0.10f, 0.16f, 1f))
                val visibleItems = itemInventory.allItems().drop((testCatalogScroll / 34f).toInt()).take(11)
                visibleItems.forEachIndexed { row, _ ->
                    panel(left + 755f, bottom + 501f - row * 34f, 80f, 28f, Color(0.56f, 0.12f, 0.12f, 1f))
                }
            }
            if (isTestResetConfirmationOpen) panel(left + 275f, bottom + 275f, 330f, 120f, Color(0.07f, 0.08f, 0.13f, 1f))
            if (isTestStatEditorOpen) {
                panel(left + 120f, bottom + 70f, 1030f, 580f, Color(0.06f, 0.07f, 0.12f, 0.995f))
                testStatGroupBounds(left, bottom).values.forEach { bounds ->
                    panel(bounds.x, bounds.y, bounds.width, bounds.height, Color(0.08f, 0.10f, 0.17f, 1f))
                }
                testStatRows(left, bottom).values.forEach { rowBounds ->
                    repeat(4) { button ->
                        val bounds = testStatAdjustmentBounds(rowBounds, button)
                        panel(bounds.x, bounds.y, bounds.width, bounds.height, Color(0.20f, 0.25f, 0.40f, 1f))
                    }
                }
                panel(right - 230f, bottom + 85f, 150f, 38f, Color(0.32f, 0.18f, 0.24f, 1f))
            }
            if (!hasTestSettingsModal) {
                draggedTestWeaponSlot?.let { slot ->
                    val weapon = weaponInventory.weaponAt(slot) ?: return@let
                    panel(pointer.x - 58f, pointer.y - 30f, 116f, 60f, Color(0.10f, 0.42f, 0.52f, 0.96f))
                }
            }
            tooltip?.let {
                tooltipX = tooltipX.coerceAtMost(right - 375f)
                tooltipY = tooltipY.coerceIn(bottom + 105f, bottom + 610f)
                panel(tooltipX, tooltipY, 340f, 82f, Color(0.02f, 0.025f, 0.045f, 0.99f))
            }
        }
        shapeRenderer.end()
        hudBatch.projectionMatrix = camera.combined
        hudBatch.begin()

        fun uiPanel(x: Float, y: Float, width: Float, height: Float, style: PanelVisualStyle = PanelVisualStyle.ADAPTIVE) {
            UiComponentRenderer.drawPanel(hudBatch, Rectangle(x, y, width, height), style)
        }

        fun uiButton(x: Float, y: Float, width: Float, height: Float, selected: Boolean = false) {
            UiComponentRenderer.drawButton(
                hudBatch,
                Rectangle(x, y, width, height),
                if (selected) ButtonVisualState.SELECTED else ButtonVisualState.NORMAL
            )
        }

        if (!isTestSettingsOpen) {
            UiComponentRenderer.drawCompactButton(
                hudBatch, Rectangle(right - 350f, bottom + 18f, 205f, 44f))
            UiComponentRenderer.drawCompactButton(
                hudBatch, Rectangle(right - 135f, bottom + 18f, 115f, 44f), isTestRunFrozen)
            UiComponentRenderer.drawCompactButton(
                hudBatch, Rectangle(right - 350f, bottom + 70f, 330f, 44f))
        } else {
            uiPanel(left + 20f, bottom + 20f, CAMERA_WIDTH - 40f, CAMERA_HEIGHT - 40f, PanelVisualStyle.MAIN)
            uiPanel(left + 40f, bottom + 565f, 850f, 70f)
            uiPanel(left + 40f, bottom + 490f, 330f, 62f)
            uiPanel(left + 385f, bottom + 490f, 505f, 62f)
            uiPanel(left + 40f, bottom + 270f, 850f, 208f, PanelVisualStyle.INNER)
            uiPanel(left + 40f, bottom + 195f, 850f, 65f)
            uiPanel(right - 330f, bottom + 75f, 290f, 555f, PanelVisualStyle.MAIN)
            uiPanel(right - 315f, bottom + 482f, 92f, 100f)
            uiButton(right - 213f, bottom + 482f, 173f, 34f)

            repeat(6) { i ->
                val column = i % 3
                val row = i / 3
                UiComponentRenderer.drawSlot(
                    hudBatch,
                    Rectangle(right - 315f + column * 96f, bottom + 358f - row * 104f, 82f, 82f)
                )
            }

            uiButton(right - 315f, bottom + 135f, 274f, 62f)
            repeat(3) { i ->
                UiComponentRenderer.drawSlot(
                    hudBatch,
                    Rectangle(right - 315f + i * 96f, bottom + 82f, 82f, 45f)
                )
            }

            fun themedCheck(x: Float, y: Float, selected: Boolean) {
                UiComponentRenderer.drawCheckbox(hudBatch, Rectangle(x, y, 20f, 20f), selected)
            }
            themedCheck(left + 55f, bottom + 600f, isTestEnemySpawningEnabled)
            themedCheck(left + 220f, bottom + 600f, !testDummySelected && testEnemyTag == EnemyTag.BEAST)
            themedCheck(left + 365f, bottom + 600f, !testDummySelected && testEnemyTag == EnemyTag.HUMANOID)
            themedCheck(left + 510f, bottom + 600f, !testDummySelected && testEnemyTag == EnemyTag.UNDEAD)
            themedCheck(left + 655f, bottom + 600f, testDummySelected)
            themedCheck(left + 55f, bottom + 510f, testObstaclesEnabled)
            themedCheck(left + 170f, bottom + 510f, !testObstaclesEnabled)
            TestDeathMode.entries.forEachIndexed { i, mode ->
                themedCheck(left + 430f + i * 145f, bottom + 510f, testDeathMode == mode)
            }
            themedCheck(left + 690f, bottom + 221f, isTestInfiniteStage)

            repeat(50) { index ->
                val stage = index + 1
                val column = index % 10
                val row = index / 10
                uiButton(
                    left + 55f + column * 78f,
                    bottom + 440f - row * 38f,
                    70f,
                    30f,
                    stageController.currentStage == stage
                )
            }
            uiButton(left + 55f, bottom + 210f, 170f, 40f)
            uiButton(left + 245f, bottom + 210f, 190f, 40f)
            uiButton(left + 455f, bottom + 210f, 190f, 40f)
            uiButton(left + 55f, bottom + 65f, 180f, 48f)
            uiButton(left + 255f, bottom + 65f, 180f, 48f)

            if (hasTestSettingsModal) {
                val modalWidth = if (testCatalogKind != null) 905f else CAMERA_WIDTH - 40f
                uiPanel(left + 20f, bottom + 20f, modalWidth, CAMERA_HEIGHT - 40f, PanelVisualStyle.MODAL)
            }
            if (testCatalogKind != null) {
                uiPanel(left + 255f, bottom + 95f, 660f, 525f, PanelVisualStyle.MAIN)
                uiPanel(left + 275f, bottom + 135f, 620f, 405f, PanelVisualStyle.INNER)
            }
            if (isTestOwnedItemsOpen) {
                uiPanel(left + 305f, bottom + 105f, 560f, 515f, PanelVisualStyle.MAIN)
                uiPanel(left + 325f, bottom + 145f, 520f, 390f, PanelVisualStyle.INNER)
            }
            if (isTestResetConfirmationOpen) {
                uiPanel(left + 275f, bottom + 275f, 330f, 120f, PanelVisualStyle.MODAL)
            }
            if (isTestStatEditorOpen) {
                uiPanel(left + 120f, bottom + 70f, 1030f, 580f, PanelVisualStyle.MODAL)
                testStatGroupBounds(left, bottom).values.forEach { bounds ->
                    UiComponentRenderer.drawPanel(hudBatch, bounds, PanelVisualStyle.INNER)
                }
                testStatRows(left, bottom).values.forEach { rowBounds ->
                    repeat(4) { button ->
                        UiComponentRenderer.drawButton(
                            hudBatch,
                            testStatAdjustmentBounds(rowBounds, button),
                            ButtonVisualState.NORMAL
                        )
                    }
                }
            }
            tooltip?.let {
                uiPanel(tooltipX, tooltipY, 340f, 82f, PanelVisualStyle.TOOLTIP)
            }
        }

        hudFont.color = Color.WHITE
        if (!isTestSettingsOpen) {
            val oldX = hudFont.data.scaleX
            val oldY = hudFont.data.scaleY
            hudFont.data.setScale(0.68f)
            hudFont.draw(hudBatch, "RUN SETTINGS", right - 350f, bottom + 47f, 205f,
                com.badlogic.gdx.utils.Align.center, false)
            hudFont.draw(hudBatch, if (isTestRunFrozen) "RESUME [P]" else "PAUSE [P]",
                right - 135f, bottom + 47f, 115f, com.badlogic.gdx.utils.Align.center, false)
            hudFont.draw(hudBatch, "RESET ALL COOLDOWNS [R]", right - 350f, bottom + 99f, 330f,
                com.badlogic.gdx.utils.Align.center, false)
            hudFont.data.setScale(oldX, oldY)
        }
        if (isTestSettingsOpen) {
            hudFont.data.setScale(0.78f)
            fun text(value: String, x: Float, y: Float) = hudFont.draw(hudBatch, value, x, y)
            fun short(value: String, maximum: Int): String =
                if (value.length <= maximum) value else value.take((maximum - 1).coerceAtLeast(1)) + "…"
            fun wrapName(value: String, maximum: Int = 13): List<String> {
                val lines = mutableListOf<String>()
                var current = ""
                value.split(' ').forEach { word ->
                    val candidate = if (current.isEmpty()) word else "$current $word"
                    if (candidate.length <= maximum) current = candidate
                    else {
                        if (current.isNotEmpty()) lines += current
                        current = word
                    }
                }
                if (current.isNotEmpty()) lines += current
                return lines
            }
            fun renderInventorySidebar() {
                text(selectedCharacter.displayName, right - 305f, bottom + 610f)
                ContentIconRenderer.draw(hudBatch, selectedCharacter, Rectangle(right - 315f, bottom + 482f, 92f, 100f))
                text("Stage ${stageController.currentStage}", right - 210f, bottom + 566f)
                text("Gold ${sessionWallet.goldCoins.toInt()}", right - 210f, bottom + 535f)
                text("EDIT STATS", right - 192f, bottom + 504f)
                weaponInventory.weaponSlots.forEachIndexed { i, slot ->
                    val column = i % 3
                    val row = i / 3
                    val weapon = slot.weapon
                    weapon?.let {
                        ContentIconRenderer.draw(
                            hudBatch,
                            it.definition,
                            Rectangle(right - 263f + column * 96f, bottom + 412f - row * 104f, 24f, 24f)
                        )
                    }
                    hudFont.data.setScale(0.55f)
                    wrapName(weapon?.definition?.displayName ?: "Empty").take(3).forEachIndexed { lineIndex, line ->
                        text(line, right - 309f + column * 96f, bottom + 429f - row * 104f - lineIndex * 14f)
                    }
                    if (weapon != null) text("Level ${weapon.level}", right - 309f + column * 96f, bottom + 371f - row * 104f)
                    hudFont.data.setScale(0.78f)
                }
                text("ITEMS (${itemInventory.allItems().size}) — OPEN LIST", right - 305f, bottom + 171f)
                text(short(selectedPreset?.weaponTamga?.displayName ?: "Tamga", 9), right - 310f, bottom + 109f)
                text(short(selectedPreset?.armorTamga?.displayName ?: "Tamga", 9), right - 214f, bottom + 109f)
                text(short(equippedArmor.definition?.displayName ?: "Armor", 9), right - 118f, bottom + 109f)
                selectedPreset?.weaponTamga?.let {
                    ContentIconRenderer.draw(hudBatch, it, Rectangle(right - 315f, bottom + 82f, 45f, 45f))
                }
                selectedPreset?.armorTamga?.let {
                    ContentIconRenderer.draw(hudBatch, it, Rectangle(right - 219f, bottom + 82f, 45f, 45f))
                }
                equippedArmor.definition?.let {
                    ContentIconRenderer.draw(hudBatch, it, Rectangle(right - 123f, bottom + 82f, 45f, 45f))
                }
            }
            if (!hasTestSettingsModal) {
            text("Return to Game", right - 185f, bottom + 680f)
            text("ENEMY SPAWN", left + 50f, bottom + 630f)
            text(if (isTestEnemySpawningEnabled) "On" else "Off", left + 78f, bottom + 616f)
            text("Beast", left + 245f, bottom + 616f); text("Humanoid", left + 390f, bottom + 616f)
            text("Undead", left + 535f, bottom + 616f); text("Test Dummy", left + 680f, bottom + 616f)
            text("OBSTACLES", left + 50f, bottom + 546f); text("On", left + 78f, bottom + 526f); text("Off", left + 193f, bottom + 526f)
            text("AT DEATH", left + 395f, bottom + 546f)
            text("End Run", left + 453f, bottom + 526f); text("Restart Stage", left + 598f, bottom + 526f); text("Immortal", left + 743f, bottom + 526f)
            text("STAGE SELECT", left + 50f, bottom + 472f)
            repeat(50) { index ->
                val stage = index + 1; val column = index % 10; val row = index / 10
                val label = if (stage % 5 == 0) "$stage Boss" else stage.toString()
                text(label, left + 62f + column * 78f, bottom + 461f - row * 38f)
            }
            text("Infinite Stage", left + 716f, bottom + 237f)
            text("ADD 10,000 GOLD (${sessionWallet.goldCoins.toInt()})", left + 64f, bottom + 236f)
            text("ADD ITEM", left + 292f, bottom + 236f); text("ADD WEAPON", left + 492f, bottom + 236f)
            text("RESET MAP", left + 92f, bottom + 95f); text("RESET ENEMIES", left + 278f, bottom + 95f)
            renderInventorySidebar()
            draggedTestWeaponSlot?.let { slot ->
                weaponInventory.weaponAt(slot)?.let { weapon ->
                    hudFont.data.setScale(0.62f)
                    text(short(weapon.definition.displayName, 18), pointer.x - 50f, pointer.y + 12f)
                    text("Lv.${weapon.level}", pointer.x - 18f, pointer.y - 12f)
                    hudFont.data.setScale(0.78f)
                }
            }
            }
            if (testCatalogKind != null) renderInventorySidebar()
            if (isTestResetConfirmationOpen) {
                text("Reset all Test Map changes and return to Stage 1?", left + 300f, bottom + 370f)
                text("YES", left + 340f, bottom + 325f)
                text("NO", left + 485f, bottom + 325f)
            }
            if (isTestStatEditorOpen) {
                text("STAT EDITOR", left + 520f, bottom + 640f)
                testStatGroupBounds(left, bottom).forEach { (group, bounds) ->
                    hudFont.data.setScale(0.68f)
                    hudFont.color = Color(0.72f, 0.78f, 0.90f, 1f)
                    hudFont.draw(hudBatch, group.title, bounds.x, bounds.y + bounds.height - 12f, bounds.width, com.badlogic.gdx.utils.Align.center, false)
                }
                testStatRows(left, bottom).forEach { (stat, rowBounds) ->
                    hudFont.data.setScale(0.68f)
                    StatSymbolRenderer.drawLabel(
                        batch = hudBatch,
                        font = hudFont,
                        statType = stat,
                        x = rowBounds.x,
                        baselineY = rowBounds.y + 20f,
                        iconSize = 18f,
                        width = rowBounds.width - 190f
                    )
                    text(playerStats.totalScore(stat).toString(), rowBounds.x + rowBounds.width - 185f, rowBounds.y + 20f)
                    listOf("-5", "-1", "+1", "+5").forEachIndexed { index, label ->
                        val button = testStatAdjustmentBounds(rowBounds, index)
                        hudFont.draw(hudBatch, label, button.x, button.y + 20f, button.width, com.badlogic.gdx.utils.Align.center, false)
                    }
                }
                text("CLOSE", right - 200f, bottom + 110f)
            }
            testCatalogKind?.let { kind ->
                val entries = filteredTestCatalogEntries()
                text("$kind CATALOG", left + 280f, bottom + 606f); text("CLOSE", left + 850f, bottom + 593f)
                if (kind == "ITEM") {
                    val filters = listOf("All") + com.azizoglu.grisilith.item.definition.ItemRarity.entries.map { it.name.lowercase().replaceFirstChar(Char::uppercase) }
                    filters.forEachIndexed { i, name -> text(name.take(10), left + 283f + i * 95f, bottom + 592f) }
                } else {
                    listOf("All", "Melee", "Ranged", "Spell Focus").forEachIndexed { i, name -> text(name, left + 283f + i * 145f, bottom + 592f) }
                }
                val start = (testCatalogScroll / 36f).toInt()
                entries.drop(start).take(11).forEachIndexed { row, entry ->
                    val label = when (entry) {
                        is com.azizoglu.grisilith.item.definition.ItemDefinition -> "${entry.rarity.name}  ${entry.displayName}"
                        is com.azizoglu.grisilith.weapon.definition.WeaponDefinition -> {
                            val group = when {
                                ContentTag.MELEE_WEAPON in entry.tags -> "MELEE"
                                ContentTag.RANGED_WEAPON in entry.tags -> "RANGED"
                                else -> "SPELL FOCUS"
                            }
                            "$group  ${short(entry.displayName, 24)}"
                        }
                        else -> entry.toString()
                    }
                    when (entry) {
                        is com.azizoglu.grisilith.item.definition.ItemDefinition ->
                            ContentIconRenderer.draw(
                                hudBatch,
                                entry,
                                Rectangle(left + 282f, bottom + 505f - row * 36f, 28f, 28f)
                            )

                        is com.azizoglu.grisilith.weapon.definition.WeaponDefinition ->
                            ContentIconRenderer.draw(
                                hudBatch,
                                entry,
                                Rectangle(left + 282f, bottom + 505f - row * 36f, 28f, 28f)
                            )
                    }
                    text(label, left + 318f, bottom + 526f - row * 36f)
                    if (entry is com.azizoglu.grisilith.weapon.definition.WeaponDefinition) {
                        hudFont.data.setScale(0.58f)
                        text("Lv1", left + 738f, bottom + 521f - row * 36f)
                        text("Lv3", left + 793f, bottom + 521f - row * 36f)
                        text("Lv5", left + 848f, bottom + 521f - row * 36f)
                        hudFont.data.setScale(0.78f)
                    }
                }
            }
            if (isTestOwnedItemsOpen) {
                text("OWNED ITEMS", left + 330f, bottom + 602f)
                text("CLOSE", left + 800f, bottom + 602f)
                val ownedStart = (testCatalogScroll / 34f).toInt()
                itemInventory.allItems().drop(ownedStart).take(11).forEachIndexed { row, item ->
                    ContentIconRenderer.draw(
                        hudBatch,
                        item.definition,
                        Rectangle(left + 334f, bottom + 506f - row * 34f, 27f, 27f)
                    )
                    text("${ownedStart + row + 1}. ${short(item.displayName, 42)}", left + 368f, bottom + 526f - row * 34f)
                    text("DELETE", left + 765f, bottom + 526f - row * 34f)
                }
            }
            tooltip?.let { value ->
                value.chunked(52).take(4).forEachIndexed { index, line -> text(line, tooltipX + 10f, tooltipY + 67f - index * 17f) }
            }
            hudFont.data.setScale(1f)
        }
        hudBatch.end()
    }

    private fun randomVillageGuardProfile(
        stage: Int
    ): Pair<String, com.azizoglu.grisilith.enemy.EnemyStats>? {
        val definition =
            mapEnemyRuntimeContext.registry
                .eligibleKinds(
                    enemyTags = mapEnemyRuntimeContext.profile.enemyTags,
                    enemyType = EnemyType.NORMAL
                )
                .randomOrNull()
                ?: return null

        val stats =
            mapEnemyRuntimeContext.runtimeFactory
                .createStats(
                    definition = definition,
                    enemyType = EnemyType.NORMAL,
                    stage = stage
                )

        return definition.displayName to stats
    }

    private fun updateCamera() {
        val halfCameraWidth =
            CAMERA_WIDTH / 2f

        val halfCameraHeight =
            CAMERA_HEIGHT / 2f

        val minimumCameraX =
            halfCameraWidth

        val maximumCameraX =
            worldWidth - halfCameraWidth

        val minimumCameraY =
            halfCameraHeight

        val maximumCameraY =
            worldHeight - halfCameraHeight

        val baseX =
            player.centerX.coerceIn(
                minimumCameraX,
                maximumCameraX
            )

        val baseY =
            player.centerY.coerceIn(
                minimumCameraY,
                maximumCameraY
            )

        val shakeRatio = (damageFeedbackRemaining / DAMAGE_FEEDBACK_DURATION).coerceIn(0f, 1f)
        val shakeX = if (GameSettings.screenShake && shakeRatio > 0f) {
            MathUtils.random(-damageFeedbackStrength, damageFeedbackStrength) * shakeRatio
        } else 0f
        val shakeY = if (GameSettings.screenShake && shakeRatio > 0f) {
            MathUtils.random(-damageFeedbackStrength, damageFeedbackStrength) * shakeRatio
        } else 0f

        camera.position.x = baseX + shakeX
        camera.position.y = baseY + shakeY

        camera.update()
    }

    private fun triggerDamageFeedback(damage: Int) {
        val damageRatio = damage.toFloat() / player.health.maximumHealth.coerceAtLeast(1).toFloat()
        damageFeedbackStrength = (2.2f + damageRatio * 42f).coerceIn(2.2f, 8.5f)
        damageFeedbackRemaining = DAMAGE_FEEDBACK_DURATION
    }

    private fun renderDamageFeedback() {
        if (damageFeedbackRemaining <= 0f) return
        val ratio = (damageFeedbackRemaining / DAMAGE_FEEDBACK_DURATION).coerceIn(0f, 1f)
        val fullEffect = GameSettings.screenShake
        val maximumThickness = if (fullEffect) 48f else 20f
        val maximumAlpha = if (fullEffect) 0.30f else 0.16f
        val left = camera.position.x - CAMERA_WIDTH / 2f
        val bottom = camera.position.y - CAMERA_HEIGHT / 2f

        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        shapeRenderer.projectionMatrix = camera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        repeat(4) { index ->
            val progress = index / 4f
            val inset = maximumThickness * progress
            val thickness = maximumThickness / 4f
            val alpha = maximumAlpha * ratio * (1f - progress) * (1f - progress)
            shapeRenderer.color = Color(0.72f, 0.015f, 0.025f, alpha)
            shapeRenderer.rect(left + inset, bottom + inset, CAMERA_WIDTH - inset * 2f, thickness)
            shapeRenderer.rect(left + inset, bottom + CAMERA_HEIGHT - inset - thickness, CAMERA_WIDTH - inset * 2f, thickness)
            shapeRenderer.rect(left + inset, bottom + inset, thickness, CAMERA_HEIGHT - inset * 2f)
            shapeRenderer.rect(left + CAMERA_WIDTH - inset - thickness, bottom + inset, thickness, CAMERA_HEIGHT - inset * 2f)
        }
        shapeRenderer.end()
        Gdx.gl.glDisable(GL20.GL_BLEND)
    }

    override fun resize(
        width: Int,
        height: Int
    ) {
        if (width <= 0 || height <= 0) {
            return
        }

        viewport.update(
            width,
            height,
            false
        )
    }

    override fun dispose() {
        onlineRun?.let { game.online?.abandon(it) }
        RunDamageTypeTelemetry.unbind(runStatistics)
        ActiveItemEffects.unbind(playerStats)

        com.azizoglu.grisilith.weapon.visual.attack
            .MeleeAttackVisualManager
            .clear()

        com.azizoglu.grisilith.weapon.visual.attack
            .SoulDrainVisualManager
            .clear()


        if (
            Gdx.input.inputProcessor ===
            marketInputController
        ) {
            Gdx.input.inputProcessor =
                previousInputProcessor
        }

        previousInputProcessor = null

        if (Gdx.input.inputProcessor === testMapScrollProcessor) {
            Gdx.input.inputProcessor = null
        }

        if (Gdx.input.inputProcessor === pauseInputController) {
            Gdx.input.inputProcessor = pausePreviousInputProcessor
        }
        pausePreviousInputProcessor = null

        if (Gdx.input.inputProcessor === runSummaryInputProcessor) {
            Gdx.input.inputProcessor = summaryPreviousInputProcessor
        }
        summaryPreviousInputProcessor = null

        marketRenderer.dispose()
        pauseRenderer.dispose()

        tamgaRuntimeManager.dispose()
        weaponRuntimeManager.dispose()
        worldRenderer.dispose()
        worldItemRenderer.dispose()
        shapeRenderer.dispose()
        hudBatch.dispose()
        hudFont.dispose()
        uiBodyFont.dispose()
        onlineUi.dispose()
        summaryTitleFont.dispose()
    }

    private fun formatGameTime(): String {
        val totalSeconds =
            gameTimeController
                .elapsedGameTimeSeconds
                .toInt()

        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60

        return String.format(
            "%02d:%02d",
            minutes,
            seconds
        )
    }

    private fun displayedStage(): Int {
        return stageController.currentStage.coerceAtMost(
            stageController.maximumStage
        )
    }

    private fun remainingStageSeconds(): Int {
        return kotlin.math.ceil(
            stageController.remainingStageTime
        ).toInt()
    }
    private fun completeBossStageIfDefeated(): Boolean {
        val requiredBossType =
            when (stageController.currentStageType) {
                StageType.NORMAL ->
                    return false

                StageType.MINI_BOSS ->
                    EnemyType.MINI_BOSS

                StageType.FINAL_BOSS ->
                    EnemyType.FINAL_BOSS
            }

        if (!enemySpawner.hasSpawnedBoss) {
            return false
        }

        val bossIsStillAlive =
            enemyManager.hasLivingEnemyOfType(
                requiredBossType
            )

        if (bossIsStillAlive) {
            return false
        }

        if (
            stageController.currentStageType !=
            StageType.FINAL_BOSS
        ) {
            /*
             * Mini Boss öldürüldüğünde Stage erken bitmez.
             * Normal/Unique düşmanlar ve 90 saniyelik Stage süresi
             * tamamlanana kadar oyun devam eder.
             */
            return false
        }

        val completedStage =
            stageController.currentStage

        val completedStageType =
            stageController.currentStageType

        val finalCurrencyLoss =
            currencyPickupManager
                .bankEverythingAtRunEnd(
                    sessionWallet =
                        sessionWallet,

                    runStatistics =
                        runStatistics
                )

        if (
            finalCurrencyLoss
                .grisilithLost >
            0
        ) {
            runRewardTracker
                .discardGrisilithCoins(
                    finalCurrencyLoss
                        .grisilithLost
                )
        }

        chestPickupManager
            .clearUncollectedChests()

        chestWorldVisualManager
            .clear()

        worldItemRuntime.clearForRunEnd()

        enemyManager.clearAll()
        damageNumberManager.clear()
        projectiles.clear()

        weaponRuntimeManager
            .prepareForStageTransition()

        stageController
            .completeCurrentStage()

        return true
    }

    private fun ownedWeaponDefinitionIds():
        Set<String> {

        return (
            0 until
                WeaponInventory
                    .WEAPON_SLOT_COUNT
            )
            .mapNotNull { slotIndex ->
                weaponInventory
                    .weaponAt(
                        slotIndex
                    )
                    ?.definition
                    ?.id
                    ?.value
            }
            .toSet()
    }

    private fun ownedItemDefinitionIds(): Set<String> =
        itemInventory.allItems()
            .map { it.definition.id.value }
            .toSet()

    private fun openMarket() {
        isMarketOpen = true
        marketRerollsUsed = 0
        itemScrollOffset = 0f

        marketInputController.reset()

        previousInputProcessor =
            Gdx.input.inputProcessor

        Gdx.input.inputProcessor =
            marketInputController

        val marketSlotCount =
            5

        /*
         * Önce korunmayan slotların kaç tane
         * olduğunu hesaplıyoruz.
         */
        val missingSlotCount =
            (
                0 until marketSlotCount
                )
                .count { slotIndex ->
                    lockedSlotsForNextMarket
                        .getOrNull(
                            slotIndex
                        ) ==
                        null
                }

        /*
         * Yalnızca boş pozisyonlar için yeni
         * teklifler üretilir.
         */

        val generatedOffers =
            MarketOfferGenerator
                .generateOffers(
                    currentStage =
                        stageController.currentStage,

                    offerCount =
                        missingSlotCount,

                    luckScore =
                        playerStats.luckScore(),

                    selectedCharacter =
                        selectedCharacter,

                    ownedWeaponIds =
                        ownedWeaponDefinitionIds(),

                    ownedItemIds =
                        ownedItemDefinitionIds(),

                    itemEligibility =
                        itemInventory::canAddDefinition
                )

        var generatedOfferIndex =
            0

        marketSlots =
            List(
                marketSlotCount
            ) { slotIndex ->

                val carriedLockedSlot =
                    lockedSlotsForNextMarket
                        .getOrNull(
                            slotIndex
                        )

                if (
                    carriedLockedSlot !=
                    null
                ) {
                    /*
                     * Kilitli kart aynı slot indeksinde
                     * tutulur.
                     */
                    MarketSlot(
                        offer =
                            carriedLockedSlot.offer,

                        isLocked =
                            true,

                        isSold =
                            false
                    )
                } else {
                    /*
                     * Kilitli olmayan pozisyona sıradaki
                     * yeni teklif yerleştirilir.
                     */
                    val generatedOffer =
                        generatedOffers
                            .getOrNull(
                                generatedOfferIndex
                            )

                    generatedOfferIndex++

                    MarketSlot(
                        offer =
                            generatedOffer,

                        isLocked =
                            false,

                        isSold =
                            false
                    )
                }
            }

        /*
         * Taşınan kilitli teklifler yeni markete
         * aktarıldı. Bir sonraki kapanışta yeniden
         * kaydedilecekler.
         */
        lockedSlotsForNextMarket =
            List(
                marketSlotCount
            ) {
                null
            }
    }

    private fun closeMarket() {
        val marketSlotCount =
            5

        /*
         * Her pozisyon ayrı ayrı kaydedilir.
         *
         * Böylece örneğin 3. slotta kilitlenen kart,
         * sonraki markette yine 3. slotta kalır.
         */
        lockedSlotsForNextMarket =
            List(
                marketSlotCount
            ) { slotIndex ->

                val slot =
                    marketSlots.getOrNull(
                        slotIndex
                    )

                if (
                    slot != null &&
                    slot.isLocked &&
                    !slot.isSold &&
                    slot.offer != null
                ) {
                    MarketSlot(
                        offer =
                            slot.offer,

                        isLocked =
                            true,

                        isSold =
                            false
                    )
                } else {
                    null
                }
            }

        if (
            Gdx.input.inputProcessor ===
            marketInputController
        ) {
            Gdx.input.inputProcessor =
                previousInputProcessor
        }

        previousInputProcessor =
            null

        marketInputController.reset()

        isMarketOpen = false

        marketSlots =
            emptyList()

        marketRerollsUsed = 0
        itemScrollOffset = 0f

        enemySpawner.resetTimer()
    }

    private fun handleWeaponDrop(
        sourceSlotIndex: Int,
        targetSlotIndex: Int?,
        droppedOnSellArea: Boolean
    ) {

        val sourceWeapon =
            weaponInventory.weaponAt(
                sourceSlotIndex
            ) ?: return

        if (droppedOnSellArea) {
            requestWeaponSale(
                sourceSlotIndex =
                    sourceSlotIndex
            )

            return
        }

        if (targetSlotIndex == null) {
            return
        }

        if (
            sourceSlotIndex ==
            targetSlotIndex
        ) {
            return
        }

        val targetWeapon =
            weaponInventory.weaponAt(
                targetSlotIndex
            )

        /*
         * Boş slota bırakma:
         * Move işlemi swapSlots ile gerçekleşir.
         */
        if (targetWeapon == null) {
            weaponInventory.swapSlots(
                sourceSlotIndex,
                targetSlotIndex
            )

            weaponRuntimeManager
                .resetAllTimers()

            return
        }

        val sameWeapon =
            sourceWeapon.definition.id ==
                targetWeapon.definition.id

        /*
         * Farklı Weapon:
         * Slotlar yer değiştirir.
         */
        if (!sameWeapon) {
            weaponInventory.swapSlots(
                sourceSlotIndex,
                targetSlotIndex
            )

            weaponRuntimeManager
                .resetAllTimers()

            return
        }

        /*
         * Hedef Lv.5 ise yükseltilemez.
         */
        if (
            targetWeapon.level >=
            WeaponInstance.MAXIMUM_LEVEL
        ) {
            return
        }

        /*
 * Yüksek seviyeli Weapon korunur.
 * Düşük seviyeli Weapon tüketilir.
 *
 * Seviyeler eşitse hedef korunur,
 * kaynak tüketilir.
 */
        val survivingWeapon =
            if (
                sourceWeapon.level >
                targetWeapon.level
            ) {
                sourceWeapon
            } else {
                targetWeapon
            }

        val consumedWeapon =
            if (
                survivingWeapon === sourceWeapon
            ) {
                targetWeapon
            } else {
                sourceWeapon
            }

        val resultLevel =
            (
                survivingWeapon.level +
                    1
                )
                .coerceAtMost(
                    WeaponInstance.MAXIMUM_LEVEL
                )

        /*
         * Tüketilecek Weapon Lv.1 ise doğrudan merge.
         * Lv.2+ değerli bir Weapon kaybedilecekse
         * kullanıcıdan onay alınır.
         */
        if (
            consumedWeapon.level ==
            WeaponInstance.MINIMUM_LEVEL
        ) {
            performWeaponMerge(
                sourceSlotIndex =
                    sourceSlotIndex,

                targetSlotIndex =
                    targetSlotIndex
            )

            return
        }

        marketInteractionState
            .openMergeConfirmation(
                MarketInteractionState
                    .PendingWeaponMerge(
                        sourceSlotIndex =
                            sourceSlotIndex,

                        targetSlotIndex =
                            targetSlotIndex,

                        sourceWeaponName =
                            sourceWeapon.displayName,

                        /*
                         * Popup'taki sourceWeaponLevel alanı
                         * artık tüketilecek Weapon seviyesidir.
                         */
                        sourceWeaponLevel =
                            consumedWeapon.level,

                        /*
                         * Bu alan korunan Weapon seviyesini
                         * temsil eder.
                         */
                        targetWeaponLevel =
                            survivingWeapon.level,

                        resultWeaponLevel =
                            resultLevel
                    )
            )
    }

    private fun requestWeaponSale(
        sourceSlotIndex: Int
    ) {
        val weapon =
            weaponInventory.weaponAt(
                sourceSlotIndex
            ) ?: return

        val salePrice =
            WeaponSalePriceCalculator
                .calculateSalePrice(
                    weaponLevel =
                        weapon.level,

                    currentStage =
                        stageController.currentStage
                )

        /*
         * Lv.1 Weapon doğrudan satılır.
         */
        if (
            weapon.level ==
            WeaponInstance.MINIMUM_LEVEL
        ) {
            performWeaponSale(
                sourceSlotIndex =
                    sourceSlotIndex,

                salePrice =
                    salePrice
            )

            return
        }

        /*
         * Lv.2–5 Weapon için onay gerekir.
         */
        marketInteractionState
            .openSaleConfirmation(
                MarketInteractionState
                    .PendingWeaponSale(
                        sourceSlotIndex =
                            sourceSlotIndex,

                        weaponName =
                            weapon.displayName,

                        weaponLevel =
                            weapon.level,

                        salePrice =
                            salePrice
                    )
            )
    }

    private fun confirmPendingWeaponSale() {
        val pendingSale =
            marketInteractionState
                .pendingWeaponSale
                ?: return

        performWeaponSale(
            sourceSlotIndex =
                pendingSale.sourceSlotIndex,

            salePrice =
                pendingSale.salePrice
        )

        marketInteractionState
            .closeSaleConfirmation()
    }
    private fun performWeaponSale(
        sourceSlotIndex: Int,
        salePrice: Int
    ) {
        val removedWeapon =
            weaponInventory
                .removeWeaponFromSlot(
                    sourceSlotIndex
                )
                ?: return

        sessionWallet.addGold(
            salePrice.toFloat()
        )

        synchronizeWeaponInventoryEffects()

        weaponRuntimeManager
            .resetAllTimers()

        if (
            marketInteractionState
                .detailedWeaponSlotIndex ==
            sourceSlotIndex
        ) {
            marketInteractionState
                .detailedWeaponSlotIndex =
                null
        }
    }

    private fun synchronizeWeaponInventoryEffects() {
        weaponStatBonusSynchronizer
            .invalidate()

        weaponStatBonusSynchronizer
            .synchronize()

        player
            .synchronizeMaximumHealthWithStats(
                healAddedHealth = false
            )
    }

    private fun confirmPendingWeaponMerge() {
        val pendingMerge =
            marketInteractionState
                .pendingWeaponMerge
                ?: return

        performWeaponMerge(
            sourceSlotIndex =
                pendingMerge.sourceSlotIndex,

            targetSlotIndex =
                pendingMerge.targetSlotIndex
        )

        marketInteractionState
            .closeMergeConfirmation()
    }
    private fun performWeaponMerge(
        sourceSlotIndex: Int,
        targetSlotIndex: Int
    ) {
        val result =
            weaponInventory.mergeSlots(
                sourceSlotIndex =
                    sourceSlotIndex,

                targetSlotIndex =
                    targetSlotIndex
            )

        if (
            result ==
            WeaponInventoryResult.MERGED
        ) {
            synchronizeWeaponInventoryEffects()

            weaponRuntimeManager
                .resetAllTimers()
        }
    }

    private fun handleMarketControls(
        delta: Float
    ) {

        marketInteractionState.update(
            delta
        )

        marketInputController
            .handleInput(
                delta =
                    delta,

                onPurchaseOffer = {
                        slotIndex ->

                    purchaseMarketOffer(
                        slotIndex
                    )
                },

                onMarketOfferDropped = {
                        offerIndex,
                        targetWeaponSlotIndex,
                        droppedOnItemPanel,
                        droppedOnStatsPanel ->

                    handleMarketOfferDrop(
                        offerIndex =
                            offerIndex,

                        targetWeaponSlotIndex =
                            targetWeaponSlotIndex,

                        droppedOnItemPanel =
                            droppedOnItemPanel,

                        droppedOnStatsPanel =
                            droppedOnStatsPanel
                    )
                },

                onToggleOfferLock = {
                        slotIndex ->

                    toggleMarketOfferLock(
                        slotIndex
                    )
                },

                onReturnToGame = {
                    closeMarket()
                },

                onReroll = {
                    rerollMarket()
                },

                canDragWeapon = {
                        slotIndex ->

                    weaponInventory
                        .weaponAt(
                            slotIndex
                        ) != null
                },

                onWeaponClicked = {
                        slotIndex ->

                    marketInteractionState
                        .showWeaponDetails(
                            slotIndex
                        )
                },

                onItemClicked = {
                        actualItemIndex ->

                    openItemDetailsByVisibleRow(
                        actualItemIndex
                    )
                },

                onItemScroll = {
                        scrollPixels ->

                    scrollMarketItems(
                        requestedPixels =
                            scrollPixels
                    )
                },

                onTamgaClicked = {
                    marketInteractionState
                        .showTamgaDetails()
                },

                onCloseDetailModal = {
                    marketInteractionState
                        .closeDetailModal()
                },

                onWeaponDropped = {
                        sourceSlotIndex,
                        targetSlotIndex,
                        droppedOnSellArea ->

                    handleWeaponDrop(
                        sourceSlotIndex =
                            sourceSlotIndex,

                        targetSlotIndex =
                            targetSlotIndex,

                        droppedOnSellArea =
                            droppedOnSellArea
                    )
                },

                onConfirmMerge = {
                    confirmPendingWeaponMerge()
                },

                onCancelMerge = {
                    marketInteractionState
                        .closeMergeConfirmation()
                },

                onConfirmSale = {
                    confirmPendingWeaponSale()
                },

                onCancelSale = {
                    marketInteractionState
                        .closeSaleConfirmation()
                }
            )
    }

    private fun showMarketFailure(
        message: String,
        offerIndex: Int? = null
    ) {
        marketInteractionState
            .showFailureFeedback(
                message =
                    message,

                offerIndex =
                    offerIndex
            )
    }

    private fun scrollMarketItems(
        requestedPixels: Float
    ) {
        if (requestedPixels == 0f) {
            return
        }

        val groupedItemCount =
            itemInventory
                .allItems()
                .distinctBy { item ->
                    item.definition.id
                }
                .size

        val layout =
            marketRenderer
                .currentLayout()

        val maximumOffset =
            com.azizoglu.grisilith.screen.market.MarketLayout
                .maximumItemScrollPixels(groupedItemCount, layout.itemPanel)

        itemScrollOffset =
            (
                itemScrollOffset +
                    requestedPixels
                )
                .coerceIn(
                    0f,
                    maximumOffset
                )
    }

    private fun openItemDetailsByVisibleRow(
        visibleRowIndex: Int
    ) {
        /*
         * Renderer ile aynı sıra:
         * İlk görülen kopyanın sırası korunur,
         * aynı Item'lar tek satırda gruplanır.
         */
        val groupedDefinitions =
            itemInventory
                .allItems()
                .distinctBy { item ->
                    item.definition.id
                }

        val itemDefinition =
            groupedDefinitions
                .getOrNull(
                    visibleRowIndex
                )
                ?.definition
                ?: return

        marketInteractionState
            .showItemDetails(
                itemDefinition.id.value
            )
    }
    private fun handleMarketOfferDrop(
        offerIndex: Int,
        targetWeaponSlotIndex: Int?,
        droppedOnItemPanel: Boolean,
        droppedOnStatsPanel: Boolean
    ) {
        val marketSlot =
            marketSlots.getOrNull(
                offerIndex
            ) ?: return

        if (
            marketSlot.isSold ||
            marketSlot.offer == null
        ) {
            return
        }

        val offer =
            marketSlot.offer
                ?: return

        when (offer.productType) {
            MarketProductType.ITEM -> {

                if (!droppedOnItemPanel) {
                    showMarketFailure(
                        message =
                            "DROP ITEM ON ITEMS",

                        offerIndex =
                            offerIndex
                    )

                    return
                }

                purchaseMarketOffer(
                    offerIndex
                )
            }

            MarketProductType.WEAPON -> {
                /*
                 * Weapon belirli bir Weapon slotuna
                 * bırakılmalıdır.
                 */
                val targetSlotIndex =
                    targetWeaponSlotIndex
                        ?: run {
                            showMarketFailure(
                                message =
                                    "DROP ON EMPTY OR MATCHING SLOT",

                                offerIndex =
                                    offerIndex
                            )

                            return
                        }

                purchaseWeaponOfferIntoSlot(
                    offerIndex =
                        offerIndex,

                    targetSlotIndex =
                        targetSlotIndex
                )
            }

            MarketProductType.STAT -> {
                if (!droppedOnStatsPanel) {
                    showMarketFailure(
                        message =
                            "DROP STAT ON STATS",

                        offerIndex =
                            offerIndex
                    )

                    return
                }

                purchaseMarketOffer(
                    offerIndex
                )
            }
        }
    }

    private fun purchaseWeaponOfferIntoSlot(
        offerIndex: Int,
        targetSlotIndex: Int
    ) {
        val marketSlot =
            marketSlots.getOrNull(
                offerIndex
            ) ?: return

        if (marketSlot.isSold) {
            return
        }

        val offer =
            marketSlot.offer
                ?: return

        if (
            offer.productType !=
            MarketProductType.WEAPON
        ) {
            return
        }

        val weaponDefinition =
            offer.weaponDefinition
                ?: return

        if (
            sessionWallet.goldCoins <
            offer.price
        ) {
            showMarketFailure(
                message =
                    "NOT ENOUGH GOLD COIN",

                offerIndex =
                    offerIndex
            )

            return
        }

        val newWeapon =
            WeaponInstance(
                definition =
                    weaponDefinition,

                level =
                    offer.productLevel
                        .coerceIn(
                            WeaponInstance.MINIMUM_LEVEL,
                            WeaponInstance.MAXIMUM_LEVEL
                        )
            )

        val inventoryResult =
            weaponInventory
                .addPurchasedWeaponToSlot(
                    weapon =
                        newWeapon,

                    targetSlotIndex =
                        targetSlotIndex
                )

        val purchaseSucceeded =
            when (inventoryResult) {
                WeaponInventoryResult
                    .ADDED_TO_EMPTY_SLOT,

                WeaponInventoryResult
                    .MERGED,

                WeaponInventoryResult
                    .UNIQUE_WEAPON_LEVELLED_UP -> {
                    true
                }

                WeaponInventoryResult
                    .INVENTORY_FULL,

                WeaponInventoryResult
                    .INCOMPATIBLE_WEAPONS,

                WeaponInventoryResult
                    .MAXIMUM_LEVEL_REACHED,

                WeaponInventoryResult
                    .INVALID_SLOT -> {
                    false
                }
            }

        if (!purchaseSucceeded) {
            val failureMessage =
                when (
                    inventoryResult
                ) {
                    WeaponInventoryResult
                        .INCOMPATIBLE_WEAPONS -> {
                        "DROP ON EMPTY OR MATCHING SLOT"
                    }

                    WeaponInventoryResult
                        .MAXIMUM_LEVEL_REACHED -> {
                        "WEAPON IS ALREADY LEVEL V"
                    }

                    WeaponInventoryResult
                        .INVENTORY_FULL -> {
                        "WEAPON INVENTORY FULL"
                    }

                    else -> {
                        "PURCHASE UNAVAILABLE"
                    }
                }

            showMarketFailure(
                message =
                    failureMessage,

                offerIndex =
                    offerIndex
            )

            return
        }

        val paymentSuccessful =
            sessionWallet.spendGold(
                amount =
                    offer.price
            )

        if (!paymentSuccessful) {
            return
        }

        runStatistics.recordGoldSpent(
            offer.price.toFloat()
        )

        weaponRuntimeManager
            .resetAllTimers()

        marketSlots =
            marketSlots
                .toMutableList()
                .apply {
                    this[offerIndex] =
                        marketSlot.copy(
                            isLocked = false,
                            isSold = true
                        )
                }
    }

    private fun toggleMarketOfferLock(
        slotIndex: Int
    ) {
        val slot =
            marketSlots.getOrNull(slotIndex)
                ?: return

        if (
            slot.isSold ||
            slot.offer == null
        ) {
            return
        }

        marketSlots =
            marketSlots.toMutableList().apply {
                this[slotIndex] =
                    slot.copy(
                        isLocked = !slot.isLocked
                    )
            }
    }

    private fun purchaseMarketOffer(
        slotIndex: Int
    ) {
        val slot =
            marketSlots.getOrNull(
                slotIndex
            ) ?: return

        if (slot.isSold) {
            return
        }

        val offer =
            slot.offer
                ?: return

        /*
         * Yetersiz Gold varsa envantere dokunma.
         */
        if (
            sessionWallet.goldCoins <
            offer.price
        ) {
            showMarketFailure(
                message =
                    "NOT ENOUGH GOLD COIN",

                offerIndex =
                    slotIndex
            )

            return
        }

        val offerApplied =
            applyMarketOffer(
                offer
            )

        if (!offerApplied) {
            showMarketFailure(
                message =
                    purchaseFailureMessage(
                        offer
                    ),

                offerIndex =
                    slotIndex
            )

            return
        }

        if (
            offer.productType ==
            MarketProductType.WEAPON
        ) {
            synchronizeWeaponInventoryEffects()

            weaponRuntimeManager
                .resetAllTimers()
        }

        val paymentSuccessful =
            sessionWallet.spendGold(
                amount =
                    offer.price
            )

        /*
         * Yukarıda bakiye kontrol edildiği için
         * normalde false olmamalı.
         */
        if (!paymentSuccessful) {
            return
        }

        runStatistics.recordGoldSpent(
            offer.price.toFloat()
        )

        marketSlots =
            marketSlots
                .toMutableList()
                .apply {
                    this[slotIndex] =
                        slot.copy(
                            isLocked = false,
                            isSold = true
                        )
                }
    }

    private fun purchaseFailureMessage(
        offer: MarketOffer
    ): String {
        return when (
            offer.productType
        ) {
            MarketProductType.ITEM -> {
                "ITEM LIMIT REACHED"
            }

            MarketProductType.STAT -> {
                "STAT IS ALREADY AT MAXIMUM"
            }

            MarketProductType.WEAPON -> {
                val weaponDefinition =
                    offer.weaponDefinition
                        ?: return "PURCHASE UNAVAILABLE"

                val equippedWeapons =
                    (
                        0 until
                            WeaponInventory
                                .WEAPON_SLOT_COUNT
                        )
                        .mapNotNull { slotIndex ->
                            weaponInventory
                                .weaponAt(
                                    slotIndex
                                )
                        }

                val matchingWeapons =
                    equippedWeapons.filter {
                            weapon ->

                        weapon.definition.id ==
                            weaponDefinition.id
                    }

                when {
                    matchingWeapons.isNotEmpty() &&
                        matchingWeapons.all {
                                weapon ->

                            weapon.level >=
                                WeaponInstance
                                    .MAXIMUM_LEVEL
                        } -> {
                        "WEAPON IS ALREADY LEVEL V"
                    }

                    equippedWeapons.size >=
                        WeaponInventory
                            .WEAPON_SLOT_COUNT -> {
                        "WEAPON INVENTORY FULL"
                    }

                    else -> {
                        "PURCHASE UNAVAILABLE"
                    }
                }
            }
        }
    }

    private fun applyMarketOffer(
        offer: MarketOffer
    ): Boolean {
        return when (
            offer.productType
        ) {
            MarketProductType.WEAPON -> {
                purchaseWeaponOffer(
                    offer
                )
            }

            MarketProductType.ITEM -> {
                purchaseItemOffer(
                    offer
                )
            }

            MarketProductType.STAT -> {
                purchaseStatOffer(
                    offer
                )
            }
        }
    }

    private fun purchaseWeaponOffer(
        offer: MarketOffer
    ): Boolean {
        val weaponDefinition =
            offer.weaponDefinition
                ?: return false

        val newWeapon =
            WeaponInstance(
                definition =
                    weaponDefinition,

                level =
                    offer.productLevel
                        .coerceIn(
                            WeaponInstance.MINIMUM_LEVEL,
                            WeaponInstance.MAXIMUM_LEVEL
                        )
            )

        val result =
            weaponInventory
                .addPurchasedWeapon(
                    newWeapon
                )

        return when (result) {
            WeaponInventoryResult
                .ADDED_TO_EMPTY_SLOT -> {
                true
            }

            WeaponInventoryResult
                .UNIQUE_WEAPON_LEVELLED_UP -> {
                true
            }

            WeaponInventoryResult.MERGED -> {
                true
            }

            WeaponInventoryResult
                .INVENTORY_FULL,

            WeaponInventoryResult
                .INCOMPATIBLE_WEAPONS,

            WeaponInventoryResult
                .MAXIMUM_LEVEL_REACHED,

            WeaponInventoryResult
                .INVALID_SLOT -> {
                false
            }
        }
    }

    private fun purchaseItemOffer(
        offer: MarketOffer
    ): Boolean {
        val itemDefinition =
            offer.itemDefinition
                ?: return false

        val result =
            itemInventory.addItem(
                ItemInstance(
                    definition =
                        itemDefinition
                )
            )

        if (
            result !=
            ItemInventoryResult.ADDED
        ) {
            return false
        }

        itemStatSynchronizer.synchronize()

        if (ActiveItemEffects.applyAcquisition(itemDefinition)) {
            itemStatSynchronizer.synchronize()
        }

        pickupRangeController.setMultiplier(
            ActiveItemEffects.pickupRangeMultiplier()
        )


        player
            .synchronizeMaximumHealthWithStats(
                healAddedHealth = true
            )

        return true
    }

    private fun purchaseStatOffer(
        offer: MarketOffer
    ): Boolean {
        val statType =
            offer.statType
                ?: return false

        val statAmount =
            offer.statAmount

        if (statAmount <= 0) {
            return false
        }

        val appliedAmount =
            player.addRunStatBonus(
                statType =
                    statType,

                amount =
                    statAmount
            )

        return appliedAmount > 0
    }

    private fun rerollMarket() {
        val rerollPrice =
            MarketRerollPriceCalculator
                .calculatePrice(
                    currentStage =
                        stageController.currentStage,
                    rerollsUsedInCurrentMarket =
                        marketRerollsUsed
                )

        if (rerollPrice > 0) {
            val rerollSuccessful =
                sessionWallet.spendGold(
                    amount =
                        rerollPrice
                )

            if (!rerollSuccessful) {
                showMarketFailure(
                    message =
                        "NOT ENOUGH GOLD COIN",

                    offerIndex =
                        null
                )

                return
            }

            runStatistics.recordGoldSpent(
                rerollPrice.toFloat()
            )
        }

        marketRerollsUsed++

        val rerolledSlotCount =
            marketSlots.count { slot ->
                !slot.isLocked ||
                    slot.isSold
            }

        val newOffers =
            MarketOfferGenerator
                .generateOffers(
                    currentStage =
                        stageController.currentStage,

                    offerCount =
                        rerolledSlotCount,

                    luckScore =
                        playerStats.luckScore(),

                    selectedCharacter =
                        selectedCharacter,

                    ownedWeaponIds =
                        ownedWeaponDefinitionIds(),

                    ownedItemIds =
                        ownedItemDefinitionIds(),

                    itemEligibility =
                        itemInventory::canAddDefinition
                )

        var newOfferIndex =
            0

        marketSlots =
            marketSlots.map { slot ->

                /*
                 * Kilitli ve satın alınmamış kart aynı
                 * pozisyonda, aynı teklifle korunur.
                 */
                if (
                    slot.isLocked &&
                    !slot.isSold &&
                    slot.offer != null
                ) {
                    slot
                } else {
                    val newOffer =
                        newOffers.getOrNull(
                            newOfferIndex
                        )

                    newOfferIndex++

                    MarketSlot(
                        offer =
                            newOffer,

                        isLocked =
                            false,

                        isSold =
                            false
                    )
                }
            }
    }

    private fun recordCurrentEnemyEncounters() {

        enemyManager
            .allEnemies
            .forEach { enemy ->

                PlayerProfileRepository
                    .markEnemyEncountered(
                        enemy.kindId?.value
                            ?: CollectionEncounterIds.enemy(
                                enemy.type
                            )
                    )
            }
    }

}
