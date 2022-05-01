package de.mcella.spring.learntool.workspace

import com.fasterxml.jackson.databind.ObjectMapper
import de.mcella.spring.learntool.UnitTest
import de.mcella.spring.learntool.config.AppProperties
import de.mcella.spring.learntool.security.CustomUserDetailsService
import de.mcella.spring.learntool.security.TokenAuthenticationFilter
import de.mcella.spring.learntool.security.oauth2.CustomOAuth2UserService
import de.mcella.spring.learntool.security.oauth2.HttpCookieOAuth2AuthorizationRequestRepository
import de.mcella.spring.learntool.security.oauth2.OAuth2AuthenticationFailureHandler
import de.mcella.spring.learntool.security.oauth2.OAuth2AuthenticationSuccessHandler
import de.mcella.spring.learntool.workspace.dto.Workspace
import de.mcella.spring.learntool.workspace.exceptions.InvalidWorkspaceNameException
import de.mcella.spring.learntool.workspace.exceptions.WorkspaceAlreadyExistsException
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
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@RunWith(SpringRunner::class)
@Category(UnitTest::class)
@WebMvcTest(WorkspaceController::class)
@AutoConfigureWebClient
@AutoConfigureMockMvc(addFilters = false)
@EnableConfigurationProperties(AppProperties::class)
@TestPropertySource(properties = ["app.auth.tokenSecret=test", "app.auth.tokenExpirationMsec=123"])
@WithMockUser("roles='USER'")
class WorkspaceControllerTest {

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
    private lateinit var workspaceService: WorkspaceService

    private val objectMapper = ObjectMapper()

    @Test
    fun `given a Workspace, when sending a POST REST request to workspaces endpoint, then the create method of WorkspaceService is called`() {
        val workspace = Workspace("workspaceTest")
        val contentBody = objectMapper.writeValueAsString(workspace)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/workspaces")
                .contentType(MediaType.APPLICATION_JSON)
                .content(contentBody)
        ).andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.header().exists(HttpHeaders.LOCATION))
            .andExpect(MockMvcResultMatchers.header().string(HttpHeaders.LOCATION, "/api/workspaces/workspaceTest"))

        Mockito.verify(workspaceService).create(workspace)
    }

    @Test
    fun `given a Workspace with invalid name, when sending a POST REST request to the workspaces endpoint, then an UNPROCESSABLE_ENTITY http status response is returned`() {
        val workspace = Workspace("workspace-invalid-Test")
        val contentBody = objectMapper.writeValueAsString(workspace)
        Mockito.`when`(workspaceService.create(workspace)).thenThrow(InvalidWorkspaceNameException(""))

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/workspaces")
                .contentType(MediaType.APPLICATION_JSON)
                .content(contentBody)
        ).andExpect(MockMvcResultMatchers.status().isUnprocessableEntity)
    }

    @Test
    fun `given a Workspace, when sending a POST REST request to the workspaces endpoint and the Workspace already exists, then a CONFLICT http status response is returned`() {
        val workspace = Workspace("workspaceTest")
        val contentBody = objectMapper.writeValueAsString(workspace)
        Mockito.`when`(workspaceService.create(workspace)).thenThrow(WorkspaceAlreadyExistsException(workspace))

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/workspaces")
                .contentType(MediaType.APPLICATION_JSON)
                .content(contentBody)
        ).andExpect(MockMvcResultMatchers.status().isConflict)
    }

    @Test
    fun `when sending a GET REST request to workspaces endpoint, then the getAll method of WorkspaceService is called and a list of Workspaces is returned`() {
        val workspace1 = Workspace("workspaceTest1")
        val workspace2 = Workspace("workspaceTest2")
        val workspaces = listOf(workspace1, workspace2)
        Mockito.`when`(workspaceService.getAll()).thenReturn(workspaces)
        val expectedContentBody = objectMapper.writeValueAsString(workspaces)

        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/workspaces")
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(MockMvcResultMatchers.content().json(expectedContentBody))

        Mockito.verify(workspaceService).getAll()
    }
}
