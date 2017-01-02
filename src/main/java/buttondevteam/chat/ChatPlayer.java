package buttondevteam.chat;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import buttondevteam.lib.chat.*;
import buttondevteam.lib.player.TBMCPlayer;

public class ChatPlayer extends TBMCPlayer {
	public String getUserName() {
		return getData();
	}

	public void setUserName(String name) {
		setData(name);
	}

	public List<String> getUserNames() {
		List<String> data = getData();
		if (data == null)
			setUserNames(data = new ArrayList<String>());
		return data;
	}

	public void setUserNames(List<String> names) {
		setData(names);
	}

	public short getFlairTime() {
		return getIntData(Short.class).orElse(FlairTimeNone);
	}

	private void setFlairTime(short time) {
		setIntData(time);
	}

	public FlairStates getFlairState() {
		FlairStates data = getEnumData(FlairStates.class);
		if (data == null)
			setFlairState(data = FlairStates.NoComment);
		return data;
	}

	public void setFlairState(FlairStates state) {
		setEnumData(state);
	}

	public int getFCount() {
		return getIntData(Integer.class).orElse(0);
	}

	public void setFCount(int count) {
		setIntData(count);
	}

	public int getFDeaths() {
		return getIntData(Integer.class).orElse(0);
	}

	public void setFDeaths(int count) {
		setIntData(count);
	}

	public boolean getFlairCheater() {
		return getBoolData();
	}

	private void setFlairCheater(boolean cheater) {
		setData(cheater);
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

	public static final short FlairTimeNonPresser = -1;
	public static final short FlairTimeCantPress = -2;
	public static final short FlairTimeNone = -3;

	/**
	 * Gets the player's flair, optionally formatting for Minecraft.
	 * 
	 * @param noformats
	 *            The MC formatting codes will be only applied if false
	 * @return The flair
	 */
	public String GetFormattedFlair(boolean noformats) {
		if (getFlairTime() == FlairTimeCantPress)
			return String.format(noformats ? "(can't press)" : "§r(--s)§r");
		if (getFlairTime() == FlairTimeNonPresser)
			return String.format(noformats ? "(non-presser)" : "§7(--s)§r");
		if (getFlairTime() == FlairTimeNone)
			return "";
		return noformats ? String.format("(%ss)", getFlairTime())
				: String.format("§%x(%ss)§r", GetFlairColor(), getFlairTime());
	}

	/**
	 * Gets the player's flair, formatted for Minecraft.
	 * 
	 * @return The flair
	 */
	public String GetFormattedFlair() {
		return GetFormattedFlair(false);
	}

	public void SetFlair(short time) {
		setFlairTime(time);
		FlairUpdate();
	}

	public void SetFlair(short time, boolean cheater) {
		setFlairTime(time);
		setFlairCheater(cheater);
		FlairUpdate();
	}

	public void FlairUpdate() {

		// Flairs from Command Block The Button - Teams
		// PluginMain.Instance.getServer().getScoreboardManager().getMainScoreboard().getTeams().add()
		Player p = Bukkit.getPlayer(getUuid());
		if (p != null)
			p.setPlayerListName(String.format("%s%s", p.getName(), GetFormattedFlair()));
	}

	public short GetFlairColor() {
		if (getFlairCheater())
			return 0x5;
		if (getFlairTime() == -1)
			return 0x7;
		else if (getFlairTime() == -2)
			return 0xf;
		else if (getFlairTime() <= 60 && getFlairTime() >= 52)
			return 0x5;
		else if (getFlairTime() <= 51 && getFlairTime() >= 42)
			return 0x9;
		else if (getFlairTime() <= 41 && getFlairTime() >= 32)
			return 0xa;
		else if (getFlairTime() <= 31 && getFlairTime() >= 22)
			return 0xe;
		else if (getFlairTime() <= 21 && getFlairTime() >= 11)
			return 0x6;
		else if (getFlairTime() <= 11 && getFlairTime() >= 0)
			return 0xc;
		return 0xf;
	}
}
