const { fork } = require('child_process')
const { join } = require('path')
import { sendLogMessage } from './index'

export function startKinectProcess(mainWindow) {
  const kinectProcess = fork(join(__dirname, 'kinect_child.js'))
  sendLogMessage(`Waiting on data from kinect`, 'normal')
  let flag = false
  kinectProcess.on('message', (message) => {
    if (message.type === 'depthData') {
      if (flag == false) {
        sendLogMessage('Receiving data from kinect', 'success')
        // PROGRAM SHOULD NOT CONTINUE UNTIL FLAG HAS BEEN SET TO TRUE
        // eg. dont trigger mc server launch
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
  })

  return kinectProcess
}
