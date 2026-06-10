import { describe, expect, it, vi } from 'vitest'

import {
  APP_SETTINGS_STORAGE_KEY,
  DEFAULT_APP_SETTINGS,
  loadAppSettings,
  normalizeAppSettings,
  persistAppSettings,
  resolveIsDarkMode,
} from '@/lib/app-settings'

function createStorageArea(initialValue?: unknown) {
  let stored = initialValue

  return {
    get: vi.fn(async () => ({
      [APP_SETTINGS_STORAGE_KEY]: stored,
    })),
    set: vi.fn(async (items: Record<string, unknown>) => {
      stored = items[APP_SETTINGS_STORAGE_KEY]
    }),
    read() {
      return stored
    },
  }
}

describe('app-settings', () => {
  it('normalizes invalid values to the defaults', () => {
    expect(normalizeAppSettings(null)).toEqual(DEFAULT_APP_SETTINGS)
    expect(
      normalizeAppSettings({
        theme: 'unknown',
        rewriteEnabled: 'no',
      }),
    ).toEqual(DEFAULT_APP_SETTINGS)
  })

  it('migrates the old modern minimal preset key to modern', () => {
    expect(
      normalizeAppSettings({
        customThemePreset: 'modern-minimal' as never,
      }).selectedThemeId,
    ).toBe('modern')
  })

  it('migrates the old x layout setting to the feed interaction mode', () => {
    expect(normalizeAppSettings({ xLayoutEnabled: true }).feedInteractionMode).toBe('x')
    expect(normalizeAppSettings({ xLayoutEnabled: false }).feedInteractionMode).toBe('weibo')
  })

  it('normalizes feed toolbar settings', () => {
    expect(
      normalizeAppSettings({
        feedPrimaryActionOrder: ['like', 'comment', 'repost'],
        feedToolbarButtonIds: ['copy-text', 'unknown', 'favorite', 'favorite'],
      }).feedPrimaryActionOrder,
    ).toEqual(['like', 'comment', 'repost'])
    expect(
      normalizeAppSettings({
        feedPrimaryActionOrder: ['like', 'comment'],
        feedToolbarButtonIds: ['copy-text', 'unknown', 'favorite', 'favorite'],
      }).feedPrimaryActionOrder,
    ).toEqual(DEFAULT_APP_SETTINGS.feedPrimaryActionOrder)
    expect(
      normalizeAppSettings({
        feedToolbarButtonIds: ['copy-text', 'unknown', 'favorite', 'favorite'],
      }).feedToolbarButtonIds,
    ).toEqual(['copy-text', 'favorite'])
  })

  it('loads and persists settings through storage', async () => {
    const storage = createStorageArea({
      theme: 'dark',
      rewriteEnabled: false,
      fontSizeClass: 'text-sm',
      fontFamilyClass: 'font-serif',
      showHotSearchCard: false,
    })

    expect(await loadAppSettings(storage)).toEqual({
      contentWidth: 'standard',
      theme: 'dark',
      rewriteEnabled: false,
      fontSizeClass: 'text-sm',
      fontWeightClass: 'font-normal',
      letterSpacingClass: 'tracking-normal',
      lineHeightClass: 'leading-relaxed',
      fontFamilyClass: 'font-serif',
      showHotSearchCard: false,
      showFollowedSuperTopicsCard: false,
      showExplore: true,
      showFavorites: true,
      showHistory: true,
      showNotifications: true,
      showDMs: true,
      showProfile: true,
      showCompose: true,
      showRightRail: true,
      sidebarCollapsed: false,
      collapseRepliesEnabled: false,
      renderReplyChainEnabled: true,
      darkModeImageDim: false,
      lightModeBgColor: 'white',
      darkModeBgColor: 'near-black',
      imageGenEnabled: true,
      imageGenShowDataArea: true,
      imageGenShowFullImages: false,
      imageGenShowWeiboLink: false,
      imageGenTheme: 'light',
      imageGenCardStyle: 'default',
      hotSearchType: 'hot',
      feedInteractionMode: 'x',
      feedPrimaryActionOrder: ['comment', 'repost', 'like'],
      feedToolbarButtonIds: [],
      browsingHistoryLimit: 200,
      xbTopicPage: true,
      ratingEnabled: true,
      forceRedirectToFollowing: false,
      firstLoadRedirect: 'for-you',
      homeTab: 'for-you',
      homeGroupId: null,
      customThemeLightCss: '',
      customThemeDarkCss: '',
      selectedThemeType: 'preset',
      selectedThemeId: 'default',
      userThemes: [],
    })

    await persistAppSettings(
      {
        contentWidth: 'standard',
        theme: 'light',
        rewriteEnabled: true,
        fontSizeClass: 'text-lg',
        fontWeightClass: 'font-medium',
        letterSpacingClass: 'tracking-wide',
        lineHeightClass: 'leading-loose',
        fontFamilyClass: 'font-serif',
        showHotSearchCard: true,
        showFollowedSuperTopicsCard: false,
        showExplore: true,
        showFavorites: true,
        showHistory: true,
        showNotifications: true,
        showDMs: true,
        showProfile: true,
        showCompose: true,
        showRightRail: true,
        sidebarCollapsed: false,
        collapseRepliesEnabled: false,
        renderReplyChainEnabled: true,
        darkModeImageDim: false,
        lightModeBgColor: 'paper',
        darkModeBgColor: 'dark-gray',
        imageGenEnabled: true,
        imageGenShowDataArea: true,
        imageGenShowFullImages: false,
        imageGenShowWeiboLink: false,
        imageGenTheme: 'light',
        imageGenCardStyle: 'default',
        hotSearchType: 'mine',
        feedInteractionMode: 'x',
        feedPrimaryActionOrder: ['comment', 'repost', 'like'],
        feedToolbarButtonIds: [],
        browsingHistoryLimit: 300,
        xbTopicPage: true,
        ratingEnabled: true,
        forceRedirectToFollowing: false,
        firstLoadRedirect: 'for-you',
        homeTab: 'for-you',
        homeGroupId: null,
        customThemeLightCss: '--primary: #1d9bf0;',
        customThemeDarkCss: '--primary: #1d9bf0;',
        selectedThemeType: 'preset',
        selectedThemeId: 'default',
        userThemes: [],
      },
      storage,
    )

    expect(storage.read()).toEqual({
      contentWidth: 'standard',
      theme: 'light',
      rewriteEnabled: true,
      fontSizeClass: 'text-lg',
      fontWeightClass: 'font-medium',
      letterSpacingClass: 'tracking-wide',
      lineHeightClass: 'leading-loose',
      fontFamilyClass: 'font-serif',
      showHotSearchCard: true,
      showFollowedSuperTopicsCard: false,
      showExplore: true,
      showFavorites: true,
      showHistory: true,
      showNotifications: true,
      showDMs: true,
      showProfile: true,
      showCompose: true,
      showRightRail: true,
      sidebarCollapsed: false,
      collapseRepliesEnabled: false,
      renderReplyChainEnabled: true,
      darkModeImageDim: false,
      lightModeBgColor: 'paper',
      darkModeBgColor: 'dark-gray',
      imageGenEnabled: true,
      imageGenShowDataArea: true,
      imageGenShowFullImages: false,
      imageGenShowWeiboLink: false,
      imageGenTheme: 'light',
      imageGenCardStyle: 'default',
      hotSearchType: 'mine',
      feedInteractionMode: 'x',
      feedPrimaryActionOrder: ['comment', 'repost', 'like'],
      feedToolbarButtonIds: [],
      browsingHistoryLimit: 300,
      xbTopicPage: true,
      ratingEnabled: true,
      forceRedirectToFollowing: false,
      firstLoadRedirect: 'for-you',
      homeTab: 'for-you',
      homeGroupId: null,
      customThemeLightCss: '--primary: #1d9bf0;',
      customThemeDarkCss: '--primary: #1d9bf0;',
      selectedThemeType: 'preset',
      selectedThemeId: 'default',
      userThemes: [],
    })
  })

  it('resolves dark mode from theme preference', () => {
    expect(resolveIsDarkMode('dark', false)).toBe(true)
    expect(resolveIsDarkMode('light', true)).toBe(false)
    expect(resolveIsDarkMode('system', true)).toBe(true)
    expect(resolveIsDarkMode('system', false)).toBe(false)
  })
})
