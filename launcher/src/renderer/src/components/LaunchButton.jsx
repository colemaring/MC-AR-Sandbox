import React, { useState, useEffect } from 'react'
import Button from 'react-bootstrap/Button'

function LaunchButton() {
  const [launchButtonDisabled, setLaunchButtonDisabled] = useState(true) // Start disabled
  const [loadingText, setLoadingText] = useState('Initializing...') // New state for loading text

  // Effect to enable button after initial loading period
  useEffect(() => {
    const timer = setTimeout(() => {
      setLaunchButtonDisabled(false)
      setLoadingText('') // Clear the loading text
    }, 2000) // 2 seconds

    return () => clearTimeout(timer) // Cleanup on unmount
  }, []) // Empty dependency array means this runs once on mount

  const handleLaunchClick = () => {
    setLaunchButtonDisabled(true) // Disable the button when clicked
    setLoadingText('Launching...') // Set launching text
    window.electronAPI.ipcRenderer.send('start-launch', '1.21.5')
  }

  useEffect(() => {
    const handleMinecraftReady = () => {
      setLaunchButtonDisabled(false) // Enable the button when Minecraft is ready
      setLoadingText('') // Clear loading text when ready
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
      {launchButtonDisabled ? loadingText || 'Launching...' : 'Launch Minecraft & Projection'}
    </Button>
  )
}

export default LaunchButton
