package com.sia.khuerydsl.expression

import com.sia.khuerydsl.HibernateQueryDsl
import kotlin.reflect.KProperty

@HibernateQueryDsl
class BetweenExpression(val fields: List<KProperty<*>>, val value1: Any?)
