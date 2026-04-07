package com.fiaocontrol.app.data

import android.content.Context
import com.fiaocontrol.app.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class SupabaseApi(context: Context) {
    private val client = HttpClient(io.ktor.client.engine.okhttp.OkHttp) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    private val prefs = context.getSharedPreferences("fiaocontrol_session", Context.MODE_PRIVATE)
    private val baseUrl = "${BuildConfig.SUPABASE_URL}/rest/v1"
    private val authUrl = "${BuildConfig.SUPABASE_URL}/auth/v1"
    private val json = Json { ignoreUnknownKeys = true }

    private val accessToken: String?
        get() = prefs.getString("access_token", null)

    private val userId: String?
        get() = prefs.getString("user_id", null)

    private val businessId: String?
        get() = prefs.getString("business_id", null)

    fun isLoggedIn(): Boolean = !accessToken.isNullOrBlank() && !businessId.isNullOrBlank()

    fun getSessionInfo(): SessionInfo? {
        val uid = userId ?: return null
        val bid = businessId ?: return null
        val email = prefs.getString("user_email", "") ?: ""
        val businessName = prefs.getString("business_name", "Mi colmado") ?: "Mi colmado"
        return SessionInfo(userId = uid, userEmail = email, businessId = bid, businessName = businessName)
    }

    suspend fun login(email: String, password: String): SessionInfo {
        val response = client.post("$authUrl/token?grant_type=password") {
            header("apikey", BuildConfig.SUPABASE_ANON_KEY)
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(email = email.trim(), password = password))
        }

        val auth = parseAuthResponse(response.status.isSuccess(), response.bodyAsText(), "iniciar sesion")

        if (auth.accessToken.isNullOrBlank() || auth.refreshToken.isNullOrBlank()) {
            throw IllegalStateException("No se pudo iniciar sesion. Verifica correo y contrasena")
        }

        val resolvedBusinessId = resolveBusinessId(auth.user.id, auth.accessToken)
        val resolvedBusinessName = fetchBusinessName(resolvedBusinessId, auth.accessToken)
        saveSession(auth, resolvedBusinessId, resolvedBusinessName)

        return SessionInfo(
            userId = auth.user.id,
            userEmail = auth.user.email ?: email.trim(),
            businessId = resolvedBusinessId,
            businessName = resolvedBusinessName,
        )
    }

    suspend fun register(email: String, password: String, businessName: String): SessionInfo {
        val response = client.post("$authUrl/signup") {
            header("apikey", BuildConfig.SUPABASE_ANON_KEY)
            contentType(ContentType.Application.Json)
            setBody(SignUpRequest(email = email.trim(), password = password))
        }

        val auth = parseAuthResponse(response.status.isSuccess(), response.bodyAsText(), "registrar cuenta")

        if (auth.accessToken.isNullOrBlank() || auth.refreshToken.isNullOrBlank()) {
            throw IllegalStateException(
                "Registro creado pero sin sesion activa. Desactiva Email Confirmations en Supabase Auth para el MVP."
            )
        }

        val newToken = auth.accessToken

        val business = client.post("$baseUrl/businesses") {
            authHeaders(this, newToken)
            contentType(ContentType.Application.Json)
            header("Prefer", "return=representation")
            setBody(mapOf("name" to businessName.trim()))
        }.body<List<Business>>().firstOrNull()
            ?: throw IllegalStateException("No se pudo crear negocio")

        client.post("$baseUrl/user_businesses") {
            authHeaders(this, newToken)
            contentType(ContentType.Application.Json)
            header("Prefer", "return=minimal")
            setBody(
                UserBusiness(
                    userId = auth.user.id,
                    businessId = business.id,
                )
            )
        }

        saveSession(auth, business.id, business.name)

        return SessionInfo(
            userId = auth.user.id,
            userEmail = auth.user.email ?: email.trim(),
            businessId = business.id,
            businessName = business.name,
        )
    }

    fun logout() {
        prefs.edit()
            .remove("access_token")
            .remove("refresh_token")
            .remove("user_id")
            .remove("user_email")
            .remove("business_id")
                .remove("business_name")
            .apply()
    }

    private fun authHeaders(builder: io.ktor.client.request.HttpRequestBuilder, overrideToken: String? = null) {
        builder.header("apikey", BuildConfig.SUPABASE_ANON_KEY)
        builder.header(HttpHeaders.Authorization, "Bearer ${overrideToken ?: accessToken ?: BuildConfig.SUPABASE_ANON_KEY}")
    }

    private suspend fun resolveBusinessId(authUserId: String, token: String): String {
        val memberships = client.get("$baseUrl/user_businesses?user_id=eq.$authUserId&select=business_id&limit=1") {
            authHeaders(this, token)
        }.body<List<UserBusiness>>()

        return memberships.firstOrNull()?.businessId
            ?: throw IllegalStateException("Este usuario no tiene colmado asignado")
    }

    private suspend fun fetchBusinessName(resolvedBusinessId: String, token: String): String {
        return runCatching {
            val business = client.get("$baseUrl/businesses?id=eq.$resolvedBusinessId&select=id,name&limit=1") {
                authHeaders(this, token)
            }.body<List<Business>>().firstOrNull()

            business?.name?.trim().orEmpty().ifBlank { "Mi colmado" }
        }.getOrElse {
            // Fallback for older accounts/policies so login still succeeds.
            "Mi colmado"
        }
    }

    private fun saveSession(auth: AuthResponse, resolvedBusinessId: String, resolvedBusinessName: String) {
        prefs.edit()
            .putString("access_token", auth.accessToken.orEmpty())
            .putString("refresh_token", auth.refreshToken.orEmpty())
            .putString("user_id", auth.user.id)
            .putString("user_email", auth.user.email.orEmpty())
            .putString("business_id", resolvedBusinessId)
            .putString("business_name", resolvedBusinessName)
            .apply()
    }

    private fun parseAuthResponse(isSuccess: Boolean, raw: String, actionLabel: String): AuthResponse {
        if (!isSuccess) {
            val message = runCatching {
                val root = json.parseToJsonElement(raw).jsonObject
                root["msg"]?.jsonPrimitive?.content
                    ?: root["error_description"]?.jsonPrimitive?.content
                    ?: root["message"]?.jsonPrimitive?.content
            }.getOrNull()

            throw IllegalStateException(message ?: "No se pudo $actionLabel")
        }

        return runCatching {
            json.decodeFromString(AuthResponse.serializer(), raw)
        }.getOrElse {
            throw IllegalStateException("Respuesta inesperada de autenticacion. Verifica la configuracion de Supabase Auth")
        }
    }

    private fun requireBusinessId(): String {
        return businessId ?: throw IllegalStateException("Sesion no iniciada")
    }

    suspend fun getCustomersWithBalance(): List<CustomerBalance> {
        val currentBusinessId = requireBusinessId()
        return client.get("$baseUrl/customer_balances?business_id=eq.$currentBusinessId&order=balance.desc") {
            authHeaders(this)
        }.body()
    }

    suspend fun createCustomer(name: String, phone: String, alias: String?) {
        val currentBusinessId = requireBusinessId()
        val normalizedPhone = normalizePhoneForRd(phone)
        client.post("$baseUrl/customers") {
            authHeaders(this)
            contentType(ContentType.Application.Json)
            header("Prefer", "return=minimal")
            setBody(
                CreateCustomerRequest(
                    businessId = currentBusinessId,
                    name = name.trim(),
                    phone = normalizedPhone,
                    alias = alias?.trim()?.takeIf { it.isNotBlank() },
                )
            )
        }
    }

    suspend fun updateCustomer(customerId: String, name: String, phone: String, alias: String?) {
        val currentBusinessId = requireBusinessId()
        val normalizedPhone = normalizePhoneForRd(phone)
        client.patch("$baseUrl/customers?id=eq.$customerId&business_id=eq.$currentBusinessId") {
            authHeaders(this)
            contentType(ContentType.Application.Json)
            header("Prefer", "return=minimal")
            setBody(
                UpdateCustomerRequest(
                    name = name.trim(),
                    phone = normalizedPhone,
                    alias = alias?.trim()?.takeIf { it.isNotBlank() },
                )
            )
        }
    }

    private fun normalizePhoneForRd(rawPhone: String): String {
        val digits = rawPhone.filter { it.isDigit() }
        return when {
            digits.length == 10 -> "+1$digits"
            digits.length == 11 && digits.startsWith("1") -> "+$digits"
            rawPhone.trim().startsWith("+") && digits.isNotBlank() -> "+$digits"
            digits.isNotBlank() -> "+$digits"
            else -> rawPhone.trim()
        }
    }

    suspend fun getLedgerByCustomer(customerId: String): List<LedgerEntry> {
        val currentBusinessId = requireBusinessId()
        return client.get("$baseUrl/ledger_entries?customer_id=eq.$customerId&business_id=eq.$currentBusinessId&order=created_at.desc") {
            authHeaders(this)
        }.body()
    }

    suspend fun getBusinessLedger(): List<LedgerEntry> {
        val currentBusinessId = requireBusinessId()
        return client.get("$baseUrl/ledger_entries?business_id=eq.$currentBusinessId&order=created_at.desc") {
            authHeaders(this)
        }.body()
    }

    suspend fun createLedgerEntry(customerId: String, type: String, amount: Double, note: String?) {
        val currentBusinessId = requireBusinessId()
        client.post("$baseUrl/ledger_entries") {
            authHeaders(this)
            contentType(ContentType.Application.Json)
            header("Prefer", "return=minimal")
            setBody(
                CreateLedgerEntryRequest(
                    businessId = currentBusinessId,
                    customerId = customerId,
                    type = type,
                    amount = amount,
                    note = note?.trim()?.takeIf { it.isNotBlank() },
                )
            )
        }
    }

    suspend fun updateLedgerEntry(entryId: String, amount: Double, note: String?) {
        val currentBusinessId = requireBusinessId()
        client.patch("$baseUrl/ledger_entries?id=eq.$entryId&business_id=eq.$currentBusinessId") {
            authHeaders(this)
            contentType(ContentType.Application.Json)
            header("Prefer", "return=minimal")
            setBody(
                UpdateLedgerEntryRequest(
                    amount = amount,
                    note = note?.trim()?.takeIf { it.isNotBlank() },
                )
            )
        }
    }
}
