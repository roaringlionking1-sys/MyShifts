package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*
import com.example.viewmodel.ShiftFlowUiState

/**
 * PDF Page 8 ("Employees") and PDF Page 9 ("Roles & permissions") + Custom Skills Manager
 */
@Composable
fun EmployeesAndRolesSection(
    uiState: ShiftFlowUiState,
    onUpdateSearch: (String) -> Unit,
    onUpdateRoleFilter: (String) -> Unit,
    onUpdateStatusFilter: (String) -> Unit,
    onToggleRolesPermissions: (Boolean) -> Unit,
    onInviteEmployee: (String, SystemRole, String, String, Boolean) -> Unit,
    onAddCustomSkill: (String, String) -> Unit
) {
    if (uiState.showRolesPermissionsSubScreen) {
        RolesAndPermissionsMatrixScreen(
            uiState = uiState,
            onBackToEmployees = { onToggleRolesPermissions(false) },
            onAddCustomSkill = onAddCustomSkill
        )
        return
    }

    var showInviteDialog by remember { mutableStateOf(false) }

    if (showInviteDialog) {
        InviteEmployeeDialog(
            skills = uiState.customSkills,
            onDismiss = { showInviteDialog = false },
            onInvite = { name, role, skill, contact, isMinor ->
                onInviteEmployee(name, role, skill, contact, isMinor)
                showInviteDialog = false
            }
        )
    }

    val filteredEmployees = uiState.employees.filter { emp ->
        val matchesQuery = uiState.employeeSearchQuery.isBlank() ||
            emp.shortName.contains(uiState.employeeSearchQuery, ignoreCase = true) ||
            emp.fullName.contains(uiState.employeeSearchQuery, ignoreCase = true) ||
            emp.primaryPosition.contains(uiState.employeeSearchQuery, ignoreCase = true)
        val matchesRole = uiState.selectedRoleFilter == "All roles" ||
            emp.role.label.equals(uiState.selectedRoleFilter, ignoreCase = true)
        val matchesStatus = uiState.selectedStatusFilter == "All statuses" ||
            emp.status.label.equals(uiState.selectedStatusFilter, ignoreCase = true)
        matchesQuery && matchesRole && matchesStatus
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header matching PDF Page 8: Employees · 15 people · 4 roles | Manage roles | + Invite employee
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Employees",
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 28.sp),
                        color = TextPrimaryNavy
                    )
                    Text(
                        text = "${uiState.employees.size} people · 4 roles · ${uiState.customSkills.size} custom skills",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondarySlate
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = { onToggleRolesPermissions(true) },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, WarmBorder),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = WarmCardSurface,
                            contentColor = TextPrimaryNavy
                        ),
                        modifier = Modifier.testTag("manage_roles_button")
                    ) {
                        Text("Manage roles", style = MaterialTheme.typography.titleSmall)
                    }

                    Button(
                        onClick = { showInviteDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CoralPeach),
                        modifier = Modifier.testTag("invite_employee_button")
                    ) {
                        Text(
                            text = "+ Invite employee",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Search & Filter Bar matching PDF Page 8
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = uiState.employeeSearchQuery,
                    onValueChange = onUpdateSearch,
                    placeholder = { Text("Search employees or custom skills...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondarySlate)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = WarmCardSurface,
                        unfocusedContainerColor = WarmCardSurface,
                        focusedBorderColor = CoralPeach,
                        unfocusedBorderColor = WarmBorder
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("employee_search_input")
                )

                val roles = listOf("All roles", "Owner", "Manager", "Assistant Manager", "Employee")
                val nextRole = roles[(roles.indexOf(uiState.selectedRoleFilter) + 1) % roles.size]
                OutlinedButton(
                    onClick = { onUpdateRoleFilter(nextRole) },
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, WarmBorder),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = WarmCardSurface,
                        contentColor = TextPrimaryNavy
                    ),
                    modifier = Modifier.height(52.dp)
                ) {
                    Text(uiState.selectedRoleFilter)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                }

                val statuses = listOf("All statuses", "Active", "Invited", "Inactive")
                val nextStatus = statuses[(statuses.indexOf(uiState.selectedStatusFilter) + 1) % statuses.size]
                OutlinedButton(
                    onClick = { onUpdateStatusFilter(nextStatus) },
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, WarmBorder),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = WarmCardSurface,
                        contentColor = TextPrimaryNavy
                    ),
                    modifier = Modifier.height(52.dp)
                ) {
                    Text(uiState.selectedStatusFilter)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                }
            }
        }

        // Employees Web Table matching PDF Page 8
        item {
            val tableScroll = rememberScrollState()
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = WarmCardSurface),
                border = BorderStroke(1.dp, WarmBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.horizontalScroll(tableScroll)) {
                    // Table Header: Name | Role | Position | Contact | Status | Hrs / wk
                    Row(
                        modifier = Modifier
                            .background(WarmSurfaceVariant)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Name", style = MaterialTheme.typography.labelLarge, color = TextSecondarySlate, modifier = Modifier.width(190.dp))
                        Text("Role", style = MaterialTheme.typography.labelLarge, color = TextSecondarySlate, modifier = Modifier.width(120.dp))
                        Text("Position / Custom Skills", style = MaterialTheme.typography.labelLarge, color = TextSecondarySlate, modifier = Modifier.width(180.dp))
                        Text("Contact", style = MaterialTheme.typography.labelLarge, color = TextSecondarySlate, modifier = Modifier.width(160.dp))
                        Text("Status", style = MaterialTheme.typography.labelLarge, color = TextSecondarySlate, modifier = Modifier.width(100.dp))
                        Text("Hrs / wk", style = MaterialTheme.typography.labelLarge, color = TextSecondarySlate, modifier = Modifier.width(80.dp))
                    }
                    HorizontalDivider(color = WarmBorder)

                    filteredEmployees.forEach { emp ->
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar + Name + Minor Warning
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.width(190.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Color(emp.avatarBgHex))
                                ) {
                                    Text(
                                        text = emp.initials,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = DeepNavy
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = emp.shortName,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = TextPrimaryNavy
                                    )
                                    if (emp.isMinor) {
                                        Text(
                                            text = "Minor · hour limits apply",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = AmberText
                                        )
                                    }
                                }
                            }

                            // Role Badge (Owner / Manager / Employee)
                            Box(modifier = Modifier.width(120.dp)) {
                                val (roleBg, roleFg) = when (emp.role) {
                                    SystemRole.OWNER -> AmberBg to AmberText
                                    SystemRole.MANAGER -> InfoBlueBg to InfoBlueText
                                    SystemRole.ASSISTANT_MANAGER -> CoralPeachLight to CoralPeachDark
                                    SystemRole.EMPLOYEE -> WarmSurfaceVariant to TextPrimaryNavy
                                }
                                Surface(shape = RoundedCornerShape(50), color = roleBg) {
                                    Text(
                                        text = emp.role.label,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = roleFg,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            // Position / Custom Skills
                            Text(
                                text = emp.primaryPosition,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimaryNavy,
                                modifier = Modifier.width(180.dp)
                            )

                            // Contact
                            Text(
                                text = emp.contact,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondarySlate,
                                modifier = Modifier.width(160.dp)
                            )

                            // Status Badge (Active / Invited / Inactive)
                            Box(modifier = Modifier.width(100.dp)) {
                                val (stBg, stFg) = when (emp.status) {
                                    EmployeeStatus.ACTIVE -> EmeraldBg to EmeraldText
                                    EmployeeStatus.INVITED -> AmberBg to AmberText
                                    EmployeeStatus.INACTIVE -> UnavailableGrayBg to UnavailableGrayText
                                }
                                Surface(shape = RoundedCornerShape(50), color = stBg) {
                                    Text(
                                        text = emp.status.label,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = stFg,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            // Hrs / wk
                            Text(
                                text = if (emp.role == SystemRole.OWNER) "—" else "${emp.scheduledHours} h",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimaryNavy,
                                modifier = Modifier.width(80.dp)
                            )
                        }
                        HorizontalDivider(color = WarmBorder.copy(alpha = 0.6f))
                    }
                }
            }
        }
    }
}

