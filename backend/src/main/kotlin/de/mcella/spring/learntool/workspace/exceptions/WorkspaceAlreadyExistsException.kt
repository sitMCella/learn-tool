package de.mcella.spring.learntool.workspace.exceptions

import de.mcella.spring.learntool.workspace.dto.WorkspaceId
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class WorkspaceAlreadyExistsException(workspaceId: WorkspaceId) : ResponseStatusException(HttpStatus.CONFLICT, "A Workspace with id ${workspaceId.id} already exists.")
