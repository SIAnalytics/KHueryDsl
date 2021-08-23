package psql

import org.hibernate.spatial.dialect.postgis.PostgisPG95Dialect

class CustomPostgisDialect : PostgisPG95Dialect() {
    init {
        registerColumnType(STRING_LIST, "character varying[]")
        registerHibernateType(STRING_LIST, PostgresStringListType.INSTANCE.name)

        registerColumnType(INT_LIST, "integer[]")
        registerHibernateType(INT_LIST, PostgresIntListType.INSTANCE.name)

        registerColumnType(UUID_LIST, "uuid[]")
        registerHibernateType(UUID_LIST, PostgresIntListType.INSTANCE.name)

        registerColumnType(TIMESTAMP_LIST, "timestamp[]")
        registerHibernateType(TIMESTAMP_LIST, PostgresTimestampListType.INSTANCE.name)

        registerColumnType(DOUBLE_LIST, "float8[]")
        registerHibernateType(DOUBLE_LIST, PostgresDoubleListType.INSTANCE.name)
    }
}
