package com.sia.khuerydsl.builder

import com.sia.khuerydsl.HibernateQueryDsl
import com.sia.khuerydsl.expression.SetExpression
import kotlin.reflect.KProperty

@HibernateQueryDsl
class SetExpressionBuilder {
    val exprList: MutableList<SetExpression> = mutableListOf()

    infix fun KProperty<*>.eq(value: Any?): SetExpressionBuilder {
        value?.let { exprList.add(SetExpression(this, value)) }
        return this@SetExpressionBuilder
    }
}
