@file:JvmName("Main")
package com.eginez.kopass

import javafx.application.Application
import javafx.scene.control.TreeItem
import javafx.scene.layout.HBox
import javafx.stage.Stage
import tornadofx.*
import java.io.File
import com.freiheit.gnupg.GnuPGContext
import javafx.scene.control.TextArea
import java.nio.file.Paths


val  maxTimePassDialog = 10.seconds
val passFile = ".password-store"

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


    init {
        loadGPGLib()
        title = "Meerkats Pass"
        with(root) {
            vbox {
                treeview<File> {
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
        val cipher = file.readBytes()
        val msg = ctx?.decrypt(cipher)
        val d = find(PassDialog::class)
        d.label.text = msg.toString()
        d.openModal()
        runLater(maxTimePassDialog) {
            d?.close()
        }
    }

    fun loadGPGLib() {
        ctx = GnuPGContext()
    }
}

class PassDialog : Fragment(){
    override val root = HBox()
    val label = TextArea()
    init {
        with(root) {this += label}
    }
}

