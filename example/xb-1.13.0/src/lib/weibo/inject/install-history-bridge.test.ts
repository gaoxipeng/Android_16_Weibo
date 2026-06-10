import { describe, expect, it } from 'vitest'

import { installHistoryBridge } from '@/lib/weibo/inject/install-history-bridge'

describe('installHistoryBridge', () => {
  it('blocks replaceState that changes URL after popstate', async () => {
    installHistoryBridge(window)

    // Navigate to /topic?q=test so there's something to go back from
    history.pushState({}, '', '/topic?q=test')
    history.pushState({}, '', '/123/456')

    // Go back — wait for popstate to fire
    const popstatePromise = new Promise<void>((resolve) => {
      window.addEventListener('popstate', () => resolve(), { once: true })
    })
    history.back()
    await popstatePromise

    // The host SPA (e.g. Vue Router) tries replaceState to /topic.
    // This should be blocked because it differs from the popstate URL.
    history.replaceState({}, '', '/topic')

    // URL should still be /topic?q=test (the popstate URL)
    expect(window.location.pathname + window.location.search).toBe('/topic?q=test')
  })

  it('allows replaceState that keeps the same URL after popstate', async () => {
    installHistoryBridge(window)

    history.pushState({}, '', '/topic?q=test')
    history.pushState({}, '', '/123/456')

    const popstatePromise = new Promise<void>((resolve) => {
      window.addEventListener('popstate', () => resolve(), { once: true })
    })
    history.back()
    await popstatePromise

    // replaceState to the same URL should be allowed
    history.replaceState({ extra: true }, '', '/topic?q=test')
    expect(window.location.pathname + window.location.search).toBe('/topic?q=test')
  })
})
