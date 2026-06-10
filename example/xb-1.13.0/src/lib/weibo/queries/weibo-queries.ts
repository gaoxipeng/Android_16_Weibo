import type { HotSearchType } from '@/lib/app-settings'
import type { FeedAuthor, TimelinePage, TopicChannel } from '@/lib/weibo/models/feed'
import type { StatusCommentsPage } from '@/lib/weibo/models/status'
import type { WeiboPageDescriptor } from '@/lib/weibo/route/page-descriptor'
import type { ProfileSearchParams } from '@/lib/weibo/route/profile-search-params'
import { PROFILE_SEARCH_FILTER_KEYS } from '@/lib/weibo/route/profile-search-params'
import type { ExploreGroup } from '@/lib/weibo/services/adapters/explore-groups'
import type { UnreadCounts } from '@/lib/weibo/services/weibo-repository'
import { checkUnreadNotifications } from '@/lib/weibo/services/weibo-repository'
import {
  loadExploreGroups,
  loadExploreHot,
  loadFavorites,
  loadFeedComments,
  loadFollowGroups,
  loadFollowedSuperTopics,
  loadGroupTimeline,
  loadHotSearchByType,
  loadHomeTimeline,
  loadLikedStatuses,
  loadNestedComments,
  loadProfileAssignedGroups,
  loadProfileAvailableGroups,
  loadProfilePosts,
  loadProfileSearchPosts,
  loadSearch,
  loadStatusComments,
  loadStatusDetail,
  loadStatusLongText,
  loadTopicSearch,
  type HomeTimelineTab,
} from '@/lib/weibo/services/weibo-repository'

export function flattenInfiniteItems<Item>(pages: Array<{ items: Item[] }> | undefined): Item[] {
  return pages?.flatMap((page) => page.items) ?? []
}

export function profileLookupFromPage(page: WeiboPageDescriptor) {
  if (page.kind !== 'profile') {
    return null
  }

  return page.profileSource === 'u'
    ? ({ uid: page.profileId } as const)
    : ({ screenName: page.profileId } as const)
}

/** Result of checking for new posts on the "following" timeline. */
interface FollowingNewPostsCheck {
  /** Authors of the new posts (deduplicated, up to 5). */
  authors: FeedAuthor[]
  /** Total count of new posts found. */
  count: number
}

export function followingNewPostsCheckOptions(
  seenFirstItemId: string | null,
  groupListId?: string | null,
) {
  return {
    queryKey: ['weibo', 'timeline', 'following', groupListId ?? 'default', 'new-check'] as const,
    queryFn: async () => {
      const page = groupListId
        ? await loadGroupTimeline(groupListId, { cursor: null })
        : await loadHomeTimeline('following')
      if (page.items.length === 0) {
        return null as FollowingNewPostsCheck | null
      }

      const newItems: typeof page.items = []
      for (const item of page.items) {
        if (item.id === seenFirstItemId) break
        newItems.push(item)
      }

      if (newItems.length === 0) {
        return null as FollowingNewPostsCheck | null
      }

      const seenAuthorIds = new Set<string>()
      const authors: FeedAuthor[] = []
      for (const item of newItems) {
        if (seenAuthorIds.has(item.author.id)) continue
        seenAuthorIds.add(item.author.id)
        authors.push(item.author)
        if (authors.length >= 5) break
      }

      return { authors, count: newItems.length } as FollowingNewPostsCheck
    },
    staleTime: 0,
    refetchInterval: 5 * 60 * 1000,
    gcTime: 5 * 60 * 1000,
  }
}

export function homeTimelineInfiniteOptions(
  activeTimelineTab: HomeTimelineTab,
  groupListId?: string | null,
) {
  const useGroupTimeline =
    (activeTimelineTab === 'following' ||
      activeTimelineTab === 'special-follow' ||
      activeTimelineTab === 'friend-circle') &&
    groupListId
  return {
    queryKey: ['weibo', 'timeline', activeTimelineTab, groupListId ?? 'default'] as const,
    queryFn: ({ pageParam }: { pageParam: string | null }) =>
      useGroupTimeline
        ? loadGroupTimeline(groupListId!, { cursor: pageParam })
        : loadHomeTimeline(activeTimelineTab, { cursor: pageParam }),
    initialPageParam: null as string | null,
    getNextPageParam: (lastPage: TimelinePage) => lastPage.nextCursor ?? undefined,
    staleTime: Infinity,
    gcTime: Infinity,
  }
}

