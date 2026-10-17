package com.azizoglu.grisilith.screen.preset

import com.azizoglu.grisilith.GrisilithGame
import com.azizoglu.grisilith.armor.definition.ArmorDefinition
import com.azizoglu.grisilith.character.CharacterDefinition
import com.azizoglu.grisilith.collection.filter.CollectionFilterRegistry
import com.azizoglu.grisilith.config.GameMap
import com.azizoglu.grisilith.content.*
import com.azizoglu.grisilith.preset.*
import com.azizoglu.grisilith.profile.PlayerProfileRepository
import com.azizoglu.grisilith.screen.components.MenuPointer
import com.azizoglu.grisilith.screen.render.PaperTooltipRenderer
import com.azizoglu.grisilith.ui.theme.UiComponentRenderer
import com.azizoglu.grisilith.ui.theme.ButtonVisualState
import com.azizoglu.grisilith.statistics.StatType
import com.azizoglu.grisilith.statistics.StatPresentationGroup
import com.azizoglu.grisilith.item.definition.ItemDefinition
import com.azizoglu.grisilith.tamga.definition.*
import com.azizoglu.grisilith.ui.icon.ContentIconRenderer
import com.azizoglu.grisilith.ui.theme.PaperUi
import com.azizoglu.grisilith.ui.typography.UiFontFactory
import com.azizoglu.grisilith.weapon.definition.WeaponDefinition
import com.badlogic.gdx.*
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g2d.*
import com.badlogic.gdx.math.*
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.FitViewport
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.abs

