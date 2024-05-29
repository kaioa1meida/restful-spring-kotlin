package org.starcode.utils

import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service

@Component
class Validation {

    fun isNumeric(number: String?): Boolean {
        if (number.isNullOrBlank()) return false
        val number = number.replace(",".toRegex(), ".")
        return number.matches("""[-+]?[0-9]*\.?[0-9]+""".toRegex())
    }
}