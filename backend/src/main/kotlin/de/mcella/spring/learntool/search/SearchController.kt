package de.mcella.spring.learntool.search

import de.mcella.spring.learntool.card.Card
import de.mcella.spring.learntool.search.exceptions.CardSearchException
import de.mcella.spring.learntool.workspace.Workspace
import de.mcella.spring.learntool.workspace.exceptions.WorkspaceNotExistsException
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/workspaces/{workspaceName}/search")
class SearchController(private val cardSearchService: CardSearchService) {

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.OK)
    fun search(@PathVariable(value = "workspaceName") workspaceName: String, @RequestParam(value = "content") content: String): List<Card> {
        try {
            return cardSearchService.searchCards(Workspace(workspaceName), SearchPattern(content))
        } catch (e: Exception) {
            when (e) {
                is WorkspaceNotExistsException -> throw e
                else -> throw CardSearchException(Workspace(workspaceName), e)
            }
        }
    }
}
