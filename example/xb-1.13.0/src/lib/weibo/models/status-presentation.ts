/** Where the same FeedItem-backed UI is shown (mapper input). */
export type StatusFeedSurface = 'timeline' | 'detail'

/** Role distinguishes the outer post from an embedded retweet. */
export type StatusCardRole = 'root' | 'quoted'

export function statusAllowsCardNavigate(
  surface: StatusFeedSurface,
  role: StatusCardRole,
): boolean {
  return role === 'root' && surface === 'timeline'
}
