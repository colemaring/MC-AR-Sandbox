import React from 'react'
import Logs from '../components/Logs'
import SendCommands from '../components/SendCommands'
import LaunchButton from '../components/LaunchButton'

function HomePage() {
  return (
    <div className="pageContainer">
      <Logs />
      <SendCommands />
      <LaunchButton />
    </div>
  )
}

export default HomePage
