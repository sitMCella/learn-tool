package de.mcella.spring.learntool.workspace

import de.mcella.spring.learntool.workspace.dto.Workspace
import java.net.URI
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
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
    fun create(@RequestBody workspace: Workspace): ResponseEntity<Workspace> {
        val createdWorkspace = workspaceService.create(workspace)
        val bodyBuilder = ResponseEntity.status(HttpStatus.CREATED)
        bodyBuilder.location(URI("/api/workspaces/${workspace.name}"))
        return bodyBuilder.body(createdWorkspace)
    }

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.OK)
    fun getAll(): List<Workspace> = workspaceService.getAll()
}
