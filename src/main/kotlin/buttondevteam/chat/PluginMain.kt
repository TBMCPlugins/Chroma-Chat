package buttondevteam.chat

import buttondevteam.chat.commands.MWikiCommand
import buttondevteam.chat.commands.MeCommand
import buttondevteam.chat.commands.SnapCommand
import buttondevteam.chat.commands.ucmds.HelpCommand
import buttondevteam.chat.commands.ucmds.HistoryCommand
import buttondevteam.chat.commands.ucmds.InfoCommand
import buttondevteam.chat.commands.ucmds.ReloadCommand
import buttondevteam.chat.commands.ucmds.admin.DebugCommand
import buttondevteam.chat.components.announce.AnnouncerComponent
import buttondevteam.chat.components.appendext.AppendTextComponent
import buttondevteam.chat.components.formatter.FormatterComponent
import buttondevteam.chat.components.`fun`.FunComponent
import buttondevteam.chat.components.towncolors.TownColorComponent
import buttondevteam.chat.listener.PlayerJoinLeaveListener
import buttondevteam.chat.listener.PlayerListener
import buttondevteam.lib.TBMCCoreAPI
import buttondevteam.lib.architecture.ButtonPlugin
import buttondevteam.lib.architecture.Component
import buttondevteam.lib.chat.TBMCChatAPI
import com.earth2me.essentials.Essentials
import net.kyori.adventure.text.format.NamedTextColor

class PluginMain : ButtonPlugin() {
    /**
     * If enabled, stores and displays the last 10 messages the player can see (public, their town chat etc.)
     * Can be used with the Discord plugin so players can see some of the conversation they missed that's visible on Discord anyways.
     */
    var storeChatHistory by iConfig.getData("storeChatHistory", true)

    // Fired when plugin is first enabled
    public override fun pluginEnable() {
        Instance = this
        essentials = (org.bukkit.Bukkit.getPluginManager().getPlugin("Essentials")) as Essentials?

        TBMCCoreAPI.RegisterEventsForExceptions(PlayerListener(), this)
        TBMCCoreAPI.RegisterEventsForExceptions(PlayerJoinLeaveListener(), this)
        Console = this.server.consoleSender

        if (org.bukkit.Bukkit.getPluginManager().isPluginEnabled("Towny"))
            Component.registerComponent<PluginMain>(
                this,
                buttondevteam.chat.components.towny.TownyComponent()
            )

        TBMCChatAPI.registerChatChannel(
            buttondevteam.core.component.channel.Channel(
                "§7RP§f",
                NamedTextColor.GRAY,
                "rp",
                null
            )
        ) //Since it's null, it's recognised as global

        if (!setupPermissions()) TBMCCoreAPI.SendException(
            "We're in trouble",
            Exception("Failed to set up permissions!"),
            this
        )

        if (org.bukkit.Bukkit.getPluginManager().isPluginEnabled("Towny"))
            Component.registerComponent<PluginMain>(this, TownColorComponent())
        Component.registerComponent<PluginMain>(this, AnnouncerComponent())
        Component.registerComponent<PluginMain>(this, FunComponent())
        Component.registerComponent<PluginMain>(this, AppendTextComponent())
        Component.registerComponent<PluginMain>(this, FormatterComponent())
        registerCommand(DebugCommand())
        registerCommand(HelpCommand())
        registerCommand(HistoryCommand())
        registerCommand(InfoCommand())
        registerCommand(MWikiCommand())
        registerCommand(ReloadCommand())
        registerCommand(SnapCommand())
        registerCommand(MeCommand())
    }

    // Fired when plugin is disabled
    public override fun pluginDisable() {
    }

    private fun setupPermissions(): Boolean {
        val permissionProvider = server.servicesManager
            .getRegistration<net.milkbowl.vault.permission.Permission?>(net.milkbowl.vault.permission.Permission::class.java)
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider()
        }
        return (permission != null)
    }

    companion object {
        // Translated to Java: 2015.07.15.
        var Instance: PluginMain? = null
        var Console: org.bukkit.command.ConsoleCommandSender? = null

        var essentials: Essentials? = null

        @JvmField
        var permission: net.milkbowl.vault.permission.Permission? = null
    }
}
