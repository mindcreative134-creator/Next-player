/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package app.gyrolet.mpvrx.ads

object AdConfig {
  /**
   * Production AdMob App ID
   */
  const val ADMOB_APP_ID: String = "ca-app-pub-9543073887536718~8714519957"

  /**
   * Production Active Ad Unit IDs
   */
  const val APP_OPEN_AD_UNIT_ID: String = "ca-app-pub-9543073887536718/3216002450"
  const val BANNER_AD_UNIT_ID: String = "ca-app-pub-9543073887536718/9781410809"
  const val INTERSTITIAL_AD_UNIT_ID: String = "ca-app-pub-9543073887536718/6955936919"
  const val NATIVE_ADVANCED_AD_UNIT_ID: String = "ca-app-pub-9543073887536718/1541506865"
  const val REWARDED_AD_UNIT_ID: String = "ca-app-pub-9543073887536718/8742440115"

  /**
   * Frequency Capping: Minimum milliseconds between Interstitial Ads (90 seconds)
   */
  const val INTERSTITIAL_COOLDOWN_MS: Long = 90_000L

  /**
   * Frequency Capping: Minimum milliseconds between Video Pause Ads (3 minutes)
   */
  const val PAUSE_AD_COOLDOWN_MS: Long = 180_000L

  /**
   * Maximum automatic retry attempts for failed ad requests
   */
  const val MAX_AD_RETRIES: Int = 3
  const val INITIAL_RETRY_DELAY_MS: Long = 5_000L

  var isAdsEnabled: Boolean = true
}
