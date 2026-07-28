package com.azizoglu.grisilith.preset

sealed class PresetValidationResult {

    data object Valid :
        PresetValidationResult()

    data class Invalid(
        val message:
        String
    ) : PresetValidationResult()
}
