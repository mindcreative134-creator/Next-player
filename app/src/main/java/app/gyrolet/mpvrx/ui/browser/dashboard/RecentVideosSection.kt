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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.gyrolet.mpvrx.domain.media.model.Video
import app.gyrolet.mpvrx.domain.thumbnail.ThumbnailRepository
import app.gyrolet.mpvrx.ui.icons.Icon
import app.gyrolet.mpvrx.ui.icons.Icons
import app.gyrolet.mpvrx.utils.media.MediaUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import kotlin.math.roundToInt

/**
 * Recent Videos horizontal scrolling carousel with 16:9 thumbnail previews,
 * duration overlay badges, video title, resolution, and 3-dots popup menu.
 * Fully adapts to dynamic Material 3 themes and dark/light modes.
 */
@Composable
fun RecentVideosSection(
  recentVideos: List<Video>,
  onViewAllClick: () -> Unit,
  onVideoClick: (Video) -> Unit,
  onShareVideo: (Video) -> Unit,
  onDeleteVideo: (Video) -> Unit,
  modifier: Modifier = Modifier,
) {
  if (recentVideos.isEmpty()) return

  Column(
    modifier =
      modifier
        .fillMaxWidth()
        .padding(top = 10.dp, bottom = 6.dp),
  ) {
    // Header Row: "🕒 Recent Videos" + "View All >"
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
              imageVector = Icons.RoundedFilled.History,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(16.dp),
            )
          }
        }

        Text(
          text = "Recent Videos",
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

    // Horizontal Scroll Row of Video Cards
    LazyRow(
      modifier = Modifier.fillMaxWidth(),
      contentPadding = PaddingValues(horizontal = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      items(recentVideos, key = { it.id }) { video ->
        RecentVideoCard(
          video = video,
          onClick = { onVideoClick(video) },
          onShare = { onShareVideo(video) },
          onDelete = { onDeleteVideo(video) },
        )
      }
    }
  }
}

@Composable
private fun RecentVideoCard(
  video: Video,
  onClick: () -> Unit,
  onShare: () -> Unit,
  onDelete: () -> Unit,
) {
  val thumbnailRepository = koinInject<ThumbnailRepository>()
  val density = LocalDensity.current
  val widthPx = with(density) { 210.dp.roundToPx() }
  val heightPx = (widthPx / (16f / 9f)).roundToInt()

  var thumbnail by remember(video.id) {
    mutableStateOf<android.graphics.Bitmap?>(thumbnailRepository.getThumbnailFromMemory(video, widthPx, heightPx))
  }

  LaunchedEffect(video.id) {
    if (thumbnail == null) {
      withContext(Dispatchers.IO) {
        val loaded = thumbnailRepository.getThumbnail(video, widthPx, heightPx)
        withContext(Dispatchers.Main) {
          thumbnail = loaded
        }
      }
    }
  }

  var menuExpanded by remember { mutableStateOf(false) }

  Card(
    modifier =
      Modifier
        .width(210.dp)
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
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      // 16:9 Thumbnail Box
      Box(
        modifier =
          Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow),
      ) {
        if (thumbnail != null) {
          Image(
            bitmap = thumbnail!!.asImageBitmap(),
            contentDescription = video.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
          )
        } else {
          Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.RoundedFilled.Videocam,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
              modifier = Modifier.size(32.dp),
            )
          }
        }

        // Duration Badge Pill (Bottom Right of Thumbnail)
        val formattedDuration = remember(video.duration) {
          formatDuration(video.duration)
        }
        if (formattedDuration.isNotBlank() && formattedDuration != "00:00") {
          Surface(
            modifier =
              Modifier
                .align(Alignment.BottomEnd)
                .padding(6.dp),
            shape = RoundedCornerShape(6.dp),
            color = Color.Black.copy(alpha = 0.75f),
          ) {
            Text(
              text = formattedDuration,
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
              ),
              modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
            )
          }
        }
      }

      // Metadata Area: Title, Resolution/Size, and 3-dots Menu
      Row(
        modifier =
          Modifier
            .fillMaxWidth()
            .padding(start = 10.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = video.title.ifBlank { video.displayName },
            style = MaterialTheme.typography.bodyMedium.copy(
              fontSize = 13.sp,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface,
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )

          Spacer(modifier = Modifier.height(2.dp))

          // Subtitle: Resolution + Size (e.g. "1080p • 128 MB")
          val resolutionStr = remember(video.width, video.height) {
            if (video.height > 0) "${video.height}p" else "HD"
          }
          val sizeStr = remember(video.size) {
            MediaUtils.formatFileSize(video.size)
          }

          Text(
            text = "$resolutionStr • $sizeStr",
            style = MaterialTheme.typography.bodySmall.copy(
              fontSize = 10.5.sp,
              fontWeight = FontWeight.Normal,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            maxLines = 1,
          )
        }

        // 3-dots Popup Menu
        Box {
          IconButton(
            onClick = { menuExpanded = true },
            modifier = Modifier.size(28.dp),
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
              text = { Text("Play") },
              onClick = {
                menuExpanded = false
                onClick()
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
              text = { Text("Delete") },
              onClick = {
                menuExpanded = false
                onDelete()
              },
              leadingIcon = {
                Icon(
                  Icons.RoundedFilled.Delete,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp),
                )
              },
            )
          }
        }
      }
    }
  }
}

private fun formatDuration(durationMs: Long): String {
  if (durationMs <= 0) return ""
  val totalSeconds = durationMs / 1000
  val seconds = totalSeconds % 60
  val minutes = (totalSeconds / 60) % 60
  val hours = totalSeconds / 3600
  return if (hours > 0) {
    String.format(java.util.Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
  } else {
    String.format(java.util.Locale.US, "%02d:%02d", minutes, seconds)
  }
}
