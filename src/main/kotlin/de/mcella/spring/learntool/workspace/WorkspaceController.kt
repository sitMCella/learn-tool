package de.mcella.spring.learntool.workspace

import de.mcella.spring.learntool.workspace.storage.Workspace
import java.net.URI
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/workspaces")
class WorkspaceController(private val workspaceService: WorkspaceService) {

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun create(@RequestBody workspace: Workspace): ResponseEntity<Workspace> {
        val createdWorkspace = workspaceService.create(workspace)
        val bodyBuilder = ResponseEntity.status(HttpStatus.CREATED)
        bodyBuilder.location(URI("/workspaces/${workspace.name}"))
        return bodyBuilder.body(createdWorkspace)
    }
}
