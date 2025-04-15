import React, { useState } from 'react'
import Button from 'react-bootstrap/Button'
import Form from 'react-bootstrap/Form'
import InputGroup from 'react-bootstrap/InputGroup'

function SendCommands() {
  const [inputText, setInputText] = useState('')

  const handleInputChange = (e) => {
    setInputText(e.target.value)
  }

  const handleKeyPress = (e) => {
    if (e.key === 'Enter') {
      e.preventDefault() // Prevent form submission
      handleSendCommand()
    }
  }

  const handleSendCommand = () => {
    if (inputText.trim() === '') return // Don't send empty commands

    // Send IPC message with the command text
    window.electronAPI.ipcRenderer.send('serverCommand', inputText)

    // Clear the input after sending
    setInputText('')
  }

  return (
    <InputGroup className="mb-3">
      <Form.Control
        type="text"
        placeholder="Send command to Minecraft server"
        value={inputText}
        onChange={handleInputChange}
        onKeyDown={handleKeyPress} // listen for enter
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
  )
}

export default SendCommands
