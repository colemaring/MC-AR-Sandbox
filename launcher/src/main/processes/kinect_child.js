const { ipcMain } = require('electron')
const Kinect2 = require('kinect2')
const kinect = new Kinect2()

// Add throttling variables
// Update can be slow as data is only being used to see what regions need to be cropped
let lastSentTimestamp = 0
const THROTTLE_INTERVAL = 3000

const main = async () => {
  if (kinect.open()) {
    console.log('Kinect Opened in child process')
    kinect.openDepthReader()

    kinect.on('depthFrame', (depthFrame) => {
      const currentTime = Date.now()

      // This logic will need to be changed because the data going to the mc plugin will need to be UNTHROTTLED but data
      // to the parent process will need THROTTLED.

      // Only send data at specified intervals
      if (currentTime - lastSentTimestamp < THROTTLE_INTERVAL) {
        return
      }

      lastSentTimestamp = currentTime

      const width = 512
      const height = 424
      let depthArray = []

      for (let y = 0; y < height; y++) {
        let row = []
        for (let x = 0; x < width; x++) {
          row.push(depthFrame[y * width + x])
        }
        depthArray.push(row)
      }

      // Send data to the parent process
      process.send({ type: 'depthData', data: depthArray })
    })
  } else {
    console.log('Failed to open Kinect in child process')
  }
}

main()
