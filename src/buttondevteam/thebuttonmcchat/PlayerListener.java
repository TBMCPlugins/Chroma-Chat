package buttondevteam.thebuttonmcchat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import com.palmergames.bukkit.towny.Towny;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.help.HelpTopic;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import au.com.mineauz.minigames.MinigamePlayer;
import au.com.mineauz.minigames.Minigames;
import buttondevteam.core.TBMCPlayer;
import buttondevteam.core.TBMCPlayerAddEvent;
import buttondevteam.core.TBMCPlayerLoadEvent;
import buttondevteam.core.TBMCPlayerSaveEvent;
import buttondevteam.thebuttonmcchat.commands.ucmds.KittycannonCommand;

import com.earth2me.essentials.Essentials;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import com.palmergames.bukkit.towny.war.eventwar.War;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;

public class PlayerListener implements Listener {
	public static HashMap<String, UUID> nicknames = new HashMap<>();

	public static boolean Enable = false;

	public static int LoginWarningCountTotal = 5;

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (essentials == null)
			essentials = ((Essentials) Bukkit.getPluginManager().getPlugin("Essentials"));
		final Player p = event.getPlayer();
		ChatPlayer mp = ChatPlayer.GetFromPlayer(p);
		if (!mp.FlairState.equals(FlairStates.NoComment)) {
			PluginMain.ConfirmUserMessage(mp);
			Timer timer = new Timer();
			PlayerJoinTimerTask tt = new PlayerJoinTimerTask() {
				@Override
				public void run() {
					p.setPlayerListName(p.getName() + mp.GetFormattedFlair());
				}
			};
			tt.mp = mp;
			timer.schedule(tt, 1000);
		} else {
			if (mp.GetFlairTime() == 0x00)
				mp.SetFlair(ChatPlayer.FlairTimeNone);
			Timer timer = new Timer();
			PlayerJoinTimerTask tt = new PlayerJoinTimerTask() {
				@Override
				public void run() {
					Player player = Bukkit.getPlayer(mp.PlayerName);
					if (player == null)
						return;

					/*
					 * PlayerProfile pp = ((FastLoginBukkit) FastLoginBukkit
					 * .getPlugin(FastLoginBukkit.class)).getStorage()
					 * .getProfile(player.getName(), true); boolean ispremium =
					 * pp != null && pp.isPremium();
					 */// Login stuff

					if (/*
						 * ispremium &&
						 */// Login stuff
					mp.FlairState.equals(FlairStates.NoComment)) {
						String json = String.format(
								"[\"\",{\"text\":\"If you're from Reddit and you'd like your /r/TheButton flair displayed ingame, write your Minecraft name to \",\"color\":\"aqua\"},{\"text\":\"[this thread].\",\"color\":\"aqua\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"%s\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Click here to go to the Reddit thread\",\"color\":\"aqua\"}]}}}]",
								PluginMain.FlairThreadURL);
						PluginMain.Instance.getServer().dispatchCommand(PluginMain.Console,
								"tellraw " + mp.PlayerName + " " + json);
						json = "[\"\",{\"text\":\"If you aren't from Reddit or don't want the flair, type /u ignore to prevent this message after next login.\",\"color\":\"aqua\"}]";
						PluginMain.Instance.getServer().dispatchCommand(PluginMain.Console,
								"tellraw " + mp.PlayerName + " " + json);
					}
				}
			};
			tt.mp = mp;
			;
			timer.schedule(tt, 15 * 1000);
		}

