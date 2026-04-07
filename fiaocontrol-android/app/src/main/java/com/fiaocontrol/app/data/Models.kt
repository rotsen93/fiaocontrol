package com.fiaocontrol.app.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CustomerBalance(
    @SerialName("customer_id") val customerId: String,
    @SerialName("business_id") val businessId: String,
    val name: String,
    val phone: String,
    val alias: String? = null,
    val balance: Double = 0.0,
)

@Serializable
data class LedgerEntry(
    val id: String,
    @SerialName("customer_id") val customerId: String,
    @SerialName("business_id") val businessId: String,
    val type: String,
    val amount: Double,
    val note: String? = null,
    @SerialName("created_at") val createdAt: String,
)

@Serializable
data class CreateCustomerRequest(
    @SerialName("business_id") val businessId: String,
    val name: String,
    val phone: String,
    val alias: String? = null,
)

@Serializable
data class UpdateCustomerRequest(
    val name: String,
    val phone: String,
    val alias: String? = null,
)

@Serializable
data class CreateLedgerEntryRequest(
    @SerialName("business_id") val businessId: String,
    @SerialName("customer_id") val customerId: String,
    val type: String,
    val amount: Double,
    val note: String? = null,
)

@Serializable
data class UpdateLedgerEntryRequest(
    val amount: Double,
    val note: String? = null,
)

@Serializable
data class AuthUser(
    val id: String,
    val email: String? = null,
)

@Serializable
data class AuthResponse(
    @SerialName("access_token") val accessToken: String? = null,
    @SerialName("refresh_token") val refreshToken: String? = null,
    val user: AuthUser,
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
)

@Serializable
data class SignUpRequest(
    val email: String,
    val password: String,
)

@Serializable
data class Business(
    val id: String,
    val name: String,
)

@Serializable
data class UserBusiness(
    @SerialName("user_id") val userId: String? = null,
    @SerialName("business_id") val businessId: String,
)

data class SessionInfo(
    val userId: String,
    val userEmail: String,
    val businessId: String,
    val businessName: String,
)
