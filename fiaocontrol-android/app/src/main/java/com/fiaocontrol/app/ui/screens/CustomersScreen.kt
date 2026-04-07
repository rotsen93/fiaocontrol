package com.fiaocontrol.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fiaocontrol.app.data.CustomerBalance

@Composable
fun CustomersScreen(
    customers: List<CustomerBalance>,
    onOpenCustomer: (String) -> Unit,
    onCreate: () -> Unit,
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(16.dp),
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Clientes", fontWeight = FontWeight.Bold)
                Button(onClick = onCreate) { Text("+ Nuevo") }
            }
        }

        items(customers) { customer ->
            Card(onClick = { onOpenCustomer(customer.customerId) }, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(customer.name, fontWeight = FontWeight.SemiBold)
                        Text(customer.phone)
                    }
                    Text("RD$ ${"%.2f".format(customer.balance)}")
                }
            }
        }
    }
}
