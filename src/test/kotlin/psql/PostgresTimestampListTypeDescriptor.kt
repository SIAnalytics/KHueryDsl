package psql

import org.hibernate.type.descriptor.ValueBinder
import org.hibernate.type.descriptor.ValueExtractor
import org.hibernate.type.descriptor.WrapperOptions
import org.hibernate.type.descriptor.java.JavaTypeDescriptor
import org.hibernate.type.descriptor.sql.BasicBinder
import org.hibernate.type.descriptor.sql.SqlTypeDescriptor
import java.sql.CallableStatement
import java.sql.PreparedStatement
import java.sql.SQLException
import kotlin.jvm.Throws

private const val POSTGRES_TIMESTAMP_TYPE = "timestamp"

internal class PostgresTimestampListTypeDescriptor : SqlTypeDescriptor {
    companion object {
        val INSTANCE =
            PostgresTimestampListTypeDescriptor()
    }

    override fun getSqlType(): Int = TIMESTAMP_LIST

    override fun canBeRemapped(): Boolean = true

    @Suppress("UNCHECKED_CAST")
    override fun <X> getBinder(javaTypeDescriptor: JavaTypeDescriptor<X>): ValueBinder<X> {
        return object : BasicBinder<X>(javaTypeDescriptor, this) {
            @Throws(SQLException::class)
            override fun doBind(st: PreparedStatement, value: X, index: Int, options: WrapperOptions) {
                val elements = javaTypeDescriptor.unwrap(value, List::class.java, options)
                val array = st.connection.createArrayOf(POSTGRES_TIMESTAMP_TYPE, elements.toTypedArray())
                st.setArray(index, array)
            }

            @Throws(SQLException::class)
            override fun doBind(st: CallableStatement, value: X, name: String, options: WrapperOptions) {
                throw UnsupportedOperationException("Binding by name is not supported!")
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <X> getExtractor(javaTypeDescriptor: JavaTypeDescriptor<X>): ValueExtractor<X> {
        return ArrayExtractor(
            javaTypeDescriptor,
            this
        )
    }
}
