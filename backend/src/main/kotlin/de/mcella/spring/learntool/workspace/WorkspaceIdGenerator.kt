package de.mcella.spring.learntool.workspace

import de.mcella.spring.learntool.workspace.dto.WorkspaceId
import java.util.UUID
import org.springframework.stereotype.Service

@Service
class WorkspaceIdGenerator {

    fun create(): WorkspaceId {
        return WorkspaceId(UUID.randomUUID().toString())
    }
}
