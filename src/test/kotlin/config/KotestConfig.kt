package config

import io.kotest.core.config.AbstractProjectConfig
import io.mockk.every
import io.mockk.mockkObject
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

object KotestConfig : AbstractProjectConfig() {

    override fun beforeAll() {
        startKoin()
        mockConfigManager()
    }

    override fun afterAll() {
        stopKoin()
    }

    private fun startKoin() = startKoin {
        modules(
            module {
//                factory<KeepableEntityRepository> { KeepableEntityHibernateRepository() }
//                factory<KeepableEntityHibernateRepository> { (name: String) ->
//                    KeepableEntityHibernateRepository(name)
//                }
            }
        )
    }

    private fun mockConfigManager() {
        mockkObject(ConfigManager)
        every { ConfigManager.getConfigOf<TimeZoneConfig>() } returns TimeZoneConfig("Asia/Seoul")
    }

    fun restartKoin() {
        startKoin()
    }
}
