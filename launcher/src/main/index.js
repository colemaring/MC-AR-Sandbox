import { app, shell, BrowserWindow, ipcMain } from "electron";
import { join } from "path";
import { electronApp, optimizer, is } from "@electron-toolkit/utils";
const custom_icon = join(__dirname, "../../resources/icon.png");
import config_handler from "./config_handler";
import { startKinectProcess } from "./kinect";
import { startMinecraftServer } from "./mc_server";
const { exec } = require("child_process");
import { terminateAllProcesses } from "./terminate_processes";
import { checkDependencies } from "./check_dependencies";
import { launchPrismLauncher, launchProjection } from "./launch";
import { serverProcess } from "./mc_server";
import { Rcon } from "rcon-client";
import { readConfig } from "./config_handler";

let kinectProcess;
let mainWindow;
let isQuitting = false;

export function sendLogMessage(text, type = "normal") {
  const windowToUse = BrowserWindow.getAllWindows().find(
    (w) => !w.isDestroyed()
  );
  if (windowToUse) {
    windowToUse.webContents.send("logMessage", { text, type });
  }
}

async function sendRconCommand(command) {
  try {
    // Create a new connection for each command (simple but reliable approach)
    const rcon = new Rcon({
      host: "localhost",
      port: "25575",
      password: "doesntmatter",
      timeout: 5000,
    });

    await rcon.connect();
    const response = await rcon.send(command);
    await rcon.end();

    return { success: true, response };
  } catch (error) {
    console.error("RCON error:", error);
    return { success: false, error: error.message };
  }
}

process.on("SIGINT", async () => {
  if (isQuitting) return; // Prevent multiple calls

  isQuitting = true;
  console.log("Received SIGINT (Ctrl+C), terminating all processes...");

  try {
    await terminateAllProcesses();
    console.log("Cleanup complete, exiting application");
  } catch (error) {
    console.error("Error during cleanup:", error);
  } finally {
    // Force exit after cleanup
    process.exit(0);
  }
});

