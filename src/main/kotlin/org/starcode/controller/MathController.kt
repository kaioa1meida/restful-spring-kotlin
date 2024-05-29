package org.starcode.controller

import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.starcode.exceptions.UnsupportedMathOperationException
import org.starcode.utils.StringToDoubleParser
import org.starcode.utils.Validation
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.sqrt

@RestController
class MathController(
    val validation: Validation,
    val parser: StringToDoubleParser
) {

    val counter: AtomicLong = AtomicLong()

    @RequestMapping(value = ["/addition/{numberOne}/{numberTwo}"])
    fun addition(
        @PathVariable(value = "numberOne") numberOne: String?,
        @PathVariable(value = "numberTwo") numberTwo: String?
    ): Double {
        if (!validation.isNumeric(numberOne) || !validation.isNumeric(numberTwo)) throw UnsupportedMathOperationException("Please set a numeric value!")
        return parser.convertToDouble(numberOne) + parser.convertToDouble(numberTwo)
    }

    @RequestMapping(value = ["/subtraction/{numberOne}/{numberTwo}"])
    fun subtraction(
        @PathVariable(value = "numberOne") numberOne: String?,
        @PathVariable(value = "numberTwo") numberTwo: String?
    ): Double {
        if (!validation.isNumeric(numberOne) || !validation.isNumeric(numberTwo)) throw UnsupportedMathOperationException("Please set a numeric value!")
        return parser.convertToDouble(numberOne) - parser.convertToDouble(numberTwo)
    }

    @RequestMapping(value = ["/multiplication/{numberOne}/{numberTwo}"])
    fun multiplication(
        @PathVariable(value = "numberOne") numberOne: String?,
        @PathVariable(value = "numberTwo") numberTwo: String?
    ): Double {
        if (!validation.isNumeric(numberOne) || !validation.isNumeric(numberTwo)) throw UnsupportedMathOperationException("Please set a numeric value!")
        return parser.convertToDouble(numberOne) * parser.convertToDouble(numberTwo)
    }

    @RequestMapping(value = ["/division/{numberOne}/{numberTwo}"])
    fun division(
        @PathVariable(value = "numberOne") numberOne: String?,
        @PathVariable(value = "numberTwo") numberTwo: String?
    ): Double {
        if (!validation.isNumeric(numberOne) || !validation.isNumeric(numberTwo)) throw UnsupportedMathOperationException("Please set a numeric value!")
        return parser.convertToDouble(numberOne) / parser.convertToDouble(numberTwo)
    }

    @RequestMapping(value = ["/average/{numberOne}/{numberTwo}"])
    fun average(
        @PathVariable(value = "numberOne") numberOne: String?,
        @PathVariable(value = "numberTwo") numberTwo: String?
    ): Double {
        if (!validation.isNumeric(numberOne) || !validation.isNumeric(numberTwo)) throw UnsupportedMathOperationException("Please set a numeric value!")
        val average = (parser.convertToDouble(numberOne) + parser.convertToDouble(numberTwo)) / 2
        return average
    }

    @RequestMapping(value = ["/sqrt/{numberOne}"])
    fun sqrt(
        @PathVariable(value = "numberOne") numberOne: String?
    ): Double {
        if (!validation.isNumeric(numberOne)) throw UnsupportedMathOperationException("Please set a numeric value!")
        val number = parser.convertToDouble(numberOne)
        return sqrt(number)
    }

}