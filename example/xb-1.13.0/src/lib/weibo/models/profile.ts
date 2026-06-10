export interface UserProfile {
  id: string
  name: string
  /** 个人简介，来自接口里的 `description`（info / detail）。 */
  bio: string
  avatarUrl: string | null
  bannerUrl: string | null
  followersCount: number | null
  followersCountStr: string | null
  friendsCount: number | null
  statusesCount: number | null
  location: string | null
  ipLocation: string | null
  verified: boolean
  verifiedReason: string | null
  /** 认证信息，来自 profile/detail 的 `desc_text`（与简介 `description` 不同）。 */
  descText: string | null
  createdAt: string | null
  mutualFollowers: { screenName: string; avatarUrl: string }[]
  mutualFollowerTotal: number | null
  /** 我是否关注对方 */
  following: boolean
  /** 对方是否关注我 */
  followMe: boolean
  /** 我是否把对方设为特别关注 */
  specialFollow: boolean
}

export interface ProfileFollowGroup {
  id: string
  idstr: string
  name: string
  mode: string | null
  memberCount: number | null
  exist: boolean
}
