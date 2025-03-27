import { contextBridge, shell, ipcRenderer } from 'electron'
import { electronAPI } from '@electron-toolkit/preload'

// Custom APIs for renderer
const api = {}

// Use `contextBridge` APIs to expose Electron APIs to
// renderer only if context isolation is enabled, otherwise
// just add to the DOM global.
if (process.contextIsolated) {
  try {
    // Expose Electron's default API only if not already set
    if (!window.electron) {
      contextBridge.exposeInMainWorld('electron', electronAPI)
    }
    contextBridge.exposeInMainWorld('api', api)

    // Handles config file read and write
    contextBridge.exposeInMainWorld('electronAPI', {
      readConfig: () => ipcRenderer.invoke('readConfig'),
      writeConfig: (config) => ipcRenderer.invoke('writeConfig', config)
    })

    // handles opening external links from renderer process
    contextBridge.exposeInMainWorld('customElectron', {
      openExternal: (url) => shell.openExternal(url)
    })
  } catch (error) {
    console.error(error)
  }
} else {
  window.electron = electronAPI
  window.api = api
  window.customElectron = {
    openExternal: (url) => shell.openExternal(url)
  }
}
