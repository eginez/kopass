@file:JvmName("Main")
package com.eginez.kopass

import javafx.application.Application
import javafx.scene.control.TreeItem
import javafx.scene.layout.HBox
import javafx.stage.Stage
import tornadofx.*
import java.io.File
import com.freiheit.gnupg.GnuPGContext
import com.freiheit.gnupg.GnuPGException
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.event.EventHandler
import javafx.scene.control.TextArea
import javafx.scene.control.TreeView
import javafx.util.Duration
import java.io.FileInputStream
import java.io.PrintWriter
import java.nio.file.Paths


val  maxTimePassDialog = 10.seconds
val passFile = ".password-store"
val logFolder = "/tmp"
val logFile = File.createTempFile("kopass", "log", File(logFolder))

fun main(args: Array<String>) {
    Application.launch(TheApp::class.java, *args)
}


fun findHome(): String = Paths.get(System.getProperty("user.home"), passFile).toString()

class TheApp: App(MainScreen::class) {

    override fun start(stage: Stage) {
        super.start(stage)
    }
}

class MainScreen: View() {
    override val root = HBox()
    val noPassFiles = setOf(".gitattributes", ".gpg-id", "")
    var ctx: GnuPGContext? = null
    var treeView = TreeView<File>()


    init {
        menubar {
            useSystemMenuBarProperty().set(true)
            menu("Actions") {
                item("Refresh") {
                    setOnAction { refresh(findHome()) }
                }
            }
        }

        title = "Meerkats Pass"
        with(root) {
            vbox {
                 treeView = treeview<File> {
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
            }
        }
    }

    fun show(file: File) {
        if (file.isDirectory) return
        runAsync {
            try {
                val msg = decryptgpg(file.path)
                val d = find(PassDialog::class)
                d.label.text = msg.toString()
                runLater {
                    d.openModal()
                    runLater(maxTimePassDialog) {
                        d?.close()
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

class PassDialog : Fragment(){
    override val root = HBox()
    val label = TextArea()
    init {
        with(root) {this += label}
    }
}

fun Exception.log() {
    val w = PrintWriter(logFile)
    println(this)
    this.printStackTrace(w)
    w.close()
}
