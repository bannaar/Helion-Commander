package com.example.helion.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.helion.core.model.CommanderProfile
import com.example.helion.core.model.HelionEnvironment
import com.example.ui.theme.HelionAmber
import com.example.ui.theme.HelionBorder
import com.example.ui.theme.HelionCyan
import com.example.ui.theme.HelionDangerRed
import com.example.ui.theme.HelionDeepGraphite
import com.example.ui.theme.HelionHighSecGreen
import com.example.ui.theme.HelionSurface
import com.example.ui.theme.HelionSurfaceHigh
import com.example.ui.theme.HelionTextMuted
import com.example.ui.theme.HelionTextPrimary
import com.example.ui.theme.HelionTextSecondary
import java.text.NumberFormat
import java.util.Locale

@Composable
fun HelionHeader(
    commander: CommanderProfile?,
    environment: HelionEnvironment,
    isOffline: Boolean,
    modifier: Modifier = Modifier,
    onLogoClick: () -> Unit = {},
    onCommanderClick: () -> Unit = {},
    onEnvironmentClick: () -> Unit = {}
) {
    val numberFormat = NumberFormat.getNumberInstance(Locale.US)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(HelionDeepGraphite)
            .border(width = 0.8.dp, color = HelionBorder)
            .testTag("helion_header")
    ) {
        if (isOffline) {
            Surface(
                color = HelionDangerRed.copy(alpha = 0.9f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Offline Warning",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "OFFLINE SIMULATION ACTIVE — Authoritative server actions (Buy/Sell/Fitting) disabled.",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Branding (Clickable to return Home)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { onLogoClick() }
                    .testTag("header_logo_button")
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(HelionSurfaceHigh, RoundedCornerShape(4.dp))
                        .border(1.dp, HelionCyan, RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "H",
                        color = HelionCyan,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "HELION",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        color = HelionTextPrimary
                    )
                    Text(
                        text = "COMMANDER COMPANION",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 8.sp,
                        letterSpacing = 1.sp,
                        color = HelionCyan
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Environment Badge (always visible across all screens)
                EnvironmentBadge(
                    environment = environment,
                    modifier = Modifier
                        .clickable { onEnvironmentClick() }
                        .testTag("header_environment_badge")
                )

                if (commander != null) {
                    Spacer(modifier = Modifier.width(8.dp))

                    // GSC
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = HelionSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, HelionBorder),
                        modifier = Modifier
                            .clickable { onCommanderClick() }
                            .testTag("header_credits_badge")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "GSC",
                                color = HelionAmber,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${numberFormat.format(commander.credits)}",
                                color = HelionTextPrimary,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Location Pill
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = HelionSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, HelionBorder),
                        modifier = Modifier
                            .clickable { onEnvironmentClick() }
                            .testTag("header_location_badge")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Circle,
                                contentDescription = null,
                                tint = HelionHighSecGreen,
                                modifier = Modifier.size(6.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = commander.currentSystemName,
                                color = HelionTextPrimary,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
