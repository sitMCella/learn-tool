package de.mcella.spring.learntool.import

import de.mcella.spring.learntool.card.Card
import de.mcella.spring.learntool.card.CardId
import de.mcella.spring.learntool.card.CardService
import de.mcella.spring.learntool.learn.LearnCard
import de.mcella.spring.learntool.learn.LearnService
import de.mcella.spring.learntool.workspace.Workspace
import de.mcella.spring.learntool.workspace.WorkspaceService
import de.mcella.spring.learntool.workspace.exceptions.WorkspaceAlreadyExistsException
import java.io.BufferedInputStream
import java.io.StringReader
import java.time.Instant
import java.util.zip.ZipInputStream
import org.apache.commons.csv.CSVFormat
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class ImportService(private val workspaceService: WorkspaceService, private val cardService: CardService, private val learnService: LearnService) {

    fun importBackup(backup: MultipartFile) {

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
                                importWorkspaceBackup(unzippedFile)
                            }
                            "/cards.csv" -> {
                                importCardsBackup(unzippedFile)
                            }
                            "/learn_cards.csv" -> {
                                importLearnCardsBackup(unzippedFile)
                            }
                        }
                    }
        }
    }

    private fun importWorkspaceBackup(unzippedFile: UnzippedFile) {
        CSVFormat.DEFAULT
                .withHeader("name")
                .withIgnoreEmptyLines()
                .withFirstRecordAsHeader().withQuote(null)
                .parse(StringReader(String(unzippedFile.content)))
                .forEach { record ->
                    val workspace = Workspace(record.get("name"))
                    if (workspaceService.exists(workspace)) {
                        throw WorkspaceAlreadyExistsException(workspace)
                    }
                    workspaceService.create(workspace)
                }
    }

    private fun importCardsBackup(unzippedFile: UnzippedFile) {
        CSVFormat.DEFAULT
                .withHeader("id", "workspace_name", "question", "response")
                .withIgnoreEmptyLines()
                .withFirstRecordAsHeader().withQuote('"').withEscape('\\')
                .parse(StringReader(String(unzippedFile.content)))
                .forEach { record ->
                    val cardId = CardId(record.get("id"))
                    val card = Card(cardId.id, record.get("workspace_name"), record.get("question"), record.get("response"))
                    cardService.create(card)
                }
    }

    private fun importLearnCardsBackup(unzippedFile: UnzippedFile) {
        CSVFormat.DEFAULT
                .withHeader("id", "workspace_name", "last_review", "next_review", "repetitions", "ease_factor", "interval_days")
                .withIgnoreEmptyLines()
                .withFirstRecordAsHeader().withQuote(null)
                .parse(StringReader(String(unzippedFile.content)))
                .forEach { record ->
                    val cardId = CardId(record.get("id"))
                    val lastReview = Instant.parse(record.get("last_review"))
                    val nextReview = Instant.parse(record.get("next_review"))
                    val repetitions = record.get("repetitions").toInt()
                    val easeFactor = record.get("ease_factor").toFloat()
                    val intervalDays = record.get("interval_days").toInt()
                    val learnCard = LearnCard(cardId.id, record.get("workspace_name"), lastReview, nextReview, repetitions, easeFactor, intervalDays)
                    learnService.create(learnCard)
                }
    }
}
