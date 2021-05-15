package de.mcella.spring.learntool.card.storage

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CardRepository : JpaRepository<Card, String> {
    fun findByWorkspaceName(workspaceName: String): List<Card>
}
