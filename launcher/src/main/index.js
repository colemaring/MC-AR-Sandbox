import { app, shell, BrowserWindow, ipcMain } from 'electron'
import { join } from 'path'
import { electronApp, optimizer, is } from '@electron-toolkit/utils'
const custom_icon = join(__dirname, '../../resources/icon.png')
import config_handler from './config_handler'
import { startKinectProcess } from './kinect'
import { startMinecraftServer } from './mc_server'
const { exec } = require('child_process')
import { terminateAllProcesses } from './terminate_processes'
import { checkDependencies } from './check_dependencies'
import { launchPrismLauncher } from './launch'

let kinectProcess
let mainWindow
let isQuitting = false

export function sendLogMessage(text, type = 'normal') {
  const windowToUse = BrowserWindow.getAllWindows().find((w) => !w.isDestroyed())
  if (windowToUse) {
    windowToUse.webContents.send('logMessage', { text, type })
  }
}

function createWindow() {
  // Create the browser window.
  mainWindow = new BrowserWindow({
    icon: custom_icon,
    width: 800,
    height: 685,
    show: false,
    autoHideMenuBar: true,
    // ...(process.platform === 'linux' ? { icon } : {}),
    webPreferences: {
      preload: join(__dirname, '../preload/index.js'),
      sandbox: false
    }
  })

  // Handle window close event
  mainWindow.on('close', async (e) => {
    if (!isQuitting) {
      e.preventDefault()
      isQuitting = true
      console.log('Window close event intercepted, shutting down services...')
      await terminateAllProcesses()
      mainWindow.destroy() // Force destroy the window after cleanup
    }
  })

  mainWindow.on('ready-to-show', () => {
    mainWindow.show()
    // Send initial logs after the window is ready and showing
    sendLogMessage('Application started', 'normal')
  })

  mainWindow.webContents.on('did-finish-load', async () => {
    kinectProcess = startKinectProcess(mainWindow)
    await checkDependencies()
    // Start Minecraft server when the app is ready
    const serverStarted = await startMinecraftServer()
    if (serverStarted) {
      // sendLogMessage('Minecraft server started on application ready', 'success')
    } else {
      sendLogMessage('Failed to start Minecraft server on application ready', 'error')
    }

    ipcMain.on('launch-prism', async (event, instanceName) => {
      const launched = await launchPrismLauncher(instanceName, mainWindow)
      if (launched) {
        sendLogMessage(`PrismLauncher launch requested for ${instanceName}`, 'normal')
      } else {
        sendLogMessage(`PrismLauncher launch failed for ${instanceName}`, 'error')
      }
    })
  })

  mainWindow.webContents.setWindowOpenHandler((details) => {
    shell.openExternal(details.url)
    return { action: 'deny' }
  })

  // HMR for renderer base on electron-vite cli.
  // Load the remote URL for development or the local html file for production.
  if (is.dev && process.env['ELECTRON_RENDERER_URL']) {
    mainWindow.loadURL(process.env['ELECTRON_RENDERER_URL'])
  } else {
    mainWindow.loadFile(join(__dirname, '../renderer/index.html'))
  }
}

// This method will be called when Electron has finished
// initialization and is ready to create browser windows.
// Some APIs can only be used after this event occurs.
app.whenReady().then(() => {
  // Set app user model id for windows
  electronApp.setAppUserModelId('com.electron')

  // Default open or close DevTools by F12 in development
  // and ignore CommandOrControl + R in production.
  // see https://github.com/alex8088/electron-toolkit/tree/master/packages/utils
  app.on('browser-window-created', (_, window) => {
    optimizer.watchWindowShortcuts(window)
  })

  createWindow()

  app.on('activate', function () {
    // On macOS it's common to re-create a window in the app when the
    // dock icon is clicked and there are no other windows open.
    if (BrowserWindow.getAllWindows().length === 0) createWindow()
  })
})

// Quit when all windows are closed, except on macOS. There, it's common
// for applications and their menu bar to stay active until the user quits
// explicitly with Cmd + Q.
app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') {
    app.quit()
  }
})

// Handle the before-quit event
app.on('before-quit', async (event) => {
  if (isQuitting) return // Prevent multiple calls

  event.preventDefault() // Prevent default quit
  isQuitting = true

  console.log('App is quitting, terminating all processes...')
  await terminateAllProcesses()

  // Now quit the app
  app.quit()
})

// In this file you can include the rest of your app's specific main process
// code. You can also put them in separate files and require them here.
