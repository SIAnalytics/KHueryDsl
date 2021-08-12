package com.sia.khuerydsl.builder

import com.sia.khuerydsl.HibernateQueryDsl
import com.sia.khuerydsl.QueryMetadata
import com.sia.khuerydsl.QueryWrapper
import com.sia.khuerydsl.exception.MissingSetExpressionException
import com.sia.khuerydsl.exception.MissingWhereExpressionException
import com.sia.khuerydsl.expression.WhereExpression
import com.sia.khuerydsl.expression.setParameters
import org.hibernate.Session
import javax.persistence.Entity
import javax.persistence.Query
import kotlin.reflect.KClass

@HibernateQueryDsl
class UpdateQueryBuilder<R : Any>(private val session: Session, private val updateType: KClass<R>) {

    private val metadata = QueryMetadata()

    fun set(setExprBuilderFun: SetExpressionBuilder.() -> SetExpressionBuilder): UpdateQueryBuilder<R> {
        metadata.setExprList = SetExpressionBuilder().setExprBuilderFun().exprList
        return this
    }

    fun where(expression: WhereExpressionBuilder.() -> WhereExpression): UpdateQueryBuilder<R> {
        metadata.whereExpr = WhereExpressionBuilder.expression()
        return this
    }

    fun getQueryString(): String {
        if (metadata.queryString == null) {
            metadata.queryString = buildString {
                val entityAnnotation = updateType.java.getAnnotation(Entity::class.java)
                val updateEntity = if (entityAnnotation.name.isBlank()) {
                    updateType.simpleName!!
                } else {
                    entityAnnotation.name
                }
                append("UPDATE $updateEntity SET ")
                metadata.setExprList.forEachIndexed { index, expr ->
                    if (index != 0) append(", ")
                    append(expr.toQueryString())
                }
                append(" WHERE ").append(metadata.whereExpr?.toQueryString(true))
            }
        }
        return metadata.queryString!!
    }

    fun build(): Query {
        if (metadata.whereExpr == null) throw MissingWhereExpressionException()
        if (metadata.setExprList.isEmpty()) throw MissingSetExpressionException()

        val queryWrapper = QueryWrapper<R>(null, session.createQuery(getQueryString()))

        for (setExpr in metadata.setExprList) {
            queryWrapper.setParameter(setExpr.getParamName(), setExpr.value)
        }

        metadata.whereExpr?.setParameters(queryWrapper, true)
        return queryWrapper.query!!
    }
}
