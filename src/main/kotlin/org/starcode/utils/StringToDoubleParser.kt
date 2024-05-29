package org.starcode.utils

import org.springframework.stereotype.Component

@Component
class StringToDoubleParser(
    val validation: Validation
) {
    fun convertToDouble(number: String?): Double {
        if (number.isNullOrBlank()) return 0.0
        val number = number.replace(",".toRegex(), ".")
        return if (validation.isNumeric(number)) number.toDouble() else 0.0
    }
}