package com.aubynsamuel.versionspace.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BranchSection(
    currentBranch: String,
    branches: List<String>,
    newBranchName: String,
    onNewBranchNameChange: (String) -> Unit,
    showCreateBranch: Boolean,
    onShowCreateBranchChange: (Boolean) -> Unit,
    onSwitchBranch: (String) -> Unit,
    onCreateBranch: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Branches",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { onShowCreateBranchChange(!showCreateBranch) }
                ) {
                    Icon(
                        if (showCreateBranch) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = if (showCreateBranch) "Cancel" else "Create branch"
                    )
                }
            }

            Text(
                text = "Current: $currentBranch",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )

            if (showCreateBranch) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newBranchName,
                    onValueChange = onNewBranchNameChange,
                    label = { Text("New branch name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = onCreateBranch,
                    enabled = newBranchName.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Create Branch")
                }
            } else {
                branches.forEach { branch ->
                    if (branch != currentBranch) {
                        TextButton(
                            onClick = { onSwitchBranch(branch) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.CallSplit,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(branch)
                            }
                        }
                    }
                }
            }
        }
    }
}