package de.mcella.spring.learntool.export

import de.mcella.spring.learntool.UnitTest
import de.mcella.spring.learntool.WithMockUser
import de.mcella.spring.learntool.config.AppProperties
import de.mcella.spring.learntool.security.CustomUserDetailsService
import de.mcella.spring.learntool.security.TokenAuthenticationFilter
import de.mcella.spring.learntool.security.UserPrincipal
import de.mcella.spring.learntool.user.exceptions.UserNotAuthorizedException
import de.mcella.spring.learntool.workspace.dto.WorkspaceRequest
import de.mcella.spring.learntool.workspace.exceptions.WorkspaceNotExistsException
import java.io.File
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
@WebMvcTest(ExportController::class)
@AutoConfigureWebClient
@AutoConfigureMockMvc(addFilters = false)
@EnableConfigurationProperties(AppProperties::class)
@TestPropertySource(properties = ["app.auth.tokenSecret=test", "app.auth.tokenExpirationMsec=123"])
class ExportControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var customUserDetailsService: CustomUserDetailsService

    @MockBean
    private lateinit var tokenAuthenticationFilter: TokenAuthenticationFilter

    @MockBean
    private lateinit var clientRegistrationRepository: ClientRegistrationRepository

    @MockBean
    private lateinit var exportService: ExportService

    @Test
    @WithMockUser
    fun `given a Workspace Id, when sending a GET REST request to the export endpoint and the Workspace exists, then the exportBackup method of ExportService is called and a backup file is returned`() {
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val userPrincipal = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        val backup = File.createTempFile("backup", ".zip")
        Mockito.`when`(exportService.exportBackup(workspaceRequest, userPrincipal)).thenReturn(backup)

        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/workspaces/${workspaceRequest.id}/export")
        ).andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .andExpect(MockMvcResultMatchers.content().bytes(backup.readBytes()))

        Mockito.verify(exportService).exportBackup(workspaceRequest, userPrincipal)
    }

    @Test
    fun `given a Workspace Id, when sending a GET REST request to the export endpoint without JWT authentication, then an UNPROCESSABLE_ENTITY http status response is returned`() {
        val workspaceRequest = WorkspaceRequest("workspaceId")

        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/workspaces/${workspaceRequest.id}/export")
        ).andExpect(MockMvcResultMatchers.status().isUnprocessableEntity)
    }

    @Test
    @WithMockUser
    fun `given a Workspace Id, when sending a GET REST request to the export endpoint and the exportBackup method of the ExportService throws UserNotAuthorizedException, then an UNAUTHORIZED http status response is returned`() {
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val userPrincipal = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(exportService.exportBackup(workspaceRequest, userPrincipal)).thenThrow(UserNotAuthorizedException(userPrincipal))

        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/workspaces/${workspaceRequest.id}/export")
        ).andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

    @Test
    @WithMockUser
    fun `given a Workspace Id, when sending a GET REST request to the export endpoint and the Workspace does not exist, then a NOT_FOUND http status response is returned`() {
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val userPrincipal = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        Mockito.`when`(exportService.exportBackup(workspaceRequest, userPrincipal)).thenThrow(WorkspaceNotExistsException(workspaceRequest))

        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/workspaces/${workspaceRequest.id}/export")
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }
}
