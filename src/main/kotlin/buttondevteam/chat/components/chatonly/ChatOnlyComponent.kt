package buttondevteam.chat.components.chatonly;

import buttondevteam.chat.ChatPlayer;
import buttondevteam.chat.PluginMain;
import buttondevteam.core.ComponentManager;
import buttondevteam.lib.architecture.Component;
import buttondevteam.lib.architecture.ComponentMetadata;
import buttondevteam.lib.player.TBMCPlayer;
import lombok.val;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.event.HoverEvent.Action.SHOW_TEXT;
import static net.kyori.adventure.text.event.HoverEvent.hoverEvent;

/**
 * Allows players to enter chat-only mode which puts them into spectator mode and disallows moving.
 */
@ComponentMetadata(enabledByDefault = false)
public class ChatOnlyComponent extends Component<PluginMain> implements Listener {
	@Override
	protected void enable() {
		registerListener(this);
		registerCommand(new ChatonlyCommand());
	}

	@Override
	protected void disable() {

	}

	@EventHandler
	public void playerJoin(PlayerJoinEvent event) {
		val p = event.getPlayer();
		val cp = TBMCPlayer.getPlayer(p.getUniqueId(), ChatPlayer.class);
		if (cp.ChatOnly || p.getGameMode().equals(GameMode.SPECTATOR)) {
			cp.ChatOnly = false;
			p.setGameMode(GameMode.SURVIVAL);
		}
	}

	public static void tellrawCreate(ChatPlayer mp, TextComponent.Builder json) {
		if (!ComponentManager.isEnabled(ChatOnlyComponent.class))
			return;
		if (mp != null && mp.ChatOnly) {
			json.append(text("[C]").hoverEvent(hoverEvent(SHOW_TEXT, text("Chat only"))));
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
