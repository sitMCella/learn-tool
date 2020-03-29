package de.mcella.spring.learntool.workspace

import de.mcella.spring.learntool.workspace.WorkspaceNameValidator.MAX_WORKSPACE_NAME_LENGTH
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import javax.validation.constraints.Size

@Entity
@Table(name = "workspaces")
data class Workspace(
    @Id @Size(max = MAX_WORKSPACE_NAME_LENGTH) val name: String = ""
)
