package psql

import org.hibernate.type.descriptor.WrapperOptions
import org.hibernate.type.descriptor.java.AbstractTypeDescriptor

internal class DoubleListTypeDescriptor : AbstractTypeDescriptor<List<*>>(List::class.java) {

    companion object {
        val INSTANCE = DoubleListTypeDescriptor()
    }

    override fun fromString(string: String): List<*> = string
        .replace("[\\[\\]]".toRegex(), "")
        .split(',')
        .map { it.toDouble() }

    @Suppress("UNCHECKED_CAST")
    override fun <X> unwrap(value: List<*>, type: Class<X>, options: WrapperOptions): X {
        return value as X
    }

    @Suppress("UNCHECKED_CAST")
    override fun <X> wrap(value: X, options: WrapperOptions): List<*>? {
        return (value as Array<Double>).toList()
    }
}
