/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.gyrolet.mpvrx.ui.browser.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.gyrolet.mpvrx.domain.media.model.VideoFolder
import app.gyrolet.mpvrx.ui.icons.Icon
import app.gyrolet.mpvrx.ui.icons.Icons
import app.gyrolet.mpvrx.utils.media.MediaUtils

/**
 * Recent Folders section replacing default dummy category cards with real device folders
 * accessed or modified by the user. Connects directly to Material 3 theme colors.
 */
@Composable
fun RecentFoldersSection(
  recentFolders: List<VideoFolder>,
  onViewAllClick: () -> Unit,
  onFolderClick: (VideoFolder) -> Unit,
  onPlayFolder: (VideoFolder) -> Unit,
  onShareFolder: (VideoFolder) -> Unit,
  onPinFolder: (VideoFolder) -> Unit,
  modifier: Modifier = Modifier,
) {
  if (recentFolders.isEmpty()) return

  Column(
    modifier =
      modifier
        .fillMaxWidth()
        .padding(top = 10.dp, bottom = 6.dp),
  ) {
    // Header Row: "📁 Recent Folders" + "View All >"
    Row(
      modifier =
        Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Surface(
          modifier = Modifier.size(28.dp),
          shape = CircleShape,
          color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.RoundedFilled.Folder,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(16.dp),
            )
          }
        }

        Text(
          text = "Recent Folders",
          style = MaterialTheme.typography.titleMedium.copy(
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          ),
        )
      }

      Text(
        text = "View All >",
        style = MaterialTheme.typography.labelMedium.copy(
          fontSize = 12.5.sp,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.primary,
        ),
        modifier =
          Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onViewAllClick)
            .padding(horizontal = 6.dp, vertical = 4.dp),
      )
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Horizontal Scrolling List of Recent Folder Cards
    LazyRow(
      modifier = Modifier.fillMaxWidth(),
      contentPadding = PaddingValues(horizontal = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      items(recentFolders, key = { it.bucketId }) { folder ->
        RecentFolderCard(
          folder = folder,
          isFirst = folder == recentFolders.firstOrNull(),
          onClick = { onFolderClick(folder) },
          onPlay = { onPlayFolder(folder) },
          onShare = { onShareFolder(folder) },
          onPin = { onPinFolder(folder) },
        )
      }
    }
  }
}

@Composable
private fun RecentFolderCard(
  folder: VideoFolder,
  isFirst: Boolean,
  onClick: () -> Unit,
  onPlay: () -> Unit,
  onShare: () -> Unit,
  onPin: () -> Unit,
) {
  var menuExpanded by remember { mutableStateOf(false) }

  Card(
    modifier =
      Modifier
        .width(170.dp)
        .clip(RoundedCornerShape(16.dp))
        .clickable(
          interactionSource = remember { MutableInteractionSource() },
          indication = null,
          onClick = onClick,
        ),
    shape = RoundedCornerShape(16.dp),
    colors =
      CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
      ),
    border =
      BorderStroke(
        1.dp,
        if (isFirst) {
          MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        } else {
          MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        },
      ),
  ) {
    Column(
      modifier =
        Modifier
          .fillMaxWidth()
          .padding(12.dp),
    ) {
      // Top Row: Folder Icon + (Active Badge or 3-dots Menu)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Surface(
          modifier = Modifier.size(36.dp),
          shape = RoundedCornerShape(10.dp),
          color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.RoundedFilled.Folder,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(20.dp),
            )
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          if (isFirst) {
            Surface(
              shape = RoundedCornerShape(4.dp),
              color = MaterialTheme.colorScheme.primaryContainer,
            ) {
              Text(
                text = "ACTIVE",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontSize = 8.5.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
              )
            }
            Spacer(modifier = Modifier.width(2.dp))
          }

          Box {
            IconButton(
              onClick = { menuExpanded = true },
              modifier = Modifier.size(26.dp),
            ) {
              Icon(
                imageVector = Icons.RoundedFilled.MoreVert,
                contentDescription = "Options",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp),
              )
            }

            DropdownMenu(
              expanded = menuExpanded,
              onDismissRequest = { menuExpanded = false },
            ) {
              DropdownMenuItem(
                text = { Text("Play All") },
                onClick = {
                  menuExpanded = false
                  onPlay()
                },
                leadingIcon = {
                  Icon(
                    Icons.RoundedFilled.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                  )
                },
              )
              DropdownMenuItem(
                text = { Text("Share") },
                onClick = {
                  menuExpanded = false
                  onShare()
                },
                leadingIcon = {
                  Icon(
                    Icons.RoundedFilled.Share,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                  )
                },
              )
              DropdownMenuItem(
                text = { Text("Pin Folder") },
                onClick = {
                  menuExpanded = false
                  onPin()
                },
                leadingIcon = {
                  Icon(
                    Icons.RoundedFilled.PushPin,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                  )
                },
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Folder Name
      Text(
        text = folder.name,
        style = MaterialTheme.typography.titleSmall.copy(
          fontSize = 13.5.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
        ),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )

      Spacer(modifier = Modifier.height(3.dp))

      // Stats Subtitle ("12 Videos • 2.4 GB")
      val formattedSize = remember(folder.totalSize) {
        MediaUtils.formatFileSize(folder.totalSize)
      }
      val videoCountText = remember(folder.videoCount) {
        "${folder.videoCount} " + if (folder.videoCount == 1) "Video" else "Videos"
      }

      Text(
        text = "$videoCountText • $formattedSize",
        style = MaterialTheme.typography.bodySmall.copy(
          fontSize = 10.5.sp,
          fontWeight = FontWeight.Normal,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}
