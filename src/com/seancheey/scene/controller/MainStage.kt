package com.seancheey.scene.controller

import com.seancheey.scene.Scenes
import com.seancheey.scene.Stages
import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage

/**
 * Created by Seancheey on 30/05/2017.
 * GitHub: https://github.com/Seancheey
 */

class MainStage : Application() {

    override fun start(primaryStage: Stage) {
        Stages.primaryStage = primaryStage
        primaryStage.title = "Ark Sonah"
        primaryStage.scene = Scene(Scenes.main, 800.0, 600.0)
        primaryStage.show()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(MainStage::class.java)
        }
    }

}