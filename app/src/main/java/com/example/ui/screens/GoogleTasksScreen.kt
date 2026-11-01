package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.TaskEntity
import com.example.ui.theme.MayaTheme

// Golden SIFRA Theme Palette
private val GoldenAccent = Color(0xFFEAB308)
private val GoldenGlow = Color(0x66EAB308)
private val DarkBg = Color(0xFF0D0D11)
private val CardBg = Color(0xFF16161D)
private val CardBorder = Color(0x26EAB308)
private val PriorityRed = Color(0xFFEF4444)
private val PriorityYellow = Color(0xFFEAB308)

@Composable
fun GoogleTasksScreen(
    tasks: List<TaskEntity>,
    onClose: () -> Unit,
    onCreateTask: (title: String, details: String, dueDate: String, category: String) -> Unit = { _, _, _, _ -> },
    onCreateTaskFull: (title: String, details: String, dueDate: String, dueTime: String, category: String, priority: String, isStarred: Boolean) -> Unit = { _, _, _, _, _, _, _ -> },
    onToggleTaskStatus: (id: String, currentStatus: Boolean) -> Unit,
    onToggleTaskStarred: (id: String, currentStarred: Boolean) -> Unit = { _, _ -> },
    onDeleteTask: (id: String) -> Unit,
    onClearCompleted: () -> Unit
) {
    val theme = MayaTheme.colors
    var selectedCategory by remember { mutableStateOf("All Tasks") }
    var isDrawerOpen by remember { mutableStateOf(false) }
    var showNewTaskModal by remember { mutableStateOf(false) }
    var showManageListsModal by remember { mutableStateOf(false) }
    var showStatsModal by remember { mutableStateOf(false) }
    var showInfoModal by remember { mutableStateOf(false) }

    var customCategories by remember {
        mutableStateOf(listOf("Personal", "Work", "Shopping", "Health"))
    }

    val filteredTasks = remember(tasks, selectedCategory) {
        if (selectedCategory == "All Tasks") tasks
        else tasks.filter { it.listCategory.equals(selectedCategory, ignoreCase = true) }
    }

    val totalCount = tasks.size
    val completedCount = tasks.count { it.isCompleted }
    val syncPercent = if (totalCount > 0) (completedCount * 100 / totalCount) else 100

    BackHandler {
        if (isDrawerOpen) isDrawerOpen = false
        else onClose()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // 1. Top Bar Header (Screenshot 1 Exact)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Drawer Menu Icon (≡)
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable { isDrawerOpen = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Back Arrow (←)
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable { onClose() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Center Title Block
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "DAILY SIFRA",
                        style = TextStyle(
                            color = GoldenAccent,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = selectedCategory,
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        )
                    )
                }

                // Top Right Icon (Overlapping Copy Squares ❐)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable { showStatsModal = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Sync Info",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Sub-header Banner Text (Screenshot 1)
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (filteredTasks.isEmpty()) "PREVIEW — TAP + TO ADD YOUR FIRST TASK"
                    else "TASKS SYNCED — TAP + TO ADD A NEW TASK",
                    style = TextStyle(
                        color = GoldenAccent.copy(alpha = 0.85f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3. Task Cards List
            LazyColumn(
                modifier = Modifier.weight(1f),
                flingBehavior = rememberChromeSmoothFlingBehavior(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (filteredTasks.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 60.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = GoldenAccent,
                                    modifier = Modifier.size(52.dp)
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "No Tasks In List",
                                    style = TextStyle(
                                        color = Color.White,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Serif
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Tap the golden + button below to create one.",
                                    style = TextStyle(
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }
                    }
                } else {
                    items(filteredTasks, key = { it.id }) { task ->
                        SifraTaskCard(
                            task = task,
                            onToggleStatus = { onToggleTaskStatus(task.id, task.isCompleted) },
                            onToggleStarred = { onToggleTaskStarred(task.id, task.isStarred) },
                            onDelete = { onDeleteTask(task.id) }
                        )
                    }
                }
            }
        }

        // 4. Floating Action Button (+) with Golden Glow (Screenshot 1)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 32.dp, end = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            // Glow aura circle
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(GoldenGlow, Color.Transparent)
                        )
                    )
            )

            // Round Golden Button
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(GoldenAccent)
                    .clickable { showNewTaskModal = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Task",
                    tint = DarkBg,
                    modifier = Modifier.size(30.dp)
                )
            }
        }

        // 5. Side Drawer Navigation Overlay (Screenshot 2)
        if (isDrawerOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable { isDrawerOpen = false }
            )

            AnimatedVisibility(
                visible = isDrawerOpen,
                enter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn(),
                exit = slideOutHorizontally(targetOffsetX = { -it }) + fadeOut(),
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                SifraSideDrawer(
                    totalTasks = totalCount,
                    completedTasks = completedCount,
                    syncPercent = syncPercent,
                    selectedCategory = selectedCategory,
                    customCategories = customCategories,
                    onSelectCategory = { cat ->
                        selectedCategory = cat
                        isDrawerOpen = false
                    },
                    onOpenManageLists = {
                        isDrawerOpen = false
                        showManageListsModal = true
                    },
                    onOpenStats = {
                        isDrawerOpen = false
                        showStatsModal = true
                    },
                    onOpenKnowSifra = {
                        isDrawerOpen = false
                        showInfoModal = true
                    }
                )
            }
        }

        // 6. "New Task" Modal Dialog (Screenshot 3)
        if (showNewTaskModal) {
            NewTaskModalDialog(
                customCategories = customCategories,
                onDismiss = { showNewTaskModal = false },
                onSave = { title, details, dueDate, dueTime, category, priority, isStarred ->
                    onCreateTaskFull(title, details, dueDate, dueTime, category, priority, isStarred)
                    showNewTaskModal = false
                }
            )
        }

        // 7. Manage Lists Modal
        if (showManageListsModal) {
            ManageListsModalDialog(
                categories = customCategories,
                onDismiss = { showManageListsModal = false },
                onAddCategory = { newCat ->
                    if (newCat.isNotBlank() && !customCategories.contains(newCat)) {
                        customCategories = customCategories + newCat
                    }
                },
                onDeleteCategory = { catToDelete ->
                    customCategories = customCategories.filter { it != catToDelete }
                }
            )
        }

        // 8. Statistics Modal
        if (showStatsModal) {
            TaskStatsModalDialog(
                totalTasks = totalCount,
                completedTasks = completedCount,
                starredTasks = tasks.count { it.isStarred },
                syncPercent = syncPercent,
                onDismiss = { showStatsModal = false }
            )
        }

        // 9. Know Sifra Daily Task Modal
        if (showInfoModal) {
            KnowSifraModalDialog(onDismiss = { showInfoModal = false })
        }
    }
}

