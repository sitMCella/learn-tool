package de.mcella.spring.learntool.learn

import de.mcella.spring.learntool.card.storage.Card
import de.mcella.spring.learntool.learn.storage.LearnCard
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.OK
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/workspaces/{workspaceName}/learn")
class LearnController(private val learnService: LearnService) {

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(CREATED)
    fun create(
        @PathVariable(value = "workspaceName") workspaceName: String,
        @RequestBody learnCardParameters: LearnCardParameters
    ): LearnCard {
        return learnService.create(workspaceName, learnCardParameters)
    }

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(OK)
    fun learn(@PathVariable(value = "workspaceName") workspaceName: String): Card = learnService.getCard(workspaceName)

    @PutMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(OK)
    fun evaluate(
        @PathVariable(value = "workspaceName") workspaceName: String,
        @RequestBody evaluationParameters: EvaluationParameters
    ): LearnCard {
        return learnService.evaluateCard(workspaceName, evaluationParameters)
    }

    @DeleteMapping
    @ResponseStatus(OK)
    fun delete(
        @PathVariable(value = "workspaceName") workspaceName: String,
        @RequestBody learnCardParameters: LearnCardParameters
    ) {
        learnService.delete(workspaceName, learnCardParameters)
    }
}
