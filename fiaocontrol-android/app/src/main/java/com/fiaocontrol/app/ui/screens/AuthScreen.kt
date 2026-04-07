package com.fiaocontrol.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun AuthScreen(
    loading: Boolean,
    onLogin: (email: String, password: String) -> Unit,
    onRegister: (email: String, password: String, businessName: String) -> Unit,
) {
    var businessName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isRegister by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(16.dp),
    ) {
        Text(if (isRegister) "Crear cuenta de colmado" else "Iniciar sesion")

        if (isRegister) {
            OutlinedTextField(
                value = businessName,
                onValueChange = { businessName = it },
                label = { Text("Nombre del colmado") },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it; localError = null },
            label = { Text("Correo") },
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; localError = null },
            label = { Text("Contrasena") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
        )

        localError?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Button(
            onClick = {
                if (isRegister) {
                    when {
                        businessName.isBlank() -> localError = "Escribe el nombre del colmado"
                        email.isBlank() -> localError = "Escribe el correo"
                        password.length < 6 -> localError = "La contrasena debe tener al menos 6 caracteres"
                        else -> { localError = null; onRegister(email, password, businessName) }
                    }
                } else {
                    when {
                        email.isBlank() -> localError = "Escribe el correo"
                        password.isBlank() -> localError = "Escribe la contrasena"
                        else -> { localError = null; onLogin(email, password) }
                    }
                }
            },
            enabled = !loading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (loading) "Procesando..." else if (isRegister) "Crear cuenta" else "Entrar")
        }

        Button(
            onClick = { isRegister = !isRegister },
            enabled = !loading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (isRegister) "Ya tengo cuenta" else "Crear cuenta nueva")
        }
    }
}
