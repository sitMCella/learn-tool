package de.mcella.spring.learntool.workspace.exceptions

import de.mcella.spring.learntool.workspace.dto.WorkspaceRequest
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class WorkspaceNotExistsException(workspaceRequest: WorkspaceRequest) : ResponseStatusException(HttpStatus.NOT_FOUND, "A Workspace with id ${workspaceRequest.id} has not been found.")
