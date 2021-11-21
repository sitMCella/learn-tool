package de.mcella.spring.learntool.card.storage

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CardRepository : JpaRepository<CardEntity, String> {
    fun findByWorkspaceName(workspaceName: String): List<CardEntity>
}
