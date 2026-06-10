import { createStore, type StoreApi } from 'zustand/vanilla'

import type { RatingSummary } from '@/lib/weibo/models/rating'

const STORAGE_KEY = 'xb-rating-cache'

export interface RatingCacheState {
  /** uid → rating summary */
  summaries: Record<string, RatingSummary>
  /** uid → current user's star rating (1-5) */
  myStars: Record<string, number>
  /** uid → last sync timestamp (ms) */
  lastSyncAt: Record<string, number>

  isHydrated: boolean
  hydrate: () => Promise<void>
  setSummary: (uid: string, summary: RatingSummary) => void
  setMyStars: (uid: string, stars: number) => void
  setBatchSummaries: (entries: Record<string, RatingSummary>) => void
  clear: () => void
}

export type RatingCacheStore = StoreApi<RatingCacheState>

const INITIAL_STATE = {
  summaries: {},
  myStars: {},
  lastSyncAt: {},
  isHydrated: false,
}

export function createRatingCacheStore(
  storageArea: typeof browser.storage.local = browser.storage.local,
): RatingCacheStore {
  return createStore<RatingCacheState>((set, get) => {
    async function persist() {
      const { summaries, myStars, lastSyncAt } = get()
      await storageArea.set({
        [STORAGE_KEY]: { summaries, myStars, lastSyncAt },
      })
    }

    return {
      ...INITIAL_STATE,

      async hydrate() {
        try {
          const result = await storageArea.get(STORAGE_KEY)
          const cached = result[STORAGE_KEY] as
            | {
                summaries?: Record<string, RatingSummary>
                myStars?: Record<string, number>
                lastSyncAt?: Record<string, number>
              }
            | undefined
          if (cached) {
            set({
              summaries: cached.summaries ?? {},
              myStars: cached.myStars ?? {},
              lastSyncAt: cached.lastSyncAt ?? {},
              isHydrated: true,
            })
          } else {
            set({ isHydrated: true })
          }
        } catch {
          set({ isHydrated: true })
        }
      },

      setSummary(uid, summary) {
        set((state) => ({
          summaries: { ...state.summaries, [uid]: summary },
          lastSyncAt: { ...state.lastSyncAt, [uid]: Date.now() },
        }))
        void persist()
      },

      setMyStars(uid, stars) {
        set((state) => ({
          myStars: { ...state.myStars, [uid]: stars },
        }))
        void persist()
      },

      setBatchSummaries(entries) {
        const now = Date.now()
        set((state) => ({
          summaries: { ...state.summaries, ...entries },
          lastSyncAt: {
            ...state.lastSyncAt,
            ...Object.fromEntries(Object.keys(entries).map((uid) => [uid, now])),
          },
        }))
        void persist()
      },

      clear() {
        set({ summaries: {}, myStars: {}, lastSyncAt: {} })
        void storageArea.remove(STORAGE_KEY)
      },
    }
  })
}

let ratingCacheStore: RatingCacheStore | null = null

export function getRatingCacheStore(): RatingCacheStore {
  if (!ratingCacheStore) {
    ratingCacheStore = createRatingCacheStore()
  }
  return ratingCacheStore
}

export function resetRatingCacheStoreForTest() {
  ratingCacheStore = null
}
