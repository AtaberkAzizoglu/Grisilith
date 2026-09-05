package com.azizoglu.grisilith.screen

import com.azizoglu.grisilith.combat.damage.DamageNumberManager

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
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.azizoglu.grisilith.util.GameTimeController
import com.badlogic.gdx.Input
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
import com.azizoglu.grisilith.map.enemy.MapEnemyRuntimeContext
import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.character.CharacterDefinition
import com.azizoglu.grisilith.weapon.runtime.WeaponInstance
import com.azizoglu.grisilith.weapon.runtime.WeaponInventory
import com.azizoglu.grisilith.screen.render.HudRenderer
import com.azizoglu.grisilith.screen.render.MarketRenderer
import com.azizoglu.grisilith.screen.render.RunSummaryRenderer
import com.azizoglu.grisilith.summary.RunGradeCalculator
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
import com.azizoglu.grisilith.reward.RunRewardTracker
import com.azizoglu.grisilith.content.CollectionEncounterIds
import com.azizoglu.grisilith.profile.PlayerProfileRepository
import com.azizoglu.grisilith.statistics.career.CompletedRunStatistics
import com.azizoglu.grisilith.statistics.career.HighScoreRepository
import com.azizoglu.grisilith.screen.render.ChestCharacterStatRow
import com.azizoglu.grisilith.screen.render.RunSummaryStatRenderer
import com.azizoglu.grisilith.screen.controller.RunSummaryStatInteractionController
import com.azizoglu.grisilith.summary.RunSummaryStatEntry
import com.azizoglu.grisilith.summary.StatContributionSource
import com.azizoglu.grisilith.summary.StatSourceContribution
import com.azizoglu.grisilith.save.RunSaveBuilder
import com.azizoglu.grisilith.save.RunSaveRepository
import com.azizoglu.grisilith.save.RunSaveSnapshot
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
    ResolvedPresetContent? = null,

    private val restoredRun:
    RunSaveSnapshot? = null
) : ScreenAdapter() {

    companion object {
        private const val CAMERA_WIDTH = 1280f
        private const val CAMERA_HEIGHT = 720f
    }

    private val worldWidth =
        gameMap.worldWidth

    private val worldHeight =
        gameMap.worldHeight

    private val shapeRenderer =
        ShapeRenderer()

    private val camera =
        OrthographicCamera()

    private val worldRenderer =
        WorldRenderer(
            shapeRenderer = shapeRenderer,
            camera = camera,
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
                selectedCharacter.trait.id
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
        EquippedArmor().apply {
            selectedPreset
                ?.armor
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
                difficulty.maximumStage
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
            inventory =
                weaponInventory
        )

    private val mapObstacles: List<MapObstacle> =
        MapObstacleGenerator.generate(
            gameMap = gameMap,
            playerStartX = player.centerX,
            playerStartY = player.centerY
        )

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
                mapObstacles
        )

    private val runStatistics = RunStatistics()

    private val gameTimeController =
        GameTimeController().apply {
            setGameSpeed(
                GameSettings.gameSpeed
            )
        }

    private val hudBatch = SpriteBatch()
    private val hudFont = BitmapFont()
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

    private var isMarketOpen = false

    private var isPauseMenuOpen =
        false

    private var hasInitializedRun =
        false

    private val checkpointSavingEnabled =
        restoredRun == null

    private var marketSlots =
        emptyList<MarketSlot>()

    private var lockedSlotsForNextMarket:
        List<MarketSlot?> =
        List(5) {
            null
        }

    private var marketRerollsUsed = 0

    private var itemScrollOffset = 0

    private var previousInputProcessor:
        InputProcessor? = null

    private var hasCommittedCareerStatistics =
        false

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
        ActiveItemEffects.bind(
            inventory = itemInventory,
            weaponInventory = weaponInventory,
            playerStats = playerStats,
            player = player,
            enemyProvider = { enemyManager.allEnemies },
            summonProvider = { summonManager.activeSummons },
            currentStageProvider = { stageController.currentStage },
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


    /*
     * A loaded checkpoint already contains the wallet state for its stage.
     * The first observed stage after restore must therefore NOT receive the
     * pre-boss growth for a second time.
     */
    private var skipFirstBossGrowthAfterRestore =
        restoredRun !=
            null

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

            shapeRenderer =
                shapeRenderer,

            batch =
                hudBatch,

            font =
                hudFont
        )

    private val runSummaryStatRenderer =
        RunSummaryStatRenderer(
            camera =
                camera,

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
                hudFont,

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
                itemInventory
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
                marketInteractionState
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
                hudFont,

            selectedCharacter =
                selectedCharacter,

            playerStats =
                playerStats,

            weaponInventory =
                weaponInventory,

            itemInventory =
                itemInventory,

            equippedArmor =
                equippedArmor
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
            }
        )

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

            characterStatsProvider = {
                chestCharacterStatRows()
            },

            onOpenMarket = {
                openMarket()
            }
        )

    private fun chestCharacterStatRows():
        List<ChestCharacterStatRow> {

        return listOf(
            ChestCharacterStatRow(
                label = "Max HP",
                value =
                    playerStats
                        .totalScore(
                            StatType.MAX_HP
                        )
                        .toString()
            ),

            ChestCharacterStatRow(
                label = "Armor",
                value =
                    playerStats
                        .totalScore(
                            StatType.ARMOR_CLASS
                        )
                        .toString()
            ),

            ChestCharacterStatRow(
                label = "STR",
                value =
                    playerStats
                        .totalScore(
                            StatType.STRENGTH
                        )
                        .toString()
            ),

            ChestCharacterStatRow(
                label = "DEX",
                value =
                    playerStats
                        .totalScore(
                            StatType.DEXTERITY
                        )
                        .toString()
            ),

            ChestCharacterStatRow(
                label = "WIS",
                value =
                    playerStats
                        .totalScore(
                            StatType.WISDOM
                        )
                        .toString()
            ),

            ChestCharacterStatRow(
                label = "Haste",
                value =
                    playerStats
                        .totalScore(
                            StatType.HASTE
                        )
                        .toString()
            ),

            ChestCharacterStatRow(
                label = "Atk Speed",
                value =
                    playerStats
                        .totalScore(
                            StatType.ATTACK_SPEED
                        )
                        .toString()
            ),

            ChestCharacterStatRow(
                label = "Crit Rate",
                value =
                    playerStats
                        .totalScore(
                            StatType.CRIT_RATE
                        )
                        .toString()
            ),

            ChestCharacterStatRow(
                label = "Crit Dmg",
                value =
                    playerStats
                        .totalScore(
                            StatType.CRIT_DAMAGE
                        )
                        .toString()
            ),

            ChestCharacterStatRow(
                label = "Move Spd",
                value =
                    playerStats
                        .totalScore(
                            StatType.MOVEMENT_SPEED
                        )
                        .toString()
            ),

            ChestCharacterStatRow(
                label = "Luck",
                value =
                    playerStats
                        .totalScore(
                            StatType.LUCK
                        )
                        .toString()
            ),

            ChestCharacterStatRow(
                label = "Harvest",
                value =
                    playerStats
                        .totalScore(
                            StatType.HARVEST
                        )
                        .toString()
            ),

            ChestCharacterStatRow(
                label = "Elemental",
                value =
                    playerStats
                        .totalScore(
                            StatType.ELEMENTAL_DAMAGE
                        )
                        .toString()
            ),

            ChestCharacterStatRow(
                label = "Nature",
                value =
                    playerStats
                        .totalScore(
                            StatType.MIGHT_OF_NATURE
                        )
                        .toString()
            ),

            ChestCharacterStatRow(
                label = "Shepherd",
                value =
                    playerStats
                        .totalScore(
                            StatType.SHEPHERD
                        )
                        .toString()
            ),

            ChestCharacterStatRow(
                label = "Piercing",
                value =
                    playerStats
                        .totalScore(
                            StatType.PIERCING
                        )
                        .toString()
            ),

            ChestCharacterStatRow(
                label = "Enemy #",
                value =
                    playerStats
                        .totalScore(
                            StatType.ENEMY_COUNT
                        )
                        .toString()
            ),

            ChestCharacterStatRow(
                label = "HP Regen",
                value =
                    playerStats
                        .totalScore(
                            StatType.HP_REGENERATION
                        )
                        .toString()
            ),

            ChestCharacterStatRow(
                label = "Life Steal",
                value =
                    playerStats
                        .totalScore(
                            StatType.LIFE_STEAL
                        )
                        .toString()
            )
        )
    }

    private fun currentRunSummaryStats():
        List<RunSummaryStatEntry> {

        return StatType.entries
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
                            ?: "No Weapon Tamga"
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
                            ?: "No Armor Tamga"
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

                        count =
                            groupedItems.size
                    )
                }
                .sortedBy {
                    it.displayName
                }

        val gradeResult =
            RunGradeCalculator
                .calculate(
                    difficulty =
                        difficulty,

                    wasSuccessful =
                        stageController
                            .isRunComplete,

                    stageReached =
                        displayedStage(),

                    enemiesKilled =
                        runStatistics
                            .totalEnemiesKilled,

                    miniBossesDefeated =
                        runStatistics
                            .totalBossesDefeated,

                    finalBossesDefeated =
                        runStatistics
                            .totalFinalBossesDefeated,

                    totalDamageDealt =
                        runStatistics
                            .totalDamageDealt
                            .toFloat(),

                    grisilithCoinsEarned =
                        runRewardTracker
                            .grisilithCoinsEarned
                )

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

            goldCollected =
                runStatistics
                    .totalGoldCollected,

            grisilithCoinsEarned =
                runRewardTracker
                    .grisilithCoinsEarned,

            chestsOpened =
                runStatistics
                    .totalChestsOpened,

            gradeResult =
                gradeResult,

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
                StatType.entries
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
                DefaultItems.all
                    .filter { it.effectId != ItemEffectId.BROKEN_HEART }
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

            characterNameProvider = {
                selectedCharacter
                    .displayName
            },

            characterStatsProvider = {
                chestCharacterStatRows()
            },

            onChestCompleted = {
                runStatistics
                    .recordChestOpened()
            },

            remainingRunDiscardCountProvider = {
                remainingRunChestDiscards
            },

            onRunDiscardUsed = {
                remainingRunChestDiscards =
                    (
                        remainingRunChestDiscards -
                            1
                        )
                        .coerceAtLeast(
                            0
                        )
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
            font = hudFont
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
                hudFont
        )

    override fun show() {
        viewport.apply()

        camera.position.set(
            player.centerX,
            player.centerY,
            0f
        )

        if (!hasInitializedRun) {

            hasInitializedRun =
                true

            if (
                restoredRun ==
                null
            ) {
                HighScoreRepository
                    .recordRunStarted()
            }

            PlayerProfileRepository
                .markMapEncountered(
                    CollectionEncounterIds.map(
                        gameMap
                    )
                )

            val snapshot =
                restoredRun

            if (
                snapshot ==
                null
            ) {
                initializeStartingLoadout()
            } else {
                restoreRunCheckpoint(
                    snapshot
                )
            }

            recordCurrentEnemyEncounters()
        }

        camera.update()
    }



    private fun restoreRunCheckpoint(
        snapshot:
        RunSaveSnapshot
    ) {

        summonManager.clear()
        enemyManager.clearAll()
        damageNumberManager.clear()
        projectiles.clear()
        currencyPickupManager
            .clearAll()

        /*
         * Uncollected chests intentionally survive stage transitions.
         * They remain at their world positions until the player picks them up
         * or the run itself ends/resets.
         */

        chestRewardQueue.clear()

        stageController
            .restoreCheckpointStage(
                snapshot.checkpointStage
            )

        gameTimeController
            .restoreElapsedGameTime(
                snapshot.elapsedRunTimeSeconds
            )

        playerStats
            .restoreSaveLayers(
                snapshot.statLayers
            )

        weaponInventory
            .clearAllSlots()

        snapshot.weaponSlots
            .sortedBy {
                it.slotIndex
            }
            .forEach {
                    savedSlot ->

                val definition =
                    DefaultWeapons.all
                        .firstOrNull {
                            it.id.toString() ==
                                savedSlot.weaponDefinitionId ||
                                it.id.value ==
                                savedSlot.weaponDefinitionId
                        }
                        ?: return@forEach

                val weapon =
                    WeaponInstance(
                        definition =
                            definition,

                        level =
                            savedSlot.level
                                .coerceIn(
                                    WeaponInstance.MINIMUM_LEVEL,
                                    WeaponInstance.MAXIMUM_LEVEL
                                )
                    )

                weapon.restoreIvySummonChoice(
                    IvySummonChoice
                        .fromSavedName(
                            savedSlot
                                .ivySummonChoiceName
                        )
                )

                weapon.restoreChalkDustSpellChoice(
                    ChalkDustSpellChoice
                        .fromSavedName(
                            savedSlot
                                .chalkDustSpellChoiceName
                        )
                )

                weaponInventory
                    .addPurchasedWeaponToSlot(
                        weapon =
                            weapon,

                        targetSlotIndex =
                            savedSlot.slotIndex
                    )
            }

        itemInventory.clear()

        snapshot.itemStacks
            .forEach {
                    savedStack ->

                val definition =
                    DefaultItems.all
                        .firstOrNull {
                            it.id.toString() ==
                                savedStack.itemDefinitionId ||
                                it.id.value ==
                                savedStack.itemDefinitionId
                        }
                        ?: return@forEach

                repeat(
                    savedStack.count
                        .coerceAtLeast(
                            0
                        )
                ) {
                    itemInventory.addItem(
                        ItemInstance(
                            definition =
                                definition
                        )
                    )
                }
            }

        equippedArmor.clear()

        snapshot.armorDefinitionId
            ?.let {
                    savedArmorId ->

                DefaultArmors.all
                    .firstOrNull {
                        it.id.toString() ==
                            savedArmorId ||
                            it.id.value ==
                            savedArmorId
                    }
                    ?.let {
                            definition ->

                        equippedArmor.equip(
                            definition
                        )
                    }
            }

        sessionWallet
            .restoreGold(
                snapshot.sessionGold
            )

        runStatistics
            .restoreSaveState(
                snapshot.statistics
            )

        runRewardTracker
            .restoreSaveState(
                snapshot.rewardState
            )

        remainingRunChestDiscards =
            snapshot.remainingChestDiscards
                .coerceAtLeast(
                    0
                )

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

        ActiveItemEffects.restoreSaveState(
            snapshot.itemEffectState
        )

        pickupRangeController.setMultiplier(
            ActiveItemEffects.pickupRangeMultiplier()
        )

        player
            .synchronizeMaximumHealthWithStats(
                healAddedHealth =
                    false
            )

        player.health
            .restoreCurrentHealth(
                snapshot.currentHealth
            )

        tamgaRuntimeManager.reset()

        weaponRuntimeManager
            .resetAllTimers()

        enemySpawner.resetTimer()

        updateCamera()
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
        handleGameControls(
            delta
        )

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

        if (
            player.health.isDead ||
            stageController.isRunComplete
        ) {

            if (
                !isRunSummaryOpen
            ) {

                /*
                 * Önce run sonunda kalıcı hâle gelmesi gereken
                 * ödül ve istatistikleri kaydediyoruz.
                 */
                runRewardTracker
                    .commitToProfile()

                commitCareerStatistics()

                /*
                 * Run artık başarıyla veya başarısızlıkla sona erdi.
                 * Devam ettirilebilecek aktif bir checkpoint kalmadığı
                 * için Load Game save'i tamamen silinir.
                 */
                RunSaveRepository
                    .delete()
            }

            isRunSummaryOpen =
                true
        }

        openPendingChalkDustSpellChoiceIfReady()
        openPendingIvySummonChoiceIfReady()

        val gameplayStopped =
            player.health.isDead ||
                stageController.isRunComplete ||
                chestRewardController.isOpen ||
                statCardController.isOpen ||
                chooseToSummonPopup.isOpen ||
                chooseGlyphSpellPopup.isOpen ||
                isMarketOpen ||
                isPauseMenuOpen ||
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
        }

        renderGame()
    }

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
                            .grisilithCoinsEarned
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
            Gdx.input.isKeyJustPressed(
                Input.Keys.ESCAPE
            )
        ) {
            openPauseMenu()
            return
        }

        /*
         * Active Tamga kısayolları:
         * Weapon Tamga -> Z
         * Armor Tamga  -> X
         */
        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.Q
            )
        ) {
            tamgaRuntimeManager
                .tryActivateWeaponTamga()
        }

        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.E
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

        val nextSpeed =
            when (
                gameTimeController
                    .gameSpeed
            ) {
                0.5f -> 1f
                1f -> 2f
                else -> 0.5f
            }

        gameTimeController
            .setGameSpeed(
                nextSpeed
            )

        GameSettings
            .setGameSpeed(
                nextSpeed
            )
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
        val weaponSkillKeys =
            intArrayOf(
                Input.Keys.NUM_1,
                Input.Keys.NUM_2,
                Input.Keys.NUM_3,
                Input.Keys.NUM_4,
                Input.Keys.NUM_5,
                Input.Keys.NUM_6
            )

        weaponSkillKeys
            .forEachIndexed {
                    slotIndex,
                    keyCode ->

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
                ?: return

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
    }

    private fun closePauseMenu() {
        isPauseMenuOpen =
            false

        pauseInputController.reset()
    }

    private fun handlePauseControls() {

        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.ESCAPE
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
                game.showSettingsFromPausedRun(
                    pausedRun =
                        this
                )
            }

            PauseAction.QUIT_GAME -> {

                RunSaveRepository.delete()

                game.showMainMenu()

            }

            null -> {
            }
        }
    }
    private fun restartRun() {

        RunSaveRepository.delete()

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
            RunSaveRepository.delete()

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

                RunSaveRepository.delete()

                game.showMainMenu()
            }
        }
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

            if (
                skipFirstBossGrowthAfterRestore
            ) {
                skipFirstBossGrowthAfterRestore =
                    false
            } else {
                applyPreBossStageGoldGrowth()
            }

            lastCurrencyTransferStage =
                stageController.currentStage
        }

        damageNumberManager.update(
            delta =
                delta
        )

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

        player.update(
            delta
        )

        mapObstacles.forEach { obstacle ->
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
                    mapObstacles = mapObstacles
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
                val grisilith = count > 0 && kotlin.random.Random.nextFloat() < 0.01f &&
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
                        appliedDamage
                    )
            }
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

        enemyManager.update(
            delta =
                delta,

            targetX =
                player.centerX,

            targetY =
                player.centerY,

            targetBounds =
                player.bounds,

            summons =
                summonManager
                    .activeSummons,

            worldWidth =
                worldWidth,

            worldHeight =
                worldHeight,

            mapObstacles =
                mapObstacles,

            onPlayerDamaged = {
                    sourceEnemy,
                    damage ->

                val appliedDamage =
                    player.takeDamage(
                        damage
                    )

                if (appliedDamage > 0) {
                    if (ActiveItemEffects.reflectContactDamage(sourceEnemy)) {
                        handleEnemyKilled(sourceEnemy)
                    }
                }

                runStatistics.recordDamageTaken(
                    appliedDamage
                )
            },

            onSummonDamaged = {
                    summon,
                    damage ->

                summon.takeDamage(
                    damage
                )
            }
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

        if (!player.health.isDead) {
            weaponRuntimeManager.update(
                delta = delta,

                player =
                    player,

                enemyManager =
                    enemyManager,

                projectiles =
                    projectiles,                        mapObstacles =
                    mapObstacles,

                worldWidth =
                    worldWidth,

                worldHeight =
                    worldHeight,

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

        /*
         * Aura hasarı oyuncuyu öldürmez ama
         * genel güvenlik için Weapon update'i
         * yalnızca oyuncu hayattaysa çalışır.
         */
        if (!player.health.isDead) {
            weaponRuntimeManager.update(
                delta = delta,

                player =
                    player,

                enemyManager =
                    enemyManager,

                projectiles =
                    projectiles,                        mapObstacles =
                    mapObstacles,

                worldWidth =
                    worldWidth,

                worldHeight =
                    worldHeight,

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

        weaponCarrierVisualSystem
            .update(
                delta =
                    delta,

                player =
                    player,

                runtimeManager =
                    weaponRuntimeManager
            )

        consumeImpaleAndDragReward()

        projectiles.forEach { projectile ->
            projectile.update(
                delta = delta,
                worldWidth = worldWidth,
                worldHeight = worldHeight
            )

            projectile.checkObstacleCollision(
                obstacleBounds =
                    mapObstacles.map {
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

                    tamgaRuntimeManager
                        .modifyWeaponDamage(
                            enemy =
                                enemy,

                            baseDamage =
                                baseDamage
                        )
                }
            )

            /*
             * Only summon-owned projectiles react here.
             * Normal player projectiles return immediately because their
             * sourceSummonRuntimeId is null.
             */
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

        enemySpawner.update(
            delta =
                delta,

            enemyManager =
                enemyManager,

            currentStage =
                stageController
                    .currentStage,

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
        val chainedDeaths =
            weaponRuntimeManager.resolveErasingSoulsChain(
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
        chainedDeaths.forEach(::recordEnemyDeath)
    }

    private fun recordEnemyDeath(
        killedEnemy: Enemy,
        suppressGold: Boolean = false
    ) {
        ActiveItemEffects.onEnemyKilled(killedEnemy)

        weaponRuntimeManager.onEnemyKilledForActiveSkills(
            killedEnemy
        )

        val isNoLootTotemMiniBoss =
            noLootTotemMiniBosses.remove(killedEnemy)

        if (!isNoLootTotemMiniBoss) {
            worldItemRuntime.onEnemyKilled(killedEnemy)
        }

        val awardedGrisilithCoins =
            if (isNoLootTotemMiniBoss) {
                0
            } else runRewardTracker.onEnemyKilled(
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
                    }
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
            if (suppressGold || isNoLootTotemMiniBoss) 0
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

        if (!isNoLootTotemMiniBoss) {
            tryDropChestFromEnemy(killedEnemy)
        }
    }

    private fun goldRewardFor(enemy: Enemy): Int =
        ActiveItemEffects.modifyGoldReward(
            GoldEconomyCalculator.enemyDrop(
                enemyType = enemy.type,
                harvestScore = playerStats.totalScore(StatType.HARVEST)
            ),
            enemyType = enemy.type
        )

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

            showEnemyHealthBars =
                !worldItemRuntime.hidesEnemyHealthBars,

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
            camera = camera
        )

        worldItemRenderer.renderEdgeIndicators(
            snapshot = worldItemRuntime.visualSnapshot(),
            enemies = enemyManager.allEnemies,
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
                            .selectedStat
                )
        }

        Gdx.gl.glDisable(
            GL20.GL_BLEND
        )

        damageNumberManager.collectFrom(
            enemyManager.allEnemies
        )

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
                            summarySnapshot
                        )
                }

                runSummaryStatRenderer
                    .renderTexts(
                        stats =
                            summarySnapshot.stats,

                        selectedEntry =
                            selectedSummaryStat
                    )
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

        camera.position.x =
            player.centerX.coerceIn(
                minimumCameraX,
                maximumCameraX
            )

        camera.position.y =
            player.centerY.coerceIn(
                minimumCameraY,
                maximumCameraY
            )

        camera.update()
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
        ActiveItemEffects.unbind(playerStats)

        com.azizoglu.grisilith.weapon.visual.attack
            .MeleeAttackVisualManager
            .clear()


        if (
            Gdx.input.inputProcessor ===
            marketInputController
        ) {
            Gdx.input.inputProcessor =
                previousInputProcessor
        }

        previousInputProcessor = null

        marketRenderer.dispose()
        pauseRenderer.dispose()

        tamgaRuntimeManager.dispose()
        worldRenderer.dispose()
        worldItemRenderer.dispose()
        shapeRenderer.dispose()
        hudBatch.dispose()
        hudFont.dispose()
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
        itemScrollOffset = 0

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
        itemScrollOffset = 0

        enemySpawner.resetTimer()
        saveCheckpoint()
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
                        visibleRowIndex ->

                    openItemDetailsByVisibleRow(
                        visibleRowIndex
                    )
                },

                onItemScroll = {
                        scrollSteps ->

                    scrollMarketItems(
                        requestedSteps =
                            scrollSteps
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
        requestedSteps: Int
    ) {
        if (requestedSteps == 0) {
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

        val maximumVisibleRows =
            (
                (
                    layout.itemPanel.height -
                        48f
                    ) / 24f
                )
                .toInt()
                .coerceAtLeast(1)

        val maximumOffset =
            (
                groupedItemCount -
                    maximumVisibleRows
                )
                .coerceAtLeast(0)

        itemScrollOffset =
            (
                itemScrollOffset +
                    requestedSteps
                )
                .coerceIn(
                    0,
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

        val actualItemIndex =
            itemScrollOffset +
                visibleRowIndex

        val itemDefinition =
            groupedDefinitions
                .getOrNull(
                    actualItemIndex
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

    private fun buildCheckpointSnapshot() =
        RunSaveBuilder.createSnapshot(
            difficulty =
                difficulty,

            gameMap =
                gameMap,

            characterDefinitionId =
                selectedCharacter
                    .id
                    .toString(),

            characterDisplayName =
                selectedCharacter
                    .displayName,

            presetId =
                selectedPreset
                    ?.preset
                    ?.presetId,

            checkpointStage =
                stageController
                    .currentStage,

            maximumStage =
                stageController
                    .maximumStage,

            elapsedRunTimeSeconds =
                gameTimeController
                    .elapsedGameTimeSeconds,

            currentHealth =
                player
                    .health
                    .currentHealth,

            playerStats =
                playerStats,

            weaponInventory =
                weaponInventory,

            equippedArmor =
                equippedArmor,

            itemInventory =
                itemInventory,

            itemEffectState =
                ActiveItemEffects.exportSaveState(),

            sessionWallet =
                sessionWallet,

            runStatistics =
                runStatistics,

            runRewardTracker =
                runRewardTracker,

            remainingChestDiscards =
                remainingRunChestDiscards,

            weaponTamgaDefinitionId =
                selectedPreset
                    ?.weaponTamga
                    ?.id
                    ?.toString(),

            armorTamgaDefinitionId =
                selectedPreset
                    ?.armorTamga
                    ?.id
                    ?.toString(),

            weaponTamgaRuntimeState =
                null,

            armorTamgaRuntimeState =
                null
        )

    private fun saveCheckpoint() {

        if (
            !checkpointSavingEnabled
        ) {

            Gdx.app.log(
                "RunSave",
                "Checkpoint skipped because this run was loaded from a consumed save."
            )

            return
        }

        try {

            val snapshot =
                buildCheckpointSnapshot()

            val saveSuccessful =
                RunSaveRepository.save(
                    snapshot
                )

            if (saveSuccessful) {

                Gdx.app.log(
                    "RunSave",
                    "Checkpoint saved successfully. " +
                        "Stage: ${snapshot.checkpointStage}"
                )

            } else {

                Gdx.app.error(
                    "RunSave",
                    "Checkpoint save returned false. " +
                        "Stage: ${snapshot.checkpointStage}, " +
                        "validationErrors: " +
                        snapshot.validationErrors()
                            .joinToString()
                )
            }

        } catch (exception: Exception) {

            Gdx.app.error(
                "RunSave",
                "Checkpoint creation or save failed.",
                exception
            )
        }
    }

    private fun recordCurrentEnemyEncounters() {

        enemyManager
            .allEnemies
            .forEach { enemy ->

                PlayerProfileRepository
                    .markEnemyEncountered(
                        CollectionEncounterIds.enemy(
                            enemy.type
                        )
                    )
            }
    }

}
