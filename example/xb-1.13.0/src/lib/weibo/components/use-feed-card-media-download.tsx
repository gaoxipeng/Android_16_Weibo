import { useState } from 'react'
import { toast } from 'sonner'

import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import type { FeedItem } from '@/lib/weibo/models/feed'
import {
  downloadAsZip,
  estimateTotalSize,
  extractMediaUrls,
  type MediaUrl,
} from '@/lib/weibo/utils/download-media'

function getMediaZipFilename(item: FeedItem) {
  const author = item.author.name
  const text = item.text
    .replace(/<[^>]*>/g, '')
    .replace(/[<>:"/\\|?*\n\r]/g, '')
    .slice(0, 10)

  return `${author}_${text}.zip`
}

export function useFeedCardMediaDownload(item?: FeedItem) {
  const [downloadLoading, setDownloadLoading] = useState(false)
  const [sizeWarningOpen, setSizeWarningOpen] = useState(false)
  const [pendingDownload, setPendingDownload] = useState<{ urls: MediaUrl[] } | null>(null)

  const performDownload = async (urls: MediaUrl[], feedItem: FeedItem) => {
    const result = await downloadAsZip(urls, getMediaZipFilename(feedItem))
    if (result.failCount > 0) {
      toast.warning(`已下载 ${result.successCount} 个文件，${result.failCount} 个未完成`)
      return
    }

    toast.success(`媒体已下载（${result.successCount} 个文件）`)
  }

  const handleDownload = async () => {
    if (!item) return

    setDownloadLoading(true)

    try {
      const urls = extractMediaUrls(item)

      if (urls.length === 0) {
        toast.error('这条微博没有可下载的媒体')
        return
      }

      const totalSize = await estimateTotalSize(urls)
      const sizeMB = totalSize / (1024 * 1024)

      if (sizeMB > 100) {
        setPendingDownload({ urls })
        setSizeWarningOpen(true)
        return
      }

      await performDownload(urls, item)
    } catch (error) {
      console.error('下载失败:', error)
      toast.error('媒体下载失败，请稍后再试')
    } finally {
      setDownloadLoading(false)
    }
  }

  const confirmLargeDownload = async () => {
    if (!pendingDownload || !item) return

    setSizeWarningOpen(false)
    setDownloadLoading(true)

    try {
      await performDownload(pendingDownload.urls, item)
    } catch (error) {
      console.error('下载失败:', error)
      toast.error('媒体下载失败，请稍后再试')
    } finally {
      setDownloadLoading(false)
      setPendingDownload(null)
    }
  }

  const downloadDialog = (
    <Dialog open={sizeWarningOpen} onOpenChange={setSizeWarningOpen}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>媒体文件超过 100MB</DialogTitle>
          <DialogDescription>这条微博的媒体文件较大，下载可能需要更长时间。</DialogDescription>
        </DialogHeader>
        <DialogFooter>
          <Button
            type="button"
            variant="outline"
            onClick={() => {
              setSizeWarningOpen(false)
              setPendingDownload(null)
            }}
          >
            暂不下载
          </Button>
          <Button type="button" onClick={confirmLargeDownload} disabled={downloadLoading}>
            {downloadLoading ? '下载中…' : '继续下载媒体'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )

  return {
    downloadDialog,
    downloadLoading,
    handleDownload,
  }
}
