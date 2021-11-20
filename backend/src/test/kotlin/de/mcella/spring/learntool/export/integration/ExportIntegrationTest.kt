package de.mcella.spring.learntool.export.integration

import de.mcella.spring.learntool.BackendApplication
import de.mcella.spring.learntool.IntegrationTest
import de.mcella.spring.learntool.card.storage.Card
import de.mcella.spring.learntool.card.storage.CardRepository
import de.mcella.spring.learntool.learn.algorithm.OutputValues
import de.mcella.spring.learntool.learn.storage.LearnCard
import de.mcella.spring.learntool.learn.storage.LearnCardRepository
import de.mcella.spring.learntool.workspace.storage.Workspace
import de.mcella.spring.learntool.workspace.storage.WorkspaceRepository
import java.net.URI
import java.time.Instant
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
import org.springframework.boot.test.web.client.getForEntity
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.core.io.Resource
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner
import org.testcontainers.containers.PostgreSQLContainer

@RunWith(SpringRunner::class)
@Category(IntegrationTest::class)
@SpringBootTest(classes = [BackendApplication::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = [ExportIntegrationTest.Companion.Initializer::class])
class ExportIntegrationTest {

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
    fun `given a Workspace name, when a GET REST request is performed to the export endpoint, then the backup file is created and the response body contains the file`() {
        val workspaceName = "workspaceTest"
        val workspace = Workspace(workspaceName)
        workspaceRepository.save(workspace)
        val cardId = "a1900ca7-dc58-4360-b41c-537d933bc9c1"
        val card = Card(cardId, workspaceName, "This is a question", "This is a response")
        cardRepository.save(card)
        val outputValues = OutputValues(0, 0, 1.3f)
        val review = Instant.ofEpochMilli(1637090403000)
        val learnCard = LearnCard.create(cardId, workspaceName, outputValues, review)
        learnCardRepository.save(learnCard)

        val responseEntity = testRestTemplate.getForEntity<Resource>(URI("http://localhost:$port/api/workspaces/$workspaceName/export"))

        // Store the zip file
        // val outputStream: OutputStream = FileOutputStream(File("src/test/resources/backup.zip"))
        // IOUtils.copy(responseEntity.body!!.inputStream, outputStream)

        val zipInputStream = ZipInputStream(responseEntity.body!!.inputStream)
        val files = zipInputStream.use { zipInputStream ->
            generateSequence { zipInputStream.nextEntry }
                    .filterNot { it.isDirectory }
                    .map {
                        UnzippedFile(
                                filename = it.name,
                                content = zipInputStream.readAllBytes()
                        )
                    }.toList()
        }
        val expectedFileNames = listOf("/workspaces.csv", "/cards.csv", "/learn_cards.csv")
        assertEquals(expectedFileNames.count(), files.count())
        for (file in files) {
            assertTrue(expectedFileNames.contains(file.filename))
            when (file.filename) {
                "/workspaces.csv" -> {
                    assertEquals("\"name\"" + System.lineSeparator() + "\"workspaceTest\"" + System.lineSeparator(), String(file.content))
                }
                "/cards.csv" -> {
                    assertEquals("\"id\"\t\"workspace_name\"\t\"question\"\t\"response\"" + System.lineSeparator() + "\"" + cardId + "\"" + "\t\"workspaceTest\"\t\"This is a question\"\t\"This is a response\"" + System.lineSeparator(), String(file.content))
                }
                "/learn_cards.csv" -> {
                    assertEquals("\"id\"\t\"workspace_name\"\t\"last_review\"\t\"next_review\"\t\"repetitions\"\t\"ease_factor\"\t\"interval_days\"" + System.lineSeparator() + "\"" + cardId + "\"" + "\t\"workspaceTest\"\t\"2021-11-16T19:20:03Z\"\t\"2021-11-16T19:20:03Z\"\t\"0\"\t\"1.3\"\t\"0\"" + System.lineSeparator(), String(file.content))
                }
            }
        }
    }
}
