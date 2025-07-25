package com.aubynsamuel.versionspace.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aubynsamuel.versionspace.data.GitStatus

@Composable
fun GitStatusCard(
    gitStatus: GitStatus,
    onGitCommand: (List<String>) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Git Status",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            FileStatusSection(
                title = "Staged",
                files = gitStatus.staged,
                color = Color.Green,
                icon = Icons.Default.CheckCircle
            )

            FileStatusSection(
                title = "Modified",
                files = gitStatus.modified,
                color = Color.Yellow,
                icon = Icons.Default.Edit,
                onAddFile = { file -> onGitCommand(listOf("add", file)) }
            )

            FileStatusSection(
                title = "Untracked",
                files = gitStatus.untracked,
                color = Color.Red,
                icon = Icons.Default.Add,
                onAddFile = { file -> onGitCommand(listOf("add", file)) }
            )
        }
    }
}