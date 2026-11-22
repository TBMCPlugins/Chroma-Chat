package buttondevteam.chat;

import buttondevteam.chat.components.flair.FlairStates;
import buttondevteam.lib.architecture.config.IConfigData;
import buttondevteam.lib.architecture.config.IListConfigData;
import buttondevteam.lib.chat.Color;
import buttondevteam.lib.player.PlayerClass;
import buttondevteam.lib.player.TBMCPlayerBase;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collections;

@PlayerClass(pluginname = "Chroma-Chat")
public class ChatPlayer extends TBMCPlayerBase {
	public final IConfigData<String> UserName = getConfig().getData("UserName", "");

	public final IListConfigData<String> UserNames = getConfig().getListData("UserNames", Collections.emptyList());

	public final IConfigData<Integer> FlairTime = getConfig().getData("FlairTime", FlairTimeNone);

	public final IConfigData<FlairStates> FlairState = getConfig().getData("FlairState", FlairStates.NoComment,
		fs -> FlairStates.valueOf((String) fs), FlairStates::toString);

	public final IConfigData<Integer> FCount = getConfig().getData("FCount", 0);

	public final IConfigData<Integer> FDeaths = getConfig().getData("FDeaths", 0);

	public final IConfigData<Boolean> FlairCheater = getConfig().getData("FlairCheater", false);

	public final IListConfigData<Integer> NameColorLocations = getConfig().getListData("NameColorLocations", Collections.emptyList()); // No byte[], no TIntArrayList

	public boolean Working;
	// public int Tables = 10;
	public boolean RainbowPresserColorMode = false;
	public Color OtherColorMode = null;
	public boolean ChatOnly = false;
	public long LastMessageTime = 0L;

	public static final int FlairTimeNonPresser = -1;
	public static final int FlairTimeCantPress = -2;
	public static final int FlairTimeNone = -3;

	/**
	 * Gets the player's flair, optionally formatting for Minecraft.
	 *
	 * @param noformats The MC formatting codes will be only applied if false
	 * @return The flair
	 */
	public String GetFormattedFlair(boolean noformats) {
		int time = FlairTime.get();
		if (time == FlairTimeCantPress)
			return noformats ? "(can't press)" : "§r(--s)§r";
		if (time == FlairTimeNonPresser)
			return noformats ? "(non-presser)" : "§7(--s)§r";
		if (time == FlairTimeNone)
			return "";
		return noformats ? String.format("(%ds)", time) : String.format("§%x(%ds)§r", GetFlairColor(), time);
	}

	/**
	 * Gets the player's flair, formatted for Minecraft.
	 *
	 * @return The flair
	 */
	public String GetFormattedFlair() {
		return GetFormattedFlair(false);
	}

	public void SetFlair(int time) {
		FlairTime.set(time);
		FlairUpdate();
	}

	public void SetFlair(int time, boolean cheater) {
		FlairTime.set(time);
		FlairCheater.set(cheater);
		FlairUpdate();
	}

	public void FlairUpdate() {

		// Flairs from Command Block The Button - Teams
		// PluginMain.Instance.getServer().getScoreboardManager().getMainScoreboard().getTeams().add()
		Player p = Bukkit.getPlayer(getUniqueId());
		if (p != null)
			p.setPlayerListName(String.format("%s%s", p.getDisplayName(), GetFormattedFlair()));
	}

	public short GetFlairColor() {
		if (FlairCheater.get())
			return 0x5;
		final int flairTime = FlairTime.get();
		if (flairTime == FlairTimeNonPresser)
			return 0x7;
		else if (flairTime == FlairTimeCantPress)
			return 0xf;
		else if (flairTime <= 60 && flairTime >= 52)
			return 0x5;
		else if (flairTime <= 51 && flairTime >= 42)
			return 0x9;
		else if (flairTime <= 41 && flairTime >= 32)
			return 0xa;
		else if (flairTime <= 31 && flairTime >= 22)
			return 0xe;
		else if (flairTime <= 21 && flairTime >= 11)
			return 0x6;
		else if (flairTime <= 11 && flairTime >= 0)
			return 0xc;
		return 0x00; //Return 0 if none or too high, so names will get aqua default color, not white
	}

	public double getF() {
		return (double) FCount.get() / (double) FDeaths.get();
	}
}