export function profilePostsInfiniteOptions(profileId: string) {
  return {
    queryKey: ['weibo', 'profile', 'posts', profileId] as const,
    queryFn: ({ pageParam }: { pageParam: string | null }) =>
      loadProfilePosts(profileId, pageParam ? Number(pageParam) : 1),
    initialPageParam: null as string | null,
    getNextPageParam: (lastPage: TimelinePage) => lastPage.nextCursor ?? undefined,
    staleTime: 30 * 60 * 1000,
  }
}

export function profileSearchInfiniteOptions(profileId: string, params: ProfileSearchParams) {
  return {
    queryKey: [
      'weibo',
      'profile',
      'search',
      profileId,
      params.query,
      params.starttime,
      params.endtime,
      ...PROFILE_SEARCH_FILTER_KEYS.map((key) => (params.filters[key] ? 1 : 0)),
    ] as const,
    queryFn: ({ pageParam }: { pageParam: string | null }) =>
      loadProfileSearchPosts(profileId, params, pageParam ? Number(pageParam) : 1),
    initialPageParam: null as string | null,
    getNextPageParam: (
      lastPage: TimelinePage & { total?: string },
      allPages: Array<TimelinePage & { total?: string }>,
    ) => {
      const loadedCount = allPages.reduce((sum, page) => sum + page.items.length, 0)
      const total = Number(lastPage.total)
      if (Number.isFinite(total) && loadedCount >= total) {
        return undefined
      }
      return lastPage.nextCursor ?? undefined
    },
    staleTime: 30 * 60 * 1000,
  }
}

export function profileAssignedGroupsQueryOptions(uid: string, enabled = true) {
  return {
    queryKey: ['weibo', 'profile', 'groups', uid, 'assigned'] as const,
    queryFn: () => loadProfileAssignedGroups(uid),
    enabled: enabled && uid !== '',
    staleTime: 30 * 1000,
  }
}

export function profileAvailableGroupsQueryOptions(uid: string, enabled = true) {
  return {
    queryKey: ['weibo', 'profile', 'groups', uid, 'available'] as const,
    queryFn: () => loadProfileAvailableGroups(uid),
    enabled: enabled && uid !== '',
    staleTime: 30 * 1000,
  }
}

export function favoritesInfiniteOptions(uid: string) {
  return {
    queryKey: ['weibo', 'favorites', uid] as const,
    queryFn: ({ pageParam }: { pageParam: string | null }) =>
      loadFavorites(uid, { page: pageParam ? Number(pageParam) : 1 }),
    initialPageParam: null as string | null,
    getNextPageParam: (lastPage: TimelinePage) => lastPage.nextCursor ?? undefined,
    staleTime: 30 * 60 * 1000,
  }
}

export function likedStatusesInfiniteOptions(uid: string) {
  return {
    queryKey: ['weibo', 'liked-statuses', uid] as const,
    queryFn: ({ pageParam }: { pageParam: string | null }) =>
      loadLikedStatuses(uid, { page: pageParam ? Number(pageParam) : 1 }),
    initialPageParam: null as string | null,
    getNextPageParam: (lastPage: TimelinePage) => lastPage.nextCursor ?? undefined,
    staleTime: 30 * 60 * 1000,
  }
}

export function statusDetailQueryOptions(statusId: string | null, enabled = true) {
  return {
    queryKey: ['weibo', 'status', statusId ?? ''] as const,
    queryFn: () => loadStatusDetail(statusId!),
    enabled: enabled && statusId !== null && statusId !== '',
  }
}

export function statusCommentsInfiniteOptions(statusId: string, authorId: string, filter?: string) {
  return {
    queryKey: ['weibo', 'status-comments', statusId, filter] as const,
    queryFn: ({ pageParam }: { pageParam: string | null }) =>
      loadStatusComments(statusId, authorId, pageParam, filter),
    initialPageParam: null as string | null,
    getNextPageParam: (lastPage: StatusCommentsPage) => lastPage.nextCursor ?? undefined,
    enabled: statusId !== '' && authorId !== '',
  }
}

export function nestedCommentsQueryOptions(statusId: string, authorUid: string, enabled = true) {
  return {
    queryKey: ['weibo', 'nested-comments', statusId] as const,
    queryFn: () => loadNestedComments(statusId, authorUid),
    enabled: enabled && statusId !== '' && authorUid !== '',
  }
}

export function feedCommentsQueryOptions(statusId: string, authorUid: string, enabled = true) {
  return {
    queryKey: ['weibo', 'feed-comments', statusId] as const,
    queryFn: () => loadFeedComments(statusId, authorUid),
    enabled: enabled && statusId !== '' && authorUid !== '',
  }
}

