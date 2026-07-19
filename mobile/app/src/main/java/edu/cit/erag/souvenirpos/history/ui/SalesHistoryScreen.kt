package edu.cit.erag.souvenirpos.history.ui
import edu.cit.erag.souvenirpos.history.viewmodel.SalesHistoryViewModel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.cit.erag.souvenirpos.core.data.model.SaleResponse
import edu.cit.erag.souvenirpos.core.ui.formatSaleDateTime
import edu.cit.erag.souvenirpos.core.ui.peso
import java.time.Instant
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesHistoryScreen(
    onBack: () -> Unit,
    viewModel: SalesHistoryViewModel = viewModel(),
) {
    var showRangePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Sales History", fontWeight = FontWeight.Bold)
                        Text(
                            if (viewModel.loading) "Loading…"
                            else "${viewModel.sales.size} sale${if (viewModel.sales.size == 1) "" else "s"} · ${peso(viewModel.totalAmount)}",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to POS")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            FilterBar(
                from = viewModel.from,
                to = viewModel.to,
                onPick = { showRangePicker = true },
                onShowAll = { viewModel.clearFilter() },
            )

            when {
                viewModel.loading && viewModel.sales.isEmpty() -> CenteredBox {
                    CircularProgressIndicator()
                }

                viewModel.error != null -> CenteredBox {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            viewModel.error!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(onClick = viewModel::load) { Text("Retry") }
                    }
                }

                viewModel.sales.isEmpty() -> CenteredBox {
                    Text(
                        if (viewModel.hasFilter) "No sales in this range." else "No sales recorded yet.",
                        color = MaterialTheme.colorScheme.outline,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                else -> LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp),
                ) {
                    items(viewModel.sales, key = { it.id }) { sale ->
                        SaleRow(sale, onClick = { viewModel.select(sale) })
                    }
                }
            }
        }
    }

    if (showRangePicker) {
        SaleRangePicker(
            onDismiss = { showRangePicker = false },
            onRangeSelected = { fromIso, toIso ->
                showRangePicker = false
                viewModel.applyRange(fromIso, toIso)
            },
        )
    }

    viewModel.selected?.let { sale ->
        ReceiptDialog(sale, onDismiss = { viewModel.select(null) })
    }
}

@Composable
private fun FilterBar(from: String?, to: String?, onPick: () -> Unit, onShowAll: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedButton(onClick = onPick) {
            Icon(Icons.Filled.DateRange, contentDescription = null, modifier = Modifier.height(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                when {
                    from != null && to != null -> "$from  →  $to"
                    from != null -> from
                    else -> "Filter by date"
                },
            )
        }
        if (from != null || to != null) {
            TextButton(onClick = onShowAll) { Text("Show all") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SaleRow(sale: SaleResponse, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Sale #${sale.id}", fontWeight = FontWeight.SemiBold)
                Text(
                    formatSaleDateTime(sale.saleDateTime),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    "${sale.cashierName} · ${sale.items.size} item${if (sale.items.size == 1) "" else "s"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                peso(sale.totalAmount),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SaleRangePicker(onDismiss: () -> Unit, onRangeSelected: (String, String?) -> Unit) {
    val state = rememberDateRangePickerState()
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val startMillis = state.selectedStartDateMillis
                    if (startMillis != null) {
                        // The picker reports UTC midnight; read dates back in UTC so the days
                        // the user tapped are the days we filter on. A missing end means a
                        // single-day filter (the backend treats `from` alone as one day).
                        val fromIso = Instant.ofEpochMilli(startMillis)
                            .atZone(ZoneOffset.UTC).toLocalDate().toString()
                        val toIso = state.selectedEndDateMillis?.let {
                            Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate().toString()
                        }
                        onRangeSelected(fromIso, toIso)
                    } else {
                        onDismiss()
                    }
                },
            ) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    ) {
        DateRangePicker(state = state, modifier = Modifier.height(480.dp))
    }
}

@Composable
private fun ReceiptDialog(sale: SaleResponse, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
        title = { Text("Receipt · Sale #${sale.id}") },
        text = {
            Column {
                Text(
                    "${formatSaleDateTime(sale.saleDateTime)} · ${sale.cashierName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                sale.items.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("${item.categoryName}  ${peso(item.unitPrice)}")
                        Text(peso(item.subtotal))
                    }
                }
                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))
                ReceiptLine("Total", peso(sale.totalAmount), bold = true)
                ReceiptLine("Payment", peso(sale.paymentAmount))
                ReceiptLine("Change", peso(sale.changeAmount))
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

@Composable
private fun CenteredBox(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) { content() }
}
