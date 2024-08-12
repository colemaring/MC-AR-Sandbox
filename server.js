const Kinect2 = require("kinect2");
const kinect = new Kinect2();
const fs = require("fs");

const main = async (useRandom) => {
  if (useRandom) {
    console.log("Using random numbers");

    const width = 512; // Kinect depth frame width
    const height = 424; // Kinect depth frame height

    // Generate a 2D array with random numbers
    const depthArray = Array.from(
      { length: Math.floor(height * 1.6) },
      () =>
        Array.from({ length: width }, () =>
          Math.floor(Math.random() * (3200 - 1958)) + 1958
        )
    );

    // Write the output to a file, overwriting any existing content
    fs.writeFileSync("output.txt", JSON.stringify(depthArray));

    // Keep the program running to generate new random data
    while (true) {
      await new Promise((resolve) => setTimeout(resolve, 1000));
      // Generate new random data
      depthArray.forEach((row, y) =>
        row.forEach((_, x) => {
          depthArray[y][x] = Math.floor(Math.random() * (3200 - 1958)) + 1958;
        })
      );
      // Write the new data to the file
      fs.writeFileSync("output.txt", JSON.stringify(depthArray));
    }
  } else {
    console.log("Using Kinect");
    kinect.open();
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
	console.log("writing to output.txt");

        // Schedule the next write after a 1-second delay
        setTimeout(() => {
          writing = false;
        }, 100);
      }
    });

    // Keep the program running to receive depth frame data
    while (true) {
      await new Promise((resolve) => setTimeout(resolve, 1000));
    }
  }
};

const useRandom = process.argv[2] === "random";
main(useRandom);