/**
 * PDF Page 9: "< Employees / Roles & permissions" + Custom Skills Manager
 */
@Composable
fun RolesAndPermissionsMatrixScreen(
    uiState: ShiftFlowUiState,
    onBackToEmployees: () -> Unit,
    onAddCustomSkill: (String, String) -> Unit
) {
    BackHandler { onBackToEmployees() }

    var showAddSkillDialog by remember { mutableStateOf(false) }

    if (showAddSkillDialog) {
        var newSkillName by remember { mutableStateOf("") }
        var newSkillCategory by remember { mutableStateOf("Specialty") }
        AlertDialog(
            onDismissRequest = { showAddSkillDialog = false },
            title = { Text("Add Custom Role / Skill") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newSkillName,
                        onValueChange = { newSkillName = it },
                        label = { Text("Skill / Role Name (e.g., Waffle Maker, Cleaner)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newSkillCategory,
                        onValueChange = { newSkillCategory = it },
                        label = { Text("Department / Category") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAddCustomSkill(newSkillName, newSkillCategory)
                        showAddSkillDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralPeach)
                ) {
                    Text("Save Custom Skill")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSkillDialog = false }) { Text("Cancel") }
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { onBackToEmployees() }
                            .padding(vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Back to employees",
                            tint = TextSecondarySlate,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Employees",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondarySlate
                        )
                    }
                    Text(
                        text = "Roles & permissions",
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 28.sp),
                        color = TextPrimaryNavy
                    )
                }

                Button(
                    onClick = { showAddSkillDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CoralPeach),
                    modifier = Modifier.testTag("add_custom_role_button")
                ) {
                    Text(
                        text = "+ Add custom role",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        }

        // Top 3 Role Summary Cards matching PDF Page 9 (Owner, Manager, Employee)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = AmberBg,
                    modifier = Modifier.width(240.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Owner", style = MaterialTheme.typography.titleLarge, color = AmberText)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("1 person · full access to everything", style = MaterialTheme.typography.bodyMedium, color = AmberText)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = InfoBlueBg,
                    modifier = Modifier.width(260.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Manager / Asst. Mgr", style = MaterialTheme.typography.titleLarge, color = InfoBlueText)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("2 people · builds rosters, handles coverage", style = MaterialTheme.typography.bodyMedium, color = InfoBlueText)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = WarmSurfaceVariant,
                    modifier = Modifier.width(280.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Employee", style = MaterialTheme.typography.titleLarge, color = TextPrimaryNavy)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("12 people · submits availability, views own shifts", style = MaterialTheme.typography.bodyMedium, color = TextSecondarySlate)
                    }
                }
            }
        }

        // Custom Skills Chips (Crew, Cleaner, Manager, Assistant Manager, Owner, Ice Cream Maker, Cake Decorator, Scooper, etc.)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Custom Store & Restaurant Skills",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimaryNavy
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.customSkills.forEach { skill ->
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color(skill.bgHex),
                            border = BorderStroke(1.dp, WarmBorder)
                        ) {
                            Text(
                                text = "${skill.name} (${skill.activeStaffCount})",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = Color(skill.colorHex),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }
        }

        // Permissions Matrix Table matching PDF Page 9
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = WarmCardSurface),
                border = BorderStroke(1.dp, WarmBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(WarmSurfaceVariant)
                            .padding(horizontal = 18.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Permission", style = MaterialTheme.typography.titleSmall, color = TextPrimaryNavy, modifier = Modifier.weight(2f))
                        Text("Owner", style = MaterialTheme.typography.titleSmall, color = AmberText, modifier = Modifier.weight(0.8f))
                        Text("Manager", style = MaterialTheme.typography.titleSmall, color = InfoBlueText, modifier = Modifier.weight(0.8f))
                        Text("Employee", style = MaterialTheme.typography.titleSmall, color = TextPrimaryNavy, modifier = Modifier.weight(0.8f))
                    }
                    HorizontalDivider(color = WarmBorder)

                    uiState.rolePermissions.forEach { perm ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = perm.label,
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextPrimaryNavy,
                                modifier = Modifier.weight(2f)
                            )
                            PermissionMark(allowed = perm.owner, modifier = Modifier.weight(0.8f))
                            PermissionMark(allowed = perm.manager, modifier = Modifier.weight(0.8f))
                            PermissionMark(allowed = perm.employee, modifier = Modifier.weight(0.8f))
                        }
                        HorizontalDivider(color = WarmBorder.copy(alpha = 0.6f))
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionMark(allowed: Boolean, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Text(
            text = if (allowed) "✓" else "×",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = if (allowed) EmeraldText else TextMuted
        )
    }
}

