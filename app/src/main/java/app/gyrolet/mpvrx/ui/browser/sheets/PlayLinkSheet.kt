/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.gyrolet.mpvrx.ui.browser.sheets

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import app.gyrolet.mpvrx.database.repository.NetworkStreamEntryRepository
import app.gyrolet.mpvrx.domain.torrent.isTorrentSource
import app.gyrolet.mpvrx.domain.torrent.normalizeTorrentSource
import app.gyrolet.mpvrx.ui.icons.Icon
import app.gyrolet.mpvrx.ui.icons.Icons
import app.gyrolet.mpvrx.utils.history.RecentlyPlayedOps
import app.gyrolet.mpvrx.utils.media.MediaUtils
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * Modern Dark Online Video Streaming Sheet with Glowing Circular Animation,
 * Clipboard Quick Paste, and Fast Hardware-Accelerated Playback.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayLinkSheet(
  isOpen: Boolean,
  onDismiss: () -> Unit,
  onPlayLink: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  if (!isOpen) return

  val context = LocalContext.current
  var linkInputUrl by remember { mutableStateOf("") }
  var isLinkInputUrlValid by remember { mutableStateOf(true) }
  val coroutineScope = rememberCoroutineScope()
  val streamEntryRepository = koinInject<NetworkStreamEntryRepository>()

  LaunchedEffect(isOpen) {
    if (isOpen) {
      linkInputUrl = ""
      isLinkInputUrlValid = true
      // Auto-check clipboard for valid URLs
      try {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val item = clipboard?.primaryClip?.getItemAt(0)?.text?.toString()?.trim()
        if (!item.isNullOrBlank()) {
          val extracted = MediaUtils.extractFirstUrl(item) ?: item
          if (MediaUtils.isURLValid(extracted)) {
            linkInputUrl = extracted
            isLinkInputUrlValid = true
          }
        }
      } catch (_: Exception) {
        // Ignore clipboard read errors
      }
    }
  }

  val handleDismiss = {
    onDismiss()
  }

  val handleConfirm = {
    val raw = linkInputUrl.trim()
    val url = MediaUtils.extractFirstUrl(raw) ?: raw
    if (url.isNotBlank() && MediaUtils.isURLValid(url)) {
      val playableSource = normalizeTorrentSource(url) ?: url
      coroutineScope.launch {
        val uri = playableSource.toUri()
        val name = uri.lastPathSegment?.substringAfterLast('/')?.ifBlank { playableSource } ?: playableSource
        if (!isTorrentSource(playableSource)) {
          try {
            RecentlyPlayedOps.addRecentlyPlayed(
              filePath = playableSource,
              fileName = name,
              launchSource = "play_link",
            )
            streamEntryRepository.saveNormalEntry(
              canonicalSourceUri = playableSource,
              fileName = name,
            )
          } catch (cancellation: CancellationException) {
            throw cancellation
          } catch (_: Exception) {
            // Playback must still open even if optional history persistence fails.
          }
        }
        onPlayLink(playableSource)
        onDismiss()
      }
    }
  }

  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

  // Infinite animations for circular glowing streaming animation
  val infiniteTransition = rememberInfiniteTransition(label = "stream_circle_anim")
  val rotationAngle by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec =
      infiniteRepeatable(
        animation = tween(durationMillis = 2400, easing = LinearEasing),
        repeatMode = RepeatMode.Restart,
      ),
    label = "stream_rotation",
  )

  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.4f,
    targetValue = 0.9f,
    animationSpec =
      infiniteRepeatable(
        animation = tween(durationMillis = 1000, easing = LinearEasing),
        repeatMode = RepeatMode.Reverse,
      ),
    label = "stream_pulse",
  )

  ModalBottomSheet(
    onDismissRequest = handleDismiss,
    sheetState = sheetState,
    containerColor = Color(0xFF13141F),
    contentColor = Color.White,
    shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
    dragHandle = {
      Box(
        modifier =
          Modifier
            .padding(vertical = 10.dp)
            .width(44.dp)
            .height(4.dp)
            .clip(CircleShape)
            .background(Color(0xFF475569)),
      )
    },
    modifier = modifier,
  ) {
    Column(
      modifier =
        Modifier
          .fillMaxWidth()
          .padding(horizontal = 24.dp, vertical = 8.dp)
          .verticalScroll(rememberScrollState())
          .navigationBarsPadding()
          .padding(bottom = 16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      // 1. Header Banner with Animated Circular Glowing Rings
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF1A1B28),
        border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          modifier = Modifier.padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          // Circular Animated Glowing Stream Icon
          Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(54.dp),
          ) {
            Canvas(
              modifier =
                Modifier
                  .size(52.dp)
                  .rotate(rotationAngle),
            ) {
              val strokeWidth = 3.dp.toPx()
              val sweepGradient =
                Brush.sweepGradient(
                  listOf(
                    Color(0xFF8B5CF6),
                    Color(0xFF38BDF8),
                    Color(0xFFEC4899),
                    Color(0xFF8B5CF6).copy(alpha = 0.1f),
                  ),
                )
              drawArc(
                brush = sweepGradient,
                startAngle = 0f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
              )
            }

            Surface(
              shape = CircleShape,
              color = Color(0xFF2E1065).copy(alpha = pulseAlpha),
              border = BorderStroke(0.8.dp, Color(0xFF8B5CF6)),
              modifier = Modifier.size(34.dp),
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.RoundedFilled.Link,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(18.dp),
                )
              }
            }
          }

          Spacer(modifier = Modifier.width(14.dp))

          Column {
            Text(
              text = "Online Stream / URL",
              style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
              ),
            )
            Text(
              text = "Ultra Fast Hardware Streaming Engine",
              style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 11.5.sp,
                color = Color(0xFFA78BFA),
              ),
            )
          }
        }
      }

      // Protocol format tags
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        listOf("HTTP/HTTPS", "HLS (m3u8)", "DASH", "RTSP", "RTMP").forEach { tag ->
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = Color(0xFF1E2030),
            border = BorderStroke(0.7.dp, Color(0xFF334155)),
          ) {
            Text(
              text = tag,
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF94A3B8),
              ),
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            )
          }
        }
      }

      // URL Input with Paste Shortcut
      Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        OutlinedTextField(
          value = linkInputUrl,
          onValueChange = { newValue ->
            linkInputUrl = newValue
            isLinkInputUrlValid = newValue.isBlank() || MediaUtils.isURLValid(newValue)
          },
          modifier = Modifier.fillMaxWidth(),
          label = { Text("Stream URL", color = Color(0xFFA78BFA)) },
          placeholder = { Text("https://example.com/live/stream.m3u8", color = Color(0xFF64748B)) },
          singleLine = true,
          shape = RoundedCornerShape(14.dp),
          colors =
            OutlinedTextFieldDefaults.colors(
              focusedBorderColor = Color(0xFF8B5CF6),
              unfocusedBorderColor = Color(0xFF334155),
              focusedContainerColor = Color(0xFF161722),
              unfocusedContainerColor = Color(0xFF161722),
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.White,
            ),
          isError = linkInputUrl.isNotBlank() && !isLinkInputUrlValid,
          trailingIcon = {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(end = 4.dp),
            ) {
              if (linkInputUrl.isNotBlank()) {
                ValidationIcon(isValid = isLinkInputUrlValid)
              } else {
                  IconButton(
                    onClick = {
                      try {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                        val text = clipboard?.primaryClip?.getItemAt(0)?.text?.toString()?.trim()
                        if (!text.isNullOrBlank()) {
                          val cleanUrl = MediaUtils.extractFirstUrl(text) ?: text
                          linkInputUrl = cleanUrl
                          isLinkInputUrlValid = MediaUtils.isURLValid(cleanUrl)
                        }
                      } catch (_: Exception) {
                        // Ignore
                      }
                    },
                  ) {
                  Icon(
                    imageVector = Icons.RoundedFilled.ContentPaste,
                    contentDescription = "Paste",
                    tint = Color(0xFFA78BFA),
                    modifier = Modifier.size(20.dp),
                  )
                }
              }
            }
          },
        )

        if (linkInputUrl.isNotBlank() && !isLinkInputUrlValid) {
          Text(
            text = "Unsupported stream protocol. Please enter a valid HTTP/HTTPS/HLS URL.",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
            fontWeight = FontWeight.Medium,
          )
        }
      }

      // Action Buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        TextButton(
          onClick = handleDismiss,
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(12.dp),
        ) {
          Text(
            text = "Cancel",
            style = MaterialTheme.typography.labelLarge.copy(
              fontWeight = FontWeight.Medium,
              color = Color(0xFF94A3B8),
            ),
          )
        }

        Button(
          onClick = handleConfirm,
          enabled = linkInputUrl.isNotBlank() && isLinkInputUrlValid,
          modifier =
            Modifier
              .weight(1.5f)
              .shadow(6.dp, shape = RoundedCornerShape(14.dp), ambientColor = Color(0xFF8B5CF6)),
          shape = RoundedCornerShape(14.dp),
          colors =
            ButtonDefaults.buttonColors(
              containerColor = Color(0xFF7C3AED),
              disabledContainerColor = Color(0xFF334155),
            ),
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.RoundedFilled.PlayArrow,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Play Stream",
              style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White,
              ),
            )
          }
        }
      }
    }
  }
}

@Composable
private fun ValidationIcon(isValid: Boolean) {
  if (isValid) {
    Icon(
      imageVector = Icons.RoundedFilled.Check,
      contentDescription = "Valid URL",
      tint = Color(0xFF10B981),
      modifier = Modifier.size(20.dp),
    )
  } else {
    Icon(
      imageVector = Icons.RoundedFilled.Close,
      contentDescription = "Invalid URL",
      tint = MaterialTheme.colorScheme.error,
      modifier = Modifier.size(20.dp),
    )
  }
}
