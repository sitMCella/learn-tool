package de.mcella.spring.learntool.search.exceptions

import de.mcella.spring.learntool.workspace.dto.WorkspaceRequest
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class CardSearchException(workspaceRequest: WorkspaceRequest, e: Throwable) : ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while searching Cards in the Workspace with id ${workspaceRequest.id}. Exception: $e")
