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
import com.azizoglu.grisilith.weapon.definition.WeaponAttackPattern
import com.azizoglu.grisilith.weapon.definition.WeaponDefinition
import com.azizoglu.grisilith.weapon.runtime.WeaponDamageCalculator
import com.azizoglu.grisilith.weapon.runtime.WeaponInstance
import com.azizoglu.grisilith.weapon.runtime.WeaponInventory
import com.badlogic.gdx.graphics.Color
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
    EquippedArmor
) {

    companion object {
        private const val PANEL_BORDER =
            2f

        private const val TEXT_PADDING =
            12f
    }

    private val whitePixel =
        createWhitePixelTexture()

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
            0.96f
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

        renderCharacterStatsPanel(
            layout
        )

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
        drawSectionPanel(
            layout.weaponPanel
        )

        drawSectionTitle(
            text =
                "WEAPONS",

            rectangle =
                layout.weaponPanel
        )

        layout.weaponSlots
            .forEachIndexed {
                    slotIndex,
                    rectangle ->

                drawPanel(
                    rectangle =
                        rectangle,

                    fillColor =
                        slotColor
                )

                val weapon =
                    weaponInventory.weaponAt(
                        slotIndex
                    )

                if (weapon == null) {
                    drawCenteredText(
                        text =
                            "EMPTY",

                        rectangle =
                            rectangle,

                        color =
                            Color.GRAY
                    )
                } else {
                    val oldScaleX =
                        font.data.scaleX

                    val oldScaleY =
                        font.data.scaleY

                    font.data.setScale(
                        0.75f
                    )

                    font.color =
                        Color.WHITE

                    font.draw(
                        batch,
                        weapon.displayName,
                        rectangle.x +
                            5f,
                        rectangle.y +
                            rectangle.height -
                            13f,
                        rectangle.width -
                            10f,
                        Align.center,
                        true
                    )

                    font.color =
                        Color.GOLD

                    font.draw(
                        batch,
                        "Lv. ${weapon.level}",
                        rectangle.x +
                            5f,
                        rectangle.y +
                            17f,
                        rectangle.width -
                            10f,
                        Align.center,
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
        drawSectionPanel(
            layout.gearPanel
        )

        val equippedArmorDefinition =
            equippedArmor.definition

        renderPlaceholderSlot(
            rectangle =
                layout.armorSlot,

            title =
                "ARMOR",

            value =
                equippedArmorDefinition
                    ?.let { armor ->
                        armor.displayName +
                            "\nHP ${armor.baseHealth}" +
                            "  ARM ${armor.baseArmor}"
                    }
                    ?: "EMPTY"
        )

        renderPlaceholderSlot(
            rectangle =
                layout.weaponTamgaSlot,

            title =
                "WEAPON\nTAMGA",

            value =
                "NONE"
        )

        renderPlaceholderSlot(
            rectangle =
                layout.armorTamgaSlot,

            title =
                "ARMOR\nTAMGA",

            value =
                "NONE"
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
        drawPanel(
            rectangle =
                rectangle,

            fillColor =
                slotColor
        )

        val oldScaleX =
            font.data.scaleX

        val oldScaleY =
            font.data.scaleY

        font.data.setScale(
            0.67f
        )

        font.color =
            Color.LIGHT_GRAY

        font.draw(
            batch,
            title,
            rectangle.x +
                4f,
            rectangle.y +
                rectangle.height -
                12f,
            rectangle.width -
                8f,
            Align.center,
            true
        )

        font.color =
            Color.GRAY

        font.draw(
            batch,
            value,
            rectangle.x +
                4f,
            rectangle.y +
                14f,
            rectangle.width -
                8f,
            Align.center,
            false
        )

        font.data.setScale(
            oldScaleX,
            oldScaleY
        )
    }

    private fun renderItemPanel(
        layout:
        PauseLayout,

        interactionState:
        PauseInteractionState
    ) {
        drawSectionPanel(
            layout.itemPanel
        )

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
                .itemRowRectangles(
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

                drawPanel(
                    rectangle =
                        rectangle,

                    fillColor =
                        slotColor
                )

                font.color =
                    Color.LIGHT_GRAY

                font.draw(
                    batch,
                    entry.definition.displayName,
                    rectangle.x +
                        8f,
                    rectangle.y +
                        19f
                )

                if (entry.count > 1) {
                    font.color =
                        Color.WHITE

                    font.draw(
                        batch,
                        "x${entry.count}",
                        rectangle.x +
                            rectangle.width -
                            50f,
                        rectangle.y +
                            19f,
                        40f,
                        Align.right,
                        false
                    )
                }
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
                "UP / DOWN TO SCROLL",
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

        font.draw(
            batch,
            "PAUSED",
            layout.centerPanel.x,
            layout.centerPanel.y +
                layout.centerPanel.height -
                45f,
            layout.centerPanel.width,
            Align.center,
            false
        )

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
                "QUIT GAME",

            rectangle =
                layout.quitGameButton,

            buttonType =
                PauseButtonType.QUIT_GAME,

            interactionState =
                interactionState
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
        PauseLayout
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
            1.12f
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            "CHARACTER",
            panel.x +
                18f,
            panel.y +
                panel.height -
                24f
        )

        font.data.setScale(
            1.08f
        )

        font.color =
            Color.CYAN

        font.draw(
            batch,
            selectedCharacter.displayName,
            panel.x +
                18f,
            panel.y +
                panel.height -
                64f,
            panel.width -
                36f,
            Align.left,
            true
        )

        font.data.setScale(
            1.1f
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            "STATS",
            panel.x +
                18f,
            panel.y +
                panel.height -
                118f
        )

        font.data.setScale(
            0.97f
        )

        val stats =
            StatType.entries

        val rowsPerColumn =
            (
                stats.size +
                    1
                ) /
                2

        val contentX =
            panel.x +
                18f

        val availableWidth =
            panel.width -
                36f

        val columnGap =
            22f

        val columnWidth =
            (
                availableWidth -
                    columnGap
                ) /
                2f

        val rowSpacing =
            34f

        val firstStatY =
            panel.y +
                panel.height -
                165f

        stats.forEachIndexed {
                index,
                statType ->

            val column =
                index /
                    rowsPerColumn

            val row =
                index %
                    rowsPerColumn

            val x =
                contentX +
                    column *
                    (
                        columnWidth +
                            columnGap
                        )

            val y =
                firstStatY -
                    row *
                    rowSpacing

            font.color =
                Color.LIGHT_GRAY

            font.draw(
                batch,
                statType.displayName,
                x,
                y
            )

            font.color =
                Color.WHITE

            font.draw(
                batch,
                playerStats
                    .totalScore(
                        statType
                    )
                    .toString(),
                x +
                    columnWidth -
                    48f,
                y,
                42f,
                Align.right,
                false
            )
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

        font.color =
            Color.WHITE

        font.draw(
            batch,
            if (isRestart) {
                "RESTART CURRENT RUN?"
            } else {
                "QUIT CURRENT RUN?"
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
            if (isRestart) {
                "All progress from the current run will be lost.\n" +
                    "The same Character, Map and Difficulty will be used."
            } else {
                "All progress from the current run will be lost.\n" +
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

            font.draw(
                batch,
                "Main Damage Scaling: " +
                    primaryScaling
                        .first
                        .displayName,
                leftX,
                leftY
            )

            leftY -=
                24f
        }

        if (secondaryScaling.isNotEmpty()) {
            font.color =
                Color.LIGHT_GRAY

            font.draw(
                batch,
                "Secondary Scaling: " +
                    secondaryScaling
                        .joinToString(
                            separator = ", "
                        ) { scaling ->
                            scaling
                                .first
                                .displayName
                        },
                leftX,
                leftY
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

        font.draw(
            batch,
            statisticsText,
            leftX,
            leftY,
            310f,
            Align.left,
            true
        )

        font.color =
            Color.LIGHT_GRAY

        font.draw(
            batch,
            definition.description,
            layout.modalPanel.x +
                390f,
            layout.modalPanel.y +
                layout.modalPanel.height -
                95f,
            layout.modalPanel.width -
                420f,
            Align.left,
            true
        )

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

        val totalBonusText =
            definition.statModifiers
                .joinToString(
                    separator = "\n"
                ) { modifier ->
                    val totalAmount =
                        modifier.amount *
                            entry.count

                    "${modifier.statType.displayName}: " +
                        formatSignedValue(
                            totalAmount
                        )
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

        font.draw(
            batch,
            definition.description,
            layout.modalPanel.x +
                24f,
            layout.modalPanel.y +
                layout.modalPanel.height -
                100f,
            layout.modalPanel.width -
                48f,
            Align.left,
            true
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
                "\n\nTotal Bonus:\n" +
                totalBonusText,
            layout.modalPanel.x +
                24f,
            layout.modalPanel.y +
                145f
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
                        "No Armor Equipped."
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
                    "WEAPON TAMGA"

                description =
                    "No Weapon Tamga Equipped.\n\n" +
                        "Weapon Tamgas will modify Weapon stats " +
                        "or attack behaviour."
            }

            PauseGearDetailType.ARMOR_TAMGA -> {
                title =
                    "ARMOR TAMGA"

                description =
                    "No Armor Tamga Equipped.\n\n" +
                        "Armor Tamgas will be connected after " +
                        "the Armor system is implemented."
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

        font.draw(
            batch,
            description,
            layout.modalPanel.x +
                24f,
            layout.modalPanel.y +
                layout.modalPanel.height -
                110f,
            layout.modalPanel.width -
                48f,
            Align.left,
            true
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
        return when (level) {
            1 -> "I"
            2 -> "II"
            3 -> "III"
            4 -> "IV"
            5 -> "V"
            else -> level.toString()
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
        Rectangle
    ) {
        font.color =
            Color.WHITE

        font.draw(
            batch,
            text,
            rectangle.x +
                TEXT_PADDING,
            rectangle.y +
                rectangle.height -
                13f
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
        drawFilledRectangle(
            rectangle =
                rectangle,

            color =
                panelBorderColor
        )

        drawFilledRectangle(
            rectangle =
                Rectangle(
                    rectangle.x +
                        PANEL_BORDER,

                    rectangle.y +
                        PANEL_BORDER,

                    rectangle.width -
                        PANEL_BORDER * 2f,

                    rectangle.height -
                        PANEL_BORDER * 2f
                ),

            color =
                fillColor
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
    }
}
