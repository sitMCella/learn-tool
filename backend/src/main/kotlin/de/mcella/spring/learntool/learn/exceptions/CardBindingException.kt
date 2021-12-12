package de.mcella.spring.learntool.learn.exceptions

import de.mcella.spring.learntool.card.dto.CardId
import de.mcella.spring.learntool.workspace.dto.Workspace
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class CardBindingException(workspace: Workspace, cardId: CardId) : ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "The Card with id ${cardId.id} does not belong to Workspace with name ${workspace.name}.")
