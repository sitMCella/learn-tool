package de.mcella.spring.learntool.workspace.integration

import de.mcella.spring.learntool.BackendApplication
import de.mcella.spring.learntool.workspace.storage.Workspace
import de.mcella.spring.learntool.workspace.storage.WorkspaceRepository
import java.net.URI
import kotlin.test.assertTrue
import org.junit.ClassRule
import org.junit.Test
import org.junit.runner.RunWith
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.http.HttpEntity
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner
import org.testcontainers.containers.PostgreSQLContainer

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [BackendApplication::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = [WorkspaceIntegrationTest.Companion.Initializer::class])
class WorkspaceIntegrationTest {

    companion object {
        @ClassRule
        @JvmField
        val postgresql = PostgreSQLContainer<Nothing>()

        private val logger = LoggerFactory.getLogger(WorkspaceIntegrationTest::class.java)

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

    @Test
    fun `given a workspace name, when the create endpoint is called, then the workspace is created`() {
        val workspace = Workspace("workspace1")
        val request = HttpEntity(workspace)
        testRestTemplate.postForObject(URI("http://localhost:$port/workspaces"), request, String::class.java)
        val workspaces = workspaceRepository.findAll()
        assertTrue { workspaces.contains(workspace) }
    }
}
