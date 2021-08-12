package com.sia.khuerydsl.builder

import com.sia.khuerydsl.HibernateQueryDsl
import com.sia.khuerydsl.expression.BetweenExpression
import com.sia.khuerydsl.expression.LeftExpression
import com.sia.khuerydsl.expression.Operator
import com.sia.khuerydsl.expression.WhereExpression
import org.locationtech.jts.geom.Point
import kotlin.reflect.KProperty

@Suppress("FunctionName", "TooManyFunctions")
@HibernateQueryDsl
object WhereExpressionBuilder {

    infix fun WhereExpressionBuilder.expr(expression: WhereExpressionBuilder.() -> WhereExpression): WhereExpression {
        return WhereExpressionBuilder.expression()
    }

    fun fieldOf(vararg fields: KProperty<*>): LeftExpression {
        return LeftExpression(fields.toList())
    }

    fun LeftExpression.valueLike(keys: List<String>, value: String?): WhereExpression {
        return WhereExpression.HStore(this.fields, keys, Operator.Like(), value)
    }

    fun KProperty<*>.valueLike(keys: List<String>, value: String?): WhereExpression {
        return WhereExpression.HStore(listOf(this), keys, Operator.Like(), value)
    }

    infix fun WhereExpression.or(expressionFun: WhereExpressionBuilder.() -> WhereExpression): WhereExpression {
        val expression = WhereExpressionBuilder.expressionFun()
        return WhereExpression.Or(this, expression, true)
    }

    infix fun WhereExpression.or(expression: WhereExpression): WhereExpression {
        return WhereExpression.Or(this, expression, false)
    }

    infix fun WhereExpression.and(expressionFun: WhereExpressionBuilder.() -> WhereExpression): WhereExpression {
        val expression = WhereExpressionBuilder.expressionFun()
        return WhereExpression.And(this, expression, true)
    }

    infix fun WhereExpression.and(expression: WhereExpression): WhereExpression {
        return WhereExpression.And(this, expression, false)
    }

    infix fun KProperty<*>.eq(value: Any?): WhereExpression {
        return WhereExpression.Plain(listOf(this), Operator.Equal(), value)
    }

    infix fun LeftExpression.eq(value: Any?): WhereExpression {
        return WhereExpression.Plain(this.fields, Operator.Equal(), value)
    }

    infix fun LeftExpression.any(value: Any?): WhereExpression {
        return WhereExpression.InAny(this.fields, value)
    }

    infix fun KProperty<*>.any(value: Any?): WhereExpression {
        return WhereExpression.InAny(listOf(this), value)
    }

    infix fun KProperty<*>.neq(value: Any?): WhereExpression {
        return WhereExpression.Plain(listOf(this), Operator.NotEqual(), value)
    }

    infix fun LeftExpression.neq(value: Any?): WhereExpression {
        return WhereExpression.Plain(this.fields, Operator.NotEqual(), value)
    }

    infix fun KProperty<*>.like(value: String?): WhereExpression {
        return WhereExpression.Plain(listOf(this), Operator.Like(), value)
    }

    infix fun LeftExpression.like(value: String?): WhereExpression {
        return WhereExpression.Plain(this.fields, Operator.Like(), value)
    }

    infix fun KProperty<*>.inList(value: Collection<*>?): WhereExpression {
        return WhereExpression.InList(listOf(this), value)
    }

    infix fun LeftExpression.inList(value: Collection<*>?): WhereExpression {
        return WhereExpression.InList(this.fields, value)
    }

    infix fun KProperty<*>.between(value1: Any?): BetweenExpression {
        return BetweenExpression(listOf(this), value1)
    }

    infix fun LeftExpression.between(value1: Any?): BetweenExpression {
        return BetweenExpression(this.fields, value1)
    }

    infix fun BetweenExpression.and(value2: Any?): WhereExpression {
        return WhereExpression.Between(this.fields, this.value1, value2)
    }

    infix fun KProperty<*>.betweenRange(range: ClosedRange<*>?): WhereExpression {
        return WhereExpression.Between(listOf(this), range?.start, range?.endInclusive)
    }

    infix fun LeftExpression.betweenRange(range: ClosedRange<*>?): WhereExpression {
        return WhereExpression.Between(this.fields, range?.start, range?.endInclusive)
    }

    infix fun KProperty<*>.lessThan(value: Any?): WhereExpression {
        return WhereExpression.Plain(listOf(this), Operator.LessThan(), value)
    }

    infix fun LeftExpression.lessThan(value: Any?): WhereExpression {
        return WhereExpression.Plain(this.fields, Operator.LessThan(), value)
    }

    fun st_intersects(field: KProperty<*>, value: Any?): WhereExpression {
        return WhereExpression.Function("ST_Intersects", listOf(field), value, false)
    }

    fun st_dwithin(field: KProperty<*>, point: Point?, radius: Double?): WhereExpression {
        return WhereExpression.DWithinFunction(field, point, radius)
    }
}
