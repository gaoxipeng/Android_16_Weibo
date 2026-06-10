import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, fireEvent, render, screen, waitFor, within } from '@testing-library/react'
import type { ReactElement } from 'react'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import {
  RatingPanel,
  RatingSummaryBadge,
  ratingScoreToDisplayStars,
} from '@/lib/weibo/components/rating-panel'
import { userRatingQueryKey } from '@/lib/weibo/queries/rating-queries'
import {
  getMyUserRating,
  getUserRatingSummary,
  rateUser,
} from '@/lib/weibo/services/xb-server-client'

vi.mock('@/lib/weibo/services/xb-server-client', () => ({
  getUserRatingSummary: vi.fn(async () => ({
    avg: 8.2,
    count: 3,
    distribution: { 1: 0, 2: 0, 3: 0, 4: 2, 5: 1 },
  })),
  getMyUserRating: vi.fn(async () => ({ stars: 4 })),
  rateUser: vi.fn(async () => ({ ok: true })),
}))

function renderWithClient(
  ui: ReactElement,
  queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
      mutations: { retry: false },
    },
  }),
) {
  return render(<QueryClientProvider client={queryClient}>{ui}</QueryClientProvider>)
}

describe('ratingScoreToDisplayStars', () => {
  it('rounds 10-point scores up to the nearest half star', () => {
    expect(ratingScoreToDisplayStars(0)).toBe(0)
    expect(ratingScoreToDisplayStars(8)).toBe(4)
    expect(ratingScoreToDisplayStars(8.2)).toBe(4.5)
    expect(ratingScoreToDisplayStars(9.1)).toBe(5)
    expect(ratingScoreToDisplayStars(10)).toBe(5)
  })
})

describe('RatingPanel', () => {
  beforeEach(() => {
    cleanup()
    vi.clearAllMocks()
  })

  it('shows the average score and the current user score in one rating module', async () => {
    renderWithClient(<RatingPanel targetUid="1001" />)

    expect(await screen.findByText('8.2')).toBeInTheDocument()
    expect(screen.getByText('我评')).toBeInTheDocument()
    expect(await screen.findByText('4')).toBeInTheDocument()
    expect(vi.mocked(getUserRatingSummary)).toHaveBeenCalledWith('1001')
    expect(vi.mocked(getMyUserRating)).toHaveBeenCalledWith('1001')
  })

  it('turns the same star area into an integer rating control on hover', async () => {
    const view = renderWithClient(<RatingPanel targetUid="1001" />)

    await within(view.container).findByText('8.2')
    const starArea = view.container.querySelector('[aria-label="rating star control"]')
    expect(starArea).toBeInTheDocument()
    fireEvent.mouseEnter(starArea!)
    fireEvent.keyDown(screen.getByRole('radiogroup', { name: '我的 rating' }), {
      key: 'ArrowRight',
    })

    await waitFor(() => {
      expect(vi.mocked(rateUser)).toHaveBeenCalledWith({ target_uid: '1001', stars: 5 })
    })
    expect(vi.mocked(getMyUserRating)).toHaveBeenCalledWith('1001')
  })
})

describe('RatingSummaryBadge', () => {
  beforeEach(() => {
    cleanup()
    vi.clearAllMocks()
  })

  it('shows only the public average score without fetching my rating', async () => {
    renderWithClient(<RatingSummaryBadge targetUid="1001" />)

    expect(await screen.findByText('8.2')).toBeInTheDocument()
    expect(screen.queryByText('我评')).not.toBeInTheDocument()
    expect(vi.mocked(getUserRatingSummary)).toHaveBeenCalledWith('1001')
    expect(vi.mocked(getMyUserRating)).not.toHaveBeenCalled()
  })

  it('reads batch-seeded cache without calling the single-user API', async () => {
    const queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false },
        mutations: { retry: false },
      },
    })
    queryClient.setQueryData(userRatingQueryKey('1001'), {
      avg: 7.5,
      count: 2,
      distribution: {},
    })

    renderWithClient(<RatingSummaryBadge targetUid="1001" useBatchCache />, queryClient)

    expect(await screen.findByText('7.5')).toBeInTheDocument()
    expect(vi.mocked(getUserRatingSummary)).not.toHaveBeenCalled()
    expect(vi.mocked(getMyUserRating)).not.toHaveBeenCalled()
  })
})
