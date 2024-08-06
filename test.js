const Kinect2 = require("kinect2");
const kinect = new Kinect2();
const fs = require("fs");

const main = async () => {
  if (kinect.open()) {
    console.log("Kinect Opened");
    kinect.openDepthReader();

    let writing = false;

    // Set up an event listener to receive depth frame data
    kinect.on("depthFrame", (depthFrame) => {
      if (!writing) {
        writing = true;

        const width = 512; // Kinect depth frame width
        const height = 424; // Kinect depth frame height

        // Convert the Buffer to a 2D array
        const depthArray = Array.from(
          { length: Math.floor(height * 1.6) },
          (_, y) =>
            Array.from({ length: width }, (_, x) => {
              const originalY = Math.floor(y / 1.6); // Map new y to original y
              return depthFrame[originalY * width + x] * 25;
            })
        );
        // Write the output to a file, overwriting any existing content
        fs.writeFileSync("output.txt", JSON.stringify(depthArray));
        console.log("Output saved to output.txt");

        // Schedule the next write after a 1-second delay
        setTimeout(() => {
          writing = false;
        }, 1000);
      }
    });

    // Keep the program running to receive depth frame data
    while (true) {
      await new Promise((resolve) => setTimeout(resolve, 1000));
    }
  }
};

main();
