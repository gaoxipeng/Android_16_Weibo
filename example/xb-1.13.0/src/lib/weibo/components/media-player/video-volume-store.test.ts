import { beforeEach, describe, expect, it } from 'vitest'

import {
  applyStoredVideoVolume,
  registerVideoVolumeElement,
  rememberVideoVolumeFromElement,
  resetVideoVolumeStoreForTest,
} from './video-volume-store'

const STORAGE_KEY = 'xb:weibo-video-volume'

function createVideo(volume = 1) {
  const video = document.createElement('video')
  video.volume = volume
  return video
}

describe('video-volume-store', () => {
  beforeEach(() => {
    localStorage.clear()
    resetVideoVolumeStoreForTest()
  })

  it('applies the last stored volume to new video elements', () => {
    localStorage.setItem(STORAGE_KEY, '0.35')
    const video = createVideo()

    applyStoredVideoVolume(video)

    expect(video.volume).toBe(0.35)
  })

  it('persists changed volume and shares it with registered videos', () => {
    const firstVideo = createVideo(1)
    const secondVideo = createVideo(1)
    const unregisterFirst = registerVideoVolumeElement(firstVideo)
    const unregisterSecond = registerVideoVolumeElement(secondVideo)

    firstVideo.volume = 0.42
    rememberVideoVolumeFromElement(firstVideo)

    expect(localStorage.getItem(STORAGE_KEY)).toBe('0.42')
    expect(secondVideo.volume).toBe(0.42)

    unregisterFirst()
    unregisterSecond()
  })

  it('does not persist volumechange events when the numeric volume did not change', () => {
    const video = createVideo(0.8)
    const unregister = registerVideoVolumeElement(video)

    rememberVideoVolumeFromElement(video)

    expect(localStorage.getItem(STORAGE_KEY)).toBeNull()
    unregister()
  })
})
