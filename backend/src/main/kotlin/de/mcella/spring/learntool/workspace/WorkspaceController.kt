package de.mcella.spring.learntool.workspace

import de.mcella.spring.learntool.security.UserPrincipal
import de.mcella.spring.learntool.workspace.dto.Workspace
import de.mcella.spring.learntool.workspace.dto.WorkspaceRequest
import java.net.URI
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/workspaces")
class WorkspaceController(private val workspaceService: WorkspaceService) {

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasRole('USER')")
    fun create(@RequestBody workspaceRequest: WorkspaceRequest, @AuthenticationPrincipal user: UserPrincipal): ResponseEntity<Workspace> {
        val createdWorkspace = workspaceService.create(workspaceRequest, user)
        val bodyBuilder = ResponseEntity.status(HttpStatus.CREATED)
        bodyBuilder.location(URI("/api/workspaces/${workspaceRequest.name}"))
        return bodyBuilder.body(createdWorkspace)
    }

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasRole('USER')")
    @ResponseStatus(HttpStatus.OK)
    fun getAll(@AuthenticationPrincipal user: UserPrincipal): List<Workspace> = workspaceService.getAll(user)
}
