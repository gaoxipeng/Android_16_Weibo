import type { HomeTab } from '@/lib/app-settings'
import type { DefaultFollowGroups } from '@/lib/weibo/services/adapters/explore-groups'
import { getDefaultFollowGroupForHomeTab } from '@/lib/weibo/services/adapters/explore-groups'

export function homeTimelinePathFromTab(
  tab: HomeTab,
  defaultGroups?: DefaultFollowGroups | null,
): string {
  switch (tab) {
    case 'following':
      return '/mygroups'
    case 'special-follow': {
      const group = defaultGroups
        ? getDefaultFollowGroupForHomeTab(defaultGroups, 'special-follow')
        : null
      return group ? `/mygroups?gid=${group.gid}` : '/mygroups'
    }
    case 'friend-circle': {
      const group = defaultGroups
        ? getDefaultFollowGroupForHomeTab(defaultGroups, 'friend-circle')
        : null
      return group ? `/mygroups?gid=${group.gid}` : '/mygroups'
    }
    case 'for-you':
    default:
      return '/'
  }
}
