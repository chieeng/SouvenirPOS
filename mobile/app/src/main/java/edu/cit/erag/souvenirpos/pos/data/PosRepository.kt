package edu.cit.erag.souvenirpos.pos.data

import edu.cit.erag.souvenirpos.core.data.model.Category
import edu.cit.erag.souvenirpos.core.data.model.SaleCreateRequest
import edu.cit.erag.souvenirpos.core.data.model.SaleResponse
import edu.cit.erag.souvenirpos.core.network.ApiService

class PosRepository(private val api: ApiService) {

    /** Owner-managed category list used for sale entry (GET /api/categories). */
    suspend fun categories(): List<Category> = api.categories()

    /** Records a completed sale (POST /api/sales); the backend computes totals/change. */
    suspend fun createSale(request: SaleCreateRequest): SaleResponse = api.createSale(request)

    /**
     * Sales history, newest first (GET /api/sales). Pass `from` alone for a single day or
     * both `from` and `to` for an inclusive date range (FR-013); omit both for all sales.
     */
    suspend fun listSales(from: String? = null, to: String? = null): List<SaleResponse> =
        api.sales(from, to)
}
