package de.mcella.spring.learntool.card.storage

import org.springframework.data.domain.Pageable
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface CardRepository : PagingAndSortingRepository<CardEntity, String> {
    fun findByWorkspaceNameOrderByCreationDateDesc(workspaceName: String, pageable: Pageable): List<CardEntity>
}
