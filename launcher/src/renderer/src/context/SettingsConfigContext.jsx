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
  const [testMode, setTestMode] = useState(false)

  // Topographic settings
  const [displayOnLaunchTopographic, setDisplayOnLaunchTopographic] = useState(false)
  const [displayTopographic, setDisplayTopographic] = useState('Display 1')
  const [smoothing, setSmoothing] = useState(40)
  const [colorMode, setColorMode] = useState('Default')

  // Minecraft settings
  const [displayOnLaunchMinecraft, setDisplayOnLaunchMinecraft] = useState(false)
  const [displayMinecraft, setDisplayMinecraft] = useState('Display 2')

  useEffect(() => {
    const loadConfig = async () => {
      try {
        const config = await window.electronAPI.readConfig()

        setX1(config?.kinect_view_crop?.x1 || 0)
        setY1(config?.kinect_view_crop?.y1 || 0)
        setX2(config?.kinect_view_crop?.x2 || 0)
        setY2(config?.kinect_view_crop?.y2 || 0)
        setDistance(config?.kinect_surface_distance_cm || 0)
        setTestMode(config?.test_mode || false)
        setDisplayOnLaunchTopographic(config?.topographic_display_on_launch || false)
        setDisplayTopographic(config?.topographic_display_assignment || 'Display 1')
        setSmoothing(config?.topographic_smoothing || 40)
        setColorMode(config?.topographic_color_mode || 'Default')
        setDisplayOnLaunchMinecraft(config?.minecraft_display_on_launch || false)
        setDisplayMinecraft(config?.minecraft_display_assignment || 'Display 2')
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
      test_mode: testMode,
      topographic_display_on_launch: displayOnLaunchTopographic,
      topographic_display_assignment: displayTopographic,
      topographic_smoothing: smoothing,
      topographic_color_mode: colorMode,
      minecraft_display_on_launch: displayOnLaunchMinecraft,
      minecraft_display_assignment: displayMinecraft
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
    testMode,
    displayOnLaunchTopographic,
    displayTopographic,
    smoothing,
    colorMode,
    displayOnLaunchMinecraft,
    displayMinecraft
  ])

  return (
    <SettingsConfigContext.Provider
      value={{
        x1,
        y1,
        x2,
        y2,
        distance,
        testMode,
        displayOnLaunchTopographic,
        displayTopographic,
        smoothing,
        colorMode,
        displayOnLaunchMinecraft,
        displayMinecraft,
        setX1,
        setY1,
        setX2,
        setY2,
        setDistance,
        setTestMode,
        setDisplayOnLaunchTopographic,
        setDisplayTopographic,
        setSmoothing,
        setColorMode,
        setDisplayOnLaunchMinecraft,
        setDisplayMinecraft,
        writeToConfig // call when you want to write state to config
      }}
    >
      {children}
    </SettingsConfigContext.Provider>
  )
}
