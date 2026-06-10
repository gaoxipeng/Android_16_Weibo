import { format } from 'date-fns'
import { zhCN } from 'date-fns/locale'
import { CalendarDays, ChevronDown, Search, X } from 'lucide-react'
import { useState } from 'react'
import type { DateRange } from 'react-day-picker'

import { Button } from '@/components/ui/button'
import { Calendar } from '@/components/ui/calendar'
import {
  DropdownMenu,
  DropdownMenuCheckboxItem,
  DropdownMenuContent,
  DropdownMenuGroup,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { Input } from '@/components/ui/input'
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover'
import { cn } from '@/lib/utils'
import {
  beijingDateStartTimestamp,
  beijingInclusiveEndDateTimestamp,
  dateFromBeijingInclusiveEndtime,
  dateFromBeijingTimestamp,
  defaultProfileSearchEndtime,
  DEFAULT_PROFILE_SEARCH_FILTERS,
  type ProfileSearchFilterKey,
  PROFILE_SEARCH_FILTER_KEYS,
  type ProfileSearchFilters,
  type ProfileSearchParams,
  type ProfileSearchUrlState,
} from '@/lib/weibo/route/profile-search-params'

const FILTER_LABELS: Record<ProfileSearchFilterKey, string> = {
  hasori: '原创',
  hasret: '转发',
  hastext: '文字',
  haspic: '图片',
  hasvideo: '视频',
  hasmusic: '音乐',
}

function formatDateLabel(date: Date): string {
  return format(date, 'M月d日', { locale: zhCN })
}

function selectedFilterCount(filters: ProfileSearchFilters): number {
  return PROFILE_SEARCH_FILTER_KEYS.filter((key) => filters[key]).length
}

function filterButtonLabel(filters: ProfileSearchFilters): string {
  const count = selectedFilterCount(filters)
  return count === PROFILE_SEARCH_FILTER_KEYS.length ? '全部类型' : `${count} 项类型`
}

function resultLabel(isSearching: boolean, resultTotal: string | undefined): string {
  if (isSearching) {
    return '检索中'
  }

  if (resultTotal) {
    return `已检索 ${resultTotal} 条`
  }

  return '检索结果'
}

function getInitialDateRange(params: ProfileSearchParams): {
  hasStartDate: boolean
  range: DateRange
} {
  const endDate = dateFromBeijingInclusiveEndtime(params.endtime)
  const startDate =
    params.starttime === null ? undefined : dateFromBeijingTimestamp(params.starttime)

  return {
    hasStartDate: Boolean(startDate),
    range: {
      from: startDate ?? endDate,
      to: endDate,
    },
  }
}

function dateRangeLabel(hasStartDate: boolean, range: DateRange): string {
  const endDate =
    range.to ?? range.from ?? dateFromBeijingInclusiveEndtime(defaultProfileSearchEndtime())

  if (hasStartDate && range.from) {
    return `${formatDateLabel(range.from)} - ${formatDateLabel(endDate)}`
  }

  const today = dateFromBeijingInclusiveEndtime(defaultProfileSearchEndtime())
  return endDate.toDateString() === today.toDateString()
    ? '截止今天'
    : `截止 ${formatDateLabel(endDate)}`
}

interface ProfileSearchBarProps {
  state: ProfileSearchUrlState
  resultTotal?: string
  isSearching?: boolean
  onSubmit: (params: ProfileSearchParams) => void
  onClear: () => void
}

export function ProfileSearchBar({
  state,
  resultTotal,
  isSearching = false,
  onSubmit,
  onClear,
}: ProfileSearchBarProps) {
  const initialDateRange = getInitialDateRange(state.params)
  const [query, setQuery] = useState(state.params.query)
  const [filters, setFilters] = useState<ProfileSearchFilters>(state.params.filters)
  const [hasStartDate, setHasStartDate] = useState(initialDateRange.hasStartDate)
  const [dateRange, setDateRange] = useState<DateRange>(initialDateRange.range)
  const [datePickerOpen, setDatePickerOpen] = useState(false)
  const count = selectedFilterCount(filters)

  const submitSearch = () => {
    const endDate =
      dateRange.to ??
      dateRange.from ??
      dateFromBeijingInclusiveEndtime(defaultProfileSearchEndtime())
    onSubmit({
      query,
      starttime: hasStartDate && dateRange.from ? beijingDateStartTimestamp(dateRange.from) : null,
      endtime: beijingInclusiveEndDateTimestamp(endDate),
      filters,
    })
  }

  const toggleFilter = (key: ProfileSearchFilterKey, checked: boolean) => {
    if (!checked && filters[key] && count <= 1) {
      return
    }

    setFilters((current) => ({
      ...current,
      [key]: checked,
    }))
  }

  const resetFilters = () => {
    setFilters({ ...DEFAULT_PROFILE_SEARCH_FILTERS })
  }

  return (
    <form
      className={cn(
        'border-border/70 bg-card flex flex-col gap-2 rounded-lg border p-2',
        state.active && 'bg-muted/35 border-foreground/15',
      )}
      onSubmit={(event) => {
        event.preventDefault()
        submitSearch()
      }}
    >
      <div className="grid gap-2 lg:grid-cols-[minmax(0,1fr)_auto]">
        <div className="bg-background border-border/60 focus-within:border-ring/60 focus-within:ring-ring/20 relative min-w-0 rounded-md border transition-[border-color,box-shadow] focus-within:ring-[3px]">
          <Search className="text-muted-foreground pointer-events-none absolute top-1/2 left-3 size-4 -translate-y-1/2" />
          <Input
            value={query}
            onChange={(event) => setQuery(event.target.value)}
            placeholder="搜索 TA 的微博"
            aria-label="搜索 TA 的微博"
            className="h-10 border-0 pr-3 pl-9 shadow-none focus-visible:ring-0"
          />
        </div>
        <div className="flex shrink-0 flex-wrap gap-1 p-1">
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button type="button" variant="secondary" size="sm" aria-label="筛选微博类型">
                <span>{filterButtonLabel(filters)}</span>
                <ChevronDown data-icon="inline-end" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end" className="w-36">
              <DropdownMenuGroup>
                {PROFILE_SEARCH_FILTER_KEYS.map((key) => (
                  <DropdownMenuCheckboxItem
                    key={key}
                    checked={filters[key]}
                    disabled={filters[key] && count <= 1}
                    onCheckedChange={(checked) => toggleFilter(key, checked === true)}
                    onSelect={(event) => event.preventDefault()}
                  >
                    {FILTER_LABELS[key]}
                  </DropdownMenuCheckboxItem>
                ))}
              </DropdownMenuGroup>
              <DropdownMenuGroup>
                <DropdownMenuCheckboxItem
                  checked={count === PROFILE_SEARCH_FILTER_KEYS.length}
                  onCheckedChange={(checked) => {
                    if (checked) {
                      resetFilters()
                    }
                  }}
                  onSelect={(event) => event.preventDefault()}
                >
                  全选
                </DropdownMenuCheckboxItem>
              </DropdownMenuGroup>
            </DropdownMenuContent>
          </DropdownMenu>

          <Popover open={datePickerOpen} onOpenChange={setDatePickerOpen}>
            <PopoverTrigger asChild>
              <Button type="button" variant="secondary" size="sm" aria-label="选择搜索日期范围">
                <CalendarDays data-icon="inline-start" />
                <span>{dateRangeLabel(hasStartDate, dateRange)}</span>
              </Button>
            </PopoverTrigger>
            <PopoverContent align="end" className="w-auto p-2">
              <div className="flex flex-col gap-2">
                <Calendar
                  mode="range"
                  selected={dateRange}
                  locale={zhCN}
                  onSelect={(range) => {
                    if (!range?.from) {
                      return
                    }
                    setHasStartDate(true)
                    setDateRange({
                      from: range.from,
                      to: range.to ?? range.from,
                    })
                  }}
                />
                <div className="flex justify-end gap-2">
                  <Button
                    type="button"
                    variant="ghost"
                    size="sm"
                    disabled={!hasStartDate}
                    onClick={() => {
                      const endDate = dateRange.to ?? dateRange.from
                      if (!endDate) {
                        return
                      }
                      setHasStartDate(false)
                      setDateRange({ from: endDate, to: endDate })
                    }}
                  >
                    清除开始日期
                  </Button>
                  <Button
                    type="button"
                    size="sm"
                    onClick={() => {
                      setDatePickerOpen(false)
                    }}
                  >
                    完成
                  </Button>
                </div>
              </div>
            </PopoverContent>
          </Popover>

          <Button size="sm" type="submit" disabled={isSearching}>
            搜索
          </Button>
          {state.active ? (
            <Button
              type="button"
              variant="ghost"
              size="icon-sm"
              aria-label="退出搜索"
              onClick={onClear}
            >
              <X />
            </Button>
          ) : null}
        </div>
      </div>

      {state.active ? (
        <div className="text-muted-foreground flex min-h-5 items-center justify-between gap-2 px-1 text-xs">
          <span className="truncate">
            {query ? `“${query}”` : '空关键词'}，{dateRangeLabel(hasStartDate, dateRange)}
          </span>
          <span className="shrink-0 font-medium tabular-nums">
            {resultLabel(isSearching, resultTotal)}
          </span>
        </div>
      ) : null}
    </form>
  )
}
