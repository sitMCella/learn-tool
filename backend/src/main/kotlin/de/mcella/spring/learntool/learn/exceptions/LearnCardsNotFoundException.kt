package de.mcella.spring.learntool.learn.exceptions

import de.mcella.spring.learntool.workspace.Workspace
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class LearnCardsNotFoundException(workspace: Workspace) : ResponseStatusException(HttpStatus.NOT_FOUND, "No Cards found in Workspace with name ${workspace.name}.")
