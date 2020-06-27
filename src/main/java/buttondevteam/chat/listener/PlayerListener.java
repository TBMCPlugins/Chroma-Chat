package buttondevteam.chat.listener;

import buttondevteam.chat.ChatPlayer;
import buttondevteam.chat.ChatUtils;
import buttondevteam.chat.PluginMain;
import buttondevteam.chat.commands.ucmds.HistoryCommand;
import buttondevteam.chat.components.flair.FlairComponent;
import buttondevteam.chat.components.formatter.FormatterComponent;
import buttondevteam.chat.components.towncolors.TownColorComponent;
import buttondevteam.core.ComponentManager;
import buttondevteam.lib.TBMCChatEvent;
import buttondevteam.lib.TBMCCoreAPI;
import buttondevteam.lib.player.ChromaGamerBase.InfoTarget;
import buttondevteam.lib.player.TBMCPlayerGetInfoEvent;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.ess3.api.events.NickChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;

import java.util.Map.Entry;
import java.util.UUID;

public class PlayerListener implements Listener {
	/**
	 * Does not contain format codes, lowercased
	 */
	public static BiMap<String, UUID> nicknames = HashBiMap.create();

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		if (event.isCancelled())
			return;
		//The custom event is called in the core, but doesn't cancel the MC event
		if (ComponentManager.isEnabled(FormatterComponent.class)) //If not enabled, then let the other plugins deal with the message
			event.setCancelled(true); // The custom event should only be cancelled when muted or similar
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

	private long lastError = 0;

	@EventHandler
	public void onPlayerTBMCChat(TBMCChatEvent e) {
		try {
			if (e.isCancelled())
				return;
			HistoryCommand.addChatMessage(e.getCm(), e.getChannel());
			e.setCancelled(FormatterComponent.handleChat(e));
		} catch (NoClassDefFoundError | Exception ex) { // Weird things can happen
			if (lastError < System.nanoTime() - 1000L * 1000L * 1000L * 60 * 60 //60 mins
				&& Bukkit.getOnlinePlayers().size() > 0) { //If there are no players on, display to the first person
				lastError = System.nanoTime(); //I put the whole thing in the if to protect against race conditions
				for (Player p : Bukkit.getOnlinePlayers())
					if (e.shouldSendTo(p))
						p.sendMessage("[" + e.getChannel().DisplayName().get() + "] §cSome features in the message below might be unavailable due to an error.");
			}
			ChatUtils.sendChatMessage(e);
			TBMCCoreAPI.SendException("An error occured while processing a chat message!", ex);
		}
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
