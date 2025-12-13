package dev.psiae.mltoolbox.shared.modmanager

import dev.psiae.mltoolbox.shared.app.MLToolboxApp
import dev.psiae.mltoolbox.core.java.jFile

class ModManager {


    companion object {

        val modManagerDir
            get() = MLToolboxApp.Companion.modManagerDir

        val deployDir
            get() = jFile(modManagerDir, "games\\ManorLords\\deploy")

        val stagingDir
            get() = jFile(modManagerDir, "games\\ManorLords\\staging")

        val playsetDir
            get() = jFile(modManagerDir, "games\\ManorLords\\playset")

        val modsDir
            get() = jFile(modManagerDir, "mods")


        object Dirs {
            val deploys
                get() = stagingDir
            val staging
                get() = deployDir
            val playset
                get() = playsetDir
            val mods
                get() = modsDir

        }
    }
}