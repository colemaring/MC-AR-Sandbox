import { useContext, useState, useRef, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import Button from "react-bootstrap/Button";
import backIcon from "@renderer/assets/icons/back.png";
import { SettingsConfigContext } from "../context/SettingsConfigContext";

function KinectPage() {
  const navigate = useNavigate();
  const { x1, x2, y1, y2, setX1, setX2, setY1, setY2, writeToConfig } =
    useContext(SettingsConfigContext);
  const [depthData, setDepthData] = useState(null);
  const canvasRef = useRef(null);

  // Track which handle is being dragged: 'tl', 'tr', 'bl', or 'br'
  const [draggingHandle, setDraggingHandle] = useState(null);
  const containerRef = useRef(null);

  // Listen for Kinect depth data
  useEffect(() => {
    const handleDepthData = (data) => {
      setDepthData(data);
    };

    // Register the listener
    window.electronAPI.onKinectDepthData(handleDepthData);
  }, []);

  // Draw the depth data to canvas when it changes
  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const ctx = canvas.getContext("2d");

    // Set standard dimensions for canvas regardless of data
    canvas.width = 512;
    canvas.height = 424;

    if (!depthData) {
      // No data yet - show placeholder
      ctx.fillStyle = "#333";
      ctx.fillRect(0, 0, canvas.width, canvas.height);

      // Display "Waiting on Kinect..." text
      ctx.fillStyle = "#fff";
      ctx.font = "24px Arial";
      ctx.textAlign = "center";
      ctx.textBaseline = "middle";
      ctx.fillText("Waiting on Kinect...", canvas.width / 2, canvas.height / 2);
      return;
    }

    // We have data, proceed with visualization
    const width = depthData[0].length;
    const height = depthData.length;

    // Create an ImageData object
    const imageData = ctx.createImageData(width, height);

    // Find min and max values for normalization
    let min = 0;
    let max = 65000;

    // Normalize and fill the image data
    for (let y = 0; y < height; y++) {
      for (let x = 0; x < width; x++) {
        const depth = depthData[y][x];
        const index = (y * width + x) * 4;

        if (depth > 0) {
          // Valid depth data
          // Normalize to 0-255
          const normalizedDepth = Math.floor(
            255 * (1 - (depth - min) / (max - min))
          );

          // Grayscale visualization
          imageData.data[index] = normalizedDepth; // R
          imageData.data[index + 1] = normalizedDepth; // G
          imageData.data[index + 2] = normalizedDepth; // B
          imageData.data[index + 3] = 255; // A
        } else {
          // No data - make transparent or specific color
          imageData.data[index] = 0;
          imageData.data[index + 1] = 0;
          imageData.data[index + 2] = 0;
          imageData.data[index + 3] = 0; // Transparent
        }
      }
    }

    // Put the image data on the canvas
    ctx.putImageData(imageData, 0, 0);
  }, [depthData]);

  useEffect(() => {
    const handleMouseMove = (e) => {
      if (!draggingHandle || !containerRef.current) return;
      const { left, top } = containerRef.current.getBoundingClientRect();

      // Get the bounds of the container
      const containerWidth = containerRef.current.offsetWidth;
      const containerHeight = containerRef.current.offsetHeight;

      // Calculate mouse position relative to container
      let mouseX = e.clientX - left;
      let mouseY = e.clientY - top;

      // Constrain mouse position to container bounds
      mouseX = Math.max(0, Math.min(mouseX, containerWidth - 1));
      mouseY = Math.max(0, Math.min(mouseY, containerHeight - 1));

      switch (draggingHandle) {
        case "tl": // top left
          setX1(Math.min(mouseX, x2));
          setY1(Math.min(mouseY, y2));
          break;
        case "tr": // top right
          setX2(Math.max(mouseX, x1));
          setY1(Math.min(mouseY, y2));
          break;
        case "bl": // bottom left
          setX1(Math.min(mouseX, x2));
          setY2(Math.max(mouseY, y1));
          break;
        case "br": // bottom right
          setX2(Math.max(mouseX, x1));
          setY2(Math.max(mouseY, y1));
          break;
        default:
          break;
      }
    };

    const handleMouseUp = () => {
      setDraggingHandle(null);
    };

    window.addEventListener("mousemove", handleMouseMove);
    window.addEventListener("mouseup", handleMouseUp);

    return () => {
      window.removeEventListener("mousemove", handleMouseMove);
      window.removeEventListener("mouseup", handleMouseUp);
    };
  }, [draggingHandle, setX1, setX2, setY1, setY2, x1, x2, y1, y2]);

  // Helper to style the draggable handle
  const handleStyle = (left, top) => ({
    position: "absolute",
    left: left - 8,
    top: top - 8,
    width: 16,
    height: 16,
    borderRadius: "50%",
    backgroundColor: "rgb(59, 125, 141)",
    cursor: "grab",
    zIndex: 2,
  });

  // Calculate overlay styles based on current crop points
  const overlayStyleDynamic = {
    left: Math.min(x1, x2),
    top: Math.min(y1, y2),
    width: Math.abs(x2 - x1),
    height: Math.abs(y2 - y1),
    border: "2px solid rgba(59, 125, 141, 0.8)",
    position: "absolute",
    pointerEvents: "none",
    backgroundColor: "rgba(59, 125, 141, 0.2)",
  };

  return (
    <div className="kinectContainer">
      <p>
        Crop the kinect view to correspond with the boundaries of your
        environment.
      </p>
      <div
        className="kinectCropContainer"
        ref={containerRef}
        style={{
          position: "relative",
          width: "512px",
          height: "424px",
          margin: "0 auto",
          backgroundColor: "#333",
        }}
      >
        {/* Canvas for depth data instead of static image */}
        <canvas
          ref={canvasRef}
          className="kinectDepthCanvas"
          style={{
            width: "100%",
            height: "100%",
            display: "block",
          }}
        />

        {/* Overlay drawn as a rectangle between the crop points */}
        {x1 !== null && x2 !== null && y1 !== null && y2 !== null && (
          <div style={overlayStyleDynamic} />
        )}

        {/* Draggable handles */}
        <div
          style={handleStyle(x1, y1)}
          onMouseDown={(e) => {
            e.stopPropagation();
            setDraggingHandle("tl");
          }}
        />
        <div
          style={handleStyle(x2, y1)}
          onMouseDown={(e) => {
            e.stopPropagation();
            setDraggingHandle("tr");
          }}
        />
        <div
          style={handleStyle(x1, y2)}
          onMouseDown={(e) => {
            e.stopPropagation();
            setDraggingHandle("bl");
          }}
        />
        <div
          style={handleStyle(x2, y2)}
          onMouseDown={(e) => {
            e.stopPropagation();
            setDraggingHandle("br");
          }}
        />
      </div>
      <div className="coordinateDisplay">
        x1: {Math.round(x1)}, x2: {Math.round(x2)}, y1: {Math.round(y1)}, y2:{" "}
        {Math.round(y2)}
      </div>
      <div className="buttonContainer">
        <Button
          onClick={() => navigate("/settings")}
          className="mt-1 backButton"
        >
          <img
            style={{ width: "1.2rem", height: "1.2rem" }}
            src={backIcon}
            alt="Back"
          />{" "}
          Back
        </Button>
        {/* <Button className="mt-1 saveButton" onClick={handleSaveSettings}>
          <img style={{ width: '1.2rem', height: '1.2rem' }} src={saveIcon} alt="Save" /> Save
        </Button> */}
      </div>
    </div>
  );
}

export default KinectPage;
