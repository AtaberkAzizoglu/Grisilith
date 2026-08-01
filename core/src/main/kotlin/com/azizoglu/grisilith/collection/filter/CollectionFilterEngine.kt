package com.azizoglu.grisilith.collection.filter

import com.azizoglu.grisilith.collection.CollectionEntry

object CollectionFilterEngine {

    fun apply(
        entries: List<CollectionEntry>,
        options: List<CollectionFilterOption>,
        selectedOptionIds: Set<String>
    ): List<CollectionEntry> {
        if (selectedOptionIds.isEmpty()) return entries

        val selectedByGroup =
            options
                .filter { it.id in selectedOptionIds }
                .groupBy { it.groupId }

        if (selectedByGroup.isEmpty()) return entries

        return entries.filter { entry ->
            selectedByGroup.values.all { groupOptions ->
                groupOptions.any { option ->
                    option.matches(entry.definition)
                }
            }
        }
    }
}