export function longTextQueryOptions(mblogId: string | null, enabled: boolean) {
  return {
    queryKey: ['weibo', 'longtext', mblogId] as const,
    queryFn: () => loadStatusLongText(mblogId!),
    enabled: enabled && mblogId !== null && mblogId !== '',
    staleTime: 30 * 60 * 1000,
    retry: false,
  }
}

export function hotSearchQueryOptions(type: HotSearchType = 'hot') {
  return {
    queryKey: ['weibo', 'hotsearch', type],
    queryFn: () => loadHotSearchByType(type),
    staleTime: 5 * 60 * 1000,
    refetchInterval: 10 * 60 * 1000,
  }
}

export const followedSuperTopicsQueryOptions = {
  queryKey: ['weibo', 'super-topics', 'followed'] as const,
  queryFn: () => loadFollowedSuperTopics(),
  staleTime: 30 * 60 * 1000,
  gcTime: 60 * 60 * 1000,
}

export function searchQueryOptions(query: string) {
  return {
    queryKey: ['weibo', 'search', query] as const,
    queryFn: () => loadSearch(query),
    enabled: query.trim().length > 0,
    staleTime: 0,
  }
}

export function exploreTimelineInfiniteOptions(group: ExploreGroup) {
  return {
    queryKey: ['weibo', 'explore', group.gid] as const,
    queryFn: ({ pageParam }: { pageParam: string | null }) =>
      loadExploreHot({ cursor: pageParam, groupId: group.gid, containerid: group.containerid }),
    initialPageParam: null as string | null,
    getNextPageParam: (lastPage: TimelinePage) => lastPage.nextCursor ?? undefined,
    staleTime: Infinity,
    gcTime: Infinity,
  }
}

import type { RelationPage } from '@/lib/weibo/models/user-relation'
import { loadFriends } from '@/lib/weibo/services/weibo-repository'

export function friendsInfiniteOptions(uid: string, tab: 'following' | 'fans') {
  return {
    queryKey: ['weibo', 'friends', tab, uid] as const,
    queryFn: ({ pageParam }: { pageParam: number }) =>
      loadFriends(uid, {
        page: pageParam,
        relate: tab === 'fans' ? 'fans' : undefined,
      }),
    initialPageParam: 1,
    getNextPageParam: (lastPage: RelationPage, _allPages: RelationPage[], lastPageParam: number) =>
      lastPage.nextPage != null ? lastPageParam + 1 : undefined,
    staleTime: 30 * 1000,
  }
}

export const exploreGroupsQueryOptions = {
  queryKey: ['weibo', 'explore', 'groups'] as const,
  queryFn: () => loadExploreGroups(),
  staleTime: 60 * 60 * 1000,
}

export const followGroupsQueryOptions = {
  queryKey: ['weibo', 'follow-groups'] as const,
  queryFn: () => loadFollowGroups(),
  staleTime: 60 * 60 * 1000,
}

export function topicSearchInfiniteOptions(topic: string, channelType?: string) {
  return {
    queryKey: ['weibo', 'topic', topic, channelType ?? '1'] as const,
    queryFn: ({ pageParam }: { pageParam: number }) =>
      loadTopicSearch(topic, pageParam, channelType),
    initialPageParam: 1 as number,
    getNextPageParam: (lastPage: TimelinePage, allPages: TimelinePage[]) =>
      lastPage.items.length > 0 ? allPages.length + 1 : undefined,
    staleTime: 5 * 60 * 1000,
  }
}

/** Extract channels from the first page of topic search results. */
export function extractTopicChannels(pages: TimelinePage[] | undefined): TopicChannel[] {
  return pages?.[0]?.channels ?? []
}

/** Extract head data from the first page of topic search results. */
export function extractTopicHeadData(pages: TimelinePage[] | undefined) {
  return pages?.[0]?.headData ?? null
}

// ─── Unread Notifications ───

/** Polls unread notification / DM counts from m.weibo.cn. */
export const unreadNotificationsQueryOptions = {
  queryKey: ['weibo', 'unread'] as const,
  queryFn: (): Promise<UnreadCounts> => checkUnreadNotifications(),
  staleTime: 30 * 1000,
  refetchInterval: 60 * 1000,
  gcTime: 5 * 60 * 1000,
}

/** Whether the notification tab should show a badge. */
export function hasNotificationBadge(counts: UnreadCounts): boolean {
  return counts.mentions + counts.comments + counts.likes > 0
}

/** Whether the DM tab should show a badge. */
export function hasDmBadge(counts: UnreadCounts): boolean {
  return counts.dm > 0
}
