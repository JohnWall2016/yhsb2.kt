package yhsb.base.util

import com.typesafe.config.ConfigFactory
import com.typesafe.config.Config as TSConfig

object Config {
    fun load(prefix: String): TSConfig {
        val factory = ConfigFactory.load()
        return if (factory.hasPath(prefix)) {
            factory.getConfig(prefix)
        } else {
            ConfigFactory.empty()
        }
    }

    val cjbSession = load("cjb.session")

    val qbSession = load("qb.session")
}

fun TSConfig.toMap(): Map<String, Any> {
    return mutableMapOf<String, Any>().apply {
        entrySet().forEach {
            this[it.key] = it.value.unwrapped()
        }
    }
}