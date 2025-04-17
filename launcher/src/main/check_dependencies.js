import { exec } from 'child_process'
import fs from 'fs'
import path from 'path'
import { sendLogMessage } from './index' // Assuming this is your main file
import { checkAndGetServerJar } from './build_server_jar'
import { startMinecraftServer } from './mc_server'
import { readConfig } from './config_handler' // Import readConfig function

/**
 * Checks for Java 21+ and PrismLauncher existence.
 */
export async function checkDependencies() {
  // Check for Java 21+
  const javaVersionCheck = await checkJavaVersion()
  if (javaVersionCheck.success) {
    sendLogMessage(javaVersionCheck.message, 'success')
  } else {
    sendLogMessage(javaVersionCheck.message, 'error')
  }

  // Check for PrismLauncher existence
  const prismLauncherCheck = await checkPrismLauncher()
  if (prismLauncherCheck.success) {
    sendLogMessage(prismLauncherCheck.message, 'success')
    sendLogMessage('PrismLauncher is ready!', 'success')
  } else {
    sendLogMessage(prismLauncherCheck.message, 'error')
  }

  // Check for Kinect SDK
  const kinectSDKCheck = await checkKinectSDK()
  if (kinectSDKCheck.success) {
    sendLogMessage(kinectSDKCheck.message, 'success')
  } else {
    sendLogMessage(kinectSDKCheck.message, 'error')
    // This is a critical error for Kinect functionality
  }

  // Start Minecraft server when the app is ready
  const serverStarted = await startMinecraftServer()
  if (serverStarted) {
    // sendLogMessage('Minecraft server started on application ready', 'success')
  } else {
    sendLogMessage('Failed to start Minecraft server on application ready', 'error')
  }

  checkAndGetServerJar().then((success) => {
    if (success) {
      console.log('Server JAR is ready to use')
      // Proceed with other initialization
    } else {
      console.error('Failed to ensure server JAR exists')
    }
  })
}

/**
 * Checks if Java 21 or higher is installed and accessible in the system's PATH.
 * @returns {Promise<{success: boolean, message: string}>} - An object with a success flag and a message.
 */
async function checkJavaVersion() {
  return new Promise((resolve) => {
    exec('java -version', (error, stdout, stderr) => {
      if (error) {
        resolve({
          success: false,
          message: `Java check failed: Java is not installed or not in PATH. Error: ${error.message}`
        })
        return
      }

      const versionLine = stderr.split('\n')[0]
      const javaVersion = versionLine.substring(
        versionLine.indexOf('"') + 1,
        versionLine.lastIndexOf('"')
      )

      const majorVersion = parseInt(javaVersion.split('.')[0])

      if (majorVersion >= 21) {
        resolve({
          success: true,
          message: `Java ${javaVersion} is installed.`
        })
      } else {
        resolve({
          success: false,
          message: `Java version is ${javaVersion}. Java 21 or higher is required.`
        })
      }
    })
  })
}

/**
 * Checks if PrismLauncher exists at the path specified in the settings config.
 * @returns {Promise<{success: boolean, message: string}>} - An object with a success flag and a message.
 */
async function checkPrismLauncher() {
  try {
    const config = await readConfig()

    // Get the PrismLauncher path from the config
    const prismLauncherPath = config?.minecraft_prismlauncher_path

    if (!prismLauncherPath) {
      return {
        success: false,
        message: 'PrismLauncher path is not defined in settings_config.json.'
      }
    }

    // Check if the file exists
    if (fs.existsSync(prismLauncherPath)) {
      return {
        success: true,
        message: `PrismLauncher found at ${prismLauncherPath}`
      }
    } else {
      return {
        success: false,
        message: `PrismLauncher not found at ${prismLauncherPath}`
      }
    }
  } catch (error) {
    return {
      success: false,
      message: `Error checking PrismLauncher: ${error.message}`
    }
  }
}

async function checkKinectSDK() {
  // Only run this check on Windows
  if (process.platform !== 'win32') {
    return { success: true, message: 'Kinect SDK check skipped (not on Windows).' }
  }

  // Registry key for Kinect SDK v2
  const registryKey = '"HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Kinect\\v2.0"'

  return new Promise((resolve) => {
    // Use reg query command to check if the key exists
    exec(`reg query ${registryKey}`, (error, stdout, stderr) => {
      if (error) {
        // If the command fails, the key likely doesn't exist (or another error occurred)
        resolve({
          success: false,
          message: 'Kinect SDK v2 not found in registry. Please install it.'
        })
      } else {
        // If the command succeeds, the key exists
        resolve({
          success: true,
          message: 'Kinect SDK v2 found.'
        })
      }
    })
  })
}
