export {}

declare global {
  interface Window {
    electron?: {
      openFolder: () => Promise<string | null>
      openPath: (path: string) => Promise<string>
    }
  }
}
