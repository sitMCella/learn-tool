package de.mcella.spring.learntool.learn.exceptions

import de.mcella.spring.learntool.workspace.dto.WorkspaceRequest
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class LearnCardsNotFoundException(workspaceRequest: WorkspaceRequest) : ResponseStatusException(HttpStatus.NOT_FOUND, "No Cards found in Workspace with name ${workspaceRequest.name}.")
