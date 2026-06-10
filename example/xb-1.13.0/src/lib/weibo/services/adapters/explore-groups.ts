export interface ExploreGroup {
  gid: string
  title: string
  containerid: string
}

export interface FollowGroup {
  gid: string
  title: string
}

export interface DefaultFollowGroups {
  specialFollow: FollowGroup | null
  friendCircle: FollowGroup | null
}

export interface FollowGroups {
  groups: FollowGroup[]
  defaultGroups: DefaultFollowGroups
}

interface ExploreGroupRaw {
  gid?: string
  title?: string
  containerid?: string
}

interface ExploreGroupsPayloadCategory {
  group?: ExploreGroupRaw[]
  title?: string
}

export interface ExploreGroupsPayload {
  groups?: ExploreGroupsPayloadCategory[]
}

export type DefaultFollowHomeTab = 'special-follow' | 'friend-circle'

export function adaptExploreGroupsResponse(payload: ExploreGroupsPayload): ExploreGroup[] {
  const list = payload.groups ?? []

  // Try to find "我的频道" category
  const myChannel = list.find((item) => item.title === '我的频道')

  let groups: ExploreGroupRaw[] = []

  if (myChannel && myChannel.group) {
    // Found "我的频道", use its list
    groups = myChannel.group ?? []
  }

  return groups as ExploreGroup[]
}

export function adaptFollowGroupsResponse(payload: ExploreGroupsPayload): FollowGroup[] {
  const list = payload.groups ?? []
  const myGroups = list.find((item) => item.title === '我的分组')
  if (!myGroups?.group) return []
  return myGroups.group
    .filter((g): g is ExploreGroupRaw & { gid: string; title: string } => Boolean(g.gid && g.title))
    .map(({ gid, title }) => ({ gid, title }))
}

export function adaptDefaultFollowGroupsResponse(
  payload: ExploreGroupsPayload,
): DefaultFollowGroups {
  const list = payload.groups ?? []
  const defaultGroups = list.find((item) => item.title === '默认分组')
  const groups = defaultGroups?.group ?? []

  const findGroup = (title: string): FollowGroup | null => {
    const group = groups.find((item) => item.title === title)
    return group?.gid && group.title ? { gid: group.gid, title: group.title } : null
  }

  return {
    specialFollow: findGroup('特别关注'),
    friendCircle: findGroup('互相关注'),
  }
}

export function adaptFollowGroupsDataResponse(payload: ExploreGroupsPayload): FollowGroups {
  return {
    groups: adaptFollowGroupsResponse(payload),
    defaultGroups: adaptDefaultFollowGroupsResponse(payload),
  }
}

export function getDefaultFollowGroupForHomeTab(
  defaultGroups: DefaultFollowGroups | null | undefined,
  tab: DefaultFollowHomeTab,
): FollowGroup | null {
  if (!defaultGroups) return null
  return tab === 'special-follow' ? defaultGroups.specialFollow : defaultGroups.friendCircle
}

export function getHomeTabForDefaultFollowGroupId(
  defaultGroups: DefaultFollowGroups | null | undefined,
  gid: string | null | undefined,
): DefaultFollowHomeTab | null {
  if (!defaultGroups || !gid) return null
  if (defaultGroups.specialFollow?.gid === gid) return 'special-follow'
  if (defaultGroups.friendCircle?.gid === gid) return 'friend-circle'
  return null
}
