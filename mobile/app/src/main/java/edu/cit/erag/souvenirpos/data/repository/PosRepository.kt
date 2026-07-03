package edu.cit.erag.souvenirpos.data.repository

import edu.cit.erag.souvenirpos.data.model.Category
import edu.cit.erag.souvenirpos.data.model.SaleCreateRequest
import edu.cit.erag.souvenirpos.data.model.SaleResponse
import edu.cit.erag.souvenirpos.data.network.ApiService

class PosRepository(private val api: ApiService) {

    /** Owner-managed category list used for sale entry (GET /api/categories). */
    suspend fun categories(): List<Category> = api.categories()

    /** Records a completed sale (POST /api/sales); the backend computes totals/change. */
    suspend fun createSale(request: SaleCreateRequest): SaleResponse = api.createSale(request)
}
