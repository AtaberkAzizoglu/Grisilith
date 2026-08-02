package com.azizoglu.grisilith.save

data class RunSavePreview(
    val characterDisplayName: String = "",
    val mapDisplayName: String = "",
    val difficultyDisplayName: String = "",
    val stage: Int = 1,
    val maximumStage: Int = 1,
    val elapsedRunTimeSeconds: Float = 0f,
    val savedAtEpochMillis: Long = 0L
)
