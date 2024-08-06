# How To Run

## SkyWay Consoleの登録
下記のリンクより、SkyWay Consoleを登録します：
- https://console.skyway.ntt.com/signup

SkyWay Consoleにて、アプリ登録が終わりましたら、AppIDとSecretKeyが発行されます。

skyway_token_generater/token.jsを開き、5行目と6行目のappIdとSecretKeyの値を書き換えてください。
```
const appId = "ここにアプリケーションIDをペーストしてください"
const secretKey = "ここにシークレットキーをペーストしてください"
```

## SkyWay Auth Tokenの生成

上記の書き換えが終わりましたら、 Terminal にて以下のコマンドを叩くと、SkyWay Auth Tokenが生成されます。
```
cd skyway_token_generater
npm install
node token.js
```

SkyWay Auth Tokenが生成されたら、MainViewModel.ktの26行目の値を差し替えてください。
```
authToken = "PLEASE_SET_YOUR_AUTH_TOKEN",
```

## Build&Run
2台のテスト端末(Emulator可)を用意し、アプリをビルドして実行してみてください。