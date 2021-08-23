package com.sia.khuerydsl.expression

import com.sia.khuerydsl.HibernateQueryDsl
import kotlin.reflect.KProperty

@HibernateQueryDsl
class LeftExpression(val fields: List<KProperty<*>>)
