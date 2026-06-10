import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen } from '@testing-library/react'
import { createRef } from 'react'
import { MemoryRouter } from 'react-router'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import { APP_SETTINGS_STORAGE_KEY } from '@/lib/app-settings'
import { getAppSettingsStore, resetAppSettingsStoreForTest } from '@/lib/app-settings-store'
import { ShellFrame } from '@/lib/weibo/app/app-shell-layout'

const queryClient = new QueryClient({
  defaultOptions: { queries: { retry: false } },
})

vi.mock('@/lib/weibo/components/right-rail', () => ({
  RightRail: () => <div data-testid="right-rail">right rail</div>,
}))

describe('ShellFrame', () => {
  beforeEach(() => {
    Object.defineProperty(globalThis, 'browser', {
      writable: true,
      value: {
        storage: {
          local: {
            get: vi.fn(async () => ({})),
            set: vi.fn(async () => {}),
          },
        },
      },
    })
    Object.defineProperty(window, 'matchMedia', {
      writable: true,
      value: vi.fn().mockImplementation(() => ({
        matches: false,
        media: '',
        onchange: null,
        addListener: vi.fn(),
        removeListener: vi.fn(),
        addEventListener: vi.fn(),
        removeEventListener: vi.fn(),
        dispatchEvent: vi.fn(),
      })),
    })
    resetAppSettingsStoreForTest()
    const store = getAppSettingsStore({
      get: async () => ({ [APP_SETTINGS_STORAGE_KEY]: undefined }),
      set: async () => {},
    })
    store.setState({
      ...store.getState(),
      homeTab: 'for-you',
      isHydrated: true,
    })
  })

  it('renders one navigation landmark and preserves page content', () => {
    const mainRef = createRef<HTMLDivElement>()
    render(
      <QueryClientProvider client={queryClient}>
        <MemoryRouter>
          <ShellFrame
            pageKind="home"
            viewingProfileUserId={null}
            rewriteEnabled
            theme="system"
            contentWidth="standard"
            onRewriteEnabledChange={vi.fn()}
            onThemeChange={vi.fn()}
            onSettingsOpen={vi.fn()}
            onComposeOpen={vi.fn()}
            mainRef={mainRef}
          >
            <div>center content</div>
          </ShellFrame>
        </MemoryRouter>
      </QueryClientProvider>,
    )

    expect(screen.getAllByRole('navigation', { name: '主导航' })).toHaveLength(1)
    expect(screen.getByText('center content')).toBeInTheDocument()
  })
})
