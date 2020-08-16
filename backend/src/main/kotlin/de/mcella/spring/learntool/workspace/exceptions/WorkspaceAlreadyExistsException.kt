package de.mcella.spring.learntool.workspace.exceptions

import de.mcella.spring.learntool.workspace.storage.Workspace
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class WorkspaceAlreadyExistsException(workspace: Workspace) : ResponseStatusException(HttpStatus.CONFLICT, "A Workspace with name ${workspace.name} already exists.")
