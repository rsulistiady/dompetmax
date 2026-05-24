package com.example.data.repository

import com.example.data.local.InvestmentDao
import com.example.data.local.SubscriptionDao
import com.example.data.local.TransactionDao
import com.example.data.model.InvestmentEntity
import com.example.data.model.SubscriptionEntity
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.flow.Flow

class FinanceRepository(
    private val transactionDao: TransactionDao,
    private val subscriptionDao: SubscriptionDao,
    private val investmentDao: InvestmentDao
) {
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val allSubscriptions: Flow<List<SubscriptionEntity>> = subscriptionDao.getAllSubscriptions()
    val allInvestments: Flow<List<InvestmentEntity>> = investmentDao.getAllInvestments()

    suspend fun insertTransaction(transaction: TransactionEntity) {
        transactionDao.insertTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun insertSubscription(subscription: SubscriptionEntity) {
        subscriptionDao.insertSubscription(subscription)
    }

    suspend fun deleteSubscription(subscription: SubscriptionEntity) {
        subscriptionDao.deleteSubscription(subscription)
    }

    suspend fun insertInvestment(investment: InvestmentEntity) {
        investmentDao.insertInvestment(investment)
    }

    suspend fun deleteInvestment(investment: InvestmentEntity) {
        investmentDao.deleteInvestment(investment)
    }

    suspend fun clearAll() {
        transactionDao.deleteAll()
        subscriptionDao.deleteAll()
        investmentDao.deleteAll()
    }
}
