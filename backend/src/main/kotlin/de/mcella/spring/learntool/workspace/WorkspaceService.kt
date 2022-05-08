package de.mcella.spring.learntool.workspace

import de.mcella.spring.learntool.security.UserPrincipal
import de.mcella.spring.learntool.user.dto.UserId
import de.mcella.spring.learntool.user.exceptions.UserNotAuthorizedException
import de.mcella.spring.learntool.workspace.dto.Workspace
import de.mcella.spring.learntool.workspace.dto.WorkspaceRequest
import de.mcella.spring.learntool.workspace.exceptions.WorkspaceAlreadyExistsException
import de.mcella.spring.learntool.workspace.storage.WorkspaceEntity
import de.mcella.spring.learntool.workspace.storage.WorkspaceRepository
import kotlin.streams.toList
import org.springframework.stereotype.Service

@Service
class WorkspaceService(private val workspaceRepository: WorkspaceRepository) {

    fun create(workspaceRequest: WorkspaceRequest, user: UserPrincipal): Workspace {
        WorkspaceNameValidator.validate(workspaceRequest.name)
        if (workspaceRepository.existsById(workspaceRequest.name)) {
            throw WorkspaceAlreadyExistsException(workspaceRequest)
        }
        val workspace = Workspace.create(workspaceRequest, user)
        val workspaceEntity = WorkspaceEntity.create(workspace)
        return Workspace.create(workspaceRepository.save(workspaceEntity))
    }

    fun getAll(user: UserPrincipal): List<Workspace> {
        val userId = UserId.create(user)
        return workspaceRepository.findByUserId(userId.id)
                .stream()
                .map { workspaceEntity -> Workspace.create(workspaceEntity) }
                .toList()
    }

    fun exists(workspaceRequest: WorkspaceRequest): Boolean = workspaceRepository.existsById(workspaceRequest.name)

    fun verifyIfUserIsAuthorized(workspaceRequest: WorkspaceRequest, userPrincipal: UserPrincipal): Boolean {
        val workspaceEntity = workspaceRepository.findById(workspaceRequest.name)
        if (!workspaceEntity.isPresent) return true
        val userId = UserId.create(userPrincipal)
        if (!WorkspaceEntity.hasUserId(workspaceEntity.get(), userId)) {
            throw UserNotAuthorizedException(userPrincipal)
        }
        return true
    }
}
