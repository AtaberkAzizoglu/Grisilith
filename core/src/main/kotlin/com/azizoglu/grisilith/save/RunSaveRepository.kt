package com.azizoglu.grisilith.save

import com.badlogic.gdx.Gdx

/** Removes checkpoint files created by versions that still supported Load Game. */
object RunSaveRepository {
    private const val SAVE_DIRECTORY = "saves"
    private val legacyFileNames = listOf(
        "active_run.json",
        "active_run.tmp",
        "active_run.backup.json"
    )

    fun delete(): Boolean {
        val directory = Gdx.files.local(SAVE_DIRECTORY)
        var changed = false
        legacyFileNames.forEach { fileName ->
            val file = directory.child(fileName)
            if (file.exists()) changed = file.delete() || changed
        }
        return changed
    }
}
