'use strict'
const electron = require('electron')
const preload = require('@electron-toolkit/preload')
const api = {}
if (process.contextIsolated) {
  try {
    if (!window.electron) {
      electron.contextBridge.exposeInMainWorld('electron', preload.electronAPI)
    }
    electron.contextBridge.exposeInMainWorld('api', api)
    electron.contextBridge.exposeInMainWorld('electronAPI', {
      readConfig: () => electron.ipcRenderer.invoke('readConfig'),
      writeConfig: (config) => electron.ipcRenderer.invoke('writeConfig', config),
      onKinectDepthData: (callback) =>
        electron.ipcRenderer.on('kinect-depth-data', (_, data) => callback(data)),
      logMessage: (callback) =>
        electron.ipcRenderer.on('logMessage', (_, statusData) => callback(statusData)),
      ipcRenderer: {
        // Add this
        send: (channel, data) => electron.ipcRenderer.send(channel, data)
      }
    })
    electron.contextBridge.exposeInMainWorld('customElectron', {
      openExternal: (url) => electron.shell.openExternal(url)
    })
  } catch (error) {
    console.error(error)
  }
} else {
  window.electron = preload.electronAPI
  window.api = api
  window.customElectron = {
    openExternal: (url) => electron.shell.openExternal(url)
  }
}
