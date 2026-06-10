/**
 * HMAC-SHA256 signing utility for xb-server requests.
 *
 * Signature payload: `${timestamp}.${uid}.${path}`
 * The secret is embedded at build time via WXT's define feature.
 */

const XB_SIGN_SECRET = import.meta.env.VITE_XB_SIGN_SECRET ?? import.meta.env.XB_SIGN_SECRET ?? ''

/**
 * Sign a request to the xb-server.
 * Returns the headers to attach to the fetch request.
 */
export async function signXbServerRequest(
  uid: string,
  path: string,
): Promise<Record<string, string>> {
  const timestamp = String(Math.floor(Date.now() / 1000))

  const headers: Record<string, string> = {
    'X-XB-UID': uid,
    'X-XB-Timestamp': timestamp,
  }

  if (XB_SIGN_SECRET) {
    const payload = `${timestamp}.${uid}.${path}`
    const key = await crypto.subtle.importKey(
      'raw',
      new TextEncoder().encode(XB_SIGN_SECRET),
      { name: 'HMAC', hash: 'SHA-256' },
      false,
      ['sign'],
    )
    const signature = await crypto.subtle.sign('HMAC', key, new TextEncoder().encode(payload))
    const hex = Array.from(new Uint8Array(signature))
      .map((b) => b.toString(16).padStart(2, '0'))
      .join('')
    headers['X-XB-Signature'] = hex
  }

  return headers
}
