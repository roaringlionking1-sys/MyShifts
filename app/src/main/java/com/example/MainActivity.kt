package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.EmployeeMobileTab
import com.example.model.WorkspaceMode
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.viewmodel.ShiftFlowUiState
import com.example.viewmodel.ShiftFlowViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                ShiftFlowApp()
            }
        }
    }
}

@Composable
fun ShiftFlowApp(viewModel: ShiftFlowViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // BackHandler for non-root tabs or Manager Web Grid mode
    if (uiState.workspaceMode == WorkspaceMode.MANAGER_WEB) {
        BackHandler {
            viewModel.setWorkspaceMode(WorkspaceMode.EMPLOYEE_MOBILE)
        }
    } else if (uiState.activeMobileTab != EmployeeMobileTab.SCHEDULE) {
        BackHandler {
            viewModel.selectMobileTab(EmployeeMobileTab.SCHEDULE)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = WarmCreamBg,
        topBar = {
            ShiftFlowTopSwitcherBar(
                uiState = uiState,
                onSwitchMode = viewModel::setWorkspaceMode,
                onSelectLocation = viewModel::selectLocation
            )
        },
        bottomBar = {
            if (uiState.workspaceMode == WorkspaceMode.EMPLOYEE_MOBILE &&
                uiState.showCallOutScreenForShift == null &&
                !uiState.showFindCoverageScreen
            ) {
                EmployeeBottomNavigationBar(
                    activeTab = uiState.activeMobileTab,
                    onSelectTab = viewModel::selectMobileTab
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Interactive Status Feedback Banner
            AnimatedVisibility(visible = uiState.statusBannerMessage != null) {
                val msg = uiState.statusBannerMessage ?: ""
                Surface(
                    color = DeepNavy,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = CoralPeach,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                        }
                        IconButton(
                            onClick = viewModel::clearBanner,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss notification",
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Main Content Router: Employee Mobile App vs Manager Web-Grid Portal
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (uiState.workspaceMode) {
                    WorkspaceMode.EMPLOYEE_MOBILE -> {
                        when (uiState.activeMobileTab) {
                            EmployeeMobileTab.SCHEDULE -> {
                                EmployeeScheduleScreen(
                                    uiState = uiState,
                                    onSelectSubTab = viewModel::selectScheduleSubTab,
                                    onSelectDayIndex = viewModel::selectDayStripIndex,
                                    onCallOutClick = viewModel::openCallOutScreen,
                                    onCloseCallOut = viewModel::closeCallOutScreen,
                                    onConfirmCallOut = viewModel::confirmCallOut,
                                    onOpenSwapModal = viewModel::openSwapModal,
                                    onExecuteShiftAction = viewModel::executeShiftAction,
                                    onClaimOpenShift = viewModel::claimOpenShift,
                                    onOpenFindCoverage = {
                                        viewModel.selectMobileTab(EmployeeMobileTab.ALERTS)
                                        viewModel.openFindCoverageScreen(true)
                                    }
                                )
                            }
                            EmployeeMobileTab.AVAILABILITY -> {
                                EmployeeAvailabilityScreen(
                                    uiState = uiState,
                                    onSelectSlot = viewModel::selectAvailabilitySlot,
                                    onCopyLastWeek = viewModel::copyLastWeekAvailability,
                                    onClearAll = viewModel::clearAllAvailability,
                                    onToggleRecurring = viewModel::toggleRecurringAvailabilityMode,
                                    onSubmitAvailability = viewModel::submitAvailability,
                                    onAddTimeOffRequest = viewModel::addTimeOffRequest
                                )
                            }
                            EmployeeMobileTab.ALERTS -> {
                                EmployeeAlertsAndCoverScreen(
                                    uiState = uiState,
                                    onRespondUrgentAlert = viewModel::respondToUrgentCoverAlert,
                                    onOpenFindCoverage = viewModel::openFindCoverageScreen,
                                    onAssignCandidate = viewModel::assignCoverageCandidate,
                                    onSendCoverToNext3 = viewModel::sendCoverRequestToNext3,
                                    onCallCandidate = {}
                                )
                            }
                            EmployeeMobileTab.WORK_CHAT -> {
                                WorkChatScreen(
                                    uiState = uiState,
                                    onSendMessage = viewModel::sendChatMessage
                                )
                            }
                            EmployeeMobileTab.PROFILE -> {
                                ProfileAndNotificationsScreen(
                                    uiState = uiState,
                                    onToggleChannel = viewModel::toggleNotificationChannel,
                                    onToggleQuietHours = viewModel::toggleQuietHours
                                )
                            }
                        }
                    }

                    WorkspaceMode.MANAGER_WEB -> {
                        ManagerWebPortalScreen(
                            uiState = uiState,
                            onSelectSection = viewModel::selectManagerSection,
                            onRequestAvailability = viewModel::requestMissingAvailability,
                            onAutoFillFromAvailability = viewModel::autoFillRosterFromAvailability,
                            onPublishRoster = viewModel::publishRoster,
                            onAssignShiftCell = viewModel::assignShiftFromRosterCell,
                            onRemoveShiftCell = viewModel::removeShiftFromRosterCell,
                            onSelectJobSiteFilter = viewModel::selectJobSiteFilter,
                            onAssignCandidate = viewModel::assignCoverageCandidate,
                            onSendCoverToNext3 = viewModel::sendCoverRequestToNext3,
                            onUpdateEmployeeSearch = viewModel::updateEmployeeSearch,
                            onUpdateRoleFilter = viewModel::updateRoleFilter,
                            onUpdateStatusFilter = viewModel::updateStatusFilter,
                            onToggleRolesPermissions = viewModel::toggleRolesPermissionsScreen,
                            onInviteEmployee = viewModel::inviteEmployee,
                            onAddCustomSkill = viewModel::addCustomSkill,
                            onApplyScheduleTemplate = viewModel::applyScheduleTemplate,
                            onUpdateTimeOffStatus = viewModel::updateTimeOffStatus,
                            onUpdateWeekStartDay = viewModel::updateWeekStartDay,
                            onUpdateAutomation = viewModel::updateStoreAutomation
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShiftFlowTopSwitcherBar(
    uiState: ShiftFlowUiState,
    onSwitchMode: (WorkspaceMode) -> Unit,
    onSelectLocation: (String) -> Unit
) {
    var showLocationMenu by remember { mutableStateOf(false) }

    Surface(
        color = DeepNavy,
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.statusBars)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Brand + Multi-Location Selector
            Box {
                Surface(
                    onClick = { showLocationMenu = true },
                    shape = RoundedCornerShape(12.dp),
                    color = SlateNavy,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                    modifier = Modifier.testTag("location_selector_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = "Store Location",
                            tint = CoralPeach,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = "ShiftFlow",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = CoralPeach
                            )
                            Text(
                                text = uiState.currentLocation.shortName,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Switch store",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = showLocationMenu,
                    onDismissRequest = { showLocationMenu = false }
                ) {
                    uiState.locations.forEach { loc ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(loc.name, fontWeight = FontWeight.Bold)
                                    Text(loc.address, style = MaterialTheme.typography.bodySmall, color = TextSecondarySlate)
                                }
                            },
                            onClick = {
                                onSelectLocation(loc.id)
                                showLocationMenu = false
                            }
                        )
                    }
                }
            }

            // Right: Unified Employee App vs Manager Web-Grid Switcher
            Surface(
                shape = RoundedCornerShape(50),
                color = SlateNavy,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
            ) {
                Row(modifier = Modifier.padding(3.dp)) {
                    WorkspaceMode.entries.forEach { mode ->
                        val selected = uiState.workspaceMode == mode
                        Surface(
                            onClick = { onSwitchMode(mode) },
                            shape = RoundedCornerShape(50),
                            color = if (selected) CoralPeach else Color.Transparent,
                            modifier = Modifier.testTag("mode_switch_${mode.name.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = if (mode == WorkspaceMode.EMPLOYEE_MOBILE) {
                                        Icons.Default.PhoneAndroid
                                    } else {
                                        Icons.Default.GridView
                                    },
                                    contentDescription = mode.label,
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = mode.label,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
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
private fun EmployeeBottomNavigationBar(
    activeTab: EmployeeMobileTab,
    onSelectTab: (EmployeeMobileTab) -> Unit
) {
    NavigationBar(
        containerColor = WarmCardSurface,
        tonalElevation = 6.dp
    ) {
        EmployeeMobileTab.entries.forEach { tab ->
            val selected = activeTab == tab
            val icon = when (tab) {
                EmployeeMobileTab.SCHEDULE -> if (selected) Icons.Filled.CalendarToday else Icons.Outlined.CalendarToday
                EmployeeMobileTab.AVAILABILITY -> if (selected) Icons.Filled.Schedule else Icons.Outlined.Schedule
                EmployeeMobileTab.ALERTS -> if (selected) Icons.Filled.Notifications else Icons.Outlined.Notifications
                EmployeeMobileTab.WORK_CHAT -> if (selected) Icons.Filled.Forum else Icons.Outlined.Forum
                EmployeeMobileTab.PROFILE -> if (selected) Icons.Filled.Person else Icons.Outlined.Person
            }

            NavigationBarItem(
                selected = selected,
                onClick = { onSelectTab(tab) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = tab.label
                    )
                },
                label = {
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        )
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = CoralPeachDark,
                    selectedTextColor = CoralPeachDark,
                    indicatorColor = CoralPeachLight,
                    unselectedIconColor = TextSecondarySlate,
                    unselectedTextColor = TextSecondarySlate
                ),
                modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
            )
        }
    }
}
