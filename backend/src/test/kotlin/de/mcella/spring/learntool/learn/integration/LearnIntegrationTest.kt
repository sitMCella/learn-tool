package de.mcella.spring.learntool.learn.integration

import de.mcella.spring.learntool.BackendApplication
import de.mcella.spring.learntool.IntegrationTest
import de.mcella.spring.learntool.card.storage.Card
import de.mcella.spring.learntool.card.storage.CardRepository
import de.mcella.spring.learntool.learn.EvaluationParameters
import de.mcella.spring.learntool.learn.LearnCardParameters
import de.mcella.spring.learntool.learn.algorithm.OutputValues
import de.mcella.spring.learntool.learn.storage.LearnCard
import de.mcella.spring.learntool.learn.storage.LearnCardRepository
import de.mcella.spring.learntool.workspace.storage.Workspace
import de.mcella.spring.learntool.workspace.storage.WorkspaceRepository
import java.net.URI
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
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
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner
import org.testcontainers.containers.PostgreSQLContainer

@RunWith(SpringRunner::class)
@Category(IntegrationTest::class)
@SpringBootTest(classes = [BackendApplication::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = [LearnIntegrationTest.Companion.Initializer::class])
class LearnIntegrationTest {

    companion object {
        @ClassRule
        @JvmField
        val postgresql = PostgreSQLContainer<Nothing>()

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
    }

    @Test
    fun `given a Workspace name, when a POST REST request is performed to the learn endpoint, then the LearnCard is created and the response body contains the LearnCard`() {
        val workspaceName = "workspaceTest"
        val workspace = Workspace(workspaceName)
        workspaceRepository.save(workspace)
        val cardId = "9e493dc0-ef75-403f-b5d6-ed510634f8a6"
        val card = Card(cardId, workspaceName, "question", "response")
        cardRepository.save(card)
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val learnCardParameters = LearnCardParameters(cardId)
        val request = HttpEntity(learnCardParameters, headers)

        val responseEntity = testRestTemplate.postForObject(URI("http://localhost:$port/api/workspaces/$workspaceName/learn"), request, LearnCard::class.java)

        val learnCards = learnCardRepository.findAll()
        assertTrue { learnCards.size == 1 }
        val createdLearnCard = learnCards[0]
        assertEquals(cardId, createdLearnCard.id)
        assertEquals(workspaceName, createdLearnCard.workspaceName)
        assertEquals(workspaceName, createdLearnCard.workspaceName)
        val expectedLearnCard = LearnCard.createInitial(cardId, workspaceName, createdLearnCard.lastReview)
        assertEquals(expectedLearnCard, responseEntity)
    }

    @Test
    fun `given a Workspace name and a LeanCard, when a GET REST request is performed to the learn endpoint, then the response HTTP Status is 200 OK and the response body contains a Card`() {
        val workspaceName = "workspaceTest"
        val workspace = Workspace(workspaceName)
        workspaceRepository.save(workspace)
        val cardId = "9e493dc0-ef75-403f-b5d6-ed510634f8a6"
        val card = Card(cardId, workspaceName, "question", "response")
        cardRepository.save(card)
        val learnCard = LearnCard.createInitial(cardId, workspaceName, Instant.now())
        learnCardRepository.save(learnCard)

        val responseEntity = testRestTemplate.getForEntity(
            URI("http://localhost:$port/api/workspaces/$workspaceName/learn"),
            Card::class.java
        )

        val expectedResponseEntity = ResponseEntity.status(HttpStatus.OK).body(card)
        assertEquals(expectedResponseEntity.statusCode, responseEntity.statusCode)
        assertEquals(expectedResponseEntity.body, responseEntity.body)
    }

    @Test
    fun `given a Workspace name and a LearnCard with nextReview before the current date, when a GET REST request is performed to the learn endpoint, then the response HTTP Status is 200 OK and the response body contains a Card`() {
        val workspaceName = "workspaceTest"
        val workspace = Workspace(workspaceName)
        workspaceRepository.save(workspace)
        val cardId = "9e493dc0-ef75-403f-b5d6-ed510634f8a6"
        val card = Card(cardId, workspaceName, "question", "response")
        cardRepository.save(card)
        val outputValues = OutputValues(1, 1, 1.0f)
        val learnCard = LearnCard.create(cardId, workspaceName, outputValues, Instant.now().minus(2, ChronoUnit.DAYS))
        learnCardRepository.save(learnCard)

        val responseEntity = testRestTemplate.getForEntity(
                URI("http://localhost:$port/api/workspaces/$workspaceName/learn"),
                Card::class.java
        )

        val expectedResponseEntity = ResponseEntity.status(HttpStatus.OK).body(card)
        assertEquals(expectedResponseEntity.statusCode, responseEntity.statusCode)
        assertEquals(expectedResponseEntity.body, responseEntity.body)
    }

    @Test
    fun `given a Workspace name and the evaluation parameters, when a PUT REST request is performed to the learn endpoint, then the response HTTP Status is 200 OK and the response body contains the LearnCard`() {
        val workspaceName = "workspaceTest"
        val workspace = Workspace(workspaceName)
        workspaceRepository.save(workspace)
        val cardId = "9e493dc0-ef75-403f-b5d6-ed510634f8a6"
        val card = Card(cardId, workspaceName, "question", "response")
        cardRepository.save(card)
        val instant = Instant.now()
        val learnCard = LearnCard.createInitial(cardId, workspaceName, instant)
        learnCardRepository.save(learnCard)
        val evaluationParameters = EvaluationParameters(cardId, 5)
        val request = HttpEntity(evaluationParameters)

        val responseEntity = testRestTemplate.exchange(URI("http://localhost:$port/api/workspaces/$workspaceName/learn"), HttpMethod.PUT, request, LearnCard::class.java)

        val lastReview = (responseEntity as ResponseEntity<LearnCard>).body?.lastReview
        val expectedLearnCard = LearnCard(cardId, workspaceName, lastReview!!, lastReview.plus(Duration.ofDays(1)), 1, 1.4f, 1)
        val expectedResponseEntity = ResponseEntity.status(HttpStatus.OK).body(expectedLearnCard)
        assertEquals(expectedResponseEntity.statusCode, responseEntity.statusCode)
        assertEquals(expectedResponseEntity.body, (responseEntity as ResponseEntity<LearnCard>).body)
    }

    @Test
    fun `given a Workspace name and a Card id, when a DELETE REST request is performed to the learn endpoint, then the LearnCard is deleted`() {
        val workspaceName = "workspaceTest"
        val workspace = Workspace(workspaceName)
        workspaceRepository.save(workspace)
        val cardId = "9e493dc0-ef75-403f-b5d6-ed510634f8a6"
        val card = Card(cardId, workspaceName, "question", "response")
        cardRepository.save(card)
        val instant = Instant.now()
        val learnCard = LearnCard.createInitial(cardId, workspaceName, instant)
        learnCardRepository.save(learnCard)
        val learnCardParameters = LearnCardParameters(cardId)
        val request = HttpEntity(learnCardParameters)

        testRestTemplate.exchange(URI("http://localhost:$port/api/workspaces/$workspaceName/learn"), HttpMethod.DELETE, request, LearnCard::class.java)

        val learnCards = learnCardRepository.findAll()
        assertTrue { learnCards.size == 0 }
    }
}
