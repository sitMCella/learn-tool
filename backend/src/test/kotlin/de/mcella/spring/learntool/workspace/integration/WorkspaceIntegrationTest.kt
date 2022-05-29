package de.mcella.spring.learntool.workspace.integration

import de.mcella.spring.learntool.BackendApplication
import de.mcella.spring.learntool.IntegrationTest
import de.mcella.spring.learntool.TestSecurityConfiguration
import de.mcella.spring.learntool.security.UserPrincipal
import de.mcella.spring.learntool.user.AuthProvider
import de.mcella.spring.learntool.user.dto.UserId
import de.mcella.spring.learntool.user.storage.UserEntity
import de.mcella.spring.learntool.user.storage.UserRepository
import de.mcella.spring.learntool.workspace.dto.Workspace
import de.mcella.spring.learntool.workspace.dto.WorkspaceCreateRequest
import de.mcella.spring.learntool.workspace.dto.WorkspaceId
import de.mcella.spring.learntool.workspace.storage.WorkspaceEntity
import de.mcella.spring.learntool.workspace.storage.WorkspaceRepository
import java.net.URI
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.Before
import org.junit.ClassRule
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.security.oauth2.provider.OAuth2Request
import org.springframework.security.oauth2.provider.token.TokenStore
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

@RunWith(SpringRunner::class)
@Category(IntegrationTest::class)
@SpringBootTest(classes = [TestSecurityConfiguration::class, BackendApplication::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = [WorkspaceIntegrationTest.Companion.Initializer::class])
class WorkspaceIntegrationTest {

    companion object {
        @ClassRule
        @JvmField
        val postgresql = PostgreSQLContainer<Nothing>(DockerImageName.parse(PostgreSQLContainer.IMAGE).withTag(PostgreSQLContainer.DEFAULT_TAG))

        class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {

            override fun initialize(applicationContext: ConfigurableApplicationContext) {
                TestPropertyValues.of(
                    "spring.datasource.url=${postgresql.jdbcUrl}",
                    "spring.datasource.username=${postgresql.username}",
                    "spring.datasource.password=${postgresql.password}",
                    "spring.datasource.platform=postgresql",
                    "spring.datasource.initialization-mode=always",
                    "spring.datasource.initialize=1",
                    "spring.datasource.schema=classpath:/db/postgresql-schema.sql",
                    "spring.datasource.driver.class.name=org.postgresql.Driver",
                    "spring.jpa.database.platform=org.hibernate.dialect.PostgreSQLDialect",
                    "spring.jpa.hibernate.ddl=none"
                ).applyTo(applicationContext.environment)
            }
        }
    }

    @LocalServerPort
    var port: Int = 0

    @Autowired
    lateinit var testRestTemplate: TestRestTemplate

    @Autowired
    lateinit var tokenStore: TokenStore

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var workspaceRepository: WorkspaceRepository

    @Before
    fun setUp() {
        workspaceRepository.deleteAll()
        val scope: MutableSet<String> = HashSet()
        scope.add("user")
        scope.add("test@google.com")
        val token = DefaultOAuth2AccessToken("FOO")
        val oAuth2Request = OAuth2Request(null, "1", listOf(SimpleGrantedAuthority("ROLE_USER")), true, scope, null, null, null, null)
        val userPrincipal = UserPrincipal(1L, "test@google.com", "PassW@rD!", listOf(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        val auth = OAuth2Authentication(oAuth2Request, TestingAuthenticationToken(userPrincipal, null, "ROLE_USER"))
        tokenStore.storeAccessToken(token, auth)
    }

    @Test
    fun `given a Workspace create request, when a POST REST request is sent to the workspaces endpoint, then a Workspace is created and the http response body contains the Workspace`() {
        val userId = UserId(1L)
        val user = UserEntity(userId.id, "user", "test@google.com", "", true, "", AuthProvider.local, "")
        userRepository.save(user)
        val headers = HttpHeaders()
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer FOO")
        headers.contentType = MediaType.APPLICATION_JSON
        val workspaceCreateRequest = WorkspaceCreateRequest("Workspace Name")
        val request = HttpEntity(workspaceCreateRequest, headers)

        val responseEntity = testRestTemplate.postForObject(URI("http://localhost:$port/api/workspaces"), request, Workspace::class.java)

        val workspaces = workspaceRepository.findAll()
        assertTrue { workspaces.size == 1 }
        val workspaceEntity = WorkspaceEntity(workspaces[0].id, "Workspace Name", userId.id)
        assertTrue { workspaces.contains(workspaceEntity) }
        val userPrincipal = UserPrincipal(userId.id, "test@google.com", "PassW@rD!", listOf(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        val workspace = Workspace.create(WorkspaceId(workspaces[0].id), workspaceCreateRequest, userPrincipal)
        assertEquals(workspace, responseEntity)
    }

    @Test
    fun `given a Workspace create request, when a PUT REST request is sent to the workspaces endpoint, then the Workspace is updated and the http response body contains the Workspace`() {
        val userId = UserId(1L)
        val user = UserEntity(userId.id, "user", "test@google.com", "", true, "", AuthProvider.local, "")
        userRepository.save(user)
        val headers = HttpHeaders()
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer FOO")
        headers.contentType = MediaType.APPLICATION_JSON
        val workspaceId = WorkspaceId(UUID.randomUUID().toString())
        val initialWorkspace = Workspace(workspaceId.id, "Workspace Name", userId)
        val initialWorkspaceEntity = WorkspaceEntity.create(initialWorkspace)
        workspaceRepository.save(initialWorkspaceEntity)
        val workspaceCreateRequest = WorkspaceCreateRequest("Workspace Update Name")
        val request = HttpEntity(workspaceCreateRequest, headers)

        val responseEntity = testRestTemplate.exchange(URI("http://localhost:$port/api/workspaces/${workspaceId.id}"), HttpMethod.PUT, request, Workspace::class.java)

        assertEquals(HttpStatus.OK, responseEntity.statusCode)
        val workspaces = workspaceRepository.findAll()
        assertTrue { workspaces.size == 1 }
        val workspaceEntity = WorkspaceEntity(workspaceId.id, workspaceCreateRequest.name, userId.id)
        assertTrue { workspaces.contains(workspaceEntity) }
    }

    @Test
    fun `given a Workspace Id, when a DELETE REST request is sent to the workspaces endpoint, then the Workspace is deleted`() {
        val userId = UserId(1L)
        val user = UserEntity(userId.id, "user", "test@google.com", "", true, "", AuthProvider.local, "")
        userRepository.save(user)
        val headers = HttpHeaders()
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer FOO")
        headers.contentType = MediaType.APPLICATION_JSON
        val workspaceId = WorkspaceId(UUID.randomUUID().toString())
        val workspace = Workspace(workspaceId.id, "Workspace Name", userId)
        val workspaceEntity = WorkspaceEntity.create(workspace)
        workspaceRepository.save(workspaceEntity)
        val request = HttpEntity(null, headers)

        testRestTemplate.exchange(URI("http://localhost:$port/api/workspaces/${workspaceId.id}"), HttpMethod.DELETE, request, Object::class.java)

        val workspaces = workspaceRepository.findAll()
        assertTrue { workspaces.size == 0 }
    }

    @Test
    fun `when a GET REST request is sent to the workspaces endpoint, then the http response body contains the list of Workspaces of the authenticated user`() {
        val userId = UserId(1L)
        val user = UserEntity(userId.id, "user", "test@google.com", "", true, "", AuthProvider.local, "")
        userRepository.save(user)
        val anotherUserId = UserId(2L)
        val anotherUser = UserEntity(anotherUserId.id, "anotherUser", "another@google.com", "", true, "", AuthProvider.local, "")
        userRepository.save(anotherUser)
        val workspaceId1 = WorkspaceId(UUID.randomUUID().toString())
        val workspace1 = Workspace(workspaceId1.id, "Workspace Name 1", userId)
        val workspaceEntity1 = WorkspaceEntity.create(workspace1)
        workspaceRepository.save(workspaceEntity1)
        val workspaceId2 = WorkspaceId(UUID.randomUUID().toString())
        val workspace2 = Workspace(workspaceId2.id, "Workspace Name 2", userId)
        val workspaceEntity2 = WorkspaceEntity.create(workspace2)
        workspaceRepository.save(workspaceEntity2)
        val workspaceId3 = WorkspaceId(UUID.randomUUID().toString())
        val workspace3 = Workspace(workspaceId3.id, "Workspace Name 3", anotherUserId)
        val workspaceEntity3 = WorkspaceEntity.create(workspace3)
        workspaceRepository.save(workspaceEntity3)
        val headers = HttpHeaders()
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer FOO")
        headers.accept = listOf(MediaType.APPLICATION_JSON)
        val request = HttpEntity(null, headers)

        var responseEntity = testRestTemplate.exchange(URI("http://localhost:$port/api/workspaces"), HttpMethod.GET, request, List::class.java)

        assertEquals(HttpStatus.OK, responseEntity.statusCode)
        val workspaces = responseEntity.body as List<*>
        assertTrue { workspaces.size == 2 }
    }
}
