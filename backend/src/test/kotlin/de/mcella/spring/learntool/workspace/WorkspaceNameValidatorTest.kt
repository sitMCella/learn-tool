package de.mcella.spring.learntool.workspace

import de.mcella.spring.learntool.UnitTest
import de.mcella.spring.learntool.workspace.dto.WorkspaceCreateRequest
import de.mcella.spring.learntool.workspace.exceptions.InvalidWorkspaceIdException
import org.junit.Test
import org.junit.experimental.categories.Category

@Category(UnitTest::class)
class WorkspaceNameValidatorTest {

    @Test
    fun `given a Workspace name with lowercase letters, when validating the name, then do not throw any exception`() {
        val workspaceCreateRequest = WorkspaceCreateRequest("workspace")
        WorkspaceNameValidator.validate(workspaceCreateRequest)
    }

    @Test
    fun `given a Workspace name with uppercase letters, when validating the name, then do not throw any exception`() {
        val workspaceCreateRequest = WorkspaceCreateRequest("WORKSPACE")
        WorkspaceNameValidator.validate(workspaceCreateRequest)
    }

    @Test
    fun `given a Workspace name with numbers, when validating the name, then do not throw any exception`() {
        val workspaceCreateRequest = WorkspaceCreateRequest("123")
        WorkspaceNameValidator.validate(workspaceCreateRequest)
    }

    @Test
    fun `given a Workspace name with letters and numbers, when validating the name, then do not throw any exception`() {
        val workspaceCreateRequest = WorkspaceCreateRequest("Workspace1")
        WorkspaceNameValidator.validate(workspaceCreateRequest)
    }

    @Test
    fun `given a Workspace name with dash characters, when validating the name, then do not throw any exception`() {
        val workspaceCreateRequest = WorkspaceCreateRequest("Workspace-test-1")
        WorkspaceNameValidator.validate(workspaceCreateRequest)
    }

    @Test
    fun `given a Workspace name with space characters, when validating the name, then do not throw any exception`() {
        val workspaceCreateRequest = WorkspaceCreateRequest("Workspace test 1")
        WorkspaceNameValidator.validate(workspaceCreateRequest)
    }

    @Test
    fun `given a Workspace name with underscore characters, when validating the name, then do not throw any exception`() {
        val workspaceCreateRequest = WorkspaceCreateRequest("Workspace_test_1")
        WorkspaceNameValidator.validate(workspaceCreateRequest)
    }

    @Test(expected = InvalidWorkspaceIdException::class)
    fun `given a Workspace name with slash characters, when validating the name, then throw InvalidWorkspaceNameException exception`() {
        val workspaceCreateRequest = WorkspaceCreateRequest("Workspace/test/1")
        WorkspaceNameValidator.validate(workspaceCreateRequest)
    }

    @Test(expected = InvalidWorkspaceIdException::class)
    fun `given a Workspace name with backslash characters, when validating the name, then throw InvalidWorkspaceNameException exception`() {
        val workspaceCreateRequest = WorkspaceCreateRequest("Workspace\\test\\1")
        WorkspaceNameValidator.validate(workspaceCreateRequest)
    }
}
