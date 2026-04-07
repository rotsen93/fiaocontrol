package com.fiaocontrol.app.ui.screens

import android.content.Intent
import android.net.Uri
import java.net.URLEncoder
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fiaocontrol.app.data.CustomerBalance
import com.fiaocontrol.app.data.LedgerEntry

@Composable
fun CustomerDetailScreen(
    businessName: String,
    customer: CustomerBalance,
    entries: List<LedgerEntry>,
    onUpdateCustomer: (name: String, phone: String, alias: String?) -> Unit,
    onSaveEntry: (type: String, amount: Double, note: String?) -> Unit,
    onUpdateEntry: (entryId: String, amount: Double, note: String?) -> Unit,
) {
    val context = LocalContext.current
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var detailsExpanded by remember { mutableStateOf(false) }
    var editingCustomer by remember(customer.customerId, customer.name, customer.phone, customer.alias) { mutableStateOf(false) }
    var customerName by remember(customer.customerId, customer.name) { mutableStateOf(customer.name) }
    var customerPhone by remember(customer.customerId, customer.phone) { mutableStateOf(customer.phone) }
    var customerAlias by remember(customer.customerId, customer.alias) { mutableStateOf(customer.alias.orEmpty()) }
    val debtStartDate = entries.lastOrNull()?.createdAt
    val latestMovementDate = entries.firstOrNull()?.createdAt

    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { detailsExpanded = !detailsExpanded },
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                        if (editingCustomer) {
                            OutlinedTextField(
                                value = customerName,
                                onValueChange = { customerName = it },
                                label = { Text("Nombre") },
                                modifier = Modifier.fillMaxWidth(),
                            )
                            OutlinedTextField(
                                value = customerPhone,
                                onValueChange = { customerPhone = it },
                                label = { Text("Telefono") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier.fillMaxWidth(),
                            )
                            OutlinedTextField(
                                value = customerAlias,
                                onValueChange = { customerAlias = it },
                                label = { Text("Alias") },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        } else {
                            Text(customer.name, style = androidx.compose.material3.MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(customer.phone)
                            customer.alias?.takeIf { it.isNotBlank() }?.let { Text("Alias: $it") }
                        }
                    }
                    IconButton(onClick = {
                        if (editingCustomer) {
                            onUpdateCustomer(customerName, customerPhone, customerAlias.ifBlank { null })
                        }
                        editingCustomer = !editingCustomer
                    }) {
                        Icon(Icons.Outlined.Edit, contentDescription = "Editar cliente")
                    }
                }

                Text(
                    "Balance: RD$ ${"%.2f".format(customer.balance)}",
                    style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(if (detailsExpanded) "Toca para ocultar detalle" else "Toca para ver detalle de la deuda")

                if (detailsExpanded) {
                    DebtDetailRow("Entrada de deuda", debtStartDate?.let { formatEntryDate(it) } ?: "Sin movimientos")
                    DebtDetailRow("Ultimo movimiento", latestMovementDate?.let { formatEntryDate(it) } ?: "Sin movimientos")
                    DebtDetailRow("Movimientos", entries.size.toString())
                    DebtDetailRow("Estado", if (customer.balance > 0) "Tiene deuda pendiente" else "Cuenta al dia")
                }
            }
        }

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
            label = { Text("Monto") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("Nota") },
            modifier = Modifier.fillMaxWidth(),
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = {
                    val value = amount.toDoubleOrNull() ?: return@Button
                    onSaveEntry("fiao", value, note.ifBlank { null })
                    amount = ""
                    note = ""
                },
                modifier = Modifier.weight(1f),
            ) { Text("+ Fiao") }

            Button(
                onClick = {
                    val value = amount.toDoubleOrNull() ?: return@Button
                    onSaveEntry("abono", value, note.ifBlank { null })
                    amount = ""
                    note = ""
                },
                modifier = Modifier.weight(1f),
            ) { Text("- Abono") }
        }

        Button(
            onClick = {
                val phone = normalizePhoneForWhatsapp(customer.phone)
                val text = buildWhatsappMessage(businessName, customer, entries)
                val encodedText = URLEncoder.encode(text, "UTF-8")
                val url = "https://wa.me/$phone?text=$encodedText"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("WhatsApp: enviar estado")
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(entries) { entry ->
                EditableLedgerEntryCard(entry = entry, onUpdateEntry = onUpdateEntry)
            }
        }
    }
}

