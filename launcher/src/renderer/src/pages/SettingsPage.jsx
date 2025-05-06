import Button from "react-bootstrap/Button";
import Form from "react-bootstrap/Form";
import Dropdown from "react-bootstrap/Dropdown";
import externalIcon from "@renderer/assets/icons/external_icon.png";
import saveIcon from "@renderer/assets/icons/save_icon.png";
import { useNavigate } from "react-router-dom";
import { useContext } from "react"; // Removed useState, useEffect if not using portal
import { SettingsConfigContext } from "../context/SettingsConfigContext";
import OverlayTrigger from "react-bootstrap/OverlayTrigger";
import Tooltip from "react-bootstrap/Tooltip";
import { useState } from "react"; // Added useState for saving state

function SettingsPage() {
  const navigate = useNavigate();
  const {
    // x1, // Assuming these are handled in KinectPage
    // setX1,
    // y1,
    // setY1,
    // x2,
    // setX2,
    // y2,
    // setY2,
    yCoordOffset,
    setYCoordOffset,
    displayOnLaunchTopographic,
    setDisplayOnLaunchTopographic,
    displayTopographic,
    kinectDistanceMM,
    setKinectDistanceMM,
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
    interpolation,
    setInterpolation,
    writeToConfig,
  } = useContext(SettingsConfigContext);

  const [isSaving, setIsSaving] = useState(false);

  // --- Removed state/effect for portal container, assuming simpler wrapping for now ---
  // const [overlayContainer, setOverlayContainer] = useState(null);
  // useEffect(() => {
  //   const container = document.getElementById('overlay-container');
  //   setOverlayContainer(container || document.body);
  // }, []);
  // ---

  const openKinectView = () => {
    navigate("/kinect");
  };

  const handleSaveSettings = async () => {
    setIsSaving(true); // Disable button and show "Saving..."
    try {
      await writeToConfig();
      // Optionally add a success message here
    } catch (error) {
      console.error("Error saving settings:", error);
      // Optionally show an error message to the user
    } finally {
      // Re-enable the button after 1 second
      setTimeout(() => {
        setIsSaving(false);
      }, 1000); // 1000 milliseconds = 1 second
    }
  };

  // Helper function to render tooltips
  const renderTooltip = (text) => (
    // Removed position: fixed as it might contribute to issues when not using portal
    <Tooltip
      style={{ position: "fixed" }}
      id={`tooltip-${text.toLowerCase().replace(/\s+/g, "-")}`}
    >
      {text}
    </Tooltip>
  );

  // Define the min and max for the slider
  const captureSpeedMin = 1;
  const captureSpeedMax = 30;

  // Calculate the reversed value for the slider display
  const reversedCaptureSpeedValue =
    captureSpeedMax + captureSpeedMin - captureSpeed;

  // Handle the change from the reversed slider
  const handleReversedCaptureSpeedChange = (e) => {
    const reversedValue = Number(e.target.value);
    const actualValue = captureSpeedMax + captureSpeedMin - reversedValue;
    setCaptureSpeed(actualValue);
  };

  return (
    <div className="pageContainer">
      {/* --- Kinect Settings --- */}
      <h3>Kinect Settings</h3>
      <div>
        <OverlayTrigger
          placement="right"
          delay={{ show: 250, hide: 400 }}
          overlay={renderTooltip(
            "Drag the crop edges to correspond with the boundaries of your sandbox."
          )}
          // container={overlayContainer} // Add back if using portal
        >
          <Button className="settingsButton" onClick={openKinectView}>
            <img
              style={{ width: "1.3rem", height: "1.3rem" }}
              src={externalIcon}
              alt="Settings"
            />{" "}
            Crop Kinect View
          </Button>
        </OverlayTrigger>
      </div>
      <div className="mt-1 centerRange">
        <OverlayTrigger
          placement="bottom"
          delay={{ show: 250, hide: 400 }}
          overlay={renderTooltip(
            "Speed at which the Kinect sends updates to Minecraft."
          )}
          // container={overlayContainer} // Add back if using portal
        >
          <span style={{ display: "flex" }}>
            {" "}
            {/* Wrapper for trigger area */}
            <span>Capture Speed</span>
            <span style={{ marginLeft: "1rem", fontStyle: "italic" }}>
              Slow
            </span>
            <Form.Range
              min={captureSpeedMin}
              max={captureSpeedMax}
              step={1}
              value={reversedCaptureSpeedValue}
              onChange={handleReversedCaptureSpeedChange}
              style={{
                width: "10rem",
                display: "inline-block",
                margin: "0 0.5rem",
              }}
            />
            <span style={{ fontStyle: "italic" }}>Fast</span>
          </span>
        </OverlayTrigger>
      </div>
      <div className="mt-1">
        <OverlayTrigger
          placement="right"
          delay={{ show: 250, hide: 400 }}
          overlay={renderTooltip(
            "This value only is only used in the projection. Measure in milimeters."
          )}
          // container={overlayContainer} // Add back if using portal
        >
          <span style={{ display: "inline-block" }}>
            {" "}
            {/* Wrapper for trigger area */}
            <span>Kinect to Sandbox Distance</span>
            <Form.Control
              type="text"
              size="sm"
              placeholder="in mm"
              value={kinectDistanceMM}
              onChange={(e) => setKinectDistanceMM(Number(e.target.value))}
              style={{
                width: "100px",
                display: "inline-block",
                marginLeft: "10px",
              }}
            />
          </span>
        </OverlayTrigger>
      </div>
      <hr />
      {/* --- Topographic Projection Settings --- */}
      <h3 className="mt-2">Projection Settings</h3>
      <div className="mt-1">
        <OverlayTrigger
          placement="left"
          delay={{ show: 250, hide: 400 }}
          overlay={renderTooltip(
            "Open the projection when the Launch button on home page is clicked."
          )}
          // container={overlayContainer} // Add back if using portal
        >
          <span style={{ display: "inline-block" }}>
            {" "}
            {/* Wrapper for trigger area */}
            <span>Open on Launch</span>
            <Form.Check
              type="switch"
              id="projection-auto-launch-switch"
              label=""
              className="custom-switch"
              checked={autoLaunchProjector}
              onChange={(e) => setAutoLaunchProjector(e.target.checked)}
              style={{ display: "inline-block", marginLeft: "10px" }}
            />
          </span>
        </OverlayTrigger>
        <br /> {/* Keep line break */}
      </div>
      <div className="mt-1 centerRange">
        <OverlayTrigger
          placement="right"
          delay={{ show: 250, hide: 400 }}
          overlay={renderTooltip(
            "Adjust to balance detail and smoothness in the projection."
          )}
          // container={overlayContainer} // Add back if using portal
        >
          <div style={{ display: "flex" }}>
            <span>Smoothing</span>
            <span style={{ marginLeft: "1rem", fontStyle: "italic" }}>
              Less
            </span>
            <Form.Range
              min={0}
              max={25}
              step={1}
              value={smoothing}
              onChange={(e) => setSmoothing(Number(e.target.value))}
              style={{
                width: "10rem",
                display: "inline-block",
                margin: "0 0.5rem",
              }}
            />
            <span style={{ fontStyle: "italic" }}>More</span>
          </div>
        </OverlayTrigger>
      </div>
      <div className="mt-1">
        <OverlayTrigger
          placement="right"
          delay={{ show: 250, hide: 400 }}
          overlay={renderTooltip("Color profile to project onto the sand.")}
          // container={overlayContainer} // Add back if using portal
        >
          <span style={{ display: "inline-block" }}>
            {" "}
            {/* Wrapper for trigger area */}
            <span>Colors</span>
            <Dropdown
              style={{ display: "inline-block", marginLeft: "10px" }}
              onSelect={(key) => setColorMode(key)}
            >
              <Dropdown.Toggle className="settingsOption" size="sm">
                {colorMode}
              </Dropdown.Toggle>
              <Dropdown.Menu>
                <Dropdown.Item eventKey="Rainbow">Rainbow</Dropdown.Item>
                <Dropdown.Item eventKey="Natural">Natural</Dropdown.Item>
              </Dropdown.Menu>
            </Dropdown>
          </span>
        </OverlayTrigger>
        <OverlayTrigger
          placement="left"
          delay={{ show: 250, hide: 400 }}
          overlay={renderTooltip(
            "Interpolation mode for rendering the topographic projection."
          )}
          // container={overlayContainer} // Add back if using portal
        >
          <span style={{ display: "inline-block" }}>
            {" "}
            {/* Wrapper for trigger area */}
            <span>Interpolation</span>
            <Dropdown
              style={{ display: "inline-block", marginLeft: "10px" }}
              onSelect={(key) => setInterpolation(key)}
            >
              <Dropdown.Toggle className="settingsOption" size="sm">
                {interpolation}
              </Dropdown.Toggle>
              <Dropdown.Menu>
                <Dropdown.Item eventKey="None">None</Dropdown.Item>
                <Dropdown.Item eventKey="Median Filter">
                  Median Filter
                </Dropdown.Item>
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
            "Open Minecraft when the Launch button on home page is clicked."
          )}
          // container={overlayContainer} // Add back if using portal
        >
          <span style={{ display: "inline-block" }}>
            {" "}
            {/* Wrapper for trigger area */}
            <span>Open on Launch</span>
            <Form.Check
              type="switch"
              id="minecraft-auto-launch-switch"
              label=""
              className="custom-switch"
              checked={autoLaunchMinecraft}
              onChange={(e) => setAutoLaunchMinecraft(e.target.checked)}
              style={{ display: "inline-block", marginLeft: "10px" }}
            />
          </span>
        </OverlayTrigger>
        <br /> {/* Keep line break */}
        {/* Uncomment and wrap this section if you re-add the Elevation Multiplier */}
        <div className="mt-1 centerRange">
          <OverlayTrigger
            placement="right"
            delay={{ show: 250, hide: 400 }}
            overlay={renderTooltip(
              "Choose how much y axis range you'd like in Minecraft."
            )}
          >
            <span style={{ display: "flex" }}>
              <span>Elevation Multiplier</span>
              <Form.Range
                min={2}
                max={20}
                step={1}
                value={elevation}
                onChange={(e) => setElevation(Number(e.target.value))}
                style={{
                  width: "10rem",
                  display: "inline-block",
                  marginLeft: "1rem",
                }}
              />
              <span
                style={{
                  minWidth: "3em",
                  display: "inline-block",
                  textAlign: "right",
                }}
              >
                ({elevation})
              </span>
            </span>
          </OverlayTrigger>
        </div>
        <div className="mt-1">
          <OverlayTrigger
            placement="right"
            delay={{ show: 250, hide: 400 }}
            overlay={renderTooltip(
              "Needs to be tuned to fit the Kinect/Sandbox environment."
            )}
            // container={overlayContainer} // Add back if using portal
          >
            <span style={{ display: "inline-block" }}>
              {" "}
              {/* Wrapper for trigger area */}
              <span>Y Coordinate Offset</span>
              <Form.Control
                type="text"
                size="sm"
                value={yCoordOffset}
                onChange={(e) => setYCoordOffset(Number(e.target.value))}
                style={{
                  width: "100px",
                  display: "inline-block",
                  marginLeft: "10px",
                }}
              />
            </span>
          </OverlayTrigger>
        </div>
      </div>
      <div className="mt-1">
        <OverlayTrigger
          placement="top"
          delay={{ show: 250, hide: 400 }}
          overlay={renderTooltip("Filepath to the Prismlauncher exe.")}
          // container={overlayContainer} // Add back if using portal
        >
          <span style={{ display: "inline-block" }}>
            {" "}
            {/* Wrapper for trigger area */}
            <span>PrismLauncher Filepath</span>
            <Form.Control
              type="text"
              size="sm"
              placeholder="C:\Path\To\PrismLauncher\prismlauncher.exe"
              value={prismlauncherPath}
              onChange={(e) => setPrismlauncherPath(e.target.value)}
              style={{
                width: "400px",
                display: "inline-block",
                marginLeft: "10px",
              }}
            />
          </span>
        </OverlayTrigger>
      </div>
      <hr />
      Stand clear of the Kinect's view before saving.
      <div>
        <OverlayTrigger
          placement="top" // Place above the button
          delay={{ show: 250, hide: 400 }}
          overlay={renderTooltip(
            isSaving
              ? "Saving settings..."
              : "Changes persist between sessions and are made immediately upon saving."
          )}
        >
          {/* Span needed for OverlayTrigger on disabled button */}
          <span className="d-inline-block">
            <Button
              className="mt-3 saveButton"
              onClick={!isSaving ? handleSaveSettings : null} // Prevent click while saving
              disabled={isSaving} // Disable button when saving
              style={isSaving ? { pointerEvents: "none" } : {}} // Ensure tooltip works on disabled
            >
              {isSaving ? (
                "Saving..." // Show "Saving..." text
              ) : (
                <>
                  <img
                    style={{ width: "1.2rem", height: "1.2rem" }}
                    src={saveIcon}
                    alt="Save"
                  />{" "}
                  Save
                </> // Show icon and "Save" text
              )}
            </Button>
          </span>
        </OverlayTrigger>
      </div>
    </div>
  );
}

export default SettingsPage;
