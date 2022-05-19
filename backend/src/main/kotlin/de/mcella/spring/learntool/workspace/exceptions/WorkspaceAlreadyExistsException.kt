package de.mcella.spring.learntool.workspace.exceptions

import de.mcella.spring.learntool.workspace.dto.WorkspaceRequest
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class WorkspaceAlreadyExistsException(workspaceRequest: WorkspaceRequest) : ResponseStatusException(HttpStatus.CONFLICT, "A Workspace with name ${workspaceRequest.name} already exists.")
