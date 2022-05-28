package de.mcella.spring.learntool.learn.storage

import java.time.Instant
import java.util.Optional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface LearnCardRepository : JpaRepository<LearnCardEntity, String> {

    fun findFirstByWorkspaceIdAndNextReviewBeforeOrderByNextReview(workspaceId: String, end: Instant): Optional<LearnCardEntity>

    fun findByWorkspaceId(workspaceId: String): List<LearnCardEntity>
}
