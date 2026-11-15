package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChatMessage
import com.example.model.NotificationPrefItem
import com.example.ui.theme.*
import com.example.viewmodel.ShiftFlowUiState

/**
 * In-built WorkChat messaging screen inspired by Messaging_e5914f3b.jpeg
 */
@Composable
fun WorkChatScreen(
    uiState: ShiftFlowUiState,
    onSendMessage: (String, Boolean) -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    var sendAsManagerAnnouncement by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmCreamBg)
    ) {
        // Channel Header Banner
        Surface(
            color = DeepNavy,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = uiState.currentLocation.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                    Text(
                        text = "${uiState.employees.size} members · Team & Shift Coverage Chat",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.78f)
                    )
                }

                FilterChip(
                    selected = sendAsManagerAnnouncement,
                    onClick = { sendAsManagerAnnouncement = !sendAsManagerAnnouncement },
                    label = {
                        Text(
                            text = if (sendAsManagerAnnouncement) "Manager Broadcast" else "Crew Reply",
                            color = if (sendAsManagerAnnouncement) DeepNavy else Color.White
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CoralPeach,
                        containerColor = SlateNavy
                    )
                )
            }
        }

        // Quick Action Templates for Shift Coverage / Availability Reminder
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(WarmSurfaceVariant)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SuggestionChip(
                onClick = {
                    onSendMessage(
                        "Is anyone able to cover the end of my shift today from 5:00p to 10:00p?",
                        false
                    )
                },
                label = { Text("Ask for shift cover") }
            )
            SuggestionChip(
                onClick = {
                    onSendMessage(
                        "Reminder: Please update your availability for the next two weeks by Monday 11:59 PM.",
                        true
                    )
                },
                label = { Text("Post availability reminder") }
            )
        }

        // Messages List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(uiState.chatMessages, key = { it.id }) { msg ->
                ChatMessageItem(msg = msg)
            }
        }

        // Bottom Composer Bar
        Surface(
            color = WarmCardSurface,
            tonalElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = { Text("Type a message to ${uiState.currentLocation.shortName}...") },
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = WarmCreamBg,
                        unfocusedContainerColor = WarmCreamBg,
                        focusedBorderColor = CoralPeach,
                        unfocusedBorderColor = WarmBorder
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("workchat_message_input")
                )

                IconButton(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            onSendMessage(messageText, sendAsManagerAnnouncement)
                            messageText = ""
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(CoralPeach)
                        .testTag("workchat_send_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send message",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatMessageItem(msg: ChatMessage) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (msg.dateDivider != null) {
            Text(
                text = msg.dateDivider,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = TextSecondarySlate,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            HorizontalDivider(color = WarmBorder, modifier = Modifier.padding(bottom = 8.dp))
        }

        if (msg.isManagerBroadcast) {
            // Right-aligned Manager Broadcast Bubble matching Messaging_e5914f3b.jpeg
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = msg.senderName,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimaryNavy
                    )
                    Text(
                        text = msg.timestamp,
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondarySlate
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(18.dp, 4.dp, 18.dp, 18.dp),
                    color = DeepNavy,
                    modifier = Modifier.widthIn(max = 320.dp)
                ) {
                    Text(
                        text = msg.text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        textAlign = TextAlign.End,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            // Left-aligned Crew Message Bubble
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(CoralPeachLight)
                ) {
                    Text(
                        text = msg.initials,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = CoralPeachDark
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = msg.senderName,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimaryNavy
                        )
                        Text(
                            text = msg.timestamp,
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondarySlate
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp, 18.dp, 18.dp, 18.dp),
                        color = WarmCardSurface,
                        border = BorderStroke(1.dp, WarmBorder)
                    ) {
                        Text(
                            text = msg.text,
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextPrimaryNavy,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * PDF Page 6: "Notifications — ALERT TYPES & Quiet hours"
 */
@Composable
fun ProfileAndNotificationsScreen(
    uiState: ShiftFlowUiState,
    onToggleChannel: (String, String) -> Unit,
    onToggleQuietHours: (Boolean) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmCreamBg),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Notifications",
                style = MaterialTheme.typography.displayLarge,
                color = TextPrimaryNavy
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "ALERT TYPES",
                style = MaterialTheme.typography.labelLarge.copy(
                    letterSpacing = 0.8.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = TextSecondarySlate
            )
        }

        items(uiState.notificationPrefs, key = { it.id }) { pref ->
            NotificationPreferenceCard(
                pref = pref,
                onToggleChannel = { channel -> onToggleChannel(pref.id, channel) }
            )
        }

        // Quiet Hours Card matching PDF Page 6
        item {
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
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Quiet hours",
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimaryNavy
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Pause non-urgent alerts overnight",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextSecondarySlate
                            )
                        }

                        Switch(
                            checked = uiState.quietHoursEnabled,
                            onCheckedChange = onToggleQuietHours,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = CoralPeach
                            ),
                            modifier = Modifier.testTag("quiet_hours_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = WarmSurfaceVariant,
                            border = BorderStroke(1.dp, WarmBorder)
                        ) {
                            Text(
                                text = uiState.quietHoursStart,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimaryNavy,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                            )
                        }
                        Text(
                            text = "to",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondarySlate
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = WarmSurfaceVariant,
                            border = BorderStroke(1.dp, WarmBorder)
                        ) {
                            Text(
                                text = uiState.quietHoursEnd,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimaryNavy,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Urgent cover requests still come through during quiet hours.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondarySlate
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationPreferenceCard(
    pref: NotificationPrefItem,
    onToggleChannel: (String) -> Unit
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
                .padding(18.dp)
        ) {
            Text(
                text = pref.title,
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimaryNavy
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = pref.description,
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondarySlate
            )
            Spacer(modifier = Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ChannelTogglePill(
                    label = "Push",
                    icon = Icons.Outlined.Notifications,
                    selected = pref.pushEnabled,
                    onClick = { onToggleChannel("PUSH") }
                )

                if (pref.supportsEmail) {
                    ChannelTogglePill(
                        label = "Email",
                        icon = Icons.Outlined.Email,
                        selected = pref.emailEnabled,
                        onClick = { onToggleChannel("EMAIL") }
                    )
                }

                if (pref.supportsText) {
                    ChannelTogglePill(
                        label = "Text",
                        icon = Icons.Outlined.ChatBubbleOutline,
                        selected = pref.textEnabled,
                        onClick = { onToggleChannel("TEXT") }
                    )
                }
            }
        }
    }
}

@Composable
private fun ChannelTogglePill(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (selected) CoralPeachLight else WarmSurfaceVariant,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) CoralPeach.copy(alpha = 0.5f) else WarmBorder
        ),
        modifier = Modifier.height(38.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) CoralPeachDark else TextSecondarySlate,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = if (selected) CoralPeachDark else TextSecondarySlate
            )
        }
    }
}
