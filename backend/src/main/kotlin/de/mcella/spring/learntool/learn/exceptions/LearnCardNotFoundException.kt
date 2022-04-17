package de.mcella.spring.learntool.learn.exceptions

import de.mcella.spring.learntool.card.dto.CardId
import de.mcella.spring.learntool.workspace.dto.Workspace
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class LearnCardNotFoundException(workspace: Workspace, cardId: CardId) : ResponseStatusException(HttpStatus.NOT_FOUND, "LearnCard with id ${cardId.id} not found in Workspace with name ${workspace.name}.")
