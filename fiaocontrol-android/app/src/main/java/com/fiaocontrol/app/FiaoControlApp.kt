package com.fiaocontrol.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.fiaocontrol.app.ui.AppViewModel
import com.fiaocontrol.app.ui.screens.CustomerDetailScreen
import com.fiaocontrol.app.ui.screens.CustomersScreen
import com.fiaocontrol.app.ui.screens.DashboardScreen
import com.fiaocontrol.app.ui.screens.NewCustomerScreen
import com.fiaocontrol.app.ui.screens.AuthScreen
import com.fiaocontrol.app.ui.screens.Screen
import com.fiaocontrol.app.ui.screens.StatsScreen

@Composable
fun FiaoControlApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val context = LocalContext.current
    val viewModel: AppViewModel = viewModel(factory = AppViewModel.factory(context))
    val state by viewModel.state.collectAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        floatingActionButton = {
            if (state.isAuthenticated && currentRoute != Screen.Stats.route) {
                FloatingActionButton(onClick = { navController.navigate(Screen.Stats.route) }) {
                    Text("Stats")
                }
            }
        },
    ) { padding ->
        if (!state.isAuthenticated) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                AuthScreen(
                    loading = state.authLoading,
                    onLogin = { email, password -> viewModel.login(email, password) },
                    onRegister = { email, password, businessName ->
                        viewModel.register(email, password, businessName)
                    },
                )

                state.error?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(12.dp),
                    )
                }
            }
            return@Scaffold
        }

        if (state.loading && state.customers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            NavHost(navController = navController, startDestination = Screen.Dashboard.route) {
                composable(Screen.Dashboard.route) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        DashboardScreen(
                            businessName = state.businessName,
                            customers = state.customers,
                            businessEntries = state.businessEntries,
                            onOpenCustomer = { navController.navigate(Screen.CustomerDetail.route(it)) },
                            onOpenCustomers = { navController.navigate(Screen.Customers.route) },
                        )
                        Button(
                            onClick = { viewModel.logout() },
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .fillMaxWidth(),
                        ) {
                            Text("Cerrar sesion")
                        }
                    }
                }

                composable(Screen.Customers.route) {
                    CustomersScreen(
                        customers = state.customers,
                        onOpenCustomer = { navController.navigate(Screen.CustomerDetail.route(it)) },
                        onCreate = { navController.navigate(Screen.NewCustomer.route) },
                    )
                }

                composable(Screen.Stats.route) {
                    StatsScreen(
                        businessName = state.businessName,
                        entries = state.businessEntries,
                    )
                }

                composable(Screen.NewCustomer.route) {
                    NewCustomerScreen { name, phone, alias ->
                        viewModel.createCustomer(name, phone, alias) {
                            navController.popBackStack()
                        }
                    }
                }

                composable(
                    route = Screen.CustomerDetail.route,
                    arguments = listOf(navArgument("id") { type = NavType.StringType }),
                ) { backStackEntry ->
                    val customerId = backStackEntry.arguments?.getString("id") ?: return@composable
                    val customer = state.customers.firstOrNull { it.customerId == customerId }
                    if (customer == null) {
                        Text("Cliente no encontrado")
                        return@composable
                    }

                    if (state.selectedCustomer?.customerId != customerId) {
                        viewModel.selectCustomer(customerId)
                    }

                    CustomerDetailScreen(
                        businessName = state.businessName,
                        customer = customer,
                        entries = state.entries,
                        onUpdateCustomer = { name, phone, alias ->
                            viewModel.updateCustomer(customerId, name, phone, alias)
                        },
                        onSaveEntry = { type, amount, note ->
                            viewModel.createEntry(customerId, type, amount, note)
                        },
                        onUpdateEntry = { entryId, amount, note ->
                            viewModel.updateEntry(customerId, entryId, amount, note)
                        },
                    )
                }
            }

            state.error?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(12.dp),
                )
            }
        }
    }
}
