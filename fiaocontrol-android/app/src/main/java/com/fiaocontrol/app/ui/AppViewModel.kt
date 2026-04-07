package com.fiaocontrol.app.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fiaocontrol.app.data.CustomerBalance
import com.fiaocontrol.app.data.FiaoRepository
import com.fiaocontrol.app.data.LedgerEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class UiState(
    val loading: Boolean = false,
    val authLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val userEmail: String = "",
    val businessId: String = "",
    val businessName: String = "Mi colmado",
    val customers: List<CustomerBalance> = emptyList(),
    val businessEntries: List<LedgerEntry> = emptyList(),
    val selectedCustomer: CustomerBalance? = null,
    val entries: List<LedgerEntry> = emptyList(),
    val error: String? = null,
)

class AppViewModel(
    private val repository: FiaoRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(UiState(loading = false))
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        bootstrapSession()
    }

    private fun bootstrapSession() {
        val session = repository.getSessionInfo()
        if (session == null) {
            _state.value = _state.value.copy(isAuthenticated = false, loading = false)
            return
        }

        _state.value = _state.value.copy(
            isAuthenticated = true,
            userEmail = session.userEmail,
            businessId = session.businessId,
            businessName = session.businessName,
            loading = true,
        )
        refreshCustomers()
        refreshBusinessEntries()
    }

    fun login(email: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = _state.value.copy(authLoading = true, error = null)
            runCatching { repository.login(email, password) }
                .onSuccess { session ->
                    _state.value = _state.value.copy(
                        isAuthenticated = true,
                        authLoading = false,
                        userEmail = session.userEmail,
                        businessId = session.businessId,
                        businessName = session.businessName,
                        error = null,
                    )
                    refreshCustomers()
                    refreshBusinessEntries()
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isAuthenticated = false,
                        authLoading = false,
                        error = error.message ?: "No se pudo iniciar sesion",
                    )
                }
        }
    }

    fun register(email: String, password: String, businessName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = _state.value.copy(authLoading = true, error = null)
            runCatching { repository.register(email, password, businessName) }
                .onSuccess { session ->
                    _state.value = _state.value.copy(
                        isAuthenticated = true,
                        authLoading = false,
                        userEmail = session.userEmail,
                        businessId = session.businessId,
                        businessName = session.businessName,
                        error = null,
                    )
                    refreshCustomers()
                    refreshBusinessEntries()
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isAuthenticated = false,
                        authLoading = false,
                        error = error.message ?: "No se pudo registrar el colmado",
                    )
                }
        }
    }

    fun logout() {
        repository.logout()
        _state.value = UiState(isAuthenticated = false)
    }

    fun refreshCustomers() {
        if (!_state.value.isAuthenticated) return

        viewModelScope.launch(Dispatchers.IO) {
            _state.value = _state.value.copy(loading = true, error = null)
            runCatching { repository.getCustomers() }
                .onSuccess { customers ->
                    _state.value = _state.value.copy(
                        loading = false,
                        customers = customers,
                        error = null,
                    )
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        loading = false,
                        error = error.message ?: "No se pudo cargar clientes",
                    )
                }
        }
    }

    fun createCustomer(name: String, phone: String, alias: String?, onDone: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { repository.createCustomer(name, phone, alias) }
                .onSuccess {
                    refreshCustomers()
                    withContext(Dispatchers.Main) { onDone() }
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(error = error.message ?: "No se pudo crear cliente")
                }
        }
    }

    fun updateCustomer(customerId: String, name: String, phone: String, alias: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { repository.updateCustomer(customerId, name, phone, alias) }
                .onSuccess {
                    refreshCustomers()
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(error = error.message ?: "No se pudo editar cliente")
                }
        }
    }

    fun selectCustomer(customerId: String) {
        val customer = _state.value.customers.firstOrNull { it.customerId == customerId }
        _state.value = _state.value.copy(selectedCustomer = customer)
        refreshLedger(customerId)
    }

    fun refreshLedger(customerId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { repository.getLedger(customerId) }
                .onSuccess { entries ->
                    _state.value = _state.value.copy(entries = entries, error = null)
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(error = error.message ?: "No se pudo cargar historial")
                }
        }
    }

    fun refreshBusinessEntries() {
        if (!_state.value.isAuthenticated) return

        viewModelScope.launch(Dispatchers.IO) {
            runCatching { repository.getBusinessLedger() }
                .onSuccess { ledgerEntries ->
                    _state.value = _state.value.copy(businessEntries = ledgerEntries, error = null)
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(error = error.message ?: "No se pudieron cargar las estadisticas")
                }
        }
    }

    fun createEntry(customerId: String, type: String, amount: Double, note: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { repository.createEntry(customerId, type, amount, note) }
                .onSuccess {
                    refreshCustomers()
                    refreshLedger(customerId)
                    refreshBusinessEntries()
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(error = error.message ?: "No se pudo registrar movimiento")
                }
        }
    }

    fun updateEntry(customerId: String, entryId: String, amount: Double, note: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { repository.updateEntry(entryId, amount, note) }
                .onSuccess {
                    refreshCustomers()
                    refreshLedger(customerId)
                    refreshBusinessEntries()
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(error = error.message ?: "No se pudo editar movimiento")
                }
        }
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val repository = FiaoRepository(context.applicationContext)
                    return AppViewModel(repository) as T
                }
            }
    }
}
