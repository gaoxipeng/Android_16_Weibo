import {
  API_REQUEST_EVENT,
  XB_SOURCE,
  isApiResponseMessage,
  isApiUnauthorizedMessage,
} from '@/lib/weibo/platform/messages'
import { emitUnauthorized } from '@/lib/weibo/services/auth-events'

export type WeiboQueryParams = Record<string, string | number | null | undefined>

const DEFAULT_TIMEOUT_MS = 10000

type PendingRequest = {
  resolve: (data: unknown) => void
  reject: (error: Error) => void
  timer: ReturnType<typeof setTimeout>
}

const pendingRequests = new Map<string, PendingRequest>()

let requestCounter = 0

function generateRequestId(): string {
  return `xb-${++requestCounter}-${Date.now()}`
}

function postApiRequest(
  method: 'get' | 'post',
  path: string,
  params?: WeiboQueryParams,
  body?: Record<string, string>,
): Promise<unknown> {
  return new Promise((resolve, reject) => {
    const id = generateRequestId()

    const timer = setTimeout(() => {
      pendingRequests.delete(id)
      reject(new Error('weibo-request-timeout'))
    }, DEFAULT_TIMEOUT_MS)

    pendingRequests.set(id, { resolve, reject, timer })

    window.postMessage(
      {
        source: XB_SOURCE,
        type: API_REQUEST_EVENT,
        id,
        method,
        path,
        ...(params !== undefined && { params }),
        ...(body !== undefined && { body }),
      },
      '*',
    )
  })
}

window.addEventListener('message', (event: MessageEvent) => {
  if (event.source !== window) return

  if (isApiResponseMessage(event.data)) {
    const pending = pendingRequests.get(event.data.id)
    if (!pending) return

    pendingRequests.delete(event.data.id)
    clearTimeout(pending.timer)

    if (event.data.error) {
      const { status, message } = event.data.error
      if (status) {
        pending.reject(new Error(message ?? `weibo-request-failed:${status}`))
      } else {
        pending.reject(new Error(message ?? 'weibo-request-failed'))
      }
    } else {
      pending.resolve(event.data.data)
    }
  }

  if (isApiUnauthorizedMessage(event.data)) {
    emitUnauthorized()
  }
})

export async function wbGet<T>(path: string, params: WeiboQueryParams = {}): Promise<T> {
  return postApiRequest('get', path, params) as Promise<T>
}

export async function wbPostForm<T>(path: string, data: Record<string, string>): Promise<T> {
  return postApiRequest('post', path, undefined, data) as Promise<T>
}
