package buttondevteam.chat.listener;

import java.util.*;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.help.HelpTopic;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import buttondevteam.chat.*;
import buttondevteam.lib.TBMCChatEvent;
import buttondevteam.lib.TBMCCoreAPI;
import buttondevteam.lib.chat.Channel;
import buttondevteam.lib.chat.ChatChannelRegisterEvent;
import buttondevteam.lib.chat.TBMCChatAPI;
import buttondevteam.lib.player.TBMCPlayer;
import buttondevteam.lib.player.TBMCPlayerGetInfoEvent;
import buttondevteam.lib.player.ChromaGamerBase.InfoTarget;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
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

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		if (event.isCancelled())
			return;
		TBMCChatAPI.SendChatMessage(
				TBMCPlayer.getPlayer(event.getPlayer().getUniqueId(), ChatPlayer.class).CurrentChannel,
				event.getPlayer(), event.getMessage());
		event.setCancelled(true); // The custom event should only be cancelled when muted or similar
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void PlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		if (event.getMessage().length() < 2)
			return;
		int index = event.getMessage().indexOf(" ");
		ChatPlayer mp = TBMCPlayer.getPlayer(event.getPlayer().getUniqueId(), ChatPlayer.class);
		String cmd = "";
		if (index == -1) {
			cmd = event.getMessage().substring(1);
			for (Channel channel : Channel.getChannels()) {
				if (cmd.equalsIgnoreCase(channel.ID)) {
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
				if (cmd.equalsIgnoreCase(channel.ID)) {
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
						Bukkit.broadcastMessage(String.format("* %s %s", event.getPlayer().getDisplayName(), message));
					}
				}
			}
		}
		if (cmd.toLowerCase().startsWith("un")) {
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
					Bukkit.broadcastMessage(
							event.getPlayer().getDisplayName() + " un" + s + "'d " + target.getDisplayName());
					event.setCancelled(true);
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
	public static ChatPlayer FPlayer = null;
	private Timer Ftimer;
	public static int AlphaDeaths;
	public static ArrayList<CommandSender> Fs = new ArrayList<>();

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		if (e.getEntity().getName().equals("Alpha_Bacca44"))
			AlphaDeaths++;
		// MinigamePlayer mgp = Minigames.plugin.pdata.getMinigamePlayer(e.getEntity());
		if (/* (mgp != null && !mgp.isInMinigame()) && */ new Random().nextBoolean()) { // Don't store Fs for NPCs
			if (Ftimer != null)
				Ftimer.cancel();
			ActiveF = true;
			Fs.clear();
			FPlayer = TBMCPlayer.getPlayer(e.getEntity().getUniqueId(), ChatPlayer.class);
			FPlayer.FDeaths().set(FPlayer.FDeaths().get() + 1);
			Bukkit.broadcastMessage("§bPress F to pay respects.§r");
			Ftimer = new Timer();
			TimerTask tt = new TimerTask() {
				@Override
				public void run() {
					if (ActiveF) {
						ActiveF = false;
						if (FPlayer != null && FPlayer.FCount().get() < Integer.MAX_VALUE - 1)
							FPlayer.FCount().set(FPlayer.FCount().get() + Fs.size());
						Bukkit.broadcastMessage("§b" + Fs.size() + " " + (Fs.size() == 1 ? "person" : "people")
								+ " paid their respects.§r");
						Fs.clear();
					}
				}
			};
			Ftimer.schedule(tt, 15 * 1000);
		}
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
		ChatPlayer mp = TBMCPlayer.getPlayer(e.getPlayer().getUniqueId(), ChatPlayer.class);
		if (mp.ChatOnly)
			e.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerTeleport(PlayerTeleportEvent e) {
		if (TBMCPlayer.getPlayer(e.getPlayer().getUniqueId(), ChatPlayer.class).ChatOnly) {
			e.setCancelled(true);
			e.getPlayer().sendMessage("§cYou are not allowed to teleport while in chat-only mode.");
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
			for (Channel channel : Channel.getChannels()) {
				if (cmd.equalsIgnoreCase(channel.ID)) {
					if (ConsoleChannel.equals(channel))
						ConsoleChannel = Channel.GlobalChat;
					else
						ConsoleChannel = channel;
					event.getSender().sendMessage("§6You are now talking in: §b" + ConsoleChannel.DisplayName);
					event.setCommand("dontrunthiscmd");
					break;
				}
			}
		} else {
			cmd = event.getCommand().substring(0, index);
			for (Channel channel : Channel.getChannels()) {
				if (cmd.equalsIgnoreCase(channel.ID)) {
					Channel c = ConsoleChannel;
					ConsoleChannel = channel;
					TBMCChatAPI.SendChatMessage(PlayerListener.ConsoleChannel, Bukkit.getConsoleSender(),
							event.getCommand().substring(index + 1));
					ConsoleChannel = c;
					event.setCommand("dontrunthiscmd");
				}
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
				Bukkit.broadcastMessage(event.getSender().getName() + " un" + s + "'d " + target.getDisplayName());
				event.setCommand("dontrunthiscmd");
			}
		}
	}

	@EventHandler
	public void onGetInfo(TBMCPlayerGetInfoEvent e) {
		try (ChatPlayer cp = e.getPlayer().getAs(ChatPlayer.class)) {
			if (cp == null)
				return;
			e.addInfo("Minecraft name: " + cp.PlayerName().get());
			if (cp.UserName().get() != null && cp.UserName().get().length() > 0)
				e.addInfo("Reddit name: " + cp.UserName().get());
			final String flair = cp.GetFormattedFlair(e.getTarget() != InfoTarget.MCCommand);
			if (flair.length() > 0)
				e.addInfo("/r/TheButton flair: " + flair);
			e.addInfo("Respect: " + (double) cp.FCount().get() / (double) cp.FDeaths().get());
		} catch (Exception ex) {
			TBMCCoreAPI.SendException("Error while providing chat info for player " + e.getPlayer().getFileName(), ex);
		}
	}

	@EventHandler
	public void onPlayerTBMCChat(TBMCChatEvent e) {
		try {
			if (e.isCancelled())
				return;
			e.setCancelled(ChatProcessing.ProcessChat(e));
		} catch (Exception ex) {
			for (Player p : Bukkit.getOnlinePlayers())
				if (e.shouldSendTo(p))
					p.sendMessage("§c!§r["
							+ e.getChannel().DisplayName + "] <" + (e.getSender() instanceof Player
									? ((Player) e.getSender()).getDisplayName() : e.getSender().getName())
							+ "> " + e.getMessage());
			TBMCCoreAPI.SendException("An error occured while processing a chat message!", ex);
		}
	}

	@EventHandler
	public void onChannelRegistered(ChatChannelRegisterEvent e) {
		if (e.getChannel().filteranderrormsg != null && PluginMain.SB.getObjective(e.getChannel().ID) == null) // Not global chat and doesn't exist yet
			PluginMain.SB.registerNewObjective(e.getChannel().ID, "dummy");
	}
}
