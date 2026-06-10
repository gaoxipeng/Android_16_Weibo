const INVALID_FILENAME_CHARS = /[\\/:*?"<>|]/g

export function sanitizeFilename(name: string): string {
  return name.replaceAll(INVALID_FILENAME_CHARS, '_')
}
