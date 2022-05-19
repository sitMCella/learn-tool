package de.mcella.spring.learntool.workspace.storage

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface WorkspaceRepository : JpaRepository<WorkspaceEntity, String> {

    fun findByUserId(userId: Long): List<WorkspaceEntity>
}
