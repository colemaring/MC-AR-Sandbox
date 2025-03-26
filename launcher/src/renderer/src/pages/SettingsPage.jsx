import Button from 'react-bootstrap/Button'
import Form from 'react-bootstrap/Form'
import Dropdown from 'react-bootstrap/Dropdown'
import settingsIcon from '@renderer/assets/icons/settings_icon.png'
import externalIcon from '@renderer/assets/icons/external_icon.png'
import saveIcon from '@renderer/assets/icons/save_icon.png'
import { useNavigate } from 'react-router-dom'

function SettingsPage() {
  const navigate = useNavigate()
  return (
    <div className="pageContainer">
      <h3>Kinect Settings</h3>
      <div>
        <Button className="settingsButton" onClick={() => navigate('/kinect')}>
          <img style={{ width: '1.3rem', height: '1.3rem' }} src={externalIcon} alt="Settings" />{' '}
          Crop Kinect View
        </Button>
      </div>

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
          className="custom-switch"
          style={{ display: 'inline-block', marginLeft: '10px' }}
        />
      </div>
      <hr />

      <h3 className="mt-2">Topographic Projection Settings</h3>
      <div className="mt-1">
        <span>Display on Launch</span>
        <Form.Check
          type="switch"
          id="custom-switch"
          label=""
          className="custom-switch"
          style={{ display: 'inline-block', marginLeft: '10px' }}
        />
        <Dropdown style={{ display: 'inline-block', marginLeft: '10px' }}>
          <Dropdown.Toggle className="settingsOption" size="sm">
            Assign Monitor
          </Dropdown.Toggle>

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
          <Dropdown.Toggle className="settingsOption" size="sm">
            Options
          </Dropdown.Toggle>

          <Dropdown.Menu>
            <Dropdown.Item>setting 1</Dropdown.Item>
            <Dropdown.Item>setting 2</Dropdown.Item>
            <Dropdown.Item>setting 3</Dropdown.Item>
          </Dropdown.Menu>
        </Dropdown>
      </div>
      <hr />

      <h3 className="mt-2">Minecraft Renderer Settings</h3>
      <div className="mt-1">
        <span>Display on Launch</span>
        <Form.Check
          type="switch"
          id="custom-switch"
          label=""
          className="custom-switch"
          style={{ display: 'inline-block', marginLeft: '10px' }}
        />
        <Dropdown style={{ display: 'inline-block', marginLeft: '10px' }}>
          <Dropdown.Toggle className="settingsOption" size="sm">
            Assign Monitor
          </Dropdown.Toggle>

          <Dropdown.Menu>monitor 1..</Dropdown.Menu>
        </Dropdown>
      </div>
      <hr />
      <div>
        <Button className="mt-1 saveButton">
          <img style={{ width: '1.2rem', height: '1.2rem' }} src={saveIcon} alt="Save" /> Save
        </Button>
      </div>
    </div>
  )
}

export default SettingsPage
