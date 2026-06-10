/**
 * Global registry that ensures only one video plays at a time.
 *
 * When a video starts playing, any previously-playing video is paused
 * and exits Picture-in-Picture mode (if active).
 */
let playingVideo: HTMLVideoElement | null = null

export function registerPlayingVideo(video: HTMLVideoElement): void {
  if (playingVideo === video) return

  const previous = playingVideo
  playingVideo = video

  if (previous) {
    exitPictureInPicture(previous)
    previous.pause()
  }
}

export function unregisterPlayingVideo(video: HTMLVideoElement): void {
  if (playingVideo === video) {
    playingVideo = null
  }
}

async function exitPictureInPicture(video: HTMLVideoElement): Promise<void> {
  try {
    if (document.pictureInPictureElement === video) {
      await document.exitPictureInPicture()
    }
  } catch {
    // ignore PiP exit failures
  }
}
