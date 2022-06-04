package de.mcella.spring.learntool.export

import de.mcella.spring.learntool.card.CardService
import de.mcella.spring.learntool.card.CardSort
import de.mcella.spring.learntool.learn.LearnService
import de.mcella.spring.learntool.security.UserPrincipal
import de.mcella.spring.learntool.workspace.WorkspaceService
import de.mcella.spring.learntool.workspace.dto.WorkspaceRequest
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
class ExportService(
    private val workspaceService: WorkspaceService,
    private val cardService: CardService,
    private val learnService: LearnService
) {

    fun exportBackup(workspaceRequest: WorkspaceRequest, userPrincipal: UserPrincipal): File {
        val tempDirectory = Files.createTempDirectory("backup")
        try {
            val workspaceFile = exportWorkspaceBackup(workspaceRequest, tempDirectory, userPrincipal)
            val cardIdsMapping = mutableMapOf<String, Long>()
            val cardsFile = exportCardsBackup(workspaceRequest, tempDirectory, cardIdsMapping, userPrincipal)
            val learnCardsFile = exportLearnCardsBackup(workspaceRequest, tempDirectory, cardIdsMapping)
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
        } finally {
            tempDirectory.toFile().deleteRecursively()
        }
    }

    private fun exportWorkspaceBackup(workspaceRequest: WorkspaceRequest, tempDirectory: Path, userPrincipal: UserPrincipal): File {
        val workspace = workspaceService.get(workspaceRequest)
        workspaceService.verifyIfUserIsAuthorized(workspaceRequest, userPrincipal)
        val file = File(tempDirectory.toString(), "workspaces.csv")
        val writer = Files.newBufferedWriter(Paths.get(file.toURI()))
        val csvPrinter = CSVPrinter(writer, CSVFormat.RFC4180
                .withHeader("name"))
        csvPrinter.printRecord(workspace.name)
        csvPrinter.flush()
        csvPrinter.close()
        return file
    }

    private fun exportCardsBackup(workspaceRequest: WorkspaceRequest, tempDirectory: Path, cardIdsMapping: MutableMap<String, Long>, userPrincipal: UserPrincipal): File {
        val file = File(tempDirectory.toString(), "cards.csv")
        val writer = Files.newBufferedWriter(Paths.get(file.toURI()))
        var i: Long = 0
        val csvPrinter = CSVPrinter(writer, CSVFormat.RFC4180
                .withHeader("id", "question", "response").withEscape('"'))
        cardService.findByWorkspace(workspaceRequest, null, CardSort.asc, userPrincipal).stream()
                .forEach {
                    card -> run {
                        cardIdsMapping[card.id] = ++i
                        csvPrinter.printRecord(i, card.question, card.response)
                    }
                }
        csvPrinter.flush()
        csvPrinter.close()
        return file
    }

    private fun exportLearnCardsBackup(workspaceRequest: WorkspaceRequest, tempDirectory: Path, cardIdsMapping: Map<String, Long>): File {
        val file = File(tempDirectory.toString(), "learn_cards.csv")
        val writer = Files.newBufferedWriter(Paths.get(file.toURI()))
        val csvPrinter = CSVPrinter(writer, CSVFormat.RFC4180
                .withHeader("id", "last_review", "next_review", "repetitions", "ease_factor", "interval_days"))
        learnService.getLearnCardsByWorkspace(workspaceRequest).stream().forEach {
            learnCard -> run {
                val i = cardIdsMapping.get(learnCard.id)
                csvPrinter.printRecord(i, learnCard.lastReview, learnCard.nextReview, learnCard.repetitions, learnCard.easeFactor, learnCard.intervalDays)
            }
        }
        csvPrinter.flush()
        csvPrinter.close()
        return file
    }
}
