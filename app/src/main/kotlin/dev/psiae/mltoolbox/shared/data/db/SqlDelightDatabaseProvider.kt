package dev.psiae.mltoolbox.shared.data.db

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import dev.psiae.mltoolbox.data.sqldelight.AppDatabase
import dev.psiae.mltoolbox.shared.app.MLToolboxApp
import dev.psiae.mltoolbox.core.LazyConstructor
import java.util.Properties

object SqlDelightDatabaseProvider {
    private val INSTANCE = LazyConstructor<AppDatabase>()

    init {
        INSTANCE.construct {
            if (!MLToolboxApp.dbDir.exists()) {
                if (!MLToolboxApp.dbDir.mkdirs())
                    error("Unable to create db directory")
            }
            val driver = JdbcSqliteDriver(
                "jdbc:sqlite:MLToolboxApp\\db\\app.db",
                Properties(),
                AppDatabase.Schema
            )
            AppDatabase(driver)
        }
    }

    fun get(): AppDatabase {
        return INSTANCE.value
    }
}