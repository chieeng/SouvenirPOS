package edu.cit.erag.souvenirpos.dashboard.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.cit.erag.souvenirpos.core.data.model.SaleSummaryResponse
import edu.cit.erag.souvenirpos.core.network.userMessage
import edu.cit.erag.souvenirpos.core.di.ServiceLocator
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {

    private val repo = ServiceLocator.dashboardRepository

    var summary by mutableStateOf<SaleSummaryResponse?>(null)
        private set
    var loading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    init {
        // FR-016 / NFR-002: poll so a sale rung up on another device shows within 5s.
        // The loop is cancelled with viewModelScope when the screen leaves the graph.
        viewModelScope.launch {
            load(silent = false)
            while (isActive) {
                delay(REFRESH_MS)
                load(silent = true)
            }
        }
    }

    /** `silent` skips the loading flag so the 5s auto-refresh doesn't flicker the UI. */
    private suspend fun load(silent: Boolean) {
        if (!silent) loading = true
        try {
            summary = repo.summary()
            error = null
        } catch (t: Throwable) {
            error = t.userMessage()
        } finally {
            if (!silent) loading = false
        }
    }

    companion object {
        private const val REFRESH_MS = 5000L
    }
}
