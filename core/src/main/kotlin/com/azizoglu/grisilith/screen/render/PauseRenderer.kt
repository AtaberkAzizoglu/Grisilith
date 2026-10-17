package com.azizoglu.grisilith.screen.render

import com.azizoglu.grisilith.armor.runtime.EquippedArmor
import com.azizoglu.grisilith.character.CharacterDefinition
import com.azizoglu.grisilith.content.ContentTag
import com.azizoglu.grisilith.item.definition.ItemDefinition
import com.azizoglu.grisilith.item.runtime.ItemInventory
import com.azizoglu.grisilith.screen.pause.PauseButtonType
import com.azizoglu.grisilith.screen.pause.PauseConfirmationType
import com.azizoglu.grisilith.screen.pause.PauseGearDetailType
import com.azizoglu.grisilith.screen.pause.PauseInteractionState
import com.azizoglu.grisilith.screen.pause.PauseLayout
import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.statistics.StatType
import com.azizoglu.grisilith.statistics.StatPresentationGroup
import com.azizoglu.grisilith.statistics.StatDetailPresentation
import com.azizoglu.grisilith.tamga.definition.TamgaDefinition
import com.azizoglu.grisilith.weapon.definition.WeaponAttackPattern
import com.azizoglu.grisilith.weapon.definition.WeaponDefinition
import com.azizoglu.grisilith.weapon.runtime.WeaponDamageCalculator
import com.azizoglu.grisilith.weapon.runtime.WeaponInstance
import com.azizoglu.grisilith.weapon.runtime.WeaponInventory
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.Align
import java.util.Locale
import kotlin.math.roundToInt
import com.azizoglu.grisilith.ui.icon.ContentIconRenderer
import com.azizoglu.grisilith.ui.stat.StatSymbolRenderer
import com.azizoglu.grisilith.ui.theme.UiComponentRenderer
import com.azizoglu.grisilith.ui.theme.PanelVisualStyle

