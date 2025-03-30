const { fork } = require('child_process')
const { join } = require('path')
import { sendLogMessage } from './index'

export function startKinectProcess(mainWindow) {
  const kinectProcess = fork(join(__dirname, 'kinect_child.js'))
  sendLogMessage(`Waiting on data from kinect`, 'normal')
  let flag = false
  let kinectWaitInterval
  let waitCount = 0 // Counter for "waiting" messages

  // Function to send "waiting on kinect" message
  const sendWaitingMessage = () => {
    waitCount++
    const messageType = waitCount > 3 ? 'warning' : 'normal' // Change to warning after 3 messages
    sendLogMessage(`Waiting on data from kinect`, messageType)
  }

  // Set interval to send message every 3 seconds
  kinectWaitInterval = setInterval(sendWaitingMessage, 3000)

  kinectProcess.on('message', (message) => {
    if (message.type === 'depthData') {
      if (flag == false) {
        sendLogMessage('Kinect is ready!', 'success')
        // Clear the interval when Kinect is ready
        clearInterval(kinectWaitInterval)
        flag = true
      }

      //   console.log('Received depth data in main process')
      // Send the depth data to the renderer process
      if (mainWindow && !mainWindow.isDestroyed()) {
        mainWindow.webContents.send('kinect-depth-data', message.data)
      }
    }
  })

  kinectProcess.on('exit', (code) => {
    console.log(`Kinect process exited with code ${code}`)
    // Clear the interval if Kinect process exits
    clearInterval(kinectWaitInterval)
  })

  return kinectProcess
}
