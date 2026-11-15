package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.AvailabilitySlot
import com.example.model.DayAvailability
import com.example.model.RequestStatus
import com.example.ui.theme.*
import com.example.viewmodel.ShiftFlowUiState

/**
 * PDF Page 1: "My availability" screen + Recurring Availability & Temporary Unavailability / Time-Off Management
 */
@Composable
fun EmployeeAvailabilityScreen(
    uiState: ShiftFlowUiState,
    onSelectSlot: (Int, AvailabilitySlot) -> Unit,
    onCopyLastWeek: () -> Unit,
    onClearAll: () -> Unit,
    onToggleRecurring: (Boolean) -> Unit,
    onSubmitAvailability: () -> Unit,
    onAddTimeOffRequest: (String, String, String, Boolean) -> Unit
) {
    var showAddTimeOffDialog by remember { mutableStateOf(false) }
    var weekOffset by remember { mutableIntStateOf(0) }

    val weekLabels = listOf(
        "Week of Nov 16 – 22",
        "Week of Nov 23 – 29",
        "Week of Nov 30 – Dec 6"
    )
    val currentWeekTitle = weekLabels.getOrElse(weekOffset.coerceIn(0, 2)) { "Week of Nov 16 – 22" }

    if (showAddTimeOffDialog) {
        AddTimeOffOrUnavailabilityDialog(
            onDismiss = { showAddTimeOffDialog = false },
            onSubmit = { typeLabel, dates, reason, recurring ->
                onAddTimeOffRequest(typeLabel, dates, reason, recurring)
                showAddTimeOffDialog = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmCreamBg)
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: My availability
            item {
                Text(
                    text = "My availability",
                    style = MaterialTheme.typography.displayLarge,
                    color = TextPrimaryNavy
                )
            }

            // Week Selector: < Week of Nov 16 – 22 / Due Mon, Nov 16 · 11:59 PM >
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedIconButton(
                        onClick = { weekOffset = (weekOffset - 1).coerceAtLeast(0) },
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, WarmBorder),
                        colors = IconButtonDefaults.outlinedIconButtonColors(containerColor = WarmCardSurface),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Previous week",
                            tint = TextPrimaryNavy
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = currentWeekTitle,
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimaryNavy
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Due Mon, Nov 16 · 11:59 PM",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondarySlate
                        )
                    }

                    OutlinedIconButton(
                        onClick = { weekOffset = (weekOffset + 1).coerceAtMost(2) },
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, WarmBorder),
                        colors = IconButtonDefaults.outlinedIconButtonColors(containerColor = WarmCardSurface),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Next week",
                            tint = TextPrimaryNavy
                        )
                    }
                }
            }

            // Action Pills: Copy last week | Clear all | Recurring Toggle
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onCopyLastWeek,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, WarmBorder),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = WarmCardSurface,
                            contentColor = TextPrimaryNavy
                        ),
                        modifier = Modifier.testTag("copy_last_week_button")
                    ) {
                        Text(
                            text = "Copy last week",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    OutlinedButton(
                        onClick = onClearAll,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, WarmBorder),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = WarmCardSurface,
                            contentColor = TextPrimaryNavy
                        ),
                        modifier = Modifier.testTag("clear_all_availability_button")
                    ) {
                        Text(
                            text = "Clear all",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    FilterChip(
                        selected = uiState.isRecurringAvailabilityMode,
                        onClick = { onToggleRecurring(!uiState.isRecurringAvailabilityMode) },
                        label = { Text("Recurring") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Repeat,
                                contentDescription = "Recurring preference",
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CoralPeachLight,
                            selectedLabelColor = CoralPeachDark
                        )
                    )
                }
            }

            // Day Availability Cards (Monday .. Sunday) matching PDF Page 1
            items(uiState.mariaAvailability, key = { it.dayIndex }) { dayAvail ->
                DayAvailabilityCard(
                    dayAvailability = dayAvail,
                    onSelectSlot = { slot -> onSelectSlot(dayAvail.dayIndex, slot) }
                )
            }

            // Temporary Unavailability & Time-Off Management Section
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Time off & unavailability rules",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimaryNavy
                        )
                        Text(
                            text = "Recurring unavailability & temporary time-off requests",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondarySlate
                        )
                    }
                    FilledTonalButton(
                        onClick = { showAddTimeOffDialog = true },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = DeepNavy,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.testTag("add_time_off_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Request")
                    }
                }
            }

            items(uiState.timeOffRequests, key = { it.id }) { req ->
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${req.typeLabel} · ${req.dateRange}",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimaryNavy
                            )
                            Text(
                                text = "${req.employeeName} — ${req.reason}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondarySlate
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = when (req.status) {
                                RequestStatus.APPROVED -> EmeraldBg
                                RequestStatus.PENDING -> AmberBg
                                RequestStatus.DECLINED -> RoseBg
                            }
                        ) {
                            Text(
                                text = req.status.label,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = when (req.status) {
                                    RequestStatus.APPROVED -> EmeraldText
                                    RequestStatus.PENDING -> AmberText
                                    RequestStatus.DECLINED -> RoseText
                                },
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }

        // Bottom Sticky Submit Button matching PDF Page 1
        Surface(
            color = WarmCreamBg,
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = onSubmitAvailability,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CoralPeach),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .height(54.dp)
                    .testTag("submit_availability_button")
            ) {
                Text(
                    text = "Submit availability",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun DayAvailabilityCard(
    dayAvailability: DayAvailability,
    onSelectSlot: (AvailabilitySlot) -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = WarmCardSurface),
        border = BorderStroke(1.dp, WarmBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
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
                    text = dayAvailability.dayName,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimaryNavy
                )
                Text(
                    text = dayAvailability.dateLabel,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondarySlate
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AvailabilitySlot.entries.forEach { slot ->
                    val isSelected = dayAvailability.selectedSlot == slot
                    Surface(
                        onClick = { onSelectSlot(slot) },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) CoralPeachSoft else WarmCardSurface,
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) CoralPeach else WarmBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("avail_${dayAvailability.dayName.lowercase()}_${slot.name.lowercase()}")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = slot.label,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) CoralPeachDark else TextPrimaryNavy
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddTimeOffOrUnavailabilityDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, String, String, Boolean) -> Unit
) {
    var selectedType by remember { mutableStateOf("Time Off Request") }
    var dateRange by remember { mutableStateOf("Nov 24 – Nov 25") }
    var reason by remember { mutableStateOf("Family visit out of town") }
    var isRecurring by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Time Off & Unavailability",
                style = MaterialTheme.typography.headlineMedium
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = !isRecurring,
                        onClick = {
                            isRecurring = false
                            selectedType = "Temporary Unavailability"
                        },
                        label = { Text("Temporary / Time Off") }
                    )
                    FilterChip(
                        selected = isRecurring,
                        onClick = {
                            isRecurring = true
                            selectedType = "Recurring Unavailability"
                        },
                        label = { Text("Recurring Rule") }
                    )
                }

                OutlinedTextField(
                    value = dateRange,
                    onValueChange = { dateRange = it },
                    label = { Text(if (isRecurring) "Recurring Days / Hours" else "Dates / Shift Window") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason or Reference Note") },
                    modifier = Modifier.fillMaxWidth()
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = EmeraldBg
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = EmeraldText,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Automated Rule: Requests submitted before roster lock with available coverage are auto-approved.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = EmeraldText
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(selectedType, dateRange, reason, isRecurring) },
                colors = ButtonDefaults.buttonColors(containerColor = CoralPeach)
            ) {
                Text("Submit Request")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
