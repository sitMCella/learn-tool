package de.mcella.spring.learntool.learn.storage

import java.time.Instant
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface LearnCardRepository : JpaRepository<LearnCardEntity, String> {

    fun findByWorkspaceIdAndNextReviewBefore(workspaceId: String, end: Instant): List<LearnCardEntity>

    fun findByWorkspaceId(workspaceId: String): List<LearnCardEntity>
}
