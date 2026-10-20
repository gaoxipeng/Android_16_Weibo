package com.example.myweibo.data

import android.view.View
import android.webkit.WebView

/**
 * Keep Chromium drawVk functors out of HWUI render layers. On affected Android
 * 16/WebView builds they trap inside libwebviewchromium on RenderThread, killing
 * the host process (not a recoverable Java exception or renderer-process exit).
 * Apply before attaching/loading WebViews in captured/animated subtrees,
 * including the hidden session. The messages page is separately hosted outside
 * those layers and uses normal window hardware drawing for IME/scroll performance.
 * This does not disable GPU rendering for Compose or native video surfaces.
 */
internal fun WebView.useSafeWebRendering() {
    setLayerType(View.LAYER_TYPE_SOFTWARE, null)
}
