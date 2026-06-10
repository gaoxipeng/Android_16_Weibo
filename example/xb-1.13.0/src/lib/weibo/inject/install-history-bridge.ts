type HistoryMethod = 'pushState' | 'replaceState'

/**
 * After a popstate (browser back/forward), the host page's SPA router may
 * call replaceState/pushState to rewrite the URL (e.g. Weibo's Vue Router
 * replaces /topic?q=<name> with /topic because it doesn't recognise the
 * parameterised path). This guard blocks those calls for a short window so
 * that React Router — which reads window.location on popstate — sees the
 * correct URL.
 *
 * Crucially, the popstate listener is registered with capture:true so it
 * fires BEFORE the host page's listeners (which are registered at page load
 * time, before this script runs). Without capture, our listener would fire
 * after the host's, and the guard would not be active when the host calls
 * replaceState.
 */
export function installHistoryBridge(targetWindow: Window) {
  let popstateHref: string | null = null

  const wrapHistoryMethod = (method: HistoryMethod) => {
    const original = targetWindow.history[method]

    Object.defineProperty(targetWindow.history, method, {
      configurable: true,
      writable: true,
      value: function patchedHistoryMethod(
        this: History,
        ...args: Parameters<History[HistoryMethod]>
      ) {
        if (popstateHref !== null && args[2] != null) {
          try {
            const resolved = new URL(args[2] as string, targetWindow.location.href).href
            if (resolved !== popstateHref) {
              return
            }
          } catch {
            // invalid URL — let it through
          }
        }
        const result = original.apply(this, args)
        return result
      },
    })
  }

  wrapHistoryMethod('pushState')
  wrapHistoryMethod('replaceState')

  // capture:true ensures this fires before the host page's popstate handlers
  targetWindow.addEventListener(
    'popstate',
    () => {
      popstateHref = targetWindow.location.href
      setTimeout(() => {
        popstateHref = null
      }, 50)
    },
    true,
  )
}
