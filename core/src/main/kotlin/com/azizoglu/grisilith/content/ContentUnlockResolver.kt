package com.azizoglu.grisilith.content

import com.azizoglu.grisilith.profile.PlayerProfileRepository

object ContentUnlockResolver {

    fun isUnlocked(
        definition: ContentDefinition
    ): Boolean {

        return when (
            definition.initialUnlockState
        ) {
            ContentUnlockState.UNLOCKED,
            ContentUnlockState.OWNED -> {
                true
            }

            ContentUnlockState.LOCKED -> {
                PlayerProfileRepository
                    .isUnlocked(
                        definition.id.value
                    )
            }
        }
    }
}
