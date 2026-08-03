package com.azizoglu.grisilith.save

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.Json
import com.badlogic.gdx.utils.JsonWriter

object RunSaveRepository {
    private const val SAVE_DIRECTORY = "saves"
    private const val SAVE_FILE_NAME = "active_run.json"
    private const val TEMP_FILE_NAME = "active_run.tmp"
    private const val BACKUP_FILE_NAME = "active_run.backup.json"

    private val json =
        Json().apply {

            setOutputType(
                JsonWriter.OutputType.json
            )

            setIgnoreUnknownFields(
                true
            )

            setUsePrototypes(
                false
            )
        }

    private val saveDirectory: FileHandle get() = Gdx.files.local(SAVE_DIRECTORY)
    private val saveFile: FileHandle get() = saveDirectory.child(SAVE_FILE_NAME)
    private val temporaryFile: FileHandle get() = saveDirectory.child(TEMP_FILE_NAME)
    private val backupFile: FileHandle get() = saveDirectory.child(BACKUP_FILE_NAME)

    fun hasSave(): Boolean = saveFile.exists()

    fun saveFilePath():
        String {

        return saveFile
            .file()
            .absolutePath
    }

    fun save(snapshot: RunSaveSnapshot): Boolean {
        val errors = snapshot.validationErrors()
        if (errors.isNotEmpty()) {
            Gdx.app.error("RunSaveRepository", "Run save was rejected: ${errors.joinToString()}")
            return false
        }
        return try {
            ensureDirectoryExists()
            temporaryFile.writeString(json.prettyPrint(snapshot), false, "UTF-8")
            val verified = decode(temporaryFile)
            if (!verified.isValid) {
                temporaryFile.delete()
                Gdx.app.error("RunSaveRepository", "Temporary run save failed validation.")
                return false
            }
            if (saveFile.exists()) saveFile.copyTo(backupFile)
            if (saveFile.exists()) saveFile.delete()
            temporaryFile.moveTo(saveFile)
            true
        } catch (exception: Exception) {
            Gdx.app.error("RunSaveRepository", "Could not write run save.", exception)
            if (temporaryFile.exists()) temporaryFile.delete()
            false
        }
    }

    fun load(): RunSaveLoadResult {
        if (!saveFile.exists()) return RunSaveLoadResult.NoSave
        return loadFrom(saveFile, true)
    }

    fun preview(): RunSavePreview? = when (val result = load()) {
        is RunSaveLoadResult.Success -> result.snapshot.preview
        RunSaveLoadResult.NoSave,
        is RunSaveLoadResult.UnsupportedVersion,
        is RunSaveLoadResult.InvalidSave,
        is RunSaveLoadResult.CorruptedSave -> null
    }

    fun delete(): Boolean {
        var changed = false
        listOf(saveFile, temporaryFile, backupFile).forEach { file ->
            if (file.exists()) changed = file.delete() || changed
        }
        return changed
    }

    private fun loadFrom(file: FileHandle, allowBackupRecovery: Boolean): RunSaveLoadResult {
        return try {
            val snapshot = decode(file)
            if (snapshot.version > RunSaveSnapshot.CURRENT_VERSION) {
                return RunSaveLoadResult.UnsupportedVersion(snapshot.version, RunSaveSnapshot.CURRENT_VERSION)
            }
            val errors = snapshot.validationErrors()
            if (errors.isNotEmpty()) return RunSaveLoadResult.InvalidSave(errors)
            RunSaveLoadResult.Success(snapshot)
        } catch (exception: Exception) {
            if (allowBackupRecovery && backupFile.exists()) {
                val backupResult = loadFrom(backupFile, false)
                if (backupResult is RunSaveLoadResult.Success) {
                    try { backupFile.copyTo(saveFile) }
                    catch (restoreException: Exception) {
                        Gdx.app.error("RunSaveRepository", "Backup loaded but primary save could not be restored.", restoreException)
                    }
                    return backupResult
                }
            }
            RunSaveLoadResult.CorruptedSave(exception.message ?: "Unknown save decoding error.")
        }
    }

    private fun decode(file: FileHandle): RunSaveSnapshot {
        val encoded = file.readString("UTF-8")
        if (encoded.isBlank()) throw IllegalStateException("Run save file is empty.")
        return json.fromJson(RunSaveSnapshot::class.java, encoded)
            ?: throw IllegalStateException("Run save JSON produced no snapshot.")
    }

    private fun ensureDirectoryExists() {
        if (!saveDirectory.exists()) saveDirectory.mkdirs()
    }
}