// ====================================================================
// SIFRA Task Card Composable (Exact Screenshot 1 Layout & Colors)
// ====================================================================
@Composable
private fun SifraTaskCard(
    task: TaskEntity,
    onToggleStatus: () -> Unit,
    onToggleStarred: () -> Unit,
    onDelete: () -> Unit
) {
    val dotColor = when (task.listCategory.lowercase()) {
        "shopping" -> PriorityYellow
        "work" -> PriorityRed
        "health" -> PriorityRed
        else -> PriorityYellow
    }

    val displayDate = if (task.dueDate.isNotBlank()) task.dueDate else "Today"
    val displayTime = if (task.dueTime.isNotBlank()) " · ${task.dueTime}" else ""

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CardBg)
            .border(1.dp, CardBorder, RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Left Radio Circle Button (◯)
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .clickable { onToggleStatus() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = "Toggle Status",
                        tint = if (task.isCompleted) GoldenAccent else Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    // Task Title
                    Text(
                        text = task.title,
                        style = TextStyle(
                            color = if (task.isCompleted) Color.White.copy(alpha = 0.5f) else Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.SansSerif,
                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Subtitle Line: Dot + Category + Calendar Icon + Date
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Colored priority dot
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = task.listCategory,
                            style = TextStyle(
                                color = Color.White.copy(alpha = 0.65f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(12.dp)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = "$displayDate$displayTime",
                            style = TextStyle(
                                color = Color.White.copy(alpha = 0.65f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal
                            )
                        )
                    }
                }
            }

            // Right Star Icon (★)
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .clickable { onToggleStarred() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (task.isStarred) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "Favorite",
                    tint = if (task.isStarred) GoldenAccent else Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ====================================================================
// SIFRA Side Navigation Drawer (Exact Screenshot 2 Layout)
// ====================================================================
@Composable
private fun SifraSideDrawer(
    totalTasks: Int,
    completedTasks: Int,
    syncPercent: Int,
    selectedCategory: String,
    customCategories: List<String>,
    onSelectCategory: (String) -> Unit,
    onOpenManageLists: () -> Unit,
    onOpenStats: () -> Unit,
    onOpenKnowSifra: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(300.dp)
            .background(DarkBg)
            .border(width = 1.dp, color = CardBorder)
            .statusBarsPadding()
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(10.dp))

            // 1. Top Drawer Header Block (Screenshot 2)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(GoldenAccent.copy(alpha = 0.2f))
                        .border(1.dp, GoldenAccent.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FormatListBulleted,
                        contentDescription = "Daily Task Icon",
                        tint = GoldenAccent,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "Daily Task",
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        )
                    )
                    Text(
                        text = "NEURAL MEMORY",
                        style = TextStyle(
                            color = GoldenAccent,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Sync Status Line (Screenshot 2)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$completedTasks of $totalTasks Sync'd",
                    style = TextStyle(
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.5.sp
                    )
                )
                Text(
                    text = "$syncPercent%",
                    style = TextStyle(
                        color = GoldenAccent,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Thin progress line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(Color.White.copy(alpha = 0.1f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(if (totalTasks > 0) completedTasks.toFloat() / totalTasks else 1f)
                        .height(2.dp)
                        .background(GoldenAccent)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Section Header: NEURAL VIEW
            Text(
                text = "NEURAL VIEW",
                style = TextStyle(
                    color = GoldenAccent,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // "All Tasks" Drawer Card (Screenshot 2)
            val isAllSelected = selectedCategory == "All Tasks"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isAllSelected) CardBg else Color.Transparent)
                    .border(
                        1.dp,
                        if (isAllSelected) CardBorder else Color.Transparent,
                        RoundedCornerShape(14.dp)
                    )
                    .clickable { onSelectCategory("All Tasks") }
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            tint = GoldenAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "All Tasks",
                            style = TextStyle(
                                color = GoldenAccent,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    // Count Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.1f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "$totalTasks",
                            style = TextStyle(
                                color = GoldenAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Custom Category Drawer Cards
            customCategories.forEach { cat ->
                val isSelected = selectedCategory == cat
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) CardBg else Color.Transparent)
                        .border(
                            1.dp,
                            if (isSelected) CardBorder else Color.Transparent,
                            RoundedCornerShape(14.dp)
                        )
                        .clickable { onSelectCategory(cat) }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = cat,
                        style = TextStyle(
                            color = if (isSelected) GoldenAccent else Color.White.copy(alpha = 0.8f),
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 3. Bottom Menu Items (Screenshot 2)
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Manage Lists
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenManageLists() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = "Manage Lists",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = "Manage Lists",
                        style = TextStyle(
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                // Statistics
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenStats() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = "Statistics",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = "Statistics",
                        style = TextStyle(
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                // Know Sifra Daily Task (Golden Highlight)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenKnowSifra() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = "Info",
                        tint = GoldenAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = "Know Sifra Daily Task",
                        style = TextStyle(
                            color = GoldenAccent,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

// ====================================================================
// "New Task" Modal Dialog (Exact Screenshot 3 Layout & Controls)
// ====================================================================
@Composable
private fun NewTaskModalDialog(
    customCategories: List<String>,
    onDismiss: () -> Unit,
    onSave: (title: String, details: String, dueDate: String, dueTime: String, category: String, priority: String, isStarred: Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedList by remember { mutableStateOf("Personal") }
    var selectedPriority by remember { mutableStateOf("Medium") }
    var selectedDueDate by remember { mutableStateOf("Today") }
    var selectedTime by remember { mutableStateOf("None") }

    var isListMenuExpanded by remember { mutableStateOf(false) }
    var isPriorityMenuExpanded by remember { mutableStateOf(false) }
    var isDueDateMenuExpanded by remember { mutableStateOf(false) }
    var isTimeMenuExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(DarkBg)
                .border(1.dp, CardBorder, RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Column {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "New Task",
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        )
                    )
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { onDismiss() }
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Field 1: TASK TITLE
                Text(
                    text = "TASK TITLE",
                    style = TextStyle(
                        color = GoldenAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("What needs to be done?", color = Color.White.copy(alpha = 0.35f), fontSize = 13.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CardBg,
                        unfocusedContainerColor = CardBg,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = GoldenAccent,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f)
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Field 2: NOTE
                Text(
                    text = "NOTE",
                    style = TextStyle(
                        color = GoldenAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = { Text("Add details...", color = Color.White.copy(alpha = 0.35f), fontSize = 13.sp) },
                    minLines = 3,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CardBg,
                        unfocusedContainerColor = CardBg,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = GoldenAccent,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f)
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Grid Row 1: LIST & PRIORITY
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // LIST Selector
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "LIST",
                            style = TextStyle(
                                color = GoldenAccent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box {
                            DropdownSelectorBox(
                                value = selectedList,
                                onClick = { isListMenuExpanded = true }
                            )
                            DropdownMenu(
                                expanded = isListMenuExpanded,
                                onDismissRequest = { isListMenuExpanded = false }
                            ) {
                                (listOf("Personal", "Work", "Shopping", "Health") + customCategories).distinct().forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat) },
                                        onClick = {
                                            selectedList = cat
                                            isListMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // PRIORITY Selector
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "PRIORITY",
                            style = TextStyle(
                                color = GoldenAccent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box {
                            DropdownSelectorBox(
                                value = selectedPriority,
                                onClick = { isPriorityMenuExpanded = true }
                            )
                            DropdownMenu(
                                expanded = isPriorityMenuExpanded,
                                onDismissRequest = { isPriorityMenuExpanded = false }
                            ) {
                                listOf("Low", "Medium", "High").forEach { pri ->
                                    DropdownMenuItem(
                                        text = { Text(pri) },
                                        onClick = {
                                            selectedPriority = pri
                                            isPriorityMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Grid Row 2: DUE DATE & TIME
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // DUE DATE Selector
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "DUE DATE",
                            style = TextStyle(
                                color = GoldenAccent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box {
                            DropdownSelectorBox(
                                value = selectedDueDate,
                                onClick = { isDueDateMenuExpanded = true }
                            )
                            DropdownMenu(
                                expanded = isDueDateMenuExpanded,
                                onDismissRequest = { isDueDateMenuExpanded = false }
                            ) {
                                listOf("Today", "Tomorrow", "Next Week").forEach { dt ->
                                    DropdownMenuItem(
                                        text = { Text(dt) },
                                        onClick = {
                                            selectedDueDate = dt
                                            isDueDateMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // TIME Selector
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "TIME",
                            style = TextStyle(
                                color = GoldenAccent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box {
                            DropdownSelectorBox(
                                value = selectedTime,
                                onClick = { isTimeMenuExpanded = true }
                            )
                            DropdownMenu(
                                expanded = isTimeMenuExpanded,
                                onDismissRequest = { isTimeMenuExpanded = false }
                            ) {
                                listOf("None", "06:30", "09:00", "12:00", "18:00").forEach { tm ->
                                    DropdownMenuItem(
                                        text = { Text(tm) },
                                        onClick = {
                                            selectedTime = tm
                                            isTimeMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Golden Create Task Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (title.isNotBlank()) GoldenAccent else GoldenAccent.copy(alpha = 0.4f))
                        .clickable {
                            if (title.isNotBlank()) {
                                onSave(
                                    title.trim(),
                                    notes.trim(),
                                    selectedDueDate,
                                    if (selectedTime != "None") selectedTime else "",
                                    selectedList,
                                    selectedPriority,
                                    false
                                )
                            }
                        }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Create Task",
                        style = TextStyle(
                            color = DarkBg,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun DropdownSelectorBox(
    value: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardBg)
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value,
                style = TextStyle(
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// Modal for Manage Lists
@Composable
private fun ManageListsModalDialog(
    categories: List<String>,
    onDismiss: () -> Unit,
    onAddCategory: (String) -> Unit,
    onDeleteCategory: (String) -> Unit
) {
    var newCatName by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(DarkBg)
                .border(1.dp, CardBorder, RoundedCornerShape(22.dp))
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Manage Task Lists",
                        style = TextStyle(color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    )
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp).clickable { onDismiss() }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newCatName,
                        onValueChange = { newCatName = it },
                        placeholder = { Text("New List Name", color = Color.White.copy(0.4f), fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = GoldenAccent,
                            unfocusedBorderColor = CardBorder
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(GoldenAccent)
                            .clickable {
                                if (newCatName.isNotBlank()) {
                                    onAddCategory(newCatName.trim())
                                    newCatName = ""
                                }
                            }
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Text("Add", style = TextStyle(color = DarkBg, fontWeight = FontWeight.Bold, fontSize = 12.sp))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    categories.forEach { cat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(CardBg)
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(cat, style = TextStyle(color = Color.White, fontSize = 13.sp))
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Delete",
                                tint = Color.Red.copy(0.7f),
                                modifier = Modifier.size(16.dp).clickable { onDeleteCategory(cat) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// Modal for Task Statistics
@Composable
private fun TaskStatsModalDialog(
    totalTasks: Int,
    completedTasks: Int,
    starredTasks: Int,
    syncPercent: Int,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(DarkBg)
                .border(1.dp, CardBorder, RoundedCornerShape(22.dp))
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Task Analytics", style = TextStyle(color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold))
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(18.dp).clickable { onDismiss() })
                }
                Spacer(modifier = Modifier.height(16.dp))

                StatRow("Total Tasks", "$totalTasks")
                StatRow("Completed Tasks", "$completedTasks")
                StatRow("Starred / Favorites", "$starredTasks")
                StatRow("Neural Sync Rate", "$syncPercent%")
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = TextStyle(color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp))
        Text(value, style = TextStyle(color = GoldenAccent, fontSize = 14.sp, fontWeight = FontWeight.Bold))
    }
}

// Modal for Know Sifra Daily Task
@Composable
private fun KnowSifraModalDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(DarkBg)
                .border(1.dp, CardBorder, RoundedCornerShape(22.dp))
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("About SIFRA Daily Task", style = TextStyle(color = GoldenAccent, fontSize = 17.sp, fontWeight = FontWeight.Bold))
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(18.dp).clickable { onDismiss() })
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "SIFRA Daily Task is an autonomous neural memory task manager designed for Maya Connect.\n\nIt synchronizes daily priorities, schedules, and reminders in real-time with instant local & cloud backup.",
                    style = TextStyle(color = Color.White.copy(0.8f), fontSize = 12.5.sp, lineHeight = 18.sp)
                )
            }
        }
    }
}
