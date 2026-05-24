package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.InvestmentEntity
import com.example.data.model.SubscriptionEntity
import com.example.data.model.TransactionEntity
import com.example.data.repository.FinanceRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FinanceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FinanceRepository

    val transactions: StateFlow<List<TransactionEntity>>
    val subscriptions: StateFlow<List<SubscriptionEntity>>
    val investments: StateFlow<List<InvestmentEntity>>

    // Settings & Features UI State
    private val _currentLanguage = MutableStateFlow("ID") // "ID" for Indonesian, "EN" for English
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    private val _themeSelection = MutableStateFlow("SYSTEM") // "SYSTEM", "LIGHT", "DARK"
    val themeSelection: StateFlow<String> = _themeSelection.asStateFlow()

    private val _syncStatus = MutableStateFlow("SYNCED") // "SYNCED", "SYNCING", "NEEDS_SYNC", "ERROR"
    val syncStatus: StateFlow<String> = _syncStatus.asStateFlow()

    private val _lastSyncTime = MutableStateFlow(System.currentTimeMillis() - 240000) // 4 min ago
    val lastSyncTime: StateFlow<Long> = _lastSyncTime.asStateFlow()

    private val _pushNotificationEnabled = MutableStateFlow(true)
    val pushNotificationEnabled: StateFlow<Boolean> = _pushNotificationEnabled.asStateFlow()

    private val _reminderNotificationEnabled = MutableStateFlow(true)
    val reminderNotificationEnabled: StateFlow<Boolean> = _reminderNotificationEnabled.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = FinanceRepository(
            database.transactionDao(),
            database.subscriptionDao(),
            database.investmentDao()
        )

        transactions = repository.allTransactions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        subscriptions = repository.allSubscriptions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        investments = repository.allInvestments.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Seed default beautiful data if Database is entirely empty
        viewModelScope.launch {
            // Check if seeding is needed
            delay(100) // Allow DB flow initialization
            if (transactions.value.isEmpty() && subscriptions.value.isEmpty() && investments.value.isEmpty()) {
                seedInitialData()
            }
        }
    }

    private suspend fun seedInitialData() {
        val now = System.currentTimeMillis()

        // Seed Transactions
        val list = listOf(
            TransactionEntity(
                title = "Gaji Bulanan PT Maju Jaya",
                amount = 8500000.0,
                type = "INCOME",
                category = "Gaji",
                dateMillis = now - 86400000 * 2,
                note = "Gaji bersih bulan Mei"
            ),
            TransactionEntity(
                title = "Makan Siang Nasi Padang",
                amount = 45000.0,
                type = "EXPENSE",
                category = "Makanan",
                dateMillis = now - 3600000 * 4,
                note = "Lauk rendang + es teh"
            ),
            TransactionEntity(
                title = "Bensin Pertamax Motor",
                amount = 50000.0,
                type = "EXPENSE",
                category = "Transportasi",
                dateMillis = now - 86400000,
                note = "Full tank bensin"
            ),
            TransactionEntity(
                title = "Belanja Mingguan Indomaret",
                amount = 230000.0,
                type = "EXPENSE",
                category = "Belanja",
                dateMillis = now - 86400000 * 3,
                note = "Sabun, minyak goreng, cemilan"
            ),
            TransactionEntity(
                title = "Keuntungan Dividen Reksa Dana",
                amount = 120000.0,
                type = "INCOME",
                category = "Investasi",
                dateMillis = now - 86400000 * 5,
                note = "Dividen reksadana pasar uang"
            )
        )
        for (tx in list) {
            repository.insertTransaction(tx)
        }

        // Seed Subscriptions (Layanan Berlangganan)
        val defaultSubs = listOf(
            SubscriptionEntity(
                name = "Netflix Premium",
                amount = 186000.0,
                billingCycle = "MONTHLY",
                nextDueDateMillis = now + 86400000 * 5, // 5 days from now
                isReminderActive = true
            ),
            SubscriptionEntity(
                name = "Spotify Family",
                amount = 86000.0,
                billingCycle = "MONTHLY",
                nextDueDateMillis = now + 86400000 * 12, // 12 days from now
                isReminderActive = true
            ),
            SubscriptionEntity(
                name = "Keanggotaan Gym",
                amount = 350000.0,
                billingCycle = "MONTHLY",
                nextDueDateMillis = now + 86400000 * 18,
                isReminderActive = false
            )
        )
        for (sub in defaultSubs) {
            repository.insertSubscription(sub)
        }

        // Seed Investments (Deposito, Crypto, Reksa Dana, Emas)
        val defaultInvestments = listOf(
            InvestmentEntity(
                name = "Deposito Bank Mandiri",
                type = "DEPOSITO",
                amountInvested = 15000000.0,
                currentValue = 15350000.0,
                annualYieldPercent = 4.75,
                note = "Tenor 6 bulan bunga bulanan"
            ),
            InvestmentEntity(
                name = "Portfolio Bitcoin (BTC)",
                type = "CRYPTO",
                amountInvested = 2500000.0,
                currentValue = 2820000.0,
                annualYieldPercent = 15.4,
                note = "Hold jangka panjang di cold wallet"
            ),
            InvestmentEntity(
                name = "Reksa Dana Bahana Syariah",
                type = "REKSADANA",
                amountInvested = 5000000.0,
                currentValue = 5180000.0,
                annualYieldPercent = 6.2,
                note = "Investasi otomatis bulanan"
            ),
            InvestmentEntity(
                name = "Antam Emas Fisik",
                type = "EMAS",
                amountInvested = 8000000.0,
                currentValue = 8320000.0,
                annualYieldPercent = 8.5,
                note = "Kepingan 5 gram di brankas"
            )
        )
        for (inv in defaultInvestments) {
            repository.insertInvestment(inv)
        }
    }

    // Transaction Handlers
    fun addTransaction(title: String, amount: Double, type: String, category: String, dateMillis: Long, note: String) {
        viewModelScope.launch {
            repository.insertTransaction(
                TransactionEntity(
                    title = title,
                    amount = amount,
                    type = type,
                    category = category,
                    dateMillis = dateMillis,
                    note = note
                )
            )
            triggerNeedsSync()
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
            triggerNeedsSync()
        }
    }

    // Subscription Handlers
    fun addSubscription(name: String, amount: Double, billingCycle: String, nextDueDateMillis: Long, isReminderActive: Boolean) {
        viewModelScope.launch {
            repository.insertSubscription(
                SubscriptionEntity(
                    name = name,
                    amount = amount,
                    billingCycle = billingCycle,
                    nextDueDateMillis = nextDueDateMillis,
                    isReminderActive = isReminderActive
                )
            )
            triggerNeedsSync()
        }
    }

    fun toggleSubscriptionReminder(subscription: SubscriptionEntity) {
        viewModelScope.launch {
            repository.insertSubscription(subscription.copy(isReminderActive = !subscription.isReminderActive))
        }
    }

    fun deleteSubscription(subscription: SubscriptionEntity) {
        viewModelScope.launch {
            repository.deleteSubscription(subscription)
            triggerNeedsSync()
        }
    }

    // Investment Handlers
    fun addInvestment(name: String, type: String, amountInvested: Double, currentValue: Double, annualYieldPercent: Double, note: String) {
        viewModelScope.launch {
            repository.insertInvestment(
                InvestmentEntity(
                    name = name,
                    type = type,
                    amountInvested = amountInvested,
                    currentValue = currentValue,
                    annualYieldPercent = annualYieldPercent,
                    note = note
                )
            )
            triggerNeedsSync()
        }
    }

    fun deleteInvestment(investment: InvestmentEntity) {
        viewModelScope.launch {
            repository.deleteInvestment(investment)
            triggerNeedsSync()
        }
    }

    // Synchronizer Simulation
    fun triggerSync() {
        viewModelScope.launch {
            _syncStatus.value = "SYNCING"
            // Simulate background network request
            delay(1500)
            _syncStatus.value = "SYNCED"
            _lastSyncTime.value = System.currentTimeMillis()
        }
    }

    private fun triggerNeedsSync() {
        if (_syncStatus.value == "SYNCED") {
            _syncStatus.value = "NEEDS_SYNC"
        }
    }

    // Preferences modifications
    fun setLanguage(lang: String) {
        _currentLanguage.value = lang
    }

    fun setThemeSelection(theme: String) {
        _themeSelection.value = theme
    }

    fun togglePushNotifications() {
        _pushNotificationEnabled.value = !_pushNotificationEnabled.value
    }

    fun toggleReminders() {
        _reminderNotificationEnabled.value = !_reminderNotificationEnabled.value
    }

    fun forceResetData() {
        viewModelScope.launch {
            repository.clearAll()
            seedInitialData()
            _syncStatus.value = "SYNCED"
        }
    }
}
