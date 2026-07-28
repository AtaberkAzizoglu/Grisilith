package com.azizoglu.grisilith.preset

sealed class PresetRepositoryResult {

    data class Success(
        val preset:
        RunPreset? =
            null
    ) : PresetRepositoryResult()

    data class Failure(
        val message:
        String
    ) : PresetRepositoryResult()
}