/** Manager and creation share one journal; edits stay in drafts until Save. */
class PresetJournalScreen(
    private val game: GrisilithGame,
    private val selectedMap: GameMap? = null,
    private val editingPresetId: String? = null,
    private val createNew: Boolean = false
) : ScreenAdapter() {
    private enum class Slot(val label: String, val category: ContentCategory) {
        CHARACTER("Characters", ContentCategory.CHARACTER),
        WEAPON("Weapon", ContentCategory.WEAPON), ARMOR("Armor", ContentCategory.ARMOR),
        ITEM("Item", ContentCategory.ITEM), WEAPON_TAMGA("Weapon Tamga", ContentCategory.TAMGA),
        ARMOR_TAMGA("Armor Tamga", ContentCategory.TAMGA)
    }
    private data class Draft(
        val id: String = UUID.randomUUID().toString(),
        var name: String,
        val gear: MutableMap<Slot, ContentId?> = mutableMapOf(),
        var allocation: PresetStatAllocation = PresetStatAllocation(),
        var budget: Int = PresetConstants.DEFAULT_STAT_POINT_BUDGET + PlayerProfileRepository.profile.purchasedStatPoints,
        var original: RunPreset? = null
    )
    private data class Scroll(val area: Rectangle, val rail: Rectangle, var offset: Float = 0f, var maximum: Float = 0f)
    private val registry = DefaultContentRegistry.instance
    private val camera = OrthographicCamera()
    private val viewport = FitViewport(1280f, 720f, camera)
    private val batch = SpriteBatch()
    private val font = UiFontFactory.createDecorative(22)
    private val body = UiFontFactory.createBody()
    private val paper = PaperUi()
    private val background = Texture(Gdx.files.internal("ui/preset/journal_table.png")).apply {
        setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
    }
    private val pointer = MenuPointer(viewport)
    private val drafts = linkedMapOf<String, Draft>()
    private var draft: Draft? = null
    private var openSlot: Slot? = null
    private var statsOpen = false
    private var characterDetailsOpen = false
    private var dropdown: String? = null
    private val filters = mutableMapOf<String, String>()
    private var sortDescending = false
    private var editingName = false
    private var leaveConfirmation = false
    private var deleteConfirmation = false
    private var leaving = false
    private var message = ""
    private var tooltip: Pair<String, String>? = null
    private var previousInput: InputProcessor? = null
    private var installedInput: InputProcessor? = null
    private val leftScroll = Scroll(Rectangle(49f, 217f, 241f, 276f), Rectangle(299f, 217f, 7f, 276f))
    private val rightScroll = Scroll(Rectangle(814f, 166f, 391f, 323f), Rectangle(1215f, 166f, 7f, 323f))
    private var dragScroll: Scroll? = null
    private var dragRail = false
    private var pressY = 0f
    private var startOffset = 0f
    private var railGrab = 0f
    private var moved = false
    private var pressedTarget: String? = null
    private val statRepeat = com.azizoglu.grisilith.ui.input.AcceleratingPressRepeat()
    private var statPress = false
    private val slots = linkedMapOf(
        Slot.CHARACTER to Rectangle(405f, 443f, 110f, 110f),
        Slot.WEAPON to Rectangle(426f, 329f, 88f, 88f),
        Slot.ARMOR to Rectangle(622f, 329f, 88f, 88f),
        Slot.ITEM to Rectangle(524f, 234f, 88f, 88f),
        Slot.WEAPON_TAMGA to Rectangle(426f, 151f, 88f, 88f),
        Slot.ARMOR_TAMGA to Rectangle(622f, 151f, 88f, 88f)
    )
    private val nameBounds = Rectangle(393f, 556f, 350f, 25f)
    private val addBounds = Rectangle(49f, 501f, 241f, 44f)
    private val saveBounds = Rectangle(385f, 88f, 98f, 36f)
    private val revertBounds = Rectangle(491f, 88f, 100f, 36f)
    private val deleteBounds = Rectangle(599f, 88f, 170f, 36f)
    private val statsBounds = Rectangle(540f, 445f, 190f, 35f)
    private val backBounds = Rectangle(35f, 51f, 130f, 34f)
    private val closeBounds = Rectangle(1183f, 548f, 30f, 30f)
    private val changeCharacterBounds = Rectangle(814f, 500f, 391f, 34f)
    private fun hasRemove() = openSlot != null && openSlot != Slot.CHARACTER
    private val statAvailableBounds = Rectangle(814f, 520f, 245f, 24f)
    private val statResetBounds = Rectangle(1068f, 520f, 137f, 24f)
    private val filterBounds = Rectangle(814f, 500f, 245f, 34f)
    private val sortBounds = Rectangle(1068f, 500f, 137f, 34f)

    override fun show() {
        viewport.apply(); camera.position.set(640f, 360f, 0f); camera.update(); pointer.reset()
        previousInput = Gdx.input.inputProcessor
        Gdx.input.inputProcessor = object : InputAdapter() {
            override fun scrolled(amountX: Float, amountY: Float): Boolean {
                pointer.updatePosition()
                val scroll = when {
                    leftScroll.area.contains(pointer.position) || leftScroll.rail.contains(pointer.position) -> leftScroll
                    rightActive() && (rightScroll.area.contains(pointer.position) || rightScroll.rail.contains(pointer.position)) -> rightScroll
                    else -> return false
                }
                scroll.offset = (scroll.offset + amountY * 42f).coerceIn(0f, scroll.maximum)
                return true
            }
            override fun keyTyped(character: Char): Boolean {
                val current = draft ?: return false
                if (!editingName) return false
                when (character) {
                    '\r', '\n' -> editingName = false
                    '\b' -> current.name = current.name.dropLast(1)
                    else -> if (!character.isISOControl() && current.name.length < PresetConstants.MAXIMUM_PRESET_NAME_LENGTH) current.name += character
                }
                if (character != '\r' && character != '\n') message = "Unsaved changes"
                return true
            }
        }.also { installedInput = it }
        if (createNew) newDraft() else selectPreset(editingPresetId ?: PresetRepository.allPresets().firstOrNull()?.presetId)
    }

    private fun fromPreset(preset: RunPreset) = Draft(
        id = preset.presetId, name = preset.presetName,
        gear = mutableMapOf(Slot.CHARACTER to preset.characterId, Slot.WEAPON to preset.weaponId,
            Slot.ARMOR to preset.armorId, Slot.ITEM to preset.itemId?.takeIf {
                (registry.findById(it) as? ItemDefinition)?.let(PresetItemRules::canStartWith) == true
            },
            Slot.WEAPON_TAMGA to preset.weaponTamgaId, Slot.ARMOR_TAMGA to preset.armorTamgaId),
        allocation = preset.statAllocation,
        budget = maxOf(preset.statPointBudget, PresetConstants.DEFAULT_STAT_POINT_BUDGET + PlayerProfileRepository.profile.purchasedStatPoints),
        original = preset
    )

    private fun selectPreset(id: String?) {
        if (id == null) { newDraft(); return }
        draft = drafts[id] ?: PresetRepository.findById(id)?.let(::fromPreset)?.also { drafts[id] = it }
        editingName = false; closeList(); message = ""
        if (draft?.gear?.get(Slot.CHARACTER) == null) openSlot = Slot.CHARACTER
    }

    private fun newDraft() {
        if (drafts.values.count { it.original == null } + PresetRepository.presetCount() >= PresetConstants.MAXIMUM_PRESET_COUNT) {
            message = "Preset limit reached (${PresetConstants.MAXIMUM_PRESET_COUNT})."; return
        }
        val names = PresetRepository.allPresets().map { it.presetName }.toSet() + drafts.values.map { it.name }
        var number = 1
        while ("Preset $number" in names) number++
        draft = Draft(name = "Preset $number").also { drafts[it.id] = it }
        editingName = false; closeList(); openSlot = Slot.CHARACTER; message = "Choose a character first."
    }

    private fun definition(slot: Slot) = draft?.gear?.get(slot)?.let(registry::findById)
    private fun character() = definition(Slot.CHARACTER) as? CharacterDefinition
    private fun ready() = character() != null
    private fun rightActive() = openSlot != null || statsOpen || characterDetailsOpen
    private fun closeList() { openSlot = null; statsOpen = false; characterDetailsOpen=false; dropdown = null; filters.clear(); rightScroll.offset = 0f }
    private fun open(slot: Slot) {
        if(slot==Slot.CHARACTER && ready()) {
            val wasOpen=characterDetailsOpen
            closeList();characterDetailsOpen=!wasOpen
            return
        }
        if (slot != Slot.CHARACTER && (!ready() || available(slot).isEmpty())) return
        if (openSlot == slot) { closeList(); return }
        closeList(); openSlot = slot
    }

    private fun available(slot: Slot): List<ContentDefinition> = registry.definitionsForCategory(slot.category)
        .filter(ContentUnlockResolver::isUnlocked)
        .filter { it !is ItemDefinition || PresetItemRules.canStartWith(it) }
        .filter { definition ->
            when (slot) {
                Slot.ARMOR -> character()?.canEquipArmor != false
                Slot.ARMOR_TAMGA, Slot.WEAPON_TAMGA -> {
                    val tamga = definition as? TamgaDefinition ?: return@filter false
                    val other = if (slot == Slot.WEAPON_TAMGA) Slot.ARMOR_TAMGA else Slot.WEAPON_TAMGA
                    definition.id != draft?.gear?.get(other) &&
                        (slot != Slot.ARMOR_TAMGA || character()?.canEquipArmor != false) &&
                        TamgaCompatibility.isCompatible(tamga,
                            if (slot == Slot.WEAPON_TAMGA) TamgaPresetSlot.WEAPON_TAMGA_SLOT else TamgaPresetSlot.ARMOR_TAMGA_SLOT,
                            definition(Slot.WEAPON) as? WeaponDefinition, definition(Slot.ARMOR) as? ArmorDefinition)
                }
                else -> true
            }
        }

    private fun visibleContent(): List<ContentDefinition> {
        val slot = openSlot ?: return emptyList()
        val options = filterOptions(slot)
        val filtered = available(slot).filter { entry ->
            filters.values.all { id -> options.firstOrNull { it.id == id }?.matches?.invoke(entry) != false }
        }.sortedBy { it.displayName.lowercase(Locale.ROOT) }
        return if (sortDescending) filtered.reversed() else filtered
    }

    private fun filterOptions(slot: Slot) = CollectionFilterRegistry.optionsFor(slot.category).filter {
        slot != Slot.ITEM || it.id == "item_uncommon" || it.id == "item_rare"
    }

    private fun selectContent(entry: ContentDefinition) {
        val current = draft ?: return
        val slot = openSlot ?: return
        val changed = current.gear[slot] != entry.id
        current.gear[slot] = entry.id
        if (slot == Slot.CHARACTER && changed) {
            current.allocation = PresetStatAllocation()
            if (character()?.canEquipArmor == false) { current.gear[Slot.ARMOR] = null; current.gear[Slot.ARMOR_TAMGA] = null }
        }
        validateTamgas()
        if(slot==Slot.CHARACTER) { closeList();characterDetailsOpen=true }
        message = "Unsaved changes"
    }

    private fun validateTamgas() {
        for (slot in listOf(Slot.WEAPON_TAMGA, Slot.ARMOR_TAMGA)) {
            val entry = definition(slot) ?: continue
            if (entry !in available(slot)) draft?.gear?.set(slot, null)
        }
    }

    private fun save() {
        val current = draft ?: return
        val char = current.gear[Slot.CHARACTER] ?: return
        val weapon = current.gear[Slot.WEAPON] ?: run { message = "Choose a weapon before saving."; return }
        if (current.name.isBlank()) { message = "Enter a preset name."; return }
        val preset = RunPreset(presetId = current.id, presetName = current.name.trim(), characterId = char, weaponId = weapon,
            armorId = current.gear[Slot.ARMOR], itemId = current.gear[Slot.ITEM],
            weaponTamgaId = current.gear[Slot.WEAPON_TAMGA], armorTamgaId = current.gear[Slot.ARMOR_TAMGA],
            statPointBudget = current.budget, statAllocation = current.allocation,
            createdAtEpochMillis = current.original?.createdAtEpochMillis ?: System.currentTimeMillis())
        val result = if (current.original == null) PresetRepository.create(preset, registry) else PresetRepository.update(preset, registry)
        when (result) {
            is PresetRepositoryResult.Success -> {
                val saved = result.preset ?: preset
                draft = fromPreset(saved).also { drafts[it.id] = it }; editingName = false; message = "All changes saved"
            }
            is PresetRepositoryResult.Failure -> message = result.message
        }
    }

    private fun revert() {
        val current = draft ?: return
        if (current.original != null) draft = fromPreset(current.original!!).also { drafts[it.id] = it }
        else { current.gear.clear(); current.allocation = PresetStatAllocation() }
        closeList(); if (!ready()) openSlot = Slot.CHARACTER
        editingName = false; message = "Changes reverted"
    }

    private fun dirty(current: Draft): Boolean {
        val original = current.original ?: return true
        return current.name != original.presetName || current.allocation != original.statAllocation ||
            current.gear[Slot.CHARACTER] != original.characterId || current.gear[Slot.WEAPON] != original.weaponId ||
            current.gear[Slot.ARMOR] != original.armorId || current.gear[Slot.ITEM] != original.itemId ||
            current.gear[Slot.WEAPON_TAMGA] != original.weaponTamgaId || current.gear[Slot.ARMOR_TAMGA] != original.armorTamgaId
    }

    override fun render(delta: Float) {
        pointer.update()
        leftScroll.maximum = (presetRows().size * 88f - leftScroll.area.height).coerceAtLeast(0f)
        val rightHeight = if(characterDetailsOpen)characterDetailsHeight() else if (statsOpen) 0f else ((visibleContent().size + (if (hasRemove()) 1 else 0) + 2) / 3) * 148f + 12f
        rightScroll.maximum = (rightHeight - rightScroll.area.height).coerceAtLeast(0f)
        leftScroll.offset = leftScroll.offset.coerceIn(0f, leftScroll.maximum)
        rightScroll.offset = rightScroll.offset.coerceIn(0f, rightScroll.maximum)
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            when { editingName -> editingName = false; deleteConfirmation -> deleteConfirmation = false; leaveConfirmation -> leaveConfirmation = false
                rightActive() -> if (ready()) closeList() else requestBack(); else -> requestBack() }
        }
        input(delta)
        if (leaving) return
        Gdx.gl.glClearColor(0.04f, 0.025f, 0.015f, 1f); Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        batch.projectionMatrix = camera.combined; batch.begin(); batch.color = Color.WHITE
        batch.draw(background, 0f, 0f, 1280f, 720f)
        tooltip = null
        renderLeft(); renderMiddle(); renderRight()
        UiComponentRenderer.drawCompactButton(batch, backBounds, backBounds.contains(pointer.position))
        text("BACK", backBounds, 0.72f, color = Color(0.94f, 0.91f, 0.84f, 1f))
        if (message.isNotEmpty()) {
            val note = Rectangle(414f, 42f, 330f, 30f)
            paper.button(batch, note)
            text(message, note, 0.48f, wrap = true)
        }
        if (deleteConfirmation) renderDeleteConfirmation() else if (leaveConfirmation) renderLeaveConfirmation() else renderTooltip()
        batch.end()
    }

    private fun presetRows(): List<Draft> {
        val saved = PresetRepository.allPresets().map { drafts[it.presetId] ?: fromPreset(it).also { d -> drafts[d.id] = d } }
        return drafts.values.filter { it.original == null } + saved
    }

    private fun rowBounds(scroll: Scroll, index: Int, height: Float): Rectangle = Rectangle(
        scroll.area.x, scroll.area.y + scroll.area.height - (index + 1) * height + scroll.offset + 4f,
        scroll.area.width, height - 8f
    )

    private fun clip(area: Rectangle, block: () -> Unit) {
        batch.flush()
        val bottom = viewport.project(Vector2(area.x, area.y))
        val top = viewport.project(Vector2(area.x + area.width, area.y + area.height))
        Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST)
        Gdx.gl.glScissor(bottom.x.toInt(), bottom.y.toInt(), (top.x - bottom.x).toInt(), (top.y - bottom.y).toInt())
        block(); batch.flush(); Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST)
    }

    private fun renderLeft() {
        text("PRESETS", Rectangle(48f, 551f, 243f, 30f), 0.85f)
        button("+", addBounds, scale = 1.2f)
        if (addBounds.contains(pointer.position)) tooltip = "New preset" to "Create a new preset and choose its character."
        clip(leftScroll.area) {
            presetRows().forEachIndexed { index, row ->
                val bounds = rowBounds(leftScroll, index, 88f)
                if (!bounds.overlaps(leftScroll.area)) return@forEachIndexed
                paper.button(batch, bounds, row.id == draft?.id)
                val portrait = Rectangle(bounds.x + 5f, bounds.y + 9f, 58f, 58f)
                row.gear[Slot.CHARACTER]?.let(registry::findById)?.let { ContentIconRenderer.draw(batch, it, portrait) }
                    ?: text("+", portrait, 1f)
                text(row.name, Rectangle(bounds.x + 69f, bounds.y + 49f, 163f, 23f), 0.51f, Align.left)
                val charName = row.gear[Slot.CHARACTER]?.let(registry::findById)?.displayName ?: "Choose character"
                text(charName, Rectangle(bounds.x + 69f, bounds.y + 28f, 163f, 20f), 0.43f, Align.left)
                val timestamp = PresetUsageRepository.lastUsed(row.id)
                val date = if (timestamp == 0L) "Never used" else SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ROOT).format(Date(timestamp))
                text(date, Rectangle(bounds.x + 69f, bounds.y + 8f, 163f, 20f), 0.37f, Align.left)
            }
        }
        drawScrollbar(leftScroll)
        text("TOTAL PRESETS: ${presetRows().size}", Rectangle(48f, 174f, 247f, 26f), 0.52f)
    }

    private fun renderMiddle() {
        val current = draft ?: return
        text(current.name + if (editingName) "|" else "", nameBounds, 0.85f)
        if (nameBounds.contains(pointer.position)) tooltip = "Preset name" to "Click to rename. Press Enter to finish editing."
        slots.forEach { (slot, bounds) ->
            val definition = definition(slot)
            val locked = slot != Slot.CHARACTER && (!ready() || available(slot).isEmpty())
            if (definition != null) ContentIconRenderer.draw(batch, definition, bounds, framed = slot.category != ContentCategory.TAMGA)
            else {
                if (slot.category != ContentCategory.TAMGA) paper.button(batch, bounds, openSlot == slot, !locked)
                if (locked) batch.draw(lockTexture, bounds.x + bounds.width * 0.2f, bounds.y + bounds.height * 0.2f, bounds.width * 0.6f, bounds.height * 0.6f)
                else text("+", bounds, 1.05f)
            }
            if (slot != Slot.CHARACTER) text(slot.label.uppercase(), Rectangle(bounds.x - 10f, bounds.y + bounds.height + 1f, bounds.width + 20f, 20f), 0.46f)
            if (slot!=Slot.CHARACTER && bounds.contains(pointer.position)) tooltip = definition?.let { it.displayName to ContentPresentation.hover(it) }
                ?: (slot.label to if (locked) "No unlocked compatible content is available for this slot." else "Choose ${slot.label.lowercase()}. Click again to close the list.")
        }
        text(character()?.displayName ?: "Choose a character", Rectangle(532f, 484f, 206f, 67f), 0.85f, wrap = true)
        button("EDIT STATS", statsBounds, enabled = ready(), selected = statsOpen, scale = 0.48f)
        button("SAVE", saveBounds, enabled = ready() && definition(Slot.WEAPON) != null && current.name.isNotBlank(), scale = 0.5f)
        button("REVERT", revertBounds, enabled = ready(), scale = 0.5f)
        paper.button(batch, deleteBounds, selected = deleteBounds.contains(pointer.position), danger = true)
        text("DELETE PRESET", deleteBounds, 0.55f, color = Color(0.16f, 0.025f, 0.015f, 1f))
    }

    private val lockTexture = Texture(Gdx.files.internal("ui/content_icons/frames/locked_overlay.png"))

    private fun renderRight() {
        if (!rightActive()) {
            text("Select a portrait or build slot\nto choose its contents.", Rectangle(842f, 345f, 341f, 110f), 0.65f, wrap = true)
            return
        }
        if(characterDetailsOpen) { renderCharacterDetails();return }
        text(if (statsOpen) "STAT ALLOCATION" else openSlot!!.label.uppercase(), Rectangle(815f, 547f, 352f, 34f), 0.8f)
        button("X", closeBounds, enabled = ready(), scale = 0.65f)
        if (statsOpen) {
            val current = draft ?: return
            text("AVAILABLE STAT POINTS: ${current.budget - current.allocation.spentPointCount}", statAvailableBounds, 0.50f)
            button("RESET", statResetBounds, scale = 0.5f)
            StatPresentationGroup.ordered.forEachIndexed { groupIndex, group ->
                val x = 814f + (groupIndex % 2) * 200f
                val top = if (groupIndex < 2) 510f else 320f
                text(group.title, Rectangle(x, top - 18f, 190f, 18f), 0.52f)
                group.stats.forEach { stat ->
                    val row = statRow(stat)
                    val score = (character()?.baseStats?.get(stat) ?: 0) + current.allocation.bonusFor(stat)
                    font.data.setScale(.48f)
                    com.azizoglu.grisilith.ui.stat.StatSymbolRenderer.drawLabel(batch,font,stat,row.x,row.y+27f,14f)
                    if(statLabelBounds(stat).contains(pointer.position)) tooltip=stat.displayName to com.azizoglu.grisilith.statistics.StatDetailPresentation.description(stat)
                    text("$score (+${current.allocation.bonusFor(stat)})", Rectangle(row.x, row.y, 54f, 14f), 0.40f)
                    listOf("-", "+").forEachIndexed { i, label ->
                        button(label, statControl(stat, i), scale = 0.44f)
                    }
                }
            }
        } else {
            val options = filterOptions(openSlot!!)
            if (options.isNotEmpty()) {
                val label = if (filters.isEmpty()) "All" else options.filter { it.id in filters.values }.joinToString(", ") { it.displayName }
                button("$label  v", filterBounds, scale = 0.5f)
            }
            button(if (sortDescending) "Z - A  v" else "A - Z  v", sortBounds, scale = 0.5f)
            clip(rightScroll.area) {
                if (hasRemove()) button("REMOVE", gridCell(0), enabled = ready(), scale = 0.60f)
                visibleContent().forEachIndexed { index, entry ->
                    val bounds = gridBounds(index)
                    if (!bounds.overlaps(rightScroll.area)) return@forEachIndexed
                    paper.button(batch, bounds, entry.id == draft?.gear?.get(openSlot))
                    ContentIconRenderer.draw(batch, entry, Rectangle(bounds.x + 17f, bounds.y + 41f, 88f, 88f), framed = entry.category != ContentCategory.TAMGA)
                    fittedName(entry.displayName, Rectangle(bounds.x + 8f, bounds.y + 12f, bounds.width - 16f, 29f))
                    if (entry !is CharacterDefinition && bounds.contains(pointer.position) && rightScroll.area.contains(pointer.position)) {
                        tooltip = entry.displayName to ContentPresentation.hover(entry)
                    }
                }
            }
            if (visibleContent().isEmpty()) text("No matching content", rightScroll.area, 0.6f)
        }
        if (!statsOpen) drawScrollbar(rightScroll)
        renderDropdown()
    }

    private fun characterDetailCopy(selected:CharacterDefinition)=listOf(
        selected.displayName to .98f,
        com.azizoglu.grisilith.character.CharacterPresentation.summary(selected) to .76f,
        "Trait — ${selected.trait.displayName}" to .83f,
        selected.trait.description to .72f
    )

    private fun characterDetailsHeight():Float {
        val selected=character() ?: return 0f
        val sx=body.data.scaleX;val sy=body.data.scaleY
        var height=240f
        characterDetailCopy(selected).forEach{(copy,scale)->
            body.data.setScale(scale)
            height+=com.azizoglu.grisilith.ui.stat.StatSymbolRenderer.drawRichText(batch,body,copy,0f,0f,391f,measureOnly=true)+10f
        }
        body.data.setScale(sx,sy)
        return height
    }

    private fun renderCharacterDetails() {
        val selected=character() ?: return
        text("CHARACTER",Rectangle(815f,547f,352f,34f),.8f)
        button("X",closeBounds,scale=.65f)
        button("CHANGE CHARACTER",changeCharacterBounds,scale=.55f)
        val ink=Color(.20f,.11f,.055f,1f)
        clip(rightScroll.area) {
        var y=482f+rightScroll.offset
        fun paragraph(copy:String,scale:Float) {
            body.data.setScale(scale);body.color=ink
            y-=com.azizoglu.grisilith.ui.stat.StatSymbolRenderer.drawRichText(batch,body,copy,814f,y,391f)+10f
        }
        characterDetailCopy(selected).forEach{(copy,scale)->paragraph(copy,scale)}
        val groupTop=y-4f
        StatPresentationGroup.ordered.forEachIndexed { i,group ->
            val x=814f+(i%2)*201f;val top=groupTop-(i/2)*124f
            body.data.setScale(.63f);body.color=ink;body.draw(batch,group.title,x,top)
            group.stats.forEachIndexed { row,stat ->
                val baseline=top-20f-row*16f
                body.data.setScale(.60f)
                com.azizoglu.grisilith.ui.stat.StatSymbolRenderer.drawLabel(batch,body,stat,x,baseline,12f)
                body.color=ink
                body.draw(batch,selected.baseStats.getValue(stat).toString(),x+171f,baseline,18f,Align.right,false)
            }
        }
        }
        drawScrollbar(rightScroll)
    }

    private fun drawScrollbar(scroll: Scroll) {
        val visible = scroll.area.height / (scroll.area.height + scroll.maximum)
        paper.scrollbar(batch, scroll.rail, visible, if (scroll.maximum > 0) scroll.offset / scroll.maximum else 0f)
    }
    private fun gridBounds(index: Int): Rectangle {
        return gridCell(index + if (hasRemove()) 1 else 0)
    }
    private fun gridCell(position: Int): Rectangle {
        return Rectangle(rightScroll.area.x + (position % 3) * 130f,
            rightScroll.area.y + rightScroll.area.height - (position / 3 + 1) * 148f + rightScroll.offset + 4f, 122f, 140f)
    }
    private fun statRow(stat: StatType): Rectangle {
        val groupIndex = StatPresentationGroup.ordered.indexOfFirst { stat in it.stats }
        val index = StatPresentationGroup.ordered[groupIndex].stats.indexOf(stat)
        val top = if (groupIndex < 2) 510f else 320f
        return Rectangle(814f + (groupIndex % 2) * 200f, top - 18f - (index + 1) * 28f, 190f, 28f)
    }
    private fun statControl(stat: StatType, index: Int): Rectangle {
        val row = statRow(stat)
        return Rectangle(row.x + 98f + index * 43f, row.y, 38f, 14f)
    }
    private fun fittedName(value: String, bounds: Rectangle) {
        var scale = 0.50f
        do {
            font.data.setScale(scale)
            val layout = GlyphLayout(font, value, font.color, bounds.width - 8f, Align.center, true)
            if (layout.height <= bounds.height - 8f && layout.width <= bounds.width - 8f) break
            scale -= 0.01f
        } while (scale > 0.15f)
        text(value, bounds, scale, wrap = true)
    }

    private fun dropdownRows(): List<Pair<String, String>> = when (dropdown) {
        "filter" -> listOf("all" to "All") + filterOptions(openSlot!!).map { it.id to it.displayName }
        "sort" -> listOf("asc" to "A - Z", "desc" to "Z - A")
        else -> emptyList()
    }
    private fun popupBounds(): Rectangle {
        val control = if (dropdown == "sort") sortBounds else filterBounds
        return Rectangle(control.x, control.y - dropdownRows().size * 35f - 14f, control.width, dropdownRows().size * 35f + 14f)
    }
    private fun popupRow(index: Int): Rectangle {
        val bounds = popupBounds()
        return Rectangle(bounds.x + 7f, bounds.y + bounds.height - 7f - (index + 1) * 35f, bounds.width - 14f, 32f)
    }
    private fun renderDropdown() {
        if (dropdown == null) return
        paper.popup(batch, popupBounds()); tooltip = null
        dropdownRows().forEachIndexed { index, entry -> button(entry.second, popupRow(index), selected = entry.first in filters.values, scale = 0.5f) }
    }

    private fun target(): String? {
        val point = pointer.position
        if (deleteConfirmation) return when {
            Rectangle(474f, 277f, 148f, 36f).contains(point) -> "confirmDelete"
            Rectangle(656f, 277f, 148f, 36f).contains(point) -> "cancelDelete"
            else -> null
        }
        if (leaveConfirmation) return when {
            Rectangle(474f, 277f, 148f, 36f).contains(point) -> "leave"
            Rectangle(656f, 277f, 148f, 36f).contains(point) -> "stay"
            else -> null
        }
        if (dropdown != null) {
            dropdownRows().indices.firstOrNull { popupRow(it).contains(point) }?.let { return "option:$it" }
            return "closeDropdown"
        }
        if (backBounds.contains(point)) return "back"
        if (addBounds.contains(point)) return "new"
        if (nameBounds.contains(point)) return "name"
        if (deleteBounds.contains(point)) return "delete"
        if (leftScroll.area.contains(point)) presetRows().indices.firstOrNull { rowBounds(leftScroll, it, 88f).contains(point) }?.let { return "preset:$it" }
        slots.entries.firstOrNull { it.value.contains(point) && (it.key == Slot.CHARACTER || ready()) }?.let { return "slot:${it.key.name}" }
        if (!ready() && openSlot != Slot.CHARACTER) return null
        if (ready()) {
            if (saveBounds.contains(point)) return "save"
            if (revertBounds.contains(point)) return "revert"
            if (statsBounds.contains(point)) return "stats"
            if (closeBounds.contains(point) && rightActive()) return "close"
        }
        if (!rightActive()) return null
        if(characterDetailsOpen) return if(changeCharacterBounds.contains(point)) "changeCharacter" else null
        if (statsOpen) {
            if (statResetBounds.contains(point)) return "resetStats"
            StatType.alphabetical.forEachIndexed { index, stat ->
                listOf("minus", "plus").forEachIndexed { control, action ->
                    if (statControl(stat, control).contains(point)) return "$action:$index"
                }
            }
        } else {
            if (filterBounds.contains(point) && filterOptions(openSlot!!).isNotEmpty()) return "filter"
            if (sortBounds.contains(point)) return "sort"
            if (ready() && hasRemove() && rightScroll.area.contains(point) && gridCell(0).contains(point)) return "remove"
            if (rightScroll.area.contains(point)) visibleContent().indices.firstOrNull { gridBounds(it).contains(point) }?.let { return "content:$it" }
        }
        return null
    }

    private fun statLabelBounds(stat:StatType):Rectangle {
        val row=statRow(stat);val sx=font.data.scaleX;val sy=font.data.scaleY
        font.data.setScale(.58f)
        val label=GlyphLayout(font,stat.displayName)
        val result=Rectangle(row.x+19f,row.y+28f-label.height,minOf(label.width,169f),maxOf(7f,label.height-2f))
        font.data.setScale(sx,sy)
        return result
    }
    private fun input(delta:Float) {
        if (pointer.justPressed) {
            pressedTarget = target(); editingName = pressedTarget == "name"
            statPress=statsOpen && (pressedTarget?.startsWith("plus:")==true || pressedTarget?.startsWith("minus:")==true)
            if(statPress) { act(pressedTarget!!);statRepeat.begin(pressedTarget!!) } else statRepeat.reset()
            dragScroll = if (deleteConfirmation || leaveConfirmation || dropdown != null) null else listOf(leftScroll, rightScroll).firstOrNull {
                (it != rightScroll || (rightActive() && !statsOpen)) && (it.area.contains(pointer.position) || Rectangle(it.rail.x - 6f, it.rail.y, it.rail.width + 12f, it.rail.height).contains(pointer.position))
            }
            moved = false; pressY = pointer.position.y
            dragScroll?.let {
                startOffset = it.offset
                dragRail = !it.area.contains(pointer.position)
                val thumb = paper.thumb(it.rail, it.area.height / (it.area.height + it.maximum), if (it.maximum > 0) it.offset / it.maximum else 0f)
                railGrab = if (thumb.contains(pointer.position)) pointer.position.y - thumb.y else thumb.height / 2f
                if (dragRail) updateRail(it)
            }
        }
        if(statPress && statsOpen && !deleteConfirmation && !leaveConfirmation) {
            repeat(statRepeat.update(delta,pointer.isHeld,target())) { pressedTarget?.let(::act) }
        } else statRepeat.reset()
        if (pointer.isHeld) dragScroll?.let {
            if (abs(pointer.position.y - pressY) > 5f) moved = true
            if (dragRail) updateRail(it)
            else if (moved) it.offset = (startOffset + pointer.position.y - pressY).coerceIn(0f, it.maximum)
        }
        if (pointer.justReleased) {
            if (!statPress && !moved && !dragRail && pressedTarget != null && pressedTarget == target()) act(pressedTarget!!)
            statRepeat.reset();statPress=false
            dragScroll = null; dragRail = false; pressedTarget = null
        }
    }
    private fun updateRail(scroll: Scroll) {
        val thumb = paper.thumb(scroll.rail, scroll.area.height / (scroll.area.height + scroll.maximum), 0f)
        val travel = scroll.rail.height - thumb.height
        if (travel > 0) scroll.offset = ((1f - (pointer.position.y - railGrab - scroll.rail.y) / travel) * scroll.maximum).coerceIn(0f, scroll.maximum)
    }

    private fun act(target: String) {
        val current = draft
        val value = target.substringAfter(':', "")
        when (target.substringBefore(':')) {
            "new" -> newDraft()
            "delete" -> deleteConfirmation = true
            "cancelDelete" -> deleteConfirmation = false
            "confirmDelete" -> {
                if (current != null) {
                    if (current.original != null) PresetRepository.delete(current.id)
                    drafts.remove(current.id)
                    val next = presetRows().firstOrNull()?.id
                    draft = null; deleteConfirmation = false
                    selectPreset(next)
                    message = "Preset deleted"
                }
            }
            "preset" -> selectPreset(presetRows().getOrNull(value.toInt())?.id)
            "slot" -> open(Slot.valueOf(value))
            "content" -> visibleContent().getOrNull(value.toInt())?.let(::selectContent)
            "save" -> if (ready()) save()
            "revert" -> if (ready()) revert()
            "name" -> editingName = true
            "stats" -> { val wasOpen = statsOpen; closeList(); statsOpen = !wasOpen }
            "changeCharacter" -> { closeList();openSlot=Slot.CHARACTER }
            "close" -> closeList()
            "remove" -> { current?.gear?.set(openSlot!!, null); validateTamgas(); message = "Unsaved changes" }
            "filter", "sort" -> dropdown = target
            "closeDropdown" -> dropdown = null
            "option" -> {
                val option = dropdownRows()[value.toInt()]
                if (dropdown == "sort") sortDescending = option.first == "desc"
                else if (option.first == "all") filters.clear()
                else filterOptions(openSlot!!).firstOrNull { it.id == option.first }?.let {
                    if (filters[it.groupId] == it.id) filters.remove(it.groupId) else filters[it.groupId] = it.id
                }
                dropdown = null; rightScroll.offset = 0f
            }
            "resetStats" -> { current?.allocation = PresetStatAllocation(); message = "Unsaved changes" }
            "plus", "minus" -> if (current != null) {
                val stat = StatType.alphabetical[value.toInt()]
                current.allocation = if (target.startsWith("plus")) current.allocation.increase(stat, character()?.baseStats?.get(stat) ?: 0, current.budget)
                    else current.allocation.decrease(stat)
                message = "Unsaved changes"
            }
            "back" -> requestBack()
            "leave" -> leave()
            "stay" -> leaveConfirmation = false
        }
    }

    private fun requestBack() {
        if (drafts.values.any(::dirty)) leaveConfirmation = true
        else leave()
    }
    private fun leave() {
        leaving = true
        game.showPlayMenu(selectedPresetId = draft?.original?.presetId, selectedMap = selectedMap)
    }
    private fun renderLeaveConfirmation() {
        paper.popup(batch, Rectangle(442f, 250f, 395f, 190f))
        text("Discard unsaved changes?", Rectangle(467f, 340f, 346f, 55f), 0.8f, wrap = true)
        button("DISCARD", Rectangle(474f, 277f, 148f, 36f), scale = 0.52f)
        button("KEEP EDITING", Rectangle(656f, 277f, 148f, 36f), scale = 0.5f)
    }
    private fun renderDeleteConfirmation() {
        paper.popup(batch, Rectangle(442f, 250f, 395f, 190f))
        text("Delete this preset?\nThis cannot be undone.", Rectangle(467f, 337f, 346f, 70f), 0.72f, wrap = true)
        paper.button(batch, Rectangle(474f, 277f, 148f, 36f), danger = true)
        text("DELETE", Rectangle(474f, 277f, 148f, 36f), 0.6f, color = Color(0.16f, 0.025f, 0.015f, 1f))
        button("CANCEL", Rectangle(656f, 277f, 148f, 36f), scale = 0.6f)
    }

    private fun button(label: String, bounds: Rectangle, enabled: Boolean = true, selected: Boolean = false, scale: Float = 0.58f) {
        paper.button(batch, bounds, selected || (enabled && bounds.contains(pointer.position)), enabled)
        text(label, bounds, scale, color = if (enabled) Color(0.20f, 0.11f, 0.055f, 1f) else Color(0.44f, 0.39f, 0.32f, 1f))
    }
    private fun text(value: String, bounds: Rectangle, scale: Float, align: Int = Align.center, wrap: Boolean = false,
                     color: Color = Color(0.20f, 0.11f, 0.055f, 1f)) {
        font.data.setScale(scale); font.color = color
        var layout = GlyphLayout(font, value, color, bounds.width - 8f, align, wrap)
        if (!wrap && layout.width > bounds.width - 8f) {
            font.data.setScale(scale * (bounds.width - 8f) / layout.width)
            layout = GlyphLayout(font, value)
        }
        font.draw(batch, value, bounds.x + 4f, bounds.y + (bounds.height + layout.height) / 2f,
            bounds.width - 8f, align, wrap)
    }
    private fun renderTooltip() {
        val hint = tooltip ?: return
        if (pointer.isHeld) return
        PaperTooltipRenderer.draw(batch, paper, body, pointer.position, hint.first, hint.second)
    }
    override fun resize(width: Int, height: Int) { if (width > 0 && height > 0) viewport.update(width, height, true) }
    override fun hide() {
        if (installedInput != null && Gdx.input.inputProcessor === installedInput) Gdx.input.inputProcessor = previousInput
        installedInput = null
    }
    override fun dispose() { hide(); background.dispose(); lockTexture.dispose(); paper.dispose(); batch.dispose(); font.dispose(); body.dispose() }
}
