# 概要

# How To Run
## 1. SkyWay Auth Token の更新
1. SkyWay Console にて、アプリ登録してください
2. skyway_token_generater/token.jsを開き、アプリ登録した際に得られた AppID と SecretKey を 書き換えてください
3. コンソールで node token.js を実行し、アプリを動かすためのSkyWay Auth Tokenが、app/src/main/res/raw/auth_token.txt に更新されます

### 注意点
生成されたAuth Tokenの有効期限は一日です。有効期限を延びたい場合、
- 再度node token.jsを生成し直して、有効期限を一日にreflashする
- token.jsの `exp` の時間を調整する

## 2. 擬似サーバを起動
以下のコマンドを実行すると、Serverが立ち上げ、Browserよりアクセス可能なURLが表示されます。
```
cd family-server
npm run dev
```
Browserにて、CLIで得られたURLを開きます。

room nameを設定し、joinをクリックすると、該当Roomが作られ、online状態になります
設定できる room nameは、Android側のサンプルアプリと連動するため、以下の五つでお願いします
- LivingRoom
- BedRoom
- Kitchen
- BabyRoom
- PetRoom

### 注意点
- joinボタン押した後に、配信止めたい場合必ずcloseボタンをクリックしてください

## 3. Androidサンプルアプリ起動
基本的にそのままビルド&実行可能。
