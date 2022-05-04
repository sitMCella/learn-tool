package de.mcella.spring.learntool.workspace

import com.fasterxml.jackson.databind.ObjectMapper
import de.mcella.spring.learntool.UnitTest
import de.mcella.spring.learntool.WithMockUser
import de.mcella.spring.learntool.config.AppProperties
import de.mcella.spring.learntool.security.CustomUserDetailsService
import de.mcella.spring.learntool.security.TokenAuthenticationFilter
import de.mcella.spring.learntool.security.UserPrincipal
import de.mcella.spring.learntool.security.oauth2.CustomOAuth2UserService
import de.mcella.spring.learntool.security.oauth2.HttpCookieOAuth2AuthorizationRequestRepository
import de.mcella.spring.learntool.security.oauth2.OAuth2AuthenticationFailureHandler
import de.mcella.spring.learntool.security.oauth2.OAuth2AuthenticationSuccessHandler
import de.mcella.spring.learntool.user.dto.UserId
import de.mcella.spring.learntool.workspace.dto.Workspace
import de.mcella.spring.learntool.workspace.dto.WorkspaceRequest
import de.mcella.spring.learntool.workspace.exceptions.InvalidWorkspaceNameException
import de.mcella.spring.learntool.workspace.exceptions.WorkspaceAlreadyExistsException
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
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.core.authority.SimpleGrantedAuthority
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
    private lateinit var workspaceService: WorkspaceService

    private val objectMapper = ObjectMapper()

    @Test
    @WithMockUser
    fun `given a Workspace, when sending a POST REST request to workspaces endpoint, then the create method of WorkspaceService is called`() {
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val contentBody = objectMapper.writeValueAsString(workspaceRequest)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/workspaces")
                .contentType(MediaType.APPLICATION_JSON)
                .content(contentBody)
        ).andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.header().exists(HttpHeaders.LOCATION))
            .andExpect(MockMvcResultMatchers.header().string(HttpHeaders.LOCATION, "/api/workspaces/workspaceTest"))

        val user = UserPrincipal(123L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.verify(workspaceService).create(workspaceRequest, user)
    }

    @Test
    @WithMockUser
    fun `given a Workspace with invalid name, when sending a POST REST request to the workspaces endpoint, then an UNPROCESSABLE_ENTITY http status response is returned`() {
        val workspaceRequest = WorkspaceRequest("workspace-invalid-Test")
        val user = UserPrincipal(123L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        val contentBody = objectMapper.writeValueAsString(workspaceRequest)
        Mockito.`when`(workspaceService.create(workspaceRequest, user)).thenThrow(InvalidWorkspaceNameException(""))

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/workspaces")
                .contentType(MediaType.APPLICATION_JSON)
                .content(contentBody)
        ).andExpect(MockMvcResultMatchers.status().isUnprocessableEntity)
    }

    @Test
    @WithMockUser
    fun `given a Workspace, when sending a POST REST request to the workspaces endpoint and the Workspace already exists, then a CONFLICT http status response is returned`() {
        val workspaceRequest = WorkspaceRequest("workspaceTest")
        val user = UserPrincipal(123L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        val contentBody = objectMapper.writeValueAsString(workspaceRequest)
        Mockito.`when`(workspaceService.create(workspaceRequest, user)).thenThrow(WorkspaceAlreadyExistsException(workspaceRequest))

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/workspaces")
                .contentType(MediaType.APPLICATION_JSON)
                .content(contentBody)
        ).andExpect(MockMvcResultMatchers.status().isConflict)
    }

    @Test
    fun `when sending a GET REST request to workspaces endpoint, then the getAll method of WorkspaceService is called and a list of Workspaces is returned`() {
        val userId = UserId(1L)
        val workspace1 = Workspace("workspaceTest1", userId)
        val workspace2 = Workspace("workspaceTest2", userId)
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
