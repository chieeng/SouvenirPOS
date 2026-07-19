package edu.cit.erag.souvenirpos.pos.ui
import edu.cit.erag.souvenirpos.pos.viewmodel.PosViewModel

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.cit.erag.souvenirpos.core.data.model.SaleResponse
import edu.cit.erag.souvenirpos.core.ui.peso
import edu.cit.erag.souvenirpos.core.ui.theme.Amber
import edu.cit.erag.souvenirpos.core.ui.theme.Faint
import edu.cit.erag.souvenirpos.core.ui.theme.Ink
import edu.cit.erag.souvenirpos.core.ui.theme.Line
import edu.cit.erag.souvenirpos.core.ui.theme.Muted
import edu.cit.erag.souvenirpos.core.ui.theme.Paper
import edu.cit.erag.souvenirpos.core.ui.theme.Sand
import edu.cit.erag.souvenirpos.core.ui.theme.Teal
import edu.cit.erag.souvenirpos.core.ui.theme.TealDark
import edu.cit.erag.souvenirpos.core.ui.theme.TealSoft
import kotlin.math.ceil

private enum class PosStep { SELL, CART, PAYMENT }

private val KEYPAD = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", ".", "0", "⌫")

private fun initials(name: String): String {
    val parts = name.trim().split(" ").filter { it.isNotEmpty() }
    return when {
        parts.isEmpty() -> "··"
        parts.size == 1 -> parts[0].take(2).uppercase()
        else -> (parts.first().take(1) + parts.last().take(1)).uppercase()
    }
}

@Composable
fun PosScreen(
    onLoggedOut: () -> Unit,
    onManageUsers: () -> Unit,
    onViewHistory: () -> Unit,
    onViewDashboard: () -> Unit,
    onManageCategories: () -> Unit,
    onChangePassword: () -> Unit,
    viewModel: PosViewModel = viewModel(),
) {
    var step by remember { mutableStateOf(PosStep.SELL) }

    BackHandler(enabled = step != PosStep.SELL) {
        step = if (step == PosStep.PAYMENT) PosStep.CART else PosStep.SELL
    }

    when (step) {
        PosStep.SELL -> SellStep(
            viewModel = viewModel,
            onOpenCart = { if (viewModel.cart.isNotEmpty()) step = PosStep.CART },
            onLoggedOut = onLoggedOut,
            onManageUsers = onManageUsers,
            onViewHistory = onViewHistory,
            onViewDashboard = onViewDashboard,
            onManageCategories = onManageCategories,
            onChangePassword = onChangePassword,
        )
        PosStep.CART -> CartStep(
            viewModel = viewModel,
            onBack = { step = PosStep.SELL },
            onCharge = { step = PosStep.PAYMENT },
        )
        PosStep.PAYMENT -> PaymentStep(
            viewModel = viewModel,
            onBack = { step = PosStep.CART },
        )
    }

    viewModel.receipt?.let { receipt ->
        ReceiptDialog(receipt) {
            viewModel.dismissReceipt()
            step = PosStep.SELL
        }
    }
}

