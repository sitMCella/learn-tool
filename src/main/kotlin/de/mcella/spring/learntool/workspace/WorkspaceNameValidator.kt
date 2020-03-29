package de.mcella.spring.learntool.workspace

import java.util.regex.Pattern

object WorkspaceNameValidator {
    const val MAX_WORKSPACE_NAME_LENGTH = 255

    private val WORKSPACE_NAME_REGEX = "^([a-z0-9]*)".format(
        MAX_WORKSPACE_NAME_LENGTH
    )
    private val patternContainerName: Pattern = Pattern.compile(WORKSPACE_NAME_REGEX)

    fun validate(workspaceName: String) {
        if (!isValid(workspaceName))
            throw InvalidWorkspaceNameException("Invalid workspace name: $workspaceName")
    }

    private fun isValid(workspaceName: String): Boolean {
        return patternContainerName.matcher(workspaceName).matches()
    }
}
