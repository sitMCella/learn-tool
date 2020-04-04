package de.mcella.spring.learntool.card

import de.mcella.spring.learntool.card.storage.Card
import java.net.URI
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/workspaces/{workspaceName}/cards")
class CardController(private val cardService: CardService) {

    @PostMapping
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
}
