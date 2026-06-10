const VIDEO_VOLUME_STORAGE_KEY = 'xb:weibo-video-volume'

const registeredVideos = new Set<HTMLVideoElement>()
const lastKnownVolumes = new WeakMap<HTMLVideoElement, number>()
let cachedVolume: number | null | undefined

function normalizeVolume(value: unknown): number | null {
  const numericValue =
    typeof value === 'number' ? value : typeof value === 'string' ? Number(value) : Number.NaN

  if (!Number.isFinite(numericValue)) {
    return null
  }

  return Math.min(1, Math.max(0, numericValue))
}

function readStoredVolume(): number | null {
  if (cachedVolume !== undefined) {
    return cachedVolume
  }

  try {
    cachedVolume = normalizeVolume(localStorage.getItem(VIDEO_VOLUME_STORAGE_KEY))
  } catch {
    cachedVolume = null
  }

  return cachedVolume
}

function writeStoredVolume(volume: number) {
  cachedVolume = volume

  try {
    localStorage.setItem(VIDEO_VOLUME_STORAGE_KEY, String(volume))
  } catch {
    // localStorage may be unavailable in privacy modes or restricted contexts.
  }
}

export function applyStoredVideoVolume(video: HTMLVideoElement) {
  const volume = readStoredVolume()

  if (volume === null || Math.abs(video.volume - volume) < 0.001) {
    lastKnownVolumes.set(video, video.volume)
    return
  }

  lastKnownVolumes.set(video, volume)
  video.volume = volume
}

export function rememberVideoVolumeFromElement(video: HTMLVideoElement) {
  const volume = normalizeVolume(video.volume)

  if (volume === null) {
    return
  }

  const lastKnownVolume = lastKnownVolumes.get(video)
  lastKnownVolumes.set(video, volume)

  if (lastKnownVolume !== undefined && Math.abs(lastKnownVolume - volume) < 0.001) {
    return
  }

  if (Math.abs((cachedVolume ?? -1) - volume) < 0.001) {
    return
  }

  writeStoredVolume(volume)

  for (const registeredVideo of registeredVideos) {
    if (Math.abs(registeredVideo.volume - volume) >= 0.001) {
      lastKnownVolumes.set(registeredVideo, volume)
      registeredVideo.volume = volume
    }
  }
}

export function registerVideoVolumeElement(video: HTMLVideoElement): () => void {
  registeredVideos.add(video)
  applyStoredVideoVolume(video)

  return () => {
    registeredVideos.delete(video)
  }
}

export function resetVideoVolumeStoreForTest() {
  registeredVideos.clear()
  cachedVolume = undefined
}