@Composable
fun ManagerAvailabilityAndTimeOffSection(
    uiState: ShiftFlowUiState,
    onRequestAvailability: () -> Unit,
    onUpdateTimeOffStatus: (String, RequestStatus) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Team Availability & Time Off",
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 26.sp),
                        color = TextPrimaryNavy
                    )
                    Text(
                        text = "${uiState.availabilitySubmittedCount} of 11 staff submitted · Automated rules active",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondarySlate
                    )
                }
                Button(
                    onClick = onRequestAvailability,
                    colors = ButtonDefaults.buttonColors(containerColor = CoralPeach)
                ) {
                    Text("Send Reminder")
                }
            }
        }

        items(uiState.timeOffRequests, key = { it.id }) { req ->
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = WarmCardSurface),
                border = BorderStroke(1.dp, WarmBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${req.employeeName} · ${req.typeLabel}",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimaryNavy
                        )
                        Text(
                            text = "${req.dateRange} — ${req.reason}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondarySlate
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (req.status == RequestStatus.PENDING) {
                            OutlinedButton(
                                onClick = { onUpdateTimeOffStatus(req.id, RequestStatus.DECLINED) }
                            ) {
                                Text("Decline")
                            }
                            Button(
                                onClick = { onUpdateTimeOffStatus(req.id, RequestStatus.APPROVED) },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldText)
                            ) {
                                Text("Approve")
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (req.status == RequestStatus.APPROVED) EmeraldBg else RoseBg
                            ) {
                                Text(
                                    text = req.status.label,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (req.status == RequestStatus.APPROVED) EmeraldText else RoseText,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TemplatesAndJobSitesSection(
    uiState: ShiftFlowUiState,
    onApplyScheduleTemplate: (ScheduleTemplate) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Schedule Templates, Shift Templates & Job Sites",
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 26.sp),
                color = TextPrimaryNavy
            )
            Text(
                text = "Apply reusable weekly rosters or standard shift blocks across store locations.",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondarySlate
            )
        }

        item {
            Text("Weekly Schedule Templates", style = MaterialTheme.typography.headlineMedium, color = TextPrimaryNavy)
        }

        items(uiState.scheduleTemplates, key = { it.id }) { schedTpl ->
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = WarmCardSurface),
                border = BorderStroke(1.dp, WarmBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(schedTpl.name, style = MaterialTheme.typography.titleLarge, color = TextPrimaryNavy)
                        Text(schedTpl.subtitle, style = MaterialTheme.typography.bodyMedium, color = TextSecondarySlate)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            schedTpl.tags.forEach { tag ->
                                Surface(shape = RoundedCornerShape(50), color = CoralPeachLight) {
                                    Text(
                                        text = tag,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = CoralPeachDark,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = { onApplyScheduleTemplate(schedTpl) },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepNavy)
                    ) {
                        Text("Apply Template")
                    }
                }
            }
        }

        item {
            Text("Standard Shift Templates & Job Sites", style = MaterialTheme.typography.headlineMedium, color = TextPrimaryNavy)
        }

        items(uiState.shiftTemplates, key = { it.id }) { tpl ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = WarmCardSurface),
                border = BorderStroke(1.dp, WarmBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(tpl.title, style = MaterialTheme.typography.titleMedium, color = TextPrimaryNavy)
                        Text("${tpl.timeRange} · Skill: ${tpl.requiredSkill} · Job Site: ${tpl.jobSite}", style = MaterialTheme.typography.bodyMedium, color = TextSecondarySlate)
                    }
                    Text("${tpl.durationHours} hrs", style = MaterialTheme.typography.titleMedium, color = CoralPeachDark)
                }
            }
        }
    }
}

