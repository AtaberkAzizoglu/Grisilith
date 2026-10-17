package com.azizoglu.grisilith.screen.preset

class PresetManagerInteractionState {

    var scrollOffset:
        Int = 0
        private set

    var modal:
        PresetManagerModal =
        PresetManagerModal.None
        private set

    val isModalOpen: Boolean
        get() =
            modal !is
                PresetManagerModal.None

    fun scroll(
        amount:
        Int,

        maximumOffset:
        Int
    ) {
        scrollOffset =
            (
                scrollOffset +
                    amount
                )
                .coerceIn(
                    0,
                    maximumOffset
                )
    }

    fun clampScroll(
        maximumOffset:
        Int
    ) {
        scrollOffset =
            scrollOffset.coerceIn(
                0,
                maximumOffset
            )
    }

    fun setScrollOffset(
        value: Int,
        maximumOffset: Int
    ) {
        scrollOffset = value.coerceIn(0, maximumOffset)
    }

    fun openStats(
        presetId:
        String
    ) {
        modal =
            PresetManagerModal
                .Stats(
                    presetId
                )
    }

    fun openCharacter(
        presetId:
        String
    ) {
        modal =
            PresetManagerModal
                .Character(
                    presetId
                )
    }

    fun openContent(
        presetId:
        String,

        contentType:
        PresetCardContentType
    ) {
        modal =
            PresetManagerModal
                .Content(
                    presetId =
                        presetId,

                    contentType =
                        contentType
                )
    }

    fun openDeleteConfirmation(
        presetId:
        String
    ) {
        modal =
            PresetManagerModal
                .DeleteConfirmation(
                    presetId
                )
    }

    fun closeModal() {
        modal =
            PresetManagerModal.None
    }

    fun reset() {
        scrollOffset =
            0

        closeModal()
    }
}

sealed class PresetManagerModal {

    data object None :
        PresetManagerModal()

    data class Stats(
        val presetId:
        String
    ) : PresetManagerModal()

    data class Character(
        val presetId:
        String
    ) : PresetManagerModal()

    data class Content(
        val presetId:
        String,

        val contentType:
        PresetCardContentType
    ) : PresetManagerModal()

    data class DeleteConfirmation(
        val presetId:
        String
    ) : PresetManagerModal()
}

enum class PresetCardContentType {

    WEAPON,

    ARMOR,

    ITEM,

    WEAPON_TAMGA,

    ARMOR_TAMGA
}
