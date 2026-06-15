package com.vasooli.radar.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vasooli.radar.domain.Reason
import com.vasooli.radar.domain.RiskBand
import com.vasooli.radar.domain.shortMoney
import com.vasooli.radar.ui.theme.HighRed
import com.vasooli.radar.ui.theme.SafeGreen
import com.vasooli.radar.ui.theme.WatchAmber

fun riskColor(b: RiskBand): Color = when (b) {
    RiskBand.SAFE -> SafeGreen
    RiskBand.WATCH -> WatchAmber
    RiskBand.HIGH -> HighRed
}

fun riskLabel(b: RiskBand): String = when (b) {
    RiskBand.SAFE -> "Safe"
    RiskBand.WATCH -> "Watch"
    RiskBand.HIGH -> "High Risk"
}

fun severityColor(severity: Int): Color = when (severity) {
    3 -> HighRed
    2 -> WatchAmber
    else -> SafeGreen
}

fun dial(context: Context, phone: String) {
    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
}

@Composable
fun RiskBadge(band: RiskBand, modifier: Modifier = Modifier) {
    val c = riskColor(band)
    Row(
        modifier
            .clip(RoundedCornerShape(50))
            .background(c.copy(alpha = 0.14f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(c))
        Spacer(Modifier.width(6.dp))
        Text(riskLabel(band), style = MaterialTheme.typography.labelMedium, color = c, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun ReasonChip(reason: Reason, modifier: Modifier = Modifier) {
    val c = severityColor(reason.severity)
    Row(
        modifier
            .clip(RoundedCornerShape(10.dp))
            .background(c.copy(alpha = 0.10f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(6.dp).clip(CircleShape).background(c))
        Spacer(Modifier.width(6.dp))
        Text(reason.text, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun InitialsAvatar(name: String, accent: Color, modifier: Modifier = Modifier) {
    val initials = name.trim().split(" ").take(2)
        .mapNotNull { it.firstOrNull()?.uppercase() }.joinToString("")
    Box(
        modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(accent.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Text(initials.ifEmpty { "?" }, color = accent, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun RiskGauge(score: Int, band: RiskBand, modifier: Modifier = Modifier) {
    val color = riskColor(band)
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    Box(modifier.size(168.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 18.dp.toPx()
            val start = 150f
            val sweepMax = 240f
            val arcSize = Size(size.width - stroke, size.height - stroke)
            val topLeft = Offset(stroke / 2, stroke / 2)
            drawArc(trackColor, start, sweepMax, false, topLeft, arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
            drawArc(color, start, sweepMax * (score / 100f), false, topLeft, arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$score", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = color)
            Text("RISK SCORE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun Sparkline(values: List<Double>, color: Color, modifier: Modifier = Modifier) {
    val max = (values.maxOrNull() ?: 0.0).coerceAtLeast(1.0)
    Canvas(modifier) {
        if (values.size < 2) return@Canvas
        val stepX = size.width / (values.size - 1)
        val line = Path()
        values.forEachIndexed { i, v ->
            val x = stepX * i
            val y = size.height - (v / max * size.height).toFloat()
            if (i == 0) line.moveTo(x, y) else line.lineTo(x, y)
        }
        val fill = Path()
        fill.addPath(line)
        fill.lineTo(size.width, size.height)
        fill.lineTo(0f, size.height)
        fill.close()
        drawPath(fill, Brush.verticalGradient(listOf(color.copy(alpha = 0.25f), color.copy(alpha = 0f))))
        drawPath(line, color, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
    }
}

@Composable
fun AgingChart(aging: List<Double>, modifier: Modifier = Modifier) {
    val labels = listOf("0–15", "16–30", "31–45", "45+")
    val colors = listOf(SafeGreen, Color(0xFF65A30D), WatchAmber, HighRed)
    val max = (aging.maxOrNull() ?: 0.0).coerceAtLeast(1.0)
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Bottom) {
        aging.forEachIndexed { i, v ->
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    if (v > 0) shortMoney(v) else "–",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height((90.0 * (v / max)).dp.coerceAtLeast(4.dp))
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (v > 0) colors[i] else MaterialTheme.colorScheme.surfaceVariant)
                )
                Spacer(Modifier.height(6.dp))
                Text(labels[i], style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
