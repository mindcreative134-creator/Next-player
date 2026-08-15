/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.gyrolet.mpvrx.utils.media

import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLDecoder

object HttpUtils {
  private const val TAG = "HttpUtils"
  private const val CONNECTION_TIMEOUT = 5000
  private const val READ_TIMEOUT = 5000

  const val DEFAULT_USER_AGENT =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"

  private val directMediaExtensions =
    setOf(
      "mp4",
      "m4v",
      "mkv",
      "webm",
      "avi",
      "mov",
      "wmv",
      "flv",
      "ts",
      "m2ts",
      "mp3",
      "m4a",
      "aac",
      "flac",
      "wav",
      "ogg",
      "opus",
      "m3u",
      "m3u8",
      "mpd",
    )
  private val genericRouteTitles =
    setOf(
      "watch",
      "stream",
      "video",
      "play",
      "embed",
      "download",
      "media",
      "live",
      "reel",
      "reels",
      "short",
      "shorts",
      "player",
    )

  suspend fun extractFilenameFromUrl(url: String): String? =
    withContext(Dispatchers.IO) {
      try {
        val uri = Uri.parse(url)
        val filenameFromHeaders = getFilenameFromHttpHeaders(url)
        if (filenameFromHeaders != null) {
          Log.d(TAG, "Extracted filename from headers: $filenameFromHeaders")
          return@withContext filenameFromHeaders
        }
        val filenameFromUrl = extractFilenameFromUrlPath(uri)
        Log.d(TAG, "Extracted filename from URL: $filenameFromUrl")
        return@withContext filenameFromUrl
      } catch (e: Exception) {
        Log.e(TAG, "Error extracting filename: ${e.message}")
        null
      }
    }

  private fun getFilenameFromHttpHeaders(url: String): String? {
    var connection: HttpURLConnection? = null
    try {
      connection = URL(url).openConnection() as HttpURLConnection
      connection.requestMethod = "HEAD"
      connection.connectTimeout = CONNECTION_TIMEOUT
      connection.readTimeout = READ_TIMEOUT
      connection.setRequestProperty("User-Agent", "mpvRx/1.0")
      connection.instanceFollowRedirects = true
      connection.connect()

      val responseCode = connection.responseCode
      if (responseCode != HttpURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_PARTIAL) {
        Log.w(TAG, "HTTP response: $responseCode")
      }

      connection.getHeaderField("Content-Disposition")?.let {
        parseContentDisposition(it)?.let { filename -> return filename }
      }

      connection.getHeaderField("Content-Location")?.let {
        return extractFilenameFromUrlPath(Uri.parse(it))
      }

      return null
    } catch (e: IOException) {
      Log.e(TAG, "IO error: ${e.message}")
      return null
    } catch (e: Exception) {
      Log.e(TAG, "Error: ${e.message}")
      return null
    } finally {
      connection?.disconnect()
    }
  }

  private fun parseContentDisposition(contentDisposition: String): String? {
    try {
      val filenameStarPattern = Regex("""filename\*=(?:UTF-8|utf-8)?''?"?([^";\r\n]+)"?""", RegexOption.IGNORE_CASE)
      filenameStarPattern.find(contentDisposition)?.let { match ->
        val encodedFilename = match.groupValues[1]
        return try {
          URLDecoder.decode(encodedFilename, "UTF-8")
        } catch (e: Exception) {
          encodedFilename
        }
      }

      val filenamePattern = Regex("""filename="?([^";\r\n]+)"?""", RegexOption.IGNORE_CASE)
      filenamePattern.find(contentDisposition)?.let { match ->
        return match.groupValues[1].trim()
      }

      return null
    } catch (e: Exception) {
      Log.e(TAG, "Error parsing Content-Disposition: ${e.message}")
      return null
    }
  }

  private fun extractFilenameFromUrlPath(uri: Uri): String {
    val path = uri.path ?: return uri.host ?: "Network Stream"
    val lastSegment = path.substringAfterLast("/")

    if (lastSegment.isNotBlank()) {
      return try {
        URLDecoder
          .decode(lastSegment, "UTF-8")
          .substringBefore("?")
          .substringBefore("#")
          .takeIf { it.isNotBlank() } ?: uri.host ?: "Network Stream"
      } catch (e: Exception) {
        lastSegment.substringBefore("?").substringBefore("#")
      }
    }

    return uri.host ?: "Network Stream"
  }

  fun isNetworkStream(uri: Uri?): Boolean {
    if (uri == null) return false
    val scheme = uri.scheme?.lowercase()
    return scheme in
      listOf("http", "https", "rtmp", "rtmps", "rtsp", "rtsps", "mms", "mmsh", "ftp", "ftps", "gopher", "sctp")
  }

  fun shouldPreferResolvedMediaTitle(
    uri: Uri?,
    fallbackTitle: String?,
  ): Boolean {
    if (uri == null || !isNetworkStream(uri)) return false
    val scheme = uri.scheme?.lowercase()
    if (scheme !in listOf("http", "https")) return false
    return isLikelyJunkTitle(fallbackTitle) || !hasDirectMediaExtension(uri)
  }

