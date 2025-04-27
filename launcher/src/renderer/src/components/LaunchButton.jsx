import React, { useState, useEffect } from "react";
import Button from "react-bootstrap/Button";

function LaunchButton() {
  // Check if this is the first load of the current app session
  const isFirstLoadThisSession = !sessionStorage.getItem("appInitialized");
  const [launchButtonDisabled, setLaunchButtonDisabled] = useState(
    isFirstLoadThisSession
  );
  const [loadingText, setLoadingText] = useState(
    isFirstLoadThisSession ? "Initializing..." : ""
  );

  // Effect to enable button after initial loading period - for each app launch
  useEffect(() => {
    console.log("isFirstLoadThisSession", isFirstLoadThisSession);
    if (isFirstLoadThisSession) {
      const timer = setTimeout(() => {
        setLaunchButtonDisabled(false);
        setLoadingText(""); // Clear the loading text
        sessionStorage.setItem("appInitialized", "true"); // Mark app as initialized for this session
      }, 4000);

      return () => clearTimeout(timer); // Cleanup on unmount
    }
  }, [isFirstLoadThisSession]);

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
      {launchButtonDisabled ? loadingText || "Launching..." : "Launch"}
    </Button>
  );
}

export default LaunchButton;
