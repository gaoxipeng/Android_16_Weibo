import { FeedCard } from '@/lib/weibo/components/feed-card'
import { PageEmptyState } from '@/lib/weibo/components/page-state'
import type { ComposeTarget } from '@/lib/weibo/models/compose'
import type { FeedItem } from '@/lib/weibo/models/feed'

export function FeedList({
  items,
  emptyLabel,
  onNavigate,
  onCommentClick,
  onRepostClick,
  onCommentReply,
}: {
  items: FeedItem[]
  emptyLabel: string
  onNavigate?: (item: FeedItem) => void
  onCommentClick?: (item: FeedItem) => void
  onRepostClick?: (item: FeedItem) => void
  onCommentReply?: (target: ComposeTarget) => void
}) {
  if (items.length === 0) {
    return <PageEmptyState label={emptyLabel} />
  }

  return (
    <div className="flex flex-col gap-3">
      {items.map((item) => (
        <FeedCard
          key={item.id}
          item={item}
          onNavigate={onNavigate}
          onCommentClick={onCommentClick}
          onRepostClick={onRepostClick}
          onCommentReply={onCommentReply}
        />
      ))}
    </div>
  )
}
