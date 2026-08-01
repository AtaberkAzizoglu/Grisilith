package com.azizoglu.grisilith.screen.preset

import com.azizoglu.grisilith.GrisilithGame
import com.azizoglu.grisilith.character.CharacterDefinition
import com.azizoglu.grisilith.character.DefaultCharacters
import com.azizoglu.grisilith.content.ContentUnlockState
import com.azizoglu.grisilith.screen.components.MenuButton
import com.azizoglu.grisilith.screen.components.MenuPointer
import com.azizoglu.grisilith.statistics.StatType
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack
import com.badlogic.gdx.utils.viewport.FitViewport
import com.azizoglu.grisilith.armor.definition.ArmorDefinition
import com.azizoglu.grisilith.armor.definition.DefaultArmors
import com.azizoglu.grisilith.weapon.definition.DefaultWeapons
import com.azizoglu.grisilith.weapon.definition.WeaponDefinition
import com.azizoglu.grisilith.content.ContentTag
import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.weapon.definition.WeaponAttackPattern
import com.azizoglu.grisilith.weapon.runtime.WeaponDamageCalculator
import com.azizoglu.grisilith.weapon.runtime.WeaponInstance
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.InputProcessor
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt
import com.azizoglu.grisilith.item.definition.DefaultItems
import com.azizoglu.grisilith.item.definition.ItemDefinition
import com.azizoglu.grisilith.content.DefaultContentRegistry
import com.azizoglu.grisilith.preset.PresetRepository
import com.azizoglu.grisilith.preset.PresetRepositoryResult
import com.azizoglu.grisilith.preset.RunPreset
import com.azizoglu.grisilith.tamga.definition.DefaultTamgas
import com.azizoglu.grisilith.tamga.definition.TamgaActivationType
import com.azizoglu.grisilith.tamga.definition.TamgaCompatibility
import com.azizoglu.grisilith.tamga.definition.TamgaCompatibilityResult
import com.azizoglu.grisilith.tamga.definition.TamgaDefinition
import com.azizoglu.grisilith.tamga.definition.TamgaPresetSlot
import com.azizoglu.grisilith.tamga.definition.TamgaSlotType
import com.azizoglu.grisilith.content.ContentUnlockResolver

