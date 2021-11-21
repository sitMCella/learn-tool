package de.mcella.spring.learntool.workspace.exceptions

import de.mcella.spring.learntool.workspace.Workspace
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class WorkspaceNotExistsException(workspace: Workspace) : ResponseStatusException(HttpStatus.NOT_FOUND, "A Workspace with name ${workspace.name} has not been found.")
