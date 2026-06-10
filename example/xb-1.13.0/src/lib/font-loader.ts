import type { FontFamilyClass } from './app-settings'

const REMOTE_FONTS: Record<RemoteFontFamily, { url: string; family: string }> = {
  'font-lxgw-wenkai': {
    url: 'https://cdn.jsdelivr.net/npm/lxgwwenkai@1.0.1/woff2/LXGWWenKai-Regular.woff2',
    family: 'LXGW WenKai',
  },
  'font-smiley-sans': {
    url: 'https://cdn.jsdelivr.net/npm/font-smiley-sans@1.0.0/SmileySans-Oblique.ttf.woff2',
    family: 'Smiley Sans',
  },
  'font-zhuque': {
    url: 'https://cdn.jsdelivr.net/npm/@fontpkg/zhuque-fangsong-technical-preview@0.212.0/ZhuqueFangsong-Regular.ttf',
    family: 'ZhuQue Fangsong',
  },
  'font-source-han-serif': {
    url: 'https://cdn.jsdelivr.net/npm/@fontsource/noto-serif-sc@5.2.9/files/noto-serif-sc-chinese-simplified-400-normal.woff2',
    family: 'Noto Serif SC',
  },
  'font-source-han-sans': {
    url: 'https://cdn.jsdelivr.net/npm/@fontsource/noto-sans-sc@5.1.0/files/noto-sans-sc-chinese-simplified-400-normal.woff2',
    family: 'Noto Sans SC',
  },
  'font-fz-kai': {
    url: 'https://cdn.jsdelivr.net/npm/cn-fontsource-fz-kai-z-03-regular@1.0.1/L1_68a8_192.woff2',
    family: 'FZ Kai-Z-03',
  },
  'font-canger-jinkai': {
    url: 'https://cdn.hycqwq.top/font/CangErJinKai-05-W04.ttf',
    family: 'CangErJinKai05',
  },
  'font-lxgw-marker-gothic': {
    url: 'https://cdn.jsdelivr.net/fontsource/fonts/lxgw-marker-gothic@latest/latin-400-normal.woff2',
    family: 'LXGW Marker Gothic',
  },
  'font-lxgw-neo-zhisong': {
    url: 'https://cdn.jsdelivr.net/npm/lxgw-neo-zhisong-webfont@1.0.0/fonts/LXGWNeoZhiSong.woff2',
    family: 'LXGW Neo ZhiSong',
  },
}

const LOADED_FONTS_KEY = 'xb:loaded-fonts'
const fontStyleMap = new Map<RemoteFontFamily, HTMLStyleElement>()

export function isRemoteFont(font: FontFamilyClass): font is RemoteFontFamily {
  return remoteFonts.includes(font as RemoteFontFamily)
}

function injectFontFaceStyle(font: RemoteFontFamily): void {
  if (fontStyleMap.has(font)) return

  const config = REMOTE_FONTS[font]
  if (!config) return

  // 构造 @font-face CSS 并注入到 document.head
  const style = document.createElement('style')
  style.id = `xb-font-${font}`
  style.textContent = `
    @font-face {
      font-family: "${config.family}";
      font-style: normal;
      font-weight: 400;
      font-display: swap;
      src: url("${config.url}") format("woff2");
    }
  `
  document.head.appendChild(style)
  fontStyleMap.set(font, style)
}

export async function loadFont(font: RemoteFontFamily): Promise<boolean> {
  const config = REMOTE_FONTS[font]
  if (!config) return false

  // 检查是否已注入过 style（说明已加载过）
  if (fontStyleMap.has(font) || document.getElementById(`xb-font-${font}`)) {
    return true
  }

  // 检查 localStorage
  try {
    const stored = localStorage.getItem(LOADED_FONTS_KEY)
    const loaded: RemoteFontFamily[] = stored ? JSON.parse(stored) : []
    if (loaded.includes(font)) {
      injectFontFaceStyle(font)
      return true
    }
  } catch {}

  // 注入 @font-face style
  injectFontFaceStyle(font)

  // 等待字体加载完成
  try {
    await document.fonts.ready

    // 验证字体是否真的加载成功
    const isAvailable = await document.fonts.check(`12px "${config.family}"`)
    if (isAvailable) {
      try {
        const stored = localStorage.getItem(LOADED_FONTS_KEY)
        const loaded: RemoteFontFamily[] = stored ? JSON.parse(stored) : []
        if (!loaded.includes(font)) {
          localStorage.setItem(LOADED_FONTS_KEY, JSON.stringify([...loaded, font]))
        }
      } catch {}
      return true
    }

    // 如果 check 失败，尝试用 FontFace 再加载一次
    const fontFace = new FontFace(config.family, `url("${config.url}")`, {
      display: 'swap',
    })
    await fontFace.load()
    document.fonts.add(fontFace)
    await document.fonts.ready

    try {
      const stored = localStorage.getItem(LOADED_FONTS_KEY)
      const loaded: RemoteFontFamily[] = stored ? JSON.parse(stored) : []
      if (!loaded.includes(font)) {
        localStorage.setItem(LOADED_FONTS_KEY, JSON.stringify([...loaded, font]))
      }
    } catch {}

    return true
  } catch (err) {
    console.error(`[font-loader] Failed to load ${font}:`, err)
    // 移除失败的 style
    const style = fontStyleMap.get(font)
    if (style) {
      style.remove()
      fontStyleMap.delete(font)
    }
    return false
  }
}
export type SystemFontFamily =
  | 'font-sans'
  | 'font-serif'
  | 'font-simsun'
  | 'font-fangsong'
  | 'font-simhei'
  | 'font-kaiti'

const remoteFonts = [
  'font-lxgw-wenkai',
  'font-smiley-sans',
  'font-zhuque',
  'font-source-han-serif',
  'font-source-han-sans',
  'font-fz-kai',
  'font-canger-jinkai',
  'font-lxgw-marker-gothic',
  'font-lxgw-neo-zhisong',
] as const
// 可下载开源字体
export type RemoteFontFamily = (typeof remoteFonts)[number]

export const FONT_FAMILY_CLASSES: FontFamilyClass[] = [
  'font-sans',
  'font-serif',
  'font-simsun',
  'font-fangsong',
  'font-simhei',
  'font-kaiti',
  ...remoteFonts,
]
