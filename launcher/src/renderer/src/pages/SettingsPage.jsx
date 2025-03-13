import Button from 'react-bootstrap/Button'
import Form from 'react-bootstrap/Form'
import Dropdown from 'react-bootstrap/Dropdown'
import settingsIcon from '@renderer/assets/icons/settings_icon.png'
import saveIcon from '@renderer/assets/icons/save_icon.png'

function SettingsPage() {
  return (
    <div className="pageContainer">
      <h3>Kinect Settings</h3>
      <Button>
        <img style={{ width: '1.2rem', height: '1.2rem' }} src={settingsIcon} alt="Settings" /> Crop
        Kinect View
      </Button>

      <div className="mt-3">
        <span>Kinect to Surface Distance</span>
        <Form.Control
          type="number"
          size="sm"
          placeholder="in cm"
          style={{ width: '100px', display: 'inline-block', marginLeft: '10px' }}
        />
      </div>
      <div className="mt-1">
        <span>Test Mode</span>
        <Form.Check
          type="switch"
          id="custom-switch"
          label=""
          style={{ display: 'inline-block', marginLeft: '10px' }}
        />
      </div>

      <h3 className="mt-3">Topographic Projection Settings</h3>
      <div className="mt-1">
        <span>Display on Launch</span>
        <Form.Check
          type="switch"
          id="custom-switch"
          label=""
          style={{ display: 'inline-block', marginLeft: '10px' }}
        />
        <Dropdown style={{ display: 'inline-block', marginLeft: '10px' }}>
          <Dropdown.Toggle size="sm">Options</Dropdown.Toggle>

          <Dropdown.Menu>monitor 1..</Dropdown.Menu>
        </Dropdown>
      </div>
      <div className="mt-1">
        <span>Smoothing</span>
        <Form.Range
          min={0}
          max={100}
          step={1}
          defaultValue={40}
          style={{ width: '10rem', display: 'inline-block', marginLeft: '1rem' }}
        />
      </div>
      <div className="mt-1">
        <span>Color Mode</span>
        <Dropdown style={{ display: 'inline-block', marginLeft: '10px' }}>
          <Dropdown.Toggle size="sm">Options</Dropdown.Toggle>

          <Dropdown.Menu>monitor 1..</Dropdown.Menu>
        </Dropdown>
      </div>

      <h3 className="mt-3">Minecraft Renderer Settings</h3>
      <div className="mt-1">
        <span>Display on Launch</span>
        <Form.Check
          type="switch"
          id="custom-switch"
          label=""
          style={{ display: 'inline-block', marginLeft: '10px' }}
        />
        <Dropdown style={{ display: 'inline-block', marginLeft: '10px' }}>
          <Dropdown.Toggle size="sm">Options</Dropdown.Toggle>

          <Dropdown.Menu>monitor 1..</Dropdown.Menu>
        </Dropdown>
      </div>
      <Button className="mt-3" style={{ bottom: '3rem' }}>
        <img style={{ width: '1.2rem', height: '1.2rem' }} src={saveIcon} alt="Save" /> Save
      </Button>
    </div>
  )
}

export default SettingsPage
