import { AnimatePresence, motion, useReducedMotion } from 'motion/react'

import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import type { FeedAuthor } from '@/lib/weibo/models/feed'

function getAuthorInitial(name: string | null | undefined) {
  return name?.slice(0, 1).toUpperCase() || '?'
}

interface NewPostsBubbleProps {
  authors: FeedAuthor[]
  count: number
}

export function NewPostsBubble({ authors, count }: NewPostsBubbleProps) {
  const shouldReduceMotion = useReducedMotion()

  return (
    <AnimatePresence>
      <motion.div
        key="new-posts-bubble"
        initial={shouldReduceMotion ? { opacity: 0 } : { opacity: 0, x: 14 }}
        animate={shouldReduceMotion ? { opacity: 1 } : { opacity: 1, x: 0 }}
        exit={shouldReduceMotion ? { opacity: 0 } : { opacity: 0, x: 14 }}
        transition={{ duration: shouldReduceMotion ? 0.12 : 0.25, ease: [0.4, 0, 0.2, 1] }}
        className="bg-muted/80 text-muted-foreground inline-flex items-center gap-1.5 rounded-full px-2.5 py-1 text-xs"
      >
        <span className="flex -space-x-1.5">
          {authors.slice(0, 3).map((author) => (
            <Avatar key={author.id} className="ring-background size-5 ring-2">
              <AvatarImage src={author.avatarUrl ?? undefined} alt={author.name} />
              <AvatarFallback className="text-[8px]">
                {getAuthorInitial(author.name)}
              </AvatarFallback>
            </Avatar>
          ))}
        </span>
        <span>{count < 10 ? `${count}条新微博` : `10+条新微博`}</span>
      </motion.div>
    </AnimatePresence>
  )
}
