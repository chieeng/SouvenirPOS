package edu.cit.erag.souvenirpos.auth.ui
import edu.cit.erag.souvenirpos.auth.viewmodel.LoginViewModel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.cit.erag.souvenirpos.core.ui.theme.Amber
import edu.cit.erag.souvenirpos.core.ui.theme.Ink
import edu.cit.erag.souvenirpos.core.ui.theme.Muted
import edu.cit.erag.souvenirpos.core.ui.theme.Paper
import edu.cit.erag.souvenirpos.core.ui.theme.Teal
import edu.cit.erag.souvenirpos.core.ui.theme.TealDark

@Composable
fun LoginScreen(
    onLoggedIn: (mustChangePassword: Boolean) -> Unit,
    viewModel: LoginViewModel = viewModel(),
) {
    val onBrand = Color(0xFFEAFAF6)
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Paper,
        unfocusedContainerColor = Paper,
        focusedTextColor = Ink,
        unfocusedTextColor = Ink,
        focusedBorderColor = Teal,
        unfocusedBorderColor = Color.Transparent,
        focusedLabelColor = Teal,
        unfocusedLabelColor = Muted,
        cursorColor = Teal,
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TealDark),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            // brand mark
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = Paper,
                    shape = RoundedCornerShape(11.dp),
                    modifier = Modifier.size(40.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("S", color = TealDark, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    }
                }
                Spacer(Modifier.width(11.dp))
                Text(
                    "SouvenirPOS",
                    color = Paper,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                )
            }

            Spacer(Modifier.height(26.dp))
            Text(
                "Welcome back",
                color = Paper,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
            )
            Spacer(Modifier.height(7.dp))
            Text(
                "Sign in to open your register drawer.",
                color = onBrand.copy(alpha = 0.78f),
                fontSize = 14.sp,
            )

            Spacer(Modifier.height(26.dp))
            OutlinedTextField(
                value = viewModel.username,
                onValueChange = viewModel::onUsername,
                label = { Text("Username") },
                singleLine = true,
                enabled = !viewModel.loading,
                shape = RoundedCornerShape(11.dp),
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value = viewModel.password,
                onValueChange = viewModel::onPassword,
                label = { Text("Password") },
                singleLine = true,
                enabled = !viewModel.loading,
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(11.dp),
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth(),
            )

            if (viewModel.error != null) {
                Spacer(Modifier.height(14.dp))
                Text(
                    text = viewModel.error!!,
                    color = Color(0xFFFFD9D2),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { viewModel.login(onLoggedIn) },
                enabled = !viewModel.loading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Amber,
                    contentColor = Paper,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) {
                if (viewModel.loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(20.dp),
                        strokeWidth = 2.dp,
                        color = Paper,
                    )
                } else {
                    Text("Sign in & open drawer", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
            }
        }

        Text(
            "SouvenirPOS 1.0 · Harbor Row · shift 2",
            color = onBrand.copy(alpha = 0.6f),
            fontSize = 11.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
        )
    }
}
