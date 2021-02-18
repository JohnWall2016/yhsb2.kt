package yhsb.base.db

import org.ktorm.database.Database
import org.ktorm.logging.ConsoleLogger
import org.ktorm.logging.LogLevel
import yhsb.base.util.Config
import yhsb.base.util.toMap

open class DbSession(private val configPrefix: String) {
    fun getConnection(): Database {
        val cfg = Config.load("$configPrefix.dataSource").toMap()
        return Database.connect(
            cfg["url"].toString(),
            user = cfg["username"].toString(),
            password = cfg["password"].toString(),
            driver = cfg["driverClassName"].toString(),
            logger = ConsoleLogger(threshold = LogLevel.valueOf(cfg["logLevel"].toString()))
        )
    }

    fun <T> use(func: Database.() -> T): T {
        return getConnection().func()
    }
}