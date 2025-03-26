import './assets/main.css'
import React from 'react'
import Button from 'react-bootstrap/Button'
import ReactDOM from 'react-dom/client'
import HomePage from './pages/HomePage'
import SettingsPage from './pages/SettingsPage'
import InfoPage from './pages/InfoPage'
import NavBar from './components/NavBar'
import KinectPage from './pages/KinectPage'
import { HashRouter, Routes, Route } from 'react-router-dom'

ReactDOM.createRoot(document.getElementById('root')).render(
  <HashRouter>
    <NavBar />
    <Routes>
      <Route path="/" element={<HomePage />} />
      <Route path="/settings" element={<SettingsPage />} />
      <Route path="/info" element={<InfoPage />} />
      <Route path="/kinect" element={<KinectPage />} />
    </Routes>
  </HashRouter>
)
