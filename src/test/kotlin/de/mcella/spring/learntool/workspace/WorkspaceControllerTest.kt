package de.mcella.spring.learntool.workspace

import com.fasterxml.jackson.databind.ObjectMapper
import de.mcella.spring.learntool.workspace.storage.Workspace
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
@WebMvcTest(WorkspaceController::class)
@AutoConfigureWebClient
class WorkspaceControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var workspaceService: WorkspaceService

    private val objectMapper = ObjectMapper()

    @Test
    fun `given a Workspace, when sending a post request to workspace endpoint, then WorkspaceService is called`() {
        val workspace = Workspace("workspaceTest")
        val contentBody = objectMapper.writeValueAsString(workspace)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/workspaces")
                .contentType(MediaType.APPLICATION_JSON)
                .content(contentBody)
        ).andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.header().exists(HttpHeaders.LOCATION))
            .andExpect(MockMvcResultMatchers.header().string(HttpHeaders.LOCATION, "/workspaces/workspaceTest"))

        Mockito.verify(workspaceService).create(workspace)
    }
}
