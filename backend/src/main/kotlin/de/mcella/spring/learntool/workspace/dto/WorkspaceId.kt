package de.mcella.spring.learntool.workspace.dto

import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

data class WorkspaceId(
    @field:NotNull @field:NotEmpty val id: String
)
