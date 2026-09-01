import { contextBridge, ipcRenderer } from 'electron'

contextBridge.exposeInMainWorld('env', {
  apiBase: 'http://localhost:7089'
})

contextBridge.exposeInMainWorld('electron', {
  openFolder: (): Promise<string | null> => ipcRenderer.invoke('dialog:openFolder'),
  openPath:   (path: string): Promise<string> => ipcRenderer.invoke('shell:openPath', path),
})
