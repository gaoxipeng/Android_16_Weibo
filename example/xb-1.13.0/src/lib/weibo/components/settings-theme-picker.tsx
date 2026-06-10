import { ArrowLeft, Pencil, Plus, Trash2 } from 'lucide-react'
import { useEffect, useRef, useState } from 'react'

import { Button } from '@/components/ui/button'
import { Label } from '@/components/ui/label'
import { Separator } from '@/components/ui/separator'
import { Textarea } from '@/components/ui/textarea'
import type { SelectedThemeType, UserTheme } from '@/lib/app-settings'
import { CUSTOM_THEME_PRESETS } from '@/lib/custom-theme'
import { cn } from '@/lib/utils'

import { ThemeUiPreview } from './theme-ui-preview'

function ThemeOptionCard({
  name,
  description,
  lightCss,
  darkCss,
  selected,
  onSelect,
  onEdit,
  onDelete,
}: {
  name: string
  description?: string
  lightCss: string
  darkCss: string
  selected: boolean
  onSelect: () => void
  onEdit?: () => void
  onDelete?: () => void
}) {
  return (
    <div className="group relative">
      <button
        type="button"
        role="radio"
        aria-checked={selected}
        onClick={onSelect}
        className={cn(
          'border-border bg-background hover:bg-accent/30 flex w-full flex-col gap-2 rounded-lg border p-2 text-left transition-[box-shadow,background-color,border-color]',
          selected && 'border-primary ring-primary/30 ring-2',
        )}
      >
        <ThemeUiPreview lightCss={lightCss} darkCss={darkCss} />
        <div className="min-w-0 px-0.5">
          <p className="truncate text-sm font-medium">{name}</p>
          {description && <p className="text-muted-foreground truncate text-xs">{description}</p>}
        </div>
      </button>

      {(onEdit || onDelete) && (
        <div className="pointer-events-none absolute top-1.5 right-1.5 flex gap-0.5 opacity-0 transition-opacity group-focus-within:pointer-events-auto group-focus-within:opacity-100 group-hover:pointer-events-auto group-hover:opacity-100">
          {onEdit && (
            <Button
              type="button"
              variant="secondary"
              size="icon"
              className="size-6 shadow-sm"
              aria-label={`编辑 ${name}`}
              onClick={(event) => {
                event.stopPropagation()
                onEdit()
              }}
            >
              <Pencil size={12} />
            </Button>
          )}
          {onDelete && (
            <Button
              type="button"
              variant="secondary"
              size="icon"
              className="text-muted-foreground hover:text-destructive size-6 shadow-sm"
              aria-label={`删除 ${name}`}
              onClick={(event) => {
                event.stopPropagation()
                onDelete()
              }}
            >
              <Trash2 size={12} />
            </Button>
          )}
        </div>
      )}
    </div>
  )
}

function StackedField({
  label,
  description,
  children,
}: {
  label: string
  description?: string
  children: React.ReactNode
}) {
  return (
    <div className="flex flex-col gap-2 py-[11px] first:pt-0 last:pb-0">
      <div className="flex min-w-0 flex-col gap-0.5">
        <Label className="text-sm leading-snug font-medium">{label}</Label>
        {description && (
          <span className="text-muted-foreground text-xs leading-relaxed">{description}</span>
        )}
      </div>
      {children}
    </div>
  )
}

interface SettingsThemePickerProps {
  scrollContainerRef?: React.RefObject<HTMLElement | null>
  selectedThemeType: SelectedThemeType
  selectedThemeId: string
  userThemes: UserTheme[]
  onSelectPreset: (presetKey: string) => void
  onSelectUserTheme: (themeId: string) => void
  onAddCustomTheme: () => string
  onDeleteUserTheme: (themeId: string) => void
  onUpdateUserTheme: (
    themeId: string,
    patch: Partial<Pick<UserTheme, 'name' | 'lightCss' | 'darkCss'>>,
  ) => void
  onLightCssChange: (value: string) => void
  onDarkCssChange: (value: string) => void
}

