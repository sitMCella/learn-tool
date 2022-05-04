package de.mcella.spring.learntool.learn

import com.fasterxml.jackson.databind.ObjectMapper
import de.mcella.spring.learntool.UnitTest
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
import de.mcella.spring.learntool.security.oauth2.CustomOAuth2UserService
import de.mcella.spring.learntool.security.oauth2.HttpCookieOAuth2AuthorizationRequestRepository
import de.mcella.spring.learntool.security.oauth2.OAuth2AuthenticationFailureHandler
import de.mcella.spring.learntool.security.oauth2.OAuth2AuthenticationSuccessHandler
import de.mcella.spring.learntool.workspace.dto.WorkspaceRequest
import de.mcella.spring.learntool.workspace.exceptions.WorkspaceNotExistsException
import java.time.Instant
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
    private lateinit var customOAuth2UserService: CustomOAuth2UserService

    @MockBean
    private lateinit var oAuth2AuthenticationSuccessHandler: OAuth2AuthenticationSuccessHandler

    @MockBean
    private lateinit var oAuth2AuthenticationFailureHandler: OAuth2AuthenticationFailureHandler

    @MockBean
    private lateinit var httpCookieOAuth2AuthorizationRequestRepository: HttpCookieOAuth2AuthorizationRequestRepository

    @MockBean
    private lateinit var tokenAuthenticationFilter: TokenAuthenticationFilter

    @MockBean
    private lateinit var clientRegistrationRepository: ClientRegistrationRepository

    @MockBean
    private lateinit var learnService: LearnService

    private val objectMapper = ObjectMapper()

    @Test
    fun `given a Workspace name and a Card id, when sending a POST REST request to the learn endpoint and the Card exists, then the create method of LearnService is called`() {
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val learnCardEntity = LearnCardEntity.createInitial(cardId, workspaceRequest, Instant.now())
        val learnCard = LearnCard.create(learnCardEntity)
        Mockito.`when`(learnService.create(workspaceRequest, cardId)).thenReturn(learnCard)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/workspaces/${workspaceRequest.name}/learn/${cardId.id}")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))

        Mockito.verify(learnService).create(workspaceRequest, cardId)
    }

    @Test
    fun `given a Workspace name and a Card id, when sending a POST REST request to the learn endpoint and the Card exists and the LearnCard already exists, then a CONFLICT http status response is returned`() {
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        Mockito.`when`(learnService.create(workspaceRequest, cardId)).thenThrow(LearnCardAlreadyExistsException(cardId))

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/workspaces/${workspaceRequest.name}/learn/${cardId.id}")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isConflict)
    }

    @Test
    fun `given a Workspace name and a Card id, when sending a POST REST request to the learn endpoint and the Card does not exist, then a NOT_FOUND http status response is returned`() {
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        Mockito.`when`(learnService.create(workspaceRequest, cardId)).thenThrow(CardNotFoundException(cardId))

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/workspaces/${workspaceRequest.name}/learn/${cardId.id}")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    fun `given a Workspace name and a Card id, when sending a POST REST request to the learn endpoint and the Card belongs to a different Workspace, then a NOT_ACCEPTABLE http status response is returned`() {
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        Mockito.`when`(learnService.create(workspaceRequest, cardId)).thenThrow(CardBindingException(workspaceRequest, cardId))

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/workspaces/${workspaceRequest.name}/learn/${cardId.id}")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isNotAcceptable)
    }

    @Test
    fun `given a Workspace name, when sending a GET REST request to the learn endpoint and the Workspace exists, then the getCard method of LearnService is called and a Card is returned`() {
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val card = Card("9e493dc0-ef75-403f-b5d6-ed510634f8a6", workspaceRequest.name, "question", "response")
        Mockito.`when`(learnService.getCard(workspaceRequest)).thenReturn(card)
        val expectedContentBody = objectMapper.writeValueAsString(card)

        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/workspaces/${workspaceRequest.name}/learn")
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(MockMvcResultMatchers.content().json(expectedContentBody))

        Mockito.verify(learnService).getCard(workspaceRequest)
    }

    @Test
    fun `given a Workspace name, when sending a GET REST request to the learn endpoint and the Workspace does not exist, then a NOT_FOUND http status response is returned`() {
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        Mockito.`when`(learnService.getCard(workspaceRequest)).thenThrow(WorkspaceNotExistsException(workspaceRequest))

        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/workspaces/${workspaceRequest.name}/learn")
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    fun `given a Workspace name, when sending a GET REST request to the learn endpoint and no Cards exist into the Workspace, then a NOT_FOUND http status response is returned`() {
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        Mockito.`when`(learnService.getCard(workspaceRequest)).thenThrow(LearnCardsNotFoundException(workspaceRequest))

        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/workspaces/${workspaceRequest.name}/learn")
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    fun `given a Workspace name and the evaluation parameters, when sending a PUT REST request to the learn endpoint and the Workspace exists and the Card exists, then the evaluateCard method of LearnService is called`() {
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val evaluationParameters = EvaluationParameters(5)
        val contentBody = objectMapper.writeValueAsString(evaluationParameters)
        val learnCardEntity = LearnCardEntity.createInitial(cardId, workspaceRequest, Instant.now())
        val learnCard = LearnCard.create(learnCardEntity)
        Mockito.`when`(learnService.evaluateCard(workspaceRequest, cardId, evaluationParameters)).thenReturn(learnCard)

        mockMvc.perform(
            MockMvcRequestBuilders.put("/api/workspaces/${workspaceRequest.name}/learn/${cardId.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(contentBody)
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))

        Mockito.verify(learnService).evaluateCard(workspaceRequest, cardId, evaluationParameters)
    }

    @Test
    fun `given a Workspace name and the evaluation parameters, when sending a PUT REST request to the learn endpoint and the Workspace does not exist, then a NOT_FOUND http status response is returned`() {
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val evaluationParameters = EvaluationParameters(5)
        val contentBody = objectMapper.writeValueAsString(evaluationParameters)
        Mockito.`when`(learnService.evaluateCard(workspaceRequest, cardId, evaluationParameters)).thenThrow(WorkspaceNotExistsException(workspaceRequest))

        mockMvc.perform(
            MockMvcRequestBuilders.put("/api/workspaces/${workspaceRequest.name}/learn/${cardId.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(contentBody)
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    fun `given a Workspace name and the evaluation parameters, when sending a PUT REST request to the learn endpoint and the Workspace exists and the Card does not exist, then a NOT_FOUND http status response is returned`() {
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val evaluationParameters = EvaluationParameters(5)
        val contentBody = objectMapper.writeValueAsString(evaluationParameters)
        Mockito.`when`(learnService.evaluateCard(workspaceRequest, cardId, evaluationParameters)).thenThrow(CardNotFoundException(cardId))

        mockMvc.perform(
            MockMvcRequestBuilders.put("/api/workspaces/${workspaceRequest.name}/learn/${cardId.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(contentBody)
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    fun `given a Workspace name and the evaluation parameters, when sending a PUT REST request to the learn endpoint and the Workspace exists and the Card exists but the Card does not belong to the Workspace, then a NOT_ACCEPTABLE http status response is returned`() {
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val evaluationParameters = EvaluationParameters(5)
        val contentBody = objectMapper.writeValueAsString(evaluationParameters)
        Mockito.`when`(learnService.evaluateCard(workspaceRequest, cardId, evaluationParameters)).thenThrow(CardBindingException(workspaceRequest, cardId))

        mockMvc.perform(
            MockMvcRequestBuilders.put("/api/workspaces/${workspaceRequest.name}/learn/${cardId.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(contentBody)
        ).andExpect(MockMvcResultMatchers.status().isNotAcceptable)
    }

    @Test
    fun `given a Workspace name and the evaluation parameters with quality equals to 10, when sending a PUT REST request to the learn endpoint and the Workspace exists and the Card exists, then a NOT_ACCEPTABLE http status response is returned`() {
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val evaluationParameters = EvaluationParameters(10)
        val contentBody = objectMapper.writeValueAsString(evaluationParameters)
        Mockito.`when`(learnService.evaluateCard(workspaceRequest, cardId, evaluationParameters)).thenThrow(InputValuesNotAcceptableException(""))

        mockMvc.perform(
            MockMvcRequestBuilders.put("/api/workspaces/${workspaceRequest.name}/learn/${cardId.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(contentBody)
        ).andExpect(MockMvcResultMatchers.status().isNotAcceptable)
    }

    @Test
    fun `given a Workspace name and a Card id, when sending a DELETE REST request to the learn endpoint and the Card does not exist, then a NOT_FOUND http status response is returned`() {
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        Mockito.`when`(learnService.delete(workspaceRequest, cardId)).thenThrow(CardNotFoundException(cardId))

        mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/workspaces/${workspaceRequest.name}/learn/${cardId.id}")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    fun `given a Workspace name and a Card id, when sending a DELETE REST request to the learn endpoint and the Workspace exists and the Card exists but the Card does not belong to the Workspace, then a NOT_ACCEPTABLE http status response is returned`() {
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        Mockito.`when`(learnService.delete(workspaceRequest, cardId)).thenThrow(CardBindingException(workspaceRequest, cardId))

        mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/workspaces/${workspaceRequest.name}/learn/${cardId.id}")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isNotAcceptable)
    }

    @Test
    fun `given a Workspace name and a Card id, when sending a DELETE REST request to the learn endpoint and the LearnCard does not exist, then a NOT_FOUND http status response is returned`() {
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        Mockito.`when`(learnService.delete(workspaceRequest, cardId)).thenThrow(LearnCardNotFoundException(workspaceRequest, cardId))

        mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/workspaces/${workspaceRequest.name}/learn/${cardId.id}")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    fun `given a Workspace name and a Card id, when sending a DELETE REST request to the learn endpoint, then an OK http status response is returned`() {
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")

        mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/workspaces/${workspaceRequest.name}/learn/${cardId.id}")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isOk)
    }
}
