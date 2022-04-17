package de.mcella.spring.learntool.export

import de.mcella.spring.learntool.workspace.dto.Workspace
import java.io.FileInputStream
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/workspaces/{workspaceName}/export")
class ExportController(private val exportService: ExportService) {

    @GetMapping(produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE])
    fun export(@PathVariable(value = "workspaceName") workspaceName: String): ResponseEntity<InputStreamResource> {
        val backup = exportService.exportBackup(Workspace(workspaceName))
        val backupStream = InputStreamResource(FileInputStream(backup))
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + backup.name + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(backup.length())
                .body(backupStream)
    }
}
