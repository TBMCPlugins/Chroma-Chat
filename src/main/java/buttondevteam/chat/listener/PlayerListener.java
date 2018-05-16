package buttondevteam.chat.listener;

import buttondevteam.chat.ChatPlayer;
import buttondevteam.chat.ChatProcessing;
import buttondevteam.chat.PluginMain;
import buttondevteam.lib.TBMCChatEvent;
import buttondevteam.lib.TBMCCoreAPI;
import buttondevteam.lib.chat.Channel;
import buttondevteam.lib.chat.ChatChannelRegisterEvent;
import buttondevteam.lib.chat.ChatRoom;
import buttondevteam.lib.chat.TBMCChatAPI;
import buttondevteam.lib.player.ChromaGamerBase.InfoTarget;
import buttondevteam.lib.player.TBMCPlayer;
import buttondevteam.lib.player.TBMCPlayerGetInfoEvent;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import net.ess3.api.events.NickChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.help.HelpTopic;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class PlayerListener implements Listener {
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
		if (!event.isCancelled())
			event.setCancelled(onCommandPreprocess(event.getPlayer(), event.getMessage()));
	}

	private boolean onCommandPreprocess(CommandSender sender, String message) {
		if (message.length() < 2)
			return false;
		int index = message.indexOf(" ");
		ChatPlayer mp;
		if (sender instanceof Player)
			mp = TBMCPlayer.getPlayer(((Player) sender).getUniqueId(), ChatPlayer.class);
		else
			mp = null;
		String cmd = "";
		final BiPredicate<Channel, String> checkchid = (chan, cmd1) -> cmd1.equalsIgnoreCase(chan.ID) || (chan.IDs != null && Arrays.stream(chan.IDs).anyMatch(cmd1::equalsIgnoreCase));
		if (index == -1) { // Only the command is run
			if (!(sender instanceof Player || sender instanceof ConsoleCommandSender))
				return false;
			// ^^ We can only store player or console channels - Directly sending to channels would still work if they had an event
			cmd = sender instanceof ConsoleCommandSender ? message : message.substring(1);
			for (Channel channel : Channel.getChannels()) {
				if (checkchid.test(channel, cmd)) {
					Supplier<Channel> getch = () -> sender instanceof Player ? mp.CurrentChannel : ConsoleChannel;
					Consumer<Channel> setch = ch -> {
						if (sender instanceof Player)
							mp.CurrentChannel = ch;
						else
							ConsoleChannel = ch;
					};
					Channel oldch = getch.get();
					if (oldch instanceof ChatRoom)
						((ChatRoom) oldch).leaveRoom(sender);
					if (oldch.equals(channel))
						setch.accept(Channel.GlobalChat);
					else {
						setch.accept(channel);
						if (channel instanceof ChatRoom)
							((ChatRoom) channel).joinRoom(sender);
					}
					sender.sendMessage("§6You are now talking in: §b" + getch.get().DisplayName);
					return true;
				}
			}
		} else { // We have arguments
			cmd = sender instanceof ConsoleCommandSender ? message.substring(0, index) : message.substring(1, index);
			if (cmd.equalsIgnoreCase("tpahere")) {
				Player player = Bukkit.getPlayer(message.substring(index + 1));
				if (player != null && sender instanceof Player)
					player.sendMessage("§b" + ((Player) sender).getDisplayName() + " §bis in this world: "
							+ ((Player) sender).getWorld().getName());
            } else if (cmd.equalsIgnoreCase("minecraft:me")) {
                if (!(sender instanceof Player) || !PluginMain.essentials.getUser((Player) sender).isMuted()) {
                    String msg = message.substring(index + 1);
                    Bukkit.broadcastMessage(String.format("* %s %s", sender instanceof Player ? ((Player) sender).getDisplayName() : sender.getName(), msg));
                    return true;
                } else {
                    sender.sendMessage("§cCan't use /minecraft:me while muted.");
                    return true;
                }
            } else if (cmd.equalsIgnoreCase("me")) { //Take over for Discord broadcast
                if (!(sender instanceof Player) || !PluginMain.essentials.getUser((Player) sender).isMuted()) {
                    String msg = message.substring(index + 1);
                    Bukkit.broadcastMessage(String.format("§5* %s %s", sender instanceof Player ? ((Player) sender).getDisplayName() : sender.getName(), msg));
                    return true;
                } else {
                    sender.sendMessage("§cCan't use /me while muted.");
                    return true;
                }
            } else
                for (Channel channel : Channel.getChannels()) {
					if (checkchid.test(channel, cmd)) { //Apparently method references don't require final variables
                        TBMCChatAPI.SendChatMessage(channel, sender, message.substring(index + 1));
                        return true;
                    }
                }
			// TODO: Target selectors
		}
		// We don't care if we have arguments
		if (cmd.toLowerCase().startsWith("un")) {
			for (HelpTopic ht : PluginMain.Instance.getServer().getHelpMap().getHelpTopics()) {
				if (ht.getName().equalsIgnoreCase("/" + cmd))
					return false;
			}
			if (PluginMain.permission.has(sender, "tbmc.admin")) {
				String s = cmd.substring(2);
				Player target = null;
				target = Bukkit.getPlayer(message.substring(index + 1));
				if (target == null) {
					sender.sendMessage("§cError: Player not found. (/un" + s + " <player>)");
					return true;
				}
				target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 10 * 20, 5, false, false));
				Bukkit.broadcastMessage(
						(sender instanceof Player ? ((Player) sender).getDisplayName() : sender.getName()) + " un" + s
								+ "'d " + target.getDisplayName());
				return true;
			}
		}
		return false;
	}

	@EventHandler
	public void onTabComplete(PlayerChatTabCompleteEvent e) {
		String name = e.getLastToken();
        for (Player p : Bukkit.getOnlinePlayers()) {
            String displayName = ChatColor.stripColor(p.getDisplayName());
            if (displayName.startsWith(name))
                e.getTabCompletions().add(displayName);
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
	public void onVotifierEvent(VotifierEvent event) { //TODO: Move to teh Core eh
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
		if (onCommandPreprocess(event.getSender(), event.getCommand()))
			event.setCommand("dontrunthiscmd");
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
		} catch (NoClassDefFoundError | Exception ex) { // Weird things can happen
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

	@EventHandler
	public void onNickChange(NickChangeEvent e) {
        //PlayerJoinLeaveListener.updatePlayerColors(e.getAffected().getBase()); //Won't fire this event again

        Bukkit.getScheduler().runTaskLater(PluginMain.Instance, () -> {
            PlayerJoinLeaveListener.updatePlayerColors(e.getAffected().getBase()); //TODO: Doesn't have effect
        }, 1);
	}
}
