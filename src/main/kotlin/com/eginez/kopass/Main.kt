@file:JvmName("Main")
package com.eginez.kopass

import javafx.application.Application
import javafx.scene.control.TreeItem
import javafx.scene.layout.HBox
import tornadofx.*
import java.io.File
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.event.EventHandler
import javafx.scene.control.TextArea
import javafx.scene.control.TreeView
import javafx.scene.layout.Priority
import javafx.util.Duration
import java.io.PrintWriter
import java.nio.file.Paths


val  maxTimePassDialog = 7.seconds
val passFile = ".password-store"
val logFolder = "/tmp"
val logFile = File.createTempFile("kopass", "log", File(logFolder))

fun main(args: Array<String>) {
    Application.launch(TheApp::class.java, *args)
}


fun findHome(): String = Paths.get(System.getProperty("user.home"), passFile).toString()

class TheApp: App(MainScreen::class)

class MainScreen: View() {
    override val root = HBox()
    val noPassFiles = setOf(".gitattributes", ".gpg-id", "")
    var treeView = TreeView<File>()
    var secretsArea = TextArea()


    init {
        menubar {
            useSystemMenuBarProperty().set(true)
            menu("Actions") {
                item("Refresh") {
                    setOnAction {
                        if (refresh(findHome())) {
                            information("Refresh", "Reloading secrets finished successfully")
                        }
                    }
                }
            }
        }

        title = "Meerkats Pass"
        with(root) {
            vbox {
                 treeView = treeview<File> {
                     vgrow = Priority.ALWAYS
                    root = TreeItem(File(findHome()))
                    cellFormat {
                        text = if (it == root.value) "Passwords"  else it.nameWithoutExtension
                    }
                    onUserSelect { show(it) }
                    populate {
                        val children = it.value?.listFiles { dir, name ->
                            dir.name != ".git" && dir.name != ""
                                    && !noPassFiles.contains(name)

                        }
                        children?.asIterable()
                    }
                }
                vgrow = Priority.ALWAYS
            }
            vbox {
                secretsArea = textarea { text = "" }
                hgrow = Priority.ALWAYS
            }
        }
    }

    fun show(file: File) {
        if (file.isDirectory) return
        runAsync {
            try {
                val msg = decryptgpg(file.path)
                runLater {
                    secretsArea.text = msg
                    runLater(maxTimePassDialog) {
                        secretsArea.clear()
                    }
                }
            } catch (ex: Exception) {
                ex.log()
                shakeStage()
            }
        }
    }

    fun shakeStage() {
        var moved = false
        val cycleCount = 10
        val move = 10
        val keyframeDuration = Duration.seconds(0.04)

        val stage = FX.primaryStage

        val timeline = Timeline(KeyFrame(keyframeDuration, EventHandler {
            if (!moved) {
                stage.x = stage.x + move
                stage.y = stage.y + move
            } else {
                stage.x = stage.x - move
                stage.y = stage.y - move
            }
            moved = moved.not()
        }))

        timeline.cycleCount = cycleCount
        timeline.isAutoReverse = false

        timeline.play()
    }
}

fun Exception.log() {
    val w = PrintWriter(logFile)
    println(this)
    this.printStackTrace(w)
    w.close()
}
