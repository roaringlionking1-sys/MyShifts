package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Info
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

@Composable
fun EmployeeScheduleScreen(
    uiState: ShiftFlowUiState,
    onSelectSubTab: (EmployeeScheduleSubTab) -> Unit,
    onSelectDayIndex: (Int) -> Unit,
    onCallOutClick: (ShiftItem) -> Unit,
    onCloseCallOut: () -> Unit,
    onConfirmCallOut: (ShiftItem, CallOutReason, String) -> Unit,
    onOpenSwapModal: (ShiftItem?) -> Unit,
    onExecuteShiftAction: (ShiftItem, String, String?) -> Unit,
    onClaimOpenShift: (String) -> Unit,
    onOpenFindCoverage: () -> Unit
) {
    // Secondary screen: Call Out Sick (PDF Page 3)
    val callOutShift = uiState.showCallOutScreenForShift
    if (callOutShift != null) {
        CallOutSickScreen(
            shift = callOutShift,
            onBack = onCloseCallOut,
            onConfirm = { reason, note ->
                onConfirmCallOut(callOutShift, reason, note)
            }
        )
        return
    }

    val swapShift = uiState.showSwapModalForShift
    if (swapShift != null) {
        ShiftSwapOfferDialog(
            shift = swapShift,
            onDismiss = { onOpenSwapModal(null) },
            onConfirmAction = { actionType, teammate ->
                onExecuteShiftAction(swapShift, actionType, teammate)
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmCreamBg)
    ) {
        // Sub-Tab Pill Bar: MY SHIFTS (PDF Page 2) | ALL SHIFTS (Green App Screenshots) | OPEN SHIFTS
        Surface(
            color = WarmCardSurface,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EmployeeScheduleSubTab.entries.forEach { tab ->
                    val selected = uiState.activeScheduleSubTab == tab
                    Surface(
                        onClick = { onSelectSubTab(tab) },
                        shape = RoundedCornerShape(50),
                        color = if (selected) DeepNavy else WarmSurfaceVariant,
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("subtab_${tab.name.lowercase()}")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = tab.label,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.4.sp
                                ),
                                color = if (selected) Color.White else TextSecondarySlate
                            )
                        }
                    }
                }
            }
        }

        when (uiState.activeScheduleSubTab) {
            EmployeeScheduleSubTab.MY_SHIFTS -> {
                MySchedulePdfPage2Content(
                    uiState = uiState,
                    onCallOutClick = onCallOutClick,
                    onRequestSwapClick = { onOpenSwapModal(it) },
                    onOpenFindCoverage = onOpenFindCoverage
                )
            }
            EmployeeScheduleSubTab.ALL_SHIFTS -> {
                AllShiftsStoreTimelineContent(
                    uiState = uiState,
                    onSelectDayIndex = onSelectDayIndex,
                    onShiftTap = { onOpenSwapModal(it) }
                )
            }
            EmployeeScheduleSubTab.OPEN_SHIFTS -> {
                OpenShiftsPoolContent(
                    uiState = uiState,
                    onClaimShift = onClaimOpenShift,
                    onOpenFindCoverage = onOpenFindCoverage
                )
            }
        }
    }
}

