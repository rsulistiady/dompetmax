package com.example.ui.util

object Translate {
    private val keysID = mapOf(
        "dashboard" to "Ringkasan",
        "transactions" to "Transaksi",
        "subscriptions" to "Tagihan",
        "investments" to "Investasi",
        "settings" to "Pengaturan",
        
        // Dashboard
        "total_wealth" to "Estimasi Kekayaan",
        "total_balance" to "Saldo Kas",
        "total_investment" to "Nilai Investasi",
        "monthly_income" to "Pemasukan Bulan Ini",
        "monthly_expense" to "Pengeluaran Bulan Ini",
        "recent_tx" to "Transaksi Terakhir",
        "no_recent_tx" to "Belum ada transaksi di database.",
        "active_subs" to "Tagihan Berikutnya",
        "no_active_subs" to "Tidak ada tagihan aktif terdekat.",
        "growth_portfolio" to "Pertumbuhan Portfolio",
        
        // Common Labels & Forms
        "income" to "Pemasukan",
        "expense" to "Pengeluaran",
        "amount" to "Nominal (Rp)",
        "title" to "Nama / Keterangan",
        "category" to "Kategori",
        "date" to "Tanggal",
        "note" to "Catatan Opsional",
        "save" to "Simpan",
        "cancel" to "Batal",
        "delete" to "Hapus",
        "add_item" to "Tambah Data",
        "edit_item" to "Ubah Data",
        "all" to "Semua",
        "total" to "Total",
        
        // Categories
        "Makanan" to "Makanan & Minuman",
        "Transportasi" to "Transportasi",
        "Belanja" to "Belanja Pengeluaran",
        "Gaji" to "Pendapatan Gaji",
        "Investasi" to "Investasi Masuk/Keluar",
        "Hiburan" to "Hiburan & Rekreasi",
        "Lainnya" to "Lain-lain",

        // Add dialogues
        "add_transaction" to "Catat Pemasukan / Pengeluaran",
        "add_subscription" to "Tambah Layanan Berlangganan",
        "add_investment" to "Tambah Aset Investasi",

        // Subscriptions
        "billing_cycle" to "Siklus Tagihan",
        "weekly" to "Mingguan",
        "monthly" to "Bulanan",
        "annually" to "Tahunan",
        "next_due" to "Jatuh Tempo: ",
        "activate_reminders" to "Aktifkan Pengingat",
        "days_left" to "hari lagi",
        "billing_cycle_label" to "Frekuensi Berulang",
        
        // Investments
        "asset_name" to "Nama Aset / Institusi",
        "asset_type" to "Jenis Instrumen",
        "amount_invested" to "Modal Investasi (Rp)",
        "current_value" to "Nilai Saat Ini (Rp)",
        "annual_yield" to "Ekspektasi Return / Bunga per Tahun (%)",
        "profit_loss" to "Keuntungan / Kerugian",
        "deposito" to "Deposito Berjangka",
        "crypto" to "Cryptocurrency",
        "reksadana" to "Reksa Dana",
        "emas" to "Logam Mulia (Emas)",
        
        // Settings & Offline Sync
        "offline_sync" to "Penyimpanan Lokal",
        "sync_status" to "Status Database",
        "synced" to "Penyimpanan Lokal Aktif",
        "syncing" to "Memperbarui Database...",
        "needs_sync" to "Perubahan Belum Disimpan",
        "last_synced" to "Terakhir di-sync: ",
        "sync_now" to "Sinkronkan Sekarang",
        "notifications" to "Notifikasi & Pengingat",
        "push_reminders" to "Notifikasi Push (Firebase FCM)",
        "due_reminders" to "Pengingat Lokal (Jatuh Tempo Berlangganan)",
        "language" to "Bahasa Aplikasi",
        "theme" to "Tema Aplikasi",
        "system_default" to "Ikuti Sistem Android",
        "light_mode" to "Mode Terang",
        "dark_mode" to "Mode Gelap",
        "experimental" to "Zona Bahaya",
        "reset_data" to "Reset ke Data Default",
        "reset_confirm" to "Apakah Anda yakin ingin mengatur ulang data keuangan ke setelan pabrik default?",
        "developer_mode" to "Hak Cipta DompetMax © 2026. Sesuai standar kepatuhan Google Play Store (Target Market: Indonesia).",

        // Empty state
        "empty_data_desc" to "Klik tombol tambahkan data di bawah ini untuk mulai mencatat keuangan Anda offline secara instan!"
    )

