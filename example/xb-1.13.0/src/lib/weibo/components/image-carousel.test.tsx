import { fireEvent, render, screen, within } from '@testing-library/react'
import type { ReactNode } from 'react'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import { APP_SETTINGS_STORAGE_KEY } from '@/lib/app-settings'
import { getAppSettingsStore, resetAppSettingsStoreForTest } from '@/lib/app-settings-store'
import { ImageCarousel } from '@/lib/weibo/components/image-carousel'

vi.mock('react-photo-view', () => ({
  PhotoProvider: ({ children }: { children: ReactNode }) => <div>{children}</div>,
  PhotoView: ({
    children,
    src,
    render: renderPhoto,
  }: {
    children: ReactNode
    src?: string
    render?: (params: {
      attrs: {
        style: Record<string, never>
      }
      scale: number
    }) => ReactNode
  }) => (
    <div data-testid={renderPhoto ? 'photo-render' : 'photo-view'} data-src={src}>
      {children}
      {renderPhoto
        ? renderPhoto({
            attrs: {
              style: {},
            },
            scale: 1,
          })
        : null}
    </div>
  ),
}))

describe('ImageCarousel', () => {
  beforeEach(() => {
    vi.clearAllMocks()
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
    resetAppSettingsStoreForTest()
    const store = getAppSettingsStore({
      get: async () => ({ [APP_SETTINGS_STORAGE_KEY]: undefined }),
      set: async () => {},
    })
    store.setState({
      ...store.getState(),
      isHydrated: true,
    })
  })

  it('uses default PhotoView for long images so drag-to-pan works in the lightbox', () => {
    render(
      <ImageCarousel
        images={[
          {
            id: 'long-pic',
            thumbnailUrl: 'https://example.com/thumb.jpg',
            largeUrl: 'https://example.com/large.jpg',
            width: 400,
            height: 2000,
          },
        ]}
      />,
    )

    expect(screen.getByText('长图')).toBeInTheDocument()
    expect(screen.getByTestId('photo-view')).toHaveAttribute(
      'data-src',
      'https://example.com/large.jpg',
    )
    expect(screen.queryByTestId('photo-render')).not.toBeInTheDocument()
  })

  it('auto-plays the live photo in the lightbox and switches to replay when video ends', () => {
    render(
      <ImageCarousel
        images={[
          {
            id: 'live-pic',
            thumbnailUrl: 'https://example.com/thumb.jpg',
            largeUrl: 'https://example.com/large.jpg',
            type: 'livephoto',
            livePhotoVideoUrl: 'https://example.com/live.mov',
          },
        ]}
      />,
    )

    // 缩略图上的 Live 徽章
    expect(screen.getAllByText('Live').length).toBeGreaterThan(0)

    const preview = screen.getByTestId('photo-render')

    // 初次进入大图,视频自动播放(autoplay/muted/playsinline,无 controls)
    const video = preview.querySelector('video')
    expect(video).toBeInTheDocument()
    expect(video).toHaveAttribute('src', 'https://example.com/live.mov')
    expect(video).toHaveAttribute('poster', 'https://example.com/large.jpg')
    expect(video).not.toHaveAttribute('controls')
    expect(within(preview).getByRole('button', { name: '正在播放 Live Photo' })).toBeInTheDocument()

    // 播放结束 -> 切换到静图 + 重播按钮
    fireEvent.ended(video!)
    expect(preview.querySelector('video')).not.toBeInTheDocument()
    expect(preview.querySelector('img')).toBeInTheDocument()
    const replayButton = within(preview).getByRole('button', { name: '重新播放 Live Photo' })
    expect(replayButton).toBeInTheDocument()

    // 点击重播按钮 -> 视频再次出现
    fireEvent.click(replayButton)
    const replayedVideo = preview.querySelector('video')
    expect(replayedVideo).toBeInTheDocument()
    expect(replayedVideo).toHaveAttribute('src', 'https://example.com/live.mov')
  })
})
