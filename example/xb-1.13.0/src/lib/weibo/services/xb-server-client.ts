/**
 * Client for the xb-server rating API.
 *
 * Since xb-server has CORS configured for the extension origin,
 * the content script can fetch directly — no background SW proxy needed.
 *
 * HMAC-SHA256 signing is done here using the build-time injected secret.
 * The current user's UID is obtained from the DOM via getCurrentUserUid().
 */

import type {
  MyRatingResponse,
  RateResponse,
  RateUserPayload,
  RatingSummary,
} from '@/lib/weibo/models/rating'
import { getCurrentUserUid } from '@/lib/weibo/platform/current-user'
import { signXbServerRequest } from '@/lib/weibo/services/xb-server-sign'

/** xb-server base URL – change for dev/prod as needed. */
const XB_SERVER_BASE = (
  import.meta.env.XB_SERVER_URL || 'https://xb-server.nnecec-3d5.workers.dev'
).replace(/\/+$/, '')

// ─── Helper: direct fetch ───

async function xbFetch<T>(
  method: 'GET' | 'POST',
  path: string,
  options?: { body?: unknown },
): Promise<T> {
  const url = `${XB_SERVER_BASE}${path}`

  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    Accept: 'application/json',
  }

  const uid = getCurrentUserUid()
  if (!uid) throw new Error('xb-rating-not-logged-in')

  const signHeaders = await signXbServerRequest(uid, path)
  Object.assign(headers, signHeaders)

  const response = await fetch(url, {
    method,
    headers,
    ...(options?.body !== undefined && { body: JSON.stringify(options.body) }),
  })

  if (!response.ok) {
    let errorMsg = `xb-rating-fetch-failed:${response.status}`
    try {
      const errorBody = (await response.json()) as { error?: string }
      if (errorBody.error) errorMsg = errorBody.error
    } catch {
      // ignore parse error
    }
    throw new Error(errorMsg)
  }

  const data = (await response.json()) as T
  return data
}

// ─── Read APIs ───

export async function getUserRatingSummary(uid: string): Promise<RatingSummary> {
  return xbFetch<RatingSummary>('GET', `/api/ratings/user/${uid}`)
}

export async function getMyUserRating(targetUid: string): Promise<MyRatingResponse> {
  return xbFetch<MyRatingResponse>('GET', `/api/ratings/user/${targetUid}/me`)
}

// ─── Batch APIs ───

export async function batchGetUserRatingSummaries(
  uids: string[],
): Promise<Record<string, RatingSummary>> {
  return xbFetch<Record<string, RatingSummary>>('POST', '/api/ratings/batch', {
    body: { uids },
  })
}

// ─── Write APIs ───

export async function rateUser(payload: RateUserPayload): Promise<RateResponse> {
  return xbFetch<RateResponse>('POST', '/api/ratings/user', {
    body: payload,
  })
}

export { XB_SERVER_BASE }
