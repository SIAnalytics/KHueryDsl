package config

import testentity.SubEntity
import testentity.TestEntity
import hibernate.HibernateConfig
import hibernate.HibernateManager
import hibernate.RepositoryConfig
import io.kotest.core.config.AbstractProjectConfig
import org.testcontainers.containers.PostgisContainerProvider
import testentity.TimestampEntity
import java.sql.DriverManager

object PsqlTestContainerConfig : AbstractProjectConfig() {
    private val db = PostgisContainerProvider().newInstance()

    override fun beforeAll() {
        db.start()

        val repositoryConfig = RepositoryConfig(
            db.containerIpAddress,
            db.firstMappedPort,
            db.databaseName,
            db.username,
            db.password
        )

        val hibernateConfig = HibernateConfig(
            driverClass = db.driverClassName,
            dialect = "psql.CustomPostgisDialect",
            isShowSql = false,
            isFormatSql = false,
            ddlAuto = "update"
        )

        val connection = DriverManager.getConnection(
            "jdbc:postgresql://${db.containerIpAddress}:${db.firstMappedPort}/${db.databaseName}",
            db.username,
            db.password
        )
        val statement = connection.createStatement()
        val enumCreateQueryList = listOf(
            "create type permissionlevel as enum ('ANNOTATOR', 'VALIDATOR', 'ADMIN')",
            "create type snapshottype as enum ('TRAINING', 'VALIDATION')",
            "create type labeltype as enum ('DET', 'UNVIEW', 'TP', 'FP', 'FP_LOC', 'FP_CLS', 'FN')",
            "create type deploystatus as enum ('STOP', 'TRAINING', 'INFERENCE')",
            "create extension HSTORE"
        )
        for (query in enumCreateQueryList) {
            statement.execute(query)
        }
        statement.close()
        connection.close()

        HibernateManager.setConfig(repositoryConfig, hibernateConfig)
        HibernateManager.setReconnectionOption(false)
        HibernateManager.registerAnnotatedClass(TestEntity::class)
        HibernateManager.registerAnnotatedClass(SubEntity::class)
        HibernateManager.registerAnnotatedClass(TimestampEntity::class)
        HibernateManager.initSessionFactory()
    }

    override fun afterAll() {
        db.stop()
    }
}
