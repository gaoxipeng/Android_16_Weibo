import { describe, expect, it } from 'vitest'

import {
  beijingDateStartTimestamp,
  beijingInclusiveEndDateTimestamp,
  dateFromBeijingInclusiveEndtime,
  dateFromBeijingTimestamp,
  defaultProfileSearchEndtime,
  parseProfileSearchUrlState,
  writeProfileSearchParams,
} from '@/lib/weibo/route/profile-search-params'

describe('profile search params', () => {
  it('uses Beijing date boundaries for profile search dates', () => {
    const start = new Date(2026, 5, 5)
    const end = new Date(2026, 5, 7)

    expect(beijingDateStartTimestamp(start)).toBe(1780588800)
    expect(beijingInclusiveEndDateTimestamp(end)).toBe(1780848000)
    expect(defaultProfileSearchEndtime(new Date('2026-06-07T12:00:00Z'))).toBe(1780848000)
  })

  it('converts Beijing timestamps back to calendar dates', () => {
    expect(dateFromBeijingTimestamp(1780588800)).toEqual(new Date(2026, 5, 5))
    expect(dateFromBeijingInclusiveEndtime(1780848000)).toEqual(new Date(2026, 5, 7))
  })

  it('treats q presence as search mode and fills dynamic defaults', () => {
    const state = parseProfileSearchUrlState(
      new URLSearchParams('q=&haspic=0'),
      new Date('2026-06-07T12:00:00Z'),
    )

    expect(state.active).toBe(true)
    expect(state.params.query).toBe('')
    expect(state.params.endtime).toBe(1780848000)
    expect(state.params.starttime).toBeNull()
    expect(state.params.filters.haspic).toBe(false)
    expect(state.params.filters.hasori).toBe(true)
  })

  it('serializes active search state and omits selected filters', () => {
    const next = writeProfileSearchParams(new URLSearchParams('tab=posts'), {
      active: true,
      params: {
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
      },
    })

    expect(next.toString()).toBe(
      'tab=posts&q=%E7%83%A7%E9%B8%A1&endtime=1780848000&starttime=1780588800&haspic=0',
    )
  })

  it('removes all search params when leaving search mode', () => {
    const next = writeProfileSearchParams(
      new URLSearchParams('q=test&endtime=1780848000&haspic=0&tab=posts'),
      {
        active: false,
        params: parseProfileSearchUrlState(new URLSearchParams()).params,
      },
    )

    expect(next.toString()).toBe('tab=posts')
  })
})
