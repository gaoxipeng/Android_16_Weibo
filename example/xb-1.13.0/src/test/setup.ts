import { notifyManager } from '@tanstack/query-core'
import '@testing-library/jest-dom/vitest'
import { vi } from 'vitest'

// Avoid setTimeout(0) batches firing after Vitest tears down jsdom (window is not defined).
notifyManager.setScheduler((run) => {
  run()
})

Object.defineProperty(window, 'matchMedia', {
  writable: true,
  configurable: true,
  value: vi.fn().mockImplementation((query: string) => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: vi.fn(),
    removeListener: vi.fn(),
    addEventListener: vi.fn(),
    removeEventListener: vi.fn(),
    dispatchEvent: vi.fn(),
  })),
})

Object.defineProperty(globalThis, 'browser', {
  writable: true,
  configurable: true,
  value: {
    runtime: {
      getManifest: () => ({ version: '0.0.0-test' }),
    },
  },
})
