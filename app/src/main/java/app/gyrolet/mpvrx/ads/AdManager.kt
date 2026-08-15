/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package app.gyrolet.mpvrx.ads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import java.util.concurrent.atomic.AtomicBoolean

object AdManager {
  private const val TAG = "AdManager"
  private val isInitialized = AtomicBoolean(false)

  private var preloadedInterstitialAd: InterstitialAd? = null
  private var isLoadingInterstitial = false

  private var lastInterstitialShowTime = 0L
  private var lastPauseAdShowTime = 0L

  // Caching & Pooling for Compose screens
  private val bannerCache = mutableMapOf<String, AdView>()
  private val nativeCache = mutableMapOf<String, NativeAd>()
  private val adRetryCounts = mutableMapOf<String, Int>()

  /**
   * Initializes Google Mobile Ads SDK asynchronously at app startup and preloads ads.
   */
  fun initialize(context: Context) {
    if (isInitialized.compareAndSet(false, true)) {
      MobileAds.initialize(context) { initializationStatus ->
        Log.d(TAG, "Google Mobile Ads SDK Initialized: $initializationStatus")
        // Preload an interstitial ad right after SDK init
        preloadInterstitialAd(context)
      }
    }
  }

  /**
   * Preloads an Interstitial Ad in background.
   */
  fun preloadInterstitialAd(context: Context) {
    if (!AdConfig.isAdsEnabled || preloadedInterstitialAd != null || isLoadingInterstitial) {
      return
    }

    isLoadingInterstitial = true
    val adRequest = AdRequest.Builder().build()

    InterstitialAd.load(
      context,
      AdConfig.INTERSTITIAL_AD_UNIT_ID,
      adRequest,
      object : InterstitialAdLoadCallback() {
        override fun onAdLoaded(ad: InterstitialAd) {
          isLoadingInterstitial = false
          preloadedInterstitialAd = ad
          Log.d(TAG, "Interstitial Ad Preloaded Successfully")
        }

        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
          isLoadingInterstitial = false
          preloadedInterstitialAd = null
          Log.e(TAG, "Failed to preload Interstitial Ad: ${loadAdError.message}")
        }
      }
    )
  }

  /**
   * Returns an Anchored Adaptive Banner AdView (or creates and caches a new one for the given ID).
   */
  fun getAdaptiveBannerAd(context: Context, adUnitId: String, id: String, adWidthDp: Int): AdView {
    bannerCache[id]?.let { existingAd ->
      Log.d(TAG, "Returning cached Adaptive Banner Ad for id: $id")
      return existingAd
    }

    Log.d(TAG, "Creating new Anchored Adaptive Banner Ad for id: $id (widthDp: $adWidthDp)")
    val adaptiveSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidthDp)

    val adView = AdView(context).apply {
      setAdUnitId(adUnitId)
      setAdSize(adaptiveSize)
      adListener = object : AdListener() {
        override fun onAdLoaded() {
          Log.d(TAG, "Adaptive Banner Ad Loaded Successfully for $id")
          adRetryCounts[id] = 0
        }

        override fun onAdFailedToLoad(error: LoadAdError) {
          Log.e(TAG, "Adaptive Banner Ad Failed to Load for $id: ${error.message}")
          scheduleRetryIfPossible(id) {
            loadAd(AdRequest.Builder().build())
          }
        }

        override fun onAdImpression() {
          Log.d(TAG, "Adaptive Banner Ad Impression Recorded for $id")
        }
      }
      loadAd(AdRequest.Builder().build())
    }

    bannerCache[id] = adView
    return adView
  }

  /**
   * Standard Banner getter for fallback.
   */
  fun getBannerAd(context: Context, adUnitId: String, id: String): AdView {
    return bannerCache.getOrPut(id) {
      Log.d(TAG, "Creating standard Banner Ad for id: $id")
      AdView(context).apply {
        setAdUnitId(adUnitId)
        setAdSize(AdSize.BANNER)
        adListener = object : AdListener() {
          override fun onAdLoaded() {
            Log.d(TAG, "Banner Ad Loaded Successfully for $id")
            adRetryCounts[id] = 0
          }
          override fun onAdFailedToLoad(error: LoadAdError) {
            Log.e(TAG, "Banner Ad Failed to Load for $id: ${error.message}")
            scheduleRetryIfPossible(id) {
              loadAd(AdRequest.Builder().build())
            }
          }
        }
        loadAd(AdRequest.Builder().build())
      }
    }
  }

  /**
   * Fetches or retrieves cached Native Ad.
   */
  fun getNativeAd(context: Context, adUnitId: String, id: String, onLoaded: (NativeAd) -> Unit) {
    nativeCache[id]?.let {
      Log.d(TAG, "Returning cached Native Ad for id: $id")
      onLoaded(it)
      return
    }

    Log.d(TAG, "Requesting new Native Ad for id: $id")
    val adLoader = AdLoader.Builder(context, adUnitId)
      .forNativeAd { ad: NativeAd ->
        Log.d(TAG, "Native Ad Loaded Successfully for $id")
        nativeCache[id] = ad
        adRetryCounts[id] = 0
        onLoaded(ad)
      }
      .withAdListener(object : AdListener() {
        override fun onAdFailedToLoad(error: LoadAdError) {
          Log.e(TAG, "Native Ad Failed to Load for $id: ${error.message}")
          scheduleRetryIfPossible(id) {
            getNativeAd(context, adUnitId, id, onLoaded)
          }
        }
      })
      .withNativeAdOptions(NativeAdOptions.Builder().build())
      .build()

    adLoader.loadAd(AdRequest.Builder().build())
  }

  private fun scheduleRetryIfPossible(id: String, retryAction: () -> Unit) {
    val currentRetry = adRetryCounts.getOrDefault(id, 0)
    if (currentRetry < AdConfig.MAX_AD_RETRIES) {
      adRetryCounts[id] = currentRetry + 1
      val delayMs = AdConfig.INITIAL_RETRY_DELAY_MS * (currentRetry + 1)
      Log.d(TAG, "Scheduling ad retry #${currentRetry + 1} for $id in ${delayMs}ms")
      Handler(Looper.getMainLooper()).postDelayed({
        retryAction()
      }, delayMs)
    }
  }

  fun clearCaches() {
    bannerCache.values.forEach { it.destroy() }
    bannerCache.clear()
    nativeCache.values.forEach { it.destroy() }
    nativeCache.clear()
    adRetryCounts.clear()
  }

  /**
   * Shows Video Pause Ad with smart frequency capping (3-min cooldown).
   */
  fun showPauseAd(
    activity: Activity,
    onAdDismissed: () -> Unit = {},
  ) {
    if (!AdConfig.isAdsEnabled) {
      onAdDismissed()
      return
    }

    val now = System.currentTimeMillis()
    if (now - lastPauseAdShowTime < AdConfig.PAUSE_AD_COOLDOWN_MS) {
      Log.d(TAG, "Pause Ad skipped due to frequency capping cooldown")
      onAdDismissed()
      return
    }

    Log.d(TAG, "Triggering Pause Ad for video player")
    loadAndShowInterstitialAd(
      activity = activity,
      forceShow = true,
      onAdDismissed = {
        lastPauseAdShowTime = System.currentTimeMillis()
        onAdDismissed()
      }
    )
  }

  /**
   * Loads and shows Interstitial Ad (uses preloaded ad if available).
   */
  fun loadAndShowInterstitialAd(
    activity: Activity,
    forceShow: Boolean = false,
    onAdDismissed: () -> Unit = {},
  ) {
    if (!AdConfig.isAdsEnabled) {
      onAdDismissed()
      return
    }

    val now = System.currentTimeMillis()
    if (!forceShow && (now - lastInterstitialShowTime < AdConfig.INTERSTITIAL_COOLDOWN_MS)) {
      Log.d(TAG, "Interstitial show skipped due to frequency capping cooldown")
      onAdDismissed()
      return
    }

    // Check if preloaded ad is ready
    preloadedInterstitialAd?.let { preloadedAd ->
      Log.d(TAG, "Showing preloaded Interstitial Ad")
      preloadedInterstitialAd = null
      showLoadedInterstitialAd(activity, preloadedAd, onAdDismissed)
      return
    }

    if (isLoadingInterstitial) {
      Log.d(TAG, "Interstitial Ad is already loading")
      onAdDismissed()
      return
    }

    isLoadingInterstitial = true
    val adRequest = AdRequest.Builder().build()

    InterstitialAd.load(
      activity,
      AdConfig.INTERSTITIAL_AD_UNIT_ID,
      adRequest,
      object : InterstitialAdLoadCallback() {
        override fun onAdLoaded(ad: InterstitialAd) {
          isLoadingInterstitial = false
          Log.d(TAG, "On-demand Interstitial Ad Loaded Successfully")
          showLoadedInterstitialAd(activity, ad, onAdDismissed)
        }

        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
          isLoadingInterstitial = false
          Log.e(TAG, "Failed to load Interstitial Ad: ${loadAdError.message}")
          onAdDismissed()
          preloadInterstitialAd(activity)
        }
      },
    )
  }

  private fun showLoadedInterstitialAd(
    activity: Activity,
    ad: InterstitialAd,
    onAdDismissed: () -> Unit
  ) {
    if (activity.isFinishing || activity.isDestroyed) {
      Log.w(TAG, "Cannot show Interstitial Ad: Activity is finishing or destroyed")
      onAdDismissed()
      return
    }

    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
      override fun onAdDismissedFullScreenContent() {
        Log.d(TAG, "Interstitial Ad Dismissed")
        lastInterstitialShowTime = System.currentTimeMillis()
        onAdDismissed()
        preloadInterstitialAd(activity.applicationContext)
      }

      override fun onAdFailedToShowFullScreenContent(adError: AdError) {
        Log.e(TAG, "Interstitial Ad Failed to Show: ${adError.message}")
        onAdDismissed()
        preloadInterstitialAd(activity.applicationContext)
      }

      override fun onAdImpression() {
        Log.d(TAG, "Interstitial Ad Impression Recorded")
      }
    }

    try {
      ad.show(activity)
    } catch (e: Exception) {
      Log.e(TAG, "Exception showing Interstitial Ad", e)
      onAdDismissed()
    }
  }

  private var isLoadingRewarded = false

  /**
   * Loads and shows a Rewarded Ad on-demand.
   * Used in Settings > Support Developer (Watch Ad).
   */
  fun loadAndShowRewardedAd(
    activity: Activity,
    onRewardEarned: () -> Unit,
    onAdDismissed: () -> Unit = {},
  ) {
    if (!AdConfig.isAdsEnabled) {
      onRewardEarned()
      onAdDismissed()
      return
    }

    if (activity.isFinishing || activity.isDestroyed) {
      Log.w(TAG, "Cannot show Rewarded Ad: Activity is finishing or destroyed")
      onAdDismissed()
      return
    }

    if (isLoadingRewarded) {
      Log.d(TAG, "Rewarded Ad is already loading")
      onAdDismissed()
      return
    }

    isLoadingRewarded = true
    val adRequest = AdRequest.Builder().build()

    RewardedAd.load(
      activity,
      AdConfig.REWARDED_AD_UNIT_ID,
      adRequest,
      object : RewardedAdLoadCallback() {
        override fun onAdLoaded(ad: RewardedAd) {
          isLoadingRewarded = false
          Log.d(TAG, "Rewarded Ad Loaded Successfully")

          if (activity.isFinishing || activity.isDestroyed) {
            onAdDismissed()
            return
          }

          ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
              Log.d(TAG, "Rewarded Ad Dismissed")
              onAdDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
              Log.e(TAG, "Rewarded Ad Failed to Show: ${adError.message}")
              onAdDismissed()
            }
          }

          try {
            ad.show(activity) { rewardItem ->
              Log.d(TAG, "Reward Earned: ${rewardItem.amount} ${rewardItem.type}")
              onRewardEarned()
            }
          } catch (e: Exception) {
            Log.e(TAG, "Exception showing Rewarded Ad", e)
            onAdDismissed()
          }
        }

        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
          isLoadingRewarded = false
          Log.e(TAG, "Failed to load Rewarded Ad: ${loadAdError.message}")
          onAdDismissed()
        }
      }
    )
  }
}