@Composable
private fun MySchedulePdfPage2Content(
    uiState: ShiftFlowUiState,
    onCallOutClick: (ShiftItem) -> Unit,
    onRequestSwapClick: (ShiftItem) -> Unit,
    onOpenFindCoverage: () -> Unit
) {
    val todayShift = uiState.shifts.find { it.id == "shift_today_maria" } ?: uiState.shifts.first()
    val myWeekShifts = uiState.shifts.filter {
        it.employeeId == uiState.activeEmployeeId && it.dayIndex >= 0
    }.sortedBy { it.dayIndex }
    val totalWeekHours = myWeekShifts.sumOf { it.durationHours }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Good morning, ${uiState.activeEmployee.shortName.substringBefore(" ")}",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondarySlate
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My schedule",
                    style = MaterialTheme.typography.displayLarge,
                    color = TextPrimaryNavy
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = CoralPeachLight,
                    border = BorderStroke(1.dp, CoralPeach.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Shift reminder active",
                            tint = CoralPeachDark,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "1h reminder on",
                            style = MaterialTheme.typography.labelSmall,
                            color = CoralPeachDark,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Hero Card: NEXT SHIFT · TODAY (Matches PDF Page 2 with Deep Navy + Coral Peach accents)
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = DeepNavy),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("next_shift_hero_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "NEXT SHIFT · TODAY",
                            style = MaterialTheme.typography.labelLarge.copy(
                                letterSpacing = 0.8.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = CoralPeach
                        )
                        val isCalledOut = todayShift.status == ShiftStatus.CALLED_OUT
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (isCalledOut) RoseBg else EmeraldBg
                        ) {
                            Text(
                                text = if (isCalledOut) "Called Out" else "Published",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isCalledOut) RoseText else EmeraldText,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "${todayShift.startTime} – ${todayShift.endTime}",
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 31.sp),
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${todayShift.dayHeader} · ${todayShift.skillRequired} · ${todayShift.locationName}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.82f)
                    )

                    if (todayShift.status == ShiftStatus.CALLED_OUT) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            onClick = onOpenFindCoverage,
                            shape = RoundedCornerShape(12.dp),
                            color = CoralPeach.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, CoralPeach)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Coverage search active · Tap to view ranked matches",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "View coverage matches",
                                    tint = CoralPeach
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { onCallOutClick(todayShift) },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = CoralPeachDark
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("call_out_sick_button")
                        ) {
                            Text(
                                text = "Call out sick",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        OutlinedButton(
                            onClick = { onRequestSwapClick(todayShift) },
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.2.dp, Color.White.copy(alpha = 0.35f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("request_swap_button")
                        ) {
                            Text(
                                text = "Request swap",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                }
            }
        }

        // Section Header: This week · 4 shifts · 26 hrs
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "This week",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimaryNavy
                )
                Text(
                    text = "${myWeekShifts.size} shifts · $totalWeekHours hrs",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondarySlate
                )
            }
        }

        // Shift rows matching PDF Page 2
        items(myWeekShifts, key = { it.id }) { shift ->
            Card(
                onClick = { onRequestSwapClick(shift) },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = WarmCardSurface),
                border = BorderStroke(1.dp, WarmBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("week_shift_card_${shift.id}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Day Column (MON / 16)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(48.dp)
                    ) {
                        Text(
                            text = shift.dayAbbrev,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = TextSecondarySlate
                        )
                        Text(
                            text = shift.dateNum,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimaryNavy
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    // Middle Shift Time & Role
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${shift.startTime} – ${shift.endTime}",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimaryNavy
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val roleSubtitle = if (shift.shiftLabel != shift.skillRequired) {
                            "${shift.shiftLabel} · ${shift.skillRequired} · ${shift.jobSite}"
                        } else {
                            "${shift.skillRequired} · ${shift.jobSite}"
                        }
                        Text(
                            text = roleSubtitle,
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondarySlate
                        )
                        if (shift.hasNote && shift.noteText.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = shift.noteText,
                                style = MaterialTheme.typography.labelMedium,
                                color = CoralPeachDark
                            )
                        }
                    }

                    // Right Duration (5 h / 8 h)
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${shift.durationHours} h",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                            color = TextSecondarySlate
                        )
                        Text(
                            text = "Swap / Drop",
                            style = MaterialTheme.typography.labelSmall,
                            color = CoralPeach
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AllShiftsStoreTimelineContent(
    uiState: ShiftFlowUiState,
    onSelectDayIndex: (Int) -> Unit,
    onShiftTap: (ShiftItem) -> Unit
) {
    val dayLetters = listOf("T", "F", "S", "S", "M", "T", "W")
    val dayNums = listOf("12", "13", "14", "15", "16", "17", "18")
    val fullDayLabels = listOf(
        "Thu, Nov 12",
        "Fri, Nov 13",
        "Sat, Nov 14",
        "Sun, Nov 15",
        "Mon, Nov 16",
        "Tue, Nov 17",
        "Wed, Nov 18"
    )

    val allStoreShifts = uiState.shifts.filter { it.employeeId != null }
    val totalStoreHours = allStoreShifts.sumOf { it.durationHours }.coerceAtLeast(176)

    Column(modifier = Modifier.fillMaxSize()) {
        // Horizontal Week Day Strip matching Schedule_template1/2/3.jpg
        Surface(
            color = WarmCardSurface,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = {
                        onSelectDayIndex((uiState.selectedDayStripIndex - 1).coerceAtLeast(0))
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous day",
                        tint = TextSecondarySlate
                    )
                }

                dayNums.forEachIndexed { idx, num ->
                    val isSelected = idx == uiState.selectedDayStripIndex
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelectDayIndex(idx) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = dayLetters[idx],
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondarySlate
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) CoralPeachLight else Color.Transparent)
                        ) {
                            Text(
                                text = num,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) CoralPeachDark else TextPrimaryNavy
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(CoralPeach)
                        )
                    }
                }

                IconButton(
                    onClick = {
                        onSelectDayIndex((uiState.selectedDayStripIndex + 1).coerceAtMost(6))
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next day",
                        tint = TextSecondarySlate
                    )
                }
            }
        }

        // Day Header Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(WarmSurfaceVariant)
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Text(
                text = fullDayLabels.getOrElse(uiState.selectedDayStripIndex) { "Thu, Nov 12" },
                style = MaterialTheme.typography.titleMedium,
                color = TextSecondarySlate
            )
        }

        // Shift Rows matching Columbia Cold Stone Creamery / Main St store screenshots
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(allStoreShifts, key = { it.id }) { shift ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(WarmCardSurface)
                        .clickable { onShiftTap(shift) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Color Indicator Strip
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(46.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(CoralPeach)
                        )
                        Spacer(modifier = Modifier.width(12.dp))

                        // Avatar Circle with Confirmed Check Badge
                        Box(modifier = Modifier.size(48.dp)) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(shift.avatarBgHex))
                            ) {
                                Text(
                                    text = shift.employeeInitials ?: "SF",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = DeepNavy
                                )
                            }
                            if (shift.isConfirmed) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .align(Alignment.BottomEnd)
                                        .clip(CircleShape)
                                        .background(EmeraldText)
                                        .border(1.5.dp, Color.White, CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Confirmed",
                                        tint = Color.White,
                                        modifier = Modifier.size(11.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "${shift.startTime} – ${shift.endTime}",
                                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 20.sp),
                                    color = TextPrimaryNavy
                                )
                                if (shift.hasNote) {
                                    Icon(
                                        imageVector = Icons.Outlined.Description,
                                        contentDescription = "Shift note",
                                        tint = TextSecondarySlate,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${shift.employeeFullName ?: shift.employeeShortName} as ${shift.skillRequired} at ${shift.locationName}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextSecondarySlate,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    HorizontalDivider(color = WarmBorder.copy(alpha = 0.6f))
                }
            }
        }

        // Persistent Bottom Total Hours Bar (matching "Total Hours 176" in screenshots)
        Surface(
            color = DeepNavy,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Hours (${uiState.currentLocation.shortName})",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Text(
                    text = "$totalStoreHours hrs",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = CoralPeach
                )
            }
        }
    }
}

