package com.sia.khuerydsl

fun String.camelToSnake(): String {
    val s = this
    return buildString {
        s.forEachIndexed { index, c ->
            if (c.isUpperCase() && index != 0) {
                append("_${c.lowercaseChar()}")
            } else {
                append(c.lowercaseChar())
            }
        }
    }
}