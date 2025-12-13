package dev.psiae.mltoolbox.shared.startup

import dev.psiae.mltoolbox.core.Core
import dev.psiae.mltoolbox.shared.app.MLToolboxApp
import dev.psiae.mltoolbox.core.mainDispatchingDispatcher
import dev.psiae.mltoolbox.core.coroutine.AppCoroutineDispatchers
import dev.psiae.mltoolbox.core.java.jFile
import dev.psiae.mltoolbox.shared.user.data.UserProfileDataStore
import dev.psiae.mltoolbox.shared.user.data.UserRepository
import dev.psiae.mltoolbox.shared.user.data.dao.UserProfileDAO
import dev.psiae.mltoolbox.shared.user.domain.UserService
import kotlinx.coroutines.Dispatchers

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
        if (!jFile(MLToolboxApp.exePath).isFile)
            error("Invalid EXE_PATH")

        AppCoroutineDispatchers.construct(
            AppCoroutineDispatchers(
                Core.mainDispatchingDispatcher,
                Dispatchers.IO,
                Dispatchers.Default,
                Dispatchers.Unconfined
            )
        )

        UserProfileDataStore.construct()
        UserProfileDAO.construct()
        UserRepository.construct()
        UserService.construct()
    }
}