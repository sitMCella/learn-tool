package de.mcella.spring.learntool.import

import de.mcella.spring.learntool.UnitTest
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@RunWith(SpringRunner::class)
@Category(UnitTest::class)
@WebMvcTest(ImportController::class)
@AutoConfigureWebClient
class ImportControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var importService: ImportService

    @Test
    fun `given a backup file, when sending a POST REST request to the import endpoint, then the importBackup method of ImportService is called`() {
        val backup = MockMultipartFile("backup", "backup.zip", "text/plain", null)

        mockMvc.perform(
                MockMvcRequestBuilders.multipart("/api/workspaces/import").file(backup)
        ).andExpect(MockMvcResultMatchers.status().isOk)

        Mockito.verify(importService).importBackup(backup)
    }
}