function createWindow() {
  // Create the browser window.
  mainWindow = new BrowserWindow({
    icon: custom_icon,
    width: 800,
    height: 794,
    show: false,
    autoHideMenuBar: true,
    // ...(process.platform === 'linux' ? { icon } : {}),
    webPreferences: {
      preload: join(__dirname, "../preload/index.js"),
      sandbox: false,
    },
  });

  // Handle window close event
  mainWindow.on("close", async (e) => {
    if (!isQuitting) {
      e.preventDefault();
      isQuitting = true;
      console.log("Window close event intercepted, shutting down services...");
      await terminateAllProcesses();
      mainWindow.destroy(); // Force destroy the window after cleanup
    }
  });

  mainWindow.on("ready-to-show", () => {
    mainWindow.show();
    // Send initial logs after the window is ready and showing
    sendLogMessage("Application started", "normal");
  });

  mainWindow.webContents.on("did-finish-load", async () => {
    kinectProcess = startKinectProcess(mainWindow);
    await checkDependencies();

    // Read configuration
    let config = await readConfig();

    // // Check if auto launch is enabled for Minecraft
    // if (config.minecraft_auto_launch === true) {
    //   sendLogMessage("Auto-launching Minecraft based on settings...", "normal");

    //   // Launch Minecraft with PrismLauncher
    //   const launched = await launchPrismLauncher("1.21.5", mainWindow);
    //   if (launched) {
    //     sendLogMessage("PrismLauncher auto-launched successfully", "success");
    //   } else {
    //     sendLogMessage("Failed to auto-launch PrismLauncher", "error");
    //   }
    // }

    // if (config.topographic_auto_launch_projector === true) {
    //   sendLogMessage(
    //     "Auto-launching Projection based on settings...",
    //     "normal"
    //   );

    //   // Launch Projection
    //   const projectionLaunched = await launchProjection(mainWindow);
    //   if (projectionLaunched) {
    //     sendLogMessage("Projection auto-launched successfully", "success");
    //   } else {
    //     sendLogMessage("Failed to auto-launch Projection", "error");
    //   }
    // }

    ipcMain.on("start-launch", async (event, instanceName) => {
      config = await readConfig();
      let launched;
      let projectionLaunched;

      if (config.minecraft_auto_launch === true) {
        sendLogMessage("Launching Minecraft based on settings...", "normal");

        // Launch Minecraft with PrismLauncher
        launched = await launchPrismLauncher("1.21.5", mainWindow);
        if (launched) {
          sendLogMessage("PrismLauncher auto-launched successfully", "success");
        } else {
          sendLogMessage("Failed to auto-launch PrismLauncher", "error");
        }
      }

      if (config.topographic_auto_launch_projector === true) {
        sendLogMessage("Launching Projection based on settings...", "normal");

        // Launch Projection
        projectionLaunched = await launchProjection(mainWindow);
        if (projectionLaunched) {
          // sendLogMessage(
          //   "Projection auto-launched successfully. Press q to quit.",
          //   "success"
          // );
        } else {
          sendLogMessage("Failed to auto-launch Projection", "error");
        }
      }

      if (config.minecraft_auto_launch === true) {
        //sendLogMessage(`PrismLauncher launch requested for ${instanceName}`, 'normal')
      } else {
        sendLogMessage(
          `PrismLauncher is not enabled for Open on Launch.`,
          "warning"
        );
        // send an update to fronend to remove "launching" text
        mainWindow.webContents.send("minecraft-ready", true);
      }
      if (config.topographic_auto_launch_projector === true) {
        //sendLogMessage(`Projection launch requested for ${instanceName}`, 'normal')
      } else {
        sendLogMessage(
          `Projection is not enabled for Open on Launch.`,
          "warning"
        );
      }
    });
    ipcMain.on("serverCommand", async (event, command) => {
      console.log(`Attempting to send RCON command: ${command}`);

      const result = await sendRconCommand(command);

      if (result.success) {
        console.log(`Command sent successfully: ${command}`);
        console.log(`Response: ${result.response}`);
        sendLogMessage(`Command sent: ${command}`, "normal");

        // Only show response if it's not empty
        if (result.response && result.response.trim() !== "") {
          sendLogMessage(`Server response: ${result.response}`, "success");
        }
      } else {
        console.error(`Failed to send command: ${result.error}`);
        sendLogMessage(`Failed to send command: ${result.error}`, "error");
      }
    });
  });

  mainWindow.webContents.setWindowOpenHandler((details) => {
    shell.openExternal(details.url);
    return { action: "deny" };
  });

  // HMR for renderer base on electron-vite cli.
  // Load the remote URL for development or the local html file for production.
  if (is.dev && process.env["ELECTRON_RENDERER_URL"]) {
    mainWindow.loadURL(process.env["ELECTRON_RENDERER_URL"]);
  } else {
    mainWindow.loadFile(join(__dirname, "../renderer/index.html"));
  }
}

// This method will be called when Electron has finished
// initialization and is ready to create browser windows.
// Some APIs can only be used after this event occurs.
app.whenReady().then(() => {
  // Set app user model id for windows
  electronApp.setAppUserModelId("com.electron");

  // Default open or close DevTools by F12 in development
  // and ignore CommandOrControl + R in production.
  // see https://github.com/alex8088/electron-toolkit/tree/master/packages/utils
  app.on("browser-window-created", (_, window) => {
    optimizer.watchWindowShortcuts(window);
  });

  createWindow();

  app.on("activate", function () {
    // On macOS it's common to re-create a window in the app when the
    // dock icon is clicked and there are no other windows open.
    if (BrowserWindow.getAllWindows().length === 0) createWindow();
  });
});

// Quit when all windows are closed, except on macOS. There, it's common
// for applications and their menu bar to stay active until the user quits
// explicitly with Cmd + Q.
app.on("window-all-closed", () => {
  if (process.platform !== "darwin") {
    app.quit();
  }
});

// Handle the before-quit event
app.on("before-quit", async (event) => {
  if (isQuitting) return; // Prevent multiple calls

  event.preventDefault(); // Prevent default quit
  isQuitting = true;

  console.log("App is quitting, terminating all processes...");
  await terminateAllProcesses();

  // Now quit the app
  app.quit();
});

// In this file you can include the rest of your app's specific main process
// code. You can also put them in separate files and require them here.
