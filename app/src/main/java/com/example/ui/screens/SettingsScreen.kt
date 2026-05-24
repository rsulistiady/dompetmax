package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.util.Translate
import com.example.ui.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SettingsScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val lang by viewModel.currentLanguage.collectAsState()
    val themeChoice by viewModel.themeSelection.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    val lastSyncTime by viewModel.lastSyncTime.collectAsState()
    val pushOn by viewModel.pushNotificationEnabled.collectAsState()
    val localOn by viewModel.reminderNotificationEnabled.collectAsState()

    var showResetDetailsDialog by remember { mutableStateOf(false) }

    // Sync progress helper rotation animation
    val infiniteTransition = rememberInfiniteTransition(label = "sync_rotation")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Section Sync Offline
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sync_section_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = "Cloud Cache",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = Translate.getString("offline_sync", lang),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Sync Status Indicator
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                when (syncStatus) {
                                    "SYNCED" -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                                    "SYNCING" -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)
                                    else -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                                }
                            )
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val statusIcon = when (syncStatus) {
                            "SYNCED" -> Icons.Default.CloudDone
                            "SYNCING" -> Icons.Default.Sync
                            else -> Icons.Default.CloudQueue
                        }
                        val statusString = when (syncStatus) {
                            "SYNCED" -> Translate.getString("synced", lang)
                            "SYNCING" -> Translate.getString("syncing", lang)
                            else -> Translate.getString("needs_sync", lang)
                        }
                        val statusTint = when (syncStatus) {
                            "SYNCED" -> MaterialTheme.colorScheme.primary
                            "SYNCING" -> MaterialTheme.colorScheme.secondary
                            else -> MaterialTheme.colorScheme.error
                        }

                        Icon(
                            imageVector = statusIcon,
                            contentDescription = "Sync Indicator State",
                            tint = statusTint,
                            modifier = Modifier
                                .size(32.dp)
                                .then(
                                    if (syncStatus == "SYNCING") Modifier.rotate(angle) else Modifier
                                )
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = Translate.getString("sync_status", lang),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = statusString,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${Translate.getString("last_synced", lang)} ${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(lastSyncTime))}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = if (lang == "ID") {
                            "Database Room menyimpan catatan transaksi Anda secara lokal secara aman di dalam penyimpanan internal perangkat."
                        } else {
                            "Your local Room database safely stores financial records offline in the device's internal storage."
                        },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { viewModel.triggerSync() },
                        enabled = syncStatus != "SYNCING",
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sync_action_button")
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = "Sync Icon", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(Translate.getString("sync_now", lang))
                    }
                }
            }
        }

        // 2. Section Notifications Preferences
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("notifications_section_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Alert Preferences",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = Translate.getString("notifications", lang),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Item Push Notification Switch Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(0.85f)) {
                            Text(
                                text = Translate.getString("push_reminders", lang),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (lang == "ID") {
                                    "Terima pengingat harian & evaluasi bulanan via Google Cloud Messaging."
                                } else {
                                    "Receive automated weekly networth metrics direct via secure Firebase console."
                                },
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = pushOn,
                            onCheckedChange = { viewModel.togglePushNotifications() },
                            modifier = Modifier.testTag("toggle_push_notifications")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                    Spacer(modifier = Modifier.height(10.dp))

                    // Item Local Reminder Notifications Switch Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(0.85f)) {
                            Text(
                                text = Translate.getString("due_reminders", lang),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (lang == "ID") {
                                    "Notifikasi lokal di status bar 2 hari sebelum tanggal jatuh tempo layanan."
                                } else {
                                    "Local high priority alert 48 hours before specific Netflix/Spotify renewals."
                                },
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = localOn,
                            onCheckedChange = { viewModel.toggleReminders() },
                            modifier = Modifier.testTag("toggle_due_reminders")
                        )
                    }
                }
            }
        }

        // 3. Section Language & App Customization Theme
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("theme_lang_section_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = Translate.getString("language", lang),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { viewModel.setLanguage("ID") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("lang_btn_id"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (lang == "ID") MaterialTheme.colorScheme.primary else Color.Transparent,
                                contentColor = if (lang == "ID") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text("Bahasa Indonesia (ID)")
                        }

                        Button(
                            onClick = { viewModel.setLanguage("EN") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("lang_btn_en"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (lang == "EN") MaterialTheme.colorScheme.primary else Color.Transparent,
                                contentColor = if (lang == "EN") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text("English (EN)")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = Translate.getString("theme", lang),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("SYSTEM", "LIGHT", "DARK").forEach { theme ->
                            val isSelected = themeChoice == theme
                            val label = when (theme) {
                                "LIGHT" -> Translate.getString("light_mode", lang)
                                "DARK" -> Translate.getString("dark_mode", lang)
                                else -> Translate.getString("system_default", lang)
                            }
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setThemeSelection(theme) },
                                label = { Text(label, fontSize = 11.sp) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("theme_chip_$theme")
                            )
                        }
                    }
                }
            }
        }

        // 4. Section Danger Zone
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("danger_zone_section_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Dangerous,
                            contentDescription = "Danger Warning",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = Translate.getString("experimental", lang).uppercase(),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (lang == "ID") {
                            "Menghapus seluruh file database saat ini dan mereset kas flow, data langganan, dan data crypto Anda ke bentuk awal."
                        } else {
                            "Purges all active tables, removing customized indices and loading pristine mockup configurations from factory preset seeds."
                        },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { showResetDetailsDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("factory_reset_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset Icon", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(Translate.getString("reset_data", lang))
                    }
                }
            }
        }

        // 5. Versioning information footer
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "DompetMax (ID) v1.0.0",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                Text(
                    text = Translate.getString("developer_mode", lang),
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    lineHeight = 14.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }

    if (showResetDetailsDialog) {
        AlertDialog(
            onDismissRequest = { showResetDetailsDialog = false },
            title = {
                Text(
                    text = Translate.getString("reset_data", lang),
                    fontWeight = FontWeight.ExtraBold
                )
            },
            text = {
                Text(
                    text = Translate.getString("reset_confirm", lang),
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.forceResetData()
                        showResetDetailsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("confirm_reset_btn")
                ) {
                    Text(Translate.getString("delete", lang))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showResetDetailsDialog = false },
                    modifier = Modifier.testTag("dismiss_reset_btn")
                ) {
                    Text(Translate.getString("cancel", lang))
                }
            },
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.testTag("reset_alert_dialog")
        )
    }
}
