/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.gyrolet.mpvrx.ui.browser.dashboard

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.gyrolet.mpvrx.R
import app.gyrolet.mpvrx.domain.media.model.Video
import app.gyrolet.mpvrx.domain.media.model.VideoFolder
import app.gyrolet.mpvrx.preferences.AppearancePreferences
import app.gyrolet.mpvrx.preferences.BrowserPreferences
import app.gyrolet.mpvrx.preferences.FoldersPreferences
import app.gyrolet.mpvrx.preferences.preference.collectAsState
import app.gyrolet.mpvrx.presentation.components.pullrefresh.PullRefreshBox
import app.gyrolet.mpvrx.repository.MediaFileRepository
import app.gyrolet.mpvrx.ui.browser.MainScreen
import app.gyrolet.mpvrx.ui.browser.dialogs.FolderSortDialog
import app.gyrolet.mpvrx.ui.browser.sheets.PlayLinkSheet
import app.gyrolet.mpvrx.ui.icons.Icon
import app.gyrolet.mpvrx.ui.icons.Icons
import app.gyrolet.mpvrx.ui.preferences.AboutScreen
import app.gyrolet.mpvrx.ui.preferences.PreferencesScreen
import app.gyrolet.mpvrx.ui.securefolder.SecureFolderGateScreen
import app.gyrolet.mpvrx.ui.utils.LocalBackStack
import app.gyrolet.mpvrx.utils.history.RecentlyPlayedOps
import app.gyrolet.mpvrx.utils.media.MediaUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import java.io.File

