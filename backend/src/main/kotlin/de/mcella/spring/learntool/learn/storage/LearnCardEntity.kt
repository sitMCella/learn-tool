package de.mcella.spring.learntool.learn.storage

import de.mcella.spring.learntool.card.dto.CardId
import de.mcella.spring.learntool.learn.algorithm.MIN_EASE_FACTOR
import de.mcella.spring.learntool.learn.algorithm.OutputValues
import de.mcella.spring.learntool.learn.dto.LearnCard
import de.mcella.spring.learntool.workspace.dto.WorkspaceRequest
import java.time.Duration
import java.time.Instant
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

@Entity
@Table(name = "learn_cards")
data class LearnCardEntity(
    @Id val id: String = "",
    @field:NotNull @field:NotEmpty val workspaceId: String = "",
    @field:NotNull val lastReview: Instant = Instant.now(),
    @field:NotNull val nextReview: Instant,
    @field:NotNull val repetitions: Int,
    @field:NotNull val easeFactor: Float,
    @field:NotNull val intervalDays: Int
) {
    companion object {
        fun createInitial(cardId: CardId, workspaceRequest: WorkspaceRequest, reviewTime: Instant): LearnCardEntity {
            return LearnCardEntity(cardId.id, workspaceRequest.id, reviewTime, reviewTime, 0, MIN_EASE_FACTOR, 0)
        }

        fun create(cardId: CardId, workspaceRequest: WorkspaceRequest, outputValues: OutputValues, reviewTime: Instant): LearnCardEntity {
            val intervalDays = outputValues.interval.toLong()
            val nextReview = reviewTime.plus(Duration.ofDays(intervalDays))
            return LearnCardEntity(cardId.id, workspaceRequest.id, reviewTime, nextReview, outputValues.repetitions, outputValues.easeFactor, outputValues.interval)
        }

        fun create(learnCard: LearnCard): LearnCardEntity {
            return LearnCardEntity(learnCard.id, learnCard.workspaceId, learnCard.lastReview, learnCard.nextReview, learnCard.repetitions, learnCard.easeFactor, learnCard.intervalDays)
        }
    }
}
