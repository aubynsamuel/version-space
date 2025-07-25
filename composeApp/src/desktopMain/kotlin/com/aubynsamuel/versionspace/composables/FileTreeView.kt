package com.aubynsamuel.versionspace.composables

import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.ContextMenuArea
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aubynsamuel.versionspace.managers.FileManager
import com.mohamedrejeb.compose.dnd.DragAndDropContainer
import com.mohamedrejeb.compose.dnd.drag.DraggableItem
import com.mohamedrejeb.compose.dnd.drop.dropTarget
import com.mohamedrejeb.compose.dnd.rememberDragAndDropState
import java.io.File


data class FileTreeItem(
    val file: File,
    val level: Int,
    val isExpanded: Boolean,
    val isDirectory: Boolean,
)

@Composable
fun FileTreeView(
    fileManager: FileManager,
    onItemClick: (File) -> Unit,
    selectedItem: File?,
    onMove: (File, File) -> Unit,
    refreshTrigger: Int,
    onRename: (File) -> Unit,
    onDelete: (File) -> Unit,
) {
    var expandedFolders by remember { mutableStateOf(setOf<String>()) }
    val fileTree = remember(fileManager, expandedFolders, refreshTrigger) {
        buildFileTree(fileManager.listAllItems(), 0, expandedFolders)
    }
    val dragAndDropState = rememberDragAndDropState<String>()

    DragAndDropContainer(
        state = dragAndDropState,
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(fileTree, key = { it.file.absolutePath }) { item ->
                DraggableItem(
                    state = dragAndDropState,
                    key = item.file.absolutePath,
                    data = item.file.absolutePath,
                ) {
                    FileTreeRow(
                        item = item,
                        selectedItem = selectedItem,
                        onClick = {
                            if (item.isDirectory) {
                                expandedFolders = if (item.isExpanded) {
                                    expandedFolders - item.file.absolutePath
                                } else {
                                    expandedFolders + item.file.absolutePath
                                }
                            }
                            onItemClick(item.file)
                        },
                        modifier = Modifier.dropTarget(
                            state = dragAndDropState,
                            key = item.file.absolutePath,
                            onDrop = { data ->
                                println("onDrop: data type = ${data.javaClass.name}")
                                val draggedItemState = data
                                val draggedData = draggedItemState.data
                                println("onDrop: draggedData type = ${draggedData.javaClass.name}")

                                val draggedFile = File(draggedData)
                                val targetDirectory =
                                    if (item.file.isDirectory) item.file else item.file.parentFile

                                // Prevent dropping a file onto itself or its parent
                                if (draggedFile.absolutePath == targetDirectory.absolutePath || draggedFile.parentFile?.absolutePath == targetDirectory.absolutePath) {
                                    println("Cannot move a file to its current location or parent.")
                                } else if (targetDirectory.absolutePath.startsWith(draggedFile.absolutePath + File.separator)) {
                                    println("Cannot move a folder into its own subfolder.")
                                } else {
                                    onMove(draggedFile, targetDirectory)
                                }
                            }
                        ),
                        onRename = { onRename(item.file) },
                        onDelete = { onDelete(item.file) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FileTreeRow(
    item: FileTreeItem,
    selectedItem: File?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onRename: (File) -> Unit,
    onDelete: (File) -> Unit,
) {
    val isSelected = item.file == selectedItem
    ContextMenuArea(
        items = {
            listOf(
                ContextMenuItem("Rename") { onRename(item.file) },
                ContextMenuItem("Delete") { onDelete(item.file) }
            )
        }
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = { /* No specific long click action */ }
                )
                .padding(start = (item.level * 16).dp)
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icon = if (item.isDirectory) {
                if (item.isExpanded) Icons.Default.KeyboardArrowDown
                else Icons.AutoMirrored.Filled.KeyboardArrowRight
            } else {
                Icons.Default.Description
            }
            val contentDescription = if (item.isDirectory) "Directory" else "File"

            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = item.file.name,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Unspecified
            )
        }
    }
}

private fun buildFileTree(
    files: List<File>,
    level: Int,
    expandedFolders: Set<String>,
): List<FileTreeItem> {
    val tree = mutableListOf<FileTreeItem>()
    for (file in files) {
        val isDirectory = file.isDirectory
        val isExpanded = expandedFolders.contains(file.absolutePath)
        tree.add(FileTreeItem(file, level, isExpanded, isDirectory))
        if (isDirectory && isExpanded) {
            val children = file.listFiles()?.filter { !it.name.startsWith(".") }?.sortedWith(
                compareBy<File> { !it.isDirectory }.thenBy { it.name.lowercase() }
            ) ?: emptyList()
            tree.addAll(buildFileTree(children, level + 1, expandedFolders))
        }
    }
    return tree
}
