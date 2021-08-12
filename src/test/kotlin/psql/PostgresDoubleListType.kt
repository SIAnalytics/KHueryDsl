package psql

import org.hibernate.type.AbstractSingleColumnStandardBasicType

/**
 * PostgreSQL 의 Int 형 리스트 타입 정의 클래스
 */
class PostgresDoubleListType : AbstractSingleColumnStandardBasicType<List<*>>(
    PostgresDoubleListTypeDescriptor.INSTANCE,
    DoubleListTypeDescriptor.INSTANCE
) {

    companion object {
        val INSTANCE = PostgresDoubleListType()
    }

    override fun getName(): String = "double_list"

    override fun registerUnderJavaType(): Boolean = true
}
