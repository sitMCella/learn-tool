package de.mcella.spring.learntool.export

import de.mcella.spring.learntool.card.CardService
import de.mcella.spring.learntool.learn.LearnService
import de.mcella.spring.learntool.security.UserPrincipal
import de.mcella.spring.learntool.workspace.WorkspaceService
import de.mcella.spring.learntool.workspace.dto.WorkspaceRequest
import de.mcella.spring.learntool.workspace.exceptions.WorkspaceNotExistsException
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.springframework.stereotype.Service

@Service
class ExportService(private val workspaceService: WorkspaceService, private val cardService: CardService, private val learnService: LearnService) {

    fun exportBackup(workspaceRequest: WorkspaceRequest, userPrincipal: UserPrincipal): File {
        val tempDirectory = Files.createTempDirectory("backup")
        val workspaceFile = exportWorkspaceBackup(workspaceRequest, tempDirectory, userPrincipal)
        val cardsFile = exportCardsBackup(workspaceRequest, tempDirectory, userPrincipal)
        val learnCardsFile = exportLearnCardsBackup(workspaceRequest, tempDirectory)
        val files = arrayOf(workspaceFile.absolutePath, cardsFile.absolutePath, learnCardsFile.absolutePath)
        val backup: File = File.createTempFile("backup", ".zip")
        ZipOutputStream(BufferedOutputStream(FileOutputStream(backup))).use { out ->
            for (file in files) {
                FileInputStream(file).use { fi ->
                    BufferedInputStream(fi).use { origin ->
                        val entry = ZipEntry(file.substring(file.lastIndexOf("/")))
                        out.putNextEntry(entry)
                        origin.copyTo(out, 1024)
                    }
                }
            }
        }
        return backup
    }

    private fun exportWorkspaceBackup(workspaceRequest: WorkspaceRequest, tempDirectory: Path, userPrincipal: UserPrincipal): File {
        if (!workspaceService.exists(workspaceRequest)) {
            throw WorkspaceNotExistsException(workspaceRequest)
        }
        workspaceService.verifyIfUserIsAuthorized(workspaceRequest, userPrincipal)
        val file = File(tempDirectory.toString(), "workspaces.csv")
        val writer = Files.newBufferedWriter(Paths.get(file.toURI()))
        val csvPrinter = CSVPrinter(writer, CSVFormat.RFC4180
                .withHeader("name"))
        csvPrinter.printRecord(workspaceRequest.name)
        csvPrinter.flush()
        csvPrinter.close()
        return file
    }

    private fun exportCardsBackup(workspaceRequest: WorkspaceRequest, tempDirectory: Path, userPrincipal: UserPrincipal): File {
        val file = File(tempDirectory.toString(), "cards.csv")
        val writer = Files.newBufferedWriter(Paths.get(file.toURI()))
        val csvPrinter = CSVPrinter(writer, CSVFormat.RFC4180
                .withHeader("id", "workspace_name", "question", "response").withEscape('"'))
        cardService.findByWorkspace(workspaceRequest, null, userPrincipal).stream()
                .forEach { card -> csvPrinter.printRecord(card.id, card.workspaceName, card.question, card.response) }
        csvPrinter.flush()
        csvPrinter.close()
        return file
    }

    private fun exportLearnCardsBackup(workspaceRequest: WorkspaceRequest, tempDirectory: Path): File {
        val file = File(tempDirectory.toString(), "learn_cards.csv")
        val writer = Files.newBufferedWriter(Paths.get(file.toURI()))
        val csvPrinter = CSVPrinter(writer, CSVFormat.RFC4180
                .withHeader("id", "workspace_name", "last_review", "next_review", "repetitions", "ease_factor", "interval_days"))
        learnService.getLearnCardsByWorkspace(workspaceRequest).stream().forEach {
            learnCard -> csvPrinter.printRecord(learnCard.id, learnCard.workspaceName, learnCard.lastReview, learnCard.nextReview, learnCard.repetitions, learnCard.easeFactor, learnCard.intervalDays)
        }
        csvPrinter.flush()
        csvPrinter.close()
        return file
    }
}
