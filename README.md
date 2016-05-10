# The Button Minecraft server plugin
It can download player flairs for /r/TheButtonMinecraft and do other cool things, like name mentioning.

## How to use
### Players
#### Connecting with a non-premium account
You can connect to the server with a non-paid account as long as you confirm your flair. To do this, follow the steps below. The server will let you in if you commented in the thread.

#### Obtaining the flair (/u accept)
At first, you need to "connect" your Reddit account with your Minecraft account. This is done by writing your Minecraft name to [this thread](https://www.reddit.com/r/TheButtonMinecraft/comments/433ptk/autoflair_thread/), following the instructions in the post.

When you're done, connect to the server, if you aren't already on. You should see a message after a while (max. 10 seconds) asking if you're the correct Reddit user. Type /u accept to confirm it and accept your flair.

__Note__: You can comment your ingame name from multiple Reddit accounts. In this case, you'll need to specify the exact username you want to pair with your Minecraft account. For example:

    /u accept NorbiPeti

__Tip__: If an admin set a custom flair for you, you may be able to reset the automatic one by doing /u accept.

#### Hiding the message (/u ignore)
You can use this command to hide the notice showing up after you log in if you don't have a flair accepted.

#### Flair not showing up
Please note that in some cases your flair cannot be obtained (specifically, if it's not stored by karmadecay.com, in which case possibly Karmancer can't show it as well). In this case, there are two possibilities.

If you're a non-presser or a can't press and only your time is recorded, it will automatically decide based on your account creation date.

If nothing is known about your flair, you need to ask an admin to set the flair for you. Please prepare a link to a comment you made on /r/thebutton, if possible.

#### Getting someone's Reddit username (/u name)
You can see a player's username if they have a flair shown.

#### Name mentioning
If you simply say any online player's full playername or nickname, it'll highlight it and play a sound for the target player. This works only once per message per target player.

If you say a nickname, it'll show it's original colors, if you say a username, then it will choose based on flair color if known.

You can also use @console to ping the console. If someone is there, they'll receive an audible bell signal.

#### RP/OOC mode (/nrp or /ooc)
You can use /ooc <message> to say something Out-of-Character. Otherwise everything you speak should be treated as said in RP, except when it is obvious it's not in RP (like talking about the server).

#### Greentext support (>message)
Start your message with '>' to make it green.

#### Hashtags (#BlameFigy)
If you say a hashtag in global chat, it'll highlight it and makes it clickable.

#### Paying respects (F)
If a player dies, sometimes the plugin will tell everyone "Press F to pay respects.". After a few seconds, a message will tell everyone how many people paid their respects.

If you hover over a player's name in chat, you can see how much respect they gained this way, divided by the number of eligible deaths.

#### Copy messages
To copy a message from chat, click the channel identifier (for example: [g] or [TC]) at the beginning of the message.

#### Tableflipping (/tableflip and /unflip)
The idea of this command came from Discord.

Examples:

    /tableflip - (╯°□°）╯︵ ┻━┻
    /tableflip test - test(╯°□°）╯︵ ┻━┻
    /unflip - ┬─┬ ノ( ゜-゜ノ)
    /unflip test - test┬─┬ ノ( ゜-゜ノ)

#### Chat only (/chatonly)
You can use this mode to protect yourself if you connect from a chat-only client. This will make you invincible, but unable to move, teleport or interact with the world in any way.

#### Rainbow chat (/u c)
Rainbow/Presser colors.

### Admins
Type /u admin for a list of the commands.
#### Seeing status of flairs (/u admin playerinfo)
You can check someone's flair status in case something goes wrong.

It outputs the player name (useful if something goes *really* wrong), the player's current ingame flair, the Reddit username, and their flair status:

* Accepted: The user has a flair shown after their name.
* Ignored: The user chose to hide their flair, but they have one.
* Recognised: The user has recognised flair(s) but haven't accepted any.
* Commented: The user commented, but their flair isn't known.
* NoComment: The user haven't commented in the thread.

#### Reloading the plugin (/u admin reload)
This is useful if you want to change a file related to the plugin.

Be careful and make sure you do /u admin save before you reload the plugin. You need to confirm your action (/u admin confirm) to make sure no setting is lost.

If you want to edit a file, you need to do /u admin save, then edit the file you want, then do /u admin reload.

#### Getting the last error (/u admin getlasterror)
This command's sole purpose is to give me (or any admins, if they want) some information about the errors and if they even happened. It's not fully tested, so a full stack trace might be needed if the plugin breaks.

#### Setting the flair by hand (/u admin setflair)
This allows you to set any flair you want to any player. This will override the automatic flairs, though it's not recommended to do so. However, the player can reset the automatic flair at any time (see /u accept).

* To remove a user's flair:

    /u admin setflair <playername> none false

* To set a non-presser or can't press flair:

    /u admin setflair <playername> non-presser/cant-press false

* To set a cheater flair:

    /u admin setflair <playername> <time> true

* To also set the username for the flair:

    /u admin setflair <playername> <time> <cheater> <username>

Usage:

    /u admin setflair <playername> <time (without s) or non-presser, can't press, none> <cheater (true/false)>> [username]

#### Updating the plugin (/u admin updateplugin)
I've made a simple command to allow updating the plugin easily. After running this command, the server needs to get restarted for the changes to take effect.

This command will not do any other thing than downloading the JAR file from here to the plugins directory. Do not spam it, because it will then generate unnecessary network traffic on the server.

#### Toggle settings

    /u admin togglerpshow - Toggles [RP] tag shown at each message except /ooc or /nrp.
    /u admin toggledebug - Toggles debug mode. Currently only gives a few info used to solve a specific error.

#### Saving and loading a player's position (/u admin savepos|loadpos)
This is used in my "Invisible Parkour" as a checkpoint system, using anywhere else is not recommended.

#### Setting the sound played on name mentioning
You can set the sound played by editing "notificationsound" and "notificationpitch" in the config file (thebuttonmc.yml).

#### Announcements (/u announce)
You can make announcements broadcasted every n minutes where you can set n and it defaults to 15.

    /u announce add <message> - Adds a new announcement. It supports formatting codes with &.
    /u announce remove <index> - Remove announcement by index (see below).
    /u announce list - List announcements with indexes.
    /u announce settime <minutes> - Set the time between announcements in minutes.
    /u announce edit <index> <message> - Directly edits the announcement at the specified index. If there are less announcements than index, it'll create enough announcements.

#### Color modes (/u c)
Use /u c \<colorname\> to set the color of your messages.
