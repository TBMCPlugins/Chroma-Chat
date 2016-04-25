package io.github.norbipeti.thebuttonmc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class MaybeOfflinePlayer {
	public String PlayerName;
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
	public String OtherColorMode = "";
	public boolean ChatOnly = false;
	public boolean FlairCheater = false;
	public static final short FlairTimeNonPresser = -1;
	public static final short FlairTimeCantPress = -2;
	public static final short FlairTimeNone = -3;

	public UUID UUID;

	public static HashMap<UUID, MaybeOfflinePlayer> AllPlayers = new HashMap<>();

	public static MaybeOfflinePlayer AddPlayerIfNeeded(UUID uuid) {
		if (!AllPlayers.containsKey(uuid)) {
			MaybeOfflinePlayer player = new MaybeOfflinePlayer();
			player.UUID = uuid;
			player.FlairTime = 0;
			player.FlairState = FlairStates.NoComment;
			player.UserNames = new ArrayList<>();
			AllPlayers.put(uuid, player);
			return player;
		}
		return AllPlayers.get(uuid);
	}

	public static void Load(YamlConfiguration yc) {
		ConfigurationSection cs = yc.getConfigurationSection("players");
		for (String key : cs.getKeys(false)) {
			ConfigurationSection cs2 = cs.getConfigurationSection(key);
			MaybeOfflinePlayer mp = AddPlayerIfNeeded(java.util.UUID
					.fromString(cs2.getString("uuid")));
			mp.UserName = cs2.getString("username");
			String tmp = cs2.getString("flairtime");
			if (tmp.equals("--"))
				mp.FlairTime = FlairTimeNonPresser;
			else if (tmp.equals("??"))
				mp.FlairTime = FlairTimeCantPress;
			else if (tmp.length() > 0)
				mp.FlairTime = Short.parseShort(tmp);
			String flairstate = cs2.getString("flairstate");
			if (flairstate != null)
				mp.FlairState = FlairStates.valueOf(flairstate);
			else
				mp.FlairState = FlairStates.NoComment;
			mp.PlayerName = cs2.getString("playername");
			mp.UserNames = cs2.getStringList("usernames");
			mp.FCount = cs2.getInt("fcount");
			mp.FDeaths = cs2.getInt("fdeaths");
			mp.FlairCheater = cs2.getBoolean("flaircheater");
		}
	}

	public static void Save(YamlConfiguration yc) {
		ConfigurationSection cs = yc.createSection("players");
		for (MaybeOfflinePlayer mp : MaybeOfflinePlayer.AllPlayers.values()) {
			ConfigurationSection cs2 = cs.createSection(mp.UUID.toString());
			cs2.set("playername", mp.PlayerName);
			cs2.set("username", mp.UserName);
			cs2.set("flairtime", mp.FlairTime);
			cs2.set("flairstate", mp.FlairState.toString());
			cs2.set("uuid", mp.UUID.toString());
			cs2.set("usernames", mp.UserNames);
			cs2.set("fcount", mp.FCount);
			cs2.set("fdeaths", mp.FDeaths);
			cs2.set("flaircheater", mp.FlairCheater);
		}
	}

	public static MaybeOfflinePlayer GetFromName(String name) {
		Player p = Bukkit.getPlayer(name);
		if (p != null)
			return AllPlayers.get(p.getUniqueId());
		else
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
		p.setPlayerListName(String.format("%s%s", p.getName(),
				GetFormattedFlair()));
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

	public static MaybeOfflinePlayer GetFromPlayer(Player p) {
		return MaybeOfflinePlayer.AllPlayers.get(p.getUniqueId());
	}
}
