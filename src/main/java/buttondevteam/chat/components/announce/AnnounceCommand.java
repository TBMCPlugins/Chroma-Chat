package buttondevteam.chat.components.announce;

import buttondevteam.chat.commands.ucmds.UCommandBase;
import buttondevteam.lib.chat.Command2;
import buttondevteam.lib.chat.CommandClass;
import buttondevteam.lib.chat.OptionallyPlayerCommandClass;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.bukkit.command.CommandSender;

@CommandClass(modOnly = true)
@OptionallyPlayerCommandClass(playerOnly = false)
@RequiredArgsConstructor
public class AnnounceCommand extends UCommandBase {
	private final AnnouncerComponent component;

	@Command2.Subcommand(helpText = {
		"Add announcement",
		"This command adds a new announcement",
	})
	public boolean add(CommandSender sender, @Command2.TextArg String text) {
		String finalmessage = text.replace('&', '§');
		component.AnnounceMessages().get().add(finalmessage);
		sender.sendMessage("§bAnnouncement added.§r");
		return true;
	}

	@Command2.Subcommand(helpText = {
		"Edit announcement",
		"This command lets you edit an announcement by its index.",
		"Shouldn't be used directly, use either command blocks or click on an announcement in /u announce list (WIP) instead." //TODO: <--
	})
	public boolean edit(CommandSender sender, byte index, @Command2.TextArg String text) {
		String finalmessage1 = text.replace('&', '§');
		if (index > 100)
			return false;
		while (component.AnnounceMessages().get().size() <= index)
			component.AnnounceMessages().get().add("");
		component.AnnounceMessages().get().set(index, finalmessage1);
		sender.sendMessage("Announcement edited.");
		return true;
	}

	@Command2.Subcommand(helpText = {
		"List announcements",
		"This command lists the announcements and the time between them"
	})
	public boolean list(CommandSender sender) {
		sender.sendMessage("§bList of announce messages:§r");
		sender.sendMessage("§bFormat: [index] message§r");
		int i = 0;
		for (String message : component.AnnounceMessages().get())
			sender.sendMessage("[" + i++ + "] " + message);
		sender.sendMessage("§bCurrent wait time between announcements: "
			+ component.AnnounceTime().get() / 60 / 1000 + " minute(s)§r");
		return true;
	}

	@Command2.Subcommand(helpText = {
		"Remove announcement",
		"This command removes an announcement"
	})
	public boolean remove(CommandSender sender, int index) {
		val msgs = component.AnnounceMessages().get();
		if (index < 0 || index > msgs.size()) return false;
		msgs.remove(index);
		sender.sendMessage("Announcement removed.");
		return true;
	}

	@Command2.Subcommand(helpText = {
		"Set time",
		"This command sets the time between the announcements"
	})
	public boolean settime(CommandSender sender, int minutes) {
		component.AnnounceTime().set(minutes * 60 * 1000);
		sender.sendMessage("Time set between announce messages to " + minutes + " minutes");
		return true;
	}
}
