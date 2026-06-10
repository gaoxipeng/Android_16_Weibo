import type { QueryClient, QueryKey } from '@tanstack/react-query'

import type { FeedItem } from '@/lib/weibo/models/feed'
import type { CommentItem } from '@/lib/weibo/models/status'

export interface StatusCacheMutationContext {
  previousItems: Array<[QueryKey, unknown]>
}

type InfinitePagesData<PageItem> = {
  pages: Array<{ items: PageItem[] }>
}

type ItemsData<Item> = {
  items: Item[]
}

function snapshotWeiboQueries(queryClient: QueryClient): StatusCacheMutationContext {
  return {
    previousItems: queryClient.getQueriesData({ queryKey: ['weibo'] }),
  }
}

export function restoreStatusCacheMutation(
  queryClient: QueryClient,
  context: StatusCacheMutationContext | undefined,
) {
  if (!context?.previousItems) {
    return
  }

  for (const [queryKey, data] of context.previousItems) {
    queryClient.setQueryData(queryKey, data)
  }
}

function isObject(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null
}

function hasInfinitePages<Item>(value: unknown): value is InfinitePagesData<Item> {
  return (
    isObject(value) &&
    Array.isArray(value.pages) &&
    value.pages.every((page) => isObject(page) && Array.isArray(page.items))
  )
}

function hasItems<Item>(value: unknown): value is ItemsData<Item> {
  return isObject(value) && Array.isArray(value.items)
}

function isFeedItem(value: unknown): value is FeedItem {
  return isObject(value) && isObject(value.stats) && isObject(value.author)
}

function isCommentItem(value: unknown): value is CommentItem {
  return isObject(value) && typeof value.likeCount === 'number' && isObject(value.author)
}

function updateFeedItemById(
  item: FeedItem,
  statusId: string,
  update: (item: FeedItem) => FeedItem,
): FeedItem {
  if (item.id === statusId) {
    return update(item)
  }

  if (item.retweetedStatus?.id === statusId) {
    return {
      ...item,
      retweetedStatus: update(item.retweetedStatus as FeedItem),
    }
  }

  return item
}

function updateFeedPages(
  queryClient: QueryClient,
  statusId: string,
  update: (item: FeedItem) => FeedItem,
) {
  queryClient.setQueriesData({ queryKey: ['weibo'] }, (old) => {
    if (!hasInfinitePages<unknown>(old)) {
      return old
    }

    return {
      ...old,
      pages: old.pages.map((page) => ({
        ...page,
        items: page.items.map((item) =>
          isFeedItem(item) ? updateFeedItemById(item, statusId, update) : item,
        ),
      })),
    }
  })
}

function updateStatusDetailCaches(
  queryClient: QueryClient,
  statusId: string,
  update: (item: FeedItem) => FeedItem,
) {
  for (const key of queryClient
    .getQueryCache()
    .getAll()
    .map((q) => q.queryKey)) {
    if (!Array.isArray(key) || key[0] !== 'weibo' || key[1] !== 'status') {
      continue
    }

    const cached = queryClient.getQueryData(key)
    if (!isObject(cached) || !isFeedItem(cached.status)) {
      continue
    }

    if (cached.status.id === statusId) {
      queryClient.setQueryData(key, {
        ...cached,
        status: update(cached.status),
      })
    }
  }
}

function updateStatusEverywhere(
  queryClient: QueryClient,
  statusId: string,
  update: (item: FeedItem) => FeedItem,
) {
  updateFeedPages(queryClient, statusId, update)
  updateStatusDetailCaches(queryClient, statusId, update)
}

function removeStatusFromFavorites(queryClient: QueryClient, statusId: string) {
  queryClient.setQueriesData({ queryKey: ['weibo', 'favorites'] }, (old) => {
    if (!hasInfinitePages<unknown>(old)) {
      return old
    }

    return {
      ...old,
      pages: old.pages.map((page) => ({
        ...page,
        items: page.items.filter((item) => !isFeedItem(item) || item.id !== statusId),
      })),
    }
  })
}

function updateCommentInTree(
  comment: CommentItem,
  commentId: string,
  update: (comment: CommentItem) => CommentItem,
): CommentItem {
  if (comment.id === commentId) {
    return update(comment)
  }

  if (comment.comments.length > 0) {
    return {
      ...comment,
      comments: comment.comments.map((child) => updateCommentInTree(child, commentId, update)),
    }
  }

  return comment
}

async function prepareMutation(queryClient: QueryClient): Promise<StatusCacheMutationContext> {
  await queryClient.cancelQueries({ queryKey: ['weibo'] })
  return snapshotWeiboQueries(queryClient)
}

export async function optimisticallyToggleStatusLike(
  queryClient: QueryClient,
  target: FeedItem,
): Promise<StatusCacheMutationContext> {
  const context = await prepareMutation(queryClient)

  updateStatusEverywhere(queryClient, target.id, (item) => ({
    ...item,
    liked: !item.liked,
    stats: {
      ...item.stats,
      likes: item.stats.likes + (item.liked ? -1 : 1),
    },
  }))

  return context
}

export async function optimisticallyToggleStatusFavorite(
  queryClient: QueryClient,
  target: FeedItem,
): Promise<StatusCacheMutationContext> {
  const context = await prepareMutation(queryClient)

  updateStatusEverywhere(queryClient, target.id, (item) => ({
    ...item,
    favorited: !item.favorited,
  }))

  if (target.favorited) {
    removeStatusFromFavorites(queryClient, target.id)
  }

  return context
}

export async function optimisticallyRemoveStatusFromFavorites(
  queryClient: QueryClient,
  statusId: string,
): Promise<StatusCacheMutationContext> {
  const context = await prepareMutation(queryClient)

  updateStatusEverywhere(queryClient, statusId, (item) => ({
    ...item,
    favorited: false,
  }))
  removeStatusFromFavorites(queryClient, statusId)

  return context
}

export async function optimisticallyIncrementStatusComments(
  queryClient: QueryClient,
  statusId: string,
): Promise<StatusCacheMutationContext> {
  const context = await prepareMutation(queryClient)

  updateStatusEverywhere(queryClient, statusId, (item) => ({
    ...item,
    stats: {
      ...item.stats,
      comments: item.stats.comments + 1,
    },
  }))

  return context
}

export async function optimisticallyToggleCommentLike(
  queryClient: QueryClient,
  target: CommentItem,
): Promise<StatusCacheMutationContext> {
  const context = await prepareMutation(queryClient)

  const updateComment = (comment: CommentItem) => ({
    ...comment,
    liked: !comment.liked,
    likeCount: comment.likeCount + (comment.liked ? -1 : 1),
  })

  queryClient.setQueriesData({ queryKey: ['weibo'] }, (old) => {
    if (hasInfinitePages<unknown>(old)) {
      return {
        ...old,
        pages: old.pages.map((page) => ({
          ...page,
          items: page.items.map((item) =>
            isCommentItem(item) ? updateCommentInTree(item, target.id, updateComment) : item,
          ),
        })),
      }
    }

    if (hasItems<unknown>(old)) {
      return {
        ...old,
        items: old.items.map((item) =>
          isCommentItem(item) ? updateCommentInTree(item, target.id, updateComment) : item,
        ),
      }
    }

    return old
  })

  return context
}
