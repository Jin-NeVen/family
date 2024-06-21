const fs = require('fs');
const path = require('path');
const { SkyWayAuthToken, uuidV4 } = require("@skyway-sdk/token");

const token = new SkyWayAuthToken({
  jti: uuidV4(),
  iat: Math.floor(Date.now() / 1000),
  exp: Math.floor(Date.now() / 1000) + 60 * 60 * 24,
  scope: {
    app: {
      id: "b72ba745-4f87-48e5-a823-fa3107440350",
      turn: true,
      actions: ["read"],
      channels: [
        {
          id: "*",
          name: "*",
          actions: ["write"],
          members: [
            {
              id: "*",
              name: "*",
              actions: ["write"],
              publication: {
                actions: ["write"],
              },
              subscription: {
                actions: ["write"],
              },
            },
          ],
          sfuBots: [
            {
              actions: ["write"],
              forwardings: [
                {
                  actions: ["write"],
                },
              ],
            },
          ],
        },
      ],
    },
  },
}).encode("q8RiV/151sRx/H2tTfkTIA2E51Dy1mrBWhqxePzsAKg=");
console.log(token);

const filePath = path.join(__dirname, '../family-client/app/src/main/res/raw/auth_token.txt');

// ファイルに書き込む
fs.writeFile(filePath, token, 'utf8', (err) => {
    if (err) {
        console.error('An error occurred while writing the file:', err);
    } else {
        console.log('File has been saved successfully at', filePath);
    }
});