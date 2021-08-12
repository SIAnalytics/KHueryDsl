package psql

import org.hibernate.type.AbstractSingleColumnStandardBasicType

/**
 * PostgreSQL 의 Timestamp 형 리스트 타입 정의 클래스
 */
class PostgresTimestampListType : AbstractSingleColumnStandardBasicType<List<*>>(
    PostgresTimestampListTypeDescriptor.INSTANCE,
    TimestampListTypeDescriptor.INSTANCE
) {
    companion object {
        val INSTANCE = PostgresTimestampListType()
    }

    override fun getName(): String = "timestamp_list"

    override fun registerUnderJavaType(): Boolean = true
}
