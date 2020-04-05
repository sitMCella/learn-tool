package de.mcella.spring.learntool.learn

import kotlin.math.ceil

object Sm2Algorithm {

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
            if (easeFactor < 1.3f) {
                return OutputValues(interval, inputValues.repetitions + 1, 1.3f)
            }
            return OutputValues(interval, inputValues.repetitions + 1, easeFactor)
        } else {
            return OutputValues(1, 0, inputValues.easeFactor)
        }
    }
}
