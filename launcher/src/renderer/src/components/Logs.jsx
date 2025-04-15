import React, { useEffect, useRef } from 'react'
import Form from 'react-bootstrap/Form'
import { useLogMessages } from '../context/LogMessageContext'

function Logs() {
  const { logMessages, getFormattedLogs } = useLogMessages()
  const textareaRef = useRef(null)

  useEffect(() => {
    if (textareaRef.current) {
      textareaRef.current.scrollTop = textareaRef.current.scrollHeight
    }
    console.log('Log messages updated:', logMessages)
  }, [logMessages])

  return (
    <Form.Group className="mb-3" controlId="exampleForm.ControlTextarea1">
      <div
        className="logTextBox"
        ref={textareaRef}
        dangerouslySetInnerHTML={{ __html: getFormattedLogs() }}
      />
    </Form.Group>
  )
}

export default Logs
