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

        private const val WEAPON_VISIBLE_CARD_COUNT =
            12

        private const val ARMOR_VISIBLE_CARD_COUNT =
            8

        private const val LIST_DRAG_THRESHOLD =
            8f

        private const val LIST_DRAG_SCROLL_STEP =
            45f

        private const val ITEM_VISIBLE_CARD_COUNT =
            15
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
            35f,
            PANEL_BOTTOM,
            380f,
            PANEL_TOP -
                PANEL_BOTTOM
        )

    private val centerPanel =
        Rectangle(
            435f,
            PANEL_BOTTOM,
            410f,
            PANEL_TOP -
                PANEL_BOTTOM
        )

    private val rightPanel =
        Rectangle(
            865f,
            PANEL_BOTTOM,
            380f,
            PANEL_TOP -
                PANEL_BOTTOM
        )

    private val characterPortrait =
        Rectangle(
            centerPanel.x +
                105f,
            centerPanel.y +
                155f,
            200f,
            245f
        )

    private val weaponSlot =
        Rectangle(
            centerPanel.x +
                20f,
            centerPanel.y +
                300f,
            72f,
            72f
        )

    private val weaponTamgaSlot =
        Rectangle(
            centerPanel.x +
                20f,
            centerPanel.y +
                175f,
            72f,
            72f
        )

    private val armorSlot =
        Rectangle(
            centerPanel.x +
                centerPanel.width -
                92f,
            centerPanel.y +
                300f,
            72f,
            72f
        )

    private val armorTamgaSlot =
        Rectangle(
            centerPanel.x +
                centerPanel.width -
                92f,
            centerPanel.y +
                175f,
            72f,
            72f
        )

    private val itemSlot =
        Rectangle(
            centerPanel.x +
                169f,
            centerPanel.y +
                30f,
            72f,
            72f
        )

    private val editStatsButtonRectangle =
        Rectangle(
            centerPanel.x +
                105f,
            centerPanel.y +
                centerPanel.height -
                72f,
            200f,
            46f
        )

    private var pointerPressedWeaponIndex:
        Int? = null

    private var pointerPressedArmorIndex:
        Int? = null

    private var pointerPressedItemIndex:
        Int? = null

    private var equipmentPressStartX =
        0f

    private var equipmentPressStartY =
        0f

    private var equipmentDragStarted =
        false

    private var weaponScrollOffset =
        0

    private var armorScrollOffset =
        0

    private var itemScrollOffset =
        0

    private var previousInputProcessor:
        InputProcessor? = null

    private var draggedListSection:
        CreatePresetListSection? = null

    private var listPressStartY =
        0f

    private var previousListDragY =
        0f

    private var accumulatedListDragY =
        0f

    private var listDragStarted =
        false


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
            0

        armorScrollOffset =
            0

        itemScrollOffset =
            0

        draggedListSection =
            null

        listDragStarted =
            false

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

                    if (
                        interactionState
                            .isAnyModalOpen ||
                        interactionState
                            .draggedContent != null
                    ) {
                        return true
                    }

                    val amount =
                        if (amountY > 0f) {
                            1
                        } else {
                            -1
                        }

                    return when {
                        weaponListArea()
                            .contains(
                                pointer.position
                            ) -> {
                            scrollWeapons(
                                amount
                            )

                            true
                        }

                        armorListArea()
                            .contains(
                                pointer.position
                            ) -> {
                            scrollArmors(
                                amount
                            )

                            true
                        }

                        itemListArea()
                            .contains(
                                pointer.position
                            ) -> {
                            scrollItems(
                                amount
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
                .isCompleteWithoutTamga

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
        if (
            handleScrollableEquipmentLists()
        ) {
            return
        }

        if (
            handleEquipmentDragInput()
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

    private fun handleScrollableEquipmentLists():
        Boolean {

        /*
         * Bir Weapon veya Armor kartına basılmışsa
         * mevcut equipment drag sistemi yönetsin.
         */
        if (
            pointer.justPressed &&
            (
                weaponCardRectangles()
                    .any { rectangle ->
                        rectangle.contains(
                            pointer.position
                        )
                    } ||
                    armorCardRectangles()
                        .any { rectangle ->
                            rectangle.contains(
                                pointer.position
                            )
                        } ||
                    itemCardRectangles()
                        .any { rectangle ->
                            rectangle.contains(
                                pointer.position
                            )
                        }
                )
        ) {
            return false
        }

        if (pointer.justPressed) {
            draggedListSection =
                when {
                    weaponListArea()
                        .contains(
                            pointer.position
                        ) -> {
                        CreatePresetListSection
                            .WEAPONS
                    }

                    armorListArea()
                        .contains(
                            pointer.position
                        ) -> {
                        CreatePresetListSection
                            .ARMORS
                    }

                    itemListArea()
                        .contains(
                            pointer.position
                        ) -> {
                        CreatePresetListSection.ITEMS
                    }

                    else -> {
                        null
                    }
                }

            if (draggedListSection != null) {
                listPressStartY =
                    pointer.position.y

                previousListDragY =
                    pointer.position.y

                accumulatedListDragY =
                    0f

                listDragStarted =
                    false

                return true
            }
        }

        val section =
            draggedListSection
                ?: return false

        if (pointer.isHeld) {
            val currentY =
                pointer.position.y

            if (
                abs(
                    currentY -
                        listPressStartY
                ) >=
                LIST_DRAG_THRESHOLD
            ) {
                listDragStarted =
                    true
            }

            if (listDragStarted) {
                accumulatedListDragY +=
                    currentY -
                        previousListDragY

                /*
                 * Listeyi yukarı sürüklemek daha aşağıdaki
                 * içerikleri gösterir.
                 */
                while (
                    accumulatedListDragY <=
                    -LIST_DRAG_SCROLL_STEP
                ) {
                    when (section) {
                        CreatePresetListSection.WEAPONS -> {
                            scrollWeapons(
                                1
                            )
                        }

                        CreatePresetListSection.ARMORS -> {
                            scrollArmors(
                                1
                            )
                        }

                        CreatePresetListSection.ITEMS -> {
                            scrollItems(
                                1
                            )
                        }
                    }

                    accumulatedListDragY +=
                        LIST_DRAG_SCROLL_STEP
                }

                /*
                 * Listeyi aşağı sürüklemek önceki
                 * içeriklere döner.
                 */
                while (
                    accumulatedListDragY >=
                    LIST_DRAG_SCROLL_STEP
                ) {
                    when (section) {
                        CreatePresetListSection.WEAPONS -> {
                            scrollWeapons(
                                -1
                            )
                        }

                        CreatePresetListSection.ARMORS -> {
                            scrollArmors(
                                -1
                            )
                        }

                        CreatePresetListSection.ITEMS -> {
                            scrollItems(
                                -1
                            )
                        }
                    }

                    accumulatedListDragY -=
                        LIST_DRAG_SCROLL_STEP
                }
            }

            previousListDragY =
                currentY

            return true
        }

        if (pointer.justReleased) {
            draggedListSection =
                null

            listDragStarted =
                false

            accumulatedListDragY =
                0f

            return true
        }

        return true
    }

    private fun handleEquipmentDragInput():
        Boolean {

        if (
            pointer.justPressed
        ) {
            pointerPressedWeaponIndex =
                weaponCardRectangles()
                    .indexOfFirst { rectangle ->
                        rectangle.contains(
                            pointer.position
                        )
                    }
                    .takeIf { index ->
                        index >= 0
                    }

            pointerPressedArmorIndex =
                armorCardRectangles()
                    .indexOfFirst { rectangle ->
                        rectangle.contains(
                            pointer.position
                        )
                    }
                    .takeIf { index ->
                        index >= 0
                    }

            pointerPressedItemIndex =
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
                pointerPressedWeaponIndex !=
                null ||
                pointerPressedArmorIndex !=
                null ||
                pointerPressedItemIndex !=
                null
            ) {
                equipmentPressStartX =
                    pointer.position.x

                equipmentPressStartY =
                    pointer.position.y

                equipmentDragStarted =
                    false

                return true
            }
        }

        val pressedWeaponIndex =
            pointerPressedWeaponIndex

        val pressedArmorIndex =
            pointerPressedArmorIndex

        val pressedItemIndex =
            pointerPressedItemIndex

        if (
            pressedWeaponIndex == null &&
            pressedArmorIndex == null &&
            pressedItemIndex == null
        ) {
            return false
        }

        if (pointer.isHeld) {
            val deltaX =
                pointer.position.x -
                    equipmentPressStartX

            val deltaY =
                pointer.position.y -
                    equipmentPressStartY

            val distanceSquared =
                deltaX *
                    deltaX +
                    deltaY *
                    deltaY

            if (
                !equipmentDragStarted &&
                distanceSquared >=
                EQUIPMENT_DRAG_THRESHOLD *
                EQUIPMENT_DRAG_THRESHOLD
            ) {
                equipmentDragStarted =
                    true

                beginEquipmentDrag(
                    weaponIndex =
                        pressedWeaponIndex,

                    armorIndex =
                        pressedArmorIndex,

                    itemIndex =
                        pressedItemIndex
                )
            }

            return true
        }

        if (pointer.justReleased) {
            if (equipmentDragStarted) {
                finishEquipmentDrag()
            } else {
                openPressedEquipmentDetail(
                    weaponIndex =
                        pressedWeaponIndex,

                    armorIndex =
                        pressedArmorIndex,

                    itemIndex =
                        pressedItemIndex
                )
            }

            clearEquipmentPressState()

            return true
        }

        return true
    }

    private fun beginEquipmentDrag(
        weaponIndex:
        Int?,

        armorIndex:
        Int?,

        itemIndex:
        Int?
    ) {
        if (
            !interactionState
                .hasCharacterSelected
        ) {
            return
        }

        if (weaponIndex != null) {
            val weapon =
                visibleWeapons()
                    .getOrNull(
                        weaponIndex
                    )
                    ?: return

            if (
                weapon.initialUnlockState !=
                ContentUnlockState.UNLOCKED
            ) {
                return
            }

            interactionState
                .beginWeaponDrag(
                    weapon.id
                )

            return
        }

        if (armorIndex != null) {
            val armor =
                visibleArmors()
                    .getOrNull(
                        armorIndex
                    )
                    ?: return

            if (
                armor.initialUnlockState !=
                ContentUnlockState.UNLOCKED
            ) {
                return
            }

            interactionState
                .beginArmorDrag(
                    armor.id
                )

            return
        }

        if (itemIndex != null) {
            val item =
                visibleItems()
                    .getOrNull(
                        itemIndex
                    )
                    ?: return

            if (
                item.initialUnlockState !=
                ContentUnlockState.UNLOCKED
            ) {
                return
            }

            interactionState
                .beginItemDrag(
                    item.id
                )
        }
    }

    private fun finishEquipmentDrag() {
        val draggedContent =
            interactionState
                .draggedContent
                ?: return

        when (draggedContent.type) {
            CreatePresetEquipmentType.WEAPON -> {
                if (
                    weaponSlot.contains(
                        pointer.position
                    )
                ) {
                    interactionState
                        .finishDragOnWeaponSlot()
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

    private fun openPressedEquipmentDetail(
        weaponIndex:
        Int?,

        armorIndex:
        Int?,

        itemIndex:
        Int?
    ) {
        if (weaponIndex != null) {
            val weapon =
                visibleWeapons()
                    .getOrNull(
                        weaponIndex
                    )
                    ?: return

            interactionState
                .inspectWeapon(
                    weapon.id
                )

            return
        }

        if (armorIndex != null) {
            val armor =
                visibleArmors()
                    .getOrNull(
                        armorIndex
                    )
                    ?: return

            interactionState
                .inspectArmor(
                    armor.id
                )

            return
        }

        if (itemIndex != null) {
            val item =
                visibleItems()
                    .getOrNull(
                        itemIndex
                    )
                    ?: return

            interactionState
                .inspectItem(
                    item.id
                )
        }
    }

    private fun clearEquipmentPressState() {
        pointerPressedWeaponIndex =
            null

        pointerPressedArmorIndex =
            null

        pointerPressedItemIndex =
            null

        equipmentDragStarted =
            false
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
                .inspectedStatType !=
            null
        ) {
            if (
                !statDescriptionRectangle()
                    .contains(
                        pointer.position
                    )
            ) {
                interactionState
                    .closeStatDescription()
            }

            return
        }

        if (
            interactionState
                .isStatEditorOpen
        ) {
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
                .isCompleteWithoutTamga
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
                ?: return

        val itemId =
            interactionState
                .selectedItemId
                ?: return

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
                    existingPreset
                        ?.weaponTamgaId,

                armorTamgaId =
                    existingPreset
                        ?.armorTamgaId,

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
                weapon.initialUnlockState ==
                ContentUnlockState.UNLOCKED
        }
    }

    private fun visibleWeapons():
        List<WeaponDefinition> {

        return filteredWeapons()
            .drop(
                weaponScrollOffset
            )
            .take(
                WEAPON_VISIBLE_CARD_COUNT
            )
    }

    private fun filteredArmors():
        List<ArmorDefinition> {

        return armors.filter { armor ->
            interactionState.showLockedContent ||
                armor.initialUnlockState ==
                ContentUnlockState.UNLOCKED
        }
    }

    private fun visibleArmors():
        List<ArmorDefinition> {

        return filteredArmors()
            .drop(
                armorScrollOffset
            )
            .take(
                ARMOR_VISIBLE_CARD_COUNT
            )
    }

    private fun filteredItems():
        List<ItemDefinition> {

        return items.filter { item ->
            interactionState.showLockedContent ||
                item.initialUnlockState ==
                ContentUnlockState.UNLOCKED
        }
    }

    private fun visibleItems():
        List<ItemDefinition> {

        return filteredItems()
            .drop(
                itemScrollOffset
            )
            .take(
                ITEM_VISIBLE_CARD_COUNT
            )
    }

    private fun maximumItemScrollOffset():
        Int {

        return (
            filteredItems().size -
                ITEM_VISIBLE_CARD_COUNT
            )
            .coerceAtLeast(
                0
            )
    }

    private fun scrollItems(
        amount:
        Int
    ) {
        itemScrollOffset =
            (
                itemScrollOffset +
                    amount
                )
                .coerceIn(
                    0,
                    maximumItemScrollOffset()
                )
    }

    private fun maximumWeaponScrollOffset():
        Int {

        return (
            filteredWeapons().size -
                WEAPON_VISIBLE_CARD_COUNT
            )
            .coerceAtLeast(
                0
            )
    }

    private fun maximumArmorScrollOffset():
        Int {

        return (
            filteredArmors().size -
                ARMOR_VISIBLE_CARD_COUNT
            )
            .coerceAtLeast(
                0
            )
    }

    private fun scrollWeapons(
        amount:
        Int
    ) {
        weaponScrollOffset =
            (
                weaponScrollOffset +
                    amount
                )
                .coerceIn(
                    0,
                    maximumWeaponScrollOffset()
                )
    }

    private fun scrollArmors(
        amount:
        Int
    ) {
        armorScrollOffset =
            (
                armorScrollOffset +
                    amount
                )
                .coerceIn(
                    0,
                    maximumArmorScrollOffset()
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
        drawPanel(
            leftPanel,
            Color(
                0.05f,
                0.25f,
                0.09f,
                1f
            )
        )

        drawPanel(
            centerPanel,
            Color(
                0.28f,
                0.07f,
                0.31f,
                1f
            )
        )

        drawPanel(
            rightPanel,
            Color(
                0.04f,
                0.28f,
                0.32f,
                1f
            )
        )
    }

    private fun weaponListArea():
        Rectangle {

        return Rectangle(
            leftPanel.x,
            leftPanel.y +
                210f,
            leftPanel.width,
            leftPanel.height -
                210f
        )
    }

    private fun armorListArea():
        Rectangle {

        return Rectangle(
            leftPanel.x,
            leftPanel.y,
            leftPanel.width,
            200f
        )
    }

    private fun itemListArea():
        Rectangle {

        return Rectangle(
            rightPanel.x,
            rightPanel.y +
                205f,
            rightPanel.width,
            rightPanel.height -
                205f
        )
    }

    private fun weaponCardRectangles():
        List<Rectangle> {

        val columns =
            4

        val rows =
            3

        val horizontalPadding =
            16f

        val gap =
            10f

        val cardWidth =
            (
                leftPanel.width -
                    horizontalPadding * 2f -
                    gap * (columns - 1)
                ) /
                columns

        val cardHeight =
            62f

        val firstY =
            leftPanel.y +
                leftPanel.height -
                78f -
                cardHeight

        return List(
            columns *
                rows
        ) { index ->
            val column =
                index %
                    columns

            val row =
                index /
                    columns

            Rectangle(
                leftPanel.x +
                    horizontalPadding +
                    column *
                    (
                        cardWidth +
                            gap
                        ),

                firstY -
                    row *
                    (
                        cardHeight +
                            gap
                        ),

                cardWidth,

                cardHeight
            )
        }
    }

    private fun armorCardRectangles():
        List<Rectangle> {

        val columns =
            4

        val rows =
            2

        val horizontalPadding =
            16f

        val gap =
            10f

        val cardWidth =
            (
                leftPanel.width -
                    horizontalPadding * 2f -
                    gap * (columns - 1)
                ) /
                columns

        val cardHeight =
            62f

        val firstY =
            leftPanel.y +
                145f -
                cardHeight

        return List(
            columns *
                rows
        ) { index ->
            val column =
                index %
                    columns

            val row =
                index /
                    columns

            Rectangle(
                leftPanel.x +
                    horizontalPadding +
                    column *
                    (
                        cardWidth +
                            gap
                        ),

                firstY -
                    row *
                    (
                        cardHeight +
                            gap
                        ),

                cardWidth,

                cardHeight
            )
        }
    }

    private fun itemCardRectangles():
        List<Rectangle> {

        val columns =
            5

        val rows =
            3

        val horizontalPadding =
            16f

        val horizontalGap =
            9f

        val verticalGap =
            12f

        val cardWidth =
            (
                rightPanel.width -
                    horizontalPadding * 2f -
                    horizontalGap *
                    (
                        columns -
                            1
                        )
                ) /
                columns

        val cardHeight =
            58f

        val itemsArea =
            itemListArea()

        val firstY =
            itemsArea.y +
                itemsArea.height -
                72f -
                cardHeight

        return List(
            columns *
                rows
        ) { index ->

            val column =
                index %
                    columns

            val row =
                index /
                    columns

            Rectangle(
                itemsArea.x +
                    horizontalPadding +
                    column *
                    (
                        cardWidth +
                            horizontalGap
                        ),

                firstY -
                    row *
                    (
                        cardHeight +
                            verticalGap
                        ),

                cardWidth,

                cardHeight
            )
        }
    }

    private fun renderLeftPanel() {
        drawSectionTitle(
            text =
                "WEAPONS LIST",

            x =
                leftPanel.x,

            y =
                leftPanel.y +
                    leftPanel.height -
                    20f,

            width =
                leftPanel.width
        )

        val visibleWeapons =
            visibleWeapons()

        weaponCardRectangles()
            .forEachIndexed {
                    index,
                    rectangle ->

                val weapon =
                    visibleWeapons
                        .getOrNull(
                            index
                        )
                        ?: return@forEachIndexed

                val unlocked =
                    weapon.initialUnlockState ==
                        ContentUnlockState.UNLOCKED

                drawPanel(
                    rectangle =
                        rectangle,

                    fillColor =
                        if (unlocked) {
                            Color(
                                0.09f,
                                0.28f,
                                0.13f,
                                1f
                            )
                        } else {
                            Color(
                                0.16f,
                                0.16f,
                                0.16f,
                                1f
                            )
                        }
                )

                drawCenteredText(
                    text =
                        if (unlocked) {
                            weapon.displayName
                        } else {
                            "${weapon.displayName}\nLOCKED"
                        },

                    rectangle =
                        rectangle,

                    color =
                        if (unlocked) {
                            Color.WHITE
                        } else {
                            Color.GRAY
                        },

                    scale =
                        0.58f
                )
            }

        drawFilledRectangle(
            Rectangle(
                leftPanel.x,
                leftPanel.y +
                    205f,
                leftPanel.width,
                5f
            ),
            Color.BLACK
        )

        if (
            maximumWeaponScrollOffset() >
            0
        ) {
            font.color =
                Color.GRAY

            font.data.setScale(
                0.58f
            )

            font.draw(
                batch,
                "${weaponScrollOffset + 1}–" +
                    "${
                        weaponScrollOffset +
                            visibleWeapons().size
                    } / ${filteredWeapons().size}",
                leftPanel.x +
                    10f,
                leftPanel.y +
                    219f
            )

            font.data.setScale(
                1f
            )
        }

        drawSectionTitle(
            text =
                "ARMORS LIST",

            x =
                leftPanel.x,

            y =
                leftPanel.y +
                    185f,

            width =
                leftPanel.width
        )

        val visibleArmors =
            visibleArmors()

        armorCardRectangles()
            .forEachIndexed {
                    index,
                    rectangle ->

                val armor =
                    visibleArmors
                        .getOrNull(
                            index
                        )
                        ?: return@forEachIndexed

                val unlocked =
                    armor.initialUnlockState ==
                        ContentUnlockState.UNLOCKED

                drawPanel(
                    rectangle =
                        rectangle,

                    fillColor =
                        if (unlocked) {
                            Color(
                                0.22f,
                                0.22f,
                                0.25f,
                                1f
                            )
                        } else {
                            Color(
                                0.16f,
                                0.16f,
                                0.16f,
                                1f
                            )
                        }
                )

                drawCenteredText(
                    text =
                        if (unlocked) {
                            armor.displayName
                        } else {
                            "${armor.displayName}\nLOCKED"
                        },

                    rectangle =
                        rectangle,

                    color =
                        if (unlocked) {
                            Color.WHITE
                        } else {
                            Color.GRAY
                        },

                    scale =
                        0.58f
                )
            }

        if (
            maximumArmorScrollOffset() >
            0
        ) {
            font.color =
                Color.GRAY

            font.data.setScale(
                0.58f
            )

            font.draw(
                batch,
                "${armorScrollOffset + 1}–" +
                    "${
                        armorScrollOffset +
                            visibleArmors().size
                    } / ${filteredArmors().size}",
                leftPanel.x +
                    10f,
                leftPanel.y +
                    9f
            )

            font.data.setScale(
                1f
            )
        }

        if (
            !interactionState
                .hasCharacterSelected
        ) {
            font.color =
                Color.GOLD

            font.data.setScale(
                0.68f
            )

            font.draw(
                batch,
                "Choose a Character before equipping Gear.",
                leftPanel.x,
                leftPanel.y +
                    12f,
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

        renderEquipmentSlot(
            rectangle =
                weaponTamgaSlot,

            title =
                "WEAPON\nTAMGA"
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

        renderEquipmentSlot(
            rectangle =
                armorTamgaSlot,

            title =
                "ARMOR\nTAMGA"
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

    private fun renderRightPanel() {
        /*
         * Üst bölüm: Items
         */
        val itemsSection =
            Rectangle(
                rightPanel.x,
                rightPanel.y +
                    205f,
                rightPanel.width,
                rightPanel.height -
                    205f
            )

        drawSectionTitle(
            text =
                "ITEMS LIST",

            x =
                itemsSection.x,

            y =
                itemsSection.y +
                    itemsSection.height -
                    20f,

            width =
                itemsSection.width
        )

        val visibleItems =
            visibleItems()

        itemCardRectangles()
            .forEachIndexed {
                    index,
                    rectangle ->

                val item =
                    visibleItems
                        .getOrNull(
                            index
                        )
                        ?: return@forEachIndexed

                val unlocked =
                    item.initialUnlockState ==
                        ContentUnlockState.UNLOCKED

                val selected =
                    item.id ==
                        interactionState
                            .selectedItemId

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
                                Color(
                                    0.25f,
                                    0.10f,
                                    0.11f,
                                    1f
                                )
                            }
                        }
                )

                drawCenteredText(
                    text =
                        if (unlocked) {
                            item.displayName
                        } else {
                            "${item.displayName}\nLOCKED"
                        },

                    rectangle =
                        rectangle,

                    color =
                        if (unlocked) {
                            Color.WHITE
                        } else {
                            Color.GRAY
                        },

                    scale =
                        0.52f
                )
            }

        if (
            maximumItemScrollOffset() >
            0
        ) {
            font.color =
                Color.GRAY

            font.data.setScale(
                0.58f
            )

            font.draw(
                batch,
                "${itemScrollOffset + 1}–" +
                    "${
                        itemScrollOffset +
                            visibleItems.size
                    } / ${filteredItems().size}",
                itemsSection.x +
                    10f,
                itemsSection.y +
                    8f
            )

            font.data.setScale(
                1f
            )
        }

        /*
         * Item ve Tamga listelerini ayıran çizgi.
         */
        drawFilledRectangle(
            Rectangle(
                rightPanel.x,
                rightPanel.y +
                    198f,
                rightPanel.width,
                7f
            ),
            Color.BLACK
        )

        /*
         * Alt bölüm: Tamgas
         */
        val tamgaSection =
            Rectangle(
                rightPanel.x,
                rightPanel.y,
                rightPanel.width,
                198f
            )

        drawSectionTitle(
            text =
                "TAMGA LIST",

            x =
                tamgaSection.x,

            y =
                tamgaSection.y +
                    tamgaSection.height -
                    18f,

            width =
                tamgaSection.width
        )

        renderPlaceholderGrid(
            section =
                tamgaSection,

            startY =
                tamgaSection.y +
                    tamgaSection.height -
                    78f,

            rowCount =
                2,

            columnCount =
                5
        )
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
                .inspectedStatType !=
                null -> {
                renderStatDescription()
            }

            interactionState
                .isStatEditorOpen -> {
                renderStatEditor()
            }

            interactionState
                .isCharacterPickerOpen -> {
                renderCharacterPicker()
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
                        205f,
                    row.y +
                        22f
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
            statType.displayName,
            panel.x,
            panel.y +
                panel.height -
                28f,
            panel.width,
            Align.center,
            false
        )

        font.data.setScale(
            0.86f
        )

        font.color =
            Color.LIGHT_GRAY

        font.draw(
            batch,
            statDescription(
                statType
            ),
            panel.x +
                28f,
            panel.y +
                panel.height -
                90f,
            panel.width -
                56f,
            Align.left,
            true
        )

        font.color =
            Color.GRAY

        font.data.setScale(
            0.7f
        )

        font.draw(
            batch,
            "Click outside to close.",
            panel.x +
                20f,
            panel.y +
                18f
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
            385f,
            205f,
            510f,
            310f
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

        ITEMS
    }
}

