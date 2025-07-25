package com.aubynsamuel.versionspace

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.onClick
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aubynsamuel.versionspace.composables.DirectoryPickerDialog
import com.aubynsamuel.versionspace.composables.FileTreeView
import com.aubynsamuel.versionspace.composables.FindReplaceDialog
import com.aubynsamuel.versionspace.composables.GitPanel
import com.aubynsamuel.versionspace.data.GitCommit
import com.aubynsamuel.versionspace.data.GitStatus
import com.aubynsamuel.versionspace.managers.FileManager
import com.aubynsamuel.versionspace.managers.GitManager
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor
import kotlinx.coroutines.launch
import java.io.File

/**
 * The main application composable, which sets up the UI and manages the application state.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun App() {
    var currentDirectory by remember {
        mutableStateOf(File(System.getProperty("user.home"), "Desktop/Try"))
    }
    var selectedItem by remember { mutableStateOf<File?>(null) }
    val richTextState = rememberRichTextState()
    var fileContent by remember { mutableStateOf("") }
    var originalFileContent by remember { mutableStateOf("") }
    var gitStatus by remember { mutableStateOf(GitStatus()) }
    var showGitPanel by remember { mutableStateOf(false) }
    var commitMessage by remember { mutableStateOf("") }
    var newBranchName by remember { mutableStateOf("") }
    var showCreateBranch by remember { mutableStateOf(false) }
    var gitOutput by remember { mutableStateOf("") }
    var branches by remember { mutableStateOf<List<String>>(emptyList()) }
    var commits by remember { mutableStateOf<List<GitCommit>>(emptyList()) }
    var newFileName by remember { mutableStateOf("") }
    var showCreateFile by remember { mutableStateOf(false) }
    var showFindReplace by remember { mutableStateOf(false) }
    var showDirectoryPicker by remember { mutableStateOf(false) }
    var hasUnsavedChanges by remember { mutableStateOf(false) }
    var showCreateFolder by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }
    var refreshTrigger by remember { mutableStateOf(0) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var fileToActOn by remember { mutableStateOf<File?>(null) }
    var newName by remember { mutableStateOf("") }

    LaunchedEffect(showRenameDialog) {
        println("showRenameDialog changed to: $showRenameDialog")
    }


    val gitManager = remember(currentDirectory) { GitManager(currentDirectory) }
    val fileManager = remember(currentDirectory) { FileManager(currentDirectory) }
    val scope = rememberCoroutineScope()

    // Check for unsaved changes
    LaunchedEffect(richTextState.toText(), originalFileContent) {
        hasUnsavedChanges = richTextState.toText() != originalFileContent && selectedItem != null
    }

    // Load initial data
    LaunchedEffect(currentDirectory) {
        gitStatus = gitManager.getStatus()
        branches = gitManager.getBranches()
        commits = gitManager.getCommitHistory()
    }

    /**
     * Refreshes the Git data, updating the status, branches, and commit history.
     */
    fun refreshGitData() {
        scope.launch {
            gitStatus = gitManager.getStatus()
            branches = gitManager.getBranches()
            commits = gitManager.getCommitHistory()
        }
    }

    /**
     * Saves the content of the currently selected file.
     */
    fun saveCurrentFile() {
        selectedItem?.let { file ->
            if (fileManager.writeFile(file, richTextState.toText())) {
                originalFileContent = richTextState.toText()
                hasUnsavedChanges = false
                refreshGitData()
            }
        }
    }

    /**
     * Loads the content of a file into the editor or selects a folder.
     * @param file The file or folder to handle.
     */
    fun handleFileOrFolderClick(file: File) {
        selectedItem = file
        if (file.isFile) {
            hasUnsavedChanges = false
            val content = fileManager.readFile(file)
            richTextState.setMarkdown(content)
            fileContent = richTextState.toText()
            originalFileContent = richTextState.toText()
        }
    }

    /**
     * Changes the current working directory and refreshes the UI.
     * @param newDir The new directory to switch to.
     */
    fun changeDirectory(newDir: File) {
        if (newDir.exists() && newDir.isDirectory) {
            currentDirectory = newDir
            selectedItem = null
            fileContent = ""
            originalFileContent = ""
            hasUnsavedChanges = false
            refreshGitData()
        }
    }

    MaterialTheme {
        Row(modifier = Modifier.fillMaxSize()) {
            // Left navigation rail
            Column(
                modifier = Modifier
                    .width(50.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                    .padding(8.dp)
            ) {
                Spacer(modifier = Modifier.fillMaxHeight(0.1f))
                IconButton(onClick = { showGitPanel = false }) {
                    Icon(Icons.Default.FolderOpen, contentDescription = "", tint = Color.White)
                }
                IconButton(onClick = { showGitPanel = true }) {
                    Icon(Icons.Default.OpenWith, contentDescription = "", tint = Color.White)
                }
            }

            // Left panel - File explorer and Git
            Column(
                modifier = Modifier
                    .width(300.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(8.dp)
            ) {
                // Directory selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Directory:",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { showDirectoryPicker = true }
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = "Change directory")
                    }
                }

                Text(
                    text = currentDirectory.absolutePath,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (showGitPanel) {
                    GitPanel(
                        gitStatus = gitStatus,
                        branches = branches,
                        commits = commits,
                        commitMessage = commitMessage,
                        onCommitMessageChange = { commitMessage = it },
                        newBranchName = newBranchName,
                        onNewBranchNameChange = { newBranchName = it },
                        showCreateBranch = showCreateBranch,
                        onShowCreateBranchChange = { showCreateBranch = it },
                        gitOutput = gitOutput,
                        onGitCommand = { command ->
                            scope.launch {
                                gitOutput = when (command.first()) {
                                    "add" -> gitManager.addFile(command[1]).fold(
                                        onSuccess = { "Added ${command[1]}" },
                                        onFailure = { "Error: ${it.message}" }
                                    )

                                    "commit" -> gitManager.commit(commitMessage).fold(
                                        onSuccess = {
                                            commitMessage = ""
                                            "Committed successfully"
                                        },
                                        onFailure = { "Error: ${it.message}" }
                                    )

                                    "branch" -> gitManager.createBranch(newBranchName).fold(
                                        onSuccess = {
                                            newBranchName = ""
                                            showCreateBranch = false
                                            "Created branch $newBranchName"
                                        },
                                        onFailure = { "Error: ${it.message}" }
                                    )

                                    "checkout" -> gitManager.switchBranch(command[1]).fold(
                                        onSuccess = { "Switched to ${command[1]}" },
                                        onFailure = { "Error: ${it.message}" }
                                    )

                                    "init" -> gitManager.initRepository().fold(
                                        onSuccess = { "Repository initialized" },
                                        onFailure = { "Error: ${it.message}" }
                                    )

                                    "push" -> gitManager.push().fold(
                                        onSuccess = { "Pushed successfully" },
                                        onFailure = { "Error: ${it.message}" }
                                    )

                                    "pull" -> gitManager.pull().fold(
                                        onSuccess = {
                                            // Refresh file content if current file was modified
                                            selectedItem?.let { handleFileOrFolderClick(it) }
                                            "Pulled successfully"
                                        },
                                        onFailure = { "Error: ${it.message}" }
                                    )

                                    "fetch" -> gitManager.fetch().fold(
                                        onSuccess = { "Fetched successfully" },
                                        onFailure = { "Error: ${it.message}" }
                                    )

                                    "diff" -> {
                                        if (command.size > 1) {
                                            gitManager.getDiff(command[1]).fold(
                                                onSuccess = { it.ifEmpty { "No changes in ${command[1]}" } },
                                                onFailure = { "Error: ${it.message}" }
                                            )
                                        } else {
                                            gitManager.getDiff().fold(
                                                onSuccess = { it.ifEmpty { "No changes" } },
                                                onFailure = { "Error: ${it.message}" }
                                            )
                                        }
                                    }

                                    else -> "Unknown command"
                                }
                                refreshGitData()
                            }
                        }
                    )
                } else {
                    // File explorer
                    Column(modifier = Modifier.onClick(onClick = {
                        if (selectedItem?.isDirectory ?: false) selectedItem = null
                    })) {
                        // Header with controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Files",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )



                            IconButton(
                                onClick = { showCreateFolder = true }
                            ) {
                                Icon(
                                    Icons.Default.CreateNewFolder,
                                    contentDescription = "Create folder"
                                )
                            }

                            IconButton(
                                onClick = { showCreateFile = true }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Create file")
                            }
                        }

                        // File tree
                        FileTreeView(
                            fileManager = fileManager,
                            onItemClick = { file -> handleFileOrFolderClick(file) },
                            selectedItem = selectedItem,
                            onMove = { draggedFile, targetDirectory ->
                                println("App: onMove triggered. Dragged: ${draggedFile.absolutePath}, Target: ${targetDirectory.absolutePath}")
                                if (fileManager.moveFile(draggedFile, targetDirectory)) {
                                    println("App: File move successful. Refreshing directory.")
                                    // Refresh the file tree after a successful move
                                    // This might require re-loading the current directory or a more granular update
                                    // For now, a full refresh of the current directory will suffice
                                    changeDirectory(currentDirectory)
                                    refreshTrigger++ // Trigger UI refresh after move
                                } else {
                                    println("App: File move failed.")
                                }
                            },
                            refreshTrigger = refreshTrigger,
                            onRename = { file ->
                                fileToActOn = file
                                newName = file.name
                                showRenameDialog = true
                            },
                            onDelete = { file ->
                                fileToActOn = file
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }

            // Right panel - Text editor
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(8.dp)
            ) {
                // Editor header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedItem?.name ?: "No file selected",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f)
                    )

                    if (selectedItem != null) {
                        IconButton(
                            onClick = { showFindReplace = true }
                        ) {
                            Icon(Icons.Default.Search, contentDescription = "Find/Replace")
                        }

                        Button(
                            onClick = { saveCurrentFile() },
                            enabled = hasUnsavedChanges
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (hasUnsavedChanges) "Save*" else "Save")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Text editor
                Card(
                    modifier = Modifier.fillMaxSize()
                ) {
                    RichTextEditor(
                        state = richTextState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        textStyle = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp
                        )
                    )
                }
            }
        }

        // Create file dialog
        if (showCreateFile) {
            AlertDialog(
                onDismissRequest = { showCreateFile = false },
                title = { Text("Create New File") },
                text = {
                    OutlinedTextField(
                        value = newFileName,
                        onValueChange = { newFileName = it },
                        label = { Text("File name") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (newFileName.isNotBlank()) {
                                val parentDir =
                                    selectedItem?.takeIf { it.isDirectory } ?: currentDirectory
                                fileManager.createFile(newFileName, parentDir)?.let { file ->
                                    handleFileOrFolderClick(file)
                                }
                                newFileName = ""
                                showCreateFile = false
                                refreshTrigger++ // Increment to trigger UI refresh
                            }
                        }
                    ) {
                        Text("Create")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateFile = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Find/Replace dialog
        if (showFindReplace) {
            FindReplaceDialog(
                text = fileContent,
                onTextChange = { fileContent = it },
                onDismiss = { showFindReplace = false }
            )
        }

        // Directory picker dialog
        if (showDirectoryPicker) {
            DirectoryPickerDialog(
                currentDirectory = currentDirectory,
                onDirectorySelected = { newDir ->
                    changeDirectory(newDir)
                    showDirectoryPicker = false
                },
                onDismiss = { showDirectoryPicker = false }
            )
        }

        // Create folder dialog
        if (showCreateFolder) {
            AlertDialog(
                onDismissRequest = { showCreateFolder = false },
                title = { Text("Create New Folder") },
                text = {
                    OutlinedTextField(
                        value = newFolderName,
                        onValueChange = { newFolderName = it },
                        label = { Text("Folder name") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (newFolderName.isNotBlank()) {
                                val parentDir =
                                    selectedItem?.takeIf { it.isDirectory } ?: currentDirectory
                                fileManager.createFolder(newFolderName, parentDir)
                                newFolderName = ""
                                showCreateFolder = false
                                refreshTrigger++ // Increment to trigger UI refresh
                            }
                        }
                    ) {
                        Text("Create")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateFolder = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}