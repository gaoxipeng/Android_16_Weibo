import type {
  RelationPage,
  RelationUser,
  WeiboFriendRaw,
  WeiboFriendsPayload,
} from '@/lib/weibo/models/user-relation'

function adaptFriendUser(raw: WeiboFriendRaw): RelationUser {
  return {
    id: raw.idstr,
    name: raw.name,
    screenName: raw.screen_name,
    avatarUrl: raw.profile_image_url,
    avatarLarge: raw.avatar_large,
    description: raw.description,
    followersCount: raw.followers_count,
    followersCountStr: raw.followers_count_str,
    friendsCount: raw.friends_count,
    following: raw.following,
    followMe: raw.follow_me,
    verified: raw.verified,
    verifiedType: raw.verified_type,
    verifiedReason: raw.verified_reason,
    location: raw.location,
    createdAt: raw.created_at,
  }
}

export function adaptFriendsResponse(payload: WeiboFriendsPayload): RelationPage {
  return {
    items: (payload.users ?? []).map(adaptFriendUser),
    nextPage: payload.next_cursor > 0 ? payload.next_cursor : null,
    totalNumber: payload.total_number,
    errorMsg: payload.ok === 0 ? payload.msg : undefined,
  }
}
