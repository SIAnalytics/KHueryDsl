package psql

import org.hibernate.type.AbstractSingleColumnStandardBasicType

/**
 * PostgreSQL 의 Int 형 리스트 타입 정의 클래스
 */
class PostgresIntListType : AbstractSingleColumnStandardBasicType<List<*>>(
    PostgresIntListTypeDescriptor.INSTANCE,
    IntListTypeDescriptor.INSTANCE
) {

    companion object {
        val INSTANCE = PostgresIntListType()
    }

    override fun getName(): String = "int_list"

    override fun registerUnderJavaType(): Boolean = true
}
