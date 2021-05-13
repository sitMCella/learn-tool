package de.mcella.spring.learntool.workspace.storage

import de.mcella.spring.learntool.workspace.WorkspaceNameValidator.MAX_WORKSPACE_NAME_LENGTH
import de.mcella.spring.learntool.workspace.WorkspaceNameValidator.MIN_WORKSPACE_NAME_LENGTH
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import javax.validation.constraints.Size

@Entity
@Table(name = "workspaces")
data class Workspace(
    @Id @Size(min = MIN_WORKSPACE_NAME_LENGTH, max = MAX_WORKSPACE_NAME_LENGTH) val name: String = ""
)
