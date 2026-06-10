import { createHmac } from 'node:crypto'

import { afterEach, describe, expect, it, vi } from 'vitest'

describe('signXbServerRequest', () => {
  afterEach(() => {
    vi.useRealTimers()
    vi.unstubAllEnvs()
    vi.resetModules()
  })

  it('adds the HMAC signature header when a signing secret is configured', async () => {
    vi.stubEnv('VITE_XB_SIGN_SECRET', 'test-secret')
    vi.useFakeTimers()
    vi.setSystemTime(new Date('2026-05-29T00:00:00Z'))

    const { signXbServerRequest } = await import('./xb-server-sign')
    const headers = await signXbServerRequest('6393557498', '/api/ratings/user/6393557498/me')
    const expectedPayload = '1780012800.6393557498./api/ratings/user/6393557498/me'
    const expectedSignature = createHmac('sha256', 'test-secret')
      .update(expectedPayload)
      .digest('hex')

    expect(headers).toEqual({
      'X-XB-UID': '6393557498',
      'X-XB-Timestamp': '1780012800',
      'X-XB-Signature': expectedSignature,
    })
  })
})
