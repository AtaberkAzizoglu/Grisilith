package com.azizoglu.grisilith

import com.azizoglu.grisilith.screen.FirstScreen
import com.badlogic.gdx.Game

class GrisilithGame : Game() {

    override fun create() {
        screen = FirstScreen()
    }
}
