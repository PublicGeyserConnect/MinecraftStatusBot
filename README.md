# MinecraftStatusBot

The is a Discord bot written in Java. Minimum Java version required to run the software is 17.

If you wish to invite the publicly available bot instance, use the following link:

https://discord.com/api/oauth2/authorize?client_id=1125537355734978561&permissions=1084480288832&scope=bot

The following commands are available:

`/ping`
Like just about any other bot, is used to ping a specified server. By default, it'll check for both Java and Bedrock servers on the specified address.

`/server add alias address port`
This is used to add a server to your Discord's saved server list. You're allowed 5 saved servers
- alias: what you want to name the server
- address: the connection address for the server
- port: the port for your server

`/server remove alias`
Used to remove a server from the saved servers list.
- alias: the name used to save the server.

`/server list`
Lists all of the servers that have been saved to the Discord.

`/status enable alias`
Enables the automatically updating embed in the channel the command is executed in. A server must be saved first using "/server add" to use this feature.

`/status disable alias`
Disables the automatically updating embed.

We also support notifying a specific role if a server is offline for 2 consecutive checks.

`/notify enable`
Enables the role notification system.

`/notify disable`
Disables the role notification system.

`/notify role roleid`
Specifies the role to ping in the current channel the command is run in.
