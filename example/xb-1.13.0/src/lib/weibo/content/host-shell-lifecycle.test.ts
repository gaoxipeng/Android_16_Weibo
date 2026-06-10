import { describe, expect, it } from 'vitest'

import { shouldRedirectInitialHomePage } from '@/lib/weibo/content/host-shell-lifecycle'

describe('shouldRedirectInitialHomePage', () => {
  it('does not redirect the default for-you home tab back to itself', () => {
    expect(
      shouldRedirectInitialHomePage({
        firstLoadRedirect: 'for-you',
        historyLength: 1,
        pathname: '/',
      }),
    ).toBe(false)
  })

  it('redirects first-load home visits for non-default tabs', () => {
    expect(
      shouldRedirectInitialHomePage({
        firstLoadRedirect: 'following',
        historyLength: 1,
        pathname: '/',
      }),
    ).toBe(true)
  })

  it('does not redirect once the user has navigation history', () => {
    expect(
      shouldRedirectInitialHomePage({
        firstLoadRedirect: 'following',
        historyLength: 3,
        pathname: '/',
      }),
    ).toBe(false)
  })
})
