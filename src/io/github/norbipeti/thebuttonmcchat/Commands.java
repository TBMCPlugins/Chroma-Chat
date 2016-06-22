package io.github.norbipeti.thebuttonmcchat;

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
			case "unlol": { //TODO
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
				String msg = " (╯°□°）╯︵ ┻━┻";
				if (args.length > 0) {
					msg = args[0] + " " + msg;
				}
				player.chat(msg);
				return true;
			}
			case "unflip": {
				String msg = " ┬─┬ ノ( ゜-゜ノ)";
				if (args.length > 0) {
					msg = args[0] + "" + msg;
				}
				player.chat(msg);
				return true;
			}
			case "shrug": {
				String msg = " ¯\\_(ツ)_/¯";
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
				return true;
			}
		}
		return false;
	}

	private static String DoAdminUsage = "§cUsage: /u admin reload|playerinfo|getlasterror|save|setflair|updateplugin|togglerpshow|toggledebug|savepos|loadpos§r";

	private static void DoAnnounce(Player player, String[] args,
			BlockCommandSender commandblock) {
		if (player == null || player.isOp()
				|| player.getName().equals("NorbiPeti")) {
			switch (args[1].toLowerCase()) {
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