export function SettingsThemePicker({
  scrollContainerRef,
  selectedThemeType,
  selectedThemeId,
  userThemes,
  onSelectPreset,
  onSelectUserTheme,
  onAddCustomTheme,
  onDeleteUserTheme,
  onUpdateUserTheme,
  onLightCssChange,
  onDarkCssChange,
}: SettingsThemePickerProps) {
  const [view, setView] = useState<'list' | 'edit'>('list')
  const [editingThemeId, setEditingThemeId] = useState<string | null>(null)
  const [themeNameInput, setThemeNameInput] = useState('')
  const [editLightCss, setEditLightCss] = useState('')
  const [editDarkCss, setEditDarkCss] = useState('')
  const editTopRef = useRef<HTMLDivElement>(null)

  const editingTheme =
    editingThemeId === null ? null : userThemes.find((theme) => theme.id === editingThemeId)

  const isEditingActiveTheme =
    selectedThemeType === 'custom' && editingThemeId !== null && selectedThemeId === editingThemeId

  useEffect(() => {
    if (view === 'edit' && editingTheme) {
      setThemeNameInput(editingTheme.name)
      setEditLightCss(editingTheme.lightCss)
      setEditDarkCss(editingTheme.darkCss)
    }
  }, [view, editingTheme])

  useEffect(() => {
    if (view !== 'edit') {
      return
    }

    scrollContainerRef?.current?.scrollTo({ top: 0, behavior: 'instant' })
    editTopRef.current?.scrollIntoView({ block: 'start', behavior: 'instant' })
  }, [view, editingThemeId, scrollContainerRef])

  function openEditView(themeId: string) {
    const theme = userThemes.find((item) => item.id === themeId)
    if (!theme) {
      return
    }

    setEditingThemeId(themeId)
    setThemeNameInput(theme.name)
    setEditLightCss(theme.lightCss)
    setEditDarkCss(theme.darkCss)
    setView('edit')
  }

  function handleEditLightCssChange(value: string) {
    if (!editingThemeId) {
      return
    }

    setEditLightCss(value)
    onUpdateUserTheme(editingThemeId, { lightCss: value })
    if (isEditingActiveTheme) {
      onLightCssChange(value)
    }
  }

  function handleEditDarkCssChange(value: string) {
    if (!editingThemeId) {
      return
    }

    setEditDarkCss(value)
    onUpdateUserTheme(editingThemeId, { darkCss: value })
    if (isEditingActiveTheme) {
      onDarkCssChange(value)
    }
  }

  function handleAddTheme() {
    const id = onAddCustomTheme()
    openEditView(id)
  }

  function handleDeleteTheme(themeId: string) {
    onDeleteUserTheme(themeId)
    if (editingThemeId === themeId) {
      setView('list')
      setEditingThemeId(null)
    }
  }

  function handleSaveThemeName() {
    if (editingThemeId && themeNameInput.trim().length > 0) {
      onUpdateUserTheme(editingThemeId, { name: themeNameInput.trim() })
    }
  }

  if (view === 'edit' && editingTheme) {
    return (
      <div ref={editTopRef} className="flex flex-col gap-4 px-6 py-4">
        <div className="flex items-center gap-2">
          <Button
            type="button"
            variant="ghost"
            size="sm"
            className="text-muted-foreground -ml-2 h-8 px-2"
            onClick={() => {
              setView('list')
              setEditingThemeId(null)
            }}
          >
            <ArrowLeft size={16} />
            返回主题列表
          </Button>
        </div>

        <div className="flex items-center gap-2">
          <Label className="shrink-0 text-sm leading-snug font-medium">主题名称</Label>
          <input
            type="text"
            value={themeNameInput}
            onChange={(event) => setThemeNameInput(event.target.value)}
            className="border-input focus-visible:border-ring focus-visible:ring-ring/50 flex h-9 w-full rounded-md border bg-transparent px-3 py-1 text-sm shadow-xs transition-[color,box-shadow] outline-none focus-visible:ring-[3px]"
          />
          <Button
            variant="default"
            size="sm"
            disabled={themeNameInput.trim() === editingTheme.name}
            onClick={handleSaveThemeName}
          >
            保存
          </Button>
        </div>

        <StackedField
          label="浅色主题样式变量"
          description="每行输入一个 CSS 变量声明，例如 --foreground: #333333;"
        >
          <Textarea
            value={editLightCss}
            onChange={(event) => {
              handleEditLightCssChange(event.target.value)
            }}
            rows={10}
            spellCheck={false}
            className="min-h-[180px] resize-none font-mono text-xs leading-relaxed"
            placeholder="--background: oklch(1.0000 0 0);
--foreground: #333333;"
          />
        </StackedField>

        <StackedField
          label="深色主题样式变量"
          description="每行输入一个 CSS 变量声明，例如 --background: #000000;"
        >
          <Textarea
            value={editDarkCss}
            onChange={(event) => {
              handleEditDarkCssChange(event.target.value)
            }}
            rows={10}
            spellCheck={false}
            className="min-h-[180px] resize-none font-mono text-xs leading-relaxed"
            placeholder="--background: oklch(0.1450 0 0);
--foreground: #ffffff;"
          />
        </StackedField>
      </div>
    )
  }

  return (
    <div className="flex flex-col gap-4 px-6 py-4" role="radiogroup" aria-label="主题选择">
      <div className="flex flex-col gap-3">
        <Label className="text-sm font-medium">默认主题</Label>
        <div className="grid grid-cols-2 gap-2.5 sm:grid-cols-3">
          {CUSTOM_THEME_PRESETS.map((preset) => (
            <ThemeOptionCard
              key={preset.key}
              name={preset.name}
              description={preset.description}
              lightCss={preset.lightCss}
              darkCss={preset.darkCss}
              selected={selectedThemeType === 'preset' && selectedThemeId === preset.key}
              onSelect={() => onSelectPreset(preset.key)}
            />
          ))}
        </div>
      </div>

      <Separator />

      <div className="flex flex-col gap-3">
        <div className="flex items-center justify-between gap-3">
          <Label className="text-sm font-medium">自定义主题</Label>
          <Button variant="secondary" size="sm" onClick={handleAddTheme}>
            <Plus size={14} />
            添加
          </Button>
        </div>

        {userThemes.length === 0 ? (
          <p className="text-muted-foreground rounded-lg border border-dashed px-4 py-6 text-center text-xs">
            还没有自定义主题，点击添加创建你的配色方案
          </p>
        ) : (
          <div className="grid grid-cols-2 gap-2.5 sm:grid-cols-3">
            {userThemes.map((theme) => (
              <ThemeOptionCard
                key={theme.id}
                name={theme.name}
                lightCss={theme.lightCss}
                darkCss={theme.darkCss}
                selected={selectedThemeType === 'custom' && selectedThemeId === theme.id}
                onSelect={() => onSelectUserTheme(theme.id)}
                onEdit={() => openEditView(theme.id)}
                onDelete={() => handleDeleteTheme(theme.id)}
              />
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
