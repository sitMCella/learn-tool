package de.mcella.spring.learntool.learn.algorithm

import de.mcella.spring.learntool.learn.dto.EvaluationParameters
import de.mcella.spring.learntool.learn.storage.LearnCardEntity

data class InputValues(
    val quality: Int,
    val repetitions: Int,
    val easeFactor: Float,
    val interval: Int
) {
    companion object {
        fun create(evaluationParameters: EvaluationParameters, learnCard: LearnCardEntity): InputValues {
            return InputValues(
                evaluationParameters.quality,
                learnCard.repetitions,
                learnCard.easeFactor,
                learnCard.intervalDays
            )
        }
    }
}
