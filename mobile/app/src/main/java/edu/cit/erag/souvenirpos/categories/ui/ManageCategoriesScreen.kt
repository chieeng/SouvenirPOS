package edu.cit.erag.souvenirpos.categories.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.cit.erag.souvenirpos.categories.viewmodel.ManageCategoriesViewModel
import edu.cit.erag.souvenirpos.core.data.model.Category

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageCategoriesScreen(
    onBack: () -> Unit,
    viewModel: ManageCategoriesViewModel = viewModel(),
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Categories", fontWeight = FontWeight.Bold) },
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
            AddCategoryCard(viewModel)
            ExistingCategories(viewModel)
        }
    }
}

@Composable
private fun AddCategoryCard(viewModel: ManageCategoriesViewModel) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Add Category", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = viewModel.name,
                onValueChange = viewModel::onName,
                label = { Text("Category Name") },
                singleLine = true,
                enabled = !viewModel.creating,
                modifier = Modifier.fillMaxWidth(),
            )

            viewModel.error?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            viewModel.success?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.tertiary,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Button(
                onClick = viewModel::createCategory,
                enabled = !viewModel.creating,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
            ) {
                if (viewModel.creating) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text("Add Category", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ExistingCategories(viewModel: ManageCategoriesViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Existing Categories", style = MaterialTheme.typography.titleMedium)

        if (viewModel.loading && viewModel.categories.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.height(24.dp), strokeWidth = 2.dp)
        } else if (viewModel.categories.isEmpty()) {
            Text(
                "No categories yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
            )
        } else {
            viewModel.categories.forEachIndexed { index, category ->
                if (index > 0) HorizontalDivider()
                CategoryRow(category)
            }
        }
    }
}

@Composable
private fun CategoryRow(category: Category) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
    ) {
        Text(category.name, fontWeight = FontWeight.SemiBold)
    }
}
