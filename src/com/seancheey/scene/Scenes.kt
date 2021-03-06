package com.seancheey.scene

import javafx.fxml.FXMLLoader
import javafx.scene.Parent

/**
 * Created by Seancheey on 29/05/2017.
 * GitHub: https://github.com/Seancheey
 */
object Scenes {
    val main
        get() = load("main.fxml")
    val menu
        get() = load("menu.fxml")
    val bot_edit
        get() = load("bot_edit.fxml")
    val bot_battle
        get() = load("bot_battle.fxml")
    val battle_select
        get() = load("battle_select.fxml")

    fun load(url: String): Parent {
        return FXMLLoader.load(javaClass.getResource(url))
    }
}