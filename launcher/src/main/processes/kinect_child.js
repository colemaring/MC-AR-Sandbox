const { ipcMain } = require('electron')
const Kinect2 = require('kinect2')
const kinect = new Kinect2()
const WebSocket = require('ws') // Import the ws library

// Add throttling variables only for parent process
let lastParentSendTimestamp = 0
const PARENT_THROTTLE_INTERVAL = 3000 // Throttle for parent process

// WebSocket server setup
const wss = new WebSocket.Server({ port: 8080 }) // Choose a port
console.log('WebSocket server started on port 8080')

wss.on('connection', (ws) => {
  console.log('Client connected to WebSocket')

  ws.on('close', () => {
    console.log('Client disconnected from WebSocket')
  })
})

const main = async () => {
  if (kinect.open()) {
    console.log('Kinect Opened in child process')
    kinect.openDepthReader()

    kinect.on('depthFrame', (depthFrame) => {
      const currentTime = Date.now()

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

      // Always send unthrottled data to WebSocket clients
      wss.clients.forEach((client) => {
        if (client.readyState === WebSocket.OPEN) {
          client.send(JSON.stringify({ type: 'depthData', data: depthArray }))
        }
      })

      // Send throttled data to parent process
      if (currentTime - lastParentSendTimestamp >= PARENT_THROTTLE_INTERVAL) {
        lastParentSendTimestamp = currentTime
        // Send data to the parent process
        process.send({ type: 'depthData', data: depthArray })
      }
    })
  } else {
    console.log('Failed to open Kinect in child process')
  }
}

main()
