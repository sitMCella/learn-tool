package de.mcella.spring.learntool.search

import de.mcella.spring.learntool.UnitTest
import de.mcella.spring.learntool.workspace.Workspace
import de.mcella.spring.learntool.workspace.WorkspaceService
import de.mcella.spring.learntool.workspace.exceptions.WorkspaceNotExistsException
import javax.persistence.EntityManager
import org.junit.Test
import org.junit.experimental.categories.Category
import org.mockito.Mockito

@Category(UnitTest::class)
class CardSearchServiceTest {

    private val entityManager = Mockito.mock(EntityManager::class.java)

    private val workspaceService = Mockito.mock(WorkspaceService::class.java)

    private val cardSearchService = CardSearchService(entityManager, workspaceService)

    @Test(expected = WorkspaceNotExistsException::class)
    fun `given a non existent Workspace name and a SearchPattern content, when searching the Cards, then throw WorkspaceNotExistsException`() {
        val workspace = Workspace("workspaceTest")
        val searchPattern = SearchPattern("content")
        Mockito.`when`(workspaceService.exists(workspace)).thenReturn(false)

        cardSearchService.searchCards(workspace, searchPattern)
    }
}
