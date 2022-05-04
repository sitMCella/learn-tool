package de.mcella.spring.learntool.search

import de.mcella.spring.learntool.card.dto.Card
import de.mcella.spring.learntool.card.storage.CardEntity
import de.mcella.spring.learntool.workspace.WorkspaceService
import de.mcella.spring.learntool.workspace.dto.WorkspaceRequest
import de.mcella.spring.learntool.workspace.exceptions.WorkspaceNotExistsException
import javax.persistence.EntityManager
import kotlin.streams.toList
import org.hibernate.search.mapper.orm.Search
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CardSearchService(private val entityManager: EntityManager, private val workspaceService: WorkspaceService) {

    @Transactional
    fun searchCards(workspaceRequest: WorkspaceRequest, searchPattern: SearchPattern): List<Card> {
        if (!workspaceService.exists(workspaceRequest)) {
            throw WorkspaceNotExistsException(workspaceRequest)
        }
        val searchSession = Search.session(entityManager)
        val result = searchSession.search(CardEntity::class.java)
                .where { f -> f.bool { b ->
                        run {
                            b.must(f.matchAll())
                            b.must(f.match().fields("workspaceName").matching(workspaceRequest.name))
                            b.must(f.match().fields("question", "response").matching(searchPattern.content))
                        }
                    }
                }
                .fetch(20)
        return result.hits().stream()
                .map { cardEntity -> Card.create(cardEntity as CardEntity) }
                .toList<Card>()
    }
}
