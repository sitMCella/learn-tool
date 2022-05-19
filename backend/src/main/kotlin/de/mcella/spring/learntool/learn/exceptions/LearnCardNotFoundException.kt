package de.mcella.spring.learntool.learn.exceptions

import de.mcella.spring.learntool.card.dto.CardId
import de.mcella.spring.learntool.workspace.dto.WorkspaceRequest
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class LearnCardNotFoundException(workspaceRequest: WorkspaceRequest, cardId: CardId) : ResponseStatusException(HttpStatus.NOT_FOUND, "LearnCard with id ${cardId.id} not found in Workspace with name ${workspaceRequest.name}.")
