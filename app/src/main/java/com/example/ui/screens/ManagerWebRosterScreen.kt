package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*
import com.example.viewmodel.ShiftFlowUiState

/**
 * Manager Web-Page Portal & Weekly Roster Builder Grid
 * Faithfully implements PDF Page 7 ("Roster · Nov 16 – 22") and Desktop ShiftFlow Mockup
 */
@Composable
fun ManagerWebPortalScreen(
    uiState: ShiftFlowUiState,
    onSelectSection: (ManagerWebSection) -> Unit,
    onRequestAvailability: () -> Unit,
    onAutoFillFromAvailability: () -> Unit,
    onPublishRoster: () -> Unit,
    onAssignShiftCell: (Employee, Int, ShiftTemplate) -> Unit,
    onRemoveShiftCell: (String) -> Unit,
    onSelectJobSiteFilter: (String) -> Unit,
    onAssignCandidate: (String) -> Unit,
    onSendCoverToNext3: () -> Unit,
    onUpdateEmployeeSearch: (String) -> Unit,
    onUpdateRoleFilter: (String) -> Unit,
    onUpdateStatusFilter: (String) -> Unit,
    onToggleRolesPermissions: (Boolean) -> Unit,
    onInviteEmployee: (String, SystemRole, String, String, Boolean) -> Unit,
    onAddCustomSkill: (String, String) -> Unit,
    onApplyScheduleTemplate: (ScheduleTemplate) -> Unit,
    onUpdateTimeOffStatus: (String, RequestStatus) -> Unit,
    onUpdateWeekStartDay: (WeekStartDay) -> Unit,
    onUpdateAutomation: (Boolean, Boolean, Int) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmCreamBg)
    ) {
        val isWideScreen = maxWidth >= 700.dp

        if (isWideScreen) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Left Web Sidebar Navigation matching PDF Pages 7, 8, 9
                ManagerWebSidebar(
                    shopName = uiState.currentLocation.name,
                    subLabel = "${uiState.currentLocation.shortName} · Owner",
                    activeSection = uiState.activeManagerSection,
                    onSelectSection = onSelectSection,
                    modifier = Modifier
                        .width(220.dp)
                        .fillMaxHeight()
                )
                VerticalDivider(color = WarmBorder)
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    ManagerSectionRouter(
                        uiState = uiState,
                        onSelectSection = onSelectSection,
                        onRequestAvailability = onRequestAvailability,
                        onAutoFillFromAvailability = onAutoFillFromAvailability,
                        onPublishRoster = onPublishRoster,
                        onAssignShiftCell = onAssignShiftCell,
                        onRemoveShiftCell = onRemoveShiftCell,
                        onSelectJobSiteFilter = onSelectJobSiteFilter,
                        onAssignCandidate = onAssignCandidate,
                        onSendCoverToNext3 = onSendCoverToNext3,
                        onUpdateEmployeeSearch = onUpdateEmployeeSearch,
                        onUpdateRoleFilter = onUpdateRoleFilter,
                        onUpdateStatusFilter = onUpdateStatusFilter,
                        onToggleRolesPermissions = onToggleRolesPermissions,
                        onInviteEmployee = onInviteEmployee,
                        onAddCustomSkill = onAddCustomSkill,
                        onApplyScheduleTemplate = onApplyScheduleTemplate,
                        onUpdateTimeOffStatus = onUpdateTimeOffStatus,
                        onUpdateWeekStartDay = onUpdateWeekStartDay,
                        onUpdateAutomation = onUpdateAutomation
                    )
                }
            }
        } else {
            // Compact screen: Web Section Ribbon at top + full scrollable Web Grid below
            Column(modifier = Modifier.fillMaxSize()) {
                ManagerWebTopRibbon(
                    activeSection = uiState.activeManagerSection,
                    onSelectSection = onSelectSection
                )
                HorizontalDivider(color = WarmBorder)
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    ManagerSectionRouter(
                        uiState = uiState,
                        onSelectSection = onSelectSection,
                        onRequestAvailability = onRequestAvailability,
                        onAutoFillFromAvailability = onAutoFillFromAvailability,
                        onPublishRoster = onPublishRoster,
                        onAssignShiftCell = onAssignShiftCell,
                        onRemoveShiftCell = onRemoveShiftCell,
                        onSelectJobSiteFilter = onSelectJobSiteFilter,
                        onAssignCandidate = onAssignCandidate,
                        onSendCoverToNext3 = onSendCoverToNext3,
                        onUpdateEmployeeSearch = onUpdateEmployeeSearch,
                        onUpdateRoleFilter = onUpdateRoleFilter,
                        onUpdateStatusFilter = onUpdateStatusFilter,
                        onToggleRolesPermissions = onToggleRolesPermissions,
                        onInviteEmployee = onInviteEmployee,
                        onAddCustomSkill = onAddCustomSkill,
                        onApplyScheduleTemplate = onApplyScheduleTemplate,
                        onUpdateTimeOffStatus = onUpdateTimeOffStatus,
                        onUpdateWeekStartDay = onUpdateWeekStartDay,
                        onUpdateAutomation = onUpdateAutomation
                    )
                }
            }
        }
    }
}

