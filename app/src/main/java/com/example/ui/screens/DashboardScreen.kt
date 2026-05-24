package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InvestmentEntity
import com.example.data.model.SubscriptionEntity
import com.example.data.model.TransactionEntity
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpenseRedDark
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.ProfitGreenDark
import com.example.ui.util.Translate
import com.example.ui.viewmodel.FinanceViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: FinanceViewModel,
    onNavigateToTab: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val lang by viewModel.currentLanguage.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val subscriptions by viewModel.subscriptions.collectAsState()
    val investments by viewModel.investments.collectAsState()

    // Analytics calculations
    val totalIncome = transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
    val totalExpense = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    val cashBalance = totalIncome - totalExpense

    val totalInvestment = investments.sumOf { it.currentValue }
    val totalWealth = cashBalance + totalInvestment

    val currencyFormat = rememberCurrencyFormatter(lang)
    val sdf = rememberDateFormat()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Net Worth Card
        item {
            TotalNetWorthCard(
                totalWealth = totalWealth,
                cashBalance = cashBalance,
                totalInvestment = totalInvestment,
                currencyFormat = currencyFormat,
                lang = lang,
                onNavigateToTab = onNavigateToTab
            )
        }

        // 2. Budget Cash Flow indicator (In / Out row)
        item {
            CashFlowIndicator(
                income = totalIncome,
                expense = totalExpense,
                currencyFormat = currencyFormat,
                lang = lang,
                onNavigateToTab = onNavigateToTab
            )
        }

        // 3. Subscription Due Alert
        val nextDueSub = findUrgentSubscription(subscriptions)
        if (nextDueSub != null) {
            item {
                UrgentBillBanner(
                    sub = nextDueSub,
                    currencyFormat = currencyFormat,
                    lang = lang,
                    sdf = sdf,
                    onNavigateToTab = onNavigateToTab
                )
            }
        }

        // 4. Recent Transactions section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Translate.getString("recent_tx", lang),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                TextButton(onClick = { onNavigateToTab(1) }) {
                    Text(
                        text = if (lang == "ID") "Lihat Semua" else "View All",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        if (transactions.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = Translate.getString("no_recent_tx", lang),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            val limit = transactions.take(4)
            items(limit, key = { it.id }) { tx ->
                TransactionRow(
                    tx = tx,
                    currencyFormat = currencyFormat,
                    sdf = sdf,
                    lang = lang,
                    onDelete = { viewModel.deleteTransaction(it) }
                )
            }
        }

        // Quick tip info card
        item {
            TipCard(lang = lang)
        }
    }
}

