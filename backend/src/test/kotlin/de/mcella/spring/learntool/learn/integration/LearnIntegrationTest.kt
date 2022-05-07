package de.mcella.spring.learntool.learn.integration

import de.mcella.spring.learntool.BackendApplication
import de.mcella.spring.learntool.IntegrationTest
import de.mcella.spring.learntool.TestSecurityConfiguration
import de.mcella.spring.learntool.card.dto.Card
import de.mcella.spring.learntool.card.dto.CardId
import de.mcella.spring.learntool.card.storage.CardEntity
import de.mcella.spring.learntool.card.storage.CardRepository
import de.mcella.spring.learntool.learn.algorithm.OutputValues
import de.mcella.spring.learntool.learn.dto.EvaluationParameters
import de.mcella.spring.learntool.learn.storage.LearnCardEntity
import de.mcella.spring.learntool.learn.storage.LearnCardRepository
import de.mcella.spring.learntool.security.UserPrincipal
import de.mcella.spring.learntool.user.AuthProvider
import de.mcella.spring.learntool.user.dto.UserId
import de.mcella.spring.learntool.user.storage.UserEntity
import de.mcella.spring.learntool.user.storage.UserRepository
import de.mcella.spring.learntool.workspace.dto.Workspace
import de.mcella.spring.learntool.workspace.dto.WorkspaceRequest
import de.mcella.spring.learntool.workspace.storage.WorkspaceEntity
import de.mcella.spring.learntool.workspace.storage.WorkspaceRepository
import java.net.URI
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
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
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
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
@ContextConfiguration(initializers = [LearnIntegrationTest.Companion.Initializer::class])
class LearnIntegrationTest {

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
    fun `given a Workspace name, when a POST REST request is performed to the learn endpoint, then the LearnCard is created and the response body contains the LearnCard`() {
        val userId = UserId(1L)
        val user = UserEntity(userId.id, "user", "test@google.com", "", true, "", AuthProvider.local, "")
        userRepository.save(user)
        val workspace = Workspace("workspaceTest", userId)
        val workspaceEntity = WorkspaceEntity.create(workspace)
        workspaceRepository.save(workspaceEntity)
        val workspaceRequest = WorkspaceRequest(workspaceEntity.name)
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val cardEntity = CardEntity(cardId.id, workspace.name, "question", "response")
        cardRepository.save(cardEntity)
        val headers = HttpHeaders()
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer FOO")
        headers.contentType = MediaType.APPLICATION_JSON
        val request = HttpEntity("", headers)

        val responseEntity = testRestTemplate.postForObject(URI("http://localhost:$port/api/workspaces/${workspace.name}/learn/${cardId.id}"), request, LearnCardEntity::class.java)

        val learnCards = learnCardRepository.findAll()
        assertTrue { learnCards.size == 1 }
        val createdLearnCard = learnCards[0]
        assertEquals(cardId.id, createdLearnCard.id)
        assertEquals(workspace.name, createdLearnCard.workspaceName)
        assertEquals(workspace.name, createdLearnCard.workspaceName)
        val expectedLearnCard = LearnCardEntity.createInitial(cardId, workspaceRequest, createdLearnCard.lastReview)
        assertEquals(expectedLearnCard, responseEntity)
    }

    @Test
    fun `given a Workspace name and a LeanCard, when a GET REST request is performed to the learn endpoint, then the response HTTP Status is 200 OK and the response body contains a Card`() {
        val userId = UserId(1L)
        val user = UserEntity(userId.id, "user", "test@google.com", "", true, "", AuthProvider.local, "")
        userRepository.save(user)
        val workspace = Workspace("workspaceTest", userId)
        val workspaceEntity = WorkspaceEntity.create(workspace)
        workspaceRepository.save(workspaceEntity)
        val workspaceRequest = WorkspaceRequest(workspace.name)
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val cardEntity = CardEntity(cardId.id, workspace.name, "question", "response")
        cardRepository.save(cardEntity)
        val learnCard = LearnCardEntity.createInitial(cardId, workspaceRequest, Instant.now())
        learnCardRepository.save(learnCard)
        val headers = HttpHeaders()
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer FOO")
        headers.accept = listOf(MediaType.APPLICATION_JSON)
        val request = HttpEntity(null, headers)

        val responseEntity = testRestTemplate.exchange(URI("http://localhost:$port/api/workspaces/${workspace.name}/learn"), HttpMethod.GET, request, Card::class.java)

        val expectedCard = Card(cardId.id, workspace.name, "question", "response")
        val expectedResponseEntity = ResponseEntity.status(HttpStatus.OK).body(expectedCard)
        assertEquals(expectedResponseEntity.statusCode, responseEntity.statusCode)
        assertEquals(expectedResponseEntity.body, responseEntity.body)
    }

