import React, { useState, useEffect } from 'react'
import Button from 'react-bootstrap/Button'

function LaunchButton() {
  const [launchButtonDisabled, setLaunchButtonDisabled] = useState(false)

  const handleLaunchClick = () => {
    setLaunchButtonDisabled(true) // Disable the button when clicked
    window.electronAPI.ipcRenderer.send('launch-prism', '1.21.5')
  }

  useEffect(() => {
    const handleMinecraftReady = () => {
      setLaunchButtonDisabled(false) // Enable the button when Minecraft is ready
    }

    window.electronAPI.onMinecraftReady(handleMinecraftReady)

    return () => {
      window.electronAPI.onMinecraftReady(handleMinecraftReady)
    }
  }, [])

  return (
    <Button
      className="launchButton buttonDropshadow"
      onClick={handleLaunchClick}
      disabled={launchButtonDisabled}
    >
      {launchButtonDisabled ? 'Launching...' : 'Launch Minecraft & Projection'}
    </Button>
  )
}

export default LaunchButton
