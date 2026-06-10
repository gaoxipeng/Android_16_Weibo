import { useMutation, useQueries, useQueryClient, type QueryClient } from '@tanstack/react-query'
import { useMemo } from 'react'

import { useAppSettings } from '@/lib/app-settings-store'
import type { FeedItem } from '@/lib/weibo/models/feed'
import type { RatingSummary } from '@/lib/weibo/models/rating'
import { getCurrentUserUid } from '@/lib/weibo/platform/current-user'
import {
  batchGetUserRatingSummaries,
  getMyUserRating,
  getUserRatingSummary,
  rateUser,
} from '@/lib/weibo/services/xb-server-client'

const RATING_SUMMARY_STALE_TIME = 60 * 60 * 1000
const RATING_SUMMARY_CACHE_TIME = 24 * 60 * 60 * 1000
const MY_RATING_CACHE_TIME = 24 * 60 * 60 * 1000

// ─── Query key factories ───

export function userRatingQueryKey(uid: string) {
  return ['rating', 'user', uid] as const
}

export function myUserRatingQueryKey(uid: string) {
  return ['rating', 'user', uid, 'me'] as const
}

export function batchRatingQueryKey(uids: string[]) {
  return ['rating', 'batch', ...[...uids].sort()] as const
}

// ─── Query options ───

export function userRatingQueryOptions(uid: string) {
  return {
    queryKey: userRatingQueryKey(uid),
    queryFn: () => getUserRatingSummary(uid),
    staleTime: RATING_SUMMARY_STALE_TIME,
    gcTime: RATING_SUMMARY_CACHE_TIME,
    refetchOnWindowFocus: false,
    refetchOnReconnect: false,
    enabled: uid !== '',
  }
}

/** Read batch-seeded summary cache only; never hits the single-user API. */
export function userRatingCacheOnlyQueryOptions(uid: string) {
  return {
    ...userRatingQueryOptions(uid),
    enabled: false,
  }
}

export function myUserRatingQueryOptions(uid: string) {
  return {
    queryKey: myUserRatingQueryKey(uid),
    queryFn: () => getMyUserRating(uid),
    staleTime: Number.POSITIVE_INFINITY,
    gcTime: MY_RATING_CACHE_TIME,
    refetchOnWindowFocus: false,
    refetchOnReconnect: false,
    enabled: uid !== '',
  }
}

export function batchRatingQueryOptions(uids: string[]) {
  return {
    queryKey: batchRatingQueryKey(uids),
    queryFn: () => batchGetUserRatingSummaries(uids),
    staleTime: RATING_SUMMARY_STALE_TIME,
    gcTime: RATING_SUMMARY_CACHE_TIME,
    refetchOnWindowFocus: false,
    refetchOnReconnect: false,
    enabled: uids.length > 0,
  }
}

/** Author UIDs from a timeline page (primary + retweeted authors). */
export function extractFeedAuthorUids(items: FeedItem[]): string[] {
  const uids = new Set<string>()
  for (const item of items) {
    if (item.author.id) {
      uids.add(item.author.id)
    }
    const retweetedUid = item.retweetedStatus?.author.id
    if (retweetedUid) {
      uids.add(retweetedUid)
    }
  }
  return [...uids]
}

export function seedUserRatingSummaries(
  queryClient: QueryClient,
  summaries: Record<string, RatingSummary>,
) {
  for (const [uid, summary] of Object.entries(summaries)) {
    queryClient.setQueryData(userRatingQueryKey(uid), summary)
  }
}

/** Batch-fetch rating summaries per feed page and seed per-uid query cache. */
export function useFeedRatingBatchSync(pages: Array<{ items: FeedItem[] }> | undefined) {
  const queryClient = useQueryClient()
  const ratingEnabled = useAppSettings((s) => s.ratingEnabled)
  const loggedIn = getCurrentUserUid() !== null

  const pageUidLists = useMemo(
    () => pages?.map((page) => extractFeedAuthorUids(page.items)) ?? [],
    [pages],
  )

  useQueries({
    queries: pageUidLists.map((uids) => ({
      ...batchRatingQueryOptions(uids),
      enabled: ratingEnabled && loggedIn && uids.length > 0,
      queryFn: async () => {
        const summaries = await batchGetUserRatingSummaries(uids)
        seedUserRatingSummaries(queryClient, summaries)
        return summaries
      },
    })),
  })
}

// ─── Mutations ───

export function useRateUser() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ targetUid, stars }: { targetUid: string; stars: number }) =>
      rateUser({ target_uid: targetUid, stars }),
    onMutate: async ({ targetUid, stars }) => {
      await queryClient.cancelQueries({ queryKey: myUserRatingQueryKey(targetUid) })
      const prevMyRating = queryClient.getQueryData<{ stars: number | null }>(
        myUserRatingQueryKey(targetUid),
      )

      queryClient.setQueryData(myUserRatingQueryKey(targetUid), { stars })

      return { prevMyRating, targetUid }
    },
    onError: (_err, { targetUid }, context) => {
      if (context?.prevMyRating !== undefined) {
        queryClient.setQueryData(myUserRatingQueryKey(targetUid), context.prevMyRating)
        return
      }

      queryClient.removeQueries({ queryKey: myUserRatingQueryKey(targetUid), exact: true })
    },
    onSuccess: (_data, { targetUid }) => {
      void queryClient.invalidateQueries({ queryKey: myUserRatingQueryKey(targetUid) })
    },
  })
}
