package io.github.norbipeti.thebuttonmc;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.earth2me.essentials.Mob;
import com.earth2me.essentials.Mob.MobException;

import au.com.mineauz.minigames.MinigamePlayer;
import au.com.mineauz.minigames.Minigames;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;

public class Commands implements CommandExecutor {

	public static Player Lastlol = null;
	public static boolean Lastlolornot;
	public static boolean Lastlolconsole;

	// This method is called, when somebody uses our command
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		if (sender instanceof Player) {
			final Player player = (Player) sender;
			switch (cmd.getName()) {
			case "u": {
				if (args.length < 1)
					return false;
				MaybeOfflinePlayer p = MaybeOfflinePlayer.AllPlayers.get(player
						.getUniqueId());
				switch (args[0].toLowerCase()) {
				case "accept": {
					if (args.length < 2 && p.UserNames.size() > 1) {
						player.sendMessage("§9Multiple users commented your name. §bPlease pick one using /u accept <username>");
						StringBuilder sb = new StringBuilder();
						sb.append("§6Usernames:");
						for (String username : p.UserNames)
							sb.append(" ").append(username);
						player.sendMessage(sb.toString());
						return true;
					}
					if (p.FlairState.equals(FlairStates.NoComment)
							|| p.UserNames.size() == 0) {
						player.sendMessage("§cError: You need to write your username to the reddit thread at /r/TheButtonMinecraft§r");
						return true;
					}
					if (args.length > 1 && !p.UserNames.contains(args[1])) {
						player.sendMessage("§cError: Unknown name: " + args[1]
								+ "§r");
						return true;
					}
					if (p.Working) {
						player.sendMessage("§cError: Something is already in progress.§r");
						return true;
					}

					if ((args.length > 1 ? args[1] : p.UserNames.get(0))
							.equals(p.UserName)) {
						player.sendMessage("§cYou already have this user's flair.§r");
						return true;
					}
					if (args.length > 1)
						p.UserName = args[1];
					else
						p.UserName = p.UserNames.get(0);

					player.sendMessage("§bObtaining flair...");
					p.Working = true;
					Timer timer = new Timer();
					PlayerJoinTimerTask tt = new PlayerJoinTimerTask() {
						@Override
						public void run() {
							try {
								PluginMain.Instance.DownloadFlair(mp);
							} catch (Exception e) {
								e.printStackTrace();
								player.sendMessage("Sorry, but an error occured while trying to get your flair. Please contact a mod.");
								mp.Working = false;
								return;
							}

							if (mp.FlairState.equals(FlairStates.Commented)) {
								player.sendMessage("Sorry, but your flair isn't recorded. Please ask an admin to set it for you. Also, prepare a comment on /r/thebutton, if possible.");
								mp.Working = false;
								return;
							}
							String flair = mp.GetFormattedFlair();
							mp.FlairState = FlairStates.Accepted;
							PluginMain.ConfirmUserMessage(mp);
							player.sendMessage("§bYour flair has been set:§r "
									+ flair);
							mp.Working = false;
						}
					};
					tt.mp = p;
					timer.schedule(tt, 20);
					break;
				}
				case "ignore": {
					if (p.FlairState.equals(FlairStates.Accepted)) {
						player.sendMessage("§cSorry, but ignoring the flair is no longer possible. As with the original Button, you can't undo what already happened.");
						return true;
					}
					if (p.FlairState.equals(FlairStates.Commented)) {
						player.sendMessage("Sorry, but your flair isn't recorded. Please ask a mod to set it for you.");
						return true;
					}
					if (!p.FlairState.equals(FlairStates.Ignored)) {
						p.FlairState = FlairStates.Ignored;
						p.SetFlair(MaybeOfflinePlayer.FlairTimeNone);
						p.UserName = "";
						player.sendMessage("§bYou have removed your flair. You can still use /u accept to get one.§r");
					} else
						player.sendMessage("§cYou already removed your flair.§r");
					break;
				}
				case "admin": // 2015.08.09.
					DoAdmin(player, args);
					break;
				case "opme": // 2015.08.10.
					player.sendMessage("It would be nice, wouldn't it?"); // Sometimes
																			// I'm
																			// bored
																			// too
					break;
				case "announce":
					DoAnnounce(player, args, null);
					break;
				case "name": {
					if (args.length == 1) {
						player.sendMessage("§cUsage: /u name <playername>§r");
						break;
					}
					MaybeOfflinePlayer mp = MaybeOfflinePlayer
							.GetFromName(args[1]);
					if (mp == null) {
						player.sendMessage("§cUnknown user (player has to be online): "
								+ args[1]);
						break;
					}
					player.sendMessage("§bUsername of " + args[1] + ": "
							+ mp.UserName);
					break;
				}
				case "enable":
					if (player.getName().equals("NorbiPeti")) {
						PlayerListener.Enable = true;
						player.sendMessage("Enabled.");
					} else
						player.sendMessage("Unknown command: " + cmd.getName());
					break;
				case "disable":
					if (player.getName().equals("NorbiPeti")) {
						PlayerListener.Enable = false;
						player.sendMessage("Disabled.");
					} else
						player.sendMessage("Unknown command: " + cmd.getName());
					break;
				case "kittycannon":
					DoKittyCannon(player, args);
					break;
				case "c":
					if (args.length < 2) {
						if (PluginMain.permission.has(player, "tbmc.rainbow")) {
							p.RainbowPresserColorMode = !p.RainbowPresserColorMode;
							p.OtherColorMode = "";
							if (p.RainbowPresserColorMode)
								player.sendMessage("§eRainbow colors §aenabled.");
							else
								player.sendMessage("§eRainbow colors §cdisabled.");
						} else {
							player.sendMessage("§cYou don't have permission for this command. Donate to get it!");
							return true;
						}
					} else {
						if (PluginMain.permission.has(player, "tbmc.admin")) {
							p.RainbowPresserColorMode = false;
							p.OtherColorMode = args[1];
							if (p.OtherColorMode.length() > 0)
								player.sendMessage(String.format(
										"§eMessage color set to %s",
										p.OtherColorMode));
							else
								player.sendMessage("§eMessage color reset.");
						} else {
							player.sendMessage("§cYou don't have permission for this command.");
							return true;
						}
					}
					break;
				default:
					return false;
				}
				return true;
			}
			case "nrp":
			case "ooc": {
				if (args.length == 0) {
					return false;
				} else {
					MaybeOfflinePlayer.AddPlayerIfNeeded(player.getUniqueId()).RPMode = false;
					String message = "";
					for (String arg : args)
						message += arg + " ";
					player.chat(message.substring(0, message.length() - 1));
					MaybeOfflinePlayer.AddPlayerIfNeeded(player.getUniqueId()).RPMode = true;
				}
				return true;
			}
			case "unlaugh":
			case "unlol": {
				if (Lastlol != null) {
					Lastlol.addPotionEffect(new PotionEffect(
							PotionEffectType.BLINDNESS, 10 * 20, 5, false,
							false));
					for (Player pl : PluginMain.GetPlayers())
						pl.sendMessage(player.getDisplayName()
								+ (Lastlolornot ? " unlolled " : " unlaughed ")
								+ Lastlol.getDisplayName());
					Bukkit.getServer()
							.getConsoleSender()
							.sendMessage(
									player.getDisplayName()
											+ (Lastlolornot ? " unlolled "
													: " unlaughed ")
											+ Lastlol.getDisplayName());
					Lastlol = null;
				} else if (Lastlolconsole) {
					for (Player pl : PluginMain.GetPlayers())
						pl.sendMessage(player.getDisplayName()
								+ (Lastlolornot ? " unlolled " : " unlaughed ")
								+ Bukkit.getServer().getConsoleSender()
										.getName());
					Bukkit.getServer()
							.getConsoleSender()
							.sendMessage(
									player.getDisplayName()
											+ (Lastlolornot ? " unlolled "
													: " unlaughed ")
											+ Bukkit.getServer()
													.getConsoleSender()
													.getName());
				}
				return true;
			}
			case "yeehaw": {
				for (Player p : PluginMain.GetPlayers()) {
					p.playSound(p.getLocation(), "tbmc.yeehaw", 1f, 1f);
					p.sendMessage("§b* " + p.getDisplayName() + " YEEHAWs.");
				}
				return true;
			}
			case "mwiki": {
				DoMWiki(player, args);
				return true;
			}
			case "tableflip": {
				String msg = "(╯°□°）╯︵ ┻�?┻";
				if (args.length > 0) {
					msg = args[0] + " " + msg;
				}
				player.chat(msg);
				return true;
			}
			case "unflip": {
				String msg = "┬─┬﻿ �?�( ゜-゜�?�)";
				if (args.length > 0) {
					msg = args[0] + "" + msg;
				}
				player.chat(msg);
				return true;
			}
			case "chatonly": {
				MaybeOfflinePlayer p = MaybeOfflinePlayer.AllPlayers.get(player
						.getUniqueId());
				p.ChatOnly = true;
				player.setGameMode(GameMode.SPECTATOR);
				player.sendMessage("§bChat-only mode enabled. You are now invincible.");
				return true;
			}
			default:
				player.sendMessage("Unknown command: " + cmd.getName());
				break;
			}
		} else if (args.length > 0 && args[0].toLowerCase().equals("admin")) // 2015.08.09.
		{
			DoAdmin(null, args); // 2015.08.09.
			return true; // 2015.08.09.
		} else if (args.length > 0 && args[0].toLowerCase().equals("announce")) {
			if (sender instanceof BlockCommandSender)
				DoAnnounce(null, args, (BlockCommandSender) sender);
			else
				DoAnnounce(null, args, null);
			return true;
		} else {
			switch (cmd.getName()) {
			case "unlaugh":
			case "unlol": {
				if (Lastlol != null) {
					Lastlol.addPotionEffect(new PotionEffect(
							PotionEffectType.BLINDNESS, 10 * 20, 5, false,
							false));
					for (Player pl : PluginMain.GetPlayers())
						pl.sendMessage(Bukkit.getServer().getConsoleSender()
								.getName()
								+ (Lastlolornot ? " unlolled " : " unlaughed ")
								+ Lastlol.getDisplayName());
					Bukkit.getServer()
							.getConsoleSender()
							.sendMessage(
									Bukkit.getServer().getConsoleSender()
											.getName()
											+ (Lastlolornot ? " unlolled "
													: " unlaughed ")
											+ Lastlol.getDisplayName());
					Lastlol = null;
				}
				return true;
			}
			}
		}
		return false;
	}

	private static void DoReload(Player player) {
		try {
			PluginMain.Console
					.sendMessage("§6-- Reloading The Button Minecraft plugin...§r");
			PluginMain.LoadFiles(true); // 2015.08.09.
			for (Player p : PluginMain.GetPlayers()) {
				MaybeOfflinePlayer mp = MaybeOfflinePlayer.AddPlayerIfNeeded(p
						.getUniqueId());
				if (mp.FlairState.equals(FlairStates.Recognised)
						|| mp.FlairState.equals(FlairStates.Commented)) {
					PluginMain.ConfirmUserMessage(mp);
				}
				String msg = "§bNote: The auto-flair plugin has been reloaded. You might need to wait 10s to have your flair.§r"; // 2015.08.09.
				p.sendMessage(msg); // 2015.08.09.
			}
			PluginMain.Console.sendMessage("§6-- Reloading done!§r");
		} catch (Exception e) {
			System.out.println("Error!\n" + e);
			if (player != null)
				player.sendMessage("§cAn error occured. See console for details.§r");
			PluginMain.LastException = e; // 2015.08.09.
		}
	}

	private static Player ReloadPlayer; // 2015.08.09.

	private static String DoAdminUsage = "§cUsage: /u admin reload|playerinfo|getlasterror|save|setflair|updateplugin|togglerpshow|toggledebug|savepos|loadpos§r";

	private static void DoAdmin(Player player, String[] args) {
		if (player == null || PluginMain.permission.has(player, "tbmc.admin")) {
			if (args.length == 1) {
				String message = DoAdminUsage;
				SendMessage(player, message);
				return;
			}
			// args[0] is "admin"
			switch (args[1].toLowerCase()) {
			case "reload":
				ReloadPlayer = player; // 2015.08.09.
				SendMessage(
						player,
						"§bMake sure to save the current settings before you modify and reload them! Type /u admin confirm when done.§r");
				break;
			case "playerinfo":
				DoPlayerInfo(player, args);
				break;
			case "getlasterror":
				DoGetLastError(player, args);
				break; // <-- 2015.08.10.
			case "confirm":
				if (ReloadPlayer == player)
					DoReload(player); // 2015.08.09.
				else
					SendMessage(player,
							"§cYou need to do /u admin reload first.§r");
				break;
			case "save":
				PluginMain.SaveFiles(); // 2015.08.09.
				SendMessage(player,
						"§bSaved files. Now you can edit them and reload if you want.§r");
				break;
			case "setflair":
				DoSetFlair(player, args);
				break;
			case "updateplugin": // 2015.08.10.
				DoUpdatePlugin(player);
				break;
			case "togglerpshow":
				PlayerListener.ShowRPTag = !PlayerListener.ShowRPTag;
				SendMessage(player, "RP tag showing "
						+ (PlayerListener.ShowRPTag ? "enabled" : "disabled"));
				break;
			case "toggledebug":
				PlayerListener.DebugMode = !PlayerListener.DebugMode;
				SendMessage(player, "DebugMode: " + PlayerListener.DebugMode);
				break;
			case "savepos":
				DoSaveLoadPos(player, args);
				break;
			case "loadpos":
				DoSaveLoadPos(player, args);
				break;
			case "updatedynmap":
				DoUpdateDynmap(player, args);
			default:
				String message = DoAdminUsage;
				SendMessage(player, message);
				return;
			}
		} else
			player.sendMessage("§cYou don't have permission to use this command.§r");
	}

	private static void DoPlayerInfo(Player player, String[] args) { // 2015.08.09.
		// args[0] is "admin" - args[1] is "playerinfo"
		if (args.length == 2) {
			String message = "§cUsage: /u admin playerinfo <player>§r";
			SendMessage(player, message);
			return;
		}
		MaybeOfflinePlayer p = MaybeOfflinePlayer.GetFromName(args[2]);
		if (p == null) {
			String message = "§cPlayer not found: " + args[2]
					+ " - Currently only online players can be viewed§r";
			SendMessage(player, message);
			return;
		}
		SendMessage(player, "Player name: " + p.PlayerName);
		SendMessage(player, "User flair: " + p.GetFormattedFlair());
		SendMessage(player, "Username: " + p.UserName);
		SendMessage(player, "Flair state: " + p.FlairState);
		StringBuilder sb = new StringBuilder();
		sb.append("§6Usernames:");
		for (String username : p.UserNames)
			sb.append(" ").append(username);
		SendMessage(player, sb.toString());
	}

	private static void SendMessage(Player player, String message) { // 2015.08.09.
		if (player == null)
			PluginMain.Console.sendMessage(message); // 2015.08.12.
		else
			player.sendMessage(message);
	}

	private static void DoGetLastError(Player player, String[] args) { // 2015.08.09.
		// args[0] is "admin" - args[1] is "getlasterror"
		if (PluginMain.LastException != null) {
			SendMessage(player, "Last error:");
			SendMessage(player, PluginMain.LastException.toString());
			PluginMain.LastException = null;
		} else
			SendMessage(player, "There were no exceptions.");
	}

	private static void SetPlayerFlair(Player player,
			MaybeOfflinePlayer targetplayer, short flairtime, boolean cheater,
			String username) {
		targetplayer.SetFlair(flairtime, cheater);
		targetplayer.FlairState = FlairStates.Accepted;
		if (username == null)
			targetplayer.UserName = "";
		else {
			targetplayer.UserName = username;
			if (!targetplayer.UserNames.contains(username))
				targetplayer.UserNames.add(username);
		}
		SendMessage(player,
				"§bThe flair has been set. Player: " + targetplayer.PlayerName
						+ " Flair: " + targetplayer.GetFormattedFlair() + "§r");
	}

	// TODO: Put commands into separate classes
	private static void DoSetFlair(Player player, String[] args) {
		// args[0] is "admin" - args[1] is "setflair"
		if (args.length < 5) {
			SendMessage(
					player,
					"§cUsage: /u admin setflair <playername> <flairtime (or non-presser, cant-press, none)> <cheater(true/false)> [username]");
			return;
		}
		Player p = Bukkit.getPlayer(args[2]);
		if (p == null) {
			SendMessage(player, "§cPlayer not found.&r");
			return;
		}
		short flairtime = 0x00;
		if (args[3].equalsIgnoreCase("non-presser"))
			flairtime = MaybeOfflinePlayer.FlairTimeNonPresser;
		else if (args[3].equalsIgnoreCase("cant-press"))
			flairtime = MaybeOfflinePlayer.FlairTimeCantPress;
		else if (args[3].equalsIgnoreCase("none"))
			flairtime = MaybeOfflinePlayer.FlairTimeNone;
		else {
			try {
				flairtime = Short.parseShort(args[3]);
			} catch (Exception e) {
				SendMessage(player,
						"§cFlairtime must be a number, \"non-presser\", \"cant-press\" or \"none\".");
				return;
			}
		} // TODO: Split config to per-player
		boolean cheater = false;
		if (args[4].equalsIgnoreCase("true"))
			cheater = true;
		else if (args[4].equalsIgnoreCase("false"))
			cheater = false;
		else {
			SendMessage(player, "§cUnknown value for cheater parameter.");
			return;
		}
		SetPlayerFlair(player,
				MaybeOfflinePlayer.AddPlayerIfNeeded(p.getUniqueId()),
				flairtime, cheater, (args.length > 5 ? args[5] : null));
	}

	private static void DoUpdatePlugin(Player player) { // 2015.08.10.
		SendMessage(player, "Updating Auto-Flair plugin...");
		System.out.println("Forced updating of Auto-Flair plugin.");
		URL url;
		try {
			url = new URL(
					"https://github.com/NorbiPeti/thebuttonautoflairmc/raw/master/TheButtonAutoFlair.jar");
			FileUtils.copyURLToFile(url, new File(
					"plugins/TheButtonAutoFlair.jar"));
			SendMessage(player, "Updating done!");
		} catch (MalformedURLException e) {
			System.out.println("Error!\n" + e);
			PluginMain.LastException = e; // 2015.08.09.
		} catch (IOException e) {
			System.out.println("Error!\n" + e);
			PluginMain.LastException = e; // 2015.08.09.
		}
	}

	private static void DoAnnounce(Player player, String[] args,
			BlockCommandSender commandblock) {
		if (player == null || player.isOp()
				|| player.getName().equals("NorbiPeti")) {
			if (args.length == 1) {
				String message = "§cUsage: /u announce add|remove|settime|list|edit§r";
				SendMessage(player, message);
				return;
			}
			switch (args[1].toLowerCase()) {
			case "add":
				if (args.length < 3) {
					SendMessage(player, "§cUsage: /u announce add <message>");
					return;
				}
				StringBuilder sb = new StringBuilder();
				for (int i = 2; i < args.length; i++) {
					sb.append(args[i]);
					if (i != args.length - 1)
						sb.append(" ");
				}
				String finalmessage = sb.toString().replace('&', '§');
				PluginMain.AnnounceMessages.add(finalmessage);
				SendMessage(player, "§bAnnouncement added.§r");
				break;
			case "remove":
				if (args.length < 3) {
					SendMessage(player, "§cUsage: /u announce remove <index>");
					return;
				}
				PluginMain.AnnounceMessages.remove(Integer.parseInt(args[2]));
				break;
			case "settime":
				if (args.length < 3) {
					SendMessage(player,
							"§cUsage: /u announce settime <minutes>");
					return;
				}
				PluginMain.AnnounceTime = Integer.parseInt(args[2]) * 60 * 1000;
				SendMessage(player, "Time set between announce messages");
				break;
			case "list":
				SendMessage(player, "§bList of announce messages:§r");
				SendMessage(player, "§bFormat: [index] message§r");
				int i = 0;
				for (String message : PluginMain.AnnounceMessages)
					SendMessage(player, "[" + i++ + "] " + message);
				SendMessage(player,
						"§bCurrent wait time between announcements: "
								+ PluginMain.AnnounceTime / 60 / 1000
								+ " minute(s)§r");
				break;
			case "edit":
				if (commandblock == null) {
					SendMessage(
							player,
							"§cError: This command can only be used from a command block. Use /u announce remove.");
					break;
				}
				if (args.length < 4) {
					commandblock
							.sendMessage("§cUsage: /u announce edit <index> <message>");
					return;
				}
				StringBuilder sb1 = new StringBuilder();
				for (int i1 = 3; i1 < args.length; i1++) {
					sb1.append(args[i1]);
					if (i1 != args.length - 1)
						sb1.append(" ");
				}
				String finalmessage1 = sb1.toString().replace('&', '§');
				int index = Integer.parseInt(args[2]);
				if (index > 100)
					break;
				while (PluginMain.AnnounceMessages.size() <= index)
					PluginMain.AnnounceMessages.add("");
				PluginMain.AnnounceMessages.set(Integer.parseInt(args[2]),
						finalmessage1);
				commandblock.sendMessage("Announcement edited.");
				break;
			default:
				String message = "§cUsage: /u announce add|remove|settime|list|edit§r";
				SendMessage(player, message);
				return;
			}
		}
	}

	@SuppressWarnings("unused")
	private static void DoSaveLoadPos(Player player, String[] args) { // 2015.08.09.
		// args[0] is "admin" - args[1] is "savepos|loadpos"
		if (args.length == 2) {
			String message = "§cUsage: /u admin savepos|loadpos <player>§r";
			SendMessage(player, message);
			return;
		}
		Player p = null;
		try {
			p = Bukkit.getPlayer(args[2]);
		} catch (Exception e) {
		}
		if (!MaybeOfflinePlayer.AllPlayers.containsKey(p.getUniqueId())) {
			String message = "§cPlayer not found: " + args[2] + "§r";
			SendMessage(player, message);
			return;
		}
		MaybeOfflinePlayer mp = MaybeOfflinePlayer.AllPlayers.get(p
				.getUniqueId());
		if (p == null) {
			String message = "§cPlayer is not online: " + args[2] + "§r";
			SendMessage(player, message);
			return;
		}
		if (args[1].equalsIgnoreCase("savepos")) {
			mp.SavedLocation = p.getLocation();
		} else if (args[1].equalsIgnoreCase("loadpos")) {
			if (mp.SavedLocation != null)
				p.teleport(mp.SavedLocation);
		} else {
			String message = "§cUsage: /u admin savepos|loadpos <player>§r";
			SendMessage(player, message);
			return;
		}
		// SendMessage(player, "Player " + p.getName() +
		// " position saved/loaded.");s
	}

	private static void DoUpdateDynmap(Player player, String[] args) {
		// args[0] is "admin" - args[1] is "updatedynmap"
		if (args.length == 2) {
			String message = "§cUsage: /u admin updatedynmap <password>§r";
			SendMessage(player, message);
			return;
		}
	}

	private static Random random = new Random();

	public static String KittyCannonMinigame = "KittyCannon";

	private static void DoKittyCannon(Player player, String[] args) {
		if (player == null) {
			SendMessage(player,
					"§cThis command can only be used by a player.§r");
			return;
		}
		MinigamePlayer mp = Minigames.plugin.pdata.getMinigamePlayer(player);
		if (!(mp.isInMinigame() && mp.getMinigame().getName(false)
				.equalsIgnoreCase(Commands.KittyCannonMinigame))) {
			SendMessage(player,
					"§cYou can only use KittyCannon in it's minigame!");
			return;
		}
		try {
			final Mob cat = Mob.OCELOT;
			final Ocelot ocelot = (Ocelot) cat.spawn(player.getWorld(),
					player.getServer(), player.getEyeLocation());
			if (ocelot == null) {
				return;
			}
			final ArrayList<String> lore = new ArrayList<>();
			lore.add(player.getName());
			final int i = random.nextInt(Ocelot.Type.values().length);
			ocelot.setCatType(Ocelot.Type.values()[i]);
			ocelot.setTamed(true);
			ocelot.setBaby();
			ocelot.addPotionEffect(new PotionEffect(
					PotionEffectType.DAMAGE_RESISTANCE, 5, 5));
			ocelot.setVelocity(player.getEyeLocation().getDirection()
					.multiply(2));
			Bukkit.getScheduler().scheduleSyncDelayedTask(PluginMain.Instance,
					new Runnable() {
						@SuppressWarnings("deprecation")
						@Override
						public void run() {
							final Location loc = ocelot.getLocation();
							ocelot.remove();
							loc.getWorld().createExplosion(loc, 0F);
							final ItemStack head = new ItemStack(
									Material.SKULL_ITEM, 1, (short) 3, (byte) 3);
							SkullMeta im = (SkullMeta) head.getItemMeta();
							im.setDisplayName("§rOcelot Head");
							im.setOwner("MHF_Ocelot");
							im.setLore(lore);
							head.setItemMeta(im);
							loc.getWorld().dropItem(loc, head);
						}
					}, 20);
		} catch (MobException e) {
		}
	}

	private static void DoMWiki(Player player, String[] args) {
		String query = "";
		for (int i = 0; i < args.length; i++)
			query += args[i];
		query = query.trim();
		if (args.length == 0)
			SendMessage(player,
					"§bMinecraft Wiki link: http://minecraft.gamepedia.com/");
		else
			SendMessage(player,
					"§bMinecraft Wiki link: http://minecraft.gamepedia.com/index.php?search="
							+ query + "&title=Special%3ASearch&go=Go");
	}
}
