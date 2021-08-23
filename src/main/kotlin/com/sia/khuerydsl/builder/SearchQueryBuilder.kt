package com.sia.khuerydsl.builder

import com.sia.khuerydsl.HibernateQueryDsl
import com.sia.khuerydsl.QueryMetadata
import com.sia.khuerydsl.QueryWrapper
import com.sia.khuerydsl.camelToSnake
import com.sia.khuerydsl.expression.WhereExpression
import com.sia.khuerydsl.expression.setParameters
import org.hibernate.Session
import javax.persistence.Entity
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

enum class SortDirection {
    ASC,
    DESC
}

@HibernateQueryDsl
class SearchQueryBuilder<R : Any>(private val session: Session, private val resultType: KClass<R>) {

    private val metadata = QueryMetadata()

    @Deprecated("Use the from() method", replaceWith = ReplaceWith("from(kClass)"))
    fun <T : Any> selectFrom(kClass: KClass<T>): SearchQueryBuilder<R> {
        val entityAnnotation = kClass.java.getAnnotation(Entity::class.java)
        metadata.selectEntity = entityAnnotation.name.ifBlank {
            kClass.simpleName!!
        }
        metadata.alias = metadata.selectEntity!![0]
        return this
    }

    fun <T : Any> from(kClass: KClass<T>): SearchQueryBuilder<R> {
        val entityAnnotation = kClass.java.getAnnotation(Entity::class.java)
        metadata.selectEntity = entityAnnotation.name.ifBlank {
            kClass.simpleName!!
        }
        metadata.alias = metadata.selectEntity!![0]
        return this
    }

    fun select(vararg field: KCallable<*>): SearchQueryBuilder<R> {
        metadata.selectFields = field.toList()
        return this
    }

    fun distinct(): SearchQueryBuilder<R> {
        metadata.distinct = true
        return this
    }

    fun where(expression: WhereExpressionBuilder.() -> WhereExpression): SearchQueryBuilder<R> {
        metadata.whereExpr = WhereExpressionBuilder.expression()
        return this
    }

    fun <T> orderBy(field: KCallable<T>, direction: SortDirection = SortDirection.ASC): SearchQueryBuilder<R> {
        metadata.orderByExpr = "ORDER BY ${field.name} $direction"
        return this
    }

    fun limit(limit: Int?): SearchQueryBuilder<R> {
        metadata.limit = limit
        return this
    }

    fun offset(offset: Int?): SearchQueryBuilder<R> {
        metadata.offset = offset
        return this
    }

    fun groupBy(field: KProperty<*>) {
        metadata.groupByExpr = "GROUP BY ${metadata.alias}.${field.name}"
    }

    private fun isAggregateFunction(f: KCallable<*>) = AggregateFunction.values().any { v -> f.name.contains(v.name) }

    private fun buildHqlSelectClause(): String {
        return buildString {
            append("SELECT ")
            if (metadata.distinct) {
                append("DISTINCT ")
            }
            if (metadata.isAllFieldsNeeded()) {
                append("${metadata.alias} ")
            } else {
                metadata.selectFields.forEach { f ->
                    if (isAggregateFunction(f)) { // 집계 함수는 함수 안에서 필드 이름을 적절하게 변경하기 때문에 별도 처리 없이 필드 이름 추가
                        append("${f.name}, ")
                    } else {
                        append("${metadata.alias}.${f.name}, ")
                    }
                }
                deleteCharAt(lastIndex - 1) // 마지막 ',' 제거
            }
            append("FROM ${metadata.selectEntity} ${metadata.alias}")
        }
    }

    private fun buildNativeSelectClause(): String {
        return buildString {
            append("SELECT ")
            if (metadata.distinct) { append("DISTINCT ") }
            if (metadata.isAllFieldsNeeded()) { append("* ") } else {
                metadata.selectFields.forEach { f -> append("${f.name}, ") }
                deleteCharAt(lastIndex - 1) // 마지막 ',' 제거
            }
            append("FROM ${metadata.selectEntity!!.camelToSnake()}")
        }
    }

    fun getQueryString(): String {
        if (metadata.queryString == null) {
            metadata.queryString = buildString {
                if (metadata.isHql()) {
                    if (metadata.isFromClause()) {
                        append("FROM ${metadata.selectEntity}") // build FROM clause
                    } else {
                        append(buildHqlSelectClause())
                    }
                } else { append(buildNativeSelectClause()) }
                metadata.whereExpr.let {
                    val whereString = it?.toQueryString(metadata.isHql())
                    if (whereString.isNullOrBlank()) "" else append(" WHERE ").append(whereString)
                }
                metadata.groupByExpr?.let { appendSpace().append(it) }
                metadata.orderByExpr?.let { appendSpace().append(it) }
            }
        }
        return metadata.queryString!!
    }

    fun build(): QueryWrapper<R> {
        val query = if (metadata.isHql()) {
            if (metadata.isFromClause()) {
                QueryWrapper<R>(session.createQuery(getQueryString(), resultType.java), null)
            } else {
                QueryWrapper(null, session.createQuery(getQueryString()))
            }
        } else {
            QueryWrapper(null, session.createNativeQuery(getQueryString(), resultType.java))
        }

        metadata.whereExpr?.setParameters(query, metadata.isHql())
        metadata.limit?.let { query.setMaxResults(it) }
        metadata.offset?.let { query.setFirstResults(it) }
        return query
    }
}

fun StringBuilder.appendSpace(): StringBuilder = this.append(' ')
