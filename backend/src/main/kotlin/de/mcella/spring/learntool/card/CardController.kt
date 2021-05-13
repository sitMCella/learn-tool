package de.mcella.spring.learntool.card

import de.mcella.spring.learntool.card.storage.Card
import java.io.InputStream
import java.net.URI
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/workspaces/{workspaceName}/cards")
class CardController(private val cardService: CardService, private val cardImportService: CardImportService) {

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun create(
        @PathVariable(value = "workspaceName") workspaceName: String,
        @RequestBody cardContent: CardContent
    ): ResponseEntity<Card> {
        try {
            val card: Card = cardService.create(workspaceName, cardContent)
            val bodyBuilder = ResponseEntity.status(HttpStatus.CREATED)
            bodyBuilder.location(URI("/workspaces/$workspaceName/cards/${card.id}"))
            return bodyBuilder.body(card)
        } catch (e: IllegalArgumentException) {
            throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, e.message)
        }
    }

    @PostMapping("many.csv", consumes = [MediaType.APPLICATION_OCTET_STREAM_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    fun createMany(
        @PathVariable(value = "workspaceName") workspaceName: String,
        content: InputStream
    ) = cardImportService.createMany(workspaceName, content)

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.OK)
    fun getFromWorkspaceName(@PathVariable(value = "workspaceName") workspaceName: String): List<Card> = cardService.findByWorkspaceName(workspaceName)
}