		/*
		 * final Timer timer = new Timer(); mp.LoginWarningCount = 0;
		 * PlayerJoinTimerTask tt = new PlayerJoinTimerTask() {
		 * 
		 * @Override public void run() { final Player player =
		 * Bukkit.getPlayer(mp.PlayerName); if (player == null) return;
		 * 
		 * PlayerProfile pp = ((FastLoginBukkit) FastLoginBukkit
		 * .getPlugin(FastLoginBukkit.class)).getStorage()
		 * .getProfile(player.getName(), true); boolean ispremium = pp != null
		 * && pp.isPremium(); final MaybeOfflinePlayer mplayer = mp;
		 * 
		 * if (mp.LoginWarningCount < LoginWarningCountTotal) { if
		 * (AuthMe.getInstance().api.isAuthenticated(player)) { // The player
		 * logged in in any way if (!ispremium &&
		 * !mp.FlairState.equals(FlairStates.Accepted) &&
		 * !mp.FlairState.equals(FlairStates.Commented)) { // The player isn't
		 * premium, and doesn't have a // flair String json = String .format(
		 * "[\"\",{\"text\":\"Welcome! If you are a premium Minecraft user, please do /premium and relog, otherwise please verify your /r/thebutton flair to play, \",\"color\":\"gold\"},{\"text\":\"[here].\",\"color\":\"gold\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"%s\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Click here to go to the Reddit thread\",\"color\":\"aqua\"}]}}}]"
		 * , PluginMain.FlairThreadURL);
		 * PluginMain.Instance.getServer().dispatchCommand( PluginMain.Console,
		 * "tellraw " + mp.PlayerName + " " + json); } } } else { if
		 * (AuthMe.getInstance().api.isAuthenticated(player)) { if (!ispremium
		 * && !mplayer.FlairState .equals(FlairStates.Accepted) &&
		 * !mplayer.FlairState .equals(FlairStates.Commented)) {
		 * Bukkit.getScheduler().runTask(PluginMain.Instance, new Runnable() {
		 * 
		 * @Override public void run() { player.kickPlayer(
		 * "Please either use /premium and relog or verify your flair by commenting in the thread and using /u accept."
		 * ); } }); timer.cancel(); } } } mp.LoginWarningCount++; } }; tt.mp =
		 * mp; int timeout = AuthMe.getInstance().getConfig()
		 * .getInt("settings.restrictions.timeout"); timer.schedule(tt, (timeout
		 * / LoginWarningCountTotal) * 1000, (timeout / LoginWarningCountTotal)
		 * * 1000);
		 */// Login stuff

		/* NICKNAME LOGIC */

		UUID id = p.getUniqueId();

		File f = new File("plugins/Essentials/userdata/" + id + ".yml");
		if (f.exists()) {
			YamlConfiguration yc = new YamlConfiguration();
			try {
				yc.load(f);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InvalidConfigurationException e) {
				e.printStackTrace();
			}
			String nickname = yc.getString("nickname");
			if (nickname != null) {
				nicknames.put(nickname, id);

				if (Enable) {
					if (!p.getName().equals("NorbiPeti")) {
						for (Player player : PluginMain.GetPlayers()) {
							if (player.getName().equals("NorbiPeti")) {
								player.chat("Hey, " + nickname + "!");
								break;
							}
						}
					}
				}
			}
		}

		mp.RPMode = true;

		mp.FlairUpdate(); // Update display

