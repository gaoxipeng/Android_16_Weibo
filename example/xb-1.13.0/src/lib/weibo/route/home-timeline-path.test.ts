import { describe, expect, it } from 'vitest'

import { homeTimelinePathFromTab } from '@/lib/weibo/route/home-timeline-path'

describe('homeTimelinePathFromTab', () => {
  const defaultGroups = {
    specialFollow: { gid: 'special-dynamic', title: '特别关注' },
    friendCircle: { gid: 'friend-dynamic', title: '互相关注' },
  }

  it('uses default follow group gids for special timelines', () => {
    expect(homeTimelinePathFromTab('special-follow', defaultGroups)).toBe(
      '/mygroups?gid=special-dynamic',
    )
    expect(homeTimelinePathFromTab('friend-circle', defaultGroups)).toBe(
      '/mygroups?gid=friend-dynamic',
    )
  })

  it('falls back to mygroups when default group gid is unavailable', () => {
    expect(homeTimelinePathFromTab('special-follow')).toBe('/mygroups')
    expect(homeTimelinePathFromTab('friend-circle')).toBe('/mygroups')
  })
})
