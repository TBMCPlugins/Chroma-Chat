package buttondevteam.chat.components.chatonly

import buttondevteam.lib.architecture.ComponentMetadata
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.HoverEvent
import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerTeleportEvent

/**
 * Allows players to enter chat-only mode which puts them into spectator mode and disallows moving.
 */
@ComponentMetadata(enabledByDefault = false)
class ChatOnlyComponent : buttondevteam.lib.architecture.Component<PluginMain?>(), Listener {
    override fun enable() {
        registerListener(this)
        registerCommand(ChatonlyCommand())
    }

    override fun disable() {
    }

    @EventHandler
    fun playerJoin(event: PlayerJoinEvent) {
        val p = event.getPlayer()
        val cp = TBMCPlayerBase.Companion.getPlayer<T>(p.getUniqueId(), ChatPlayer::class.java)
        if (cp.ChatOnly || p.getGameMode().equals(GameMode.SPECTATOR)) {
            cp.ChatOnly = false
            p.setGameMode(GameMode.SURVIVAL)
        }
    }

    @EventHandler
    fun onPlayerMove(e: PlayerMoveEvent) {
        val mp: ChatPlayer =
            TBMCPlayerBase.Companion.getPlayer<ChatPlayer>(e.getPlayer().getUniqueId(), ChatPlayer::class.java)
        if (mp.ChatOnly) e.setCancelled(true)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onPlayerTeleport(e: PlayerTeleportEvent) {
        if (TBMCPlayerBase.Companion.getPlayer<ChatPlayer?>(
                e.getPlayer().getUniqueId(),
                ChatPlayer::class.java
            ).ChatOnly
        ) {
            e.setCancelled(true)
            e.getPlayer().sendMessage("§cYou are not allowed to teleport while in chat-only mode.")
        }
    }

    companion object {
        fun tellrawCreate(mp: ChatPlayer?, json: TextComponent.Builder) {
            if (!isEnabled(ChatOnlyComponent::class.java)) return
            if (mp != null && mp.ChatOnly) {
                json.append(
                    Component.text("[C]").hoverEvent(
                        HoverEvent.hoverEvent<Component>(
                            HoverEvent.Action.SHOW_TEXT,
                            Component.text("Chat only")
                        )
                    )
                )
            }
        }
    }
}
