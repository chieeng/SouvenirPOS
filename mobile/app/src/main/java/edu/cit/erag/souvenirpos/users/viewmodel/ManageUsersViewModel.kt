package edu.cit.erag.souvenirpos.users.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.cit.erag.souvenirpos.core.data.model.UserCreateRequest
import edu.cit.erag.souvenirpos.core.data.model.UserResponse
import edu.cit.erag.souvenirpos.core.network.userMessage
import edu.cit.erag.souvenirpos.core.di.ServiceLocator
import kotlinx.coroutines.launch

class ManageUsersViewModel : ViewModel() {

    private val repo = ServiceLocator.userRepository
    val currentUserId: Long = ServiceLocator.tokenStore.userId()

    var users by mutableStateOf<List<UserResponse>>(emptyList())
        private set
    var name by mutableStateOf("")
        private set
    var username by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var role by mutableStateOf(ROLE_CASHIER)
        private set
    var loading by mutableStateOf(false)
        private set
    var creating by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var success by mutableStateOf<String?>(null)
        private set
    var togglingId by mutableStateOf<Long?>(null)
        private set

    init {
        loadUsers()
    }

    fun onName(value: String) { name = value; clearBanners() }
    fun onUsername(value: String) { username = value; clearBanners() }
    fun onPassword(value: String) { password = value; clearBanners() }
    fun onRole(value: String) { role = value }

    fun loadUsers() {
        loading = true
        error = null
        viewModelScope.launch {
            try {
                users = repo.users()
            } catch (t: Throwable) {
                error = t.userMessage()
            } finally {
                loading = false
            }
        }
    }

    fun createUser() {
        // Mirror the backend validation (UserCreateRequest: @NotBlank, password @Size(min=6))
        // so the cashier sees a message before the round-trip; the server re-validates regardless.
        if (name.isBlank() || username.isBlank() || password.isBlank()) {
            error = "Fill in name, username, and password"
            return
        }
        if (password.length < 8) {
            error = "Password must be at least 8 characters"
            return
        }
        creating = true
        clearBanners()
        viewModelScope.launch {
            try {
                val created = repo.createUser(
                    UserCreateRequest(name.trim(), username.trim(), password, role),
                )
                success = "Created ${created.username} (${created.role})"
                name = ""
                username = ""
                password = ""
                role = ROLE_CASHIER
                loadUsers()
            } catch (t: Throwable) {
                error = t.userMessage()
            } finally {
                creating = false
            }
        }
    }

    fun toggleEnabled(user: UserResponse) {
        togglingId = user.id
        clearBanners()
        viewModelScope.launch {
            try {
                val updated = repo.setEnabled(user.id, !user.enabled)
                success = if (updated.enabled) "Reactivated ${updated.username}"
                else "Deactivated ${updated.username}"
                loadUsers()
            } catch (t: Throwable) {
                error = t.userMessage()
            } finally {
                togglingId = null
            }
        }
    }

    private fun clearBanners() {
        error = null
        success = null
    }

    companion object {
        const val ROLE_CASHIER = "CASHIER"
        const val ROLE_OWNER = "OWNER"
    }
}
