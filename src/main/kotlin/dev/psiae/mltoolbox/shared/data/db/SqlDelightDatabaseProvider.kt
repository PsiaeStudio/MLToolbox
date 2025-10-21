package dev.psiae.mltoolbox.shared.data.db

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import dev.psiae.mltoolbox.data.Database
import dev.psiae.mltoolbox.shared.app.MLToolboxApp
import dev.psiae.mltoolbox.shared.utils.LazyConstructor
import java.util.Properties

object SqlDelightDatabaseProvider {
    private val INSTANCE = LazyConstructor<Database>()

    init {
        INSTANCE.construct {
            if (!MLToolboxApp.dbDir.exists()) {
                if (!MLToolboxApp.dbDir.mkdirs())
                    error("Unable to create db directory")
            }
            val driver = JdbcSqliteDriver(
                "jdbc:sqlite:MLToolboxApp\\db\\app.db",
                Properties(),
                Database.Schema
            )
            Database(driver)
        }
    }

    fun get(): Database {
        return INSTANCE.value
    }
}