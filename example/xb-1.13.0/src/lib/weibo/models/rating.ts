/** Rating summary returned by the xb-server API. */
export interface RatingSummary {
  /** Average score on a 10-point scale (stars * 2). */
  avg: number
  /** Total number of ratings. */
  count: number
  /** Distribution of star counts: key = star (1-5), value = count. */
  distribution: Record<number, number>
}

/** A target that can be rated — always a Weibo user UID. */
export type RatingTarget = string

/** Response for getting the current user's own rating. */
export interface MyRatingResponse {
  stars: number | null
}

/** Response for submitting a rating. */
export interface RateResponse {
  ok: boolean
}

/** Payload for submitting a user rating. */
export interface RateUserPayload {
  target_uid: string
  stars: number
}
