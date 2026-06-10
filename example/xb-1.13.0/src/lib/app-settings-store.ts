import { useStore } from 'zustand'
import { useShallow } from 'zustand/react/shallow'
import { createStore, type StoreApi } from 'zustand/vanilla'

import {
  DEFAULT_APP_SETTINGS,
  loadAppSettings,
  persistAppSettings,
  type AppSettings,
  type AppSettingsStorageArea,
  type UserTheme,
} from '@/lib/app-settings'
import { CUSTOM_THEME_PRESETS } from '@/lib/custom-theme'

const PERSISTED_KEYS = Object.keys(DEFAULT_APP_SETTINGS) as (keyof AppSettings)[]

export interface AppSettingsStoreState extends AppSettings {
  isHydrated: boolean
  hydrate: () => Promise<void>
  updateSettings: (patch: Partial<AppSettings>) => Promise<void>
  addUserTheme: (theme: UserTheme) => Promise<void>
  deleteUserTheme: (id: string) => Promise<void>
  updateUserTheme: (
    id: string,
    updates: Partial<Pick<UserTheme, 'lightCss' | 'darkCss' | 'name'>>,
  ) => Promise<void>
}

export type AppSettingsStore = StoreApi<AppSettingsStoreState>

function toPersistedSettings(state: AppSettingsStoreState): AppSettings {
  return Object.fromEntries(
    PERSISTED_KEYS.map((key) => [key, state[key]]),
  ) as unknown as AppSettings
}

export function createAppSettingsStore(
  storageArea: AppSettingsStorageArea = browser.storage.local,
): AppSettingsStore {
  return createStore<AppSettingsStoreState>((set, get) => {
    async function updateAndPersist(patch: Partial<AppSettings>) {
      set(patch)
      await persistAppSettings(
        {
          ...toPersistedSettings(get()),
          ...patch,
        },
        storageArea,
      )
    }

    return {
      ...DEFAULT_APP_SETTINGS,
      isHydrated: false,
      async hydrate() {
        const settings = await loadAppSettings(storageArea)

        if (
          settings.selectedThemeType === 'preset' &&
          !settings.customThemeLightCss.trim() &&
          !settings.customThemeDarkCss.trim()
        ) {
          const preset = CUSTOM_THEME_PRESETS.find((p) => p.key === settings.selectedThemeId)
          if (preset) {
            settings.customThemeLightCss = preset.lightCss
            settings.customThemeDarkCss = preset.darkCss
          }
        }

        set({
          ...settings,
          isHydrated: true,
        })
      },
      updateSettings: updateAndPersist,
      async addUserTheme(theme) {
        const current = get()
        await updateAndPersist({
          userThemes: [...current.userThemes, theme],
        })
      },
      async deleteUserTheme(id) {
        const current = get()
        await updateAndPersist({
          userThemes: current.userThemes.filter((t) => t.id !== id),
        })
      },
      async updateUserTheme(id, updates) {
        const current = get()
        await updateAndPersist({
          userThemes: current.userThemes.map((t) => (t.id === id ? { ...t, ...updates } : t)),
        })
      },
    }
  })
}

let appSettingsStore: AppSettingsStore | null = null

export function getAppSettingsStore(storageArea?: AppSettingsStorageArea): AppSettingsStore {
  if (!appSettingsStore) {
    appSettingsStore = createAppSettingsStore(storageArea)
  }

  return appSettingsStore
}

export function resetAppSettingsStoreForTest() {
  appSettingsStore = null
}

export function useAppSettings<T>(selector: (state: AppSettingsStoreState) => T): T {
  return useStore(getAppSettingsStore(), selector)
}

export function useUpdateAppSettings() {
  return useAppSettings((state) => state.updateSettings)
}

export { useShallow }
