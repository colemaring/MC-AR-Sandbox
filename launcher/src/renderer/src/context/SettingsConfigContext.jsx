import React, { createContext, useState, useEffect, useCallback } from 'react'
import { useLogMessages } from './LogMessageContext'
export const SettingsConfigContext = createContext()

export const SettingsConfigProvider = ({ children }) => {
  const { addLogMessage } = useLogMessages() // call to add message to log context state
  // Kinect settings
  const [x1, setX1] = useState(0)
  const [y1, setY1] = useState(0)
  const [x2, setX2] = useState(0)
  const [y2, setY2] = useState(0)
  const [distance, setDistance] = useState(0)
  const [captureSpeed, setCaptureSpeed] = useState(50)

  // Topographic settings
  const [displayOnLaunchTopographic, setDisplayOnLaunchTopographic] = useState(false)
  const [displayTopographic, setDisplayTopographic] = useState('Display 1')
  const [smoothing, setSmoothing] = useState(40)
  const [colorMode, setColorMode] = useState('Default')
  const [autoLaunchProjector, setAutoLaunchProjector] = useState(false)
  const [interpolation, setInterpolation] = useState('None')

  // Minecraft settings
  const [displayMinecraft, setDisplayMinecraft] = useState('Display 2')
  const [prismlauncherPath, setPrismlauncherPath] = useState(
    'C:\\Users\\colem\\AppData\\Local\\Programs\\PrismLauncher\\prismlauncher.exe'
  )
  const [autoLaunchMinecraft, setAutoLaunchMinecraft] = useState(false)
  const [elevation, setElevation] = useState(40)

  useEffect(() => {
    const loadConfig = async () => {
      try {
        const config = await window.electronAPI.readConfig()

        setX1(config?.kinect_view_crop?.x1 || 0)
        setY1(config?.kinect_view_crop?.y1 || 0)
        setX2(config?.kinect_view_crop?.x2 || 0)
        setY2(config?.kinect_view_crop?.y2 || 0)
        setDistance(config?.kinect_surface_distance_cm || 0)
        setCaptureSpeed(config?.kinect_capture_speed || 15)
        setDisplayOnLaunchTopographic(config?.topographic_display_on_launch || false)
        setDisplayTopographic(config?.topographic_display_assignment || 'Display 1')
        setSmoothing(config?.topographic_smoothing || 40)
        setColorMode(config?.topographic_color_mode || 'Default')
        setInterpolation(config?.topographic_interpolation || 'None')
        setAutoLaunchProjector(config?.topographic_auto_launch_projector || false)
        setElevation(config?.minecraft_elevation || 40)
        setDisplayMinecraft(config?.minecraft_display_assignment || 'Display 2')
        setAutoLaunchMinecraft(config?.minecraft_auto_launch || false)
        setPrismlauncherPath(
          config?.minecraft_prismlauncher_path ||
            'C:\\Users\\colem\\AppData\\Local\\Programs\\PrismLauncher\\prismlauncher.exe'
        )
        addLogMessage('Configuration loaded successfully', 'success')
      } catch (error) {
        console.error('Failed to load config:', error)
        addLogMessage(`Failed to load config`, 'error')
      }
    }

    loadConfig()
  }, [])

  const writeToConfig = useCallback(async () => {
    const config = {
      kinect_view_crop: { x1, y1, x2, y2 },
      kinect_surface_distance_cm: distance,
      kinect_capture_speed: captureSpeed,
      topographic_display_on_launch: displayOnLaunchTopographic,
      topographic_display_assignment: displayTopographic,
      topographic_smoothing: smoothing,
      topographic_color_mode: colorMode,
      topographic_interpolation: interpolation,
      topographic_auto_launch_projector: autoLaunchProjector,
      minecraft_elevation: elevation,
      minecraft_display_assignment: displayMinecraft,
      minecraft_auto_launch: autoLaunchMinecraft,
      minecraft_prismlauncher_path: prismlauncherPath
    }

    try {
      const result = await window.electronAPI.writeConfig(config)
      if (result?.success) {
        console.log('Config written successfully to file')
        addLogMessage('Settings saved successfully', 'success')
        return true
      } else {
        console.error('Failed to write config to file:', result?.error)
        addLogMessage(`Failed to save settings: ${result?.error || 'Unknown error'}`, 'error')
        return false
      }
    } catch (error) {
      console.error('Failed to write config to file:', error)
      addLogMessage(`Failed to save settings: ${error.message}`, 'error')
      return false
    }
  }, [
    x1,
    y1,
    x2,
    y2,
    distance,
    displayOnLaunchTopographic,
    displayTopographic,
    smoothing,
    elevation,
    colorMode,
    displayMinecraft,
    prismlauncherPath,
    autoLaunchMinecraft,
    autoLaunchProjector,
    captureSpeed,
    interpolation
  ])

  return (
    <SettingsConfigContext.Provider
      value={{
        x1,
        y1,
        x2,
        y2,
        distance,
        displayOnLaunchTopographic,
        displayTopographic,
        smoothing,
        elevation,
        setElevation,
        colorMode,
        displayMinecraft,
        prismlauncherPath,
        setX1,
        setY1,
        setX2,
        setY2,
        setDistance,
        setDisplayOnLaunchTopographic,
        setDisplayTopographic,
        setSmoothing,
        setColorMode,
        setDisplayMinecraft,
        setPrismlauncherPath,
        autoLaunchMinecraft,
        setAutoLaunchMinecraft,
        autoLaunchProjector,
        setAutoLaunchProjector,
        captureSpeed,
        setCaptureSpeed,
        interpolation,
        setInterpolation,
        writeToConfig // call when you want to write state to config
      }}
    >
      {children}
    </SettingsConfigContext.Provider>
  )
}
