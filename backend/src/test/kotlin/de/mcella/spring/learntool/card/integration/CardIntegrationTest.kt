package de.mcella.spring.learntool.card.integration

import de.mcella.spring.learntool.BackendApplication
import de.mcella.spring.learntool.IntegrationTest
import de.mcella.spring.learntool.card.dto.Card
import de.mcella.spring.learntool.card.dto.CardContent
import de.mcella.spring.learntool.card.dto.CardId
import de.mcella.spring.learntool.card.storage.CardEntity
import de.mcella.spring.learntool.card.storage.CardRepository
import de.mcella.spring.learntool.workspace.dto.Workspace
import de.mcella.spring.learntool.workspace.storage.WorkspaceEntity
import de.mcella.spring.learntool.workspace.storage.WorkspaceRepository
import java.net.URI
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
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
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

@RunWith(SpringRunner::class)
@Category(IntegrationTest::class)
@SpringBootTest(classes = [BackendApplication::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = [CardIntegrationTest.Companion.Initializer::class])
class CardIntegrationTest {

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
    lateinit var cardRepository: CardRepository

    @Autowired
    lateinit var workspaceRepository: WorkspaceRepository

    @Before
    fun setUp() {
        cardRepository.deleteAll()
    }

    @Test
    fun `given a Workspace name and a CardContent, when a POST REST request is sent to the cards endpoint, then a Card is created and the response body contains the Card`() {
        val workspace = Workspace("workspaceTest")
        val cardContent = CardContent("question", "response")
        val request = HttpEntity(cardContent)
        val workspaceEntity = WorkspaceEntity(workspace.name)
        workspaceRepository.save(workspaceEntity)

        val responseEntity = testRestTemplate.postForObject(URI("http://localhost:$port/api/workspaces/${workspace.name}/cards"), request, Card::class.java)

        assertTrue { cardRepository.count() == 1L }
        cardRepository.findAll().forEach {
            val createdCard = it
            assertNotNull(createdCard.id)
            assertEquals(workspace.name, createdCard.workspaceName)
            assertEquals("question", createdCard.question)
            assertEquals("response", createdCard.response)
            val expectedCard = Card(createdCard.id, workspace.name, "question", "response")
            assertEquals(expectedCard, responseEntity)
        }
    }

    @Test
    fun `given a Workspace name, a Card Id, and a CardContent, when a PUT REST request is sent to the cards endpoint, then a Card is updated and the response body contains the Card`() {
        val workspace = Workspace("workspaceTest")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val cardContent = CardContent("question", "response")
        val workspaceEntity = WorkspaceEntity(workspace.name)
        workspaceRepository.save(workspaceEntity)
        val cardEntity = CardEntity.create(cardId, workspace, cardContent)
        cardRepository.save(cardEntity)
        val updatedCardContent = CardContent("updated question", "updated response")
        val request = HttpEntity(updatedCardContent)

        var responseEntity = testRestTemplate.exchange(URI("http://localhost:$port/api/workspaces/${workspace.name}/cards/${cardId.id}"), HttpMethod.PUT, request, Card::class.java)

        assertTrue { cardRepository.count() == 1L }
        cardRepository.findAll().forEach {
            val updatedCard = it
            assertEquals(cardId.id, updatedCard.id)
            assertEquals(workspace.name, updatedCard.workspaceName)
            assertEquals("updated question", updatedCard.question)
            assertEquals("updated response", updatedCard.response)
            assertEquals(HttpStatus.OK, responseEntity.statusCode)
        }
    }

    @Test
    fun `given a Workspace name and a Card Id, when a DELETE REST request is sent to the cards endpoint, then a Card is deleted`() {
        val workspace = Workspace("workspaceTest")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val cardContent = CardContent("question", "response")
        val workspaceEntity = WorkspaceEntity(workspace.name)
        workspaceRepository.save(workspaceEntity)
        val cardEntity = CardEntity.create(cardId, workspace, cardContent)
        cardRepository.save(cardEntity)

        testRestTemplate.delete(URI("http://localhost:$port/api/workspaces/${workspace.name}/cards/${cardId.id}"))

        assertTrue { cardRepository.count() == 0L }
    }

    @Test
    fun `given a Workspace name and a Cards CSV stream content, when a POST REST request is sent to the cards many csv endpoint, then the Cards are created`() {
        val workspace = Workspace("workspaceTest")
        val streamContent = "question,response\nquestionTest1,responseTest1\nquestionTest2,responseTest2"
        val request = HttpEntity(ByteArrayResource(streamContent.toByteArray()))
        val workspaceEntity = WorkspaceEntity(workspace.name)
        workspaceRepository.save(workspaceEntity)

        testRestTemplate.postForObject(URI("http://localhost:$port/api/workspaces/${workspace.name}/cards/many.csv"), request, String::class.java)

        assertTrue { cardRepository.count() == 2L }
        var i = 0
        cardRepository.findAll().forEach {
            val createdCard = it
            i = i.inc()
            assertNotNull(createdCard.id)
            assertEquals(workspace.name, createdCard.workspaceName)
            assertEquals("questionTest$i", createdCard.question)
            assertEquals("responseTest$i", createdCard.response)
        }
    }

    @Test
    fun `given a Workspace named, when a GET REST request is sent to the cards endpoint, then the response HTTP Status is 200 OK and the response body contains the list of Cards`() {
        val workspace = Workspace("workspaceTest")
        val cardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val cardContent = CardContent("question", "response")
        val workspaceEntity = WorkspaceEntity(workspace.name)
        workspaceRepository.save(workspaceEntity)
        val cardEntity = CardEntity.create(cardId, workspace, cardContent)
        cardRepository.save(cardEntity)

        val responseEntity = testRestTemplate.getForEntity(URI("http://localhost:$port/api/workspaces/${workspace.name}/cards"), List::class.java)

        val expectedCard = Card(cardId.id, workspace.name, "question", "response")
        val expectedCards = listOf(expectedCard)
        val expectedResponseEntity = ResponseEntity.status(HttpStatus.OK).body(expectedCards)
        assertEquals(expectedResponseEntity.statusCode, responseEntity.statusCode)
        val cards = responseEntity.body as List<*>
        assertTrue { cards.size == 1 }
    }
}
