package com.sia.khuerydsl.builder

import kotlin.jvm.internal.CallableReference
import kotlin.reflect.*

enum class AggregateFunction {
    COUNT, SUM, AVG, MAX, MIN
}

object SelectExpressionBuilder {

    fun KClass<*>.count(): KCallable<*> {
        return getKCallable(null, "COUNT")
    }

    fun KProperty<*>.count(): KCallable<*> {
        return getKCallable(this, "COUNT")
    }

    fun KProperty<*>.sum(): KCallable<*> {
        return getKCallable(this, "SUM")
    }

    fun KProperty<*>.avg(): KCallable<*> {
        return getKCallable(this, "AVG")
    }

    fun KProperty<*>.max(): KCallable<*> {
        return getKCallable(this, "MAX")
    }

    fun KProperty<*>.min(): KCallable<*> {
        return getKCallable(this, "MIN")
    }

    private fun getKCallable(field: KProperty<*>?, functionName: String): KCallable<*> {
        val kCallable = object : CallableReference() {
            override var name: String = ""
            override fun computeReflected(): KCallable<*> { throw UnsupportedOperationException() }
        }

        if (field != null) {
            val alias = field.toString().find { c -> c.isUpperCase() }
            kCallable.name = "$functionName($alias.${field.name})"
        } else {
            kCallable.name = "$functionName(*)"
        }
        return kCallable
    }

}
