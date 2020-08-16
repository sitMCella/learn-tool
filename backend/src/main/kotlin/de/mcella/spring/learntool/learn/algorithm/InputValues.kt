package de.mcella.spring.learntool.learn.algorithm

import de.mcella.spring.learntool.learn.EvaluationParameters
import de.mcella.spring.learntool.learn.storage.LearnCard

data class InputValues(
    val quality: Int,
    val repetitions: Int,
    val easeFactor: Float,
    val interval: Int
) {
    companion object {
        fun create(evaluationParameters: EvaluationParameters, learnCard: LearnCard): InputValues {
            return InputValues(
                evaluationParameters.quality,
                learnCard.repetitions,
                learnCard.easeFactor,
                learnCard.intervalDays
            )
        }
    }
}
