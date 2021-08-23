package com.sia.khuerydsl

import com.sia.khuerydsl.expression.SetExpression
import com.sia.khuerydsl.expression.WhereExpression
import kotlin.reflect.KCallable

data class QueryMetadata(
    var selectEntity: String? = null,
    var selectFields: List<KCallable<*>> = listOf(),
    var alias: Char? = null,
    var orderByExpr: String? = null,
    var groupByExpr: String? = null,
    var whereExpr: WhereExpression? = null,
    var setExprList: List<SetExpression> = listOf(),
    var queryString: String? = null,
    var limit: Int? = null,
    var offset: Int? = null,
    var distinct: Boolean = false,
) {
    fun isHql(): Boolean {
        return whereExpr?.hql ?: true
    }

    fun isFromClause(): Boolean {
        return isAllFieldsNeeded() && !distinct // DISTINCT는 from clause에서 사용할 수 없음
    }

    fun isAllFieldsNeeded(): Boolean {
        return selectFields.isEmpty()
    }
}
