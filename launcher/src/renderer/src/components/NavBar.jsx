import Button from 'react-bootstrap/Button'
import { useNavigate } from 'react-router-dom' // Import useNavigate
import homeIcon from '@renderer/assets/icons/home_icon.png'
import settingsIcon from '@renderer/assets/icons/settings_icon.png'
import infoIcon from '@renderer/assets/icons/info_icon.png'

function NavBar() {
  const navigate = useNavigate() // Get the navigate function

  return (
    <div className="navContainer">
      <Button className="navButtons buttonDropshadow" onClick={() => navigate('/')}>
        <img src={homeIcon} alt="Home"></img> Home
      </Button>
      <Button className="navButtons buttonDropshadow" onClick={() => navigate('/settings')}>
        <img src={settingsIcon} alt="Settings"></img> Settings
      </Button>
      <Button className="navButtons buttonDropshadow" onClick={() => navigate('/info')}>
        <img src={infoIcon} alt="Info"></img> Info
      </Button>
    </div>
  )
}

export default NavBar
