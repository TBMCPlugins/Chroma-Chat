package buttondevteam.chat;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import buttondevteam.chat.formatting.ChatFormatter;
import buttondevteam.lib.TBMCPlayer;

public class ChatPlayer extends TBMCPlayer {
	public String UserName;
	public List<String> UserNames;
	private short FlairTime;
	public FlairStates FlairState;
	public boolean RPMode = true;
	public boolean PressedF;
	public Location SavedLocation;
	public boolean Working;
	// public int Tables = 10;
	public Channel CurrentChannel = Channel.GlobalChat;
	public int FCount;
	public boolean SendingLink = false;
	public int FDeaths;
	public boolean RainbowPresserColorMode = false;
	public ChatFormatter.Color OtherColorMode = null;
	public boolean ChatOnly = false;
	public boolean FlairCheater = false;
	public int LoginWarningCount = 0;
	public static final short FlairTimeNonPresser = -1;
	public static final short FlairTimeCantPress = -2;
	public static final short FlairTimeNone = -3;

	public static HashMap<UUID, ChatPlayer> OnlinePlayers = new HashMap<>();

	@SuppressWarnings("deprecation")
	public static ChatPlayer GetFromName(String name) {
		OfflinePlayer p = Bukkit.getOfflinePlayer(name);
		if (p != null) {
			if (!ChatPlayer.OnlinePlayers.containsKey(p.getUniqueId())) {
				TBMCPlayer player = TBMCPlayer.LoadPlayer(p);
				if (player == null) {
					Bukkit.getServer().getLogger()
							.warning("Can't load player " + p.getUniqueId() + " - " + p.getName());
					return null;
				}
				return ChatPlayer.OnlinePlayers.get(player.UUID);
			}
			return ChatPlayer.OnlinePlayers.get(p.getUniqueId());
		} else
			return null;
	}

	public String GetFormattedFlair() {
		if (FlairTime == FlairTimeCantPress)
			return String.format("§r(--s)§r");
		if (FlairTime == FlairTimeNonPresser)
			return String.format("§7(--s)§r");
		if (FlairTime == FlairTimeNone)
			return "";
		return String.format("§%x(%ss)§r", GetFlairColor(), FlairTime);
	}

	public void SetFlair(short time) {
		FlairTime = time;
		FlairUpdate();
	}

	public void SetFlair(short time, boolean cheater) {
		FlairTime = time;
		FlairCheater = cheater;
		FlairUpdate();
	}

	public void FlairUpdate() {

		// Flairs from Command Block The Button - Teams
		// PluginMain.Instance.getServer().getScoreboardManager().getMainScoreboard().getTeams().add()
		Player p = Bukkit.getPlayer(UUID);
		if (p != null)
			p.setPlayerListName(String.format("%s%s", p.getName(), GetFormattedFlair()));
	}

	public short GetFlairColor() {
		if (FlairCheater)
			return 0x5;
		if (FlairTime == -1)
			return 0x7;
		else if (FlairTime == -2)
			return 0xf;
		else if (FlairTime <= 60 && FlairTime >= 52)
			return 0x5;
		else if (FlairTime <= 51 && FlairTime >= 42)
			return 0x9;
		else if (FlairTime <= 41 && FlairTime >= 32)
			return 0xa;
		else if (FlairTime <= 31 && FlairTime >= 22)
			return 0xe;
		else if (FlairTime <= 21 && FlairTime >= 11)
			return 0x6;
		else if (FlairTime <= 11 && FlairTime >= 0)
			return 0xc;
		return 0xf;
	}

	public short GetFlairTime() {
		return FlairTime;
	}

	public static ChatPlayer GetFromPlayer(Player p) {
		return ChatPlayer.OnlinePlayers.get(p.getUniqueId());
	}
}
