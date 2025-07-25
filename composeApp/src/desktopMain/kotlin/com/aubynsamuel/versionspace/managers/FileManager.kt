package com.aubynsamuel.versionspace.managers

import java.io.File

/**
 * Manages file operations within a specified working directory.
 * @param workingDirectory The directory where file operations will be performed.
 */
class FileManager(private val workingDirectory: File) {

    /**
     * Lists all non-hidden files in the working directory, sorted by name.
     * @return A list of File objects.
     */
    fun listFiles(): List<File> {
        return workingDirectory.listFiles()?.filter {
            it.isFile && !it.name.startsWith(".")
        }?.sortedBy { it.name } ?: emptyList()
    }

    /**
     * Lists all items (files and directories) in the working directory, sorted with directories first.
     * @return A list of File objects.
     */
    fun listAllItems(): List<File> {
        return workingDirectory.listFiles()?.filter {
            !it.name.startsWith(".")
        }?.sortedWith(
            compareBy<File> { !it.isDirectory }.thenBy { it.name.lowercase() }
        ) ?: emptyList()
    }

    /**
     * Reads the content of a file.
     * @param file The file to read.
     * @return The content of the file as a String, or an error message if reading fails.
     */
    fun readFile(file: File): String {
        return try {
            file.readText()
        } catch (e: Exception) {
            "Error reading file: ${e.message}"
        }
    }

    /**
     * Writes content to a file.
     * @param file The file to write to.
     * @param content The content to write.
     * @return True if the write operation is successful, false otherwise.
     */
    fun writeFile(file: File, content: String): Boolean {
        return try {
            file.writeText(content)
            true
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Creates a new file in the specified parent directory.
     * @param filename The name of the file to create.
     * @param parentDirectory The directory in which to create the file.
     * @return The created File object, or null if the file already exists or creation fails.
     */
    fun createFile(filename: String, parentDirectory: File): File? {
        return try {
            val file = File(parentDirectory, filename)
            if (!file.exists()) {
                file.createNewFile()
                file
            } else null
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Creates a new folder in the specified parent directory.
     * @param folderName The name of the folder to create.
     * @param parentDirectory The directory in which to create the folder.
     * @return The created File object representing the folder, or null if the folder already exists or creation fails.
     */
    fun createFolder(folderName: String, parentDirectory: File): File? {
        return try {
            val folder = File(parentDirectory, folderName)
            if (!folder.exists()) {
                val created = folder.mkdirs()
                if (created) folder else null
            } else null
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Deletes a file or directory.
     * @param file The file or directory to delete.
     * @return True if the deletion is successful, false otherwise.
     */
    fun deleteFile(file: File): Boolean {
        return try {
            if (file.isDirectory) {
                file.deleteRecursively()
            } else {
                file.delete()
            }
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Renames a file or directory.
     * @param file The file or directory to rename.
     * @param newName The new name.
     * @return True if the rename is successful, false otherwise.
     */
    fun renameFile(file: File, newName: String): Boolean {
        return try {
            val newFile = File(file.parentFile, newName)
            file.renameTo(newFile)
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Moves a file or directory to a new destination.
     * @param file The file or directory to move.
     * @param destinationDirectory The directory to move the file/directory to.
     * @return True if the move is successful, false otherwise.
     */
    fun moveFile(file: File, destinationDirectory: File): Boolean {
        println("FileManager: Attempting to move file: ${file.absolutePath} to ${destinationDirectory.absolutePath}")
        return try {
            val newFile = File(destinationDirectory, file.name)
            val success = file.renameTo(newFile)
            println("FileManager: Move successful: $success")
            success
        } catch (e: Exception) {
            println("FileManager: Error moving file: ${e.message}")
            false
        }
    }
}