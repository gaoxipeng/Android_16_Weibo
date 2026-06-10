type AuthEventListener = () => void

let listener: AuthEventListener | null = null

export function onUnauthorized(cb: AuthEventListener): () => void {
  listener = cb
  return () => {
    if (listener === cb) {
      listener = null
    }
  }
}

export function emitUnauthorized(): void {
  listener?.()
}
