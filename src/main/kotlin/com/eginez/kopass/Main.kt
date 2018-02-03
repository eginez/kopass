@file:JvmName("Main")
package com.eginez.kopass

import javafx.application.Application
import javafx.scene.layout.GridPane
import javafx.stage.Stage
import tornadofx.App
import tornadofx.View;


fun main(args: Array<String>) {
    Application.launch(TheApp::class.java, *args)
}

class TheApp: App(MainScreen::class) {
    override fun start(stage: Stage) {
        super.start(stage)
    }
}

class MainScreen: View() {
    override val root = GridPane()

    init {
        title = "Meerkats Pass"
    }

}

