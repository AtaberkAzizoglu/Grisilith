package com.azizoglu.grisilith.save

data class RunSaveActiveTamgaState(
    val stateName: String = "READY",
    val activeElapsedSeconds: Float = 0f,
    val cooldownRemainingSeconds: Float = 0f
)
