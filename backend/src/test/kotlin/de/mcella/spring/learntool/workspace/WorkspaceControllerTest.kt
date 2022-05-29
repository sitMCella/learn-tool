package de.mcella.spring.learntool.workspace

import com.fasterxml.jackson.databind.ObjectMapper
import de.mcella.spring.learntool.UnitTest
import de.mcella.spring.learntool.WithMockUser
import de.mcella.spring.learntool.config.AppProperties
import de.mcella.spring.learntool.security.CustomUserDetailsService
import de.mcella.spring.learntool.security.TokenAuthenticationFilter
import de.mcella.spring.learntool.security.UserPrincipal
import de.mcella.spring.learntool.user.dto.UserId
import de.mcella.spring.learntool.user.exceptions.UserNotAuthorizedException
import de.mcella.spring.learntool.workspace.dto.Workspace
import de.mcella.spring.learntool.workspace.dto.WorkspaceCreateRequest
import de.mcella.spring.learntool.workspace.dto.WorkspaceId
import de.mcella.spring.learntool.workspace.dto.WorkspaceRequest
import de.mcella.spring.learntool.workspace.exceptions.InvalidWorkspaceNameException
import de.mcella.spring.learntool.workspace.exceptions.WorkspaceAlreadyExistsException
import de.mcella.spring.learntool.workspace.exceptions.WorkspaceNotExistsException
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
    private lateinit var tokenAuthenticationFilter: TokenAuthenticationFilter

    @MockBean
    private lateinit var workspaceService: WorkspaceService

    private val objectMapper = ObjectMapper()

    @Test
    @WithMockUser
    fun `given a Workspace create request, when sending a POST REST request to workspaces endpoint, then the create method of WorkspaceService is called`() {
        val workspaceCreateRequest = WorkspaceCreateRequest("Workspace Name")
        val contentBody = objectMapper.writeValueAsString(workspaceCreateRequest)
        val userId = UserId(1L)
        val userPrincipal = UserPrincipal(userId.id, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        val workspace = Workspace("workspaceId", "Workspace Name", userId)
        Mockito.`when`(workspaceService.create(workspaceCreateRequest, userPrincipal)).thenReturn(workspace)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/workspaces")
                .contentType(MediaType.APPLICATION_JSON)
                .content(contentBody)
        ).andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.header().exists(HttpHeaders.LOCATION))
            .andExpect(MockMvcResultMatchers.header().string(HttpHeaders.LOCATION, "/api/workspaces/${workspace.id}"))

        Mockito.verify(workspaceService).create(workspaceCreateRequest, userPrincipal)
    }

    @Test
    fun `when sending a POST REST request to workspaces endpoint without JWT authentication, then an UNPROCESSABLE_ENTITY http status response is returned`() {
        val workspaceCreateRequest = WorkspaceCreateRequest("Workspace Name")
        val contentBody = objectMapper.writeValueAsString(workspaceCreateRequest)

        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/workspaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contentBody)
        ).andExpect(MockMvcResultMatchers.status().isUnprocessableEntity)
    }

    @Test
    @WithMockUser
    fun `given a Workspace create request, when sending a POST REST request to the workspaces endpoint and the Workspace name is empty, then an UNPROCESSABLE_ENTITY http status response is returned`() {
        val workspaceCreateRequest = WorkspaceCreateRequest("")
        val userPrincipal = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        val contentBody = objectMapper.writeValueAsString(workspaceCreateRequest)
        Mockito.`when`(workspaceService.create(workspaceCreateRequest, userPrincipal)).thenThrow(IllegalArgumentException())

        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/workspaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contentBody)
        ).andExpect(MockMvcResultMatchers.status().isUnprocessableEntity)
    }

    @Test
    @WithMockUser
    fun `given a Workspace create request, when sending a POST REST request to the workspaces endpoint and the Workspace name is invalid, then an UNPROCESSABLE_ENTITY http status response is returned`() {
        val workspaceCreateRequest = WorkspaceCreateRequest("workspace-InvalidName!")
        val userPrincipal = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        val contentBody = objectMapper.writeValueAsString(workspaceCreateRequest)
        Mockito.`when`(workspaceService.create(workspaceCreateRequest, userPrincipal)).thenThrow(InvalidWorkspaceNameException(""))

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/workspaces")
                .contentType(MediaType.APPLICATION_JSON)
                .content(contentBody)
        ).andExpect(MockMvcResultMatchers.status().isUnprocessableEntity)
    }

    @Test
    @WithMockUser
    fun `given a Workspace create request, when sending a POST REST request to the workspaces endpoint and the Workspace Id already exists, then a CONFLICT http status response is returned`() {
        val workspaceCreateRequest = WorkspaceCreateRequest("workspace-InvalidName!")
        val userPrincipal = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        val contentBody = objectMapper.writeValueAsString(workspaceCreateRequest)
        Mockito.`when`(workspaceService.create(workspaceCreateRequest, userPrincipal)).thenThrow(WorkspaceAlreadyExistsException(WorkspaceId("workspaceId")))

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/workspaces")
                .contentType(MediaType.APPLICATION_JSON)
                .content(contentBody)
        ).andExpect(MockMvcResultMatchers.status().isConflict)
    }

    @Test
    @WithMockUser
    fun `given a Workspace create request, when sending a PUT REST request to workspaces endpoint, then the update method of WorkspaceService is called`() {
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val workspaceCreateRequest = WorkspaceCreateRequest("Workspace Update Name")
        val contentBody = objectMapper.writeValueAsString(workspaceCreateRequest)
        val userId = UserId(1L)
        val userPrincipal = UserPrincipal(userId.id, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        val workspace = Workspace(workspaceRequest.id, workspaceCreateRequest.name, userId)
        Mockito.`when`(workspaceService.update(workspaceRequest, workspaceCreateRequest, userPrincipal)).thenReturn(workspace)

        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/workspaces/${workspaceRequest.id}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contentBody)
        ).andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.header().exists(HttpHeaders.LOCATION))
                .andExpect(MockMvcResultMatchers.header().string(HttpHeaders.LOCATION, "/api/workspaces/${workspaceRequest.id}"))

        Mockito.verify(workspaceService).update(workspaceRequest, workspaceCreateRequest, userPrincipal)
    }

    @Test
    fun `when sending a PUT REST request to workspaces endpoint without JWT authentication, then an UNPROCESSABLE_ENTITY http status response is returned`() {
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val workspaceCreateRequest = WorkspaceCreateRequest("Workspace Update Name")
        val contentBody = objectMapper.writeValueAsString(workspaceCreateRequest)

        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/workspaces/${workspaceRequest.id}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contentBody)
        ).andExpect(MockMvcResultMatchers.status().isUnprocessableEntity)
    }

    @Test
    @WithMockUser
    fun `given a Workspace create request, when sending a PUT REST request to the workspaces endpoint and the Workspace name is empty, then an UNPROCESSABLE_ENTITY http status response is returned`() {
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val workspaceCreateRequest = WorkspaceCreateRequest("")
        val userPrincipal = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        val contentBody = objectMapper.writeValueAsString(workspaceCreateRequest)
        Mockito.`when`(workspaceService.update(workspaceRequest, workspaceCreateRequest, userPrincipal)).thenThrow(IllegalArgumentException())

        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/workspaces/${workspaceRequest.id}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contentBody)
        ).andExpect(MockMvcResultMatchers.status().isUnprocessableEntity)
    }

    @Test
    @WithMockUser
    fun `given a Workspace create request, when sending a PUT REST request to the workspaces endpoint and the Workspace name is invalid, then an UNPROCESSABLE_ENTITY http status response is returned`() {
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val workspaceCreateRequest = WorkspaceCreateRequest("workspace-InvalidName!")
        val userPrincipal = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        val contentBody = objectMapper.writeValueAsString(workspaceCreateRequest)
        Mockito.`when`(workspaceService.update(workspaceRequest, workspaceCreateRequest, userPrincipal)).thenThrow(InvalidWorkspaceNameException(""))

        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/workspaces/${workspaceRequest.id}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contentBody)
        ).andExpect(MockMvcResultMatchers.status().isUnprocessableEntity)
    }

    @Test
    @WithMockUser
    fun `given a Workspace create request, when sending a PUT REST request to the workspaces endpoint and the Workspace does not exist, then an NOT_FOUND http status response is returned`() {
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val workspaceCreateRequest = WorkspaceCreateRequest("Workspace Update Name")
        val userPrincipal = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        val contentBody = objectMapper.writeValueAsString(workspaceCreateRequest)
        Mockito.`when`(workspaceService.update(workspaceRequest, workspaceCreateRequest, userPrincipal)).thenThrow(WorkspaceNotExistsException(workspaceRequest))

        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/workspaces/${workspaceRequest.id}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contentBody)
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    @WithMockUser
    fun `given a Workspace create request, when sending a PUT REST request to the workspaces endpoint and the User does not own the Workspace, then an UNAUTHORIZED http status response is returned`() {
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val workspaceCreateRequest = WorkspaceCreateRequest("Workspace Update Name")
        val userPrincipal = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        val contentBody = objectMapper.writeValueAsString(workspaceCreateRequest)
        Mockito.`when`(workspaceService.update(workspaceRequest, workspaceCreateRequest, userPrincipal)).thenThrow(UserNotAuthorizedException(userPrincipal))

        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/workspaces/${workspaceRequest.id}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contentBody)
        ).andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

    @Test
    @WithMockUser
    fun `given a Workspace Id, when sending a DELETE REST request to the workspaces endpoint, then the delete method of WorkspaceService is called`() {
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val userPrincipal = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())

        mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/workspaces/${workspaceRequest.id}")
        ).andExpect(MockMvcResultMatchers.status().isOk)

        Mockito.verify(workspaceService).delete(workspaceRequest, userPrincipal)
    }

    @Test
    fun `when sending a DELETE REST request to workspaces endpoint without JWT authentication, then an UNPROCESSABLE_ENTITY http status response is returned`() {
        val workspaceRequest = WorkspaceRequest("workspaceId")

        mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/workspaces/${workspaceRequest.id}")
        ).andExpect(MockMvcResultMatchers.status().isUnprocessableEntity)
    }

    @Test
    @WithMockUser
    fun `given a Workspace Id, when sending a DELETE REST request to the workspaces endpoint and the Workspace does not exist, then an NOT_FOUND http status response is returned`() {
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val userPrincipal = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(workspaceService.delete(workspaceRequest, userPrincipal)).thenThrow(WorkspaceNotExistsException(workspaceRequest))

        mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/workspaces/${workspaceRequest.id}")
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    @WithMockUser
    fun `given a Workspace Id, when sending a DELETE REST request to the workspaces endpoint and the User does not own the Workspace, then an UNAUTHORIZED http status response is returned`() {
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val userPrincipal = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(workspaceService.delete(workspaceRequest, userPrincipal)).thenThrow(UserNotAuthorizedException(userPrincipal))

        mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/workspaces/${workspaceRequest.id}")
        ).andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

    @Test
    @WithMockUser
    fun `when sending a GET REST request to workspaces endpoint, then the getAll method of WorkspaceService is called and the list of Workspaces for the authenticated user is returned`() {
        val userId = UserId(1L)
        val userPrincipal = UserPrincipal(userId.id, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        val workspace1 = Workspace("workspaceId1", "Workspace Name 1", userId)
        val workspace2 = Workspace("workspaceId2", "Workspace Name 2", userId)
        val workspaces = listOf(workspace1, workspace2)
        Mockito.`when`(workspaceService.getAll(userPrincipal)).thenReturn(workspaces)
        val expectedContentBody = objectMapper.writeValueAsString(workspaces)

        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/workspaces")
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(MockMvcResultMatchers.content().json(expectedContentBody))

        Mockito.verify(workspaceService).getAll(userPrincipal)
    }

    @Test
    fun `when sending a GET REST request to workspaces endpoint without JWT authentication, then an UNPROCESSABLE_ENTITY http status response is returned`() {
        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/workspaces")
        ).andExpect(MockMvcResultMatchers.status().isUnprocessableEntity)
    }
}
