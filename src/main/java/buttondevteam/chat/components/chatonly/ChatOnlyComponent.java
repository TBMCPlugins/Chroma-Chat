package buttondevteam.chat.components.chatonly;

import buttondevteam.chat.ChatPlayer;
import buttondevteam.chat.components.formatter.formatting.TellrawEvent;
import buttondevteam.chat.components.formatter.formatting.TellrawPart;
import buttondevteam.core.ComponentManager;
import buttondevteam.lib.architecture.Component;
import buttondevteam.lib.architecture.ComponentMetadata;
import buttondevteam.lib.player.TBMCPlayer;
import buttondevteam.lib.player.TBMCPlayerJoinEvent;
import lombok.val;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 * Allows players to enter chat-only mode which puts them into spectator mode and disallows moving.
 */
@ComponentMetadata(enabledByDefault = false)
public class ChatOnlyComponent extends Component implements Listener {
	@Override
	protected void enable() {
		registerListener(this);
		registerCommand(new ChatonlyCommand());
	}

	@Override
	protected void disable() {

	}

	@EventHandler
	public void playerJoin(TBMCPlayerJoinEvent event) {
		val p = event.getPlayer();
		val cp = event.GetPlayer().asPluginPlayer(ChatPlayer.class);
		if (cp.ChatOnly || p.getGameMode().equals(GameMode.SPECTATOR)) {
			cp.ChatOnly = false;
			p.setGameMode(GameMode.SURVIVAL);
		}
	}

	public static void tellrawCreate(ChatPlayer mp, TellrawPart json) {
		if(!ComponentManager.isEnabled(ChatOnlyComponent.class))
			return;
		if (mp != null && mp.ChatOnly) {
			json.addExtra(new TellrawPart("[C]")
				.setHoverEvent(TellrawEvent.create(TellrawEvent.HoverAction.SHOW_TEXT, "Chat only")));
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
}
