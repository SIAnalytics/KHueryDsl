package psql

import org.hibernate.type.descriptor.WrapperOptions
import org.hibernate.type.descriptor.java.JavaTypeDescriptor
import org.hibernate.type.descriptor.sql.BasicExtractor
import org.hibernate.type.descriptor.sql.SqlTypeDescriptor
import java.sql.CallableStatement
import java.sql.ResultSet
import java.sql.SQLException

@Suppress("UNCHECKED_CAST")
internal class ArrayExtractor<J>(
    private val javaTypeDescriptor: JavaTypeDescriptor<J>,
    sqlDescriptor: SqlTypeDescriptor
) : BasicExtractor<J>(javaTypeDescriptor, sqlDescriptor) {

    @Throws(SQLException::class)
    override fun doExtract(rs: ResultSet, name: String, options: WrapperOptions): J {
        return javaTypeDescriptor.wrap(rs.getArray(name).array as Array<J>, options)
    }

    @Throws(SQLException::class)
    override fun doExtract(statement: CallableStatement, index: Int, options: WrapperOptions): J {
        return javaTypeDescriptor.wrap(statement.getArray(index).array as Array<J>, options)
    }

    @Throws(SQLException::class)
    override fun doExtract(statement: CallableStatement, name: String, options: WrapperOptions): J {
        return javaTypeDescriptor.wrap(statement.getArray(name).array as Array<J>, options)
    }
}
