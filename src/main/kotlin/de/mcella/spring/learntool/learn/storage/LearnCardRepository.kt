package de.mcella.spring.learntool.learn.storage

import java.time.Instant
import java.util.Optional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface LearnCardRepository : JpaRepository<LearnCard, String> {

    fun findFirstByWorkspaceNameAndNextReviewBetweenOrderByNextReview(workspaceName: String, begin: Instant, end: Instant): Optional<LearnCard>
}
