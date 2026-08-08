package com.azizoglu.grisilith.lwjgl3

import com.azizoglu.grisilith.GrisilithGame
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration

object Lwjgl3Launcher {

    @JvmStatic
    fun main(args: Array<String>) {
        if (StartupHelper.startNewJvmIfRequired()) {
            return
        }

        Lwjgl3Application(
            GrisilithGame(),
            createConfiguration()
        )
    }

    private fun createConfiguration(): Lwjgl3ApplicationConfiguration {
        return Lwjgl3ApplicationConfiguration().apply {
            setTitle("Grisilith")
            useVsync(true)

            setForegroundFPS(
                Lwjgl3ApplicationConfiguration
                    .getDisplayMode()
                    .refreshRate + 1
            )

            setWindowedMode(1280, 720)

            setWindowIcon(
                "icons/grisilith_icon_128.png",
                "icons/grisilith_icon_64.png",
                "icons/grisilith_icon_32.png",
                "icons/grisilith_icon_16.png"
            )
        }
    }
}
