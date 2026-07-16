package edu.cit.erag.souvenirpos.auth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.cit.erag.souvenirpos.auth.viewmodel.ChangePasswordViewModel

@Composable
fun ChangePasswordScreen(
    forced: Boolean,
    onChanged: () -> Unit,
    onCancel: () -> Unit,
    viewModel: ChangePasswordViewModel = viewModel(),
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            if (forced) "Set a new password" else "Change password",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            if (forced)
                "Your account still uses its initial password. Please replace it before continuing."
            else
                "Enter your current password and a new one.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(22.dp))
        OutlinedTextField(
            value = viewModel.currentPassword,
            onValueChange = viewModel::onCurrent,
            label = { Text("Current password") },
            singleLine = true,
            enabled = !viewModel.loading,
            visualTransformation = PasswordVisualTransformation(),
            shape = RoundedCornerShape(11.dp),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = viewModel.newPassword,
            onValueChange = viewModel::onNew,
            label = { Text("New password (min 8)") },
            singleLine = true,
            enabled = !viewModel.loading,
            visualTransformation = PasswordVisualTransformation(),
            shape = RoundedCornerShape(11.dp),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = viewModel.confirmPassword,
            onValueChange = viewModel::onConfirm,
            label = { Text("Confirm new password") },
            singleLine = true,
            enabled = !viewModel.loading,
            visualTransformation = PasswordVisualTransformation(),
            shape = RoundedCornerShape(11.dp),
            modifier = Modifier.fillMaxWidth(),
        )

        viewModel.error?.let { message ->
            Spacer(Modifier.height(12.dp))
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Spacer(Modifier.height(22.dp))
        Button(
            onClick = { viewModel.submit(onChanged) },
            enabled = !viewModel.loading,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
        ) {
            if (viewModel.loading) {
                CircularProgressIndicator(
                    modifier = Modifier.height(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Text("Update password", fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(Modifier.height(6.dp))
        TextButton(
            onClick = onCancel,
            enabled = !viewModel.loading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (forced) "Sign out instead" else "Cancel & sign out")
        }
    }
}
