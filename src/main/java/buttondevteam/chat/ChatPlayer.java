package buttondevteam.chat;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import buttondevteam.lib.chat.*;
import buttondevteam.lib.player.EnumPlayerData;
import buttondevteam.lib.player.PlayerClass;
import buttondevteam.lib.player.PlayerData;
import buttondevteam.lib.player.TBMCPlayerBase;

@PlayerClass(pluginname = "ButtonChat")
public class ChatPlayer extends TBMCPlayerBase {
	public PlayerData<String> UserName() {
		return data();
	}

	public List<String> UserNames() {
		PlayerData<List<String>> data = data();
		if (data.get() == null)
			data.set(new ArrayList<String>());
		return data.get();
	}

	public PlayerData<Integer> FlairTime() {
		return data();
	}

	public EnumPlayerData<FlairStates> FlairState() {
		return dataEnum(FlairStates.class);
	}

	public PlayerData<Integer> FCount() {
		return data();
	}

	public PlayerData<Integer> FDeaths() {
		return data();
	}

	public PlayerData<Boolean> FlairCheater() {
		return data();
	}

	public boolean RPMode = true;
	public Location SavedLocation;
	public boolean Working;
	// public int Tables = 10;
	public Channel CurrentChannel = Channel.GlobalChat;
	public boolean SendingLink = false;
	public boolean RainbowPresserColorMode = false;
	public Color OtherColorMode = null;
	public boolean ChatOnly = false;
	public int LoginWarningCount = 0;

	public static final int FlairTimeNonPresser = -1;
	public static final int FlairTimeCantPress = -2;
	public static final int FlairTimeNone = -3;

	/**
	 * Gets the player's flair, optionally formatting for Minecraft.
	 * 
	 * @param noformats
	 *            The MC formatting codes will be only applied if false
	 * @return The flair
	 */
	public String GetFormattedFlair(boolean noformats) {
		int time = FlairTime().getOrDefault(FlairTimeNone);
		if (time == FlairTimeCantPress)
			return String.format(noformats ? "(can't press)" : "§r(--s)§r");
		if (time == FlairTimeNonPresser)
			return String.format(noformats ? "(non-presser)" : "§7(--s)§r");
		if (time == FlairTimeNone)
			return "";
		return noformats ? String.format("(%ss)", FlairTime().get())
				: String.format("§%x(%ss)§r", GetFlairColor(), FlairTime().get());
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
		FlairTime().set(time);
		FlairUpdate();
	}

	public void SetFlair(int time, boolean cheater) {
		FlairTime().set(time);
		FlairCheater().set(cheater);
		FlairUpdate();
	}

	public void FlairUpdate() {

		// Flairs from Command Block The Button - Teams
		// PluginMain.Instance.getServer().getScoreboardManager().getMainScoreboard().getTeams().add()
		Player p = Bukkit.getPlayer(uuid);
		if (p != null)
			p.setPlayerListName(String.format("%s%s", p.getName(), GetFormattedFlair()));
	}

	public short GetFlairColor() {
		if (FlairCheater().get())
			return 0x5;
		if (FlairTime().get() == -1)
			return 0x7;
		else if (FlairTime().get() == -2)
			return 0xf;
		else if (FlairTime().get() <= 60 && FlairTime().get() >= 52)
			return 0x5;
		else if (FlairTime().get() <= 51 && FlairTime().get() >= 42)
			return 0x9;
		else if (FlairTime().get() <= 41 && FlairTime().get() >= 32)
			return 0xa;
		else if (FlairTime().get() <= 31 && FlairTime().get() >= 22)
			return 0xe;
		else if (FlairTime().get() <= 21 && FlairTime().get() >= 11)
			return 0x6;
		else if (FlairTime().get() <= 11 && FlairTime().get() >= 0)
			return 0xc;
		return 0xf;
	}
}
