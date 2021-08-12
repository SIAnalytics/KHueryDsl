package com.sia.khuerydsl.expression

import com.sia.khuerydsl.HibernateQueryDsl
import kotlin.reflect.KProperty

@HibernateQueryDsl
class SetExpression(
    private val field: KProperty<*>,
    val value: Any,
) {
    fun getParamName(): String = field.name.capitalize()
    fun toQueryString(): String = "${field.name} = :${getParamName()}"
}
