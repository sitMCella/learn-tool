package de.mcella.spring.learntool.learn

import de.mcella.spring.learntool.card.dto.Card
import de.mcella.spring.learntool.card.dto.CardId
import de.mcella.spring.learntool.learn.dto.EvaluationParameters
import de.mcella.spring.learntool.learn.dto.LearnCard
import de.mcella.spring.learntool.security.UserPrincipal
import de.mcella.spring.learntool.workspace.dto.WorkspaceRequest
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.OK
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
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
@RequestMapping("/api/workspaces/{workspaceId}/learn")
class LearnController(private val learnService: LearnService) {

    @PostMapping("/{cardId}", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasRole('USER')")
    @ResponseStatus(CREATED)
    fun create(
        @PathVariable(value = "workspaceId") workspaceId: String,
        @PathVariable(value = "cardId") cardId: String,
        @AuthenticationPrincipal user: UserPrincipal
    ): LearnCard {
        return learnService.create(WorkspaceRequest(workspaceId), CardId(cardId), user)
    }

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasRole('USER')")
    @ResponseStatus(OK)
    fun learn(
        @PathVariable(value = "workspaceId") workspaceId: String,
        @AuthenticationPrincipal user: UserPrincipal
    ): Card = learnService.getCard(WorkspaceRequest(workspaceId), user)

    @PutMapping("/{cardId}", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasRole('USER')")
    @ResponseStatus(OK)
    fun evaluate(
        @PathVariable(value = "workspaceId") workspaceId: String,
        @PathVariable(value = "cardId") cardId: String,
        @RequestBody evaluationParameters: EvaluationParameters,
        @AuthenticationPrincipal user: UserPrincipal
    ): LearnCard {
        return learnService.evaluateCard(WorkspaceRequest(workspaceId), CardId(cardId), evaluationParameters, user)
    }

    @DeleteMapping("/{cardId}")
    @PreAuthorize("hasRole('USER')")
    @ResponseStatus(OK)
    fun delete(
        @PathVariable(value = "workspaceId") workspaceId: String,
        @PathVariable(value = "cardId") cardId: String,
        @AuthenticationPrincipal user: UserPrincipal
    ) {
        learnService.delete(WorkspaceRequest(workspaceId), CardId(cardId), user)
    }
}
