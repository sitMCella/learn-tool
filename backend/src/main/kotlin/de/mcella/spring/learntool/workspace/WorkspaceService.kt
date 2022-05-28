package de.mcella.spring.learntool.workspace

import de.mcella.spring.learntool.security.UserPrincipal
import de.mcella.spring.learntool.user.dto.UserId
import de.mcella.spring.learntool.user.exceptions.UserNotAuthorizedException
import de.mcella.spring.learntool.workspace.dto.Workspace
import de.mcella.spring.learntool.workspace.dto.WorkspaceCreateRequest
import de.mcella.spring.learntool.workspace.dto.WorkspaceRequest
import de.mcella.spring.learntool.workspace.exceptions.WorkspaceAlreadyExistsException
import de.mcella.spring.learntool.workspace.exceptions.WorkspaceNotExistsException
import de.mcella.spring.learntool.workspace.storage.WorkspaceEntity
import de.mcella.spring.learntool.workspace.storage.WorkspaceRepository
import kotlin.streams.toList
import org.springframework.stereotype.Service

@Service
class WorkspaceService(
    private val workspaceRepository: WorkspaceRepository,
    private val workspaceIdGenerator: WorkspaceIdGenerator
) {

    fun create(workspaceCreateRequest: WorkspaceCreateRequest, userPrincipal: UserPrincipal): Workspace {
        WorkspaceNameValidator.validate(workspaceCreateRequest)
        val workspaceId = workspaceIdGenerator.create()
        if (workspaceRepository.existsById(workspaceId.id)) {
            throw WorkspaceAlreadyExistsException(workspaceId)
        }
        val workspace = Workspace.create(workspaceId, workspaceCreateRequest, userPrincipal)
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

    fun exists(workspaceRequest: WorkspaceRequest): Boolean {
        return workspaceRepository.existsById(workspaceRequest.id)
    }

    fun get(workspaceRequest: WorkspaceRequest): Workspace {
        val workspaceEntity = workspaceRepository.findById(workspaceRequest.id)
        if (!workspaceEntity.isPresent) {
            throw WorkspaceNotExistsException(workspaceRequest)
        }
        return Workspace.create(workspaceEntity.get())
    }

    fun verifyIfUserIsAuthorized(workspaceRequest: WorkspaceRequest, userPrincipal: UserPrincipal): Boolean {
        val workspaceEntity = workspaceRepository.findById(workspaceRequest.id)
        if (!workspaceEntity.isPresent) return true
        val userId = UserId.create(userPrincipal)
        if (!WorkspaceEntity.hasUserId(workspaceEntity.get(), userId)) {
            throw UserNotAuthorizedException(userPrincipal)
        }
        return true
    }
}
