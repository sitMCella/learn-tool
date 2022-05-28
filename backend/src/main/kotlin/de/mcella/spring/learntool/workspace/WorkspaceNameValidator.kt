package de.mcella.spring.learntool.workspace

import de.mcella.spring.learntool.workspace.dto.WorkspaceCreateRequest
import de.mcella.spring.learntool.workspace.exceptions.InvalidWorkspaceIdException
import java.util.regex.Pattern

object WorkspaceNameValidator {
    const val MIN_WORKSPACE_NAME_LENGTH = 1
    const val MAX_WORKSPACE_NAME_LENGTH = 255

    private val WORKSPACE_NAME_REGEX = "^([a-zA-Z0-9-_ ]*)".format(
        MAX_WORKSPACE_NAME_LENGTH
    )
    private val patternContainerName: Pattern = Pattern.compile(WORKSPACE_NAME_REGEX)

    fun validate(workspaceCreateRequest: WorkspaceCreateRequest) {
        if (!isValid(workspaceCreateRequest.name))
            throw InvalidWorkspaceIdException("Invalid workspace name: ${workspaceCreateRequest.name}")
    }

    private fun isValid(workspaceName: String): Boolean {
        return workspaceName.length in MIN_WORKSPACE_NAME_LENGTH..MAX_WORKSPACE_NAME_LENGTH && patternContainerName.matcher(workspaceName).matches()
    }
}
