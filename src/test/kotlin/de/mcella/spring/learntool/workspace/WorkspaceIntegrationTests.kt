package de.mcella.spring.learntool.workspace

import org.junit.runner.RunWith
import de.mcella.spring.learntool.BackendApplication
import org.junit.ClassRule
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner
import org.testcontainers.containers.PostgreSQLContainer

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [BackendApplication::class], webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ContextConfiguration(initializers = [WorkspaceIntegrationTests.Companion.Initializer::class])
class WorkspaceIntegrationTests {

    companion object {
        @ClassRule
        @JvmField
        val mssql = PostgreSQLContainer<Nothing>()
    }
}
