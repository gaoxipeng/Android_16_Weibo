import type { StoreApi } from 'zustand/vanilla'
import { createStore } from 'zustand/vanilla'

import type { AppSettingsStorageArea } from '@/lib/app-settings'
import { createSingletonStoreAccess } from '@/lib/zustand-singleton-store'

interface RecentEmoticonEntry {
  phrase: string
  url: string
}

export const RECENT_EMOTICONS_STORAGE_KEY = 'xb:weibo-recent-emoticons'

interface RecentEmoticonsState {
  isHydrated: boolean
  items: RecentEmoticonEntry[]
  hydrate: () => Promise<void>
  remember: (entry: RecentEmoticonEntry) => Promise<void>
  clear: () => Promise<void>
}

function normalizeRecentEntries(value: unknown): RecentEmoticonEntry[] {
  if (!Array.isArray(value)) {
    return []
  }

  return value
    .filter((item): item is RecentEmoticonEntry => {
      return Boolean(
        item &&
        typeof item === 'object' &&
        typeof (item as RecentEmoticonEntry).phrase === 'string' &&
        typeof (item as RecentEmoticonEntry).url === 'string',
      )
    })
    .slice(0, 10)
}

export function createRecentEmoticonsStore(
  storageArea: AppSettingsStorageArea = browser.storage.local,
): StoreApi<RecentEmoticonsState> {
  return createStore<RecentEmoticonsState>((set, get) => ({
    isHydrated: false,
    items: [],
    async hydrate() {
      const stored = await storageArea.get(RECENT_EMOTICONS_STORAGE_KEY)
      set({
        isHydrated: true,
        items: normalizeRecentEntries(stored[RECENT_EMOTICONS_STORAGE_KEY]),
      })
    },
    async remember(entry) {
      const nextItems = [
        entry,
        ...get().items.filter((item) => item.phrase !== entry.phrase),
      ].slice(0, 10)

      set({ items: nextItems })
      await storageArea.set({
        [RECENT_EMOTICONS_STORAGE_KEY]: nextItems,
      })
    },
    async clear() {
      set({ items: [] })
      await storageArea.set({ [RECENT_EMOTICONS_STORAGE_KEY]: [] })
    },
  }))
}

let recentEmoticonsStorageArea: AppSettingsStorageArea | undefined

const recentEmoticonsStoreAccess = createSingletonStoreAccess(() =>
  createRecentEmoticonsStore(recentEmoticonsStorageArea),
)

function getRecentEmoticonsStore(storageArea?: AppSettingsStorageArea) {
  if (storageArea) {
    recentEmoticonsStorageArea = storageArea
  }

  return recentEmoticonsStoreAccess.getStore()
}

export function resetRecentEmoticonsStoreForTest() {
  recentEmoticonsStorageArea = undefined
  recentEmoticonsStoreAccess.resetForTest()
}

export function useRecentEmoticons<T>(selector: (state: RecentEmoticonsState) => T): T {
  return recentEmoticonsStoreAccess.useSingletonStore(selector)
}

export { getRecentEmoticonsStore }
