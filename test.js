const fs = require("fs");

//512x424
function generate2DArray() {
  const array = [];
  for (let i = 0; i < 100; i++) {
    const row = [];
    for (let j = 0; j < 100; j++) {
      row.push(parseInt(Math.random() * (3200 - 1958)) + 1958);
    }
    array.push(row);
  }
  return array;
}

setInterval(() => {
  const array = generate2DArray();
  const message = JSON.stringify(array);
  fs.writeFile("output.txt", message, (err) => {
    if (err) {
      console.error("Error writing to file", err);
    } else {
      console.log("Data written to output.txt");
    }
  });
}, 1000);

console.log("Data generation and writing to file is running every 1 seconds.");
