package buttondevteam.chat;

import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import buttondevteam.chat.formatting.ChatFormatter;
import buttondevteam.lib.TBMCPlayer;

public class ChatPlayer extends TBMCPlayer {
	public String getUserName() {
		return getData();
	}

	public void setUserName(String name) {
		setData(name);
	}

	public List<String> getUserNames() {
		return getData();
	}

	public void setUserNames(List<String> names) {
		setData(names);
	}

	public short getFlairTime() {
		return getData();
	}

	private void setFlairTime(short time) {
		setData(time);
	}

	public FlairStates getFlairState() {
		return getEnumData(FlairStates.class);
	}

	public void setFlairState(FlairStates state) {
		setEnumData(state);
	}

	public int getFCount() {
		return getData();
	}

	public void setFCount(int count) {
		setData(count);
	}

	public int getFDeaths() {
		return getData();
	}

	public void setFDeaths(int count) {
		setData(count);
	}

	public boolean getFlairCheater() {
		return getData();
	}

	private void setFlairCheater(boolean cheater) {
		setData(cheater);
	}

	public boolean RPMode = true;
	public boolean PressedF;
	public Location SavedLocation;
	public boolean Working;
	// public int Tables = 10;
	public Channel CurrentChannel = Channel.GlobalChat;
	public boolean SendingLink = false;
	public boolean RainbowPresserColorMode = false;
	public ChatFormatter.Color OtherColorMode = null;
	public boolean ChatOnly = false;
	public int LoginWarningCount = 0;

	public static final short FlairTimeNonPresser = -1;
	public static final short FlairTimeCantPress = -2;
	public static final short FlairTimeNone = -3;

	public String GetFormattedFlair() {
		if (getFlairTime() == FlairTimeCantPress)
			return String.format("§r(--s)§r");
		if (getFlairTime() == FlairTimeNonPresser)
			return String.format("§7(--s)§r");
		if (getFlairTime() == FlairTimeNone)
			return "";
		return String.format("§%x(%ss)§r", GetFlairColor(), getFlairTime());
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
