package edu.cit.erag.souvenirpos.ui.history

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.cit.erag.souvenirpos.data.model.SaleResponse
import edu.cit.erag.souvenirpos.data.network.userMessage
import edu.cit.erag.souvenirpos.di.ServiceLocator
import kotlinx.coroutines.launch

class SalesHistoryViewModel : ViewModel() {

    private val repo = ServiceLocator.posRepository

    var sales by mutableStateOf<List<SaleResponse>>(emptyList())
        private set

    /** Active date filter as an ISO date (yyyy-MM-dd), or null for "all sales" (FR-013). */
    var date by mutableStateOf<String?>(null)
        private set
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
    }

    fun applyDateFilter(newDate: String?) {
        date = newDate
        load()
    }

    fun select(sale: SaleResponse?) {
        selected = sale
    }

    fun load() {
        loading = true
        error = null
        viewModelScope.launch {
            try {
                sales = repo.listSales(date)
            } catch (t: Throwable) {
                error = t.userMessage()
            } finally {
                loading = false
            }
        }
    }
}
