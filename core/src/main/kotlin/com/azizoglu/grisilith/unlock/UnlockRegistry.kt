package com.azizoglu.grisilith.unlock

object UnlockRegistry {

    const val STAT_POINT_01_ID =
        "stat_point_01"

    private val definitions:
        List<UnlockDefinition> =
        listOf(
            UnlockDefinition(
                id =
                    STAT_POINT_01_ID,

                displayName =
                    "+1 PURCHASED STAT POINT",

                description =
                    "Adds one permanent point to your purchased stat pool.",

                type =
                    UnlockType.STAT_POINT,

                cost =
                    25
            )
        )

    private val definitionsById:
        Map<String, UnlockDefinition> =
        definitions.associateBy {
            it.id
        }

    init {
        require(
            definitionsById.size ==
                definitions.size
        ) {
            "UnlockRegistry contains duplicate ids."
        }

        validateDependencies()
    }

    fun all():
        List<UnlockDefinition> {

        return definitions
    }

    fun visible():
        List<UnlockDefinition> {

        return definitions.filterNot {
            it.hidden
        }
    }

    fun availableForMerchant(
        unlockedIds: Set<String>
    ): List<UnlockDefinition> {

        return visible()
            .filterNot {
                it.id in unlockedIds
            }
            .filter {
                dependenciesAreMet(
                    definition =
                        it,

                    unlockedIds =
                        unlockedIds
                )
            }
    }

    fun find(
        id: String
    ): UnlockDefinition? {

        return definitionsById[
            id
        ]
    }

    fun require(
        id: String
    ): UnlockDefinition {

        return checkNotNull(
            find(
                id
            )
        ) {
            "Unknown unlock id: $id"
        }
    }

    fun byType(
        type: UnlockType,
        includeHidden: Boolean = false
    ): List<UnlockDefinition> {

        return definitions.filter {
                definition ->

            definition.type ==
                type &&
                (
                    includeHidden ||
                        !definition.hidden
                    )
        }
    }

    fun dependenciesAreMet(
        definition: UnlockDefinition,
        unlockedIds: Set<String>
    ): Boolean {

        return unlockedIds.containsAll(
            definition.requiredUnlockIds
        )
    }

    private fun validateDependencies() {
        definitions.forEach {
                definition ->

            definition
                .requiredUnlockIds
                .forEach {
                        requiredId ->

                    require(
                        definitionsById.containsKey(
                            requiredId
                        )
                    ) {
                        "Unlock ${definition.id} requires unknown unlock $requiredId"
                    }
                }
        }
    }
}
