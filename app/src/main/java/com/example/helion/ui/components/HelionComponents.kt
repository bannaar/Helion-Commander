package com.example.helion.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.helion.core.model.HelionEnvironment
import com.example.helion.core.model.SecurityClass
import com.example.ui.theme.HelionAmber
import com.example.ui.theme.HelionBorder
import com.example.ui.theme.HelionBorderGlow
import com.example.ui.theme.HelionCyan
import com.example.ui.theme.HelionCyanGlow
import com.example.ui.theme.HelionDangerRed
import com.example.ui.theme.HelionDeepGraphite
import com.example.ui.theme.HelionHazardOrange
import com.example.ui.theme.HelionHighSecGreen
import com.example.ui.theme.HelionLowSecOrange
import com.example.ui.theme.HelionNullSecPurple
import com.example.ui.theme.HelionShieldBlue
import com.example.ui.theme.HelionSurface
import com.example.ui.theme.HelionSurfaceHigh
import com.example.ui.theme.HelionSurfaceVariant
import com.example.ui.theme.HelionTextMuted
import com.example.ui.theme.HelionTextPrimary
import com.example.ui.theme.HelionTextSecondary
import com.example.ui.theme.HelionVoidBlack

@Composable
fun HelionCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    badgeText: String? = null,
    badgeColor: Color = HelionCyan,
    accentColor: Color = HelionCyan,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable { onClick() } else Modifier
            ),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = HelionSurface
        ),
        border = BorderStroke(1.dp, HelionBorder)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            if (title != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(width = 3.dp, height = 14.dp)
                                .background(accentColor)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = title.uppercase(),
                            style = MaterialTheme.typography.labelLarge,
                            color = HelionTextPrimary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                    if (badgeText != null) {
                        Surface(
                            shape = CutCornerShape(topStart = 4.dp, bottomEnd = 4.dp),
                            color = badgeColor.copy(alpha = 0.15f),
                            border = BorderStroke(0.8.dp, badgeColor)
                        ) {
                            Text(
                                text = badgeText.uppercase(),
                                color = badgeColor,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
            content()
        }
    }
}

@Composable
fun SecurityBadge(
    rating: Float,
    modifier: Modifier = Modifier
) {
    val secClass = SecurityClass.fromRating(rating)
    val color = when (secClass) {
        SecurityClass.HIGH_SECURITY -> HelionHighSecGreen
        SecurityClass.LOW_SECURITY -> HelionLowSecOrange
        SecurityClass.NULL_SECURITY -> HelionNullSecPurple
    }

    Surface(
        modifier = modifier,
        shape = CutCornerShape(topStart = 4.dp, bottomEnd = 4.dp),
        color = color.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.8f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(color, CutCornerShape(1.dp))
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = "${secClass.label} ${if (rating > 0) "+${String.format("%.1f", rating)}" else String.format("%.1f", rating)}",
                color = color,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun EnvironmentBadge(
    environment: HelionEnvironment,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, borderCol) = when (environment) {
        HelionEnvironment.PRODUCTION -> Triple(Color(0xFF0D3320), HelionHighSecGreen, HelionHighSecGreen.copy(alpha = 0.6f))
        HelionEnvironment.PRIVATE_TEST -> Triple(Color(0xFF33200D), HelionAmber, HelionAmber.copy(alpha = 0.6f))
        HelionEnvironment.DEVELOPMENT -> Triple(Color(0xFF1E1033), HelionNullSecPurple, HelionNullSecPurple.copy(alpha = 0.6f))
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderCol)
    ) {
        Text(
            text = environment.displayName,
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun StatBar(
    label: String,
    currentValue: String,
    percent: Float, // 0.0 - 1.0
    barColor: Color = HelionCyan,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = HelionTextSecondary
            )
            Text(
                text = currentValue,
                style = MaterialTheme.typography.labelSmall,
                color = HelionTextPrimary,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { percent.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = barColor,
            trackColor = HelionSurfaceHigh
        )
    }
}
