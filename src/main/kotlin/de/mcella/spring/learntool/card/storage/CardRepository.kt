package de.mcella.spring.learntool.card.storage

import java.util.Optional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CardRepository : JpaRepository<Card, String> {

    fun findFirstByWorkspaceName(workspaceName: String): Optional<Card>
}
