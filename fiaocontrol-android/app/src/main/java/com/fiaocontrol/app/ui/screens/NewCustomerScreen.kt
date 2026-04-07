package com.fiaocontrol.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NewCustomerScreen(
    onSave: (name: String, phone: String, alias: String?) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var alias by remember { mutableStateOf("") }

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(16.dp),
    ) {
        Text("Nuevo cliente")
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Telefono") },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = alias,
            onValueChange = { alias = it },
            label = { Text("Alias (opcional)") },
            modifier = Modifier.fillMaxWidth(),
        )

        Button(
            onClick = {
                if (name.isNotBlank() && phone.isNotBlank()) {
                    onSave(name, phone, alias.ifBlank { null })
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Guardar")
        }
    }
}
