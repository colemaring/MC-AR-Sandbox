const { spawn, exec } = require('child_process')
const path = require('path')
const fs = require('fs')
import { sendLogMessage } from './index'
import { app } from 'electron'

let serverProcess = null
let serverRunning = false
let serverReady = false // Add a flag to track server readiness
let startingInterval // Variable to hold the interval ID

// Configure Java settings here
const JAVA_PATH = 'java' // Use 'java' to use system's default Java or specify full path
const SERVER_JAR = 'spigot-1.21.5.jar' // Your server JAR filename
const SERVER_DIR = app.isPackaged ? process.resourcesPath : path.join(__dirname, '../../server')

const MIN_RAM = '1G' // Minimum RAM allocation
const MAX_RAM = '4G' // Maximum RAM allocation

async function killExistingMinecraftServer() {
  return new Promise((resolve) => {
    if (process.platform === 'win32') {
      console.log('Checking for running Java processes')
      sendLogMessage('Checking for running Java processes', 'normal')

      // Kill all Java processes forcefully
      exec('taskkill /F /IM java.exe', (killError) => {
        if (killError) {
          // If error is none found that's good
          if (killError.message.includes('not found')) {
            console.log('No Java processes found to kill')
            sendLogMessage('No Java processes found to kill', 'success')
            resolve(true)
            // Othewise error is probably a bad thing and log it
          } else {
            console.error(`Error killing Java processes: ${killError}`)
            sendLogMessage('Failed to kill Java processes', 'error')
            resolve(false)
          }
          // If no kill error, then there must have been a java process and it was killed
        } else {
          console.log('Java processes were found and killed')
          sendLogMessage('Java processes were found and killed', 'warning')
          // Wait a moment to make sure processes are fully terminated
          setTimeout(() => resolve(true), 1000)
        }
      })
    }
  })
}

/**
 * Start the Minecraft server
 * @returns {Promise<boolean>} True if server started successfully
 */
export async function startMinecraftServer() {
  if (serverRunning) {
    sendLogMessage('Minecraft server is already running', 'warning')
    return true
  }

  try {
    // First kill any existing Minecraft server processes
    const killResult = await killExistingMinecraftServer()

    // Check if server jar exists
    const jarPath = path.join(SERVER_DIR, SERVER_JAR)
    if (!fs.existsSync(jarPath)) {
      sendLogMessage(`Server JAR not found at ${jarPath}`, 'error')
      return false
    }

    // Java parameters for launching the server
    const javaArgs = [
      `-Xms${MIN_RAM}`, // Minimum RAM allocation
      `-Xmx${MAX_RAM}`, // Maximum RAM allocation
      '-DIReallyKnowWhatIAmDoingISwear', // Remove update message
      '-jar',
      SERVER_JAR,
      'nogui' // Run without GUI
    ]

    // Spawn the Java process
    serverProcess = spawn(JAVA_PATH, javaArgs, {
      cwd: SERVER_DIR, // Working directory for the server
      stdio: ['pipe', 'pipe', 'pipe'], // Enable stdin, stdout, stderr pipes\
      detached: false
    })

    // Set server as running
    serverRunning = true
    serverReady = false // Reset the serverReady flag
    sendLogMessage('Minecraft server starting...', 'normal')

    // Function to send "Minecraft server starting..." message
    const sendStartingMessage = () => {
      if (!serverReady) {
        sendLogMessage('Minecraft server starting...', 'normal')
      } else {
        clearInterval(startingInterval) // Stop the interval if server is ready
      }
    }

    // Set interval to send message every 3 seconds
    startingInterval = setInterval(sendStartingMessage, 3000)

    // Handle server output
    serverProcess.stdout.on('data', (data) => {
      const output = data.toString().trim()

      // Log server output
      console.log(`[MC Server] ${output}`)

      // Detect server ready message
      if (output.includes('Done') && output.includes('For help, type "help"')) {
        sendLogMessage('Minecraft server is ready!', 'success')
        serverReady = true // Set the serverReady flag
        clearInterval(startingInterval) // Stop the interval when server is ready
      }

      // You can parse and send specific messages to the renderer
      //sendLogMessage(`Server: ${output}`, 'normal')
    })

    // Handle server errors
    serverProcess.stderr.on('data', (data) => {
      const errorOutput = data.toString().trim()
      console.error(`[MC Server Error] ${errorOutput}`)
      //sendLogMessage(`Server Error: ${errorOutput}`, 'error')
    })

    // Handle server exit
    serverProcess.on('exit', (code, signal) => {
      serverRunning = false
      serverReady = false // Reset the serverReady flag
      clearInterval(startingInterval) // Clear the interval when server exits

      if (code === 0) {
        sendLogMessage(
          'Minecraft server stopped gracefully. If this is the first launch, ensure eula=true. See the GitHub readme instructions.',
          'warning'
        )
      } else {
        sendLogMessage(`Minecraft server crashed (code: ${code}, signal: ${signal})`, 'error')
      }

      serverProcess = null
    })

    // Handle unexpected errors
    serverProcess.on('error', (err) => {
      serverRunning = false
      serverReady = false // Reset the serverReady flag
      clearInterval(startingInterval) // Clear the interval on error
      sendLogMessage(`Failed to start Minecraft server: ${err.message}`, 'error')
      serverProcess = null
      return false
    })

    return true
  } catch (error) {
    sendLogMessage(`Error starting Minecraft server: ${error.message}`, 'error')
    serverRunning = false
    serverReady = false // Reset the serverReady flag
    return false
  }
}

export function isServerRunning() {
  return serverRunning
}

export function getServerDirectory() {
  return SERVER_DIR
}

export { serverProcess }
