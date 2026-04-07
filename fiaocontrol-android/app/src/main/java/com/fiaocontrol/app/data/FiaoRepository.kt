package com.fiaocontrol.app.data

import android.content.Context

class FiaoRepository(
    context: Context,
    private val api: SupabaseApi = SupabaseApi(context),
) {
    fun hasSession() = api.isLoggedIn()
    fun getSessionInfo() = api.getSessionInfo()
    suspend fun login(email: String, password: String) = api.login(email, password)
    suspend fun register(email: String, password: String, businessName: String) =
        api.register(email, password, businessName)
    fun logout() = api.logout()

    suspend fun getCustomers() = api.getCustomersWithBalance()
    suspend fun createCustomer(name: String, phone: String, alias: String?) = api.createCustomer(name, phone, alias)
    suspend fun updateCustomer(customerId: String, name: String, phone: String, alias: String?) =
        api.updateCustomer(customerId, name, phone, alias)
    suspend fun getLedger(customerId: String) = api.getLedgerByCustomer(customerId)
    suspend fun getBusinessLedger() = api.getBusinessLedger()
    suspend fun createEntry(customerId: String, type: String, amount: Double, note: String?) =
        api.createLedgerEntry(customerId, type, amount, note)
    suspend fun updateEntry(entryId: String, amount: Double, note: String?) =
        api.updateLedgerEntry(entryId, amount, note)
}
