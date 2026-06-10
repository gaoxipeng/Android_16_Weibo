import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { renderHook, waitFor } from '@testing-library/react'
import type { ReactNode } from 'react'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import type { FeedItem } from '@/lib/weibo/models/feed'
import {
  batchRatingQueryOptions,
  extractFeedAuthorUids,
  myUserRatingQueryKey,
  myUserRatingQueryOptions,
  seedUserRatingSummaries,
  useFeedRatingBatchSync,
  useRateUser,
  userRatingCacheOnlyQueryOptions,
  userRatingQueryKey,
  userRatingQueryOptions,
} from '@/lib/weibo/queries/rating-queries'
import { batchGetUserRatingSummaries, rateUser } from '@/lib/weibo/services/xb-server-client'

vi.mock('@/lib/app-settings-store', () => ({
  useAppSettings: (selector: (state: { ratingEnabled: boolean }) => unknown) =>
    selector({ ratingEnabled: true }),
}))

vi.mock('@/lib/weibo/platform/current-user', () => ({
  getCurrentUserUid: vi.fn(() => '9999'),
}))

vi.mock('@/lib/weibo/services/xb-server-client', () => ({
  batchGetUserRatingSummaries: vi.fn(),
  getMyUserRating: vi.fn(),
  getUserRatingSummary: vi.fn(),
  rateUser: vi.fn(async () => ({ ok: true })),
}))

function createFeedItem(authorId: string, retweetedAuthorId?: string): FeedItem {
  return {
    id: 'status-1',
    mblogId: 'status-1',
    isLongText: false,
    author: { id: authorId, name: 'Alice', avatarUrl: null },
    text: 'hello',
    createdAt: '',
    createdAtLabel: '',
    stats: { likes: 0, comments: 0, reposts: 0 },
    images: [],
    media: null,
    retweetedStatus: retweetedAuthorId
      ? {
          id: 'rt-1',
          mblogId: 'rt-1',
          isLongText: false,
          author: { id: retweetedAuthorId, name: 'Bob', avatarUrl: null },
          text: 'rt',
          createdAt: '',
          createdAtLabel: '',
          stats: { likes: 0, comments: 0, reposts: 0 },
          images: [],
          media: null,
        }
      : undefined,
  }
}

function createQueryClient() {
  return new QueryClient({
    defaultOptions: {
      queries: { retry: false },
      mutations: { retry: false },
    },
  })
}

function createWrapper(queryClient: QueryClient) {
  return function Wrapper({ children }: { children: ReactNode }) {
    return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
  }
}

describe('rating query options', () => {
  it('keeps summaries fresh for one hour and cached for one day', () => {
    const oneHour = 60 * 60 * 1000
    const oneDay = 24 * 60 * 60 * 1000

    expect(userRatingQueryOptions('1001').staleTime).toBe(oneHour)
    expect(userRatingQueryOptions('1001').gcTime).toBe(oneDay)
    expect(userRatingQueryOptions('1001').refetchOnWindowFocus).toBe(false)
    expect(userRatingQueryOptions('1001').refetchOnReconnect).toBe(false)
    expect(userRatingCacheOnlyQueryOptions('1001').enabled).toBe(false)
    expect(batchRatingQueryOptions(['1001']).staleTime).toBe(oneHour)
    expect(batchRatingQueryOptions(['1001']).gcTime).toBe(oneDay)
    expect(myUserRatingQueryOptions('1001').staleTime).toBe(Number.POSITIVE_INFINITY)
    expect(myUserRatingQueryOptions('1001').gcTime).toBe(oneDay)
  })

  it('does not mutate uid arrays when creating batch query keys', () => {
    const uids = ['1002', '1001']

    expect(batchRatingQueryOptions(uids).queryKey).toEqual(['rating', 'batch', '1001', '1002'])
    expect(uids).toEqual(['1002', '1001'])
  })
})

describe('extractFeedAuthorUids', () => {
  it('collects primary and retweeted author ids without duplicates', () => {
    expect(
      extractFeedAuthorUids([
        createFeedItem('1001'),
        createFeedItem('1002', '1003'),
        createFeedItem('1002'),
      ]),
    ).toEqual(['1001', '1002', '1003'])
  })
})

describe('seedUserRatingSummaries', () => {
  it('writes each summary into the per-user query cache', () => {
    const queryClient = createQueryClient()
    seedUserRatingSummaries(queryClient, {
      '1001': { avg: 8, count: 1, distribution: {} },
      '1002': { avg: 6, count: 2, distribution: {} },
    })

    expect(queryClient.getQueryData(userRatingQueryKey('1001'))).toEqual({
      avg: 8,
      count: 1,
      distribution: {},
    })
    expect(queryClient.getQueryData(userRatingQueryKey('1002'))).toEqual({
      avg: 6,
      count: 2,
      distribution: {},
    })
  })
})

describe('useFeedRatingBatchSync', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(batchGetUserRatingSummaries).mockResolvedValue({
      '1001': { avg: 8.2, count: 3, distribution: {} },
    })
  })

  it('batch-fetches each page and seeds per-user summary cache', async () => {
    const queryClient = createQueryClient()
    const pages = [{ items: [createFeedItem('1001'), createFeedItem('1002')] }]

    renderHook(() => useFeedRatingBatchSync(pages), {
      wrapper: createWrapper(queryClient),
    })

    await waitFor(() => {
      expect(vi.mocked(batchGetUserRatingSummaries)).toHaveBeenCalledWith(['1001', '1002'])
    })
    await waitFor(() => {
      expect(queryClient.getQueryData(userRatingQueryKey('1001'))).toEqual({
        avg: 8.2,
        count: 3,
        distribution: {},
      })
    })
  })
})

describe('useRateUser', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('updates only my rating and leaves the public average untouched', async () => {
    const queryClient = createQueryClient()
    const targetUid = '1001'

    queryClient.setQueryData(userRatingQueryKey(targetUid), {
      avg: 8.2,
      count: 3,
      distribution: { 1: 0, 2: 0, 3: 0, 4: 2, 5: 1 },
    })
    queryClient.setQueryData(myUserRatingQueryKey(targetUid), { stars: 4 })

    const invalidateQueries = vi.spyOn(queryClient, 'invalidateQueries')
    const { result } = renderHook(() => useRateUser(), {
      wrapper: createWrapper(queryClient),
    })

    result.current.mutate({ targetUid, stars: 5 })

    await waitFor(() => {
      expect(queryClient.getQueryData(myUserRatingQueryKey(targetUid))).toEqual({ stars: 5 })
    })
    await waitFor(() => {
      expect(vi.mocked(rateUser)).toHaveBeenCalledWith({ target_uid: targetUid, stars: 5 })
    })

    expect(queryClient.getQueryData(userRatingQueryKey(targetUid))).toEqual({
      avg: 8.2,
      count: 3,
      distribution: { 1: 0, 2: 0, 3: 0, 4: 2, 5: 1 },
    })
    expect(invalidateQueries).toHaveBeenCalledWith({
      queryKey: myUserRatingQueryKey(targetUid),
    })
    expect(invalidateQueries).not.toHaveBeenCalledWith({
      queryKey: userRatingQueryKey(targetUid),
    })
  })
})
