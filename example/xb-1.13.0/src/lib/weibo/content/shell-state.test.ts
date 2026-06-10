import { describe, expect, it } from 'vitest'

import { APP_SETTINGS_STORAGE_KEY } from '@/lib/app-settings'
import { createAppSettingsStore } from '@/lib/app-settings-store'
import { bindShellState } from '@/lib/weibo/content/shell-state'

describe('bindShellState', () => {
  it('applies dark mode and rewrite takeover from the shared store', async () => {
    const container = document.createElement('div')
    const appRoot = document.createElement('div')
    const store = createAppSettingsStore({
      get: async () => ({ [APP_SETTINGS_STORAGE_KEY]: undefined }),
      set: async () => {},
    })

    Object.defineProperty(window, 'matchMedia', {
      value: () => ({
        matches: false,
        addEventListener: () => {},
        removeEventListener: () => {},
      }),
      configurable: true,
    })

    const cleanup = bindShellState({
      container,
      appRoot,
      settingsStore: store,
    })

    expect(document.documentElement.getAttribute('data-xb-weibo-ready')).toBe('')

    await store.getState().updateSettings({ theme: 'dark', rewriteEnabled: false })

    expect(container.classList.contains('dark')).toBe(true)
    expect(appRoot.getAttribute('data-xb-hidden')).toBeNull()
    expect(document.documentElement.style.overflow).toBe('auto')

    cleanup()
  })

  it('cleans up takeover and dark mode classes on unbind', async () => {
    const container = document.createElement('div')
    const appRoot = document.createElement('div')
    const store = createAppSettingsStore({
      get: async () => ({ [APP_SETTINGS_STORAGE_KEY]: undefined }),
      set: async () => {},
    })

    Object.defineProperty(window, 'matchMedia', {
      value: () => ({
        matches: false,
        addEventListener: () => {},
        removeEventListener: () => {},
      }),
      configurable: true,
    })

    const cleanup = bindShellState({
      container,
      appRoot,
      settingsStore: store,
    })

    await store.getState().updateSettings({ theme: 'dark' })

    expect(container.classList.contains('dark')).toBe(true)
    expect(appRoot.getAttribute('data-xb-hidden')).toBe('true')
    expect(document.documentElement.style.overflow).toBe('hidden')

    cleanup()

    expect(container.classList.contains('dark')).toBe(false)
    expect(appRoot.getAttribute('data-xb-hidden')).toBeNull()
    expect(document.documentElement.style.overflow).toBe('auto')
  })

  it('applies custom theme variables for the active color mode', async () => {
    const container = document.createElement('div')
    const appRoot = document.createElement('div')
    const store = createAppSettingsStore({
      get: async () => ({ [APP_SETTINGS_STORAGE_KEY]: undefined }),
      set: async () => {},
    })

    Object.defineProperty(window, 'matchMedia', {
      value: () => ({
        matches: false,
        addEventListener: () => {},
        removeEventListener: () => {},
      }),
      configurable: true,
    })

    const cleanup = bindShellState({
      container,
      appRoot,
      settingsStore: store,
    })

    await store.getState().updateSettings({
      customThemeLightCss: '--primary: #111111; --background: #ffffff;',
      customThemeDarkCss: '--primary: #eeeeee; --background: #000000;',
    })

    expect(container.style.getPropertyValue('--primary')).toBe('#111111')
    expect(container.style.getPropertyValue('--background')).toBe('#ffffff')

    await store.getState().updateSettings({ theme: 'dark' })

    expect(container.style.getPropertyValue('--primary')).toBe('#eeeeee')
    expect(container.style.getPropertyValue('--background')).toBe('#000000')

    await store.getState().updateSettings({
      customThemeLightCss: '',
      customThemeDarkCss: '',
    })

    expect(container.style.getPropertyValue('--primary')).toBe('')
    expect(container.style.getPropertyValue('--background')).toBe('oklch(0.1908 0.002 106.59)')

    cleanup()
  })
})
