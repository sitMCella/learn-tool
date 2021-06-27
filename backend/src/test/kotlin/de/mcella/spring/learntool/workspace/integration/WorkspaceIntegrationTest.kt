package de.mcella.spring.learntool.workspace.integration

import de.mcella.spring.learntool.BackendApplication
import de.mcella.spring.learntool.IntegrationTest
import de.mcella.spring.learntool.workspace.storage.Workspace
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
import org.springframework.http.HttpEntity
import org.springframework.http.HttpStatus
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner
import org.testcontainers.containers.PostgreSQLContainer

@RunWith(SpringRunner::class)
@Category(IntegrationTest::class)
@SpringBootTest(classes = [BackendApplication::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = [WorkspaceIntegrationTest.Companion.Initializer::class])
class WorkspaceIntegrationTest {

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
    lateinit var workspaceRepository: WorkspaceRepository

    @Before
    fun setUp() {
        workspaceRepository.deleteAll()
    }

    @Test
    fun `given a Workspace name, when a POST REST request is sent to the workspaces endpoint, then a Workspace is created and the http response body contains the Workspace`() {
        val workspace = Workspace("workspace1")
        val request = HttpEntity(workspace)

        val responseEntity = testRestTemplate.postForObject(URI("http://localhost:$port/api/workspaces"), request, Workspace::class.java)

        val workspaces = workspaceRepository.findAll()
        assertTrue { workspaces.contains(workspace) }
        assertEquals(workspace, responseEntity)
    }

    @Test
    fun `when a GET REST request is sent to the workspaces endpoint, then the http response body contains the list of Workspaces`() {
        val workspace1 = Workspace("workspace1")
        workspaceRepository.save(workspace1)
        val workspace2 = Workspace("workspace2")
        workspaceRepository.save(workspace2)

        val responseEntity = testRestTemplate.getForEntity(URI("http://localhost:$port/api/workspaces"), List::class.java)

        assertEquals(HttpStatus.OK, responseEntity.statusCode)
        val workspaces = responseEntity.body as List<*>
        assertTrue { workspaces.size == 2 }
    }
}
