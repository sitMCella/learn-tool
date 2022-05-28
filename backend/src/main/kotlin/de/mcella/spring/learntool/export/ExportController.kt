package de.mcella.spring.learntool.export

import de.mcella.spring.learntool.security.UserPrincipal
import de.mcella.spring.learntool.workspace.dto.WorkspaceRequest
import java.io.FileInputStream
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/export")
class ExportController(private val exportService: ExportService) {

    @GetMapping(produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE])
    @PreAuthorize("hasRole('USER')")
    fun export(
        @PathVariable(value = "workspaceId") workspaceId: String,
        @AuthenticationPrincipal user: UserPrincipal
    ): ResponseEntity<InputStreamResource> {
        val backup = exportService.exportBackup(WorkspaceRequest(workspaceId), user)
        val backupStream = InputStreamResource(FileInputStream(backup))
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + backup.name + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(backup.length())
                .body(backupStream)
    }
}
