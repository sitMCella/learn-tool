package de.mcella.spring.learntool.learn.dto

import de.mcella.spring.learntool.learn.storage.LearnCardEntity
import java.time.Instant
import javax.persistence.Id
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

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
        fun create(learnCardEntity: LearnCardEntity): LearnCard {
            return LearnCard(learnCardEntity.id, learnCardEntity.workspaceName, learnCardEntity.lastReview, learnCardEntity.nextReview, learnCardEntity.repetitions, learnCardEntity.easeFactor, learnCardEntity.intervalDays)
        }
    }
}
