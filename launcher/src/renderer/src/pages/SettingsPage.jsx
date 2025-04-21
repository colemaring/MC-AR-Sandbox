import Button from 'react-bootstrap/Button'
import Form from 'react-bootstrap/Form'
import Dropdown from 'react-bootstrap/Dropdown'
import externalIcon from '@renderer/assets/icons/external_icon.png'
import saveIcon from '@renderer/assets/icons/save_icon.png'
import { useNavigate } from 'react-router-dom'
import { useContext } from 'react' // Removed useState, useEffect if not using portal
import { SettingsConfigContext } from '../context/SettingsConfigContext'
import OverlayTrigger from 'react-bootstrap/OverlayTrigger'
import Tooltip from 'react-bootstrap/Tooltip'

function SettingsPage() {
  const navigate = useNavigate()
  const {
    // x1, // Assuming these are handled in KinectPage
    // setX1,
    // y1,
    // setY1,
    // x2,
    // setX2,
    // y2,
    // setY2,
    distance,
    setDistance,
    displayOnLaunchTopographic,
    setDisplayOnLaunchTopographic,
    displayTopographic,
    setDisplayTopographic,
    smoothing,
    setSmoothing,
    elevation, // Make sure elevation is destructured if the commented-out section is used
    setElevation, // Make sure setElevation is destructured if the commented-out section is used
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

  // --- Removed state/effect for portal container, assuming simpler wrapping for now ---
  // const [overlayContainer, setOverlayContainer] = useState(null);
  // useEffect(() => {
  //   const container = document.getElementById('overlay-container');
  //   setOverlayContainer(container || document.body);
  // }, []);
  // ---

  const openKinectView = () => {
    navigate('/kinect')
  }

  const handleSaveSettings = async () => {
    await writeToConfig()
  }

  // Helper function to render tooltips
  const renderTooltip = (text) => (
    // Removed position: fixed as it might contribute to issues when not using portal
    <Tooltip
      style={{ position: 'fixed' }}
      id={`tooltip-${text.toLowerCase().replace(/\s+/g, '-')}`}
    >
      {text}
    </Tooltip>
  )

  // Define the min and max for the slider
  const captureSpeedMin = 1
  const captureSpeedMax = 30

  // Calculate the reversed value for the slider display
  const reversedCaptureSpeedValue = captureSpeedMax + captureSpeedMin - captureSpeed

  // Handle the change from the reversed slider
  const handleReversedCaptureSpeedChange = (e) => {
    const reversedValue = Number(e.target.value)
    const actualValue = captureSpeedMax + captureSpeedMin - reversedValue
    setCaptureSpeed(actualValue)
  }

  return (
    <div className="pageContainer">
      {/* --- Kinect Settings --- */}
      <h3>Kinect Settings</h3>
      <div>
        <OverlayTrigger
          placement="right"
          delay={{ show: 250, hide: 400 }}
          overlay={renderTooltip(
            'Drag the crop edges to correspond with the boundaries of your sandbox.'
          )}
          // container={overlayContainer} // Add back if using portal
        >
          <Button className="settingsButton" onClick={openKinectView}>
            <img style={{ width: '1.3rem', height: '1.3rem' }} src={externalIcon} alt="Settings" />{' '}
            Crop Kinect View
          </Button>
        </OverlayTrigger>
      </div>

      <div className="mt-3">
        <OverlayTrigger
          placement="right"
          delay={{ show: 250, hide: 400 }}
          overlay={renderTooltip('The distance from your Kinect sensor to the sand, in cm.')}
          // container={overlayContainer} // Add back if using portal
        >
          <span style={{ display: 'inline-block' }}>
            {' '}
            {/* Wrapper for trigger area */}
            <span>Kinect to Surface Offset</span>
            <Form.Control
              type="number"
              size="sm"
              placeholder="in cm"
              value={distance}
              onChange={(e) => setDistance(Number(e.target.value))}
              style={{ width: '100px', display: 'inline-block', marginLeft: '10px' }}
            />
          </span>
        </OverlayTrigger>
      </div>
      <div className="mt-1 centerRange">
        <OverlayTrigger
          placement="bottom"
          delay={{ show: 250, hide: 400 }}
          overlay={renderTooltip(
            'Change the speed at which the Kinect sends updates to Minecraft.'
          )}
          // container={overlayContainer} // Add back if using portal
        >
          <span style={{ display: 'flex' }}>
            {' '}
            {/* Wrapper for trigger area */}
            <span>Capture Speed</span>
            <span style={{ marginLeft: '1rem', fontStyle: 'italic' }}>Slow</span>
            <Form.Range
              min={captureSpeedMin}
              max={captureSpeedMax}
              step={1}
              value={reversedCaptureSpeedValue}
              onChange={handleReversedCaptureSpeedChange}
              style={{ width: '10rem', display: 'inline-block', margin: '0 0.5rem' }}
            />
            <span style={{ fontStyle: 'italic' }}>Fast</span>
          </span>
        </OverlayTrigger>
      </div>

      <hr />

      {/* --- Topographic Projection Settings --- */}
      <h3 className="mt-2">Projection Settings 🛠️</h3>
      <div className="mt-1">
        <OverlayTrigger
          placement="right"
          delay={{ show: 250, hide: 400 }}
          overlay={renderTooltip('Toggle to launch the projection in fullscreen.')}
          // container={overlayContainer} // Add back if using portal
        >
          <span style={{ display: 'inline-block', marginRight: '15px' }}>
            {' '}
            {/* Wrapper for trigger area */}
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
          </span>
        </OverlayTrigger>
        <OverlayTrigger
          placement="left"
          delay={{ show: 250, hide: 400 }}
          overlay={renderTooltip(
            'Toggle to automatically launch the projection when the MC-AR Launcher opens.'
          )}
          // container={overlayContainer} // Add back if using portal
        >
          <span style={{ display: 'inline-block' }}>
            {' '}
            {/* Wrapper for trigger area */}
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
          </span>
        </OverlayTrigger>
        <br /> {/* Keep line break */}
        <div className="mt-2">
          <OverlayTrigger
            placement="right"
            delay={{ show: 250, hide: 400 }}
            overlay={renderTooltip('Choose which display device to launch the projection on.')}
            // container={overlayContainer} // Add back if using portal
          >
            <span style={{ display: 'inline-block' }}>
              {' '}
              {/* Wrapper for trigger area */}
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
            </span>
          </OverlayTrigger>
        </div>
      </div>
      <div className="mt-1 centerRange">
        <OverlayTrigger
          placement="right"
          delay={{ show: 250, hide: 400 }}
          overlay={renderTooltip(
            'Determine how much detail you want on your topographic layer lines.'
          )}
          // container={overlayContainer} // Add back if using portal
        >
          <div style={{ display: 'flex' }}>
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
        </OverlayTrigger>
      </div>
      <div className="mt-1">
        <OverlayTrigger
          placement="right"
          delay={{ show: 250, hide: 400 }}
          overlay={renderTooltip('Choose which color profile to project onto the sand.')}
          // container={overlayContainer} // Add back if using portal
        >
          <span style={{ display: 'inline-block' }}>
            {' '}
            {/* Wrapper for trigger area */}
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
          </span>
        </OverlayTrigger>
      </div>
      <hr />

      {/* --- Minecraft Settings --- */}
      <h3 className="mt-2">Minecraft Settings</h3>
      <div className="mt-1">
        <OverlayTrigger
          placement="right"
          delay={{ show: 250, hide: 400 }}
          overlay={renderTooltip(
            'Toggle to automatically launch Minecraft when the MC-AR Launcher opens.'
          )}
          // container={overlayContainer} // Add back if using portal
        >
          <span style={{ display: 'inline-block' }}>
            {' '}
            {/* Wrapper for trigger area */}
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
          </span>
        </OverlayTrigger>
        <br /> {/* Keep line break */}
        <div className="mt-2">
          <OverlayTrigger
            placement="right"
            delay={{ show: 250, hide: 400 }}
            overlay={renderTooltip('Choose which display device to launch Minecraft on.')}
            // container={overlayContainer} // Add back if using portal
          >
            <span style={{ display: 'inline-block' }}>
              {' '}
              {/* Wrapper for trigger area */}
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
            </span>
          </OverlayTrigger>
        </div>
        {/* Uncomment and wrap this section if you re-add the Elevation Multiplier */}
        {/* <div className="mt-1 centerRange">
          <OverlayTrigger
            placement="right"
            delay={{ show: 250, hide: 400 }}
            overlay={renderTooltip("Choose how much y axis range you'd like in Minecraft.")}
            // container={overlayContainer} // Add back if using portal
          >
            <span style={{ display: 'inline-block' }}> {/* Wrapper for trigger area */}
        {/* <span>Elevation Multipler</span>
              <Form.Range
                min={0}
                max={100} // Adjust range as needed
                step={1}
                value={elevation}
                onChange={(e) => setElevation(Number(e.target.value))}
                style={{ width: '10rem', display: 'inline-block', marginLeft: '1rem' }}
              />
              <span style={{ marginLeft: '1rem', minWidth: '3em', display: 'inline-block', textAlign: 'right' }}>({elevation})</span>
            </span>
          </OverlayTrigger>
        </div> */}
      </div>
      <div className="mt-1">
        <OverlayTrigger
          placement="top"
          delay={{ show: 250, hide: 400 }}
          overlay={renderTooltip('Specify the filepath to the Prismlauncher exe.')}
          // container={overlayContainer} // Add back if using portal
        >
          <span style={{ display: 'inline-block' }}>
            {' '}
            {/* Wrapper for trigger area */}
            <span>PrismLauncher Filepath</span>
            <Form.Control
              type="text"
              size="sm"
              placeholder="C:\Path\To\PrismLauncher\prismlauncher.exe"
              value={prismlauncherPath}
              onChange={(e) => setPrismlauncherPath(e.target.value)}
              style={{ width: '400px', display: 'inline-block', marginLeft: '10px' }}
            />
          </span>
        </OverlayTrigger>
      </div>
      <hr />
      <div>
        <OverlayTrigger
          placement="top" // Place above the button
          delay={{ show: 250, hide: 400 }}
          overlay={renderTooltip(
            'Save your configuration to persist between launches and reflect changes in the currently running Minecraft instance.'
          )}
          // container={overlayContainer} // Add back if using portal
        >
          <Button className="mt-1 saveButton" onClick={handleSaveSettings}>
            <img style={{ width: '1.2rem', height: '1.2rem' }} src={saveIcon} alt="Save" /> Save
          </Button>
        </OverlayTrigger>
      </div>
    </div>
  )
}

export default SettingsPage
