package com.azizoglu.grisilith.content

interface ContentDefinition {

    val id: ContentId

    val displayName: String

    val description: String

    val category: ContentCategory

    val tags: Set<ContentTag>

    val initialUnlockState: ContentUnlockState
}
