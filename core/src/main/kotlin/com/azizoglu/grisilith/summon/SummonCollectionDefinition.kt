package com.azizoglu.grisilith.summon

import com.azizoglu.grisilith.content.*

/** Archive-only definitions: these never enter equipment or merchant loot pools. */
data class SummonCollectionDefinition(val type: SummonType) : ContentDefinition {
    override val id = ContentId("SUMMON_${type.name}")
    override val displayName = type.displayName
    override val description get() = SummonPresentation.archive(type)
    override val category = ContentCategory.SUMMON
    override val tags: Set<ContentTag> = emptySet()
    override val initialUnlockState = ContentUnlockState.UNLOCKED

    companion object {
        val all = SummonType.entries.map(::SummonCollectionDefinition)
    }
}