/**
 * Modern Dynamic Video Player Dashboard Screen.
 * Fully adapts to any Theme (Dark, Light, System, Cotton Candy, Lavender, Monet Dynamic, AMOLED Pure Black).
 * Features:
 * - Dynamic Theme Top Bar & Hero Play Banner
 * - Real Device Recent Folders section
 * - Real Recent Videos Carousel
 * - All Folders 2-Column Grid
 * - Navigation Drawer Menu Sheet & Working URL Stream Dialog
 * - Configurable Quick Play Speed Dial FAB
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ModernHomeDashboard(
  folders: List<VideoFolder>,
  isRefreshing: MutableState<Boolean>,
  onRefresh: suspend () -> Unit,
  onFolderClick: (VideoFolder) -> Unit,
  onFolderLongClick: (VideoFolder) -> Unit,
  onTogglePin: (VideoFolder) -> Unit,
  onSearchClick: () -> Unit,
  navigationBarHeight: Dp,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val backstack = LocalBackStack.current
  val coroutineScope = rememberCoroutineScope()
  val listState = rememberLazyListState()

  val browserPreferences = koinInject<BrowserPreferences>()
  val foldersPreferences = koinInject<FoldersPreferences>()
  val appearancePreferences = koinInject<AppearancePreferences>()

  val showQuickPlayFab by appearancePreferences.showQuickPlayFab.collectAsState()
  val quickPlayFabDirect by appearancePreferences.quickPlayFabDirect.collectAsState()
  val folderSortType by browserPreferences.folderSortType.collectAsState()
  val folderSortOrder by browserPreferences.folderSortOrder.collectAsState()

  var recentVideos by remember { mutableStateOf<List<Video>>(emptyList()) }
  var recentFolders by remember { mutableStateOf<List<VideoFolder>>(emptyList()) }

  var isMenuSheetOpen by remember { mutableStateOf(false) }
  val menuSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  var isSortDialogOpen by remember { mutableStateOf(false) }
  var showLinkDialog by remember { mutableStateOf(false) }
  val isFabExpanded = remember { mutableStateOf(false) }

  val filePicker =
    rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
      uri?.let {
        MediaUtils.playFile(it.toString(), context, "file_picker")
      }
    }

  val onTabSelected = app.gyrolet.mpvrx.ui.browser.LocalOnTabSelected.current

  // Load Recent Videos and Recent Folders reactively from real device storage and playback history
  LaunchedEffect(folders) {
    withContext(Dispatchers.IO) {
      try {
        val allVids =
          folders.flatMap { folder ->
            try {
              MediaFileRepository.getVideosForBuckets(context, setOf(folder.bucketId))
            } catch (e: Exception) {
              emptyList()
            }
          }

        // Real playback history from RecentlyPlayedOps
        val recents = RecentlyPlayedOps.getRecentlyPlayed(limit = 1000)
        val recentPaths = recents.map { it.filePath }

        // 1. Build Recent Folders list from playback history + most recently modified folders
        val matchedFolders = mutableListOf<VideoFolder>()
        val seenBucketIds = mutableSetOf<String>()

        for (filePath in recentPaths) {
          val folderMatch =
            folders.firstOrNull { folder ->
              filePath.startsWith(folder.path) ||
                (folder.path.isNotBlank() && File(filePath).parentFile?.absolutePath == folder.path)
            }
          if (folderMatch != null && seenBucketIds.add(folderMatch.bucketId)) {
            matchedFolders.add(folderMatch)
          }
        }

        // Fill in with most recently modified folders if history has few items
        val remainingFolders =
          folders
            .filter { it.bucketId !in seenBucketIds }
            .sortedByDescending { it.lastModified }

        matchedFolders.addAll(remainingFolders)
        recentFolders = matchedFolders

        // 2. Build Recent Videos list
        val recentPathSet = recentPaths.take(20).toSet()
        val foundRecentVideos = allVids.filter { it.path in recentPathSet }
        recentVideos =
          if (foundRecentVideos.isNotEmpty()) {
            foundRecentVideos
          } else {
            allVids.sortedByDescending { it.dateModified }.take(10)
          }
      } catch (e: Exception) {
        android.util.Log.e("ModernHomeDashboard", "Error calculating dashboard data", e)
      }
    }
  }

  // Quick Play handler for "▶ Play Now" CTA button and Resume shortcut
  val handlePlayNow = {
    coroutineScope.launch {
      val lastPlayed = RecentlyPlayedOps.getLastPlayedEntity()
      if (lastPlayed != null) {
        MediaUtils.playFile(
          source = lastPlayed.filePath,
          context = context,
          launchSource = "dashboard_hero_play_now",
          title =
            lastPlayed.videoTitle?.takeIf { it.isNotBlank() }
              ?: lastPlayed.fileName.takeIf { it.isNotBlank() },
        )
      } else if (recentVideos.isNotEmpty()) {
        MediaUtils.playFile(recentVideos.first(), context)
      } else if (folders.isNotEmpty()) {
        val firstFolder = folders.first()
        val vids = MediaFileRepository.getVideosForBuckets(context, setOf(firstFolder.bucketId))
        if (vids.isNotEmpty()) {
          MediaUtils.playFile(vids.first(), context)
        }
      } else {
        Toast.makeText(context, "No media files found to play", Toast.LENGTH_SHORT).show()
      }
    }
  }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    containerColor = MaterialTheme.colorScheme.background,
    topBar = {
      DashboardTopBar(
        title = "NextPlayer",
        subtitle = "All Format Video Player",
        onMenuClick = {
          isMenuSheetOpen = true
        },
        onSearchClick = onSearchClick,
        onSortClick = {
          isSortDialogOpen = true
        },
        onCastClick = {
          Toast.makeText(context, "Scanning for Cast devices...", Toast.LENGTH_SHORT).show()
        },
        onSettingsClick = {
          backstack.add(PreferencesScreen)
        },
        onTitleLongPress = {
          backstack.add(SecureFolderGateScreen)
        },
        onTitleDoubleTap = {
          backstack.add(SecureFolderGateScreen)
        },
      )
    },
    floatingActionButton = {
      if (showQuickPlayFab) {
        if (quickPlayFabDirect) {
          FloatingActionButton(
            onClick = { handlePlayNow() },
            modifier = Modifier.padding(bottom = (navigationBarHeight - 16.dp).coerceAtLeast(0.dp)),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
          ) {
            Icon(
              imageVector = Icons.RoundedFilled.PlayArrow,
              contentDescription = "Play",
            )
          }
        } else {
          FloatingActionButtonMenu(
            modifier = Modifier.padding(bottom = (navigationBarHeight - 16.dp).coerceAtLeast(0.dp)),
            expanded = isFabExpanded.value,
            button = {
              TooltipBox(
                positionProvider =
                  TooltipDefaults.rememberTooltipPositionProvider(
                    if (isFabExpanded.value) TooltipAnchorPosition.Start else TooltipAnchorPosition.Above,
                  ),
                tooltip = {
                  PlainTooltip {
                    Text(stringResource(R.string.ui_toggle_menu))
                  }
                },
                state = rememberTooltipState(),
              ) {
                ToggleFloatingActionButton(
                  checked = isFabExpanded.value,
                  onCheckedChange = {
                    isFabExpanded.value = !isFabExpanded.value
                  },
                ) {
                  val imageVector by remember {
                    derivedStateOf {
                      if (checkedProgress > 0.5f) Icons.RoundedFilled.Close else Icons.RoundedFilled.PlayArrow
                    }
                  }
                  Icon(
                    imageVector = imageVector,
                    contentDescription = null,
                    modifier = Modifier.animateIcon({ checkedProgress }),
                  )
                }
              }
            },
          ) {
            FloatingActionButtonMenuItem(
              onClick = {
                isFabExpanded.value = false
                filePicker.launch(arrayOf("video/*"))
              },
              icon = { Icon(Icons.RoundedFilled.FileOpen, contentDescription = null) },
              text = { Text(stringResource(R.string.ui_open_file)) },
            )

            FloatingActionButtonMenuItem(
              onClick = {
                isFabExpanded.value = false
                handlePlayNow()
              },
              icon = { Icon(Icons.RoundedFilled.History, contentDescription = null) },
              text = { Text("Resume Last Video") },
            )

            FloatingActionButtonMenuItem(
              onClick = {
                isFabExpanded.value = false
                showLinkDialog = true
              },
              icon = { Icon(Icons.RoundedFilled.Link, contentDescription = null) },
              text = { Text(stringResource(R.string.ui_open_link)) },
            )
          }
        }
      }
    },
  ) { paddingValues ->
    PullRefreshBox(
      isRefreshing = isRefreshing,
      onRefresh = onRefresh,
      listState = listState,
      modifier =
        Modifier
          .fillMaxSize()
          .padding(paddingValues),
    ) {
      LazyColumn(
        state = listState,
        modifier =
          Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
      ) {
        // 1. Hero Feature Banner (Themed Gradient + Play Now CTA + 3D Format Graphic)
        item(key = "hero_banner") {
          HeroFeatureBanner(
            onPlayNowClick = { handlePlayNow() },
          )
        }

        // 2. Real Recent Folders Section (Horizontal Carousel with details & actions)
        if (recentFolders.isNotEmpty()) {
          item(key = "recent_folders") {
            RecentFoldersSection(
              recentFolders = recentFolders,
              onViewAllClick = {
                browserPreferences.folderViewMode.set(app.gyrolet.mpvrx.preferences.FolderViewMode.FileManager)
              },
              onFolderClick = onFolderClick,
              onPlayFolder = { folder ->
                coroutineScope.launch {
                  val vids = MediaFileRepository.getVideosForBuckets(context, setOf(folder.bucketId))
                  if (vids.isNotEmpty()) {
                    if (vids.size == 1) {
                      MediaUtils.playFile(vids.first(), context)
                    } else {
                      val intent = Intent(Intent.ACTION_VIEW, vids.first().uri)
                      intent.setClass(context, app.gyrolet.mpvrx.ui.player.PlayerActivity::class.java)
                      intent.putExtra("internal_launch", true)
                      intent.putParcelableArrayListExtra("playlist", ArrayList(vids.map { it.uri }))
                      intent.putExtra("playlist_index", 0)
                      intent.putExtra("launch_source", "playlist")
                      context.startActivity(intent)
                    }
                  }
                }
              },
              onShareFolder = { folder ->
                coroutineScope.launch {
                  val vids = MediaFileRepository.getVideosForBuckets(context, setOf(folder.bucketId))
                  if (vids.isNotEmpty()) {
                    MediaUtils.shareVideos(context, vids)
                  }
                }
              },
              onPinFolder = onTogglePin,
            )
          }
        }

        // 3. Recent Videos Carousel (16:9 thumbnails + metadata + options menu)
        if (recentVideos.isNotEmpty()) {
          item(key = "recent_videos") {
            RecentVideosSection(
              recentVideos = recentVideos,
              onViewAllClick = {
                onTabSelected(MainScreen.MainTab.RECENTS)
              },
              onVideoClick = { video ->
                app.gyrolet.mpvrx.ads.AdManager.loadAndShowInterstitialAd(context as Activity) {
                  MediaUtils.playFile(video, context)
                }
              },
              onShareVideo = { video ->
                MediaUtils.shareVideos(context, listOf(video))
              },
              onDeleteVideo = { video ->
                coroutineScope.launch {
                  val ok =
                    app.gyrolet.mpvrx.utils.permission.PermissionUtils.StorageOps.deleteVideos(
                      context.applicationContext as android.app.Application,
                      listOf(video),
                    )
                  if (ok.first > 0) {
                    Toast.makeText(context, "Video deleted", Toast.LENGTH_SHORT).show()
                    onRefresh()
                  }
                }
              },
            )
          }
        }

        // 4. All Folders 2-Column Grid Section
        item(key = "folders_grid") {
          FolderGridSection(
            folders = folders,
            onViewAllClick = {
              browserPreferences.folderViewMode.set(app.gyrolet.mpvrx.preferences.FolderViewMode.FileManager)
            },
            onFolderClick = onFolderClick,
            onPlayFolder = { folder ->
              coroutineScope.launch {
                val vids = MediaFileRepository.getVideosForBuckets(context, setOf(folder.bucketId))
                if (vids.isNotEmpty()) {
                  if (vids.size == 1) {
                    MediaUtils.playFile(vids.first(), context)
                  } else {
                    val intent = Intent(Intent.ACTION_VIEW, vids.first().uri)
                    intent.setClass(context, app.gyrolet.mpvrx.ui.player.PlayerActivity::class.java)
                    intent.putExtra("internal_launch", true)
                    intent.putParcelableArrayListExtra("playlist", ArrayList(vids.map { it.uri }))
                    intent.putExtra("playlist_index", 0)
                    intent.putExtra("launch_source", "playlist")
                    context.startActivity(intent)
                  }
                }
              }
            },
            onShareFolder = { folder ->
              coroutineScope.launch {
                val vids = MediaFileRepository.getVideosForBuckets(context, setOf(folder.bucketId))
                if (vids.isNotEmpty()) {
                  MediaUtils.shareVideos(context, vids)
                }
              }
            },
            onPinFolder = onTogglePin,
            onDeleteFolder = { folder ->
              coroutineScope.launch {
                onRefresh()
              }
            },
          )
        }

        // Bottom clearance for navigation bar + mini player
        item(key = "bottom_clearance") {
          Spacer(modifier = Modifier.height(navigationBarHeight + 32.dp))
        }
      }
    }
  }

  // Dashboard Navigation Drawer / Menu Sheet
  if (isMenuSheetOpen) {
    DashboardMenuSheet(
      sheetState = menuSheetState,
      onDismiss = { isMenuSheetOpen = false },
      onOpenUrlStream = {
        showLinkDialog = true
      },
      onResumeLastVideo = {
        handlePlayNow()
      },
      onOpenSecureFolder = {
        backstack.add(SecureFolderGateScreen)
      },
      onOpenSettings = {
        backstack.add(PreferencesScreen)
      },
      onOpenAbout = {
        backstack.add(AboutScreen)
      },
    )
  }

  // Sort (A-Z) & View Options Dialog
  FolderSortDialog(
    isOpen = isSortDialogOpen,
    onDismiss = { isSortDialogOpen = false },
    sortType = folderSortType,
    sortOrder = folderSortOrder,
    onSortTypeChange = { browserPreferences.folderSortType.set(it) },
    onSortOrderChange = { browserPreferences.folderSortOrder.set(it) },
  )

  // Network Online URL Stream Dialog
  PlayLinkSheet(
    isOpen = showLinkDialog,
    onDismiss = { showLinkDialog = false },
    onPlayLink = { url -> MediaUtils.playFile(url, context, "play_link") },
  )
}
