package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AlertItem
import com.example.model.CandidateInviteStatus
import com.example.model.CoverageCandidate
import com.example.ui.theme.*
import com.example.viewmodel.ShiftFlowUiState

/**
 * PDF Page 5 ("Alerts — Can you cover?") and PDF Page 4 ("Find coverage — Best matches")
 */
@Composable
fun EmployeeAlertsAndCoverScreen(
    uiState: ShiftFlowUiState,
    onRespondUrgentAlert: (String, Boolean) -> Unit,
    onOpenFindCoverage: (Boolean) -> Unit,
    onAssignCandidate: (String) -> Unit,
    onSendCoverToNext3: () -> Unit,
    onCallCandidate: (String) -> Unit
) {
    if (uiState.showFindCoverageScreen) {
        FindCoverageScreen(
            uiState = uiState,
            onBack = { onOpenFindCoverage(false) },
            onAssignCandidate = onAssignCandidate,
            onSendCoverToNext3 = onSendCoverToNext3,
            onCallCandidate = onCallCandidate
        )
        return
    }

    val urgentAlert = uiState.alerts.firstOrNull { it.isUrgentCover }
    val earlierAlerts = uiState.alerts.filterNot { it.isUrgentCover }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmCreamBg),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
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
                        text = "Alerts",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondarySlate
                    )
                    Text(
                        text = "Can you cover?",
                        style = MaterialTheme.typography.displayLarge,
                        color = TextPrimaryNavy
                    )
                }

                // Quick button to view PDF Page 4 (Find coverage auto-ranker)
                FilledTonalButton(
                    onClick = { onOpenFindCoverage(true) },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = DeepNavy,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.testTag("open_find_coverage_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonSearch,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Find Coverage")
                }
            }
        }

        // Urgent Cover Request Card matching PDF Page 5
        if (urgentAlert != null) {
            item {
                UrgentCoverCard(
                    alert = urgentAlert,
                    onAccept = { onRespondUrgentAlert(urgentAlert.id, true) },
                    onDecline = { onRespondUrgentAlert(urgentAlert.id, false) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Earlier",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimaryNavy
            )
        }

        items(earlierAlerts, key = { it.id }) { alert ->
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = WarmCardSurface),
                border = BorderStroke(1.dp, WarmBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = alert.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimaryNavy
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = alert.subtitle,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondarySlate
                    )
                }
            }
        }
    }
}

@Composable
private fun UrgentCoverCard(
    alert: AlertItem,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = WarmCardSurface),
        border = BorderStroke(1.5.dp, CoralPeach),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("urgent_cover_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = CoralPeachLight
                ) {
                    Text(
                        text = "URGENT · TODAY",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = CoralPeachDark,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }

                Text(
                    text = alert.timeAgo,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondarySlate
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = alert.urgentTimeRange ?: "2:00 – 10:00 PM",
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 30.sp),
                color = TextPrimaryNavy
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = alert.urgentMeta ?: "Sat, Nov 14 · Scooper · Main St store",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondarySlate
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Two info sub-cards: Your hours after | Asked by
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = WarmSurfaceVariant,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Your hours after",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondarySlate
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = alert.hoursAfterAccept ?: "20 h this week",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimaryNavy
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = WarmSurfaceVariant,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Asked by",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondarySlate
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = alert.askedBy ?: "Sam (Manager)",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimaryNavy
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (alert.isResolved) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = EmeraldBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = EmeraldText,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = alert.resolutionLabel ?: "Responded",
                            style = MaterialTheme.typography.titleMedium,
                            color = EmeraldText
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDecline,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, WarmBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimaryNavy),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("decline_cover_button")
                    ) {
                        Text(
                            text = "Decline",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Button(
                        onClick = onAccept,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CoralPeach),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("accept_cover_button")
                    ) {
                        Text(
                            text = "Accept shift",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = alert.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondarySlate,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * PDF Page 4: "Find coverage — Best matches Ranked by availability & hours"
 */
@Composable
fun FindCoverageScreen(
    uiState: ShiftFlowUiState,
    onBack: () -> Unit,
    onAssignCandidate: (String) -> Unit,
    onSendCoverToNext3: () -> Unit,
    onCallCandidate: (String) -> Unit
) {
    BackHandler { onBack() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmCreamBg)
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: < Find coverage
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("find_coverage_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimaryNavy
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Find coverage",
                        style = MaterialTheme.typography.headlineLarge,
                        color = TextPrimaryNavy
                    )
                }
            }

            // Pink/Coral Open Shift Banner matching PDF Page 4
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = CoralPeachLight,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "OPEN SHIFT · CALLED OUT SICK",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.6.sp
                            ),
                            color = CoralPeachDark
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = uiState.activeCalledOutShiftTitle,
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextPrimaryNavy
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = uiState.activeCalledOutSub,
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondarySlate
                        )
                    }
                }
            }

            // Section Title: Best matches · Ranked by availability & hours
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "Best matches",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimaryNavy
                    )
                    Text(
                        text = "Ranked by availability & hours",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondarySlate
                    )
                }
            }

            // Ranked Candidate Cards (1. Jordan T., 2. Priya S., 3. Luis M.)
            items(uiState.coverageCandidates, key = { it.employeeId }) { candidate ->
                CoverageCandidateCard(
                    candidate = candidate,
                    onCall = { onCallCandidate(candidate.name) },
                    onAssign = { onAssignCandidate(candidate.employeeId) }
                )
            }
        }

        // Bottom Sticky CTA: Send cover request to next 3
        Surface(
            color = WarmCreamBg,
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Button(
                    onClick = onSendCoverToNext3,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CoralPeach),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("send_cover_next_3_button")
                ) {
                    Text(
                        text = "Send cover request to next 3",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Push + SMS · first to accept gets the shift",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondarySlate
                )
            }
        }
    }
}

@Composable
private fun CoverageCandidateCard(
    candidate: CoverageCandidate,
    onCall: () -> Unit,
    onAssign: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = WarmCardSurface),
        border = BorderStroke(1.dp, WarmBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(WarmSurfaceVariant)
                ) {
                    Text(
                        text = candidate.initials,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimaryNavy
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = candidate.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimaryNavy
                    )
                    Text(
                        text = candidate.overlapSummary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondarySlate
                    )
                }

                // Status badge (Asked / Declined / Assigned)
                if (candidate.status != CandidateInviteStatus.READY) {
                    val (bg, fg) = when (candidate.status) {
                        CandidateInviteStatus.ASKED -> InfoBlueBg to InfoBlueText
                        CandidateInviteStatus.DECLINED -> UnavailableGrayBg to UnavailableGrayText
                        CandidateInviteStatus.ASSIGNED -> EmeraldBg to EmeraldText
                        CandidateInviteStatus.READY -> EmeraldBg to EmeraldText
                    }
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = bg
                    ) {
                        Text(
                            text = candidate.status.label,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = fg,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Availability & Capability Chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = EmeraldBg
                ) {
                    Text(
                        text = candidate.availabilityTag,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = EmeraldText,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (candidate.isPartialAvailability) AmberBg else EmeraldBg
                ) {
                    Text(
                        text = candidate.capabilityTag,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = if (candidate.isPartialAvailability) AmberText else EmeraldText,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCall,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, WarmBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimaryNavy),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text(
                        text = "Call",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Button(
                    onClick = onAssign,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("assign_shift_${candidate.employeeId}")
                ) {
                    Text(
                        text = if (candidate.status == CandidateInviteStatus.ASSIGNED) "Assigned ✓" else "Assign shift",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        }
    }
}
