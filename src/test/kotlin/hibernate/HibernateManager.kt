package hibernate

import com.vladmihalcea.hibernate.type.util.CamelCaseToSnakeCaseNamingStrategy
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.hibernate.cfg.Configuration
import org.hibernate.cfg.Environment
import org.hibernate.exception.JDBCConnectionException
import org.hibernate.service.spi.ServiceException
import java.util.*
import kotlin.reflect.KClass

private const val RECONNECTION_DELAY_TIME_MS = 3000L
private const val CONNECTION_POOL = 100

object HibernateManager {
    private var reconnection = true
    private val configuration: Configuration = Configuration().apply {
        setPhysicalNamingStrategy(CamelCaseToSnakeCaseNamingStrategy.INSTANCE)
    }

    private lateinit var sessionFactory: SessionFactory

    fun setConfig(repositoryConfig: RepositoryConfig, hibernateConfig: HibernateConfig): HibernateManager {
        configuration.apply {
            properties[Environment.URL] = repositoryConfig.url
            properties[Environment.USER] = repositoryConfig.userName
            properties[Environment.PASS] = repositoryConfig.password
            properties["hibernate.connection.driver_class"] = hibernateConfig.driverClass
            properties["hibernate.dialect"] = hibernateConfig.dialect
            properties["hibernate.show_sql"] = hibernateConfig.isShowSql
            properties["hibernate.format_sql"] = hibernateConfig.isFormatSql
            properties["hibernate.hbm2ddl.auto"] = hibernateConfig.ddlAuto
            properties["hibernate.connection.pool_size"] = CONNECTION_POOL
        }
        return this
    }

    fun setReconnectionOption(flag: Boolean) {
        reconnection = flag
    }

    fun registerAnnotatedClass(kClass: KClass<out Any>): HibernateManager {
        configuration.addAnnotatedClass(kClass.java)
        return this
    }

    fun registerAnnotatedClasses(kClasses: Iterable<KClass<out Any>>): HibernateManager {
        kClasses.forEach { kClass -> registerAnnotatedClass(kClass) }
        return this
    }

    fun getSession(): Session {
        return try {
            sessionFactory.withOptions()
                .jdbcTimeZone(TimeZone.getTimeZone("Etc/UTC"))
                .openSession()
        } catch (e: UninitializedPropertyAccessException) {
            initSessionFactory()
            getSession()
        }
    }

    fun initSessionFactory() {
        if (!HibernateManager::sessionFactory.isInitialized) {
            sessionFactory = try {
                configuration.buildSessionFactory()
            } catch (e: ServiceException) {
                if (e.cause is JDBCConnectionException && reconnection) {
                    retryDbConnection()
                } else {
                    null
                }
            } as SessionFactory
        }
    }

    fun closeSessionFactory(): Boolean {
        return if (!sessionFactory.isClosed) {
            sessionFactory.close()
            true
        } else false
    }

    private fun retryDbConnection() {
        var tryAgain = true
        do {
            try {
                sessionFactory = configuration.buildSessionFactory()
                tryAgain = false
            } catch (e: ServiceException) {
                Thread.sleep(RECONNECTION_DELAY_TIME_MS)
            }
        } while (tryAgain)
    }
}
