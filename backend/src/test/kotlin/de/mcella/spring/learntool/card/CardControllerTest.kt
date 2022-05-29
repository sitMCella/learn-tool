package de.mcella.spring.learntool.card

import com.fasterxml.jackson.databind.ObjectMapper
import de.mcella.spring.learntool.UnitTest
import de.mcella.spring.learntool.WithMockUser
import de.mcella.spring.learntool.card.dto.Card
import de.mcella.spring.learntool.card.dto.CardContent
import de.mcella.spring.learntool.card.dto.CardId
import de.mcella.spring.learntool.card.dto.CardPagination
import de.mcella.spring.learntool.card.exceptions.CardAlreadyExistsException
import de.mcella.spring.learntool.config.AppProperties
import de.mcella.spring.learntool.security.CustomUserDetailsService
import de.mcella.spring.learntool.security.TokenAuthenticationFilter
import de.mcella.spring.learntool.security.UserPrincipal
import de.mcella.spring.learntool.user.exceptions.UserNotAuthorizedException
import de.mcella.spring.learntool.workspace.dto.WorkspaceRequest
import de.mcella.spring.learntool.workspace.exceptions.InvalidWorkspaceIdException
import de.mcella.spring.learntool.workspace.exceptions.WorkspaceNotExistsException
import java.lang.IllegalArgumentException
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@RunWith(SpringRunner::class)
@Category(UnitTest::class)
@WebMvcTest(CardController::class)
@AutoConfigureWebClient
@AutoConfigureMockMvc(addFilters = false)
@EnableConfigurationProperties(AppProperties::class)
@TestPropertySource(properties = ["app.auth.tokenSecret=test", "app.auth.tokenExpirationMsec=123"])
class CardControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var customUserDetailsService: CustomUserDetailsService

    @MockBean
    private lateinit var tokenAuthenticationFilter: TokenAuthenticationFilter

    @MockBean
    private lateinit var clientRegistrationRepository: ClientRegistrationRepository

    @MockBean
    private lateinit var cardService: CardService

    @MockBean
    private lateinit var cardImportService: CardImportService

    private val objectMapper = ObjectMapper()

    @Test
    @WithMockUser
    fun `given a Workspace id and a CardContent, when sending a POST REST request to the cards endpoint, then the create method of CardService is called`() {
        val workspace = WorkspaceRequest("workspaceId")
        val cardContent = CardContent("question", "response")
        val contentBody = objectMapper.writeValueAsString(cardContent)
        val card = Card("9e493dc0-ef75-403f-b5d6-ed510634f8a6", workspace.id, "question", "response")
        val user = UserPrincipal(1L, "test@google.com", "PassW@rD!", listOf(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(cardService.create(workspace, cardContent, user)).thenReturn(card)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/workspaces/${workspace.id}/cards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(contentBody)
        ).andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.header().exists(HttpHeaders.LOCATION))
            .andExpect(MockMvcResultMatchers.header().string(HttpHeaders.LOCATION, "/workspaces/${workspace.id}/cards/${card.id}"))

        Mockito.verify(cardService).create(workspace, cardContent, user)
    }

    @Test
    fun `when sending a POST REST request to cards endpoint without JWT authentication, then an UNPROCESSABLE_ENTITY http status response is returned`() {
        val workspace = WorkspaceRequest("workspaceId")
        val cardContent = CardContent("question", "response")
        val contentBody = objectMapper.writeValueAsString(cardContent)

        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/workspaces/${workspace.id}/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contentBody)
        ).andExpect(MockMvcResultMatchers.status().isUnprocessableEntity)
    }

    @Test
    @WithMockUser
    fun `given a Workspace id and a CardContent, when sending a POST REST request to the cards endpoint and the create method of CardService throws IllegalArgumentException, then an UNPROCESSABLE_ENTITY http status response is returned`() {
        val workspace = WorkspaceRequest("workspaceId")
        val cardContent = CardContent("", "response")
        val contentBody = objectMapper.writeValueAsString(cardContent)
        val user = UserPrincipal(1L, "test@google.com", "PassW@rD!", listOf(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(cardService.create(workspace, cardContent, user)).thenThrow(IllegalArgumentException::class.java)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/workspaces/${workspace.id}/cards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(contentBody)
        ).andExpect(MockMvcResultMatchers.status().isUnprocessableEntity)
    }

    @Test
    @WithMockUser
    fun `given a Workspace id and a CardContent, when sending a POST REST request to the cards endpoint and the Workspace does not exist, then a NOT_FOUND http status response is returned`() {
        val workspace = WorkspaceRequest("workspaceId")
        val cardContent = CardContent("request", "response")
        val contentBody = objectMapper.writeValueAsString(cardContent)
        val user = UserPrincipal(1L, "test@google.com", "PassW@rD!", listOf(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(cardService.create(workspace, cardContent, user)).thenThrow(WorkspaceNotExistsException(workspace))

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/workspaces/${workspace.id}/cards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(contentBody)
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    @WithMockUser
    fun `given a Workspace id and a CardContent, when sending a POST REST request to the cards endpoint and the Card already exists, then a CONFLICT http status response is returned`() {
        val workspace = WorkspaceRequest("workspaceId")
        val cardContent = CardContent("request", "response")
        val contentBody = objectMapper.writeValueAsString(cardContent)
        val user = UserPrincipal(1L, "test@google.com", "PassW@rD!", listOf(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(cardService.create(workspace, cardContent, user)).thenThrow(CardAlreadyExistsException(CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")))

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/workspaces/${workspace.id}/cards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(contentBody)
        ).andExpect(MockMvcResultMatchers.status().isConflict)
    }

    @Test
    @WithMockUser
    fun `given a Workspace id and a CardContent, when sending a POST REST request to the cards endpoint and the cardService create method throws UserNotAuthorizedException, then a UNAUTHORIZED http status response is returned`() {
        val workspace = WorkspaceRequest("workspaceId")
        val cardContent = CardContent("request", "response")
        val contentBody = objectMapper.writeValueAsString(cardContent)
        val user = UserPrincipal(1L, "test@google.com", "PassW@rD!", listOf(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(cardService.create(workspace, cardContent, user)).thenThrow(UserNotAuthorizedException(user))

        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/workspaces/${workspace.id}/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contentBody)
        ).andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

    @Test
    @WithMockUser
    fun `given a Workspace name, a Card Id and a CardContent, when sending a PUT REST request to the cards endpoint, then the update method of CardService is called`() {
        val workspace = WorkspaceRequest("workspaceId")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val cardContent = CardContent("updated question", "updated response")
        val contentBody = objectMapper.writeValueAsString(cardContent)
        val user = UserPrincipal(1L, "test@google.com", "PassW@rD!", listOf(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        val card = Card(cardId.id, workspace.id, "updated question", "updated response")
        Mockito.`when`(cardService.update(cardId, workspace, cardContent, user)).thenReturn(card)

        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/workspaces/${workspace.id}/cards/${cardId.id}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contentBody)
        ).andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.header().exists(HttpHeaders.LOCATION))
                .andExpect(MockMvcResultMatchers.header().string(HttpHeaders.LOCATION, "/workspaces/${workspace.id}/cards/${cardId.id}"))

        Mockito.verify(cardService).update(cardId, workspace, cardContent, user)
    }

    @Test
    fun `when sending a PUT REST request to cards endpoint without JWT authentication, then an UNPROCESSABLE_ENTITY http status response is returned`() {
        val workspace = WorkspaceRequest("workspaceId")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val cardContent = CardContent("updated question", "updated response")
        val contentBody = objectMapper.writeValueAsString(cardContent)

        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/workspaces/${workspace.id}/cards/${cardId.id}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contentBody)
        ).andExpect(MockMvcResultMatchers.status().isUnprocessableEntity)
    }

    @Test
    @WithMockUser
    fun `given a Workspace id, a Card Id, and a CardContent, when sending a PUT REST request to the cards endpoint and the CardService update method throws IllegalArgumentException, then an UNPROCESSABLE_ENTITY http status response is returned`() {
        val workspace = WorkspaceRequest("workspaceId")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val cardContent = CardContent("", "response")
        val contentBody = objectMapper.writeValueAsString(cardContent)
        val user = UserPrincipal(1L, "test@google.com", "PassW@rD!", listOf(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(cardService.update(cardId, workspace, cardContent, user)).thenThrow(IllegalArgumentException::class.java)

        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/workspaces/${workspace.id}/cards/${cardId.id}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contentBody)
        ).andExpect(MockMvcResultMatchers.status().isUnprocessableEntity)
    }

    @Test
    @WithMockUser
    fun `given a Workspace id, a Card Id, and a CardContent, when sending a PUT REST request to the cards endpoint and the Workspace does not exist, then a NOT_FOUND http status response is returned`() {
        val workspace = WorkspaceRequest("workspaceId")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val cardContent = CardContent("request", "response")
        val contentBody = objectMapper.writeValueAsString(cardContent)
        val user = UserPrincipal(1L, "test@google.com", "PassW@rD!", listOf(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(cardService.update(cardId, workspace, cardContent, user)).thenThrow(WorkspaceNotExistsException(workspace))

        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/workspaces/${workspace.id}/cards/${cardId.id}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contentBody)
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    @WithMockUser
    fun `given a Workspace id, a Card Id, and a CardContent, when sending a PUT REST request to the cards endpoint and the cardService update method throws UserNotAuthorizedException, then a UNAUTHORIZED http status response is returned`() {
        val workspace = WorkspaceRequest("workspaceId")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val cardContent = CardContent("request", "response")
        val contentBody = objectMapper.writeValueAsString(cardContent)
        val user = UserPrincipal(1L, "test@google.com", "PassW@rD!", listOf(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(cardService.update(cardId, workspace, cardContent, user)).thenThrow(UserNotAuthorizedException(user))

        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/workspaces/${workspace.id}/cards/${cardId.id}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contentBody)
        ).andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

    @Test
    @WithMockUser
    fun `given a Workspace id, a Card Id, and a CardContent, when sending a PUT REST request to the cards endpoint and the cardService update method throws InvalidWorkspaceIdException, then an UNPROCESSABLE_ENTITY http status response is returned`() {
        val workspace = WorkspaceRequest("workspaceId")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val cardContent = CardContent("request", "response")
        val contentBody = objectMapper.writeValueAsString(cardContent)
        val user = UserPrincipal(1L, "test@google.com", "PassW@rD!", listOf(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(cardService.update(cardId, workspace, cardContent, user)).thenThrow(InvalidWorkspaceIdException("The provided Workspace Id does not match with the Card Workspace Id"))

        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/workspaces/${workspace.id}/cards/${cardId.id}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contentBody)
        ).andExpect(MockMvcResultMatchers.status().isUnprocessableEntity)
    }

    @Test
    @WithMockUser
    fun `given a Workspace id and a Card Id, when sending a DELETE REST request to the cards endpoint, then the delete method of CardService is called`() {
        val workspace = WorkspaceRequest("workspaceId")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val user = UserPrincipal(1L, "test@google.com", "PassW@rD!", listOf(SimpleGrantedAuthority("ROLE_USER")), emptyMap())

        mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/workspaces/${workspace.id}/cards/${cardId.id}")
        ).andExpect(MockMvcResultMatchers.status().isOk)

        Mockito.verify(cardService).delete(cardId, workspace, user)
    }

    @Test
    fun `when sending a DELETE REST request to cards endpoint without JWT authentication, then an UNPROCESSABLE_ENTITY http status response is returned`() {
        val workspace = WorkspaceRequest("workspaceId")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")

        mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/workspaces/${workspace.id}/cards/${cardId.id}")
        ).andExpect(MockMvcResultMatchers.status().isUnprocessableEntity)
    }

    @Test
    @WithMockUser
    fun `given a Workspace id and a Card Id, when sending a DELETE REST request to the cards endpoint and the CardService delete method throws IllegalArgumentException, then an UNPROCESSABLE_ENTITY http status response is returned`() {
        val workspace = WorkspaceRequest("workspaceId")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val user = UserPrincipal(1L, "test@google.com", "PassW@rD!", listOf(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(cardService.delete(cardId, workspace, user)).thenThrow(IllegalArgumentException::class.java)

        mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/workspaces/${workspace.id}/cards/${cardId.id}")
        ).andExpect(MockMvcResultMatchers.status().isUnprocessableEntity)
    }

    @Test
    @WithMockUser
    fun `given a Workspace id and a Card Id, when sending a DELETE REST request to the cards endpoint and the Workspace does not exist, then a NOT_FOUND http status response is returned`() {
        val workspace = WorkspaceRequest("workspaceTest")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val user = UserPrincipal(1L, "test@google.com", "PassW@rD!", listOf(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(cardService.delete(cardId, workspace, user)).thenThrow(WorkspaceNotExistsException(workspace))

        mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/workspaces/${workspace.id}/cards/${cardId.id}")
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    @WithMockUser
    fun `given a Workspace id and a Card Id, when sending a DELETE REST request to the cards endpoint and the cardService update method throws UserNotAuthorizedException, then an UNAUTHORIZED http status response is returned`() {
        val workspace = WorkspaceRequest("workspaceId")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val user = UserPrincipal(1L, "test@google.com", "PassW@rD!", listOf(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(cardService.delete(cardId, workspace, user)).thenThrow(UserNotAuthorizedException(user))

        mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/workspaces/${workspace.id}/cards/${cardId.id}")
        ).andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

    @Test
    @WithMockUser
    fun `given a Workspace id and a Card Id, when sending a DELETE REST request to the cards endpoint and the cardService update method throws InvalidWorkspaceIdException, then an UNPROCESSABLE_ENTITY http status response is returned`() {
        val workspace = WorkspaceRequest("workspaceId")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val user = UserPrincipal(1L, "test@google.com", "PassW@rD!", listOf(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(cardService.delete(cardId, workspace, user)).thenThrow(InvalidWorkspaceIdException("The provided Workspace Id does not match with the Card Workspace Id"))

        mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/workspaces/${workspace.id}/cards/${cardId.id}")
        ).andExpect(MockMvcResultMatchers.status().isUnprocessableEntity)
    }

    @Test
    @WithMockUser
    fun `given a Workspace id, when sending a GET REST request to the cards endpoint without pagination query parameters, then the findByWorkspace method of CardService is called and the retrieved Cards are returned`() {
        val workspace = WorkspaceRequest("workspaceId")
        val cardPagination = CardPagination(0, 20)
        val user = UserPrincipal(1L, "test@google.com", "PassW@rD!", listOf(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        val card = Card("9e493dc0-ef75-403f-b5d6-ed510634f8a6", workspace.id, "question", "response content")
        val cards = listOf(card)
        Mockito.`when`(cardService.findByWorkspace(workspace, cardPagination, user)).thenReturn(cards)
        val expectedContentBody = objectMapper.writeValueAsString(cards)

        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/workspaces/${workspace.id}/cards")
        ).andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.content().json(expectedContentBody))
    }

    @Test
    fun `when sending a GET REST request to cards endpoint without JWT authentication, then an UNPROCESSABLE_ENTITY http status response is returned`() {
        val workspace = WorkspaceRequest("workspaceId")

        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/workspaces/${workspace.id}/cards")
        ).andExpect(MockMvcResultMatchers.status().isUnprocessableEntity)
    }

    @Test
    @WithMockUser
    fun `given a Workspace id, when sending a GET REST request to the cards endpoint and the CardService findByWorkspace method throws WorkspaceNotExistsException exception, then an INTERNAL_SERVER_ERROR http status response is returned`() {
        val workspace = WorkspaceRequest("workspaceId")
        val cardPagination = CardPagination(0, 20)
        val user = UserPrincipal(1L, "test@google.com", "PassW@rD!", listOf(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(cardService.findByWorkspace(workspace, cardPagination, user)).thenThrow(WorkspaceNotExistsException(workspace))

        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/workspaces/${workspace.id}/cards")
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    @WithMockUser
    fun `given a Workspace id, when sending a GET REST request to the cards endpoint and the cardService findByWorkspace method throws UserNotAuthorizedException, then an UNAUTHORIZED http status response is returned`() {
        val workspace = WorkspaceRequest("workspaceId")
        val cardPagination = CardPagination(0, 20)
        val user = UserPrincipal(1L, "test@google.com", "PassW@rD!", listOf(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(cardService.findByWorkspace(workspace, cardPagination, user)).thenThrow(UserNotAuthorizedException(user))

        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/workspaces/${workspace.id}/cards")
        ).andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

    @Test
    @WithMockUser
    fun `given a Workspace id, when sending a GET REST request to the cards endpoint with page query parameter, then the findByWorkspace method of CardService is called and the retrieved Cards are returned`() {
        val workspace = WorkspaceRequest("workspaceId")
        val cardPagination = CardPagination(2, 20)
        val user = UserPrincipal(1L, "test@google.com", "PassW@rD!", listOf(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        val card = Card("9e493dc0-ef75-403f-b5d6-ed510634f8a6", workspace.id, "question", "response content")
        val cards = listOf(card)
        Mockito.`when`(cardService.findByWorkspace(workspace, cardPagination, user)).thenReturn(cards)
        val expectedContentBody = objectMapper.writeValueAsString(cards)

        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/workspaces/${workspace.id}/cards?page=2")
        ).andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.content().json(expectedContentBody))
    }

    @Test
    @WithMockUser
    fun `given a Workspace id, when sending a GET REST request to the cards endpoint with page and size query parameters, then the findByWorkspace method of CardService is called and the retrieved Cards are returned`() {
        val workspace = WorkspaceRequest("workspaceId")
        val cardPagination = CardPagination(0, 10)
        val user = UserPrincipal(1L, "test@google.com", "PassW@rD!", listOf(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        val card = Card("9e493dc0-ef75-403f-b5d6-ed510634f8a6", workspace.id, "question", "response content")
        val cards = listOf(card)
        Mockito.`when`(cardService.findByWorkspace(workspace, cardPagination, user)).thenReturn(cards)
        Mockito.`when`(cardService.countByWorkspace(workspace, user)).thenReturn(1L)
        val expectedContentBody = objectMapper.writeValueAsString(cards)

        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/workspaces/${workspace.id}/cards?page=0&size=10")
        ).andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.content().json(expectedContentBody))
    }

    @Test
    @WithMockUser
    fun `given a Workspace id, when sending a GET REST request to the cards endpoint with page and size query parameters, then the countByWorkspace method of CardService is called and the count of Cards is returned as Header`() {
        val workspace = WorkspaceRequest("workspaceId")
        val cardPagination = CardPagination(0, 10)
        val user = UserPrincipal(1L, "test@google.com", "PassW@rD!", listOf(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        val card = Card("9e493dc0-ef75-403f-b5d6-ed510634f8a6", workspace.id, "question", "response content")
        val cards = listOf(card)
        Mockito.`when`(cardService.findByWorkspace(workspace, cardPagination, user)).thenReturn(cards)
        Mockito.`when`(cardService.countByWorkspace(workspace, user)).thenReturn(1L)

        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/workspaces/${workspace.id}/cards?page=0&size=10")
        ).andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.header().longValue("count", 1L))
    }
}
