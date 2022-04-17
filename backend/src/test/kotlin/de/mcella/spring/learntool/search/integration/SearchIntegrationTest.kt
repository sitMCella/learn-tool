package de.mcella.spring.learntool.search.integration

import de.mcella.spring.learntool.BackendApplication
import de.mcella.spring.learntool.IntegrationTest
import de.mcella.spring.learntool.card.dto.Card
import de.mcella.spring.learntool.card.dto.CardId
import de.mcella.spring.learntool.card.storage.CardEntity
import de.mcella.spring.learntool.card.storage.CardRepository
import de.mcella.spring.learntool.search.SearchPattern
import de.mcella.spring.learntool.workspace.dto.Workspace
import de.mcella.spring.learntool.workspace.storage.WorkspaceEntity
import de.mcella.spring.learntool.workspace.storage.WorkspaceRepository
import java.net.URI
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
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

@RunWith(SpringRunner::class)
@Category(IntegrationTest::class)
@SpringBootTest(classes = [BackendApplication::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = [SearchIntegrationTest.Companion.Initializer::class])
class SearchIntegrationTest {

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
    lateinit var workspaceRepository: WorkspaceRepository

    @Autowired
    lateinit var cardRepository: CardRepository

    @Before
    fun setUp() {
        cardRepository.deleteAll()
        workspaceRepository.deleteAll()
    }

    @Test
    fun `given a Workspace name and a search content, when a GET REST request is performed to the search endpoint and one Card matches, then the response HTTP Status is 200 OK and the response body contains the retrieved Card`() {
        val workspace = Workspace("workspaceTest")
        val workspaceEntity = WorkspaceEntity.create(workspace)
        workspaceRepository.save(workspaceEntity)
        val matchingCardId = CardId("9e493dc0-ef75-403f-b5d6-ed510634f8a6")
        val matchingCardEntity = CardEntity(matchingCardId.id, workspace.name, "question", "response content")
        cardRepository.save(matchingCardEntity)
        val nonMatchingCardId = CardId("a1900ca7-dc58-4360-b41c-537d933bc9c1")
        val nonMatchingCardEntity = CardEntity(nonMatchingCardId.id, workspace.name, "new question", "new response")
        cardRepository.save(nonMatchingCardEntity)
        val searchPattern = SearchPattern("content")

        val responseEntity = testRestTemplate.getForEntity(
                URI("http://localhost:$port/api/workspaces/${workspace.name}/search?content=${searchPattern.content}"),
                List::class.java
        )

        val expectedCard = Card(matchingCardId.id, workspace.name, "question", "response content")
        val expectedCards = listOf(expectedCard)
        val expectedResponseEntity = ResponseEntity.status(HttpStatus.OK).body(expectedCards)
        assertEquals(expectedResponseEntity.statusCode, responseEntity.statusCode)
        val cards = responseEntity.body as List<*>
        assertTrue { cards.size == 1 }
    }
}
