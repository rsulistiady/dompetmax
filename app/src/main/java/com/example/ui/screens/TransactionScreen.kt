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
import com.example.data.model.TransactionEntity
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpenseRedDark
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.ProfitGreenDark
import com.example.ui.util.Translate
import com.example.ui.viewmodel.FinanceViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val lang by viewModel.currentLanguage.collectAsState()
    val transactions by viewModel.transactions.collectAsState()

    val isDark = MaterialTheme.colorScheme.primary == com.example.ui.theme.DarkPrimary
    val profitColor = if (isDark) ProfitGreenDark else ProfitGreen
    val expenseColor = if (isDark) ExpenseRedDark else ExpenseRed

    var filterType by remember { mutableStateOf("ALL") } // "ALL", "INCOME", "EXPENSE"
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    val currencyFormat = rememberCurrencyFormatter(lang)
    val sdf = rememberDateFormat()

    // Filter calculations
    val filteredTransactions = remember(transactions, filterType, searchQuery) {
        transactions.filter { tx ->
            val matchesType = when (filterType) {
                "INCOME" -> tx.type == "INCOME"
                "EXPENSE" -> tx.type == "EXPENSE"
                else -> true
            }
            val matchesQuery = tx.title.contains(searchQuery, ignoreCase = true) || 
                               tx.category.contains(searchQuery, ignoreCase = true) ||
                               tx.note.contains(searchQuery, ignoreCase = true)
            matchesType && matchesQuery
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Search OutlinedTextField
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text(if (lang == "ID") "Cari transaksi..." else "Search transactions...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_bar")
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            // Filter Tabs Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filterType == "ALL",
                    onClick = { filterType = "ALL" },
                    label = { Text(Translate.getString("all", lang)) },
                    modifier = Modifier.testTag("filter_all")
                )
                FilterChip(
                    selected = filterType == "INCOME",
                    onClick = { filterType = "INCOME" },
                    label = { Text(Translate.getString("income", lang)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = "Income",
                            modifier = Modifier.size(16.dp),
                            tint = if (filterType == "INCOME") profitColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.testTag("filter_income")
                )
                FilterChip(
                    selected = filterType == "EXPENSE",
                    onClick = { filterType = "EXPENSE" },
                    label = { Text(Translate.getString("expense", lang)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = "Expense",
                            modifier = Modifier.size(16.dp),
                            tint = if (filterType == "EXPENSE") expenseColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.testTag("filter_expense")
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Transaction log list
            if (filteredTransactions.isEmpty()) {
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
                            imageVector = Icons.Default.Inbox,
                            contentDescription = "Empty",
                            modifier = Modifier.size(72.dp),
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (lang == "ID") "Belum ada transaksi" else "No transactions found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = Translate.getString("empty_data_desc", lang),
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
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 80.dp)
                ) {
                    items(filteredTransactions, key = { it.id }) { tx ->
                        TransactionRow(
                            tx = tx,
                            currencyFormat = currencyFormat,
                            sdf = sdf,
                            lang = lang,
                            onDelete = { viewModel.deleteTransaction(it) }
                        )
                    }
                }
            }
        }

        // Floating Action Button
        LargeFloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("add_transaction_fab"),
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add transaction",
                modifier = Modifier.size(28.dp)
            )
        }
    }

    if (showAddDialog) {
        AddTransactionDialog(
            lang = lang,
            onDismiss = { showAddDialog = false },
            onSave = { title, amount, type, category, note ->
                viewModel.addTransaction(title, amount, type, category, System.currentTimeMillis(), note)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun AddTransactionDialog(
    lang: String,
    onDismiss: () -> Unit,
    onSave: (String, Double, String, String, String) -> Unit
) {
    val isDark = MaterialTheme.colorScheme.primary == com.example.ui.theme.DarkPrimary
    val profitColor = if (isDark) ProfitGreenDark else ProfitGreen
    val expenseColor = if (isDark) ExpenseRedDark else ExpenseRed

    var title by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("EXPENSE") } // "EXPENSE" or "INCOME"
    var category by remember { mutableStateOf("Makanan") }
    var note by remember { mutableStateOf("") }

    val categories = if (type == "EXPENSE") {
        listOf("Makanan", "Transportasi", "Belanja", "Hiburan", "Lainnya")
    } else {
        listOf("Gaji", "Investasi", "Lainnya")
    }

    // Set fallback category when type shifts
    LaunchedEffect(type) {
        if (type == "EXPENSE") {
            category = "Makanan"
        } else {
            category = "Gaji"
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("add_transaction_dialog")
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                item {
                    Text(
                        text = Translate.getString("add_transaction", lang),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Type selector Segmented Buttons (simulated)
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { type = "EXPENSE" },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("type_btn_expense"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (type == "EXPENSE") MaterialTheme.colorScheme.error else Color.Transparent,
                                contentColor = if (type == "EXPENSE") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text(Translate.getString("expense", lang))
                        }

                        Button(
                            onClick = { type = "INCOME" },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("type_btn_income"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (type == "INCOME") profitColor else Color.Transparent,
                                contentColor = if (type == "INCOME") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text(Translate.getString("income", lang))
                        }
                    }
                }

                // Input title
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(if (lang == "ID") "Keterangan Transaksi" else "Transaction source/target") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_title"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                // Input amount
                item {
                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it },
                        label = { Text(Translate.getString("amount", lang)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_amount"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                // Category selector chip grid
                item {
                    Text(
                        text = Translate.getString("category", lang),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { cat ->
                            val isSelected = category == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { category = cat },
                                label = { Text(Translate.getString(cat, lang)) },
                                modifier = Modifier.testTag("chip_cat_$cat")
                            )
                        }
                    }
                }

                // Input note
                item {
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text(Translate.getString("note", lang)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_note"),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 2
                    )
                }

                // Buttons Save & Cancel
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("cancel_btn")
                        ) {
                            Text(Translate.getString("cancel", lang))
                        }

                        Button(
                            onClick = {
                                val amt = amountStr.toDoubleOrNull() ?: 0.0
                                if (title.isNotBlank() && amt > 0.0) {
                                    onSave(title, amt, type, category, note)
                                }
                            },
                            enabled = title.isNotBlank() && (amountStr.toDoubleOrNull() ?: 0.0) > 0.0,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("save_btn")
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
