package de.mcella.spring.learntool.workspace

import de.mcella.spring.learntool.UnitTest
import de.mcella.spring.learntool.workspace.exceptions.InvalidWorkspaceNameException
import org.junit.Test
import org.junit.experimental.categories.Category

@Category(UnitTest::class)
class WorkspaceNameValidatorTest {

    @Test
    fun `given a Workspace name with lowercase letters, when validating the name, then do not throw any exception`() {
        WorkspaceNameValidator.validate("workspace")
    }

    @Test
    fun `given a Workspace name with uppercase letters, when validating the name, then do not throw any exception`() {
        WorkspaceNameValidator.validate("WORKSPACE")
    }

    @Test
    fun `given a Workspace name with numbers, when validating the name, then do not throw any exception`() {
        WorkspaceNameValidator.validate("123")
    }

    @Test
    fun `given a Workspace name with letters and numbers, when validating the name, then do not throw any exception`() {
        WorkspaceNameValidator.validate("Workspace1")
    }

    @Test
    fun `given a Workspace name with dash characters, when validating the name, then do not throw any exception`() {
        WorkspaceNameValidator.validate("Workspace-test-1")
    }

    @Test
    fun `given a Workspace name with underscore characters, when validating the name, then do not throw any exception`() {
        WorkspaceNameValidator.validate("Workspace_test_1")
    }

    @Test(expected = InvalidWorkspaceNameException::class)
    fun `given a Workspace name with slash characters, when validating the name, then throw InvalidWorkspaceNameException exception`() {
        WorkspaceNameValidator.validate("Workspace/test/1")
    }

    @Test(expected = InvalidWorkspaceNameException::class)
    fun `given a Workspace name with backslash characters, when validating the name, then throw InvalidWorkspaceNameException exception`() {
        WorkspaceNameValidator.validate("Workspace\\test\\1")
    }
}
