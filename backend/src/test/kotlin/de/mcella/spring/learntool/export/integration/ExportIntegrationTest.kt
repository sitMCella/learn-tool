package de.mcella.spring.learntool.export.integration

import de.mcella.spring.learntool.BackendApplication
import de.mcella.spring.learntool.IntegrationTest
import de.mcella.spring.learntool.TestSecurityConfiguration
import de.mcella.spring.learntool.card.dto.CardId
import de.mcella.spring.learntool.card.storage.CardEntity
import de.mcella.spring.learntool.card.storage.CardRepository
import de.mcella.spring.learntool.learn.algorithm.OutputValues
import de.mcella.spring.learntool.learn.storage.LearnCardEntity
import de.mcella.spring.learntool.learn.storage.LearnCardRepository
import de.mcella.spring.learntool.security.UserPrincipal
import de.mcella.spring.learntool.user.AuthProvider
import de.mcella.spring.learntool.user.dto.UserId
import de.mcella.spring.learntool.user.storage.UserEntity
import de.mcella.spring.learntool.user.storage.UserRepository
import de.mcella.spring.learntool.workspace.dto.WorkspaceRequest
import de.mcella.spring.learntool.workspace.storage.WorkspaceEntity
import de.mcella.spring.learntool.workspace.storage.WorkspaceRepository
import java.net.URI
import java.time.Instant
import java.util.Collections
import java.util.zip.ZipInputStream
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
import org.springframework.core.io.Resource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
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
@ContextConfiguration(initializers = [ExportIntegrationTest.Companion.Initializer::class])
class ExportIntegrationTest {

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
    lateinit var cardRepository: CardRepository

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

    @Test
    fun `given a Workspace Id, when a GET REST request is performed to the export endpoint, then the backup file is created and the response body contains the file`() {
        val userId = UserId(1L)
        val user = UserEntity(userId.id, "user", "test@google.com", "", true, "", AuthProvider.local, "")
        userRepository.save(user)
        val workspaceRequest = WorkspaceRequest("workspaceId")
        val workspace = WorkspaceEntity(workspaceRequest.id, "Workspace Name", userId.id)
        workspaceRepository.save(workspace)
        val cardId = CardId("a1900ca7-dc58-4360-b41c-537d933bc9c1")
        val cardEntity = CardEntity(cardId.id, workspaceRequest.id, "This is a \"question\"", "This, is a response")
        cardRepository.save(cardEntity)
        val outputValues = OutputValues(0, 0, 1.3f)
        val review = Instant.ofEpochMilli(1637090403000)
        val learnCard = LearnCardEntity.create(cardId, workspaceRequest, outputValues, review)
        learnCardRepository.save(learnCard)
        val headers = HttpHeaders()
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer FOO")
        headers.accept = listOf(MediaType.APPLICATION_OCTET_STREAM)
        val request = HttpEntity(null, headers)

        val responseEntity = testRestTemplate.exchange(URI("http://localhost:$port/api/workspaces/${workspaceRequest.id}/export"), HttpMethod.GET, request, Resource::class.java)

        // Store the zip file. This file is used in the ImportIntegrationTest.
        // val outputStream: OutputStream = FileOutputStream(File("src/test/resources/backup.zip"))
        // IOUtils.copy(responseEntity.body!!.inputStream, outputStream)

        val zipInputStream = ZipInputStream(responseEntity.body!!.inputStream)
        val files = zipInputStream.use { zipInputStreamResource ->
            generateSequence { zipInputStreamResource.nextEntry }
                    .filterNot { it.isDirectory }
                    .map {
                        UnzippedFile(
                                filename = it.name,
                                content = zipInputStreamResource.readAllBytes()
                        )
                    }.toList()
        }
        val expectedFileNames = listOf("/workspaces.csv", "/cards.csv", "/learn_cards.csv")
        assertEquals(expectedFileNames.count(), files.count())
        for (file in files) {
            assertTrue(expectedFileNames.contains(file.filename))
            when (file.filename) {
                "/workspaces.csv" -> {
                    assertEquals("name\r\nWorkspace Name\r\n", String(file.content))
                }
                "/cards.csv" -> {
                    assertEquals("id,question,response\r\n" + cardId.id + ",\"This is a \"\"question\"\"\",\"This, is a response\"\r\n", String(file.content))
                }
                "/learn_cards.csv" -> {
                    assertEquals("id,last_review,next_review,repetitions,ease_factor,interval_days\r\n" + cardId.id + ",2021-11-16T19:20:03Z,2021-11-16T19:20:03Z,0,1.3,0\r\n", String(file.content))
                }
            }
        }
    }
}
