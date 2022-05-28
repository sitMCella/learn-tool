package de.mcella.spring.learntool.import.integration

import de.mcella.spring.learntool.BackendApplication
import de.mcella.spring.learntool.IntegrationTest
import de.mcella.spring.learntool.TestSecurityConfiguration
import de.mcella.spring.learntool.WithMockUser
import de.mcella.spring.learntool.card.storage.CardEntity
import de.mcella.spring.learntool.card.storage.CardRepository
import de.mcella.spring.learntool.learn.storage.LearnCardEntity
import de.mcella.spring.learntool.learn.storage.LearnCardRepository
import de.mcella.spring.learntool.security.UserPrincipal
import de.mcella.spring.learntool.user.AuthProvider
import de.mcella.spring.learntool.user.dto.UserId
import de.mcella.spring.learntool.user.storage.UserEntity
import de.mcella.spring.learntool.user.storage.UserRepository
import de.mcella.spring.learntool.workspace.storage.WorkspaceEntity
import de.mcella.spring.learntool.workspace.storage.WorkspaceRepository
import java.io.File
import java.net.URI
import java.time.Instant
import java.util.Collections
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
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.security.oauth2.provider.OAuth2Request
import org.springframework.security.oauth2.provider.token.TokenStore
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.util.LinkedMultiValueMap
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

@RunWith(SpringRunner::class)
@Category(IntegrationTest::class)
@SpringBootTest(classes = [TestSecurityConfiguration::class, BackendApplication::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = [ImportIntegrationTest.Companion.Initializer::class])
class ImportIntegrationTest {

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
    lateinit var cardRepository: CardRepository

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var workspaceRepository: WorkspaceRepository

    @Autowired
    lateinit var learnCardRepository: LearnCardRepository

    @Before
    fun setUp() {
        learnCardRepository.deleteAll()
        cardRepository.deleteAll()
        workspaceRepository.deleteAll()
        val scope: MutableSet<String> = HashSet()
        scope.add("user")
        scope.add("test@google.com")
        val token = DefaultOAuth2AccessToken("FOO")
        val oAuth2Request = OAuth2Request(null, "1", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), true, scope, null, null, null, null)
        val userPrincipal = UserPrincipal(1L, "test@google.com", "password", Collections.singletonList(SimpleGrantedAuthority("ROLE_USER")), emptyMap())
        val auth = OAuth2Authentication(oAuth2Request, TestingAuthenticationToken(userPrincipal, null, "ROLE_USER"))
        tokenStore.storeAccessToken(token, auth)
    }

    // curl -X POST --header "Content-Type:multipart/form-data" -F backup=@/path/to/backup.zip  http://localhost:8080/api/workspaces/import
    @Test
    @WithMockUser
    fun `given a backup file, when a POST REST request is performed to the import endpoint, then the data is imported`() {
        val userId = UserId(1L)
        val user = UserEntity(userId.id, "user", "test@google.com", "", true, "", AuthProvider.local, "")
        userRepository.save(user)
        val parameters = LinkedMultiValueMap<String, Any>()
        parameters.add("backup", FileSystemResource(File("src/test/resources/backup.zip")))
        val headers = HttpHeaders()
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer FOO")
        headers.contentType = MediaType.MULTIPART_FORM_DATA
        val entity = HttpEntity(parameters, headers)

        testRestTemplate.postForEntity(URI("http://localhost:$port/api/workspaces/import"), entity, String::class.java)

        val workspaceEntities = workspaceRepository.findAll()
        assertEquals(1, workspaceEntities.size)
        val expectedWorkspaceEntity = WorkspaceEntity(workspaceEntities[0].id, "Workspace Name", userId.id)
        val expectedWorkspaceEntities = listOf(expectedWorkspaceEntity)
        assertEquals(expectedWorkspaceEntities, workspaceEntities)
        assertTrue { cardRepository.count() == 1L }
        val cardEntities = cardRepository.findAll()
        cardRepository.findAll().forEach {
            val expectedCardEntity = CardEntity("a1900ca7-dc58-4360-b41c-537d933bc9c1", workspaceEntities[0].id, "This is a \"question\"", "This, is a response", it.creationDate)
            val expectedCardEntities = listOf(expectedCardEntity)
            assertEquals(expectedCardEntities, cardEntities)
        }
        val learnCardEntities = learnCardRepository.findAll()
        assertEquals(1, learnCardEntities.size)
        val lastReview = Instant.ofEpochMilli(1637090403000)
        val expectedLearnCardEntity = LearnCardEntity("a1900ca7-dc58-4360-b41c-537d933bc9c1", workspaceEntities[0].id, lastReview, lastReview, 0, 1.3f, 0)
        val expectedLearnCardEntities = listOf(expectedLearnCardEntity)
        assertEquals(expectedLearnCardEntities, learnCardEntities)
    }
}
