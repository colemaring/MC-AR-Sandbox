const { fork } = require('child_process')
const path = require('path') // Use path module
const { app } = require('electron') // Import app
import { sendLogMessage } from './index'

export function startKinectProcess(mainWindow) {
  // Determine the correct path to kinect_child.js
  const childPath = app.isPackaged
    ? path.join(process.resourcesPath, 'kinect_child.js')
    : path.join(__dirname, 'kinect_child.js')

  try {
    const kinectProcess = fork(childPath, [], {
      cwd: path.dirname(childPath) // Set CWD to the script's directory
    })
    sendLogMessage(`Waiting on data from kinect`, 'normal')
    let flag = false
    let kinectWaitInterval
    let waitCount = 0 // Counter for "waiting" messages

    // Function to send "waiting on kinect" message
    const sendWaitingMessage = () => {
      waitCount++
      const messageType = waitCount > 0 ? 'warning' : 'normal' // Change to warning after 1 message
      sendLogMessage(`Waiting on data from kinect`, messageType)
    }

    // Set interval to send message every 10 seconds
    kinectWaitInterval = setInterval(sendWaitingMessage, 10000)

    kinectProcess.on('message', (message) => {
      if (message.type === 'depthData') {
        if (flag == false) {
          sendLogMessage('Kinect is ready!', 'success')
          // Clear the interval when Kinect is ready
          clearInterval(kinectWaitInterval)
          flag = true
        }

        // Send the depth data to the renderer process
        if (mainWindow && !mainWindow.isDestroyed()) {
          mainWindow.webContents.send('kinect-depth-data', message.data)
        }
      }
    })

    kinectProcess.on('error', (err) => {
      // Add error handling for fork
      sendLogMessage(`Failed to start Kinect child process: ${err.message}`, 'error')
      console.error('Fork Error:', err)
      if (kinectWaitInterval) clearInterval(kinectWaitInterval)
    })

    kinectProcess.on('exit', (code) => {
      sendLogMessage(`Kinect process exited with code ${code}`, code === 0 ? 'normal' : 'error')
      // Clear the interval if Kinect process exits
      if (kinectWaitInterval) clearInterval(kinectWaitInterval)
    })

    return kinectProcess
  } catch (error) {
    sendLogMessage(`Error during fork setup: ${error.message}`, 'error')
    console.error('Fork Catch Error:', error)
    return null // Indicate failure
  }
}
