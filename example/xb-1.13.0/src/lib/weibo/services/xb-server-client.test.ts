import { beforeEach, describe, expect, it, vi } from 'vitest'

const getCurrentUserUidMock = vi.fn<() => string | null>()
const signXbServerRequestMock =
  vi.fn<(uid: string, path: string) => Promise<Record<string, string>>>()

vi.mock('@/lib/weibo/platform/current-user', () => ({
  getCurrentUserUid: () => getCurrentUserUidMock(),
}))

vi.mock('@/lib/weibo/services/xb-server-sign', () => ({
  signXbServerRequest: (uid: string, path: string) => signXbServerRequestMock(uid, path),
}))

function mockFetchJson(body: unknown) {
  const fetchMock = vi.fn(async () => {
    return new Response(JSON.stringify(body), {
      status: 200,
      headers: { 'Content-Type': 'application/json' },
    })
  })
  vi.stubGlobal('fetch', fetchMock)
  return fetchMock
}

describe('xb-server-client', () => {
  beforeEach(() => {
    vi.resetModules()
    vi.unstubAllGlobals()
    vi.clearAllMocks()
    getCurrentUserUidMock.mockReturnValue('9999')
    signXbServerRequestMock.mockResolvedValue({
      'X-XB-UID': '9999',
      'X-XB-Timestamp': '1780012800',
      'X-XB-Signature': 'signed',
    })
  })

  it('signs user rating summary reads', async () => {
    const fetchMock = mockFetchJson({
      avg: 8.2,
      count: 3,
      distribution: { 1: 0, 2: 0, 3: 0, 4: 2, 5: 1 },
    })
    const { getUserRatingSummary } = await import('./xb-server-client')

    await getUserRatingSummary('1001')

    expect(signXbServerRequestMock).toHaveBeenCalledWith('9999', '/api/ratings/user/1001')
    expect(fetchMock).toHaveBeenCalledWith(
      expect.stringContaining('/api/ratings/user/1001'),
      expect.objectContaining({
        method: 'GET',
        headers: expect.objectContaining({
          'X-XB-UID': '9999',
          'X-XB-Timestamp': '1780012800',
          'X-XB-Signature': 'signed',
        }),
      }),
    )
  })

  it('signs batch reads and rating writes', async () => {
    const fetchMock = mockFetchJson({ ok: true })
    const { batchGetUserRatingSummaries, rateUser } = await import('./xb-server-client')

    await batchGetUserRatingSummaries(['1001', '1002'])
    await rateUser({ target_uid: '1001', stars: 4 })

    expect(signXbServerRequestMock).toHaveBeenNthCalledWith(1, '9999', '/api/ratings/batch')
    expect(signXbServerRequestMock).toHaveBeenNthCalledWith(2, '9999', '/api/ratings/user')
    expect(fetchMock).toHaveBeenNthCalledWith(
      1,
      expect.stringContaining('/api/ratings/batch'),
      expect.objectContaining({
        method: 'POST',
        headers: expect.objectContaining({ 'X-XB-Signature': 'signed' }),
      }),
    )
    expect(fetchMock).toHaveBeenNthCalledWith(
      2,
      expect.stringContaining('/api/ratings/user'),
      expect.objectContaining({
        method: 'POST',
        headers: expect.objectContaining({ 'X-XB-Signature': 'signed' }),
      }),
    )
  })

  it('does not call the server when the current Weibo UID is unavailable', async () => {
    const fetchMock = mockFetchJson({ ok: true })
    getCurrentUserUidMock.mockReturnValue(null)
    const { getUserRatingSummary } = await import('./xb-server-client')

    await expect(getUserRatingSummary('1001')).rejects.toThrow('xb-rating-not-logged-in')

    expect(signXbServerRequestMock).not.toHaveBeenCalled()
    expect(fetchMock).not.toHaveBeenCalled()
  })
})