@Composable
private fun OpenShiftsPoolContent(
    uiState: ShiftFlowUiState,
    onClaimShift: (String) -> Unit,
    onOpenFindCoverage: () -> Unit
) {
    val openShifts = uiState.shifts.filter {
        it.status == ShiftStatus.OPEN || it.status == ShiftStatus.CALLED_OUT || it.status == ShiftStatus.SWAP_REQUESTED
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CoralPeachSoft),
                border = BorderStroke(1.dp, CoralPeach.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Automated Shift Pickup & Coverage",
                            style = MaterialTheme.typography.titleMedium,
                            color = DeepNavy
                        )
                        Text(
                            text = "Qualified staff with no overlap can claim open shifts immediately.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondarySlate
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    FilledTonalButton(
                        onClick = onOpenFindCoverage,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = DeepNavy,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Best Matches")
                    }
                }
            }
        }

        items(openShifts, key = { it.id }) { shift ->
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = WarmCardSurface),
                border = BorderStroke(1.dp, WarmBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = RoseBg
                        ) {
                            Text(
                                text = shift.status.label.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = RoseText,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Text(
                            text = "${shift.durationHours} hrs · ${shift.locationName}",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondarySlate
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "${shift.dayHeader} · ${shift.startTime} – ${shift.endTime}",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimaryNavy
                    )
                    Text(
                        text = "Required Skill: ${shift.skillRequired} · Job Site: ${shift.jobSite}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondarySlate
                    )
                    if (shift.noteText.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = shift.noteText,
                            style = MaterialTheme.typography.labelMedium,
                            color = CoralPeachDark
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { onClaimShift(shift.id) },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CoralPeach),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("claim_open_shift_${shift.id}")
                    ) {
                        Text(
                            text = "Pick up open shift",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}

/**
 * PDF Page 3: "Call out sick" dedicated screen
 */
@Composable
fun CallOutSickScreen(
    shift: ShiftItem,
    onBack: () -> Unit,
    onConfirm: (CallOutReason, String) -> Unit
) {
    BackHandler { onBack() }

    var selectedReason by remember { mutableStateOf(CallOutReason.SICK) }
    var managerNote by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmCreamBg)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Header: < Call out sick
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("call_out_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to schedule",
                    tint = TextPrimaryNavy
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Call out sick",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimaryNavy
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Shift You'll Miss summary card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = WarmCardSurface),
                border = BorderStroke(1.dp, WarmBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "SHIFT YOU'LL MISS",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp
                        ),
                        color = TextSecondarySlate
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Today · ${shift.startTime} – ${shift.endTime}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimaryNavy
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${shift.skillRequired} · ${shift.locationName} · starts in 5 h 20 m",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondarySlate
                    )
                }
            }

            Text(
                text = "Reason",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimaryNavy
            )

            CallOutReason.entries.forEach { reason ->
                val isSelected = selectedReason == reason
                Card(
                    onClick = { selectedReason = reason },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) CoralPeachSoft else WarmCardSurface
                    ),
                    border = BorderStroke(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) CoralPeach else WarmBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reason_option_${reason.name.lowercase()}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { selectedReason = reason },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = CoralPeach,
                                unselectedColor = TextSecondarySlate
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = reason.label,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            ),
                            color = TextPrimaryNavy
                        )
                    }
                }
            }

            Text(
                text = "Message to manager (optional)",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimaryNavy
            )

            OutlinedTextField(
                value = managerNote,
                onValueChange = { managerNote = it },
                placeholder = { Text("Anything they should know") },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = WarmCardSurface,
                    unfocusedContainerColor = WarmCardSurface,
                    focusedBorderColor = CoralPeach,
                    unfocusedBorderColor = WarmBorder
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .testTag("call_out_message_input")
            )

            // Reassurance green banner matching PDF Page 3
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = EmeraldBg,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "Automatic coverage info",
                        tint = EmeraldText,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Your manager is notified right away, and the app starts asking available teammates to cover this shift.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = EmeraldText
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { onConfirm(selectedReason, managerNote) },
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CoralPeach),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("confirm_call_out_button")
        ) {
            Text(
                text = "Confirm call-out",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }
    }
}

