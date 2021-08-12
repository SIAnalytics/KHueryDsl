package com.sia.khuerydsl

import com.sia.khuerydsl.exception.SameNamedParameterException
import javax.persistence.TypedQuery
import javax.persistence.Query

class QueryWrapper<R : Any>(private val typedQuery: TypedQuery<R>?, val query: Query?) {
    @Suppress("UNCHECKED_CAST")
    val resultList: List<R>
        get() = typedQuery?.resultList ?: query!!.resultList as List<R>

    fun setMaxResults(maxResult: Int): QueryWrapper<R> {
        typedQuery?.setMaxResults(maxResult) ?: query!!.setMaxResults(maxResult)
        return this
    }

    fun setFirstResults(firstResult: Int): QueryWrapper<R> {
        typedQuery?.setFirstResult(firstResult) ?: query!!.setFirstResult(firstResult)
        return this
    }

    fun setParameter(paramName: String, value: Any?): QueryWrapper<R> {
        val query = typedQuery ?: query
        val parameter = query!!.getParameter(paramName)
        if (query.isBound(parameter)) {
            throw SameNamedParameterException(
                "using two or more same named parameter, " +
                    "the last one overwrite every named parameter which appear before it."
            )
        }
        query.setParameter(paramName, value)
        return this
    }
}
