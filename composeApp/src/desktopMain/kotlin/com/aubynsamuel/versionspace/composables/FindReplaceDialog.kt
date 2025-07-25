package com.aubynsamuel.versionspace.composables

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FindReplace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FindReplaceDialog(
    text: String,
    onTextChange: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var findText by remember { mutableStateOf("") }
    var replaceText by remember { mutableStateOf("") }
    var caseSensitive by remember { mutableStateOf(false) }
    var wholeWords by remember { mutableStateOf(false) }
    var matchCount by remember { mutableStateOf(0) }

    fun isWholeWord(text: String, start: Int, length: Int): Boolean {
        val before = if (start > 0) text[start - 1] else ' '
        val after = if (start + length < text.length) text[start + length] else ' '
        return !before.isLetterOrDigit() && !after.isLetterOrDigit()
    }

    fun replaceWholeWords(
        text: String,
        find: String,
        replace: String,
        caseSensitive: Boolean,
    ): String {
        val regex = if (caseSensitive) {
            Regex("\\b${Regex.escape(find)}\\b")
        } else {
            Regex("\\b${Regex.escape(find)}\\b", RegexOption.IGNORE_CASE)
        }
        return regex.replace(text, replace)
    }

    fun findMatches(): List<IntRange> {
        if (findText.isBlank()) return emptyList()

        val searchText = if (caseSensitive) text else text.lowercase()
        val pattern = if (caseSensitive) findText else findText.lowercase()

        val matches = mutableListOf<IntRange>()
        var startIndex = 0

        while (true) {
            val index = searchText.indexOf(pattern, startIndex)
            if (index == -1) break

            if (!wholeWords || isWholeWord(text, index, pattern.length)) {
                matches.add(index until index + pattern.length)
            }
            startIndex = index + 1
        }

        return matches
    }

    fun replaceAll() {
        if (findText.isBlank()) return

        val newText = if (wholeWords) {
            replaceWholeWords(text, findText, replaceText, caseSensitive)
        } else {
            if (caseSensitive) {
                text.replace(findText, replaceText)
            } else {
                text.replace(findText, replaceText, ignoreCase = true)
            }
        }
        onTextChange(newText)
    }

    matchCount = findMatches().size

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.FindReplace, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Find & Replace")
            }
        },
        text = {
            Column {
                OutlinedTextField(
                    value = findText,
                    onValueChange = { findText = it },
                    label = { Text("Find") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        if (matchCount > 0) {
                            Text(
                                text = "$matchCount",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = replaceText,
                    onValueChange = { replaceText = it },
                    label = { Text("Replace with") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Options
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = caseSensitive,
                            onCheckedChange = { caseSensitive = it }
                        )
                        Text("Case sensitive", style = MaterialTheme.typography.bodySmall)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = wholeWords,
                            onCheckedChange = { wholeWords = it }
                        )
                        Text("Whole words", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            Row {
                Button(
                    onClick = { replaceAll() },
                    enabled = findText.isNotBlank() && matchCount > 0
                ) {
                    Icon(Icons.Default.FindReplace, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Replace All")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}