package edu.cit.erag.souvenirpos.pos.ui
import edu.cit.erag.souvenirpos.pos.viewmodel.PosViewModel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.cit.erag.souvenirpos.core.ui.peso

private val NUMPAD_ROWS = listOf(
    listOf("7", "8", "9"),
    listOf("4", "5", "6"),
    listOf("1", "2", "3"),
    listOf(".", "0", "⌫"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreen(
    onLoggedOut: () -> Unit,
    onManageUsers: () -> Unit,
    onViewHistory: () -> Unit,
    onViewDashboard: () -> Unit,
    onManageCategories: () -> Unit,
    viewModel: PosViewModel = viewModel(),
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("SouvenirPOS", fontWeight = FontWeight.Bold)
                        Text(
                            viewModel.userName,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onViewDashboard) {
                        Icon(Icons.Filled.BarChart, contentDescription = "Sales dashboard")
                    }
                    IconButton(onClick = onViewHistory) {
                        Icon(Icons.Filled.History, contentDescription = "Sales history")
                    }
                    if (viewModel.isOwner) {
                        IconButton(onClick = onManageCategories) {
                            Icon(Icons.Filled.Category, contentDescription = "Manage categories")
                        }
                        IconButton(onClick = onManageUsers) {
                            Icon(Icons.Filled.ManageAccounts, contentDescription = "Manage users")
                        }
                    }
                    IconButton(onClick = { viewModel.logout(onLoggedOut) }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Log out")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            PriceDisplay(viewModel.priceInput)

            Spacer(Modifier.height(16.dp))
            CategorySection(viewModel)

            Spacer(Modifier.height(16.dp))
            Numpad(viewModel)

            Spacer(Modifier.height(16.dp))
            QuantityRow(viewModel)

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = viewModel::addToCart,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("+ Add to Cart")
            }

            Spacer(Modifier.height(24.dp))
            CartSection(viewModel)

            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = viewModel.payment,
                onValueChange = viewModel::onPaymentChange,
                label = { Text("Payment Received") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(12.dp))
            AmountRow("Change", peso(viewModel.change))
            AmountRow("TOTAL", peso(viewModel.total), emphasize = true)

            viewModel.error?.let { message ->
                Spacer(Modifier.height(12.dp))
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = viewModel::checkout,
                enabled = viewModel.canCheckout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) {
                if (viewModel.submitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text("Checkout", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    viewModel.receipt?.let { receipt ->
        ReceiptDialog(receipt, onDismiss = viewModel::dismissReceipt)
    }
}

@Composable
private fun PriceDisplay(priceInput: String) {
    val value = priceInput.toDoubleOrNull() ?: 0.0
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Price", style = MaterialTheme.typography.labelMedium)
            Text(
                text = peso(value),
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun CategorySection(viewModel: PosViewModel) {
    Text("Category", style = MaterialTheme.typography.titleSmall)
    Spacer(Modifier.height(8.dp))
    if (viewModel.loadingCategories) {
        CircularProgressIndicator(modifier = Modifier.height(24.dp), strokeWidth = 2.dp)
    } else {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            viewModel.categories.forEach { category ->
                FilterChip(
                    selected = viewModel.selectedCategory?.id == category.id,
                    onClick = { viewModel.selectCategory(category) },
                    label = { Text(category.name) },
                )
            }
        }
    }
}

@Composable
private fun Numpad(viewModel: PosViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        NUMPAD_ROWS.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { key ->
                    OutlinedButton(
                        onClick = {
                            viewModel.onNumpad(if (key == "⌫") PosViewModel.KEY_BACKSPACE else key)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                    ) {
                        Text(key, fontSize = 20.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun QuantityRow(viewModel: PosViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("Quantity", style = MaterialTheme.typography.titleSmall)
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(onClick = viewModel::decQuantity) { Text("−", fontSize = 20.sp) }
            Text(
                text = viewModel.quantity.toString(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .width(48.dp)
                    .padding(horizontal = 8.dp),
            )
            OutlinedButton(onClick = viewModel::incQuantity) { Text("+", fontSize = 20.sp) }
        }
    }
}

@Composable
private fun CartSection(viewModel: PosViewModel) {
    Text("Sales Breakdown", style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(8.dp))
    if (viewModel.cart.isEmpty()) {
        Text(
            "Nothing added yet.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
        )
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            viewModel.cart.forEach { line ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(line.category.name, fontWeight = FontWeight.SemiBold)
                        Text(
                            "${peso(line.unitPrice)} · x${line.quantity}",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    Text(peso(line.subtotal), fontWeight = FontWeight.SemiBold)
                    TextButton(onClick = { viewModel.removeLine(line.id) }) { Text("✕") }
                }
            }
        }
    }
}

@Composable
private fun AmountRow(label: String, value: String, emphasize: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            label,
            fontWeight = if (emphasize) FontWeight.Bold else FontWeight.Normal,
            style = if (emphasize) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
        )
        Text(
            value,
            fontWeight = if (emphasize) FontWeight.Bold else FontWeight.Normal,
            style = if (emphasize) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
            color = if (emphasize) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun ReceiptDialog(
    receipt: edu.cit.erag.souvenirpos.core.data.model.SaleResponse,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done") }
        },
        title = { Text("Receipt · Sale #${receipt.id}") },
        text = {
            Column {
                receipt.items.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("${item.categoryName} x${item.quantity}")
                        Text(peso(item.subtotal))
                    }
                }
                Spacer(Modifier.height(8.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        ReceiptLine("Total", peso(receipt.totalAmount), bold = true)
                        ReceiptLine("Payment", peso(receipt.paymentAmount))
                        ReceiptLine("Change", peso(receipt.changeAmount))
                    }
                }
            }
        },
    )
}

@Composable
private fun ReceiptLine(label: String, value: String, bold: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
        Text(value, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
    }
}
