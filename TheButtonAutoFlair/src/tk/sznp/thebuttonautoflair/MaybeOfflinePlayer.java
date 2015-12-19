package tk.sznp.thebuttonautoflair;

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
	private String FlairTime;
	private short FlairColor;
	public FlairStates FlairState;
	public boolean RPMode = true;
	public boolean PressedF;
	public Location SavedLocation;
	public boolean Working;
	public int Tables = 10;

	public UUID UUID;

	public static HashMap<UUID, MaybeOfflinePlayer> AllPlayers = new HashMap<>();

	public static MaybeOfflinePlayer AddPlayerIfNeeded(UUID uuid) {
		if (!AllPlayers.containsKey(uuid)) {
			MaybeOfflinePlayer player = new MaybeOfflinePlayer();
			player.UUID = uuid;
			player.FlairColor = 0;
			player.FlairTime = "";
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
			mp.FlairColor = (short) cs2.getInt("flaircolor");
			mp.FlairTime = cs2.getString("flairtime");
			String flairstate = cs2.getString("flairstate");
			if (flairstate != null)
				mp.FlairState = FlairStates.valueOf(flairstate);
			else
				mp.FlairState = FlairStates.NoComment;
			mp.PlayerName = cs2.getString("playername");
			mp.UserNames = cs2.getStringList("usernames");
		}
	}

	public static void Save(YamlConfiguration yc) {
		ConfigurationSection cs = yc.createSection("players");
		for (MaybeOfflinePlayer mp : MaybeOfflinePlayer.AllPlayers.values()) {
			ConfigurationSection cs2 = cs.createSection(mp.UUID.toString());
			cs2.set("playername", mp.PlayerName);
			cs2.set("username", mp.UserName);
			cs2.set("flaircolor", mp.FlairColor);
			cs2.set("flairtime", mp.FlairTime);
			cs2.set("flairstate", mp.FlairState.toString());
			cs2.set("uuid", mp.UUID.toString());
			cs2.set("usernames", mp.UserNames);
		}
	}

	public static MaybeOfflinePlayer GetFromName(String name) {
		for (MaybeOfflinePlayer mp : AllPlayers.values())
			if (mp.PlayerName.equalsIgnoreCase(name))
				return mp;
		return null;
	}

	public String GetFormattedFlair() {
		if (FlairColor == 0x00)
			return "";
		if (FlairTime == null || FlairTime.length() == 0)
			return String.format("§%x(??s)§r", FlairColor);
		return String.format("§%x(%ss)§r", FlairColor, FlairTime);
	}

	public void SetFlairColor(int color) {
		FlairColor = (short) color;
		SetFlair2();
	}

	public void SetFlairTime(String time) {
		FlairTime = time;
		SetFlair2();
	}

	public void SetFlair(int color, String time) {
		FlairColor = (short) color;
		FlairTime = time;
		SetFlair2();
	}

	private void SetFlair2() {

		// Flairs from Command Block The Button - Teams
		// PluginMain.Instance.getServer().getScoreboardManager().getMainScoreboard().getTeams().add()
		Player p = Bukkit.getPlayer(UUID);
		p.setPlayerListName(String.format("%s%s", p.getPlayerListName(),
				GetFormattedFlair()));
	}

	public short GetFlairColor() {
		return FlairColor;
	}

	public String GetFlairTime() {
		return FlairTime;
	}
}
