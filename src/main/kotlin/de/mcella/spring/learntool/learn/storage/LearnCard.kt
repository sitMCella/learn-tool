package de.mcella.spring.learntool.learn.storage

import de.mcella.spring.learntool.learn.algorithm.MIN_EASE_FACTOR
import de.mcella.spring.learntool.learn.algorithm.OutputValues
import java.time.Duration
import java.time.Instant
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

@Entity
@Table(name = "learn_cards")
data class LearnCard(
    @Id val id: String = "",
    @field:NotNull @field:NotEmpty val workspaceName: String = "",
    @field:NotNull val lastReview: Instant = Instant.now(),
    @field:NotNull val nextReview: Instant,
    @field:NotNull val repetitions: Int,
    @field:NotNull val easeFactor: Float,
    @field:NotNull val intervalDays: Int
) {
    companion object {
        fun createInitial(cardId: String, workspaceName: String, reviewTime: Instant): LearnCard {
            return LearnCard(cardId, workspaceName, reviewTime, reviewTime, 0, MIN_EASE_FACTOR, 0)
        }

        fun create(cardId: String, workspaceName: String, outputValues: OutputValues, reviewTime: Instant): LearnCard {
            val intervalDays = outputValues.interval.toLong()
            val nextReview = reviewTime.plus(Duration.ofDays(intervalDays))
            return LearnCard(cardId, workspaceName, reviewTime, nextReview, outputValues.repetitions, outputValues.easeFactor, outputValues.interval)
        }
    }
}
