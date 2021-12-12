package de.mcella.spring.learntool.export.integration

import de.mcella.spring.learntool.BackendApplication
import de.mcella.spring.learntool.IntegrationTest
import de.mcella.spring.learntool.card.dto.CardId
import de.mcella.spring.learntool.card.storage.CardEntity
import de.mcella.spring.learntool.card.storage.CardRepository
import de.mcella.spring.learntool.learn.algorithm.OutputValues
import de.mcella.spring.learntool.learn.storage.LearnCardEntity
import de.mcella.spring.learntool.learn.storage.LearnCardRepository
import de.mcella.spring.learntool.workspace.dto.Workspace
import de.mcella.spring.learntool.workspace.storage.WorkspaceEntity
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
import org.testcontainers.utility.DockerImageName

@RunWith(SpringRunner::class)
@Category(IntegrationTest::class)
@SpringBootTest(classes = [BackendApplication::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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
        val workspace = Workspace("workspaceTest")
        val workspaceEntity = WorkspaceEntity(workspace.name)
        workspaceRepository.save(workspaceEntity)
        val cardId = CardId("a1900ca7-dc58-4360-b41c-537d933bc9c1")
        val cardEntity = CardEntity(cardId.id, workspace.name, "This is a \"question\"", "This, is a response")
        cardRepository.save(cardEntity)
        val outputValues = OutputValues(0, 0, 1.3f)
        val review = Instant.ofEpochMilli(1637090403000)
        val learnCard = LearnCardEntity.create(cardId, workspace, outputValues, review)
        learnCardRepository.save(learnCard)

        val responseEntity = testRestTemplate.getForEntity<Resource>(URI("http://localhost:$port/api/workspaces/${workspace.name}/export"))

        // Store the zip file
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
                    assertEquals("name\r\nworkspaceTest\r\n", String(file.content))
                }
                "/cards.csv" -> {
                    assertEquals("id,workspace_name,question,response\r\n" + cardId.id + ",workspaceTest,\"This is a \"\"question\"\"\",\"This, is a response\"\r\n", String(file.content))
                }
                "/learn_cards.csv" -> {
                    assertEquals("id,workspace_name,last_review,next_review,repetitions,ease_factor,interval_days\r\n" + cardId.id + ",workspaceTest,2021-11-16T19:20:03Z,2021-11-16T19:20:03Z,0,1.3,0\r\n", String(file.content))
                }
            }
        }
    }
}
