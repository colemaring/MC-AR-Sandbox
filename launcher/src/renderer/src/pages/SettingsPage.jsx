import Button from 'react-bootstrap/Button'
import Form from 'react-bootstrap/Form'
import Dropdown from 'react-bootstrap/Dropdown'
import externalIcon from '@renderer/assets/icons/external_icon.png'
import saveIcon from '@renderer/assets/icons/save_icon.png'
import { useNavigate } from 'react-router-dom'
import { useContext } from 'react'
import { SettingsConfigContext } from '../context/SettingsConfigContext'

function SettingsPage() {
  const navigate = useNavigate()
  const {
    x1,
    setX1,
    y1,
    setY1,
    x2,
    setX2,
    y2,
    setY2,
    distance,
    setDistance,
    displayOnLaunchTopographic,
    setDisplayOnLaunchTopographic,
    displayTopographic,
    setDisplayTopographic,
    smoothing,
    setSmoothing,
    elevation,
    setElevation,
    colorMode,
    setColorMode,
    displayMinecraft,
    setDisplayMinecraft,
    prismlauncherPath,
    setPrismlauncherPath,
    autoLaunchMinecraft,
    setAutoLaunchMinecraft,
    autoLaunchProjector,
    setAutoLaunchProjector,
    captureSpeed,
    setCaptureSpeed,
    writeToConfig
  } = useContext(SettingsConfigContext)

  const openKinectView = () => {
    // Navigate to /kinect and pass state variables as state
    navigate('/kinect')
  }

  const handleSaveSettings = async () => {
    await writeToConfig()
  }

  return (
    <div className="pageContainer">
      <h3>Kinect Settings</h3>
      <div>
        <Button className="settingsButton" onClick={openKinectView}>
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
          value={distance}
          onChange={(e) => setDistance(Number(e.target.value))}
          style={{ width: '100px', display: 'inline-block', marginLeft: '10px' }}
        />
      </div>
      <div className="mt-1 centerRange">
        <span>Capture Speed</span>
        <Form.Range
          min={1}
          max={30}
          step={1}
          value={captureSpeed}
          onChange={(e) => setCaptureSpeed(Number(e.target.value))}
          style={{ width: '10rem', display: 'inline-block', marginLeft: '1rem' }}
        />
      </div>

      <hr />

      <h3 className="mt-2">Topographic Projection Settings</h3>
      <div className="mt-1">
        <span>Fullscreen on Launch</span>
        <Form.Check
          type="switch"
          id="topographic-launch-switch"
          label=""
          className="custom-switch"
          checked={displayOnLaunchTopographic}
          onChange={(e) => setDisplayOnLaunchTopographic(e.target.checked)}
          style={{ display: 'inline-block', marginLeft: '10px' }}
        />
        <span>Auto Launch</span>
        <Form.Check
          type="switch"
          id="projection-auto-launch-switch"
          label=""
          className="custom-switch"
          checked={autoLaunchProjector}
          onChange={(e) => setAutoLaunchProjector(e.target.checked)}
          style={{ display: 'inline-block', marginLeft: '10px' }}
        />
        <br></br>
        <div className="mt-2">
          <span>Show on: </span>

          <Dropdown
            style={{ display: 'inline-block', marginLeft: '10px' }}
            onSelect={(key) => setDisplayTopographic(key)}
          >
            <Dropdown.Toggle className="settingsOption" size="sm">
              {displayTopographic}
            </Dropdown.Toggle>

            <Dropdown.Menu>
              <Dropdown.Item eventKey="Display 1">Display 1</Dropdown.Item>
              <Dropdown.Item eventKey="Display 2">Display 2</Dropdown.Item>
            </Dropdown.Menu>
          </Dropdown>
        </div>
      </div>
      <div className="mt-1 centerRange">
        <span>Smoothing</span>
        <Form.Range
          min={0}
          max={100}
          step={1}
          value={smoothing}
          onChange={(e) => setSmoothing(Number(e.target.value))}
          style={{ width: '10rem', display: 'inline-block', marginLeft: '1rem' }}
        />
      </div>
      <div className="mt-1">
        <span>Color Mode</span>
        <Dropdown
          style={{ display: 'inline-block', marginLeft: '10px' }}
          onSelect={(key) => setColorMode(key)}
        >
          <Dropdown.Toggle className="settingsOption" size="sm">
            {colorMode}
          </Dropdown.Toggle>

          <Dropdown.Menu>
            <Dropdown.Item eventKey="Rainbow">Rainbow</Dropdown.Item>
            <Dropdown.Item eventKey="Earthchromic">Earthchromic</Dropdown.Item>
            <Dropdown.Item eventKey="Default">Default</Dropdown.Item>
          </Dropdown.Menu>
        </Dropdown>
      </div>
      <hr />

      <h3 className="mt-2">Minecraft Settings</h3>
      <div className="mt-1">
        <span>Auto Launch</span>
        <Form.Check
          type="switch"
          id="minecraft-auto-launch-switch"
          label=""
          className="custom-switch"
          checked={autoLaunchMinecraft}
          onChange={(e) => setAutoLaunchMinecraft(e.target.checked)}
          style={{ display: 'inline-block', marginLeft: '10px' }}
        />
        <br></br>
        <div className="mt-2">
          <span>Show on: </span>
          <Dropdown
            style={{ display: 'inline-block', marginLeft: '10px' }}
            onSelect={(key) => setDisplayMinecraft(key)}
          >
            <Dropdown.Toggle className="settingsOption" size="sm">
              {displayMinecraft}
            </Dropdown.Toggle>

            <Dropdown.Menu>
              <Dropdown.Item eventKey="Display 1">Display 1</Dropdown.Item>
              <Dropdown.Item eventKey="Display 2">Display 2</Dropdown.Item>
            </Dropdown.Menu>
          </Dropdown>
        </div>

        {/* <div className="mt-1 centerRange">
          <span>Elevation Multipler</span>
          <Form.Range
            min={0}
            max={100}
            step={1}
            value={elevation}
            onChange={(e) => setElevation(Number(e.target.value))}
            style={{ width: '10rem', display: 'inline-block', marginLeft: '1rem' }}
          />
        </div> */}
      </div>
      <div className="mt-1">
        <span>PrismLauncher Filepath</span>
        <Form.Control
          type="text"
          size="sm"
          placeholder="C:\Users\colem\AppData\Local\Programs\PrismLauncher\prismlauncher.exe"
          value={prismlauncherPath}
          onChange={(e) => setPrismlauncherPath(e.target.value)}
          style={{ width: '400px', display: 'inline-block', marginLeft: '10px' }}
        />
      </div>
      <hr />
      <div>
        <Button className="mt-1 saveButton" onClick={handleSaveSettings}>
          <img style={{ width: '1.2rem', height: '1.2rem' }} src={saveIcon} alt="Save" /> Save
        </Button>
      </div>
    </div>
  )
}

export default SettingsPage
