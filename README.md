# MinecraftStatusBot

The purpose of this bot is to provide a means within Discord to allow visibility of a Minecraft server status to users. It also has a RCON (Remote CONsole) system built-in that can be used to manage the server.

This bot supports the following features:
- Supporting both Bedrock and Java edition servers.
- Pinging any address for server information. (Automatically checks for both Java and Bedrock on the given address.)
- Saving servers for quick reference.
- Specifying a custom image for the server imbeds.
- Displaying a regularly updated message embed with the user selected server info.
- Displaying a regularly updated message embed with the list of players on a specified server (if supported by server).
- Notifying a specified role to alert of a server being offline.

## How to add to your Discord
If you wish to invite the publicly available bot instance, use the following link:

https://discord.com/api/oauth2/authorize?client_id=1125537355734978561&permissions=1084480288832&scope=bot

## Community and support
We have a community Discord server available at the link:

https://discord.gg/rFWgQxcV

## Configuring and using the bot in your Discord.
The following commands are available:

`/ping`
Like just about any other bot, is used to ping a specified server. By default, it'll check for both Java and Bedrock servers on the specified address.

`/server add <alias> <address> <port>`
This is used to add a server to your Discord's saved server list. You're allowed 5 saved servers. All fields are required. 
- alias: what you want to name the server
- address: the connection address for the server
- port: the port for your server (defaults: Java 25565, Bedrock 19132)


`/server remove <alias>`
Used to remove a server from the saved servers list.
- alias: the name used to save the server.

`/server image <alias> <url>`
This allows an image URL to be specified for the server.

`/server list`
Lists all of the servers that have been saved to the Discord.

`/status enable <alias>`
Enables the automatically updating embed in the channel the command is executed in. A server must be saved first using "/server add" to use this feature.

`/status disable alias`
Disables the automatically updating embed.

`/playerlist enable <alias>`
Enables an auto-updating embed showing the players on the specified server. The bot will return a message if the server doesn't reveal its player list, and will not enable the embed.

`/playerlist disable <alias>`
Disables the specified embed.

`/notify enable`
Enables the role notification system.

`/notify disable`
Disables the role notification system.

`/notify role <roleid>`
Specifies the role to ping in the current channel the command is run in.

## RCON (Remote CONsole)
The RCON system allows remote management of even vanilla servers. The system requires a second TCP port open on your firewall/management interface, and must be configured from within the `server.properties` file. 
### To configure the RCON on the server, locate the following lines (defaults are shown):
```
enable-rcon=false
rcon.port=25575
rcon.password=
```
and set `enable-rcon` to true to enable the feature, set the port to a port you have available (or leave as default), and put in a password of choice.


### The following commands are available from the Discord:

`/rcon set <server name> <rcon port> <channelid>` Use this to configure the server's RCON settings to your Discord. 
- `<server name>` Is the alias the server has been saved as using the `/server add` command.
- `<rcon port>` This is the port that your server is configured to listen on. The default is 25575.
- `<channel id>` This is used to specify the rcon Discord channel of choice. This channel should be role locked only to the users that you want to have access to the command.

`/rcon login <servername> <rcon password>`
This is used to login to the server so that you can remotely access the console. The password is *not* saved to long term storage, and is only remembered for 30 minutes.


## Limitations
- Bedrock servers do not reveal a player list, so the player list feature is not supported on them.
- The player list only works by default on 1.19.x or older servers, or 1.20 or newer servers that have the "hide-online-players" set to false. Some servers also use plugins to hide the players list.
- We wait for two consecutive "server offline" pings before notfying the role that the server is offline to minimize false notifications. Unfortunately, false notifications do still happen.
- BDS servers (vanilla Bedrock server software) are not know to support RCON. Third party options/plugins may or may not exist.
- The RCON system is lmited to 5 seconds between command sends. Currently only Discord users with Administrator permissions can use this feature.

## Feedback
Feature requests and bug reports are welcome. On GitHub, you may open an issue, or start a discussion. You may also join our Discord, and make use of our suggestions and bug report channels.

## Software Info
This is a Discord bot written in Java, and the minimum Java version required to run the software is 17. This bot is inspired from the code of [Doze's MCSS bot](https://github.com/Doze42/MCSS) and borrows some of its core code from the [GeyserMC Discord Bot](https://github.com/GeyserMC/GeyserDiscordBot).
