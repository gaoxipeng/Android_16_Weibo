# AGENTS.md

文档地图：`AGENTS.md`（本文，HOW）· `PRODUCT.md`（产品边界）· `DESIGN.md` /
`DESIGN.json`（UI 规则与 token）· `README.md` / `README.en.md`（面向用户）·
`src/lib/app-settings.ts`（设置项默认值）

## Project Overview

xb is a browser extension that rewrites weibo.com into a cleaner X-like reading
experience. Built with WXT, React 19, TypeScript, Tailwind CSS 4, shadcn/ui,
motion (same as framer-motion), Zustand, TanStack Query, and `@reactuses/core`.

## Developer Commands

```bash
bun run dev           # Start dev server (Chrome)
bun run dev:edge      # Start dev server (Edge)
bun run dev:firefox   # Start dev server (Firefox)
bun run build         # Build for Chrome/Chromium
bun run build:edge    # Build for Edge
bun run build:firefox # Build for Firefox
bun run test:unit     # Run unit tests (Vitest + jsdom)
bun run test:watch    # Run tests in watch mode
bun run compile       # TypeScript type check only
bun run lint          # Format + lint with oxfmt/oxlint
bun run knip          # Find unused files/exports/dependencies
bun run zip           # Package as .zip (Chrome)
bun run zip:edge      # Package as .zip (Edge)
bun run zip:firefox   # Package as .zip (Firefox)
```

- 包管理器使用 **bun**（项目已配置 `only-allow bun`）

## Verification Order

lint → typecheck → test → build

## Project Structure

```
src/
├── entrypoints/       # weibo.content, weibo-main-world, weibo-hide, options, …
├── lib/
│   ├── app-settings.ts / app-settings-store.ts
│   ├── custom-theme.ts / font-loader.ts / utils.ts
│   └── weibo/
│       ├── app/          # shell, root, layout
│       ├── components/   # feed, profile, gen-image, media-player, …
│       ├── content/      # host selectors, shell state, page takeover, lifecycle
│       ├── hooks/ pages/ route/ inject/ platform/ stores/ utils/
│       ├── services/     # client, adapters, weibo-repository, xb-server-*
│       ├── models/ queries/
├── components/ui/     # shadcn
├── hooks/
└── test/
```

## Architecture Notes

- **Content Script UI**: WXT `createShadowRootUi` + `cssInjectionMode: 'ui'` —
  React in shadow root, isolated from Weibo CSS.
- **weibo-main-world.ts**: Unlisted script in page context; history + API
  bridges.
- **Settings Store**: `app-settings-store.ts` → `chrome.storage`; call
  `hydrate()` before use.
- **Host Shell Lifecycle**: `host-shell-lifecycle.tsx` — inject bridge, wait for
  host DOM, hydrate, first-load redirect, mount UI, cleanup.
- **API Layer**: `client.ts` + `adapters/` → internal models.
- **Query Layer**: `weibo-queries.ts` wraps repository.
- **Status Cache**: `status-cache.ts` — optimistic like/favorite/comment count
  across timeline/detail/comment caches.
- **xb Rating**: `xb-server-client.ts` + `xb-server-sign.ts` +
  `rating-queries.ts`.

## Key Patterns

- **Hooks**: Prefer `@reactuses/core` over reimplementing common hooks.
- **Routing**: `parse-weibo-url.ts` → `page-descriptor.ts` (home, profile,
  follows, favorites, liked, notifications, explore, history, topics).
- **Mounting**: `host-selectors.ts` waits for DOM; `shell-state.ts` binds to
  host; `page-takeover.ts` marks handled pages.
- **API bridge**: `install-api-bridge.ts` in page context; **m.weibo.cn**
  fallback for topic/search when desktop API is weak.
- **Media**: `download-media.ts` + `jszip` for batch; **themes**:
  `custom-theme.ts` → CSS variables via `shell-state.ts`.

## Code Patterns

### Settings

`useAppSettings` 选择性订阅；开关与默认值以 `app-settings.ts` 为准。

```typescript
const fontSizeClass = useAppSettings((s) => s.fontSizeClass)
const setFontSizeClass = useAppSettings((s) => s.setFontSizeClass)
```

### Mutations

- 跨页面乐观更新 → `status-cache.ts`
- 仅刷新查询 → mutation `meta.invalidates`

```typescript
const followMutation = useMutation({
  mutationFn: () => followUser(uid),
  meta: {
    invalidates: [
      ['weibo', 'profile'],
      ['weibo', 'profile-hover'],
    ],
  },
})
```

### Shared UI

- Profile 共享：`profile-shared.tsx`（`ProfileBanner`、`ProfileMutualFollowers`、`formatProfileCount`）
- 产品范围与 UI 原则：见 `PRODUCT.md`、`DESIGN.md`；评分/env 见下文 Extension
  Notes

## Testing

- Vitest + `jsdom`; setup: `src/test/setup.ts`
- Tests: `*.test.ts` / `*.spec.ts` alongside source; components use
  `@testing-library/react`

## Code Quality

- **Linter**: oxlint (strict, TS/React/unicorn)
- **Formatter**: oxfmt (no semicolons, single quotes, sorted imports)
- **TypeScript**: extends `.wxt/tsconfig.json`

## Browser Extension Notes

- Manifest v3 (WXT)
- Permissions: `storage`, `cookies`
- Host permissions: `https://weibo.com/*`, `https://www.weibo.com/*`,
  `https://*.sinaimg.cn/*`, `https://*.sinajs.cn/*`, `https://*.weibocdn.com/*`,
  `https://m.weibo.cn/*`, `https://xb-server.nnecec-3d5.workers.dev/*`
- Web accessible resource: `weibo-main-world.js`
- Rating build env: `XB_SIGN_SECRET` / `VITE_XB_SIGN_SECRET`; optional
  `XB_SERVER_URL`
