package edu.cit.erag.souvenirpos.ui.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.cit.erag.souvenirpos.data.network.userMessage
import edu.cit.erag.souvenirpos.di.ServiceLocator
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    var username by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var loading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    fun onUsername(value: String) {
        username = value
        error = null
    }

    fun onPassword(value: String) {
        password = value
        error = null
    }

    fun login(onSuccess: () -> Unit) {
        if (username.isBlank() || password.isBlank()) {
            error = "Enter your username and password"
            return
        }
        loading = true
        error = null
        viewModelScope.launch {
            try {
                ServiceLocator.authRepository.login(username, password)
                onSuccess()
            } catch (t: Throwable) {
                error = t.userMessage()
            } finally {
                loading = false
            }
        }
    }
}
