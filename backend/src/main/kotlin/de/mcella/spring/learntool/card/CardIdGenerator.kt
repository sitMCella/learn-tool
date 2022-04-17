package de.mcella.spring.learntool.card

import de.mcella.spring.learntool.card.dto.CardId
import java.util.UUID
import org.springframework.stereotype.Service

@Service
class CardIdGenerator {

    fun create(): CardId {
        return CardId(UUID.randomUUID().toString())
    }
}
