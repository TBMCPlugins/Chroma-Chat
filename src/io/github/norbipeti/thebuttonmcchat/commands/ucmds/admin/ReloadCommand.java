package io.github.norbipeti.thebuttonmcchat.commands.ucmds.admin;

import org.bukkit.command.CommandSender;

public final class ReloadCommand extends AdminCommandBase {

	@Override
	public String[] GetHelpText(String alias) {
		return new String[] {
				"§6---- Reload plugin ----",
				"This command allows you to reload the plugin's config",
				"This isn't the same as reloading the server, and should not cause any issues with other plugins",
				"Save the config by using /u admin save before you reload it",
				"Because of this, you'll need to confirm the reload with /u admin confirm" };
	}

	public static CommandSender Reloader;

	@Override
	public boolean OnCommand(CommandSender sender, String alias,
			String[] args) {
		Reloader = sender;
		sender.sendMessage("§bMake sure to save the current settings before you modify and reload them! Type /u admin confirm when ready.§r");
		return true;
	}

	@Override
	public String GetAdminCommandPath() {
		return "reload";
	}

}
