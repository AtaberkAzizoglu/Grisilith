package com.azizoglu.grisilith.preset

import com.badlogic.gdx.Gdx

/** Kept separate from edits: changing a preset is not playing a run. */
object PresetUsageRepository {
    fun lastUsed(id: String): Long = Gdx.app.getPreferences("grisilith-preset-usage").getLong(id, 0L)
    fun markUsed(id: String) {
        Gdx.app.getPreferences("grisilith-preset-usage").putLong(id, System.currentTimeMillis()).flush()
    }
}
