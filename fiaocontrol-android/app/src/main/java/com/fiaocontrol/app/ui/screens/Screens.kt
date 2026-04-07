package com.fiaocontrol.app.ui.screens

sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object Stats : Screen("stats")
    data object Customers : Screen("customers")
    data object NewCustomer : Screen("new-customer")
    data object CustomerDetail : Screen("customer/{id}") {
        fun route(id: String) = "customer/$id"
    }
}
