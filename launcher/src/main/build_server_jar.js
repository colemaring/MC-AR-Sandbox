import { spawn } from 'child_process'
import fs from 'fs'
import path from 'path'
import { sendLogMessage } from './index'
import { startMinecraftServer } from './mc_server' // Add this import

/**
 * Checks if the Spigot server JAR exists, and if not, runs BuildTools to generate it
 * @returns {Promise<boolean>} True if server JAR exists or was successfully created
 */
export async function checkAndGetServerJar() {
  // Define paths
  const serverDir = path.join(__dirname, '../../server')
  const serverJarPath = path.join(serverDir, 'spigot-1.21.5.jar')
  const buildToolsPath = path.join(__dirname, '../../server/BuildTools.jar')

  // Check if server JAR already exists
  if (fs.existsSync(serverJarPath)) {
    sendLogMessage('Spigot server JAR found.', 'success')
    return true
  }

  // Check if BuildTools exists
  if (!fs.existsSync(buildToolsPath)) {
    sendLogMessage('BuildTools.jar not found in server directory.', 'error')
    return false
  }

  // If JAR doesn't exist, run BuildTools
  sendLogMessage('Spigot server JAR not found. Running BuildTools to generate it...', 'warning')

  return new Promise((resolve) => {
    try {
      // Run BuildTools.jar with the specified version
      const buildProcess = spawn('java', ['-jar', buildToolsPath, '--rev', '1.21.5'], {
        cwd: serverDir // Run in the server directory
      })

      // Forward stdout logs
      buildProcess.stdout.on('data', (data) => {
        const output = data.toString().trim()
        console.log(`[BuildTools] ${output}`)
        sendLogMessage(`BuildTools: ${output}`, 'normal')
      })

      // Forward stderr logs
      buildProcess.stderr.on('data', (data) => {
        const output = data.toString().trim()
        console.error(`[BuildTools Error] ${output}`)
        sendLogMessage(`BuildTools Error: ${output}`, 'error')
      })

      // Handle process completion
      buildProcess.on('close', (code) => {
        if (code === 0) {
          sendLogMessage('BuildTools completed successfully. Server JAR created.', 'success')

          // Verify the JAR was actually created
          if (fs.existsSync(serverJarPath)) {
            // First resolve the promise to indicate success
            resolve(true)

            // Then attempt to start the Minecraft server
            sendLogMessage(
              'Attempting to start Minecraft server with newly created JAR...',
              'normal'
            )

            // Wrap in setTimeout to ensure promise is resolved first
            setTimeout(async () => {
              try {
                const serverStarted = await startMinecraftServer()
                if (serverStarted) {
                  sendLogMessage(
                    'Successfully started Minecraft server after creating JAR',
                    'success'
                  )
                } else {
                  sendLogMessage('Failed to start Minecraft server after creating JAR', 'error')
                }
              } catch (error) {
                sendLogMessage(
                  `Error starting Minecraft server after creating JAR: ${error.message}`,
                  'error'
                )
              }
            }, 2000)
          } else {
            sendLogMessage(
              'BuildTools completed but server JAR not found. Check BuildTools output.',
              'error'
            )
            resolve(false)
          }
        } else {
          sendLogMessage(`BuildTools failed with exit code ${code}.`, 'error')
          resolve(false)
        }
      })

      // Handle process errors
      buildProcess.on('error', (err) => {
        sendLogMessage(`Failed to start BuildTools: ${err.message}`, 'error')
        resolve(false)
      })
    } catch (error) {
      sendLogMessage(`Error running BuildTools: ${error.message}`, 'error')
      resolve(false)
    }
  })
}
