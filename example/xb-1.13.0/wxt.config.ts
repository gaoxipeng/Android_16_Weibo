import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'

import tailwindcss from '@tailwindcss/vite'
import { loadEnv } from 'vite'
import { defineConfig } from 'wxt'

const projectRoot = fileURLToPath(new URL('.', import.meta.url))
const envMode = process.env.MODE ?? process.env.NODE_ENV ?? 'development'
const fileEnv = loadEnv(envMode, projectRoot, '')

function getEnv(name: string): string | undefined {
  return process.env[name] ?? fileEnv[name]
}

const packageJson = JSON.parse(
  readFileSync(new URL('./package.json', import.meta.url), 'utf8'),
) as {
  version: string
}

const extensionVersion = getEnv('EXTENSION_VERSION') ?? packageJson.version

// See https://wxt.dev/api/config.html
export default defineConfig({
  modules: ['@wxt-dev/module-react'],
  srcDir: 'src',
  manifest: {
    name: 'xb',
    description: 'xb - Redesign Weibo, like X but even better',
    icons: {
      16: 'icon/logo-16.png',
      32: 'icon/logo-32.png',
      48: 'icon/logo-48.png',
      128: 'icon/logo-128.png',
    },
    version: extensionVersion,
    version_name: extensionVersion,
    permissions: ['storage', 'cookies', 'declarativeNetRequest'],
    host_permissions: [
      'https://weibo.com/*',
      'https://www.weibo.com/*',
      // html-to-image fetches media URLs to embed; avatars/images live on CDNs.
      'https://*.sinaimg.cn/*',
      'https://*.sinajs.cn/*',
      'https://*.weibocdn.com/*',
      // m.weibo.cn API for topic search (proxied via background SW)
      'https://m.weibo.cn/*',
      // xb-server rating API
      'https://xb-server.nnecec-3d5.workers.dev/*',
      // 'https://s.weibo.com/*',
    ],
    web_accessible_resources: [
      {
        resources: ['weibo-main-world.js'],
        matches: [
          'https://weibo.com/*',
          'https://www.weibo.com/*',
          // 'https://s.weibo.com/*',
        ],
      },
    ],
    browser_specific_settings: {
      gecko: {
        id: getEnv('FIREFOX_EXTENSION_ID') ?? '@xb-weibo',
        data_collection_permissions: {
          required: ['none'],
        },
      },
    },
  },
  suppressWarnings: {
    firefoxDataCollection: true,
  },
  vite: () => ({
    plugins: [tailwindcss()],
    define: {
      'import.meta.env.VITE_XB_SIGN_SECRET': JSON.stringify(
        getEnv('VITE_XB_SIGN_SECRET') ?? getEnv('XB_SIGN_SECRET') ?? '',
      ),
      'import.meta.env.XB_SIGN_SECRET': JSON.stringify(getEnv('XB_SIGN_SECRET') ?? ''),
      'import.meta.env.XB_SERVER_URL': JSON.stringify(getEnv('XB_SERVER_URL') ?? ''),
    },
  }),
  dev: {
    server: {
      port: 3111,
    },
  },
})
