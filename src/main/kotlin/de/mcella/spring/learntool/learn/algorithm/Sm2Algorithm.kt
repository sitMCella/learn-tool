package de.mcella.spring.learntool.learn.algorithm

import de.mcella.spring.learntool.learn.exceptions.InputValuesNotAcceptableException
import kotlin.math.ceil

const val MIN_EASE_FACTOR = 1.3f

object Sm2Algorithm {

    fun validate(inputValues: InputValues) {
        if (inputValues.quality < 0 || inputValues.quality > 5) throw InputValuesNotAcceptableException("The quality parameter value must be an integer value between 0 and 5")
    }

    fun evaluate(inputValues: InputValues): OutputValues {
        if (inputValues.quality >= 3) {
            val interval = when (inputValues.repetitions) {
                0 -> {
                    1
                }
                1 -> {
                    6
                }
                else -> {
                    ceil(inputValues.interval * inputValues.easeFactor).toInt()
                }
            }
            val easeFactor = inputValues.easeFactor.plus(0.1f.minus((5.minus(inputValues.quality)).times(0.08f.plus(((5.minus(inputValues.quality)).times(0.02f))))))
            if (easeFactor < MIN_EASE_FACTOR) {
                return OutputValues(interval, inputValues.repetitions.plus(1), MIN_EASE_FACTOR)
            }
            return OutputValues(interval, inputValues.repetitions.plus(1), easeFactor)
        } else {
            return OutputValues(1, 0, inputValues.easeFactor)
        }
    }
}
