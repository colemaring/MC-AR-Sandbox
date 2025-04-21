import Accordion from 'react-bootstrap/Accordion'

function InfoPage() {
  return (
    <div className="pageContainer">
      <h3 className="mt-2">In-Game GUI</h3>
      <Accordion>
        <Accordion.Item eventKey="0">
          <Accordion.Header>Biome Menu (Compass)</Accordion.Header>
          <Accordion.Body>
            Here is where you can choose from 7 different biomes to render your terran in. You can
            also toggle water to be on or off. The water toggle is global and its state will persist
            across biome changes. Its default state is disabled. Each biome has a unique water
            level, and the nether will have lava instead of water.
          </Accordion.Body>
        </Accordion.Item>
        <Accordion.Item eventKey="1">
          <Accordion.Header>Game Menu (Nether Star)</Accordion.Header>
          <Accordion.Body>
            Here is where you can choose from 7 different gamemodes to play. You can view a brief
            description of the gamemode by hovering over the item in Minecraft or viewing the
            Gamemode Information section below. The Redstone block will stop any currently active
            gamemode should you decided to cancel the current game. If you start a new gamemode
            while one is currently active, it will cancel the current gamemode and start the new
            one.
          </Accordion.Body>
        </Accordion.Item>
      </Accordion>
      <h3 className="mt-4">Gamemode Information</h3>
      <Accordion>
        <Accordion.Item eventKey="0">
          <Accordion.Header>Zombie Rush</Accordion.Header>
          <Accordion.Body>
            Stop an army of zombies from traversing your terrain! <br></br>
            Before the Zombie Rush beings, you will have 30 second to prepare your terrain. The more
            difficult your terrain is to traverse, the less likely zombies are to reach the other
            side. After the 30 seconds are up, the terrain will be locked and no further edits can
            be made for 1 minute while the zombies attempt to traverse it. Zombie start on the left
            and move right. After the 1 minute is up, you will be informed of how many zombies
            crossed the terrain and the terrain will be unlocked.
          </Accordion.Body>
        </Accordion.Item>
        <Accordion.Item eventKey="1">
          <Accordion.Header>Ore Hunt</Accordion.Header>
          <Accordion.Body>
            Find as many buried ores as possible in 30 seconds! <br></br>Points are rewarded on the
            following scale:<br></br> Coal = 5 pts, Iron = 10 pts, Diamond = 15 pts, Emerald = 15
            pts. <br></br>In order for the ores you uncover to count, the ore needs to be completely
            uncovered, meaning the surrounding blocks are all empty. 2 Player mode is also
            available, where there is a wall down the middle of the terrain and you are rewarded for
            ores you find in your half of the terrain. After the 30 seconds are up, you will be
            informed of the points you earned.
          </Accordion.Body>
        </Accordion.Item>
        <Accordion.Item eventKey="2">
          <Accordion.Header>Dig Roulette</Accordion.Header>
          <Accordion.Body>
            Dig up as much gold as possible without hitting TNT! <br></br>
            Uncovering TNT will immediatley end the game. When the game ends (either from hitting
            TNT or after 30 seconds), you will be informed of how much gold you uncovered. TNT is
            less common in easy mode and more common in hard mode.
          </Accordion.Body>
        </Accordion.Item>
        <Accordion.Item eventKey="3">
          <Accordion.Header>Volcano Simulator 🛠️</Accordion.Header>
          <Accordion.Body>
            (WIP)<br></br>
            The idea for this gamemode is that you have 30 seconds to build a volcano, and after
            those 30 seconds you will watch the volcano erupt and modify the terrain around it.
          </Accordion.Body>
        </Accordion.Item>
        <Accordion.Item eventKey="4">
          <Accordion.Header>Aquaduct 🛠️</Accordion.Header>
          <Accordion.Body>
            (WIP) <br></br>
            The idea for this gamemode is that a source of water and a sink for the water to go into
            are randomly placed onto the terrain. You then have 1 minute to sculpt the terrain such
            that the water flows from the source to the sink.
          </Accordion.Body>
        </Accordion.Item>
      </Accordion>

      <a
        className="mt-4"
        href="#"
        onClick={(e) => {
          e.preventDefault()
          if (window.customElectron?.openExternal) {
            window.customElectron.openExternal('https://github.com/colemaring/MC-AR-Sandbox')
          } else {
            console.error('Electron API not available')
          }
        }}
      >
        github.com/colemaring/MC-AR-Sandbox
      </a>
    </div>
  )
}

export default InfoPage
