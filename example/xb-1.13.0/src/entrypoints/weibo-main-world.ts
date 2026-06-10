import { installApiBridge } from '@/lib/weibo/inject/install-api-bridge'
import { installHistoryBridge } from '@/lib/weibo/inject/install-history-bridge'

export default defineUnlistedScript(() => {
  installHistoryBridge(window)
  installApiBridge(window)
})
