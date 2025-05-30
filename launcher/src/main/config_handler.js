const { ipcMain, app } = require("electron");
const fs = require("fs");
const path = require("path");

// Use app.getPath('userData') for the config file location
const configPath = path.join(app.getPath("userData"), "settings_config.json");

export async function readConfig() {
  try {
    // Check if the file exists in the userData directory
    if (!fs.existsSync(configPath)) {
      // Create a new config file with the specified structure
      const defaultConfig = {
        kinect_view_crop: {
          x1: 44,
          y1: 111,
          x2: 404,
          y2: 348,
        },
        y_coord_offset: 1700,
        kinect_distance_mm: 1800,
        kinect_capture_speed: 25,
        topographic_smoothing: 19,
        topographic_color_mode: "Natural",
        topographic_interpolation: "None",
        topographic_auto_launch_projector: false,
        minecraft_display_on_launch: false,
        minecraft_elevation: 4,
        minecraft_auto_launch: false,
        minecraft_prismlauncher_path:
          "C:\\Users\\colem\\AppData\\Local\\Programs\\PrismLauncher\\prismlauncher.exe",
      };

      // Ensure the directory exists
      const configDir = path.dirname(configPath);
      if (!fs.existsSync(configDir)) {
        fs.mkdirSync(configDir, { recursive: true });
      }

      fs.writeFileSync(
        configPath,
        JSON.stringify(defaultConfig, null, 2),
        "utf8"
      );
      console.log("New config file created at:", configPath);
    }

    const data = fs.readFileSync(configPath, "utf8");
    console.log("Config read successfully from:", configPath);
    return JSON.parse(data);
  } catch (error) {
    console.error("Failed to read config:", error);
    return {}; // Return an empty object if all else fails
  }
}

export async function writeConfig(event, newConfig) {
  try {
    // Ensure the directory exists
    const configDir = path.dirname(configPath);
    if (!fs.existsSync(configDir)) {
      fs.mkdirSync(configDir, { recursive: true });
    }

    fs.writeFileSync(configPath, JSON.stringify(newConfig, null, 2), "utf8");
    console.log("Config written successfully to:", configPath);
    return { success: true };
  } catch (error) {
    console.error("Failed to write config:", error);
    return { success: false, error: error.message };
  }
}

ipcMain.handle("readConfig", readConfig);
ipcMain.handle("writeConfig", writeConfig);
