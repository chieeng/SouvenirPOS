package edu.cit.erag.souvenirpos.data.repository

import edu.cit.erag.souvenirpos.core.data.model.UserCreateRequest
import edu.cit.erag.souvenirpos.core.data.model.UserResponse
import edu.cit.erag.souvenirpos.core.network.ApiService

class UserRepository(private val api: ApiService) {

    /** Lists all accounts (GET /api/users). Owner-only on the backend. */
    suspend fun users(): List<UserResponse> = api.users()

    /** Creates a cashier/owner account (POST /api/users). Owner-only on the backend. */
    suspend fun createUser(request: UserCreateRequest): UserResponse = api.createUser(request)
}