class CreatePresetScreen(
    private val game:
    GrisilithGame,

    private val editingPresetId:
    String? = null
) : ScreenAdapter() {

    companion object {

        private const val VIEWPORT_WIDTH =
            1280f

        private const val VIEWPORT_HEIGHT =
            720f

        private const val PANEL_TOP =
            575f

        private const val PANEL_BOTTOM =
            75f

        private const val EQUIPMENT_DRAG_THRESHOLD =
            8f

        private const val LIST_DRAG_THRESHOLD =
            5f

        private const val LIST_ROW_HEIGHT =
            34f

        private const val LIST_ROW_GAP =
            5f

        private const val LIST_ROW_PITCH =
            LIST_ROW_HEIGHT +
                LIST_ROW_GAP

        private const val MOUSE_WHEEL_SCROLL_PIXELS =
            34f

        private const val LIST_BUTTON_SCROLL_PIXELS =
            LIST_ROW_PITCH * 2f
    }

    private val camera =
        OrthographicCamera()

    private val viewport =
        FitViewport(
            VIEWPORT_WIDTH,
            VIEWPORT_HEIGHT,
            camera
        )

    private val batch =
        SpriteBatch()

    private val font =
        BitmapFont()

    private val pointer =
        MenuPointer(
            viewport
        )

    private val whitePixel =
        createWhitePixel()

    private val interactionState =
        CreatePresetInteractionState()

    private val characters =
        DefaultCharacters.all

    private val weapons =
        DefaultWeapons.all

    private val armors =
        DefaultArmors.all

    private val items =
        DefaultItems.all

    private val tamgas =
        DefaultTamgas.all

    private val backButton =
        MenuButton(
            text =
                "BACK",

            x =
                35f,

            y =
                635f,

            width =
                170f,

            height =
                55f
        ) {
            requestBack()
        }

    private val lockFilterButton =
        MenuButton(
            text =
                "LOCK FILTER",

            x =
                1065f,

            y =
                635f,

            width =
                180f,

            height =
                55f
        ) {
            interactionState
                .toggleLockedContentVisibility()
        }

    private val saveButton =
        MenuButton(
            text =
                "SAVE PRESET",

            x =
                505f,

            y =
                18f,

            width =
                270f,

            height =
                48f,

            isEnabled =
                false
        ) {
            requestSave()
        }

    private val normalButtons =
        listOf(
            backButton,
            lockFilterButton,
            saveButton
        )

    private val leftPanel =
        Rectangle(
            28f,
            PANEL_BOTTOM,
            335f,
            PANEL_TOP - PANEL_BOTTOM
        )

    private val centerPanel =
        Rectangle(
            382f,
            PANEL_BOTTOM,
            516f,
            PANEL_TOP - PANEL_BOTTOM
        )

    private val rightPanel =
        Rectangle(
            917f,
            PANEL_BOTTOM,
            335f,
            PANEL_TOP - PANEL_BOTTOM
        )

    private val characterPortrait =
        Rectangle(
            centerPanel.x + 148f,
            centerPanel.y + 150f,
            220f,
            252f
        )

    private val weaponSlot =
        Rectangle(
            centerPanel.x + 30f,
            centerPanel.y + 315f,
            92f,
            78f
        )

    private val weaponTamgaSlot =
        Rectangle(
            centerPanel.x + 30f,
            centerPanel.y + 184f,
            92f,
            78f
        )

    private val armorSlot =
        Rectangle(
            centerPanel.x + centerPanel.width - 122f,
            centerPanel.y + 315f,
            92f,
            78f
        )

    private val armorTamgaSlot =
        Rectangle(
            centerPanel.x + centerPanel.width - 122f,
            centerPanel.y + 184f,
            92f,
            78f
        )

    private val itemSlot =
        Rectangle(
            centerPanel.x + 212f,
            centerPanel.y + 28f,
            92f,
            78f
        )

    private val editStatsButtonRectangle =
        Rectangle(
            centerPanel.x + 148f,
            centerPanel.y + centerPanel.height - 68f,
            220f,
            44f
        )

    private var weaponScrollOffset =
        0f

    private var armorScrollOffset =
        0f

    private var itemScrollOffset =
        0f

    private var tamgaScrollOffset =
        0f

    private var previousInputProcessor:
        InputProcessor? = null

    private var activeListGesture:
        CreatePresetListGesture? =
        null

    private var listGestureMode:
        CreatePresetListGestureMode =
        CreatePresetListGestureMode.NONE

    private var listGestureStartX =
        0f

    private var listGestureStartY =
        0f

    private var previousListGestureY =
        0f

    private var accumulatedListGestureY =
        0f


    override fun show() {
        viewport.apply()

        camera.position.set(
            VIEWPORT_WIDTH / 2f,
            VIEWPORT_HEIGHT / 2f,
            0f
        )

        camera.update()

        pointer.reset()
        interactionState.reset()

        editingPresetId
            ?.let { presetId ->
                PresetRepository
                    .findById(
                        presetId
                    )
                    ?.let { preset ->
                        interactionState
                            .loadFromPreset(
                                preset
                            )
                    }
            }

        weaponScrollOffset =
            0f

        armorScrollOffset =
            0f

        itemScrollOffset =
            0f

        tamgaScrollOffset =
            0f

        clearListGesture()

        previousInputProcessor =
            Gdx.input.inputProcessor

        Gdx.input.inputProcessor =
            object : InputAdapter() {

                override fun scrolled(
                    amountX:
                    Float,

                    amountY:
                    Float
                ): Boolean {

                    pointer.updatePosition()

                    if (
                        interactionState
                            .isAnyModalOpen ||
                        interactionState
                            .draggedContent != null
                    ) {
                        return true
                    }

                    /*
                     * Önceki sürümde tekerlek yönü ters
                     * hissediyordu. Bu nedenle amountY
                     * işaretini burada ters çeviriyoruz.
                     */
                    val pixelAmount =
                        -amountY *
                            MOUSE_WHEEL_SCROLL_PIXELS

                    return when {
                        weaponListArea()
                            .contains(
                                pointer.position
                            ) -> {
                            scrollWeapons(
                                pixelAmount
                            )

                            true
                        }

                        armorListArea()
                            .contains(
                                pointer.position
                            ) -> {
                            scrollArmors(
                                pixelAmount
                            )

                            true
                        }

                        itemListArea()
                            .contains(
                                pointer.position
                            ) -> {
                            scrollItems(
                                pixelAmount
                            )

                            true
                        }

                        tamgaListArea()
                            .contains(
                                pointer.position
                            ) -> {
                            scrollTamgas(
                                pixelAmount
                            )

                            true
                        }

                        else -> {
                            false
                        }
                    }
                }
            }
    }

    override fun render(
        delta:
        Float
    ) {
        pointer.update()

        saveButton.isEnabled =
            interactionState
                .hasRequiredSelections

        if (
            interactionState
                .isAnyModalOpen
        ) {
            handleModalInput()
        } else {
            handleNormalInput()
        }

        renderScreen()
    }

    private fun handleNormalInput() {
        if (handleListControlClick()) {
            return
        }

        if (
            handleListAndEquipmentGesture()
        ) {
            return
        }

        normalButtons.forEach { button ->
            val screenChanged =
                button.update(
                    pointerPosition =
                        pointer.position,

                    pointerJustPressed =
                        pointer.justPressed,

                    pointerJustReleased =
                        pointer.justReleased
                )

            if (screenChanged) {
                return
            }
        }

        if (!pointer.justReleased) {
            return
        }

        if (
            characterPortrait.contains(
                pointer.position
            )
        ) {
            interactionState
                .openCharacterPicker()

            return
        }

        val selectedWeapon =
            selectedWeapon()

        if (
            selectedWeapon != null &&
            weaponSlot.contains(
                pointer.position
            )
        ) {
            interactionState
                .inspectWeapon(
                    selectedWeapon.id
                )

            return
        }

        val selectedArmor =
            selectedArmor()

        if (
            selectedArmor != null &&
            armorSlot.contains(
                pointer.position
            )
        ) {
            interactionState
                .inspectArmor(
                    selectedArmor.id
                )

            return
        }

        val selectedItem =
            selectedItem()

        if (
            selectedItem != null &&
            itemSlot.contains(
                pointer.position
            )
        ) {
            interactionState
                .inspectItem(
                    selectedItem.id
                )

            return
        }

        selectedWeaponTamga()
            ?.let { tamga ->
                if (
                    weaponTamgaSlot.contains(
                        pointer.position
                    )
                ) {
                    interactionState.inspectTamga(
                        tamga.id
                    )

                    return
                }
            }

        selectedArmorTamga()
            ?.let { tamga ->
                if (
                    armorTamgaSlot.contains(
                        pointer.position
                    )
                ) {
                    interactionState.inspectTamga(
                        tamga.id
                    )

                    return
                }
            }

        if (
            editStatsButtonRectangle.contains(
                pointer.position
            )
        ) {
            interactionState
                .openStatEditor()

            return
        }
    }

    private fun handleListAndEquipmentGesture():
        Boolean {

        if (pointer.justPressed) {
            val gesture =
                createListGestureAtPointer()

            if (gesture != null) {
                activeListGesture =
                    gesture

                listGestureMode =
                    CreatePresetListGestureMode.PENDING

                listGestureStartX =
                    pointer.position.x

                listGestureStartY =
                    pointer.position.y

                previousListGestureY =
                    pointer.position.y

                return true
            }
        }

        val gesture =
            activeListGesture
                ?: return false

        if (pointer.isHeld) {
            val deltaX =
                pointer.position.x -
                    listGestureStartX

            val deltaY =
                pointer.position.y -
                    listGestureStartY

            val absoluteX =
                abs(
                    deltaX
                )

            val absoluteY =
                abs(
                    deltaY
                )

            if (
                listGestureMode ==
                CreatePresetListGestureMode.PENDING &&
                (
                    absoluteX >=
                        LIST_DRAG_THRESHOLD ||
                        absoluteY >=
                        LIST_DRAG_THRESHOLD
                    )
            ) {
                if (
                    absoluteY >=
                    absoluteX
                ) {
                    listGestureMode =
                        CreatePresetListGestureMode.SCROLLING
                } else if (
                    gesture.visibleContentIndex !=
                    null &&
                    gesture.equipmentType !=
                    null
                ) {
                    val dragStarted =
                        beginGestureEquipmentDrag(
                            gesture
                        )

                    listGestureMode =
                        if (dragStarted) {
                            CreatePresetListGestureMode
                                .EQUIPMENT_DRAGGING
                        } else {
                            CreatePresetListGestureMode
                                .SCROLLING
                        }
                } else {
                    listGestureMode =
                        CreatePresetListGestureMode.SCROLLING
                }
            }

            if (
                listGestureMode ==
                CreatePresetListGestureMode.SCROLLING
            ) {
                val currentY =
                    pointer.position.y

                /*
                 * Liste, fareyle tutulmuş fiziksel bir
                 * yüzey gibi imleci takip eder.
                 */
                val pixelMovement =
                    currentY -
                        previousListGestureY

                scrollListSection(
                    section =
                        gesture.section,

                    amount =
                        pixelMovement
                )

                previousListGestureY =
                    currentY
            }

            return true
        }

        if (pointer.justReleased) {
            when (listGestureMode) {
                CreatePresetListGestureMode.PENDING -> {
                    openGestureContentDetail(
                        gesture
                    )
                }

                CreatePresetListGestureMode
                    .EQUIPMENT_DRAGGING -> {
                    finishEquipmentDrag()
                }

                else -> {
                }
            }

            clearListGesture()

            return true
        }

        return true
    }

    private fun createListGestureAtPointer():
        CreatePresetListGesture? {

        val weaponIndex =
            weaponCardRectangles()
                .indexOfFirst { rectangle ->
                    rectangle.contains(
                        pointer.position
                    )
                }
                .takeIf { index ->
                    index >= 0
                }

        if (
            weaponListArea()
                .contains(
                    pointer.position
                )
        ) {
            return CreatePresetListGesture(
                section =
                    CreatePresetListSection.WEAPONS,

                visibleContentIndex =
                    weaponIndex,

                equipmentType =
                    if (weaponIndex != null) {
                        CreatePresetEquipmentType.WEAPON
                    } else {
                        null
                    }
            )
        }

        val armorIndex =
            armorCardRectangles()
                .indexOfFirst { rectangle ->
                    rectangle.contains(
                        pointer.position
                    )
                }
                .takeIf { index ->
                    index >= 0
                }

        if (
            armorListArea()
                .contains(
                    pointer.position
                )
        ) {
            return CreatePresetListGesture(
                section =
                    CreatePresetListSection.ARMORS,

                visibleContentIndex =
                    armorIndex,

                equipmentType =
                    if (armorIndex != null) {
                        CreatePresetEquipmentType.ARMOR
                    } else {
                        null
                    }
            )
        }

        val itemIndex =
            itemCardRectangles()
                .indexOfFirst { rectangle ->
                    rectangle.contains(
                        pointer.position
                    )
                }
                .takeIf { index ->
                    index >= 0
                }

        if (
            itemListArea()
                .contains(
                    pointer.position
                )
        ) {
            return CreatePresetListGesture(
                section =
                    CreatePresetListSection.ITEMS,

                visibleContentIndex =
                    itemIndex,

                equipmentType =
                    if (itemIndex != null) {
                        CreatePresetEquipmentType.ITEM
                    } else {
                        null
                    }
            )
        }

        val tamgaIndex =
            tamgaCardRectangles()
                .indexOfFirst { rectangle ->
                    rectangle.contains(
                        pointer.position
                    )
                }
                .takeIf { index ->
                    index >= 0
                }

        if (
            tamgaListArea()
                .contains(
                    pointer.position
                )
        ) {
            return CreatePresetListGesture(
                section =
                    CreatePresetListSection.TAMGAS,

                visibleContentIndex =
                    tamgaIndex,

                equipmentType =
                    if (tamgaIndex != null) {
                        CreatePresetEquipmentType.TAMGA
                    } else {
                        null
                    }
            )
        }

        return null
    }

    private fun beginGestureEquipmentDrag(
        gesture:
        CreatePresetListGesture
    ): Boolean {

        if (
            !interactionState
                .hasCharacterSelected
        ) {
            return false
        }

        val index =
            gesture.visibleContentIndex
                ?: return false

        when (gesture.equipmentType) {

            CreatePresetEquipmentType.TAMGA -> {
                val tamga =
                    visibleTamgas()
                        .getOrNull(
                            index
                        )
                        ?: return false

                if (
                    !ContentUnlockResolver
                        .isUnlocked(
                            tamga
                        )
                ) {
                    return false
                }

                interactionState.beginTamgaDrag(
                    tamga.id
                )

                return true
            }

            CreatePresetEquipmentType.WEAPON -> {
                val weapon =
                    visibleWeapons()
                        .getOrNull(
                            index
                        )
                        ?: return false

                if (
                    !ContentUnlockResolver
                        .isUnlocked(
                            weapon
                        )
                ) {
                    return false
                }

                interactionState
                    .beginWeaponDrag(
                        weapon.id
                    )

                return true
            }

            CreatePresetEquipmentType.ARMOR -> {
                val armor =
                    visibleArmors()
                        .getOrNull(
                            index
                        )
                        ?: return false

                if (
                    !ContentUnlockResolver
                        .isUnlocked(
                            armor
                        )
                ) {
                    return false
                }

                interactionState
                    .beginArmorDrag(
                        armor.id
                    )

                return true
            }

            CreatePresetEquipmentType.ITEM -> {
                val item =
                    visibleItems()
                        .getOrNull(
                            index
                        )
                        ?: return false

                if (
                    !ContentUnlockResolver
                        .isUnlocked(
                            item
                        )
                ) {
                    return false
                }

                interactionState
                    .beginItemDrag(
                        item.id
                    )

                return true
            }

            null -> {
                return false
            }
        }
    }

    private fun openGestureContentDetail(
        gesture:
        CreatePresetListGesture
    ) {
        val index =
            gesture.visibleContentIndex
                ?: return

        when (gesture.equipmentType) {

            CreatePresetEquipmentType.TAMGA -> {
                val tamga =
                    visibleTamgas()
                        .getOrNull(
                            index
                        )
                        ?: return

                interactionState.inspectTamga(
                    tamga.id
                )
            }

            CreatePresetEquipmentType.WEAPON -> {
                val weapon =
                    visibleWeapons()
                        .getOrNull(
                            index
                        )
                        ?: return

                interactionState
                    .inspectWeapon(
                        weapon.id
                    )
            }

            CreatePresetEquipmentType.ARMOR -> {
                val armor =
                    visibleArmors()
                        .getOrNull(
                            index
                        )
                        ?: return

                interactionState
                    .inspectArmor(
                        armor.id
                    )
            }

            CreatePresetEquipmentType.ITEM -> {
                val item =
                    visibleItems()
                        .getOrNull(
                            index
                        )
                        ?: return

                interactionState
                    .inspectItem(
                        item.id
                    )
            }

            null -> {
            }
        }
    }

    private fun scrollListSection(
        section:
        CreatePresetListSection,

        amount:
        Float
    ) {
        when (section) {
            CreatePresetListSection.WEAPONS -> {
                scrollWeapons(
                    amount
                )
            }

            CreatePresetListSection.ARMORS -> {
                scrollArmors(
                    amount
                )
            }

            CreatePresetListSection.ITEMS -> {
                scrollItems(
                    amount
                )
            }

            CreatePresetListSection.TAMGAS -> {
                scrollTamgas(
                    amount
                )
            }
        }
    }

    private fun clearListGesture() {
        activeListGesture =
            null

        listGestureMode =
            CreatePresetListGestureMode.NONE

        accumulatedListGestureY =
            0f
    }

    private fun finishEquipmentDrag() {
        val draggedContent =
            interactionState
                .draggedContent
                ?: return

        when (draggedContent.type) {

            CreatePresetEquipmentType.TAMGA -> {
                finishTamgaDrag(
                    draggedContent.contentId
                )
            }

            CreatePresetEquipmentType.WEAPON -> {
                if (
                    weaponSlot.contains(
                        pointer.position
                    )
                ) {
                    interactionState
                        .finishDragOnWeaponSlot()

                    validateSelectedTamgasAfterEquipmentChange()
                } else {
                    interactionState
                        .cancelDrag()
                }
            }

            CreatePresetEquipmentType.ARMOR -> {
                if (
                    armorSlot.contains(
                        pointer.position
                    )
                ) {
                    interactionState
                        .finishDragOnArmorSlot()

                    validateSelectedTamgasAfterEquipmentChange()
                } else {
                    interactionState
                        .cancelDrag()
                }
            }

            CreatePresetEquipmentType.ITEM -> {
                if (
                    itemSlot.contains(
                        pointer.position
                    )
                ) {
                    interactionState
                        .finishDragOnItemSlot()
                } else {
                    interactionState
                        .cancelDrag()
                }
            }
        }
    }

    private fun finishTamgaDrag(
        tamgaId:
        com.azizoglu.grisilith.content.ContentId
    ) {
        val tamga =
            tamgas.firstOrNull { definition ->
                definition.id ==
                    tamgaId
            }

        if (
            tamga == null
        ) {
            interactionState.cancelDrag()
            return
        }

        val targetSlot =
            when {
                weaponTamgaSlot.contains(
                    pointer.position
                ) -> {
                    TamgaPresetSlot
                        .WEAPON_TAMGA_SLOT
                }

                armorTamgaSlot.contains(
                    pointer.position
                ) -> {
                    TamgaPresetSlot
                        .ARMOR_TAMGA_SLOT
                }

                else -> {
                    interactionState.cancelDrag()
                    return
                }
            }

        when (
            TamgaCompatibility.check(
                tamga =
                    tamga,

                targetSlot =
                    targetSlot,

                selectedWeapon =
                    selectedWeapon(),

                selectedArmor =
                    selectedArmor()
            )
        ) {
            TamgaCompatibilityResult.Compatible -> {
                interactionState.finishTamgaDrag(
                    targetSlot =
                        targetSlot,

                    tamga =
                        tamga
                )
            }

            is TamgaCompatibilityResult.Incompatible -> {
                interactionState.cancelDrag()
            }
        }
    }

    private fun isTamgaSlotValidDropTarget(
        targetSlot:
        TamgaPresetSlot
    ): Boolean {

        val dragged =
            interactionState
                .draggedContent
                ?: return false

        if (
            dragged.type !=
            CreatePresetEquipmentType.TAMGA
        ) {
            return false
        }

        val tamga =
            tamgas.firstOrNull { definition ->
                definition.id ==
                    dragged.contentId
            }
                ?: return false

        return TamgaCompatibility.isCompatible(
            tamga =
                tamga,

            targetSlot =
                targetSlot,

            selectedWeapon =
                selectedWeapon(),

            selectedArmor =
                selectedArmor()
        )
    }

    private fun validateSelectedTamgasAfterEquipmentChange() {
        selectedWeaponTamga()
            ?.let { tamga ->
                val compatible =
                    TamgaCompatibility.isCompatible(
                        tamga =
                            tamga,

                        targetSlot =
                            TamgaPresetSlot
                                .WEAPON_TAMGA_SLOT,

                        selectedWeapon =
                            selectedWeapon(),

                        selectedArmor =
                            selectedArmor()
                    )

                if (
                    !compatible
                ) {
                    interactionState.clearWeaponTamga()
                }
            }

        selectedArmorTamga()
            ?.let { tamga ->
                val compatible =
                    TamgaCompatibility.isCompatible(
                        tamga =
                            tamga,

                        targetSlot =
                            TamgaPresetSlot
                                .ARMOR_TAMGA_SLOT,

                        selectedWeapon =
                            selectedWeapon(),

                        selectedArmor =
                            selectedArmor()
                    )

                if (
                    !compatible
                ) {
                    interactionState.clearArmorTamga()
                }
            }
    }

    private fun handleModalInput() {
        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.ESCAPE
            )
        ) {
            interactionState.closeModals()
            return
        }

        if (!pointer.justReleased) {
            return
        }

        if (
            interactionState
                .isConfirmationOpen
        ) {
            when {
                modalConfirmButton()
                    .contains(
                        pointer.position
                    ) -> {
                    when (
                        interactionState
                            .confirmationType
                    ) {
                        CreatePresetConfirmationType.DISCARD_AND_GO_BACK -> {
                            game.showPresetMenu()
                        }

                        CreatePresetConfirmationType.SAVE_PRESET -> {
                            savePreset()
                        }

                        else -> {
                        }
                    }
                }

                modalCancelButton()
                    .contains(
                        pointer.position
                    ) -> {
                    interactionState
                        .closeModals()
                }
            }

            return
        }

        if (
            interactionState
                .isStatEditorOpen
        ) {
            /*
             * Küçük açıklama kutusu açıksa:
             * - Kutunun içine basmak bir şey yapmaz.
             * - Kutunun dışına basmak açıklamayı kapatır.
             * - Ardından aynı tıklama Edit Stats üzerinde
             *   normal şekilde işlenebilir.
             */
            if (
                interactionState
                    .inspectedStatType !=
                null
            ) {
                if (
                    statDescriptionRectangle()
                        .contains(
                            pointer.position
                        )
                ) {
                    return
                }

                interactionState
                    .closeStatDescription()
            }

            handleStatEditorClick()

            return
        }

        if (
            interactionState
                .isCharacterPickerOpen
        ) {
            val visibleCharacters =
                visibleCharacters()

            characterPickerCardRectangles()
                .forEachIndexed {
                        index,
                        rectangle ->

                    val character =
                        visibleCharacters
                            .getOrNull(
                                index
                            )
                            ?: return@forEachIndexed

                    if (
                        rectangle.contains(
                            pointer.position
                        )
                    ) {
                        /*
                         * Kilitli içerikler gösterilse bile
                         * seçilemez.
                         */
                        if (
                            character.initialUnlockState ==
                            ContentUnlockState.UNLOCKED
                        ) {
                            interactionState
                                .inspectCharacter(
                                    character.id
                                )
                        }

                        return
                    }
                }

            /*
             * Popup'ın dışına basılırsa yalnızca
             * Character Picker kapanır.
             */
            if (
                !characterPickerRectangle()
                    .contains(
                        pointer.position
                    )
            ) {
                interactionState
                    .closeModals()
            }

            return
        }

        if (
            interactionState
                .isTamgaDetailOpen
        ) {
            if (
                !modalRectangle()
                    .contains(
                        pointer.position
                    )
            ) {
                interactionState.closeModals()
            }

            return
        }

        if (
            interactionState
                .isEquipmentDetailOpen
        ) {
            if (
                !modalRectangle()
                    .contains(
                        pointer.position
                    )
            ) {
                interactionState
                    .closeModals()
            }

            return
        }

        if (
            interactionState
                .isCharacterDetailOpen
        ) {
            if (
                chooseCharacterButton()
                    .contains(
                        pointer.position
                    )
            ) {
                interactionState
                    .chooseInspectedCharacter()

                return
            }

            /*
             * Detay kartının içine basmak hiçbir
             * şey yapmaz. Dışarı basmak kapatır.
             */
            if (
                !modalRectangle()
                    .contains(
                        pointer.position
                    )
            ) {
                interactionState.closeModals()
            }
        }
    }

    private fun requestBack() {
        if (
            interactionState
                .hasUnsavedChanges
        ) {
            interactionState
                .openConfirmation(
                    CreatePresetConfirmationType
                        .DISCARD_AND_GO_BACK
                )
        } else {
            game.showPresetMenu()
        }
    }

    private fun requestSave() {
        if (
            !interactionState
                .hasRequiredSelections
        ) {
            return
        }

        interactionState
            .openConfirmation(
                CreatePresetConfirmationType
                    .SAVE_PRESET
            )
    }

    private fun savePreset() {
        val characterId =
            interactionState
                .selectedCharacterId
                ?: return

        val weaponId =
            interactionState
                .selectedWeaponId
                ?: return

        val armorId =
            interactionState
                .selectedArmorId

        val itemId =
            interactionState
                .selectedItemId

        val existingPreset =
            editingPresetId
                ?.let { presetId ->
                    PresetRepository
                        .findById(
                            presetId
                        )
                }

        val preset =
            RunPreset(
                presetId =
                    existingPreset
                        ?.presetId
                        ?: java.util.UUID
                            .randomUUID()
                            .toString(),

                presetName =
                    existingPreset
                        ?.presetName
                        ?: "Preset " +
                        (
                            PresetRepository
                                .presetCount() +
                                1
                            ),

                characterId =
                    characterId,

                weaponId =
                    weaponId,

                armorId =
                    armorId,

                itemId =
                    itemId,

                weaponTamgaId =
                    interactionState
                        .selectedWeaponTamgaId,

                armorTamgaId =
                    interactionState
                        .selectedArmorTamgaId,

                statPointBudget =
                    interactionState
                        .statPointBudget,

                statAllocation =
                    interactionState
                        .statAllocation,

                createdAtEpochMillis =
                    existingPreset
                        ?.createdAtEpochMillis
                        ?: System.currentTimeMillis(),

                updatedAtEpochMillis =
                    System.currentTimeMillis()
            )

        val result =
            if (existingPreset == null) {
                PresetRepository.create(
                    preset =
                        preset,

                    contentRegistry =
                        DefaultContentRegistry
                            .instance
                )
            } else {
                PresetRepository.update(
                    preset =
                        preset,

                    contentRegistry =
                        DefaultContentRegistry
                            .instance
                )
            }

        when (result) {
            is PresetRepositoryResult.Success -> {
                game.showPresetMenu()
            }

            is PresetRepositoryResult.Failure -> {
                interactionState
                    .closeModals()

                Gdx.app.error(
                    "CreatePresetScreen",
                    result.message
                )
            }
        }
    }

    private fun handleStatEditorClick() {
        val character =
            selectedCharacter()
                ?: return

        /*
         * Normal for döngüsü kullanıyoruz.
         * Böylece tıklama işlendiğinde doğrudan
         * handleStatEditorClick() fonksiyonundan
         * çıkabiliriz.
         */
        for (
        (
            statType,
            row
        ) in statEditorRows()
        ) {
            if (
                statMinusRectangle(
                    row
                )
                    .contains(
                        pointer.position
                    )
            ) {
                interactionState
                    .decreaseStat(
                        statType
                    )

                return
            }

            if (
                statPlusRectangle(
                    row
                )
                    .contains(
                        pointer.position
                    )
            ) {
                interactionState
                    .increaseStat(
                        statType =
                            statType,

                        characterBaseScore =
                            character.baseStat(
                                statType
                            )
                    )

                return
            }

            /*
             * + ve - kutularına basılmadıysa,
             * Stat satırının kendisine basıldığında
             * açıklama kartı açılır.
             */
            if (
                row.contains(
                    pointer.position
                )
            ) {
                interactionState
                    .inspectStat(
                        statType
                    )

                return
            }
        }

        /*
         * Stat editörünün dışına basılırsa pencere
         * kapanır.
         */
        if (
            !statEditorRectangle()
                .contains(
                    pointer.position
                )
        ) {
            interactionState
                .closeModals()
        }
    }

    private fun filteredWeapons():
        List<WeaponDefinition> {

        return weapons.filter { weapon ->

            interactionState.showLockedContent ||
                ContentUnlockResolver
                    .isUnlocked(
                        weapon
                    )
        }
    }

    private fun visibleWeapons():
        List<WeaponDefinition> {

        return filteredWeapons()
    }

    private fun filteredArmors():
        List<ArmorDefinition> {

        return armors.filter { armor ->

            interactionState.showLockedContent ||
                ContentUnlockResolver
                    .isUnlocked(
                        armor
                    )
        }
    }

    private fun visibleArmors():
        List<ArmorDefinition> {

        return filteredArmors()
    }

    private fun filteredItems():
        List<ItemDefinition> {

        return items.filter { item ->

            interactionState.showLockedContent ||
                ContentUnlockResolver
                    .isUnlocked(
                        item
                    )
        }
    }

    private fun visibleItems():
        List<ItemDefinition> {

        return filteredItems()
    }

    private fun filteredTamgas():
        List<TamgaDefinition> {

        return tamgas.filter { tamga ->

            interactionState.showLockedContent ||
                ContentUnlockResolver
                    .isUnlocked(
                        tamga
                    )
        }
    }

    private fun visibleTamgas():
        List<TamgaDefinition> {

        return filteredTamgas()
    }

    private fun maximumTamgaScrollOffset():
        Float {

        return maximumListScrollOffset(
            tamgaListArea(),
            filteredTamgas().size
        )
    }

    private fun scrollTamgas(
        amount:
        Float
    ) {
        tamgaScrollOffset =
            (
                tamgaScrollOffset +
                    amount
                )
                .coerceIn(
                    0f,
                    maximumTamgaScrollOffset()
                )
    }

    private fun maximumItemScrollOffset():
        Float {

        return maximumListScrollOffset(
            itemListArea(),
            filteredItems().size
        )
    }


    private fun scrollItems(
        amount:
        Float
    ) {
        itemScrollOffset =
            (
                itemScrollOffset +
                    amount
                )
                .coerceIn(
                    0f,
                    maximumItemScrollOffset()
                )
    }


    private fun maximumWeaponScrollOffset():
        Float {

        return maximumListScrollOffset(
            weaponListArea(),
            filteredWeapons().size
        )
    }


    private fun maximumArmorScrollOffset():
        Float {

        return maximumListScrollOffset(
            armorListArea(),
            filteredArmors().size
        )
    }


    private fun scrollWeapons(
        amount:
        Float
    ) {
        weaponScrollOffset =
            (
                weaponScrollOffset +
                    amount
                )
                .coerceIn(
                    0f,
                    maximumWeaponScrollOffset()
                )
    }


    private fun scrollArmors(
        amount:
        Float
    ) {
        armorScrollOffset =
            (
                armorScrollOffset +
                    amount
                )
                .coerceIn(
                    0f,
                    maximumArmorScrollOffset()
                )
    }


    private fun maximumListScrollOffset(
        area:
        Rectangle,

        itemCount:
        Int
    ): Float {

        if (itemCount <= 0) {
            return 0f
        }

        val contentHeight =
            itemCount *
                LIST_ROW_PITCH -
                LIST_ROW_GAP

        return (
            contentHeight -
                area.height
            )
            .coerceAtLeast(
                0f
            )
    }

    private fun visibleCharacters():
        List<CharacterDefinition> {

        return characters.filter { character ->
            when {
                interactionState
                    .showLockedContent -> {
                    true
                }

                else -> {
                    character.initialUnlockState ==
                        ContentUnlockState.UNLOCKED
                }
            }
        }
    }

    private fun selectedWeapon():
        WeaponDefinition? {

        val selectedId =
            interactionState
                .selectedWeaponId
                ?: return null

        return weapons.firstOrNull { weapon ->
            weapon.id ==
                selectedId
        }
    }

    private fun selectedArmor():
        ArmorDefinition? {

        val selectedId =
            interactionState
                .selectedArmorId
                ?: return null

        return armors.firstOrNull { armor ->
            armor.id ==
                selectedId
        }
    }

    private fun selectedItem():
        ItemDefinition? {

        val selectedId =
            interactionState
                .selectedItemId
                ?: return null

        return items.firstOrNull { item ->
            item.id ==
                selectedId
        }
    }

    private fun selectedWeaponTamga():
        TamgaDefinition? {

        val selectedId =
            interactionState
                .selectedWeaponTamgaId
                ?: return null

        return tamgas.firstOrNull { tamga ->
            tamga.id ==
                selectedId
        }
    }

    private fun selectedArmorTamga():
        TamgaDefinition? {

        val selectedId =
            interactionState
                .selectedArmorTamgaId
                ?: return null

        return tamgas.firstOrNull { tamga ->
            tamga.id ==
                selectedId
        }
    }

    private fun inspectedTamga():
        TamgaDefinition? {

        val inspectedId =
            interactionState
                .inspectedTamgaId
                ?: return null

        return tamgas.firstOrNull { tamga ->
            tamga.id ==
                inspectedId
        }
    }

    private fun selectedCharacter():
        CharacterDefinition? {

        val selectedId =
            interactionState
                .selectedCharacterId
                ?: return null

        return characters.firstOrNull { character ->
            character.id ==
                selectedId
        }
    }

    private fun createPresetPreviewStats():
        PlayerStats? {

        val character =
            selectedCharacter()
                ?: return null

        val finalBaseStats =
            interactionState
                .statAllocation
                .finalScores(
                    character.baseStats
                )

        return PlayerStats(
            initialBaseScores =
                finalBaseStats
        ).also {
                previewStats ->

            val gearBonuses =
                StatType.entries
                    .associateWith { 0 }
                    .toMutableMap()

            selectedArmor()
                ?.allStatModifiers()
                ?.forEach { modifier ->
                    gearBonuses[
                        modifier.statType
                    ] =
                        (
                            gearBonuses[
                                modifier.statType
                            ] ?: 0
                            ) +
                            modifier.amount
                }

            selectedItem()
                ?.statModifiers
                ?.forEach { modifier ->
                    gearBonuses[
                        modifier.statType
                    ] =
                        (
                            gearBonuses[
                                modifier.statType
                            ] ?: 0
                            ) +
                            modifier.amount
                }

            previewStats
                .setGearBonuses(
                    gearBonuses
                )
        }
    }

    private fun inspectedCharacter():
        CharacterDefinition? {

        val inspectedId =
            interactionState
                .inspectedCharacterId
                ?: return null

        return characters.firstOrNull { character ->
            character.id ==
                inspectedId
        }
    }

    private fun renderScreen() {
        Gdx.gl.glClearColor(
            0.035f,
            0.025f,
            0.055f,
            1f
        )

        Gdx.gl.glClear(
            GL20.GL_COLOR_BUFFER_BIT
        )

        batch.projectionMatrix =
            camera.combined

        batch.begin()

        renderHeader()
        renderPanels()
        renderLeftPanel()
        renderCenterPanel()
        renderRightPanel()

        normalButtons.forEach { button ->
            button.render(
                batch =
                    batch,

                font =
                    font,

                pointerPosition =
                    pointer.position,

                pointerHeld =
                    pointer.isHeld
            )
        }

        renderLockState()
        renderDraggedEquipmentPreview()

        if (
            interactionState
                .isAnyModalOpen
        ) {
            renderModal()
        }

        batch.end()
    }

    private fun renderDraggedEquipmentPreview() {
        val dragged =
            interactionState
                .draggedContent
                ?: return

        val displayName =
            when (dragged.type) {

                CreatePresetEquipmentType.TAMGA -> {
                    tamgas
                        .firstOrNull { tamga ->
                            tamga.id ==
                                dragged.contentId
                        }
                        ?.displayName
                }

                CreatePresetEquipmentType.WEAPON -> {
                    weapons
                        .firstOrNull { weapon ->
                            weapon.id ==
                                dragged.contentId
                        }
                        ?.displayName
                }

                CreatePresetEquipmentType.ARMOR -> {
                    armors
                        .firstOrNull { armor ->
                            armor.id ==
                                dragged.contentId
                        }
                        ?.displayName
                }

                CreatePresetEquipmentType.ITEM -> {
                    items
                        .firstOrNull { item ->
                            item.id ==
                                dragged.contentId
                        }
                        ?.displayName
                }
            }
                ?: return

        val preview =
            Rectangle(
                pointer.position.x -
                    60f,

                pointer.position.y -
                    30f,

                120f,

                60f
            )

        drawPanel(
            rectangle =
                preview,

            fillColor =
                Color(
                    0.08f,
                    0.20f,
                    0.29f,
                    0.94f
                )
        )

        drawCenteredText(
            text =
                displayName,

            rectangle =
                preview,

            color =
                Color.WHITE,

            scale =
                0.62f
        )
    }

    private fun renderHeader() {
        font.data.setScale(
            1.8f
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            if (editingPresetId == null) {
                "CREATE PRESET"
            } else {
                "EDIT PRESET"
            },
            0f,
            672f,
            VIEWPORT_WIDTH,
            Align.center,
            false
        )

        font.data.setScale(
            0.82f
        )

        font.color =
            Color.GRAY

        font.draw(
            batch,
            "Choose a Character first.",
            0f,
            610f,
            VIEWPORT_WIDTH,
            Align.center,
            false
        )

        font.data.setScale(
            1f
        )
    }

    private fun renderPanels() {
        drawPanel(centerPanel, Color(0.20f, 0.06f, 0.25f, 1f))
        drawPanel(weaponPanel(), Color(0.04f, 0.20f, 0.09f, 1f))
        drawPanel(armorPanel(), Color(0.10f, 0.14f, 0.18f, 1f))
        drawPanel(itemPanel(), Color(0.18f, 0.07f, 0.09f, 1f))
        drawPanel(tamgaPanel(), Color(0.09f, 0.12f, 0.24f, 1f))
    }


    private fun weaponPanel() =
        Rectangle(leftPanel.x, leftPanel.y + 242f, leftPanel.width, 258f)

    private fun armorPanel() =
        Rectangle(leftPanel.x, leftPanel.y, leftPanel.width, 224f)

    private fun itemPanel() =
        Rectangle(rightPanel.x, rightPanel.y + 242f, rightPanel.width, 258f)

    private fun tamgaPanel() =
        Rectangle(rightPanel.x, rightPanel.y, rightPanel.width, 224f)

    private fun weaponListArea(): Rectangle {
        val panel = weaponPanel()
        return Rectangle(panel.x + 8f, panel.y + 35f, panel.width - 16f, panel.height - 76f)
    }


    private fun armorListArea(): Rectangle {
        val panel = armorPanel()
        return Rectangle(panel.x + 8f, panel.y + 35f, panel.width - 16f, panel.height - 76f)
    }


    private fun itemListArea(): Rectangle {
        val panel = itemPanel()
        return Rectangle(panel.x + 8f, panel.y + 35f, panel.width - 16f, panel.height - 76f)
    }


    private fun weaponCardRectangles():
        List<Rectangle> {

        return listRowRectangles(
            area =
                weaponListArea(),

            itemCount =
                filteredWeapons().size,

            scrollOffset =
                weaponScrollOffset
        )
    }


    private fun armorCardRectangles():
        List<Rectangle> {

        return listRowRectangles(
            area =
                armorListArea(),

            itemCount =
                filteredArmors().size,

            scrollOffset =
                armorScrollOffset
        )
    }


    private fun tamgaListArea(): Rectangle {
        val panel =
            tamgaPanel()

        return Rectangle(
            panel.x + 8f,
            panel.y + 35f,
            panel.width - 16f,
            panel.height - 76f
        )
    }

    private fun itemCardRectangles():
        List<Rectangle> {

        return listRowRectangles(
            area =
                itemListArea(),

            itemCount =
                filteredItems().size,

            scrollOffset =
                itemScrollOffset
        )
    }


    private fun tamgaCardRectangles():
        List<Rectangle> {

        return listRowRectangles(
            area =
                tamgaListArea(),

            itemCount =
                filteredTamgas().size,

            scrollOffset =
                tamgaScrollOffset
        )
    }

    private fun listRowRectangles(
        area:
        Rectangle,

        itemCount:
        Int,

        scrollOffset:
        Float
    ): List<Rectangle> {

        return List(
            itemCount
        ) { index ->
            Rectangle(
                area.x +
                    4f,

                area.y +
                    area.height -
                    LIST_ROW_HEIGHT -
                    index *
                    LIST_ROW_PITCH +
                    scrollOffset,

                area.width -
                    8f,

                LIST_ROW_HEIGHT
            )
        }
    }

    private fun Rectangle.intersectsVertically(
        area:
        Rectangle
    ): Boolean {

        return y <
            area.y +
            area.height &&
            y +
            height >
            area.y
    }

    private fun renderLinearListRow(
        rectangle:
        Rectangle,

        title:
        String,

        subtitle:
        String,

        unlocked:
        Boolean,

        selected:
        Boolean,

        normalFillColor:
        Color
    ) {
        drawPanel(
            rectangle =
                rectangle,

            fillColor =
                when {
                    !unlocked -> {
                        Color(
                            0.16f,
                            0.16f,
                            0.16f,
                            1f
                        )
                    }

                    selected -> {
                        Color(
                            0.08f,
                            0.40f,
                            0.22f,
                            1f
                        )
                    }

                    else -> {
                        normalFillColor
                    }
                }
        )

        val imageRectangle =
            Rectangle(
                rectangle.x +
                    6f,
                rectangle.y +
                    4f,
                26f,
                26f
            )

        drawPanel(
            rectangle =
                imageRectangle,

            fillColor =
                if (unlocked) {
                    Color(
                        0.10f,
                        0.13f,
                        0.18f,
                        1f
                    )
                } else {
                    Color(
                        0.12f,
                        0.12f,
                        0.12f,
                        1f
                    )
                }
        )

        drawCenteredText(
            text =
                "IMG",

            rectangle =
                imageRectangle,

            color =
                if (unlocked) {
                    Color.GRAY
                } else {
                    Color.DARK_GRAY
                },

            scale =
                0.38f
        )

        font.color =
            if (unlocked) {
                Color.WHITE
            } else {
                Color.GRAY
            }

        font.data.setScale(
            0.72f
        )

        font.draw(
            batch,
            if (unlocked) {
                title
            } else {
                "$title — LOCKED"
            },
            rectangle.x +
                40f,
            rectangle.y +
                23f,
            rectangle.width -
                50f,
            Align.left,
            false
        )

        font.color =
            Color.GRAY

        font.data.setScale(
            0.55f
        )

        font.draw(
            batch,
            subtitle,
            rectangle.x +
                40f,
            rectangle.y +
                9f,
            rectangle.width -
                50f,
            Align.left,
            false
        )

        font.data.setScale(
            1f
        )
    }

    private fun renderListHeader(
        title:
        String,

        panel:
        Rectangle,

        scrollOffset:
        Float,

        totalCount:
        Int,

        section:
        CreatePresetListSection
    ) {
        drawSectionTitle(
            title,
            panel.x +
                8f,
            panel.y +
                panel.height -
                16f,
            panel.width -
                90f
        )

        val up =
            listUpButton(
                section
            )

        val down =
            listDownButton(
                section
            )

        drawPanel(
            up,
            Color(
                0.08f,
                0.16f,
                0.25f,
                1f
            )
        )

        drawPanel(
            down,
            Color(
                0.08f,
                0.16f,
                0.25f,
                1f
            )
        )

        drawCenteredText(
            "▲",
            up,
            Color.WHITE,
            0.7f
        )

        drawCenteredText(
            "▼",
            down,
            Color.WHITE,
            0.7f
        )

        renderListPosition(
            scrollOffset =
                scrollOffset,

            totalCount =
                totalCount,

            section =
                section,

            x =
                panel.x +
                    14f,

            y =
                panel.y +
                    14f
        )
    }

    private fun listUpButton(section: CreatePresetListSection): Rectangle {
        val panel = when (section) {
            CreatePresetListSection.WEAPONS -> weaponPanel()
            CreatePresetListSection.ARMORS -> armorPanel()
            CreatePresetListSection.ITEMS -> itemPanel()
            CreatePresetListSection.TAMGAS -> tamgaPanel()
        }
        return Rectangle(panel.x + panel.width - 70f, panel.y + panel.height - 34f, 28f, 24f)
    }

    private fun listDownButton(section: CreatePresetListSection): Rectangle {
        val up = listUpButton(section)
        return Rectangle(up.x + 34f, up.y, 28f, 24f)
    }

    private fun handleListControlClick(): Boolean {
        if (!pointer.justReleased) return false
        for (section in CreatePresetListSection.values()) {
            if (listUpButton(section).contains(pointer.position)) {
                scrollListSection(section, -LIST_BUTTON_SCROLL_PIXELS)
                return true
            }
            if (listDownButton(section).contains(pointer.position)) {
                scrollListSection(section, LIST_BUTTON_SCROLL_PIXELS)
                return true
            }
        }
        return false
    }

    private inline fun renderClippedList(
        area:
        Rectangle,

        renderContent:
            () -> Unit
    ) {
        val scissors =
            Rectangle()

        ScissorStack.calculateScissors(
            camera,
            batch.transformMatrix,
            area,
            scissors
        )

        batch.flush()

        if (
            ScissorStack.pushScissors(
                scissors
            )
        ) {
            renderContent()

            batch.flush()

            ScissorStack.popScissors()
        }
    }

    private fun renderLeftPanel() {
        renderListHeader(
            title =
                "WEAPONS",

            panel =
                weaponPanel(),

            scrollOffset =
                weaponScrollOffset,

            totalCount =
                filteredWeapons().size,

            section =
                CreatePresetListSection.WEAPONS
        )

        renderClippedList(
            area =
                weaponListArea()
        ) {
            val definitions =
                filteredWeapons()

            weaponCardRectangles()
                .forEachIndexed {
                        index,
                        rectangle ->

                    if (
                        !rectangle.intersectsVertically(
                            weaponListArea()
                        )
                    ) {
                        return@forEachIndexed
                    }

                    val weapon =
                        definitions
                            .getOrNull(
                                index
                            )
                            ?: return@forEachIndexed

                    renderLinearListRow(
                        rectangle =
                            rectangle,

                        title =
                            weapon.displayName,

                        subtitle =
                            weaponTypeText(
                                weapon
                            ),

                        unlocked =
                            ContentUnlockResolver
                                .isUnlocked(
                                    weapon
                                ),

                        selected =
                            weapon.id ==
                                interactionState
                                    .selectedWeaponId,

                        normalFillColor =
                            Color(
                                0.07f,
                                0.27f,
                                0.12f,
                                1f
                            )
                    )
                }
        }

        renderListHeader(
            title =
                "ARMORS",

            panel =
                armorPanel(),

            scrollOffset =
                armorScrollOffset,

            totalCount =
                filteredArmors().size,

            section =
                CreatePresetListSection.ARMORS
        )

        renderClippedList(
            area =
                armorListArea()
        ) {
            val definitions =
                filteredArmors()

            armorCardRectangles()
                .forEachIndexed {
                        index,
                        rectangle ->

                    if (
                        !rectangle.intersectsVertically(
                            armorListArea()
                        )
                    ) {
                        return@forEachIndexed
                    }

                    val armor =
                        definitions
                            .getOrNull(
                                index
                            )
                            ?: return@forEachIndexed

                    renderLinearListRow(
                        rectangle =
                            rectangle,

                        title =
                            armor.displayName,

                        subtitle =
                            "Armor",

                        unlocked =
                            ContentUnlockResolver
                                .isUnlocked(
                                    armor
                                ),

                        selected =
                            armor.id ==
                                interactionState
                                    .selectedArmorId,

                        normalFillColor =
                            Color(
                                0.16f,
                                0.19f,
                                0.23f,
                                1f
                            )
                    )
                }
        }

        if (
            !interactionState
                .hasCharacterSelected
        ) {
            font.color =
                Color.GOLD

            font.data.setScale(
                0.62f
            )

            font.draw(
                batch,
                "Choose a Character before equipping.",
                leftPanel.x,
                62f,
                leftPanel.width,
                Align.center,
                false
            )

            font.data.setScale(
                1f
            )
        }
    }


    private fun renderCenterPanel() {
        renderEditStatsPlaceholder()

        renderCharacterPortrait()

        renderSelectedEquipmentSlot(
            rectangle =
                weaponSlot,

            emptyTitle =
                "WEAPON",

            selectedName =
                selectedWeapon()
                    ?.displayName,

            isValidDropTarget =
                interactionState
                    .draggedContent
                    ?.type ==
                    CreatePresetEquipmentType.WEAPON
        )

        renderSelectedEquipmentSlot(
            rectangle =
                weaponTamgaSlot,

            emptyTitle =
                "WEAPON\nTAMGA",

            selectedName =
                selectedWeaponTamga()
                    ?.displayName,

            isValidDropTarget =
                isTamgaSlotValidDropTarget(
                    TamgaPresetSlot
                        .WEAPON_TAMGA_SLOT
                )
        )

        renderSelectedEquipmentSlot(
            rectangle =
                armorSlot,

            emptyTitle =
                "ARMOR",

            selectedName =
                selectedArmor()
                    ?.displayName,

            isValidDropTarget =
                interactionState
                    .draggedContent
                    ?.type ==
                    CreatePresetEquipmentType.ARMOR
        )

        renderSelectedEquipmentSlot(
            rectangle =
                armorTamgaSlot,

            emptyTitle =
                "ARMOR\nTAMGA",

            selectedName =
                selectedArmorTamga()
                    ?.displayName,

            isValidDropTarget =
                isTamgaSlotValidDropTarget(
                    TamgaPresetSlot
                        .ARMOR_TAMGA_SLOT
                )
        )

        renderSelectedEquipmentSlot(
            rectangle =
                itemSlot,

            emptyTitle =
                "ITEM",

            selectedName =
                selectedItem()
                    ?.displayName,

            isValidDropTarget =
                interactionState
                    .draggedContent
                    ?.type ==
                    CreatePresetEquipmentType.ITEM
        )
    }

    private fun renderEditStatsPlaceholder() {
        drawPanel(
            editStatsButtonRectangle,
            Color(
                0.34f,
                0.27f,
                0.05f,
                1f
            )
        )

        drawCenteredText(
            text =
                "EDIT STATS",

            rectangle =
                editStatsButtonRectangle,

            color =
                if (
                    selectedCharacter() !=
                    null
                ) {
                    Color.WHITE
                } else {
                    Color.GRAY
                },

            scale =
                0.8f
        )
    }

    private fun renderCharacterPortrait() {
        drawPanel(
            characterPortrait,
            Color(
                0.25f,
                0.18f,
                0.07f,
                1f
            )
        )

        val selectedCharacter =
            selectedCharacter()

        if (selectedCharacter == null) {
            drawCenteredText(
                text =
                    "FIRSTLY CHOOSE\nYOUR CHARACTER",

                rectangle =
                    characterPortrait,

                color =
                    Color.GRAY,

                scale =
                    1f
            )

            return
        }

        drawCenteredText(
            text =
                "CHARACTER\nPORTRAIT",

            rectangle =
                characterPortrait,

            color =
                Color.LIGHT_GRAY,

            scale =
                0.85f
        )

        font.color =
            Color.CYAN

        font.data.setScale(
            0.82f
        )

        font.draw(
            batch,
            selectedCharacter.displayName,
            characterPortrait.x -
                20f,
            characterPortrait.y -
                18f,
            characterPortrait.width +
                40f,
            Align.center,
            true
        )

        font.data.setScale(
            1f
        )
    }

    private fun renderSelectedEquipmentSlot(
        rectangle:
        Rectangle,

        emptyTitle:
        String,

        selectedName:
        String?,

        isValidDropTarget:
        Boolean
    ) {
        drawPanel(
            rectangle =
                rectangle,

            fillColor =
                when {
                    isValidDropTarget -> {
                        Color(
                            0.08f,
                            0.48f,
                            0.22f,
                            1f
                        )
                    }

                    selectedName != null -> {
                        Color(
                            0.12f,
                            0.30f,
                            0.18f,
                            1f
                        )
                    }

                    else -> {
                        Color(
                            0.32f,
                            0.28f,
                            0.08f,
                            1f
                        )
                    }
                }
        )

        drawCenteredText(
            text =
                selectedName
                    ?: emptyTitle,

            rectangle =
                rectangle,

            color =
                if (
                    selectedName != null ||
                    isValidDropTarget
                ) {
                    Color.WHITE
                } else {
                    Color.GRAY
                },

            scale =
                0.58f
        )
    }

    private fun renderEquipmentSlot(
        rectangle:
        Rectangle,

        title:
        String
    ) {
        drawPanel(
            rectangle,
            Color(
                0.32f,
                0.28f,
                0.08f,
                1f
            )
        )

        drawCenteredText(
            text =
                title,

            rectangle =
                rectangle,

            color =
                Color.GRAY,

            scale =
                0.64f
        )
    }

    private fun renderListPosition(
        scrollOffset:
        Float,

        totalCount:
        Int,

        section:
        CreatePresetListSection,

        x:
        Float,

        y:
        Float
    ) {
        if (totalCount <= 0) {
            return
        }

        font.color =
            Color.GRAY

        font.data.setScale(
            0.55f
        )

        val area =
            when (section) {
                CreatePresetListSection.WEAPONS ->
                    weaponListArea()

                CreatePresetListSection.ARMORS ->
                    armorListArea()

                CreatePresetListSection.ITEMS ->
                    itemListArea()

                CreatePresetListSection.TAMGAS ->
                    tamgaListArea()
            }

        val firstVisible =
            (
                scrollOffset /
                    LIST_ROW_PITCH
                )
                .toInt()
                .coerceIn(
                    0,
                    totalCount -
                        1
                )

        val visibleCapacity =
            (
                area.height /
                    LIST_ROW_PITCH
                )
                .toInt() +
                2

        val lastVisible =
            (
                firstVisible +
                    visibleCapacity
                )
                .coerceAtMost(
                    totalCount
                )

        font.draw(
            batch,
            "${firstVisible + 1}–$lastVisible / $totalCount",
            x,
            y
        )

        font.data.setScale(
            1f
        )
    }

    private fun renderRightPanel() {
        renderListHeader(
            title =
                "ITEMS",

            panel =
                itemPanel(),

            scrollOffset =
                itemScrollOffset,

            totalCount =
                filteredItems().size,

            section =
                CreatePresetListSection.ITEMS
        )

        renderClippedList(
            area =
                itemListArea()
        ) {
            val definitions =
                filteredItems()

            itemCardRectangles()
                .forEachIndexed {
                        index,
                        rectangle ->

                    if (
                        !rectangle.intersectsVertically(
                            itemListArea()
                        )
                    ) {
                        return@forEachIndexed
                    }

                    val item =
                        definitions
                            .getOrNull(
                                index
                            )
                            ?: return@forEachIndexed

                    renderLinearListRow(
                        rectangle =
                            rectangle,

                        title =
                            item.displayName,

                        subtitle =
                            "${item.rarity.displayName} Item",

                        unlocked =
                            ContentUnlockResolver
                                .isUnlocked(
                                    item
                                ),

                        selected =
                            item.id ==
                                interactionState
                                    .selectedItemId,

                        normalFillColor =
                            Color(
                                0.24f,
                                0.08f,
                                0.10f,
                                1f
                            )
                    )
                }
        }

        renderListHeader(
            title =
                "TAMGAS",

            panel =
                tamgaPanel(),

            scrollOffset =
                tamgaScrollOffset,

            totalCount =
                filteredTamgas().size,

            section =
                CreatePresetListSection.TAMGAS
        )

        renderClippedList(
            area =
                tamgaListArea()
        ) {
            val definitions =
                filteredTamgas()

            tamgaCardRectangles()
                .forEachIndexed {
                        index,
                        rectangle ->

                    if (
                        !rectangle.intersectsVertically(
                            tamgaListArea()
                        )
                    ) {
                        return@forEachIndexed
                    }

                    val tamga =
                        definitions
                            .getOrNull(
                                index
                            )
                            ?: return@forEachIndexed

                    val selected =
                        tamga.id ==
                            interactionState
                                .selectedWeaponTamgaId ||
                            tamga.id ==
                            interactionState
                                .selectedArmorTamgaId

                    renderLinearListRow(
                        rectangle =
                            rectangle,

                        title =
                            tamga.displayName,

                        subtitle =
                            tamgaListSubtitle(
                                tamga
                            ),

                        unlocked =
                            ContentUnlockResolver
                                .isUnlocked(
                                    tamga
                                ),

                        selected =
                            selected,

                        normalFillColor =
                            Color(
                                0.13f,
                                0.11f,
                                0.31f,
                                1f
                            )
                    )
                }
        }
    }


    private fun renderPlaceholderGrid(
        section:
        Rectangle,

        startY:
        Float,

        rowCount:
        Int,

        columnCount:
        Int
    ) {
        val horizontalPadding =
            18f

        val gap =
            12f

        val slotWidth =
            (
                section.width -
                    horizontalPadding * 2f -
                    gap *
                    (
                        columnCount -
                            1
                        )
                ) /
                columnCount

        val slotHeight =
            55f

        val rowGap =
            14f

        repeat(
            rowCount
        ) { row ->

            repeat(
                columnCount
            ) { column ->

                val rectangle =
                    Rectangle(
                        section.x +
                            horizontalPadding +
                            column *
                            (
                                slotWidth +
                                    gap
                                ),

                        startY -
                            row *
                            (
                                slotHeight +
                                    rowGap
                                ),

                        slotWidth,

                        slotHeight
                    )

                drawPanel(
                    rectangle =
                        rectangle,

                    fillColor =
                        Color(
                            0.24f,
                            0.09f,
                            0.10f,
                            1f
                        )
                )
            }
        }
    }

    private fun characterPickerCardRectangles():
        List<Rectangle> {

        val picker =
            characterPickerRectangle()

        val horizontalPadding =
            25f

        val topPadding =
            75f

        val cardGap =
            16f

        val cardHeight =
            90f

        val cardWidth =
            picker.width -
                horizontalPadding * 2f

        val firstY =
            picker.y +
                picker.height -
                topPadding -
                cardHeight

        return List(
            4
        ) { index ->

            Rectangle(
                picker.x +
                    horizontalPadding,

                firstY -
                    index *
                    (
                        cardHeight +
                            cardGap
                        ),

                cardWidth,

                cardHeight
            )
        }
    }

    private fun renderLockState() {
        font.data.setScale(
            0.72f
        )

        font.color =
            if (
                interactionState
                    .showLockedContent
            ) {
                Color.LIGHT_GRAY
            } else {
                Color.GOLD
            }

        font.draw(
            batch,
            if (
                interactionState
                    .showLockedContent
            ) {
                "UNLOCKED ICON — LOCKED CONTENT VISIBLE"
            } else {
                "LOCKED ICON — AVAILABLE CONTENT ONLY"
            },
            905f,
            617f
        )

        font.data.setScale(
            1f
        )
    }

    private fun renderModal() {
        drawFilledRectangle(
            Rectangle(
                0f,
                0f,
                VIEWPORT_WIDTH,
                VIEWPORT_HEIGHT
            ),
            Color(
                0f,
                0f,
                0f,
                0.75f
            )
        )

        when {
            interactionState
                .isConfirmationOpen -> {
                when (
                    interactionState
                        .confirmationType
                ) {
                    CreatePresetConfirmationType.SAVE_PRESET -> {
                        renderSaveConfirmation()
                    }

                    else -> {
                        renderBackConfirmation()
                    }
                }
            }

            interactionState
                .isStatEditorOpen -> {
                /*
                 * Edit Stats her zaman arkada kalır.
                 */
                renderStatEditor()

                /*
                 * Seçili Stat varsa küçük açıklama kutusu
                 * Edit Stats'ın üzerine çizilir.
                 */
                if (
                    interactionState
                        .inspectedStatType !=
                    null
                ) {
                    renderStatDescription()
                }
            }

            interactionState
                .isCharacterPickerOpen -> {
                renderCharacterPicker()
            }

            interactionState
                .isTamgaDetailOpen -> {
                renderTamgaDetail()
            }

            interactionState
                .isEquipmentDetailOpen -> {
                renderEquipmentDetail()
            }

            interactionState
                .isCharacterDetailOpen -> {
                renderCharacterDetail()
            }
        }
    }

    private fun renderCharacterPicker() {
        val picker =
            characterPickerRectangle()

        drawPanel(
            rectangle =
                picker,

            fillColor =
                Color(
                    0.06f,
                    0.11f,
                    0.18f,
                    1f
                )
        )

        font.data.setScale(
            1.25f
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            "CHOOSE YOUR CHARACTER",
            picker.x,
            picker.y +
                picker.height -
                27f,
            picker.width,
            Align.center,
            false
        )

        font.data.setScale(
            0.82f
        )

        val visibleCharacters =
            visibleCharacters()

        characterPickerCardRectangles()
            .forEachIndexed {
                    index,
                    rectangle ->

                val character =
                    visibleCharacters
                        .getOrNull(
                            index
                        )
                        ?: return@forEachIndexed

                val unlocked =
                    character.initialUnlockState ==
                        ContentUnlockState.UNLOCKED

                val selected =
                    character.id ==
                        interactionState
                            .selectedCharacterId

                drawPanel(
                    rectangle =
                        rectangle,

                    fillColor =
                        when {
                            !unlocked -> {
                                Color(
                                    0.16f,
                                    0.16f,
                                    0.16f,
                                    1f
                                )
                            }

                            selected -> {
                                Color(
                                    0.08f,
                                    0.38f,
                                    0.20f,
                                    1f
                                )
                            }

                            else -> {
                                Color(
                                    0.08f,
                                    0.18f,
                                    0.28f,
                                    1f
                                )
                            }
                        }
                )

                font.color =
                    if (unlocked) {
                        Color.WHITE
                    } else {
                        Color.GRAY
                    }

                font.draw(
                    batch,
                    if (unlocked) {
                        character.displayName
                    } else {
                        "${character.displayName} — LOCKED"
                    },
                    rectangle.x +
                        18f,
                    rectangle.y +
                        rectangle.height / 2f +
                        7f,
                    rectangle.width -
                        36f,
                    Align.center,
                    true
                )
            }

        font.color =
            Color.GRAY

        font.data.setScale(
            0.70f
        )

        font.draw(
            batch,
            "Choose a Character to inspect its details.",
            picker.x,
            picker.y +
                18f,
            picker.width,
            Align.center,
            false
        )

        font.data.setScale(
            1f
        )
    }

    private fun renderTamgaDetail() {
        val tamga =
            inspectedTamga()
                ?: return

        drawPanel(
            rectangle =
                modalRectangle(),

            fillColor =
                Color(
                    0.07f,
                    0.08f,
                    0.18f,
                    1f
                )
        )

        font.data.setScale(
            1.25f
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            tamga.displayName,
            modalRectangle().x +
                24f,
            modalRectangle().y +
                modalRectangle().height -
                28f
        )

        font.data.setScale(
            0.82f
        )

        font.color =
            Color.GOLD

        font.draw(
            batch,
            tamgaDetailSubtitle(
                tamga
            ),
            modalRectangle().x +
                24f,
            modalRectangle().y +
                modalRectangle().height -
                65f
        )

        font.color =
            Color.LIGHT_GRAY

        font.draw(
            batch,
            tamga.description,
            modalRectangle().x +
                24f,
            modalRectangle().y +
                modalRectangle().height -
                105f,
            modalRectangle().width -
                48f,
            Align.left,
            true
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            tamgaDetailStatText(
                tamga
            ),
            modalRectangle().x +
                24f,
            modalRectangle().y +
                155f,
            modalRectangle().width -
                48f,
            Align.left,
            true
        )

        renderModalCloseHint()
    }

    private fun tamgaListSubtitle(
        tamga:
        TamgaDefinition
    ): String {

        val slotText =
            when (tamga.slotType) {
                TamgaSlotType.WEAPON ->
                    "Weapon"

                TamgaSlotType.ARMOR ->
                    "Armor"

                TamgaSlotType.GENERAL ->
                    "General"
            }

        val activationText =
            when (tamga.activationType) {
                TamgaActivationType.ACTIVE ->
                    "Active"

                TamgaActivationType.PASSIVE ->
                    "Passive"
            }

        return "$slotText Tamga • $activationText"
    }

    private fun tamgaDetailSubtitle(
        tamga:
        TamgaDefinition
    ): String {

        return tamgaListSubtitle(
            tamga
        )
    }

    private fun tamgaDetailStatText(
        tamga:
        TamgaDefinition
    ): String {

        return buildString {
            appendLine(
                "Compatible Slot: " +
                    when (tamga.slotType) {
                        TamgaSlotType.WEAPON ->
                            "Weapon Tamga"

                        TamgaSlotType.ARMOR ->
                            "Armor Tamga"

                        TamgaSlotType.GENERAL ->
                            "Weapon or Armor Tamga"
                    }
            )

            if (
                tamga.requiredWeaponTags
                    .isNotEmpty()
            ) {
                appendLine(
                    "Required Weapon: " +
                        tamga.requiredWeaponTags
                            .joinToString {
                                contentTagDisplayName(
                                    it
                                )
                            }
                )
            }

            if (
                tamga.requiredArmorTags
                    .isNotEmpty()
            ) {
                appendLine(
                    "Required Armor: " +
                        tamga.requiredArmorTags
                            .joinToString {
                                contentTagDisplayName(
                                    it
                                )
                            }
                )
            }

            if (
                tamga.interactableStats
                    .isNotEmpty()
            ) {
                appendLine(
                    "Interactable Stats: " +
                        tamga.interactableStats
                            .joinToString {
                                it.displayName
                            }
                )
            }

            if (
                tamga.statModifiers
                    .isNotEmpty()
            ) {
                appendLine()
                appendLine(
                    "Stat Modifiers:"
                )

                tamga.statModifiers
                    .forEach { modifier ->
                        appendLine(
                            modifier.statType.displayName +
                                ": " +
                                signedValue(
                                    modifier.amount
                                )
                        )
                    }
            }
        }
            .trim()
            .ifBlank {
                "No direct Stat modifiers."
            }
    }

    private fun contentTagDisplayName(
        tag:
        ContentTag
    ): String {

        return tag.name
            .lowercase(
                Locale.US
            )
            .split(
                "_"
            )
            .joinToString(
                " "
            ) { word ->
                word.replaceFirstChar {
                    it.uppercase(
                        Locale.US
                    )
                }
            }
    }

    private fun renderModalCloseHint() {
        font.color =
            Color.GRAY

        font.data.setScale(
            0.70f
        )

        font.draw(
            batch,
            "Click outside to close.",
            modalRectangle().x +
                24f,
            modalRectangle().y +
                22f
        )

        font.data.setScale(
            1f
        )
    }

    private fun renderEquipmentDetail() {
        val inspected =
            interactionState
                .inspectedEquipment
                ?: return

        val title: String
        val description: String
        val statText: String

        when (inspected.type) {
            CreatePresetEquipmentType.WEAPON -> {
                val definition =
                    weapons.firstOrNull { weapon ->
                        weapon.id ==
                            inspected.contentId
                    }
                        ?: return

                val previewPlayerStats =
                    createPresetPreviewStats()

                val previewWeapon =
                    WeaponInstance(
                        definition =
                            definition,

                        level =
                            1
                    )

                title =
                    definition.displayName

                description =
                    definition.description

                statText =
                    if (previewPlayerStats == null) {
                        buildWeaponBaseDetailText(
                            weapon =
                                previewWeapon
                        )
                    } else {
                        buildWeaponCharacterDetailText(
                            weapon =
                                previewWeapon,

                            playerStats =
                                previewPlayerStats
                        )
                    }
            }

            CreatePresetEquipmentType.ARMOR -> {
                val armor =
                    armors.firstOrNull { definition ->
                        definition.id ==
                            inspected.contentId
                    }
                        ?: return

                title =
                    armor.displayName

                description =
                    armor.description

                statText =
                    armor.allStatModifiers()
                        .joinToString(
                            separator = "\n"
                        ) { modifier ->
                            "${modifier.statType.displayName}: " +
                                signedValue(
                                    modifier.amount
                                )
                        }
            }

            CreatePresetEquipmentType.ITEM -> {
                val item =
                    items.firstOrNull { definition ->
                        definition.id ==
                            inspected.contentId
                    }
                        ?: return

                title =
                    item.displayName

                description =
                    item.description

                statText =
                    buildString {
                        appendLine(
                            item.rarity.displayName +
                                " Item"
                        )

                        appendLine()

                        if (
                            item.statModifiers
                                .isEmpty()
                        ) {
                            appendLine(
                                "No Stat bonuses."
                            )
                        } else {
                            item.statModifiers
                                .forEach { modifier ->
                                    appendLine(
                                        modifier.statType.displayName +
                                            ": " +
                                            signedValue(
                                                modifier.amount
                                            )
                                    )
                                }
                        }

                        val maximumCopies =
                            item.maximumInventoryCopies
                                ?: item.rarity
                                    .maximumInventoryCopies

                        appendLine()
                        append(
                            "Maximum Copies: " +
                                (
                                    maximumCopies
                                        ?.toString()
                                        ?: "Unlimited"
                                    )
                        )
                    }
            }

            CreatePresetEquipmentType.TAMGA -> {
                title = "Tamga"

                description =
                    "Tamga system is not implemented yet."

                statText = ""
            }

        }

        drawPanel(
            rectangle =
                modalRectangle(),

            fillColor =
                Color(
                    0.07f,
                    0.10f,
                    0.16f,
                    1f
                )
        )

        font.data.setScale(
            1.25f
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            title,
            270f,
            555f
        )
        font.data.setScale(
            0.82f
        )

        if (
            inspected.type ==
            CreatePresetEquipmentType.WEAPON
        ) {
            /*
             * Weapon detayları solda.
             */
            font.color =
                Color.LIGHT_GRAY

            font.draw(
                batch,
                statText,
                modalRectangle().x +
                    24f,
                modalRectangle().y +
                    modalRectangle().height -
                    78f,
                330f,
                Align.left,
                true
            )

            /*
             * Weapon açıklaması sağda.
             */
            font.draw(
                batch,
                description,
                modalRectangle().x +
                    395f,
                modalRectangle().y +
                    modalRectangle().height -
                    78f,
                modalRectangle().width -
                    425f,
                Align.left,
                true
            )
        } else {
            /*
             * Armor için eski dikey düzen korunur.
             */
            font.color =
                Color.LIGHT_GRAY

            font.draw(
                batch,
                description,
                modalRectangle().x +
                    24f,
                modalRectangle().y +
                    modalRectangle().height -
                    78f,
                modalRectangle().width -
                    48f,
                Align.left,
                true
            )

            font.color =
                Color.WHITE

            font.draw(
                batch,
                statText,
                modalRectangle().x +
                    24f,
                modalRectangle().y +
                    220f,
                modalRectangle().width -
                    48f,
                Align.left,
                true
            )
        }
    }

    private fun buildWeaponBaseDetailText(
        weapon:
        WeaponInstance
    ): String {

        val definition =
            weapon.definition

        val levelStats =
            weapon.currentLevelStats

        return buildString {
            appendLine(
                "Level I"
            )

            appendLine(
                "Weapon Type: " +
                    weaponTypeText(
                        definition
                    )
            )

            appendDamageScalingText(
                definition
            )

            appendLine(
                "Attack Model: " +
                    attackModelText(
                        weapon
                    )
            )

            appendLine(
                "Base Damage: " +
                    (
                        definition.baseDamage *
                            levelStats.damageMultiplier
                        )
                        .roundToInt()
            )

            appendLine(
                "Base Attacks per Second: " +
                    formatDecimal(
                        definition.baseAttacksPerSecond *
                            levelStats.attackSpeedMultiplier
                    )
            )

            appendLine(
                "Range: " +
                    (
                        definition.baseRange *
                            levelStats.rangeMultiplier
                        )
                        .roundToInt()
            )

            val knockback =
                definition.baseKnockback *
                    levelStats.knockbackMultiplier

            if (knockback > 0f) {
                appendLine(
                    "Knockback: " +
                        knockback.roundToInt()
                )
            }

            append(
                "\nChoose a Character to see final values."
            )
        }
    }

    private fun buildWeaponCharacterDetailText(
        weapon:
        WeaponInstance,

        playerStats:
        PlayerStats
    ): String {

        val definition =
            weapon.definition

        val levelStats =
            weapon.currentLevelStats

        val damageCalculator =
            WeaponDamageCalculator(
                playerStats =
                    playerStats
            )

        val damage =
            damageCalculator
                .calculateNonCriticalDamage(
                    weapon
                )

        val statAttackSpeedMultiplier =
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
                statAttackSpeedMultiplier

        val range =
            definition.baseRange *
                levelStats.rangeMultiplier

        val knockback =
            definition.baseKnockback *
                levelStats.knockbackMultiplier

        return buildString {
            appendLine(
                "Level I"
            )

            appendLine(
                "Weapon Type: " +
                    weaponTypeText(
                        definition
                    )
            )

            appendDamageScalingText(
                definition
            )

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
    }

    private fun StringBuilder
        .appendDamageScalingText(
        definition:
        WeaponDefinition
    ) {

        val orderedScaling =
            definition
                .orderedDamageScaling()

        val mainScaling =
            orderedScaling
                .firstOrNull()

        val secondaryScaling =
            orderedScaling
                .drop(1)

        if (mainScaling != null) {
            appendLine(
                "Main Damage Scaling: " +
                    mainScaling
                        .first
                        .displayName
            )
        }

        if (secondaryScaling.isNotEmpty()) {
            appendLine(
                "Secondary Scaling: " +
                    secondaryScaling
                        .joinToString(
                            separator = ", "
                        ) { scaling ->
                            scaling
                                .first
                                .displayName
                        }
            )
        }
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

            else -> {
                "Weapon"
            }
        }
    }

    private fun attackModelText(
        weapon:
        WeaponInstance
    ): String {

        if (
            weapon.displayName
                .lowercase(
                    Locale.US
                ) ==
            "double swords"
        ) {
            return "X"
        }

        return when (
            weapon.definition
                .attackPattern
        ) {
            WeaponAttackPattern.SHORT_SLASH_LINES ->
                "Slash"

            WeaponAttackPattern.FORWARD_CONE ->
                "Cone"

            WeaponAttackPattern.TARGET_AREA_CIRCLE ->
                "Circle"

            WeaponAttackPattern.MELEE_OR_THROW ->
                "Stab / Throw"

            WeaponAttackPattern.STRAIGHT_PROJECTILE ->
                "Projectile"

            WeaponAttackPattern.PIERCING_PROJECTILE ->
                "Piercing Projectile"

            WeaponAttackPattern.LONG_PIERCING_LINE ->
                "Piercing Line"

            WeaponAttackPattern.SHIELD_CONE ->
                "Shield Cone"

            WeaponAttackPattern.TOME_PROJECTILE_OR_BEAM ->
                "Projectile / Beam"

            WeaponAttackPattern.TOTEM_PROJECTILE_OR_DRAIN ->
                "Projectile / Drain"

            WeaponAttackPattern.NONE ->
                "Passive"
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

    private fun signedValue(
        value:
        Int
    ): String {

        return if (value >= 0) {
            "+$value"
        } else {
            value.toString()
        }
    }

    private fun renderCharacterDetail() {
        val character =
            inspectedCharacter()
                ?: return

        drawPanel(
            modalRectangle(),
            Color(
                0.07f,
                0.10f,
                0.16f,
                1f
            )
        )

        font.data.setScale(
            1.25f
        )

        font.color =
            Color.CYAN

        font.draw(
            batch,
            character.displayName,
            270f,
            565f
        )

        font.data.setScale(
            0.82f
        )

        font.color =
            Color.LIGHT_GRAY

        font.draw(
            batch,
            character.description,
            270f,
            520f,
            360f,
            Align.left,
            true
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            "Minions:\n" +
                character.minionDescription,
            270f,
            355f,
            360f,
            Align.left,
            true
        )

        renderCharacterStats(
            character
        )

        drawPanel(
            chooseCharacterButton(),
            Color(
                0.08f,
                0.34f,
                0.18f,
                1f
            )
        )

        drawCenteredText(
            text =
                "CHOOSE " +
                    character.displayName,

            rectangle =
                chooseCharacterButton(),

            color =
                Color.WHITE,

            scale =
                0.74f
        )

        font.color =
            Color.GRAY

        font.draw(
            batch,
            "Click outside to close.",
            275f,
            165f
        )

        font.data.setScale(
            1f
        )
    }

    private fun renderCharacterStats(
        character:
        CharacterDefinition
    ) {
        font.data.setScale(
            0.72f
        )

        val stats =
            StatType.entries

        val rowsPerColumn =
            (
                stats.size +
                    1
                ) /
                2

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
                665f +
                    column *
                    185f

            val y =
                520f -
                    row *
                    28f

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
                character.baseStat(
                    statType
                )
                    .toString(),
                x +
                    125f,
                y,
                42f,
                Align.right,
                false
            )
        }
    }

    private fun renderStatEditor() {
        val character =
            selectedCharacter()
                ?: return

        val panel =
            statEditorRectangle()

        drawPanel(
            panel,
            Color(
                0.07f,
                0.10f,
                0.16f,
                1f
            )
        )

        font.data.setScale(
            1.2f
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            "EDIT STATS",
            panel.x,
            panel.y +
                panel.height -
                26f,
            panel.width,
            Align.center,
            false
        )

        font.data.setScale(
            0.82f
        )

        font.color =
            Color.GOLD

        font.draw(
            batch,
            "Remaining Stat Points: " +
                interactionState
                    .remainingStatPoints,
            panel.x,
            panel.y +
                panel.height -
                62f,
            panel.width,
            Align.center,
            false
        )

        statEditorRows()
            .forEach {
                    statType,
                    row ->

                drawPanel(
                    row,
                    Color(
                        0.09f,
                        0.14f,
                        0.21f,
                        1f
                    )
                )

                val base =
                    character.baseStat(
                        statType
                    )

                val bonus =
                    interactionState
                        .statAllocation
                        .bonusFor(
                            statType
                        )

                val finalScore =
                    base +
                        bonus

                font.color =
                    Color.LIGHT_GRAY

                font.draw(
                    batch,
                    statType.displayName,
                    row.x +
                        12f,
                    row.y +
                        22f
                )

                font.color =
                    Color.WHITE

                font.draw(
                    batch,
                    "$base + $bonus = $finalScore",
                    row.x +
                        145f,
                    row.y +
                        22f,
                    row.width -
                        230f,
                    Align.right,
                    false
                )

                drawPanel(
                    statMinusRectangle(
                        row
                    ),
                    Color(
                        0.29f,
                        0.10f,
                        0.10f,
                        1f
                    )
                )

                drawCenteredText(
                    text =
                        "-",

                    rectangle =
                        statMinusRectangle(
                            row
                        ),

                    color =
                        if (bonus > 0) {
                            Color.WHITE
                        } else {
                            Color.GRAY
                        },

                    scale =
                        0.9f
                )

                drawPanel(
                    statPlusRectangle(
                        row
                    ),
                    Color(
                        0.08f,
                        0.30f,
                        0.16f,
                        1f
                    )
                )

                drawCenteredText(
                    text =
                        "+",

                    rectangle =
                        statPlusRectangle(
                            row
                        ),

                    color =
                        if (
                            interactionState
                                .remainingStatPoints >
                            0 &&
                            finalScore <
                            25
                        ) {
                            Color.WHITE
                        } else {
                            Color.GRAY
                        },

                    scale =
                        0.9f
                )
            }

        font.color =
            Color.GRAY

        font.data.setScale(
            0.68f
        )

        font.draw(
            batch,
            "Click a Stat row to read its description. Click outside to close.",
            panel.x,
            panel.y +
                14f,
            panel.width,
            Align.center,
            false
        )

        font.data.setScale(
            1f
        )
    }

    private fun renderStatDescription() {
        val statType =
            interactionState
                .inspectedStatType
                ?: return

        val panel =
            statDescriptionRectangle()

        /*
         * Küçük bir gölge.
         */
        drawFilledRectangle(
            Rectangle(
                panel.x +
                    5f,
                panel.y -
                    5f,
                panel.width,
                panel.height
            ),
            Color(
                0f,
                0f,
                0f,
                0.45f
            )
        )

        drawPanel(
            panel,
            Color(
                0.07f,
                0.10f,
                0.16f,
                1f
            )
        )

        font.data.setScale(
            0.95f
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            statType.displayName,
            panel.x +
                16f,
            panel.y +
                panel.height -
                22f
        )

        font.data.setScale(
            0.70f
        )

        font.color =
            Color.LIGHT_GRAY

        font.draw(
            batch,
            statDescription(
                statType
            ),
            panel.x +
                16f,
            panel.y +
                panel.height -
                55f,
            panel.width -
                32f,
            Align.left,
            true
        )

        font.color =
            Color.GRAY

        font.data.setScale(
            0.58f
        )

        font.draw(
            batch,
            "Click outside this box to close.",
            panel.x +
                16f,
            panel.y +
                13f
        )

        font.data.setScale(
            1f
        )
    }

    private fun renderSaveConfirmation() {
        drawPanel(
            modalRectangle(),
            Color(
                0.07f,
                0.16f,
                0.10f,
                1f
            )
        )

        font.data.setScale(
            1.25f
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            if (editingPresetId == null) {
                "SAVE THIS PRESET?"
            } else {
                "SAVE PRESET CHANGES?"
            },
            0f,
            530f,
            VIEWPORT_WIDTH,
            Align.center,
            false
        )

        font.data.setScale(
            0.9f
        )

        font.color =
            Color.LIGHT_GRAY

        font.draw(
            batch,
            "Character, Weapon, Armor, Item and Stat distribution will be saved.",
            0f,
            445f,
            VIEWPORT_WIDTH,
            Align.center,
            false
        )

        drawPanel(
            modalConfirmButton(),
            Color(
                0.08f,
                0.34f,
                0.18f,
                1f
            )
        )

        drawCenteredText(
            text =
                "SAVE",

            rectangle =
                modalConfirmButton(),

            color =
                Color.WHITE,

            scale =
                0.82f
        )

        drawPanel(
            modalCancelButton(),
            Color(
                0.08f,
                0.20f,
                0.31f,
                1f
            )
        )

        drawCenteredText(
            text =
                "CANCEL",

            rectangle =
                modalCancelButton(),

            color =
                Color.WHITE,

            scale =
                0.82f
        )

        font.data.setScale(
            1f
        )
    }

    private fun statDescription(
        statType:
        StatType
    ): String {

        return when (statType) {
            StatType.MAX_HP ->
                "Increases maximum health. Current HP is calculated from this score together with Character and Armor values."

            StatType.ARMOR_CLASS ->
                "Improves physical protection and contributes to damage reduction."

            StatType.STRENGTH ->
                "Improves damage scaling for Strength-based Weapons."

            StatType.DEXTERITY ->
                "Improves damage scaling for Dexterity-based Weapons."

            StatType.WISDOM ->
                "Improves damage scaling for spell-focused Weapons and magical effects."

            StatType.HASTE ->
                "Improves attack speed for Weapons whose normal attack speed is controlled by Haste."

            StatType.ATTACK_SPEED ->
                "Improves attacks per second for Weapons controlled by Attack Speed."

            StatType.CRIT_RATE ->
                "Increases the chance for attacks to become critical hits."

            StatType.CRIT_DAMAGE ->
                "Increases the damage multiplier applied by critical hits."

            StatType.MOVEMENT_SPEED ->
                "Increases Character movement speed."

            StatType.LUCK ->
                "Slightly improves chest and market reward probabilities."

            StatType.HARVEST ->
                "Improves Gold Coin and resource gain."

            StatType.ELEMENTAL_DAMAGE ->
                "Improves damage caused by elemental effects."

            StatType.MIGHT_OF_NATURE ->
                "Improves nature-based effects and related Weapons."

            StatType.SHEPHERD ->
                "Improves minion-related effects."

            StatType.PIERCING ->
                "Increases how many enemies a piercing attack can pass through."

            StatType.ENEMY_COUNT ->
                "Modifies the number of enemies generated during a Stage."

            StatType.HP_REGENERATION ->
                "Restores health over time."

            StatType.LIFE_STEAL ->
                "Restores health from a portion of damage dealt."
        }
    }

    private fun renderBackConfirmation() {
        drawPanel(
            modalRectangle(),
            Color(
                0.17f,
                0.07f,
                0.08f,
                1f
            )
        )

        font.data.setScale(
            1.25f
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            "DISCARD PRESET CHANGES?",
            0f,
            530f,
            VIEWPORT_WIDTH,
            Align.center,
            false
        )

        font.data.setScale(
            0.9f
        )

        font.color =
            Color.LIGHT_GRAY

        font.draw(
            batch,
            "Your current Create Preset progress will be lost.",
            0f,
            445f,
            VIEWPORT_WIDTH,
            Align.center,
            false
        )

        drawPanel(
            modalConfirmButton(),
            Color(
                0.48f,
                0.13f,
                0.13f,
                1f
            )
        )

        drawCenteredText(
            text =
                "DISCARD",

            rectangle =
                modalConfirmButton(),

            color =
                Color.WHITE,

            scale =
                0.82f
        )

        drawPanel(
            modalCancelButton(),
            Color(
                0.08f,
                0.20f,
                0.31f,
                1f
            )
        )

        drawCenteredText(
            text =
                "CANCEL",

            rectangle =
                modalCancelButton(),

            color =
                Color.WHITE,

            scale =
                0.82f
        )

        font.data.setScale(
            1f
        )
    }

    private fun statEditorRectangle() =
        Rectangle(
            285f,
            72f,
            710f,
            575f
        )

    private fun statDescriptionRectangle() =
        Rectangle(
            665f,
            345f,
            300f,
            190f
        )

    private fun statEditorRows():
        Map<StatType, Rectangle> {

        val panel =
            statEditorRectangle()

        val stats =
            StatType.entries

        val rowsPerColumn =
            (
                stats.size +
                    1
                ) /
                2

        val columnGap =
            18f

        val horizontalPadding =
            20f

        val columnWidth =
            (
                panel.width -
                    horizontalPadding * 2f -
                    columnGap
                ) /
                2f

        val rowHeight =
            38f

        val rowGap =
            7f

        val firstY =
            panel.y +
                panel.height -
                112f -
                rowHeight

        return stats
            .associateWith {
                    statType ->

                val index =
                    stats.indexOf(
                        statType
                    )

                val column =
                    index /
                        rowsPerColumn

                val row =
                    index %
                        rowsPerColumn

                Rectangle(
                    panel.x +
                        horizontalPadding +
                        column *
                        (
                            columnWidth +
                                columnGap
                            ),

                    firstY -
                        row *
                        (
                            rowHeight +
                                rowGap
                            ),

                    columnWidth,

                    rowHeight
                )
            }
    }

    private fun statMinusRectangle(
        row:
        Rectangle
    ) =
        Rectangle(
            row.x +
                row.width -
                76f,
            row.y +
                4f,
            30f,
            row.height -
                8f
        )

    private fun statPlusRectangle(
        row:
        Rectangle
    ) =
        Rectangle(
            row.x +
                row.width -
                38f,
            row.y +
                4f,
            30f,
            row.height -
                8f
        )

    private fun characterPickerRectangle() =
        Rectangle(
            350f,
            90f,
            580f,
            545f
        )

    private fun modalRectangle() =
        Rectangle(
            235f,
            135f,
            810f,
            460f
        )

    private fun chooseCharacterButton() =
        Rectangle(
            475f,
            195f,
            330f,
            58f
        )

    private fun modalConfirmButton() =
        Rectangle(
            390f,
            235f,
            220f,
            58f
        )

    private fun modalCancelButton() =
        Rectangle(
            670f,
            235f,
            220f,
            58f
        )

    private fun drawSectionTitle(
        text:
        String,

        x:
        Float,

        y:
        Float,

        width:
        Float
    ) {
        font.data.setScale(
            0.95f
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            text,
            x,
            y,
            width,
            Align.center,
            false
        )

        font.data.setScale(
            1f
        )
    }

    private fun drawPanel(
        rectangle:
        Rectangle,

        fillColor:
        Color
    ) {
        drawFilledRectangle(
            rectangle,
            Color(
                0.34f,
                0.52f,
                0.73f,
                1f
            )
        )

        drawFilledRectangle(
            Rectangle(
                rectangle.x +
                    2f,
                rectangle.y +
                    2f,
                rectangle.width -
                    4f,
                rectangle.height -
                    4f
            ),
            fillColor
        )
    }

    private fun drawCenteredText(
        text:
        String,

        rectangle:
        Rectangle,

        color:
        Color,

        scale:
        Float
    ) {
        val oldScaleX =
            font.data.scaleX

        val oldScaleY =
            font.data.scaleY

        font.data.setScale(
            scale
        )

        font.color =
            color

        font.draw(
            batch,
            text,
            rectangle.x +
                5f,
            rectangle.y +
                rectangle.height / 2f +
                7f,
            rectangle.width -
                10f,
            Align.center,
            true
        )

        font.data.setScale(
            oldScaleX,
            oldScaleY
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

    private fun createWhitePixel():
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

    override fun resize(
        width:
        Int,

        height:
        Int
    ) {
        if (
            width <= 0 ||
            height <= 0
        ) {
            return
        }

        viewport.update(
            width,
            height,
            true
        )
    }

    override fun dispose() {

        if (
            Gdx.input.inputProcessor !=
            previousInputProcessor
        ) {
            Gdx.input.inputProcessor =
                previousInputProcessor
        }

        previousInputProcessor =
            null

        whitePixel.dispose()
        batch.dispose()
        font.dispose()
    }

    private enum class CreatePresetListSection {

        WEAPONS,

        ARMORS,

        ITEMS,

        TAMGAS
    }

    private enum class CreatePresetListGestureMode {

        NONE,

        PENDING,

        SCROLLING,

        EQUIPMENT_DRAGGING
    }

    private data class CreatePresetListGesture(
        val section:
        CreatePresetListSection,

        val visibleContentIndex:
        Int?,

        val equipmentType:
        CreatePresetEquipmentType?
    )
}
