import { useRef, useEffect } from 'react'
import Button from 'react-bootstrap/Button'
import Form from 'react-bootstrap/Form'
import { useLogMessages } from '../context/LogMessageContext'

function HomePage() {
  const { logMessages, getFormattedLogs } = useLogMessages()
  const textareaRef = useRef(null)

  // Auto-scroll to the bottom when new logs are added
  useEffect(() => {
    if (textareaRef.current) {
      textareaRef.current.scrollTop = textareaRef.current.scrollHeight
    }
    console.log('Log messages updated:', logMessages)
  }, [logMessages])

  return (
    <div className="pageContainer">
      <Form.Group className="mb-3" controlId="exampleForm.ControlTextarea1">
        <Form.Control
          as="textarea"
          rows={14}
          readOnly
          className="logTextBox"
          value={getFormattedLogs()}
          ref={textareaRef}
        />
      </Form.Group>
      <Button className="launchButton buttonDropshadow">LAUNCH</Button>
    </div>
  )
}

export default HomePage
