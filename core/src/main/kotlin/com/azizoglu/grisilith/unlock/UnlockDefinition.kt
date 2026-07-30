package com.azizoglu.grisilith.unlock

data class UnlockDefinition(
    val id: String,
    val displayName: String,
    val description: String,
    val type: UnlockType,
    val cost: Int,
    val hidden: Boolean = false,
    val requiredUnlockIds: Set<String> =
        emptySet()
) {

    init {
        require(
            id.isNotBlank()
        ) {
            "Unlock id cannot be blank."
        }

        require(
            displayName.isNotBlank()
        ) {
            "Unlock display name cannot be blank."
        }

        require(
            description.isNotBlank()
        ) {
            "Unlock description cannot be blank."
        }

        require(
            cost >= 0
        ) {
            "Unlock cost cannot be negative."
        }

        require(
            id !in requiredUnlockIds
        ) {
            "An unlock cannot require itself: $id"
        }

        require(
            requiredUnlockIds.none {
                it.isBlank()
            }
        ) {
            "Required unlock ids cannot contain blank values."
        }
    }
}
