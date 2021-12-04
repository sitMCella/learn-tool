package de.mcella.spring.learntool.search

import com.fasterxml.jackson.databind.ObjectMapper
import de.mcella.spring.learntool.UnitTest
import de.mcella.spring.learntool.card.Card
import de.mcella.spring.learntool.workspace.Workspace
import de.mcella.spring.learntool.workspace.exceptions.WorkspaceNotExistsException
import org.hibernate.search.util.common.SearchException
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
@WebMvcTest(SearchController::class)
@AutoConfigureWebClient
class SearchControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var cardSearchService: CardSearchService

    private val objectMapper = ObjectMapper()

    @Test
    fun `given a Workspace name and a search content, when sending a GET REST request to the search endpoint, then the searchCards method of CardSearchService is called and the retrieved Cards are returned`() {
        val workspace = Workspace("workspaceTest")
        val searchContent = SearchPattern("content")
        val card = Card("9e493dc0-ef75-403f-b5d6-ed510634f8a6", workspace.name, "question", "response content")
        val cards = listOf(card)
        Mockito.`when`(cardSearchService.searchCards(workspace, searchContent)).thenReturn(cards)
        val expectedContentBody = objectMapper.writeValueAsString(cards)

        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/workspaces/${workspace.name}/search?content=${searchContent.content}")
        ).andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.content().json(expectedContentBody))

        Mockito.verify(cardSearchService).searchCards(workspace, searchContent)
    }

    @Test
    fun `given a Workspace name and a search content, when sending a GET REST request to the search endpoint and the CardSearchService throws WorkspaceNotExistsException exception, then a NOT_FOUND http status response is returned`() {
        val workspace = Workspace("workspaceTest")
        val searchPattern = SearchPattern("content")
        Mockito.`when`(cardSearchService.searchCards(workspace, searchPattern)).thenThrow(WorkspaceNotExistsException(workspace))

        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/workspaces/${workspace.name}/search?content=${searchPattern.content}")
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    fun `given a Workspace name and a search content, when sending a GET REST request to the search endpoint and the CardSearchService throws SearchException exception, then a NOT_FOUND http status response is returned`() {
        val workspace = Workspace("workspaceTest")
        val searchPattern = SearchPattern("content")
        Mockito.`when`(cardSearchService.searchCards(workspace, searchPattern)).thenThrow(SearchException("search error"))

        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/workspaces/${workspace.name}/search?content=${searchPattern.content}")
        ).andExpect(MockMvcResultMatchers.status().is5xxServerError)
    }
}
