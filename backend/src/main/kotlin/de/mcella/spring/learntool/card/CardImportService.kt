package de.mcella.spring.learntool.card

import de.mcella.spring.learntool.card.dto.CardContent
import de.mcella.spring.learntool.card.exceptions.CsvContentParseException
import de.mcella.spring.learntool.card.storage.QUESTION
import de.mcella.spring.learntool.card.storage.RESPONSE
import de.mcella.spring.learntool.workspace.dto.WorkspaceRequest
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.IllegalArgumentException
import org.apache.commons.csv.CSVFormat
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class CardImportService(private val cardService: CardService) {

    fun createMany(workspace: WorkspaceRequest, content: InputStream) {
        val csvRecords by lazy {
            try {
                CSVFormat.DEFAULT
                    .withHeader(QUESTION, RESPONSE)
                    .withFirstRecordAsHeader()
                    .parse(InputStreamReader(content)).records
            } catch (e: IOException) {
                logger.error("Cannot parse CSV content.", e)
                throw CsvContentParseException()
            }
        }
        for (csvRecord in csvRecords) {
            try {
                cardService.create(workspace, CardContent(csvRecord.get(QUESTION), csvRecord.get(RESPONSE)))
            } catch (e: Exception) {
                when (e) {
                    is IllegalArgumentException,
                    is ArrayIndexOutOfBoundsException -> {
                        logger.error("Cannot parse CSV content.", e)
                        throw CsvContentParseException(e.message)
                    }
                    else -> throw e
                }
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }
}
