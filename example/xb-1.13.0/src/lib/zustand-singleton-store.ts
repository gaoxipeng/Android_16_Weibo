import { useStore } from 'zustand'
import type { StoreApi } from 'zustand/vanilla'

export function createSingletonStoreAccess<T>(create: () => StoreApi<T>) {
  let store: StoreApi<T> | null = null

  function getStore(): StoreApi<T> {
    if (!store) {
      store = create()
    }

    return store
  }

  function resetForTest() {
    store = null
  }

  function useSingletonStore<S>(selector: (state: T) => S): S {
    return useStore(getStore(), selector)
  }

  return { getStore, resetForTest, useSingletonStore }
}
