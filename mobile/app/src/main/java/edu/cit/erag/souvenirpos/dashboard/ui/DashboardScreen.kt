package edu.cit.erag.souvenirpos.dashboard.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.cit.erag.souvenirpos.core.data.model.DailyBucket
import edu.cit.erag.souvenirpos.core.data.model.SaleSummaryResponse
import edu.cit.erag.souvenirpos.core.ui.peso
import edu.cit.erag.souvenirpos.dashboard.viewmodel.DashboardViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dayFormatter = DateTimeFormatter.ofPattern("EEE M/d", Locale.US)

private fun dayLabel(iso: String): String = try {
    LocalDate.parse(iso).format(dayFormatter)
} catch (e: Exception) {
    iso
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onBack: () -> Unit,
    viewModel: DashboardViewModel = viewModel(),
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Sales Dashboard", fontWeight = FontWeight.Bold)
                        Text(
                            if (viewModel.loading && viewModel.summary == null) "Loading…"
                            else "Live · updates every 5s",
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
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            viewModel.error?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            val summary = viewModel.summary
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TotalCard(
                    label = "Today",
                    total = summary?.todayTotal ?: 0.0,
                    count = summary?.todayCount ?: 0,
                    modifier = Modifier.weight(1f),
                )
                TotalCard(
                    label = "This Week",
                    total = summary?.weekTotal ?: 0.0,
                    count = summary?.weekCount ?: 0,
                    modifier = Modifier.weight(1f),
                    accent = true,
                )
            }

            Breakdown(summary, loading = viewModel.loading)
        }
    }
}

@Composable
private fun TotalCard(
    label: String,
    total: Double,
    count: Int,
    modifier: Modifier = Modifier,
    accent: Boolean = false,
) {
    ElevatedCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                text = peso(total),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = if (accent) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                "$count sale${if (count == 1) "" else "s"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun Breakdown(summary: SaleSummaryResponse?, loading: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Last 7 days", style = MaterialTheme.typography.titleMedium)

        val daily = summary?.daily ?: emptyList()
        when {
            daily.isEmpty() && loading ->
                CircularProgressIndicator(modifier = Modifier.height(24.dp), strokeWidth = 2.dp)
            daily.isEmpty() ->
                Text(
                    "No sales recorded yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                )
            else -> {
                val maxDaily = daily.maxOf { it.total }
                daily.forEachIndexed { index, bucket ->
                    if (index > 0) HorizontalDivider()
                    DailyRow(bucket, maxDaily)
                }
            }
        }
    }
}

@Composable
private fun DailyRow(bucket: DailyBucket, maxDaily: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            dayLabel(bucket.date),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(84.dp),
        )
        // A lightweight proportional bar so the busiest day stands out at a glance.
        Box(
            modifier = Modifier
                .weight(1f)
                .height(10.dp)
                .padding(end = 8.dp),
        ) {
            val fraction = if (maxDaily > 0) (bucket.total / maxDaily).toFloat() else 0f
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(10.dp)
                    .background(
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(5.dp),
                    ),
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(peso(bucket.total), fontWeight = FontWeight.SemiBold)
            Text(
                "${bucket.count}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
