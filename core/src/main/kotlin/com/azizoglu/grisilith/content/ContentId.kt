package com.azizoglu.grisilith.content

@JvmInline
value class ContentId(
    val value: String
) {
    init {
        require(value.isNotBlank()) {
            "Content ID cannot be blank."
        }

        require(
            value.matches(
                Regex("[A-Z0-9_]+")
            )
        ) {
            "Content ID must contain only uppercase letters, numbers and underscores."
        }
    }

    override fun toString(): String {
        return value
    }
}