class PauseRenderer(
    private val camera:
    OrthographicCamera,

    private val cameraWidth:
    Float,

    private val cameraHeight:
    Float,

    private val batch:
    SpriteBatch,

    private val font:
    BitmapFont,

    private val selectedCharacter:
    CharacterDefinition,

    private val playerStats:
    PlayerStats,

    private val weaponInventory:
    WeaponInventory,

    private val itemInventory:
    ItemInventory,

    private val equippedArmor:
    EquippedArmor,

    private val weaponTamga:
    TamgaDefinition? = null,

    private val armorTamga:
    TamgaDefinition? = null
) {

    companion object {
        private const val PANEL_BORDER =
            2f

        private const val TEXT_PADDING =
            12f
    }

    private val whitePixel =
        createWhitePixelTexture()

    private val pauseBackground =
        Texture(Gdx.files.internal("ui/backgrounds/pause_stone_table.png")).apply {
            setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        }

    private val glyphLayout =
        GlyphLayout()

    private val weaponDamageCalculator =
        WeaponDamageCalculator(
            playerStats =
                playerStats
        )

    private val backgroundColor =
        Color(
            0.015f,
            0.02f,
            0.035f,
            0.30f
        )

    private val panelColor =
        Color(
            0.07f,
            0.10f,
            0.15f,
            0.98f
        )

    private val panelBorderColor =
        Color(
            0.34f,
            0.52f,
            0.73f,
            1f
        )

    private val slotColor =
        Color(
            0.11f,
            0.15f,
            0.22f,
            1f
        )

    private val buttonColor =
        Color(
            0.30f,
            0.25f,
            0.05f,
            1f
        )

    private val hoveredButtonColor =
        Color(
            0.48f,
            0.39f,
            0.06f,
            1f
        )

    private val pressedButtonColor =
        Color(
            0.17f,
            0.36f,
            0.24f,
            1f
        )

    private data class GroupedItemEntry(
        val definition:
        ItemDefinition,

        val count:
        Int
    )

    fun currentLayout():
        PauseLayout {

        return PauseLayout.create(
            cameraX =
                camera.position.x,

            cameraY =
                camera.position.y,

            cameraWidth =
                cameraWidth,

            cameraHeight =
                cameraHeight
        )
    }

    fun groupedItemCount():
        Int {

        return groupedItemEntries()
            .size
    }

    fun render(
        interactionState:
        PauseInteractionState
    ) {
        val layout =
            currentLayout()

        batch.color = Color.WHITE
        batch.draw(
            pauseBackground,
            layout.fullScreen.x,
            layout.fullScreen.y,
            layout.fullScreen.width,
            layout.fullScreen.height
        )

        drawFilledRectangle(
            rectangle =
                layout.fullScreen,

            color =
                backgroundColor
        )

        renderLeftPanel(
            layout =
                layout,

            interactionState =
                interactionState
        )

        renderCenterPanel(
            layout =
                layout,

            interactionState =
                interactionState
        )

        renderCharacterStatsPanel(layout, interactionState)

        when {
            interactionState
                .isConfirmationOpen -> {
                renderConfirmationPopup(
                    layout =
                        layout,

                    interactionState =
                        interactionState
                )
            }

            interactionState
                .detailedWeaponSlotIndex !=
                null -> {
                renderWeaponDetailPopup(
                    layout =
                        layout,

                    slotIndex =
                        interactionState
                            .detailedWeaponSlotIndex
                            ?: return
                )
            }

            interactionState
                .detailedItemIndex !=
                null -> {
                renderItemDetailPopup(
                    layout =
                        layout,

                    groupedItemIndex =
                        interactionState
                            .detailedItemIndex
                            ?: return
                )
            }

            interactionState
                .gearDetailType !=
                null -> {
                renderGearDetailPopup(
                    layout =
                        layout,

                    detailType =
                        interactionState
                            .gearDetailType
                            ?: return
                )
            }

            else -> {
                val dragged = interactionState.draggedWeaponSlotIndex
                if (dragged == null) renderHoverTooltip(layout, interactionState)
                else {
                    interactionState.weaponDropTargetIndex?.let {
                        val r=layout.weaponSlots[it]
                        val color=Color(1f,.78f,.2f,1f)
                        drawFilledRectangle(Rectangle(r.x,r.y,r.width,2f),color)
                        drawFilledRectangle(Rectangle(r.x,r.y+r.height-2f,r.width,2f),color)
                        drawFilledRectangle(Rectangle(r.x,r.y,2f,r.height),color)
                        drawFilledRectangle(Rectangle(r.x+r.width-2f,r.y,2f,r.height),color)
                    }
                    weaponInventory.weaponAt(dragged)?.let { weapon ->
                        ContentIconRenderer.draw(batch, weapon.definition, Rectangle(interactionState.pointerPosition.x - 32f, interactionState.pointerPosition.y - 32f, 64f, 64f))
                    }
                }
            }
        }

        batch.color =
            Color.WHITE

        font.color =
            Color.WHITE
    }

    private fun renderLeftPanel(
        layout:
        PauseLayout,

        interactionState:
        PauseInteractionState
    ) {
        drawPanel(
            layout.leftPanel
        )

        renderWeaponPanel(
            layout
        )

        renderGearPanel(
            layout
        )

        renderItemPanel(
            layout =
                layout,

            interactionState =
                interactionState
        )
    }

    private fun renderWeaponPanel(
        layout:
        PauseLayout
    ) {
        UiComponentRenderer.drawHorizontalSeparator(
            batch,
            Rectangle(layout.leftPanel.x + 18f, layout.gearPanel.y + layout.gearPanel.height + 3f, layout.leftPanel.width - 36f, 8f)
        )
        UiComponentRenderer.drawHorizontalSeparator(
            batch,
            Rectangle(layout.leftPanel.x + 18f, layout.itemPanel.y + layout.itemPanel.height + 3f, layout.leftPanel.width - 36f, 8f)
        )

        drawSectionTitle(
            text =
                "WEAPONS",

            rectangle =
                layout.weaponPanel,

            verticalOffset =
                -8f
        )

        layout.weaponSlots
            .forEachIndexed {
                    slotIndex,
                    rectangle ->

                val weapon =
                    weaponInventory.weaponAt(
                        slotIndex
                    )

                if (weapon == null) {
                    UiComponentRenderer.drawSlot(batch, rectangle)
                    return@forEachIndexed
                } else {
                    val iconSize =
                        rectangle.height - 6f

                    val iconBounds =
                        Rectangle(
                            rectangle.x + 3f,
                            rectangle.y + 3f,
                            iconSize,
                            iconSize
                        )

                    ContentIconRenderer.draw(
                            batch = batch,
                            definition = weapon.definition,
                            bounds = iconBounds
                        )

                    val oldScaleX =
                        font.data.scaleX

                    val oldScaleY =
                        font.data.scaleY

                    font.data.setScale(0.72f)
                    font.color = Color.WHITE

                    font.draw(
                        batch,
                        romanNumeral(weapon.level),
                        rectangle.x + rectangle.width - 28f,
                        rectangle.y + 17f,
                        23f,
                        Align.right,
                        false
                    )

                    font.data.setScale(
                        oldScaleX,
                        oldScaleY
                    )
                }
            }
    }

    private fun renderGearPanel(
        layout:
        PauseLayout
    ) {
        drawSectionTitle("EQUIPMENT", layout.gearPanel, 8f)

        val equippedArmorDefinition =
            equippedArmor.definition

        if (equippedArmorDefinition == null) {
            renderPlaceholderSlot(layout.armorSlot, "ARMOR", "EMPTY")
        }

        if (equippedArmorDefinition != null) {
            val armorIconSize =
                layout.armorSlot.height - 6f

            ContentIconRenderer.draw(
                batch = batch,
                definition = equippedArmorDefinition,
                bounds = Rectangle(
                    layout.armorSlot.x + 3f,
                    layout.armorSlot.y + 3f,
                    armorIconSize,
                    armorIconSize
                )
            )
        }

        if (weaponTamga == null) {
            renderPlaceholderSlot(layout.weaponTamgaSlot, "WEAPON TAMGA", "NONE")
        }

        weaponTamga?.let { tamga ->
            ContentIconRenderer.draw(
                batch = batch,
                definition = tamga,
                bounds = insetIconBounds(layout.weaponTamgaSlot)
            )
        }

        if (armorTamga == null) {
            renderPlaceholderSlot(layout.armorTamgaSlot, "ARMOR TAMGA", "NONE")
        }

        armorTamga?.let { tamga ->
            ContentIconRenderer.draw(
                batch = batch,
                definition = tamga,
                bounds = insetIconBounds(layout.armorTamgaSlot)
            )
        }
    }

    private fun insetIconBounds(rectangle: Rectangle): Rectangle {
        val size = rectangle.height - 6f
        return Rectangle(
            rectangle.x + 3f,
            rectangle.y + 3f,
            size,
            size
        )
    }

    private fun renderPlaceholderSlot(
        rectangle:
        Rectangle,

        title:
        String,

        value:
        String
    ) {
        UiComponentRenderer.drawSlot(batch, rectangle)
    }

    private fun renderItemPanel(
        layout:
        PauseLayout,

        interactionState:
        PauseInteractionState
    ) {
        drawSectionTitle(
            text =
                "ITEMS  (${itemInventory.itemCount})",

            rectangle =
                layout.itemPanel
        )

        val groupedItems =
            groupedItemEntries()

        if (groupedItems.isEmpty()) {
            font.color =
                Color.GRAY

            font.draw(
                batch,
                "NO ITEMS",
                layout.itemPanel.x,
                layout.itemPanel.y +
                    layout.itemPanel.height / 2f,
                layout.itemPanel.width,
                Align.center,
                false
            )

            return
        }

        val visibleRows =
            PauseLayout
                .itemSlotRectangles(
                    layout.itemPanel
                )

        visibleRows
            .forEachIndexed {
                    visibleRowIndex,
                    rectangle ->

                val actualIndex =
                    interactionState
                        .itemScrollOffset +
                        visibleRowIndex

                val entry =
                    groupedItems
                        .getOrNull(
                            actualIndex
                        )
                        ?: return@forEachIndexed

                val itemIconSize =
                    rectangle.height - 6f

                val itemIconBounds =
                    Rectangle(
                        rectangle.x + 3f,
                        rectangle.y + 3f,
                        itemIconSize,
                        itemIconSize
                    )

                ContentIconRenderer.draw(
                        batch = batch,
                        definition = entry.definition,
                        bounds = itemIconBounds
                    )
            }

        if (
            groupedItems.size >
            visibleRows.size
        ) {
            val oldScaleX =
                font.data.scaleX

            val oldScaleY =
                font.data.scaleY

            font.data.setScale(
                0.65f
            )

            font.color =
                Color.GRAY

            font.draw(
                batch,
                "MOUSE WHEEL OR DRAG TO SCROLL",
                layout.itemPanel.x +
                    12f,
                layout.itemPanel.y +
                    10f
            )

            font.data.setScale(
                oldScaleX,
                oldScaleY
            )
        }
    }

    private fun renderCenterPanel(
        layout:
        PauseLayout,

        interactionState:
        PauseInteractionState
    ) {
        val oldScaleX =
            font.data.scaleX

        val oldScaleY =
            font.data.scaleY

        font.data.setScale(
            1.7f
        )

        font.color =
            Color.WHITE

        val pausedBaseline = layout.centerPanel.y + layout.centerPanel.height - 45f
        font.draw(batch, "PAUSED", layout.centerPanel.x, pausedBaseline, layout.centerPanel.width, Align.center, false)

        font.data.setScale(
            oldScaleX,
            oldScaleY
        )

        renderPauseButton(
            text =
                "RETURN TO GAME",

            rectangle =
                layout.returnToGameButton,

            buttonType =
                PauseButtonType.RETURN_TO_GAME,

            interactionState =
                interactionState
        )

        renderPauseButton(
            text =
                "RESTART",

            rectangle =
                layout.restartButton,

            buttonType =
                PauseButtonType.RESTART,

            interactionState =
                interactionState
        )

        renderPauseButton(
            text =
                "SETTINGS",

            rectangle =
                layout.settingsButton,

            buttonType =
                PauseButtonType.SETTINGS,

            interactionState =
                interactionState
        )

        renderPauseButton(
            text =
                "MAIN MENU",

            rectangle =
                layout.mainMenuButton,

            buttonType =
                PauseButtonType.MAIN_MENU,

            interactionState =
                interactionState
        )

        renderPauseButton(
            text = "QUIT GAME",
            rectangle = layout.quitGameButton,
            buttonType = PauseButtonType.QUIT_GAME,
            interactionState = interactionState
        )
    }

    private fun renderPauseButton(
        text:
        String,

        rectangle:
        Rectangle,

        buttonType:
        PauseButtonType,

        interactionState:
        PauseInteractionState
    ) {
        val fillColor =
            when {
                interactionState
                    .pressedButton ==
                    buttonType -> {
                    pressedButtonColor
                }

                interactionState
                    .hoveredButton ==
                    buttonType -> {
                    hoveredButtonColor
                }

                else -> {
                    buttonColor
                }
            }

        drawPanel(
            rectangle =
                rectangle,

            fillColor =
                fillColor
        )

        drawCenteredText(
            text =
                text,

            rectangle =
                rectangle,

            color =
                Color.WHITE
        )
    }

    /*
     * Daha büyük font, daha geniş iki sütun ve
     * daha fazla satır aralığı kullanılır.
     */
    private fun renderCharacterStatsPanel(
        layout:
        PauseLayout,

        interactionState:
        PauseInteractionState
    ) {
        drawPanel(
            layout.characterStatsPanel
        )

        val panel =
            layout.characterStatsPanel

        val oldScaleX =
            font.data.scaleX

        val oldScaleY =
            font.data.scaleY

        font.data.setScale(
            1.08f
        )

        font.color =
            Color.CYAN

        font.draw(
            batch,
            selectedCharacter.displayName,
            panel.x,
            panel.y +
                panel.height -
                37f,
            panel.width,
            Align.center,
            false
        )

        ContentIconRenderer.drawCharacterPortrait(
            batch = batch,
            displayName = selectedCharacter.displayName,
            bounds = layout.characterPortrait
        )

        font.data.setScale(
            1.1f
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            "STATS",
            panel.x,
            panel.y +
                panel.height -
                230f,
            panel.width,
            Align.center,
            false
        )

        font.data.setScale(
            0.97f
        )

        val statRows = PauseLayout.statRowRectangles(panel)
        StatPresentationGroup.ordered.zip(PauseLayout.statGroupRectangles(panel)).forEach { (group, bounds) ->
            UiComponentRenderer.drawHorizontalSeparator(
                batch,
                Rectangle(bounds.x + 4f, bounds.y + bounds.height - 25f, bounds.width - 8f, 7f)
            )
            font.data.setScale(0.82f)
            font.color = Color(0.72f, 0.78f, 0.90f, 1f)
            font.draw(batch, group.title, bounds.x, bounds.y + bounds.height - 5f, bounds.width, Align.center, false)

            group.stats.forEach { statType ->
                val rowBounds = statRows.getValue(statType)
                val hovered = interactionState.hoveredStatType == statType
                if (hovered) {
                    drawFilledRectangle(rowBounds, Color(0.20f, 0.25f, 0.32f, 0.52f))
                }
                val y = rowBounds.y + rowBounds.height * 0.67f
                font.data.setScale(0.88f)
                StatSymbolRenderer.drawLabel(
                    batch = batch,
                    font = font,
                    statType = statType,
                    x = rowBounds.x + 4f,
                    baselineY = y,
                    iconSize = 20f,
                    width = rowBounds.width - 52f
                )
                font.color = Color.WHITE
                font.draw(
                    batch,
                    playerStats.totalScore(statType).toString(),
                    rowBounds.x + rowBounds.width - 42f,
                    y,
                    34f,
                    Align.right,
                    false
                )
            }
        }

        font.data.setScale(
            oldScaleX,
            oldScaleY
        )
    }

    private fun renderConfirmationPopup(
        layout:
        PauseLayout,

        interactionState:
        PauseInteractionState
    ) {
        drawModalBackdrop(
            layout
        )

        drawPanel(
            rectangle =
                layout.modalPanel,

            fillColor =
                Color(
                    0.12f,
                    0.08f,
                    0.09f,
                    1f
                )
        )

        val isRestart =
            interactionState
                .confirmationType ==
                PauseConfirmationType
                    .RESTART_RUN

        val isExitGame = interactionState.confirmationType == PauseConfirmationType.EXIT_GAME

        font.color =
            Color.WHITE

        font.draw(
            batch,
            when {
                interactionState.confirmationType == PauseConfirmationType.MERGE_WEAPONS -> "MERGE WEAPONS?"
                isRestart -> "RESTART CURRENT RUN?"
                isExitGame -> "QUIT GAME?"
                else -> "RETURN TO MAIN MENU?"
            },
            layout.modalPanel.x +
                25f,
            layout.modalPanel.y +
                layout.modalPanel.height -
                30f
        )

        font.color =
            Color.LIGHT_GRAY

        font.draw(
            batch,
            when {
                interactionState.confirmationType == PauseConfirmationType.MERGE_WEAPONS -> interactionState.mergeConfirmationText
                isRestart -> "All progress from the current run will be lost.\n" +
                    "The same Character, Map and Difficulty will be used."
                isExitGame -> "All progress from the current run will be lost.\n" +
                    "The game will close."
                else -> "All progress from the current run will be lost.\n" +
                    "You will return to the Main Menu."
            },
            layout.modalPanel.x +
                25f,
            layout.modalPanel.y +
                layout.modalPanel.height -
                95f,
            layout.modalPanel.width -
                50f,
            Align.center,
            true
        )

        drawPanel(
            rectangle =
                layout.modalConfirmButton,

            fillColor =
                Color(
                    0.48f,
                    0.16f,
                    0.16f,
                    1f
                )
        )

        drawCenteredText(
            text =
                "CONFIRM",

            rectangle =
                layout.modalConfirmButton,

            color =
                Color.WHITE
        )

        drawPanel(
            rectangle =
                layout.modalCancelButton,

            fillColor =
                Color(
                    0.13f,
                    0.22f,
                    0.31f,
                    1f
                )
        )

        drawCenteredText(
            text =
                "CANCEL",

            rectangle =
                layout.modalCancelButton,

            color =
                Color.WHITE
        )
    }

    private fun renderWeaponDetailPopup(
        layout:
        PauseLayout,

        slotIndex:
        Int
    ) {
        val weapon =
            weaponInventory.weaponAt(
                slotIndex
            )
                ?: return

        val definition =
            weapon.definition

        val levelStats =
            weapon.currentLevelStats

        val damage =
            weaponDamageCalculator
                .calculateNonCriticalDamage(
                    weapon
                )

        val attackSpeedStatMultiplier =
            when (
                definition.normalAttackSpeedStat
            ) {
                StatType.HASTE -> {
                    playerStats
                        .hasteAttackSpeedMultiplier()
                }

                else -> {
                    playerStats
                        .attackSpeedMultiplier()
                }
            }

        val attacksPerSecond =
            definition.baseAttacksPerSecond *
                levelStats.attackSpeedMultiplier *
                attackSpeedStatMultiplier

        val range =
            definition.baseRange *
                levelStats.rangeMultiplier

        val knockback =
            definition.baseKnockback *
                levelStats.knockbackMultiplier

        drawModalBackdrop(
            layout
        )

        drawPanel(
            rectangle =
                layout.modalPanel,

            fillColor =
                Color(
                    0.08f,
                    0.11f,
                    0.17f,
                    1f
                )
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            weapon.displayName,
            layout.modalPanel.x +
                24f,
            layout.modalPanel.y +
                layout.modalPanel.height -
                26f
        )

        font.color =
            Color.GOLD

        font.draw(
            batch,
            "Level ${romanNumeral(weapon.level)}",
            layout.modalPanel.x +
                24f,
            layout.modalPanel.y +
                layout.modalPanel.height -
                55f
        )

        val orderedScaling =
            definition
                .orderedDamageScaling()

        val primaryScaling =
            orderedScaling
                .firstOrNull()

        val secondaryScaling =
            orderedScaling
                .drop(1)

        var leftY =
            layout.modalPanel.y +
                layout.modalPanel.height -
                95f

        val leftX =
            layout.modalPanel.x +
                24f

        font.color =
            Color.LIGHT_GRAY

        font.draw(
            batch,
            "Weapon Type: " +
                weaponTypeText(
                    definition
                ),
            leftX,
            leftY
        )

        leftY -=
            24f

        if (primaryScaling != null) {
            font.color =
                Color.GOLD

            StatSymbolRenderer.drawRichText(
                batch, font,
                "Main Damage Scaling: ${primaryScaling.first.displayName}",
                leftX, leftY, 310f
            )

            leftY -=
                24f
        }

        if (secondaryScaling.isNotEmpty()) {
            font.color =
                Color.LIGHT_GRAY

            StatSymbolRenderer.drawRichText(
                batch, font,
                "Secondary Scaling: " + secondaryScaling.joinToString(", ") { it.first.displayName },
                leftX, leftY, 310f
            )

            leftY -=
                24f
        }

        font.color =
            Color.LIGHT_GRAY

        val statisticsText =
            buildString {
                appendLine(
                    "Attack Model: " +
                        attackModelText(
                            weapon
                        )
                )

                appendLine(
                    "Damage: $damage"
                )

                appendLine(
                    "Attacks per Second: " +
                        formatDecimal(
                            attacksPerSecond
                        )
                )

                appendLine(
                    "Range: " +
                        range.roundToInt()
                )

                if (knockback > 0f) {
                    appendLine(
                        "Knockback: " +
                            knockback.roundToInt()
                    )
                }

                appendLine(
                    "Piercing: " +
                        playerStats.totalScore(
                            StatType.PIERCING
                        )
                )

                appendLine(
                    "Critical Rate: " +
                        formatDecimal(
                            playerStats
                                .criticalHitChance() *
                                100f
                        ) +
                        "%"
                )

                append(
                    "Critical Damage: " +
                        formatDecimal(
                            playerStats
                                .criticalDamageMultiplier() *
                                100f
                        ) +
                        "%"
                )
            }

        StatSymbolRenderer.drawRichText(
            batch, font, statisticsText,
            leftX,
            leftY,
            310f
        )

        font.color =
            Color.LIGHT_GRAY

        WeaponDetailPanel.drawCopy(batch,font,
            com.azizoglu.grisilith.weapon.definition.WeaponPresentation.hover(weapon),
            Rectangle(layout.modalPanel.x+390f,layout.modalPanel.y+50f,
                layout.modalPanel.width-420f,layout.modalPanel.height-145f))

        renderDetailCloseHint(
            layout
        )
    }

    private fun renderItemDetailPopup(
        layout:
        PauseLayout,

        groupedItemIndex:
        Int
    ) {
        val entry =
            groupedItemEntries()
                .getOrNull(
                    groupedItemIndex
                )
                ?: return

        val definition =
            entry.definition

        val maximumCopies =
            definition.maximumInventoryCopies
                ?: definition.rarity
                    .maximumInventoryCopies

        drawModalBackdrop(
            layout
        )

        drawPanel(
            rectangle =
                layout.modalPanel,

            fillColor =
                Color(
                    0.08f,
                    0.13f,
                    0.10f,
                    1f
                )
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            definition.displayName,
            layout.modalPanel.x +
                24f,
            layout.modalPanel.y +
                layout.modalPanel.height -
                27f
        )

        font.color =
            Color(
                0.40f,
                0.90f,
                0.60f,
                1f
            )

        font.draw(
            batch,
            definition.rarity.displayName +
                " Item",
            layout.modalPanel.x +
                24f,
            layout.modalPanel.y +
                layout.modalPanel.height -
                57f
        )

        font.color =
            Color.LIGHT_GRAY

        StatSymbolRenderer.drawRichText(
            batch, font, definition.description,
            layout.modalPanel.x + 24f,
            layout.modalPanel.y + layout.modalPanel.height - 100f,
            layout.modalPanel.width - 48f
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            "Owned: ${entry.count} / " +
                (
                    maximumCopies
                        ?.toString()
                        ?: "Unlimited"
                    ) +
                "\n\nTotal Bonus:",
            layout.modalPanel.x +
                24f,
            layout.modalPanel.y +
                145f
        )

        StatSymbolRenderer.drawModifiersVertical(
            batch = batch,
            font = font,
            modifiers = definition.statModifiers.map { modifier ->
                modifier.statType to modifier.amount * entry.count
            },
            x = layout.modalPanel.x + 24f,
            firstBaselineY = layout.modalPanel.y + 91f,
            lineHeight = 23f,
            iconSize = 18f
        )

        renderDetailCloseHint(
            layout
        )
    }

    private fun renderGearDetailPopup(
        layout:
        PauseLayout,

        detailType:
        PauseGearDetailType
    ) {
        val title: String

        val description: String

        when (detailType) {
            PauseGearDetailType.ARMOR -> {
                val armor =
                    equippedArmor.definition

                if (armor == null) {
                    title =
                        "ARMOR"

                    description =
                        "No Armor Equipped.\n\n" +
                            "Base HP: ${playerStats.armorBaseHealth()}\n" +
                            "Base Armor: ${playerStats.armorBaseArmor()}\n" +
                            "Base Magic Resistance: ${playerStats.armorBaseMagicResistance()}\n" +
                            "Current Max HP: ${playerStats.calculatedMaximumHealth()}\n" +
                            "Physical Damage Reduce: ${formatDecimal(playerStats.rawArmorDamageReduction())}\n" +
                            "Magical Damage Reduce: ${formatDecimal(playerStats.rawMagicDamageReduction())}"
                } else {
                    title =
                        armor.displayName

                    val modifierText =
                        armor
                            .allStatModifiers()
                            .joinToString(
                                separator =
                                    "\n"
                            ) { modifier ->

                                "${modifier.statType.displayName}: " +
                                    formatSignedValue(
                                        modifier.amount
                                    )
                            }
                            .ifBlank {
                                "No Stat changes."
                            }

                    description =
                        buildString {
                            appendLine(
                                "Base HP: ${armor.baseHealth}"
                            )

                            appendLine(
                                "Base Armor: ${armor.baseArmor}"
                            )

                            appendLine("Base Magic Resistance: ${armor.baseMagicResistance}")
                            appendLine("Magical Damage Reduce: ${formatDecimal(playerStats.rawMagicDamageReduction())}")

                            appendLine()

                            appendLine(
                                "Current Max HP: " +
                                    playerStats
                                        .calculatedMaximumHealth()
                            )

                            appendLine(
                                "Damage Reduce: " +
                                    formatDecimal(
                                        playerStats
                                            .rawArmorDamageReduction()
                                    )
                            )

                            appendLine()
                            appendLine(
                                modifierText
                            )
                            appendLine()
                            append(
                                armor.description
                            )
                        }
                }
            }

            PauseGearDetailType.WEAPON_TAMGA -> {
                title =
                    weaponTamga?.displayName ?: "WEAPON TAMGA"

                description =
                    weaponTamga?.description
                        ?: "No Weapon Tamga Equipped."
            }

            PauseGearDetailType.ARMOR_TAMGA -> {
                title =
                    armorTamga?.displayName ?: "ARMOR TAMGA"

                description =
                    armorTamga?.description
                        ?: "No Armor Tamga Equipped."
            }
        }

        drawModalBackdrop(
            layout
        )

        drawPanel(
            rectangle =
                layout.modalPanel,

            fillColor =
                Color(
                    0.08f,
                    0.11f,
                    0.17f,
                    1f
                )
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            title,
            layout.modalPanel.x +
                24f,
            layout.modalPanel.y +
                layout.modalPanel.height -
                28f
        )

        font.color =
            if (
                detailType ==
                PauseGearDetailType.ARMOR &&
                equippedArmor.definition !=
                null
            ) {
                Color.GOLD
            } else {
                Color.GRAY
            }

        font.draw(
            batch,
            if (
                detailType ==
                PauseGearDetailType.ARMOR &&
                equippedArmor.definition !=
                null
            ) {
                "EQUIPPED"
            } else {
                "NOT EQUIPPED"
            },
            layout.modalPanel.x +
                24f,
            layout.modalPanel.y +
                layout.modalPanel.height -
                60f
        )

        font.color =
            Color.LIGHT_GRAY

        StatSymbolRenderer.drawRichText(
            batch, font, description,
            layout.modalPanel.x + 24f,
            layout.modalPanel.y + layout.modalPanel.height - 110f,
            layout.modalPanel.width - 48f
        )

        renderDetailCloseHint(
            layout
        )
    }

    private fun renderDetailCloseHint(
        layout:
        PauseLayout
    ) {
        font.color =
            Color.GRAY

        font.draw(
            batch,
            "Click outside to close.",
            layout.modalPanel.x +
                24f,
            layout.modalPanel.y +
                22f
        )
    }

    private fun renderHoverTooltip(
        layout: PauseLayout,
        interactionState: PauseInteractionState
    ) {
        val hoveredStat = interactionState.hoveredStatType
        val content: Pair<String, String> = when {
            hoveredStat != null -> hoveredStat.displayName to
                StatDetailPresentation.tooltipText(playerStats, hoveredStat)

            interactionState.hoveredWeaponSlotIndex != null -> {
                val weapon = weaponInventory.weaponAt(interactionState.hoveredWeaponSlotIndex ?: -1)
                    ?: return
                "${weapon.displayName}  •  ${romanNumeral(weapon.level)}" to com.azizoglu.grisilith.weapon.definition.WeaponPresentation.hover(weapon)
            }

            interactionState.hoveredItemIndex != null -> {
                val item = groupedItemEntries().getOrNull(interactionState.hoveredItemIndex ?: -1)
                    ?: return
                item.definition.displayName to
                    "${item.definition.rarity.displayName} Item  •  Owned ${romanNumeral(item.count)}\n\n${item.definition.description}"
            }

            interactionState.hoveredGearDetailType != null -> when (interactionState.hoveredGearDetailType) {
                PauseGearDetailType.ARMOR -> {
                    val armor = equippedArmor.definition
                    (armor?.displayName ?: "Armor") to (armor?.description ?: "No Armor equipped.")
                }
                PauseGearDetailType.WEAPON_TAMGA ->
                    (weaponTamga?.displayName ?: "Weapon Tamga") to
                        (weaponTamga?.description ?: "No Weapon Tamga equipped.")
                PauseGearDetailType.ARMOR_TAMGA ->
                    (armorTamga?.displayName ?: "Armor Tamga") to
                        (armorTamga?.description ?: "No Armor Tamga equipped.")
                null -> return
            }

            else -> return
        }

        val pointer = interactionState.pointerPosition
        AdaptiveTooltipRenderer.draw(
            batch, font, camera, cameraWidth, cameraHeight, pointer,
            content.first, content.second,
            maxWidth = if (hoveredStat != null) 460f else 440f
        )
    }

    private fun groupedItemEntries():
        List<GroupedItemEntry> {

        return itemInventory
            .allItems()
            .groupBy { item ->
                item.definition.id
            }
            .values
            .mapNotNull { copies ->
                val firstItem =
                    copies.firstOrNull()
                        ?: return@mapNotNull null

                GroupedItemEntry(
                    definition =
                        firstItem.definition,

                    count =
                        copies.size
                )
            }
    }

    private fun drawModalBackdrop(
        layout:
        PauseLayout
    ) {
        drawFilledRectangle(
            rectangle =
                layout.fullScreen,

            color =
                Color(
                    0f,
                    0f,
                    0f,
                    0.68f
                )
        )
    }

    private fun weaponTypeText(
        definition:
        WeaponDefinition
    ): String {
        return when {
            ContentTag.SPELL_FOCUS in
                definition.tags -> {
                "Spell Focus"
            }

            ContentTag.MELEE_WEAPON in
                definition.tags -> {
                "Melee Weapon"
            }

            ContentTag.RANGED_WEAPON in
                definition.tags -> {
                "Ranged Weapon"
            }

            definition.normalAttackSpeedStat ==
                StatType.HASTE -> {
                "Spell Focus"
            }

            else -> {
                "Weapon"
            }
        }
    }

    private fun attackModelText(
        weapon:
        WeaponInstance
    ): String {
        return when (
            weapon.displayName
                .lowercase(
                    Locale.US
                )
        ) {
            "double swords" -> {
                "X"
            }

            else -> {
                when (
                    weapon.definition
                        .attackPattern
                ) {
                    WeaponAttackPattern
                        .SHORT_SLASH_LINES -> {
                        "Slash"
                    }

                    WeaponAttackPattern
                        .FORWARD_CONE -> {
                        "Cone"
                    }

                    WeaponAttackPattern
                        .TARGET_AREA_CIRCLE -> {
                        "Circle"
                    }

                    WeaponAttackPattern
                        .MELEE_OR_THROW -> {
                        "Stab / Throw"
                    }

                    WeaponAttackPattern
                        .STRAIGHT_PROJECTILE -> {
                        "Projectile"
                    }

                    WeaponAttackPattern
                        .PIERCING_PROJECTILE -> {
                        "Piercing Projectile"
                    }

                    WeaponAttackPattern
                        .LONG_PIERCING_LINE -> {
                        "Piercing Line"
                    }

                    WeaponAttackPattern
                        .SHIELD_CONE -> {
                        "Shield Cone"
                    }

                    WeaponAttackPattern
                        .TOME_PROJECTILE_OR_BEAM -> {
                        "Projectile / Beam"
                    }

                    WeaponAttackPattern
                        .TOTEM_PROJECTILE_OR_DRAIN -> {
                        "Projectile / Drain"
                    }

                    WeaponAttackPattern.EXTENDED_PHYSICAL ->
                        com.azizoglu.grisilith.weapon.definition.catalog.NewPhysicalWeapons.patternLabel(weapon.definition.id.value)
                    WeaponAttackPattern.NONE -> {
                        "Passive"
                    }
                }
            }
        }
    }

    private fun romanNumeral(
        level:
        Int
    ): String {
        if (level <= 0 || level > 3999) return level.toString()
        var remaining = level
        val symbols = listOf(
            1000 to "M", 900 to "CM", 500 to "D", 400 to "CD",
            100 to "C", 90 to "XC", 50 to "L", 40 to "XL",
            10 to "X", 9 to "IX", 5 to "V", 4 to "IV", 1 to "I"
        )
        return buildString {
            symbols.forEach { (value, symbol) ->
                while (remaining >= value) {
                    append(symbol)
                    remaining -= value
                }
            }
        }
    }

    private fun formatDecimal(
        value:
        Float
    ): String {
        return String.format(
            Locale.US,
            "%.2f",
            value
        )
    }

    private fun formatSignedValue(
        value:
        Int
    ): String {
        return if (value >= 0) {
            "+$value"
        } else {
            value.toString()
        }
    }

    private fun drawSectionTitle(
        text:
        String,

        rectangle:
        Rectangle,

        verticalOffset:
        Float = 0f
    ) {
        font.color =
            Color.WHITE

        font.draw(
            batch,
            text,
            rectangle.x,
            rectangle.y +
                rectangle.height -
                13f +
                verticalOffset,
            rectangle.width,
            Align.center,
            false
        )
    }

    private fun drawSectionPanel(
        rectangle:
        Rectangle
    ) {
        drawPanel(
            rectangle =
                rectangle,

            fillColor =
                panelColor
        )
    }

    private fun drawPanel(
        rectangle:
        Rectangle,

        fillColor:
        Color =
            panelColor
    ) {
        UiComponentRenderer.drawPanel(
            batch = batch,
            bounds = rectangle
        )
    }

    private fun drawCenteredText(
        text:
        String,

        rectangle:
        Rectangle,

        color:
        Color
    ) {
        font.color =
            color

        glyphLayout.setText(
            font,
            text
        )

        font.draw(
            batch,
            text,
            rectangle.x +
                (
                    rectangle.width -
                        glyphLayout.width
                    ) /
                2f,
            rectangle.y +
                (
                    rectangle.height +
                        glyphLayout.height
                    ) /
                2f
        )
    }

    private fun drawFilledRectangle(
        rectangle:
        Rectangle,

        color:
        Color
    ) {
        batch.color =
            color

        batch.draw(
            whitePixel,
            rectangle.x,
            rectangle.y,
            rectangle.width,
            rectangle.height
        )

        batch.color =
            Color.WHITE
    }

    private fun createWhitePixelTexture():
        Texture {

        val pixmap =
            Pixmap(
                1,
                1,
                Pixmap.Format.RGBA8888
            )

        pixmap.setColor(
            Color.WHITE
        )

        pixmap.fill()

        val texture =
            Texture(
                pixmap
            )

        pixmap.dispose()

        return texture
    }

    fun dispose() {
        whitePixel.dispose()
        pauseBackground.dispose()
    }
}
