package de.mcella.spring.learntool.learn

import com.fasterxml.jackson.databind.ObjectMapper
import de.mcella.spring.learntool.card.storage.Card
import de.mcella.spring.learntool.learn.exceptions.CardNotFoundException
import de.mcella.spring.learntool.workspace.exceptions.WorkspaceDoesNotExistException
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
        Mockito.`when`(learnService.getCard(workspaceName)).thenThrow(WorkspaceDoesNotExistException(workspaceName))

        mockMvc.perform(
            MockMvcRequestBuilders.get("/workspaces/$workspaceName/learn")
        ).andExpect(MockMvcResultMatchers.status().isNotFound)

        Mockito.verify(learnService).getCard(workspaceName)
    }

    @Test
    fun `given a Workspace name, when sending a get request to the learn endpoint and no Cards exist into the Workspace, then the getCard method of LearnService is called and a NOT_FOUND response is returned`() {
        val workspaceName = "workspaceTest"
        Mockito.`when`(learnService.getCard(workspaceName)).thenThrow(CardNotFoundException(workspaceName))

        mockMvc.perform(
            MockMvcRequestBuilders.get("/workspaces/$workspaceName/learn")
        ).andExpect(MockMvcResultMatchers.status().isNotFound)

        Mockito.verify(learnService).getCard(workspaceName)
    }
}
