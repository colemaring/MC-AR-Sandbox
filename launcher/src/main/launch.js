import { exec, spawn } from 'child_process'
import fs from 'fs'
import path from 'path'
import { sendLogMessage } from './index' // Assuming this is your main file
import { ipcMain } from 'electron' // Import ipcMain

/**
 * Launches PrismLauncher with the specified instance.
 * @param {string} instanceName - The name of the PrismLauncher instance to launch.
 * @returns {Promise<boolean>} - True if PrismLauncher was launched successfully, false otherwise.
 */
export async function launchPrismLauncher(instanceName, mainWindow) {
  // Add mainWindow parameter
  try {
    // 1. Read the config file
    const configPath = path.join(__dirname, '../../settings_config.json')
    const configData = fs.readFileSync(configPath, 'utf8')
    const config = JSON.parse(configData)

    // 2. Get the PrismLauncher path from the config
    const prismLauncherPath = config?.minecraft_prismlauncher_path

    if (!prismLauncherPath) {
      sendLogMessage('PrismLauncher path is not defined in settings_config.json.', 'error')
      return false
    }

    // 3. Check if the PrismLauncher executable exists
    if (!fs.existsSync(prismLauncherPath)) {
      sendLogMessage(`PrismLauncher not found at ${prismLauncherPath}`, 'error')
      mainWindow.webContents.send('minecraft-ready', true)
      return false
    }

    // 4. Check if Minecraft is already running
    const isMinecraftRunning = await checkIfMinecraftIsRunning()
    if (isMinecraftRunning) {
      sendLogMessage(
        'Minecraft is already running. Please close it before launching again.',
        'warning'
      )
      mainWindow.webContents.send('minecraft-ready', true) // Immediately un-grey the button
      return false
    }

    // 5. Construct the command
    const command = `"${prismLauncherPath}" -l "${instanceName}" -s localhost` // Quote the path and instance name

    // 6. Launch PrismLauncher
    sendLogMessage(`Launching PrismLauncher with command: ${command}`, 'normal')

    exec(command, (error, stdout, stderr) => {
      if (error) {
        sendLogMessage(`PrismLauncher launch failed: ${error.message}`, 'error')
        console.error(`PrismLauncher launch error: ${error}`)
        return
      }
      if (stderr) {
        //sendLogMessage(`PrismLauncher stderr: ${stderr}`, 'warning')
        console.warn(`PrismLauncher stderr: ${stderr}`)
      }
      //sendLogMessage(`PrismLauncher stdout: ${stdout}`, 'normal')
      console.log(`PrismLauncher stdout: ${stdout}`)
    })

    // 7. Start checking for Minecraft
    let minecraftCheckInterval = setInterval(async () => {
      if (process.platform === 'win32') {
        exec(
          'tasklist /FI "imagename eq javaw.exe" /FI "windowtitle eq Minecraft*"',
          (error, stdout, stderr) => {
            if (error) {
              sendLogMessage(`Error checking for Minecraft process: ${error.message}`, 'error')
              console.error(`Error checking for Minecraft process: ${error}`)
              clearInterval(minecraftCheckInterval) // Stop checking on error
              return
            }

            const isRunning =
              stdout.toLowerCase().includes('javaw.exe') &&
              !stdout.toLowerCase().includes('info: no tasks')
            if (isRunning) {
              sendLogMessage('Minecraft is ready!', 'success')
              mainWindow.webContents.send('minecraft-ready', true) // Send IPC event
              clearInterval(minecraftCheckInterval) // Stop checking
            }
          }
        )
      } else {
        // Implement macOS/Linux check here if needed
        clearInterval(minecraftCheckInterval) // Stop checking if not Windows
        sendLogMessage('Minecraft ready check is only implemented on Windows.', 'warning')
      }
    }, 2000)

    return true // Assume success (non-blocking)
  } catch (error) {
    sendLogMessage(`Error launching PrismLauncher: ${error.message}`, 'error')
    console.error(`Error launching PrismLauncher:`, error)
    return false
  }
}

/**
 * Checks if Minecraft is already running by checking window titles.
 * @returns {Promise<boolean>} - True if Minecraft is running, false otherwise.
 */
async function checkIfMinecraftIsRunning() {
  return new Promise((resolve) => {
    if (process.platform === 'win32') {
      // Windows-specific code
      exec(
        'tasklist /FI "imagename eq javaw.exe" /FI "windowtitle eq Minecraft*"',
        (error, stdout, stderr) => {
          if (error) {
            sendLogMessage(`Error checking for Minecraft process: ${error.message}`, 'error')
            console.error(`Error checking for Minecraft process: ${error}`)
            resolve(false)
            return
          }

          // If any output is returned, it means a Minecraft process is running
          const isRunning =
            stdout.toLowerCase().includes('javaw.exe') &&
            !stdout.toLowerCase().includes('info: no tasks')
          resolve(isRunning)
        }
      )
    } else {
      // macOS and Linux (less accurate, but better than nothing)
      exec('ps -ax | grep java', (error, stdout, stderr) => {
        if (error) {
          sendLogMessage(`Error checking for Minecraft process: ${error.message}`, 'error')
          console.error(`Error checking for Minecraft process: ${error}`)
          resolve(false)
          return
        }

        const isRunning =
          stdout.toLowerCase().includes('minecraft') && stdout.toLowerCase().includes('java')
        resolve(isRunning)
      })
    }
  })
}
