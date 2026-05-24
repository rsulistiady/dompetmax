package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.SubscriptionEntity
import com.example.ui.util.Translate
import com.example.ui.viewmodel.FinanceViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SubscriptionScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val lang by viewModel.currentLanguage.collectAsState()
    val subscriptions by viewModel.subscriptions.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    val currencyFormat = rememberCurrencyFormatter(lang)
    val sdf = rememberDateFormat()

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            // Subscriptions Page Header
            Text(
                text = Translate.getString("active_subs", lang),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            if (subscriptions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EventNote,
                            contentDescription = "No Subscriptions",
                            modifier = Modifier.size(72.dp),
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (lang == "ID") "Belum ada tagihan berulang" else "No active subscriptions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (lang == "ID") {
                                "Catat Netflix, Spotify, atau tagihan bulanan Anda agar tidak terlewat tanggal jatuh temponya."
                            } else {
                                "Track Spotify, Netflix, or other periodic utilities to monitor next due dates with smart notifications."
                            },
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 80.dp)
                ) {
                    items(subscriptions, key = { it.id }) { sub ->
                        SubscriptionCard(
                            sub = sub,
                            currencyFormat = currencyFormat,
                            sdf = sdf,
                            lang = lang,
                            onToggleReminder = { viewModel.toggleSubscriptionReminder(sub) },
                            onDelete = { viewModel.deleteSubscription(sub) }
                        )
                    }
                }
            }
        }

        // Add Button
        LargeFloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("add_subscription_fab"),
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add subscription",
                modifier = Modifier.size(28.dp)
            )
        }
    }

    if (showAddDialog) {
        AddSubscriptionDialog(
            lang = lang,
            onDismiss = { showAddDialog = false },
            onSave = { name, amount, cycle, daysAhead, reminder ->
                val nextDue = System.currentTimeMillis() + (86400000L * daysAhead)
                viewModel.addSubscription(name, amount, cycle, nextDue, reminder)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun SubscriptionCard(
    sub: SubscriptionEntity,
    currencyFormat: NumberFormat,
    sdf: SimpleDateFormat,
    lang: String,
    onToggleReminder: () -> Unit,
    onDelete: () -> Unit
) {
    val now = System.currentTimeMillis()
    val daysRemaining = remember(sub.nextDueDateMillis, now) {
        val diff = sub.nextDueDateMillis - now
        val days = (diff / 86400000L).toInt()
        if (days < 0) 0 else days
    }

    // Progress percentage
    val maxProgressDays = when (sub.billingCycle) {
        "WEEKLY" -> 7
        "ANNUALLY" -> 365
        else -> 30 // MONTHLY
    }
    val progressRatio = if (daysRemaining >= maxProgressDays) 1.0f else (daysRemaining.toFloat() / maxProgressDays.toFloat())

    // Determine warning color based on urgency
    val urgencyColor = when {
        daysRemaining <= 3 -> MaterialTheme.colorScheme.error
        daysRemaining <= 7 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("subscription_card_${sub.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Subscriptions,
                        contentDescription = "Subscription",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = sub.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${Translate.getString(sub.billingCycle, lang)} • ${currencyFormat.format(sub.amount)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onToggleReminder,
                    modifier = Modifier.size(36.dp)
                ) {
                    val alertIcon = if (sub.isReminderActive) {
                        Icons.Outlined.NotificationsActive
                    } else {
                        Icons.Outlined.Notifications
                    }
                    val iconTint = if (sub.isReminderActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    Icon(
                        imageVector = alertIcon,
                        contentDescription = "Toggle reminder",
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Due Date details with linear indicator Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${Translate.getString("next_due", lang)} ${sdf.format(Date(sub.nextDueDateMillis))}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$daysRemaining ${Translate.getString("days_left", lang)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = urgencyColor
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { progressRatio },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = urgencyColor,
                trackColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
fun AddSubscriptionDialog(
    lang: String,
    onDismiss: () -> Unit,
    onSave: (String, Double, String, Int, Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var cycle by remember { mutableStateOf("MONTHLY") } // "WEEKLY", "MONTHLY", "ANNUALLY"
    var daysAhead by remember { mutableStateOf(30) } // Default Monthly: 30 days ahead
    var reminderActive by remember { mutableStateOf(true) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("add_subscription_dialog")
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = Translate.getString("add_subscription", lang),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Service Name Input
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(if (lang == "ID") "Nama Layanan / Tagihan" else "Service name (e.g., Netflix)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_sub_name"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                // Price Input
                item {
                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it },
                        label = { Text(Translate.getString("amount", lang)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_sub_amount"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                // Billing Cycle frequency selector
                item {
                    Text(
                        text = Translate.getString("billing_cycle_label", lang),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("WEEKLY", "MONTHLY", "ANNUALLY").forEach { item ->
                            val isSelected = cycle == item
                            val labelKey = when (item) {
                                "WEEKLY" -> "weekly"
                                "ANNUALLY" -> "annually"
                                else -> "monthly"
                            }
                            val targetDays = when (item) {
                                "WEEKLY" -> 7
                                "ANNUALLY" -> 365
                                else -> 30
                            }

                            FilterChip(
                                selected = isSelected,
                                onClick = { 
                                    cycle = item
                                    daysAhead = targetDays
                                },
                                label = { Text(Translate.getString(labelKey, lang)) },
                                modifier = Modifier.testTag("chip_cycle_$item")
                            )
                        }
                    }
                }

                // Days Offset Slider to customize exact next Billing Date
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (lang == "ID") "Jatuh Tempo Rentang Waktu" else "Billing date relative range",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$daysAhead ${Translate.getString("days_left", lang)}",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 12.sp
                        )
                    }
                    Slider(
                        value = daysAhead.toFloat(),
                        onValueChange = { daysAhead = it.toInt() },
                        valueRange = 1f..60f,
                        steps = 59,
                        modifier = Modifier.testTag("sub_days_slider")
                    )
                }

                // Reminder notification switch
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = Translate.getString("activate_reminders", lang),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Switch(
                            checked = reminderActive,
                            onCheckedChange = { reminderActive = it },
                            modifier = Modifier.testTag("sub_reminder_switch")
                        )
                    }
                }

                // Dialog Buttons save & cancel
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("sub_cancel_btn")
                        ) {
                            Text(Translate.getString("cancel", lang))
                        }

                        Button(
                            onClick = {
                                val amt = amountStr.toDoubleOrNull() ?: 0.0
                                if (name.isNotBlank() && amt > 0.0) {
                                    onSave(name, amt, cycle, daysAhead, reminderActive)
                                }
                            },
                            enabled = name.isNotBlank() && (amountStr.toDoubleOrNull() ?: 0.0) > 0.0,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("sub_save_btn")
                        ) {
                            Text(Translate.getString("save", lang))
                        }
                    }
                }
            }
        }
    }
}
