package com.azizoglu.grisilith.collection.filter

class CollectionFilterState(
    val options: List<CollectionFilterOption>
) {
    private val selectedIds = mutableSetOf<String>()

    var isDropdownOpen: Boolean = false
        private set

    val selectedOptionIds: Set<String>
        get() = selectedIds.toSet()

    fun toggleDropdown() {
        isDropdownOpen = !isDropdownOpen
    }

    fun closeDropdown() {
        isDropdownOpen = false
    }

    fun toggle(optionId: String): Boolean {
        if (options.none { it.id == optionId }) return false

        if (optionId in selectedIds) {
            selectedIds.remove(optionId)
        } else {
            selectedIds.add(optionId)
        }

        return true
    }

    fun reset() {
        selectedIds.clear()
        isDropdownOpen = false
    }
}
