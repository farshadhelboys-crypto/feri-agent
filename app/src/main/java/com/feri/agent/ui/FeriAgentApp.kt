package com.feri.agent.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FeriAgentApp(
    onStartService: () -> Unit,
    onStopService: () -> Unit
) {
    var isRunning by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("آماده برای شروع") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F0F13),
                        Color(0xFF1A1A2E),
                        Color(0xFF16213E)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Logo / Title
            Text(
                text = "Feri Agent",
                style = MaterialTheme.typography.displayLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "دستیار صوتی هوشمند",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFFB0B0C0),
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Animated mic indicator
            MicIndicator(isActive = isRunning)

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = statusText,
                style = MaterialTheme.typography.titleLarge,
                color = if (isRunning) Color(0xFF00CEC9) else Color(0xFFB0B0C0),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Feature cards
            FeatureGrid()

            Spacer(modifier = Modifier.weight(1f))

            // Start / Stop button
            Button(
                onClick = {
                    if (isRunning) {
                        onStopService()
                        isRunning = false
                        statusText = "متوقف شد"
                    } else {
                        onStartService()
                        isRunning = true
                        statusText = "فعال • منتظر «داداش؟»"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) Color(0xFFE17055) else Color(0xFF6C5CE7)
                )
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (isRunning) "توقف سرویس" else "شروع Feri Agent",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "برای بهترین عملکرد، ایرپاد را وصل کنید",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF707080),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MicIndicator(isActive: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "mic")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isActive) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(140.dp)
    ) {
        // Outer glow
        if (isActive) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(Color(0xFF6C5CE7).copy(alpha = 0.2f))
            )
        }

        // Main circle
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(
                    if (isActive) Color(0xFF6C5CE7) else Color(0xFF252532)
                )
        ) {
            Icon(
                imageVector = Icons.Rounded.Mic,
                contentDescription = "Microphone",
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

@Composable
private fun FeatureGrid() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FeatureCard(
                icon = Icons.Rounded.MusicNote,
                title = "کنترل موزیک",
                subtitle = "بعدی • قبلی • ولوم",
                modifier = Modifier.weight(1f)
            )
            FeatureCard(
                icon = Icons.Rounded.Phone,
                title = "تماس سریع",
                subtitle = "زنگ به مخاطبین",
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FeatureCard(
                icon = Icons.Rounded.Headset,
                title = "ایرپاد",
                subtitle = "فعال با بلوتوث",
                modifier = Modifier.weight(1f)
            )
            FeatureCard(
                icon = Icons.Rounded.RecordVoiceOver,
                title = "دستور فارسی",
                subtitle = "بگو داداش؟",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun FeatureCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A24)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF6C5CE7),
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF9090A0),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
