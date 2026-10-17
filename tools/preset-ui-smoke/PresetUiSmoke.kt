package qa

import com.azizoglu.grisilith.GrisilithGame
import com.azizoglu.grisilith.preset.*
import com.azizoglu.grisilith.screen.preset.PresetJournalScreen
import com.badlogic.gdx.*
import com.badlogic.gdx.backends.lwjgl3.*
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.PixmapIO
import com.badlogic.gdx.files.FileHandle
import java.lang.reflect.Proxy

object PresetUiSmoke {
    @JvmStatic fun main(args: Array<String>) {
        val config = Lwjgl3ApplicationConfiguration().apply {
            setWindowedMode(1280, 720); setInitialVisible(false); disableAudio(true)
            setPreferencesConfig("../tools/preset-ui-smoke/prefs", Files.FileType.Local)
            setForegroundFPS(30)
        }
        Lwjgl3Application(object : ApplicationAdapter() {
            lateinit var screen: PresetJournalScreen
            var frame = 0
            var savedId = ""
            fun call(name: String, vararg args: Any?): Any? {
                val method = screen.javaClass.declaredMethods.first { it.name == name && it.parameterCount == args.size }
                method.isAccessible = true
                return method.invoke(screen, *args)
            }
            fun field(name: String): Any? = screen.javaClass.getDeclaredField(name).apply { isAccessible = true }.get(screen)
            fun capture(name: String) {
                val pixmap = Pixmap.createFromFrameBuffer(0, 0, 1280, 720)
                PixmapIO.writePNG(FileHandle("../tools/preset-ui-smoke/$name.png"), pixmap, -1, true)
                pixmap.dispose()
            }
            fun inputChecks() {
                val realInput = Gdx.input
                var mouseX = 900
                var mouseY = 420
                var held = false
                var pressed = false
                Gdx.input = Proxy.newProxyInstance(Input::class.java.classLoader, arrayOf(Input::class.java)) { _, method, args ->
                    when (method.name) {
                        "getX" -> mouseX
                        "getY" -> mouseY
                        "isButtonPressed" -> held
                        "isButtonJustPressed" -> pressed
                        "isKeyJustPressed" -> false
                        else -> method.invoke(realInput, *(args ?: emptyArray()))
                    }
                } as Input
                fun offset(name: String): Float {
                    val scroll = field(name)!!
                    return scroll.javaClass.getDeclaredField("offset").apply { isAccessible = true }.getFloat(scroll)
                }
                fun tick() { screen.render(1f / 30f); pressed = false }
                fun drag(x: Int, startY: Int, endY: Int) {
                    mouseX = x; mouseY = 720 - startY; held = true; pressed = true; tick()
                    mouseY = 720 - endY; tick(); held = false; tick()
                }
                try {
                    call("act", "plus:0")
                    val statsDraft = field("draft")!!
                    val allocationField = statsDraft.javaClass.getDeclaredField("allocation").apply { isAccessible = true }
                    check((allocationField.get(statsDraft) as PresetStatAllocation).spentPointCount == 1)
                    call("act", "minus:0")
                    check((allocationField.get(statsDraft) as PresetStatAllocation).spentPointCount == 0)
                    call("act", "slot:ITEM")
                    val offeredItems = call("visibleContent") as List<com.azizoglu.grisilith.content.ContentDefinition>
                    check(offeredItems.all { it is com.azizoglu.grisilith.item.definition.ItemDefinition && PresetItemRules.canStartWith(it) })
                    call("act", "filter")
                    val filterRows = call("dropdownRows") as List<Pair<String, String>>
                    check(filterRows.map { it.first }.toSet() == setOf("all", "item_uncommon", "item_rare"))
                    call("act", "closeDropdown")
                    tick()
                    check(Gdx.input.inputProcessor.scrolled(0f, 1f))
                    check(offset("rightScroll") > 0f)
                    val before = offset("rightScroll")
                    drag(900, 300, 350)
                    check(offset("rightScroll") > before)
                    drag(1218, 400, 180)
                    check(offset("rightScroll") > before + 50f)
                    call("act", "slot:ITEM"); call("act", "slot:ITEM"); tick()
                    mouseX = 1005; mouseY = 720 - 405; pressed = true; held = true; tick()
                    held = false; tick()
                    val selected = field("draft")!!
                    val gear = selected.javaClass.getDeclaredField("gear").apply { isAccessible = true }.get(selected) as Map<*, *>
                    check(gear.entries.any { it.key.toString() == "ITEM" && it.value != null })
                    mouseX = 1005; mouseY = 420
                    check(Gdx.input.inputProcessor.scrolled(0f, 3f)); tick()
                    mouseX = 855; mouseY = 720 - 405
                    tick()
                    check(call("target") != "remove")
                    check(Gdx.input.inputProcessor.scrolled(0f, -100f)); tick()
                    check(call("target") == "remove")
                    call("act", "remove")
                    check(gear.entries.any { it.key.toString() == "ITEM" && it.value == null })
                    repeat(5) { call("act", "new") }; tick()
                    mouseX = 180; mouseY = 420
                    check(Gdx.input.inputProcessor.scrolled(0f, 1f))
                    check(offset("leftScroll") > 0f)
                    drag(302, 420, 220)
                    check(offset("leftScroll") > 42f)
                } finally { Gdx.input = realInput }
            }
            override fun create() {
                screen = PresetJournalScreen(GrisilithGame(), createNew = true)
                screen.show(); screen.resize(1280, 720)
                check(call("ready") == false)
                call("act", "slot:WEAPON")
                check(field("openSlot").toString() == "CHARACTER")
            }
            override fun render() {
                screen.render(1f / 30f)
                when (++frame) {
                    3 -> {
                        capture("preset-new"); call("act", "content:0")
                        check(call("ready") == true)
                        call("act", "slot:WEAPON"); check(field("openSlot").toString() == "WEAPON")
                        call("act", "content:0")
                    }
                    6 -> {
                        capture("preset-editor"); call("act", "slot:WEAPON")
                        check(field("openSlot") == null)
                        call("act", "slot:ITEM"); call("act", "filter")
                    }
                    9 -> {
                        capture("preset-filter"); call("act", "option:1")
                        call("act", "content:0"); call("act", "remove"); call("act", "save")
                        check(PresetRepository.presetCount() > 0)
                        savedId = PresetRepository.allPresets().last().presetId
                        val registry = com.azizoglu.grisilith.content.DefaultContentRegistry.instance
                        val savedPreset = PresetRepository.findById(savedId)!!
                        val restrictedItems = registry.definitionsForCategory(com.azizoglu.grisilith.content.ContentCategory.ITEM)
                            .filterIsInstance<com.azizoglu.grisilith.item.definition.ItemDefinition>()
                            .filter { !PresetItemRules.canStartWith(it) }
                        check(restrictedItems.isNotEmpty())
                        restrictedItems.forEach { item ->
                            val legacy = savedPreset.copy(itemId = item.id)
                            check(PresetValidator.validate(legacy, registry) is PresetValidationResult.Invalid)
                            val resolved = PresetContentResolver(registry).resolve(legacy)!!
                            check(resolved.item == null && resolved.preset.itemId == null)
                            check(legacy.itemId == item.id)
                        }
                        call("act", "stats"); call("act", "plus:0")
                    }
                    12 -> {
                        capture("preset-stats"); call("act", "revert"); call("act", "stats")
                        val draft = field("draft")!!
                        val allocation = draft.javaClass.getDeclaredField("allocation").apply { isAccessible = true }.get(draft) as PresetStatAllocation
                        check(allocation.spentPointCount == 0)
                        inputChecks()
                        call("selectPreset", savedId)
                        val count = PresetRepository.presetCount()
                        call("act", "delete")
                        check(field("deleteConfirmation") == true)
                        check(PresetRepository.presetCount() == count)
                        screen.render(1f / 30f); capture("preset-delete")
                        call("act", "cancelDelete")
                        check(PresetRepository.presetCount() == count)
                        call("act", "delete"); call("act", "confirmDelete")
                        check(PresetRepository.presetCount() == count - 1)
                        val run = com.azizoglu.grisilith.screen.PlayMenuScreen(GrisilithGame())
                        run.show(); run.resize(1280, 720); run.render(1f / 30f)
                        capture("run-preview")
                        run.javaClass.getDeclaredField("leftPageLift").apply { isAccessible = true }.setFloat(run, 0.95f)
                        run.render(0f); capture("run-left-corner"); run.dispose()
                        val collection = com.azizoglu.grisilith.screen.CollectionScreen(GrisilithGame())
                        collection.show(); collection.resize(1280, 720); collection.render(1f / 30f)
                        capture("collection-preview"); collection.dispose()
                        val merchant = com.azizoglu.grisilith.screen.MerchantScreen(GrisilithGame())
                        merchant.show(); merchant.resize(1280, 720); merchant.render(1f / 30f)
                        capture("merchant-preview"); merchant.dispose()
                        val chronicle = com.azizoglu.grisilith.screen.HighScoreScreen(GrisilithGame())
                        chronicle.show(); chronicle.resize(1280, 720)
                        val scores = com.azizoglu.grisilith.statistics.career.HighScoreRepository
                        val achievement = com.azizoglu.grisilith.statistics.career.CareerAchievement.ENEMIES
                        val oldTarget = scores.achievementTarget(achievement)
                        scores.commitCompletedRun(com.azizoglu.grisilith.statistics.career.CompletedRunStatistics(true, 60, 2500, 45, 3, 0L, 0L, 0f, 0f, 0))
                        check(scores.hasClaimableAchievementRewards())
                        chronicle.render(1f / 30f); capture("chronicle-preview")
                        val coinsBefore = com.azizoglu.grisilith.profile.PlayerProfileRepository.profile.grisilithCoins
                        val claim = chronicle.javaClass.getDeclaredField("claimAllButton").apply { isAccessible = true }.get(chronicle) as com.azizoglu.grisilith.screen.components.MenuButton
                        val center = com.badlogic.gdx.math.Vector2(claim.bounds.x + 80f, claim.bounds.y + 30f)
                        claim.update(center, true, false); claim.update(center, false, true)
                        check(!scores.hasClaimableAchievementRewards())
                        check(scores.achievementTarget(achievement) > oldTarget)
                        check(com.azizoglu.grisilith.profile.PlayerProfileRepository.profile.grisilithCoins > coinsBefore)
                        check(scores.claimAllAchievementRewards() == 0)
                        chronicle.dispose()
                        println("PRESET_UI_SMOKE_PASS: character gate, gear grid, filters, remove, save, stats, revert, wheel, list drag, scrollbar drag, delete confirmation and claim-all without duplicate rewards")
                        Gdx.app.exit()
                    }
                }
            }
            override fun dispose() { screen.dispose() }
        }, config)
    }
}
