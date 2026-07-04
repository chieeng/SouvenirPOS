package edu.cit.erag.souvenirpos.data.model

// Request/response payloads that mirror the Spring Boot DTOs. Field names must match
// the backend JSON exactly so Gson can (de)serialize them.

data class LoginRequest(
    val username: String,
    val password: String,
)

data class LoginResponse(
    val token: String,
    val userId: Long,
    val name: String,
    val username: String,
    val role: String,
)

data class Category(
    val id: Long,
    val name: String,
)

data class UserResponse(
    val id: Long,
    val name: String,
    val username: String,
    val role: String,
)

data class UserCreateRequest(
    val name: String,
    val username: String,
    val password: String,
    val role: String,
)

data class SaleLineRequest(
    val categoryId: Long,
    val quantity: Int,
    val unitPrice: Double,
)

data class SaleCreateRequest(
    val items: List<SaleLineRequest>,
    val paymentAmount: Double,
)

data class SaleItemResponse(
    val id: Long,
    val categoryId: Long,
    val categoryName: String,
    val quantity: Int,
    val unitPrice: Double,
    val subtotal: Double,
)

data class SaleResponse(
    val id: Long,
    val saleDateTime: String,
    val cashierId: Long,
    val cashierName: String,
    val totalAmount: Double,
    val paymentAmount: Double,
    val changeAmount: Double,
    val items: List<SaleItemResponse>,
)
