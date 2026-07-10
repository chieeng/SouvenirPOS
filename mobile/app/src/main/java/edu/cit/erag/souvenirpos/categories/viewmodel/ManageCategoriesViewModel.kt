package edu.cit.erag.souvenirpos.categories.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.cit.erag.souvenirpos.core.data.model.Category
import edu.cit.erag.souvenirpos.core.data.model.CategoryCreateRequest
import edu.cit.erag.souvenirpos.core.network.userMessage
import edu.cit.erag.souvenirpos.core.di.ServiceLocator
import kotlinx.coroutines.launch

class ManageCategoriesViewModel : ViewModel() {

    private val repo = ServiceLocator.categoryRepository

    var categories by mutableStateOf<List<Category>>(emptyList())
        private set
    var name by mutableStateOf("")
        private set
    var loading by mutableStateOf(false)
        private set
    var creating by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var success by mutableStateOf<String?>(null)
        private set

    // Category management is an owner-only screen; the backend also enforces it (403 for cashiers).
    val isOwner: Boolean get() = ServiceLocator.tokenStore.role() == "OWNER"

    init {
        loadCategories()
    }

    fun onName(value: String) { name = value; clearBanners() }

    fun loadCategories() {
        loading = true
        error = null
        viewModelScope.launch {
            try {
                categories = repo.categories()
            } catch (t: Throwable) {
                error = t.userMessage()
            } finally {
                loading = false
            }
        }
    }

    fun createCategory() {
        // Mirror the backend validation (CategoryCreateRequest.name @NotBlank) before the round-trip.
        if (name.isBlank()) {
            error = "Enter a category name"
            return
        }
        creating = true
        clearBanners()
        viewModelScope.launch {
            try {
                val created = repo.createCategory(CategoryCreateRequest(name.trim()))
                success = "Added ${created.name}"
                name = ""
                loadCategories()
            } catch (t: Throwable) {
                error = t.userMessage()
            } finally {
                creating = false
            }
        }
    }

    private fun clearBanners() {
        error = null
        success = null
    }
}
