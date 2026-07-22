package com.azizoglu.grisilith.content

class ContentRegistry {

    private val definitions =
        mutableMapOf<ContentId, ContentDefinition>()

    val allDefinitions: List<ContentDefinition>
        get() = definitions.values.toList()

    fun register(
        definition: ContentDefinition
    ) {
        require(
            definition.id !in definitions
        ) {
            "Duplicate content ID: ${definition.id}"
        }

        definitions[definition.id] =
            definition
    }

    fun registerAll(
        newDefinitions: Iterable<ContentDefinition>
    ) {
        newDefinitions.forEach { definition ->
            register(definition)
        }
    }

    fun findById(
        id: ContentId
    ): ContentDefinition? {
        return definitions[id]
    }

    fun definitionsForCategory(
        category: ContentCategory
    ): List<ContentDefinition> {
        return definitions.values
            .filter { definition ->
                definition.category == category
            }
    }

    fun definitionsWithTag(
        tag: ContentTag
    ): List<ContentDefinition> {
        return definitions.values
            .filter { definition ->
                tag in definition.tags
            }
    }
}
