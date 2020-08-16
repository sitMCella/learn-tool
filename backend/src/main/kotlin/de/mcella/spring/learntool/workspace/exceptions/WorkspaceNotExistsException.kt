package de.mcella.spring.learntool.workspace.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class WorkspaceNotExistsException(workspaceName: String) : ResponseStatusException(HttpStatus.NOT_FOUND, "A Workspace with name $workspaceName has not been found.")
