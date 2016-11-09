package buttondevteam.chat.listener;

import java.util.Map.Entry;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import com.palmergames.bukkit.towny.Towny;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.help.HelpTopic;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import au.com.mineauz.minigames.MinigamePlayer;
import au.com.mineauz.minigames.Minigames;
import buttondevteam.chat.Channel;
import buttondevteam.chat.ChatPlayer;
import buttondevteam.chat.ChatProcessing;
import buttondevteam.chat.PluginMain;
import buttondevteam.chat.commands.ucmds.KittycannonCommand;
import buttondevteam.lib.TBMCPlayer;
import buttondevteam.lib.TBMCPlayer.InfoTarget;
import buttondevteam.lib.TBMCPlayerGetInfoEvent;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
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
	/**
	 * Does not contain format codes
	 */
	public static BiMap<String, UUID> nicknames = HashBiMap.create();

	public static boolean Enable = false;

	public static int LoginWarningCountTotal = 5;

	public static String NotificationSound;
	public static double NotificationPitch;

	public static boolean ShowRPTag = false;

	public final static String[] LaughStrings = new String[] { "xd", "lel", "lawl", "kek", "lmao", "hue", "hah" };

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
		ChatPlayer mp = TBMCPlayer.getPlayer(event.getPlayer()).asPluginPlayer(ChatPlayer.class);
		String cmd = "";
		if (index == -1) {
			cmd = event.getMessage().substring(1);
			for (Channel channel : Channel.getChannels()) {
				if (cmd.equalsIgnoreCase(channel.Command)) {
					if (mp.CurrentChannel.equals(channel))
						mp.CurrentChannel = Channel.GlobalChat;
					else
						mp.CurrentChannel = channel;
					event.getPlayer().sendMessage("§6You are now talking in: §b" + mp.CurrentChannel.DisplayName);
					event.setCancelled(true);
					break;
				}
			}
		} else {
			cmd = event.getMessage().substring(1, index);
			for (Channel channel : Channel.getChannels()) {
				if (cmd.equalsIgnoreCase(channel.Command)) {
					event.setCancelled(true);
					Channel c = mp.CurrentChannel;
					mp.CurrentChannel = channel;
					event.getPlayer().chat(event.getMessage().substring(index + 1));
					mp.CurrentChannel = c;
				} else if (cmd.equalsIgnoreCase("tpahere")) {
					Player player = Bukkit.getPlayer(event.getMessage().substring(index + 1));
					if (player != null)
						player.sendMessage("§b" + event.getPlayer().getDisplayName() + " §bis in this world: "
								+ event.getPlayer().getWorld().getName());
				} else if (cmd.equalsIgnoreCase("minecraft:me")) {
					if (!PluginMain.essentials.getUser(event.getPlayer()).isMuted()) {
						event.setCancelled(true);
						String message = event.getMessage().substring(index + 1);
						for (Player p : PluginMain.GetPlayers())
							p.sendMessage(String.format("* %s %s", event.getPlayer().getDisplayName(), message));
					}
				}
			}
		}
		if (cmd.equalsIgnoreCase("sethome")) { // TODO: Move out?
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
			String currentWorld = event.getPlayer().getLocation().getWorld().getName();
			Location currentLocation = event.getPlayer().getLocation();
			TownyUniverse universe = Towny.getPlugin(Towny.class).getTownyUniverse();
			if (TownyUniverse.isWarTime()) {
				War war = universe.getWarEvent();
				if (war.isWarZone(
						new WorldCoord(currentWorld, currentLocation.getBlockX(), currentLocation.getBlockZ()))) {
					event.getPlayer().sendMessage("§cError: You can't teleport out of a war zone!");
					event.setCancelled(true);
				}
			}
		} else if (cmd.toLowerCase().startsWith("un")) {
			for (HelpTopic ht : PluginMain.Instance.getServer().getHelpMap().getHelpTopics()) {
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
		for (Entry<String, UUID> nicknamekv : nicknames.entrySet()) {
			if (nicknamekv.getKey().startsWith(name)
					&& !nicknamekv.getKey().equals(Bukkit.getPlayer(nicknamekv.getValue()).getName()))
				e.getTabCompletions().add(nicknamekv.getKey());
		}
	}

	public static boolean ActiveF = false;
	public static int FCount = 0;
	public static ChatPlayer FPlayer = null;
	private Timer Ftimer;
	public static int AlphaDeaths;

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		if (e.getEntity().getName().equals("Alpha_Bacca44"))
			AlphaDeaths++;
		MinigamePlayer mgp = Minigames.plugin.pdata.getMinigamePlayer(e.getEntity());
		if ((mgp != null && !mgp.isInMinigame()) && new Random().nextBoolean()) { // Don't store Fs for NPCs
			if (Ftimer != null)
				Ftimer.cancel();
			ActiveF = true;
			FCount = 0;
			FPlayer = TBMCPlayer.getPlayer(e.getEntity().getUniqueId()).asPluginPlayer(ChatPlayer.class);
			FPlayer.setFDeaths(FPlayer.getFDeaths() + 1);
			for (Player p : PluginMain.GetPlayers()) {
				ChatPlayer mp = TBMCPlayer.getPlayerAs(p.getUniqueId(), ChatPlayer.class);
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
		MinigamePlayer mp = Minigames.plugin.pdata.getMinigamePlayer(e.getPlayer());

		if (!(mp.isInMinigame()
				&& mp.getMinigame().getName(false).equalsIgnoreCase(KittycannonCommand.KittyCannonMinigame)))
			return;
		ItemStack item = e.getItem().getItemStack();
		if (!item.getType().equals(Material.SKULL_ITEM) && !item.getType().equals(Material.SKULL))
			return;
		SkullMeta meta = (SkullMeta) item.getItemMeta();
		if (!meta.getDisplayName().equals("§rOcelot Head") || !meta.getOwner().equals("MHF_Ocelot"))
			return;
		if (meta.getLore() == null || meta.getLore().size() == 0)
			return;
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
		PluginMain.Instance.getLogger().info("Vote: " + vote);
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
		ChatPlayer mp = TBMCPlayer.getPlayerAs(e.getPlayer(), ChatPlayer.class);
		if (mp.ChatOnly)
			e.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerTeleport(PlayerTeleportEvent e) {
		if (TBMCPlayer.getPlayerAs(e.getPlayer(), ChatPlayer.class).ChatOnly) {
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
				PluginMain.Instance.getLogger()
						.warning(e.getPlayer().getName() + " tried to use an enderchest in twohundred.");
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

	@EventHandler
	public void onGetInfo(TBMCPlayerGetInfoEvent e) {
		ChatPlayer cp = e.getPlayer().asPluginPlayer(ChatPlayer.class);
		e.addInfo("Minecraft name: " + cp.getPlayerName());
		e.addInfo("Reddit name: " + cp.getUserName());
		if (e.getTarget() == InfoTarget.MCCommand)
			e.addInfo("/r/TheButton flair: " + cp.GetFormattedFlair());
		else
			e.addInfo("/r/TheButton flair: (" + cp.getFlairTime() + "s)");
		e.addInfo("Respect: " + cp.getFCount() / cp.getFDeaths());
	}
}