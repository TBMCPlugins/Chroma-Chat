package tk.sznp.thebuttonautoflair;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class MaybeOfflinePlayer { // 2015.08.08.
	public String PlayerName;
	public String UserName;
	public String Flair; // If the user comments their name, it gets set, it
							// doesn't matter if they accepted it or not
	public boolean AcceptedFlair;
	public boolean IgnoredFlair;
	public boolean FlairDecided; // 2015.08.09. //TODO: Detect date
	public boolean FlairRecognised; // 2015.08.10.
	public boolean CommentedOnReddit; // 2015.08.10.
	public boolean RPMode; // 2015.08.25.
	public boolean PressedF; // 2015.09.18.
	public Location SavedLocation; // 2015.10.02.

	public UUID UUID;

	public static HashMap<UUID, MaybeOfflinePlayer> AllPlayers = new HashMap<>();

	public static MaybeOfflinePlayer AddPlayerIfNeeded(UUID uuid) {
		if (!AllPlayers.containsKey(uuid)) {
			MaybeOfflinePlayer player = new MaybeOfflinePlayer();
			// player.PlayerName = playername;
			player.UUID = uuid;
			player.Flair = ""; // 2015.08.10.
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
			mp.Flair = cs2.getString("flair");
			mp.AcceptedFlair = cs2.getBoolean("acceptedflair");
			mp.IgnoredFlair = cs2.getBoolean("ignoredflair");
			mp.FlairDecided = cs2.getBoolean("flairdecided");
			mp.FlairRecognised = cs2.getBoolean("flairrecognised");
			mp.CommentedOnReddit = cs2.getBoolean("commentedonreddit");
			mp.PlayerName = cs2.getString("playername");
		}
	}

	public static void Save(YamlConfiguration yc) {
		ConfigurationSection cs = yc
				.createSection("players");
		for (MaybeOfflinePlayer mp : MaybeOfflinePlayer.AllPlayers.values()) {
			ConfigurationSection cs2 = cs.createSection(mp.UUID.toString());
			cs2.set("playername", mp.PlayerName);
			cs2.set("username", mp.UserName);
			cs2.set("flair", mp.Flair);
			cs2.set("acceptedflair", mp.AcceptedFlair);
			cs2.set("ignoredflair", mp.IgnoredFlair);
			cs2.set("flairdecided", mp.FlairDecided);
			cs2.set("flairrecognised", mp.FlairRecognised);
			cs2.set("commentedonreddit", mp.CommentedOnReddit);
			cs2.set("uuid", mp.UUID.toString());
		}
	}
	
	public static MaybeOfflinePlayer GetFromName(String name)
	{
		for(MaybeOfflinePlayer mp : AllPlayers.values())
			if(mp.PlayerName.equalsIgnoreCase(name))
				return mp;
		return null;
	}
}
