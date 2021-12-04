package de.mcella.spring.learntool.search.exceptions

import de.mcella.spring.learntool.workspace.Workspace
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class CardSearchException(workspace: Workspace, e: Throwable) : ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while searching Cards in the Workspace ${workspace.name}. Exception: $e")
