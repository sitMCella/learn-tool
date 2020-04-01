package de.mcella.spring.learntool.workspace

import de.mcella.spring.learntool.workspace.storage.Workspace
import java.net.URI
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/workspaces")
class WorkspaceController(private val workspaceService: WorkspaceService) {

    @PostMapping
    fun create(@RequestBody workspace: Workspace): ResponseEntity<Void> {
        workspaceService.create(workspace)
        return ResponseEntity.created(URI("/workspaces/${workspace.name}")).build()
    }
}
