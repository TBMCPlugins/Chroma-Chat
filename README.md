# The Button Minecraft server plugin
It can download player flairs for /r/TheButtonMinecraft and do other cool things, like name mentioning.

## How to use
### Players
#### Obtaining the flair
At first, you need to "connect" your Reddit account with your Minecraft account. This is done by writing your Minecraft name to [this thread](https://www.reddit.com/r/TheButtonMinecraft/comments/3d25do/autoflair_system_comment_your_minecraft_name_and/), following the instructions in the post.

When you're done, connect to the server, if you aren't already on. You should see a message asking if you're the correct Reddit user. Type /u accept to confirm it and accept your flair.

#### Hiding/ignoring the flair
If you decide to not show your flair, or someone else tried to use your name, the easiest thing to do is /u ignore. This command works even if you already accepted your flair.

#### Flair not showing up
Please note that in some cases your flair cannot be obtained (specifically, if it's not stored by karmadecay.com, in which case possibly Karmancer can't show it as well). In this case, there are two possibilities.

If you're a non-presser or a can't press and only your time is recorded, you can do /u nonpresser or /u cantpress, as a message should tell you.

If nothing is known about your flair, you need to ask an admin (currently FigyTuna, Ghostise) or me (NorbiPeti) to set the flair for you. This is done to prevent abusing the system with setting random flairs and rendering the core of the plugin useless.

#### Name mentioning
If you simply say any online player's full playername, it'll highlight it and play a sound for the target player. This works only once per message per target player.

Essentials nicknames are now supported. If you say a nickname, it'll show it's original colors, if you say a username, then it will choose based on flair color if known.

### Admins
Type /u admin for a list of the commands.
#### Seeing status of flairs (/u admin playerinfo)
You can check someone's flair status in case something goes wrong.

It outputs the player name (useful if something goes *really* wrong), the player's current ingame flair, the Reddit username, and some other things:

* Flair accepted: True if the player accepted the flair with /u accept in the past
* Flair ignored: True if the player ignored the flair with /u ignore in the past
* Flair decided: False if we don't know if the user is a non-presser or a can't press
* Flair recognised: False if we don't know anything about the flair (i.e. it could be a presser too) and the flair isn't set by any manual way too
* Commented on Reddit: True if the player commented their name on that specific thread with the correct format or a custom flair has been set

#### Reloading the plugin (/u admin reload)
This is useful if you want to change a file related to the plugin or the plugin bugs out (which shouldn't happen).

Be careful and make sure you do /u admin save before you reload the plugin. You need to confirm your action (/u admin confirm) to make sure no setting is lost.

If you want to edit a file, you need to do /u admin save, then edit the file you want, then do /u admin reload.

#### Getting the last error (/u admin getlasterror)
This command's sole purpose is to give me (or any admins, if they want) some information about the errors and if they even happened. It's not fully tested, so a full stack trace might be needed if the plugin breaks.

#### Setting the flair by hand (/u admin setflair)
This allows you to set any flair you want to any player. This will override the automatic flairs, though it's not recommended to do so.

Note that you need to specify the full flair with color codes, for example:

/u admin setflair Player &7(--s)&r

#### Updating the plugin (/u admin updateplugin)
I've made a simple command to allow updating the plugin easily. After running this command, the server needs to get restarted for the changes to take effect.

This command will not do any other thing than downloading the JAR file from here to the plugins directory. Do not spam it, because it will then generate unnecessary network traffic on the server.

#### Setting the sound played on name mentioning
You can set the sound played by creating a file named notificationsound.txt and placing a line (*with an endline at the end*) in this format:

<sound name> <pitch>

Example:

mob.pig.say 1.0
