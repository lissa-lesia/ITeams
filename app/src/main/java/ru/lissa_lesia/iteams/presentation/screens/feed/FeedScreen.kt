package ru.lissa_lesia.iteams.presentation.screens.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.NotificationAdd
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.lissa_lesia.iteams.domain.models.Project
import ru.lissa_lesia.iteams.domain.models.ProjectStatus
import ru.lissa_lesia.iteams.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    viewModel: FeedViewModel,
    onNavigateToCreateProject: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToApplications: () -> Unit,
    onLogout: () -> Unit,
    onProjectClick: (String) -> Unit,
    onUserClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filteredProjects = uiState.filteredProjects
    val focusManager = LocalFocusManager.current

    val topBarGradient = Brush.horizontalGradient(
        colors = listOf(PrimaryGradientStart, PrimaryGradientEnd)
    )

    var showFilterDialog by remember { mutableStateOf(false) }
    var showStatusDropdown by remember { mutableStateOf(false) }
    var showRoleDropdown by remember { mutableStateOf(false) }
    var showSkillDropdown by remember { mutableStateOf(false) }

    val allRoles = uiState.projects.flatMap { it.requiredRoles }.distinct().sorted()
    val allSkills = uiState.projects.flatMap { it.requiredSkills }.distinct().sorted()

    LaunchedEffect(Unit) {
        viewModel.loadProjects()
    }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Лента проектов",
                        color = TextOnPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier
                    .background(brush = topBarGradient)
                    .shadow(4.dp),
                actions = {
                    IconButton(onClick = onNavigateToApplications) {
                        Icon(
                            Icons.Default.NotificationAdd,
                            contentDescription = "Заявки",
                            tint = TextOnPrimary
                        )
                    }
                    IconButton(
                        onClick = { viewModel.loadProjects() },
                        enabled = !uiState.isLoading
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Обновить",
                            tint = TextOnPrimary
                        )
                    }
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Профиль",
                            tint = TextOnPrimary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreateProject,
                containerColor = ActionPrimary,
                contentColor = TextOnPrimary,
                shape = CircleShape,
                modifier = Modifier.size(60.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Создать проект")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Поле поиска и кнопка фильтров
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = viewModel::updateSearchQuery,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Поиск проектов...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Поиск")
                        },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { viewModel.updateSearchQuery("") },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = "Очистить",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CardBackground,
                            unfocusedContainerColor = CardBackground,
                            focusedBorderColor = InputBorderFocused,
                            unfocusedBorderColor = InputBorderUnfocused,
                            focusedLabelColor = InputLabelFocused
                        )
                    )

                    IconButton(
                        onClick = { showFilterDialog = true },
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                if (uiState.selectedStatus != null ||
                                    uiState.selectedRole.isNotEmpty() ||
                                    uiState.selectedSkill.isNotEmpty()
                                ) ActionPrimary.copy(alpha = 0.1f)
                                else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = "Фильтры",
                            tint = if (uiState.selectedStatus != null ||
                                uiState.selectedRole.isNotEmpty() ||
                                uiState.selectedSkill.isNotEmpty()
                            ) ActionPrimary
                            else TextMuted
                        )
                    }
                }

                TabRow(
                    selectedTabIndex = uiState.selectedTab,
                    containerColor = TabContainer,
                    contentColor = ActionPrimary
                ) {
                    Tab(
                        selected = uiState.selectedTab == 0,
                        onClick = { viewModel.selectTab(0) },
                        text = {
                            Text(
                                "Все проекты",
                                color = if (uiState.selectedTab == 0) ActionPrimary else TextMuted
                            )
                        }
                    )
                    Tab(
                        selected = uiState.selectedTab == 1,
                        onClick = { viewModel.selectTab(1) },
                        text = {
                            Text(
                                "Мои проекты",
                                color = if (uiState.selectedTab == 1) ActionPrimary else TextMuted
                            )
                        }
                    )
                }

                when {
                    uiState.isLoading && uiState.projects.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = ActionPrimary)
                        }
                    }
                    uiState.errorMessage != null -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Ошибка: ${uiState.errorMessage}",
                                color = ErrorText,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { viewModel.loadProjects() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ActionPrimary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Повторить", color = TextOnPrimary)
                            }
                        }
                    }
                    filteredProjects.isEmpty() && !uiState.isLoading -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (uiState.searchQuery.isNotEmpty() ||
                                    uiState.selectedStatus != null ||
                                    uiState.selectedRole.isNotEmpty() ||
                                    uiState.selectedSkill.isNotEmpty()
                                ) "Ничего не найдено по вашему запросу"
                                else if (uiState.selectedTab == 0) "Пока нет проектов"
                                else "Вы пока не участвуете в проектах",
                                fontSize = 18.sp,
                                color = TextLight
                            )
                            if (uiState.selectedTab == 0 &&
                                uiState.searchQuery.isEmpty() &&
                                uiState.selectedStatus == null &&
                                uiState.selectedRole.isEmpty() &&
                                uiState.selectedSkill.isEmpty()
                            ) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = onNavigateToCreateProject,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ActionPrimary
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Создать первый проект", color = TextOnPrimary)
                                }
                            }
                            if (uiState.searchQuery.isNotEmpty() ||
                                uiState.selectedStatus != null ||
                                uiState.selectedRole.isNotEmpty() ||
                                uiState.selectedSkill.isNotEmpty()
                            ) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { viewModel.clearFilters() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ActionPrimary
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Очистить фильтры", color = TextOnPrimary)
                                }
                            }
                        }
                    }
                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(filteredProjects) { project ->
                                ProjectCard(
                                    project = project,
                                    onProjectClick = { onProjectClick(project.id) },
                                    onUserClick = onUserClick
                                )
                            }
                        }
                    }
                }

                if (uiState.isLoading && uiState.projects.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(36.dp),
                            color = ActionPrimary,
                            strokeWidth = 3.dp
                        )
                    }
                }
            }

            if (showFilterDialog) {
                AlertDialog(
                    onDismissRequest = { showFilterDialog = false },
                    title = { Text("Фильтры") },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Фильтр по статусу
                            Column {
                                Text(
                                    text = "Статус проекта",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Medium
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FilterChip(
                                        label = "Все",
                                        isSelected = uiState.selectedStatus == null,
                                        onClick = { viewModel.updateSelectedStatus(null) }
                                    )
                                    FilterChip(
                                        label = "Открыт",
                                        isSelected = uiState.selectedStatus == ProjectStatus.OPEN,
                                        onClick = { viewModel.updateSelectedStatus(ProjectStatus.OPEN) }
                                    )
                                    FilterChip(
                                        label = "Закрыт",
                                        isSelected = uiState.selectedStatus == ProjectStatus.CLOSED,
                                        onClick = { viewModel.updateSelectedStatus(ProjectStatus.CLOSED) }
                                    )
                                }
                            }

                            if (allRoles.isNotEmpty()) {
                                Column {
                                    Text(
                                        text = "Роль",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (uiState.selectedRole.isNotEmpty()) {
                                            FilterChip(
                                                label = uiState.selectedRole,
                                                isSelected = true,
                                                onClick = { viewModel.updateSelectedRole("") }
                                            )
                                        } else {
                                            Text(
                                                text = "Выберите роль",
                                                color = TextLight,
                                                fontSize = 14.sp
                                            )
                                        }
                                        IconButton(
                                            onClick = { showRoleDropdown = !showRoleDropdown },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.FilterList,
                                                contentDescription = "Выбрать роль",
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    DropdownMenu(
                                        expanded = showRoleDropdown,
                                        onDismissRequest = { showRoleDropdown = false }
                                    ) {
                                        allRoles.forEach { role ->
                                            DropdownMenuItem(
                                                text = { Text(role) },
                                                onClick = {
                                                    viewModel.updateSelectedRole(role)
                                                    showRoleDropdown = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            if (allSkills.isNotEmpty()) {
                                Column {
                                    Text(
                                        text = "Навык",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (uiState.selectedSkill.isNotEmpty()) {
                                            FilterChip(
                                                label = uiState.selectedSkill,
                                                isSelected = true,
                                                onClick = { viewModel.updateSelectedSkill("") }
                                            )
                                        } else {
                                            Text(
                                                text = "Выберите навык",
                                                color = TextLight,
                                                fontSize = 14.sp
                                            )
                                        }
                                        IconButton(
                                            onClick = { showSkillDropdown = !showSkillDropdown },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.FilterList,
                                                contentDescription = "Выбрать навык",
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    DropdownMenu(
                                        expanded = showSkillDropdown,
                                        onDismissRequest = { showSkillDropdown = false }
                                    ) {
                                        allSkills.forEach { skill ->
                                            DropdownMenuItem(
                                                text = { Text(skill) },
                                                onClick = {
                                                    viewModel.updateSelectedSkill(skill)
                                                    showSkillDropdown = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            if (uiState.selectedStatus != null ||
                                uiState.selectedRole.isNotEmpty() ||
                                uiState.selectedSkill.isNotEmpty()
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.clearFilters()
                                        showFilterDialog = false
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ActionDanger
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Сбросить все фильтры", color = TextOnPrimary)
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { showFilterDialog = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ActionPrimary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Применить", color = TextOnPrimary)
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = {
                                viewModel.clearFilters()
                                showFilterDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = TextMuted
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Сбросить")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.height(32.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) ActionPrimary else Color.Transparent,
            contentColor = if (isSelected) TextOnPrimary else TextPrimary,
            disabledContainerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (isSelected) 2.dp else 0.dp
        )
    ) {
        Text(label, fontSize = 12.sp)
    }
}

@Composable
fun ProjectCard(
    project: Project,
    onProjectClick: () -> Unit,
    onUserClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        onClick = onProjectClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = project.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    val statusColor = if (project.status == ProjectStatus.OPEN) StatusOpen else StatusClosed
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = if (project.status == ProjectStatus.OPEN) "Открыт" else "Закрыт",
                        fontSize = 12.sp,
                        color = statusColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = project.description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Автор: ",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextLight
                )
                Text(
                    text = project.authorName,
                    style = MaterialTheme.typography.bodySmall,
                    color = ActionPrimary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { onUserClick(project.authorId) }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (project.requiredSkills.isNotEmpty() || project.requiredRoles.isNotEmpty()) {
                Column {
                    if (project.requiredSkills.isNotEmpty()) {
                        Text(
                            text = "Навыки: ${project.requiredSkills.joinToString(", ")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (project.requiredRoles.isNotEmpty()) {
                        Text(
                            text = "Роли: ${project.requiredRoles.joinToString(", ")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            Text(
                text = "Создан: ${formatTimestamp(project.createdAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = TextVeryLight
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val format = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    return format.format(date)
}