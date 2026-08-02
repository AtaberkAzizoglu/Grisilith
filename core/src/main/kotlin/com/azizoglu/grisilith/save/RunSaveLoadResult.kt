package com.azizoglu.grisilith.save

sealed interface RunSaveLoadResult {
    data class Success(val snapshot: RunSaveSnapshot) : RunSaveLoadResult
    data object NoSave : RunSaveLoadResult
    data class UnsupportedVersion(val foundVersion: Int, val supportedVersion: Int) : RunSaveLoadResult
    data class InvalidSave(val errors: List<String>) : RunSaveLoadResult
    data class CorruptedSave(val message: String) : RunSaveLoadResult
}
