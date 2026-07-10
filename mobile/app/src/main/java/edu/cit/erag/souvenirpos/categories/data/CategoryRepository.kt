package edu.cit.erag.souvenirpos.categories.data

import edu.cit.erag.souvenirpos.core.data.model.Category
import edu.cit.erag.souvenirpos.core.data.model.CategoryCreateRequest
import edu.cit.erag.souvenirpos.core.network.ApiService

class CategoryRepository(private val api: ApiService) {

    /** Lists all categories (GET /api/categories). Open to any signed-in user. */
    suspend fun categories(): List<Category> = api.categories()

    /** Creates a category (POST /api/categories). Owner-only on the backend. */
    suspend fun createCategory(request: CategoryCreateRequest): Category =
        api.createCategory(request)
}
