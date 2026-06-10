import '../assets/global.css'
import { mountWeiboHostShell } from '@/lib/weibo/content/host-shell-lifecycle'

import sonnerStyles from 'sonner/dist/styles.css?raw'

export let ui: Awaited<ReturnType<typeof mountWeiboHostShell>>

export default defineContentScript({
  matches: ['https://weibo.com/*', 'https://www.weibo.com/*'],
  runAt: 'document_idle',
  cssInjectionMode: 'ui',
  async main(ctx) {
    ui = await mountWeiboHostShell({ ctx, sonnerStyles })
  },
})
