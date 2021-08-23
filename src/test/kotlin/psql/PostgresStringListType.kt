package psql

import org.hibernate.type.AbstractSingleColumnStandardBasicType

/**
 * PostgreSQL 의 String 형 리스트 타입 정의 클래스
 */
class PostgresStringListType : AbstractSingleColumnStandardBasicType<List<*>>(
    PostgresStringListTypeDescriptor.INSTANCE,
    StringListTypeDescriptor.INSTANCE
) {
    companion object {
        val INSTANCE = PostgresStringListType()
    }

    override fun getName(): String = "string_list"

    override fun registerUnderJavaType(): Boolean = true
}
