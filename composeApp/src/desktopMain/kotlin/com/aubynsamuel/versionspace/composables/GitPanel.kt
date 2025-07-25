package com.aubynsamuel.versionspace.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aubynsamuel.versionspace.data.GitCommit
import com.aubynsamuel.versionspace.data.GitStatus

@Composable
fun GitPanel(
    gitStatus: GitStatus,
    branches: List<String>,
    commits: List<GitCommit>,
    commitMessage: String,
    onCommitMessageChange: (String) -> Unit,
    newBranchName: String,
    onNewBranchNameChange: (String) -> Unit,
    showCreateBranch: Boolean,
    onShowCreateBranchChange: (Boolean) -> Unit,
    gitOutput: String,
    onGitCommand: (List<String>) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Git status
        item {
            GitStatusCard(gitStatus, onGitCommand)
        }

        // Commit section
        item {
            CommitSection(
                commitMessage = commitMessage,
                onCommitMessageChange = onCommitMessageChange,
                hasStaged = gitStatus.staged.isNotEmpty(),
                onCommit = { onGitCommand(listOf("commit")) }
            )
        }

        // Branch section
        item {
            BranchSection(
                currentBranch = gitStatus.currentBranch,
                branches = branches,
                newBranchName = newBranchName,
                onNewBranchNameChange = onNewBranchNameChange,
                showCreateBranch = showCreateBranch,
                onShowCreateBranchChange = onShowCreateBranchChange,
                onSwitchBranch = { branch -> onGitCommand(listOf("checkout", branch)) },
                onCreateBranch = { onGitCommand(listOf("branch")) }
            )
        }

        // Recent commits
        item {
            CommitHistoryCard(commits)
        }

        // Git output
        if (gitOutput.isNotBlank()) {
            item {
                GitOutputCard(gitOutput)
            }
        }

        // Git init button
        item {
            Button(
                onClick = { onGitCommand(listOf("init")) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Initialize Git Repository")
            }
        }
    }
}