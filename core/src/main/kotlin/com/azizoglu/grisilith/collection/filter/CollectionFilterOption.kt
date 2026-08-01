package com.azizoglu.grisilith.collection.filter

import com.azizoglu.grisilith.content.ContentDefinition
import com.badlogic.gdx.graphics.Color

data class CollectionFilterOption(
    val id: String,
    val groupId: String,
    val displayName: String,
    val textColor: Color = Color.WHITE,
    val matches: (ContentDefinition) -> Boolean
)