  /**
   * Normalizes a user-input URL string while preserving all query parameters, tokens,
   * fragments, and encoding. Adds missing scheme if necessary.
   */
  fun normalizeUrl(inputUrl: String): String {
    val trimmed = inputUrl.trim()
    if (trimmed.isBlank()) return ""
    return when {
      trimmed.startsWith("http://", ignoreCase = true) ||
        trimmed.startsWith("https://", ignoreCase = true) ||
        trimmed.startsWith("rtmp://", ignoreCase = true) ||
        trimmed.startsWith("rtmps://", ignoreCase = true) ||
        trimmed.startsWith("rtsp://", ignoreCase = true) ||
        trimmed.startsWith("mms://", ignoreCase = true) -> trimmed
      trimmed.contains("://") -> trimmed
      else -> "https://$trimmed"
    }
  }

  /**
   * Describes the stream container or protocol based on URL pattern for UI preview cards.
   */
  fun getStreamTypeDescription(url: String): String {
    val lower = url.lowercase()
    return when {
      lower.contains(".m3u8") || lower.contains("/hls/") -> "HLS Adaptive Stream (.m3u8)"
      lower.contains(".mpd") || lower.contains("/dash/") -> "MPEG-DASH Manifest (.mpd)"
      lower.contains(".mp4") || lower.contains("/mp4/") -> "HTTP/HTTPS MP4 Video"
      lower.contains(".mkv") -> "Matroska Video (.mkv)"
      lower.contains(".webm") -> "WebM Stream (.webm)"
      lower.contains("youtube.com") || lower.contains("youtu.be") -> "YouTube Video Stream"
      lower.contains("twitch.tv") -> "Twitch Stream"
      lower.contains("vimeo.com") -> "Vimeo Stream"
      else -> "Network Media Stream"
    }
  }

  /**
   * True when [uri] is a network stream that points directly at a media/manifest file
   * (e.g. .m3u8/.mpd/.mp4/.ts). Such URLs should bypass yt-dlp and be handed straight to
   * mpv/ffmpeg's native demuxers, exactly like a dedicated player (MX Player/VLC) would.
   */
  fun isDirectMediaUrl(uri: Uri?): Boolean {
    if (uri == null || !isNetworkStream(uri)) return false
    return hasDirectMediaExtension(uri)
  }

  private fun hasDirectMediaExtension(uri: Uri): Boolean {
    val path = uri.path.orEmpty()
    val cleanPath = path.substringBefore('?').substringBefore('#')
    val pathExtension = cleanPath.substringAfterLast('.', "").lowercase()
    if (pathExtension in directMediaExtensions) return true

    val lastSegment =
      uri.lastPathSegment
        ?.substringAfterLast('/')
        ?.let(Uri::decode)
        .orEmpty()
    if (lastSegment.isNotBlank()) {
      val segExtension = lastSegment.substringBefore('?').substringBefore('#').substringAfterLast('.', "").lowercase()
      if (segExtension in directMediaExtensions) return true
    }
    return false
  }

  /**
   * Extracts the referer domain from a Uri.
   * Returns the full origin (scheme + host + port) to be used as Referer header.
   *
   * @param uri The Uri to extract the referer from
   * @return The referer origin string, or null if extraction fails
   */
  fun extractRefererDomain(uri: Uri?): String? {
    if (uri == null) return null

    return try {
      val scheme = uri.scheme ?: return null
      val host = uri.host ?: return null
      val port = uri.port

      // Build the referer origin
      if (port != -1 && port != 80 && port != 443) {
        // Include non-standard port
        "$scheme://$host:$port"
      } else {
        // Standard port or no port specified
        "$scheme://$host"
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error extracting referer domain: ${e.message}")
      null
    }
  }

  /**
   * Checks if a title is likely to be a raw URL, query string, or generic filename
   * rather than a human-readable media title.
   */
  fun isLikelyJunkTitle(title: String?): Boolean {
    if (title.isNullOrBlank()) return true

    val lower = title.lowercase()

    if (lower in genericRouteTitles) return true

    // Check for common URL patterns
    if (lower.startsWith("http") || lower.contains("://") || lower.contains("www.")) return true

    // Check for query parameters or common dynamic file types
    if (lower.contains("?") ||
      lower.contains("&") ||
      lower.contains("=") ||
      lower.contains(".aspx") ||
      lower.contains(".php") ||
      lower.contains(".jsp") ||
      lower.contains(".cfm")
    ) {
      return true
    }

    // Check for "download" prefix followed by nonsense
    if (lower.startsWith("download.")) return true

    // Check for specific junk seen in user screenshots
    if (lower.contains("share=") || lower.contains("tokens=")) return true

    // Unusually long strings with no spaces are likely URLs or hashes
    if (title.length > 60 && !title.contains(" ")) return true

    return false
  }
}
