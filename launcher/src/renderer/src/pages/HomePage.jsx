import React, { useState, useEffect, useRef } from 'react'
import Button from 'react-bootstrap/Button'
import Form from 'react-bootstrap/Form'
import { useLogMessages } from '../context/LogMessageContext'

function HomePage() {
  const { logMessages, getFormattedLogs } = useLogMessages()
  const textareaRef = useRef(null)
  const [launchButtonDisabled, setLaunchButtonDisabled] = useState(false) // Initialize to true

  useEffect(() => {
    if (textareaRef.current) {
      textareaRef.current.scrollTop = textareaRef.current.scrollHeight
    }
    console.log('Log messages updated:', logMessages)
  }, [logMessages])

  const handleLaunchClick = () => {
    // In your renderer process
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
    <div className="pageContainer">
      <Form.Group className="mb-3" controlId="exampleForm.ControlTextarea1">
        <div
          className="logTextBox"
          ref={textareaRef}
          dangerouslySetInnerHTML={{ __html: getFormattedLogs() }}
        />
      </Form.Group>
      <Button
        className="launchButton buttonDropshadow"
        onClick={handleLaunchClick}
        disabled={launchButtonDisabled}
      >
        {launchButtonDisabled ? 'Launching...' : 'Launch Minecraft & Projection'}
      </Button>
    </div>
  )
}

export default HomePage
