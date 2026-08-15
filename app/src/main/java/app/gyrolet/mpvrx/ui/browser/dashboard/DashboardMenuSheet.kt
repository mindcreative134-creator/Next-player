/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.gyrolet.mpvrx.ui.browser.dashboard

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.gyrolet.mpvrx.BuildConfig
import app.gyrolet.mpvrx.ui.icons.AppIcon
import app.gyrolet.mpvrx.ui.icons.Icon
import app.gyrolet.mpvrx.ui.icons.Icons

/**
 * Modern Dashboard Menu Sheet displaying App Details, Check for Updates,
 * Quick Action Shortcuts (URL stream, Resume video), Rating, Share, About, and Settings.
 * Fully adapts to dynamic Material 3 themes and dark/light modes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardMenuSheet(
  sheetState: SheetState,
  onDismiss: () -> Unit,
  onOpenUrlStream: () -> Unit,
  onResumeLastVideo: () -> Unit,
  onOpenSecureFolder: () -> Unit,
  onOpenSettings: () -> Unit,
  onOpenAbout: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val scrollState = rememberScrollState()

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    contentColor = MaterialTheme.colorScheme.onSurface,
    shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
    dragHandle = {
      Box(
        modifier =
          Modifier
            .padding(vertical = 10.dp)
            .width(44.dp)
            .height(4.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)),
      )
    },
    modifier = modifier,
  ) {
    Column(
      modifier =
        Modifier
          .fillMaxWidth()
          .verticalScroll(scrollState)
          .padding(horizontal = 20.dp)
          .navigationBarsPadding()
          .padding(bottom = 24.dp),
    ) {
      // 1. App Header & Branding Card
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
      ) {
        Row(
          modifier = Modifier.padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          // Glowing Themed App Icon
          Surface(
            modifier = Modifier.size(54.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
          ) {
            Box(
              modifier = Modifier.fillMaxWidth(),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.RoundedFilled.Videocam,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp),
              )
            }
          }

          Spacer(modifier = Modifier.width(14.dp))

          Column(modifier = Modifier.weight(1f)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Text(
                text = "NextPlayer",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontSize = 18.sp,
                  fontWeight = FontWeight.Black,
                  color = MaterialTheme.colorScheme.onSurface,
                ),
              )
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
              ) {
                Text(
                  text = "v${BuildConfig.VERSION_NAME}",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                  ),
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                )
              }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
              text = "All Format Ultra HD Video Player",
              style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 11.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              ),
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 2. "✨ Check for Updates" Action Button
      Surface(
        modifier =
          Modifier
            .fillMaxWidth()
            .shadow(
              elevation = 6.dp,
              shape = RoundedCornerShape(16.dp),
              ambientColor = MaterialTheme.colorScheme.primary,
              spotColor = MaterialTheme.colorScheme.primary,
            ).clip(RoundedCornerShape(16.dp))
            .clickable {
              openPlayStore(context)
            },
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
      ) {
        Box(
          modifier =
            Modifier
              .fillMaxWidth()
              .background(
                Brush.horizontalGradient(
                  listOf(
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.secondary.takeIf { it != MaterialTheme.colorScheme.primary }
                      ?: MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                  ),
                ),
              ).padding(horizontal = 16.dp, vertical = 13.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.RoundedFilled.Refresh,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(22.dp),
              )
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = "Check for Updates",
                  style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 14.sp,
                  ),
                )
                Text(
                  text = "v${BuildConfig.VERSION_NAME} • Latest Version Installed",
                  style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 10.5.sp,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                  ),
                )
              }
            }

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
            ) {
              Text(
                text = "CHECK",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontSize = 10.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = MaterialTheme.colorScheme.onPrimary,
                ),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Section Title: Quick Actions
      Text(
        text = "QUICK ACTIONS",
        style = MaterialTheme.typography.labelSmall.copy(
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary,
          letterSpacing = 1.sp,
        ),
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
      )

      // Shortcut 1: Network / URL Online Streaming
      MenuItemRow(
        icon = Icons.RoundedFilled.Link,
        title = "Online Stream / URL",
        subtitle = "Play direct network or M3U/HLS stream",
        accentColor = MaterialTheme.colorScheme.primary,
        onClick = {
          onDismiss()
          onOpenUrlStream()
        },
      )

      // Shortcut 2: Resume Last Played Video
      MenuItemRow(
        icon = Icons.RoundedFilled.History,
        title = "Resume Last Video",
        subtitle = "Jump back to previous playback session",
        accentColor = MaterialTheme.colorScheme.secondary,
        onClick = {
          onDismiss()
          onResumeLastVideo()
        },
      )

      // Shortcut 3: Secure Folder
      MenuItemRow(
        icon = Icons.RoundedFilled.Lock,
        title = "Secure Folder",
        subtitle = "PIN protected private video safe",
        accentColor = MaterialTheme.colorScheme.tertiary,
        onClick = {
          onDismiss()
          onOpenSecureFolder()
        },
      )

      Spacer(modifier = Modifier.height(14.dp))
      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.8.dp)
      Spacer(modifier = Modifier.height(14.dp))

      // Section Title: Preferences & Support
      Text(
        text = "PREFERENCES & SUPPORT",
        style = MaterialTheme.typography.labelSmall.copy(
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary,
          letterSpacing = 1.sp,
        ),
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
      )

      // Settings
      MenuItemRow(
        icon = Icons.RoundedFilled.Settings,
        title = "Settings & Player Controls",
        subtitle = "Hardware decoder, gestures, subtitles, theme",
        accentColor = MaterialTheme.colorScheme.primary,
        onClick = {
          onDismiss()
          onOpenSettings()
        },
      )

      // Rate App on Google Play
      MenuItemRow(
        icon = Icons.RoundedFilled.Star,
        title = "Rate on Google Play",
        subtitle = "Support development with a 5-star rating",
        accentColor = MaterialTheme.colorScheme.secondary,
        onClick = {
          onDismiss()
          openPlayStore(context)
        },
      )

      // Share App
      MenuItemRow(
        icon = Icons.RoundedFilled.Share,
        title = "Share App",
        subtitle = "Share NextPlayer with friends & family",
        accentColor = MaterialTheme.colorScheme.tertiary,
        onClick = {
          onDismiss()
          shareApp(context)
        },
      )

      // About
      MenuItemRow(
        icon = Icons.RoundedFilled.Info,
        title = "About NextPlayer",
        subtitle = "Version, licenses, developer info",
        accentColor = MaterialTheme.colorScheme.primary,
        onClick = {
          onDismiss()
          onOpenAbout()
        },
      )
    }
  }
}

@Composable
private fun MenuItemRow(
  icon: AppIcon,
  title: String,
  subtitle: String,
  accentColor: Color,
  onClick: () -> Unit,
) {
  Row(
    modifier =
      Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(14.dp))
        .clickable(
          interactionSource = remember { MutableInteractionSource() },
          indication = null,
          onClick = onClick,
        ).padding(horizontal = 8.dp, vertical = 9.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Surface(
      modifier = Modifier.size(40.dp),
      shape = RoundedCornerShape(12.dp),
      color = accentColor.copy(alpha = 0.15f),
      border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f)),
    ) {
      Box(contentAlignment = Alignment.Center) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = accentColor,
          modifier = Modifier.size(20.dp),
        )
      }
    }

    Spacer(modifier = Modifier.width(14.dp))

    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        style = MaterialTheme.typography.bodyMedium.copy(
          fontSize = 14.sp,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface,
        ),
      )
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall.copy(
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
      )
    }

    Icon(
      imageVector = Icons.RoundedFilled.ChevronRight,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
      modifier = Modifier.size(18.dp),
    )
  }
}

private fun openPlayStore(context: Context) {
  val packageName = context.packageName
  try {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
  } catch (e: Exception) {
    val webIntent =
      Intent(
        Intent.ACTION_VIEW,
        Uri.parse("https://play.google.com/store/apps/details?id=$packageName"),
      )
    webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(webIntent)
  }
}

private fun shareApp(context: Context) {
  val packageName = context.packageName
  val shareIntent =
    Intent(Intent.ACTION_SEND).apply {
      type = "text/plain"
      putExtra(
        Intent.EXTRA_TEXT,
        "Check out NextPlayer - Powerful Ultra HD Video Player for Android:\nhttps://play.google.com/store/apps/details?id=$packageName",
      )
    }
  context.startActivity(Intent.createChooser(shareIntent, "Share NextPlayer via"))
}
