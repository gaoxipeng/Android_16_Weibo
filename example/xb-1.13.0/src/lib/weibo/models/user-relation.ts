export interface RelationUser {
  id: string
  name: string
  screenName: string
  avatarUrl: string
  avatarLarge: string
  description: string
  followersCount: number
  followersCountStr: string | null
  friendsCount: number
  following: boolean
  followMe: boolean
  verified: boolean
  verifiedType: number
  verifiedReason: string
  location: string
  createdAt: string
}

export interface WeiboFriendRaw {
  id: number
  idstr: string
  screen_name: string
  name: string
  description: string
  profile_image_url: string
  avatar_large: string
  avatar_hd: string
  followers_count: number
  followers_count_str: string
  friends_count: number
  following: boolean
  follow_me: boolean
  verified: boolean
  verified_type: number
  verified_reason: string
  location: string
  created_at: string
  [key: string]: unknown
}

export interface WeiboFriendsPayload {
  ok: number
  users?: WeiboFriendRaw[]
  next_cursor: number
  total_number: number
  followers_count?: number
  screenName?: string
  msg?: string
}

export interface RelationPage {
  items: RelationUser[]
  nextPage: number | null
  totalNumber: number
  errorMsg?: string
}
