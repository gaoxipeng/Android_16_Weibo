import axios from 'axios'

import {
  API_RESPONSE_EVENT,
  API_UNAUTHORIZED_EVENT,
  XB_SOURCE,
  isApiRequestMessage,
} from '@/lib/weibo/platform/messages'

const DEFAULT_TIMEOUT_MS = 10000

function getWeiboApiBase(): string {
  if (import.meta.env.FIREFOX) {
    const host = window.location.host
    if (host === 'weibo.com' || host === 'www.weibo.com') {
      return `https://${host}`
    }
    return 'https://weibo.com'
  }
  return ''
}

const WEIBO_API_BASE = getWeiboApiBase()

function readXsrfTokenFromCookie(): string | null {
  const match = document.cookie.match(/(?:^|;\s*)XSRF-TOKEN=([^;]+)/)
  if (!match?.[1]) {
    return null
  }

  try {
    return decodeURIComponent(match[1])
  } catch {
    return match[1]
  }
}

export function installApiBridge(targetWindow: Window) {
  const client = axios.create({
    baseURL: WEIBO_API_BASE,
    timeout: DEFAULT_TIMEOUT_MS,
    withCredentials: true,
    headers: {
      Accept: 'application/json, text/plain, */*',
      'X-Requested-With': 'XMLHttpRequest',
    },
  })

  const postResponse = (
    id: string,
    data?: unknown,
    error?: { status?: number; message?: string },
  ) => {
    targetWindow.postMessage(
      {
        source: XB_SOURCE,
        type: API_RESPONSE_EVENT,
        id,
        ...(data !== undefined && { data }),
        ...(error !== undefined && { error }),
      },
      '*',
    )
  }

  const postUnauthorized = () => {
    targetWindow.postMessage(
      {
        source: XB_SOURCE,
        type: API_UNAUTHORIZED_EVENT,
      },
      '*',
    )
  }

  const handleRequest = async (
    id: string,
    method: 'get' | 'post',
    path: string,
    params?: Record<string, string | number | null | undefined>,
    body?: Record<string, string>,
  ) => {
    try {
      if (method === 'get') {
        const response = await client.get(path, { params })
        postResponse(id, response.data)
      } else {
        const xsrf = readXsrfTokenFromCookie()
        const response = await client.post(path, new URLSearchParams(body), {
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            ...(xsrf ? { 'X-XSRF-TOKEN': xsrf } : {}),
          },
        })
        postResponse(id, response.data)
      }
    } catch (error) {
      if (axios.isAxiosError(error)) {
        const status = error.response?.status
        if (status === 401 || status === 403) {
          postUnauthorized()
        }
        postResponse(id, undefined, {
          status,
          message: error.response?.data?.message || error.response?.data?.msg || error.message,
        })
      } else {
        postResponse(id, undefined, {
          message: error instanceof Error ? error.message : 'Unknown error',
        })
      }
    }
  }

  targetWindow.addEventListener('message', (event: MessageEvent) => {
    if (event.source !== targetWindow) return
    const data = event.data
    if (!isApiRequestMessage(data)) return
    handleRequest(data.id, data.method, data.path, data.params, data.body)
  })
}
