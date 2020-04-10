package de.mcella.spring.learntool.learn

import de.mcella.spring.learntool.card.CardService
import de.mcella.spring.learntool.card.storage.Card
import kotlin.test.assertEquals
import org.junit.Test
import org.mockito.Mockito

class LearnServiceTest {

    private val cardService = Mockito.mock(CardService::class.java)

    private val learnService = LearnService(cardService)

    @Test
    fun `given a Workspace name, when retrieving a Card from the Workspace, then call the method getFirstCardFromWorkspace of CardService and return the Card`() {
        val workspaceName = "workspaceTest"
        val expectedCard = Card("9e493dc0-ef75-403f-b5d6-ed510634f8a6", workspaceName, "question", "response")
        Mockito.`when`(cardService.getFirstCardFromWorkspace(workspaceName)).thenReturn(expectedCard)

        val card = learnService.getCard(workspaceName)

        assertEquals(expectedCard, card)
    }
}
