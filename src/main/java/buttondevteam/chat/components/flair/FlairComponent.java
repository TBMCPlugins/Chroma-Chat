package buttondevteam.chat.components.flair;

import buttondevteam.chat.ChatPlayer;
import buttondevteam.chat.PluginMain;
import buttondevteam.lib.TBMCCoreAPI;
import buttondevteam.lib.architecture.Component;
import buttondevteam.lib.architecture.ConfigData;
import buttondevteam.lib.player.TBMCPlayerBase;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.*;

public class FlairComponent extends Component<PluginMain> {
	private ConfigData<String> FlairThreadURL() {
		return getConfig().getData("flairThreadURL", "https://www.reddit.com/r/Chromagamers/comments/51ys94/flair_thread_for_the_mc_server/");
	}

	/**
	 * <p>
	 * This variable is used as a cache for flair state checking when reading the flair thread.
	 * </p>
	 * <p>
	 * It's used because normally it has to load all associated player files every time to read the flair state
	 * </p>
	 */
	private Set<String> PlayersWithFlairs = new HashSet<>();

	@Override
	protected void enable() {
		registerCommand(new AcceptCommand());
		registerCommand(new IgnoreCommand());
		registerCommand(new SetFlairCommand());
		new Thread(this::FlairGetterThreadMethod).start();
	}

	@Override
	protected void disable() {

	}

	private void FlairGetterThreadMethod() {
		int errorcount = 0;
		while (isEnabled()) {
			try {
				String body = TBMCCoreAPI.DownloadString(FlairThreadURL().get() + ".json?limit=1000");
				JsonArray json = new JsonParser().parse(body).getAsJsonArray().get(1).getAsJsonObject().get("data")
					.getAsJsonObject().get("children").getAsJsonArray();
				for (Object obj : json) {
					JsonObject item = (JsonObject) obj;
					String author = item.get("data").getAsJsonObject().get("author").getAsString();
					String ign = item.get("data").getAsJsonObject().get("body").getAsString();
					int start = ign.indexOf("IGN:") + "IGN:".length();
					if (start == -1 + "IGN:".length())
						continue;
					int end = ign.indexOf(' ', start);
					if (end == -1 || end == start)
						end = ign.indexOf('\n', start);
					if (end == -1 || end == start)
						ign = ign.substring(start);
					else
						ign = ign.substring(start, end);
					ign = ign.trim();
					if (PlayersWithFlairs.contains(ign))
						continue;
					try (ChatPlayer mp = TBMCPlayerBase.getFromName(ign, ChatPlayer.class)) { // Loads player file
						if (mp == null)
							continue;
						/*
						 * if (!JoinedBefore(mp, 2015, 6, 5)) continue;
						 */
						if (!mp.UserNames().contains(author))
							mp.UserNames().add(author);
						if (mp.FlairState().get().equals(FlairStates.NoComment)) {
							mp.FlairState().set(FlairStates.Commented);
							ConfirmUserMessage(mp);
						}
						PlayersWithFlairs.add(ign); // Don't redownload even if flair isn't accepted
					}
				}
			} catch (Exception e) {
				errorcount++;
				if (errorcount >= 10) {
					errorcount = 0;
					if (!e.getMessage().contains("Server returned HTTP response code")
						&& !(e instanceof UnknownHostException))
						TBMCCoreAPI.SendException("Error while getting flairs from Reddit!", e);
				}
			}
			try {
				Thread.sleep(10000);
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
		}
	}

	public static void DownloadFlair(ChatPlayer mp) throws IOException {
		String[] flairdata = TBMCCoreAPI
			.DownloadString("http://karmadecay.com/thebutton-data.php?users=" + mp.UserName().get())
			.replace("\"", "").split(":");
		String flair;
		if (flairdata.length > 1)
			flair = flairdata[1];
		else
			flair = "";
		String flairclass;
		if (flairdata.length > 2)
			flairclass = flairdata[2];
		else
			flairclass = "unknown";
		SetFlair(mp, flair, flairclass, mp.UserName().get());
	}

	private static void SetFlair(ChatPlayer p, String text, String flairclass, String username) {
		p.UserName().set(username);
		p.FlairState().set(FlairStates.Recognised);
		switch (flairclass) {
			case "cheater":
				p.SetFlair(Short.parseShort(text), true);
				return;
			case "unknown":
				try {
					if (CheckForJoinDate(p)) {
						if (text.equals("-1")) // If true, only non-presser/can't press; if false, any flair (but we can still detect can't press)
							p.SetFlair(ChatPlayer.FlairTimeNonPresser);
						else
							p.SetFlair(ChatPlayer.FlairTimeNone); // Flair unknown
					} else {
						p.SetFlair(ChatPlayer.FlairTimeCantPress);
					}
				} catch (Exception e) {
					p.FlairState().set(FlairStates.Commented); // Flair unknown
					p.SetFlair(ChatPlayer.FlairTimeNone);
					TBMCCoreAPI.SendException("Error while checking join date for player " + p.PlayerName() + "!", e);
				}
				return;
			default:
				break;
		}
		p.SetFlair(Short.parseShort(text));
	}

	private static boolean CheckForJoinDate(ChatPlayer mp) throws Exception {
		return JoinedBefore(mp, 2015, 4, 1);
	}

	private static boolean JoinedBefore(ChatPlayer mp, int year, int month, int day) throws Exception {
		URL url = new URL("https://www.reddit.com/u/" + mp.UserName());
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
			.build().getTime());
	}

	public static void ConfirmUserMessage(ChatPlayer mp) {
		Player p = Bukkit.getPlayer(mp.getUUID());
		if (mp.FlairState().get().equals(FlairStates.Commented) && p != null)
			if (mp.UserNames().size() > 1)
				p.sendMessage(
					"§9Multiple Reddit users commented your name. You can select with /u accept.§r §6Type /u accept or /u ignore§r");
			else
				p.sendMessage("§9A Reddit user commented your name. Is that you?§r §6Type /u accept or /u ignore§r");
	}
}
