import './assets/main.css'
import React from 'react'
import Button from 'react-bootstrap/Button'
import ReactDOM from 'react-dom/client'
import HomePage from './pages/HomePage'
import SettingsPage from './pages/SettingsPage'
import InfoPage from './pages/InfoPage'
import NavBar from './components/NavBar'
import { BrowserRouter, Routes, Route } from 'react-router-dom'

ReactDOM.createRoot(document.getElementById('root')).render(
  <BrowserRouter>
    <NavBar />
    <Routes>
      <Route path="/" element={<HomePage />} />
      <Route path="/settings" element={<SettingsPage />} />
      <Route path="/info" element={<InfoPage />} />
    </Routes>
  </BrowserRouter>
)
