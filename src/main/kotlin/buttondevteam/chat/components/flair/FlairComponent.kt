package buttondevteam.chat.components.flair

import buttondevteam.chat.ChatPlayer
import buttondevteam.chat.PluginMain
import buttondevteam.lib.TBMCCoreAPI.DownloadString
import buttondevteam.lib.TBMCCoreAPI.SendException
import buttondevteam.lib.architecture.Component
import buttondevteam.lib.architecture.ComponentMetadata
import buttondevteam.lib.player.TBMCPlayerBase.Companion.getFromName
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.bukkit.Bukkit
import java.io.IOException
import java.net.UnknownHostException

/**
 * This component checks a specific Reddit thread every 10 seconds for comments such as "IGN: NorbiPeti" to link Reddit accounts and to determine their /r/thebutton flair.
 * This was the original goal of this plugin when it was made.
 */
@ComponentMetadata(enabledByDefault = false)
class FlairComponent : Component<PluginMain>() {
    /**
     * The Reddit thread to check for account connections. Re-enable the component if this was empty.
     */
    var flairThreadURL: IConfigData<String?> = config.getData<String?>("flairThreadURL", "")

    /**
     *
     *
     * This variable is used as a cache for flair state checking when reading the flair thread.
     *
     *
     *
     * It's used because normally it has to load all associated player files every time to read the flair state
     *
     */
    private val PlayersWithFlairs: MutableSet<String?> = HashSet<String?>()

    override fun enable() {
        registerCommand(AcceptCommand(this))
        registerCommand(IgnoreCommand())
        registerCommand(SetFlairCommand())
        Thread(Runnable { this.FlairGetterThreadMethod() }).start()
    }

    override fun disable() {
    }

    private fun FlairGetterThreadMethod() {
        var errorcount = 0
        while (isEnabled && flairThreadURL.get().length > 0) {
            try {
                val body = DownloadString(flairThreadURL.get() + ".json?limit=1000")
                val json = JsonParser().parse(body).getAsJsonArray().get(1).getAsJsonObject().get("data")
                    .getAsJsonObject().get("children").getAsJsonArray()
                for (obj in json) {
                    val item = obj as JsonObject
                    val author = item.get("data").getAsJsonObject().get("author").getAsString()
                    var ign = item.get("data").getAsJsonObject().get("body").getAsString()
                    val start = ign.indexOf("IGN:") + "IGN:".length
                    if (start == -1 + "IGN:".length) continue
                    var end = ign.indexOf(' ', start)
                    if (end == -1 || end == start) end = ign.indexOf('\n', start)
                    if (end == -1 || end == start) ign = ign.substring(start)
                    else ign = ign.substring(start, end)
                    ign = ign.trim { it <= ' ' }
                    if (PlayersWithFlairs.contains(ign)) continue
                    val mp = getFromName<ChatPlayer>(ign, ChatPlayer::class.java) // Loads player file
                    if (mp == null) continue
                    /*
					 * if (!JoinedBefore(mp, 2015, 6, 5)) continue;
					 */
                    if (!mp.UserNames.get().contains(author)) mp.UserNames.get().add(author)
                    if (mp.FlairState.get() == FlairStates.NoComment) {
                        mp.FlairState.set(FlairStates.Commented)
                        ConfirmUserMessage(mp)
                    }
                    PlayersWithFlairs.add(ign) // Don't redownload even if flair isn't accepted
                }
            } catch (e: Exception) {
                errorcount++
                if (errorcount >= 10) {
                    errorcount = 0
                    if (!e.message!!.contains("Server returned HTTP response code")
                        && e !is UnknownHostException
                    ) SendException("Error while getting flairs from Reddit!", e, this)
                }
            }
            try {
                Thread.sleep(10000)
            } catch (ex: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }
    }

    @Throws(IOException::class)
    fun DownloadFlair(mp: ChatPlayer) {
        val flairdata = DownloadString("http://karmadecay.com/thebutton-data.php?users=" + mp.UserName.get())
            .replace("\"", "").split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val flair: String
        if (flairdata.size > 1) flair = flairdata[1]
        else flair = ""
        val flairclass: String
        if (flairdata.size > 2) flairclass = flairdata[2]
        else flairclass = "unknown"
        SetFlair(mp, flair, flairclass, mp.UserName.get())
    }

    private fun SetFlair(p: ChatPlayer, text: String, flairclass: String, username: String?) {
        p.UserName.set(username)
        p.FlairState.set(FlairStates.Recognised)
        when (flairclass) {
            "cheater" -> {
                p.SetFlair(text.toShort().toInt(), true)
                return
            }

            "unknown" -> {
                try {
                    if (CheckForJoinDate(p)) {
                        if (text == "-1")  // If true, only non-presser/can't press; if false, any flair (but we can still detect can't press)
                            p.SetFlair(ChatPlayer.Companion.FlairTimeNonPresser)
                        else p.SetFlair(ChatPlayer.Companion.FlairTimeNone) // Flair unknown
                    } else {
                        p.SetFlair(ChatPlayer.Companion.FlairTimeCantPress)
                    }
                } catch (e: Exception) {
                    p.FlairState.set(FlairStates.Commented) // Flair unknown
                    p.SetFlair(ChatPlayer.Companion.FlairTimeNone)
                    SendException("Error while checking join date for player " + p.playerName + "!", e, this)
                }
                return
            }

            else -> {}
        }
        p.SetFlair(text.toShort().toInt())
    }

    companion object {
        @Throws(Exception::class)
        private fun CheckForJoinDate(mp: ChatPlayer): Boolean {
            return JoinedBefore(mp, 2015, 4, 1)
        }

        @Throws(Exception::class)
        private fun JoinedBefore(mp: ChatPlayer, year: Int, month: Int, day: Int): Boolean {
            /*URL url = new URL("https://www.reddit.com/u/" + mp.UserName());
		URLConnection con = url.openConnection();
		con.setRequestProperty("User-Agent", "TheButtonAutoFlair");
		InputStream in = con.getInputStream();
		HtmlCleaner cleaner = new HtmlCleaner();
		TagNode node = cleaner.clean(in);

		node = node.getElementsByAttValue("class", "age", true, true)[0];
		node = node.getElementsByName("time", false)[0];
		String joindate = node.getAttributeByName("datetime");
		SimpleDateFormat parserSDF = new SimpleDateFormat("yyyy-MM-dd");
		joindate = joindate.split("T")[0];
		Date date = parserSDF.parse(joindate);
		return date.before(new Calendar.Builder().setTimeZone(TimeZone.getTimeZone("UTC")).setDate(year, month, day)
			.build().getTime());*/
            return true
        }

        fun ConfirmUserMessage(mp: ChatPlayer) {
            val p = Bukkit.getPlayer(mp.uniqueId)
            if (mp.FlairState.get() == FlairStates.Commented && p != null) if (mp.UserNames.get().size > 1) p.sendMessage(
                "§9Multiple Reddit users commented your name. You can select with /u accept.§r §6Type /u accept or /u ignore§r"
            )
            else p.sendMessage("§9A Reddit user commented your name. Is that you?§r §6Type /u accept or /u ignore§r")
        }
    }
}
