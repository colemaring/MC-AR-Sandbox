import { useRef, useEffect } from 'react'
import Button from 'react-bootstrap/Button'
import Form from 'react-bootstrap/Form'
import { useLogMessages } from '../context/LogMessageContext'

function HomePage() {
  const { logMessages, getFormattedLogs } = useLogMessages()
  const textareaRef = useRef(null)

  const handleLaunchClick = () => {
    // In your renderer process
    window.electronAPI.ipcRenderer.send('launch-prism', '1.21.5')
    console.log('Launch button clicked')
  }

  useEffect(() => {
    if (textareaRef.current) {
      textareaRef.current.scrollTop = textareaRef.current.scrollHeight
    }
    console.log('Log messages updated:', logMessages)
  }, [logMessages])

  return (
    <div className="pageContainer">
      <Form.Group className="mb-3" controlId="exampleForm.ControlTextarea1">
        <div
          className="logTextBox"
          ref={textareaRef}
          dangerouslySetInnerHTML={{ __html: getFormattedLogs() }}
        />
      </Form.Group>
      <Button className="launchButton buttonDropshadow" onClick={handleLaunchClick}>
        LAUNCH
      </Button>
    </div>
  )
}

export default HomePage