@Composable
fun ScheduleReportsSection(uiState: ShiftFlowUiState) {
    val totalScheduledHours = uiState.shifts.sumOf { it.durationHours }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Schedule & Labor Reports",
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 26.sp),
                color = TextPrimaryNavy
            )
            Text(
                text = "Weekly labor hours, custom skill distribution, and minor compliance metrics.",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondarySlate
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ReportMetricCard(
                    title = "Total Scheduled",
                    value = "$totalScheduledHours hrs",
                    subtitle = "Across 3 locations",
                    modifier = Modifier.weight(1f)
                )
                ReportMetricCard(
                    title = "Minor Compliance",
                    value = "100% Safe",
                    subtitle = "Ava K. at 13h / 16h cap",
                    modifier = Modifier.weight(1f)
                )
                ReportMetricCard(
                    title = "Coverage Rate",
                    value = "96.4%",
                    subtitle = "Auto-ranker active",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = WarmCardSurface),
                border = BorderStroke(1.dp, WarmBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Hours by Custom Skill / Role", style = MaterialTheme.typography.titleLarge, color = TextPrimaryNavy)
                    uiState.customSkills.take(7).forEach { skill ->
                        val hoursForSkill = (skill.activeStaffCount * 8).coerceAtMost(64)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(skill.name, style = MaterialTheme.typography.titleSmall, color = TextPrimaryNavy)
                                Text("$hoursForSkill hrs · ${skill.activeStaffCount} staff", style = MaterialTheme.typography.bodyMedium, color = TextSecondarySlate)
                            }
                            LinearProgressIndicator(
                                progress = { (hoursForSkill / 64f).coerceIn(0.15f, 1f) },
                                color = CoralPeach,
                                trackColor = WarmSurfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportMetricCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = WarmCardSurface),
        border = BorderStroke(1.dp, WarmBorder),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = TextSecondarySlate)
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, color = CoralPeachDark)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = TextSecondarySlate)
        }
    }
}

