import { useContext, useState, useRef, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import Button from 'react-bootstrap/Button'
import backIcon from '@renderer/assets/icons/back.png'
import placeholder from '@renderer/assets/placeholder.png'
import { SettingsConfigContext } from '../context/SettingsConfigContext'

function KinectPage() {
  const navigate = useNavigate()
  const { x1, x2, y1, y2, setX1, setX2, setY1, setY2 } = useContext(SettingsConfigContext)

  // Track which handle is being dragged: 'tl', 'tr', 'bl', or 'br'
  const [draggingHandle, setDraggingHandle] = useState(null)
  const containerRef = useRef(null)

  // Mouse move handler updates the appropriate coordinates,
  // ensuring that the box cannot be mirrored.
  useEffect(() => {
    const handleMouseMove = (e) => {
      if (!draggingHandle || !containerRef.current) return
      const { left, top } = containerRef.current.getBoundingClientRect()
      const mouseX = e.clientX - left
      const mouseY = e.clientY - top

      switch (draggingHandle) {
        case 'tl': // top left
          setX1(Math.min(mouseX, x2))
          setY1(Math.min(mouseY, y2))
          break
        case 'tr': // top right
          setX2(Math.max(mouseX, x1))
          setY1(Math.min(mouseY, y2))
          break
        case 'bl': // bottom left
          setX1(Math.min(mouseX, x2))
          setY2(Math.max(mouseY, y1))
          break
        case 'br': // bottom right
          setX2(Math.max(mouseX, x1))
          setY2(Math.max(mouseY, y1))
          break
        default:
          break
      }
    }

    const handleMouseUp = () => {
      setDraggingHandle(null)
    }

    window.addEventListener('mousemove', handleMouseMove)
    window.addEventListener('mouseup', handleMouseUp)

    return () => {
      window.removeEventListener('mousemove', handleMouseMove)
      window.removeEventListener('mouseup', handleMouseUp)
    }
  }, [draggingHandle, setX1, setX2, setY1, setY2, x1, x2, y1, y2])

  // Helper to style the draggable handle (red circle)
  const handleStyle = (left, top) => ({
    position: 'absolute',
    left: left - 8, // adjust for handle radius (8px)
    top: top - 8,
    width: 16,
    height: 16,
    borderRadius: '50%',
    backgroundColor: 'rgb(59, 125, 141)',
    cursor: 'grab',
    zIndex: 2
  })

  // Calculate overlay styles based on current crop points.
  // (This example uses an overlay with a border; you can later replace
  //  it with CSS classes.)
  const overlayStyleDynamic = {
    left: Math.min(x1, x2),
    top: Math.min(y1, y2),
    width: Math.abs(x2 - x1),
    height: Math.abs(y2 - y1)
  }

  return (
    <div className="kinectContainer">
      <p>Crop the kinect view to correspond with the boundaries of the sandbox.</p>
      <div className="kinectCropContainer" ref={containerRef}>
        <img className="kinectPlaceholderImg" src={placeholder} alt="Placeholder" />

        {/* Overlay drawn as a rectangle between the crop points */}
        {x1 !== null && x2 !== null && y1 !== null && y2 !== null && (
          <div className="overlayStyle" style={overlayStyleDynamic} />
        )}

        {/* Draggable handles */}
        <div
          style={handleStyle(x1, y1)}
          onMouseDown={(e) => {
            e.stopPropagation()
            setDraggingHandle('tl')
          }}
        />
        <div
          style={handleStyle(x2, y1)}
          onMouseDown={(e) => {
            e.stopPropagation()
            setDraggingHandle('tr')
          }}
        />
        <div
          style={handleStyle(x1, y2)}
          onMouseDown={(e) => {
            e.stopPropagation()
            setDraggingHandle('bl')
          }}
        />
        <div
          style={handleStyle(x2, y2)}
          onMouseDown={(e) => {
            e.stopPropagation()
            setDraggingHandle('br')
          }}
        />
      </div>
      x1: {Math.round(x1)}, x2: {Math.round(x2)}, y1: {Math.round(y1)}, y2: {Math.round(y2)}
      <div>
        <Button onClick={() => navigate('/settings')} className="mt-1 backButton">
          <img style={{ width: '1.2rem', height: '1.2rem' }} src={backIcon} alt="Back" /> Back
        </Button>
      </div>
    </div>
  )
}

export default KinectPage
