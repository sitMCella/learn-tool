package de.mcella.spring.learntool.learn

import com.fasterxml.jackson.databind.ObjectMapper
import de.mcella.spring.learntool.UnitTest
import de.mcella.spring.learntool.card.exceptions.CardNotFoundException
import de.mcella.spring.learntool.card.storage.Card
import de.mcella.spring.learntool.learn.exceptions.CardBindingException
import de.mcella.spring.learntool.learn.exceptions.InputValuesNotAcceptableException
import de.mcella.spring.learntool.learn.exceptions.LearnCardAlreadyExistsException
import de.mcella.spring.learntool.learn.exceptions.LearnCardsNotFoundException
import de.mcella.spring.learntool.learn.storage.LearnCard
import de.mcella.spring.learntool.workspace.exceptions.WorkspaceNotExistsException
import java.time.Instant
import org.junit.Test
import org.junit.experimental.categories.Category
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
@Category(UnitTest::class)
@WebMvcTest(LearnController::class)
@AutoConfigureWebClient
class LearnControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var learnService: LearnService

    private val objectMapper = ObjectMapper()

    @Test
    fun `given a Workspace name and a Card id, when sending a POST REST request to the learn endpoint and the Card exists, then the create method of LearnService is called`() {
        val workspaceName = "workspaceTest"
        val cardId = "9e493dc0-ef75-403f-b5d6-ed510634f8a6"
        val learnCard = LearnCard.createInitial(cardId, workspaceName, Instant.now())
        val learnCardParameters = LearnCardParameters(cardId)
        Mockito.`when`(learnService.create(workspaceName, learnCardParameters)).thenReturn(learnCard)
        val contentBody = objectMapper.writeValueAsString(learnCardParameters)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/workspaces/$workspaceName/learn")
                .contentType(MediaType.APPLICATION_JSON)
                .content(contentBody)
        ).andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))

        Mockito.verify(learnService).create(workspaceName, learnCardParameters)
    }

    @Test
    fun `given a Workspace name and a Card id, when sending a POST REST request to the learn endpoint and the Card exists and the LearnCard already exists, then a CONFLICT http status response is returned`() {
        val workspaceName = "workspaceTest"
        val cardId = "9e493dc0-ef75-403f-b5d6-ed510634f8a6"
        val learnCardParameters = LearnCardParameters(cardId)
        Mockito.`when`(learnService.create(workspaceName, learnCardParameters)).thenThrow(LearnCardAlreadyExistsException(cardId))
        val contentBody = objectMapper.writeValueAsString(learnCardParameters)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/workspaces/$workspaceName/learn")
                .contentType(MediaType.APPLICATION_JSON)
                .content(contentBody)
        ).andExpect(MockMvcResultMatchers.status().isConflict)
    }

    @Test
    fun `given a Workspace name and a Card id, when sending a POST REST request to the learn endpoint and the Card does not exist, then a NOT_FOUND http status response is returned`() {
        val workspaceName = "workspaceTest"
        val cardId = "9e493dc0-ef75-403f-b5d6-ed510634f8a6"
        val learnCardParameters = LearnCardParameters(cardId)
        Mockito.`when`(learnService.create(workspaceName, learnCardParameters)).thenThrow(CardNotFoundException(cardId))
        val contentBody = objectMapper.writeValueAsString(learnCardParameters)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/workspaces/$workspaceName/learn")
                .contentType(MediaType.APPLICATION_JSON)
                .content(contentBody)
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    fun `given a Workspace name and a Card id, when sending a POST REST request to the learn endpoint and the Card belongs to a different Workspace, then a NOT_ACCEPTABLE http status response is returned`() {
        val workspaceName = "workspaceTest"
        val cardId = "9e493dc0-ef75-403f-b5d6-ed510634f8a6"
        val learnCardParameters = LearnCardParameters(cardId)
        Mockito.`when`(learnService.create(workspaceName, learnCardParameters)).thenThrow(CardBindingException(workspaceName, cardId))
        val contentBody = objectMapper.writeValueAsString(learnCardParameters)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/workspaces/$workspaceName/learn")
                .contentType(MediaType.APPLICATION_JSON)
                .content(contentBody)
        ).andExpect(MockMvcResultMatchers.status().isNotAcceptable)
    }

    @Test
    fun `given a Workspace name, when sending a GET REST request to the learn endpoint and the Workspace exists, then the getCard method of LearnService is called and a Card is returned`() {
        val workspaceName = "workspaceTest"
        val card = Card("9e493dc0-ef75-403f-b5d6-ed510634f8a6", workspaceName, "question", "response")
        Mockito.`when`(learnService.getCard(workspaceName)).thenReturn(card)
        val expectedContentBody = objectMapper.writeValueAsString(card)

        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/workspaces/$workspaceName/learn")
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(MockMvcResultMatchers.content().json(expectedContentBody))

        Mockito.verify(learnService).getCard(workspaceName)
    }

    @Test
    fun `given a Workspace name, when sending a GET REST request to the learn endpoint and the Workspace does not exist, then a NOT_FOUND http status response is returned`() {
        val workspaceName = "workspaceTest"
        Mockito.`when`(learnService.getCard(workspaceName)).thenThrow(WorkspaceNotExistsException(workspaceName))

        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/workspaces/$workspaceName/learn")
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    fun `given a Workspace name, when sending a GET REST request to the learn endpoint and no Cards exist into the Workspace, then a NOT_FOUND http status response is returned`() {
        val workspaceName = "workspaceTest"
        Mockito.`when`(learnService.getCard(workspaceName)).thenThrow(LearnCardsNotFoundException(workspaceName))

        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/workspaces/$workspaceName/learn")
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    fun `given a Workspace name and the evaluation parameters, when sending a PUT REST request to the learn endpoint and the Workspace exists and the Card exists, then the evaluateCard method of LearnService is called`() {
        val workspaceName = "workspaceTest"
        val cardId = "9e493dc0-ef75-403f-b5d6-ed510634f8a6"
        val evaluationParameters = EvaluationParameters(cardId, 5)
        val contentBody = objectMapper.writeValueAsString(evaluationParameters)
        val learnCard = LearnCard.createInitial(cardId, workspaceName, Instant.now())
        Mockito.`when`(learnService.evaluateCard(workspaceName, evaluationParameters)).thenReturn(learnCard)

        mockMvc.perform(
            MockMvcRequestBuilders.put("/api/workspaces/$workspaceName/learn")
                .contentType(MediaType.APPLICATION_JSON)
                .content(contentBody)
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))

        Mockito.verify(learnService).evaluateCard(workspaceName, evaluationParameters)
    }

    @Test
    fun `given a Workspace name and the evaluation parameters, when sending a PUT REST request to the learn endpoint and the Workspace does not exist, then a NOT_FOUND http status response is returned`() {
        val workspaceName = "workspaceTest"
        val evaluationParameters = EvaluationParameters("9e493dc0-ef75-403f-b5d6-ed510634f8a6", 5)
        val contentBody = objectMapper.writeValueAsString(evaluationParameters)
        Mockito.`when`(learnService.evaluateCard(workspaceName, evaluationParameters)).thenThrow(WorkspaceNotExistsException(workspaceName))

        mockMvc.perform(
            MockMvcRequestBuilders.put("/api/workspaces/$workspaceName/learn")
                .contentType(MediaType.APPLICATION_JSON)
                .content(contentBody)
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    fun `given a Workspace name and the evaluation parameters, when sending a PUT REST request to the learn endpoint and the Workspace exists and the Card does not exist, then a NOT_FOUND http status response is returned`() {
        val workspaceName = "workspaceTest"
        val cardId = "9e493dc0-ef75-403f-b5d6-ed510634f8a6"
        val evaluationParameters = EvaluationParameters(cardId, 5)
        val contentBody = objectMapper.writeValueAsString(evaluationParameters)
        Mockito.`when`(learnService.evaluateCard(workspaceName, evaluationParameters)).thenThrow(CardNotFoundException(cardId))

        mockMvc.perform(
            MockMvcRequestBuilders.put("/api/workspaces/$workspaceName/learn")
                .contentType(MediaType.APPLICATION_JSON)
                .content(contentBody)
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    fun `given a Workspace name and the evaluation parameters, when sending a PUT REST request to the learn endpoint and the Workspace exists and the Card exists but the Card does not belong to the Workspace, then a NOT_ACCEPTABLE http status response is returned`() {
        val workspaceName = "workspaceTest"
        val cardId = "9e493dc0-ef75-403f-b5d6-ed510634f8a6"
        val evaluationParameters = EvaluationParameters(cardId, 5)
        val contentBody = objectMapper.writeValueAsString(evaluationParameters)
        Mockito.`when`(learnService.evaluateCard(workspaceName, evaluationParameters)).thenThrow(CardBindingException(workspaceName, cardId))

        mockMvc.perform(
            MockMvcRequestBuilders.put("/api/workspaces/$workspaceName/learn")
                .contentType(MediaType.APPLICATION_JSON)
                .content(contentBody)
        ).andExpect(MockMvcResultMatchers.status().isNotAcceptable)
    }

    @Test
    fun `given a Workspace name and the evaluation parameters with quality equals to 10, when sending a PUT REST request to the learn endpoint and the Workspace exists and the Card exists, then a NOT_ACCEPTABLE http status response is returned`() {
        val workspaceName = "workspaceTest"
        val cardId = "9e493dc0-ef75-403f-b5d6-ed510634f8a6"
        val evaluationParameters = EvaluationParameters(cardId, 10)
        val contentBody = objectMapper.writeValueAsString(evaluationParameters)
        Mockito.`when`(learnService.evaluateCard(workspaceName, evaluationParameters)).thenThrow(InputValuesNotAcceptableException(""))

        mockMvc.perform(
            MockMvcRequestBuilders.put("/api/workspaces/$workspaceName/learn")
                .contentType(MediaType.APPLICATION_JSON)
                .content(contentBody)
        ).andExpect(MockMvcResultMatchers.status().isNotAcceptable)
    }
}
