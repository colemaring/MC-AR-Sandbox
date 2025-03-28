const { exec } = require('child_process')

/**
 * Terminates all processes, including Kinect and Java processes.
 * @param {object} kinectProcess - The Kinect process object.
 * @returns {Promise<boolean>} - True if all processes were terminated successfully, false otherwise.
 */
export async function terminateAllProcesses(kinectProcess) {
  console.log('Terminating all processes...')
  let success = true

  // Kill Kinect process
  if (kinectProcess) {
    console.log('Killing Kinect process...')
    try {
      kinectProcess.kill('SIGKILL')
    } catch (e) {
      console.error(`Error killing Kinect process: ${e.message}`)
      success = false
    }
  }

  // Force kill all Java processes using taskkill on Windows
  if (process.platform === 'win32') {
    console.log('Force killing all Java processes...')
    // I think this is necessary
    try {
      exec('taskkill /F /IM java.exe /T', (error) => {
        if (error) {
          console.error(`Error killing Java processes: ${error}`)
          success = false
        } else {
          console.log('Successfully terminated all Java processes')
        }
      })
    } catch (e) {
      console.error('Failed to execute taskkill command:', e)
      success = false
    }
  }

  return success
}
