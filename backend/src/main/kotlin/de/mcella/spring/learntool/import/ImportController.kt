package de.mcella.spring.learntool.import

import de.mcella.spring.learntool.security.UserPrincipal
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/workspaces/import")
class ImportController(private val importService: ImportService) {

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @PreAuthorize("hasRole('USER')")
    @ResponseStatus(HttpStatus.OK)
    fun import(@RequestParam("backup") backup: MultipartFile, @AuthenticationPrincipal user: UserPrincipal) {
        importService.importBackup(backup, user)
    }
}
