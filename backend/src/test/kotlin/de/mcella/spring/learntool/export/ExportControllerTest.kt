package de.mcella.spring.learntool.export

import de.mcella.spring.learntool.UnitTest
import de.mcella.spring.learntool.config.AppProperties
import de.mcella.spring.learntool.security.CustomUserDetailsService
import de.mcella.spring.learntool.security.TokenAuthenticationFilter
import de.mcella.spring.learntool.security.oauth2.CustomOAuth2UserService
import de.mcella.spring.learntool.security.oauth2.HttpCookieOAuth2AuthorizationRequestRepository
import de.mcella.spring.learntool.security.oauth2.OAuth2AuthenticationFailureHandler
import de.mcella.spring.learntool.security.oauth2.OAuth2AuthenticationSuccessHandler
import de.mcella.spring.learntool.workspace.dto.Workspace
import de.mcella.spring.learntool.workspace.exceptions.WorkspaceNotExistsException
import java.io.File
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
@WebMvcTest(ExportController::class)
@AutoConfigureWebClient
@AutoConfigureMockMvc(addFilters = false)
@EnableConfigurationProperties(AppProperties::class)
@TestPropertySource(properties = ["app.auth.tokenSecret=aaa", "app.auth.tokenExpirationMsec=123"])
class ExportControllerTest {

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
    private lateinit var exportService: ExportService

    @Test
    fun `given a Workspace name, when sending a GET REST request to the export endpoint and the Workspace exists, then the exportBackup method of ExportService is called and a backup file is returned`() {
        val workspace = Workspace("workspaceTest")
        val backup = File.createTempFile("backup", ".zip")
        Mockito.`when`(exportService.exportBackup(workspace)).thenReturn(backup)

        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/workspaces/${workspace.name}/export")
        ).andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .andExpect(MockMvcResultMatchers.content().bytes(backup.readBytes()))

        Mockito.verify(exportService).exportBackup(workspace)
    }

    @Test
    fun `given a Workspace name, when sending a GET REST request to the export endpoint and the Workspace does not exist, then a NOT_FOUND http status response is returned`() {
        val workspace = Workspace("workspaceTest")
        Mockito.`when`(exportService.exportBackup(workspace)).thenThrow(WorkspaceNotExistsException(workspace))

        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/workspaces/${workspace.name}/export")
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }
}
