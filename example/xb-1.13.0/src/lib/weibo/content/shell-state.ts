import { DARK_BG_PRESETS, LIGHT_BG_PRESETS, resolveIsDarkMode } from '@/lib/app-settings'
import type { AppSettingsStore } from '@/lib/app-settings-store'
import { CUSTOM_THEME_VARIABLE_NAMES, parseCustomThemeVariables } from '@/lib/custom-theme'
import {
  applyPageTakeover,
  clearPageTakeover,
  markWeiboPageReady,
} from '@/lib/weibo/content/page-takeover'
import { parseWeiboUrl } from '@/lib/weibo/route/parse-weibo-url'

const OVERFLOW_STORAGE_KEY = 'data-xb-previous-overflow'

function isRouteSupported(): boolean {
  return parseWeiboUrl(window.location.href).kind !== 'unsupported'
}

function clearCustomThemeVariables(container: HTMLElement) {
  for (const variable of CUSTOM_THEME_VARIABLE_NAMES) {
    container.style.removeProperty(variable)
  }
}

export function bindShellState({
  container,
  appRoot,
  settingsStore,
}: {
  container: HTMLElement
  appRoot: HTMLElement
  settingsStore: AppSettingsStore
}) {
  const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')

  const applyShellState = () => {
    const settings = settingsStore.getState()
    const isDark = resolveIsDarkMode(settings.theme, mediaQuery.matches)

    container.classList.toggle('dark', isDark)
    clearCustomThemeVariables(container)

    // Apply background color preset CSS variables
    const preset = isDark
      ? DARK_BG_PRESETS.find((p) => p.key === settings.darkModeBgColor)
      : LIGHT_BG_PRESETS.find((p) => p.key === settings.lightModeBgColor)

    if (preset) {
      container.style.setProperty('--background', preset.background)
      container.style.setProperty('--card', preset.card)
    }

    const customThemeCss = isDark ? settings.customThemeDarkCss : settings.customThemeLightCss
    if (customThemeCss.trim()) {
      const variables = parseCustomThemeVariables(customThemeCss)
      for (const [name, value] of Object.entries(variables)) {
        container.style.setProperty(name, value)
      }
    }

    // When route is unsupported, release the host page and hide xb UI
    // so Weibo's native UI is visible and functional.
    if (!isRouteSupported()) {
      container.style.display = 'none'
      const previousOverflow = document.documentElement.getAttribute(OVERFLOW_STORAGE_KEY)
      document.documentElement.style.overflow =
        previousOverflow !== null && previousOverflow !== '' ? previousOverflow : 'auto'
      document.documentElement.removeAttribute(OVERFLOW_STORAGE_KEY)
      clearPageTakeover(appRoot)
      return
    }

    // Route is supported — make sure xb container is visible
    container.style.removeProperty('display')

    if (settings.rewriteEnabled) {
      if (!document.documentElement.hasAttribute(OVERFLOW_STORAGE_KEY)) {
        document.documentElement.setAttribute(
          OVERFLOW_STORAGE_KEY,
          document.documentElement.style.overflow || '',
        )
      }
      document.documentElement.style.overflow = 'hidden'
      applyPageTakeover(appRoot)
      return
    }

    const previousOverflow = document.documentElement.getAttribute(OVERFLOW_STORAGE_KEY)
    document.documentElement.style.overflow =
      previousOverflow !== null && previousOverflow !== '' ? previousOverflow : 'auto'
    document.documentElement.removeAttribute(OVERFLOW_STORAGE_KEY)
    clearPageTakeover(appRoot)
  }

  const unsubscribe = settingsStore.subscribe(applyShellState)
  const onSystemThemeChange = () => applyShellState()

  // Re-evaluate shell state when URL changes (SPA navigation)
  const onUrlChange = () => applyShellState()
  window.addEventListener('popstate', onUrlChange)

  // Also detect pushState/replaceState navigations by patching History methods.
  const originalPushState = history.pushState.bind(history)
  const originalReplaceState = history.replaceState.bind(history)

  history.pushState = function (...args: Parameters<typeof history.pushState>) {
    originalPushState(...args)
    onUrlChange()
  }
  history.replaceState = function (...args: Parameters<typeof history.replaceState>) {
    originalReplaceState(...args)
    onUrlChange()
  }

  applyShellState()
  markWeiboPageReady()
  mediaQuery.addEventListener('change', onSystemThemeChange)

  return () => {
    unsubscribe()
    window.removeEventListener('popstate', onUrlChange)
    mediaQuery.removeEventListener('change', onSystemThemeChange)
    history.pushState = originalPushState
    history.replaceState = originalReplaceState
    container.classList.remove('dark')
    clearCustomThemeVariables(container)
    container.style.removeProperty('display')
    const previousOverflow = document.documentElement.getAttribute(OVERFLOW_STORAGE_KEY)
    document.documentElement.style.overflow =
      previousOverflow !== null && previousOverflow !== '' ? previousOverflow : 'auto'
    document.documentElement.removeAttribute(OVERFLOW_STORAGE_KEY)
    clearPageTakeover(appRoot)
  }
}
