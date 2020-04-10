package de.mcella.spring.learntool.learn.integration

import de.mcella.spring.learntool.BackendApplication
import de.mcella.spring.learntool.card.CardService
import de.mcella.spring.learntool.card.storage.Card
import de.mcella.spring.learntool.card.storage.CardRepository
import de.mcella.spring.learntool.workspace.storage.Workspace
import de.mcella.spring.learntool.workspace.storage.WorkspaceRepository
import java.net.URI
import kotlin.test.assertEquals
import org.junit.Before
import org.junit.ClassRule
import org.junit.Test
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

@RunWith(SpringRunner::class)
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
    lateinit var cardService: CardService

    @Before
    fun setUp() {
        cardRepository.deleteAll()
        workspaceRepository.deleteAll()
    }

    @Test
    fun `given a Workspace name, when a GET request is performed to the learn endpoint, then a Card is returned`() {
        val workspaceName = "workspaceTest"
        val workspace = Workspace(workspaceName)
        workspaceRepository.save(workspace)
        val card = Card("9e493dc0-ef75-403f-b5d6-ed510634f8a6", workspaceName, "question", "response")
        cardRepository.save(card)

        val responseEntity = testRestTemplate.getForEntity(
            URI("http://localhost:$port/workspaces/$workspaceName/learn"),
            Card::class.java
        )

        val expectedResponseEntity = ResponseEntity.status(HttpStatus.OK).body(card)
        assertEquals(expectedResponseEntity.statusCode, responseEntity.statusCode)
        assertEquals(expectedResponseEntity.body, responseEntity.body)
    }
}