    private val keysEN = mapOf(
        "dashboard" to "Overview",
        "transactions" to "Transactions",
        "subscriptions" to "Bills",
        "investments" to "Investments",
        "settings" to "Settings",
        
        // Dashboard
        "total_wealth" to "Estimated Net Worth",
        "total_balance" to "Cash Balance",
        "total_investment" to "Investment Value",
        "monthly_income" to "Monthly Income",
        "monthly_expense" to "Monthly Expenses",
        "recent_tx" to "Recent Transactions",
        "no_recent_tx" to "No transaction history in database.",
        "active_subs" to "Upcoming Deliveries / Bills",
        "no_active_subs" to "No upcoming active subscriptions.",
        "growth_portfolio" to "Portfolio Growth",
        
        // Common Labels
        "income" to "Income",
        "expense" to "Expense",
        "amount" to "Amount (Rp)",
        "title" to "Title / Details",
        "category" to "Category",
        "date" to "Date",
        "note" to "Optional Note",
        "save" to "Save",
        "cancel" to "Cancel",
        "delete" to "Delete",
        "add_item" to "Add Record",
        "edit_item" to "Edit Record",
        "all" to "All",
        "total" to "Total",
        
        // Categories
        "Makanan" to "Food & Beverage",
        "Transportasi" to "Transportation",
        "Belanja" to "Shopping & Outlays",
        "Gaji" to "Salary Income",
        "Investasi" to "Investment In/Out",
        "Hiburan" to "Leisure & Entertainment",
        "Lainnya" to "Miscellaneous",

        // Add dialogues
        "add_transaction" to "Log Income / Expense",
        "add_subscription" to "Track Recurring Subscription",
        "add_investment" to "Record Asset Portfolio",

        // Subscriptions
        "billing_cycle" to "Billing Period",
        "weekly" to "Weekly",
        "monthly" to "Monthly",
        "annually" to "Annually",
        "next_due" to "Next Due: ",
        "activate_reminders" to "Enable Reminders",
        "days_left" to "days left",
        "billing_cycle_label" to "Recurrence Rate",
        
        // Investments
        "asset_name" to "Asset name / Institution",
        "asset_type" to "Asset Instrument",
        "amount_invested" to "Principal Investment (Rp)",
        "current_value" to "Current Valuation (Rp)",
        "annual_yield" to "Target Annual Yield (%)",
        "profit_loss" to "Profit / Loss",
        "deposito" to "Term Deposit",
        "crypto" to "Cryptocurrency",
        "reksadana" to "Mutual Funds",
        "emas" to "Physical Bullion (Gold)",
        
        // Settings
        "offline_sync" to "Local Storage",
        "sync_status" to "Database Status",
        "synced" to "Database Synced (Local Cache)",
        "syncing" to "Syncing Local Databases...",
        "needs_sync" to "Pending Changes (Local Cache)",
        "last_synced" to "Last Synced: ",
        "sync_now" to "Force Synchronize Now",
        "notifications" to "Alerts & Notifications",
        "push_reminders" to "Push Notifications (FCM Reminders)",
        "due_reminders" to "Local Expiry Signals (Subscription Due)",
        "language" to "Application Interface Language",
        "theme" to "System Theme Override",
        "system_default" to "Follow Android System Settings",
        "light_mode" to "Light Mode Active",
        "dark_mode" to "Dark Mode Active",
        "experimental" to "Developer Console",
        "reset_data" to "Purge and Reset to Default",
        "reset_confirm" to "Are you absolutely sure you want to hard reset all offline financial records and reload sample data?",
        "developer_mode" to "DompetMax App © 2026. Google Play Store Compliant Release (Target: Indonesia).",

        // Empty state
        "empty_data_desc" to "Click the action action button below to insert your first persistent entry offline!"
    )

    fun getString(key: String, lang: String): String {
        return if (lang == "ID") {
            keysID[key] ?: keysEN[key] ?: key
        } else {
            keysEN[key] ?: keysID[key] ?: key
        }
    }
}