@Composable
private fun ShiftSwapOfferDialog(
    shift: ShiftItem,
    onDismiss: () -> Unit,
    onConfirmAction: (String, String?) -> Unit
) {
    var selectedAction by remember { mutableStateOf("SWAP_DIRECT") }
    var selectedTeammate by remember { mutableStateOf("Jordan T.") }
    val teammates = listOf("Jordan T.", "Priya S.", "Luis M.", "Jacqueline O.", "Simon G.")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Shift Swap, Offer & Drop",
                style = MaterialTheme.typography.headlineMedium
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "${shift.dayHeader} · ${shift.startTime} – ${shift.endTime} (${shift.skillRequired})",
                    style = MaterialTheme.typography.titleMedium,
                    color = CoralPeachDark
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SoftNavySurface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Automation active: Trades with teammates trained in ${shift.skillRequired} with no overtime are approved automatically.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DeepNavy,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedAction == "SWAP_DIRECT",
                        onClick = { selectedAction = "SWAP_DIRECT" },
                        label = { Text("Swap / Trade") }
                    )
                    FilterChip(
                        selected = selectedAction == "DROP_TO_OPEN",
                        onClick = { selectedAction = "DROP_TO_OPEN" },
                        label = { Text("Drop to Open Pool") }
                    )
                }

                if (selectedAction == "SWAP_DIRECT") {
                    Text(
                        text = "Select qualified teammate:",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        teammates.forEach { mate ->
                            FilterChip(
                                selected = selectedTeammate == mate,
                                onClick = { selectedTeammate = mate },
                                label = { Text(mate) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmAction(selectedAction, selectedTeammate) },
                colors = ButtonDefaults.buttonColors(containerColor = CoralPeach)
            ) {
                Text(if (selectedAction == "DROP_TO_OPEN") "Drop Shift" else "Confirm Swap")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
