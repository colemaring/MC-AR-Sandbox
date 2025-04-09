const { ipcMain, app } = require('electron')
const fs = require('fs')
const path = require('path')
const configPath = path.join(__dirname, '../../settings_config.json')

export async function readConfig() {
  try {
    const data = fs.readFileSync(configPath, 'utf8')
    console.log('Config read successfully')
    return JSON.parse(data)
  } catch (error) {
    console.error('Failed to read config:', error)
    return {} // Return a default object or handle the error as needed
  }
}

async function writeConfig(event, newConfig) {
  try {
    fs.writeFileSync(configPath, JSON.stringify(newConfig, null, 2), 'utf8')
    console.log('Config written successfully')
    return { success: true }
  } catch (error) {
    console.error('Failed to write config:', error)
    return { success: false, error: error.message }
  }
}

ipcMain.handle('readConfig', readConfig)
ipcMain.handle('writeConfig', writeConfig)
