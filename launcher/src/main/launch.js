import { exec, spawn } from "child_process";
import fs from "fs";
import path from "path";
import { sendLogMessage } from "./index"; // Assuming this is your main file
import { ipcMain } from "electron"; // Import ipcMain
import { readConfig } from "./config_handler"; // Import readConfig function
import { join } from "path"; // Import join from path
import { app } from "electron"; // Import app from electron
import { send } from "process";

/**
 * Launches PrismLauncher with the specified instance.
 * @param {string} instanceName - The name of the PrismLauncher instance to launch.
 * @returns {Promise<boolean>} - True if PrismLauncher was launched successfully, false otherwise.
 */
export async function launchPrismLauncher(instanceName, mainWindow) {
  // Add mainWindow parameter
  try {
    const config = await readConfig();

    // 2. Get the PrismLauncher path from the config
    const prismLauncherPath = config?.minecraft_prismlauncher_path;

    if (!prismLauncherPath) {
      sendLogMessage(
        "PrismLauncher path is not defined in settings_config.json.",
        "error"
      );
      return false;
    }

    // 3. Check if the PrismLauncher executable exists
    if (!fs.existsSync(prismLauncherPath)) {
      sendLogMessage(
        `PrismLauncher not found at ${prismLauncherPath}`,
        "error"
      );
      mainWindow.webContents.send("minecraft-ready", true);
      return false;
    }

    // 4. Check if Minecraft is already running
    const isMinecraftRunning = await checkIfMinecraftIsRunning();
    if (isMinecraftRunning) {
      sendLogMessage(
        "Minecraft is already running. Please close it before launching again.",
        "warning"
      );
      mainWindow.webContents.send("minecraft-ready", true); // Immediately un-grey the button
      return false;
    }

    // 5. Construct the command
    const command = `"${prismLauncherPath}" -l "${instanceName}" -s localhost`; // Quote the path and instance name

    // 6. Launch PrismLauncher
    sendLogMessage(
      `Launching PrismLauncher with command: ${command}`,
      "normal"
    );

    exec(command, (error, stdout, stderr) => {
      if (error) {
        sendLogMessage(
          `PrismLauncher launch failed: ${error.message}`,
          "error"
        );
        console.error(`PrismLauncher launch error: ${error}`);
        return;
      }
      if (stderr) {
        //sendLogMessage(`PrismLauncher stderr: ${stderr}`, 'warning')
        console.warn(`PrismLauncher stderr: ${stderr}`);
      }
      //sendLogMessage(`PrismLauncher stdout: ${stdout}`, 'normal')
      console.log(`PrismLauncher stdout: ${stdout}`);
    });

    // 7. Start checking for Minecraft
    let minecraftCheckInterval = setInterval(async () => {
      if (process.platform === "win32") {
        exec(
          'tasklist /FI "imagename eq javaw.exe" /FI "windowtitle eq Minecraft*"',
          (error, stdout, stderr) => {
            if (error) {
              sendLogMessage(
                `Error checking for Minecraft process: ${error.message}`,
                "error"
              );
              console.error(`Error checking for Minecraft process: ${error}`);
              clearInterval(minecraftCheckInterval); // Stop checking on error
              return;
            }

            const isRunning =
              stdout.toLowerCase().includes("javaw.exe") &&
              !stdout.toLowerCase().includes("info: no tasks");
            if (isRunning) {
              sendLogMessage("Minecraft is ready!", "success");
              mainWindow.webContents.send("minecraft-ready", true); // Send IPC event
              clearInterval(minecraftCheckInterval); // Stop checking
            }
          }
        );
      } else {
        // Implement macOS/Linux check here if needed
        clearInterval(minecraftCheckInterval); // Stop checking if not Windows
        sendLogMessage(
          "Minecraft ready check is only implemented on Windows.",
          "warning"
        );
      }
    }, 2000);

    return true; // Assume success (non-blocking)
  } catch (error) {
    sendLogMessage(`Error launching PrismLauncher: ${error.message}`, "error");
    console.error(`Error launching PrismLauncher:`, error);
    return false;
  }
}

async function isProjectionRunning() {
  return new Promise((resolve) => {
    // Windows specific check - simpler approach
    exec('tasklist | findstr /I "python"', (error, stdout, stderr) => {
      if (error) {
        console.error(`Error checking for Python process: ${error}`);
        resolve(false); // Assume not running on error
        return;
      }

      if (!stdout.trim()) {
        // No Python processes found
        resolve(false);
        return;
      }

      // Check if topoprojection.py is in the command line
      exec(
        "wmic process where \"name like '%python%'\" get commandline",
        (err, output) => {
          if (err) {
            console.error(`Error checking Python command line: ${err}`);
            resolve(false);
            return;
          }

          const isRunning = output.toLowerCase().includes("topoprojection.py");
          resolve(isRunning);
        }
      );
    });
  });
}

