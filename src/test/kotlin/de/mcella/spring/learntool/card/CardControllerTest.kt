package de.mcella.spring.learntool.card

import com.fasterxml.jackson.databind.ObjectMapper
import de.mcella.spring.learntool.card.storage.Card
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@RunWith(SpringRunner::class)
@WebMvcTest(CardController::class)
@AutoConfigureWebClient
class CardControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var cardService: CardService

    private val objectMapper = ObjectMapper()

    @Test
    fun `given a Workspace name and a CardContent, when sending a post request to the card endpoint, then the create method of CardService is called`() {
        val workspaceName = "workspaceTest"
        val cardContent = CardContent("question", "response")
        val contentBody = objectMapper.writeValueAsString(cardContent)
        val card = Card("9e493dc0-ef75-403f-b5d6-ed510634f8a6", workspaceName, "question", "response")
        Mockito.`when`(cardService.create(workspaceName, cardContent)).thenReturn(card)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/workspaces/$workspaceName/cards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(contentBody)
        ).andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.header().exists(HttpHeaders.LOCATION))
            .andExpect(MockMvcResultMatchers.header().string(HttpHeaders.LOCATION, "/workspaces/$workspaceName/cards/${card.id}"))

        Mockito.verify(cardService).create(workspaceName, cardContent)
    }
}
