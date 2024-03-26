# SolBot (Eclipse Java project)

A simple Discord bot using [Discord4J](https://github.com/Discord4J/Discord4J) and named pipes (`mkfifo`) to send commands to the worldserver stdin. It is more or less a proof of concept, but works quite well.

In order to use the bot it needs the following permissions:

- Set message content intent on
- Add the permissions `VIEW_CHANNEL` and `SEND_MESSAGES` to the invite link:<br>
  `https://discord.com/oauth2/authorize?client_id=<CLIENT_ID>&scope=bot&permissions=3072`
