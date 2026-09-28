package com.skillbuilder.app.ui.screens.mentor

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.MonetizationOn
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Stars
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skillbuilder.app.data.local.AppSettings
import com.skillbuilder.app.data.local.AppThemeMode
import com.skillbuilder.app.data.local.RealTimeDataManager
import com.skillbuilder.app.data.local.SampleData
import com.skillbuilder.app.data.local.UserSession
import com.skillbuilder.app.domain.model.WalletTransaction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MentorWalletScreen(
    onBack: () -> Unit = {}
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val user by UserSession.currentUser.collectAsState()
    val themeMode by AppSettings.themeMode.collectAsState()

    val systemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        AppThemeMode.SYSTEM -> systemDark
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    // Design System Palette
    val screenBg = if (isDark) Color(0xFF1E1E1E) else Color(0xFFF8FAFC)
    val surfaceBg = if (isDark) Color(0xFF2B2B2B) else Color(0xFFFFFFFF)
    val textPrimary = if (isDark) Color(0xFFFFFFFF) else Color(0xFF0F172A)
    val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val borderCol = if (isDark) Color(0xFF383838) else Color(0xFFE2E8F0)
    val emeraldGreen = Color(0xFF10B981)
    val royalBlue = if (isDark) Color(0xFF3B82F6) else Color(0xFF2563EB)

    val realTimeTxs by RealTimeDataManager.transactionsFlow.collectAsState()
    val wallet = SampleData.mentorWallet
    val allTransactions = remember(realTimeTxs, wallet) { wallet.transactions }

    var isBalanceHidden by remember { mutableStateOf(false) }
    var showWithdrawDialog by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf(0) } // 0: All, 1: Earnings, 2: Payouts

    val filteredTxs = remember(allTransactions, selectedFilter) {
        when (selectedFilter) {
            1 -> allTransactions.filter { it.isCredit }
            2 -> allTransactions.filter { !it.isCredit }
            else -> allTransactions
        }
    }

    // Modal Withdrawal Dialog
    if (showWithdrawDialog) {
        WithdrawalDialog(
            currentBalance = wallet.balance,
            onDismiss = { showWithdrawDialog = false },
            onConfirmWithdraw = { amount, destination ->
                val num = amount.toIntOrNull() ?: 0
                val newTx = WalletTransaction(
                    id = "tx_payout_${System.currentTimeMillis()}",
                    title = "Bank Payout to $destination",
                    subtitle = "IMPS Instant Transfer · Ref #IMPS${System.currentTimeMillis().toString().takeLast(6)}",
                    amount = "-₹$amount",
                    date = "Today",
                    isCredit = false
                )
                RealTimeDataManager.addWalletTransaction(newTx)
                showWithdrawDialog = false
                Toast.makeText(context, "✅ Payout of ₹$amount initiated! Transferred to $destination.", Toast.LENGTH_LONG).show()
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // ==================== TOP APP BAR ====================
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "Mentor Wallet",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = textPrimary
                    )
                    Text(
                        text = "Financial Hub & Instant Creator Payouts",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = textSecondary
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = textPrimary
                    )
                }
            },
            actions = {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = emeraldGreen.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, emeraldGreen.copy(alpha = 0.3f)),
                    modifier = Modifier.padding(end = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Security,
                            contentDescription = null,
                            tint = emeraldGreen,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "256-Bit Escrow",
                            color = emeraldGreen,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = surfaceBg)
        )

        HorizontalDivider(color = borderCol, thickness = 0.8.dp)

        // ==================== SCROLLABLE WALLET CONTENT ====================
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ==================== 1. TITANIUM CREATOR CARD ====================
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(215.dp),
                    shape = RoundedCornerShape(22.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    border = BorderStroke(
                        1.2.dp,
                        if (isDark) Color(0xFFF59E0B).copy(alpha = 0.4f) else Color(0xFF60A5FA).copy(alpha = 0.5f)
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = if (isDark) {
                                        listOf(
                                            Color(0xFF0F172A),
                                            Color(0xFF1E293B),
                                            Color(0xFF0F172A)
                                        )
                                    } else {
                                        listOf(
                                            Color(0xFF1E3A8A),
                                            Color(0xFF2563EB),
                                            Color(0xFF1D4ED8)
                                        )
                                    }
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Card Top: Brand & Chip
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFFF59E0B).copy(alpha = 0.25f),
                                        border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f)),
                                        modifier = Modifier.size(width = 30.dp, height = 22.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Rounded.CreditCard,
                                                contentDescription = null,
                                                tint = Color(0xFFF59E0B),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "SKILLBUILDER CREATOR ELITE",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 1.2.sp,
                                            fontSize = 10.sp
                                        ),
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                }

                                IconButton(
                                    onClick = { isBalanceHidden = !isBalanceHidden },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isBalanceHidden) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                        contentDescription = "Toggle Balance",
                                        tint = Color.White.copy(alpha = 0.8f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            // Card Middle: Available Balance
                            Column {
                                Text(
                                    text = "AVAILABLE FOR PAYOUT",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        letterSpacing = 1.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = if (isBalanceHidden) "••••••••" else wallet.balance,
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 32.sp
                                    ),
                                    color = Color.White
                                )
                            }

                            // Card Bottom: Creator Name & Quick Payout Action
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = user.name.uppercase(),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp,
                                            fontSize = 12.sp
                                        ),
                                        color = Color.White
                                    )
                                    Text(
                                        text = "ID: CR-88219 · VERIFIED CREATOR",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = Color.White.copy(alpha = 0.65f)
                                    )
                                }

                                Button(
                                    onClick = { showWithdrawDialog = true },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White,
                                        contentColor = Color(0xFF0F172A)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Payments,
                                        contentDescription = null,
                                        tint = Color(0xFF0F172A),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Withdraw",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ==================== 2. PERFORMANCE & REVENUE GRID ====================
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Card 1: Monthly Revenue
                    WalletMetricCard(
                        title = "This Month",
                        value = wallet.monthlyRevenue,
                        subtitle = "+24.8% growth",
                        subtitleColor = emeraldGreen,
                        icon = Icons.AutoMirrored.Rounded.TrendingUp,
                        iconTint = emeraldGreen,
                        surfaceBg = surfaceBg,
                        borderCol = borderCol,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        modifier = Modifier.weight(1f)
                    )

                    // Card 2: Lifetime Earned
                    WalletMetricCard(
                        title = "Total Earned",
                        value = wallet.totalEarned,
                        subtitle = "Lifetime sales",
                        subtitleColor = textSecondary,
                        icon = Icons.Rounded.MonetizationOn,
                        iconTint = royalBlue,
                        surfaceBg = surfaceBg,
                        borderCol = borderCol,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Card 3: Active Students
                    WalletMetricCard(
                        title = "Paid Learners",
                        value = "148 Students",
                        subtitle = "Across all courses",
                        subtitleColor = textSecondary,
                        icon = Icons.Rounded.People,
                        iconTint = Color(0xFF8B5CF6),
                        surfaceBg = surfaceBg,
                        borderCol = borderCol,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        modifier = Modifier.weight(1f)
                    )

                    // Card 4: In Escrow
                    WalletMetricCard(
                        title = "In Escrow",
                        value = wallet.pendingPayout,
                        subtitle = "Clearing in 48h",
                        subtitleColor = Color(0xFFF59E0B),
                        icon = Icons.Rounded.Shield,
                        iconTint = Color(0xFFF59E0B),
                        surfaceBg = surfaceBg,
                        borderCol = borderCol,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ==================== 3. VERIFIED PAYOUT DESTINATION ====================
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = surfaceBg),
                    border = BorderStroke(1.dp, borderCol)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = royalBlue.copy(alpha = 0.12f),
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.AccountBalance,
                                    contentDescription = null,
                                    tint = royalBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "HDFC Bank •••• 4021",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = textPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Rounded.Verified,
                                    contentDescription = "Verified",
                                    tint = emeraldGreen,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "IFSC: HDFC0001234 · 0% Transfer Fee",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = textSecondary
                            )
                        }

                        OutlinedButton(
                            onClick = { showWithdrawDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            border = BorderStroke(1.dp, borderCol)
                        ) {
                            Text("Payout", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = royalBlue)
                        }
                    }
                }
            }

            // ==================== 4. TRANSACTION LEDGER ====================
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TRANSACTION HISTORY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        ),
                        color = textSecondary
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = surfaceBg,
                        border = BorderStroke(1.dp, borderCol)
                    ) {
                        Text(
                            text = "${allTransactions.size} Records",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                            color = textSecondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Filter Tabs
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("All", "Earnings (+)", "Debits (-)").forEachIndexed { index, title ->
                        val isSelected = selectedFilter == index
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFilter = index },
                            label = { Text(title, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            shape = RoundedCornerShape(8.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = royalBlue,
                                selectedLabelColor = Color.White,
                                containerColor = surfaceBg,
                                labelColor = textSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) royalBlue else borderCol
                            )
                        )
                    }
                }
            }

            // Transaction Cards
            if (filteredTxs.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = surfaceBg),
                        border = BorderStroke(1.dp, borderCol)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AccountBalanceWallet,
                                contentDescription = null,
                                tint = textSecondary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No transactions found in this category",
                                style = MaterialTheme.typography.bodyMedium,
                                color = textSecondary
                            )
                        }
                    }
                }
            } else {
                items(filteredTxs, key = { it.id }) { tx ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = surfaceBg),
                        border = BorderStroke(1.dp, borderCol)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (tx.isCredit) emeraldGreen.copy(alpha = 0.15f)
                                            else Color(0xFFEF4444).copy(alpha = 0.15f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (tx.isCredit) Icons.Rounded.ArrowDownward else Icons.Rounded.ArrowUpward,
                                        contentDescription = null,
                                        tint = if (tx.isCredit) emeraldGreen else Color(0xFFEF4444),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = tx.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = textPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = tx.subtitle,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = textSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = tx.date,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = textSecondary.copy(alpha = 0.7f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = tx.amount,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                    color = if (tx.isCredit) emeraldGreen else Color(0xFFEF4444)
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (tx.isCredit) emeraldGreen.copy(alpha = 0.12f) else borderCol.copy(alpha = 0.5f)
                                ) {
                                    Text(
                                        text = "Completed",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                        color = if (tx.isCredit) emeraldGreen else textSecondary,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WalletMetricCard(
    title: String,
    value: String,
    subtitle: String,
    subtitleColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    surfaceBg: Color,
    borderCol: Color,
    textPrimary: Color,
    textSecondary: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceBg),
        border = BorderStroke(1.dp, borderCol)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                    color = textSecondary
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = 16.sp),
                color = textPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Medium),
                color = subtitleColor
            )
        }
    }
}

@Composable
private fun WithdrawalDialog(
    currentBalance: String,
    onDismiss: () -> Unit,
    onConfirmWithdraw: (amount: String, destination: String) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf("HDFC Bank •••• 4021") }

    val quickAmounts = listOf("2000", "5000", "10000", "25000")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Payments,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Withdraw Creator Funds", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Available Balance:", style = MaterialTheme.typography.bodySmall)
                        Text(
                            text = currentBalance,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Quick Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickAmounts.forEach { amt ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (amount == amt) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { amount = amt }
                        ) {
                            Text(
                                text = "₹$amt",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (amount == amt) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(vertical = 6.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amount,
                    onValueChange = { if (it.all { char -> char.isDigit() }) amount = it },
                    label = { Text("Enter Amount") },
                    prefix = { Text("₹ ", fontWeight = FontWeight.Bold) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Text(
                    text = "Transfer to:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AccountBalance,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = selectedMethod,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (amount.isBlank() || (amount.toIntOrNull() ?: 0) < 100) {
                        return@Button
                    }
                    onConfirmWithdraw(amount, selectedMethod)
                },
                enabled = amount.isNotBlank() && (amount.toIntOrNull() ?: 0) >= 100,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Confirm Payout", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
