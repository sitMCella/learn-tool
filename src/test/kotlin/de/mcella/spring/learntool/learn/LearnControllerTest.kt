package de.mcella.spring.learntool.learn

import com.fasterxml.jackson.databind.ObjectMapper
import de.mcella.spring.learntool.card.storage.Card
import de.mcella.spring.learntool.learn.exceptions.CardBindingException
import de.mcella.spring.learntool.learn.exceptions.CardNotFoundException
import de.mcella.spring.learntool.learn.exceptions.CardsNotFoundException
import de.mcella.spring.learntool.learn.exceptions.InputValuesNotAcceptableException
import de.mcella.spring.learntool.learn.storage.LearnCard
import de.mcella.spring.learntool.workspace.exceptions.WorkspaceNotExistsException
import java.time.Instant
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@RunWith(SpringRunner::class)
@WebMvcTest(LearnController::class)
@AutoConfigureWebClient
class LearnControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var learnService: LearnService

    private val objectMapper = ObjectMapper()

    @Test
    fun `given a Workspace name, when sending a get request to the learn endpoint and the Workspace exists, then the getCard method of LearnService is called and a Card is returned`() {
        val workspaceName = "workspaceTest"
        val card = Card("9e493dc0-ef75-403f-b5d6-ed510634f8a6", workspaceName, "question", "response")
        Mockito.`when`(learnService.getCard(workspaceName)).thenReturn(card)
        val expectedContentBody = objectMapper.writeValueAsString(card)

        mockMvc.perform(
            MockMvcRequestBuilders.get("/workspaces/$workspaceName/learn")
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(MockMvcResultMatchers.content().json(expectedContentBody))

        Mockito.verify(learnService).getCard(workspaceName)
    }

    @Test
    fun `given a Workspace name, when sending a get request to the learn endpoint and the Workspace does not exist, then the getCard method of LearnService is called and a NOT_FOUND response is returned`() {
        val workspaceName = "workspaceTest"
        Mockito.`when`(learnService.getCard(workspaceName)).thenThrow(WorkspaceNotExistsException(workspaceName))

        mockMvc.perform(
            MockMvcRequestBuilders.get("/workspaces/$workspaceName/learn")
        ).andExpect(MockMvcResultMatchers.status().isNotFound)

        Mockito.verify(learnService).getCard(workspaceName)
    }

    @Test
    fun `given a Workspace name, when sending a get request to the learn endpoint and no Cards exist into the Workspace, then the getCard method of LearnService is called and a NOT_FOUND response is returned`() {
        val workspaceName = "workspaceTest"
        Mockito.`when`(learnService.getCard(workspaceName)).thenThrow(CardsNotFoundException(workspaceName))

        mockMvc.perform(
            MockMvcRequestBuilders.get("/workspaces/$workspaceName/learn")
        ).andExpect(MockMvcResultMatchers.status().isNotFound)

        Mockito.verify(learnService).getCard(workspaceName)
    }

    @Test
    fun `given a Workspace name and the evaluation parameters, when sending a put request to the learn endpoint and the Workspace exists and the Card exists, then the evaluateCard method of LearnService is called and a LearnCard is returned`() {
        val workspaceName = "workspaceTest"
        val cardId = "9e493dc0-ef75-403f-b5d6-ed510634f8a6"
        val evaluationParameters = EvaluationParameters(cardId, 5)
        val contentBody = objectMapper.writeValueAsString(evaluationParameters)
        val learnCard = LearnCard.createInitial(cardId, Instant.now())
        Mockito.`when`(learnService.evaluateCard(workspaceName, evaluationParameters)).thenReturn(learnCard)

        mockMvc.perform(
            MockMvcRequestBuilders.put("/workspaces/$workspaceName/learn")
                .contentType(MediaType.APPLICATION_JSON)
                .content(contentBody)
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))

        Mockito.verify(learnService).evaluateCard(workspaceName, evaluationParameters)
    }

    @Test
    fun `given a Workspace name and the evaluation parameters, when sending a put request to the learn endpoint and the Workspace does not exist, then the evaluateCard method of LearnService is called and a NOT_FOUND response is returned`() {
        val workspaceName = "workspaceTest"
        val evaluationParameters = EvaluationParameters("9e493dc0-ef75-403f-b5d6-ed510634f8a6", 5)
        val contentBody = objectMapper.writeValueAsString(evaluationParameters)
        Mockito.`when`(learnService.evaluateCard(workspaceName, evaluationParameters)).thenThrow(WorkspaceNotExistsException(workspaceName))

        mockMvc.perform(
            MockMvcRequestBuilders.put("/workspaces/$workspaceName/learn")
                .contentType(MediaType.APPLICATION_JSON)
                .content(contentBody)
        ).andExpect(MockMvcResultMatchers.status().isNotFound)

        Mockito.verify(learnService).evaluateCard(workspaceName, evaluationParameters)
    }

    @Test
    fun `given a Workspace name and the evaluation parameters, when sending a put request to the learn endpoint and the Workspace exists and the Card does not exist, then the evaluateCard method of LearnService is called and a NOT_FOUND response is returned`() {
        val workspaceName = "workspaceTest"
        val cardId = "9e493dc0-ef75-403f-b5d6-ed510634f8a6"
        val evaluationParameters = EvaluationParameters(cardId, 5)
        val contentBody = objectMapper.writeValueAsString(evaluationParameters)
        Mockito.`when`(learnService.evaluateCard(workspaceName, evaluationParameters)).thenThrow(CardNotFoundException(workspaceName, cardId))

        mockMvc.perform(
            MockMvcRequestBuilders.put("/workspaces/$workspaceName/learn")
                .contentType(MediaType.APPLICATION_JSON)
                .content(contentBody)
        ).andExpect(MockMvcResultMatchers.status().isNotFound)

        Mockito.verify(learnService).evaluateCard(workspaceName, evaluationParameters)
    }

    @Test
    fun `given a Workspace name and the evaluation parameters, when sending a put request to the learn endpoint and the Workspace exists and the Card exists but the Card does not belong to the Workspace, then the evaluateCard method of LearnService is called and a NOT_ACCEPTABLE response is returned`() {
        val workspaceName = "workspaceTest"
        val cardId = "9e493dc0-ef75-403f-b5d6-ed510634f8a6"
        val evaluationParameters = EvaluationParameters(cardId, 5)
        val contentBody = objectMapper.writeValueAsString(evaluationParameters)
        Mockito.`when`(learnService.evaluateCard(workspaceName, evaluationParameters)).thenThrow(CardBindingException(workspaceName, cardId))

        mockMvc.perform(
            MockMvcRequestBuilders.put("/workspaces/$workspaceName/learn")
                .contentType(MediaType.APPLICATION_JSON)
                .content(contentBody)
        ).andExpect(MockMvcResultMatchers.status().isNotAcceptable)

        Mockito.verify(learnService).evaluateCard(workspaceName, evaluationParameters)
    }

    @Test
    fun `given a Workspace name and the evaluation parameters with quality equals to 10, when sending a put request to the learn endpoint and the Workspace exists and the Card exists, then the evaluateCard method of LearnService is called and a NOT_ACCEPTABLE response is returned`() {
        val workspaceName = "workspaceTest"
        val cardId = "9e493dc0-ef75-403f-b5d6-ed510634f8a6"
        val evaluationParameters = EvaluationParameters(cardId, 10)
        val contentBody = objectMapper.writeValueAsString(evaluationParameters)
        Mockito.`when`(learnService.evaluateCard(workspaceName, evaluationParameters)).thenThrow(InputValuesNotAcceptableException(""))

        mockMvc.perform(
            MockMvcRequestBuilders.put("/workspaces/$workspaceName/learn")
                .contentType(MediaType.APPLICATION_JSON)
                .content(contentBody)
        ).andExpect(MockMvcResultMatchers.status().isNotAcceptable)

        Mockito.verify(learnService).evaluateCard(workspaceName, evaluationParameters)
    }
}
