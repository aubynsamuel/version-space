package com.aubynsamuel.versionspace.composables

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.io.File

@Composable
fun DirectoryPickerDialog(
    currentDirectory: File,
    onDirectorySelected: (File) -> Unit,
    onDismiss: () -> Unit,
) {
    var selectedDirectory by remember { mutableStateOf(currentDirectory) }
    var pathText by remember { mutableStateOf(currentDirectory.absolutePath) }

    fun navigateToDirectory(dir: File) {
        if (dir.exists() && dir.isDirectory) {
            selectedDirectory = dir
            pathText = dir.absolutePath
        }
    }

    fun getDirectories(dir: File): List<File> {
        return try {
            dir.listFiles()?.filter { it.isDirectory && !it.isHidden }?.sortedBy { it.name }
                ?: emptyList()
        } catch (_: SecurityException) {
            emptyList()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.FolderOpen, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Select Directory")
            }
        },
        text = {
            Column {
                // Current path with manual input
                OutlinedTextField(
                    value = pathText,
                    onValueChange = { pathText = it },
                    label = { Text("Directory Path") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        TextButton(
                            onClick = {
                                val newDir = File(pathText)
                                if (newDir.exists() && newDir.isDirectory) {
                                    navigateToDirectory(newDir)
                                }
                            }
                        ) {
                            Text("Go")
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Navigation buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            navigateToDirectory(File(System.getProperty("user.home")))
                        }
                    ) {
                        Icon(Icons.Default.Home, contentDescription = "Home")
                    }

                    IconButton(
                        onClick = {
                            selectedDirectory.parentFile?.let { parent ->
                                navigateToDirectory(parent)
                            }
                        },
                        enabled = selectedDirectory.parentFile != null
                    ) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Up")
                    }

                    Text(
                        text = selectedDirectory.name.ifEmpty { selectedDirectory.absolutePath },
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Directory list
                LazyColumn(
                    modifier = Modifier.height(300.dp)
                ) {
                    items(getDirectories(selectedDirectory)) { directory ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            onClick = { navigateToDirectory(directory) }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Folder,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = directory.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onDirectorySelected(selectedDirectory) }
            ) {
                Text("Select")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}