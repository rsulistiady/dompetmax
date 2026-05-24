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
import com.example.data.model.InvestmentEntity
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpenseRedDark
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.ProfitGreenDark
import com.example.ui.util.Translate
import com.example.ui.viewmodel.FinanceViewModel
import java.text.NumberFormat
import java.util.*

@Composable
fun InvestmentScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val lang by viewModel.currentLanguage.collectAsState()
    val investments by viewModel.investments.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    val currencyFormat = rememberCurrencyFormatter(lang)

    val totalInvested = remember(investments) { investments.sumOf { it.amountInvested } }
    val totalCurrent = remember(investments) { investments.sumOf { it.currentValue } }
    val totalGainLoss = totalCurrent - totalInvested
    val totalGainLossPercent = if (totalInvested > 0.0) (totalGainLoss / totalInvested) * 100.0 else 0.0

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Net wealth portfolio overview card
            PortfolioSummaryCard(
                totalInvested = totalInvested,
                totalCurrent = totalCurrent,
                totalGainLoss = totalGainLoss,
                totalGainLossPercent = totalGainLossPercent,
                currencyFormat = currencyFormat,
                lang = lang
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (lang == "ID") "Aset Portfolio Anda" else "Your Asset Portfolio",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            if (investments.isEmpty()) {
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
                            imageVector = Icons.Default.Analytics,
                            contentDescription = "No Investments",
                            modifier = Modifier.size(72.dp),
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (lang == "ID") "Belum ada pencatatan investasi" else "No tracked investments",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (lang == "ID") {
                                "Catat deposito bank, cryptocurrency, reksa dana, hingga emas batangan dalam satu dashboard portfolio terintegrasi."
                            } else {
                                "Record stock indexes, gold reserves, yield deposits, or tokens to see aggregated profit and asset percentage distributions."
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
                    items(investments, key = { it.id }) { inv ->
                        InvestmentCard(
                            inv = inv,
                            currencyFormat = currencyFormat,
                            lang = lang,
                            onDelete = { viewModel.deleteInvestment(inv) }
                        )
                    }
                }
            }
        }

        // Add Floating Button
        LargeFloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("add_investment_fab"),
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add investment",
                modifier = Modifier.size(28.dp)
            )
        }
    }

    if (showAddDialog) {
        AddInvestmentDialog(
            lang = lang,
            onDismiss = { showAddDialog = false },
            onSave = { name, type, invested, current, yieldPercent, note ->
                viewModel.addInvestment(name, type, invested, current, yieldPercent, note)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun PortfolioSummaryCard(
    totalInvested: Double,
    totalCurrent: Double,
    totalGainLoss: Double,
    totalGainLossPercent: Double,
    currencyFormat: NumberFormat,
    lang: String
) {
    val isDark = MaterialTheme.colorScheme.primary == com.example.ui.theme.DarkPrimary
    val profitColor = if (isDark) ProfitGreenDark else ProfitGreen
    val expenseColor = if (isDark) ExpenseRedDark else ExpenseRed

    val gainLossColor = if (totalGainLoss >= 0.0) profitColor else expenseColor
    val gainLossPrefix = if (totalGainLoss >= 0.0) "+" else ""

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("portfolio_summary_card"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = Translate.getString("growth_portfolio", lang).uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currencyFormat.format(totalCurrent),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Gain/Loss dynamic badge representation
                Box(
                    modifier = Modifier
                        .background(gainLossColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "$gainLossPrefix${String.format("%.2f", totalGainLossPercent)}%",
                        color = gainLossColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = Translate.getString("amount_invested", lang),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = currencyFormat.format(totalInvested),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text(
                        text = Translate.getString("profit_loss", lang),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$gainLossPrefix${currencyFormat.format(totalGainLoss)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = gainLossColor
                    )
                }
            }
        }
    }
}

@Composable
fun InvestmentCard(
    inv: InvestmentEntity,
    currencyFormat: NumberFormat,
    lang: String,
    onDelete: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.primary == com.example.ui.theme.DarkPrimary
    val profitColor = if (isDark) ProfitGreenDark else ProfitGreen
    val expenseColor = if (isDark) ExpenseRedDark else ExpenseRed

    val gainLossValue = inv.currentValue - inv.amountInvested
    val gainLossColor = if (gainLossValue >= 0.0) profitColor else expenseColor
    val gainLossPrefix = if (gainLossValue >= 0.0) "+" else ""
    val yieldPercent = if (inv.amountInvested > 0.0) (gainLossValue / inv.amountInvested) * 100.0 else 0.0

    val badgeText = when (inv.type) {
        "DEPOSITO" -> Translate.getString("deposito", lang)
        "CRYPTO" -> Translate.getString("crypto", lang)
        "REKSADANA" -> Translate.getString("reksadana", lang)
        else -> Translate.getString("emas", lang)
    }

    val badgeBg = when (inv.type) {
        "DEPOSITO" -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        "CRYPTO" -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
        "REKSADANA" -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
        else -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
    }

    val icon = when (inv.type) {
        "DEPOSITO" -> Icons.Default.AccountBalance
        "CRYPTO" -> Icons.Default.CurrencyBitcoin
        "REKSADANA" -> Icons.Default.ShowChart
        else -> Icons.Default.WorkspacePremium
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("investment_card_${inv.id}"),
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
                        .size(40.dp)
                        .background(badgeBg, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = inv.type,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = inv.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = badgeText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (inv.note.isNotBlank()) {
                            Text(text = "•", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                            Text(
                                text = inv.note,
                                fontSize = 11.sp,
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))

            Spacer(modifier = Modifier.height(10.dp))

            // Investment financial comparisons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (lang == "ID") "Modal Investasi" else "Principal Value",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = currencyFormat.format(inv.amountInvested),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Column(modifier = Modifier.weight(1.2f), horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (lang == "ID") "Estimasi Nilai" else "Current Value",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = currencyFormat.format(inv.currentValue),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "$gainLossPrefix${currencyFormat.format(gainLossValue)} ($gainLossPrefix${String.format("%.1f", yieldPercent)}%)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = gainLossColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun AddInvestmentDialog(
    lang: String,
    onDismiss: () -> Unit,
    onSave: (String, String, Double, Double, Double, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("DEPOSITO") } // "DEPOSITO", "CRYPTO", "REKSADANA", "EMAS"
    var investedStr by remember { mutableStateOf("") }
    var currentStr by remember { mutableStateOf("") }
    var yieldPercentStr by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    val instrumentTypes = listOf("DEPOSITO", "CRYPTO", "REKSADANA", "EMAS")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("add_investment_dialog")
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = Translate.getString("add_investment", lang),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Name Input
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(Translate.getString("asset_name", lang)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_inv_name"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                // Instrument Selection
                item {
                    Text(
                        text = Translate.getString("asset_type", lang),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        instrumentTypes.forEach { item ->
                            val isSelected = type == item
                            val label = when (item) {
                                "DEPOSITO" -> Translate.getString("deposito", lang)
                                "CRYPTO" -> Translate.getString("crypto", lang)
                                "REKSADANA" -> Translate.getString("reksadana", lang)
                                else -> Translate.getString("emas", lang)
                            }
                            FilterChip(
                                selected = isSelected,
                                onClick = { type = item },
                                label = { Text(label) },
                                modifier = Modifier.testTag("chip_inv_type_$item")
                            )
                        }
                    }
                }

                // Input Invested Base
                item {
                    OutlinedTextField(
                        value = investedStr,
                        onValueChange = { investedStr = it },
                        label = { Text(Translate.getString("amount_invested", lang)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_inv_invested"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                // Input Current Value
                item {
                    OutlinedTextField(
                        value = currentStr,
                        onValueChange = { currentStr = it },
                        label = { Text(Translate.getString("current_value", lang)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_inv_current"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                // Input APY Target rate
                item {
                    OutlinedTextField(
                        value = yieldPercentStr,
                        onValueChange = { yieldPercentStr = it },
                        label = { Text(Translate.getString("annual_yield", lang)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_inv_yield"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                // Input note details
                item {
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text(Translate.getString("note", lang)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_inv_note"),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 2
                    )
                }

                // Dialog Action Buttons save & cancel
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("inv_cancel_btn")
                        ) {
                            Text(Translate.getString("cancel", lang))
                        }

                        Button(
                            onClick = {
                                val investedAmt = investedStr.toDoubleOrNull() ?: 0.0
                                val currentVal = currentStr.toDoubleOrNull() ?: investedAmt
                                val apY = yieldPercentStr.toDoubleOrNull() ?: 0.0
                                if (name.isNotBlank() && investedAmt > 0.0) {
                                    onSave(name, type, investedAmt, currentVal, apY, note)
                                }
                            },
                            enabled = name.isNotBlank() && (investedStr.toDoubleOrNull() ?: 0.0) > 0.0,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("inv_save_btn")
                        ) {
                            Text(Translate.getString("save", lang))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement,
        content = { content() }
    )
}
