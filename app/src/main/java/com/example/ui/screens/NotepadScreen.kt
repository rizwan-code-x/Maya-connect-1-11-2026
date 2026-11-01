package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.example.ui.components.rememberChromeSmoothFlingBehavior
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.NoteEntity
import com.example.ui.components.glassmorphic
import com.example.ui.theme.MayaTheme
import com.example.ui.viewmodel.MayaViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Point 7: Functional Notepad Module with Room DB persistence,
 * note creation, editing, color tagging, pinning, and deletion.
 */
@Composable
fun NotepadScreen(
    viewModel: MayaViewModel,
    onClose: () -> Unit
) {
    val theme = MayaTheme.colors
    val notes by viewModel.allNotes.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var noteToEdit by remember { mutableStateOf<NoteEntity?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }

    val filteredNotes = remember(notes, searchQuery) {
        if (searchQuery.isBlank()) notes
        else notes.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.content.contains(searchQuery, ignoreCase = true)
        }
    }

    BackHandler {
        onClose()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.backgroundGradient)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // 1. Top Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(theme.surfaceGlass)
                        .border(1.dp, theme.borderGlass, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = theme.textPrimary
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Notepad & Ideas",
                        style = TextStyle(
                            color = theme.textPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "${notes.size} Notes Synchronized",
                        style = TextStyle(
                            color = theme.textSecondary,
                            fontSize = 11.sp
                        )
                    )
                }

                IconButton(
                    onClick = { showCreateDialog = true },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(theme.accentPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Note",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Search Field
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassmorphic(
                        shape = RoundedCornerShape(16.dp),
                        backgroundColor = theme.surfaceGlass,
                        borderColor = theme.borderGlass,
                        borderWidth = 1.dp
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = theme.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = TextStyle(color = theme.textPrimary, fontSize = 13.5.sp),
                        cursorBrush = SolidColor(theme.accentPrimary),
                        decorationBox = { inner ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search your notes and ideas...",
                                    style = TextStyle(color = theme.textSecondary, fontSize = 13.5.sp)
                                )
                            }
                            inner()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Notes List
            if (filteredNotes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = null,
                            tint = theme.textSecondary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (searchQuery.isBlank()) "No notes yet" else "No matching notes found",
                            style = TextStyle(
                                color = theme.textPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap the + button above to create your first note",
                            style = TextStyle(
                                color = theme.textSecondary,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    flingBehavior = rememberChromeSmoothFlingBehavior(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredNotes, key = { it.id }) { note ->
                        NoteCard(
                            note = note,
                            onClick = { noteToEdit = note },
                            onPinToggle = {
                                viewModel.updateNote(note.copy(isPinned = !note.isPinned))
                            },
                            onDelete = {
                                viewModel.deleteNote(note.id)
                            }
                        )
                    }
                }
            }
        }

        // Create / Edit Note Dialog
        if (showCreateDialog) {
            NoteEditDialog(
                note = null,
                onDismiss = { showCreateDialog = false },
                onSave = { title, content, colorHex ->
                    viewModel.createNote(title, content, colorHex)
                    showCreateDialog = false
                }
            )
        }

        noteToEdit?.let { note ->
            NoteEditDialog(
                note = note,
                onDismiss = { noteToEdit = null },
                onSave = { title, content, colorHex ->
                    viewModel.updateNote(note.copy(title = title, content = content, colorHex = colorHex))
                    noteToEdit = null
                }
            )
        }
    }
}

@Composable
private fun NoteCard(
    note: NoteEntity,
    onClick: () -> Unit,
    onPinToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val theme = MayaTheme.colors
    val dateStr = remember(note.timestamp) {
        SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault()).format(Date(note.timestamp))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(theme.surfaceGlass)
            .border(1.dp, theme.borderGlass, RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(note.colorHex))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = note.title,
                        style = TextStyle(
                            color = theme.textPrimary,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth(0.75f)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onPinToggle,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (note.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = "Pin",
                            tint = if (note.isPinned) Color(0xFFF59E0B) else theme.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            if (note.content.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = note.content,
                    style = TextStyle(
                        color = theme.textSecondary,
                        fontSize = 12.5.sp,
                        lineHeight = 18.sp
                    ),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = dateStr,
                style = TextStyle(
                    color = theme.textSecondary.copy(alpha = 0.7f),
                    fontSize = 10.5.sp
                )
            )
        }
    }
}

@Composable
private fun NoteEditDialog(
    note: NoteEntity?,
    onDismiss: () -> Unit,
    onSave: (title: String, content: String, colorHex: Long) -> Unit
) {
    val theme = MayaTheme.colors
    var title by remember { mutableStateOf(note?.title ?: "") }
    var content by remember { mutableStateOf(note?.content ?: "") }
    var selectedColor by remember { mutableLongStateOf(note?.colorHex ?: 0xFF4F46E5) }

    val colorPalette = listOf(
        0xFF4F46E5, // Indigo
        0xFF0284C7, // Cyan
        0xFF10B981, // Emerald
        0xFFF59E0B, // Amber
        0xFFEC4899, // Pink
        0xFF8B5CF6  // Purple
    )

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glassmorphic(
                    shape = RoundedCornerShape(24.dp),
                    backgroundColor = theme.surface,
                    borderColor = theme.borderGlass,
                    borderWidth = 1.dp
                )
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (note == null) "New Note" else "Edit Note",
                        style = TextStyle(
                            color = theme.textPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = theme.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Title Input
                BasicTextField(
                    value = title,
                    onValueChange = { title = it },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = theme.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    cursorBrush = SolidColor(theme.accentPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(theme.surfaceGlass)
                        .border(1.dp, theme.borderGlass, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    decorationBox = { inner ->
                        if (title.isEmpty()) {
                            Text(
                                text = "Note Title...",
                                style = TextStyle(color = theme.textSecondary, fontSize = 15.sp)
                            )
                        }
                        inner()
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Content Input
                BasicTextField(
                    value = content,
                    onValueChange = { content = it },
                    textStyle = TextStyle(
                        color = theme.textPrimary,
                        fontSize = 13.5.sp,
                        lineHeight = 20.sp
                    ),
                    cursorBrush = SolidColor(theme.accentPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(theme.surfaceGlass)
                        .border(1.dp, theme.borderGlass, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    decorationBox = { inner ->
                        if (content.isEmpty()) {
                            Text(
                                text = "Write your note content here...",
                                style = TextStyle(color = theme.textSecondary, fontSize = 13.5.sp)
                            )
                        }
                        inner()
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Color Picker Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    colorPalette.forEach { colorVal ->
                        val isSelected = colorVal == selectedColor
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(colorVal))
                                .border(
                                    if (isSelected) 2.5.dp else 1.dp,
                                    if (isSelected) Color.White else Color(0x33FFFFFF),
                                    CircleShape
                                )
                                .clickable { selectedColor = colorVal }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Save Action Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(theme.accentPrimary)
                        .clickable {
                            onSave(title.ifBlank { "Untitled Note" }, content, selectedColor)
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (note == null) "Save Note" else "Update Note",
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}
