package de.mcella.spring.learntool.learn

import de.mcella.spring.learntool.card.CardService
import de.mcella.spring.learntool.card.storage.Card
import de.mcella.spring.learntool.learn.exceptions.CardNotFoundException
import org.springframework.stereotype.Service

@Service
class LearnService(private val cardService: CardService) {

    fun getCard(workspaceName: String): Card = cardService.getFirstCardFromWorkspace(workspaceName) ?: throw CardNotFoundException(
        workspaceName
    )
}
