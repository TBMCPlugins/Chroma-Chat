package buttondevteam.chat.components.flair

import lombok.RequiredArgsConstructor

@CommandClass(
    modOnly = false, helpText = ["Accept flair" //
        , "Accepts a flair from Reddit" //
        , "Use /u accept <username> if you commented from multiple accounts"
    ]
)
@RequiredArgsConstructor
class AcceptCommand : UCommandBase() {
    private val component: FlairComponent? = null

    @Subcommand
    fun def(player: org.bukkit.entity.Player, @OptionalArg username: kotlin.String?): kotlin.Boolean {
        val p: ChatPlayer = TBMCPlayerBase.Companion.getPlayer<ChatPlayer>(player.getUniqueId(), ChatPlayer::class.java)
        if (username == null && p.UserNames.get().size > 1) {
            player.sendMessage("§9Multiple users commented your name. §bPlease pick one using /u accept <username>")
            val sb = java.lang.StringBuilder()
            sb.append("§6Usernames:")
            for (name in p.UserNames.get()) sb.append(" ").append(name)
            player.sendMessage(sb.toString())
            return true
        }
        if (p.FlairState.get() == FlairStates.NoComment || p.UserNames.get().size == 0) {
            player.sendMessage("§cError: You need to write your username to the reddit thread§r")
            player.sendMessage(component!!.flairThreadURL.get())
            return true
        }
        if (username != null && !p.UserNames.get().contains(username)) {
            player.sendMessage("§cError: Unknown name: " + username + "§r")
            return true
        }
        if (p.Working) {
            player.sendMessage("§cError: Something is already in progress.§r")
            return true
        }

        if ((if (username != null) username else p.UserNames.get().get(0)) == p.UserName.get()) {
            player.sendMessage("§cYou already have this user's flair.§r")
            return true
        }
        if (username != null) p.UserName.set(username)
        else p.UserName.set(p.UserNames.get().get(0))

        player.sendMessage("§bObtaining flair...")
        p.Working = true
        val timer = java.util.Timer()
        val tt: PlayerJoinTimerTask = object : PlayerJoinTimerTask() {
            public override fun run() {
                try {
                    component!!.DownloadFlair(mp)
                } catch (e: java.lang.Exception) {
                    SendException(
                        "An error occured while downloading flair for " + player.getCustomName() + "!",
                        e,
                        component
                    )
                    player.sendMessage("Sorry, but an error occured while trying to get your flair. Please contact a mod.")
                    mp.Working = false
                    return
                }

                if (mp.FlairState.get() == FlairStates.Commented) {
                    player.sendMessage("Sorry, but your flair isn't recorded. Please ask an admin to set it for you. Also, prepare a comment on /r/thebutton, if possible.")
                    mp.Working = false
                    return
                }
                val flair: kotlin.String? = mp.GetFormattedFlair()
                mp.FlairState.set(FlairStates.Accepted)
                FlairComponent.Companion.ConfirmUserMessage(mp)
                player.sendMessage("§bYour flair has been set:§r " + flair)
                mp.Working = false
            }
        }
        tt.mp = p
        timer.schedule(tt, 20)
        return true
    }
}
