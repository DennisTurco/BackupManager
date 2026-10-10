import { contextBridge, ipcRenderer } from 'electron'

contextBridge.exposeInMainWorld('env', {
  apiBase: 'http://127.0.0.1:7089'
})

contextBridge.exposeInMainWorld('electron', {
  openFolder: (): Promise<string | null> => ipcRenderer.invoke('dialog:openFolder'),
  openPath:   (path: string): Promise<string> => ipcRenderer.invoke('shell:openPath', path),
})