		if (mp.ChatOnly || p.getGameMode().equals(GameMode.SPECTATOR)) {
			mp.ChatOnly = false;
			p.setGameMode(GameMode.SURVIVAL);
		}
	}

	@EventHandler
	public void onPlayerLoad(TBMCPlayerLoadEvent e) {
		YamlConfiguration yc = e.GetPlayerConfig();
		ChatPlayer cp = new ChatPlayer();
		cp.UUID = UUID.fromString(yc.getString("uuid"));
		cp.UserName = yc.getString("username");
		String tcp = yc.getString("flairtime");
		if (tcp.equals("--"))
			cp.SetFlair(ChatPlayer.FlairTimeNonPresser);
		else if (tcp.equals("??"))
			cp.SetFlair(ChatPlayer.FlairTimeCantPress);
		else if (tcp.length() > 0)
			cp.SetFlair(Short.parseShort(tcp));
		String flairstate = yc.getString("flairstate");
		if (flairstate != null)
			cp.FlairState = FlairStates.valueOf(flairstate);
		else
			cp.FlairState = FlairStates.NoComment;
		cp.UserNames = yc.getStringList("usernames");
		cp.FCount = yc.getInt("fcount");
		cp.FDeaths = yc.getInt("fdeaths");
		cp.FlairCheater = yc.getBoolean("flaircheater");
		ChatPlayer.OnlinePlayers.put(cp.UUID, cp);
	}

	@EventHandler
	public void onPlayerSave(TBMCPlayerSaveEvent e) {
		YamlConfiguration yc = e.GetPlayerConfig();
		ChatPlayer cp = ChatPlayer.OnlinePlayers.get(e.GetPlayer().UUID);
		yc.set("username", cp.UserName);
		yc.set("flairtime", cp.GetFlairTime());
		yc.set("flairstate", cp.FlairState.toString());
		yc.set("uuid", cp.UUID.toString());
		yc.set("usernames", cp.UserNames);
		yc.set("fcount", cp.FCount);
		yc.set("fdeaths", cp.FDeaths);
		yc.set("flaircheater", cp.FlairCheater);
	}

	@EventHandler
	public void onPlayerAdd(TBMCPlayerAddEvent e) {
		TBMCPlayer p = e.GetPlayer();
		ChatPlayer player = new ChatPlayer();
		player.UUID = p.UUID;
		player.SetFlair(ChatPlayer.FlairTimeNone);
		player.FlairState = FlairStates.NoComment; //TODO: NullPointerException
		player.UserNames = new ArrayList<>(); //TODO: Move some code here from join?
		ChatPlayer.OnlinePlayers.put(p.UUID, player);
	}

	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
		String deletenick = null;
		for (String nickname : nicknames.keySet()) {
			UUID uuid = nicknames.get(nickname);
			if (event.getPlayer().getUniqueId().equals(uuid)) {
				deletenick = nickname;
				break;
			}
		}
		if (deletenick != null)
			nicknames.remove(deletenick);
	}

	public static String NotificationSound;
	public static double NotificationPitch;

	public static boolean ShowRPTag = false;

	public static Essentials essentials = null;
	final static String[] LaughStrings = new String[] { "xd", "lel", "lawl", "kek", "lmao", "hue", "hah" };

	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		if (event.isCancelled())
			return;
		event.setCancelled(ChatProcessing.ProcessChat(event.getPlayer(), event.getMessage()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void PlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		if (event.getMessage().length() < 2)
			return;
		int index = event.getMessage().indexOf(" ");
		ChatPlayer mp = ChatPlayer.OnlinePlayers.get(event.getPlayer().getUniqueId());
		String cmd = "";
		if (index == -1) {
			cmd = event.getMessage().substring(1);
			// System.out.println("cmd: " + cmd);
			if (cmd.equalsIgnoreCase(Channel.GlobalChat.Command)) {
				mp.CurrentChannel = Channel.GlobalChat;
				event.getPlayer().sendMessage("§6You are now talking in: §b" + mp.CurrentChannel.DisplayName);
				event.setCancelled(true);
			} else if (cmd.equalsIgnoreCase(Channel.TownChat.Command)) {
				if (mp.CurrentChannel.equals(Channel.TownChat))
					mp.CurrentChannel = Channel.GlobalChat;
				else
					mp.CurrentChannel = Channel.TownChat;
				event.getPlayer().sendMessage("§6You are now talking in: §b" + mp.CurrentChannel.DisplayName);
				event.setCancelled(true);
			} else if (cmd.equalsIgnoreCase(Channel.NationChat.Command)) {
				if (mp.CurrentChannel.equals(Channel.NationChat))
					mp.CurrentChannel = Channel.GlobalChat;
				else
					mp.CurrentChannel = Channel.NationChat;
				event.getPlayer().sendMessage("§6You are now talking in: §b" + mp.CurrentChannel.DisplayName);
				event.setCancelled(true);
			} else if (cmd.equalsIgnoreCase(Channel.AdminChat.Command)) {
				if (mp.CurrentChannel.equals(Channel.AdminChat))
					mp.CurrentChannel = Channel.GlobalChat;
				else
					mp.CurrentChannel = Channel.AdminChat;
				event.getPlayer().sendMessage("§6You are now talking in: §b" + mp.CurrentChannel.DisplayName);
				event.setCancelled(true);
			} else if (cmd.equalsIgnoreCase(Channel.ModChat.Command)) {
				if (mp.CurrentChannel.equals(Channel.ModChat))
					mp.CurrentChannel = Channel.GlobalChat;
				else
					mp.CurrentChannel = Channel.ModChat;
				event.getPlayer().sendMessage("§6You are now talking in: §b" + mp.CurrentChannel.DisplayName);
				event.setCancelled(true);
			}
		} else {
			cmd = event.getMessage().substring(1, index);
			// System.out.println("cmd: " + cmd);
			if (cmd.equalsIgnoreCase(Channel.GlobalChat.Command)) {
				event.setCancelled(true);
				Channel c = mp.CurrentChannel;
				mp.CurrentChannel = Channel.GlobalChat;
				event.getPlayer().chat(event.getMessage().substring(index + 1));
				mp.CurrentChannel = c;
			} else if (cmd.equalsIgnoreCase(Channel.TownChat.Command)) {
				event.setCancelled(true);
				Channel c = mp.CurrentChannel;
				mp.CurrentChannel = Channel.TownChat;
				event.getPlayer().chat(event.getMessage().substring(index + 1));
				mp.CurrentChannel = c;
			} else if (cmd.equalsIgnoreCase(Channel.NationChat.Command)) {
				event.setCancelled(true);
				Channel c = mp.CurrentChannel;
				mp.CurrentChannel = Channel.NationChat;
				event.getPlayer().chat(event.getMessage().substring(index + 1));
				mp.CurrentChannel = c;
			} else if (cmd.equalsIgnoreCase(Channel.AdminChat.Command)) {
				event.setCancelled(true);
				Channel c = mp.CurrentChannel;
				mp.CurrentChannel = Channel.AdminChat;
				event.getPlayer().chat(event.getMessage().substring(index + 1));
				mp.CurrentChannel = c;
			} else if (cmd.equalsIgnoreCase(Channel.ModChat.Command)) {
				event.setCancelled(true);
				Channel c = mp.CurrentChannel;
				mp.CurrentChannel = Channel.ModChat;
				event.getPlayer().chat(event.getMessage().substring(index + 1));
				mp.CurrentChannel = c;
			} else if (cmd.equalsIgnoreCase("tpahere")) {
				Player player = Bukkit.getPlayer(event.getMessage().substring(index + 1));
				if (player != null)
					player.sendMessage("§b" + event.getPlayer().getDisplayName() + " §bis in this world: "
							+ event.getPlayer().getWorld().getName());
			} else if (cmd.equalsIgnoreCase("minecraft:me")) {
				if (!essentials.getUser(event.getPlayer()).isMuted()) {
					event.setCancelled(true);
					String message = event.getMessage().substring(index + 1);
					for (Player p : PluginMain.GetPlayers())
						p.sendMessage(String.format("* %s %s", event.getPlayer().getDisplayName(), message));
				}
			}
			/*
			 * boolean tping = false; boolean tphering = false; if
			 * (cmd.equalsIgnoreCase("tpa") || cmd.equalsIgnoreCase("call") ||
			 * cmd.equalsIgnoreCase("ecall") || cmd.equalsIgnoreCase("etpa") ||
			 * cmd.equalsIgnoreCase("tpask") || cmd.equalsIgnoreCase("etpask"))
			 * tping = true; if (cmd.equalsIgnoreCase("tpahere") ||
			 * cmd.equalsIgnoreCase("etpahere")) tphering = true;
			 */

			/*
			 * for (HelpTopic ht : PluginMain.Instance.getServer()
			 * .getHelpMap().getHelpTopics()) { if
			 * (ht.getName().equalsIgnoreCase("/tpa")) { tping = true; break; }
			 * }
			 */

			/*
			 * for (HelpTopic ht : PluginMain.Instance.getServer()
			 * .getHelpMap().getHelpTopics()) { if
			 * (ht.getName().equalsIgnoreCase("/tpahere")) tphering = true;
			 * break; }
			 */

			/*
			 * if (tphering) { Player target =
			 * Bukkit.getPlayer(event.getMessage() .substring(index +
			 * 1).split(" ")[0]); if (target != null && BoardColl.get()
			 * .getFactionAt(PS.valueOf(target.getLocation()))
			 * .getId().equalsIgnoreCase("tower")) { event.getPlayer()
			 * .sendMessage(
			 * "§cYou are not allowed to teleport players out from the Tower");
			 * event.setCancelled(true); } }
			 */
			/*
			 * for (String s : Bukkit.getCommandAliases().get("/tpahere")) { if
			 * (cmd.equalsIgnoreCase(s)) { tping = true; break; } }
			 */
			/*
			 * if (tping) { if ( //
			 * MPlayer.get(event.getPlayer()).getFaction().getId
			 * ().equalsIgnoreCase("nomansland")) // { BoardColl .get()
			 * .getFactionAt( PS.valueOf(event.getPlayer().getLocation()))
			 * .getId().equalsIgnoreCase("tower")) {
			 * event.getPlayer().sendMessage(
			 * "§cYou are not allowed to teleport to the Tower");
			 * event.setCancelled(true); } }
			 */
		}
		if (cmd.equalsIgnoreCase("sethome")) {
			TownyUniverse tu = PluginMain.Instance.TU;
			try {
				TownBlock tb = WorldCoord.parseWorldCoord(event.getPlayer()).getTownBlock();
				if (tb.hasTown()) {
					Town town = tb.getTown();
					if (town.hasNation()) {
						Resident res = tu.getResidentMap().get(event.getPlayer().getName());
						if (res != null && res.hasTown()) {
							Town town2 = res.getTown();
							if (town2.hasNation()) {
								if (town.getNation().getEnemies().contains(town2.getNation())) {
									event.getPlayer().sendMessage("§cYou cannot set homes in enemy territory.");
									event.setCancelled(true);
									return;
								}
							}
						}
					}
				}
			} catch (NotRegisteredException e) {
				return;
			}
		} else if (cmd.equalsIgnoreCase("home") || cmd.equalsIgnoreCase("tpa") || cmd.equalsIgnoreCase("tp")) {
			MinigamePlayer mgp = Minigames.plugin.pdata.getMinigamePlayer(event.getPlayer());
			String currentWorld = event.getPlayer().getLocation().getWorld().getName();
			Location currentLocation = event.getPlayer().getLocation();
			TownyUniverse universe = Towny.getPlugin(Towny.class).getTownyUniverse();
			if (mgp.isInMinigame() && mgp.getMinigame().getMechanic().getMechanic().equals("creativeglobal")) {
				mgp.setAllowTeleport(true);
			} else if (TownyUniverse.isWarTime()) {
				War war = universe.getWarEvent();
				if (war.isWarZone(
						new WorldCoord(currentWorld, currentLocation.getBlockX(), currentLocation.getBlockZ()))) {
					event.getPlayer().sendMessage("§cError: You can't teleport out of a war zone!");
					event.setCancelled(true);
				}
			}
		} else if (cmd.toLowerCase().startsWith("un")) {
			for (HelpTopic ht : PluginMain.Instance.getServer().getHelpMap().getHelpTopics()) {
				// event.getPlayer().sendMessage("HT: " + ht.getName());
				if (ht.getName().equalsIgnoreCase("/" + cmd))
					return;
			}
			if (PluginMain.permission.has(event.getPlayer(), "tbmc.admin")) {
				String s = cmd.substring(2);
				Player target = null;
				target = Bukkit.getPlayer(event.getMessage().substring(index + 1));
				if (target == null) {
					event.getPlayer().sendMessage("§cError: Player not found. (/un" + s + " <player>)");
					event.setCancelled(true);
				}
				if (target != null) {
					target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 10 * 20, 5, false, false));
					for (Player pl : PluginMain.GetPlayers())
						pl.sendMessage(
								event.getPlayer().getDisplayName() + " un" + s + "'d " + target.getDisplayName());
					Bukkit.getServer().getConsoleSender().sendMessage(
							event.getPlayer().getDisplayName() + " un" + s + "'d " + target.getDisplayName());
					event.setCancelled(true);
				}
			}
		}
		if (cmd.equalsIgnoreCase("f")) {
			String[] args = event.getMessage().substring(index + 1).split(" ");
			if (args.length > 1) {
				if (args[0].toLowerCase().equals("enemy") && args[1].equalsIgnoreCase("newhaven")) {
					event.setCancelled(true);
					event.getPlayer().sendMessage("§cYou are not allowed to set New Haven as your enemy faction.");
				}
			}
		}
	}

	@EventHandler
	public void onTabComplete(PlayerChatTabCompleteEvent e) {
		String name = e.getLastToken();
		for (String nickname : nicknames.keySet()) {
			String nwithoutformatting = nickname;
			int index;
			while ((index = nwithoutformatting.indexOf("§k")) != -1)
				nwithoutformatting = nwithoutformatting.replace("§k" + nwithoutformatting.charAt(index + 2), ""); // Support
																													// for
																													// one
																													// random
																													// char
			while ((index = nwithoutformatting.indexOf('§')) != -1)
				nwithoutformatting = nwithoutformatting.replace("§" + nwithoutformatting.charAt(index + 1), "");
			if (nwithoutformatting.startsWith(name)
					&& !nwithoutformatting.equals(Bukkit.getPlayer(nicknames.get(nickname)).getName()))
				e.getTabCompletions().add(nwithoutformatting);
		}
	}

	public static boolean DebugMode = false;

	public void SendForDebug(String message) {
		if (DebugMode) {
			for (Player player : PluginMain.GetPlayers()) {
				if (player.getName().equals("NorbiPeti")) {
					player.sendMessage("[DEBUG] " + message);
					break;
				}
			}
		}
	}

	static boolean ActiveF = false;
	static int FCount = 0;
	static ChatPlayer FPlayer = null;
	private Timer Ftimer;
	public static int AlphaDeaths;

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		if (e.getEntity().getName().equals("Alpha_Bacca44"))
			AlphaDeaths++;
		MinigamePlayer mgp = Minigames.plugin.pdata.getMinigamePlayer(e.getEntity());
		if ((mgp != null && !mgp.isInMinigame()) && new Random().nextBoolean()) { // Don't
																					// store
																					// Fs
																					// for
																					// NPCs
			if (Ftimer != null)
				Ftimer.cancel();
			ActiveF = true;
			FCount = 0;
			FPlayer = ChatPlayer.OnlinePlayers.get(e.getEntity().getUniqueId());
			FPlayer.FDeaths++;
			for (Player p : PluginMain.GetPlayers()) {
				ChatPlayer mp = ChatPlayer.OnlinePlayers.get(p.getUniqueId());
				mp.PressedF = false;
				p.sendMessage("§bPress F to pay respects.§r");
			}
			Ftimer = new Timer();
			TimerTask tt = new TimerTask() {
				@Override
				public void run() {
					if (ActiveF) {
						ActiveF = false;
						for (Player p : PluginMain.GetPlayers()) {
							p.sendMessage("§b" + FCount + " " + (FCount == 1 ? "person" : "people")
									+ " paid their respects.§r");
						}
					}
				}
			};
			Ftimer.schedule(tt, 15 * 1000);
		}
	}

	@EventHandler
	public void onPlayerItemPickup(PlayerPickupItemEvent e) {
		// System.out.println("A");
		MinigamePlayer mp = Minigames.plugin.pdata.getMinigamePlayer(e.getPlayer());
		// System.out.println("B");

		/*
		 * if (!e.getPlayer().isOp() && (!mp.isInMinigame() ||
		 * mp.getMinigame().getName(false)
		 * .equalsIgnoreCase(Commands.KittyCannonMinigame))) return;
		 */
		if (!(mp.isInMinigame()
				&& mp.getMinigame().getName(false).equalsIgnoreCase(KittycannonCommand.KittyCannonMinigame)))
			return;
		// System.out.println("C");
		ItemStack item = e.getItem().getItemStack();
		if (!item.getType().equals(Material.SKULL_ITEM) && !item.getType().equals(Material.SKULL))
			return;
		// System.out.println("D");
		SkullMeta meta = (SkullMeta) item.getItemMeta();
		if (!meta.getDisplayName().equals("§rOcelot Head") || !meta.getOwner().equals("MHF_Ocelot"))
			return;
		// System.out.println("E");
		if (meta.getLore() == null || meta.getLore().size() == 0)
			return;
		// System.out.println("F");
		ItemStack hat = e.getPlayer().getInventory().getHelmet();
		if (!(hat != null && (hat.getType().equals(Material.SKULL) || hat.getType().equals(Material.SKULL_ITEM))
				&& ((SkullMeta) hat.getItemMeta()).getDisplayName().equals("§rWolf Head")))
			e.getPlayer().damage(1f * item.getAmount(), Bukkit.getPlayer(meta.getLore().get(0)));
		e.getItem().remove();
		e.setCancelled(true);
	}

	@EventHandler
	@SuppressWarnings("deprecation")
	public void onVotifierEvent(VotifierEvent event) {
		Vote vote = event.getVote();
		System.out.println("Vote: " + vote);
		org.bukkit.OfflinePlayer op = Bukkit.getOfflinePlayer(vote.getUsername());
		Player p = Bukkit.getPlayer(vote.getUsername());
		if (op != null) {
			PluginMain.economy.depositPlayer(op, 50.0);
		}
		if (p != null) {
			p.sendMessage("§bThanks for voting! $50 was added to your account.");
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		// minecraft:tp @a[x=190,y=-80,z=45,dx=5,dy=50,dz=5] 190 1 45
		if (e.getPlayer().getWorld().getName().equals("wilds") && e.getTo().getBlockX() > 100
				&& e.getTo().getBlockX() < 250 && e.getTo().getBlockZ() > 0 && e.getTo().getBlockZ() < 200
				&& e.getTo().getBlockY() < -64) {
			final Player p = e.getPlayer();
			p.setVelocity(new Vector(0, 0, 0));
			p.setFallDistance(0);
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(PluginMain.Instance, new Runnable() {
				public void run() {
					p.setVelocity(new Vector(0, 0, 0));
					p.setFallDistance(0);
					p.teleport(new Location(Bukkit.getWorld("wilds"), 190, 1, 50));
					p.setVelocity(new Vector(0, 0, 0));
					p.setFallDistance(0);
				}
			});
		}

		ChatPlayer mp = ChatPlayer.GetFromPlayer(e.getPlayer());
		if (mp.ChatOnly)
			e.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerTeleport(PlayerTeleportEvent e) {
		/*
		 * if (BoardColl.get().getFactionAt(PS.valueOf(e.getFrom())).getId()
		 * .equalsIgnoreCase("nomansland") ||
		 * BoardColl.get().getFactionAt(PS.valueOf(e.getTo())).getId()
		 * .equalsIgnoreCase("nomansland")) {
		 */
		// e.setTo(e.getFrom());
		// e.setCancelled(true); // Relative coordinates mess it up
		/*
		 * System.out.println("From: " + e.getFrom()); System.out.println("To: "
		 * + e.getTo()); System.out.println("Cause: "+e.getCause());
		 */
		/*
		 * e.getPlayer().sendMessage(
		 * "§cYou are not allowed to teleport to/from No Mans Land."); }
		 */

		if (ChatPlayer.GetFromPlayer(e.getPlayer()).ChatOnly) {
			e.setCancelled(true);
			e.getPlayer().sendMessage("§cYou are not allowed to teleport while in chat-only mode.");
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		MinigamePlayer mp = Minigames.plugin.pdata.getMinigamePlayer(e.getPlayer());
		if (mp == null)
			return;
		if (mp.isInMinigame() && mp.getMinigame().getName(false).equalsIgnoreCase("twohundred")) {
			Block block = e.getClickedBlock();
			if (block == null)
				return;
			if (block.getType() == Material.ENDER_CHEST) {
				e.setCancelled(true);
				e.getPlayer().sendMessage("§You are not allowed to use enderchests here.");
				System.out.println(e.getPlayer().getName() + " tried to use an enderchest in twohundred.");
			}
		}
	}

	public static Channel ConsoleChannel = Channel.GlobalChat;

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onConsoleCommand(ServerCommandEvent event) {
		if (event.getCommand().length() < 2)
			return;
		int index = event.getCommand().indexOf(" ");
		String cmd = "";
		if (index == -1) {
			cmd = event.getCommand();
			if (cmd.equalsIgnoreCase(Channel.GlobalChat.Command)) {
				ConsoleChannel = Channel.GlobalChat;
				event.getSender().sendMessage("§6You are now talking in: §b" + ConsoleChannel.DisplayName);
				event.setCommand("dontrunthiscmd");
			} else if (cmd.equalsIgnoreCase(Channel.AdminChat.Command)) {
				if (ConsoleChannel.equals(Channel.AdminChat))
					ConsoleChannel = Channel.GlobalChat;
				else
					ConsoleChannel = Channel.AdminChat;
				event.getSender().sendMessage("§6You are now talking in: §b" + ConsoleChannel.DisplayName);
				event.setCommand("dontrunthiscmd");
			} else if (cmd.equalsIgnoreCase(Channel.ModChat.Command)) {
				if (ConsoleChannel.equals(Channel.ModChat))
					ConsoleChannel = Channel.GlobalChat;
				else
					ConsoleChannel = Channel.ModChat;
				event.getSender().sendMessage("§6You are now talking in: §b" + ConsoleChannel.DisplayName);
				event.setCommand("dontrunthiscmd");
			}
		} else {
			cmd = event.getCommand().substring(0, index);
			if (cmd.equalsIgnoreCase(Channel.GlobalChat.Command)) {
				Channel c = ConsoleChannel;
				ConsoleChannel = Channel.GlobalChat;
				ChatProcessing.ProcessChat(Bukkit.getServer().getConsoleSender(),
						event.getCommand().substring(index + 1));
				ConsoleChannel = c;
				event.setCommand("dontrunthiscmd");
			} else if (cmd.equalsIgnoreCase(Channel.TownChat.Command)) {
				Channel c = ConsoleChannel;
				ConsoleChannel = Channel.TownChat;
				ChatProcessing.ProcessChat(Bukkit.getServer().getConsoleSender(),
						event.getCommand().substring(index + 1));
				ConsoleChannel = c;
				event.setCommand("dontrunthiscmd");
			} else if (cmd.equalsIgnoreCase(Channel.NationChat.Command)) {
				Channel c = ConsoleChannel;
				ConsoleChannel = Channel.NationChat;
				ChatProcessing.ProcessChat(Bukkit.getServer().getConsoleSender(),
						event.getCommand().substring(index + 1));
				ConsoleChannel = c;
				event.setCommand("dontrunthiscmd");
			} else if (cmd.equalsIgnoreCase(Channel.AdminChat.Command)) {
				Channel c = ConsoleChannel;
				ConsoleChannel = Channel.AdminChat;
				ChatProcessing.ProcessChat(Bukkit.getServer().getConsoleSender(),
						event.getCommand().substring(index + 1));
				ConsoleChannel = c;
				event.setCommand("dontrunthiscmd");
			} else if (cmd.equalsIgnoreCase(Channel.ModChat.Command)) {
				Channel c = ConsoleChannel;
				ConsoleChannel = Channel.ModChat;
				ChatProcessing.ProcessChat(Bukkit.getServer().getConsoleSender(),
						event.getCommand().substring(index + 1));
				ConsoleChannel = c;
				event.setCommand("dontrunthiscmd");
			}
		}
		if (cmd.toLowerCase().startsWith("un")) {
			for (HelpTopic ht : PluginMain.Instance.getServer().getHelpMap().getHelpTopics()) {
				// event.getSender().sendMessage("HT: " + ht.getName());
				if (ht.getName().equalsIgnoreCase("/" + cmd))
					return;
			}
			String s = cmd.substring(2);
			Player target = null;
			target = Bukkit.getPlayer(event.getCommand().substring(index + 1));
			if (target == null) {
				event.getSender().sendMessage("§cError: Player not found. (/un" + s + " <player>)");
				event.setCommand("dontrunthiscmd");
			}
			if (target != null) {
				target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 10 * 20, 5, false, false));
				for (Player pl : PluginMain.GetPlayers())
					pl.sendMessage(event.getSender().getName() + " un" + s + "'d " + target.getDisplayName());
				Bukkit.getServer().getConsoleSender()
						.sendMessage(event.getSender().getName() + " un" + s + "'d " + target.getDisplayName());
				event.setCommand("dontrunthiscmd");
			}
		}
	}
}
