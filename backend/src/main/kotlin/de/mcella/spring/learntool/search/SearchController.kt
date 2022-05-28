package de.mcella.spring.learntool.search

import de.mcella.spring.learntool.card.dto.Card
import de.mcella.spring.learntool.search.exceptions.CardSearchException
import de.mcella.spring.learntool.security.UserPrincipal
import de.mcella.spring.learntool.user.exceptions.UserNotAuthorizedException
import de.mcella.spring.learntool.workspace.dto.WorkspaceRequest
import de.mcella.spring.learntool.workspace.exceptions.WorkspaceNotExistsException
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/search")
class SearchController(private val cardSearchService: CardSearchService) {

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasRole('USER')")
    @ResponseStatus(HttpStatus.OK)
    fun search(
        @PathVariable(value = "workspaceId") workspaceId: String,
        @RequestParam(value = "content") content: String,
        @AuthenticationPrincipal user: UserPrincipal
    ): List<Card> {
        val workspaceRequest = WorkspaceRequest(workspaceId)
        try {
            return cardSearchService.searchCards(workspaceRequest, SearchPattern(content), user)
        } catch (e: Exception) {
            when (e) {
                is WorkspaceNotExistsException -> throw e
                is UserNotAuthorizedException -> throw e
                else -> throw CardSearchException(workspaceRequest, e)
            }
        }
    }
}
