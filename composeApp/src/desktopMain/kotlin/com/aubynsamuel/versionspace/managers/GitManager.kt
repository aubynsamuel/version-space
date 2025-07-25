package com.aubynsamuel.versionspace.managers

import com.aubynsamuel.versionspace.data.GitCommit
import com.aubynsamuel.versionspace.data.GitStatus
import java.io.File

/**
 * Manages Git operations for a given working directory.
 * @param workingDirectory The directory where Git commands will be executed.
 */
class GitManager(private val workingDirectory: File) {

    /**
     * Executes a Git command with the given arguments.
     * @param command The Git command and its arguments.
     * @return A Result containing the command's output on success, or an Exception on failure.
     */
    fun executeGitCommand(vararg command: String): Result<String> {
        return try {
            val process = ProcessBuilder(*command)
                .directory(workingDirectory)
                .redirectErrorStream(true)
                .start()

            val output = process.inputStream.bufferedReader().readText()
            process.waitFor()

            if (process.exitValue() == 0) {
                Result.success(output)
            } else {
                Result.failure(Exception("Git command failed: $output"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Gets the status of the Git repository, including staged, modified, and untracked files.
     * @return A GitStatus object representing the repository's state.
     */
    fun getStatus(): GitStatus {
        val result = executeGitCommand("git", "status", "--porcelain")
        return if (result.isSuccess) {
            val lines = result.getOrNull()?.lines()?.filter { it.isNotBlank() } ?: emptyList()
            val staged = mutableListOf<String>()
            val modified = mutableListOf<String>()
            val untracked = mutableListOf<String>()

            lines.forEach { line ->
                val status = line.take(2)
                val filename = line.drop(3)
                when {
                    status.startsWith("A") || status.startsWith("M") && status[1] != 'M' -> staged.add(
                        filename
                    )

                    status.endsWith("M") || status.startsWith(" M") -> modified.add(filename)
                    status.startsWith("??") -> untracked.add(filename)
                }
            }

            val branch = getCurrentBranch()
            GitStatus(staged, modified, untracked, branch)
        } else {
            GitStatus()
        }
    }

    /**
     * Gets the name of the current active branch.
     * @return The current branch name, or "main" if it cannot be determined.
     */
    fun getCurrentBranch(): String {
        val result = executeGitCommand("git", "branch", "--show-current")
        return result.getOrNull()?.trim() ?: "main"
    }

    /**
     * Stages a file for commit.
     * @param filename The name of the file to add.
     * @return A Result indicating the success or failure of the operation.
     */
    fun addFile(filename: String): Result<String> {
        return executeGitCommand("git", "add", filename)
    }

    /**
     * Commits staged changes with a given message.
     * @param message The commit message.
     * @return A Result indicating the success or failure of the operation.
     */
    fun commit(message: String): Result<String> {
        return executeGitCommand("git", "commit", "-m", message)
    }

    /**
     * Creates a new branch and switches to it.
     * @param branchName The name of the new branch.
     * @return A Result indicating the success or failure of the operation.
     */
    fun createBranch(branchName: String): Result<String> {
        return executeGitCommand("git", "checkout", "-b", branchName)
    }

    /**
     * Switches to an existing branch.
     * @param branchName The name of the branch to switch to.
     * @return A Result indicating the success or failure of the operation.
     */
    fun switchBranch(branchName: String): Result<String> {
        return executeGitCommand("git", "checkout", branchName)
    }

    /**
     * Gets a list of all local branches.
     * @return A list of branch names.
     */
    fun getBranches(): List<String> {
        val result = executeGitCommand("git", "branch")
        return if (result.isSuccess) {
            result.getOrNull()?.lines()
                ?.map { it.trim().removePrefix("* ").trim() }
                ?.filter { it.isNotBlank() } ?: emptyList()
        } else {
            emptyList()
        }
    }

    /**
     * Gets the recent commit history.
     * @param limit The maximum number of commits to retrieve.
     * @return A list of GitCommit objects.
     */
    fun getCommitHistory(limit: Int = 10): List<GitCommit> {
        val result = executeGitCommand(
            "git",
            "log",
            "--oneline",
            "-n",
            limit.toString(),
            "--pretty=format:%H|%s|%an|%ad",
            "--date=short"
        )
        return if (result.isSuccess) {
            result.getOrNull()?.lines()
                ?.filter { it.isNotBlank() }
                ?.map { line ->
                    val parts = line.split("|")
                    if (parts.size >= 4) {
                        GitCommit(parts[0], parts[1], parts[2], parts[3])
                    } else {
                        GitCommit("", line, "", "")
                    }
                } ?: emptyList()
        } else {
            emptyList()
        }
    }

    /**
     * Initializes a new Git repository in the working directory.
     * @return A Result indicating the success or failure of the operation.
     */
    fun initRepository(): Result<String> {
        return executeGitCommand("git", "init")
    }

    /**
     * Pushes committed changes to the remote repository.
     * @return A Result indicating the success or failure of the operation.
     */
    fun push(): Result<String> {
        return executeGitCommand("git", "push")
    }

    /**
     * Pushes committed changes to a specific branch on the remote repository.
     * @param branch The name of the branch to push to.
     * @return A Result indicating the success or failure of the operation.
     */
    fun push(branch: String): Result<String> {
        return executeGitCommand("git", "push", "origin", branch)
    }

    /**
     * Pulls changes from the remote repository.
     * @return A Result indicating the success or failure of the operation.
     */
    fun pull(): Result<String> {
        return executeGitCommand("git", "pull")
    }

    /**
     * Pulls changes from a specific remote and branch.
     * @param remote The name of the remote repository (defaults to "origin").
     * @param branch The name of the branch to pull from (optional).
     * @return A Result indicating the success or failure of the operation.
     */
    fun pull(remote: String = "origin", branch: String? = null): Result<String> {
        return if (branch != null) {
            executeGitCommand("git", "pull", remote, branch)
        } else {
            executeGitCommand("git", "pull", remote)
        }
    }

    /**
     * Fetches changes from the remote repository.
     * @return A Result indicating the success or failure of the operation.
     */
    fun fetch(): Result<String> {
        return executeGitCommand("git", "fetch")
    }

    /**
     * Fetches changes from a specific remote repository.
     * @param remote The name of the remote to fetch from.
     * @return A Result indicating the success or failure of the operation.
     */
    fun fetch(remote: String): Result<String> {
        return executeGitCommand("git", "fetch", remote)
    }

    /**
     * Gets the diff of all changes in the working directory.
     * @return A Result containing the diff output.
     */
    fun getDiff(): Result<String> {
        return executeGitCommand("git", "diff")
    }

    /**
     * Gets the diff for a specific file.
     * @param filename The name of the file.
     * @return A Result containing the diff output.
     */
    fun getDiff(filename: String): Result<String> {
        return executeGitCommand("git", "diff", filename)
    }

    /**
     * Gets the diff between two commits.
     * @param commit1 The first commit hash.
     * @param commit2 The second commit hash.
     * @return A Result containing the diff output.
     */
    fun getDiff(commit1: String, commit2: String): Result<String> {
        return executeGitCommand("git", "diff", commit1, commit2)
    }

    /**
     * Gets the diff of staged changes.
     * @return A Result containing the diff output.
     */
    fun getDiffStaged(): Result<String> {
        return executeGitCommand("git", "diff", "--staged")
    }

    /**
     * Gets the diff of staged changes for a specific file.
     * @param filename The name of the file.
     * @return A Result containing the diff output.
     */
    fun getDiffStaged(filename: String): Result<String> {
        return executeGitCommand("git", "diff", "--staged", filename)
    }

    /**
     * Adds all modified and untracked files to the staging area.
     * @return A Result indicating the success or failure of the operation.
     */
    fun addAll(): Result<String> {
        return executeGitCommand("git", "add", ".")
    }

    /**
     * Removes a file from the staging area.
     * @param filename The name of the file to unstage.
     * @return A Result indicating the success or failure of the operation.
     */
    fun unstageFile(filename: String): Result<String> {
        return executeGitCommand("git", "reset", "HEAD", filename)
    }

    /**
     * Discards changes in the working directory for a specific file.
     * @param filename The name of the file to discard changes for.
     * @return A Result indicating the success or failure of the operation.
     */
    fun discardChanges(filename: String): Result<String> {
        return executeGitCommand("git", "checkout", "--", filename)
    }

    /**
     * Gets a list of remote repositories.
     * @return A list of remote names.
     */
    fun getRemotes(): List<String> {
        val result = executeGitCommand("git", "remote", "-v")
        return if (result.isSuccess) {
            result.getOrNull()?.lines()
                ?.filter { it.isNotBlank() }
                ?.map { it.split("	")[0] }
                ?.distinct() ?: emptyList()
        } else {
            emptyList()
        }
    }

    /**
     * Adds a new remote repository.
     * @param name The name of the remote.
     * @param url The URL of the remote.
     * @return A Result indicating the success or failure of the operation.
     */
    fun addRemote(name: String, url: String): Result<String> {
        return executeGitCommand("git", "remote", "add", name, url)
    }

    /**
     * Removes a remote repository.
     * @param name The name of the remote to remove.
     * @return A Result indicating the success or failure of the operation.
     */
    fun removeRemote(name: String): Result<String> {
        return executeGitCommand("git", "remote", "remove", name)
    }

    /**
     * Gets the URL of a remote repository.
     * @param remote The name of the remote (defaults to "origin").
     * @return A Result containing the remote URL.
     */
    fun getRemoteUrl(remote: String = "origin"): Result<String> {
        return executeGitCommand("git", "remote", "get-url", remote)
    }

    /**
     * Checks if the current working directory is a Git repository.
     * @return True if it is a Git repository, false otherwise.
     */
    fun isGitRepository(): Boolean {
        val result = executeGitCommand("git", "rev-parse", "--git-dir")
        return result.isSuccess
    }

    /**
     * Gets the root directory of the Git repository.
     * @return A Result containing the absolute path to the repository root.
     */
    fun getRepositoryRoot(): Result<String> {
        return executeGitCommand("git", "rev-parse", "--show-toplevel")
    }

    /**
     * Gets the full hash of the current commit (HEAD).
     * @return A Result containing the commit hash.
     */
    fun getCurrentCommitHash(): Result<String> {
        return executeGitCommand("git", "rev-parse", "HEAD")
    }

    /**
     * Gets the short hash of the current commit (HEAD).
     * @return A Result containing the short commit hash.
     */
    fun getCurrentCommitHashShort(): Result<String> {
        return executeGitCommand("git", "rev-parse", "--short", "HEAD")
    }

    /**
     * Deletes a local branch.
     * @param branchName The name of the branch to delete.
     * @return A Result indicating the success or failure of the operation.
     */
    fun deleteBranch(branchName: String): Result<String> {
        return executeGitCommand("git", "branch", "-d", branchName)
    }

    /**
     * Forcefully deletes a local branch.
     * @param branchName The name of the branch to delete.
     * @return A Result indicating the success or failure of the operation.
     */
    fun forceDeleteBranch(branchName: String): Result<String> {
        return executeGitCommand("git", "branch", "-D", branchName)
    }

    /**
     * Merges a branch into the current branch.
     * @param branchName The name of the branch to merge.
     * @return A Result indicating the success or failure of the operation.
     */
    fun mergeBranch(branchName: String): Result<String> {
        return executeGitCommand("git", "merge", branchName)
    }

    /**
     * Rebases the current branch onto another branch.
     * @param branchName The branch to rebase onto.
     * @return A Result indicating the success or failure of the operation.
     */
    fun rebase(branchName: String): Result<String> {
        return executeGitCommand("git", "rebase", branchName)
    }

    /**
     * Gets the Git log with a custom format.
     * @param limit The maximum number of log entries to return.
     * @param format The format string for the log output.
     * @return A Result containing the formatted log.
     */
    fun getLog(limit: Int = 10, format: String = "--oneline"): Result<String> {
        return executeGitCommand("git", "log", format, "-n", limit.toString())
    }

    /**
     * Stashes the current changes in the working directory.
     * @param message An optional message for the stash.
     * @return A Result indicating the success or failure of the operation.
     */
    fun stash(message: String? = null): Result<String> {
        return if (message != null) {
            executeGitCommand("git", "stash", "push", "-m", message)
        } else {
            executeGitCommand("git", "stash")
        }
    }

    /**
     * Applies the most recent stash and removes it from the stash list.
     * @return A Result indicating the success or failure of the operation.
     */
    fun stashPop(): Result<String> {
        return executeGitCommand("git", "stash", "pop")
    }

    /**
     * Lists all stashes.
     * @return A Result containing the list of stashes.
     */
    fun stashList(): Result<String> {
        return executeGitCommand("git", "stash", "list")
    }

    /**
     * Applies a specific stash from the stash list.
     * @param stashId The identifier of the stash to apply (e.g., "stash@{0}").
     * @return A Result indicating the success or failure of the operation.
     */
    fun stashApply(stashId: String): Result<String> {
        return executeGitCommand("git", "stash", "apply", stashId)
    }

    /**
     * Drops a specific stash from the stash list.
     * @param stashId The identifier of the stash to drop (e.g., "stash@{0}").
     * @return A Result indicating the success or failure of the operation.
     */
    fun stashDrop(stashId: String): Result<String> {
        return executeGitCommand("git", "stash", "drop", stashId)
    }
}