@Composable
fun TotalNetWorthCard(
    totalWealth: Double,
    cashBalance: Double,
    totalInvestment: Double,
    currencyFormat: NumberFormat,
    lang: String,
    onNavigateToTab: (Int) -> Unit
) {
    val isDark = MaterialTheme.colorScheme.primary == com.example.ui.theme.DarkPrimary
    val gradientColors = if (isDark) {
        listOf(Color(0xFF4F378B), Color(0xFF381E72)) // Elegant Dark purple gradient from HTML
    } else {
        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
    }
    val emeraldBrush = Brush.linearGradient(colors = gradientColors)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("net_worth_card")
            .clip(RoundedCornerShape(24.dp))
            .clickable { onNavigateToTab(3) }, // Navigate to Investments
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(emeraldBrush)
                .padding(24.dp)
        ) {
            // Elegant background top-right glow bubble from design HTML
            if (isDark) {
                Box(
                    modifier = Modifier
                        .size(128.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 30.dp, y = (-30).dp)
                        .background(
                            color = Color(0xFFD0BCFF).copy(alpha = 0.12f),
                            shape = CircleShape
                        )
                )
            }

            Column {
                Text(
                    text = Translate.getString("total_wealth", lang).uppercase(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = currencyFormat.format(totalWealth),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = Translate.getString("total_balance", lang),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = currencyFormat.format(cashBalance),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                        Text(
                            text = Translate.getString("total_investment", lang),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = currencyFormat.format(totalInvestment),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CashFlowIndicator(
    income: Double,
    expense: Double,
    currencyFormat: NumberFormat,
    lang: String,
    onNavigateToTab: (Int) -> Unit
) {
    val isDark = MaterialTheme.colorScheme.primary == com.example.ui.theme.DarkPrimary
    val profitColor = if (isDark) com.example.ui.theme.ProfitGreenDark else com.example.ui.theme.ProfitGreen
    val expenseColor = if (isDark) com.example.ui.theme.ExpenseRedDark else com.example.ui.theme.ExpenseRed

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Income card
        Card(
            modifier = Modifier
                .weight(1f)
                .clickable { onNavigateToTab(1) },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(2.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            color = profitColor.copy(alpha = 0.15f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = "Income",
                        tint = profitColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = Translate.getString("monthly_income", lang),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = currencyFormat.format(income),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = profitColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Expense card
        Card(
            modifier = Modifier
                .weight(1f)
                .clickable { onNavigateToTab(1) },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(2.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            color = expenseColor.copy(alpha = 0.15f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "Expense",
                        tint = expenseColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = Translate.getString("monthly_expense", lang),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = currencyFormat.format(expense),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = expenseColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun UrgentBillBanner(
    sub: SubscriptionEntity,
    currencyFormat: NumberFormat,
    lang: String,
    sdf: SimpleDateFormat,
    onNavigateToTab: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .clickable { onNavigateToTab(2) }, // Subscriptions tab
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.errorContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Subscription warning",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = Translate.getString("active_subs", lang),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    text = sub.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${Translate.getString("next_due", lang)} ${sdf.format(Date(sub.nextDueDateMillis))}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = currencyFormat.format(sub.amount),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
fun TransactionRow(
    tx: TransactionEntity,
    currencyFormat: NumberFormat,
    sdf: SimpleDateFormat,
    lang: String,
    onDelete: (TransactionEntity) -> Unit
) {
    val isDark = MaterialTheme.colorScheme.primary == com.example.ui.theme.DarkPrimary
    val profitColor = if (isDark) com.example.ui.theme.ProfitGreenDark else com.example.ui.theme.ProfitGreen
    val expenseColor = if (isDark) com.example.ui.theme.ExpenseRedDark else com.example.ui.theme.ExpenseRed

    val isExpense = tx.type == "EXPENSE"
    val tintColor = if (isExpense) expenseColor else profitColor
    val amountIndicator = if (isExpense) "- " else "+ "
    val icon = categoryIcon(tx.category)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = tx.category,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tx.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = Translate.getString(tx.category, lang),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(text = "•", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                    Text(
                        text = sdf.format(Date(tx.dateMillis)),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "$amountIndicator${currencyFormat.format(tx.amount)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = tintColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                IconButton(
                    onClick = { onDelete(tx) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TipCard(lang: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Finance Tip",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = if (lang == "ID") "Tips Keuangan Pintar" else "Smart Money Tip",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 13.sp
                )
                Text(
                    text = if (lang == "ID") {
                        "Ikuti saran ekonom 50/30/20. Alokasikan 50% pendapatan untuk kebutuhan pokok, 30% hiburan & layanan subscription, dan 20% investasi masa depan."
                    } else {
                        "Follow the 50/30/20 savings rule. Allocate 50% of monthly income to needs, 30% to subscriptions/desires, and 20% to savings and investment packages."
                    },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

fun findUrgentSubscription(subs: List<SubscriptionEntity>): SubscriptionEntity? {
    if (subs.isEmpty()) return null
    // Find active subscription that has the minimum due date
    return subs.filter { it.isReminderActive }.minByOrNull { it.nextDueDateMillis }
}

fun categoryIcon(category: String): ImageVector {
    return when (category) {
        "Makanan" -> Icons.Default.Restaurant
        "Transportasi" -> Icons.Default.DirectionsCar
        "Belanja" -> Icons.Default.ShoppingBag
        "Gaji" -> Icons.Default.Payments
        "Investasi" -> Icons.Default.TrendingUp
        "Hiburan" -> Icons.Default.Movie
        else -> Icons.Default.Category
    }
}

@Composable
fun rememberCurrencyFormatter(lang: String): NumberFormat {
    return remember(lang) {
        val locale = Locale("id", "ID")
        NumberFormat.getCurrencyInstance(locale).apply {
            maximumFractionDigits = 0
        }
    }
}

@Composable
fun rememberDateFormat(): SimpleDateFormat {
    return remember {
        SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
    }
}
