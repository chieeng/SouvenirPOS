package edu.cit.erag.souvenirpos.ui.users

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.cit.erag.souvenirpos.data.model.UserResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageUsersScreen(
    onBack: () -> Unit,
    viewModel: ManageUsersViewModel = viewModel(),
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Users", fontWeight = FontWeight.Bold) },
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
            CreateAccountCard(viewModel)
            ExistingUsers(viewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateAccountCard(viewModel: ManageUsersViewModel) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Create Account", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = viewModel.name,
                onValueChange = viewModel::onName,
                label = { Text("Full Name") },
                singleLine = true,
                enabled = !viewModel.creating,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = viewModel.username,
                onValueChange = viewModel::onUsername,
                label = { Text("Username") },
                singleLine = true,
                enabled = !viewModel.creating,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = viewModel.password,
                onValueChange = viewModel::onPassword,
                label = { Text("Password (min 6)") },
                singleLine = true,
                enabled = !viewModel.creating,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
            )

            Text("Role", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = viewModel.role == ManageUsersViewModel.ROLE_CASHIER,
                    onClick = { viewModel.onRole(ManageUsersViewModel.ROLE_CASHIER) },
                    label = { Text("Cashier") },
                    enabled = !viewModel.creating,
                )
                FilterChip(
                    selected = viewModel.role == ManageUsersViewModel.ROLE_OWNER,
                    onClick = { viewModel.onRole(ManageUsersViewModel.ROLE_OWNER) },
                    label = { Text("Owner") },
                    enabled = !viewModel.creating,
                )
            }

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
                onClick = viewModel::createUser,
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
                    Text("Create Account", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ExistingUsers(viewModel: ManageUsersViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Existing Users", style = MaterialTheme.typography.titleMedium)

        if (viewModel.loading && viewModel.users.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.height(24.dp), strokeWidth = 2.dp)
        } else if (viewModel.users.isEmpty()) {
            Text(
                "No users found.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
            )
        } else {
            viewModel.users.forEachIndexed { index, user ->
                if (index > 0) HorizontalDivider()
                UserRow(user)
            }
        }
    }
}

@Composable
private fun UserRow(user: UserResponse) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(user.name, fontWeight = FontWeight.SemiBold)
            Text(
                "@${user.username}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = user.role,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (user.role == ManageUsersViewModel.ROLE_OWNER)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.tertiary,
        )
    }
}
