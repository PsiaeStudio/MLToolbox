package dev.psiae.mltoolbox.shared.startup

import dev.psiae.mltoolbox.shared.app.MLToolboxApp
import dev.psiae.mltoolbox.shared.java.jFile
import dev.psiae.mltoolbox.shared.user.data.UserProfileDataStore
import dev.psiae.mltoolbox.shared.user.data.UserRepository
import dev.psiae.mltoolbox.shared.user.data.dao.UserProfileDAO
import dev.psiae.mltoolbox.shared.user.domain.UserService

object AppInitializer {

    private fun getExecutablePath(): String {
        val path = ProcessHandle.current()
            .info()
            .command()
            .orElse(null)
        if (path == null)
            throw RuntimeException("Unable to get executable path via ProcessHandle.current()")
        return path
    }

    fun init() {
        MLToolboxApp.construct()
        MLToolboxApp.provideExePath(getExecutablePath())
        if (!jFile(MLToolboxApp.getExePath()).isFile)
            error("Invalid EXE_PATH")

        UserProfileDataStore.construct()
        UserProfileDAO.construct()
        UserRepository.construct()
        UserService.construct()
    }
}