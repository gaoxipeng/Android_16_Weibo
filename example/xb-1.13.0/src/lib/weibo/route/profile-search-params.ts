export const PROFILE_SEARCH_FILTER_KEYS = [
  'hasori',
  'hasret',
  'hastext',
  'haspic',
  'hasvideo',
  'hasmusic',
] as const

export type ProfileSearchFilterKey = (typeof PROFILE_SEARCH_FILTER_KEYS)[number]

export type ProfileSearchFilters = Record<ProfileSearchFilterKey, boolean>

export interface ProfileSearchParams {
  query: string
  starttime: number | null
  endtime: number
  filters: ProfileSearchFilters
}

export interface ProfileSearchUrlState {
  active: boolean
  params: ProfileSearchParams
}

const BEIJING_OFFSET_SECONDS = 8 * 60 * 60
const DAY_SECONDS = 24 * 60 * 60

export const DEFAULT_PROFILE_SEARCH_FILTERS: ProfileSearchFilters = {
  hasori: true,
  hasret: true,
  hastext: true,
  haspic: true,
  hasvideo: true,
  hasmusic: true,
}

function positiveInteger(value: string | null): number | null {
  if (!value) {
    return null
  }

  const parsed = Number(value)
  return Number.isInteger(parsed) && parsed > 0 ? parsed : null
}

function beijingDateParts(date: Date): { year: number; month: number; day: number } {
  const parts = new Intl.DateTimeFormat('en-CA', {
    timeZone: 'Asia/Shanghai',
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  }).formatToParts(date)

  const year = Number(parts.find((part) => part.type === 'year')?.value)
  const month = Number(parts.find((part) => part.type === 'month')?.value)
  const day = Number(parts.find((part) => part.type === 'day')?.value)

  return { year, month, day }
}

export function beijingDateStartTimestamp(date: Date): number {
  return (
    Date.UTC(date.getFullYear(), date.getMonth(), date.getDate()) / 1000 - BEIJING_OFFSET_SECONDS
  )
}

export function beijingInclusiveEndDateTimestamp(date: Date): number {
  return beijingDateStartTimestamp(date) + DAY_SECONDS
}

export function defaultProfileSearchEndtime(now = new Date()): number {
  const { year, month, day } = beijingDateParts(now)
  return Date.UTC(year, month - 1, day + 1) / 1000 - BEIJING_OFFSET_SECONDS
}

export function dateFromBeijingTimestamp(timestamp: number): Date {
  const shifted = new Date((timestamp + BEIJING_OFFSET_SECONDS) * 1000)
  return new Date(shifted.getUTCFullYear(), shifted.getUTCMonth(), shifted.getUTCDate())
}

export function dateFromBeijingInclusiveEndtime(endtime: number): Date {
  return dateFromBeijingTimestamp(Math.max(0, endtime - 1))
}

function parseFilters(searchParams: URLSearchParams): ProfileSearchFilters {
  const filters = { ...DEFAULT_PROFILE_SEARCH_FILTERS }

  for (const key of PROFILE_SEARCH_FILTER_KEYS) {
    const value = searchParams.get(key)
    if (value === '0') {
      filters[key] = false
    } else if (value === '1') {
      filters[key] = true
    }
  }

  return Object.values(filters).some(Boolean) ? filters : { ...DEFAULT_PROFILE_SEARCH_FILTERS }
}

export function parseProfileSearchUrlState(
  searchParams: URLSearchParams,
  now = new Date(),
): ProfileSearchUrlState {
  const endtime = positiveInteger(searchParams.get('endtime')) ?? defaultProfileSearchEndtime(now)

  return {
    active: searchParams.has('q'),
    params: {
      query: searchParams.get('q') ?? '',
      starttime: positiveInteger(searchParams.get('starttime')),
      endtime,
      filters: parseFilters(searchParams),
    },
  }
}

export function writeProfileSearchParams(
  searchParams: URLSearchParams,
  state: ProfileSearchUrlState,
): URLSearchParams {
  const next = new URLSearchParams(searchParams)
  next.delete('q')
  next.delete('starttime')
  next.delete('endtime')
  for (const key of PROFILE_SEARCH_FILTER_KEYS) {
    next.delete(key)
  }

  if (!state.active) {
    return next
  }

  next.set('q', state.params.query)
  next.set('endtime', String(state.params.endtime))

  if (state.params.starttime !== null) {
    next.set('starttime', String(state.params.starttime))
  }

  for (const key of PROFILE_SEARCH_FILTER_KEYS) {
    if (!state.params.filters[key]) {
      next.set(key, '0')
    }
  }

  return next
}

export function profileSearchStateKey(state: ProfileSearchUrlState): string {
  const { params } = state
  return [
    state.active ? 'search' : 'posts',
    params.query,
    params.starttime ?? '',
    params.endtime,
    ...PROFILE_SEARCH_FILTER_KEYS.map((key) => (params.filters[key] ? '1' : '0')),
  ].join('|')
}
