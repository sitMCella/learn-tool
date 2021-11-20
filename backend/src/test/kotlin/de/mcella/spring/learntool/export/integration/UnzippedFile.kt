package de.mcella.spring.learntool.export.integration

import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

data class UnzippedFile(
    @field:NotNull @field:NotEmpty val filename: String = "",
    @field:NotNull @field:NotEmpty val content: ByteArray
)
