import { Badge } from '@/components/ui/badge'
import { useAppSettings } from '@/lib/app-settings-store'

export function FontPreviewCard() {
  const size = useAppSettings((s) => s.fontSizeClass)
  const weight = useAppSettings((s) => s.fontWeightClass)
  const spacing = useAppSettings((s) => s.letterSpacingClass)
  const height = useAppSettings((s) => s.lineHeightClass)
  const family = useAppSettings((s) => s.fontFamilyClass)

  return (
    <div className="bg-muted/30 rounded-xl p-4">
      <div className="mb-3 flex items-center gap-2.5">
        <div className="bg-muted-foreground/20 size-8 shrink-0 rounded-full" />
        <div className="flex min-w-0 flex-1 items-center gap-1.5">
          <span className="truncate text-sm font-semibold">用户名称</span>
          <Badge variant="secondary">22:00</Badge>
        </div>
      </div>
      <div className={`${size} ${weight} ${spacing} ${height} ${family} mb-3 leading-relaxed`}>
        今天的天气真好，适合出去走走。分享一下最近拍的照片，大家觉得怎么样？
      </div>
    </div>
  )
}
