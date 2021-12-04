package de.mcella.spring.learntool.card

import com.fasterxml.jackson.databind.ObjectMapper
import de.mcella.spring.learntool.UnitTest
import de.mcella.spring.learntool.card.exceptions.CardAlreadyExistsException
import de.mcella.spring.learntool.workspace.Workspace
import de.mcella.spring.learntool.workspace.exceptions.InvalidWorkspaceNameException
import de.mcella.spring.learntool.workspace.exceptions.WorkspaceNotExistsException
import java.lang.IllegalArgumentException
import org.junit.Test
import org.junit.experimental.categories.Category
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
@Category(UnitTest::class)
@WebMvcTest(CardController::class)
@AutoConfigureWebClient
class CardControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var cardService: CardService

    @MockBean
    private lateinit var cardImportService: CardImportService

    private val objectMapper = ObjectMapper()

    @Test
    fun `given a Workspace name and a CardContent, when sending a POST REST request to the cards endpoint, then the create method of CardService is called`() {
        val workspace = Workspace("workspaceTest")
        val cardContent = CardContent("question", "response")
        val contentBody = objectMapper.writeValueAsString(cardContent)
        val card = Card("9e493dc0-ef75-403f-b5d6-ed510634f8a6", workspace.name, "question", "response")
        Mockito.`when`(cardService.create(workspace, cardContent)).thenReturn(card)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/workspaces/${workspace.name}/cards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(contentBody)
        ).andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.header().exists(HttpHeaders.LOCATION))
            .andExpect(MockMvcResultMatchers.header().string(HttpHeaders.LOCATION, "/workspaces/${workspace.name}/cards/${card.id}"))

        Mockito.verify(cardService).create(workspace, cardContent)
    }

    @Test
    fun `given a Workspace name and a CardContent, when sending a POST REST request to the cards endpoint and the create method of CardService throws IllegalArgumentException, then an UNPROCESSABLE_ENTITY http status response is returned`() {
        val workspace = Workspace("workspaceTest")
        val cardContent = CardContent("", "response")
        val contentBody = objectMapper.writeValueAsString(cardContent)
        Mockito.`when`(cardService.create(workspace, cardContent)).thenThrow(IllegalArgumentException::class.java)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/workspaces/${workspace.name}/cards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(contentBody)
        ).andExpect(MockMvcResultMatchers.status().isUnprocessableEntity)
    }

    @Test
    fun `given a Workspace name and a CardContent, when sending a POST REST request to the cards endpoint and the Workspace does not exist, then a NOT_FOUND http status response is returned`() {
        val workspace = Workspace("workspaceTest")
        val cardContent = CardContent("request", "response")
        val contentBody = objectMapper.writeValueAsString(cardContent)
        Mockito.`when`(cardService.create(workspace, cardContent)).thenThrow(WorkspaceNotExistsException(workspace))

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/workspaces/${workspace.name}/cards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(contentBody)
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    fun `given a Workspace name and a CardContent, when sending a POST REST request to the cards endpoint and the Card already exists, then a CONFLICT http status response is returned`() {
        val workspace = Workspace("workspaceTest")
        val cardContent = CardContent("request", "response")
        val contentBody = objectMapper.writeValueAsString(cardContent)
        Mockito.`when`(cardService.create(workspace, cardContent)).thenThrow(CardAlreadyExistsException(CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")))

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/workspaces/${workspace.name}/cards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(contentBody)
        ).andExpect(MockMvcResultMatchers.status().isConflict)
    }

    @Test
    fun `given a Workspace name, a Card Id and a CardContent, when sending a PUT REST request to the cards endpoint, then the update method of CardService is called`() {
        val workspace = Workspace("workspaceTest")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val cardContent = CardContent("updated question", "updated response")
        val contentBody = objectMapper.writeValueAsString(cardContent)
        val card = Card(cardId.id, workspace.name, "updated question", "updated response")
        Mockito.`when`(cardService.update(cardId, workspace, cardContent)).thenReturn(card)

        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/workspaces/${workspace.name}/cards/${cardId.id}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contentBody)
        ).andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.header().exists(HttpHeaders.LOCATION))
                .andExpect(MockMvcResultMatchers.header().string(HttpHeaders.LOCATION, "/workspaces/${workspace.name}/cards/${cardId.id}"))

        Mockito.verify(cardService).update(cardId, workspace, cardContent)
    }

    @Test
    fun `given a Workspace name, a Card Id, and a CardContent, when sending a PUT REST request to the cards endpoint and the CardService update method throws IllegalArgumentException, then an UNPROCESSABLE_ENTITY http status response is returned`() {
        val workspace = Workspace("workspaceTest")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val cardContent = CardContent("", "response")
        val contentBody = objectMapper.writeValueAsString(cardContent)
        Mockito.`when`(cardService.update(cardId, workspace, cardContent)).thenThrow(IllegalArgumentException::class.java)

        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/workspaces/${workspace.name}/cards/${cardId.id}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contentBody)
        ).andExpect(MockMvcResultMatchers.status().isUnprocessableEntity)
    }

    @Test
    fun `given a Workspace name, a Card Id, and a CardContent, when sending a PUT REST request to the cards endpoint and the Workspace does not exist, then a NOT_FOUND http status response is returned`() {
        val workspace = Workspace("workspaceTest")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val cardContent = CardContent("request", "response")
        val contentBody = objectMapper.writeValueAsString(cardContent)
        Mockito.`when`(cardService.update(cardId, workspace, cardContent)).thenThrow(WorkspaceNotExistsException(workspace))

        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/workspaces/${workspace.name}/cards/${cardId.id}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contentBody)
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    fun `given a Workspace name, a Card Id, and a CardContent, when sending a PUT REST request to the cards endpoint and the cardService update method throws InvalidWorkspaceNameException, then an UNPROCESSABLE_ENTITY http status response is returned`() {
        val workspace = Workspace("workspaceTest")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val cardContent = CardContent("request", "response")
        val contentBody = objectMapper.writeValueAsString(cardContent)
        Mockito.`when`(cardService.update(cardId, workspace, cardContent)).thenThrow(InvalidWorkspaceNameException("The provided workspaceName does not match with the card workspace"))

        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/workspaces/${workspace.name}/cards/${cardId.id}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contentBody)
        ).andExpect(MockMvcResultMatchers.status().isUnprocessableEntity)
    }

    @Test
    fun `given a Workspace name and a Card Id, when sending a DELETE REST request to the cards endpoint, then the delete method of CardService is called`() {
        val workspace = Workspace("workspaceTest")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")

        mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/workspaces/${workspace.name}/cards/${cardId.id}")
        ).andExpect(MockMvcResultMatchers.status().isOk)

        Mockito.verify(cardService).delete(cardId, workspace)
    }

    @Test
    fun `given a Workspace name and a Card Id, when sending a DELETE REST request to the cards endpoint and the CardService delete method throws IllegalArgumentException, then an UNPROCESSABLE_ENTITY http status response is returned`() {
        val workspace = Workspace("workspaceTest")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        Mockito.`when`(cardService.delete(cardId, workspace)).thenThrow(IllegalArgumentException::class.java)

        mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/workspaces/${workspace.name}/cards/${cardId.id}")
        ).andExpect(MockMvcResultMatchers.status().isUnprocessableEntity)
    }

    @Test
    fun `given a Workspace name and a Card Id, when sending a DELETE REST request to the cards endpoint and the Workspace does not exist, then a NOT_FOUND http status response is returned`() {
        val workspace = Workspace("workspaceTest")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        Mockito.`when`(cardService.delete(cardId, workspace)).thenThrow(WorkspaceNotExistsException(workspace))

        mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/workspaces/${workspace.name}/cards/${cardId.id}")
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    fun `given a Workspace name and a Card Id, when sending a DELETE REST request to the cards endpoint and the cardService update method throws InvalidWorkspaceNameException, then an UNPROCESSABLE_ENTITY http status response is returned`() {
        val workspace = Workspace("workspaceTest")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        Mockito.`when`(cardService.delete(cardId, workspace)).thenThrow(InvalidWorkspaceNameException("The provided workspaceName does not match with the card workspace"))

        mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/workspaces/${workspace.name}/cards/${cardId.id}")
        ).andExpect(MockMvcResultMatchers.status().isUnprocessableEntity)
    }

    @Test
    fun `given a Workspace name, when sending a GET REST request to the cards endpoint, then the findByWorkspace method of CardService is called and the retrieved Cards are returned`() {
        val workspace = Workspace("workspaceTest")
        val card = Card("9e493dc0-ef75-403f-b5d6-ed510634f8a6", workspace.name, "question", "response content")
        val cards = listOf(card)
        Mockito.`when`(cardService.findByWorkspace(workspace)).thenReturn(cards)
        val expectedContentBody = objectMapper.writeValueAsString(cards)

        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/workspaces/${workspace.name}/cards")
        ).andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.content().json(expectedContentBody))
    }

    @Test
    fun `given a Workspace name, when sending a GET REST request to the cards endpoint and the CardService throws WorkspaceNotExistsException exception, then an INTERNAL_SERVER_ERROR http status response is returned`() {
        val workspace = Workspace("workspaceTest")
        Mockito.`when`(cardService.findByWorkspace(workspace)).thenThrow(WorkspaceNotExistsException(workspace))

        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/workspaces/${workspace.name}/cards")
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }
}
