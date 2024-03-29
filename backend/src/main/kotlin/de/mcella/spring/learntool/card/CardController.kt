package de.mcella.spring.learntool.card

import de.mcella.spring.learntool.card.dto.Card
import de.mcella.spring.learntool.card.dto.CardContent
import de.mcella.spring.learntool.card.dto.CardId
import de.mcella.spring.learntool.card.dto.CardPagination
import de.mcella.spring.learntool.security.UserPrincipal
import de.mcella.spring.learntool.workspace.dto.WorkspaceRequest
import java.io.InputStream
import java.net.URI
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/cards")
class CardController(private val cardService: CardService, private val cardImportService: CardImportService) {

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasRole('USER')")
    fun create(
        @PathVariable(value = "workspaceId") workspaceId: String,
        @RequestBody cardContent: CardContent,
        @AuthenticationPrincipal user: UserPrincipal
    ): ResponseEntity<Card> {
        val workspaceRequest = WorkspaceRequest(workspaceId)
        val card: Card = cardService.create(workspaceRequest, cardContent, user)
        val bodyBuilder = ResponseEntity.status(HttpStatus.CREATED)
        bodyBuilder.location(URI("/workspaces/$workspaceId/cards/${card.id}"))
        return bodyBuilder.body(card)
    }

    @PutMapping("/{cardId}", consumes = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasRole('USER')")
    fun update(
        @PathVariable(value = "workspaceId") workspaceId: String,
        @PathVariable(value = "cardId") cardId: String,
        @RequestBody cardContent: CardContent,
        @AuthenticationPrincipal user: UserPrincipal
    ): ResponseEntity<Card> {
        val card: Card = cardService.update(CardId(cardId), WorkspaceRequest(workspaceId), cardContent, user)
        val bodyBuilder = ResponseEntity.status(HttpStatus.OK)
        bodyBuilder.location(URI("/workspaces/$workspaceId/cards/$cardId"))
        return bodyBuilder.body(card)
    }

    @DeleteMapping("/{cardId}")
    @PreAuthorize("hasRole('USER')")
    @ResponseStatus(HttpStatus.OK)
    fun delete(
        @PathVariable(value = "workspaceId") workspaceId: String,
        @PathVariable(value = "cardId") cardId: String,
        @AuthenticationPrincipal user: UserPrincipal
    ) = cardService.delete(CardId(cardId), WorkspaceRequest(workspaceId), user)

    @PostMapping("many.csv", consumes = [MediaType.APPLICATION_OCTET_STREAM_VALUE])
    @PreAuthorize("hasRole('USER')")
    @ResponseStatus(HttpStatus.CREATED)
    fun createMany(
        @PathVariable(value = "workspaceId") workspaceId: String,
        content: InputStream,
        @AuthenticationPrincipal user: UserPrincipal
    ) = cardImportService.createMany(WorkspaceRequest(workspaceId), content, user)

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasRole('USER')")
    @ResponseStatus(HttpStatus.OK)
    fun get(
        @PathVariable(value = "workspaceId") workspaceId: String,
        @RequestParam(value = "page", required = false, defaultValue = "0") page: Int,
        @RequestParam(value = "size", required = false, defaultValue = "20") size: Int,
        @AuthenticationPrincipal user: UserPrincipal
    ): ResponseEntity<List<Card>> {
        val workspace = WorkspaceRequest(workspaceId)
        val cards = cardService.findByWorkspace(workspace, CardPagination(page, size), CardSort.desc, user)
        val bodyBuilder = ResponseEntity.status(HttpStatus.OK)
        bodyBuilder.header("count", cardService.countByWorkspace(workspace, user).toString())
        return bodyBuilder.body(cards)
    }
}