/* ------------------------------------------------------------------ SELL */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SellStep(
    viewModel: PosViewModel,
    onOpenCart: () -> Unit,
    onLoggedOut: () -> Unit,
    onManageUsers: () -> Unit,
    onViewHistory: () -> Unit,
    onViewDashboard: () -> Unit,
    onManageCategories: () -> Unit,
    onChangePassword: () -> Unit,
) {
    Scaffold(
        containerColor = Sand,
        topBar = { SellHeader(viewModel.userName) },
        bottomBar = {
            Column {
                if (viewModel.cart.isNotEmpty()) {
                    CartBar(
                        count = viewModel.cart.size,
                        total = viewModel.total,
                        onClick = onOpenCart,
                    )
                }
                PosBottomNav(
                    isOwner = viewModel.isOwner,
                    onViewDashboard = onViewDashboard,
                    onViewHistory = onViewHistory,
                    onManageCategories = onManageCategories,
                    onManageUsers = onManageUsers,
                    onChangePassword = onChangePassword,
                    onLogout = { viewModel.logout(onLoggedOut) },
                )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            Text(
                "Choose category",
                style = MaterialTheme.typography.labelMedium,
                color = Faint,
            )
            Spacer(Modifier.height(10.dp))
            if (viewModel.loadingCategories) {
                CircularProgressIndicator(modifier = Modifier.height(24.dp), strokeWidth = 2.dp)
            } else {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    viewModel.categories.forEach { category ->
                        val selected = viewModel.selectedCategory?.id == category.id
                        CategoryChip(category.name, selected) { viewModel.selectCategory(category) }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            AmountCard("Item amount", viewModel.priceInput.toDoubleOrNull() ?: 0.0)

            Spacer(Modifier.height(16.dp))
            Keypad { viewModel.onNumpad(if (it == "⌫") PosViewModel.KEY_BACKSPACE else it) }

            viewModel.error?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = viewModel::addToCart,
                shape = RoundedCornerShape(13.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Paper),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) {
                Text("+  Add to sale", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun SellHeader(userName: String) {
    Surface(color = TealDark) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text("Sell", color = Paper, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("Point of sale", color = Color(0xFFEAFAF6).copy(alpha = 0.75f), fontSize = 12.sp)
            }
            AvatarCircle(initials(userName), Amber)
        }
    }
}

@Composable
private fun CartBar(count: Int, total: Double, onClick: () -> Unit) {
    Surface(color = Sand) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Teal)
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 13.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "$count item${if (count == 1) "" else "s"} · ${peso(total)}",
                color = Paper,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
            )
            Text("View cart →", color = Paper, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
private fun PosBottomNav(
    isOwner: Boolean,
    onViewDashboard: () -> Unit,
    onViewHistory: () -> Unit,
    onManageCategories: () -> Unit,
    onManageUsers: () -> Unit,
    onChangePassword: () -> Unit,
    onLogout: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    val navColors = NavigationBarItemDefaults.colors(
        selectedIconColor = TealDark,
        selectedTextColor = TealDark,
        indicatorColor = TealSoft,
        unselectedIconColor = Faint,
        unselectedTextColor = Faint,
    )
    NavigationBar(containerColor = Paper) {
        NavigationBarItem(
            selected = true,
            onClick = {},
            icon = { Icon(Icons.Filled.ShoppingCart, contentDescription = null) },
            label = { Text("Sell") },
            colors = navColors,
        )
        NavigationBarItem(
            selected = false,
            onClick = onViewDashboard,
            icon = { Icon(Icons.Filled.BarChart, contentDescription = null) },
            label = { Text("Dashboard") },
            colors = navColors,
        )
        NavigationBarItem(
            selected = false,
            onClick = onViewHistory,
            icon = { Icon(Icons.Filled.History, contentDescription = null) },
            label = { Text("Sales") },
            colors = navColors,
        )
        NavigationBarItem(
            selected = false,
            onClick = { menuOpen = true },
            icon = {
                Box {
                    Icon(Icons.Filled.Menu, contentDescription = null)
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        if (isOwner) {
                            DropdownMenuItem(
                                text = { Text("Categories") },
                                leadingIcon = { Icon(Icons.Filled.Category, contentDescription = null) },
                                onClick = { menuOpen = false; onManageCategories() },
                            )
                            DropdownMenuItem(
                                text = { Text("Team & access") },
                                leadingIcon = { Icon(Icons.Filled.ManageAccounts, contentDescription = null) },
                                onClick = { menuOpen = false; onManageUsers() },
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Change password") },
                            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                            onClick = { menuOpen = false; onChangePassword() },
                        )
                        DropdownMenuItem(
                            text = { Text("Sign out") },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
                            onClick = { menuOpen = false; onLogout() },
                        )
                    }
                }
            },
            label = { Text("Menu") },
            colors = navColors,
        )
    }
}

/* ------------------------------------------------------------------ CART */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CartStep(
    viewModel: PosViewModel,
    onBack: () -> Unit,
    onCharge: () -> Unit,
) {
    Scaffold(
        containerColor = Paper,
        topBar = { StepTopBar("Current order", "${viewModel.cart.size} items", onBack) },
        bottomBar = {
            Surface(color = Color(0xFFFBFAF8)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    SummaryRow("Subtotal", peso(viewModel.total), muted = true)
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("Total", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(peso(viewModel.total), fontWeight = FontWeight.Bold, fontSize = 21.sp)
                    }
                    Spacer(Modifier.height(13.dp))
                    Button(
                        onClick = onCharge,
                        enabled = viewModel.cart.isNotEmpty(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Teal, contentColor = Paper),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                    ) {
                        Text("Charge ${peso(viewModel.total)}", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    }
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            if (viewModel.cart.isEmpty()) {
                Text(
                    "Cart is empty.",
                    color = Faint,
                    modifier = Modifier.padding(vertical = 40.dp),
                )
            }
            viewModel.cart.forEach { line ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(line.category.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text(peso(line.unitPrice), color = Faint, fontSize = 12.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(peso(line.subtotal), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        TextButton(onClick = { viewModel.removeLine(line.id) }) {
                            Text("Remove", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Line),
                )
            }
        }
    }
}

/* --------------------------------------------------------------- PAYMENT */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentStep(
    viewModel: PosViewModel,
    onBack: () -> Unit,
) {
    val due = viewModel.total
    val suggestions = remember(due) {
        val options = linkedSetOf<Double>()
        options.add(due)
        listOf(50.0, 100.0, 500.0).forEach { step ->
            val up = ceil(due / step) * step
            if (up > due) options.add(up)
        }
        options.toList().take(4)
    }

    Scaffold(
        containerColor = Paper,
        topBar = { StepTopBar("Take payment", "Cash only", onBack) },
        bottomBar = {
            Surface(color = Paper, shadowElevation = 8.dp) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(11.dp))
                            .background(Ink)
                            .padding(horizontal = 14.dp, vertical = 11.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Cash tendered", color = Paper.copy(alpha = 0.7f), fontSize = 12.sp)
                        Text(peso(viewModel.paymentAmount), color = Paper, fontWeight = FontWeight.Bold, fontSize = 19.sp)
                    }
                    Spacer(Modifier.height(9.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("Change due", color = Amber, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text(peso(viewModel.change), color = Amber, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    viewModel.error?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = viewModel::checkout,
                        enabled = viewModel.canCheckout,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Teal, contentColor = Paper),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                    ) {
                        if (viewModel.submitting) {
                            CircularProgressIndicator(modifier = Modifier.height(20.dp), strokeWidth = 2.dp, color = Paper)
                        } else {
                            Text("Complete sale", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        }
                    }
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(16.dp))
            Text("Amount due", color = Faint, fontSize = 12.sp, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Text(
                peso(due),
                fontWeight = FontWeight.Bold,
                fontSize = 34.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.fillMaxWidth()) {
                suggestions.forEach { amount ->
                    val exact = amount == due
                    QuickCash(
                        label = if (exact) "Exact" else peso(amount).removeSuffix(".00"),
                        modifier = Modifier.weight(1f),
                    ) { viewModel.setPayment(amount) }
                }
            }
            Spacer(Modifier.height(12.dp))
            Keypad { viewModel.onPaymentNumpad(if (it == "⌫") PosViewModel.KEY_BACKSPACE else it) }
        }
    }
}

/* --------------------------------------------------------- shared pieces */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StepTopBar(title: String, subtitle: String, onBack: () -> Unit) {
    TopAppBar(
        title = {
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text(subtitle, color = Faint, fontSize = 11.sp)
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Paper,
            titleContentColor = Ink,
            navigationIconContentColor = Muted,
        ),
    )
}

@Composable
private fun AvatarCircle(text: String, color: Color) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(50))
            .background(color),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = Paper, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (selected) Ink else Paper,
        shape = RoundedCornerShape(20.dp),
        border = if (selected) null else androidx.compose.foundation.BorderStroke(1.dp, Line),
        onClick = onClick,
    ) {
        Text(
            label,
            color = if (selected) Paper else Muted,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun AmountCard(label: String, value: Double) {
    Surface(
        color = Paper,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Line),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
            Text(label, color = Faint, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(8.dp))
            Text(peso(value), fontWeight = FontWeight.Bold, fontSize = 44.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Keypad(onKey: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        KEYPAD.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { key ->
                    Surface(
                        color = Paper,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Line),
                        onClick = { onKey(key) },
                        modifier = Modifier
                            .weight(1f)
                            .height(58.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                key,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 22.sp,
                                color = if (key == "⌫") MaterialTheme.colorScheme.error else Ink,
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickCash(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        color = Sand,
        shape = RoundedCornerShape(10.dp),
        onClick = onClick,
        modifier = modifier,
    ) {
        Box(modifier = Modifier.padding(vertical = 11.dp), contentAlignment = Alignment.Center) {
            Text(label, color = Muted, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, muted: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = if (muted) Muted else Ink, fontSize = 12.5.sp)
        Text(value, color = if (muted) Muted else Ink, fontSize = 12.5.sp)
    }
}

/* ---------------------------------------------------------------- receipt */

@Composable
private fun ReceiptDialog(receipt: SaleResponse, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("New sale") } },
        title = { Text("Payment complete") },
        text = {
            Column {
                Text(
                    "${peso(receipt.changeAmount)} change · Sale #${receipt.id}",
                    color = Faint,
                    fontSize = 12.sp,
                )
                Spacer(Modifier.height(10.dp))
                receipt.items.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(item.categoryName)
                        Text(peso(item.subtotal))
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Total", fontWeight = FontWeight.Bold)
                    Text(peso(receipt.totalAmount), fontWeight = FontWeight.Bold)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Cash", color = Muted)
                    Text(peso(receipt.paymentAmount), color = Muted)
                }
            }
        },
    )
}
