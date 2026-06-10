import { describe, expect, it } from 'vitest'

import {
  longTextQueryOptions,
  nestedCommentsQueryOptions,
  profileSearchInfiniteOptions,
  statusCommentsInfiniteOptions,
  statusDetailQueryOptions,
} from '@/lib/weibo/queries/weibo-queries'

describe('weibo query options', () => {
  it('centralizes status detail query key and enabled semantics', () => {
    expect(statusDetailQueryOptions('abc').queryKey).toEqual(['weibo', 'status', 'abc'])
    expect(statusDetailQueryOptions('abc').enabled).toBe(true)
    expect(statusDetailQueryOptions(null).enabled).toBe(false)
    expect(statusDetailQueryOptions('abc', false).enabled).toBe(false)
  })

  it('centralizes status comments pagination semantics', () => {
    const options = statusCommentsInfiniteOptions('status-1', 'author-1', 'flow=1')

    expect(options.queryKey).toEqual(['weibo', 'status-comments', 'status-1', 'flow=1'])
    expect(options.initialPageParam).toBeNull()
    expect(options.getNextPageParam({ items: [], nextCursor: 'next' })).toBe('next')
    expect(options.getNextPageParam({ items: [], nextCursor: null })).toBeUndefined()
  })

  it('centralizes nested comments enabled semantics', () => {
    expect(nestedCommentsQueryOptions('comment-1', 'author-1', true).queryKey).toEqual([
      'weibo',
      'nested-comments',
      'comment-1',
    ])
    expect(nestedCommentsQueryOptions('comment-1', 'author-1', true).enabled).toBe(true)
    expect(nestedCommentsQueryOptions('', 'author-1', true).enabled).toBe(false)
    expect(nestedCommentsQueryOptions('comment-1', '', true).enabled).toBe(false)
    expect(nestedCommentsQueryOptions('comment-1', 'author-1', false).enabled).toBe(false)
  })

  it('centralizes long text cache semantics', () => {
    const options = longTextQueryOptions('mblog-1', true)

    expect(options.queryKey).toEqual(['weibo', 'longtext', 'mblog-1'])
    expect(options.enabled).toBe(true)
    expect(options.staleTime).toBe(30 * 60 * 1000)
    expect(options.retry).toBe(false)
    expect(longTextQueryOptions(null, true).enabled).toBe(false)
    expect(longTextQueryOptions('mblog-1', false).enabled).toBe(false)
  })

  it('centralizes profile search cache and pagination semantics', () => {
    const options = profileSearchInfiniteOptions('1783497251', {
      query: '烧鸡',
      starttime: 1780588800,
      endtime: 1780848000,
      filters: {
        hasori: true,
        hasret: true,
        hastext: true,
        haspic: false,
        hasvideo: true,
        hasmusic: true,
      },
    })

    expect(options.queryKey).toEqual([
      'weibo',
      'profile',
      'search',
      '1783497251',
      '烧鸡',
      1780588800,
      1780848000,
      1,
      1,
      1,
      0,
      1,
      1,
    ])
    expect(
      options.getNextPageParam({ items: [{ id: '2' } as never], nextCursor: '2', total: '2' }, [
        { items: [{ id: '1' } as never], nextCursor: '2', total: '2' },
        { items: [{ id: '2' } as never], nextCursor: '3', total: '2' },
      ]),
    ).toBeUndefined()
    expect(
      options.getNextPageParam({ items: [{ id: '1' } as never], nextCursor: '2', total: '3' }, [
        { items: [{ id: '1' } as never], nextCursor: '2', total: '3' },
      ]),
    ).toBe('2')
  })
})
