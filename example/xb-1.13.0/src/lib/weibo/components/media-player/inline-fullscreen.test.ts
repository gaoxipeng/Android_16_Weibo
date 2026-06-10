import { afterEach, describe, expect, it, vi } from 'vitest'

import { setUiPortalContainer } from '@/components/ui/portal'

import { activateInlineFullscreen } from './inline-fullscreen'

describe('activateInlineFullscreen', () => {
  afterEach(() => {
    setUiPortalContainer(null)
    document.body.replaceChildren()
  })

  it('moves the player container to the ui portal and restores its original position', () => {
    const portal = document.createElement('div')
    const parent = document.createElement('div')
    const before = document.createElement('span')
    const player = document.createElement('div')
    const after = document.createElement('span')

    setUiPortalContainer(portal)
    document.body.append(parent, portal)
    parent.append(before, player, after)

    const session = activateInlineFullscreen(player, vi.fn())

    expect(portal.lastChild).toBe(player)
    expect(player).toHaveClass('media-inline-fullscreen')
    expect(player).toHaveAttribute('data-xb-inline-fullscreen', 'true')

    session.deactivate()

    expect([...parent.childNodes]).toEqual(expect.arrayContaining([before, player, after]))
    expect(parent.childNodes[0]).toBe(before)
    expect(parent.childNodes[1]).toBe(player)
    expect(parent.childNodes[2]).toBe(after)
    expect(player).not.toHaveClass('media-inline-fullscreen')
    expect(player).not.toHaveAttribute('data-xb-inline-fullscreen')
  })

  it('calls onExit when Escape is pressed', () => {
    const player = document.createElement('div')
    const onExit = vi.fn()

    document.body.append(player)
    const session = activateInlineFullscreen(player, onExit)

    window.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape' }))

    expect(onExit).toHaveBeenCalledTimes(1)
    session.deactivate()
  })
})
