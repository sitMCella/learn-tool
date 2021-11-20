package de.mcella.spring.learntool.learn.storage

import java.time.Instant
import java.util.Optional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface LearnCardRepository : JpaRepository<LearnCard, String> {

    fun findFirstByWorkspaceNameAndNextReviewBeforeOrderByNextReview(workspaceName: String, end: Instant): Optional<LearnCard>

    fun findByWorkspaceName(workspaceName: String): List<LearnCard>
}
