package de.mcella.spring.learntool.learn

import com.fasterxml.jackson.databind.ObjectMapper
import de.mcella.spring.learntool.UnitTest
import de.mcella.spring.learntool.WithMockUser
import de.mcella.spring.learntool.card.dto.Card
import de.mcella.spring.learntool.card.dto.CardId
import de.mcella.spring.learntool.card.exceptions.CardNotFoundException
import de.mcella.spring.learntool.config.AppProperties
import de.mcella.spring.learntool.learn.dto.EvaluationParameters
import de.mcella.spring.learntool.learn.dto.LearnCard
import de.mcella.spring.learntool.learn.exceptions.CardBindingException
import de.mcella.spring.learntool.learn.exceptions.InputValuesNotAcceptableException
import de.mcella.spring.learntool.learn.exceptions.LearnCardAlreadyExistsException
import de.mcella.spring.learntool.learn.exceptions.LearnCardNotFoundException
import de.mcella.spring.learntool.learn.exceptions.LearnCardsNotFoundException
import de.mcella.spring.learntool.learn.storage.LearnCardEntity
import de.mcella.spring.learntool.security.CustomUserDetailsService
import de.mcella.spring.learntool.security.TokenAuthenticationFilter
import de.mcella.spring.learntool.security.UserPrincipal
import de.mcella.spring.learntool.user.exceptions.UserNotAuthorizedException
import de.mcella.spring.learntool.workspace.dto.WorkspaceRequest
import de.mcella.spring.learntool.workspace.exceptions.WorkspaceNotExistsException
import java.time.Instant
import java.util.Collections
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
@WebMvcTest(LearnController::class)
@AutoConfigureWebClient
@AutoConfigureMockMvc(addFilters = false)
@EnableConfigurationProperties(AppProperties::class)
@TestPropertySource(properties = ["app.auth.tokenSecret=test", "app.auth.tokenExpirationMsec=123"])
class LearnControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var customUserDetailsService: CustomUserDetailsService

    @MockBean
    private lateinit var tokenAuthenticationFilter: TokenAuthenticationFilter

    @MockBean
    private lateinit var clientRegistrationRepository: ClientRegistrationRepository

    @MockBean
    private lateinit var learnService: LearnService

    private val objectMapper = ObjectMapper()

    @Test
    @WithMockUser
    fun `given a Workspace Id and a Card id, when sending a POST REST request to the learn endpoint and the Card exists, then the create method of LearnService is called`() {
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val userPrincipal = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        val learnCardEntity = LearnCardEntity.createInitial(cardId, workspaceRequest, Instant.now())
        val learnCard = LearnCard.create(learnCardEntity)
        Mockito.`when`(learnService.create(workspaceRequest, cardId, userPrincipal)).thenReturn(learnCard)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/workspaces/${workspaceRequest.id}/learn/${cardId.id}")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))

        Mockito.verify(learnService).create(workspaceRequest, cardId, userPrincipal)
    }

    @Test
    fun `given a Workspace Id and a Card id, when sending a POST REST request to the learn endpoint without JWT authentication, then an UNPROCESSABLE_ENTITY http status response is returned`() {
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")

        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/workspaces/${workspaceRequest.id}/learn/${cardId.id}")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isUnprocessableEntity)
    }

    @Test
    @WithMockUser
    fun `given a Workspace Id and a Card id, when sending a POST REST request to the learn endpoint and the learnService create method throws UserNotAuthorizedException, then a UNAUTHORIZED http status response is returned`() {
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val userPrincipal = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(learnService.create(workspaceRequest, cardId, userPrincipal)).thenThrow(UserNotAuthorizedException(userPrincipal))

        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/workspaces/${workspaceRequest.id}/learn/${cardId.id}")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

    @Test
    @WithMockUser
    fun `given a Workspace Id and a Card id, when sending a POST REST request to the learn endpoint and the LearnCard already exists, then a CONFLICT http status response is returned`() {
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val userPrincipal = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(learnService.create(workspaceRequest, cardId, userPrincipal)).thenThrow(LearnCardAlreadyExistsException(cardId))

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/workspaces/${workspaceRequest.id}/learn/${cardId.id}")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isConflict)
    }

    @Test
    @WithMockUser
    fun `given a Workspace Id and a Card id, when sending a POST REST request to the learn endpoint and the Card does not exist, then a NOT_FOUND http status response is returned`() {
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val userPrincipal = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(learnService.create(workspaceRequest, cardId, userPrincipal)).thenThrow(CardNotFoundException(cardId))

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/workspaces/${workspaceRequest.id}/learn/${cardId.id}")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    @WithMockUser
    fun `given a Workspace Id and a Card id, when sending a POST REST request to the learn endpoint and the Card belongs to a different Workspace, then a NOT_ACCEPTABLE http status response is returned`() {
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val userPrincipal = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(learnService.create(workspaceRequest, cardId, userPrincipal)).thenThrow(CardBindingException(workspaceRequest, cardId))

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/workspaces/${workspaceRequest.id}/learn/${cardId.id}")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isNotAcceptable)
    }

    @Test
    @WithMockUser
    fun `given a Workspace Id, when sending a GET REST request to the learn endpoint and the Workspace exists, then the getCard method of LearnService is called and a Card is returned`() {
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val userPrincipal = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        val card = Card("9e493dc0-ef75-403f-b5d6-ed510634f8a6", workspaceRequest.id, "question", "response")
        Mockito.`when`(learnService.getCard(workspaceRequest, userPrincipal)).thenReturn(card)
        val expectedContentBody = objectMapper.writeValueAsString(card)

        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/workspaces/${workspaceRequest.id}/learn")
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(MockMvcResultMatchers.content().json(expectedContentBody))

        Mockito.verify(learnService).getCard(workspaceRequest, userPrincipal)
    }

    @Test
    fun `given a Workspace Id, when sending a GET REST request to the learn endpoint without JWT authentication, then an UNPROCESSABLE_ENTITY http status response is returned`() {
        val workspaceRequest = WorkspaceRequest("workspaceId")

        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/workspaces/${workspaceRequest.id}/learn")
        ).andExpect(MockMvcResultMatchers.status().isUnprocessableEntity)
    }

    @Test
    @WithMockUser
    fun `given a Workspace Id, when sending a GET REST request to the learn endpoint method and the learnService getCard method throws UserNotAuthorizedException, then a UNAUTHORIZED http status response is returned`() {
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val userPrincipal = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(learnService.getCard(workspaceRequest, userPrincipal)).thenThrow(UserNotAuthorizedException(userPrincipal))

        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/workspaces/${workspaceRequest.id}/learn")
        ).andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

    @Test
    @WithMockUser
    fun `given a Workspace Id, when sending a GET REST request to the learn endpoint and the Workspace does not exist, then a NOT_FOUND http status response is returned`() {
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val userPrincipal = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(learnService.getCard(workspaceRequest, userPrincipal)).thenThrow(WorkspaceNotExistsException(workspaceRequest))

        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/workspaces/${workspaceRequest.id}/learn")
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    @WithMockUser
    fun `given a Workspace Id, when sending a GET REST request to the learn endpoint and no Cards exist into the Workspace, then a NOT_FOUND http status response is returned`() {
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val userPrincipal = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(learnService.getCard(workspaceRequest, userPrincipal)).thenThrow(LearnCardsNotFoundException(workspaceRequest))

        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/workspaces/${workspaceRequest.id}/learn")
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    @WithMockUser
    fun `given a Workspace Id and the evaluation parameters, when sending a PUT REST request to the learn endpoint and the Workspace exists and the Card exists, then the evaluateCard method of LearnService is called`() {
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val evaluationParameters = EvaluationParameters(5)
        val userPrincipal = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        val contentBody = objectMapper.writeValueAsString(evaluationParameters)
        val learnCardEntity = LearnCardEntity.createInitial(cardId, workspaceRequest, Instant.now())
        val learnCard = LearnCard.create(learnCardEntity)
        Mockito.`when`(learnService.evaluateCard(workspaceRequest, cardId, evaluationParameters, userPrincipal)).thenReturn(learnCard)

        mockMvc.perform(
            MockMvcRequestBuilders.put("/api/workspaces/${workspaceRequest.id}/learn/${cardId.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(contentBody)
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))

        Mockito.verify(learnService).evaluateCard(workspaceRequest, cardId, evaluationParameters, userPrincipal)
    }

    @Test
    fun `given a Workspace Id and the evaluation parameters, when sending a PUT REST request to the learn endpoint without JWT authentication, then an UNPROCESSABLE_ENTITY http status response is returned`() {
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val evaluationParameters = EvaluationParameters(5)
        val contentBody = objectMapper.writeValueAsString(evaluationParameters)

        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/workspaces/${workspaceRequest.id}/learn/${cardId.id}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contentBody)
        ).andExpect(MockMvcResultMatchers.status().isUnprocessableEntity)
    }

    @Test
    @WithMockUser
    fun `given a Workspace Id and the evaluation parameters, when sending a PUT REST to the learn endpoint and the learnService evaluateCard method throws UserNotAuthorizedException, then a UNAUTHORIZED http status response is returned`() {
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val evaluationParameters = EvaluationParameters(5)
        val userPrincipal = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        val contentBody = objectMapper.writeValueAsString(evaluationParameters)
        Mockito.`when`(learnService.evaluateCard(workspaceRequest, cardId, evaluationParameters, userPrincipal)).thenThrow(UserNotAuthorizedException(userPrincipal))

        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/workspaces/${workspaceRequest.id}/learn/${cardId.id}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contentBody)
        ).andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

    @Test
    @WithMockUser
    fun `given a Workspace Id and the evaluation parameters, when sending a PUT REST request to the learn endpoint and the Workspace does not exist, then a NOT_FOUND http status response is returned`() {
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val evaluationParameters = EvaluationParameters(5)
        val userPrincipal = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        val contentBody = objectMapper.writeValueAsString(evaluationParameters)
        Mockito.`when`(learnService.evaluateCard(workspaceRequest, cardId, evaluationParameters, userPrincipal)).thenThrow(WorkspaceNotExistsException(workspaceRequest))

        mockMvc.perform(
            MockMvcRequestBuilders.put("/api/workspaces/${workspaceRequest.id}/learn/${cardId.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(contentBody)
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    @WithMockUser
    fun `given a Workspace Id and the evaluation parameters, when sending a PUT REST request to the learn endpoint and the Card does not exist, then a NOT_FOUND http status response is returned`() {
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val evaluationParameters = EvaluationParameters(5)
        val userPrincipal = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        val contentBody = objectMapper.writeValueAsString(evaluationParameters)
        Mockito.`when`(learnService.evaluateCard(workspaceRequest, cardId, evaluationParameters, userPrincipal)).thenThrow(CardNotFoundException(cardId))

        mockMvc.perform(
            MockMvcRequestBuilders.put("/api/workspaces/${workspaceRequest.id}/learn/${cardId.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(contentBody)
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    @WithMockUser
    fun `given a Workspace Id and the evaluation parameters, when sending a PUT REST request to the learn endpoint and the Card exists but the Card does not belong to the Workspace, then a NOT_ACCEPTABLE http status response is returned`() {
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val evaluationParameters = EvaluationParameters(5)
        val userPrincipal = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        val contentBody = objectMapper.writeValueAsString(evaluationParameters)
        Mockito.`when`(learnService.evaluateCard(workspaceRequest, cardId, evaluationParameters, userPrincipal)).thenThrow(CardBindingException(workspaceRequest, cardId))

        mockMvc.perform(
            MockMvcRequestBuilders.put("/api/workspaces/${workspaceRequest.id}/learn/${cardId.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(contentBody)
        ).andExpect(MockMvcResultMatchers.status().isNotAcceptable)
    }

    @Test
    @WithMockUser
    fun `given a Workspace Id and the evaluation parameters with quality equals to 10, when sending a PUT REST request to the learn endpoint, then a NOT_ACCEPTABLE http status response is returned`() {
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val evaluationParameters = EvaluationParameters(10)
        val userPrincipal = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        val contentBody = objectMapper.writeValueAsString(evaluationParameters)
        Mockito.`when`(learnService.evaluateCard(workspaceRequest, cardId, evaluationParameters, userPrincipal)).thenThrow(InputValuesNotAcceptableException(""))

        mockMvc.perform(
            MockMvcRequestBuilders.put("/api/workspaces/${workspaceRequest.id}/learn/${cardId.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(contentBody)
        ).andExpect(MockMvcResultMatchers.status().isNotAcceptable)
    }

    @Test
    fun `given a Workspace Id and a Card id, when sending a DELETE REST request to the learn endpoint without JWT authentication, then an UNPROCESSABLE_ENTITY http status response is returned`() {
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")

        mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/workspaces/${workspaceRequest.id}/learn/${cardId.id}")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isUnprocessableEntity)
    }

    @Test
    @WithMockUser
    fun `given a Workspace Id and a Card id, when sending a DELETE REST request to the learn endpoint and the learnService delete method throws UserNotAuthorizedException, then a UNAUTHORIZED http status response is returned`() {
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val userPrincipal = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(learnService.delete(workspaceRequest, cardId, userPrincipal)).thenThrow(UserNotAuthorizedException(userPrincipal))

        mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/workspaces/${workspaceRequest.id}/learn/${cardId.id}")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

    @Test
    @WithMockUser
    fun `given a Workspace Id and a Card id, when sending a DELETE REST request to the learn endpoint and the Card does not exist, then a NOT_FOUND http status response is returned`() {
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val userPrincipal = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(learnService.delete(workspaceRequest, cardId, userPrincipal)).thenThrow(CardNotFoundException(cardId))

        mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/workspaces/${workspaceRequest.id}/learn/${cardId.id}")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    @WithMockUser
    fun `given a Workspace Id and a Card id, when sending a DELETE REST request to the learn endpoint and the Card does not belong to the Workspace, then a NOT_ACCEPTABLE http status response is returned`() {
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val userPrincipal = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(learnService.delete(workspaceRequest, cardId, userPrincipal)).thenThrow(CardBindingException(workspaceRequest, cardId))

        mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/workspaces/${workspaceRequest.id}/learn/${cardId.id}")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isNotAcceptable)
    }

    @Test
    @WithMockUser
    fun `given a Workspace Id and a Card id, when sending a DELETE REST request to the learn endpoint and the LearnCard does not exist, then a NOT_FOUND http status response is returned`() {
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val userPrincipal = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(learnService.delete(workspaceRequest, cardId, userPrincipal)).thenThrow(LearnCardNotFoundException(workspaceRequest, cardId))

        mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/workspaces/${workspaceRequest.id}/learn/${cardId.id}")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    @WithMockUser
    fun `given a Workspace Id and a Card id, when sending a DELETE REST request to the learn endpoint, then the method delete of LearnService is called and an OK http status response is returned`() {
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val userPrincipal = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())

        mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/workspaces/${workspaceRequest.id}/learn/${cardId.id}")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isOk)

        Mockito.verify(learnService).delete(workspaceRequest, cardId, userPrincipal)
    }
}
