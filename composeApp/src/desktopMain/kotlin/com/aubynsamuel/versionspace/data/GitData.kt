package com.aubynsamuel.versionspace.data

/**
 * Represents the status of a Git repository.
 * @param staged A list of files that are staged for commit.
 * @param modified A list of files that have been modified but not staged.
 * @param untracked A list of files that are not tracked by Git.
 * @param currentBranch The name of the current active branch.
 */
data class GitStatus(
    val staged: List<String> = emptyList(),
    val modified: List<String> = emptyList(),
    val untracked: List<String> = emptyList(),
    val currentBranch: String = "main",
)

/**
 * Represents a single commit in a Git repository.
 * @param hash The commit hash.
 * @param message The commit message.
 * @param author The author of the commit.
 * @param date The date of the commit.
 */
data class GitCommit(
    val hash: String,
    val message: String,
    val author: String,
    val date: String,
)