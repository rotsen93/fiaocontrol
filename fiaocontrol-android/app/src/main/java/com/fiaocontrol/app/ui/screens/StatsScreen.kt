package com.fiaocontrol.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fiaocontrol.app.data.LedgerEntry
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

private enum class StatsPeriod {
    DAY,
    WEEK,
    MONTH,
}

@Composable
fun StatsScreen(
    businessName: String,
    entries: List<LedgerEntry>,
) {
    var selectedPeriod by remember { mutableStateOf(StatsPeriod.DAY) }
    val now = remember { LocalDate.now() }
    val filteredEntries = remember(entries, selectedPeriod) {
        when (selectedPeriod) {
            StatsPeriod.DAY -> entries.filter { parseEntryDate(it.createdAt) == now }
            StatsPeriod.WEEK -> entries.filter { parseEntryDate(it.createdAt) >= now.minusDays(6) }
            StatsPeriod.MONTH -> entries.filter { parseEntryDate(it.createdAt) >= now.minusDays(29) }
        }
    }
    val totalFiao = filteredEntries.filter { it.type == "fiao" }.sumOf { it.amount }
    val totalAbono = filteredEntries.filter { it.type == "abono" }.sumOf { it.amount }
    val netDebt = totalFiao - totalAbono

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(businessName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Stats del colmado", style = MaterialTheme.typography.bodyMedium)
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                StatsPeriodButton("Dia", selectedPeriod == StatsPeriod.DAY, Modifier.weight(1f)) { selectedPeriod = StatsPeriod.DAY }
                StatsPeriodButton("Semana", selectedPeriod == StatsPeriod.WEEK, Modifier.weight(1f)) { selectedPeriod = StatsPeriod.WEEK }
                StatsPeriodButton("Mes", selectedPeriod == StatsPeriod.MONTH, Modifier.weight(1f)) { selectedPeriod = StatsPeriod.MONTH }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                StatsMetricCard("Fiao", "RD$ ${formatAmount(totalFiao)}", Modifier.weight(1f))
                StatsMetricCard("Abono", "RD$ ${formatAmount(totalAbono)}", Modifier.weight(1f))
                StatsMetricCard("Neto", "RD$ ${formatAmount(netDebt)}", Modifier.weight(1f))
            }
        }

        item {
            if (selectedPeriod == StatsPeriod.DAY) {
                DailyDonutCard(totalFiao = totalFiao, totalAbono = totalAbono)
            } else {
                TrendBarsCard(period = selectedPeriod, entries = filteredEntries, today = now)
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Lectura rapida", fontWeight = FontWeight.SemiBold)
                    Text(buildStatsInsight(selectedPeriod, totalFiao, totalAbono, netDebt, filteredEntries.size))
                }
            }
        }
    }
}

@Composable
private fun RowScope.StatsPeriodButton(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier,
    ) {
        Text(if (selected) "[$label]" else label)
    }
}

