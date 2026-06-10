import { describe, expect, it } from 'vitest'

import { CUSTOM_THEME_PRESETS, parseCustomThemeVariables } from '@/lib/custom-theme'

describe('custom-theme', () => {
  it('parses shadcn token declarations from variable snippets', () => {
    expect(
      parseCustomThemeVariables(`
        --background: oklch(1.0000 0 0);
        --foreground: #333333;
        --primary: #1d9bf0 !important;
      `),
    ).toEqual({
      '--background': 'oklch(1.0000 0 0)',
      '--foreground': '#333333',
      '--primary': '#1d9bf0',
    })
  })

  it('ignores non-token declarations and normal css rules', () => {
    expect(
      parseCustomThemeVariables(`
        body { color: red; }
        --unknown-brand-token: #fff;
        --background: #ffffff;
      `),
    ).toEqual({
      '--background': '#ffffff',
    })
  })

  it('keeps the modern preset aligned with the provided theme variables', () => {
    const modern = CUSTOM_THEME_PRESETS.find((preset) => preset.key === 'modern')
    expect(modern?.name).toBe('Modern')
    expect(parseCustomThemeVariables(modern?.lightCss ?? '')).toMatchObject({
      '--background': '#ffffff',
      '--foreground': '#333333',
      '--primary': '#3b82f6',
      '--sidebar-ring': '#3b82f6',
      '--font-sans': 'Inter, sans-serif',
      '--shadow-sm': '0 1px 3px 0px hsl(0 0% 0% / 0.10), 0 1px 2px -1px hsl(0 0% 0% / 0.10)',
      '--spacing': '0.25rem',
    })
    expect(parseCustomThemeVariables(modern?.darkCss ?? '')).toMatchObject({
      '--background': '#171717',
      '--foreground': '#e5e5e5',
      '--accent': '#1e3a8a',
      '--sidebar-ring': '#3b82f6',
      '--font-mono': 'JetBrains Mono, monospace',
      '--shadow-2xl': '0 1px 3px 0px hsl(0 0% 0% / 0.25)',
    })
  })
})
