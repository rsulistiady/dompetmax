package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val amount: Double,
    val type: String, // "INCOME" or "EXPENSE"
    val category: String, // "Makanan", "Transportasi", "Belanja", "Gaji", "Investasi", "Hiburan", "Lainnya"
    val dateMillis: Long,
    val note: String
) : Serializable

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val amount: Double,
    val billingCycle: String, // "WEEKLY", "MONTHLY", "ANNUALLY"
    val nextDueDateMillis: Long,
    val isReminderActive: Boolean
) : Serializable

@Entity(tableName = "investments")
data class InvestmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: String, // "DEPOSITO", "CRYPTO", "REKSADANA", "EMAS"
    val amountInvested: Double,
    val currentValue: Double,
    val annualYieldPercent: Double,
    val note: String
) : Serializable
