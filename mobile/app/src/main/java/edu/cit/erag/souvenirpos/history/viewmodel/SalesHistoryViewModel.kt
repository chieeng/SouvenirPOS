package edu.cit.erag.souvenirpos.history.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.cit.erag.souvenirpos.core.data.model.SaleResponse
import edu.cit.erag.souvenirpos.core.network.userMessage
import edu.cit.erag.souvenirpos.core.di.ServiceLocator
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SalesHistoryViewModel : ViewModel() {

    private val repo = ServiceLocator.posRepository

    var sales by mutableStateOf<List<SaleResponse>>(emptyList())
        private set

    /**
     * Active date-range filter as ISO dates (yyyy-MM-dd), or null for "all sales" (FR-013).
     * `from` alone means a single day; `from`+`to` means an inclusive range.
     */
    var from by mutableStateOf<String?>(null)
        private set
    var to by mutableStateOf<String?>(null)
        private set

    val hasFilter: Boolean get() = from != null || to != null
    var loading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    /** Sale currently shown in the receipt dialog (FR-014); null = dialog closed. */
    var selected by mutableStateOf<SaleResponse?>(null)
        private set

    val totalAmount: Double get() = sales.sumOf { it.totalAmount }

    init {
        load()
        // FR-016 / NFR-002: poll so a sale rung up on another device shows within 5s.
        // The loop honours the current date filter and is cancelled with viewModelScope.
        viewModelScope.launch {
            while (isActive) {
                delay(REFRESH_MS)
                load(silent = true)
            }
        }
    }

    fun applyRange(newFrom: String?, newTo: String?) {
        from = newFrom
        to = newTo
        load()
    }

    fun clearFilter() {
        from = null
        to = null
        load()
    }

    fun select(sale: SaleResponse?) {
        selected = sale
    }

    /** `silent` skips the loading flag so the 5s auto-refresh doesn't flicker the UI. */
    fun load(silent: Boolean = false) {
        if (!silent) loading = true
        error = null
        viewModelScope.launch {
            try {
                sales = repo.listSales(from, to)
            } catch (t: Throwable) {
                error = t.userMessage()
            } finally {
                if (!silent) loading = false
            }
        }
    }

    companion object {
        private const val REFRESH_MS = 5000L
    }
}
