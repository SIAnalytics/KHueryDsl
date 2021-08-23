package com.sia.khuerydsl.expression

import com.sia.khuerydsl.HibernateQueryDsl
import com.sia.khuerydsl.QueryWrapper
import com.sia.khuerydsl.camelToSnake
import org.locationtech.jts.geom.Point
import java.util.*
import javax.persistence.Column
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.javaField

@HibernateQueryDsl
sealed class Operator(val text: String) {
    class Equal : Operator(" = ")
    class NotEqual : Operator(" != ")
    class Like : Operator(" LIKE ")
    class LessThan : Operator(" <= ")
}

@HibernateQueryDsl
sealed class WhereExpression {
    abstract fun toQueryString(hql: Boolean): String?
    abstract val hql: Boolean
    val ESPG_BESSEL_TM_SRID = 2097

    class Plain(
        private val fieldList: List<KProperty<*>>,
        private val operator: Operator,
        val value: Any?,
        override val hql: Boolean = true
    ) : WhereExpression() {
        fun getParamName(): String = fieldList.fold("") { acc, field -> acc + field.name.capitalize() }

        override fun toQueryString(hql: Boolean): String? = value?.let {
            "${getColumnName(fieldList, hql)}${operator.text}:${getParamName()}"
        }
    }

    class HStore(
        private val fieldList: List<KProperty<*>>,
        private val keys: List<String>,
        private val op: Operator,
        val value: String?,
        override val hql: Boolean = false
    ) : WhereExpression() {
        fun getParamName(): String = fieldList.fold("") { acc, field -> acc + field.name.capitalize() }

        override fun toQueryString(hql: Boolean): String? = value?.let {
            buildString {
                append('(')
                for ((keyIdx, key) in keys.withIndex()) {
                    for ((fieldIdx, field) in fieldList.withIndex()) {
                        if (fieldIdx != 0) append('.')
                        if (hql) {
                            append("${field.name}->\'$key\'")
                        } else {
                            append("${field.name}->\'$key\'".camelToSnake())
                        }
                    }
                    append("${op.text}:${getParamName()}")
                    if (keyIdx < keys.size - 1) {
                        append(" OR ")
                    }
                }
                append(')')
            }
        }
    }

    class InList(
        private val fieldList: List<KProperty<*>>,
        val value: Collection<*>?,
        override val hql: Boolean = true
    ) : WhereExpression() {
        fun getParamName(): String = fieldList.fold("") { acc, field -> acc + field.name.capitalize() }

        override fun toQueryString(hql: Boolean): String? = if (value != null && value.isNotEmpty()) {
            buildString {
                append("${getColumnName(fieldList, hql)} IN ")
                if (hql) {
                    append(":" + getParamName())
                } else {
                    append("('${value.joinToString("','")}')")
                }
            }
        } else {
            null
        }
    }

    open class And(val expr1: WhereExpression, val expr2: WhereExpression, val bracket: Boolean) : WhereExpression() {
        private val className = this::class.simpleName
        override val hql
            get() = expr1.hql && expr2.hql

        override fun toQueryString(hql: Boolean): String? {
            val expr1Hql = expr1.toQueryString(hql)
            var expr2Hql = expr2.toQueryString(hql)
            if (bracket && expr2Hql != null) {
                expr2Hql = "($expr2Hql)"
            }
            return if (expr1Hql != null && expr2Hql != null) {
                "$expr1Hql ${className!!.toUpperCase()} $expr2Hql"
            } else if (expr1Hql == null) {
                expr2Hql
            } else if (expr2Hql == null) {
                expr1Hql
            } else {
                null
            }
        }
    }

    class Or(expr1: WhereExpression, expr2: WhereExpression, bracket: Boolean) : And(expr1, expr2, bracket)

    class Function(
        private val name: String,
        private val fieldList: List<KProperty<*>>,
        val value: Any?,
        override val hql: Boolean = true
    ) : WhereExpression() {
        fun getParamName(): String = fieldList.fold("") { acc, field -> acc + field.name.capitalize() }

        override fun toQueryString(hql: Boolean): String? = value?.let {
            "$name(${getColumnName(fieldList, hql)}, :${getParamName()})"
        }
    }

