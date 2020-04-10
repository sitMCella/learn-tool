package de.mcella.spring.learntool.learn

import de.mcella.spring.learntool.card.storage.Card
import org.springframework.http.HttpStatus.OK
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/workspaces/{workspaceName}/learn")
class LearnController(private val learnService: LearnService) {

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(OK)
    fun learn(@PathVariable(value = "workspaceName") workspaceName: String): Card {
        return learnService.getCard(workspaceName)
    }
}
