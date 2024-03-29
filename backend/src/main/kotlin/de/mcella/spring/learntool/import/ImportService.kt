package de.mcella.spring.learntool.import

import de.mcella.spring.learntool.card.CardIdGenerator
import de.mcella.spring.learntool.card.CardService
import de.mcella.spring.learntool.card.dto.Card
import de.mcella.spring.learntool.card.dto.CardId
import de.mcella.spring.learntool.learn.LearnService
import de.mcella.spring.learntool.learn.dto.LearnCard
import de.mcella.spring.learntool.security.UserPrincipal
import de.mcella.spring.learntool.workspace.WorkspaceService
import de.mcella.spring.learntool.workspace.dto.Workspace
import de.mcella.spring.learntool.workspace.dto.WorkspaceCreateRequest
import java.io.BufferedInputStream
import java.io.StringReader
import java.time.Instant
import java.util.zip.ZipInputStream
import org.apache.commons.csv.CSVFormat
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class ImportService(
    private val workspaceService: WorkspaceService,
    private val cardService: CardService,
    private val learnService: LearnService,
    private val cardIdGenerator: CardIdGenerator
) {

    fun importBackup(backup: MultipartFile, userPrincipal: UserPrincipal) {
        lateinit var workspace: Workspace
        ZipInputStream(BufferedInputStream(backup.inputStream)).use { zipInputStream ->
            generateSequence { zipInputStream.nextEntry }
                    .filterNot { it.isDirectory }
                    .map {
                        UnzippedFile(
                                filename = it.name,
                                content = zipInputStream.readAllBytes()
                        )
                    }
                    .forEach { unzippedFile ->
                        when (unzippedFile.filename) {
                            "/workspaces.csv" -> {
                                workspace = importWorkspaceBackup(unzippedFile, userPrincipal)
                            }
                        }
                    }
        }
        lateinit var cardIdsMapping: Map<Long, String>
        ZipInputStream(BufferedInputStream(backup.inputStream)).use { zipInputStream ->
            generateSequence { zipInputStream.nextEntry }
                    .filterNot { it.isDirectory }
                    .map {
                        UnzippedFile(
                                filename = it.name,
                                content = zipInputStream.readAllBytes()
                        )
                    }
                    .forEach { unzippedFile ->
                        when (unzippedFile.filename) {
                            "/cards.csv" -> {
                                cardIdsMapping = importCardsBackup(unzippedFile, workspace, userPrincipal)
                            }
                        }
                    }
        }
        ZipInputStream(BufferedInputStream(backup.inputStream)).use { zipInputStream ->
            generateSequence { zipInputStream.nextEntry }
                    .filterNot { it.isDirectory }
                    .map {
                        UnzippedFile(
                                filename = it.name,
                                content = zipInputStream.readAllBytes()
                        )
                    }
                    .forEach { unzippedFile ->
                        when (unzippedFile.filename) {
                            "/learn_cards.csv" -> {
                                importLearnCardsBackup(unzippedFile, workspace, cardIdsMapping)
                            }
                        }
                    }
        }
    }

    private fun importWorkspaceBackup(unzippedFile: UnzippedFile, userPrincipal: UserPrincipal): Workspace {
        lateinit var workspace: Workspace
        CSVFormat.DEFAULT
                .withHeader("name")
                .withIgnoreEmptyLines()
                .withFirstRecordAsHeader().withQuote(null)
                .parse(StringReader(String(unzippedFile.content)))
                .forEach { record ->
                    val workspaceCreateRequest = WorkspaceCreateRequest(record.get("name"))
                    workspace = workspaceService.create(workspaceCreateRequest, userPrincipal)
                }
        return workspace
    }

    private fun importCardsBackup(unzippedFile: UnzippedFile, workspace: Workspace, userPrincipal: UserPrincipal): Map<Long, String> {
        val cardIdsMapping = mutableMapOf<Long, String>()
        CSVFormat.DEFAULT
                .withHeader("id", "question", "response")
                .withIgnoreEmptyLines()
                .withFirstRecordAsHeader().withQuote('"').withEscape('\\')
                .parse(StringReader(String(unzippedFile.content)))
                .forEach { record ->
                    val cardId = cardIdGenerator.create()
                    val id = record.get("id")
                    cardIdsMapping[id.toLong()] = cardId.id
                    val card = Card(cardId.id, workspace.id, record.get("question"), record.get("response"))
                    cardService.create(card, userPrincipal)
                }
        return cardIdsMapping
    }

    private fun importLearnCardsBackup(unzippedFile: UnzippedFile, workspace: Workspace, cardIdsMapping: Map<Long, String>) {
        CSVFormat.DEFAULT
                .withHeader("id", "last_review", "next_review", "repetitions", "ease_factor", "interval_days")
                .withIgnoreEmptyLines()
                .withFirstRecordAsHeader().withQuote(null)
                .parse(StringReader(String(unzippedFile.content)))
                .forEach { record ->
                    val id = record.get("id")
                    val cardId = CardId(cardIdsMapping.getValue(id.toLong()))
                    val lastReview = Instant.parse(record.get("last_review"))
                    val nextReview = Instant.parse(record.get("next_review"))
                    val repetitions = record.get("repetitions").toInt()
                    val easeFactor = record.get("ease_factor").toFloat()
                    val intervalDays = record.get("interval_days").toInt()
                    val learnCard = LearnCard(cardId.id, workspace.id, lastReview, nextReview, repetitions, easeFactor, intervalDays)
                    learnService.create(learnCard)
                }
    }
}