    class Between(
        private val fieldList: List<KProperty<*>>,
        val value1: Any?,
        val value2: Any?,
        override val hql: Boolean = true
    ) : WhereExpression() {
        fun getParamName1(): String = fieldList.fold("") { acc, field -> acc + field.name.capitalize() } + "1"
        fun getParamName2(): String = fieldList.fold("") { acc, field -> acc + field.name.capitalize() } + "2"

        override fun toQueryString(hql: Boolean): String? = buildString {
            if (value1 == null || value2 == null) {
                return null
            } else {
                return "${getColumnName(fieldList, hql)} BETWEEN :${getParamName1()} AND :${getParamName2()}"
            }
        }
    }

    class InAny(
        private val fieldList: List<KProperty<*>>,
        val value: Any?,
        override val hql: Boolean = false
    ) : WhereExpression() {
        fun getParamName(): String = fieldList.fold("") { acc, field -> acc + field.name.capitalize() }

        override fun toQueryString(hql: Boolean): String? = value?.let {
            fieldList.joinToString(".") {
                if (hql) {
                    ":${getParamName()} = ANY($it)"
                } else {
                    ":${getParamName()} = ANY(${getNativeQueryColumnName(it)})"
                }
            }
        }
    }

    class DWithinFunction(
        private val field: KProperty<*>,
        val value1: Point?,
        val value2: Double?
    ) : WhereExpression() {
        override val hql: Boolean = false
        fun getParamName1(): String = "Point"
        fun getParamName2(): String = "Radius"

        override fun toQueryString(hql: Boolean): String? = buildString {
            if (value1 == null || value2 == null) {
                return null
            } else {
                val columnName = getNativeQueryColumnName(field)
                append("ST_DWithin(ST_Transform(cast($columnName AS geometry), $ESPG_BESSEL_TM_SRID)")
                append(", ST_Transform(:${getParamName1()}, $ESPG_BESSEL_TM_SRID), :${getParamName2()})")
            }
        }
    }
}

private fun getColumnName(fieldList: List<KProperty<*>>, hql: Boolean): String {
    return fieldList.joinToString(".") {
        if (hql) {
            it.name
        } else {
            getNativeQueryColumnName(it)
        }
    }
}

private fun getNativeQueryColumnName(it: KProperty<*>): String {
    val column = it.javaField!!.getAnnotation(Column::class.java)
    return if (column != null && column.name.isNotBlank()) {
        column.name
    } else {
        it.name.camelToSnake()
    }
}

@SuppressWarnings("ComplexMethod")
fun <T : Any> WhereExpression.setParameters(queryWrapper: QueryWrapper<T>, isHql: Boolean) {
    val queue = LinkedList(mutableListOf(this))
    while (!queue.isEmpty()) {
        when (val expr = queue.pop()) {
            is WhereExpression.And -> queue.addAll(listOf(expr.expr1, expr.expr2))
            is WhereExpression.Or -> queue.addAll(listOf(expr.expr1, expr.expr2))
            is WhereExpression.Plain -> expr.value?.let { queryWrapper.setParameter(expr.getParamName(), expr.value) }
            is WhereExpression.HStore -> expr.value?.let { queryWrapper.setParameter(expr.getParamName(), expr.value) }
            is WhereExpression.InList -> if (expr.value != null && expr.value.isNotEmpty() && isHql) {
                queryWrapper.setParameter(expr.getParamName(), expr.value)
            }
            is WhereExpression.Function -> expr.value?.let {
                queryWrapper.setParameter(expr.getParamName(), expr.value)
            }
            is WhereExpression.InAny -> expr.value?.let { queryWrapper.setParameter(expr.getParamName(), expr.value) }
            is WhereExpression.Between ->
                if (expr.value1 == null || expr.value2 == null) {
                    continue
                } else {
                    queryWrapper.setParameter(expr.getParamName1(), expr.value1)
                        .setParameter(expr.getParamName2(), expr.value2)
                }
            is WhereExpression.DWithinFunction ->
                if (expr.value1 == null || expr.value2 == null) {
                    continue
                } else {
                    queryWrapper.setParameter(expr.getParamName1(), expr.value1)
                        .setParameter(expr.getParamName2(), expr.value2)
                }
        }
    }
}
