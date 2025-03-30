import { createContext, useContext, useState, useEffect, useRef } from 'react'

// listen to IPC channel logMessage from main process
// add log messages to state when received (also export this function to be used in other components)
// provide getFormattedLogs to hold messages formatted with dates and by joining them with newlines

export const LogMessageContext = createContext()

export const useLogMessages = () => {
  return useContext(LogMessageContext)
}

export const LogMessageProvider = ({ children }) => {
  const [logMessages, setLogMessages] = useState([])

  // Add a new log message
  const addLogMessage = (message, type = 'normal') => {
    const timestamp = new Date().toLocaleTimeString()
    setLogMessages((prevLogs) => {
      // Simply add the new log to the array
      return [...prevLogs, { message, type, timestamp }]
    })
  }

  // Listen for log messages from the main process
  useEffect(() => {
    const handleLogMessage = (messageData) => {
      addLogMessage(messageData.text, messageData.type || 'normal')
    }

    // Register listener for log messages from main process
    window.electronAPI.logMessage(handleLogMessage)
  }, [])

  const getFormattedLogs = () => {
    return logMessages
      .map((log) => {
        let color = 'black' // Default color
        switch (log.type) {
          case 'success':
            color = 'green'
            break
          case 'normal':
            color = 'black'
            break
          case 'warning':
            color = 'orange'
            break
          case 'error':
            color = 'red'
            break
          default:
            color = 'black'
        }
        return `<span style="color: ${color}">[${log.timestamp}] ${log.message}</span>`
      })
      .join('<br/>') // Use <br/> for HTML line breaks
  }

  const contextValue = {
    logMessages,
    addLogMessage,
    // Formatted logs as a string for the textarea
    getFormattedLogs: getFormattedLogs
  }

  return <LogMessageContext.Provider value={contextValue}>{children}</LogMessageContext.Provider>
}