@Composable
private fun DebtDetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.size(8.dp))
        Text(value)
    }
}

@Composable
private fun EditableLedgerEntryCard(
    entry: LedgerEntry,
    onUpdateEntry: (entryId: String, amount: Double, note: String?) -> Unit,
) {
    var editingEntry by remember(entry.id) { mutableStateOf(false) }
    var editAmount by remember(entry.id) { mutableStateOf("%.2f".format(entry.amount)) }
    var editNote by remember(entry.id) { mutableStateOf(entry.note.orEmpty()) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(movementLabel(entry.type), fontWeight = FontWeight.SemiBold)
                    Text(formatEntryDate(entry.createdAt))
                }
                IconButton(onClick = {
                    if (editingEntry) {
                        val parsedAmount = editAmount.toDoubleOrNull()
                        if (parsedAmount != null) {
                            onUpdateEntry(entry.id, parsedAmount, editNote.ifBlank { null })
                        }
                    }
                    editingEntry = !editingEntry
                }) {
                    Icon(Icons.Outlined.Edit, contentDescription = "Editar movimiento")
                }
            }

            if (editingEntry) {
                OutlinedTextField(
                    value = editAmount,
                    onValueChange = { editAmount = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Monto") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = editNote,
                    onValueChange = { editNote = it },
                    label = { Text("Nota") },
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                entry.note?.let { Text(it) }
                Text("RD$ ${"%.2f".format(entry.amount)}", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
            }
        }
    }
}

private fun normalizePhoneForWhatsapp(phoneRaw: String): String {
    val digits = phoneRaw.filter { it.isDigit() }
    return when {
        digits.length == 10 -> "1$digits"
        digits.length == 11 && digits.startsWith("1") -> digits
        else -> digits
    }
}

private fun buildWhatsappMessage(businessName: String, customer: CustomerBalance, entries: List<LedgerEntry>): String {
    val latest = entries.firstOrNull()
    val latestLine = if (latest == null) {
        "No hay movimientos recientes."
    } else {
        val signedAmount = if (latest.type == "abono") "-" else "+"
        val noteText = latest.note?.takeIf { it.isNotBlank() } ?: "sin nota"
        "Ultimo movimiento: ${movementLabel(latest.type)} $signedAmount RD$ ${"%.2f".format(latest.amount)} | ${formatEntryDate(latest.createdAt)} | $noteText"
    }

    val recent = entries.take(5).joinToString("\n") { entry ->
        val sign = if (entry.type == "abono") "-" else "+"
        val noteText = entry.note?.takeIf { it.isNotBlank() } ?: "sin nota"
        "- ${formatEntryDate(entry.createdAt)} | ${movementLabel(entry.type)} $sign RD$ ${"%.2f".format(entry.amount)} | $noteText"
    }.ifBlank { "- Sin historial todavia" }

    return """
Hola ${customer.name},

Este es tu estado de cuenta actualizado de $businessName.
Balance actual: RD$ ${"%.2f".format(customer.balance)}
$latestLine

Movimientos recientes:
$recent

Gracias por mantenerte al dia.
    """.trimIndent()
}

private fun movementLabel(type: String): String =
    if (type == "abono") "Abono" else "Fiao"

private fun formatEntryDate(createdAt: String): String {
    val normalized = createdAt.replace('T', ' ').replace("Z", "")
    return if (normalized.length >= 16) normalized.substring(0, 16) else normalized
}