    @Test
    fun `given a Workspace name and a LearnCard with nextReview before the current date, when a GET REST request is performed to the learn endpoint, then the response HTTP Status is 200 OK and the response body contains a Card`() {
        val userId = UserId(1L)
        val user = UserEntity(userId.id, "user", "test@google.com", "", true, "", AuthProvider.local, "")
        userRepository.save(user)
        val workspace = Workspace("workspaceTest", userId)
        val workspaceEntity = WorkspaceEntity.create(workspace)
        workspaceRepository.save(workspaceEntity)
        val workspaceRequest = WorkspaceRequest(workspace.name)
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val cardEntity = CardEntity(cardId.id, workspace.name, "question", "response")
        cardRepository.save(cardEntity)
        val outputValues = OutputValues(1, 1, 1.0f)
        val learnCard = LearnCardEntity.create(cardId, workspaceRequest, outputValues, Instant.now().minus(2, ChronoUnit.DAYS))
        learnCardRepository.save(learnCard)
        val headers = HttpHeaders()
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer FOO")
        headers.accept = listOf(MediaType.APPLICATION_JSON)
        val request = HttpEntity(null, headers)

        val responseEntity = testRestTemplate.exchange(URI("http://localhost:$port/api/workspaces/${workspace.name}/learn"), HttpMethod.GET, request, Card::class.java)

        val expectedCard = Card(cardId.id, workspace.name, "question", "response")
        val expectedResponseEntity = ResponseEntity.status(HttpStatus.OK).body(expectedCard)
        assertEquals(expectedResponseEntity.statusCode, responseEntity.statusCode)
        assertEquals(expectedResponseEntity.body, responseEntity.body)
    }

    @Test
    fun `given a Workspace name and the evaluation parameters, when a PUT REST request is performed to the learn endpoint, then the response HTTP Status is 200 OK and the response body contains the LearnCard`() {
        val userId = UserId(1L)
        val user = UserEntity(userId.id, "user", "test@google.com", "", true, "", AuthProvider.local, "")
        userRepository.save(user)
        val workspace = Workspace("workspaceTest", userId)
        val workspaceEntity = WorkspaceEntity.create(workspace)
        workspaceRepository.save(workspaceEntity)
        val workspaceRequest = WorkspaceRequest(workspace.name)
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val cardEntity = CardEntity(cardId.id, workspace.name, "question", "response")
        cardRepository.save(cardEntity)
        val instant = Instant.now()
        val learnCard = LearnCardEntity.createInitial(cardId, workspaceRequest, instant)
        learnCardRepository.save(learnCard)
        val evaluationParameters = EvaluationParameters(5)
        val headers = HttpHeaders()
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer FOO")
        headers.contentType = MediaType.APPLICATION_JSON
        headers.accept = listOf(MediaType.APPLICATION_JSON)
        val request = HttpEntity(evaluationParameters, headers)

        val responseEntity = testRestTemplate.exchange(URI("http://localhost:$port/api/workspaces/${workspace.name}/learn/${cardId.id}"), HttpMethod.PUT, request, LearnCardEntity::class.java)

        val lastReview = (responseEntity as ResponseEntity<LearnCardEntity>).body?.lastReview
        val expectedLearnCard = LearnCardEntity(cardId.id, workspace.name, lastReview!!, lastReview.plus(Duration.ofDays(1)), 1, 1.4f, 1)
        val expectedResponseEntity = ResponseEntity.status(HttpStatus.OK).body(expectedLearnCard)
        assertEquals(expectedResponseEntity.statusCode, responseEntity.statusCode)
        assertEquals(expectedResponseEntity.body, (responseEntity as ResponseEntity<LearnCardEntity>).body)
    }

    @Test
    fun `given a Workspace name and a Card id, when a DELETE REST request is performed to the learn endpoint, then the LearnCard is deleted`() {
        val userId = UserId(1L)
        val user = UserEntity(userId.id, "user", "test@google.com", "", true, "", AuthProvider.local, "")
        userRepository.save(user)
        val workspace = Workspace("workspaceTest", userId)
        val workspaceEntity = WorkspaceEntity.create(workspace)
        workspaceRepository.save(workspaceEntity)
        val workspaceRequest = WorkspaceRequest(workspace.name)
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val cardEntity = CardEntity(cardId.id, workspace.name, "question", "response")
        cardRepository.save(cardEntity)
        val instant = Instant.now()
        val learnCard = LearnCardEntity.createInitial(cardId, workspaceRequest, instant)
        learnCardRepository.save(learnCard)
        val headers = HttpHeaders()
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer FOO")
        val request = HttpEntity(null, headers)

        testRestTemplate.exchange(URI("http://localhost:$port/api/workspaces/${workspace.name}/learn/${cardId.id}"), HttpMethod.DELETE, request, Object::class.java)

        val learnCards = learnCardRepository.findAll()
        assertTrue { learnCards.size == 0 }
    }
}
