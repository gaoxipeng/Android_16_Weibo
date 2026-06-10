import { describe, expect, it } from 'vitest'

import { injectSonnerStyles } from '@/lib/weibo/content/host-shell-lifecycle'

describe('injectSonnerStyles', () => {
  it('injects sonner styles into the shadow root once', () => {
    const host = document.createElement('div')
    const shadow = host.attachShadow({ mode: 'open' })

    injectSonnerStyles(shadow, '.sonner { color: red; }')
    injectSonnerStyles(shadow, '.sonner { color: blue; }')

    const styles = shadow.querySelectorAll('[data-sonner-styles]')
    expect(styles).toHaveLength(1)
    expect(styles[0]?.textContent).toBe('.sonner { color: red; }')
  })
})