export async function launchProjection(mainWindow) {
  try {
    // First, check if projection is already running
    const alreadyRunning = await isProjectionRunning();
    if (alreadyRunning) {
      const errorMsg = "Topographic projection is already running";
      sendLogMessage(errorMsg, "warning");
      return false;
    }
    sendLogMessage("Starting topographic projection...", "normal");

    const pythonExecutable = "python";
    let scriptPath;
    let requirementsPath;

    if (app.isPackaged) {
      scriptPath = path.join(process.resourcesPath, "topoprojection.py");
      requirementsPath = path.join(process.resourcesPath, "requirements.txt");
    } else {
      scriptPath = path.join(__dirname, "../../topoprojection.py");
      requirementsPath = path.join(__dirname, "../../requirements.txt");
    }

    // Install requirements first
    sendLogMessage("Checking Python dependencies...", "normal");
    try {
      await new Promise((resolve, reject) => {
        const pipProcess = spawn(pythonExecutable, [
          "-m",
          "pip",
          "install",
          "-r",
          requirementsPath,
        ]);

        pipProcess.stdout.on("data", (data) => {
          console.log(`pip stdout: ${data}`);
        });

        pipProcess.stderr.on("data", (data) => {
          console.error(`pip stderr: ${data}`);
        });

        pipProcess.on("close", (code) => {
          if (code === 0) {
            sendLogMessage("Dependencies installed successfully", "success");
            resolve();
          } else {
            reject(new Error(`pip process exited with code ${code}`));
          }
        });

        pipProcess.on("error", reject);
      });
    } catch (pipError) {
      sendLogMessage(
        `Failed to install dependencies: ${pipError.message}`,
        "error"
      );
      mainWindow.webContents.send("logMessage", {
        text: `Failed to install dependencies: ${pipError.message}`,
        type: "error",
      });
      return false;
    }

    // Spawn Python process
    const projectionProcess = spawn(pythonExecutable, [scriptPath], {
      stdio: ["pipe", "pipe", "pipe"],
    });

    // Handle stdout
    projectionProcess.stdout.on("data", (data) => {
      console.log(`Projection stdout: ${data}`);
      //sendLogMessage(`${data}`, 'normal')
      // mainWindow.webContents.send('logMessage', {
      //   text: `Projection: ${data}`,
      //   type: 'normal'
      // })
    });

    // Handle stderr
    projectionProcess.stderr.on("data", (data) => {
      console.error(`Projection stderr: ${data}`);
      sendLogMessage(`Projection Error: ${data}`, "error");
      mainWindow.webContents.send("logMessage", {
        text: `Projection Error: ${data}`,
        type: "error",
      });
    });

    // Handle process exit
    projectionProcess.on("close", (code) => {
      console.log(`Projection process exited with code ${code}`);
      if (code !== 0) {
        sendLogMessage(`Projection process exited with code ${code}`, "error");
        mainWindow.webContents.send("logMessage", {
          text: `Projection process exited with code ${code}`,
          type: "error",
        });
      } else {
        sendLogMessage("Projection process closed successfully", "success");
      }
    });

    // Handle process errors
    projectionProcess.on("error", (err) => {
      console.error("Failed to start projection:", err);
      sendLogMessage(`Failed to start projection: ${err.message}`, "error");
      mainWindow.webContents.send("logMessage", {
        text: `Failed to start projection: ${err.message}`,
        type: "error",
      });
      return false;
    });

    // Store the process for cleanup
    global.projectionProcess = projectionProcess;
    sendLogMessage(
      "Projection started successfully. Press q to quit.",
      "success"
    );

    return true;
  } catch (error) {
    console.error("Error launching projection:", error);
    sendLogMessage(`Error launching projection: ${error.message}`, "error");
    mainWindow.webContents.send("logMessage", {
      text: `Error launching projection: ${error.message}`,
      type: "error",
    });
    return false;
  }
}

/**
 * Checks if Minecraft is already running by checking window titles.
 * @returns {Promise<boolean>} - True if Minecraft is running, false otherwise.
 */
async function checkIfMinecraftIsRunning() {
  return new Promise((resolve) => {
    if (process.platform === "win32") {
      // Windows-specific code
      exec(
        'tasklist /FI "imagename eq javaw.exe" /FI "windowtitle eq Minecraft*"',
        (error, stdout, stderr) => {
          if (error) {
            sendLogMessage(
              `Error checking for Minecraft process: ${error.message}`,
              "error"
            );
            console.error(`Error checking for Minecraft process: ${error}`);
            resolve(false);
            return;
          }

          // If any output is returned, it means a Minecraft process is running
          const isRunning =
            stdout.toLowerCase().includes("javaw.exe") &&
            !stdout.toLowerCase().includes("info: no tasks");
          resolve(isRunning);
        }
      );
    } else {
      // macOS and Linux (less accurate, but better than nothing)
      exec("ps -ax | grep java", (error, stdout, stderr) => {
        if (error) {
          sendLogMessage(
            `Error checking for Minecraft process: ${error.message}`,
            "error"
          );
          console.error(`Error checking for Minecraft process: ${error}`);
          resolve(false);
          return;
        }

        const isRunning =
          stdout.toLowerCase().includes("minecraft") &&
          stdout.toLowerCase().includes("java");
        resolve(isRunning);
      });
    }
  });
}
