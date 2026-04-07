package com.fiaocontrol.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fiaocontrol.app.data.CustomerBalance
import com.fiaocontrol.app.data.LedgerEntry
import java.time.LocalDate
import java.time.OffsetDateTime

@Composable
fun DashboardScreen(
    businessName: String,
    customers: List<CustomerBalance>,
    businessEntries: List<LedgerEntry>,
    onOpenCustomer: (String) -> Unit,
    onOpenCustomers: () -> Unit,
) {
    var showAdvanced by remember { mutableStateOf(false) }
    val today = remember { LocalDate.now() }
    val debtors = customers.filter { it.balance > 0.0 }
    val totalDebt = debtors.sumOf { it.balance }
    val averageDebt = if (debtors.isNotEmpty()) totalDebt / debtors.size else 0.0
    val highRiskCount = debtors.count { it.balance >= 3000.0 }
    val topDebtor = debtors.maxByOrNull { it.balance }
    val todayEntries = remember(businessEntries) {
        businessEntries.filter { entry ->
            val entryDate = parseDashboardDate(entry.createdAt)
            entryDate == today
        }
    }
    val weekEntries = remember(businessEntries) {
        businessEntries.filter { parseDashboardDate(it.createdAt) >= today.minusDays(6) }
    }
    val totalAbono = todayEntries.filter { it.type == "abono" }.sumOf { it.amount }
    val totalFiao = todayEntries.filter { it.type == "fiao" }.sumOf { it.amount }
    val netMovement = totalFiao - totalAbono
    val payerLeader = weekEntries
        .filter { it.type == "abono" }
        .groupBy { it.customerId }
        .mapValues { (_, entries) -> entries.sumOf { it.amount } }
        .maxByOrNull { it.value }
        ?.let { leader ->
            customers.firstOrNull { it.customerId == leader.key }?.let { customer -> customer to leader.value }
        }
    val fiaoLeader = weekEntries
        .filter { it.type == "fiao" }
        .groupBy { it.customerId }
        .mapValues { (_, entries) -> entries.sumOf { it.amount } }
        .maxByOrNull { it.value }
        ?.let { leader ->
            customers.firstOrNull { it.customerId == leader.key }?.let { customer -> customer to leader.value }
        }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(16.dp),
    ) {
        item {
            Text(businessName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Resumen claro del dia", style = MaterialTheme.typography.bodyLarge)
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Ahora mismo te deben", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "RD$ ${"%.2f".format(totalDebt)}",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    if (topDebtor != null) {
                        Text(
                            "Prioridad: ${topDebtor.name} debe RD$ ${"%.2f".format(topDebtor.balance)}",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    } else {
                        Text("No hay deudas pendientes.", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard(
                    title = "Cobrado hoy",
                    value = "RD$ ${"%.2f".format(totalAbono)}",
                    modifier = Modifier.weight(1f),
                )
                MetricCard(
                    title = "Fiado hoy",
                    value = "RD$ ${"%.2f".format(totalFiao)}",
                    modifier = Modifier.weight(1f),
                )
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard(
                    title = "Balance de hoy",
                    value = "RD$ ${"%.2f".format(netMovement)}",
                    modifier = Modifier.weight(1f),
                )
                MetricCard(
                    title = "Fiadores activos",
                    value = debtors.size.toString(),
                    modifier = Modifier.weight(1f),
                )
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Que mirar hoy", fontWeight = FontWeight.SemiBold)
                    Text(buildFocusMessage(totalAbono, totalFiao, netMovement, highRiskCount))
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Button(onClick = onOpenCustomers, modifier = Modifier.weight(1f)) {
                    Text("Ver clientes")
                }
                Button(onClick = { showAdvanced = !showAdvanced }, modifier = Modifier.weight(1f)) {
                    Text(if (showAdvanced) "Ocultar detalle" else "Ver mas")
                }
            }
        }

        item {
            Text("Clientes que atender primero", fontWeight = FontWeight.SemiBold)
        }

        items(debtors.take(3)) { customer ->
            Card(
                onClick = { onOpenCustomer(customer.customerId) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(customer.name, fontWeight = FontWeight.SemiBold)
                        Text(customer.phone, style = MaterialTheme.typography.bodySmall)
                    }
                    Text("RD$ ${"%.2f".format(customer.balance)}")
                }
            }
        }

        if (showAdvanced) {
            item {
                Text("Detalle ampliado", fontWeight = FontWeight.SemiBold)
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard(
                        title = "Promedio por fiador",
                        value = "RD$ ${"%.2f".format(averageDebt)}",
                        modifier = Modifier.weight(1f),
                    )
                    MetricCard(
                        title = "Riesgo alto (>=3000)",
                        value = highRiskCount.toString(),
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    LeaderCard(
                        title = "Mas pago esta semana",
                        customerName = payerLeader?.first?.name,
                        subtitle = payerLeader?.first?.phone,
                        amount = payerLeader?.second,
                        emptyText = "Sin abonos esta semana",
                        modifier = Modifier.weight(1f),
                    )
                    LeaderCard(
                        title = "Mas fiao esta semana",
                        customerName = fiaoLeader?.first?.name,
                        subtitle = fiaoLeader?.first?.phone,
                        amount = fiaoLeader?.second,
                        emptyText = "Sin fiaos esta semana",
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        if (debtors.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(14.dp)) {
                        Text("No hay deudas pendientes. Excelente trabajo hoy.")
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.bodySmall)
            Row {
                Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(2.dp))
            }
        }
    }
}

private fun buildFocusMessage(totalAbono: Double, totalFiao: Double, netMovement: Double, highRiskCount: Int): String {
    return when {
        highRiskCount > 0 && netMovement > 0 -> "Hoy la deuda subio. Hay $highRiskCount cliente(s) en riesgo alto; conviene cobrar antes de seguir fiando."
        totalAbono > totalFiao -> "Vas bien hoy: se ha cobrado mas de lo que se ha fiado."
        totalFiao > totalAbono -> "Ojo: hoy se ha fiado mas de lo que se ha cobrado."
        else -> "El dia va parejo; revisa primero los clientes con mayor deuda."
    }
}

@Composable
private fun LeaderCard(
    title: String,
    customerName: String?,
    subtitle: String?,
    amount: Double?,
    emptyText: String,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.bodySmall)
            if (customerName == null || amount == null) {
                Text(emptyText, style = MaterialTheme.typography.bodyMedium)
            } else {
                Text(customerName, fontWeight = FontWeight.SemiBold)
                subtitle?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                Text("RD$ ${"%.2f".format(amount)}", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

private fun parseDashboardDate(raw: String): LocalDate {
    return runCatching { OffsetDateTime.parse(raw).toLocalDate() }
        .getOrElse {
            runCatching { LocalDate.parse(raw.take(10)) }.getOrDefault(LocalDate.now())
        }
}
