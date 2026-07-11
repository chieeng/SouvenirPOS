package edu.cit.erag.souvenirpos.pos.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.cit.erag.souvenirpos.core.data.model.Category
import edu.cit.erag.souvenirpos.core.data.model.SaleCreateRequest
import edu.cit.erag.souvenirpos.core.data.model.SaleLineRequest
import edu.cit.erag.souvenirpos.core.data.model.SaleResponse
import edu.cit.erag.souvenirpos.core.network.userMessage
import edu.cit.erag.souvenirpos.core.di.ServiceLocator
import kotlinx.coroutines.launch

data class CartLine(
    val id: Long,
    val category: Category,
    val unitPrice: Double,
    val quantity: Int,
) {
    val subtotal: Double get() = unitPrice * quantity
}

class PosViewModel : ViewModel() {

    private val repo = ServiceLocator.posRepository

    var categories by mutableStateOf<List<Category>>(emptyList())
        private set
    var selectedCategory by mutableStateOf<Category?>(null)
        private set
    var priceInput by mutableStateOf("")
        private set
    var cart by mutableStateOf<List<CartLine>>(emptyList())
        private set
    var payment by mutableStateOf("")
        private set
    var loadingCategories by mutableStateOf(false)
        private set
    var submitting by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var receipt by mutableStateOf<SaleResponse?>(null)
        private set

    val userName: String get() = ServiceLocator.tokenStore.name() ?: "Cashier"

    // Owner-only actions (e.g. Manage Users) are hidden for cashiers; the backend enforces it too.
    val isOwner: Boolean get() = ServiceLocator.tokenStore.role() == "OWNER"

    val total: Double get() = cart.sumOf { it.subtotal }
    val paymentAmount: Double get() = payment.toDoubleOrNull() ?: 0.0
    val change: Double get() = (paymentAmount - total).coerceAtLeast(0.0)

    // BR-002 (>=1 item) and BR-003 (payment >= total) enforced before allowing checkout.
    val canCheckout: Boolean get() = cart.isNotEmpty() && paymentAmount >= total && !submitting

    init {
        loadCategories()
    }

    fun loadCategories() {
        loadingCategories = true
        error = null
        viewModelScope.launch {
            try {
                val list = repo.categories()
                categories = list
                if (selectedCategory == null) selectedCategory = list.firstOrNull()
            } catch (t: Throwable) {
                error = t.userMessage()
            } finally {
                loadingCategories = false
            }
        }
    }

    fun selectCategory(category: Category) {
        selectedCategory = category
    }

    fun onNumpad(key: String) {
        when (key) {
            KEY_BACKSPACE -> if (priceInput.isNotEmpty()) priceInput = priceInput.dropLast(1)
            "." -> if (!priceInput.contains(".")) priceInput += if (priceInput.isEmpty()) "0." else "."
            else -> priceInput += key
        }
    }

    fun onPaymentChange(value: String) {
        val cleaned = value.filter { it.isDigit() || it == '.' }
        val parts = cleaned.split(".")
        payment = if (parts.size > 2) parts[0] + "." + parts.drop(1).joinToString("") else cleaned
    }

    fun addToCart() {
        val price = priceInput.toDoubleOrNull()
        val category = selectedCategory
        if (category == null || price == null || price <= 0.0) {
            error = "Pick a category and enter a valid price"
            return
        }
        // Each line is a single item; the sale API still expects a quantity field.
        cart = cart + CartLine(System.nanoTime(), category, price, quantity = 1)
        priceInput = ""
        error = null
    }

    fun removeLine(id: Long) {
        cart = cart.filterNot { it.id == id }
    }

    /** On-screen keypad for the cash-payment step. */
    fun onPaymentNumpad(key: String) {
        payment = when (key) {
            KEY_BACKSPACE -> if (payment.isNotEmpty()) payment.dropLast(1) else payment
            "." -> if (payment.contains(".")) payment else if (payment.isEmpty()) "0." else "$payment."
            else -> payment + key
        }
    }

    /** Quick-cash chip / "Exact" sets the tendered amount directly. */
    fun setPayment(amount: Double) {
        payment = String.format(java.util.Locale.US, "%.2f", amount)
    }

    fun checkout() {
        if (!canCheckout) return
        submitting = true
        error = null
        viewModelScope.launch {
            try {
                val request = SaleCreateRequest(
                    items = cart.map { SaleLineRequest(it.category.id, it.quantity, it.unitPrice) },
                    paymentAmount = paymentAmount,
                )
                receipt = repo.createSale(request)
                cart = emptyList()
                payment = ""
            } catch (t: Throwable) {
                error = t.userMessage()
            } finally {
                submitting = false
            }
        }
    }

    fun dismissReceipt() {
        receipt = null
    }

    fun clearError() {
        error = null
    }

    fun logout(onDone: () -> Unit) {
        ServiceLocator.authRepository.logout()
        onDone()
    }

    companion object {
        const val KEY_BACKSPACE = "back"
    }
}
