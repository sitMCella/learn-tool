package de.mcella.spring.learntool.workspace

import de.mcella.spring.learntool.security.UserPrincipal
import de.mcella.spring.learntool.workspace.dto.Workspace
import de.mcella.spring.learntool.workspace.dto.WorkspaceCreateRequest
import de.mcella.spring.learntool.workspace.dto.WorkspaceRequest
import java.net.URI
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/workspaces")
class WorkspaceController(private val workspaceService: WorkspaceService) {

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasRole('USER')")
    fun create(
        @RequestBody workspaceCreateRequest: WorkspaceCreateRequest,
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<Workspace> {
        val workspace = workspaceService.create(workspaceCreateRequest, userPrincipal)
        val bodyBuilder = ResponseEntity.status(HttpStatus.CREATED)
        bodyBuilder.location(URI("/api/workspaces/${workspace.id}"))
        return bodyBuilder.body(workspace)
    }

    @PutMapping("/{workspaceId}", consumes = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasRole('USER')")
    fun update(
        @PathVariable(value = "workspaceId") workspaceId: String,
        @RequestBody workspaceCreateRequest: WorkspaceCreateRequest,
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<Workspace> {
        val workspace = workspaceService.update(WorkspaceRequest(workspaceId), workspaceCreateRequest, userPrincipal)
        val bodyBuilder = ResponseEntity.status(HttpStatus.OK)
        bodyBuilder.location(URI("/api/workspaces/${workspace.id}"))
        return bodyBuilder.body(workspace)
    }

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasRole('USER')")
    @ResponseStatus(HttpStatus.OK)
    fun getAll(@AuthenticationPrincipal userPrincipal: UserPrincipal): List<Workspace> = workspaceService.getAll(userPrincipal)
}
