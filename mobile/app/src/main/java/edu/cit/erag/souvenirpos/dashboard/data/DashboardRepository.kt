package edu.cit.erag.souvenirpos.dashboard.data

import edu.cit.erag.souvenirpos.core.data.model.SaleSummaryResponse
import edu.cit.erag.souvenirpos.core.network.ApiService

class DashboardRepository(private val api: ApiService) {

    /** Daily/weekly sales aggregates for the dashboard (GET /api/sales/summary, FR-015). */
    suspend fun summary(): SaleSummaryResponse = api.salesSummary()
}
