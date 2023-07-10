# MinecraftStatusBot

The is a Discord bot written in Java. The purpose of this bot is to provide a means within Discord to allow visibility of a Minecraft server status to users. 

This bot supports the following:
- Pinging any address for server information.
- Saving servers for quick reference.
- Specifying a custom image for the server imbeds.
- Displays a regularly updated message embed with the user selected server info.
- Displays a regularly updated message embed with the list of players on a specified server (if supported by server).
- Can notify a specified role to alert of a server being offline.

If you wish to invite the publicly available bot instance, use the following link:

https://discord.com/api/oauth2/authorize?client_id=1125537355734978561&permissions=1084480288832&scope=bot

There is also a simple support Discord: 

https://discord.gg/rFWgQxcV

The following commands are available:

`/ping`
Like just about any other bot, is used to ping a specified server. By default, it'll check for both Java and Bedrock servers on the specified address.

`/server add alias address port`
This is used to add a server to your Discord's saved server list. You're allowed 5 saved servers
- alias: what you want to name the server
- address: the connection address for the server
- port: the port for your server

`/server image alias url`
This allows an image URL to be specified for the server.

`/server remove alias`
Used to remove a server from the saved servers list.
- alias: the name used to save the server.

`/server list`
Lists all of the servers that have been saved to the Discord.

`/status enable alias`
Enables the automatically updating embed in the channel the command is executed in. A server must be saved first using "/server add" to use this feature.

`/status disable alias`
Disables the automatically updating embed.

`/playerlist enable alias`
Enables an auto-updating embed showing the players on the specified server. This only works on by default on 1.19x or older servers, or 1.20 or newer servers that has the "hide-online-players" set to false. Some servers also use plugins to hide the players list. The bot will return a message if the server doesn't reveal its player list, and will not enable the embed.

`/playerlist disable alias`
Disables the specified embed.

We also support notifying a specific role if a server is offline for 2 consecutive checks.

`/notify enable`
Enables the role notification system.

`/notify disable`
Disables the role notification system.

`/notify role roleid`
Specifies the role to ping in the current channel the command is run in.


Minimum Java version required to run the software is 17. This bot is inspired from the code of [Doze's MCSS bot](https://github.com/Doze42/MCSS). 
