import React, { useState, useEffect } from "react";
import Button from "react-bootstrap/Button";

function LaunchButton() {
  // Check if this is the first load of the app
  const isFirstLoad = !localStorage.getItem("appInitialized");
  const [launchButtonDisabled, setLaunchButtonDisabled] = useState(isFirstLoad); // Only disabled on first load
  const [loadingText, setLoadingText] = useState(
    isFirstLoad ? "Initializing..." : ""
  ); // Only show text on first load

  // Effect to enable button after initial loading period - only on first app load
  useEffect(() => {
    if (isFirstLoad) {
      const timer = setTimeout(() => {
        setLaunchButtonDisabled(false);
        setLoadingText(""); // Clear the loading text
        localStorage.setItem("appInitialized", "true"); // Mark app as initialized
      }, 2000); // 2 seconds

      return () => clearTimeout(timer); // Cleanup on unmount
    }
  }, [isFirstLoad]); // Dependency on isFirstLoad

  const handleLaunchClick = () => {
    setLaunchButtonDisabled(true); // Disable the button when clicked
    setLoadingText("Launching..."); // Set launching text
    window.electronAPI.ipcRenderer.send("start-launch", "1.21.5");
  };

  useEffect(() => {
    const handleMinecraftReady = () => {
      setLaunchButtonDisabled(false); // Enable the button when Minecraft is ready
      setLoadingText(""); // Clear loading text when ready
    };

    window.electronAPI.onMinecraftReady(handleMinecraftReady);

    return () => {
      window.electronAPI.onMinecraftReady(handleMinecraftReady);
    };
  }, []);

  return (
    <Button
      className="launchButton buttonDropshadow"
      onClick={handleLaunchClick}
      disabled={launchButtonDisabled}
    >
      {launchButtonDisabled
        ? loadingText || "Launching..."
        : "Launch Minecraft & Projection"}
    </Button>
  );
}

export default LaunchButton;
