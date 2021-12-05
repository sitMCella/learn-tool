package de.mcella.spring.learntool.card

import de.mcella.spring.learntool.card.exceptions.CardAlreadyExistsException
import de.mcella.spring.learntool.card.exceptions.CreateCardException
import de.mcella.spring.learntool.card.exceptions.DeleteCardException
import de.mcella.spring.learntool.card.exceptions.UpdateCardException
import de.mcella.spring.learntool.workspace.Workspace
import de.mcella.spring.learntool.workspace.exceptions.InvalidWorkspaceNameException
import de.mcella.spring.learntool.workspace.exceptions.WorkspaceNotExistsException
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
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/workspaces/{workspaceName}/cards")
class CardController(private val cardService: CardService, private val cardImportService: CardImportService) {

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun create(
        @PathVariable(value = "workspaceName") workspaceName: String,
        @RequestBody cardContent: CardContent
    ): ResponseEntity<Card> {
        try {
            val workspace = Workspace(workspaceName)
            val card: Card = cardService.create(workspace, cardContent)
            val bodyBuilder = ResponseEntity.status(HttpStatus.CREATED)
            bodyBuilder.location(URI("/workspaces/$workspaceName/cards/${card.id}"))
            return bodyBuilder.body(card)
        } catch (e: Exception) {
            when (e) {
                is IllegalArgumentException -> throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, e.message)
                is WorkspaceNotExistsException -> throw e
                is CardAlreadyExistsException -> throw e
                else -> throw CreateCardException(e)
            }
        }
    }

    @PutMapping("/{cardId}", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun update(
        @PathVariable(value = "workspaceName") workspaceName: String,
        @PathVariable(value = "cardId") cardId: String,
        @RequestBody cardContent: CardContent
    ): ResponseEntity<Card> {
        try {
            val card: Card = cardService.update(CardId(cardId), Workspace(workspaceName), cardContent)
            val bodyBuilder = ResponseEntity.status(HttpStatus.OK)
            bodyBuilder.location(URI("/workspaces/$workspaceName/cards/$cardId"))
            return bodyBuilder.body(card)
        } catch (e: Exception) {
            when (e) {
                is IllegalArgumentException -> throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, e.message)
                is WorkspaceNotExistsException -> throw e
                is InvalidWorkspaceNameException -> throw e
                else -> throw UpdateCardException(CardId(cardId), e)
            }
        }
    }

    @DeleteMapping("/{cardId}")
    @ResponseStatus(HttpStatus.OK)
    fun delete(
        @PathVariable(value = "workspaceName") workspaceName: String,
        @PathVariable(value = "cardId") cardId: String
    ) {
        try {
            cardService.delete(CardId(cardId), Workspace(workspaceName))
        } catch (e: Exception) {
            when (e) {
                is IllegalArgumentException -> throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, e.message)
                is WorkspaceNotExistsException -> throw e
                is InvalidWorkspaceNameException -> throw e
                else -> throw DeleteCardException(CardId(cardId), e)
            }
        }
    }

    @PostMapping("many.csv", consumes = [MediaType.APPLICATION_OCTET_STREAM_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    fun createMany(
        @PathVariable(value = "workspaceName") workspaceName: String,
        content: InputStream
    ) = cardImportService.createMany(Workspace(workspaceName), content)

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.OK)
    fun getFromWorkspaceName(@PathVariable(value = "workspaceName") workspaceName: String): List<Card> = cardService.findByWorkspace(Workspace(workspaceName))
}
