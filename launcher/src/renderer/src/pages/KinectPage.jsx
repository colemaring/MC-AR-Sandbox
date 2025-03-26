import Button from 'react-bootstrap/Button'
import Form from 'react-bootstrap/Form'

function KinectPage() {
  return (
    <div className="pageContainer">
      <Form.Group className="mb-3" controlId="exampleForm.ControlTextarea1">
        <Form.Control
          as="textarea"
          rows={14}
          readOnly // Make the textarea read-only
          className="logTextBox" // Apply the CSS class
        />
      </Form.Group>
      <div>
        <Button>back</Button>
        <Button>save</Button>
      </div>
    </div>
  )
}

export default KinectPage
