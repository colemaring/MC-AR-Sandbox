import React, { useState, useEffect, useRef } from 'react'
import Button from 'react-bootstrap/Button'
import Form from 'react-bootstrap/Form'
import { useLogMessages } from '../context/LogMessageContext'
import InputGroup from 'react-bootstrap/InputGroup'

function HomePage() {
  const { logMessages, getFormattedLogs } = useLogMessages()
  const textareaRef = useRef(null)
  const [launchButtonDisabled, setLaunchButtonDisabled] = useState(false) // Initialize to true
  const [inputText, setInputText] = useState('')

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

  const handleInputChange = (e) => {
    setInputText(e.target.value)
  }

  const handleSendCommand = () => {
    if (inputText.trim() === '') return // Don't send empty commands

    // Send IPC message with the command text
    window.electronAPI.ipcRenderer.send('serverCommand', inputText)

    // Clear the input after sending
    setInputText('')
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
      <InputGroup className="mb-3">
        <Form.Control
          type="text"
          placeholder="Send command to Minecraft server"
          value={inputText}
          onChange={handleInputChange}
        />
        <Button
          style={{
            backgroundColor: 'rgb(59, 125, 141)'
          }}
          onClick={handleSendCommand}
        >
          Send
        </Button>
      </InputGroup>
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
