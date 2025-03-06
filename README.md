# Telegram Bot to track PUMP.FUN profiles

## How does it work
- ### It listens on a websocket, waiting for any created token and then checks if it's creator is present in a list
- ### If it is present, it checks if the creator is in "favorites" and alerts based on that principal
- ### Alert contains of generated picture of a user profile and links to pump.fun token page as well as bullx token page
![alert example](https://i.postimg.cc/2SqmptmV/image.png)

## Commands
- /add <addr>
- /rm <addr/number in "/list">
- /fav <addr/number in "/list">
- /unfav <addr/number in "/list fav">
- /list
- /list fav
- /stop

## Build
### Maven Package is configured to build a fat JAR

## Libraries used
### okhttp3, [TelegramBots](https://github.com/rubenlagus/TelegramBots), fasterxml
