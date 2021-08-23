package config

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigRenderOptions
import io.github.config4k.ClassContainer
import io.github.config4k.TypeReference
import io.github.config4k.extract
import io.github.config4k.readers.SelectReader
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.reflect.KClass

const val CONFIG_FILE_PATH = "config/application.conf"
const val CAMEL_REGEX_STRING = "([a-z])([A-Z]+)"
const val REPLACE_CONFIG_STRING = "$1-$2"
const val CONFIG_STRING = "Config"

/**
 * Configuration 파일을 관리하고 설정 값을 쉽게 가져오도록 돕는 클래스.
 */
object ConfigManager {
    private var configRoot: Config = ConfigFactory.parseFile(File(CONFIG_FILE_PATH)).resolve()

    fun getPathFrom(kClass: KClass<out Config>): String =
        kClass.simpleName!!.substringBeforeLast(CONFIG_STRING)
            .replace(
                Regex(CAMEL_REGEX_STRING),
                REPLACE_CONFIG_STRING
            )
            .toLowerCase()

    class ConfigExtractor<T>(private val kClass: KClass<*>, private val path: String) {
        @Suppress("UNCHECKED_CAST")
        fun getConfig(configRoot: Config): Any {
            val config = configRoot.getConfig(path)!!

            val genericType = object : TypeReference<T>() {}.genericType()

            return SelectReader.extractWithoutPath(ClassContainer(kClass, genericType), config)
        }
    }

    private val configMap = mutableMapOf<KClass<*>, ConfigExtractor<*>>()

    fun <T : Config> register(kClass: KClass<out Config>, path: String) {
        configMap[kClass] =
            ConfigExtractor<T>(kClass, path)
    }

    inline fun <reified T : Config> register(path: String) = register<T>(T::class, path)

    fun <T : Config> register(kClass: KClass<out Config>) {
        val path = getPathFrom(kClass)
        configMap[kClass] =
            ConfigExtractor<T>(kClass, path)
    }

    inline fun <reified T : Config> register() = register<T>(T::class)

    @Suppress("UNCHECKED_CAST")
    fun <T> getConfigOf(kClass: KClass<*>): T = configMap[kClass]!!.getConfig(configRoot) as T

    inline fun <reified T> getConfigOf(): T = getConfigOf(T::class)

    fun getConfigFromPath(path: String) = configRoot.getConfig(path)!!

    inline fun <reified T : Config> getConfig(path: String): T = getConfigFromPath(path).extract()

    fun isConfigEqual(configStr: String, path: String): Boolean {
        val updatedConfig = ConfigFactory.parseString(configStr)
        val originalConfig = configRoot.getConfig(path)
        return originalConfig == updatedConfig
    }

    fun updateConfig(updatedConfigStr: String, path: String) {
        val updatedConfig = ConfigFactory.parseString(updatedConfigStr)
        configRoot = updatedConfig.atPath(path).withFallback(configRoot)
        updateConfigFile()
    }

    fun updateConfig(updatedConfig: Config) {
        configRoot = updatedConfig.withFallback(configRoot)
        updateConfigFile()
    }

    private fun updateConfigFile() {
        val options = ConfigRenderOptions.concise()
            .setFormatted(true)
            .setJson(false)
            .setComments(true)
            .setOriginComments(false)
        Files.writeString(Path.of(CONFIG_FILE_PATH), configRoot.root().render(options))
    }

    fun getJsonString(path: String): String {
        val options = ConfigRenderOptions.concise().setFormatted(true).setJson(true)
        return configRoot.getConfig(path).root().render(options)
    }
}
