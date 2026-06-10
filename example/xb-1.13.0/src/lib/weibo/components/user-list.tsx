import { useEffect, useRef } from 'react'

import { Card } from '@/components/ui/card'
import { Spinner } from '@/components/ui/spinner'
import { UserCard } from '@/lib/weibo/components/user-card'
import type { RelationUser } from '@/lib/weibo/models/user-relation'

interface UserListProps {
  items: RelationUser[]
  hasNextPage: boolean
  isFetchingNextPage: boolean
  onLoadMore: () => void
  isLoading: boolean
  emptyLabel?: string
  msg?: string | null
}

export function UserList({
  items,
  hasNextPage,
  isFetchingNextPage,
  onLoadMore,
  isLoading,
  emptyLabel = '暂无内容',
  msg,
}: UserListProps) {
  const loadMoreRef = useRef<HTMLDivElement | null>(null)

  useEffect(() => {
    const el = loadMoreRef.current
    if (!el || !hasNextPage) return

    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0]?.isIntersecting) {
          onLoadMore()
        }
      },
      { threshold: 0.2 },
    )

    observer.observe(el)
    return () => observer.disconnect()
  }, [hasNextPage, onLoadMore])

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-12">
        <Spinner size="sm" />
      </div>
    )
  }

  if (items.length === 0) {
    return (
      <Card className="text-muted-foreground flex min-h-48 items-center justify-center p-6 text-center text-sm">
        {msg ?? emptyLabel}
      </Card>
    )
  }

  return (
    <div className="flex flex-col gap-3">
      {items.map((user) => (
        <UserCard key={user.id} user={user} />
      ))}
      {hasNextPage && (
        <div ref={loadMoreRef} className="flex justify-center py-4">
          {isFetchingNextPage ? <Spinner size="sm" /> : null}
        </div>
      )}
    </div>
  )
}