@Composable
private fun StatsMetricCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.bodySmall)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun DailyDonutCard(totalFiao: Double, totalAbono: Double) {
    val total = totalFiao + totalAbono
    val fiaoColor = Color(0xFFD95D39)
    val abonoColor = Color(0xFF2D8F6F)
    val neutralColor = Color(0xFFD9D5CC)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Hoy: fiao vs abono", fontWeight = FontWeight.SemiBold)
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(220.dp)) {
                Canvas(modifier = Modifier.size(220.dp)) {
                    val stroke = 28.dp.toPx()
                    drawArc(
                        color = neutralColor,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = stroke, cap = StrokeCap.Round),
                        size = Size(size.width, size.height),
                    )

                    if (total > 0.0) {
                        val fiaoSweep = ((totalFiao / total) * 360f).toFloat()
                        drawArc(
                            color = fiaoColor,
                            startAngle = -90f,
                            sweepAngle = fiaoSweep,
                            useCenter = false,
                            style = Stroke(width = stroke, cap = StrokeCap.Round),
                            size = Size(size.width, size.height),
                        )
                        drawArc(
                            color = abonoColor,
                            startAngle = -90f + fiaoSweep,
                            sweepAngle = 360f - fiaoSweep,
                            useCenter = false,
                            style = Stroke(width = stroke, cap = StrokeCap.Round),
                            size = Size(size.width, size.height),
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${if (total == 0.0) 0 else ((totalAbono / total) * 100).roundToInt()}%", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("abonos del dia", style = MaterialTheme.typography.bodySmall)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                StatsLegend("Fiao", fiaoColor, totalFiao, Modifier.weight(1f))
                StatsLegend("Abono", abonoColor, totalAbono, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatsLegend(label: String, color: Color, amount: Double, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(modifier = Modifier.size(12.dp).background(color))
        Column {
            Text(label, style = MaterialTheme.typography.bodySmall)
            Text("RD$ ${formatAmount(amount)}", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun TrendBarsCard(period: StatsPeriod, entries: List<LedgerEntry>, today: LocalDate) {
    val bucketDates = when (period) {
        StatsPeriod.WEEK -> (6L downTo 0L).map { today.minusDays(it) }
        StatsPeriod.MONTH -> (29L downTo 0L step 3L).map { today.minusDays(it) }
        StatsPeriod.DAY -> listOf(today)
    }
    val grouped = bucketDates.map { date ->
        val dayEntries = entries.filter { parseEntryDate(it.createdAt) == date }
        val net = dayEntries.sumOf { if (it.type == "fiao") it.amount else -it.amount }
        date to net
    }
    val maxValue = grouped.maxOfOrNull { kotlin.math.abs(it.second) }?.takeIf { it > 0.0 } ?: 1.0

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(if (period == StatsPeriod.WEEK) "Ultimos 7 dias" else "Ultimo mes", fontWeight = FontWeight.SemiBold)
            Canvas(modifier = Modifier.fillMaxWidth().height(220.dp)) {
                val chartWidth = size.width
                val chartHeight = size.height
                val barWidth = chartWidth / (grouped.size * 1.7f)
                val baselineY = chartHeight * 0.8f

                drawLine(
                    color = Color(0xFF9E9A90),
                    start = Offset(0f, baselineY),
                    end = Offset(chartWidth, baselineY),
                    strokeWidth = 4f,
                )

                grouped.forEachIndexed { index, (_, value) ->
                    val x = (index * chartWidth / grouped.size) + (barWidth * 0.5f)
                    val heightFactor = (kotlin.math.abs(value) / maxValue).toFloat()
                    val barHeight = (chartHeight * 0.55f * heightFactor)
                    val top = if (value >= 0) baselineY - barHeight else baselineY
                    val color = if (value >= 0) Color(0xFFD95D39) else Color(0xFF2D8F6F)
                    drawRect(
                        color = color,
                        topLeft = Offset(x, top),
                        size = Size(barWidth, barHeight),
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                grouped.forEach { (date, _) ->
                    val label = if (period == StatsPeriod.WEEK) {
                        date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("es", "DO"))
                    } else {
                        date.format(DateTimeFormatter.ofPattern("d/MM"))
                    }
                    Text(label, style = MaterialTheme.typography.bodySmall)
                }
            }

            Text("Barras rojas: sube deuda por fiaos. Barras verdes: baja deuda por abonos.")
        }
    }
}

private fun buildStatsInsight(
    period: StatsPeriod,
    totalFiao: Double,
    totalAbono: Double,
    netDebt: Double,
    movesCount: Int,
): String {
    val periodLabel = when (period) {
        StatsPeriod.DAY -> "hoy"
        StatsPeriod.WEEK -> "esta semana"
        StatsPeriod.MONTH -> "este mes"
    }
    return when {
        movesCount == 0 -> "No hay movimientos $periodLabel. Cuando registres fiaos o abonos, aqui veras el pulso del colmado."
        netDebt > 0 -> "$periodLabel se han fiado RD$ ${formatAmount(totalFiao)} y se han cobrado RD$ ${formatAmount(totalAbono)}. La deuda neta subio RD$ ${formatAmount(netDebt)}."
        netDebt < 0 -> "$periodLabel se han cobrado RD$ ${formatAmount(totalAbono)} frente a RD$ ${formatAmount(totalFiao)} fiados. La deuda neta bajo RD$ ${formatAmount(-netDebt)}."
        else -> "$periodLabel el colmado va parejo: RD$ ${formatAmount(totalFiao)} en fiaos y RD$ ${formatAmount(totalAbono)} en abonos."
    }
}

private fun parseEntryDate(raw: String): LocalDate {
    return runCatching { OffsetDateTime.parse(raw).toLocalDate() }
        .getOrElse {
            runCatching { LocalDate.parse(raw.take(10)) }.getOrDefault(LocalDate.now())
        }
}

private fun formatAmount(value: Double): String = "%.2f".format(value)