@Composable
fun ManagerSettingsSection(
    uiState: ShiftFlowUiState,
    onUpdateWeekStartDay: (WeekStartDay) -> Unit,
    onUpdateAutomation: (Boolean, Boolean, Int) -> Unit
) {
    val loc = uiState.currentLocation
    var autoSwaps by remember(loc) { mutableStateOf(loc.autoApproveQualifiedSwaps) }
    var autoTimeOff by remember(loc) { mutableStateOf(loc.autoApproveTimeOffUnder24h) }
    var reminderMins by remember(loc) { mutableIntStateOf(loc.shiftReminderMinutesBefore) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Store Scheduling & Automation Settings",
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 26.sp),
                color = TextPrimaryNavy
            )
            Text(
                text = "Configure flexible week start day, automated swap approvals, and shift reminders for ${loc.name}.",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondarySlate
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = WarmCardSurface),
                border = BorderStroke(1.dp, WarmBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Flexible Week Starting Day", style = MaterialTheme.typography.titleLarge, color = TextPrimaryNavy)
                    Text("Reorders the Weekly Roster Grid and employee availability sheets.", style = MaterialTheme.typography.bodyMedium, color = TextSecondarySlate)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        WeekStartDay.entries.forEach { day ->
                            FilterChip(
                                selected = loc.weekStartDay == day,
                                onClick = { onUpdateWeekStartDay(day) },
                                label = { Text(day.label) }
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = WarmCardSurface),
                border = BorderStroke(1.dp, WarmBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("Automated Shift Swaps, Open Shifts & Reminders", style = MaterialTheme.typography.titleLarge, color = TextPrimaryNavy)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Auto-approve qualified shift swaps & open pickups", style = MaterialTheme.typography.titleMedium)
                            Text("Instantly approves trades when skill matches and weekly hours stay under 40h (16h for minors).", style = MaterialTheme.typography.bodyMedium, color = TextSecondarySlate)
                        }
                        Switch(
                            checked = autoSwaps,
                            onCheckedChange = {
                                autoSwaps = it
                                onUpdateAutomation(autoSwaps, autoTimeOff, reminderMins)
                            }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Auto-process time-off requests before roster lock", style = MaterialTheme.typography.titleMedium)
                            Text("Approves time-off and marks cells Unavailable when submitted before Monday 11:59 PM.", style = MaterialTheme.typography.bodyMedium, color = TextSecondarySlate)
                        }
                        Switch(
                            checked = autoTimeOff,
                            onCheckedChange = {
                                autoTimeOff = it
                                onUpdateAutomation(autoSwaps, autoTimeOff, reminderMins)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InviteEmployeeDialog(
    skills: List<CustomSkill>,
    onDismiss: () -> Unit,
    onInvite: (String, SystemRole, String, String, Boolean) -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(SystemRole.EMPLOYEE) }
    var selectedSkill by remember { mutableStateOf(skills.firstOrNull()?.name ?: "Scooper") }
    var isMinor by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Invite Employee to Store") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = contact,
                    onValueChange = { contact = it },
                    label = { Text("Phone or Email") },
                    singleLine = true
                )
                Text("Primary Custom Skill:", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    skills.forEach { sk ->
                        FilterChip(
                            selected = selectedSkill == sk.name,
                            onClick = { selectedSkill = sk.name },
                            label = { Text(sk.name) }
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isMinor, onCheckedChange = { isMinor = it })
                    Text("Minor employee (16h weekly limit alert)")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onInvite(fullName, selectedRole, selectedSkill, contact, isMinor) },
                colors = ButtonDefaults.buttonColors(containerColor = CoralPeach)
            ) {
                Text("Send Invite")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
