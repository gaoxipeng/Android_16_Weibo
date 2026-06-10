import { Sparkles, Zap } from 'lucide-react'
import type { ReactNode } from 'react'
import { useCallback, useEffect, useLayoutEffect, useRef, useState } from 'react'
import { useLocation, useOutletContext } from 'react-router'

import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import type { AppTheme, ContentWidth } from '@/lib/app-settings'
import { useAppSettings } from '@/lib/app-settings-store'
import { cn } from '@/lib/utils'
import type { AppShellContext } from '@/lib/weibo/app/app-shell'
import { BackToTop } from '@/lib/weibo/components/back-to-top'
import { NavigationRail } from '@/lib/weibo/components/navigation-rail'
import { RightRail } from '@/lib/weibo/components/right-rail'
import {
  PAGE_KINDS_WITH_SCROLL_RESTORATION,
  type WeiboPageDescriptor,
} from '@/lib/weibo/route/page-descriptor'
import { parseWeiboUrl } from '@/lib/weibo/route/parse-weibo-url'

/** Routes whose primary feed scrolls inside ShellFrame `<main>` (timeline + profile posts). */
function mainScrollRestorationKey(pathname: string, search: string): string | null {
  const page = parseWeiboUrl(new URL(`${pathname}${search}`, window.location.origin).href)
  if (PAGE_KINDS_WITH_SCROLL_RESTORATION.has(page.kind)) {
    return `${pathname}${search}`
  }
  return null
}

interface ShellFrameProps {
  pageKind: WeiboPageDescriptor['kind']
  viewingProfileUserId?: string | null
  rewriteEnabled: boolean
  theme: AppTheme
  contentWidth: ContentWidth
  onRewriteEnabledChange: (enabled: boolean) => void
  onThemeChange: (theme: AppTheme) => void
  onSettingsOpen: () => void
  onComposeOpen: () => void
  mainRef: React.RefObject<HTMLDivElement | null>
  children: ReactNode
}

export function useAppShellContext() {
  return useOutletContext<AppShellContext>()
}

export function ShellFrame({
  pageKind,
  viewingProfileUserId,
  rewriteEnabled,
  theme,
  contentWidth,
  onRewriteEnabledChange,
  onThemeChange,
  onSettingsOpen,
  onComposeOpen,
  mainRef,
  children,
}: ShellFrameProps) {
  const location = useLocation()
  const savedMainScrollByRouteRef = useRef<Partial<Record<string, number>>>({})
  const locationRef = useRef(location)
  locationRef.current = location
  const showRightRail = useAppSettings((state) => state.showRightRail)
  const updateSettings = useAppSettings((state) => state.updateSettings)

  const [mainScrollRoot, setMainScrollRoot] = useState<HTMLDivElement | null>(null)
  const assignShellRef = useCallback(
    (node: HTMLDivElement | null) => {
      mainRef.current = node
      setMainScrollRoot((prev) => (prev === node ? prev : node))
    },
    [mainRef],
  )

  useEffect(() => {
    const main = mainRef.current
    if (!main) {
      return
    }
    const onScroll = () => {
      const { pathname, search } = locationRef.current
      const key = mainScrollRestorationKey(pathname, search)
      if (key) {
        savedMainScrollByRouteRef.current[key] = main.scrollTop
      }
    }
    main.addEventListener('scroll', onScroll, { passive: true })
    return () => main.removeEventListener('scroll', onScroll)
  }, [mainRef])

  useLayoutEffect(() => {
    const main = mainRef.current
    const key = mainScrollRestorationKey(location.pathname, location.search)
    if (!main || !key) {
      return
    }
    const y = savedMainScrollByRouteRef.current[key] ?? 0
    main.scrollTop = y
  }, [location.pathname, location.search, mainRef])

  const contentWidthClass: Record<ContentWidth, string> = {
    standard: 'lg:max-w-[1000px] xl:max-w-[1200px]',
    wide: 'lg:max-w-[1100px] xl:max-w-[1300px]',
    wider: 'lg:max-w-[1200px] xl:max-w-[1400px]',
  }

  return (
    <div
      className="bg-background text-foreground flex h-screen flex-col overflow-y-auto"
      ref={assignShellRef}
    >
      <div
        className={cn('relative mx-auto flex w-full gap-3 px-3', contentWidthClass[contentWidth])}
      >
        <div className="sticky top-0 h-screen shrink-0">
          <NavigationRail
            pageKind={pageKind}
            viewingProfileUserId={viewingProfileUserId}
            rewriteEnabled={rewriteEnabled}
            theme={theme}
            onRewriteEnabledChange={onRewriteEnabledChange}
            onThemeChange={onThemeChange}
            onSettingsOpen={onSettingsOpen}
            onComposeOpen={onComposeOpen}
            onSidebarCollapsedChange={(collapsed) =>
              updateSettings({ sidebarCollapsed: collapsed })
            }
          />
        </div>
        <main className="min-w-0 flex-1 pb-8">{children}</main>
        {showRightRail && (
          <div className={cn('sticky top-0 h-screen shrink-0 w-[260px]')}>
            <RightRail />
            <BackToTop scrollRoot={mainScrollRoot} />
          </div>
        )}
      </div>
    </div>
  )
}

export function RewritePausedCard({ onResume }: { onResume: () => void }) {
  return (
    <div className="fixed bottom-4 left-4 z-2147483647">
      <Card className="bg-card/95 w-40 shadow-lg shadow-black/5 backdrop-blur md:w-60 lg:w-60">
        <CardHeader>
          <CardTitle className="flex items-center gap-2 text-base">
            <Sparkles className="text-primary h-4 w-4" />
            xb
          </CardTitle>
          <CardDescription>一键切换「更清爽、更 X 的」超级体验</CardDescription>
        </CardHeader>
        <CardContent className="flex flex-col gap-3">
          <Button onClick={onResume} className="justify-between">
            <span>Let's xb!</span>
            <Zap className="h-4 w-4" />
          </Button>
        </CardContent>
      </Card>
    </div>
  )
}
