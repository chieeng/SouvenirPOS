package edu.cit.erag.souvenirpos.auth.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.cit.erag.souvenirpos.core.di.ServiceLocator
import edu.cit.erag.souvenirpos.core.network.userMessage
import kotlinx.coroutines.launch

class ChangePasswordViewModel : ViewModel() {

    var currentPassword by mutableStateOf("")
        private set
    var newPassword by mutableStateOf("")
        private set
    var confirmPassword by mutableStateOf("")
        private set
    var loading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    fun onCurrent(value: String) { currentPassword = value; error = null }
    fun onNew(value: String) { newPassword = value; error = null }
    fun onConfirm(value: String) { confirmPassword = value; error = null }

    fun submit(onSuccess: () -> Unit) {
        if (currentPassword.isBlank() || newPassword.isBlank()) {
            error = "Fill in your current and new password"
            return
        }
        if (newPassword.length < 8) {
            error = "New password must be at least 8 characters"
            return
        }
        if (newPassword != confirmPassword) {
            error = "New passwords do not match"
            return
        }
        loading = true
        error = null
        viewModelScope.launch {
            try {
                ServiceLocator.authRepository.changePassword(currentPassword, newPassword)
                onSuccess()
            } catch (t: Throwable) {
                error = t.userMessage()
            } finally {
                loading = false
            }
        }
    }
}