@Composable
private fun ManagerWebSidebar(
    shopName: String,
    subLabel: String,
    activeSection: ManagerWebSection,
    onSelectSection: (ManagerWebSection) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(WarmCreamBg)
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = shopName.uppercase(),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            ),
            color = TextPrimaryNavy
        )
        Text(
            text = subLabel,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondarySlate
        )
        Spacer(modifier = Modifier.height(14.dp))

        ManagerWebSection.entries.forEach { section ->
            val selected = activeSection == section
            Surface(
                onClick = { onSelectSection(section) },
                shape = RoundedCornerShape(12.dp),
                color = if (selected) CoralPeachLight else Color.Transparent,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("web_nav_${section.name.lowercase()}")
            ) {
                Text(
                    text = section.label,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = if (selected) CoralPeachDark else TextPrimaryNavy,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp)
                )
            }
        }
    }
}

@Composable
private fun ManagerWebTopRibbon(
    activeSection: ManagerWebSection,
    onSelectSection: (ManagerWebSection) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(WarmCardSurface)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ManagerWebSection.entries.forEach { section ->
            val selected = activeSection == section
            Surface(
                onClick = { onSelectSection(section) },
                shape = RoundedCornerShape(10.dp),
                color = if (selected) CoralPeachLight else WarmSurfaceVariant,
                border = BorderStroke(
                    1.dp,
                    if (selected) CoralPeach else WarmBorder
                ),
                modifier = Modifier.testTag("web_ribbon_${section.name.lowercase()}")
            ) {
                Text(
                    text = section.label,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = if (selected) CoralPeachDark else TextPrimaryNavy,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun ManagerSectionRouter(
    uiState: ShiftFlowUiState,
    onSelectSection: (ManagerWebSection) -> Unit,
    onRequestAvailability: () -> Unit,
    onAutoFillFromAvailability: () -> Unit,
    onPublishRoster: () -> Unit,
    onAssignShiftCell: (Employee, Int, ShiftTemplate) -> Unit,
    onRemoveShiftCell: (String) -> Unit,
    onSelectJobSiteFilter: (String) -> Unit,
    onAssignCandidate: (String) -> Unit,
    onSendCoverToNext3: () -> Unit,
    onUpdateEmployeeSearch: (String) -> Unit,
    onUpdateRoleFilter: (String) -> Unit,
    onUpdateStatusFilter: (String) -> Unit,
    onToggleRolesPermissions: (Boolean) -> Unit,
    onInviteEmployee: (String, SystemRole, String, String, Boolean) -> Unit,
    onAddCustomSkill: (String, String) -> Unit,
    onApplyScheduleTemplate: (ScheduleTemplate) -> Unit,
    onUpdateTimeOffStatus: (String, RequestStatus) -> Unit,
    onUpdateWeekStartDay: (WeekStartDay) -> Unit,
    onUpdateAutomation: (Boolean, Boolean, Int) -> Unit
) {
    when (uiState.activeManagerSection) {
        ManagerWebSection.ROSTER -> {
            RosterBuilderWebGrid(
                uiState = uiState,
                onRequestAvailability = onRequestAvailability,
                onAutoFillFromAvailability = onAutoFillFromAvailability,
                onPublishRoster = onPublishRoster,
                onAssignShiftCell = onAssignShiftCell,
                onRemoveShiftCell = onRemoveShiftCell,
                onSelectJobSiteFilter = onSelectJobSiteFilter,
                onOpenCoverageSection = { onSelectSection(ManagerWebSection.COVERAGE) }
            )
        }
        ManagerWebSection.AVAILABILITY -> {
            ManagerAvailabilityAndTimeOffSection(
                uiState = uiState,
                onRequestAvailability = onRequestAvailability,
                onUpdateTimeOffStatus = onUpdateTimeOffStatus
            )
        }
        ManagerWebSection.COVERAGE -> {
            FindCoverageScreen(
                uiState = uiState,
                onBack = { onSelectSection(ManagerWebSection.ROSTER) },
                onAssignCandidate = onAssignCandidate,
                onSendCoverToNext3 = onSendCoverToNext3,
                onCallCandidate = {}
            )
        }
        ManagerWebSection.TEMPLATES -> {
            TemplatesAndJobSitesSection(
                uiState = uiState,
                onApplyScheduleTemplate = onApplyScheduleTemplate
            )
        }
        ManagerWebSection.EMPLOYEES_ROLES -> {
            EmployeesAndRolesSection(
                uiState = uiState,
                onUpdateSearch = onUpdateEmployeeSearch,
                onUpdateRoleFilter = onUpdateRoleFilter,
                onUpdateStatusFilter = onUpdateStatusFilter,
                onToggleRolesPermissions = onToggleRolesPermissions,
                onInviteEmployee = onInviteEmployee,
                onAddCustomSkill = onAddCustomSkill
            )
        }
        ManagerWebSection.REPORTS -> {
            ScheduleReportsSection(uiState = uiState)
        }
        ManagerWebSection.SETTINGS -> {
            ManagerSettingsSection(
                uiState = uiState,
                onUpdateWeekStartDay = onUpdateWeekStartDay,
                onUpdateAutomation = onUpdateAutomation
            )
        }
    }
}

/**
 * PDF Page 7: "Roster · Nov 16 – 22" Interactive Web Grid
 */
@Composable
private fun RosterBuilderWebGrid(
    uiState: ShiftFlowUiState,
    onRequestAvailability: () -> Unit,
    onAutoFillFromAvailability: () -> Unit,
    onPublishRoster: () -> Unit,
    onAssignShiftCell: (Employee, Int, ShiftTemplate) -> Unit,
    onRemoveShiftCell: (String) -> Unit,
    onSelectJobSiteFilter: (String) -> Unit,
    onOpenCoverageSection: () -> Unit
) {
    // Roster rows matching PDF Page 7 (Maria R., Jordan T., Priya S., Luis M., Ava K. (16), Noah B.)
    val rosterStaffIds = listOf("emp_maria", "emp_jordan", "emp_priya", "emp_luis", "emp_ava", "emp_noah")
    val rosterEmployees = uiState.employees.filter { it.id in rosterStaffIds }

    var selectedCellForAssignment by remember { mutableStateOf<Pair<Employee, Int>?>(null) }
    var selectedExistingShiftForEdit by remember { mutableStateOf<Pair<Employee, ShiftItem>?>(null) }

    val assignTarget = selectedCellForAssignment
    if (assignTarget != null) {
        RosterCellAssignDialog(
            employee = assignTarget.first,
            dayIndex = assignTarget.second,
            shiftTemplates = uiState.shiftTemplates,
            onDismiss = { selectedCellForAssignment = null },
            onSelectTemplate = { tpl ->
                onAssignShiftCell(assignTarget.first, assignTarget.second, tpl)
                selectedCellForAssignment = null
            }
        )
    }

    val editTarget = selectedExistingShiftForEdit
    if (editTarget != null) {
        RosterShiftEditDialog(
            employee = editTarget.first,
            shift = editTarget.second,
            shiftTemplates = uiState.shiftTemplates,
            onDismiss = { selectedExistingShiftForEdit = null },
            onReplaceWithTemplate = { tpl ->
                onAssignShiftCell(editTarget.first, editTarget.second.dayIndex, tpl)
                selectedExistingShiftForEdit = null
            },
            onDeleteShift = {
                onRemoveShiftCell(editTarget.second.id)
                selectedExistingShiftForEdit = null
            }
        )
    }

    val orderedShortDays = uiState.currentLocation.weekStartDay.shortDays
    val mainStShifts = uiState.shifts.filter { it.locationId == "main_st" && it.dayIndex in 0..6 }
    val totalGridHours = mainStShifts.filter { it.employeeId in rosterStaffIds }.sumOf { it.durationHours }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header & Action Bar matching PDF Page 7
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Roster · Nov 16 – 22",
                            style = MaterialTheme.typography.displayLarge.copy(fontSize = 28.sp),
                            color = TextPrimaryNavy
                        )
                        Text(
                            text = if (uiState.isRosterPublished) {
                                "Published · ${uiState.availabilitySubmittedCount} of 11 employees submitted availability"
                            } else {
                                "Draft · ${uiState.availabilitySubmittedCount} of 11 employees submitted availability"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondarySlate
                        )
                    }
                }

                // Action Buttons Row: Request availability | Auto-fill from availability | Publish roster
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onRequestAvailability,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, WarmBorder),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = WarmCardSurface,
                            contentColor = TextPrimaryNavy
                        ),
                        modifier = Modifier.testTag("roster_request_availability_btn")
                    ) {
                        Text(
                            text = "Request availability",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }

                    OutlinedButton(
                        onClick = onAutoFillFromAvailability,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, WarmBorder),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = WarmCardSurface,
                            contentColor = TextPrimaryNavy
                        ),
                        modifier = Modifier.testTag("roster_autofill_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = CoralPeach,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Auto-fill from availability",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }

                    Button(
                        onClick = onPublishRoster,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CoralPeach),
                        modifier = Modifier.testTag("roster_publish_btn")
                    ) {
                        Text(
                            text = if (uiState.isRosterPublished) "Published ✓" else "Publish roster",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                    // Job Site Filter Chips
                    val sites = listOf("All Job Sites") + uiState.currentLocation.jobSites
                    sites.forEach { site ->
                        FilterChip(
                            selected = uiState.selectedJobSiteFilter == site,
                            onClick = { onSelectJobSiteFilter(site) },
                            label = { Text(site) }
                        )
                    }
                }
            }
        }

        // Three Warning / Info Chips Bar matching PDF Page 7
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    onClick = onOpenCoverageSection,
                    shape = RoundedCornerShape(10.dp),
                    color = RoseBg
                ) {
                    Text(
                        text = "Fri evening needs 1 more scooper",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = RoseText,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = AmberBg
                ) {
                    Text(
                        text = "Ava K. (16) is near the minor-hour limit",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = AmberText,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }

                Surface(
                    onClick = onRequestAvailability,
                    shape = RoundedCornerShape(10.dp),
                    color = InfoBlueBg
                ) {
                    Text(
                        text = "${11 - uiState.availabilitySubmittedCount} employees haven't submitted availability",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = InfoBlueText,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
            }
        }

        // Full Interactive Web Roster Table matching PDF Page 7
        item {
            val horizontalGridScroll = rememberScrollState()
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = WarmCardSurface),
                border = BorderStroke(1.dp, WarmBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .horizontalScroll(horizontalGridScroll)
                        .padding(bottom = 4.dp)
                ) {
                    // Header Row: Employee | Mon 16 | Tue 17 | Wed 18 | Thu 19 | Fri 20 | Sat 21 | Sun 22 | Hours
                    Row(
                        modifier = Modifier
                            .background(WarmSurfaceVariant)
                            .padding(vertical = 12.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Employee",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimaryNavy,
                            modifier = Modifier.width(136.dp)
                        )
                        orderedShortDays.forEach { dayCol ->
                            Text(
                                text = dayCol,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimaryNavy,
                                modifier = Modifier
                                    .width(118.dp)
                                    .padding(horizontal = 4.dp)
                            )
                        }
                        Text(
                            text = "Hours",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimaryNavy,
                            modifier = Modifier.width(64.dp)
                        )
                    }

                    HorizontalDivider(color = WarmBorder)

                    // Employee Rows
                    rosterEmployees.forEach { employee ->
                        val empShifts = mainStShifts.filter { it.employeeId == employee.id }
                        val empTotalHours = empShifts.sumOf { it.durationHours }

                        Row(
                            modifier = Modifier
                                .padding(vertical = 10.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Employee Name & Role Column
                            Column(modifier = Modifier.width(136.dp)) {
                                Text(
                                    text = if (employee.isMinor) "${employee.shortName} (${employee.age})" else employee.shortName,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimaryNavy
                                )
                                Text(
                                    text = if (employee.isMinor) "Scooper · minor" else employee.primaryPosition.substringBefore(","),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondarySlate
                                )
                            }

                            // 7 Day Cells (0..6)
                            for (dayIdx in 0..6) {
                                val assignedShift = empShifts.firstOrNull { it.dayIndex == dayIdx }
                                val isAvailable = uiState.gridAvailability[employee.id]?.get(dayIdx) ?: true

                                Box(
                                    modifier = Modifier
                                        .width(118.dp)
                                        .height(64.dp)
                                        .padding(horizontal = 4.dp)
                                ) {
                                    when {
                                        assignedShift != null -> {
                                            // Assigned Shift Cell (Coral/Pink tint matching PDF Page 7)
                                            Surface(
                                                onClick = {
                                                    selectedExistingShiftForEdit = employee to assignedShift
                                                },
                                                shape = RoundedCornerShape(10.dp),
                                                color = ShiftPinkBg,
                                                border = BorderStroke(1.dp, RoseBorder.copy(alpha = 0.5f)),
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .testTag("cell_shift_${employee.id}_$dayIdx")
                                            ) {
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .padding(horizontal = 8.dp, vertical = 8.dp),
                                                    verticalArrangement = Arrangement.Center
                                                ) {
                                                    Text(
                                                        text = assignedShift.compactTime,
                                                        style = MaterialTheme.typography.labelMedium.copy(
                                                            fontWeight = FontWeight.Bold
                                                        ),
                                                        color = ShiftPinkText,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Text(
                                                        text = assignedShift.shiftLabel,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = ShiftPinkText.copy(alpha = 0.85f),
                                                        maxLines = 1
                                                    )
                                                }
                                            }
                                        }
                                        isAvailable -> {
                                            // "+ Available" Dashed/Bordered Green Cell matching PDF Page 7
                                            Surface(
                                                onClick = {
                                                    selectedCellForAssignment = employee to dayIdx
                                                },
                                                shape = RoundedCornerShape(10.dp),
                                                color = EmeraldBg.copy(alpha = 0.45f),
                                                border = BorderStroke(1.dp, EmeraldBorder),
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .testTag("cell_avail_${employee.id}_$dayIdx")
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = "+ Available",
                                                        style = MaterialTheme.typography.labelMedium.copy(
                                                            fontWeight = FontWeight.Bold
                                                        ),
                                                        color = EmeraldText
                                                    )
                                                }
                                            }
                                        }
                                        else -> {
                                            // "Unavailable" Gray Cell matching PDF Page 7
                                            Surface(
                                                onClick = {
                                                    selectedCellForAssignment = employee to dayIdx
                                                },
                                                shape = RoundedCornerShape(10.dp),
                                                color = UnavailableGrayBg,
                                                modifier = Modifier.fillMaxSize()
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = "Unavailable",
                                                        style = MaterialTheme.typography.labelMedium,
                                                        color = UnavailableGrayText
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Employee Total Weekly Hours Column
                            Box(
                                modifier = Modifier.width(64.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = "$empTotalHours h",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (employee.isMinor && empTotalHours >= 13) AmberText else TextPrimaryNavy
                                )
                            }
                        }
                        HorizontalDivider(color = WarmBorder.copy(alpha = 0.7f))
                    }

                    // Bottom Summary Row: Staff (AM / PM) | 1/1 | 1/2 | 1/1 | 1/1 | 1/2 short | 2/2 | 1/1 | 120 h
                    Row(
                        modifier = Modifier
                            .background(WarmSurfaceVariant.copy(alpha = 0.6f))
                            .padding(vertical = 12.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Staff (AM / PM)",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimaryNavy,
                            modifier = Modifier.width(136.dp)
                        )
                        val staffRatios = listOf("1 / 1", "1 / 2", "1 / 1", "1 / 1", "1 / 2 short", "2 / 2", "1 / 1")
                        staffRatios.forEachIndexed { idx, ratio ->
                            val isShort = ratio.contains("short") &&
                                mainStShifts.count { it.dayIndex == 4 } < 3
                            Text(
                                text = if (idx == 4 && !isShort) "1 / 3 full" else ratio,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = if (isShort) RoseText else TextPrimaryNavy,
                                modifier = Modifier
                                    .width(118.dp)
                                    .padding(horizontal = 6.dp)
                            )
                        }
                        Text(
                            text = "$totalGridHours h",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimaryNavy,
                            modifier = Modifier.width(64.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RosterCellAssignDialog(
    employee: Employee,
    dayIndex: Int,
    shiftTemplates: List<ShiftTemplate>,
    onDismiss: () -> Unit,
    onSelectTemplate: (ShiftTemplate) -> Unit
) {
    val dayNames = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Assign Shift to ${employee.shortName}",
                style = MaterialTheme.typography.headlineMedium
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "${dayNames.getOrElse(dayIndex) { "Day" }} · Skills: ${employee.skills.joinToString(", ")}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondarySlate
                )
                Spacer(modifier = Modifier.height(4.dp))
                shiftTemplates.forEach { tpl ->
                    Card(
                        onClick = { onSelectTemplate(tpl) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = WarmCreamBg),
                        border = BorderStroke(1.dp, WarmBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${tpl.title} (${tpl.compactTime})",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextPrimaryNavy
                                )
                                Text(
                                    text = "${tpl.requiredSkill} · ${tpl.jobSite}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondarySlate
                                )
                            }
                            Text(
                                text = "${tpl.durationHours}h",
                                style = MaterialTheme.typography.labelLarge,
                                color = CoralPeachDark
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun RosterShiftEditDialog(
    employee: Employee,
    shift: ShiftItem,
    shiftTemplates: List<ShiftTemplate>,
    onDismiss: () -> Unit,
    onReplaceWithTemplate: (ShiftTemplate) -> Unit,
    onDeleteShift: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "${employee.shortName} · ${shift.dayHeader}",
                style = MaterialTheme.typography.headlineMedium
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Current: ${shift.compactTime} (${shift.shiftLabel} · ${shift.skillRequired})",
                    style = MaterialTheme.typography.titleSmall,
                    color = CoralPeachDark
                )
                Text(
                    text = "Switch to another Shift Template:",
                    style = MaterialTheme.typography.labelLarge
                )
                shiftTemplates.take(4).forEach { tpl ->
                    OutlinedButton(
                        onClick = { onReplaceWithTemplate(tpl) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("${tpl.title} (${tpl.compactTime})")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDeleteShift,
                colors = ButtonDefaults.buttonColors(containerColor = RoseText)
            ) {
                Text("Remove Shift")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
