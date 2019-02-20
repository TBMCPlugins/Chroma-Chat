package buttondevteam.chat.listener;

import buttondevteam.chat.ChatPlayer;
import buttondevteam.chat.ChatProcessing;
import buttondevteam.chat.PluginMain;
import buttondevteam.chat.commands.ucmds.HistoryCommand;
import buttondevteam.chat.components.flair.FlairComponent;
import buttondevteam.chat.components.fun.FunComponent;
import buttondevteam.chat.components.towncolors.TownColorComponent;
import buttondevteam.core.ComponentManager;
import buttondevteam.core.component.channel.Channel;
import buttondevteam.core.component.channel.ChatChannelRegisterEvent;
import buttondevteam.core.component.channel.ChatRoom;
import buttondevteam.lib.TBMCChatEvent;
import buttondevteam.lib.TBMCCoreAPI;
import buttondevteam.lib.chat.ChatMessage;
import buttondevteam.lib.chat.TBMCChatAPI;
import buttondevteam.lib.player.ChromaGamerBase;
import buttondevteam.lib.player.ChromaGamerBase.InfoTarget;
import buttondevteam.lib.player.TBMCPlayer;
import buttondevteam.lib.player.TBMCPlayerGetInfoEvent;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import lombok.val;
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

import java.util.Arrays;
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;
import java.util.function.BiPredicate;

public class PlayerListener implements Listener {
	/**
	 * Does not contain format codes, lowercased
	 */
	public static BiMap<String, UUID> nicknames = HashBiMap.create();

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		if (event.isCancelled())
			return;
		ChatPlayer cp = TBMCPlayer.getPlayer(event.getPlayer().getUniqueId(), ChatPlayer.class);
		TBMCChatAPI.SendChatMessage(ChatMessage.builder(event.getPlayer(), cp, event.getMessage()).build());
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
		val mp = ChromaGamerBase.getFromSender(sender);
		String cmd;
		final BiPredicate<Channel, String> checkchid = (chan, cmd1) -> cmd1.equalsIgnoreCase(chan.ID) || (Arrays.stream(chan.IDs().get()).anyMatch(cmd1::equalsIgnoreCase));
		if (index == -1) { // Only the command is run
			if (!(sender instanceof Player || sender instanceof ConsoleCommandSender))
				return false;
			// ^^ We can only store player or console channels - Directly sending to channels would still work if they had an event
			cmd = sender instanceof ConsoleCommandSender ? message : message.substring(1);
			for (Channel channel : ((Iterable<Channel>) Channel.getChannels()::iterator)) { //Using Stream.forEach would be too easy
				if (checkchid.test(channel, cmd)) {
					Channel oldch = mp.channel().get();
					if (oldch instanceof ChatRoom)
						((ChatRoom) oldch).leaveRoom(sender);
					if (oldch.equals(channel))
						mp.channel().set(Channel.GlobalChat);
					else {
						mp.channel().set(channel);
						if (channel instanceof ChatRoom)
							((ChatRoom) channel).joinRoom(sender);
					}
					sender.sendMessage("§6You are now talking in: §b" + mp.channel().get().DisplayName().get());
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
				for (Channel channel : (Iterable<Channel>) Channel.getChannels()::iterator) {
					if (checkchid.test(channel, cmd)) { //Apparently method references don't require final variables
						TBMCChatAPI.SendChatMessage(ChatMessage.builder(sender, mp, message.substring(index + 1)).build(), channel);
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
				Player target = Bukkit.getPlayer(message.substring(index + 1));
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
		for (Entry<String, UUID> nicknamekv : nicknames.entrySet()) {
			if (nicknamekv.getKey().startsWith(name.toLowerCase()))
                e.getTabCompletions().add(PluginMain.essentials.getUser(Bukkit.getPlayer(nicknamekv.getValue())).getNick(true)); //Tabcomplete with the correct case
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
			if (ComponentManager.isEnabled(FlairComponent.class)) {
				final String flair = cp.GetFormattedFlair(e.getTarget() != InfoTarget.MCCommand);
				if (flair.length() > 0)
					e.addInfo("/r/TheButton flair: " + flair);
			}
            e.addInfo(String.format("Respect: %.2f", cp.getF()));
		} catch (Exception ex) {
			TBMCCoreAPI.SendException("Error while providing chat info for player " + e.getPlayer().getFileName(), ex);
		}
	}

	@EventHandler
	public void onPlayerTBMCChat(TBMCChatEvent e) {
		try {
			if (e.isCancelled())
				return;
			HistoryCommand.addChatMessage(e.getCm(), e.getChannel());
			e.setCancelled(ChatProcessing.ProcessChat(e));
		} catch (NoClassDefFoundError | Exception ex) { // Weird things can happen
			for (Player p : Bukkit.getOnlinePlayers())
				if (e.shouldSendTo(p))
					p.sendMessage("§c!§r["
						+ e.getChannel().DisplayName().get() + "] <" + (e.getSender() instanceof Player
							? ((Player) e.getSender()).getDisplayName() : e.getSender().getName())
							+ "> " + e.getMessage());
			TBMCCoreAPI.SendException("An error occured while processing a chat message!", ex);
		}
	}

	@EventHandler
	public void onChannelRegistered(ChatChannelRegisterEvent e) {
		if (!e.getChannel().isGlobal() && PluginMain.SB.getObjective(e.getChannel().ID) == null) // Not global chat and doesn't exist yet
			PluginMain.SB.registerNewObjective(e.getChannel().ID, "dummy");
	}

	@EventHandler
	public void onNickChange(NickChangeEvent e) {
        String nick = e.getValue();
        if (nick == null)
            nicknames.inverse().remove(e.getAffected().getBase().getUniqueId());
        else
            nicknames.inverse().forcePut(e.getAffected().getBase().getUniqueId(), ChatColor.stripColor(nick).toLowerCase());

		Bukkit.getScheduler().runTaskLater(PluginMain.Instance, () -> {
			TownColorComponent.updatePlayerColors(e.getAffected().getBase()); //Won't fire this event again
		}, 1);
	}
}
