import { describe, expect, it } from 'vitest'

import { createMediaRequestHeaderRule } from '@/entrypoints/background'

describe('background media request header rule', () => {
  it('only matches background fetch requests for media downloads', () => {
    const rule = createMediaRequestHeaderRule()

    expect(rule.condition.resourceTypes).toEqual(['xmlhttprequest'])
    expect(rule.condition.tabIds).toEqual([-1])
    expect(rule.condition.resourceTypes).not.toContain('media')
    expect(rule.condition.resourceTypes).not.toContain('image')
  })
})
