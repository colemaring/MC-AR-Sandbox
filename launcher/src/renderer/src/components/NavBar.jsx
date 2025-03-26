import Button from 'react-bootstrap/Button'
import { useNavigate, useLocation } from 'react-router-dom'
import homeIcon from '@renderer/assets/icons/home_icon.png'
import settingsIcon from '@renderer/assets/icons/settings_icon.png'
import infoIcon from '@renderer/assets/icons/info_icon.png'
import { useState, useEffect } from 'react'

function NavBar() {
  const navigate = useNavigate()
  const location = useLocation()
  const [isVisible, setIsVisible] = useState(true)

  useEffect(() => {
    if (location.pathname === '/kinect') {
      setIsVisible(false)
    } else {
      setIsVisible(true)
    }
  }, [location.pathname])

  const isActive = (path) => {
    return location.pathname === path
  }

  if (!isVisible) {
    return null // Hide the navbar
  }

  return (
    <div className="navContainer">
      <Button
        className={`navButtons buttonDropshadow ${isActive('/') ? 'active' : ''}`}
        onClick={() => navigate('/')}
      >
        <img src={homeIcon} alt="Home"></img> Home
      </Button>
      <Button
        className={`navButtons buttonDropshadow ${isActive('/settings') ? 'active' : ''}`}
        onClick={() => navigate('/settings')}
      >
        <img src={settingsIcon} alt="Settings"></img> Settings
      </Button>
      <Button
        className={`navButtons buttonDropshadow ${isActive('/info') ? 'active' : ''}`}
        onClick={() => navigate('/info')}
      >
        <img src={infoIcon} alt="Info"></img> Info
      </Button>
    </div>
  )
}

export default NavBar
