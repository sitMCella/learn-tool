package de.mcella.spring.learntool.card.integration

import de.mcella.spring.learntool.BackendApplication
import de.mcella.spring.learntool.IntegrationTest
import de.mcella.spring.learntool.card.CardContent
import de.mcella.spring.learntool.card.storage.Card
import de.mcella.spring.learntool.card.storage.CardRepository
import de.mcella.spring.learntool.workspace.storage.Workspace
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
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner
import org.testcontainers.containers.PostgreSQLContainer

@RunWith(SpringRunner::class)
@Category(IntegrationTest::class)
@SpringBootTest(classes = [BackendApplication::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = [CardIntegrationTest.Companion.Initializer::class])
class CardIntegrationTest {

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

    @Before
    fun setUp() {
        cardRepository.deleteAll()
    }

    @Test
    fun `given a Workspace name and a CardContent, when a POST REST request is sent to the cards endpoint, then a Card is created and the response body contains the Card`() {
        val workspaceName = "workspaceTest"
        val cardContent = CardContent("question", "response")
        val request = HttpEntity(cardContent)
        val workspace = Workspace(workspaceName)
        workspaceRepository.save(workspace)

        val responseEntity = testRestTemplate.postForObject(URI("http://localhost:$port/workspaces/$workspaceName/cards"), request, Card::class.java)

        val cards = cardRepository.findAll()
        assertTrue { cards.size == 1 }
        val createdCard = cards[0]
        assertNotNull(createdCard.id)
        assertEquals(workspaceName, createdCard.workspaceName)
        assertEquals("question", createdCard.question)
        assertEquals("response", createdCard.response)
        val expectedCard = Card(createdCard.id, workspaceName, "question", "response")
        assertEquals(expectedCard, responseEntity)
    }

    @Test
    fun `given a Workspace name and a Cards CSV stream content, when a POST REST request is sent to the cards many csv endpoint, then the Cards are created`() {
        val workspaceName = "workspaceTest"
        val streamContent = "question,response\nquestionTest1,responseTest1\nquestionTest2,responseTest2"
        val request = HttpEntity(ByteArrayResource(streamContent.toByteArray()))
        val workspace = Workspace(workspaceName)
        workspaceRepository.save(workspace)

        testRestTemplate.postForObject(URI("http://localhost:$port/workspaces/$workspaceName/cards/many.csv"), request, String::class.java)

        val cards = cardRepository.findAll()
        assertTrue { cards.size == 2 }
        for (i in 0 until 1) {
            val createdCard = cards[i]
            assertNotNull(createdCard.id)
            assertEquals(workspaceName, createdCard.workspaceName)
            assertEquals("questionTest${i + 1}", createdCard.question)
            assertEquals("responseTest${i + 1}", createdCard.response)
        }
    }
}
