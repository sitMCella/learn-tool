package de.mcella.spring.learntool.card

import de.mcella.spring.learntool.card.dto.Card
import de.mcella.spring.learntool.card.dto.CardContent
import de.mcella.spring.learntool.card.dto.CardId
import de.mcella.spring.learntool.card.dto.CardPagination
import de.mcella.spring.learntool.workspace.dto.Workspace
import java.io.InputStream
import java.net.URI
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
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
@RequestMapping("/api/workspaces/{workspaceName}/cards")
class CardController(private val cardService: CardService, private val cardImportService: CardImportService) {

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun create(
        @PathVariable(value = "workspaceName") workspaceName: String,
        @RequestBody cardContent: CardContent
    ): ResponseEntity<Card> {
        val workspace = Workspace(workspaceName)
        val card: Card = cardService.create(workspace, cardContent)
        val bodyBuilder = ResponseEntity.status(HttpStatus.CREATED)
        bodyBuilder.location(URI("/workspaces/$workspaceName/cards/${card.id}"))
        return bodyBuilder.body(card)
    }

    @PutMapping("/{cardId}", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun update(
        @PathVariable(value = "workspaceName") workspaceName: String,
        @PathVariable(value = "cardId") cardId: String,
        @RequestBody cardContent: CardContent
    ): ResponseEntity<Card> {
        val card: Card = cardService.update(CardId(cardId), Workspace(workspaceName), cardContent)
        val bodyBuilder = ResponseEntity.status(HttpStatus.OK)
        bodyBuilder.location(URI("/workspaces/$workspaceName/cards/$cardId"))
        return bodyBuilder.body(card)
    }

    @DeleteMapping("/{cardId}")
    @ResponseStatus(HttpStatus.OK)
    fun delete(
        @PathVariable(value = "workspaceName") workspaceName: String,
        @PathVariable(value = "cardId") cardId: String
    ) = cardService.delete(CardId(cardId), Workspace(workspaceName))

    @PostMapping("many.csv", consumes = [MediaType.APPLICATION_OCTET_STREAM_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    fun createMany(
        @PathVariable(value = "workspaceName") workspaceName: String,
        content: InputStream
    ) = cardImportService.createMany(Workspace(workspaceName), content)

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.OK)
    fun get(
        @PathVariable(value = "workspaceName") workspaceName: String,
        @RequestParam(value = "page", required = false, defaultValue = "0") page: Int,
        @RequestParam(value = "size", required = false, defaultValue = "20") size: Int
    ): List<Card> = cardService.findByWorkspace(Workspace(workspaceName), CardPagination(page, size))
}
